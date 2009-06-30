/**
 * Copyright 2009  Ibrahim CHAEHOI
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
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.ServletContextResourceHandler;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;

import org.apache.log4j.Logger;

/**
 * Image Request handling class. Jawr image servlet delegates to this class to handle requests.
 * 
 * @author Ibrahim CHAEHOI
 */
public class JawrImageRequestHandler extends JawrRequestHandler {

	/** The logger */
	private static final Logger log = Logger.getLogger(JawrImageRequestHandler.class);

	/** The cache buster patter */
	private static Pattern cacheBusterPattern = Pattern.compile("("+JawrConstant.CACHE_BUSTER_PREFIX+"|"+JawrConstant.CLASSPATH_CACHE_BUSTER_PREFIX+")[a-zA-Z0-9]+/(.*)$");

	/** The cache buster replace pattern */
	private static final String CACHE_BUSTER_REPLACE_PATTERN = "$2";

	/** The resource handler */
	private ResourceHandler rsHandler;
	

	/**
	 * Reads the properties file and initializes all configuration using the ServletConfig object. If aplicable, a ConfigChangeListenerThread will be
	 * started to listen to changes in the properties configuration.
	 * 
	 * @param servletContext ServletContext
	 * @param servletConfig ServletConfig
	 * @throws ServletException
	 */
	public JawrImageRequestHandler(ServletContext context, ServletConfig config) throws ServletException {
		super(context, config);
		resourceType = "img";
	}

	/**
	 * Alternate constructor that does not need a ServletConfig object. Parameters normally read rom it are read from the initParams Map, and the
	 * configProps are used instead of reading a .properties file.
	 * 
	 * @param servletContext ServletContext
	 * @param servletConfig ServletConfig
	 * @throws ServletException
	 */
	public JawrImageRequestHandler(ServletContext context, Map initParams, Properties configProps) throws ServletException {

		super(context, initParams, configProps);
	}

	/**
	 * Initialize the Jawr config
	 * 
	 * @param props the properties
	 * @throws ServletException if an exception occurs
	 */
	protected void initializeJawrConfig(Properties props) throws ServletException {
		// Initialize config
		if (null != jawrConfig)
			jawrConfig.invalidate();

		jawrConfig = new JawrConfig(props);
		jawrConfig.setContext(servletContext);
		jawrConfig.setGeneratorRegistry(generatorRegistry);

		// Set the content type to be used for every request.
		contentType = "img";

		// Set mapping, to be used by the tag lib to define URLs that point to this servlet. 
		String mapping = (String) initParameters.get("mapping");
		if(null != mapping){
			jawrConfig.setServletMapping(mapping);
		}
		
		rsHandler = new ServletContextResourceHandler(servletContext,jawrConfig.getResourceCharset(),jawrConfig.getGeneratorRegistry());
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(jawrConfig);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		if (log.isDebugEnabled()) {
			log.debug("Image Configuration read. Current config:");
			log.debug(jawrConfig);
		}

		// Warn when in debug mode
		if (jawrConfig.isDebugModeOn()) {
			log.warn("Jawr initialized in DEVELOPMENT MODE. Do NOT use this mode in production or integration servers. ");
		}
		
	}

	/**
	 * Handles a resource request by getting the requested path from the request object and invoking processRequest.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String requestedPath = "".equals(jawrConfig.getServletMapping()) ? request.getServletPath() : request.getPathInfo();
		processRequest(requestedPath, request, response);
	}

	/**
	 * Handles a resource request.
	 * <ul>
	 * <li>If the request contains an If-Modified-Since header, the 304 status is set and no data is written to the response</li>
	 * <li>If the requested path begins with the gzip prefix, a gzipped version of the resource is served, with the corresponding content-encoding
	 * header.</li>
	 * <li>Otherwise, the resource is written as text to the response.</li>
	 * <li>If the resource is not found, the response satus is set to 404 and no response is written.</li>
	 * </ul>
	 * 
	 * @param requestedPath
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void processRequest(String requestedPath, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if (log.isDebugEnabled())
			log.debug("Request received for path:" + requestedPath);

		// Retrieve the file path
		String filePath = getFilePath(request);
		if (filePath == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// Retrieve the content type
		String imgContentType = getContentType(request, filePath);
		if (imgContentType == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// Set the content type
		response.setContentType(imgContentType);
		// If debug mode is off, check for If-Modified-Since and If-none-match headers and set response caching headers.
		if (!this.jawrConfig.isDebugModeOn()) {
			// If a browser checks for changes, always respond 'no changes'.
			if (null != request.getHeader(IF_MODIFIED_SINCE_HEADER) || null != request.getHeader(IF_NONE_MATCH_HEADER)) {
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				if (log.isDebugEnabled())
					log.debug("Returning 'not modified' header. ");
				return;
			}

			// Add caching headers
			setResponseHeaders(response);
		}

		boolean isClasspathImg = isClasspathImage(filePath);
		
		// Returns the real file path
		filePath = getRealFilePath(filePath);
		
		// Read the file from the classpath and send it in the outputStream
		try {
			writeContent(response, filePath, isClasspathImg);

			// Set the content length, and the content type based on the file extension
			response.setContentType(contentType);

		} catch (Exception ex) {

			log.error("Unable to load the image for the request URI : " + request.getRequestURI(), ex);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}

		if (log.isDebugEnabled())
			log.debug("request succesfully attended");
	}

	/**
	 * Returns the content type for the image
	 * 
	 * @param request the request
	 * @param filePath the image file path
	 * @return the content type of the image
	 */
	private String getContentType(HttpServletRequest request, String filePath) {
		String requestUri = request.getRequestURI();

		// Retrieve the extension
		String extension = getExtension(filePath);
		if (extension == null) {

			log.error("No extension found for the request URI : " + requestUri);
			return null;
		}

		String contentType = (String) imgMimeMap.get(extension);
		if (contentType == null) {

			log.error("No image extension match the extension '" + extension + "' for the request URI : " + requestUri);
			return null;
		}
		return contentType;
	}

	/**
	 * Returns the file path
	 * 
	 * @param request the request
	 * @return the image file path
	 */
	private String getFilePath(HttpServletRequest request) {
		
		String requestedPath = "".equals(jawrConfig.getServletMapping()) ? request.getServletPath() : request.getPathInfo();
		
		// Return the file path requested
		return requestedPath;
	}

	/**
	 * Write the image content to the response
	 * 
	 * @param response the response
	 * @param fileName the filename
	 * @throws IOException if an IO exception occurs.
	 */
	private void writeContent(HttpServletResponse response, String fileName, boolean fromClasspath) throws IOException {

		int length = 0;

		OutputStream os = response.getOutputStream();
		InputStream is = null;
		
		if(fromClasspath){
			if(fileName.startsWith("/")){
				fileName = fileName.substring(1);
			}
			is = ClassLoaderResourceUtils.getResourceAsStream(fileName, this);
		}else{
			try {
				is = rsHandler.getResourceAsStream(fileName);
			} catch (ResourceNotFoundException e) {
				// Nothing to do
			}
		}
		
		if(is == null){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			if (log.isInfoEnabled())
				log.info("Received a request for a non existing image resource: " + fileName);
			return;
		}
		
		IOUtils.copy(is, os, true);
		
		response.setContentLength(length);
	}

	/**
	 * Removes the cache buster
	 * 
	 * @param fileName the file name
	 * @return the file name without the cache buster.
	 */
	private String getRealFilePath(String fileName) {

		if(fileName.startsWith("/")){
			fileName = fileName.substring(1);
		}
		Matcher matcher = cacheBusterPattern.matcher(fileName);
		StringBuffer result = new StringBuffer();
		if (matcher.find()) {
			matcher.appendReplacement(result, CACHE_BUSTER_REPLACE_PATTERN);
			return result.toString();
		}

		return fileName;
	}

	/**
	 * Returns true if the file is a class path image
	 * @param fileName the file name
	 * @return true if the file is a class path image
	 */
	private boolean isClasspathImage(String fileName){
		if(fileName.startsWith("/")){
			fileName = fileName.substring(1);
		}
		
		return fileName.startsWith(JawrConstant.CLASSPATH_CACHE_BUSTER_PREFIX);
	}
	
}
