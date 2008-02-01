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

import java.io.IOException;

import org.apache.log4j.Logger;


/**
 * Chained abstract implementation of ResourceBundlePostProcessor. Implementations
 * can be used as a processing chain. 
 * 
 * @author Jordi Hernández Sellés
 *
 */ 
public abstract class AbstractChainedResourceBundlePostProcessor implements
		ResourceBundlePostProcessor {
	private ResourceBundlePostProcessor nextProcessor;
	private static final Logger log = Logger.getLogger(ResourceBundlePostProcessor.class);
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor#postProcessBundle(java.lang.StringBuffer)
	 */
	public StringBuffer postProcessBundle(BundleProcessingStatus status, StringBuffer bundleData) {
		StringBuffer processedBundle = null;
		try {
			if(log.isInfoEnabled())
				log.info("postprocessing bundle:" + status.getCurrentBundle());
			processedBundle = doPostProcessBundle(status,bundleData);
		} catch (IOException e) {
			throw new RuntimeException("Unexpected IOException during execution of a postprocessor.");
		}
		if(null != nextProcessor) {
			processedBundle = nextProcessor.postProcessBundle(status,processedBundle);
		}		
		return processedBundle;
	}
	
	/**
	 * Set the next post processor in the chain. 
	 * @param nextProcessor
	 */
	public void setNextProcessor(ResourceBundlePostProcessor nextProcessor) {
		this.nextProcessor = nextProcessor;
	}
	
	/**
	 * Postprocess a bundle of resources in the context of this chain of processors. 
	 * @param bundleData
	 * @return
	 * @throws IOException
	 */
	protected abstract StringBuffer doPostProcessBundle(BundleProcessingStatus status, StringBuffer bundleData) throws IOException;
}
