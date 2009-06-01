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

import java.nio.charset.Charset;
import java.util.Properties;

import javax.servlet.ServletContext;

import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.locale.DefaultLocaleResolver;
import net.jawr.web.resource.bundle.locale.LocaleResolver;
import net.jawr.web.resource.bundle.renderer.CSSHTMLBundleLinkRenderer;

/**
 * This class holds configuration details for Jawr in a given ServletContext.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * @author Matt Ruby
 */
public class JawrConfig {

	/**
	 * The property name for the debug mode system flag
	 */
	private static final String DEBUG_MODE_SYSTEM_FLAG = "net.jawr.debug.on";

	/**
	 * The property name for the flag indicating if JAWR should override the CSS image url to map the classpath image servlet
	 */
	public static final String JAWR_CSS_IMG_USE_CLASSPATH_SERVLET = "jawr.css.image.classpath.use.servlet";

	/**
	 * The property name for the image hash algorithm.
	 */
	private static final String JAWR_IMAGE_HASH_ALGORITHM = "jawr.image.hash.algorithm";

	/**
	 * The generator registry
	 */
	private GeneratorRegistry generatorRegistry;

	/**
	 * The local resolver
	 */
	private LocaleResolver localeResolver;

	/**
	 * The servlet context
	 */
	private ServletContext context;

	/**
	 * The root configuration properties
	 */
	private Properties configProperties;

	/**
	 * Name of the charset to use to interprest and sen resources. Defaults to UTF-8
	 */
	private String charsetName = "UTF-8";

	/**
	 * The charset to use to interprest and sen resources.
	 */
	private Charset resourceCharset;

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
	 * Servlet mapping corresponding to this config. Defaults to an empty string
	 */
	private String servletMapping = "";

	/**
	 * Override value to use instead of the context path of the application in generated urls. If null, contextPath is used. If blank, urls are
	 * generated to be relative.
	 */
	private String contextPathOverride;

	/**
	 * Determines if the contextPathOverride is a full domain path (http://.... or https://...)
	 */
	private boolean isDomainOverriden;

	/**
	 * Override value to use instead of the relative path of the application when css urls paths are re-written. urls are generated to be relative if
	 * this is not set. (//cssbackgroundimageserverlocation)
	 * 
	 * Note that absolute urls will not be re-written in the css files.
	 */
	private String cssImagePathOverride;

	/**
	 * Determines if the servlet, which provide CSS image for CSS define in the classpath should be used or not
	 */
	private boolean useClasspathCssImageServlet;

	/**
	 * Defines the image hash algorithm.
	 * By default the value is CRC32. 
	 * There are only 2 algorithm available CRC32 and MD5. 
	 */
	private String imageHashAlgorithm = "CRC32";

	/**
	 * Used to check if a configuration has not been outdated by a new one.
	 */
	private boolean isValid = true;

	/**
	 * Mapping path to the dwr servlet, in case it is integrated with jawr.
	 */
	private String dwrMapping;

	/**
	 * Initialize configuration using params contained in the initialization properties file.
	 * 
	 * @param props the properties
	 */
	public JawrConfig(Properties props) {
		this.configProperties = props;
		if (null != props.getProperty("jawr.debug.on")) {
			setDebugModeOn(Boolean.valueOf(props.getProperty("jawr.debug.on")).booleanValue());
		}
		// If system flag is available, override debug mode from properties
		if (null != System.getProperty(DEBUG_MODE_SYSTEM_FLAG)) {
			setDebugModeOn(Boolean.valueOf(System.getProperty(DEBUG_MODE_SYSTEM_FLAG)).booleanValue());
		}
		if (null != props.getProperty("jawr.debug.overrideKey")) {
			setDebugOverrideKey(props.getProperty("jawr.debug.overrideKey"));
		}
		if (null != props.getProperty("jawr.config.reload.refreshKey")) {
			setRefreshKey(props.getProperty("jawr.config.reload.refreshKey"));
		}
		if (null != props.getProperty("jawr.gzip.on")) {
			setGzipResourcesModeOn(Boolean.valueOf(props.getProperty("jawr.gzip.on")).booleanValue());
		}
		if (null != props.getProperty("jawr.charset.name")) {
			setCharsetName(props.getProperty("jawr.charset.name"));
		}
		if (null != props.getProperty("jawr.gzip.ie6.on")) {
			setGzipResourcesForIESixOn(Boolean.valueOf(props.getProperty("jawr.gzip.ie6.on")).booleanValue());
		}
		if (null != props.getProperty("jawr.url.contextpath.override")) {
			setContextPathOverride(props.getProperty("jawr.url.contextpath.override"));
		}
		if (null != props.getProperty("jawr.dwr.mapping")) {
			setDwrMapping(props.getProperty("jawr.dwr.mapping"));
		}

		if (null != props.getProperty("jawr.locale.resolver")) {
			localeResolver = (LocaleResolver) ClassLoaderResourceUtils.buildObjectInstance(props.getProperty("jawr.locale.resolver"));
		} else
			localeResolver = new DefaultLocaleResolver();

		if (null != props.getProperty("jawr.csslinks.flavor")) {
			setCssLinkFlavor(props.getProperty("jawr.csslinks.flavor").trim());
		}

		if (null != props.getProperty("jawr.css.imagepath.override")) {
			setCssImagePathOverride(props.getProperty("jawr.css.imagepath.override").trim());
		}

		if (null != props.getProperty(JAWR_CSS_IMG_USE_CLASSPATH_SERVLET)) {
			setUseClasspathCssImageServlet(Boolean.valueOf(props.getProperty(JAWR_CSS_IMG_USE_CLASSPATH_SERVLET)).booleanValue());
		}

		if (null != props.getProperty(JAWR_IMAGE_HASH_ALGORITHM)) {
			setImageHashAlgorithm(props.getProperty(JAWR_IMAGE_HASH_ALGORITHM).trim());
		}
		
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
	public void setDebugOverrideKey(String debugOverrideKey) {
		this.debugOverrideKey = debugOverrideKey;
	}

	/**
	 * Get the isDomainOverriden flag.
	 * 
	 * @return the isDomainOverriden attribute, which determines if the contextPathOverride is a full domain path (http://.... or https://...)
	 */
	public boolean isDomainOverriden() {
		return isDomainOverriden;
	}

	/**
	 * Get debug mode status.
	 * This flag may be overridden using the debugOverrideKey
	 * 
	 * @return the debug mode flag.
	 */
	public boolean isDebugModeOn() {
		if(!debugModeOn && ThreadLocalDebugOverride.getDebugOverride().booleanValue()){
			return true;
		}
		return debugModeOn;
	}

	/**
	 * Set debug mode.
	 * 
	 * @param debugModeOn the flag to set
	 */
	public void setDebugModeOn(boolean debugMode) {
		this.debugModeOn = debugMode;
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
	public void setCharsetName(String charsetName) {
		if (!Charset.isSupported(charsetName))
			throw new IllegalArgumentException("The specified charset [" + charsetName + "] is not supported by the jvm.");
		this.charsetName = charsetName;
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
		if (null != contextPathOverride
				&& (contextPathOverride.startsWith("http://") || contextPathOverride.startsWith("https://") || contextPathOverride.startsWith("//")))
			this.isDomainOverriden = true;
	}

	/**
	 * Get the path which will be used to override CSS image.
	 * 
	 * @return The string that will be prepended to css url paths after the ../'s have been removed.
	 * 
	 *         So: background:transparent url(../../img/bkrnd/header_1_sprite.gif) no-repeat 0 0; Becomes: background:transparent
	 *         url(getCssImagePathOverride()+img/bkrnd/header_1_sprite.gif) no-repeat 0 0;
	 */
	public String getCssImagePathOverride() {
		return cssImagePathOverride;
	}

	/**
	 * Set the path which will be used to override CSS image.
	 * 
	 * @param The string that will be prepended to css url paths after the ../'s have been removed.
	 * 
	 *            So: background:transparent url(../../img/bkrnd/header_1_sprite.gif) no-repeat 0 0; Becomes: background:transparent
	 *            url(getCssImagePathOverride()+img/bkrnd/header_1_sprite.gif) no-repeat 0 0;
	 */
	public void setCssImagePathOverride(String cssImagePathOverride) {
		this.cssImagePathOverride = cssImagePathOverride;
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
	public boolean isUsingClasspathCssImageServlet() {
		return useClasspathCssImageServlet;
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
	public void setUseClasspathCssImageServlet(boolean useClasspathCssImgServlet) {
		this.useClasspathCssImageServlet = useClasspathCssImgServlet;
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
	 * Invalidate this configuration. Used to signal objects that have a hold on this instance but cannot be explicitly notified when the
	 * configuration is reloaded.
	 */
	public void invalidate() {
		this.isValid = false;
	}

	/**
	 * Get the flag indicating if the configuration has been invalidated.
	 * 
	 * @return the flag indicating if the configuration has been invalidated.
	 */
	public boolean isValid() {
		return this.isValid;
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
	 * @param cssLinkFlavor the cssLinkFlavor to set
	 */
	public void setCssLinkFlavor(String cssLinkFlavor) {
		if (CSSHTMLBundleLinkRenderer.FLAVORS_HTML.equalsIgnoreCase(cssLinkFlavor)
				|| CSSHTMLBundleLinkRenderer.FLAVORS_XHTML.equalsIgnoreCase(cssLinkFlavor)
				|| CSSHTMLBundleLinkRenderer.FLAVORS_XHTML_EXTENDED.equalsIgnoreCase(cssLinkFlavor))
			CSSHTMLBundleLinkRenderer.setClosingTag(cssLinkFlavor);
		else
			throw new IllegalArgumentException("The value for the jawr.csslinks.flavor " + "property [" + cssLinkFlavor + "] is invalid. "
					+ "Please check the docs for valid values ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[JawrConfig:'").append("charset name:'").append(this.charsetName).append("'\n").append("debugModeOn:'").append(isDebugModeOn())
				.append("'\n").append("servletMapping:'").append(getServletMapping()).append("' ]");
		return sb.toString();
	}

	public String getRefreshKey() {
		return refreshKey;
	}

	public void setRefreshKey(String refreshKey) {
		this.refreshKey = refreshKey;
	}

}
