/**
 * 
 */
package net.jawr.resource.generator;

import java.io.Reader;
import java.io.StringReader;

import net.jawr.web.resource.bundle.generator.AbstractJavascriptGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;

/**
 * A sample generator
 * @author ibrahim Chaehoi
 *
 */
public class SampleJsGenerator extends AbstractJavascriptGenerator {

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public Reader createResource(GeneratorContext context) {
		
		String result = ";function foo(){};";
		return new StringReader(result);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#getMappingPrefix()
	 */
	public String getMappingPrefix() {
		return "testJs";
	}

}
