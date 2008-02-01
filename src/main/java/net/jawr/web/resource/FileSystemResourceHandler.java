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
package net.java.jawr.web.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.java.jawr.web.exception.InvalidPathException;
import net.java.jawr.web.exception.ResourceNotFoundException;

/**
 * Implementation of resourcehandler that gets its resources from the filesystem.  
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class FileSystemResourceHandler extends AbstractResourceHandler implements ResourceHandler {

	private String baseDir;
	
	/**
	 * Createss a filesystem based handler. 
	 * @param baseDir Directory where js files are located. 
	 * @param tempDirRoot Directory to store temporary files
	 * @param charset Charset to use for reading/writing the files. 
	 */
	public FileSystemResourceHandler(String baseDir, File tempDirRoot,Charset charset) {
		super(tempDirRoot, charset);
		this.baseDir = baseDir.replace('/', File.separatorChar);
        this.baseDir = this.baseDir.replaceAll("%20", " ");
	}

	/* (non-Javadoc)
	 * @see net.java.jawr.web.resource.bundle.ResourceHandler#getResource(java.lang.String)
	 */
	public Reader getResource(String resourceName) throws ResourceNotFoundException {
		Reader rd = null;
		try {
			File resource = new File(baseDir, resourceName);
			
			if(!resource.exists())
				throw new ResourceNotFoundException(baseDir +  resourceName);
			
			FileInputStream fis = new FileInputStream( resource );
	        FileChannel inchannel = fis.getChannel();
	        rd = Channels.newReader(inchannel,charset.newDecoder (),-1);			
		} catch (IOException e) {
			throw new RuntimeException("Unexpected IOException reading resource file with path [" + baseDir +  resourceName + "]");
		}
		
		return rd;
	}

	/* (non-Javadoc)
	 * @see net.java.jawr.web.resource.bundle.ResourceHandler#getResourceNames(java.lang.String)
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
		Collections.addAll(ret, resArray);
		return ret;
	}

	/* (non-Javadoc)
	 * @see net.java.jawr.web.resource.bundle.ResourceHandler#isDirectory(java.lang.String)
	 */
	public boolean isDirectory(String path) {
		path = path.replace('/', File.separatorChar);
		return new File(baseDir, path).isDirectory();
	}	

}
