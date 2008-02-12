/**
 * Copyright 2007 Jordi Hernández Sellés
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.jawr.web.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Calendar;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.ServletContextResourceHandler;
import net.jawr.web.resource.bundle.factory.PropertiesBasedBundlesHandlerFactory;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;

import org.apache.log4j.Logger;

/**
 * Request handling class. Any jawr enabled servlet delegates to this class to handle requests. 
 * 
 * @author Jordi Hernández Sellés
 */
public class JawrRequestHandler {
	
    private static final String CACHE_CONTROL_HEADER = "Cache-Control";
    private static final String CACHE_CONTROL_VALUE = "public, max-age=315360000, post-check=315360000, pre-check=315360000";
    private static final String LAST_MODIFIED_HEADER = "Last-Modified";
    private static final String LAST_MODIFIED_VALUE = "Sun, 06 Nov 2005 12:00:00 GMT";
    private static final String ETAG_HEADER = "ETag";
    private static final String ETAG_VALUE = "2740050219";
    private static final String EXPIRES_HEADER = "Expires";
    
	private static final Logger log = Logger.getLogger(JawrRequestHandler.class.getName());
	
	private ResourceBundlesHandler bundlesHandler;
	
	private String contentType;
	
	private final JawrConfig jawrConfig;

	/**
	 * Reads the properties file and  initializes all configuration. 
	 * @param context ServletContext
	 * @param config ServletConfig 
	 * @throws ServletException
	 */
	public JawrRequestHandler(ServletContext context, ServletConfig config) throws ServletException{
		if(log.isInfoEnabled())
			log.info("Initializing jawr config for servlet named " + config.getServletName());
		
		long initialTime = System.currentTimeMillis();
		
		String configLocation = config.getInitParameter("configLocation");
		if(null == configLocation) 
			throw new ServletException("configLocation init param not found. Check your web.xml file");
		
		String resourceType = config.getInitParameter("type");
		resourceType = null == resourceType ? "js" : resourceType;
		
		
		// Read properties from config file
		Properties props = readConfigProperties(configLocation);
		
		// Initialize config 
		jawrConfig = new JawrConfig(props);

		// Set the content type to be used for every request. 
		contentType = "text/";
		contentType += "js".equals(resourceType) ? "javascript" : "css";
		
		contentType += "; charset=" + jawrConfig.getResourceCharset().name();		
		
		// Set mapping, to be used by the tag lib to define URLs that point to this servlet. 
		String mapping = config.getInitParameter("mapping");
		if(null != mapping)
			jawrConfig.setServletMapping(mapping);
		
		if(log.isInfoEnabled()) {
			log.info("Configuration read. Current config:");
			log.info(jawrConfig);
		}
		
		// Create a resource handler to read files from the WAR archive or exploded dir. 
		ResourceHandler rsHandler;
		
		rsHandler = new ServletContextResourceHandler(context,jawrConfig.getResourceCharset());
		PropertiesBasedBundlesHandlerFactory factory = new PropertiesBasedBundlesHandlerFactory(props,resourceType,rsHandler);
		try {
			bundlesHandler = factory.buildResourceBundlesHandler(jawrConfig);
		} catch (DuplicateBundlePathException e) {
			throw new ServletException(e);
		}
		
		if(resourceType.equals("js"))
			context.setAttribute(ResourceBundlesHandler.JS_CONTEXT_ATTRIBUTE, bundlesHandler);
		else context.setAttribute(ResourceBundlesHandler.CSS_CONTEXT_ATTRIBUTE, bundlesHandler);
		
		long totaltime = System.currentTimeMillis() - initialTime;
		
		if(log.isInfoEnabled()) {
			log.info("content type set to: " + contentType);
			log.info(bundlesHandler);
			log.info("Init method sucesful. jawr started in " + (totaltime/1000) + " seconds.");
		}
		
		// Warn when in debug mode
		if(jawrConfig.isDebugModeOn()){
			log.warn("Jawr initialized in DEVELOPMENT MODE. Do NOT use this mode in production or integration servers. ");
		}
			
	}
	
	
	/**
	 * Handles a resource request. 
	 * <ul>
	 * <li>If the request contains an If-Modified-Since header, the 304 status is set and no data is written to the response </li>
	 * <li>If the requested path begins with the gzip prefix, a gzipped version of the resource is served, 
	 * 		with the corresponding content-encoding header.  </li>
	 * <li>Otherwise, the resource is written as text to the response. </li>
	 * <li>If the resource is not found, the response satus is set to 404 and no response is written. </li>
	 * </ul>
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String requestedPath = "".equals(jawrConfig.getServletMapping()) ? request.getServletPath() : request.getPathInfo();
		
		
		if(log.isInfoEnabled())
			log.info("Request received for path:" + requestedPath);
		
        // If a browser checks for changes, always respond 'no changes'. 
        if(null != request.getHeader("If-Modified-Since")) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            if(log.isInfoEnabled())
				log.info("Returning 'not modified' header. ");
            return;
        }
        
        // Add caching headers
        setResponseHeaders(response);
               
		// By setting content type, the response writer will use appropiate encoding
		response.setContentType(contentType);
		        
		
		try {
			// Send gzipped resource if user agent supports it. 
			if(requestedPath.startsWith(BundleRenderer.GZIP_PATH_PREFIX) ) {
			    requestedPath = "/" + requestedPath.substring(BundleRenderer.GZIP_PATH_PREFIX.length(),requestedPath.length());
				response.setHeader("Content-Encoding", "gzip");
				bundlesHandler.streamBundleTo(requestedPath, response.getOutputStream());
			}
			else {
				Writer out = response.getWriter();
				bundlesHandler.writeBundleTo(requestedPath, out);
			}
		} catch (ResourceNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			if(log.isInfoEnabled())
				log.info("Received a request for a non existing bundle: " + requestedPath);
			return;
		}
		
		if(log.isDebugEnabled())
			log.debug("request succesfully attended");
	}

	/**
	 * Reads the config properties file. 
	 * @param configLocation
	 * @return
	 * @throws ServletException
	 */
	private Properties readConfigProperties(String configLocation)
			throws ServletException {
		
		if(log.isInfoEnabled())
			log.info("Reading properties from file at classpath: " + configLocation);
		
		Properties props = new Properties();	
		
		// Load properties file
		try {	
			InputStream is = getClass().getResourceAsStream(configLocation);
			if(null == is)
				throw new ServletException("jawr configuration could not be found. "
						+ "Make sure init-param configLocation is properly set "
						+ "in web.xml and that it points to a file in the classpath. ");

			// load properties into a Properties object
			props.load(is);
		} 
		catch (IOException e) {
			throw new ServletException("jawr configuration could not be found. "
										+ "Make sure init-param configLocation is properly set "
										+ "in web.xml and that it points to a file in the classpath. ",e);
		}
		return props;
	}

	/**
     * Adds aggresive caching headers to the response in order to prevent browsers requesting the same file
     * twice. 
     * @param resp 
     */
    private void setResponseHeaders( HttpServletResponse resp ) {
            // Force resource caching as best as possible
            resp.setHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_VALUE);                
            resp.setHeader(LAST_MODIFIED_HEADER,LAST_MODIFIED_VALUE);
            resp.setHeader(ETAG_HEADER,ETAG_VALUE);
            Calendar cal = Calendar.getInstance();
            cal.roll(Calendar.YEAR,10);
            resp.setDateHeader(EXPIRES_HEADER, cal.getTimeInMillis());
    }
}
