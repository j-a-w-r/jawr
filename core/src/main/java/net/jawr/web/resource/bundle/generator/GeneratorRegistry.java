/**
 * Copyright 2008 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.jawr.web.JawrConstant;
import net.jawr.web.collections.ConcurrentCollectionsFactory;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.generator.classpath.ClassPathCSSGenerator;
import net.jawr.web.resource.bundle.generator.classpath.ClassPathImgResourceGenerator;
import net.jawr.web.resource.bundle.generator.classpath.ClasspathJSGenerator;
import net.jawr.web.resource.bundle.generator.dwr.DWRGeneratorFactory;
import net.jawr.web.resource.bundle.generator.validator.CommonsValidatorGenerator;
import net.jawr.web.resource.bundle.locale.ResourceBundleMessagesGenerator;
import net.jawr.web.resource.handler.reader.ResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.servlet.JawrRequestHandler;

/**
 * Registry for resource generators, which create scripts or CSS data dynamically, 
 * as opposed to the usual behavior of reading a resource from the war file. 
 * It provides methods to determine if a path mapping should be handled by a generator, 
 * and to actually render the resource using the appropriate generator. 
 * Path mappings which require generation will use a prefix (preferably one which ends with 
 * a colon, such as 'messages:'). 
 * Generators provided with Jawr will be automatically mapped. 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class GeneratorRegistry {
	
	/** The message bundle prefix */
	public static final String MESSAGE_BUNDLE_PREFIX = "messages";
	
	/** The classpath resource bundle prefix */
	public static final String CLASSPATH_RESOURCE_BUNDLE_PREFIX = "jar";
	
	/** The DWR bundle prefix */
	public static final String DWR_BUNDLE_PREFIX = "dwr";
	
	/** The commons validator bundle prefix */
	public static final String COMMONS_VALIDATOR_PREFIX = "acv";
	
	/** The IE CSS generator bundle prefix */
	public static final String IE_CSS_GENERATOR_PREFIX = "ieCssGen";

	/** The generator prefix separator */
	public static final String PREFIX_SEPARATOR = ":";
	
	/** The generator prefix registry */
	private final List prefixRegistry = ConcurrentCollectionsFactory.buildCopyOnWriteArrayList();
	
	/** The locale aware resource prefix registry */
	private final List localeAwareResourceGeneratorPrefixRegistry = ConcurrentCollectionsFactory.buildCopyOnWriteArrayList();
	
	/** The CSS image resource prefix registry */
	private final List cssImageResourceGeneratorPrefixRegistry = ConcurrentCollectionsFactory.buildCopyOnWriteArrayList();
	
	/** The image resource prefix registry */
	private final List imageResourceGeneratorPrefixRegistry = ConcurrentCollectionsFactory.buildCopyOnWriteArrayList();
	
	/** The generator registry */
	private final Map registry = ConcurrentCollectionsFactory.buildConcurrentHashMap();
	
	/** The resource type */
	private String resourceType;
	
	/** The Jawr config */
	private JawrConfig config;
	
	/** The resource handler */
	private ResourceReaderHandler rsHandler;
	
	/**
	 * Use only for testing purposes.
	 */
	public GeneratorRegistry(){
		this(JawrConstant.JS_TYPE);
	}
	
	/**
	 * Constructor
	 */
	public GeneratorRegistry(String resourceType){
		this.resourceType = resourceType;
		prefixRegistry.add(MESSAGE_BUNDLE_PREFIX + PREFIX_SEPARATOR);
		prefixRegistry.add(CLASSPATH_RESOURCE_BUNDLE_PREFIX + PREFIX_SEPARATOR);
		prefixRegistry.add(DWR_BUNDLE_PREFIX + PREFIX_SEPARATOR);
		prefixRegistry.add(COMMONS_VALIDATOR_PREFIX + PREFIX_SEPARATOR);
		prefixRegistry.add(MESSAGE_BUNDLE_PREFIX + PREFIX_SEPARATOR);
		prefixRegistry.add(IE_CSS_GENERATOR_PREFIX + PREFIX_SEPARATOR);
	}
	
	/**
	 * Set the Jawr config
	 * 
	 * @param config the config to set
	 */
	public void setConfig(JawrConfig config) {
		this.config = config;
	}
	
	/**
	 * Sets the resource handler 
	 * @param rsHandler the rsHandler to set
	 */
	public void setResourceReaderHandler(ResourceReaderHandler rsHandler) {
		this.rsHandler = rsHandler;
	}

	/**
	 * Lazy loads generators, to avoid the need for undesired dependencies. 
	 * 
	 * @param generatorKey the generator key
	 */
	private void loadGenerator(String generatorKey) {
		PrefixedResourceGenerator generator = null;
		if((MESSAGE_BUNDLE_PREFIX + PREFIX_SEPARATOR).equals(generatorKey)){
			generator = new ResourceBundleMessagesGenerator();
		}
		else if((CLASSPATH_RESOURCE_BUNDLE_PREFIX + PREFIX_SEPARATOR).equals(generatorKey)){
			if(resourceType.equals(JawrConstant.JS_TYPE)){
				generator = new ClasspathJSGenerator();
			}else if(resourceType.equals(JawrConstant.CSS_TYPE)){
				generator = new ClassPathCSSGenerator(config.isUsingClasspathCssImageServlet());
			}else{
				generator = new ClassPathImgResourceGenerator();
			}
		}
		else if((DWR_BUNDLE_PREFIX + PREFIX_SEPARATOR).equals(generatorKey)){
			generator = DWRGeneratorFactory.createDWRGenerator();
		}
		else if((COMMONS_VALIDATOR_PREFIX + PREFIX_SEPARATOR).equals(generatorKey)){
			generator = new CommonsValidatorGenerator();
		}else if(resourceType.equals(JawrConstant.CSS_TYPE) && (IE_CSS_GENERATOR_PREFIX + PREFIX_SEPARATOR).equals(generatorKey)){
			generator = new IECssBundleGenerator();
		}
		
		if(generator != null){
			updateRegistries(generator, generatorKey);
			ResourceReader proxy = ResourceGeneratorReaderProxyFactory.getResourceReaderProxy(generator, rsHandler, config);
			rsHandler.addResourceReaderToEnd(proxy);
		}
	}

	/**
	 * Update the registries with the generator given in parameter
	 * @param generator the generator
	 * @param generatorKey the generator key
	 */
	private void updateRegistries(PrefixedResourceGenerator generator, String generatorKey) {
		
		registry.put(generatorKey, generator);
		if(generator instanceof LocaleAwareResourceGenerator){
			localeAwareResourceGeneratorPrefixRegistry.add(generatorKey);
		}
		if(generator instanceof StreamResourceGenerator){
			imageResourceGeneratorPrefixRegistry.add(generatorKey);
		}
		if(generator instanceof CssResourceGenerator){
			if(((CssResourceGenerator) generator).isHandlingCssImage()){
				cssImageResourceGeneratorPrefixRegistry.add(generatorKey);
			}
		}
	}
	
	/**
	 * Register a generator mapping it to the specified prefix. 
	 * 
	 * @param clazz the classname of the generator
	 */
	public void registerGenerator(String clazz){
		PrefixedResourceGenerator generator = (PrefixedResourceGenerator) ClassLoaderResourceUtils.buildObjectInstance(clazz);
		
		if(null == generator.getMappingPrefix() || "".equals(generator.getMappingPrefix()) )
			throw new IllegalStateException("The getMappingPrefix() method must return something at " + clazz);
		
		String fullPrefix = generator.getMappingPrefix() + PREFIX_SEPARATOR;
		
		// Verify this prefix is unused
		if(prefixRegistry.contains(fullPrefix)) {
			String generatorName = registry.get(fullPrefix).getClass().getName();
			if(!clazz.equals(generatorName)) {
				String errorMsg = "Cannot register the generator of class " 
								+ generator.getClass().getName()
								+ " using the prefix " + generator.getMappingPrefix() + " since such prefix is being used by "
								+ generatorName + ". Please pecify a different return value at the getMappingPrefix() method.";
				throw new IllegalStateException(errorMsg);
			}
		}
		
		initializeGeneratorProperties(generator);
		
		prefixRegistry.add(generator.getMappingPrefix() + PREFIX_SEPARATOR);
		updateRegistries(generator, generator.getMappingPrefix() + PREFIX_SEPARATOR);
		rsHandler.addResourceReaderToEnd(ResourceGeneratorReaderProxyFactory.getResourceReaderProxy(generator, rsHandler, config));
	}

	/**
	 * Initializes the generator properties.
	 * 
	 * @param generator the generator
	 */
	private void initializeGeneratorProperties(
			PrefixedResourceGenerator generator) {
		// Initialize the generator
		if(generator instanceof InitializingResourceGenerator){
			if(generator instanceof ConfigurationAwareResourceGenerator){
				((ConfigurationAwareResourceGenerator) generator).setConfig(config);
			}
			if(generator instanceof TypeAwareResourceGenerator){
				((TypeAwareResourceGenerator) generator).setResourceType(resourceType);
			}
			if(generator instanceof PostInitializationAwareResourceGenerator){
				((PostInitializationAwareResourceGenerator) generator).afterPropertiesSet();
			}
		}
	}
	
	/**
	 * Determines wether a path is to be handled by a generator. 
	 * @param path the resource path
	 * @return true if the path could be handled by a generator
	 */
	public boolean isPathGenerated(String path) {
		return null != matchPath(path);
	}
	
	/**
	 * Creates the contents corresponding to a path, by using the appropriate generator. 
	 * @param path the resource path
	 * @param resourceHandler the current resource handler
	 * @param processingBundle the flag indicating if if we are currently processing the bundles
	 * @return the reader for the contents
	 */
	public Reader createResource(String path,
			ResourceReaderHandler resourceHandler, boolean processingBundle) {
		String key = matchPath(path);
		Locale locale = null;
		
		if(path.indexOf('@') != -1){
			String localeKey = path.substring(path.indexOf('@')+1);
			path = path.substring(0,path.indexOf('@'));
			
			// Resourcebundle should be doing this for me...
			String[] params = localeKey.split("_");			
			switch(params.length) {
				case 3:
					locale = new Locale(params[0],params[1],params[2]);
					break;
				case 2: 
					locale = new Locale(params[0],params[1]);
					break;
				default:
					locale = new Locale(localeKey);
			}
		}
		GeneratorContext context = new GeneratorContext(config, path.substring(key.length()));
		context.setLocale(locale);
		context.setResourceReaderHandler(resourceHandler);
		context.setProcessingBundle(processingBundle);
		
		return ((ResourceGenerator)registry.get(key)).createResource(context);
	}
	
	
	/**
	 * Returns the path to use in the generation URL for debug mode. 
	 * @param path the resource path
	 * @return the path to use in the generation URL for debug mode. 
	 */
	public String getDebugModeGenerationPath(String path) {
		String key = matchPath(path);
		return ((ResourceGenerator)registry.get(key)).getDebugModeRequestPath();
	}
	
	/**
	 * Returns the path to use in the "build time process" to generate the resource path for debug mode. 
	 * @param path the resource path
	 * @return the path to use in the "build time process" to generate the resource path for debug mode. 
	 */
	public String getDebugModeBuildTimeGenerationPath(String path){
		
		int idx = path.indexOf("?");
		String debugModeGeneratorPath = path.substring(0, idx);
		debugModeGeneratorPath = debugModeGeneratorPath.replaceAll("\\.", "/");
		
		int jawrGenerationParamIdx = path.indexOf(JawrRequestHandler.GENERATION_PARAM);
		String parameter = path.substring(jawrGenerationParamIdx+JawrRequestHandler.GENERATION_PARAM.length()+1); // Add 1 for the '=' character 
		String key = matchPath(parameter);
		ResourceGenerator resourceGenerator = (ResourceGenerator)registry.get(key);
		String suffixPath = null;
		if(resourceGenerator instanceof SpecificCDNDebugPathResourceGenerator){
			suffixPath = ((SpecificCDNDebugPathResourceGenerator)resourceGenerator).getDebugModeBuildTimeGenerationPath(parameter);
		}else{
			suffixPath = parameter.replaceFirst(GeneratorRegistry.PREFIX_SEPARATOR, JawrConstant.URL_SEPARATOR);
		}
		return debugModeGeneratorPath+"/"+suffixPath;
	}
	
	/**
	 * Get the key from the mappings that corresponds to the specified path. 
	 * @param path the resource path
	 * @return the registry key corresponding to the path
	 */
	private String matchPath(String path) {
		String generatorKey = null;
		for(Iterator it = prefixRegistry.iterator();it.hasNext() && generatorKey == null;) {
			String prefix = (String) it.next();
			if(path.startsWith(prefix))
				generatorKey = prefix;			
		}	
		// Lazy load generator
		if(null != generatorKey && !registry.containsKey(generatorKey))
			loadGenerator(generatorKey);
		
		return generatorKey;
	}

	/**
	 * Loads the generator which corresponds to the specified path. 
	 * @param path the resource path
	 */
	public void loadGeneratorIfNeeded(String path) {
		
		matchPath(path);
	}
	
	/**
	 * Returns the available locales for a bundle
	 * @param bundle the message
	 * @return the available locales for a bundle
	 */
	public List getAvailableLocales(String bundle) {
		
		List availableLocales = new ArrayList();
		String generatorKey = matchPath(bundle);
		if(generatorKey != null){
			ResourceGenerator generator = (ResourceGenerator) registry.get(generatorKey);
			if(generator instanceof LocaleAwareResourceGenerator){
				
				List tempResult = ((LocaleAwareResourceGenerator)generator).getAvailableLocales(bundle.substring(generatorKey.length()));
				if(tempResult != null){
					availableLocales = tempResult;
				}
			}
		}
		
		return availableLocales;
	}
	
	/**
	 * Returns true if the generator associated to the css resource path handle also CSS image.
	 * @param cssResourcePath the Css resource path
	 * @return true if the generator associated to the css resource path handle also CSS image.
	 */
	public boolean isHandlingCssImage(String cssResourcePath){
	
		boolean isHandlingCssImage = false;
		
		String generatorKey = matchPath(cssResourcePath);
		if(generatorKey != null && cssImageResourceGeneratorPrefixRegistry.contains(generatorKey)){
			isHandlingCssImage = true;
		}
		
		return isHandlingCssImage;
	}
	
	/**
	 * Returns true if the generator associated to the image resource path is an Image generator.
	 * @param imgResourcePath the image resource path
	 * @return true if the generator associated to the image resource path is an Image generator.
	 */
	public boolean isGeneratedImage(String imgResourcePath){
	
		boolean isGeneratedImage = false;
		
		String generatorKey = matchPath(imgResourcePath);
		if(generatorKey != null && imageResourceGeneratorPrefixRegistry.contains(generatorKey)){
			isGeneratedImage = true;
		}
		
		return isGeneratedImage;
	}
}
