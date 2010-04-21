package net.jawr.web.resource;

import java.io.InputStream;

import javax.servlet.ServletContext;

import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.StreamResourceGenerator;

public class ImageResourceGenerator implements StreamResourceGenerator {

	public InputStream createResourceAsStream(GeneratorContext context) {
		
		ServletContext servletContext = context.getConfig().getContext();
		return servletContext.getResourceAsStream("/img/"+context.getPath());
	}

	public String getMappingPrefix() {
		return "img";
	}

}
