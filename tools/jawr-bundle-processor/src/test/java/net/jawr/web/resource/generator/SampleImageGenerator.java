package net.jawr.web.resource.generator;

import java.io.InputStream;

import javax.servlet.ServletContext;

import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.StreamResourceGenerator;

public class SampleImageGenerator implements StreamResourceGenerator {

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.StreamResourceGenerator#createResourceAsStream(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public InputStream createResourceAsStream(GeneratorContext context) {
		
		ServletContext servletContext = context.getConfig().getContext();
		return servletContext.getResourceAsStream("/img/"+context.getPath());
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.PrefixedResourceGenerator#getMappingPrefix()
	 */
	public String getMappingPrefix() {
		return "img";
	}

}
