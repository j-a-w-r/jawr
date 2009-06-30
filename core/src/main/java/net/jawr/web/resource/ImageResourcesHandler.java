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
package net.jawr.web.resource;

import java.util.HashMap;
import java.util.Map;

import net.jawr.web.config.JawrConfig;

/**
 * This class defines the image resource handler.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class ImageResourcesHandler {

	/** The image map */
	private Map imageMap = new HashMap();
	
	/** The Jawr config */
	private JawrConfig jawrConfig;
	
	/**
	 * Constructor
	 * @param config
	 */
	public ImageResourcesHandler(JawrConfig config){
		this.jawrConfig = config;
	}
	
	/**
	 * @return the jawrConfig
	 */
	public JawrConfig getJawrConfig() {
		return jawrConfig;
	}

	/**
	 * @param jawrConfig the jawrConfig to set
	 */
	public void setJawrConfig(JawrConfig jawrConfig) {
		this.jawrConfig = jawrConfig;
	}
	
	/**
	 * Returns the image map
	 * @return the image Map
	 */
	public Map getImageMap() {
		return imageMap;
	}

	/**
	 * Add an image mapping
	 * @param imgUrl the original url
	 * @param cacheUrl the cache url
	 */
	public void addMapping(String imgUrl, String cacheUrl){
		imageMap.put(imgUrl, cacheUrl);
	}
	
	/**
	 * Return the cache image URL
	 * @param imgUrl the image url
	 * @return the cache image URL
	 */
	public String getCacheUrl(String imgUrl){
		return (String) imageMap.get(imgUrl);
	}
	
	/**
	 * Clears the image map 
	 */
	public void clear(){
	
		imageMap.clear();
	}
	
}
