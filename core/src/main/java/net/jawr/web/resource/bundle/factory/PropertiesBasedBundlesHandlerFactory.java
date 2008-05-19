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
package net.jawr.web.resource.bundle.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.bundle.factory.util.PropertiesConfigHelper;
import net.jawr.web.resource.bundle.factory.util.ResourceBundleDefinition;
import net.jawr.web.resource.bundle.generated.GeneratorRegistry;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;

/**
 * Properties based configuration entry point. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class PropertiesBasedBundlesHandlerFactory {
	
	private static final String RESOURCES_BASEDIR = "bundle.basedir";
	private static final String RESOURCES_USE_CACHE = "use.cache";
	
	// Single bundle switch and param. 
	private static final String FACTORY_USE_SINGLE_BUNDLE = "factory.use.singlebundle";
	private static final String FACTORY_SINGLE_FILE_NAME = "factory.singlebundle.bundlename";
	
	// Dir mapper switch
	private static final String FACTORY_USE_DIR_MAPPER = "factory.use.dirmapper";
	private static final String FACTORY_DIR_MAPPER_EXCLUSION = "factory.dirmapper.excluded";


	// Which postprocessors to use. 
	private static final String BUNDLE_FACTORY_POSTPROCESSOR = "bundle.factory.bundlepostprocessors";
	private static final String BUNDLE_FACTORY_FILE_POSTPROCESSOR = "bundle.factory.filepostprocessors";
	
	// Custom bundle factory parameters
	private static final String BUNDLE_FACTORY_CUSTOM_NAMES = "bundle.names";
	private static final String BUNDLE_FACTORY_CUSTOM_ID = ".id";
	private static final String BUNDLE_FACTORY_CUSTOM_MAPPINGS = ".mappings";
	private static final String BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG = ".global";
	private static final String BUNDLE_FACTORY_CUSTOM_ORDER = ".order";
	private static final String BUNDLE_FACTORY_CUSTOM_DEBUGONLY = ".debugonly";
	private static final String BUNDLE_FACTORY_CUSTOM_DEBUGNEVER = ".debugnever";
	private static final String BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR = ".bundlepostprocessors";
	private static final String BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR = ".filepostprocessors";
	private static final String BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION = ".ieonly.condition";

	private static final String BUNDLE_FACTORY_CUSTOM_COMPOSITE_FLAG = ".composite";
	private static final String BUNDLE_FACTORY_CUSTOM_COMPOSITE_NAMES = ".child.names";
	
	// Custom postprocessors factory parameters
	private static final String CUSTOM_POSTPROCESSORS = "jawr.custom.postprocessors";
	private static final String CUSTOM_POSTPROCESSORS_NAMES = ".names";
	private static final String CUSTOM_POSTPROCESSORS_CLASS = ".class";
	
	// Locale variants

	private static final String BUNDLE_FACTORY_CUSTOM_LOCALE_VARIANTS = ".locales";
		
	
	private PropertiesConfigHelper props;
	private BundlesHandlerFactory factory;
	
	
	/**
	 * Create a PropertiesBasedBundlesHandlerFactory using the specified properties. 
	 * @param properties
	 * @param resourceType js or css
	 * @param rsHandler ResourceHandler to access files. 
	 */
	public PropertiesBasedBundlesHandlerFactory(Properties properties, String resourceType,ResourceHandler rsHandler,GeneratorRegistry generatorRegistry){
		this.props = new PropertiesConfigHelper(properties,resourceType);
		
		// Create the BundlesHandlerFactory
		factory = new BundlesHandlerFactory();
		factory.setResourceHandler(rsHandler);
		factory.setBundlesType(resourceType);
		
		// Root resources dir
		factory.setBaseDir(props.getProperty(RESOURCES_BASEDIR,"/"));
		
		// Use cache by default
		factory.setUseInMemoryCache(Boolean.valueOf(props.getProperty(RESOURCES_USE_CACHE,"true")).booleanValue());
		
		// Postprocessor definitions
		factory.setGlobalPostProcessorKeys(props.getProperty(BUNDLE_FACTORY_POSTPROCESSOR));
		factory.setUnitPostProcessorKeys(props.getProperty(BUNDLE_FACTORY_FILE_POSTPROCESSOR));
		
		// Single or multiple bundle for orphans settings.  
		factory.setUseSingleResourceFactory(Boolean.valueOf(props.getProperty(FACTORY_USE_SINGLE_BUNDLE,"false")).booleanValue());
		factory.setSingleFileBundleName(props.getProperty(FACTORY_SINGLE_FILE_NAME));
		
		// Use the automatic directory-as-bundle mapper. 
		factory.setUseDirMapperFactory(Boolean.valueOf(props.getProperty(FACTORY_USE_DIR_MAPPER,"false")).booleanValue());
		factory.setExludedDirMapperDirs(props.getPropertyAsSet(FACTORY_DIR_MAPPER_EXCLUSION));
		
		// Initialize custom bundles
		Set customBundles = null;
		if(null != props.getProperty(BUNDLE_FACTORY_CUSTOM_NAMES)) {
			customBundles = new HashSet();
			StringTokenizer tk = new StringTokenizer(props.getProperty(BUNDLE_FACTORY_CUSTOM_NAMES),",");
			while(tk.hasMoreTokens())
				customBundles.add(buildCustomBundleDefinition(tk.nextToken().trim(),false));
		}
		
		// Read custom postprocessor definitions
		if(null != properties.getProperty(CUSTOM_POSTPROCESSORS + CUSTOM_POSTPROCESSORS_NAMES)) {
			Map customPostprocessors = new HashMap();
			StringTokenizer tk = new StringTokenizer(properties.getProperty(CUSTOM_POSTPROCESSORS + CUSTOM_POSTPROCESSORS_NAMES),",");
			
			while(tk.hasMoreTokens()) {
				String processorKey = tk.nextToken();
				String processorClass = properties.getProperty(CUSTOM_POSTPROCESSORS + "." + processorKey + CUSTOM_POSTPROCESSORS_CLASS);
				if(null != processorClass) 
					customPostprocessors.put(processorKey, processorClass);
			}
			factory.setCustomPostprocessors(customPostprocessors);
			
		}
		
		factory.setBundleDefinitions(customBundles);
	}
	
	/**
	 * Build a resources handler based on the configuration. 
	 * @param jawrConfig
	 * @return
	 * @throws DuplicateBundlePathException 
	 */
	public ResourceBundlesHandler buildResourceBundlesHandler(JawrConfig jawrConfig) throws DuplicateBundlePathException {		
		factory.setJawrConfig(jawrConfig);
		return factory.buildResourceBundlesHandler();		
	}
	
	
	/**
	 * Create a BundleDefinition based on the properties file.  
	 * @param bundleName
	 * @return
	 */
	private ResourceBundleDefinition buildCustomBundleDefinition(String bundleName, boolean isChildBundle) {

		// Id for the bundle
		String bundleId = props.getCustomBundleProperty(bundleName,BUNDLE_FACTORY_CUSTOM_ID);
		if(null == bundleId && !isChildBundle)
			throw new IllegalArgumentException("No id defined for the bundle with name:" + bundleName +". Please specify one in configuration. ");
		
		// Wether it's a composite or not
		boolean isComposite = Boolean.valueOf(props.getCustomBundleProperty(bundleName,BUNDLE_FACTORY_CUSTOM_COMPOSITE_FLAG,"false")).booleanValue();		
		
		
		// Create definition and set its id
		ResourceBundleDefinition bundle = new ResourceBundleDefinition();
		bundle.setBundleId(bundleId);

		// Wether it's global or not
		Boolean isGlobal = Boolean.valueOf(props.getCustomBundleProperty(bundleName,BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG,"false"));		
		bundle.setGlobal(isGlobal.booleanValue());
		
		// Set order if its a global bundle
		if(isGlobal.booleanValue()) {
			Integer order = Integer.valueOf(props.getCustomBundleProperty(bundleName,BUNDLE_FACTORY_CUSTOM_ORDER, "0"));
			bundle.setInclusionOrder(order.intValue());
		}
		
		// Override bundle postprocessor
		if(null != props.getCustomBundleProperty(bundleName, BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR)) 
			bundle.setBundlePostProcessorKeys(props.getCustomBundleProperty(bundleName, BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR));
		
		// Override unitary postprocessor
		if(null != props.getCustomBundleProperty(bundleName, BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR)) 
			bundle.setUnitaryPostProcessorKeys(props.getCustomBundleProperty(bundleName, BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR));
		
		// Use only with debug mode on
		Boolean isDebugOnly = Boolean.valueOf(props.getCustomBundleProperty(bundleName, BUNDLE_FACTORY_CUSTOM_DEBUGONLY, "false"));		
		bundle.setDebugOnly(isDebugOnly.booleanValue());
		
		// Use only with debug mode off
		Boolean isDebugNever = Boolean.valueOf(props.getCustomBundleProperty(bundleName, BUNDLE_FACTORY_CUSTOM_DEBUGNEVER, "false"));		
		bundle.setDebugNever(isDebugNever.booleanValue());
		
		// Set conditional comment for IE, in case one is specified
		if(null != props.getCustomBundleProperty(bundleName,BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION))
			bundle.setIeConditionalExpression(props.getCustomBundleProperty(bundleName,BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION));
		
		if(isComposite) {
			String childBundlesProperty = props.getCustomBundleProperty(bundleName,BUNDLE_FACTORY_CUSTOM_COMPOSITE_NAMES);
			if(null == childBundlesProperty) 
				throw new IllegalArgumentException("No child bundle names were defined for the composite bundle with name:" + bundleName +". Please specify at least one in configuration. ");
			
			bundle.setComposite(true);
			
			// add children 
			List children = new ArrayList();
			StringTokenizer tk = new StringTokenizer(childBundlesProperty,",");
			while(tk.hasMoreTokens()) {
				ResourceBundleDefinition childDef = buildCustomBundleDefinition(tk.nextToken().trim(), true);
				childDef.setBundleId(bundleId);
				children.add(childDef);
			}
			bundle.setChildren(children);
		}
		else {
			String mappingsProperty = props.getCustomBundleProperty(bundleName,BUNDLE_FACTORY_CUSTOM_MAPPINGS);
			if(null == mappingsProperty)
				throw new IllegalArgumentException("No mappings were defined for the bundle with name:" + bundleName +". Please specify at least one in configuration. ");

			// Add the mappings
			List mappings = new ArrayList();
			StringTokenizer tk = new StringTokenizer(mappingsProperty,",");
			while(tk.hasMoreTokens())
				mappings.add(tk.nextToken().trim());
			bundle.setMappings(mappings);
			
			String locales = props.getCustomBundleProperty(bundleName,BUNDLE_FACTORY_CUSTOM_LOCALE_VARIANTS);
			if(null !=  locales) {
				List localeKeys = new ArrayList();
				StringTokenizer tkl = new StringTokenizer(locales,",");
				while(tkl.hasMoreTokens())
					localeKeys.add(tkl.nextToken().trim());
				bundle.setLocaleVariantKeys(localeKeys);
				
			}
		}
		
		
		return bundle;
	}
	
	
}
