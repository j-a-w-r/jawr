/**
 * Copyright 2009-2010 Ibrahim Chaehoi
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
package net.jawr.web.bundle.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.jawr.web.JawrConstant;
import net.jawr.web.bundle.processor.renderer.BasicBundleRenderer;
import net.jawr.web.bundle.processor.renderer.RenderedLink;
import net.jawr.web.bundle.processor.spring.SpringControllerBundleProcessor;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRendererContext;
import net.jawr.web.resource.bundle.variant.VariantUtils;
import net.jawr.web.servlet.JawrRequestHandler;
import net.jawr.web.servlet.JawrServlet;
import net.jawr.web.servlet.mock.MockServletConfig;
import net.jawr.web.servlet.mock.MockServletContext;
import net.jawr.web.servlet.mock.MockServletRequest;
import net.jawr.web.servlet.mock.MockServletResponse;
import net.jawr.web.servlet.mock.MockServletSession;
import net.jawr.web.util.FileUtils;
import net.jawr.web.util.StringUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The bundle processor is managing the bundle processing at build time.
 * 
 * @author Ibrahim Chaehoi
 */
public class BundleProcessor {

	/** The logger */
	private static Logger logger = Logger.getLogger(BundleProcessor.class);
	
	/** The /WEB-INF/lib directory path */
	private static final String WEB_INF_LIB_DIR_PATH = "/WEB-INF/lib/";

	/** The /WEB-INF/classes directory path */
	private static final String WEB_INF_CLASSES_DIR_PATH = "/WEB-INF/classes/";

	/** The path to the web.xml file from the web application root directory */
	private static final String WEB_XML_FILE_PATH = "WEB-INF/web.xml";

	/** The name of the param-name tag */
	private static final String PARAM_NAME_TAG_NAME = "param-name";

	/** The name of the param-value tag */
	private static final String PARAM_VALUE_TAG_NAME = "param-value";

	/** The name of the servlet tag */
	private static final String SERVLET_TAG_NAME = "servlet";

	/** The name of the listener tag */
	private static final String CONTEXT_TAG_NAME = "context-param";

	/** The name of the servlet-class tag */
	private static final String SERVLET_CLASS_TAG_NAME = "servlet-class";

	/** The name of the servlet-name tag */
	private static final String SERVLET_NAME_TAG_NAME = "servlet-name";

	/** The name of the param-value tag */
	private static final String INIT_PARAM_TAG_NAME = "init-param";

	/** The name of the load on startup tag */
	private static final String LOAD_ON_STARTUP_TAG_NAME = "load-on-startup";

	/** The init type parameter */
	private static final String TYPE_INIT_PARAMETER = "type";

	/** The CDN directory name */
	private static final String CDN_DIR_NAME = "/CDN";

	/** The parameter name of the spring context config location */
	public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";
	
	// The following constants are related to the jawr-apache-httpd.conf file

	/** The path to the template Jawr apache HTTPD conf */
	private static final String TEMPLATE_JAWR_APACHE_HTTPD_CONF_PATH = "/net/jawr/web/bundle/resource/template-jawr-apache-httpd.conf";

	/** The file name of the jawr-apache-httpd.conf file */
	private static final String JAWR_APACHE_HTTPD_CONF_FILE = "jawr-apache-httpd.conf";

	/** 
	 * The statement which define that we should check the JS servlet mapping is defined,
	 * before processing the next line
	 */
	private static final String CHECKS_JAWR_JS_SERVLET_MAPPING_EXISTS = "## if <jawr.js.servlet.mapping>";

	/** 
	 * The statement which define that we should check the CSS servlet mapping is defined,
	 * before processing the next line
	 */
	private static final String CHECK_JAWR_CSS_SERVLET_MAPPING_EXISTS = "## if <jawr.css.servlet.mapping>";

	/** The pattern for the jawr image servlet mapping in the template file	*/
	private static final String JAWR_IMG_SERVLET_MAPPING_PATTERN = "<jawr\\.img\\.servlet\\.mapping>";

	/** The pattern for the jawr CSS servlet mapping in the template file */
	private static final String JAWR_CSS_SERVLET_MAPPING_PATTERN = "<jawr\\.css\\.servlet\\.mapping>";

	/** The pattern for the jawr JS servlet mapping in the template file */
	private static final String JAWR_JS_SERVLET_MAPPING_PATTERN = "<jawr\\.js\\.servlet\\.mapping>";

	/** The root directory which will contains the resource on the CDN */
	private static final String APP_ROOT_DIR_PATTERN = "<app\\.root\\.dir>";

	private static final String DEFAULT_WEBAPP_URL = "{WEBAPP_URL}";
	
	/**
	 * Launch the bundle processing
	 * 
	 * @param baseDirPath the base directory path
	 * @param tmpDirPath the temp directory path
	 * @param destDirPath the destination directory path
	 * @param generateCdnFiles the flag indicating if we should generate the CDN files or not
	 * @throws Exception if an exception occurs
	 */
	public void process(String baseDirPath, String tmpDirPath, String destDirPath, boolean generateCdnFiles) throws Exception {

		process(baseDirPath, tmpDirPath, destDirPath, null, new ArrayList(),generateCdnFiles, false);
	}

	/**
	 * Launch the bundle processing
	 * 
	 * @param baseDirPath the base directory path
	 * @param tmpDirPath the temp directory path
	 * @param destDirPath the destination directory path
	 * @param springConfigFiles the spring config file to initialize
	 * @param propertyPlaceHolderFile the path to the property place holder file
	 * @param servletNames the list of the name of servlets to initialized
	 * @param generateCdnFiles the flag indicating if we should generate the CDN files or not
	 * @param keepUrlMapping the flag indicating if we should keep the URL mapping or not.
	 * @throws Exception if an exception occurs
	 */
	public void process(String baseDirPath, String tmpDirPath, String destDirPath, String springConfigFiles, List servletsToInitialize, boolean generateCdnFiles, boolean keepUrlMapping) throws Exception {

		// Creates the web app class loader
		URL webAppClasses = new File(baseDirPath+WEB_INF_CLASSES_DIR_PATH).toURI().toURL();
		URL webAppLibs =  new File(baseDirPath+WEB_INF_LIB_DIR_PATH).toURI().toURL();
		ClassLoader webAppClassLoader = new JawrBundleProcessorCustomClassLoader(new URL[]{webAppClasses, webAppLibs}, getClass().getClassLoader());
		Thread.currentThread().setContextClassLoader(webAppClassLoader);
		
		// Retrieve the parameters from baseDir+"/WEB-INF/web.xml"
		File webXml = new File(baseDirPath, WEB_XML_FILE_PATH);
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		docBuilder.setEntityResolver(new EntityResolver() {
			
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				
				return null;
			}
		});
		
		Document doc = docBuilder.parse(webXml);
		MockServletContext servletContext = initServletContext(doc,
				baseDirPath, tmpDirPath, springConfigFiles);
		
		List servletDefinitions = getWebXmlServletDefinitions(
				doc, servletContext, servletsToInitialize, webAppClassLoader);

		// Initialize the servlets and retrieve the jawr servlet definitions
		List jawrServletDefinitions = initServlets(servletDefinitions);
		if(jawrServletDefinitions.isEmpty()){
			
			logger.debug("No Jawr Servlet defined in web.xml");
			if(servletContext.getInitParameter(CONFIG_LOCATION_PARAM) != null){
				logger.debug("Spring config location defined. Try loading spring context");
				jawrServletDefinitions = initJawrSpringControllers(servletContext);
			}
		}
		
		// Copy the temporary directory in the dest directory
		FileUtils.copyDirectory(new File(tmpDirPath), new File(destDirPath));
		
		if(generateCdnFiles){
			// Process the Jawr servlet to generate the bundles
			processJawrServlets(destDirPath, jawrServletDefinitions, keepUrlMapping);
		}
		
	}

	/**
	 * Initalize the servlet context
	 * @param webXmlDoc the web.xml document
	 * @param baseDirPath the base drectory path
	 * @param tmpDirPath the temp directory path
	 * @param springConfigFiles the list of spring config files
	 * @return the servlet context
	 */
	private MockServletContext initServletContext(Document webXmlDoc,
			String baseDirPath, String tmpDirPath, String springConfigFiles) {
		
		// Parse the context parameters
		MockServletContext servletContext = new MockServletContext(baseDirPath, tmpDirPath);
		Map servletContextInitParams = new HashMap();
		NodeList contextParamsNodes = webXmlDoc.getElementsByTagName(CONTEXT_TAG_NAME);
		for (int i = 0; i < contextParamsNodes.getLength(); i++) {
			Node node = contextParamsNodes.item(i);
			initializeInitParams(node, servletContextInitParams);
		}
		
		// Override spring config file if needed
		if(StringUtils.isNotEmpty(springConfigFiles)){
			servletContextInitParams.put(CONFIG_LOCATION_PARAM, springConfigFiles);
		}
		
		servletContext.setInitParameters(servletContextInitParams);
		return servletContext;
	}

	/**
	 * Returns the list of servlet definition, which must be initialize
	 * @param webXmlDoc the web.xml document
	 * @param servletContext the servlet context
	 * @param servletsToInitialize the list of servlet to initialize
	 * @param webAppClassLoader the web application class loader
	 * @return the list of servlet definition, which must be initialize
	 * @throws ClassNotFoundException if a class is not found
	 */
	private List getWebXmlServletDefinitions(Document webXmlDoc,
			ServletContext servletContext, List servletsToInitialize,	
			ClassLoader webAppClassLoader) throws ClassNotFoundException {

		// Parse the servlet configuration
		NodeList servletNodes = webXmlDoc.getElementsByTagName(SERVLET_TAG_NAME);
		
		List servletDefinitions = new ArrayList();
		
		for (int i = 0; i < servletNodes.getLength(); i++) {

			String servletName = null;
			Class servletClass = null;
			MockServletConfig config = new MockServletConfig(servletContext);
			int order = i;

			Node servletNode = servletNodes.item(i);
			Map initParameters = new HashMap();
			NodeList childNodes = servletNode.getChildNodes();
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node servletChildNode = childNodes.item(j);
				if (servletChildNode.getNodeName().equals(SERVLET_NAME_TAG_NAME)) {

					servletName = servletChildNode.getFirstChild().getNodeValue();
					config.setServletName(servletName);

				} else if (servletChildNode.getNodeName().equals(SERVLET_CLASS_TAG_NAME)) {

					String servletClassName = servletChildNode.getFirstChild().getNodeValue();
					servletClass = webAppClassLoader.loadClass(servletClassName);

				} else if (servletChildNode.getNodeName().equals(INIT_PARAM_TAG_NAME)) {

					initializeInitParams(servletChildNode, initParameters);
				} else if (servletChildNode.getNodeName().equals(LOAD_ON_STARTUP_TAG_NAME)) {

					order = Integer.parseInt(servletChildNode.getFirstChild().getNodeValue());
				}
			}

			// Initialize the servlet config with the init parameters
			config.setInitParameters(initParameters);

			// If the servlet name is part of the list of servlet to initialized
			// Set the flag accordingly
			if (servletsToInitialize.contains(servletName) || JawrServlet.class.isAssignableFrom(servletClass)) {
				ServletDefinition servletDef = new ServletDefinition(servletClass, config, order);
				servletDefinitions.add(servletDef);
			}
			// Handle Spring MVC servlet definition
			if(servletContext.getInitParameter(CONFIG_LOCATION_PARAM) == null && 
						servletClass.getName().equals("org.springframework.web.servlet.DispatcherServlet")){
				((MockServletContext)servletContext).putInitParameter(CONFIG_LOCATION_PARAM, "/WEB-INF/"+servletName+"-servlet.xml");
			}
			
		}
		return servletDefinitions;
	}

	/**
	 * Initialize the Jawr spring controller
	 * @param servletContext the servlet context
	 * @return the Jawr spring controller
	 * @throws ServletException if a servlet exception occurs
	 */
	private List initJawrSpringControllers(ServletContext servletContext) throws ServletException {
		
		SpringControllerBundleProcessor springBundleProcessor = new SpringControllerBundleProcessor();
		return springBundleProcessor.initJawrSpringServlets(servletContext);
	}

	/**
	 * Initialize the servlets and returns only the list of Jawr servlets
	 * 
	 * @param servletDefinitions the list of servlet definition
	 * @throws Exception if an exception occurs
	 */
	private List initServlets(List servletDefinitions) throws Exception {

		// Sort the list taking in account the load-on-startup attribute
		Collections.sort(servletDefinitions);

		// Sets the Jawr context at "bundle processing at build time"
		ThreadLocalJawrContext.setBundleProcessingAtBuildTime(true);
		
		List jawrServletDefinitions = new ArrayList();
		for (Iterator iterator = servletDefinitions.iterator(); iterator.hasNext();) {
			ServletDefinition servletDefinition = (ServletDefinition) iterator.next();
			servletDefinition.initServlet();
			if (servletDefinition.isJawrServletDefinition()) {
				jawrServletDefinitions.add(servletDefinition);
			}
		}

		return jawrServletDefinitions;
	}

	/**
	 * Initialize the init parameters define in the servlet config
	 * 
	 * @param initParameters the map of initialization parameters
	 */
	private void initializeInitParams(Node initParamNode, Map initParameters) {

		String paramName = null;
		String paramValue = null;

		NodeList childNodes = initParamNode.getChildNodes();
		for (int j = 0; j < childNodes.getLength(); j++) {
			Node childNode = childNodes.item(j);
			String nodeName = childNode.getNodeName();
			if (nodeName.equals(PARAM_NAME_TAG_NAME)) {
				paramName = childNode.getFirstChild().getNodeValue();
			} else if (nodeName.equals(PARAM_VALUE_TAG_NAME)) {
				paramValue = childNode.getFirstChild().getNodeValue();
			}
		}

		initParameters.put(paramName, paramValue);
	}

	/**
	 * Process the Jawr Servlets
	 *
	 * @param destDirPath the destination directory path
	 * @param jawrServletDefinitions the destination directory
	 * @throws Exception if an exception occurs.
	 */
	private void processJawrServlets(String destDirPath, List jawrServletDefinitions, boolean keepUrlMapping) throws Exception {

		String appRootDir = "";
		String jsServletMapping = "";
		String cssServletMapping = "";
		String imgServletMapping = "";
		
		String cdnDestDirPath = destDirPath + CDN_DIR_NAME;
		
		for (Iterator iterator = jawrServletDefinitions.iterator(); iterator.hasNext();) {

			ServletDefinition servletDef = (ServletDefinition) iterator.next();
			ServletConfig servletConfig = servletDef.getServletConfig();

			// Force the production mode, and remove config listener parameters
			Map initParameters = ((MockServletConfig) servletConfig).getInitParameters();
			initParameters.remove("jawr.config.reload.interval");
			
			String jawrServletMapping  = servletConfig.getInitParameter(JawrConstant.SERVLET_MAPPING_PROPERTY_NAME);
			String servletMapping = servletConfig.getInitParameter(JawrConstant.SPRING_SERVLET_MAPPING_PROPERTY_NAME);
			if(servletMapping == null){
				servletMapping = jawrServletMapping;
			}
			
			ResourceBundlesHandler bundleHandler = null;
			ImageResourcesHandler imgRsHandler = null;

			// Retrieve the bundle Handler
			ServletContext servletContext = servletConfig.getServletContext();
			String type = servletConfig.getInitParameter(TYPE_INIT_PARAMETER);
			if (type == null || type.equals(JawrConstant.JS_TYPE)) {
				bundleHandler = (ResourceBundlesHandler) servletContext.getAttribute(JawrConstant.JS_CONTEXT_ATTRIBUTE);
				String contextPathOverride = bundleHandler.getConfig().getContextPathOverride();
				if(StringUtils.isNotEmpty(contextPathOverride)){
					int idx = contextPathOverride.indexOf("//");
					if(idx != -1){
						idx = contextPathOverride.indexOf("/", idx+2);
						if(idx != -1){
							appRootDir = PathNormalizer.asPath(contextPathOverride.substring(idx));
						}
					}
				}
				
				if(jawrServletMapping != null){
					jsServletMapping = PathNormalizer.asPath(jawrServletMapping);
				}
				
			} else if (type.equals(JawrConstant.CSS_TYPE)) {
				bundleHandler = (ResourceBundlesHandler) servletContext.getAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE);
				if(jawrServletMapping != null){
					cssServletMapping = PathNormalizer.asPath(jawrServletMapping);
				}
			} else if (type.equals(JawrConstant.IMG_TYPE)) {
				imgRsHandler = (ImageResourcesHandler) servletContext.getAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE);
				if(jawrServletMapping != null){
					imgServletMapping = PathNormalizer.asPath(jawrServletMapping);
				}
			}

			if (bundleHandler != null) {
				createBundles(servletDef.getServlet(), bundleHandler, cdnDestDirPath, servletMapping, keepUrlMapping);
			} else if (imgRsHandler != null) {
				createImageBundle(servletDef.getServlet(), imgRsHandler, cdnDestDirPath, servletConfig, keepUrlMapping);
			}
		}
		
		// Create the apache rewrite config file.
		createApacheRewriteConfigFile(cdnDestDirPath, appRootDir,
				jsServletMapping, cssServletMapping, imgServletMapping);
		
	}

	/**
	 * Create the apache rewrite configuration file
	 * 
	 * @param cdnDestDirPath the CDN destination directory
	 * @param appRootDir the application root dir path in the CDN
	 * @param jsServletMapping the JS servlet mapping
	 * @param cssServletMapping the CSS servlet mapping
	 * @param imgServletMapping the image servlet mapping
	 * @throws IOException if an IOException occurs.
	 */
	private void createApacheRewriteConfigFile(String cdnDestDirPath,
			String appRootDir, String jsServletMapping,
			String cssServletMapping, String imgServletMapping)
			throws IOException {
	
		BufferedReader templateFileReader = null;
		FileWriter fileWriter = null;
		try{
			
			templateFileReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(TEMPLATE_JAWR_APACHE_HTTPD_CONF_PATH)));
			fileWriter = new FileWriter(cdnDestDirPath+File.separator+JAWR_APACHE_HTTPD_CONF_FILE);
			String line = null;
			
			boolean processNextString = true;
			while((line = templateFileReader.readLine()) != null){
				
				// If the line starts with the condition to check the existence of the JS servlet mapping,
				// sets the processNextString flag accordingly
				if(line.startsWith(CHECKS_JAWR_JS_SERVLET_MAPPING_EXISTS)){
					if(StringUtils.isEmpty(jsServletMapping)){
						processNextString = false;
					}
				// If the line starts with the condition to check the existence of the servlet mapping,
				// sets the processNextString flag accordingly
				}else if(line.startsWith(CHECK_JAWR_CSS_SERVLET_MAPPING_EXISTS)){
					if(StringUtils.isEmpty(cssServletMapping)){
						processNextString = false;
					}
				// If the processNextString flag is set to false, skip the current line, and process the next one
				}else if(processNextString == false){
					processNextString = true;
				}else{
					
					// Make the replacement
					line = line.replaceAll(APP_ROOT_DIR_PATTERN, appRootDir);
					line = line.replaceAll(JAWR_JS_SERVLET_MAPPING_PATTERN, jsServletMapping);
					line = line.replaceAll(JAWR_CSS_SERVLET_MAPPING_PATTERN, cssServletMapping);
					line = line.replaceAll(JAWR_IMG_SERVLET_MAPPING_PATTERN, imgServletMapping);
					fileWriter.write(line+"\n");
				}
			}
		}finally{
			
			IOUtils.close(templateFileReader);
			IOUtils.close(fileWriter);
			
		}
	}

	/**
	 * Creates the bundles in the destination directory
	 * 
	 * @param servlet the servlet
	 * @param bundleHandler the bundles handler
	 * @param destDirPath the destination directory path
	 * @param servletMapping the mapping of the servlet
	 * @param keepUrlMapping the flag indicating if we must keep the URL mapping
	 * @throws IOException if an IO exception occurs
	 * @throws ServletException if a servlet exception occurs
	 */
	private void createBundles(HttpServlet servlet, ResourceBundlesHandler bundleHandler, String destDirPath, String servletMapping,
			boolean keepUrlMapping) throws IOException,
			ServletException {

		List bundles = bundleHandler.getContextBundles();

		Iterator bundleIterator = bundles.iterator();
		MockServletResponse response = new MockServletResponse();
		MockServletRequest request = new MockServletRequest();
		MockServletSession session = new MockServletSession(servlet.getServletContext());
		request.setSession(session);

		String resourceType = servlet.getServletConfig().getInitParameter(TYPE_INIT_PARAMETER);
		if(resourceType == null){
			resourceType = JawrConstant.JS_TYPE;
		}
		
		// For the list of bundle defines, create the file associated
		while (bundleIterator.hasNext()) {
			JoinableResourceBundle bundle = (JoinableResourceBundle) bundleIterator.next();
			
			// Check if there is a resource file, which could be in conflict with the bundle name 
			URL url = servlet.getServletContext().getResource(bundle.getId());
			if(url != null){
				logger.error("It is not recommended to use a bundle name which could be in conflict with a resource.\n" +
						"Please rename your bundle '"+bundle.getId()+"' to avoid any issue");
				
			}
			
			List allVariants = VariantUtils.getAllVariants(bundle.getVariants()); 
				
			if(allVariants == null){
				allVariants = new ArrayList();
			}
			
			if(allVariants.isEmpty()){
				allVariants.add(new HashMap());
			}
			// Creates the bundle file for each local variant 
			for (Iterator it = allVariants.iterator(); it.hasNext();) {
				Map variantMap = (Map) it.next();
	
				List linksToBundle = createLinkToBundle(bundleHandler, bundle.getId(), resourceType, variantMap);
				for (Iterator iteratorLinks = linksToBundle.iterator(); iteratorLinks.hasNext();) {
					RenderedLink renderedLink = (RenderedLink) iteratorLinks.next();
					String path = (String) renderedLink.getLink();
					
					// Force the debug mode of the config to match what was used in the generated link
					JawrConfig config = bundleHandler.getConfig();
					config.setDebugModeOn(renderedLink.isDebugMode());
					
					String finalBundlePath = null;
					if(keepUrlMapping){
						finalBundlePath = path;
					}else{
						finalBundlePath = getFinalBundlePath(path, config, variantMap);
					}
					
					// Sets the request URL
					setRequestUrl(request, variantMap, path, config);
					
					// We can't use path for generated resources because it's not a valid file path ( /jawr_generator.js?xxx.... ) 
					if(!path.contains("?") || !keepUrlMapping){
						File bundleFile = new File(destDirPath, finalBundlePath);
						createBundleFile(servlet, response, request, path, bundleFile, servletMapping);
					}
				}
			}
		}
	}

	/**
	 * Set the request URL
	 * @param request the request
	 * @param variantMap the variantMap
	 * @param path the path
	 * @param config the Jawr config
	 */
	private void setRequestUrl(MockServletRequest request, Map variantMap,
			String path, JawrConfig config) {
		
		String domainURL = JawrConstant.HTTP_URL_PREFIX+DEFAULT_WEBAPP_URL;
		
		if(JawrConstant.SSL.equals(variantMap.get(JawrConstant.CONNECTION_TYPE_VARIANT_TYPE))){
			if(StringUtils.isNotEmpty(config.getContextPathSslOverride())){
				domainURL = config.getContextPathSslOverride();
			}else{
				domainURL = JawrConstant.HTTPS_URL_PREFIX+DEFAULT_WEBAPP_URL;
			}
		}else{
			if(StringUtils.isNotEmpty(config.getContextPathOverride())){
				domainURL = config.getContextPathOverride();
			}else{
				domainURL = JawrConstant.HTTP_URL_PREFIX+DEFAULT_WEBAPP_URL;
			}
		}
		
		request.setRequestUrl(PathNormalizer.joinDomainToPath(domainURL, path));
	}

	/**
	 * Retrieves the final path, where the servlet mapping and the cache prefix have been removed,
	 * and take also in account the jawr generator URLs. 
	 * 
	 * <pre>
	 * 	"/N1785986402/js/bundle/msg.js" -> "/js/bundle/msg.js"
	 *  "/jawr_generator.js?generationConfigParam=messages%3Amessages%40fr" -> "/jawr_generator/js/messages/messages_fr.js"
	 *  "/cssJawrPath/1414653084/folder/core/component.css" -> "folder/core/component.css"
	 * </pre>
	 * 
	 * @param path the path
	 * @param jawrConfig the jawr config
	 * @param localVariantKey The local variant key
	 * @return the final path
	 */
	public String getFinalBundlePath(String path, JawrConfig jawrConfig, Map variantMap) {

		String finalPath = path;
		int jawrGenerationParamIdx = finalPath.indexOf(JawrRequestHandler.GENERATION_PARAM);
		if(jawrGenerationParamIdx != -1){
			
			try {
				finalPath = URLDecoder.decode(path, "UTF-8");
			} catch (UnsupportedEncodingException neverHappens) {
				/*URLEncoder:how not to use checked exceptions...*/
				throw new RuntimeException("Something went unexpectedly wrong while decoding a URL for a generator. ",
											neverHappens);
			}
			
			// Remove servlet mapping if it exists.
			finalPath = removeServletMappingFromPath(finalPath, jawrConfig.getServletMapping());
			
			finalPath = jawrConfig.getGeneratorRegistry().getDebugModeBuildTimeGenerationPath(finalPath);
		
		}else{
			
			// Remove servlet mapping if it exists.
			finalPath = removeServletMappingFromPath(finalPath, jawrConfig.getServletMapping());
			if (finalPath.startsWith("/")) {
				finalPath = finalPath.substring(1);
			}

			// remove cache prefix, when not in debug mode
			if(!jawrConfig.isDebugModeOn()){
			
				int idx = finalPath.indexOf("/");
				finalPath = finalPath.substring(idx + 1);
			}
			
			// For localized bundle add the local info in the file name
			// For example, with local variant = 'en'
			// /bundle/myBundle.js -> /bundle/myBundle_en.js 
			finalPath = VariantUtils.getVariantBundleName(finalPath, variantMap);
		}
		
		return finalPath;
	}

	/**
	 * Retrieves the image final path, where the servlet mapping and the cache prefix have been removed
	 * 
	 * @param path the path
	 * @param jawrConfig the jawr config
	 * @return the final path
	 */
	public String getImageFinalPath(String path, JawrConfig jawrConfig) {

		String finalPath = path;

		// Remove servlet mapping if it exists.
		finalPath = removeServletMappingFromPath(finalPath, jawrConfig.getServletMapping());
		if (finalPath.startsWith("/")) {
			finalPath = finalPath.substring(1);
		}

		// remove cache prefix
		int idx = finalPath.indexOf("/");
		finalPath = finalPath.substring(idx + 1);
		
		return finalPath;
	}

	/**
	 * Remove the servlet mapping from the path
	 * @param path the path
	 * @param mapping the servlet mapping
	 * @return the path without the servlet mapping
	 */
	private String removeServletMappingFromPath(String path, String mapping) {
		if (mapping != null && mapping.length() > 0) {
			int idx = path.indexOf(mapping);
			if (idx > -1) {
				path = path.substring(idx + mapping.length());
			}

			path = PathNormalizer.asPath(path);
		}
		return path;
	}
	
	/**
	 * Create the image bundle
	 * 
	 * @param servlet the servlet
	 * @param imgRsHandler the image resource handler
	 * @param destDirPath the destination directory path
	 * @param servletMapping the mapping
	 * @param keepUrlMapping = the flag indicating if we must keep the url mapping
	 * @throws IOException if an IOExceptin occurs
	 * @throws ServletException if an exception occurs
	 */
	private void createImageBundle(HttpServlet servlet, ImageResourcesHandler imgRsHandler, String destDirPath, ServletConfig servletConfig, 
			boolean keepUrlMapping) throws IOException,
			ServletException {
		Map bundleImgMap = imgRsHandler.getImageMap();

		Iterator bundleIterator = bundleImgMap.values().iterator();
		MockServletResponse response = new MockServletResponse();
		MockServletRequest request = new MockServletRequest();

		String jawrServletMapping  = servletConfig.getInitParameter(JawrConstant.SERVLET_MAPPING_PROPERTY_NAME);
		if(jawrServletMapping == null){
			jawrServletMapping = "";
		}
		
		String servletMapping = servletConfig.getInitParameter(JawrConstant.SPRING_SERVLET_MAPPING_PROPERTY_NAME);
		if(servletMapping == null){
			servletMapping = jawrServletMapping;
		}
		
		// For the list of bundle defines, create the file associated
		while (bundleIterator.hasNext()) {
			String path = (String) bundleIterator.next();
			
			String imageFinalPath = null;
			
			if(keepUrlMapping){
				imageFinalPath = path;
			}else{
				imageFinalPath = getImageFinalPath(path, imgRsHandler.getJawrConfig());
			}
			
			File destFile = new File(destDirPath, imageFinalPath);
			
			// Update the bundle mapping
			path = PathNormalizer.concatWebPath(PathNormalizer.asDirPath(jawrServletMapping), path);
			createBundleFile(servlet, response, request, path, destFile, servletMapping);
		}
	}

	/**
	 * Create the bundle file
	 * 
	 * @param servlet the servlet
	 * @param response the response
	 * @param request the request
	 * @param path the path
	 * @param destFile the destination file
	 * @param mapping the mapping
	 * @throws IOException if an IO exception occurs
	 * @throws ServletException if an exception occurs
	 */
	private void createBundleFile(HttpServlet servlet, MockServletResponse response, MockServletRequest request, String path, File destFile,
			String mapping) throws IOException, ServletException {
		
		request.setRequestPath(mapping, path);
				
		// Create the parent directory of the destination file
		if (!destFile.getParentFile().exists()) {
			boolean dirsCreated = destFile.getParentFile().mkdirs();
			if(!dirsCreated){
				throw new IOException("The directory '"+destFile.getParentFile().getCanonicalPath()+"' can't be created.");
			}
		}

		// Set the response mock to write in the destination file
		try{
			response.setOutputStream(new FileOutputStream(destFile));
			servlet.service(request, response);
		}finally{
			response.close();
		}
		
		if(destFile.length() == 0){
			logger.warn("No content retrieved for file '"+destFile.getAbsolutePath()+"', which is associated to the path : "+path);
			System.out.println("No content retrieved for file '"+destFile.getAbsolutePath()+"', which is associated to the path : "+path);
		}
	}

	/**
	 * Returns the link to the bundle
	 * 
	 * @param handler the resource bundles handler
	 * @param path the path
	 * @param variantKey the local variant key
	 * @return the link to the bundle
	 * @throws IOException if an IO exception occurs
	 */
	private List createLinkToBundle(ResourceBundlesHandler handler, String path, String resourceType, Map variantMap) throws IOException {

		List linksToBundle = new ArrayList();
		
		BasicBundleRenderer bundleRenderer = new BasicBundleRenderer(handler, resourceType);
		StringWriter sw = new StringWriter();
		
		// The gzip compression will be made by the CDN server
		// So we force it to false.
		boolean useGzip = false;
		
		// The generation of bundle is the same in SSL and non SSL mode 
		boolean isSslRequest = false;
		
		// First deals with the production mode
		handler.getConfig().setDebugModeOn(false);
		handler.getConfig().setGzipResourcesModeOn(useGzip);
		
		BundleRendererContext ctx = new BundleRendererContext("", variantMap, useGzip, isSslRequest);
		bundleRenderer.renderBundleLinks(path, ctx, sw);
		
		// Then take in account the debug mode
		handler.getConfig().setDebugModeOn(true);
		ctx = new BundleRendererContext("", variantMap, useGzip, isSslRequest);
		bundleRenderer.renderBundleLinks(path, ctx, sw);
		
		List renderedLinks = bundleRenderer.getRenderedLinks();
		// Remove context override path if it's defined.
		String contextPathOverride = handler.getConfig().getContextPathOverride();
		for (Iterator iterator = renderedLinks.iterator(); iterator.hasNext();) {
			RenderedLink renderedLink = (RenderedLink) iterator.next();
			String renderedLinkPath = renderedLink.getLink();
			// Remove the context path override
			if (StringUtils.isNotEmpty(contextPathOverride) && renderedLinkPath.startsWith(contextPathOverride)) {
				renderedLinkPath = renderedLinkPath.substring(contextPathOverride.length());
			}
			renderedLink.setLink(PathNormalizer.asPath(renderedLinkPath));
			linksToBundle.add(renderedLink);
		}
		
		return linksToBundle;
	}

	/**
	 * This is the custom class loader for Jawr Bundle processor
	 * 
	 * @author Ibrahim Chaehoi
	 *
	 */
	private static class JawrBundleProcessorCustomClassLoader extends URLClassLoader {
		
		/**
		 * Constructor
		 * @param urls the URL location for the class loading  
		 * @param parent the parent classloader
		 */
		public JawrBundleProcessorCustomClassLoader(URL[] urls, ClassLoader parent) {
			super(urls, parent);
		}

		/* (non-Javadoc)
		 * @see java.net.URLClassLoader#findResource(java.lang.String)
		 */
		public URL findResource(String name) {
			URL url = super.findResource(name);
			if(url == null && name.startsWith("/")){
				url = super.findResource(name.substring(1));
			}
			return url;
		}
	}
	
}
