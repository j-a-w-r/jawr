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
package net.java.jawr.web.resource.bundle.factory.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.java.jawr.web.exception.DuplicateBundlePathException;
import net.java.jawr.web.resource.ResourceHandler;
import net.java.jawr.web.resource.bundle.JoinableResourceBundle;
import net.java.jawr.web.resource.bundle.factory.util.PathNormalizer;

import org.apache.log4j.Logger;

/**
 * Base class to implement map-based automatic bundles generators. 
 * The generated bundles are added to a Map instance in which the keys 
 * are bundles ids and the values are the bundles. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public abstract class AbstractResourceMapper {
	private static final Logger log = Logger.getLogger(AbstractResourceMapper.class);
	protected String baseDir;
	protected ResourceHandler rsHandler;
	protected List currentBundles;
	protected String resourceExtension;
	private Map bundleMapping;
	
	public AbstractResourceMapper(String baseDir,
			ResourceHandler rsHandler, List currentBundles,
			String resourceExtension) {
		super();
		this.baseDir = PathNormalizer.normalizePath(baseDir);
		this.rsHandler = rsHandler;
		this.currentBundles = new ArrayList();
		if(null != currentBundles)
			this.currentBundles.addAll(currentBundles);
		this.resourceExtension = resourceExtension;
		this.bundleMapping = new HashMap();
	}
	

	/**
	 * Find the required files to add to the mapping. Subclasses must use the addBundleToMap method. 
	 * @throws DuplicateBundlePathException
	 */
	protected abstract void addBundlesToMapping() throws DuplicateBundlePathException;
	
	public final Map getBundleMapping() throws DuplicateBundlePathException{
		addBundlesToMapping();
		return bundleMapping;
	}
	
	/**
	 * Add a bundle and its mapping to the resulting Map. 
	 * @param bundleId
	 * @param mapping
	 * @throws DuplicateBundlePathException 
	 */
	protected final void addBundleToMap(String bundleId, String mapping) throws DuplicateBundlePathException {
		
		for(Iterator it = currentBundles.iterator();it.hasNext(); ) {
			JoinableResourceBundle bundle = (JoinableResourceBundle) it.next();			
			if(bundleId.equals(bundle.getName()) || this.bundleMapping.containsKey(bundleId)){
				log.fatal("Duplicate bundle id resulted from mapping:" + bundleId);
				throw new DuplicateBundlePathException(bundleId);
			}
		}
		
		bundleMapping.put(bundleId, mapping);
	}
	
	
}
