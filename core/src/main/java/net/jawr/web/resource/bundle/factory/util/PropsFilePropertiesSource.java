/**
 * Copyright 2008  Jordi Hernández Sellés
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
package net.jawr.web.resource.bundle.factory.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * ConfigPropertiesSource implementation that reads its values from a .properties file. 
 * @author Jordi Hernández Sellés
 */
public class PropsFilePropertiesSource implements ConfigPropertiesSource {

	private static final Logger log = Logger.getLogger(PropsFilePropertiesSource.class.getName());
	
	private String configLocation;
	private int propsHashCode;
	
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.factory.util.ConfigPropertiesSource#getConfigProperties()
	 */
	public Properties getConfigProperties() {
		if(log.isDebugEnabled())
			log.debug("Reading properties from file at classpath: " + configLocation);
		
		return readConfigProperties();
	}

	/**
	 * Reads config properties from the classpath route specified by configLocation
	 * @return
	 */
	protected Properties readConfigProperties() {
		Properties props = new Properties();	
		
		// Load properties file
		try {	
			InputStream is = ClassLoaderResourceUtils.getResourceAsStream(configLocation,this);
			
			// load properties into a Properties object
			props.load(is);
			
			// Initialize hashcode of newly loaded properties. 
			if(0 == this.propsHashCode)
				this.propsHashCode = props.hashCode();
		} 
		catch (IOException e) {
			throw new IllegalArgumentException("jawr configuration could not be found. "
										+ "Make sure init-param configLocation is properly set "
										+ "in web.xml and that it points to a file in the classpath. ");
		}
		return props;
	}

	/**
	 * @param configLocation the configLocation to set
	 */
	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.factory.util.ConfigPropertiesSource#configChanged()
	 */
	public boolean configChanged() {
		int currentConfigHash = readConfigProperties().hashCode();
		
		boolean configChanged = this.propsHashCode != currentConfigHash;
		
		if(configChanged && log.isDebugEnabled())
			log.debug("Changes in configuration properties file detected.");
			
		this.propsHashCode = currentConfigHash;
		
		return configChanged;
	}

}
