/**
 * Copyright 2007-2009 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.postprocess.impl;

import java.io.IOException;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.bundle.CheckSumUtils;
import net.jawr.web.resource.bundle.css.CssImageUrlRewriter;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;
import net.jawr.web.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * Single file postprocessor used to rewrite CSS URLs according to the new relative locations of the references when
 * added to a bundle. Since the path changes, the URLs must be rewritten accordingly.  
 * URLs in css files are expected to be according to the css spec (see http://www.w3.org/TR/REC-CSS2/syndata.html#value-def-uri). 
 * Thus, single double, or no quotes enclosing the url are allowed (and remain as they are after rewriting). Escaped parens and quotes 
 * are allowed within the url.   
 *  
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class CSSURLPathRewriterPostProcessor extends
		AbstractChainedResourceBundlePostProcessor {
	
	/** Logger */
	private static Logger LOGGER = Logger.getLogger(CSSURLPathRewriterPostProcessor.class);
	
	/** This variable is used to fake the gzip prefix for the full bundle path */
	private static final String FAKE_BUNDLE_PREFIX = "/prefix/";

	/** The URL separator */
	private static final String URL_SEPARATOR = "/";

	/**
	 * Constructor
	 */
	public CSSURLPathRewriterPostProcessor() {
		super(PostProcessFactoryConstant.URL_PATH_REWRITER);
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.postprocess.impl.AbstractChainedResourceBundlePostProcessor#doPostProcessBundle(net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus, java.lang.StringBuffer)
	 */
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status,
			StringBuffer bundleData) throws IOException {
		
		// Retrieve the full bundle path, so we will be able to define the relative path for the css images
		String fullBundlePath = getFinalFullBundlePath(status);
		PostProcessorCssImageUrlRewriter urlRewriter = new PostProcessorCssImageUrlRewriter(status);
		return urlRewriter.rewriteUrl(status.getLastPathAdded(), fullBundlePath, bundleData.toString());
	}
	
	/**
	 * Returns the full path for the CSS bundle, taking in account the css servlet path if defined, 
	 * the caching prefix, and the url context path overriden
	 *   
	 * @return the full bundle path
	 */
	public String getFinalFullBundlePath(BundleProcessingStatus status) {

		JawrConfig jawrConfig = status.getJawrConfig();
		String fullBundlePath = null;
		String bundleName = status.getCurrentBundle().getId();
		
		// Generation the bundle prefix
		String bundlePrefix = "";
		if(!bundleName.equals(ResourceGenerator.CSS_DEBUGPATH)){
			bundlePrefix = FAKE_BUNDLE_PREFIX;
		}
		
		// Add path reference for the servlet mapping if it exists 
		if(! "".equals(jawrConfig.getServletMapping())){
			bundlePrefix = PathNormalizer.asPath(jawrConfig.getServletMapping()+bundlePrefix)+"/";
			
		}  
		
		// Concatenate the bundle prefix and the bundle name
		fullBundlePath = PathNormalizer.concatWebPath(bundlePrefix, bundleName);
		
		return fullBundlePath;
	}


	/**
	 * This class defines the URL rewriter for the Css post processor
	 *  
	 * @author Ibrahim Chaehoi
	 */
	private static class PostProcessorCssImageUrlRewriter extends CssImageUrlRewriter {
		
		/** The bundle processing status */
		private BundleProcessingStatus status;
		
		/**
		 * Constructor
		 * @param status the bundle processing status
		 */
		public PostProcessorCssImageUrlRewriter(BundleProcessingStatus status) {
			this.status = status;
		}
		
		
		/* (non-Javadoc)
		 * @see net.jawr.web.resource.bundle.css.CssImageUrlRewriter#getRewrittenImagePath(java.lang.String, java.lang.String, java.lang.String)
		 */
		protected String getRewrittenImagePath(String originalCssPath,
				String newCssPath, String url) throws IOException {
			
			JawrConfig jawrConfig = status.getJawrConfig();
			
			// Retrieve the image servlet mapping
			ImageResourcesHandler imgRsHandler = (ImageResourcesHandler) jawrConfig.getContext().getAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE);
			String imgServletPath = "";
			
			if(imgRsHandler != null){
				imgServletPath = PathNormalizer.asPath(imgRsHandler.getJawrConfig().getServletMapping());
			}
			
			String imgUrl = null;
			
			// Retrieve the current CSS file from which the CSS image is referenced
			String currentCss = originalCssPath;
			boolean generatedImg = false;
			if(imgRsHandler != null){
				GeneratorRegistry imgRsGeneratorRegistry = imgRsHandler.getJawrConfig().getGeneratorRegistry();
				generatedImg = imgRsGeneratorRegistry.isGeneratedImage(url);
			}
			
			boolean cssGeneratorIsHandleCssImage = isCssGeneratorHandlingCssImage(currentCss, status);
			
			String rootPath = currentCss;
			
			// If the CSS image is taken from the classpath, add the classpath cache prefix
			if(generatedImg || cssGeneratorIsHandleCssImage){
				
				String tempUrl = url;
				
				// If it's a classpath CSS, the url of the CSS image is defined relatively to it.
				if(cssGeneratorIsHandleCssImage && !generatedImg){
					tempUrl = PathNormalizer.concatWebPath(rootPath, url);
				}

				// generate image cache URL
				String cacheUrl = addCacheBuster(status, tempUrl, imgRsHandler);
				imgUrl = cacheUrl;
			}else{
				
				if(jawrConfig.getGeneratorRegistry().isPathGenerated(rootPath)){
					rootPath = rootPath.substring(rootPath.indexOf(GeneratorRegistry.PREFIX_SEPARATOR)+1);
				}
					
				// Generate the image URL from the current CSS path
				imgUrl = PathNormalizer.concatWebPath(rootPath, url);
				
				// If the image is part of the images cached by the Jawr image servlet
				// use the image cache url
				// Note : the Jawr image servlet must always be initialized before the CSS one
				String imgCacheUrl = null;
				if(imgRsHandler != null){
					imgCacheUrl = imgRsHandler.getCacheUrl(imgUrl);
					if(imgCacheUrl == null){
						imgCacheUrl = addCacheBuster(status, imgUrl, imgRsHandler);
					}
					imgUrl = imgCacheUrl;
				}
			}
			
			// This following condition should never be true. 
			// If it does, it means that the image path is wrongly defined.
			if(imgUrl == null){
				LOGGER.error("The CSS image path for '"+url+"' defined in '"+currentCss+"' is out of the application context. Please check your CSS file.");
			}
			
			// Add image servlet path in the URL, if it's defined
			if(StringUtils.isNotEmpty(imgServletPath)){
				imgUrl = imgServletPath+URL_SEPARATOR+imgUrl;
			}
			
			imgUrl = PathNormalizer.asPath(imgUrl);
			return PathNormalizer.getRelativeWebPath(PathNormalizer
					.getParentPath(newCssPath), imgUrl);
		}


		/**
		 * Checks if the Css generator associated to the Css resource path handle also the Css image resources. 
		 * @param currentCss the CSS resource path
		 * @param status the status
		 * @return true if the Css generator associated to the Css resource path handle also the Css image resources. 
		 */
		private boolean isCssGeneratorHandlingCssImage(String currentCss, BundleProcessingStatus status) {
			return status.getJawrConfig().getGeneratorRegistry().isHandlingCssImage(currentCss);
		}
		
		/**
		 * Adds the cache buster to the CSS image
		 * @param status the bundle processing status
		 * @param url the URL of the image
		 * @param imgRsHandler the image resource handler
		 * @return the url of the CSS image with a cache buster
		 * @throws IOException if an IO exception occurs
		 */
		private String addCacheBuster(BundleProcessingStatus status, String url, ImageResourcesHandler imgRsHandler) throws IOException {
			
			// Try to retrieve the from the bundle processing cache
			String newUrl = status.getImageMapping(url);
			if(newUrl != null){
				return newUrl;
			}
			
			// Try to retrieve the from the image resource handler cache
			if(imgRsHandler != null){
				newUrl = imgRsHandler.getCacheUrl(url);
				if(newUrl != null){
					return newUrl;
				}
				// Retrieve the new URL with the cache prefix
				try {
					newUrl = CheckSumUtils.getCacheBustedUrl(url, imgRsHandler.getRsReaderHandler(), imgRsHandler.getJawrConfig());
				} catch (ResourceNotFoundException e) {
					LOGGER.info("Impossible to define the checksum for the resource '"+url+"'. ");
					return url;
				} catch (IOException e) {
					LOGGER.info("Impossible to define the checksum for the resource '"+url+"'.");
					return url;
				}
				
				imgRsHandler.addMapping(url, newUrl);
				
			}else{
				newUrl = url;
			}
			
			// Set the result in a cache, so we will not search for it the next time
			status.setImageMapping(url, newUrl);
			
			return newUrl;
		}
	}
}
