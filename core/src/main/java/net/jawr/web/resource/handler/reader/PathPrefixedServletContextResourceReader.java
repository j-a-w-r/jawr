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
package net.jawr.web.resource.handler.reader;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Set;

import javax.servlet.ServletContext;

import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.util.StringUtils;


/**
 * This class defines a resource handler which is based on the servlet context (so which retrieves the resource 
 * from the web application),  and which use a path prefix to access the resource.
 * 
 * For example : 
 * if the pathPrefix is set to /css/section1/, to access the resource /css/section1/subsectionA/flower.css,
 * we must use => /subsectionA/flower.css. The path prefix will be automatically added.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class PathPrefixedServletContextResourceReader extends
		ServletContextResourceReader {

	/** The path prefix to append to any requested resource */
	private String pathPrefix;
	
	/**
	 * Constructor 
	 * @param context the context
	 * @param charset the charset
	 */
	public PathPrefixedServletContextResourceReader(ServletContext context, Charset charset, String pathPrefix) {
		super(context, charset);
		if(StringUtils.isNotEmpty(pathPrefix)){
			this.pathPrefix = PathNormalizer.asDirPath(pathPrefix);
		}
	}
	
	/**
	 * Returns the full path for the specified resource
	 * @param path the resource path
	 * @return the full path for the specified resource
	 */
	public String getFullPath(String path){
		
		if(StringUtils.isEmpty(pathPrefix)){
			return path;
		}
		
		return PathNormalizer.asPath(pathPrefix+path);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.ResourceReader#getResource(java.lang.String, boolean)
	 */
	public Reader getResource(String resourceName, boolean processingBundle) {
		
		return super.getResource(getFullPath(resourceName), processingBundle);
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.stream.StreamResourceReader#getResourceAsStream(java.lang.String, boolean)
	 */
	public InputStream getResourceAsStream(String resourceName,
			boolean processingBundle) {
		
		return  super.getResourceAsStream(getFullPath(resourceName), processingBundle);
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.ResourceInfoProvider#getResourceNames(java.lang.String)
	 */
	public Set getResourceNames(String path) {
		return super.getResourceNames(getFullPath(path));
	}
	
}
