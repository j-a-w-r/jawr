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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.generator.AbstractJavascriptGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;

/**
 * Loads resources from the classpath.  
 * 
 * @author Jordi Hernández Sellés
 */
public class ClasspathResourceGenerator extends AbstractJavascriptGenerator implements ResourceGenerator {

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource(java.lang.String, java.nio.charset.Charset)
	 */
	public Reader createResource(GeneratorContext context) {
		try {
			InputStream is = ClassLoaderResourceUtils.getResourceAsStream(context.getPath(), this);
			 ReadableByteChannel chan = Channels.newChannel(is);
			 return Channels.newReader(chan,context.getCharset().newDecoder (),-1);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#getMappingPrefix()
	 */
	public String getMappingPrefix() {
		return GeneratorRegistry.CLASSPATH_BUNDLE_PREFIX;
	}

}
