/**
 * Copyright 2009 Ibrahim Chaehoi
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.jawr.web.resource.bundle.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.factory.processor.PostProcessorChainFactory;
import net.jawr.web.resource.bundle.factory.util.PropertiesConfigHelper;
import net.jawr.web.util.StringUtils;

/**
 * This factory is used to build JoinableResourceBundle from the generated properties mapping file, 
 * which contains all calculated information about the bundle.
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class FullMappingPropertiesBasedBundlesHandlerFactory {

	/** The post processor chain factory */
	private PostProcessorChainFactory chainFactory;

	/** The resource type */
	private String resourceType;
	
	/** The resource handler */
	private ResourceHandler rsHandler;
	
	/**
	 * Create a PropertiesBasedBundlesHandlerFactory using the specified properties.
	 * 
	 * @param resourceType js or css
	 * @param rsHandler ResourceHandler to access files.
	 * @param generatorRegistry the generator registry
	 * @param chainFactory the post processor chain factory
	 */
	public FullMappingPropertiesBasedBundlesHandlerFactory(String resourceType, 
			ResourceHandler rsHandler,
			PostProcessorChainFactory chainFactory) {

		this.resourceType = resourceType;
		this.chainFactory = chainFactory;
		this.rsHandler = rsHandler;
	}

	/**
	 * Returns the list of joinable resource bundle
	 * 
	 * @return the list of joinable resource bundle
	 */
	public List getResourceBundles(Properties properties) {
		
		PropertiesConfigHelper props = new PropertiesConfigHelper(properties, resourceType);
		String fileExtension = "." + resourceType;

		// Initialize custom bundles
		List customBundles = new ArrayList();
		// Check if we should use the bundle names property or
		// find the bundle name using the bundle id declaration :
		// jawr.<type>.bundle.<name>.id
		if (null != props
				.getProperty(PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_NAMES)) {
			StringTokenizer tk = new StringTokenizer(
					props
							.getProperty(PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_NAMES),
					",");
			while (tk.hasMoreTokens()) {
				customBundles.add(buildJoinableResourceBundle(props, tk.nextToken()
						.trim(), fileExtension, rsHandler));
			}
		} else {
			Iterator bundleNames = props.getPropertyBundleNameSet().iterator();
			while (bundleNames.hasNext()) {
				customBundles.add(buildJoinableResourceBundle(props, 
						(String) bundleNames.next(), fileExtension,
						rsHandler));
			}
		}
		
		return customBundles;
	}

	/**
	 * Create a JoinableResourceBundle based on the properties file.
	 * 
	 * @param props the properties config helper
	 * @param bundleName the bundle name
	 * @param rsHandler the resource handler
	 * @return BundleDefinition
	 */
	private JoinableResourceBundle buildJoinableResourceBundle(
			PropertiesConfigHelper props, String bundleName, String fileExtension,
			ResourceHandler rsHandler) {

		// Id for the bundle
		String bundleId = props.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID);

		InclusionPattern inclusionPattern = getInclusionPattern(props, bundleName);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl(
				bundleId, bundleName, fileExtension, inclusionPattern,
				rsHandler);

		// Override bundle postprocessor
		String bundlePostProcessors = props.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR);
		if (StringUtils.isNotEmpty(bundlePostProcessors)) {
			bundle.setBundlePostProcessor(chainFactory
					.buildPostProcessorChain(bundlePostProcessors));
		}

		// Override unitary postprocessor
		String unitaryPostProcessors = props
				.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR);
		if (StringUtils.isNotEmpty(unitaryPostProcessors)) {
			bundle.setUnitaryPostProcessor(chainFactory
					.buildPostProcessorChain(unitaryPostProcessors));
		}

		// Set conditional comment for IE, in case one is specified
		String explorerConditionalCondition = props
				.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION);
		if (StringUtils.isNotEmpty(explorerConditionalCondition)) {
			bundle
					.setExplorerConditionalExpression(explorerConditionalCondition);
		}

		// Sets the alternate URL for production mode.
		String alternateProductionURL = props
				.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL);
		if (StringUtils.isNotEmpty(alternateProductionURL)) {
			bundle
					.setAlternateProductionURL(props
							.getCustomBundleProperty(
									bundleName,
									PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL));
		}
		
		// Sets the licence path lists.
		Set licencePathList = props
				.getCustomBundlePropertyAsSet(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_LICENCE_PATH_LIST);
		if (!licencePathList.isEmpty()) {
			bundle.setLicensesPathList(licencePathList);
		}

		String mappingsProperty = props.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS);
		if (null == mappingsProperty) {
			throw new IllegalArgumentException(
					"No mappings were defined for the bundle with name:"
							+ bundleName
							+ ". Please specify at least one in configuration. ");
		}
		
		// Add the mappings
		List mappings = new ArrayList();
		StringTokenizer tk = new StringTokenizer(mappingsProperty, ",");
		while (tk.hasMoreTokens()) {
			String mapping = tk.nextToken().trim();
			mappings.add(mapping);
		}

		bundle.setMappings(mappings);
		
		Set localeKeys = new HashSet();
		Set tmpLocales = props.getCustomBundlePropertyAsSet(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_LOCALE_VARIANTS);
		for (Iterator iterator = tmpLocales.iterator(); iterator.hasNext();) {
			String localVariantKey = (String) iterator.next();
			if(StringUtils.isNotEmpty(localVariantKey)){
				localeKeys.add(localVariantKey);
				String hashcode = props.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT+localVariantKey);
				bundle.setBundleDataHashCode(localVariantKey, hashcode);
			}
		}
		
		if(!localeKeys.isEmpty()){
			bundle.setLocaleVariantKeys(Collections.list(Collections
					.enumeration(localeKeys)));
		}
		
		String hashcode = props.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE);
		bundle.setBundleDataHashCode(null, hashcode);
	
		
		return bundle;
	}

	/**
	 * Returns the inclusion pattern for a bundle
	 * 
	 * @param props the properties helper
	 * @param bundleName the bundle name
	 * @return the inclusion pattern for a bundle
	 */
	private InclusionPattern getInclusionPattern(PropertiesConfigHelper props, String bundleName) {
		// Wether it's global or not
		boolean isGlobal = Boolean
				.valueOf(
						props
								.getCustomBundleProperty(
										bundleName,
										PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG,
										"false")).booleanValue();

		// Set order if its a global bundle
		int order = 0;
		if (isGlobal) {
			order = Integer.parseInt(props.getCustomBundleProperty(bundleName,
					PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ORDER, "0"));
		}

		// Use only with debug mode on
		boolean isDebugOnly = Boolean
				.valueOf(
						props
								.getCustomBundleProperty(
										bundleName,
										PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY,
										"false")).booleanValue();

		// Use only with debug mode off
		boolean isDebugNever = Boolean
				.valueOf(
						props
								.getCustomBundleProperty(
										bundleName,
										PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER,
										"false")).booleanValue();

		return new InclusionPattern(isGlobal, order, isDebugOnly, isDebugNever);
	}

}
