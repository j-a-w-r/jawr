package prueba.pack.test;

import java.io.Reader;
import java.io.StringReader;

import net.jawr.web.resource.bundle.generator.AbstractJavascriptGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;

public class PruebaGenerator extends AbstractJavascriptGenerator implements ResourceGenerator {

	public Reader createResource(GeneratorContext context) {
		
		return new StringReader(";alert('generated!!!!!" +context.getPath() +  "');");
	}

	public String getMappingPrefix() {
		return "test";
	}

}
