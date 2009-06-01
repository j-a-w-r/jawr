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
package net.jawr.web.resource.bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines the content of a joinable resource bundle.
 * 
 * @author Ibrahim Chaehoi
 */
public class JoinableResourceBundleContent {

	// ~---------- Variables ----------

	/** The content */
	private StringBuffer content;
	
	/** The map associating the CSS class path and their content which is usable in debug mode */
	private Map cssClasspathDebugContentMap;

	// ~---------- Constructor ----------
	
	/**
	 * Constructor 
	 */
	public JoinableResourceBundleContent() {
		this(new StringBuffer());
	}


	/**
	 * Constructor
	 * @param sb the content
	 */
	public JoinableResourceBundleContent(StringBuffer content) {
		
		this.content = content;
		this.cssClasspathDebugContentMap = new HashMap();
	}

	// ~---------- Getters & Setters ----------

	/**
	 * Returns the content.
	 * @return the content.
	 */
	public StringBuffer getContent() {
		return content;
	}

	/**
	 * Sets the content
	 * @param content the content to set
	 */
	public void setContent(StringBuffer content) {
		this.content = content;
	}

	/**
	 * Returns the map for the CSS class path content for Debug mode
	 * @return the cssClasspathDebugContentMap the map to set
	 */
	public Map getCssClasspathDebugContentMap() {
		return cssClasspathDebugContentMap;
	}

	/**
	 * Set the map for the CSS class path content for Debug mode
	 * @param the cssClasspathDebugContentMap the map to set
	 */
	public void setCssClasspathDebugContentMap(Map cssClasspathDebugContentMap) {
		this.cssClasspathDebugContentMap = cssClasspathDebugContentMap;
	}
	
	// ~---------- Methods ----------

	/**
	 * Append a value to the bundle content
	 * @param value the value to append
	 * @return the stringBuffer content
	 */
	public StringBuffer append(String value){
		return this.content.append(content);
	}
	
	/**
	 * Append the bundle content
	 * @param bundleContent the value to append
	 */
	public void append(JoinableResourceBundleContent bundleContent){
		
		this.content.append(bundleContent.content);
		this.cssClasspathDebugContentMap.putAll(bundleContent.cssClasspathDebugContentMap);
	}
	
	/**
	 * Put a CSS class path content in the map. 
	 * @param cssPath the cssPath
	 * @param content the content
	 */
	public void putCssClasspathDebugContent(String cssPath, String content){
		
		this.cssClasspathDebugContentMap.put(cssPath, content);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.toString().hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JoinableResourceBundleContent other = (JoinableResourceBundleContent) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (cssClasspathDebugContentMap == null) {
			if (other.cssClasspathDebugContentMap != null)
				return false;
		} else if (!cssClasspathDebugContentMap.equals(other.cssClasspathDebugContentMap))
			return false;
		return true;
	}
	
}
