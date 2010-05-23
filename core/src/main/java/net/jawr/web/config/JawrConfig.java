/**
 * Copyright 2007-2009 Jordi Hernández Sellés, Ibrahim Chaehoi, Matt Ruby
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
package net.jawr.web.config;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.servlet.ServletContext;

import net.jawr.web.JawrConstant;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.hash.DefaultBundleHashcodeGenerator;
import net.jawr.web.resource.bundle.hash.BundleHashcodeGenerator;
import net.jawr.web.resource.bundle.hash.MD5BundleHascodeGenerator;
import net.jawr.web.resource.bundle.locale.DefaultLocaleResolver;
import net.jawr.web.resource.bundle.locale.LocaleResolver;
import net.jawr.web.resource.bundle.locale.LocaleVariantResolverWrapper;
import net.jawr.web.resource.bundle.renderer.CSSHTMLBundleLinkRenderer;
import net.jawr.web.resource.bundle.variant.VariantResolver;
import net.jawr.web.resource.bundle.variant.resolver.BrowserResolver;
import net.jawr.web.resource.bundle.variant.resolver.ConnectionTypeResolver;
import net.jawr.web.util.StringUtils;

/**
 * This class holds configuration details for Jawr in a given ServletContext.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * @author Matt Ruby
 */
public class JawrConfig implements Serializable {

	/** The serial version UID */
	private static final long serialVersionUID = -6243263853446050289L;

	/**
	 * The property name for the css link flavor
	 */
	public static final String JAWR_CSSLINKS_FLAVOR = "jawr.csslinks.flavor";

	/**
	 * The property name for the locale resolver
	 */
	public static final String JAWR_LOCALE_RESOLVER = "jawr.locale.resolver";

	/**
	 * The property name for the browser resolver
	 */
	public static final String JAWR_BROWSER_RESOLVER = "jawr.browser.resolver";
	
	/**
	 * The property name for the URL scheme resolver
	 */
	public static final String JAWR_URL_SCHEME_RESOLVER = "jawr.url.scheme.resolver";
	
	/**
	 * The property name for the bundle hashcode generator
	 */
	public static final String JAWR_BUNDLE_HASHCODE_GENERATOR = "jawr.bundle.hashcode.generator";
	
	/**
	 * The property name for the connection type resolver
	 */
	public static final String JAWR_CONNECTION_TYPE_SCHEME_RESOLVER = "jawr.url.connection.type.resolver";

	/**
	 * The property name for the dwr mapping
	 */
	public static final String JAWR_DWR_MAPPING = "jawr.dwr.mapping";

	/**
	 * The property name for the url context path used to override
	 */
	public static final String JAWR_URL_CONTEXTPATH_OVERRIDE = "jawr.url.contextpath.override";

	/**
	 * The property name for the url context path used to override SSL path
	 */
	public static final String JAWR_URL_CONTEXTPATH_SSL_OVERRIDE = "jawr.url.contextpath.ssl.override";

	/**
	 * The property name for the flag indicating if we should use or not the url context path override even in debug mode
	 */
	public static final String JAWR_USE_URL_CONTEXTPATH_OVERRIDE_IN_DEBUG_MODE = "jawr.url.contextpath.override.used.in.debug.mode";

	/**
	 * The property name for the Gzip IE6 flag
	 */
	public static final String JAWR_GZIP_IE6_ON = "jawr.gzip.ie6.on";

	/**
	 * The property name to force the CSS bundle in debug mode
	 */
	public static final String JAWR_DEBUG_IE_FORCE_CSS_BUNDLE = "jawr.debug.ie.force.css.bundle";
	
	/**
	 * The property name for the charset name
	 */
	public static final String JAWR_CHARSET_NAME = "jawr.charset.name";

	/**
	 * The property name for the Gzip flag
	 */
	public static final String JAWR_GZIP_ON = "jawr.gzip.on";

	/**
	 * The property name for the debug override key
	 */
	public static final String JAWR_DEBUG_OVERRIDE_KEY = "jawr.debug.overrideKey";

	/**
	 * The property name for the reload refresh key
	 */
	private static final String JAWR_CONFIG_RELOAD_REFRESH_KEY = "jawr.config.reload.refreshKey";

	/**
	 * The property name for the Debug flag
	 */
	public static final String JAWR_DEBUG_ON = "jawr.debug.on";

	/**
	 * The property name for the jawr working directory. By default it's jawrTemp in the application server working directory
	 * associated to the application.
	 */
	public static final String JAWR_WORKING_DIRECTORY = "jawr.working.directory";

	/**
	 * The property name for the flag indicating if we should process the bundle at startup
	 */
	public static final String JAWR_USE_BUNDLE_MAPPING = "jawr.use.bundle.mapping";

	/**
	 * The property name for the debug mode system flag
	 */
	private static final String DEBUG_MODE_SYSTEM_FLAG = "net.jawr.debug.on";

	/**
	 * The property name for the flag indicating if JAWR should override the CSS image url to map the classpath image servlet
	 * TODO remove this property. It's replaced by jawr.css.classpath.handling.image
	 */
	public static final String JAWR_CSS_IMG_USE_CLASSPATH_SERVLET = "jawr.css.image.classpath.use.servlet";
	
	/**
	 * The property name for the flag indicating if the CSS image for the CSS retrieved from classpath must be also retrieved from classpath
	 */
	public static final String JAWR_CSS_CLASSPATH_HANDLE_IMAGE = "jawr.css.classpath.handle.image";
	
	/**
	 * The property name for the flag indicating if the CSS image for the CSS retrieved from classpath must be also retrieved from classpath
	 */
	public static final String JAWR_CSS_SKIN_COOKIE = "jawr.css.skin.cookie";
	
	/**
	 * The property name for the image hash algorithm.
	 */
	public static final String JAWR_IMAGE_HASH_ALGORITHM = "jawr.image.hash.algorithm";

	/**
	 * The property name for the image resources.
	 */
	public static final String JAWR_IMAGE_RESOURCES = "jawr.image.resources";
	
	/**
	 * The property name for the Jawr strict mode.
	 */
	public static final String JAWR_STRICT_MODE = "jawr.strict.mode";

	/**
	 * The generator registry
	 */
	private GeneratorRegistry generatorRegistry;

	/**
	 * The local resolver
	 */
	private LocaleResolver localeResolver;

	/** The bundle hashcode generator */
	private BundleHashcodeGenerator bundleHashcodeGenerator;
	
	/**
	 * The servlet context
	 */
	private ServletContext context;

	/**
	 * The root configuration properties
	 */
	private Properties configProperties;

	/**
	 * Name of the charset to use to interpret and send resources. Defaults to UTF-8
	 */
	private String charsetName = "UTF-8";

	/**
	 * The charset to use to interpret and send resources.
	 */
	private Charset resourceCharset;

	/**
	 * Flag to switch on the strict mode. defaults to false.
	 * In strict mode, Jawr checks that the hashcode of the bundle requested is the right one or not.
	 */
	private boolean strictMode = false;
	
	/**
	 * Flag to switch on the debug mode. defaults to false.
	 */
	private boolean debugModeOn = false;
	
	/**
	 * Key that may be passed in to override production mode
	 */
	private String debugOverrideKey = "";
	
	/**
	 * Key that may be passed in to reload the bundles on the fly
	 */
	private String refreshKey = "";
		
	/**
	 * Flag to switch on the gzipped resources mode. defaults to true.
	 */
	private boolean gzipResourcesModeOn = true;

	/**
	 * Flag to switch on the gzipped resources mode for internet explorer 6. defaults to true.
	 */
	private boolean gzipResourcesForIESixOn = true;

	/**
	 * Flag to switch on css resources bundle in debug mode. defaults to false.
	 */
	private boolean forceCssBundleInDebugForIEOn = false;
	
	/**
	 * Flag which defines if we should process the bundle at server startup. defaults to false.
	 */
	private boolean useBundleMapping = false;

	/** 
	 * The jawr working directory path
	 */
	private String jawrWorkingDirectory;
	
	/**
	 * Servlet mapping corresponding to this config. Defaults to an empty string
	 */
	private String servletMapping = "";

	/**
	 * Override value to use instead of the context path of the application in generated urls. If null, contextPath is used. If blank, urls are
	 * generated to be relative.
	 */
	private String contextPathOverride;

	/**
	 * Override value to use instead of the context path of the application in generated urls for SSL page. 
	 * If null, contextPath is used. If blank, urls are generated to be relative.
	 */
	private String contextPathSslOverride;

	/**
	 * The flag indicating that we should use the overriden context path even in debug mode.
	 * The default value is false.
	 */
	private boolean useContextPathOverrideInDebugMode = false;

	/**
	 * Determines if the servlet, which provide CSS image for CSS define in the classpath should be used or not
	 */
	private boolean classpathCssHandleImage = false;

	/**
	 * Defines the image resources definition.
	 */
	private String imageResourcesDefinition;
	
	/**
	 * Defines the image hash algorithm.
	 * By default the value is CRC32. 
	 * There are only 2 algorithm available CRC32 and MD5. 
	 */
	private String imageHashAlgorithm = "CRC32";

	/**
	 * Used to check if a configuration has not been outdated by a new one.
	 */
	private boolean valid = true;

	/**
	 * Mapping path to the dwr servlet, in case it is integrated with jawr.
	 */
	private String dwrMapping;

	/** The skin cookie name*/
	private String skinCookieName = JawrConstant.JAWR_SKIN;
	
	/**
	 * Initialize configuration using params contained in the initialization properties file.
	 * 
	 * @param props the properties
	 */
	public JawrConfig(final Properties props) {
		this.configProperties = props;
		if (null != props.getProperty(JAWR_DEBUG_ON)) {
			this.debugModeOn = Boolean.valueOf(props.getProperty(JAWR_DEBUG_ON)).booleanValue();
		}
		// If system flag is available, override debug mode from properties
		if (null != System.getProperty(DEBUG_MODE_SYSTEM_FLAG)) {
			this.debugModeOn = Boolean.valueOf(System.getProperty(DEBUG_MODE_SYSTEM_FLAG)).booleanValue();
		}
		if (null != props.getProperty(JAWR_DEBUG_OVERRIDE_KEY)) {
			this.debugOverrideKey = props.getProperty(JAWR_DEBUG_OVERRIDE_KEY);
		}
		
		if (null != props.getProperty(JAWR_STRICT_MODE)) {
			this.strictMode = Boolean.valueOf(props.getProperty(JAWR_STRICT_MODE)).booleanValue();
		}
		
		if (null != props.getProperty(JAWR_USE_BUNDLE_MAPPING)) {
			this.useBundleMapping = Boolean.valueOf(props.getProperty(JAWR_USE_BUNDLE_MAPPING)).booleanValue();
		}
		
		if (null != props.getProperty(JAWR_WORKING_DIRECTORY)) {
			this.jawrWorkingDirectory = props.getProperty(JAWR_WORKING_DIRECTORY);
		}
		
		if (null != props.getProperty(JAWR_GZIP_ON)) {
			this.gzipResourcesModeOn = Boolean.valueOf(props.getProperty(JAWR_GZIP_ON)).booleanValue();
		}
		if (null != props.getProperty(JAWR_CHARSET_NAME)) {
			setCharsetName(props.getProperty(JAWR_CHARSET_NAME));
		}
		if (null != props.getProperty(JAWR_GZIP_IE6_ON)) {
			this.gzipResourcesForIESixOn = Boolean.valueOf(props.getProperty(JAWR_GZIP_IE6_ON)).booleanValue();
		}
		if (null != props.getProperty(JAWR_DEBUG_IE_FORCE_CSS_BUNDLE)) {
			this.forceCssBundleInDebugForIEOn = Boolean.valueOf(props.getProperty(JAWR_DEBUG_IE_FORCE_CSS_BUNDLE)).booleanValue();
		}
		if (null != props.getProperty(JAWR_URL_CONTEXTPATH_OVERRIDE)) {
			this.contextPathOverride = props.getProperty(JAWR_URL_CONTEXTPATH_OVERRIDE);
		}
		if (null != props.getProperty(JAWR_URL_CONTEXTPATH_SSL_OVERRIDE)) {
			this.contextPathSslOverride = props.getProperty(JAWR_URL_CONTEXTPATH_SSL_OVERRIDE);
		}
		
		if (null != props.getProperty(JAWR_USE_URL_CONTEXTPATH_OVERRIDE_IN_DEBUG_MODE)) {
			this.useContextPathOverrideInDebugMode = Boolean.valueOf(props.getProperty(JAWR_USE_URL_CONTEXTPATH_OVERRIDE_IN_DEBUG_MODE)).booleanValue();
		}
		
		if (null != props.getProperty(JAWR_CONFIG_RELOAD_REFRESH_KEY)) {
			this.refreshKey = props.getProperty(JAWR_CONFIG_RELOAD_REFRESH_KEY);
		}
		
		if (null != props.getProperty(JAWR_DWR_MAPPING)) {
			this.dwrMapping = props.getProperty(JAWR_DWR_MAPPING);
		}

		if (props.getProperty(JAWR_LOCALE_RESOLVER) == null) {
			localeResolver = new DefaultLocaleResolver();
		} else{
			localeResolver = (LocaleResolver) ClassLoaderResourceUtils.buildObjectInstance(props.getProperty(JAWR_LOCALE_RESOLVER));
		}
		
		String bundleHashCodeGenerator = props.getProperty(JAWR_BUNDLE_HASHCODE_GENERATOR, "").trim();
		if(bundleHashCodeGenerator.length() == 0 || JawrConstant.DEFAULT.equalsIgnoreCase(bundleHashCodeGenerator)){
			bundleHashcodeGenerator = new DefaultBundleHashcodeGenerator();
		}else if(JawrConstant.MD5_ALGORITHM.equalsIgnoreCase(bundleHashCodeGenerator)){
			bundleHashcodeGenerator = new MD5BundleHascodeGenerator();
		}else{
			bundleHashcodeGenerator = (BundleHashcodeGenerator) ClassLoaderResourceUtils.buildObjectInstance(bundleHashCodeGenerator);
		}
		
		if (null != props.getProperty(JAWR_CSS_SKIN_COOKIE)) {
			skinCookieName = props.getProperty(JAWR_CSS_SKIN_COOKIE).trim();
		}
		
		if (null != props.getProperty(JAWR_CSSLINKS_FLAVOR)) {
			setCssLinkFlavor(props.getProperty(JAWR_CSSLINKS_FLAVOR).trim());
		}

		if (null != props.getProperty(JAWR_CSS_IMG_USE_CLASSPATH_SERVLET)
				 && null != props.getProperty(JAWR_CSS_CLASSPATH_HANDLE_IMAGE)) {
			throw new IllegalStateException("You are using in the same configuration file '"+JAWR_CSS_IMG_USE_CLASSPATH_SERVLET+"' and '"+JAWR_CSS_CLASSPATH_HANDLE_IMAGE+"'. " +
					"The property '"+JAWR_CSS_IMG_USE_CLASSPATH_SERVLET+"' is deprecated. Please use only the property '"+JAWR_CSS_CLASSPATH_HANDLE_IMAGE+"'");
		}
		if (null != props.getProperty(JAWR_CSS_IMG_USE_CLASSPATH_SERVLET)) {
			this.classpathCssHandleImage = Boolean.valueOf(props.getProperty(JAWR_CSS_IMG_USE_CLASSPATH_SERVLET)).booleanValue();
		}
		if (null != props.getProperty(JAWR_CSS_CLASSPATH_HANDLE_IMAGE)) {
			this.classpathCssHandleImage = Boolean.valueOf(props.getProperty(JAWR_CSS_CLASSPATH_HANDLE_IMAGE)).booleanValue();
		}
		
		if (null != props.getProperty(JAWR_IMAGE_HASH_ALGORITHM)) {
			this.imageHashAlgorithm = props.getProperty(JAWR_IMAGE_HASH_ALGORITHM).trim();
		}
		
		if (null != props.getProperty(JAWR_IMAGE_RESOURCES)) {
			this.imageResourcesDefinition = props.getProperty(JAWR_IMAGE_RESOURCES).trim();
		}
		
	}
	
	/**
	 * Returns the flag indicating if we are in strict mode or not
	 * @return the strict mode flag
	 */
	public boolean isStrictMode() {
		return strictMode;
	}

	/**
	 * Sets the flag indicating if we are in strict mode or not
	 * @param strictMode the flag to set
	 */
	public void setStrictMode(boolean strictMode) {
		this.strictMode = strictMode;
	}

	/**
	 * Get the debugOverrideKey
	 * 
	 * @return the debugOverrideKey that is used to override production mode per request
	 */
	public String getDebugOverrideKey() {
		return debugOverrideKey;
	}

	/**
	 * Set the debugOverrideKey
	 * 
	 * @param debugOverrideKey the String to set as the key
	 */
	public void setDebugOverrideKey(final String debugOverrideKey) {
		this.debugOverrideKey = debugOverrideKey;
	}

	/**
	 * Get debug mode status.
	 * This flag may be overridden using the debugOverrideKey
	 * 
	 * @return the debug mode flag.
	 */
	public boolean isDebugModeOn() {
		if(!debugModeOn && ThreadLocalJawrContext.isDebugOverriden()){
			return true;
		}
		return debugModeOn;
	}

	/**
	 * Set debug mode.
	 * 
	 * @param debugModeOn the flag to set
	 */
	public void setDebugModeOn(final boolean debugMode) {
		this.debugModeOn = debugMode;
	}

	/**
	 * Returns the refresh key
	 * @return the refresh key
	 */
	public String getRefreshKey() {
		return refreshKey;
	}

	/**
	 * Sets the refresh key
	 * @param refreshKey the refresh key
	 */
	public void setRefreshKey(final String refreshKey) {
		this.refreshKey = refreshKey;
	}
	
	/**
	 * Returns the jawr working directory
	 * @return the jawr working directory
	 */
	public String getJawrWorkingDirectory() {
		return jawrWorkingDirectory;
	}

	/**
	 * Sets the flag indicating if we should process the bundle at startup
	 * @param dirPath the directory path to set
	 */
	public void setJawrWorkingDirectory(final String dirPath) {
		this.jawrWorkingDirectory = dirPath;
	}

	/**
	 * Returns the flag indicating if we should use the bundle mapping properties file.
	 * @return the flag indicating if we should use the bundle mapping properties file.
	 */
	public boolean getUseBundleMapping() {
		return useBundleMapping;
	}

	/**
	 * Sets the flag indicating if we should use the bundle mapping properties file.
	 * @param bundleMappingPath the flag to set
	 */
	public void setUseBundleMapping(boolean useBundleMapping) {
		this.useBundleMapping = useBundleMapping;
	}
	
	/**
	 * Get the charset to interpret and generate resource.
	 * 
	 * @return the resource charset
	 */
	public Charset getResourceCharset() {
		if (null == resourceCharset) {
			resourceCharset = Charset.forName(charsetName);
		}
		return resourceCharset;
	}

	/**
	 * Set the charsetname to be used to interpret and generate resource.
	 * 
	 * @param charsetName the charset name to set
	 */
	public final void setCharsetName(String charsetName) {
		if (!Charset.isSupported(charsetName))
			throw new IllegalArgumentException("The specified charset [" + charsetName + "] is not supported by the jvm.");
		this.charsetName = charsetName;
	}

	/**
	 * Returns the bundle hashcode generator
	 * @return the bundleHashcodeGenerator
	 */
	public BundleHashcodeGenerator getBundleHashcodeGenerator() {
		return bundleHashcodeGenerator;
	}

	/**
	 * Get the servlet mapping corresponding to this config.
	 * 
	 * @return the servlet mapping corresponding to this config.
	 */
	public String getServletMapping() {
		return servletMapping;
	}

	/**
	 * Set the servlet mapping corresponding to this config.
	 * 
	 * @param servletMapping the servelt mapping to set
	 */
	public void setServletMapping(String servletMapping) {
		this.servletMapping = PathNormalizer.normalizePath(servletMapping);
	}

	/**
	 * Get the flag indicating if the resource must be gzipped or not
	 * 
	 * @return the flag indicating if the resource must be gzipped or not
	 */
	public boolean isGzipResourcesModeOn() {
		return gzipResourcesModeOn;
	}

	/**
	 * Sets the flag indicating if the resource must be gzipped or not
	 * 
	 * @param gzipResourcesModeOn the flag to set
	 */
	public void setGzipResourcesModeOn(boolean gzipResourcesModeOn) {
		this.gzipResourcesModeOn = gzipResourcesModeOn;
	}

	/**
	 * Get the flag indicating if the resource must be gzipped for IE6 or less
	 * 
	 * @return the flag indicating if the resource must be gzipped for IE6 or less
	 */
	public boolean isGzipResourcesForIESixOn() {
		return gzipResourcesForIESixOn;
	}

	/**
	 * Sets the flag indicating if the resource must be gzipped for IE6 or less
	 * 
	 * @param gzipResourcesForIESixOn the flag to set.
	 */
	public void setGzipResourcesForIESixOn(boolean gzipResourcesForIESixOn) {
		this.gzipResourcesForIESixOn = gzipResourcesForIESixOn;
	}

	/**
	 * Returns the flag indicating if the CSS resources must be bundle for IE in debug mode
	 * 
	 * @return the flag indicating if the CSS resources must be bundle for IE in debug mode
	 */
	public boolean isForceCssBundleInDebugForIEOn() {
		return forceCssBundleInDebugForIEOn;
	}

	/**
	 * Sets the flag indicating if the CSS resources must be bundle for IE in debug mode
	 * 
	 * @param forceBundleCssForIEOn the flag to set
	 */
	public void setForceCssBundleInDebugForIEOn(boolean forceBundleCssForIEOn) {
		this.forceCssBundleInDebugForIEOn = forceBundleCssForIEOn;
	}

	/**
	 * Get the the string to use instead of the regular context path. If it is an empty string, urls will be relative to the path (i.e, not start with
	 * a slash).
	 * 
	 * @return The string to use instead of the regular context path.
	 */
	public String getContextPathOverride() {
		return contextPathOverride;
	}

	/**
	 * Set the string to use instead of the regular context path. If it is an empty string, urls will be relative to the path (i.e, not start with a
	 * slash).
	 * 
	 * @param contextPathOverride The string to use instead of the regular context path.
	 */
	public void setContextPathOverride(String contextPathOverride) {
		this.contextPathOverride = contextPathOverride;
	}

	/**
	 * @return the contextPathSslOverride
	 */
	public String getContextPathSslOverride() {
		return contextPathSslOverride;
	}

	/**
	 * @param contextPathSslOverride the contextPathSslOverride to set
	 */
	public void setContextPathSslOverride(String contextPathSslOverride) {
		this.contextPathSslOverride = contextPathSslOverride;
	}

	/**
	 * @return the useContextPathOverrideInDebugMode
	 */
	public boolean isUseContextPathOverrideInDebugMode() {
		return useContextPathOverrideInDebugMode;
	}

	/**
	 * @param useContextPathOverrideInDebugMode the useContextPathOverrideInDebugMode to set
	 */
	public void setUseContextPathOverrideInDebugMode(boolean useContextPathOverrideInDebugMode) {
		this.useContextPathOverrideInDebugMode = useContextPathOverrideInDebugMode;
	}

	/**
	 * Returns true if the URL of the image defines in CSS loaded from classpath, should be overridden for the classpath CSS image servlet.
	 * 
	 * @return true if the image defines in CSS load from classpath, should be overridden for the classpath CSS image servlet, false otherwise.
	 * 
	 *         So if you have a CSS define in a jar file at 'style/default/assets/myStyle.css, where you have the following statement:
	 *         background:transparent url(../../img/bkrnd/header_1_sprite.gif) no-repeat 0 0; Becomes: background:transparent
	 *         url(getCssImageServletPath()+style/default/ img/bkrnd/header_1_sprite.gif) no-repeat 0 0; And the CSS image servlet will be in charge
	 *         of loading the image from the classpath.
	 */
	public boolean isCssClasspathImageHandledByClasspathCss() {
		return classpathCssHandleImage;
	}

	/**
	 * Set the flag indicating if the URL of the image defines in CSS loaded from classpath, should be overridden for the classpath CSS image servlet.
	 * 
	 * @param useClasspathCssImgServlet the flag to set
	 * 
	 *            So if you have a CSS define in a jar file at 'style/default/assets/myStyle.css, where you have the following statement:
	 *            background:transparent url(../../img/bkrnd/header_1_sprite.gif) no-repeat 0 0; Becomes: background:transparent
	 *            url(getCssImageServletPath()+style /default/img/bkrnd/header_1_sprite.gif) no-repeat 0 0; And the CSS image servlet will be in
	 *            charge of loading the image from the classpath.
	 */
	public void setCssClasspathImageHandledByClasspathCss(boolean classpathCssHandleImage) {
		this.classpathCssHandleImage = classpathCssHandleImage;
	}

	/**
	 * Get the image hash algorithm
	 * @return the image hash algorithm
	 */
	public String getImageHashAlgorithm() {
		return imageHashAlgorithm;
	}

	/**
	 * Sets the image hash algorithm
	 * @param imageHashAlgorithm, the hash algorithm to set
	 */
	public void setImageHashAlgorithm(String imageHashAlgorithm) {
		this.imageHashAlgorithm = imageHashAlgorithm;
	}

	/**
	 * Returns the image resources definition.
	 * @return the image resources definition.
	 */
	public String getImageResourcesDefinition() {
		return imageResourcesDefinition;
	}

	/**
	 * Sets the image resources definition.
	 * @param imageResourcesDefinition the image resources definition to set
	 */
	public void setImageResourcesDefinition(String imageResourcesDefinition) {
		this.imageResourcesDefinition = imageResourcesDefinition;
	}

	/**
	 * Invalidate this configuration. Used to signal objects that have a hold on this instance but cannot be explicitly notified when the
	 * configuration is reloaded.
	 */
	public void invalidate() {
		this.valid = false;
	}

	/**
	 * Get the flag indicating if the configuration has been invalidated.
	 * 
	 * @return the flag indicating if the configuration has been invalidated.
	 */
	public boolean isValid() {
		return this.valid;
	}

	/**
	 * Get the generator registry
	 * 
	 * @return the generator registry
	 */
	public GeneratorRegistry getGeneratorRegistry() {
		return generatorRegistry;
	}

	/**
	 * Set the generator registry
	 * 
	 * @param generatorRegistry the generatorRegistry to set
	 */
	public void setGeneratorRegistry(GeneratorRegistry generatorRegistry) {
		this.generatorRegistry = generatorRegistry;
		this.generatorRegistry.setConfig(this);
		localeResolver = null;
		if (configProperties.getProperty(JAWR_LOCALE_RESOLVER) == null) {
			localeResolver = new DefaultLocaleResolver();
		} else{
			localeResolver = (LocaleResolver) ClassLoaderResourceUtils.buildObjectInstance(configProperties.getProperty(JAWR_LOCALE_RESOLVER));
		}

		this.generatorRegistry.registerVariantResolver(new LocaleVariantResolverWrapper(localeResolver));
		
		registerResolver(new BrowserResolver(), JAWR_BROWSER_RESOLVER);
		registerResolver(new ConnectionTypeResolver(), JAWR_CONNECTION_TYPE_SCHEME_RESOLVER);
		
	}

	/**
	 * Register a resolver in the generator registry
	 * @param defaultResolver the default resolver
	 * @param configPropertyName the configuration property whose the value define the resolver class
	 * @return
	 */
	private VariantResolver registerResolver(VariantResolver defaultResolver, String configPropertyName ) {
		VariantResolver resolver = null;
		if (configProperties.getProperty(configPropertyName) == null) {
			resolver = defaultResolver;
		} else{
			resolver = (VariantResolver) ClassLoaderResourceUtils.buildObjectInstance(configProperties.getProperty(configPropertyName));
		}
		
		this.generatorRegistry.registerVariantResolver(resolver);
		return resolver;
	}

	/**
	 * Get the local resolver
	 * 
	 * @return the local resolver.
	 */
	public LocaleResolver getLocaleResolver() {
		return localeResolver;
	}

	/**
	 * Get the servlet context
	 * 
	 * @return the servlet context
	 */
	public ServletContext getContext() {
		return context;
	}

	/**
	 * Set the servlet context
	 * 
	 * @param context the context to set
	 */
	public void setContext(ServletContext context) {
		this.context = context;
	}

	/**
	 * Get the dwrMapping
	 * 
	 * @return the dwrMapping
	 */
	public String getDwrMapping() {
		return dwrMapping;
	}

	/**
	 * Set the dwr mapping
	 * 
	 * @param dwrMapping the dwrMapping to set
	 */
	public void setDwrMapping(String dwrMapping) {
		this.dwrMapping = dwrMapping;
	}

	/**
	 * Get the config properties
	 * 
	 * @return the config properties
	 */
	public Properties getConfigProperties() {
		return configProperties;
	}

	/**
	 * Sets the css link flavor
	 * 
	 * @param cssLinkFlavor the cssLinkFlavor to set
	 */
	public final void setCssLinkFlavor(String cssLinkFlavor) {
		if (CSSHTMLBundleLinkRenderer.FLAVORS_HTML.equalsIgnoreCase(cssLinkFlavor)
				|| CSSHTMLBundleLinkRenderer.FLAVORS_XHTML.equalsIgnoreCase(cssLinkFlavor)
				|| CSSHTMLBundleLinkRenderer.FLAVORS_XHTML_EXTENDED.equalsIgnoreCase(cssLinkFlavor))
			CSSHTMLBundleLinkRenderer.setClosingTag(cssLinkFlavor);
		else{
			throw new IllegalArgumentException("The value for the jawr.csslinks.flavor " + "property [" + cssLinkFlavor + "] is invalid. "
					+ "Please check the docs for valid values ");
		}
	}

	/**
	 * Returns the value of the property associated to the key passed in parameter
	 * @param key the key of the property
	 * @return the value of the property
	 */
	public String getProperty(String key){
		return configProperties.getProperty(key);
	}
	
	/**
	 * Returns true if the Jawr working directory is defined in the web application.
	 * 
	 * @return true if the Jawr working directory is defined in the web application.
	 */
	public boolean isWorkingDirectoryInWebApp(){
		
		return useBundleMapping && StringUtils.isNotEmpty(jawrWorkingDirectory) && !jawrWorkingDirectory.startsWith(JawrConstant.FILE_URI_PREFIX);
	}
	
	/**
	 * Returns the skin cookie name
	 * @return the skinCookieName
	 */
	public String getSkinCookieName() {
		return skinCookieName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(65);
		sb.append("[JawrConfig:'charset name:'").append(this.charsetName).append("'\ndebugModeOn:'").append(isDebugModeOn())
				.append("'\nservletMapping:'").append(getServletMapping()).append("' ]");
		return sb.toString();
	}

}
