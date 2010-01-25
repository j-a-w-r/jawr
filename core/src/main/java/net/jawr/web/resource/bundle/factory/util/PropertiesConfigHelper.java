/**
 * Copyright 2007-2009 Jordi Hernández Sellés, Ibrahim Chaehoi
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.jawr.web.resource.bundle.factory.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jawr.web.resource.bundle.factory.PropertiesBundleConstant;

/**
 * Helper class to make properties access less verbose.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public class PropertiesConfigHelper {
	
	/** The properties */
	private Properties props;
	
	/** The prefix of the properties */
	private String prefix;
	
	/** The bundle name pattern */
	private Pattern bundleNamePattern;
	
	/** The post processor class name pattern */
	private Pattern postProcessorClassPattern = Pattern.compile("(jawr\\.custom\\.postprocessors\\.)([-_a-zA-Z0-9]+).class");

	/** The global preprocessor class name pattern */
	private Pattern globalPreProcessorClassPattern = Pattern.compile("(jawr\\.custom\\.global\\.preprocessor\\.)([-_a-zA-Z0-9]+).class");
	
	/**
	 * Build a properties wrapper that appends 'jawr.' and the specified
	 * resourceType to a a supplied key before retrieveing its value from the
	 * properties.
	 * 
	 * @param props
	 *            Properties to wrap
	 * @param resourceType
	 *            resource type to use.
	 */
	public PropertiesConfigHelper(Properties props, String resourceType) {
		super();
		this.props = props;
		this.prefix = PropertiesBundleConstant.PROPS_PREFIX + resourceType + ".";
		String bundle = prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PROPERTY;
		String pattern = "(" + bundle.replaceAll("\\.", "\\\\.")
				+ ")([-_a-zA-Z0-9]+)\\.id";
		this.bundleNamePattern = Pattern.compile(pattern);
	}

	/**
	 * Returns the value of the common property, or the default value if no value is defined
	 * instead.
	 * @param key the key of the property
	 * @param defaultValue the default value
	 * @return the value of the common property
	 */
	public String getCommonProperty(String key, String defaultValue) {
		return props.getProperty(PropertiesBundleConstant.PROPS_PREFIX + key, defaultValue);
	}

	/**
	 * Returns the value of the common property
	 * @param key the key of the property
	 * @return the value of the common property
	 */
	public String getCommonProperty(String key) {
		return props.getProperty(PropertiesBundleConstant.PROPS_PREFIX + key);
	}

	/**
	 * Returns as a set, the comma separated values of a property 
	 * @param key the key of the property
	 * @return a set of the comma separated values of a property 
	 */
	public Set getCommonPropertyAsSet(String key) {
		Set propertiesSet = new HashSet();
		StringTokenizer tk = new StringTokenizer(props.getProperty(PropertiesBundleConstant.PROPS_PREFIX+key, ""),
				",");
		while (tk.hasMoreTokens())
			propertiesSet.add(tk.nextToken().trim());
		return propertiesSet;
	}
	
	/**
	 * Returns the value of the custom bundle property, or the default value if no value is defined
	 * @param bundleName the bundle name
	 * @param key the key of the property
	 * @param defaultValue the default value
	 * @return the value of the custom bundle property, or the default value if no value is defined
	 */
	public String getCustomBundleProperty(String bundleName, String key,
			String defaultValue) {
		return props.getProperty(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PROPERTY
				+ bundleName + key, defaultValue);
	}

	/**
	 * Returns the value of the custom bundle property, or the default value if no value is defined
	 * @param bundleName the bundle name
	 * @param key the key of the property
	 * @return the value of the custom bundle property
	 */
	public String getCustomBundleProperty(String bundleName, String key) {
		return props.getProperty(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PROPERTY
				+ bundleName + key);
	}

	/**
	 * Returns as a set, the comma separated values of a property 
	 * @param key the key of the property
	 * @return a set of the comma separated values of a property 
	 */
	public Set getCustomBundlePropertyAsSet(String bundleName, String key) {
		Set propertiesSet = new HashSet();
		StringTokenizer tk = new StringTokenizer(getCustomBundleProperty(bundleName, key, ""),
				",");
		while (tk.hasMoreTokens())
			propertiesSet.add(tk.nextToken().trim());
		return propertiesSet;
	}
	
	/**
	 * Returns as a set, the comma separated values of a property 
	 * @param key the key of the property
	 * @return a set of the comma separated values of a property 
	 */
	public Set getPropertyAsSet(String key) {
		Set propertiesSet = new HashSet();
		StringTokenizer tk = new StringTokenizer(props.getProperty(prefix+key, ""),
				",");
		while (tk.hasMoreTokens())
			propertiesSet.add(tk.nextToken().trim());
		return propertiesSet;
	}

	/**
	 * Returns the value of a property, or the default value if no value is defined
	 * @param key the key of the property
	 * @param defaultValue the default value
	 * @return the value of a property, or the default value if no value is defined
	 */
	public String getProperty(String key, String defaultValue) {
		return props.getProperty(prefix + key, defaultValue);
	}

	/**
	 * Returns the set of names for the bundles 
	 * @return the set of names for the bundles 
	 */
	public Set getPropertyBundleNameSet() {
		
		Set bundleNameSet = new HashSet();

		for (Iterator it = props.keySet().iterator();it.hasNext();) {
			Object key = it.next();
			Matcher matcher = bundleNamePattern.matcher((String) key);
			if (matcher.matches()) {

				String id = matcher.group(2);
				bundleNameSet.add(id);
			}
		}
		return bundleNameSet;
	}

	/**
	 * Returns the set of post processor name based on the class definition
	 * @return the set of post processor name based on the class definition
	 */
	public Map getCustomPostProcessorMap() {
		return getCustomMap(postProcessorClassPattern);
	}
	
	/**
	 * Returns the map of custom global preprocessor
	 * @return the map of custom global preprocessor
	 */
	public Map getCustomGlobalPreprocessorMap() {
		return getCustomMap(globalPreProcessorClassPattern);
	}
	
	/**
	 * Returns the map, where the key is the 2 group of the pattern and the value is the property value
	 * @param keyPattern the pattern of the key
	 * @return the map.
	 */
	private Map getCustomMap(Pattern keyPattern) {
		Map map = new HashMap();

		for (Iterator it = props.keySet().iterator();it.hasNext();) {
			String key = (String) it.next();
			Matcher matcher = keyPattern.matcher(key);
			if (matcher.matches()) {

				String id = matcher.group(2);
				String propertyValue = props.getProperty(key);
				map.put(id, propertyValue);
			}
		}
		return map;
	}
	
	/**
	 * Appends the prefix (jawr.) to the specified key and reads it from the
	 * properties object.
	 * 
	 * @param key the suffix of the key property 
	 * @return the value of the property jawr.+key
	 */
	public String getProperty(String key) {
		return props.getProperty(prefix + key);
	}

}
