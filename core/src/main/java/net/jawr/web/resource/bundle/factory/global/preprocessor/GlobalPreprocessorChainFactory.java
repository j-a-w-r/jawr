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
package net.jawr.web.resource.bundle.factory.global.preprocessor;

import java.util.Map;

import net.jawr.web.resource.bundle.global.preprocessor.GlobalPreprocessor;

/**
 * Interface for a factory of chained preprocessor objects. It is meant to ease 
 * configuration implementations. 
 * 
 * @author Ibrahim Chaehoi
 *
 */
public interface GlobalPreprocessorChainFactory {

	/**
	 * Builds the default chain of preprocessors for resources, be it javascript or css ones. 
	 * @return the default chain of preprocessors
	 */
	public abstract GlobalPreprocessor buildDefaultProcessorChain();

	/**
	 * Builds a chain of preprocessors based on a comma-separated list of postprocessor keys. 
	 * @param preprocessorKeys the comma-separated list of preprocessor keys.
	 * @return a chain of preprocessors
	 */
	public abstract GlobalPreprocessor buildProcessorChain(String preprocessorKeys);
	
	
	/**
	 * Sets a map of custom preprocessors to use. 
	 * The map has a key to name a preprocessor (to be used in bundle definitions), and 
	 * the class name of a custom processor class which must implement 
	 * net.jawr.web.resource.bundle.global.preprocessor.GlobalPreprocessor 
	 * 
	 * @param keysClassNames the map associated the keys and the class names.
	 */
	public abstract void setCustomPreprocessors(Map keysClassNames);

}
