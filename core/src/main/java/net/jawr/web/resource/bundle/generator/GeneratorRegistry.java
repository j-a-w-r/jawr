/**
 * Copyright 2008 Jordi Hernández Sellés
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.jawr.web.collections.ConcurrentCollectionsFactory;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.generator.classpath.ClassPathCSSGenerator;
import net.jawr.web.resource.bundle.generator.classpath.ClasspathJSGenerator;
import net.jawr.web.resource.bundle.generator.dwr.DWRGeneratorFactory;
import net.jawr.web.resource.bundle.generator.validator.CommonsValidatorGenerator;
import net.jawr.web.resource.bundle.locale.ResourceBundleMessagesGenerator;

/**
 * Registry for resource generators, which create scripts or CSS data dynamically, 
 * as opposed to the usual behavior of reading a resource from the war file. 
 * It provides methods to determine if a path mapping should be handled by a generator, 
 * and to actually render the resource using the appropiate generator. 
 * Path mappings which require generation will use a prefix (preferrably one which ends with 
 * a colon, such as 'messages:'). 
 * Generators provided with Jawr will be automatically mapped. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class GeneratorRegistry {
	
	public static final String MESSAGE_BUNDLE_PREFIX = "messages";
	public static final String CLASSPATH_JS_BUNDLE_PREFIX = "jar";
	public static final String CLASSPATH_CSS_BUNDLE_PREFIX = "jar_css";
	public static final String DWR_BUNDLE_PREFIX = "dwr";
	public static final String COMMONS_VALIDATOR_PREFIX = "acv";
	
	public static final String PREFIX_SEPARATOR = ":";
	
	private static final Map registry = ConcurrentCollectionsFactory.buildConcurrentHashMap();
	private static final List prefixRegistry = ConcurrentCollectionsFactory.buildCopyOnWriteArrayList();
	private JawrConfig config;
	
	static
	{
		prefixRegistry.add(MESSAGE_BUNDLE_PREFIX + PREFIX_SEPARATOR);
		prefixRegistry.add(CLASSPATH_JS_BUNDLE_PREFIX + PREFIX_SEPARATOR);
		prefixRegistry.add(CLASSPATH_CSS_BUNDLE_PREFIX + PREFIX_SEPARATOR);
		prefixRegistry.add(DWR_BUNDLE_PREFIX + PREFIX_SEPARATOR);
		prefixRegistry.add(COMMONS_VALIDATOR_PREFIX + PREFIX_SEPARATOR);
	}
	

	/**
	 * Use only for testing purposes.
	 */
	public GeneratorRegistry(){
		super();
	}
	
	/**
	 * Lazy loads generators, to avoid the need for undesired dependencies. 
	 * 
	 * @param generatorKey
	 * @return
	 */
	private void loadGenerator(String generatorKey) {
		if((MESSAGE_BUNDLE_PREFIX + PREFIX_SEPARATOR).equals(generatorKey)){
			registry.put(generatorKey, new ResourceBundleMessagesGenerator());
		}
		else if((CLASSPATH_JS_BUNDLE_PREFIX + PREFIX_SEPARATOR).equals(generatorKey)){
			registry.put(generatorKey, new ClasspathJSGenerator());
		}
		else if((CLASSPATH_CSS_BUNDLE_PREFIX + PREFIX_SEPARATOR).equals(generatorKey)){
			registry.put(generatorKey, new ClassPathCSSGenerator());
		}
		else if((DWR_BUNDLE_PREFIX + PREFIX_SEPARATOR).equals(generatorKey)){
			registry.put(generatorKey, DWRGeneratorFactory.createDWRGenerator());
		}
		else if((COMMONS_VALIDATOR_PREFIX + PREFIX_SEPARATOR).equals(generatorKey)){
			registry.put(generatorKey, new CommonsValidatorGenerator());
		}
	}
	
	/**
	 * Register a generator mapping it to the specified prefix. 
	 * 
	 * @param clazz String Qualified classname of the generator
	 */
	public void registerGenerator(String clazz){
		ResourceGenerator generator = (ResourceGenerator) ClassLoaderResourceUtils.buildObjectInstance(clazz);
		
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
		
		prefixRegistry.add(generator.getMappingPrefix() + PREFIX_SEPARATOR);
		registry.put(generator.getMappingPrefix() + PREFIX_SEPARATOR, generator);
	}
	
	/**
	 * Determines wether a path is to be handled by a generator. 
	 * @param path
	 * @return
	 */
	public boolean isPathGenerated(String path) {
		return null != matchPath(path);
	}
	
	/**
	 * Creates the contents corresponding to a path, by using the appropiate generator. 
	 * @param path
	 * @param charset
	 * @return
	 */
	public Reader createResource(String path) {
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
		
		return ((ResourceGenerator)registry.get(key)).createResource(context);
	}
	
	
	/**
	 * Returns the path to use in the generation URL for debug mode. 
	 * @param path
	 * @return
	 */
	public String getDebugModeGenerationPath(String path) {
		String key = matchPath(path);
		return ((ResourceGenerator)registry.get(key)).getDebugModeRequestPath();
	}
	
	/**
	 * Get the key from the mappings that corresponds to the specified path. 
	 * @param path
	 * @return
	 */
	private String matchPath(String path) {
		String rets = null;
		for(Iterator it = prefixRegistry.iterator();it.hasNext() && rets == null;) {
			String prefix = (String) it.next();
			if(path.startsWith(prefix))
				rets = prefix;			
		}	
		// Lazy load generator
		if(null != rets && !registry.containsKey(rets))
			loadGenerator(rets);
		
		return rets;
	}

	/**
	 * @param config the config to set
	 */
	public void setConfig(JawrConfig config) {
		this.config = config;
	}

}
