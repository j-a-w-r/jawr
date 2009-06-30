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

import net.jawr.web.exception.InvalidPathException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;

/**
 * Implementation of resourcehandler that gets its resources from the filesystem.  
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class FileSystemResourceHandler extends AbstractResourceHandler implements ResourceHandler {

	/** The base directory */
	private String baseDir;
	
	/**
	 * Creates a filesystem based handler. 
	 * @param baseDir Directory where js files are located. 
	 * @param tempDirRoot Directory to store temporary files
	 * @param charset Charset to use for reading/writing the files. 
	 */
	public FileSystemResourceHandler(String baseDir, File tempDirRoot,Charset charset,GeneratorRegistry generatorRegistry, String resourceType) {
		super(tempDirRoot, charset,generatorRegistry, resourceType);
		this.baseDir = baseDir.replace('/', File.separatorChar);
        this.baseDir = this.baseDir.replaceAll("%20", " ");
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.ResourceHandler#getResourceInputStream(java.lang.String)
	 */
	public InputStream doGetResourceAsStream(String resourceName) {
		
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
	 * @see net.jawr.web.resource.bundle.ResourceHandler#getResource(java.lang.String)
	 */
	public Reader doGetResource(String resourceName) throws ResourceNotFoundException {
		
		FileInputStream fis = (FileInputStream) getResourceAsStream(resourceName);
        FileChannel inchannel = fis.getChannel();
	    return Channels.newReader(inchannel,charset.newDecoder (),-1);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.ResourceHandler#getResourceNames(java.lang.String)
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
	 * @see net.jawr.web.resource.bundle.ResourceHandler#isDirectory(java.lang.String)
	 */
	public boolean isDirectory(String path) {
		path = path.replace('/', File.separatorChar);
		return new File(baseDir, path).isDirectory();
	}

}
