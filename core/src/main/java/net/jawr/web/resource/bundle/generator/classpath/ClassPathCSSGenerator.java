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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.AbstractCSSGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.CSSURLPathRewriterPostProcessor;
import net.jawr.web.resource.handler.reader.WorkingDirectoryLocationAware;

/**
 * This class defines the generator for the CSS defined in the classpath.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class ClassPathCSSGenerator extends AbstractCSSGenerator implements WorkingDirectoryLocationAware {
	
	/** The name of the directory which contain the CSS defined in classpath for the DEBUG mode */
	private static final String TEMP_CSS_CLASSPATH_SUBDIR = "cssClasspath";

	/** The classpath generator helper */
	private ClassPathGeneratorHelper helper;
	
	/** The working directory */
	private String workingDir;
	
	/** The flag indicating if the generator is handling the Css Image ressources */
	private boolean isHandlingCssImage;
	
	/**
	 * Constructor 
	 */
	public ClassPathCSSGenerator(boolean isHandlingCssImage) {
		helper = new ClassPathGeneratorHelper();
		this.isHandlingCssImage = isHandlingCssImage;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.CssResourceGenerator#isHandlingCssImage()
	 */
	public boolean isHandlingCssImage() {
		return isHandlingCssImage;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.TemporaryResourceLocationAware#setTemporaryDirectory(java.lang.String)
	 */
	public void setWorkingDirectory(String workingDir) {
		this.workingDir = workingDir;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#getMappingPrefix()
	 */
	public String getMappingPrefix() {
		return GeneratorRegistry.CLASSPATH_RESOURCE_BUNDLE_PREFIX;
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

			Reader rd = null;
			FileInputStream fis;
			try {
				fis = new FileInputStream(new File(workingDir+"/"+TEMP_CSS_CLASSPATH_SUBDIR, context.getPath()));
			} catch (FileNotFoundException e) {
				throw new RuntimeException("An error occured while creating temporary resource for "+context.getPath(), e);
			}
	        if(fis != null){
	        	FileChannel inchannel = fis.getChannel();
	        	rd = Channels.newReader(inchannel,context.getConfig().getResourceCharset().newDecoder(),-1);
	        }
			
	        return rd;
		
			//reader = context.getResourceReaderHandler().getCssClasspathResource(context.getPath());
		}
		
		if(reader == null){
			reader = helper.createResource(context);
			reader = createTempResource(context, reader);
		}
		
		return reader;
	}


	private Reader createTempResource(GeneratorContext generatorContext, Reader rd) {
		
		Reader result = null;
		
		// Here we create a new context where the bundle name is the Jawr generator CSS path
		// The version of the CSS classpath for debug mode will be different compare to the standard one
		String bundlePath = PathNormalizer.concatWebPath(generatorContext.getConfig().getServletMapping(), ResourceGenerator.CSS_DEBUGPATH);
		JoinableResourceBundle tempBundle = new JoinableResourceBundleImpl(bundlePath, null, null, null, null);
		BundleProcessingStatus tempStatus = new BundleProcessingStatus(tempBundle, generatorContext.getResourceReaderHandler(), generatorContext.getConfig());
		
		CSSURLPathRewriterPostProcessor postProcessor = new CSSURLPathRewriterPostProcessor();
		String resourcePath = generatorContext.getPath();
		tempStatus.setLastPathAdded(JawrConstant.CLASSPATH_RESOURCE_PREFIX+resourcePath);
		FileWriter fWriter = null;
		try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(rd, writer);
			result = new StringReader(writer.getBuffer().toString());
			StringBuffer resourceData = postProcessor.postProcessBundle(tempStatus, writer.getBuffer());
			
			File cssTempFile = new File(workingDir+"/"+TEMP_CSS_CLASSPATH_SUBDIR, resourcePath);
			File tempCssDir = cssTempFile.getParentFile(); 
			if(!tempCssDir.exists()){
				tempCssDir.mkdirs();
			}
			
			fWriter = new FileWriter(cssTempFile);
			IOUtils.copy(new StringReader(resourceData.toString()), fWriter);
		} catch (IOException e) {
			throw new RuntimeException("An error occured while creating temporary resource for "+resourcePath, e);
		}finally{
			IOUtils.close(fWriter);
		}
		
		return result;
	}

}
