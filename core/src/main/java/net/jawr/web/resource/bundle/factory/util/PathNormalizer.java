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
package net.jawr.web.resource.bundle.factory.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import net.jawr.web.servlet.JawrRequestHandler;

/**
 * Utility class to work with relative paths. 
 * 
 * @author Jordi Hernández Sellés
 *
 *
 */
public class PathNormalizer {
	private static final String SEPARATOR = "/";
	
	/**
	 * Normalizes a bundle path mapping. If it ends with a wildcard, the wildcard is removed. 
	 * @param pathMapping
	 * @return
	 */
	public static final String normalizePathMapping(String pathMapping) {
		pathMapping = normalizePath(pathMapping);
		if(pathMapping.endsWith("/**"))
			pathMapping = pathMapping.substring(0,pathMapping.length()-3);
		return pathMapping;
	}
	
	/**
	 * Normalizes a path and adds a separator at its start. 
	 * @param path
	 * @return
	 */
	public static final String asPath(String path) {
		return(SEPARATOR + normalizePath(path));
	}
	
	/**
	 * Normalizes two paths and joins them as a single path. 
	 * @param prefix
	 * @param path
	 * @return
	 */
	public static final String joinPaths(String prefix,String path) {
		prefix = PathNormalizer.normalizePath(prefix);
		path = PathNormalizer.normalizePath(path);
		StringBuffer sb = new StringBuffer(SEPARATOR);
		if(!"".equals(prefix))
			sb.append(prefix).append(SEPARATOR);
		sb.append(path);
		
		return sb.toString();
		
	}
	
	/**
	 * Removes leading and trailing separators from a path, and removes 
	 * double separators (// is replaced by /). 
	 * @param path
	 * @return
	 */
	public static final String normalizePath(String path) {
		path = path.replaceAll("//", SEPARATOR);
		StringTokenizer tk = new StringTokenizer(path,SEPARATOR);
		StringBuffer sb = new StringBuffer();
		while(tk.hasMoreTokens()) {
			sb.append(tk.nextToken());
			if(tk.hasMoreTokens())
				sb.append(SEPARATOR);
		}
		return sb.toString();
		
	}
	
	/**
	 * Normalizes all the paths in a Set. 
	 * @param paths
	 * @return
	 */
	public static final Set normalizePaths(Set paths) {
		Set ret = new HashSet();
		for(Iterator it = paths.iterator();it.hasNext();) {
			String path = normalizePath((String)it.next());
			ret.add(path);
		}
		return ret;
	}
	
	/**
	 * converts a generation path (such as jar:/some/path/file) into 
	 * a request path that the request handler can understand and process.  
	 * @param path
	 * @return
	 */
	public static String createGenerationPath(String path){
		try {
			path = "/generate.js?" + JawrRequestHandler.GENERATION_PARAM + "=" + URLEncoder.encode(path, "UTF-8");
		} catch (UnsupportedEncodingException neverHappens) {/*URLEncoder:how not to use checked exceptions...*/}
		return path;
	}
}
