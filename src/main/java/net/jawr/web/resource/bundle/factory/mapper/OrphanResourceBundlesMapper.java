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
package net.jawr.web.resource.bundle.factory.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;

import org.apache.log4j.Logger;

/**
 * Instances of this class will find all the resources which don't belong to 
 * any defined bundle. Will return a mapping for each of them. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class OrphanResourceBundlesMapper {
	private static final Logger log = Logger.getLogger(OrphanResourceBundlesMapper.class);
	protected String baseDir;
	protected ResourceHandler rsHandler;
	protected List currentBundles;
	protected String resourceExtension;
	private List bundleMapping;
	
	public OrphanResourceBundlesMapper(String baseDir,
			ResourceHandler rsHandler, List currentBundles,
			String resourceExtension) {
		this.baseDir = "/" + PathNormalizer.normalizePath(baseDir) + "/**";
		this.rsHandler = rsHandler;
		this.currentBundles = new ArrayList();
		if(null != currentBundles)
			this.currentBundles.addAll(currentBundles);
		this.resourceExtension = resourceExtension;
		this.bundleMapping = new ArrayList();
	}

	/**
	 * Scan all dirs starting at baseDir, and add each orphan 
	 * resource to the resources map.  
	 * @return
	 */
	public List getOrphansList() throws DuplicateBundlePathException {
		
		// Create a mapping for every resource available
		JoinableResourceBundleImpl tempBundle = new JoinableResourceBundleImpl("orphansTemp",
																				this.resourceExtension,
																				new InclusionPattern(),
																				Collections.singletonList(this.baseDir),
																				rsHandler,
																				"");
		
		// Add licenses
		Set licensesPathList = tempBundle.getLicensesPathList();
		for(Iterator it = licensesPathList.iterator(); it.hasNext();) {
			addFileIfNotMapped((String)it.next());
		}
		
		// Add resources
		List allPaths = tempBundle.getItemPathList();
		for(Iterator it = allPaths.iterator(); it.hasNext();) {
			addFileIfNotMapped((String)it.next());
		}
		return this.bundleMapping;
	}

	
	
	/**
	 * Determine wether a resource is already added to some bundle, add it to the list if it is not. 
	 * @param filePath
	 * @param currentMappedResources
	 */
	private void addFileIfNotMapped(String filePath)  throws DuplicateBundlePathException{				
		
		for(Iterator it = currentBundles.iterator();it.hasNext(); ) {
			JoinableResourceBundle bundle = (JoinableResourceBundle) it.next();			
			List items = bundle.getItemPathList();
			
			Set licenses = bundle.getLicensesPathList();
			/*log.fatal(" LICENSES:" + filePath);
			for(Iterator it3 = licenses.iterator();it3.hasNext();) {
				log.fatal(" PATH:" + it3.next());
			}*/
			if(items.contains(filePath))
				return;
			else if (licenses.contains(filePath))
				return;
			else if(filePath.equals(bundle.getName())){
				log.fatal("Duplicate bundle id resulted from orphan mapping of:" + filePath);
				throw new DuplicateBundlePathException(filePath);
			}
		}
		if(log.isDebugEnabled())
			log.debug("Adding orphan resource: " + filePath);
		
		// If we got here, the resource belongs to no other bundle.  
		bundleMapping.add(filePath);		
	}
	
	
}
