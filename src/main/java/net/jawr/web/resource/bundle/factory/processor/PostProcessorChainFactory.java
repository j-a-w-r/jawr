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
package net.java.jawr.web.resource.bundle.factory.processor;

import net.java.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;

/**
 * Interface for a factory of chained PostProcessor objects. It is meant to ease 
 * configuration implementations. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public interface PostProcessorChainFactory {

	/**
	 * Builds the default chain for resources, be it javascript or css ones. 
	 * @return
	 */
	public abstract ResourceBundlePostProcessor buildDefaultProcessorChain();

	/**
	 * Builds the default unitary resource chain for resources. 
	 * @return
	 */
	public abstract ResourceBundlePostProcessor buildDefaultUnitProcessorChain();

	/**
	 * Builds a chain based on a comma-separated list of postprocessor keys. 
	 * @param processorKeys
	 * @return
	 */
	public abstract ResourceBundlePostProcessor buildPostProcessorChain(String processorKeys);

}