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
package net.jawr.web.resource.bundle.generator;

import java.io.Reader;
import java.util.Locale;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.handler.reader.TextResourceReader;

/**
 * This class defines the wrapper class for resource generator in text resource reader.
 * 
 * @author Ibrahim Chaehoi
 */
public class ResourceGeneratorReaderWrapper implements TextResourceReader {

	/** The resource generator wrapped */
	private ResourceGenerator generator;
	
	/** The resource handler */
	private ResourceReaderHandler rsHandler;
	
	/** The Jawr config */
	private JawrConfig config;
	
	/**
	 * Constructor
	 * @param generator the generator
	 */
	public ResourceGeneratorReaderWrapper(ResourceGenerator generator, ResourceReaderHandler rsHandler, JawrConfig config) {
		this.generator = generator;
		this.config = config;
		this.rsHandler = rsHandler;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.ResourceReader#getResource(java.lang.String)
	 */
	public Reader getResource(String resourceName) {
		
		return getResource(resourceName, false);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.ResourceReader#getResource(java.lang.String, boolean)
	 */
	public Reader getResource(String requestedResource, boolean processingBundle) {
			
		String resourceName = requestedResource;
		Locale locale = null;
		if(resourceName.indexOf('@') != -1){
			String localeKey = resourceName.substring(resourceName.indexOf('@')+1);
			resourceName = resourceName.substring(0,resourceName.indexOf('@'));
			
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
		GeneratorContext context = new GeneratorContext(config, resourceName.substring((generator.getMappingPrefix()+GeneratorRegistry.PREFIX_SEPARATOR).length()));
		context.setLocale(locale);
		context.setResourceReaderHandler(rsHandler);
		context.setProcessingBundle(processingBundle);
		
		return generator.createResource(context);
	}
	
}
