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
package net.jawr.web.resource.bundle.factory.processor;

import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.JSMinPostProcessor;

/**
 * PostProcessorChainFactory for javascript resources. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class JSPostProcessorChainFactory extends AbstractPostProcessorChainFactory implements PostProcessorChainFactory {
	
	private static final String JSMIN = "JSMin";

		
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.factory.processor.PostProcessorChainFactory#buildDefaultProcessor()
	 */
	public ResourceBundlePostProcessor buildDefaultProcessorChain() {
		JSMinPostProcessor processor = buildJSMinPostProcessor();
		processor.setNextProcessor(buildLicensesProcessor());
		return processor;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.factory.processor.PostProcessorChainFactory#buildDefaultUnitProcessor()
	 */
	public ResourceBundlePostProcessor buildDefaultUnitProcessorChain() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.factory.processor.PostProcessorChainFactory#getPostProcessor(java.lang.String)
	 */
	protected AbstractChainedResourceBundlePostProcessor buildProcessorByKey(String procesorKey){
		if(JSMIN.equals(procesorKey))
			return buildJSMinPostProcessor();
		else if (LICENSE_INCLUDER.equals(procesorKey))
			return buildLicensesProcessor();
		else throw new IllegalArgumentException("The supplied key [" + procesorKey + "] is not bound to any ResourceBundlePostProcessor. Please check the documentation for valid keys. ");
	}
	
	private JSMinPostProcessor buildJSMinPostProcessor() {
		return new JSMinPostProcessor();
	}
	
}
