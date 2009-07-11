/**
 * Copyright 2009 Ibrahim Chaehoi
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
package net.jawr.web.config.jmx;

/**
 * This interface defines the MBean which manage the Jawr configuration for a servlet.
 * 
 * @author Ibrahim Chaehoi
 */
public interface JawrConfigManagerMBean {

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#getContextPathOverride()
	 */
	public String getContextPathOverride();


	/**
	 * @param contextPathOverride
	 * @see net.jawr.web.config.JawrConfig#setContextPathOverride(java.lang.String)
	 */
	public void setContextPathOverride(String contextPathOverride);

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#getContextPathOverride()
	 */
	public String getContextPathSslOverride();


	/**
	 * @param contextPathOverride
	 * @see net.jawr.web.config.JawrConfig#setContextPathOverride(java.lang.String)
	 */
	public void setContextPathSslOverride(String contextPathOverride);

	/**
	 * @param contextPathOverride
	 * @see net.jawr.web.config.JawrConfig#setUseContextPathOverrideInDebugMode(boolean)
	 */
	public void setUseContextPathOverrideInDebugMode(boolean useContextPathOverrideInDebugMode);

	/**
	 * @param contextPathOverride
	 * @see net.jawr.web.config.JawrConfig#getUseContextPathOverrideInDebugMode()
	 */
	public boolean getUseContextPathOverrideInDebugMode();

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#getDebugOverrideKey()
	 */
	public String getDebugOverrideKey();

	/**
	 * @param debugOverrideKey
	 * @see net.jawr.web.config.JawrConfig#setDebugOverrideKey(java.lang.String)
	 */
	public void setDebugOverrideKey(String debugOverrideKey);

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#getDwrMapping()
	 */
	public String getDwrMapping();

	/**
	 * @param dwrMapping
	 * @see net.jawr.web.config.JawrConfig#setDwrMapping(java.lang.String)
	 */
	public void setDwrMapping(String dwrMapping);

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#getImageBundleDefinition()
	 */
	public String getImageBundleDefinition();

	/**
	 * @param imageBundleDefinition
	 * @see net.jawr.web.config.JawrConfig#setImageBundleDefinition(java.lang.String)
	 */
	public void setImageBundleDefinition(String imageBundleDefinition);

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#getImageHashAlgorithm()
	 */
	public String getImageHashAlgorithm();

	/**
	 * @param imageHashAlgorithm
	 * @see net.jawr.web.config.JawrConfig#setImageHashAlgorithm(java.lang.String)
	 */
	public void setImageHashAlgorithm(String imageHashAlgorithm);

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#isDebugModeOn()
	 */
	public boolean isDebugModeOn();

	/**
	 * @param debugMode
	 * @see net.jawr.web.config.JawrConfig#setDebugModeOn(boolean)
	 */
	public void setDebugModeOn(boolean debugMode);

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#isGzipResourcesForIESixOn()
	 */
	public boolean isGzipResourcesForIESixOn();

	/**
	 * @param gzipResourcesForIESixOn
	 * @see net.jawr.web.config.JawrConfig#setGzipResourcesForIESixOn(boolean)
	 */
	public void setGzipResourcesForIESixOn(boolean gzipResourcesForIESixOn);

	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#isGzipResourcesModeOn()
	 */
	public boolean isGzipResourcesModeOn();

	/**
	 * @param gzipResourcesModeOn
	 * @see net.jawr.web.config.JawrConfig#setGzipResourcesModeOn(boolean)
	 */
	public void setGzipResourcesModeOn(boolean gzipResourcesModeOn);
	
	/**
	 * @return
	 * @see net.jawr.web.config.JawrConfig#isUsingClasspathCssImageServlet()
	 */
	public boolean isUsingClasspathCssImageServlet();

	/**
	 * @param useClasspathCssImgServlet
	 * @see net.jawr.web.config.JawrConfig#setUseClasspathCssImageServlet(boolean)
	 */
	public void setUsingClasspathCssImageServlet(boolean useClasspathCssImgServlet);
	
	/**
	 * @param charsetName
	 * @see net.jawr.web.config.JawrConfig#getCharsetName(java.lang.String)
	 */
	public String getCharsetName();

	/**
	 * @param charsetName
	 * @see net.jawr.web.config.JawrConfig#setCharsetName(java.lang.String)
	 */
	public void setCharsetName(String charsetName);

	/**
	 * @param cssLinkFlavor
	 * @see net.jawr.web.config.JawrConfig#setCssLinkFlavor(java.lang.String)
	 */
	public String getCssLinkFlavor();

	/**
	 * @param cssLinkFlavor
	 * @see net.jawr.web.config.JawrConfig#setCssLinkFlavor(java.lang.String)
	 */
	public void setCssLinkFlavor(String cssLinkFlavor);

	/**
	 * Returns the flag which defines if we should process the bundle at server startup. defaults to false.
	 * @return the flag which defines if we should process the bundle at server startup.
	 */
	public boolean isUseBundleMapping();

	/**
	 * Sets the flag which defines if we should process the bundle at server startup. 
	 * @param usBundleMapping the flag to set
	 */
	public void setUseBundleMapping(boolean usBundleMapping);

	/** 
	 * Returns the jawr working directory path
	 * @return the jawr working directory path
	 */
	public String getJawrWorkingDirectory();
	
	/** 
	 * Sets the jawr working directory path
	 * @param jawrWorkingDirectory the path to set
	 */
	public void setJawrWorkingDirectory(String jawrWorkingDirectory);
	
	/**
	 * Refresh the configuration. 
	 */
	public void refreshConfig();
	
}
