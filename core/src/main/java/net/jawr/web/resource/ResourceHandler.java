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

import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.util.Set;

import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.JoinableResourceBundleContent;

/**
 * Helps in identifying, retrieving and listing of resources. An abstraction of a file system, 
 * offers a common interface to access files in a web servlet context or a traditiional filesystem. 
  * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public interface ResourceHandler {

	/**
	 * Retrieves the resource input stream of a resource. 
	 * @param resourceName String Name of the resource.  
	 * @return a input stream of the resource
	 */
	public InputStream getResourceAsStream(String resourceName) throws ResourceNotFoundException;
	
	/**
	 * Retrieves a single resource. 
	 * @param resourceName String Name of the resource.  
	 * @return a reader for the resource
	 */
	public Reader getResource(String resourceName) throws ResourceNotFoundException;
	
	/**
	 * Retrieves a single resource. 
	 * @param resourceName String Name of the resource.  
	 * @param processingBundle the flag indicating that we are currently processing the bundles
	 * @return the reader to the resource
	 */
	public Reader getResource(String resourceName, boolean processingBundle) throws ResourceNotFoundException;
	
	/**
	 * Retrieves a css classpath resource. 
	 * @param resourceName String Name of the resource.  
	 * @return a css classpath resource. 
	 */
	public Reader getCssClasspathResource(String resourceName) throws ResourceNotFoundException;
	
	/**
	 * Stores a collected group of resources with the specified name. 
	 * Creates a text version, a gzipped binary version, and the CSS classpath file for DEBUG. 
	 * @param bundleName the bundle name.
	 * @param bundleResources the bundle resources
	 */
	public void storeBundle(String bundleName,
			JoinableResourceBundleContent bundleResourcesContent);
	
	/**
	 * Retrieves a reader for a bundle from the store. 
	 * @param bundleName the bundle name.
	 * @return a reader for a bundle from the store. 
	 */
	public Reader getResourceBundleReader(String bundleName) throws ResourceNotFoundException;
	
	/**
	 * Retrieves FileChannel on a resource bundle.
	 * @param bundleName the bundle name
	 * @return FileChannel channel to read the file where the bundle is stored. 
	 */
	public FileChannel getResourceBundleChannel(String bundleName) throws ResourceNotFoundException;
	
	/**
	 * Returns a list of resources at a specified path within the resources directory
	 * (normally in the war). 
	 * @param path
	 * @return a list of resources at the specified path
	 */
	public Set getResourceNames(String path);
	
	
	/**
	 * Determines wether a given path is a directory. 
	 * @param path the path to check
	 * @return true if the path is a directory
	 */
	public boolean isDirectory(String path);
	
	/**
	 * Determines if a resource is not strictly a file in the war structure but  
	 * a resource either java generated or read from elsewhere. 
	 * 
	 * @param path the path to check
	 * @return true if a resource is not strictly a file in the war structure but  
	 * a resource either java generated or read from elsewhere
	 */
	public boolean isResourceGenerated(String path);
}
