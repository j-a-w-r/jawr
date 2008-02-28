/**
 * Copyright 2007  Jordi Hernández Sellés
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
import net.jawr.web.resource.bundle.factory.util.ConfigChangeListener;
import net.jawr.web.resource.bundle.factory.util.ConfigChangeListenerThread;
import net.jawr.web.resource.bundle.factory.util.ConfigPropertiesSource;
import net.jawr.web.resource.bundle.factory.util.PropsFilePropertiesSource;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;

import org.apache.log4j.Logger;

/**
 * Request handling class. Any jawr enabled servlet delegates to this class to handle requests. 
 * 
 * @author Jordi Hernández Sellés
 */
public class JawrRequestHandler implements ConfigChangeListener{
	
    private static final String CACHE_CONTROL_HEADER = "Cache-Control";
    private static final String CACHE_CONTROL_VALUE = "public, max-age=315360000, post-check=315360000, pre-check=315360000";
    private static final String LAST_MODIFIED_HEADER = "Last-Modified";
    private static final String LAST_MODIFIED_VALUE = "Sun, 06 Nov 2005 12:00:00 GMT";
    private static final String ETAG_HEADER = "ETag";
    private static final String ETAG_VALUE = "2740050219";
    private static final String EXPIRES_HEADER = "Expires";
    
    private static final String CONFIG_RELOAD_INTERVAL = "jawr.config.reload.interval";
    
	private static final Logger log = Logger.getLogger(JawrRequestHandler.class.getName());
	
	private ResourceBundlesHandler bundlesHandler;
	
	private String contentType;
	private String resourceType;
	private ServletContext servletContext;
	private ServletConfig servletConfig;
	
	private JawrConfig jawrConfig;

	/**
	 * Reads the properties file and  initializes all configuration. 
	 * @param servletContext ServletContext
	 * @param servletConfig ServletConfig 
	 * @throws ServletException
	 */
	public JawrRequestHandler(ServletContext context, ServletConfig config) throws ServletException{
		if(log.isInfoEnabled())
			log.info("Initializing jawr config for servlet named " + config.getServletName());
		
		long initialTime = System.currentTimeMillis();
		this.servletContext = context;
		this.servletConfig = config;

		resourceType = servletConfig.getInitParameter("type");
		resourceType = null == resourceType ? "js" : resourceType;
		
		String configLocation = servletConfig.getInitParameter("configLocation");
		String configPropsSourceClass = servletConfig.getInitParameter("configPropertiesSourceClass");
		if(null == configLocation && null == configPropsSourceClass) 
			throw new ServletException("Neither configLocation nor configPropertiesSourceClass init params were set."
										+" You must set at least the configLocation param. Please check your web.xml file");
		
		// Initialize the config properties source that will provide with all configuration options. 
		ConfigPropertiesSource propsSrc;
		
		// Load a custom class to set config properties
		if(null != configPropsSourceClass) {
			try {
				Class clazz = Class.forName(configPropsSourceClass);
				propsSrc = (ConfigPropertiesSource) clazz.newInstance();
				
			} catch (Exception e) {
				throw new ServletException(e.getMessage() 
											+ " [The custom property source class " 
											+ configPropsSourceClass 
											+ " could not be instantiated, check wether it is available on the classpath and" 
											+ " that it has a zero-arg constructor]");
			}
		}
		else {
			// Default config properties source, reads from a .properties file in the classpath. 
			propsSrc = new PropsFilePropertiesSource();
		}

		// If a custom properties source is a subclass of PropsFilePropertiesSource, we hand it the configLocation param. 
		// This affects the standard one as well. 
		if(propsSrc instanceof PropsFilePropertiesSource)
			((PropsFilePropertiesSource) propsSrc).setConfigLocation(configLocation);
		
		
		// Read properties from properties source
		Properties props = propsSrc.getConfigProperties();
		
		// Initialize config 
		initializeJawrConfig(props);
		
		// Initialize the properties reloading checker daemon if specified
		if(null != props.getProperty(CONFIG_RELOAD_INTERVAL))
		{	
			int interval = Integer.valueOf(props.getProperty(CONFIG_RELOAD_INTERVAL)).intValue();
			log.warn("Jawr started with configuration auto reloading on. " 
					+ "Be aware that a daemon thread will be checking for changes to configuration every " + interval + " seconds.");
			
			new ConfigChangeListenerThread(propsSrc,this, interval).start();
		}	
		

		if(log.isInfoEnabled()) {
			long totaltime = System.currentTimeMillis() - initialTime;
			log.info("Init method sucesful. jawr started in " + (totaltime/1000) + " seconds....");
		}

	}


	/**
	 * @param props
	 * @throws ServletException
	 */
	private void initializeJawrConfig(Properties props) throws ServletException {
		// Initialize config 
		jawrConfig = new JawrConfig(props);

		// Set the content type to be used for every request. 
		contentType = "text/";
		contentType += "js".equals(resourceType) ? "javascript" : "css";		
		contentType += "; charset=" + jawrConfig.getResourceCharset().name();		
		
		// Set mapping, to be used by the tag lib to define URLs that point to this servlet. 
		String mapping = servletConfig.getInitParameter("mapping");
		if(null != mapping)
			jawrConfig.setServletMapping(mapping);
		
		if(log.isDebugEnabled()) {
			log.debug("Configuration read. Current config:");
			log.debug(jawrConfig);
		}
		
		// Create a resource handler to read files from the WAR archive or exploded dir. 
		ResourceHandler rsHandler;
		
		rsHandler = new ServletContextResourceHandler(servletContext,jawrConfig.getResourceCharset());
		PropertiesBasedBundlesHandlerFactory factory = new PropertiesBasedBundlesHandlerFactory(props,resourceType,rsHandler);
		try {
			bundlesHandler = factory.buildResourceBundlesHandler(jawrConfig);
		} catch (DuplicateBundlePathException e) {
			throw new ServletException(e);
		}
		
		if(resourceType.equals("js"))
			servletContext.setAttribute(ResourceBundlesHandler.JS_CONTEXT_ATTRIBUTE, bundlesHandler);
		else servletContext.setAttribute(ResourceBundlesHandler.CSS_CONTEXT_ATTRIBUTE, bundlesHandler);
		
		
		if(log.isDebugEnabled()) {
			log.debug("content type set to: " + contentType);
			log.debug(bundlesHandler);
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
		
		
		if(log.isDebugEnabled())
			log.debug("Request received for path:" + requestedPath);
		
        // If a browser checks for changes, always respond 'no changes'. 
        if(null != request.getHeader("If-Modified-Since")) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            if(log.isDebugEnabled())
				log.debug("Returning 'not modified' header. ");
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


	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.factory.util.ConfigChangeListener#configChanged(java.util.Properties)
	 */
	public synchronized void configChanged(Properties newConfig) {
		if(log.isDebugEnabled())
			log.debug("Reloading Jawr configuration");
		try {
			initializeJawrConfig(newConfig);
		} catch (ServletException e) {
			throw new RuntimeException("Error reloading Jawr config: " + e.getMessage());
		}
		if(log.isDebugEnabled())
			log.debug("Jawr configuration succesfully reloaded. ");
		
	}
}
