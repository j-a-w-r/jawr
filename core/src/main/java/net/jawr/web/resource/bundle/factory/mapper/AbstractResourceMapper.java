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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;

import org.apache.log4j.Logger;

/**
 * Base class to implement map-based automatic bundles generators. The generated bundles are added to a Map instance in which the keys are bundles ids
 * and the values are the bundles.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public abstract class AbstractResourceMapper {

	/** The logger */
	private static final Logger log = Logger
			.getLogger(AbstractResourceMapper.class);

	/** The base directory */
	protected String baseDir;

	/** The resource handler */
	protected ResourceHandler rsHandler;

	/** The list of current bundles */
	protected List currentBundles;

	/** The resource extension */
	protected String resourceExtension;

	/** The bundle mapping */
	private Map bundleMapping;

	/**
	 * Constructor
	 * 
	 * @param baseDir the base directory of the resource mapper
	 * @param rsHandler the resource handler
	 * @param currentBundles the list of current bundles
	 * @param resourceExtension the resource file extension
	 */
	public AbstractResourceMapper(String baseDir, ResourceHandler rsHandler,
			List currentBundles, String resourceExtension) {
		super();
		this.baseDir = PathNormalizer.normalizePath(baseDir);
		this.rsHandler = rsHandler;
		this.currentBundles = new ArrayList();
		if (null != currentBundles)
			this.currentBundles.addAll(currentBundles);
		this.resourceExtension = resourceExtension;
		this.bundleMapping = new HashMap();
	}

	/**
	 * Find the required files to add to the mapping. Subclasses must use the addBundleToMap method.
	 * 
	 * @throws DuplicateBundlePathException if we try to add a bundle with a name, which already exists.
	 */
	protected abstract void addBundlesToMapping()
			throws DuplicateBundlePathException;

	public final Map getBundleMapping() throws DuplicateBundlePathException {
		addBundlesToMapping();
		return bundleMapping;
	}

	/**
	 * Add a bundle and its mapping to the resulting Map.
	 * 
	 * @param bundleId the bundle Id
	 * @param mapping the mapping
	 * @throws DuplicateBundlePathException if we try to add a bundle with a name, which already exists.
	 */
	protected final void addBundleToMap(String bundleId, String mapping)
			throws DuplicateBundlePathException {

		for (Iterator it = currentBundles.iterator(); it.hasNext();) {
			JoinableResourceBundle bundle = (JoinableResourceBundle) it.next();
			if (bundleId.equals(bundle.getId())
					|| this.bundleMapping.containsKey(bundleId)) {
				log.fatal("Duplicate bundle id resulted from mapping:"
						+ bundleId);
				throw new DuplicateBundlePathException(bundleId);
			}
		}

		bundleMapping.put(bundleId, mapping);
	}

}
