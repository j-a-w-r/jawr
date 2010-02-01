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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.global.preprocessor.AbstractChainedGlobalPreprocessor;
import net.jawr.web.resource.bundle.global.preprocessor.GlobalPreprocessingContext;
import net.jawr.web.resource.handler.reader.ResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.carrot2.labs.smartsprites.SmartSpritesParameters;
import org.carrot2.labs.smartsprites.SpriteBuilder;
import org.carrot2.labs.smartsprites.SmartSpritesParameters.PngDepth;
import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.MessageSink;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;


/**
 * This class defines the global preprocessor which will process all CSS files which
 * used smartsprites annotations.
 *  
 * @author Ibrahim Chaehoi
 * 
 */
public class CssSmartSpritesGlobalPreprocessor extends
		AbstractChainedGlobalPreprocessor {

	/** The logger */
	private static Logger log = Logger.getLogger(CssSmartSpritesGlobalPreprocessor.class);
	
	/** The error level name */
	private static final String ERROR_LEVEL = "ERROR";
	
	/** The warn level name */
	private static final String WARN_LEVEL = "WARN";
	
	/** The info level name */
	private static final String INFO_LEVEL = "INFO";
	
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
		cssSpriteResourceReader = new CssSmartSpritesResourceReader(rsHandler.getWorkingDirectory(),jawrConfig);
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
		
		Level logLevel = log.getEffectiveLevel();
		MessageLevel msgLevel = MessageLevel.valueOf(ERROR_LEVEL);
		if(logLevel != null){
			if(logLevel.isGreaterOrEqual(Level.DEBUG)){
				msgLevel = MessageLevel.valueOf(INFO_LEVEL);
			}else if(logLevel.isGreaterOrEqual(Level.WARN)){
				msgLevel = MessageLevel.valueOf(WARN_LEVEL);
			}
		}
		
		MessageLog messageLog = new MessageLog(new MessageSink[]{new LogMessageSink()});
		
		SmartSpritesResourceHandler smartSpriteRsHandler = new SmartSpritesResourceHandler(cssRsHandler, imgRsHandler.getRsReaderHandler(), 
				imgRsHandler.getJawrConfig().getGeneratorRegistry(), charset.toString(), messageLog);
		String outDir = cssRsHandler.getWorkingDirectory()+JawrConstant.CSS_SMARTSPRITES_TMP_DIR;
		
		SmartSpritesParameters params = new SmartSpritesParameters("/", null, outDir, null, msgLevel, "", PngDepth.valueOf("AUTO"),
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
	 * The log message sink
	 * 
	 * @author Ibrahim Chaehoi
	 */
	private static class LogMessageSink implements MessageSink{
		
		
		/* (non-Javadoc)
		 * @see org.carrot2.labs.smartsprites.message.MessageSink#add(org.carrot2.labs.smartsprites.message.Message)
		 */
		public void add(Message message) {
			
			Level logLevel = log.getEffectiveLevel();
			if(logLevel == null){
				logLevel = Level.INFO;
			}
			log.log(logLevel, message.getFormattedMessage());
		}
		
	}
}
