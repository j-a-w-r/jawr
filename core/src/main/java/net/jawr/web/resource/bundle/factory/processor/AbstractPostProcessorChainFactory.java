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
package net.jawr.web.resource.bundle.factory.processor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.EmptyResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.CustomPostProcessorChainWrapper;
import net.jawr.web.resource.bundle.postprocess.impl.LicensesIncluderPostProcessor;

/**
 * Abstract implementation of the PostProcessorChainFactory with functionalities common to js and css resources.  
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public abstract class AbstractPostProcessorChainFactory implements	PostProcessorChainFactory {

	/** The map of custom postprocessor */
	private Map customPostProcessors;
	
	/**
	 * Constructor 
	 */
	public AbstractPostProcessorChainFactory() {
		this.customPostProcessors = new HashMap();
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.factory.processor.PostProcessorChainFactory#buildPostProcessorChain(java.lang.String)
	 */
	public ResourceBundlePostProcessor buildPostProcessorChain(String processorKeys) {
		if(null == processorKeys)
			return null;
		else if(PostProcessFactoryConstant.NO_POSTPROCESSING_KEY.equals(processorKeys))
			return new EmptyResourceBundlePostProcessor();
		
		StringTokenizer tk = new StringTokenizer(processorKeys,",");
		
		AbstractChainedResourceBundlePostProcessor chain = null;
		while(tk.hasMoreTokens())
			chain = addOrCreateChain(chain,tk.nextToken());
		
		return chain;
	}
	
	/**
	 * Creates an AbstractChainedResourceBundlePostProcessor. If the supplied chain is null, the new chain is returned. Otherwise it
	 * is added to the existing chain.  
	 * @param chain the chained post processor
	 * @param key the id of the post processor
	 * @return the chained post processor, with the new post processor.
	 */
	private AbstractChainedResourceBundlePostProcessor addOrCreateChain(AbstractChainedResourceBundlePostProcessor chain, String key) {
		
		AbstractChainedResourceBundlePostProcessor toAdd;
		
		if(null != customPostProcessors.get(key)) {
			toAdd = (AbstractChainedResourceBundlePostProcessor)customPostProcessors.get(key);
		}
		else toAdd = buildProcessorByKey(key);
		
		if(null == chain){
			chain = toAdd;
		}
		else{
			
			chain.addNextProcessor(toAdd);
		}
		
		return chain;
	}
	
	/**
	 * Builds an AbstractChainedResourceBundlePostProcessor based on the supplied key. If the key doesn't match
	 * any PostProcessor (as defined in the documentation), an IllegalArgumentException is thrown. 
	 * @param key the key name of the post processor
	 * @return the chained post processor
	 */
	protected abstract AbstractChainedResourceBundlePostProcessor buildProcessorByKey(String key);
	

	/**
	 * Builds an instance of LicensesIncluderPostProcessor. 
	 * @return an instance of LicensesIncluderPostProcessor. 
	 */
	protected LicensesIncluderPostProcessor buildLicensesProcessor() {
		return new LicensesIncluderPostProcessor();
	}

	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.factory.processor.PostProcessorChainFactory#setCustomPostprocessors(java.util.Map)
	 */
	public void setCustomPostprocessors(Map keysClassNames) {
		for(Iterator it = keysClassNames.entrySet().iterator(); it.hasNext();){
			
			Entry entry = (Entry) it.next();
			ResourceBundlePostProcessor customProcessor = 
				(ResourceBundlePostProcessor) ClassLoaderResourceUtils.buildObjectInstance((String) entry.getValue());
			
			String key = (String) entry.getKey();			
			customPostProcessors.put(key, new CustomPostProcessorChainWrapper(key, customProcessor));
			
		}		
	}

}
