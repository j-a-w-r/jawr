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
package net.jawr.web.resource.bundle.postprocess.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.factory.util.RegexUtil;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;

/**
 * This class defines the Post processor which handle the inclusion of the CSS define with @import statement
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class CSSImportPostProcessor extends
		AbstractChainedResourceBundlePostProcessor {

	/** The url pattern */
	private static final Pattern importPattern = Pattern.compile(	"@import\\s*url\\(\\s*" // 'url(' and any number of whitespaces 
																+ "[\"']?([^\"']*)[\"']?" // any sequence of characters, except an unescaped ')'
																+ "\\s*\\);?",  // Any number of whitespaces, then ')'
																Pattern.CASE_INSENSITIVE); // works with 'URL('
	
	/**
	 * Constructor
	 * @param id the Id of the post processor
	 */
	public CSSImportPostProcessor() {
		super(PostProcessFactoryConstant.CSS_IMPORT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor#doPostProcessBundle(net.jawr.web.resource.bundle.postprocess
	 * .BundleProcessingStatus, java.lang.StringBuffer)
	 */
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status,
			StringBuffer bundleData) throws IOException {

		String data = bundleData.toString();
		
		// Rewrite each css image url path
		Matcher matcher = importPattern.matcher(data);
		StringBuffer sb = new StringBuffer();
		while(matcher.find()) {
		
			String url = getCssPathContent(matcher.group(1), status);
			matcher.appendReplacement(sb, RegexUtil.adaptReplacementToMatcher(url));
		}
		matcher.appendTail(sb);
		return sb;
		
	}

	/**
	 * Retrieve the content of the css to import
	 * @param cssPathToImport
	 * @param status the bundle processing status
	 * @return the content of the css to import
	 * @throws IOException if an IOException occurs
	 */
	private String getCssPathContent(String cssPathToImport, BundleProcessingStatus status) throws IOException {
		
		String currentCssPath = status.getLastPathAdded();
		
		String path = cssPathToImport;
		if(!cssPathToImport.startsWith("/") && !path.startsWith(JawrConstant.CLASSPATH_RESOURCE_PREFIX)){ // relative URL
			path = PathNormalizer.concatWebPath(currentCssPath, cssPathToImport);
		}
		
		Reader reader = null;
		
		if(path.startsWith(JawrConstant.CLASSPATH_RESOURCE_PREFIX)){
			String realPath = path.substring(JawrConstant.CLASSPATH_RESOURCE_PREFIX.length());
			InputStream is = ClassLoaderResourceUtils.getResourceAsStream(realPath, this.getClass());
			if(is == null){
				throw new IOException("Css to import '"+path+"' was not found");
			}
			ReadableByteChannel chan = Channels.newChannel(is);
			reader = Channels.newReader(chan,status.getJawrConfig().getResourceCharset().newDecoder (),-1);
		}else{
			try {
				reader = status.getRsHandler().getResource(path, true);
			} catch (ResourceNotFoundException e) {
				throw new IOException("Css to import '"+path+"' was not found");
			}
		}
		
		StringWriter content = new StringWriter();
		IOUtils.copy(reader, content);
		return content.toString();
	}

}
