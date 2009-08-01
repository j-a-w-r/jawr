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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.bundle.CheckSumUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.factory.util.RegexUtil;
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
	
	/** This variable is used to fake the gzip prefix for the full bundle path */
	private static final String FAKE_BUNDLE_PREFIX = "/prefix/";

	/** Logger */
	private static Logger log = Logger.getLogger(CSSURLPathRewriterPostProcessor.class);
	
	/** The URL separator */
	private static final String URL_SEPARATOR = "/";

	/** The url pattern */
	private static final Pattern urlPattern = Pattern.compile(	"url\\(\\s*" // 'url(' and any number of whitespaces 
																+ "((\\\\\\))|[^)])*" // any sequence of characters, except an unescaped ')'
																+ "\\s*\\)",  // Any number of whitespaces, then ')'
																Pattern.CASE_INSENSITIVE); // works with 'URL('
	
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
		
		String data = bundleData.toString();
		
		JawrConfig jawrConfig = status.getJawrConfig();
		
		// Retrieve the full bundle path, so we will be able to define the relative path for the css images
		String bundleName = getFinalFullBundlePath(status, jawrConfig);
		
		// Rewrite each css image url path
		Matcher matcher = urlPattern.matcher(data);
		StringBuffer sb = new StringBuffer();
		while(matcher.find()) {
		
			String url = getUrlPath(matcher.group(), bundleName, status);
			matcher.appendReplacement(sb, RegexUtil.adaptReplacementToMatcher(url));
		}
		matcher.appendTail(sb);
		return sb;
	}
	

	/**
	 * Transform a matched url so it points to the proper relative path with respect to the given path.  
	 * @param match the matched URL
	 * @param fullBundlePath the full bundle path
	 * @param status the bundle processing status
	 * @return the image URL path
	 * @throws IOException if an IO exception occurs
	 */
	private String getUrlPath(String match, String fullBundlePath, BundleProcessingStatus status) throws IOException {

		JawrConfig jawrConfig = status.getJawrConfig();
				
		// Retrieve the image servlet mapping
		ImageResourcesHandler imgRsHandler = (ImageResourcesHandler) jawrConfig.getContext().getAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE);
		String classPathImgServletPath = "";
		
		if(imgRsHandler != null){
			classPathImgServletPath = PathNormalizer.asPath(imgRsHandler.getJawrConfig().getServletMapping());
		}
		
		String url = match.substring(match.indexOf('(')+1,match.lastIndexOf(')'))
					.trim();

		// To keep quotes as they are, first they are checked and removed. 
		String quoteStr = "";
		if( url.startsWith("'") || url.startsWith("\"")) {
			quoteStr = url.charAt(0)+"";
			url = url.substring(1,url.length()-1);
		}
		
		// Check if the URL is absolute, if it is return it as is. 
		int firstSlash = url.indexOf('/');
		if(0 == firstSlash || (firstSlash != -1 && url.charAt(++firstSlash) == '/')){
			StringBuffer sb = new StringBuffer("url(");
			sb.append(quoteStr).append(url).append(quoteStr).append(")");
			return sb.toString();
		}
		
		// Check if the URL is embedded data (RFC2397), if it is return it as is
		if(url.trim().toLowerCase().startsWith("data:")) {
			StringBuffer sb = new StringBuffer("url(");
			sb.append(quoteStr).append(url).append(quoteStr).append(")");
			return sb.toString();
		}

		if(url.startsWith(URL_SEPARATOR))
			url = url.substring(1,url.length());
		else if(url.startsWith("./"))
			url = url.substring(2,url.length());
		
		// Here we generate the full path of the CSS image
		// to be able to define the relative path from the full bundle path
		String fullImgPath = getFinalFullImagePath(url, classPathImgServletPath, status, imgRsHandler);
		String imgUrl = PathNormalizer.getRelativeWebPath(PathNormalizer.getParentPath(fullBundlePath), fullImgPath);
		
		// Start rendering the result, starting by the initial quote, if any. 
		StringBuffer urlPrefix = new StringBuffer("url(").append(quoteStr);
		return PathNormalizer.normalizePath(urlPrefix.append(imgUrl).append(quoteStr).append(")").toString());
	}


	/**
	 * Returns the full path for the CSS bundle, taking in account the css servlet path if defined, 
	 * the caching prefix, and the url context path overriden
	 *   
	 * @param status the status
	 * @param jawrConfig the jawr configuration
	 * @return the full bundle path
	 */
	private String getFinalFullBundlePath(BundleProcessingStatus status, JawrConfig jawrConfig) {

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
	 * Returns the full path of the CSS image, taking in account the css servlet path if defined, 
	 * the caching prefix, and the url context path overriden.
	 * 
	 * @param url the image url
	 * @param imgServletPath the image servlet path
	 * @param status the status
	 * @param imgRsHandler the image Resource handler
	 * @return the full image path from the web application context path
	 * @throws IOException if an IOException occurs
	 */
	private String getFinalFullImagePath(String url, String imgServletPath, BundleProcessingStatus status,
			ImageResourcesHandler imgRsHandler) throws IOException {
		
		String imgUrl = null;
		
		// Retrieve the current CSS file from which the CSS image is referenced
		String currentCss = status.getLastPathAdded();
		
		boolean classpathImg = url.startsWith(JawrConstant.CLASSPATH_RESOURCE_PREFIX);
		boolean classpathCss = isClassPathCss(currentCss, status);
		
		String rootPath = currentCss;
		
		// If the CSS image is taken from the classpath, add the classpath cache prefix
		if(classpathImg || classpathCss){
			
			String tempUrl = url;
			
			// If it's a classpath CSS, the url of the CSS image is defined relatively to it.
			if(classpathCss){
				tempUrl = PathNormalizer.concatWebPath(rootPath, url);
			}

			// generate image cache URL
			String cacheUrl = addCacheBuster(status, tempUrl, imgRsHandler);
			imgUrl = cacheUrl;
		}else{
			
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
			log.error("The CSS image path for '"+url+"' defined in '"+currentCss+"' is out of the application context. Please check your CSS file.");
		}
		
		// Add image servlet path in the URL, if it's defined
		if(StringUtils.isNotEmpty(imgServletPath)){
			imgUrl = imgServletPath+URL_SEPARATOR+imgUrl;
		}
		
		return PathNormalizer.asPath(imgUrl);
	}


	/**
	 * Checks if the Css path in parameter is a classpath CSS. 
	 * @param currentCss the CSS 
	 * @param status the status
	 * @return true if if the Css path in parameter is a classpath CSS. 
	 */
	private boolean isClassPathCss(String currentCss, BundleProcessingStatus status) {
		return currentCss.startsWith(JawrConstant.CLASSPATH_RESOURCE_PREFIX) && status.getJawrConfig().isUsingClasspathCssImageServlet();
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
			newUrl = CheckSumUtils.getCacheBustedUrl(url, status.getRsHandler(), status.getJawrConfig());
			if(imgRsHandler != null){
				imgRsHandler.addMapping(url, newUrl);
			}
		}else{
			newUrl = url;
		}
		
		
		// Set the result in a cache, so we will not search for it the next time
		status.setImageMapping(url, newUrl);
		
		return newUrl;
	}

}
