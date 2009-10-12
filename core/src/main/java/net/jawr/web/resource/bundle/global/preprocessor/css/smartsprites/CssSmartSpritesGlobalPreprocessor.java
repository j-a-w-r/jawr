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
package net.jawr.web.resource.bundle.global.preprocessor.css.smartsprites;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.global.preprocessor.AbstractChainedGlobalPreprocessor;
import net.jawr.web.resource.bundle.global.preprocessor.GlobalPreprocessingContext;
import net.jawr.web.resource.handler.reader.ResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.carrot2.labs.smartsprites.SmartSpritesParameters;
import org.carrot2.labs.smartsprites.SpriteBuilder;
import org.carrot2.labs.smartsprites.SmartSpritesParameters.PngDepth;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.MessageSink;
import org.carrot2.labs.smartsprites.message.PrintStreamMessageSink;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.carrot2.labs.smartsprites.resource.ResourceHandler;


/**
 * This class defines the global preprocessor which will process all CSS files which
 * used smartsprites annotations.
 *  
 * @author Ibrahim Chaehoi
 * 
 */
public class CssSmartSpritesGlobalPreprocessor extends
		AbstractChainedGlobalPreprocessor {

	/**
	 * Constructor 
	 */
	public CssSmartSpritesGlobalPreprocessor() {
		super(JawrConstant.GLOBAL_CSS_SMARTSPRITES_PREPROCESSOR_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.jawr.web.resource.bundle.global.processor.ResourceTypeBundleProcessor#processBundles(net.jawr.web.resource.bundle.global.processor.
	 * ResourceTypeBundleProcessingContext, java.util.List)
	 */
	public void processBundles(GlobalPreprocessingContext ctx,
			List bundles) {
		
		ResourceReaderHandler rsHandler = ctx.getRsReaderHandler();
		Set resourcePaths = getResourcePaths(bundles);
		JawrConfig jawrConfig = ctx.getJawrConfig();
		Charset charset = jawrConfig.getResourceCharset();
		
		ImageResourcesHandler imgRsHandler = (ImageResourcesHandler) jawrConfig.getContext().getAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE);
		
		ResourceReader cssSpriteResourceReader = null;
		if(ctx.hasBundleToBePreprocessed()){
			generateSprites(rsHandler, imgRsHandler,
					resourcePaths, jawrConfig, charset);
		}
		
		// Update CSS resource handler
		cssSpriteResourceReader = new CssSmartSpritesResourceReader(rsHandler.getWorkingDirectory(),jawrConfig, ctx.hasBundleToBePreprocessed());
		ctx.getRsReaderHandler().addResourceReaderToStart(cssSpriteResourceReader);
		
		// Update image resource handler
		ResourceReaderHandler imgStreamRsHandler = imgRsHandler.getRsReaderHandler();
		imgStreamRsHandler.addResourceReaderToStart(cssSpriteResourceReader);
	}

	/**
	 * Generates the image sprites from the smartsprites annotation in the CSS, rewrite the CSS files
	 * to references the generated sprites.
	 * @param cssRsHandler the css resourceHandler 
	 * @param imgRsHandler the image resourceHandler
	 * @param resourcePaths the set of CSS resource paths to handle
	 * @param jawrConfig the Jawr config
	 * @param charset the charset
	 */
	private void generateSprites(
			ResourceReaderHandler cssRsHandler, ImageResourcesHandler imgRsHandler, Set resourcePaths,
			JawrConfig jawrConfig, Charset charset) {
		
		MessageLog messageLog = new MessageLog(new MessageSink[]{new PrintStreamMessageSink(
	            System.out)});
		
		SmartSpritesResourceHandler smartSpriteRsHandler = new SmartSpritesResourceHandler(cssRsHandler, imgRsHandler.getRsReaderHandler(), 
				imgRsHandler.getJawrConfig().getGeneratorRegistry(), charset.toString(), messageLog);
		String outDir = cssRsHandler.getWorkingDirectory()+JawrConstant.CSS_SMARTSPRITES_TMP_DIR;
		
		SmartSpritesParameters params = new SmartSpritesParameters("/", null, outDir, null, MessageLevel.valueOf("INFO"), "", PngDepth.valueOf("AUTO"),
			 false, charset.toString());
		
		SpriteBuilder spriteBuilder = new SpriteBuilder(params, messageLog, smartSpriteRsHandler);
		try {
			spriteBuilder.buildSprites(resourcePaths);
		} catch (IOException e) {
			throw new RuntimeException("Unable to build sprites", e);
		}
		
	}

	/**
	 * Returns the list of all CSS files defined in the bundles.
	 * 
	 * @param bundles the list of bundle
	 * @return the list of all CSS files defined in the bundles.
	 */
	private Set getResourcePaths(List bundles) {

		Set resourcePaths = new HashSet();

		for (Iterator iterator = bundles.iterator(); iterator.hasNext();) {
			JoinableResourceBundle bundle = (JoinableResourceBundle) iterator
					.next();
			resourcePaths.addAll(bundle.getItemPathList());
		}

		return resourcePaths;
	}

	/**
	 * This class defines the resource handler for smartSprites
	 * 
	 * @author Ibrahim Chaehoi
	 */
	private class SmartSpritesResourceHandler implements ResourceHandler {
		
		/** The resource handler for CSS resources */
		private ResourceReaderHandler rsHandler;

		/** The resource handler for image resources */
		private ResourceReaderHandler imgRsHandler;
		
		/** The image generator registry */
		private GeneratorRegistry imgGeneratorRegistry;
		
		/** The charset */
		private final String charset;

		/**
		 * Constructor
		 * 
		 * @param rsHandler the CSS resource handler
		 * @param imgRsHandler the image resource handler
		 * @param imgGeneratorRegistry the image generator registry
		 * @param charset the charser
		 * @param messageLog the message log
		 */
		public SmartSpritesResourceHandler(
				ResourceReaderHandler rsHandler,
				ResourceReaderHandler imgRsHandler, 
				GeneratorRegistry imgGeneratorRegistry,
				String charset, MessageLog messageLog) {
			this.rsHandler = rsHandler;
			this.imgRsHandler = imgRsHandler;
			this.imgGeneratorRegistry = imgGeneratorRegistry;
			this.charset = charset;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.carrot2.labs.smartsprites.resource.ResourceHandler#getReader(java.lang.String)
		 */
		public Reader getResourceAsReader(String resourceName)
				throws IOException {

			try {
				return rsHandler.getResource(resourceName, true);
			} catch (ResourceNotFoundException e) {
				throw new IOException("The resource '"+resourceName+"' was not found.");
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.carrot2.labs.smartsprites.resource.ResourceHandler#getResourceAsStream(java.lang.String)
		 */
		public InputStream getResourceAsInputStream(String resourceName)
				throws IOException {
			
			try {
				return imgRsHandler.getResourceAsStream(resourceName);
			} catch (ResourceNotFoundException e) {
				throw new IOException("The resource '"+resourceName+"' was not found.");
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.carrot2.labs.smartsprites.resource.ResourceHandler#getResourcePath(java.lang.String, java.lang.String)
		 */
		public String getResourcePath(String basePath, String relativePath) {

			String result = null;
			if (imgGeneratorRegistry.isGeneratedImage(relativePath)) {
				result = relativePath;
			} else {
				result = PathNormalizer.concatWebPath(basePath, relativePath);
			}
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.carrot2.labs.smartsprites.resource.ResourceHandler#getResourceAsOutputStream(java.lang.String)
		 */
		public OutputStream getResourceAsOutputStream(String resourceName)
				throws IOException {

			// Create directories if needed
			final File parentFile = new File(resourceName).getParentFile();
			if (!parentFile.exists()) {
				if (!parentFile.mkdirs()) {
					throw new IOException("Unable to create the directory : "
							+ parentFile.getPath());
				}
			}
			
			File file = new File(resourceName);
	        try
	        {
	        	file = file.getCanonicalFile();
	        }
	        catch (final IOException e)
	        {
	        	file = file.getAbsoluteFile();
	        }
	        
			return new FileOutputStream(file);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.carrot2.labs.smartsprites.resource.ResourceHandler#getResourceAsWriter(java.lang.String)
		 */
		public Writer getResourceAsWriter(String path) throws IOException {
			try {
				return new OutputStreamWriter(getResourceAsOutputStream(path),
						charset);
			} catch (UnsupportedEncodingException e) {
				// Should not happen as we're checking the charset in constructor
				throw new RuntimeException(e);
			}
		}
	}

}
