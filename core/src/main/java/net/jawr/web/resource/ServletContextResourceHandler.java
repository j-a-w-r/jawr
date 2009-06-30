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
package net.jawr.web.resource;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;

import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;

/**
 * Implementation of resourcehandler that gets its resources
 * from a ServletContext instance, as resources contained in 
 * a deployed war archive (or exploded directory). The generated
 * files are stored in the web server temporary folder. 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 *
 */
public class ServletContextResourceHandler extends AbstractResourceHandler implements ResourceHandler {
	
	/** The servlet temp directory property name */
	private static final String SERVLET_CONTEXT_TEMPDIR = "javax.servlet.context.tempdir";
	
	/** The servlet context */
	private ServletContext context;
	
	/**
	 * Constructor
	 * @param context the servlet context
	 * @param charset the charset
	 * @param generatorRegistry the generator registry
	 * @param resourceType the resource type
	 */
	public ServletContextResourceHandler(ServletContext context, Charset charset,GeneratorRegistry generatorRegistry, String resourceType) {
		super((File) context.getAttribute(SERVLET_CONTEXT_TEMPDIR),charset,generatorRegistry, resourceType);		
		this.context  = context;
		this.charset = charset;
	}

	/**
	 * Constructor
	 * @param context the servlet context
	 * @param workingDirectory the working directory
	 * @param charset the charset
	 * @param generatorRegistry the generator registry
	 * @param resourceType the resource type
	 */
	public ServletContextResourceHandler(ServletContext context, String workingDirectory, Charset charset,GeneratorRegistry generatorRegistry, String resourceType) {
		super(workingDirectory,charset,generatorRegistry, resourceType, false);		
		this.context  = context;
		this.charset = charset;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.AbstractResourceHandler#doGetResourceAsStream(java.lang.String)
	 */
	protected InputStream doGetResourceAsStream(String resourceName) {
		
		return context.getResourceAsStream(resourceName);	
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.ResourceHandler#getResource(java.lang.String)
	 */
	public Reader doGetResource(String resourceName)  throws ResourceNotFoundException{
		
		InputStream is = getResourceAsStream(resourceName);		
		return new InputStreamReader(is, charset);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.ResourceHandler#getResourceNames(java.lang.String)
	 */
	public Set getResourceNames(String path) {
		Set paths = context.getResourcePaths(path);
		Set names = new HashSet();
		int length = path.length();
		if(null != paths) {
			for(Iterator it = paths.iterator();it.hasNext();) {
				String resourcePath = (String) it.next();
				names.add(resourcePath.substring(length, resourcePath.length()));
			}
		}
		return names;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.ResourceHandler#isDirectory(java.lang.String)
	 */
	public boolean isDirectory(String path) {
		Set paths = context.getResourcePaths(path);
		return (null != paths && paths.size() > 0);
	}

}
