/**
 * Copyright 2007-2009 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.postprocess;

import java.util.HashMap;
import java.util.Map;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * This class encapsulates the status of a bundling process. It is meant to let 
 * postprocessors have metadata available about the processed data
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class BundleProcessingStatus {
	
	/** The current bundle */
	private final JoinableResourceBundle currentBundle;
	
	/** The resource reader */
	private final ResourceReaderHandler rsReader;
	
	/** The Jawr config */
	private final JawrConfig jawrConfig;
	
	/** The last path added */
	private String lastPathAdded;
	
	/** The image resource mapping */
	private Map imgResourceMapping = new HashMap();
	
	/**
	 * Constructor
	 * @param currentBundle the current bundle
	 * @param rsHandler the resource handler
	 * @param jawrConfig the Jawr config
	 */
	public BundleProcessingStatus(final JoinableResourceBundle currentBundle,
			final ResourceReaderHandler rsHandler,final JawrConfig jawrConfig) {
		super();
		this.currentBundle = currentBundle;
		this.rsReader = rsHandler;
		this.jawrConfig = jawrConfig;
	}
	
	/**
	 * Returns the last (current) resource path added to the bundle. 
	 * @return The last (current) resource path added to the bundle. 
	 */
	public String getLastPathAdded() {
		return lastPathAdded;
	}
	
	/**
	 * Sets the last (current) resource path added to the bundle. 
	 * @param lastPathAdded the path to set
	 */
	public void setLastPathAdded(String lastPathAdded) {
		this.lastPathAdded = lastPathAdded;
	}
	
	/**
	 * Returns the currently processed bundle. 
	 * @return currently processed bundle. 
	 */
	public JoinableResourceBundle getCurrentBundle() {
		return currentBundle;
	}
	
	/**
	 * Returns the resource handler
	 * @return the resource handler
	 */
	public ResourceReaderHandler getRsReader() {
		return rsReader;
	}

	/**
	 * Returns the current Jawr config
	 * @return the current Jawr config
	 */
	public JawrConfig getJawrConfig() {
		return jawrConfig;
	}
	
	/**
	 * Returns the image resource map
	 * @return the image resource map
	 */
	public Map getImgResourceMapping() {
		return imgResourceMapping;
	}

	/**
	 * Sets the image resource map
	 * @param imgResourceMapping the map to set
	 */
	public void setImgResourceMapping(Map imgResourceMapping) {
		this.imgResourceMapping = imgResourceMapping;
	}
	
	/**
	 * Sets the image mapping for a image resource
	 * @param resourceKey the image source
	 * @param resourceValue the result imaged source
	 */
	public void setImageMapping(String resourceKey, String resourceValue){
		this.imgResourceMapping.put(resourceKey, resourceValue);
	}

	/**
	 * Gets the image mapping for a image resource
	 * @param resourceKey the image source
	 * @return the result imaged source
	 */
	public String getImageMapping(String resourceKey){
		return (String) this.imgResourceMapping.get(resourceKey);
	}

}
