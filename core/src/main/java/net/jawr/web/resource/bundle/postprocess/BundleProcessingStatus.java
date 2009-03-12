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
package net.jawr.web.resource.bundle.postprocess;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.bundle.JoinableResourceBundle;

/**
 * This class encapsulates the status of a bundling process. It is meant to let 
 * postprocessors have metadata available about the processed data
 * 
 * @author Jordi Hernández Sellés
 */
public class BundleProcessingStatus {
	
	final JoinableResourceBundle currentBundle;
	final ResourceHandler rsHandler;
	final JawrConfig jawrConfig;
	String lastPathAdded;
	
	public BundleProcessingStatus(final JoinableResourceBundle currentBundle,
			final ResourceHandler rsHandler,final JawrConfig jawrConfig) {
		super();
		this.currentBundle = currentBundle;
		this.rsHandler = rsHandler;
		this.jawrConfig = jawrConfig;
	}
	
	/**
	 * @return String The last (current) resource path added to the bundle. 
	 */
	public String getLastPathAdded() {
		return lastPathAdded;
	}
	public void setLastPathAdded(String lastPathAdded) {
		this.lastPathAdded = lastPathAdded;
	}
	
	/**
	 * @return JoinableResourceBundle Currently processed bundle. 
	 */
	public JoinableResourceBundle getCurrentBundle() {
		return currentBundle;
	}
	
	/**
	 * @return ResourceHandler 
	 */
	public ResourceHandler getRsHandler() {
		return rsHandler;
	}

	/**
	 * @return Current config. 
	 */
	public JawrConfig getJawrConfig() {
		return jawrConfig;
	}
	
	

}
