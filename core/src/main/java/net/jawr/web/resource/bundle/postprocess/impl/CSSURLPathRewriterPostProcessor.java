/**
 * Copyright 2007 Jordi Hernández Sellés
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jawr.web.resource.bundle.factory.util.RegexUtil;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;

/**
 * Single file postprocessor used to rewrite CSS URLs according to the new relative locations of the references when
 * added to a bundle. Since the path changes, the URLs must be rewritten accordingly.  
 * URLs in css files are expected to be according to the css spec (see http://www.w3.org/TR/REC-CSS2/syndata.html#value-def-uri). 
 * Thus, single double, or no quotes enclosing the url are allowed (and remain as they are after rewriting). Escaped parens and quotes 
 * are allowed within the url.   
 *  
 * @author Jordi Hernández Sellés
 *
 */
public class CSSURLPathRewriterPostProcessor extends
		AbstractChainedResourceBundlePostProcessor {
	
	private static final Pattern urlPattern = Pattern.compile(	"url\\(\\s*" // 'url(' and any number of whitespaces 
																+ "((\\\\\\))|[^)])*" // any sequence of characters, except an unescaped ')'
																+ "\\s*\\)",  // Any number of whitespaces, then ')'
																Pattern.CASE_INSENSITIVE); // works with 'URL('
	private static final String backRefRegex = "(\\.\\./)";
	private static final Pattern backRefRegexPattern = Pattern.compile(backRefRegex);
	private static final Pattern URLSeparatorPattern = Pattern.compile("/");
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.postprocess.impl.AbstractChainedResourceBundlePostProcessor#doPostProcessBundle(net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus, java.lang.StringBuffer)
	 */
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status,
			StringBuffer bundleData) throws IOException {
		
		String data = bundleData.toString();
		
		// Initial backrefs (number of backtracking paths needed, i.e. '../').
		int initialBackRefs = 1;
		
		if(! "".equals(status.getJawrConfig().getServletMapping())){
			StringTokenizer tk = new StringTokenizer(status.getJawrConfig().getServletMapping(),"/");
			initialBackRefs += tk.countTokens();
		}
		
		String bundleName = status.getCurrentBundle().getName();
		initialBackRefs += numberOfForwardReferences(bundleName);
		List resourceBackRefs = getPathNamesInResource(status.getLastPathAdded());
		Matcher matcher = urlPattern.matcher(data);
		StringBuffer sb = new StringBuffer();
		while(matcher.find()) {
			List backRefs = new ArrayList();
			backRefs.addAll(resourceBackRefs);
			String url = getUrlPath(matcher.group(), initialBackRefs, backRefs);
			matcher.appendReplacement(sb, RegexUtil.adaptReplacementToMatcher(url));
		}
		matcher.appendTail(sb);
		return sb;
	}
	

	/**
	 * Transform a matched url so it points to the proper relative path with respect to the given path.  
	 * @param matcher
	 * @param initialBackRefs
	 * @return
	 */
	private String getUrlPath(String match, int initialBackRefs, List resourceBackRefs) {

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
		
		int backRefsInURL = 0;
		// Remove leading slash
		if(url.startsWith("../")) {
			Matcher backUrls = backRefRegexPattern.matcher(url);
			while(backUrls.find())
				backRefsInURL++;
			url = url.replaceAll(backRefRegex, "");
		}
		else if(url.startsWith("/"))
			url = url.substring(1,url.length());
		else if(url.startsWith("./"))
			url = url.substring(2,url.length());
		
		// Adjust the forward pathnames according to the backrefs in the url
		if(backRefsInURL > 0 && resourceBackRefs.size() > 0) {
			int size = resourceBackRefs.size();
			int counter = 0;
			while(size > 0) {
				counter++;
				if(counter >= 50)
					throw new RuntimeException("Infinite loop on url: " + url + " :size:" + size);
				size--;
				backRefsInURL--;
				resourceBackRefs.remove(size);
				if(backRefsInURL == 0)
					break;
			}
		}		
		
		// Start rendering the result, starting by the initial quote, if any. 
		StringBuffer urlPrefix = new StringBuffer("url(").append(quoteStr);
		
		// Add the back references as needed
		for (int i = 0; i < initialBackRefs; i++) {
			urlPrefix.append("../");
		}		
		for(Iterator it = resourceBackRefs.iterator();it.hasNext(); ) {
			urlPrefix.append(it.next()).append("/");			
		}
		return urlPrefix.append(url).append(quoteStr).append(")").toString();
	}
	
	
	/**
	 * Gets the path names within a reource URL, exluding the resource file name. 
	 * @param resourceURL
	 * @return
	 */
	private List getPathNamesInResource(String resourceURL) {
		List names = new ArrayList();
		String[] namesArray = resourceURL.split("/");
		
		// Add all but the last pathname
		for (int i = 0; i < namesArray.length -1 ; i++) {
			if(!"".equals(namesArray[i]))
				names.add(namesArray[i]);
		}
		
		return names;
	}
	
	/**
	 * Find the number of occurrences of / within a url. 
	 * @param url
	 * @return
	 */
	private int numberOfForwardReferences(String url) {
		Matcher backUrls = URLSeparatorPattern.matcher(url);
		int numMatches = 0;
		while(backUrls.find())
			numMatches++;
		
		if(url.startsWith("/"))
			numMatches--;
		
		return numMatches;
	}
	
}
