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
package net.jawr.web.resource;

import java.io.Reader;
import java.nio.channels.FileChannel;
import java.util.Set;

import net.jawr.web.exception.ResourceNotFoundException;

/**
 * Helps in identifying, retrieving and listing of resources. An abstraction of a file system, 
 * offers a common interface to access files in a web servlet context or a traditiional filesystem. 
  * 
 * @author Jordi Hernández Sellés
 */
public interface ResourceHandler {

	/**
	 * Retrieves a single resource. 
	 * @param resourceName String Name of the resource.  
	 * @return
	 */
	public Reader getResource(String resourceName) throws ResourceNotFoundException;
	
	/**
	 * Stores a collected group of resources with the specified name. Creates a text version
	 * and a gzipped binary version. 
	 * @param bundleName
	 * @param bundleResources
	 */
	public void storeBundle(String bundleName, StringBuffer bundleResources);
	
	/**
	 * Retrieves a reader for a bundle from the store. 
	 * @param bundleName
	 * @return
	 */
	public Reader getResourceBundleReader(String bundleName) throws ResourceNotFoundException;
	
	
	/**
	 * Retieves FileChannel on a resource bundle.
	 * @param bundleName
	 * @return FileChannel channel to read the file where the bundle is stored. 
	 */
	public FileChannel getResourceBundleChannel(String bundleName) throws ResourceNotFoundException;
	
	/**
	 * Returns a list of resources at a specified path within the resources directory
	 * (normally in the war). 
	 * @param path
	 * @return
	 */
	public Set getResourceNames(String path);
	
	
	/**
	 * Determines wether a given path is a directory. 
	 * @param path
	 * @return
	 */
	public boolean isDirectory(String path);
}
