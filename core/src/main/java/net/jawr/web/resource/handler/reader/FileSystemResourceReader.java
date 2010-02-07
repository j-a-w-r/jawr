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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.InvalidPathException;

/**
 * This class defines the resource reader which is based on a file system and which can handle
 * text and stream resources.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class FileSystemResourceReader implements TextResourceReader, StreamResourceReader, ResourceBrowser {

	/** The base directory */
	private String baseDir;
	
	/** The charset */
	private Charset charset;
	
	/**
	 * Constructor
	 * 
	 * @param baseDir the base directory
	 * @param charset the charset
	 */
	public FileSystemResourceReader(String baseDir, JawrConfig config) {
		this.baseDir = baseDir;
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
		FileInputStream fis = (FileInputStream) getResourceAsStream(resourceName);
        if(fis != null){
        	FileChannel inchannel = fis.getChannel();
        	rd = Channels.newReader(inchannel,charset.newDecoder (),-1);
        }
		
        return rd;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.ResourceReader#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsStream(String resourceName) {
		
		return getResourceAsStream(resourceName, false);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.stream.StreamResourceReader#getResourceAsStream(java.lang.String, boolean)
	 */
	public InputStream getResourceAsStream(String resourceName,
			boolean processingBundle) {
		
		InputStream is = null;
		try {
			File resource = new File(baseDir, resourceName);
			is = new FileInputStream( resource );
		} catch (FileNotFoundException e) {
			// Nothing to do
		}
		
		return is; 
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceBrowser#getResourceNames(java.lang.String)
	 */
	public Set getResourceNames(String path) {
		path = path.replace('/', File.separatorChar);
		File resource = new File(baseDir, path);
             
		// If the path is not valid throw an exception
		String[] resArray = resource.list();
		if(null == resArray)
			throw new InvalidPathException(baseDir + File.separator + path);
		
		// Make the returned dirs end with '/', to match a servletcontext behavior. 
		for (int i = 0; i < resArray.length; i++) {
			if(isDirectory(path + resArray[i]))
				resArray[i] += '/';
		}
		Set ret = new HashSet();
		ret.addAll(Arrays.asList(resArray));

		return ret;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceBrowser#isDirectory(java.lang.String)
	 */
	public boolean isDirectory(String dirPath) {
		String path = dirPath.replace('/', File.separatorChar);
		return new File(baseDir, path).isDirectory();
	}

}
