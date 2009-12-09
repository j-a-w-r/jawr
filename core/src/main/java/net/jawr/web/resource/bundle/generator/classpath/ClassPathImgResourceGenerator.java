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
package net.jawr.web.resource.bundle.generator.classpath;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

import net.jawr.web.resource.FileNameUtils;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.StreamResourceGenerator;
import net.jawr.web.servlet.util.MIMETypesSupport;

/**
 * This class defines the resource generator which loads image resources from the classpath.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class ClassPathImgResourceGenerator implements StreamResourceGenerator {

	/** The image extensions */
	private Set imageExtensions;
	
	/** The classpath generator helper */
	private ClassPathGeneratorHelper helper;
	
	/**
	 * Constructor. 
	 */
	public ClassPathImgResourceGenerator() {
		helper = new ClassPathGeneratorHelper();
		imageExtensions = MIMETypesSupport.getSupportedProperties(this).keySet();
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.StreamResourceGenerator#createResourceAsStream(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public InputStream createResourceAsStream(GeneratorContext context) {
		
		InputStream is = null;
		if(FileNameUtils.isExtension(context.getPath(), imageExtensions)){
			is = helper.createStreamResource(context);
		}
		
		return is;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.StreamResourceGenerator#getMappingPrefix()
	 */
	public String getMappingPrefix() {
		
		return GeneratorRegistry.CLASSPATH_RESOURCE_BUNDLE_PREFIX;
	}
}
