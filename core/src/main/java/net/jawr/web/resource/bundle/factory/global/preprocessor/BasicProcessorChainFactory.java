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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.global.preprocessor.AbstractChainedGlobalPreprocessor;
import net.jawr.web.resource.bundle.global.preprocessor.CustomGlobalPreprocessorChainedWrapper;
import net.jawr.web.resource.bundle.global.preprocessor.EmptyGlobalPreprocessor;
import net.jawr.web.resource.bundle.global.preprocessor.GlobalPreprocessor;
import net.jawr.web.resource.bundle.global.preprocessor.css.smartsprites.CssSmartSpritesGlobalPreprocessor;

/**
 * This class defines the global preprocessor factory.
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class BasicProcessorChainFactory implements
		GlobalPreprocessorChainFactory {

	/** The user-defined preprocessors */
	private Map customPreprocessors = new HashMap();

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.jawr.web.resource.bundle.factory.global.preprocessor.
	 * GlobalPreprocessorChainFactory#setCustomGlobalPreprocessors(java.util.Map)
	 */
	public void setCustomGlobalPreprocessors(Map keysClassNames) {
		
		for(Iterator it = keysClassNames.entrySet().iterator(); it.hasNext();){
			
			Entry entry = (Entry) it.next();
			GlobalPreprocessor customGlobalPreprocessor = 
				(GlobalPreprocessor) ClassLoaderResourceUtils.buildObjectInstance((String) entry.getValue());
			
			String key = (String) entry.getKey();			
			customPreprocessors.put(key, new CustomGlobalPreprocessorChainedWrapper(key, customGlobalPreprocessor));
		}		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.factory.processor.ProcessorChainFactory#
	 * buildDefaultProcessorChain()
	 */
	public GlobalPreprocessor buildDefaultProcessorChain() {

		return new EmptyGlobalPreprocessor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.factory.processor.ProcessorChainFactory#
	 * buildProcessorChain(java.lang.String)
	 */
	public GlobalPreprocessor buildProcessorChain(String processorKeys) {

		if (null == processorKeys)
			return null;
		else if (JawrConstant.EMPTY_GLOBAL_PREPROCESSOR_ID
				.equals(processorKeys))
			return new EmptyGlobalPreprocessor();

		StringTokenizer tk = new StringTokenizer(processorKeys, ",");

		AbstractChainedGlobalPreprocessor chain = null;
		while (tk.hasMoreTokens())
			chain = addOrCreateChain(chain, tk.nextToken());

		return chain;
	}

	/**
	 * Creates an AbstractChainedGlobalPreprocessor. If the supplied
	 * chain is null, the new chain is returned. Otherwise it is added to the
	 * existing chain.
	 * 
	 * @param chain
	 *            the chained post processor
	 * @param key
	 *            the id of the post processor
	 * @return the chained post processor, with the new post processor.
	 */
	private AbstractChainedGlobalPreprocessor addOrCreateChain(
			AbstractChainedGlobalPreprocessor chain, String key) {

		AbstractChainedGlobalPreprocessor toAdd;

		if (customPreprocessors.get(key) == null) {
			toAdd = buildProcessorByKey(key);
		} else{
			toAdd = (AbstractChainedGlobalPreprocessor) customPreprocessors
				.get(key);
		}
		
		AbstractChainedGlobalPreprocessor newChainResult = null;
		if (chain == null) {
			newChainResult = toAdd;
		}else{
			chain.addNextProcessor(toAdd);
			newChainResult = chain;
		}

		return newChainResult;
	}

	/**
	 * Build the global preprocessor from the ID given in parameter
	 * 
	 * @param key the ID of the preprocessor
	 * @return a global preprocessor
	 */
	private AbstractChainedGlobalPreprocessor buildProcessorByKey(String key) {

		AbstractChainedGlobalPreprocessor processor = null;

		if (key.equals(JawrConstant.GLOBAL_CSS_SMARTSPRITES_PREPROCESSOR_ID)) {
			processor = new CssSmartSpritesGlobalPreprocessor();
		}

		return processor;
	}

}
