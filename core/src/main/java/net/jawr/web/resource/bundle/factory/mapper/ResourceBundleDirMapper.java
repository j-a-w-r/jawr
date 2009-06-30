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
package net.jawr.web.resource.bundle.factory.mapper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;

import org.apache.log4j.Logger;

/**
 * Utility class to generate ResourceBundles by reading files under a certain path. 
 * Each subdir will generate a RasourceBundle that includes every file and directory
 * below it. 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 *
 */
public class ResourceBundleDirMapper extends AbstractResourceMapper{
	
	/** The logger */
	private static final Logger log = Logger.getLogger(ResourceBundleDirMapper.class);
	
	/** The Set of path to exclude */
	private Set excludedPaths;
	
	/**
	 * Constructor
	 * @param baseDir Root dir from which to define the paths. 
	 * @param rsHandler Resource handler to resolve the file structure
	 * @param currentBundles Bundles created so far
	 * @param resourceExtension Expected resource extension
	 * @param excludedPaths Paths to exclude from the mappings. 
	 */
	public ResourceBundleDirMapper(String baseDir, ResourceHandler rsHandler, List currentBundles,String resourceExtension, Set excludedPaths) {
		super(baseDir,rsHandler,currentBundles,resourceExtension);
		this.excludedPaths = initExcludedPathList(excludedPaths);
	}
	
	/**
	 * Determine which paths are to be excluded based on a set of path mappings from the configuration. 
	 * @param paths the Set of path to exclude
	 * @return the Set of path to exclude
	 */
	private Set initExcludedPathList(Set paths) {
		Set toExclude = new HashSet();
		if(null == paths)
			return toExclude;		
		
		for(Iterator it = paths.iterator();it.hasNext(); ) {
			String path = (String) it.next();
			path = PathNormalizer.asPath(path);			
			toExclude.add(path);
		}
		return toExclude;
	}
	
	
	/**
	 * Generates the resource bunles mapping expressions. 
	 * @return Map A map with the resource bundle key and the mapping for it as a value. 
	 */
	protected void addBundlesToMapping() throws DuplicateBundlePathException {
		Set paths = rsHandler.getResourceNames(baseDir);
		for(Iterator it = paths.iterator(); it.hasNext();) {
			String path = (String) it.next();
			path = PathNormalizer.joinPaths(baseDir, path);
			if( !excludedPaths.contains(path)  && rsHandler.isDirectory(path)) {	
				String bundleKey =  path + resourceExtension;			
				addBundleToMap(bundleKey, path + "/**");
				if(log.isDebugEnabled())
					log.debug("Added [" + bundleKey
							+ "] with value [" + path  + "/**] to a generated path list");
			}
		}
	}
	
}
