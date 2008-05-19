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
package net.jawr.web.config;

import java.nio.charset.Charset;
import java.util.Properties;

import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generated.GeneratorRegistry;
import net.jawr.web.resource.bundle.locale.DefaultLocaleResolver;
import net.jawr.web.resource.bundle.locale.LocaleResolver;


/**
 * This class holds configuration details for Jawr in a given ServletContext. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class JawrConfig {
	private static final String DEBUG_MODE_SYSTEM_FLAG = "net.jawr.debug.on"; 
	private GeneratorRegistry generatorRegistry;
	private LocaleResolver localeResolver;
	
	/**
	 * Initialize configuration using params contained in the initialization properties file. 
	 * @param configId
	 * @param props
	 */
	public JawrConfig(Properties props) 
	{
		
		if(null != props.getProperty("jawr.debug.on"))
		{			
			setDebugModeOn(Boolean.valueOf(props.getProperty("jawr.debug.on")).booleanValue());
		}	
		// If system flag is available, override debug mode from properties
		if(null != System.getProperty(DEBUG_MODE_SYSTEM_FLAG)) {
			setDebugModeOn(Boolean.valueOf(System.getProperty(DEBUG_MODE_SYSTEM_FLAG)).booleanValue());
		}
		if(null != props.getProperty("jawr.gzip.on"))
		{			
			setGzipResourcesModeOn(Boolean.valueOf(props.getProperty("jawr.gzip.on")).booleanValue());
		}	
		if(null != props.getProperty("jawr.charset.name"))
		{			
			setCharsetName(props.getProperty("jawr.charset.name"));
		}	
		if(null != props.getProperty("jawr.gzip.ie6.on"))
		{			
			setGzipResourcesForIESixOn(Boolean.valueOf(props.getProperty("jawr.gzip.ie6.on")).booleanValue());
		}		
		if(null != props.getProperty("jawr.url.contextpath.override"))
		{			
			setContextPathOverride(props.getProperty("jawr.url.contextpath.override"));
		}
		
		if(null != props.getProperty("jawr.locale.resolver")) {
			localeResolver = (LocaleResolver) ClassLoaderResourceUtils.buildObjectInstance(props.getProperty("jawr.locale.resolver"));
		}
		else localeResolver = new DefaultLocaleResolver();
			
		this.generatorRegistry = new GeneratorRegistry();
	}
	
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
	 * Override value to use instead of the context path of the application in generated urls. If null, contextPath is used. If blank, 
	 * urls are generated to be relative. 
	 */
	private String contextPathOverride;
	
	/**
	 * Used to check if a configuration has not been outdated by a new one. 
	 */
	private boolean isValid = true;
	
		
	/**
	 * Get debug mode status. 
	 * @return boolean
	 */
	public boolean isDebugModeOn() {
		return debugModeOn;
	}

	/**
	 * Set debug mode. 
	 * @param debugModeOn boolean
	 */
	public void setDebugModeOn(boolean debugMode) {
		this.debugModeOn = debugMode;
	}


	
	
	/**
	 * Get the charset to interpret and generate resource. 
	 * @return Charset
	 */
	public Charset getResourceCharset() {
		if(null == resourceCharset)
		{
			resourceCharset = Charset.forName(charsetName);
		}
		return resourceCharset;
	}
	
	/**
	 * Set the charsetname to be used to interpret and generate resource. 
	 * @param charsetName
	 */
	public void setCharsetName(String charsetName) {
		if(!Charset.isSupported(charsetName))
			throw new IllegalArgumentException("The specified charset [" + charsetName + "] is not supported by the jvm.");
		this.charsetName = charsetName;
	}
	

	/**
	 * Get the servlet mapping corresponding to this config. 
	 * @return
	 */
	public String getServletMapping() {
		return servletMapping;
	}

	/**
	 * Set the servlet mapping corresponding to this config. 
	 * @param servletMapping
	 */
	public void setServletMapping(String servletMapping) {
		this.servletMapping = PathNormalizer.normalizePath(servletMapping);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("[JawrConfig:'")
			.append("charset name:'")
			.append(this.charsetName)
			.append("'\n")
			.append("debugModeOn:'")
			.append(isDebugModeOn())
			.append("'\n")
			.append("servletMapping:'")
			.append(getServletMapping())
			.append("' ]");
		return sb.toString();
	}

	/**
	 * @return the gzipResourcesModeOn
	 */
	public boolean isGzipResourcesModeOn() {
		return gzipResourcesModeOn;
	}

	/**
	 * @param gzipResourcesModeOn the gzipResourcesModeOn to set
	 */
	public void setGzipResourcesModeOn(boolean gzipResourcesModeOn) {
		this.gzipResourcesModeOn = gzipResourcesModeOn;
	}
    
        /**
	 * @return Wether gzipping for IE6 or less is on. 
	 */
        public boolean isGzipResourcesForIESixOn() {
            return gzipResourcesForIESixOn;
        }

        /**
	 * @param Wether gzipping for IE6 or less will be on. 
	 */
        public void setGzipResourcesForIESixOn(boolean gzipResourcesForIESixOn) {
            this.gzipResourcesForIESixOn = gzipResourcesForIESixOn;
        }

		/**
		 * @return The string to use instead of the regular context path. 
		 */
		public String getContextPathOverride() {
			return contextPathOverride;
		}

		/**
		 * Set the string to use instead of the regular context path. If it is an empty string, 
		 * urls will be relative to the path (i.e, not start with a slash). 
		 * @param contextPathOverride The string to use instead of the regular context path. 
		 */
		public void setContextPathOverride(String contextPathOverride) {
			this.contextPathOverride = contextPathOverride;
		}

		/**
		 * Invalidate this configuration. Used to signal objects that have a hold on this instance but 
		 * cannot be explicitly notified when the configuration is reloaded. 
		 */
		public void invalidate() {
			this.isValid = false;
		}
		
		/**
		 * @return Whether this configuration has been invalidated. 
		 */
		public boolean isValid() {
			return this.isValid;
		}

		/**
		 * @return the generatorRegistry
		 */
		public GeneratorRegistry getGeneratorRegistry() {
			return generatorRegistry;
		}

		/**
		 * @param generatorRegistry the generatorRegistry to set
		 */
		public void setGeneratorRegistry(GeneratorRegistry generatorRegistry) {
			this.generatorRegistry = generatorRegistry;
		}

		public LocaleResolver getLocaleResolver() {
			return localeResolver;
		}
	
}
