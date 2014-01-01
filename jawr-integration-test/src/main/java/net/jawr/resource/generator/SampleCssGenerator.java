/**
 * 
 */
package net.jawr.resource.generator;

import java.io.Reader;
import java.io.StringReader;

import net.jawr.web.resource.bundle.generator.AbstractCSSGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;

/**
 * A sample generator
 * @author ibrahim Chaehoi
 *
 */
public class SampleCssGenerator extends AbstractCSSGenerator {

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public Reader createResource(GeneratorContext context) {
		
		String result = ".generatedContent { color : black; }";
		return new StringReader(result);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#getMappingPrefix()
	 */
	public String getMappingPrefix() {
		return "testCss";
	}

}
