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
package net.jawr.web.resource.bundle.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.FileNameUtils;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.bundle.CompositeResourceBundle;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.factory.mapper.OrphanResourceBundlesMapper;
import net.jawr.web.resource.bundle.factory.mapper.ResourceBundleDirMapper;
import net.jawr.web.resource.bundle.factory.processor.CSSPostProcessorChainFactory;
import net.jawr.web.resource.bundle.factory.processor.JSPostProcessorChainFactory;
import net.jawr.web.resource.bundle.factory.processor.PostProcessorChainFactory;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.factory.util.ResourceBundleDefinition;
import net.jawr.web.resource.bundle.handler.CachedResourceBundlesHandler;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandlerImpl;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;

import org.apache.log4j.Logger;

/**
 * Factory to create a ResourceBundlesHandler as per configuration options set by the user.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public class BundlesHandlerFactory {
	
	/** The logger */
	private static final Logger log = Logger
			.getLogger(BundlesHandlerFactory.class);

	/** The flag indicating if we should use the in memory cache */
	private boolean useInMemoryCache = true;
	
	/** The root directory for the resources */
	private String baseDir = "";
	
	/** The resource type */
	private String resourceType;
	
	/** The file extension */
	private String fileExtension;
	
	/** The keys of the global post processors */
	private String globalPostProcessorKeys;
	
	/** The keys of the unitary post processors */
	private String unitPostProcessorKeys;
	
	/** The set of bundle definitions */
	private Set bundleDefinitions;
	
	/** The resource handler */
	private ResourceHandler resourceHandler;
	
	/** The post processor chain factory */
	private PostProcessorChainFactory chainFactory;
	
	/** The flag indicating if we should use a single resource factory for the orphans resource of the base directory */
	private boolean useSingleResourceFactory = false;
	
	/** The file name for the single file bundle for orphans */
	private String singleFileBundleName;
	
	/** The flag indicating if we should use the directory mapper to define the resource bundles */
	private boolean useDirMapperFactory = false;
	
	/** The set of directory to exclude from the directory mapper factory */
	private Set excludedDirMapperDirs;
	
	/** The jawr config */
	private JawrConfig jawrConfig;
	
	/** The map of custom post processor */
	private Map customPostprocessors;
	
	/** The flag indicating if we should skip the scan for the orphans */
	private boolean scanForOrphans = true;

	/**
	 * Build a ResourceBundlesHandler. Must be invoked after setting at least the ResourceHandler.
	 * 
	 * @param jawrConfig the jawr config
	 * @return the resource bundles handler
	 * @throws DuplicateBundlePathException if two bundles are defined with the same path
	 */
	public ResourceBundlesHandler buildResourceBundlesHandler()
			throws DuplicateBundlePathException {
		if (log.isInfoEnabled())
			log.info("Building resources handler... ");

		// Ensure state is correct
		if (null == jawrConfig)
			throw new IllegalStateException(
					"Must set the JawrConfig for this factory before invoking buildResourceBundlesHandler(). ");

		if (null == resourceHandler)
			throw new IllegalStateException(
					"Must set the resourceHandler for this factory before invoking buildResourceBundlesHandler(). ");
		if (useSingleResourceFactory && null == singleFileBundleName)
			throw new IllegalStateException(
					"Must set the singleFileBundleName when useSingleResourceFactory is set to true. Please check the documentation. ");

		// Initialize custom postprocessors before using the factory to build the postprocessing chains
		if (null != customPostprocessors)
			chainFactory.setCustomPostprocessors(customPostprocessors);

		// List of bundles
		List resourceBundles = new ArrayList();

		boolean processBundle = !jawrConfig.getUseBundleMapping()
				|| !resourceHandler.isExistingMappingFile();
		if (processBundle) {
			initResourceBundles(resourceBundles);
		} else {
			initResourceBundlesFromFullMapping(resourceBundles);
		}

		// Build the postprocessor for bundles
		ResourceBundlePostProcessor processor = null;
		if (null == this.globalPostProcessorKeys)
			processor = this.chainFactory.buildDefaultProcessorChain();
		else
			processor = this.chainFactory
					.buildPostProcessorChain(globalPostProcessorKeys);

		// Build the postprocessor to use on resources before adding them to the bundle.
		ResourceBundlePostProcessor unitProcessor = null;
		if (null == this.unitPostProcessorKeys)
			unitProcessor = this.chainFactory.buildDefaultUnitProcessorChain();
		else
			unitProcessor = this.chainFactory
					.buildPostProcessorChain(unitPostProcessorKeys);

		// Build the handler
		ResourceBundlesHandler collector = new ResourceBundlesHandlerImpl(
				resourceBundles, resourceHandler, jawrConfig, processor,
				unitProcessor);

		// Use the cached proxy if specified when debug mode is off.
		if (useInMemoryCache && !jawrConfig.isDebugModeOn())
			collector = new CachedResourceBundlesHandler(collector);

		collector.initAllBundles();

		return collector;
	}

	/**
	 * Initialize the resource bundles from the mapping file
	 */
	private void initResourceBundlesFromFullMapping(List resourceBundles) {

		if (log.isInfoEnabled()){
			log.info("Building bundles from the full bundle mapping. The bundles will not be processed.");
		}
		Properties mappingProperties = resourceHandler.getJawrBundleMapping();
		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory(resourceType, 
				resourceHandler, chainFactory);
		
		resourceBundles.addAll(factory.getResourceBundles(mappingProperties));
	}

	/**
	 * Initialize the resource bundles
	 * 
	 * @param resourceBundles the resource bundles
	 * @throws DuplicateBundlePathException if two bundles are defined with the same path 
	 */
	private void initResourceBundles(List resourceBundles)
			throws DuplicateBundlePathException {

		// Create custom defined bundles
		if (null != bundleDefinitions) {
			if (log.isInfoEnabled())
				log.info("Adding custom bundle definitions. ");
			for (Iterator it = bundleDefinitions.iterator(); it.hasNext();) {
				ResourceBundleDefinition def = (ResourceBundleDefinition) it
						.next();

				// If this is a composite bundle
				if (def.isComposite()) {
					List childBundles = new ArrayList();
					for (Iterator childIterator = def.getChildren().iterator(); childIterator
							.hasNext();) {
						ResourceBundleDefinition child = (ResourceBundleDefinition) childIterator
								.next();
						childBundles.add(buildResourcebundle(child));
					}
					resourceBundles.add(buildCompositeResourcebundle(def,
							childBundles));
				} else
					resourceBundles.add(buildResourcebundle(def));
			}
		}

		// Use the dirmapper if specified
		if (useDirMapperFactory) {
			if (log.isInfoEnabled())
				log.info("Using ResourceBundleDirMapper. ");

			ResourceBundleDirMapper dirFactory = new ResourceBundleDirMapper(
					baseDir, resourceHandler, resourceBundles, fileExtension,
					excludedDirMapperDirs);
			Map mappings = dirFactory.getBundleMapping();
			for (Iterator it = mappings.entrySet().iterator(); it.hasNext();) {
				Entry entry = (Entry) it.next();
				resourceBundles.add(buildDirMappedResourceBundle((String) entry.getKey(),
						(String) entry.getValue()));
			}
		}

		if (this.scanForOrphans) {
			// Add all orphan bundles
			OrphanResourceBundlesMapper orphanFactory = new OrphanResourceBundlesMapper(
					baseDir, resourceHandler, resourceBundles, fileExtension);
			List orphans = orphanFactory.getOrphansList();

			// Orphans may be added separately or as one single resource bundle.
			if (useSingleResourceFactory) {
				// Add extension to the filename
				if (!singleFileBundleName.endsWith(fileExtension))
					singleFileBundleName += fileExtension;

				if (log.isInfoEnabled())
					log
							.info("Building bundle of orphan resources with the name: "
									+ singleFileBundleName);

				resourceBundles.add(buildOrphansResourceBundle(
						singleFileBundleName, orphans));

			} else {
				if (log.isInfoEnabled())
					log.info("Creating mappings for orphan resources. ");
				for (Iterator it = orphans.iterator(); it.hasNext();) {
					resourceBundles.add(buildOrphanResourceBundle((String) it
							.next()));
				}
			}
		} else if (log.isDebugEnabled()) {
			log.debug("Skipping orphan file auto processing. ");
			if ("".equals(jawrConfig.getServletMapping()))
				log
						.debug("Note that there is no specified mapping for Jawr "
								+ "(it has been seet to serve *.js or *.css requests). "
								+ "The orphan files will become unreachable through the server.");
		}
	}

	/**
	 * Build a Composite resource bundle using a ResourceBundleDefinition
	 * 
	 * @param definition the bundle definition
	 * @param childBundles the list of child bundles
	 * @return a Composite resource bundle
	 */
	private JoinableResourceBundle buildCompositeResourcebundle(
			ResourceBundleDefinition definition, List childBundles) {

		if (log.isDebugEnabled())
			log.debug("Init composite bundle with id:"
					+ definition.getBundleId());

		InclusionPattern include = new InclusionPattern(definition.isGlobal(),
				definition.getInclusionOrder(), definition.isDebugOnly(),
				definition.isDebugNever());

		CompositeResourceBundle composite = new CompositeResourceBundle(
				definition.getBundleId(), definition.getBundleName(), 
				childBundles, include, resourceHandler, fileExtension,
				jawrConfig);
		if (null != definition.getBundlePostProcessorKeys())
			composite.setBundlePostProcessor(chainFactory
					.buildPostProcessorChain(definition
							.getBundlePostProcessorKeys()));

		if (null != definition.getUnitaryPostProcessorKeys())
			composite.setUnitaryPostProcessor(chainFactory
					.buildPostProcessorChain(definition
							.getUnitaryPostProcessorKeys()));

		if (null != definition.getIeConditionalExpression())
			composite.setExplorerConditionalExpression(definition
					.getIeConditionalExpression());

		if (null != definition.getAlternateProductionURL())
			composite.setAlternateProductionURL(definition
					.getAlternateProductionURL());

		return composite;
	}

	/**
	 * Build a JoinableResourceBundle using a ResourceBundleDefinition
	 * 
	 * @param definition the resource bundle definition
	 * @return a JoinableResourceBundle
	 */
	private JoinableResourceBundle buildResourcebundle(
			ResourceBundleDefinition definition) {
		if (log.isDebugEnabled())
			log.debug("Init bundle with id:" + definition.getBundleId());

		InclusionPattern include = new InclusionPattern(definition.isGlobal(),
				definition.getInclusionOrder(), definition.isDebugOnly(),
				definition.isDebugNever());

		JoinableResourceBundleImpl newBundle = new JoinableResourceBundleImpl(
				definition.getBundleId(), definition.getBundleName(), 
				fileExtension, include, definition.getMappings(),
				resourceHandler);
		if (null != definition.getBundlePostProcessorKeys())
			newBundle.setBundlePostProcessor(chainFactory
					.buildPostProcessorChain(definition
							.getBundlePostProcessorKeys()));

		if (null != definition.getUnitaryPostProcessorKeys())
			newBundle.setUnitaryPostProcessor(chainFactory
					.buildPostProcessorChain(definition
							.getUnitaryPostProcessorKeys()));

		if (null != definition.getIeConditionalExpression())
			newBundle.setExplorerConditionalExpression(definition
					.getIeConditionalExpression());

		if (null != definition.getLocaleVariantKeys())
			newBundle.setLocaleVariantKeys(definition.getLocaleVariantKeys());

		if (null != definition.getAlternateProductionURL())
			newBundle.setAlternateProductionURL(definition
					.getAlternateProductionURL());

		return newBundle;
	}

	/**
	 * Build a bundle based on a mapping returned by the ResourceBundleDirMapperFactory.
	 * 
	 * @param bundleId the bundle Id
	 * @param pathMapping the path mapping
	 * @return a bundle based on a mapping returned by the ResourceBundleDirMapperFactory
	 */
	private JoinableResourceBundle buildDirMappedResourceBundle(
			String bundleId, String pathMapping) {
		List path = Collections.singletonList(pathMapping);
		JoinableResourceBundle newBundle = new JoinableResourceBundleImpl(
				bundleId, generateBundleNameFromBundleId(bundleId),
				fileExtension, new InclusionPattern(), path, resourceHandler);
		return newBundle;
	}

	/**
	 * Generates the bundle ID from the bundle name
	 * 
	 * @param bundleId the bundle name
	 * @return the generated bundle ID
	 */
	private String generateBundleNameFromBundleId(String bundleId) {
		String bundleName = bundleId;
		if(bundleName.startsWith("/")){
			bundleName = bundleName.substring(1);
		}
		int idxExtension = FileNameUtils.indexOfExtension(bundleName);
		if(idxExtension != -1){
			bundleName = bundleName.substring(0, idxExtension);
		}
		return bundleName.replaceAll("/", "_");
	}

	/**
	 * Builds a single bundle containing all the paths specified. Useful to make a single bundle out of every resource that is orphan after processing
	 * config definitions.
	 * 
	 * @param bundleId the bundle Id
	 * @param orphanPaths the orphan paths
	 * @return a single bundle containing all the paths specified
	 */
	private JoinableResourceBundle buildOrphansResourceBundle(
			String bundleId, List orphanPaths) {
		JoinableResourceBundle newBundle = new JoinableResourceBundleImpl(
				bundleId, generateBundleNameFromBundleId(bundleId), 
				fileExtension, new InclusionPattern(), orphanPaths,
				resourceHandler);
		return newBundle;
	}

	/**
	 * Build a non-global, single-file resource bundle for orphans.
	 * 
	 * @param orphanPath the path
	 * @return a non-global, single-file resource bundle for orphans.
	 */
	private JoinableResourceBundle buildOrphanResourceBundle(String orphanPath) {
		String mapping = orphanPath;// .startsWith("/") ? orphanPath.substring(0) : orphanPath;

		List paths = Collections.singletonList(mapping);
		JoinableResourceBundle newBundle = new JoinableResourceBundleImpl(
				orphanPath, generateBundleNameFromBundleId(orphanPath), 
				fileExtension, new InclusionPattern(), paths, resourceHandler);
		return newBundle;
	}

	/**
	 * Set the type of bundle (js or css) to use for this factory.
	 * 
	 * @param resourceType the resource type
	 */
	public void setBundlesType(String resourceType) {
		// Set the extension for resources and bundles
		this.resourceType = resourceType;
		this.fileExtension = "." + resourceType.toLowerCase();

		// Create the chain factory.
		if ("js".equals(resourceType))
			this.chainFactory = new JSPostProcessorChainFactory();
		else
			this.chainFactory = new CSSPostProcessorChainFactory();
	}

	/**
	 * Set the custom bundle definitions to use.
	 * 
	 * @param bundleDefinitions the set of bundle definitions 
	 */
	public void setBundleDefinitions(Set bundleDefinitions) {
		this.bundleDefinitions = bundleDefinitions;
	}

	/**
	 * Set the base dir from which to fetch the resources.
	 * 
	 * @param baseDir the base directory to set
	 */
	public void setBaseDir(String baseDir) {
		this.baseDir = PathNormalizer.normalizePath(baseDir);
	}

	/**
	 * Set the keys to pass to the postprocessor factory upon processors creation. If none specified, the default version is used.
	 * 
	 * @param globalPostProcessorKeys String Comma separated list of processor keys.
	 */
	public void setGlobalPostProcessorKeys(String globalPostProcessorKeys) {
		this.globalPostProcessorKeys = globalPostProcessorKeys;
	}

	/**
	 * Set the keys to pass to the postprocessor factory upon unitary processors creation. If none specified, the default version is used.
	 * 
	 * @param unitPostProcessorKeys String Comma separated list of processor keys.
	 */
	public void setUnitPostProcessorKeys(String unitPostProcessorKeys) {
		this.unitPostProcessorKeys = unitPostProcessorKeys;
	}

	/**
	 * Set the resource handler to use for file access.
	 * 
	 * @param rsHandler
	 */
	public void setResourceHandler(ResourceHandler rsHandler) {
		this.resourceHandler = rsHandler;
	}

	/**
	 * Set wether resoures not specifically mapped to any bundle should be joined together in a single bundle, or served separately.
	 * 
	 * @param useSingleResourceFactory boolean If true, bundles are joined together. In that case, the singleFileBundleName must be set as well.
	 */
	public void setUseSingleResourceFactory(boolean useSingleResourceFactory) {
		this.useSingleResourceFactory = useSingleResourceFactory;
	}

	/**
	 * Set the name for the joint orphans bundle. Must be set when useSingleResourceFactory is true.
	 * 
	 * @param singleFileBundleName
	 */
	public void setSingleFileBundleName(String singleFileBundleName) {
		if (null != singleFileBundleName)
			this.singleFileBundleName = PathNormalizer
					.normalizePath(singleFileBundleName);
	}

	/**
	 * If true, the mapper factory that creates bundles from all directories under baseDir will be used.
	 * 
	 * @param useDirMapperFactory
	 */
	public void setUseDirMapperFactory(boolean useDirMapperFactory) {
		this.useDirMapperFactory = useDirMapperFactory;
	}

	/**
	 * Set wether bundles will be cached in memory instead of being always read from the filesystem.
	 * 
	 * @param useInMemoryCache
	 */
	public void setUseInMemoryCache(boolean useInMemoryCache) {
		this.useInMemoryCache = useInMemoryCache;
	}

	/**
	 * Sets the paths to exclude when using the dirMapper.
	 * 
	 * @param excludedDirMapperDirs
	 */
	public void setExludedDirMapperDirs(Set exludedDirMapperDirs) {
		if (null != excludedDirMapperDirs)
			this.excludedDirMapperDirs = PathNormalizer
					.normalizePaths(exludedDirMapperDirs);
	}

	/**
	 * Sets the Jawr configuration
	 * @param jawrConfig the configuration to set
	 */
	public void setJawrConfig(JawrConfig jawrConfig) {
		this.jawrConfig = jawrConfig;
	}

	/**
	 * Sets the map of custom post processor 
	 * @param customPostprocessors the map to set
	 */
	public void setCustomPostprocessors(Map customPostprocessors) {
		this.customPostprocessors = customPostprocessors;
	}

	/**
	 * Sets the flag inficating if we should scan or not for the orphan resources
	 * @param scanForOrphans the flag to set
	 */
	public void setScanForOrphans(boolean scanForOrphans) {
		this.scanForOrphans = scanForOrphans;
	}

}
