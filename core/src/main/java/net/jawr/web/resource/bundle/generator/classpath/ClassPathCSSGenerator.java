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
package net.jawr.web.resource.bundle.generator.classpath;

import java.io.Reader;

import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.generator.AbstractCSSGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;

/**
 * This class defines the generator for the CSS defined in the classpath.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class ClassPathCSSGenerator extends AbstractCSSGenerator {
	
	/** The classpath generator helper */
	private ClassPathGeneratorHelper helper;
	
	/**
	 * Constructor 
	 */
	public ClassPathCSSGenerator() {
		helper = new ClassPathGeneratorHelper();
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public Reader createResource(GeneratorContext context) {
		
		Reader reader = null;
		
		// The following section is executed in DEBUG mode to retrieve the classpath CSS from the temporary folder, 
		// if the user defines that the image servlet should be used to retrieve the CSS images.
		// It's not executed at the initialization process to be able to read data from classpath.
		if(!context.isProcessingBundle() && context.getConfig().isUsingClasspathCssImageServlet()){
			
			try {
				reader = context.getResourceHandler().getCssClasspathResource(context.getPath());
			} catch (ResourceNotFoundException e) {
				 throw new RuntimeException(e);
			}
		}
		
		if(reader == null){
			reader = helper.createResource(context);
		}
		
		return reader;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#getMappingPrefix()
	 */
	public String getMappingPrefix() {
		return GeneratorRegistry.CLASSPATH_RESOURCE_BUNDLE_PREFIX;
	}

}
