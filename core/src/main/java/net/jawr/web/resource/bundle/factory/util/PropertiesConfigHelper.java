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
package net.jawr.web.resource.bundle.factory.util;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Helper class to make properties access less verbose. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class PropertiesConfigHelper {
	private static final String PROPS_PREFIX = "jawr.";
	private static final String BUNDLE_FACTORY_CUSTOM_PROPERTY = "bundle.";
	
	private Properties props;
	private String prefix;
	
	/**
	 * Build a properties wrapper that appends 'jawr.' and the specified resourceType 
	 * to a a supplied key before  retrieveing its value from the properties.   
	 * @param props Properties to wrap
	 * @param resourceType resource type to use. 
	 */
	public PropertiesConfigHelper(Properties props, String resourceType) {
		super();
		this.props = props;
		this.prefix = PROPS_PREFIX + resourceType + ".";
	}
	
	public String getCommonProperty(String key, String defaultValue) {
		return props.getProperty(PROPS_PREFIX + key, defaultValue);
	}
	public String getCommonProperty(String key) {
		return props.getProperty(PROPS_PREFIX + key);
	}

	public String getCustomBundleProperty(String bundleName, String key, String defaultValue) {
		return props.getProperty(prefix + BUNDLE_FACTORY_CUSTOM_PROPERTY + bundleName + key, defaultValue);
	}
	
	public String getCustomBundleProperty(String bundleName, String key) {
		return props.getProperty(prefix + BUNDLE_FACTORY_CUSTOM_PROPERTY + bundleName + key);
	}

	public Set getPropertyAsSet(String key) {
		Set propertiesSet = new HashSet();
		StringTokenizer tk = new StringTokenizer(props.getProperty(key, ""),",");
		while(tk.hasMoreTokens())
			propertiesSet.add(tk.nextToken().trim());
		return propertiesSet;
	}
	
	public String getProperty(String key, String defaultValue) {
		return props.getProperty(prefix + key, defaultValue);
	}

	/**
	 * Appends the prefix (jawr.) to the specified key and reads it from the properties object. 
	 * @param key
	 * @return
	 */
	public String getProperty(String key) {
		return props.getProperty(prefix + key);
	}
	

}
