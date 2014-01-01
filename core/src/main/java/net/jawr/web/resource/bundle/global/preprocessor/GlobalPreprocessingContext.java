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
package net.jawr.web.resource.bundle.global.preprocessor;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * This class defines the context for the global preprocessing
 * 
 * @author Ibrahim Chaehoi
 */
public class GlobalPreprocessingContext {

	/** The resource handler */
	private ResourceReaderHandler rsHandler;

	/** The Jawr config */
	private JawrConfig config;
	
	/** The flag indicating if the bundle must be processed */
	private boolean bundleMustBePreprocessed;
	
	/**
	 * Constructor
	 */
	public GlobalPreprocessingContext(JawrConfig config, ResourceReaderHandler resourceHandler, boolean processBundle) {
		
		this.config = config;
		this.rsHandler = resourceHandler;
		this.bundleMustBePreprocessed = processBundle;
	}

	/**
	 * Returns the config
	 * @return the config
	 */
	public JawrConfig getJawrConfig() {
		return config;
	}

	/**
	 * Returns the resource reader handler.
	 * @return the resource reader Handler
	 */
	public ResourceReaderHandler getRsReaderHandler() {
		return rsHandler;
	}

	/**
	 * Sets the resource handler 
	 * @param rsHandler the rsHandler to set
	 */
	public void setRsReaderHandler(ResourceReaderHandler rsHandler) {
		this.rsHandler = rsHandler;
	}

	/**
	 * Returns true if the bundles will be processed
	 * @return true if the bundles will be processed
	 */
	public boolean hasBundleToBePreprocessed() {
		return bundleMustBePreprocessed;
	}
	
}
