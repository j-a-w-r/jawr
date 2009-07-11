/**
 * Copyright 2007-2009  Jordi Hernández Sellés, Ibrahim Chaehoi
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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.management.MBeanServer;
//import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.config.jmx.JmxUtils;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.FileNameUtils;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.ServletContextResourceHandler;
import net.jawr.web.resource.bundle.factory.PropertiesBasedBundlesHandlerFactory;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.factory.util.ConfigChangeListener;
import net.jawr.web.resource.bundle.factory.util.ConfigChangeListenerThread;
import net.jawr.web.resource.bundle.factory.util.ConfigPropertiesSource;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.factory.util.PropsFilePropertiesSource;
import net.jawr.web.resource.bundle.factory.util.ServletContextAware;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;
import net.jawr.web.resource.bundle.handler.ClientSideHandlerScriptRequestHandler;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.servlet.util.MIMETypesSupport;
import net.jawr.web.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * Request handling class. Any jawr enabled servlet delegates to this class to handle requests.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class JawrRequestHandler implements ConfigChangeListener {

	protected static final String CACHE_CONTROL_HEADER = "Cache-Control";
	protected static final String CACHE_CONTROL_VALUE = "public, max-age=315360000, post-check=315360000, pre-check=315360000";
	protected static final String LAST_MODIFIED_HEADER = "Last-Modified";
	protected static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";
	protected static final String IF_NONE_MATCH_HEADER = "If-None-Match";
	protected static final String LAST_MODIFIED_VALUE = "Sun, 06 Nov 2005 12:00:00 GMT";
	protected static final String ETAG_HEADER = "ETag";
	protected static final String ETAG_VALUE = "2740050219";
	protected static final String EXPIRES_HEADER = "Expires";

	protected static final String CONFIG_RELOAD_INTERVAL = "jawr.config.reload.interval";
	public static final String GENERATION_PARAM = "generationConfigParam";
	
	public static final String CLIENTSIDE_HANDLER_REQ_PATH = "/jawr_loader.js";

	/** The CSS classpath image pattern */
	private static Pattern CSS_CLASSPATH_IMG_PATTERN = Pattern.compile("(url\\(([\"' ]*))(jar:)([^\\)\"']*)([\"']?\\))");

	/** The URL separator pattern */
	private static Pattern URL_SEPARATOR_PATTERN = Pattern.compile("([^/]*)/");

	/** The pattern to go to the root */
	private static String ROOT_REPLACE_PATTERN = "../";

	private static final Logger log = Logger.getLogger(JawrRequestHandler.class);

	protected ResourceBundlesHandler bundlesHandler;

	protected String contentType;
	protected String resourceType;
	protected ServletContext servletContext;
	protected Map initParameters;
	protected ConfigChangeListenerThread configChangeListenerThread;
	protected GeneratorRegistry generatorRegistry;
	protected JawrConfig jawrConfig;
	protected ConfigPropertiesSource propertiesSource;
	protected ClientSideHandlerScriptRequestHandler clientSideScriptRequestHandler;

	/** The image MIME map, associating the image extension to their MIME type */
	protected Map imgMimeMap;
	
	/**
	 * Reads the properties file and initializes all configuration using the ServletConfig object. If applicable, a ConfigChangeListenerThread will be
	 * started to listen to changes in the properties configuration.
	 * 
	 * @param servletContext ServletContext
	 * @param servletConfig ServletConfig
	 * @throws ServletException
	 */
	public JawrRequestHandler(ServletContext context, ServletConfig config) throws ServletException {
		this.imgMimeMap = MIMETypesSupport.getSupportedProperties(this);
		this.initParameters = new HashMap();
		Enumeration params = config.getInitParameterNames();
		while (params.hasMoreElements()) {
			String param = (String) params.nextElement();
			initParameters.put(param, config.getInitParameter(param));
		}
		initParameters.put("handlerName", config.getServletName());

		if (log.isInfoEnabled())
			log.info("Initializing jawr config for servlet named " + config.getServletName());
		long initialTime = System.currentTimeMillis();
		this.servletContext = context;

		resourceType = config.getInitParameter("type");
		resourceType = null == resourceType ? "js" : resourceType;

		String configLocation = config.getInitParameter("configLocation");
		String configPropsSourceClass = config.getInitParameter("configPropertiesSourceClass");
		if (null == configLocation && null == configPropsSourceClass)
			throw new ServletException("Neither configLocation nor configPropertiesSourceClass init params were set."
					+ " You must set at least the configLocation param. Please check your web.xml file");

		// Initialize the config properties source that will provide with all configuration options.
		ConfigPropertiesSource propsSrc;

		// Load a custom class to set config properties
		if (null != configPropsSourceClass) {
			propsSrc = (ConfigPropertiesSource) ClassLoaderResourceUtils.buildObjectInstance(configPropsSourceClass);
			if (propsSrc instanceof ServletContextAware) {
				((ServletContextAware) propsSrc).setServletContext(context);
			}
		} else {
			// Default config properties source, reads from a .properties file in the classpath.
			propsSrc = new PropsFilePropertiesSource();
		}

		// If a custom properties source is a subclass of PropsFilePropertiesSource, we hand it the configLocation param.
		// This affects the standard one as well.
		if (propsSrc instanceof PropsFilePropertiesSource)
			((PropsFilePropertiesSource) propsSrc).setConfigLocation(configLocation);

		// Read properties from properties source
		Properties props = propsSrc.getConfigProperties();

		// init registry
		generatorRegistry = new GeneratorRegistry(resourceType);

		// hang onto the propertiesSource for manual reloads
		this.propertiesSource = propsSrc;

		// Initialize config
		initializeJawrConfig(props);

		// Initialize the properties reloading checker daemon if specified
		if(!ThreadLocalJawrContext.isBundleProcessingAtBuildTime() && null != props.getProperty(CONFIG_RELOAD_INTERVAL)) {
			int interval = Integer.valueOf(props.getProperty(CONFIG_RELOAD_INTERVAL)).intValue();
			log.warn("Jawr started with configuration auto reloading on. "
					+ "Be aware that a daemon thread will be checking for changes to configuration every " + interval + " seconds.");

			this.configChangeListenerThread = new ConfigChangeListenerThread(propsSrc, this, interval);
			configChangeListenerThread.start();
		}

		// initialize the jmx Bean
		if(isJmxEnabled()){
				JmxUtils.initJMXBean(this, servletContext, resourceType, jawrConfig.getConfigProperties());
		}
		
		if (log.isInfoEnabled()) {
			long totaltime = System.currentTimeMillis() - initialTime;
			log.info("Init method succesful. jawr started in " + (totaltime / 1000) + " seconds....");
		}

	}

	/**
	 * Returns true if JMX is enabled for the applcation
	 * @return true if JMX is enabled for the applcation
	 */
	private boolean isJmxEnabled() {
		return System.getProperty(JawrConstant.JMX_ENABLE_FLAG_SYSTEL_PROPERTY) != null;
	}

//	/**
//	 * Initialize the JMX Bean 
//	 */
//	protected void initJMXBean() {
//		
//		// Skip the initialisation if no JMX jar is find.
//		try {
//			getClass().getClassLoader().loadClass("javax.management.MBeanServer");
//		} catch (ClassNotFoundException e1) {
//			log.info("JMX API is not define in the classpath.");
//			return;
//		}
//		
//		try {
//
//			MBeanServer mbs = JmxUtils.getMBeanServer();
//			if(mbs != null){
//				
//				ObjectName jawrConfigMgrObjName = JmxUtils.getMBeanObjectName(servletContext, resourceType);
//				JawrApplicationConfigManager appConfigMgr = (JawrApplicationConfigManager) servletContext.getAttribute(JawrConstant.JAWR_APPLICATION_CONFIG_MANAGER);
//				if(appConfigMgr == null){
//					appConfigMgr = new JawrApplicationConfigManager();
//					servletContext.setAttribute(JawrConstant.JAWR_APPLICATION_CONFIG_MANAGER, appConfigMgr);
//				}
//				
//				// register the jawrApplicationConfigManager if it's not already done
//				ObjectName appJawrMgrObjectName = JmxUtils.getAppJawrConfigMBeanObjectName(servletContext);
//				if(!mbs.isRegistered(appJawrMgrObjectName)){
//					mbs.registerMBean(appConfigMgr, appJawrMgrObjectName);
//				}
//				
//				// Create the MBean for the current Request Handler
//				JawrConfigManager mbean = new JawrConfigManager(this, jawrConfig.getConfigProperties());
//				if(mbs.isRegistered(jawrConfigMgrObjName)){
//					log.warn("The MBean '"+jawrConfigMgrObjName.getCanonicalName()+"' already exists. It will be unregisterd and registered with the new JawrConfigManagerMBean.");
//					mbs.unregisterMBean(jawrConfigMgrObjName);
//				}
//				
//				// Initialize the jawrApplicationConfigManager
//				if(resourceType.equals(JawrConstant.JS_TYPE)){
//					appConfigMgr.setJsMBean(mbean);
//				}else if(resourceType.equals(JawrConstant.CSS_TYPE)){
//					appConfigMgr.setCssMBean(mbean);
//				}else{
//					appConfigMgr.setImgMBean(mbean);
//				}
//				
//				mbs.registerMBean(mbean, jawrConfigMgrObjName);
//			}
//			
//		} catch (Exception e) {
//			log.error("Unable to instanciate the Jawr MBean for resource type '"+resourceType+"'", e);
//		}
//
//	}

	

	/**
	 * Alternate constructor that does not need a ServletConfig object. Parameters normally read rom it are read from the initParams Map, and the
	 * configProps are used instead of reading a .properties file.
	 * 
	 * @param servletContext ServletContext
	 * @param servletConfig ServletConfig
	 * @throws ServletException
	 */
	public JawrRequestHandler(ServletContext context, Map initParams, Properties configProps) throws ServletException {

		this.imgMimeMap = MIMETypesSupport.getSupportedProperties(this);
		this.initParameters = initParams;

		if (log.isInfoEnabled())
			log.info("Initializing jawr config for request handler named " + (String) initParams.get("handlerName"));

		long initialTime = System.currentTimeMillis();
		this.servletContext = context;

		resourceType = (String) initParameters.get("type");
		resourceType = null == resourceType ? "js" : resourceType;

		// init registry
		generatorRegistry = new GeneratorRegistry(resourceType);

		// Initialize config
		initializeJawrConfig(configProps);

		if (log.isInfoEnabled()) {
			long totaltime = System.currentTimeMillis() - initialTime;
			log.info("Init method succesful. jawr started in " + (totaltime / 1000) + " seconds....");
		}
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

		createJawrConfig(props);
		
		jawrConfig.setContext(servletContext);
		jawrConfig.setGeneratorRegistry(generatorRegistry);

		// Set the content type to be used for every request.
		contentType = "text/";
		contentType += "js".equals(resourceType) ? "javascript" : "css";
		contentType += "; charset=" + jawrConfig.getResourceCharset().name();

		// Set mapping, to be used by the tag lib to define URLs that point to this servlet.
		String mapping = (String) initParameters.get("mapping");
		if (null != mapping)
			jawrConfig.setServletMapping(mapping);

		if (jawrConfig.isUsingClasspathCssImageServlet() && resourceType.equals("css")) {
			ImageResourcesHandler imgRsHandler = (ImageResourcesHandler) servletContext.getAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE);
			if (imgRsHandler == null) {
				log.error("You are using the CSS classpath image feature, but the JAWR Image servlet is yet initialized.\n"
						+ "The JAWR Image servlet must be initialized before the JAWR CSS servlet.\n"
						+ "Please check you web application configuration.");
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Configuration read. Current config:");
			log.debug(jawrConfig);
		}

		// Create a resource handler to read files from the WAR archive or exploded dir.
		ResourceHandler rsHandler = initResourceHandler();
		PropertiesBasedBundlesHandlerFactory factory = new PropertiesBasedBundlesHandlerFactory(props, resourceType, rsHandler, jawrConfig
				.getGeneratorRegistry());
		try {
			bundlesHandler = factory.buildResourceBundlesHandler(jawrConfig);
		} catch (DuplicateBundlePathException e) {
			throw new ServletException(e);
		}

		if (resourceType.equals(JawrConstant.JS_TYPE))
			servletContext.setAttribute(JawrConstant.JS_CONTEXT_ATTRIBUTE, bundlesHandler);
		else
			servletContext.setAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE, bundlesHandler);

		this.clientSideScriptRequestHandler = new ClientSideHandlerScriptRequestHandler(bundlesHandler, jawrConfig);

		if (log.isDebugEnabled()) {
			log.debug("content type set to: " + contentType);
		}

		// Warn when in debug mode
		if (jawrConfig.isDebugModeOn()) {
			log.warn("Jawr initialized in DEVELOPMENT MODE. Do NOT use this mode in production or integration servers. ");
		}
	}

	/**
	 * Initialize the resource handler
	 * @return the resource handler
	 */
	protected ResourceHandler initResourceHandler() {
		ResourceHandler rsHandler = null;
		if(jawrConfig.getUseBundleMapping() && StringUtils.isNotEmpty(jawrConfig.getJawrWorkingDirectory())){
			rsHandler = new ServletContextResourceHandler(servletContext, jawrConfig.getJawrWorkingDirectory(), jawrConfig.getResourceCharset(), jawrConfig.getGeneratorRegistry(), resourceType);
		}else{
			rsHandler = new ServletContextResourceHandler(servletContext, jawrConfig.getResourceCharset(), jawrConfig.getGeneratorRegistry(), resourceType);
		}
		return rsHandler;
	}

	/**
	 * Create the Jawr config from the properties
	 * @param props the properties
	 */
	protected JawrConfig createJawrConfig(Properties props) {
		jawrConfig = new JawrConfig(props);
		
		// Override properties which are incompatble with the build time bundle processing
		if(ThreadLocalJawrContext.isBundleProcessingAtBuildTime()){
			jawrConfig.setUseBundleMapping(true);
			
			// Use the standard working directory
			jawrConfig.setJawrWorkingDirectory(null);
		}
		
		return jawrConfig;
	}

	/**
	 * Handles a resource request by getting the requested path from the request object and invoking processRequest.
	 * 
	 * @param request the request
	 * @param response the response
	 * @throws ServletException if a servlet exception occurs
	 * @throws IOException if an IO exception occurs.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try{
			// Initialize the Thread local for the Jawr context
			if(isJmxEnabled()){
				ThreadLocalJawrContext.setJawrConfigMgrObjectName(JmxUtils.getMBeanObjectName(request.getContextPath(), resourceType));
			}
			
			RendererRequestUtils.setRequestDebuggable(request, jawrConfig);
			
			String requestedPath = "".equals(jawrConfig.getServletMapping()) ? request.getServletPath() : request.getPathInfo();
			processRequest(requestedPath, request, response);
			
		} catch (Exception e) {
			throw new ServletException(e);
		}finally{
			
			// Reset the Thread local for the Jawr context
			ThreadLocalJawrContext.reset();
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
	 * @param requestedPath the requested path
	 * @param request the request
	 * @param response the response
	 * @throws ServletException if a servlet exception occurs
	 * @throws IOException if an IO exception occurs
	 */
	public void processRequest(String requestedPath, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// manual reload request
		if (this.jawrConfig.getRefreshKey().length() > 0 && null != request.getParameter("refreshKey")
				&& this.jawrConfig.getRefreshKey().equals(request.getParameter("refreshKey"))) {
			this.configChanged(propertiesSource.getConfigProperties());
		}

		if (log.isDebugEnabled())
			log.debug("Request received for path:" + requestedPath);

		if (CLIENTSIDE_HANDLER_REQ_PATH.equals(requestedPath)) {
			this.clientSideScriptRequestHandler.handleClientSideHandlerRequest(request, response);
			return;
		}

		// CSS images would be requested through this handler in case servletMapping is used
		// if( this.jawrConfig.isDebugModeOn() && !("".equals(this.jawrConfig.getServletMapping())) && null == request.getParameter(GENERATION_PARAM)) {
		if (JawrConstant.CSS_TYPE.equals(resourceType) && 
				!JawrConstant.CSS_TYPE.equals(getExtension(requestedPath)) &&
				this.imgMimeMap.containsKey(getExtension(requestedPath))) {

			if (null == bundlesHandler.resolveBundleForPath(requestedPath)) {
				if (log.isDebugEnabled())
					log.debug("Path '" + requestedPath + "' does not belong to a bundle. Forwarding request to the server. ");
				request.getRequestDispatcher(requestedPath).forward(request, response);
				return;
			}
		}

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
		} else if (null != request.getParameter(GENERATION_PARAM))
			requestedPath = request.getParameter(GENERATION_PARAM);

		// By setting content type, the response writer will use appropiate encoding
		response.setContentType(contentType);

		try {
			// Send gzipped resource if user agent supports it.
			if (requestedPath.startsWith(BundleRenderer.GZIP_PATH_PREFIX)) {
				requestedPath = "/" + requestedPath.substring(BundleRenderer.GZIP_PATH_PREFIX.length(), requestedPath.length());
				response.setHeader("Content-Encoding", "gzip");
				bundlesHandler.streamBundleTo(requestedPath, response.getOutputStream());
			} else {

				// In debug mode, we take in account the image defined in the classpath
				// The following code will rewrite the URL path for the classpath images,
				// because in debug mode, we are retrieving the CSS ressources directly from the webapp
				// and if the CSS contains image classpath, we should rewrite the URL.
				//
				// TODO Create a temporary file which will store the result,
				// and use a map which will allow us to associate a path to a hashcode.
				// We will process the file only if the hashcode of the content change
				if (this.jawrConfig.isDebugModeOn() && resourceType.equals(JawrConstant.CSS_TYPE)) {

					// Write the content of the CSS in the Stringwriter
					Writer writer = new StringWriter();
					bundlesHandler.writeBundleTo(requestedPath, writer);
					String content = writer.toString();

					ImageResourcesHandler imgRsHandler = (ImageResourcesHandler) servletContext.getAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE);
					String imageServletMapping = imgRsHandler.getJawrConfig().getServletMapping();
					if (imageServletMapping == null) {
						imageServletMapping = "";
					}

					// Define the replacement pattern for the image define in the classpath (like jar:img/myImg.png)
					String relativeRootUrlPath = getRootRelativeCssUrlPath(request, requestedPath);
					String replacementPattern = PathNormalizer.normalizePath("$1" + relativeRootUrlPath + imageServletMapping + "/cpCbDebug/" + "$4$5");
					
					Matcher matcher = CSS_CLASSPATH_IMG_PATTERN.matcher(content);

					// Rewrite the images define in the classpath, to point to the image servlet
					StringBuffer result = new StringBuffer();
					while (matcher.find()) {
						matcher.appendReplacement(result, replacementPattern);
					}
					matcher.appendTail(result);
					Writer out = response.getWriter();
					out.write(result.toString());
				} else {

					Writer out = response.getWriter();
					bundlesHandler.writeBundleTo(requestedPath, out);
				}

			}
		} catch (ResourceNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			if (log.isInfoEnabled())
				log.info("Received a request for a non existing bundle: " + requestedPath);
			return;
		}

		if (log.isDebugEnabled())
			log.debug("request succesfully attended");
	}

	/**
	 * Returns the extension for the requested path
	 * @param requestedPath the requested path
	 * @return the extension for the requested path
	 */
	protected String getExtension(String requestedPath) {
		
		return FileNameUtils.getExtension(requestedPath);
	}

	/**
	 * Returns the relative path of an url to go back to the root.
	 * For example : if the url path is defined as "/cssServletPath/css/myStyle.css" -> "../../"
	 * 
	 * @param request the request
	 * @param url the requested url
	 * @return the relative path of an url to go back to the root.
	 */
	private String getRootRelativeCssUrlPath(HttpServletRequest request, String url) {

		String servletPath = "".equals(jawrConfig.getServletMapping()) ? "" : request.getServletPath();
		String originalRequestPath = "".equals(jawrConfig.getServletMapping()) ? request.getServletPath() : request.getPathInfo();
		// Deals with Jawr generated resource path containing /jawr_generator.css
		if(originalRequestPath.startsWith(ResourceGenerator.CSS_DEBUGPATH)){
			url = ResourceGenerator.CSS_DEBUGPATH;
		}

		url = PathNormalizer.asPath(servletPath + url);
		
		Matcher matcher = URL_SEPARATOR_PATTERN.matcher(url);
		StringBuffer result = new StringBuffer();
		int i = 0;
		while (matcher.find()) {
			if (i == 0) {
				matcher.appendReplacement(result, "");
				i++;
			} else {
				matcher.appendReplacement(result, ROOT_REPLACE_PATTERN);
			}

		}

		return result.toString();
	}

	/**
	 * Adds aggresive caching headers to the response in order to prevent browsers requesting the same file twice.
	 * 
	 * @param resp
	 */
	protected void setResponseHeaders(HttpServletResponse resp) {
		// Force resource caching as best as possible
		resp.setHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_VALUE);
		resp.setHeader(LAST_MODIFIED_HEADER, LAST_MODIFIED_VALUE);
		resp.setHeader(ETAG_HEADER, ETAG_VALUE);
		Calendar cal = Calendar.getInstance();
		cal.roll(Calendar.YEAR, 10);
		resp.setDateHeader(EXPIRES_HEADER, cal.getTimeInMillis());
	}

	/**
	 * Analog to Servlet.destroy(), should be invoked whenever the app is redeployed.
	 */
	public void destroy() {
		// Stop the config change listener.
		if (null != this.configChangeListenerThread)
			configChangeListenerThread.stopPolling();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.factory.util.ConfigChangeListener#configChanged(java.util.Properties)
	 */
	public synchronized void configChanged(Properties newConfig) {
		if (log.isDebugEnabled())
			log.debug("Reloading Jawr configuration");
		try {
			// Initialize the Thread local for the Jawr context
			if(isJmxEnabled()){
				ThreadLocalJawrContext.setJawrConfigMgrObjectName(JmxUtils.getMBeanObjectName(servletContext, resourceType));
			}
			
			initializeJawrConfig(newConfig);
		} catch (Exception e) {
			throw new RuntimeException("Error reloading Jawr config: " + e.getMessage(), e);
		}finally{
			
			// Reset the Thread local for the Jawr context
			ThreadLocalJawrContext.reset();
		}
		
		if (log.isDebugEnabled())
			log.debug("Jawr configuration succesfully reloaded. ");

	}
}
