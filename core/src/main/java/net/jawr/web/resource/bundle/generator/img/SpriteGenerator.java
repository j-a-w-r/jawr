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
package net.jawr.web.resource.bundle.generator.img;

import java.io.InputStream;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.StreamResourceGenerator;
import net.jawr.web.resource.bundle.global.preprocessor.css.smartsprites.CssSmartSpritesResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.handler.reader.StreamResourceReader;

/**
 * The sprite generator.
 * 
 * @author Ibrahim CHAEHOI
 *
 */
public class SpriteGenerator implements StreamResourceGenerator {

	/** The stream resource handle for image sprite */
	private final StreamResourceReader rd;
	
	/**
	 * Constructor
	 * @param rsHandler the resource handler
	 * @param config the config
	 */
	public SpriteGenerator(ResourceReaderHandler rsHandler, JawrConfig config) {
		rd = new CssSmartSpritesResourceReader(rsHandler.getWorkingDirectory(), config); // TODO does it work with the build time feature??
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.StreamResourceGenerator#createResourceAsStream(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public InputStream createResourceAsStream(GeneratorContext context) {
		
		String path = context.getPath();
		return rd.getResourceAsStream(path);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.PrefixedResourceGenerator#getMappingPrefix()
	 */
	public String getMappingPrefix() {
		return GeneratorRegistry.SPRITE_GENERATOR_PREFIX;
	}
	
}
