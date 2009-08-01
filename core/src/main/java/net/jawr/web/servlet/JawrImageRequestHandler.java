/**
 * Copyright 2009  Ibrahim Chaehoi
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.InvalidPathException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.bundle.CheckSumUtils;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;

import org.apache.log4j.Logger;

/**
 * Image Request handling class. Jawr image servlet delegates to this class to handle requests.
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrImageRequestHandler extends JawrRequestHandler {

	/** The logger */
	private static final Logger log = Logger.getLogger(JawrImageRequestHandler.class);

	/** The cache buster patter */
	private static Pattern cacheBusterPattern = Pattern.compile("(" + JawrConstant.CACHE_BUSTER_PREFIX + "|"
			+ JawrConstant.CLASSPATH_CACHE_BUSTER_PREFIX + ")[a-zA-Z0-9]+/(.*)$");

	/** The cache buster replace pattern */
	private static final String CACHE_BUSTER_REPLACE_PATTERN = "$2";

	/** The resource handler */
	private ResourceHandler rsHandler;

	/** The bundle mapping */
	private Properties bundleMapping;
	
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

		jawrConfig = createJawrConfig(props);

		jawrConfig.setContext(servletContext);
		jawrConfig.setGeneratorRegistry(generatorRegistry);

		// Set the content type to be used for every request.
		contentType = "img";

		// Set mapping, to be used by the tag lib to define URLs that point to this servlet.
		String mapping = (String) initParameters.get(JawrConstant.SERVLET_MAPPING_PROPERTY_NAME);
		if (null != mapping) {
			jawrConfig.setServletMapping(mapping);
		}

		// Initialize the resource handler
		rsHandler = initResourceHandler();
		bundleMapping = rsHandler.getJawrBundleMapping();
		
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(jawrConfig, rsHandler);
		initImageMapping(imgRsHandler);

		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);

		if (log.isDebugEnabled()) {
			log.debug("Configuration read. Current config:");
			log.debug(jawrConfig);
		}

		// Warn when in debug mode
		if (jawrConfig.isDebugModeOn()) {
			log.warn("Jawr initialized in DEVELOPMENT MODE. Do NOT use this mode in production or integration servers. ");
		}
	}

	/**
	 * Initialize the image mapping of the image resources handler
	 * 
	 * @param imgRsHandler the image resources handler
	 */
	private void initImageMapping(ImageResourcesHandler imgRsHandler) {

		if (jawrConfig.getUseBundleMapping() && rsHandler.isExistingMappingFile()) {

			// Initialize the image mapping
			Iterator mapIterator = bundleMapping.keySet().iterator();
			while (mapIterator.hasNext()) {
				String key = (String) mapIterator.next();
				imgRsHandler.addMapping(key, bundleMapping.getProperty(key));
			}
			
		} else {
			// Create a resource handler to read files from the WAR archive or exploded dir.
			String imageResourcesDefinition = jawrConfig.getImageResourcesDefinition();
			if (imageResourcesDefinition != null) {

				StringTokenizer tokenizer = new StringTokenizer(imageResourcesDefinition, ",");
				while (tokenizer.hasMoreTokens()) {
					String pathMapping = tokenizer.nextToken();

					// path in the classpath and ends with an image extension
					if (pathMapping.startsWith("jar:") && hasImageFileExtension(pathMapping)) {

						addImagePath(imgRsHandler, pathMapping);
					}
					// path ends in /, the folder is included without subfolders
					else if (pathMapping.endsWith("/")) {
						addItemsFromDir(imgRsHandler, pathMapping, false);
					}
					// path ends in /, the folder is included with all subfolders
					else if (pathMapping.endsWith("/**")) {
						addItemsFromDir(imgRsHandler, pathMapping.substring(0, pathMapping.lastIndexOf("**")), true);
					} else if (hasImageFileExtension(pathMapping)) {
						addImagePath(imgRsHandler, pathMapping);
					} else
						log.warn("Wrong mapping [" + pathMapping + "] for image bundle. Please check configuration. ");
				}
			}
		}

		// Store the bundle mapping
		if (jawrConfig.getUseBundleMapping() && !rsHandler.isExistingMappingFile()) {
			rsHandler.storeJawrBundleMapping(bundleMapping);
		}
		
		if (log.isDebugEnabled())
			log.debug("Finish creation of map for image bundle");
	}

	/**
	 * Add an image path to the image map
	 * 
	 * @param imgRsHandler the image resources handler
	 * @param imgPath the image path
	 * @param classPathImg the flag indicating if the image should be retrieved from classpath
	 */
	private void addImagePath(ImageResourcesHandler imgRsHandler, String imgPath) {

		try {
			String resultPath = CheckSumUtils.getCacheBustedUrl(imgPath, rsHandler, jawrConfig);
			imgRsHandler.addMapping(imgPath, resultPath);
			bundleMapping.put(imgPath, resultPath);
		} catch (IOException e) {
			log.error("An exception occurs while defining the mapping for the file : " + imgPath, e);
		}
	}

	/**
	 * Returns true of the path contains an image file extension
	 * 
	 * @param path the path
	 * @return the image file extension
	 */
	private boolean hasImageFileExtension(String path) {
		boolean result = false;

		int extFileIdx = path.lastIndexOf(".");
		if (extFileIdx != -1 && extFileIdx + 1 < path.length()) {
			String extension = path.substring(extFileIdx + 1);
			result = imgMimeMap.containsKey(extension);
		}

		return result;
	}

	/**
	 * Adds all the resources within a path to the image map.
	 * 
	 * @param imgRsHandler the image resources handler
	 * @param dirName the directory name
	 * @param addSubDirs boolean If subfolders will be included. In such case, every folder below the path is included.
	 */
	private void addItemsFromDir(ImageResourcesHandler imgRsHandler, String dirName, boolean addSubDirs) {
		Set resources = rsHandler.getResourceNames(dirName);

		if (log.isDebugEnabled()) {
			log.debug("Adding " + resources.size() + " resources from path [" + dirName + "] to image bundle");
		}

		// Add remaining resources (remaining after sorting, or all if no sort file present)
		List folders = new ArrayList();
		for (Iterator it = resources.iterator(); it.hasNext();) {
			String resourceName = (String) it.next();
			String resourcePath = PathNormalizer.joinPaths(dirName, resourceName);
			if (hasImageFileExtension(resourceName)) {
				addImagePath(imgRsHandler, resourcePath);

				if (log.isDebugEnabled())
					log.debug("Added to item path list:" + PathNormalizer.asPath(resourcePath));
			} else if (addSubDirs) {

				try {
					if (rsHandler.isDirectory(resourcePath)) {
						folders.add(resourceName);
					}
				} catch (InvalidPathException e) {
					if (log.isDebugEnabled())
						log.debug("Enable to define if the following resource is a directory : " + PathNormalizer.asPath(resourcePath));
				}
			}
		}

		// Add subfolders if requested. Subfolders are added last unless specified in sorting file.
		if (addSubDirs) {
			for (Iterator it = folders.iterator(); it.hasNext();) {
				String folderName = (String) it.next();
				addItemsFromDir(imgRsHandler, PathNormalizer.joinPaths(dirName, folderName), true);
			}
		}
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

		if (fromClasspath) {
			if (fileName.startsWith("/")) {
				fileName = fileName.substring(1);
			}
			is = ClassLoaderResourceUtils.getResourceAsStream(fileName, this);
		} else {
			try {
				if (!fileName.startsWith("/")) {
					fileName = "/" + fileName;
				}
				is = rsHandler.getResourceAsStream(fileName);
			} catch (ResourceNotFoundException e) {
				// Nothing to do
			}
		}

		if (is == null) {
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

		if (fileName.startsWith("/")) {
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
	 * 
	 * @param fileName the file name
	 * @return true if the file is a class path image
	 */
	private boolean isClasspathImage(String fileName) {
		if (fileName.startsWith("/")) {
			fileName = fileName.substring(1);
		}

		return fileName.startsWith(JawrConstant.CLASSPATH_CACHE_BUSTER_PREFIX);
	}

}
