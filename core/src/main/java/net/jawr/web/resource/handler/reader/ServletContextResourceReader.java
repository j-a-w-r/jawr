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
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;

import net.jawr.web.config.JawrConfig;



/**
 * This class defines the resource reader which is based on the servlet context
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class ServletContextResourceReader implements TextResourceReader, StreamResourceReader, ResourceBrowser {

	/** The sevrlet context */
	private ServletContext context;
	
	/** The charset */
	private Charset charset;
	
	/**
	 * Constructor 
	 * @param context the context
	 * @param charset the charset
	 */
	public ServletContextResourceReader(ServletContext context, JawrConfig config) {
		this.context = context;
		this.charset = config.getResourceCharset();
	}
	

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.ResourceReader#getResource(java.lang.String)
	 */
	public Reader getResource(String resourceName) {
		
		return getResource(resourceName, false);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.ResourceReader#getResource(java.lang.String, boolean)
	 */
	public Reader getResource(String resourceName, boolean processingBundle) {
		
		Reader rd = null;
		InputStream is = context.getResourceAsStream(resourceName);
		if(is != null){
			rd = new InputStreamReader(is, charset);
		}
		return rd;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.stream.StreamResourceReader#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsStream(String resourceName) {
		
		return getResourceAsStream(resourceName, false);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.stream.StreamResourceReader#getResourceAsStream(java.lang.String, boolean)
	 */
	public InputStream getResourceAsStream(String resourceName,
			boolean processingBundle) {
		
		return  context.getResourceAsStream(resourceName);
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.ResourceInfoProvider#getResourceNames(java.lang.String)
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
	 * @see net.jawr.web.resource.handler.ResourceInfoProvider#isDirectory(java.lang.String)
	 */
	public boolean isDirectory(String path) {
		Set paths = context.getResourcePaths(path);
		return (null != paths && paths.size() > 0);
	}

}
