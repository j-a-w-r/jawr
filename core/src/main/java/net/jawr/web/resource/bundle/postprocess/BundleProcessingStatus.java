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
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.bundle.variant.VariantUtils;
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
	
	/** The flag indicating if we are processing a composite bundle */
	private boolean compositeBundle;
	
	/** The flag indicating if we are processing the child of a composite bundle */
	private boolean childCompositeBundle;
	
	/** The flag indicating if the post processor must search for post process variants or not */
	private boolean searchingPostProcessorVariants = true;
	
	/** The map of current variants of the bundle to process  */
	private Map bundleVariants = new HashMap();
	
	/** The map of variants which must be generated from the post processors */
	private Map postProcessVariants = new HashMap();
	
	/** The map containing the data used by the processor */
	private Map dataMap = new HashMap();
	
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
	 * Returns true if it's a composite bundle
	 * @return true if it's a composite bundle
	 */
	public boolean isCompositeBundle() {
		return compositeBundle;
	}

	/**
	 * Sets the flag indicating that we are processing a composite bundle
	 * @param compositeBundle the flag to set
	 */
	public void setCompositeBundle(boolean compositeBundle) {
		this.compositeBundle = compositeBundle;
	}

	/**
	 * Returns true if we are processing a child composite bundle
	 * @return true if we are processing a child composite bundle
	 */
	public boolean isChildCompositeBundle() {
		return childCompositeBundle;
	}

	/**
	 * Sets the flag indicating if we are processing a child composite bundle
	 * @param childCompositeBundle the flag to set
	 */
	public void setChildCompositeBundle(boolean childCompositeBundle) {
		this.childCompositeBundle = childCompositeBundle;
	}

	/**
	 * Returns true if we are searching for post processor variants.
	 * @return true if we are searching for post processor variants.
	 */
	public boolean isSearchingPostProcessorVariants() {
		return searchingPostProcessorVariants;
	}

	/**
	 * Sets the flag indicating if we are searching for post processor variants.
	 * @param searchingPostProcessVariants the flag to set
	 */
	public void setSearchingPostProcessorVariants(boolean searchingPostProcessorVariants) {
		this.searchingPostProcessorVariants = searchingPostProcessorVariants;
	}

	/**
	 * Returns the current bundle variants used for the processing
	 * @return the bundle variants
	 */
	public Map getBundleVariants() {
		return bundleVariants;
	}

	/**
	 * Sets the current bundle variants used for the processing
	 * @param bundleVariants the bundle variants to set
	 */
	public void setBundleVariants(Map bundleVariants) {
		this.bundleVariants = bundleVariants;
	}

	/**
	 * Returns the current variant for the variant type specified in parameter
	 * @param variantType the variant type
	 * @return the current variant
	 */
	public String getVariant(String variantType) {
		String variant = null;
		if (bundleVariants != null) {
			variant = (String) bundleVariants.get(variantType);
		}
		return variant;
	}
	
	/**
	 * Returns the extra bundle variants generated by the post processors
	 * @return the variants
	 */
	public Map getPostProcessVariants() {
		return postProcessVariants;
	}

	/**
	 * Sets the extra bundle variants generated by the post processors
	 * @param postProcessVariants the postProcessVariants to set
	 */
	public void setPostProcessVariants(Map postProcessVariants) {
		this.postProcessVariants = postProcessVariants;
	}

	/**
	 * Add a post process variant
	 * @param variantType the variant type
	 * @param variantSet the variant set
	 */
	public void adPostProcessVariant(String variantType, VariantSet variantSet){
		
		Map variantMap = new HashMap();
		variantMap.put(variantType, variantSet);
		adPostProcessVariant(variantMap);
	}
	
	/**
	 * Add a post process variant
	 * @param variantType the variant type
	 * @param variantSet the variant set
	 */
	public void adPostProcessVariant(Map variants){
		
		postProcessVariants = VariantUtils.concatVariants(postProcessVariants, variants);
	}
	
	/**
	 * Sets the data using the key
	 * @param key the key
	 * @param value the value
	 */
	public void putData(String key, Object value){
		dataMap.put(key, value);
	}
	
	/**
	 * Gets the data from its key
	 * @param key th key
	 * @return the data
	 */
	public Object getData(String key){
		return dataMap.get(key);
	}
	
}
