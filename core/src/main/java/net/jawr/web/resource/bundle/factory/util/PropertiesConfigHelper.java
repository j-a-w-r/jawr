/**
 * Copyright 2007-2009 Jordi Hernández Sellés, Ibrahim CHAEHOI
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to make properties access less verbose.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim CHAEHOI
 * 
 */
public class PropertiesConfigHelper {
	private static final String PROPS_PREFIX = "jawr.";
	private static final String BUNDLE_FACTORY_CUSTOM_PROPERTY = "bundle.";

	private Properties props;
	private String prefix;
	private Pattern bundleNamePattern;

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
		this.prefix = PROPS_PREFIX + resourceType + ".";
		String bundle = prefix + BUNDLE_FACTORY_CUSTOM_PROPERTY;
		String pattern = "(" + bundle.replaceAll("\\.", "\\\\.")
				+ ")([a-zA-Z0-9]+)\\.id";
		this.bundleNamePattern = Pattern.compile(pattern);
	}

	public String getCommonProperty(String key, String defaultValue) {
		return props.getProperty(PROPS_PREFIX + key, defaultValue);
	}

	public String getCommonProperty(String key) {
		return props.getProperty(PROPS_PREFIX + key);
	}

	public String getCustomBundleProperty(String bundleName, String key,
			String defaultValue) {
		return props.getProperty(prefix + BUNDLE_FACTORY_CUSTOM_PROPERTY
				+ bundleName + key, defaultValue);
	}

	public String getCustomBundleProperty(String bundleName, String key) {
		return props.getProperty(prefix + BUNDLE_FACTORY_CUSTOM_PROPERTY
				+ bundleName + key);
	}

	public Set getPropertyAsSet(String key) {
		Set propertiesSet = new HashSet();
		StringTokenizer tk = new StringTokenizer(props.getProperty(key, ""),
				",");
		while (tk.hasMoreTokens())
			propertiesSet.add(tk.nextToken().trim());
		return propertiesSet;
	}

	public String getProperty(String key, String defaultValue) {
		return props.getProperty(prefix + key, defaultValue);
	}

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
	 * Appends the prefix (jawr.) to the specified key and reads it from the
	 * properties object.
	 * 
	 * @param key
	 * @return
	 */
	public String getProperty(String key) {
		return props.getProperty(prefix + key);
	}

}
