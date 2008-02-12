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
package net.jawr.web.resource.bundle.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.DuplicateBundlePathException;
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
 *
 */
public class BundlesHandlerFactory {
	private static final Logger log = Logger.getLogger(BundlesHandlerFactory.class);
	
	private boolean useInMemoryCache = true;
	private String baseDir = "";
	private String fileExtension;
	private String commonURLPrefix;
	private String globalPostProcessorKeys;
	private String unitPostProcessorKeys; 
	private Set bundleDefinitions;
	private ResourceHandler resourceHandler;
	private PostProcessorChainFactory chainFactory;
	private boolean useSingleResourceFactory = false;
	private String singleFileBundleName;
	private boolean useDirMapperFactory = false;
	private Set excludedDirMapperDirs;
	private JawrConfig jawrConfig;
	

	/**
	 * Build a ResourceBundlesHandler. Must be invoked after setting at least the ResourceHandler. 
	 * @param jawrConfig
	 * @return
	 * @throws DuplicateBundlePathException 
	 */
	public ResourceBundlesHandler buildResourceBundlesHandler() throws DuplicateBundlePathException {
		if(log.isInfoEnabled())
			log.info("Building resources handler... ");
		
		// Ensure state is correct
		if(null == jawrConfig)
			throw new IllegalStateException("Must set the JawrConfig for this factory before invoking buildResourceBundlesHandler(). ");

		if(null == resourceHandler)
			throw new IllegalStateException("Must set the resourceHandler for this factory before invoking buildResourceBundlesHandler(). ");
		if(useSingleResourceFactory && null == singleFileBundleName)
			throw new IllegalStateException("Must set the singleFileBundleName when useSingleResourceFactory is set to true. Please check the documentation. ");
		if(null == this.commonURLPrefix)
			throw new IllegalStateException("Must set the the commonURLPrefix. Please check the documentation. ");
			
		
		// List of bundles
		List resourceBundles = new ArrayList();
		
		// Create custom defined bundles
		if(null != bundleDefinitions) {
			if(log.isInfoEnabled())
				log.info("Adding custom bundle definitions. ");
			for(Iterator it = bundleDefinitions.iterator();it.hasNext();) {
				ResourceBundleDefinition def = (ResourceBundleDefinition)it.next();
				
				// If this is a composite bundle
				if(def.isComposite()) {
					List childBundles = new ArrayList();
					for(Iterator childIterator = def.getChildren().iterator();childIterator.hasNext();) {
						ResourceBundleDefinition child = (ResourceBundleDefinition)childIterator.next();
						childBundles.add(buildResourcebundle(child));
					}
					resourceBundles.add(buildCompositeResourcebundle(def,childBundles));					
				}				
				else resourceBundles.add(buildResourcebundle(def));
			}
		}
		
		// Use the dirmapper if specified
		if(useDirMapperFactory) {
			if(log.isInfoEnabled())
				log.info("Using ResourceBundleDirMapper. ");
			
			ResourceBundleDirMapper dirFactory = new ResourceBundleDirMapper(	baseDir,
																				resourceHandler,
																				resourceBundles,
																				fileExtension,
																				excludedDirMapperDirs);
			Map mappings = dirFactory.getBundleMapping();
			for(Iterator it = mappings.keySet().iterator();it.hasNext();) {
				String key = (String) it.next();
				resourceBundles.add(buildDirMappedResourceBundle(key,(String)mappings.get(key)));
			}
		}
		
		// Add all orphan bundles
		OrphanResourceBundlesMapper orphanFactory = new OrphanResourceBundlesMapper(baseDir,resourceHandler,resourceBundles,fileExtension);
		List orphans = orphanFactory.getOrphansList();
		
		// Orphans may be added separately or as one single resource bundle. 
		if(useSingleResourceFactory){
			// Add extension to the filename
			if(!singleFileBundleName.endsWith(fileExtension))
				singleFileBundleName += fileExtension;
				
			if(log.isInfoEnabled())
				log.info("Building bundle of orphan resources with the name: " + singleFileBundleName);

			resourceBundles.add(buildOrphansResourceBundle(singleFileBundleName, orphans));
		
		}
		else {
			if(log.isInfoEnabled())
				log.info("Creating mappings for orphan resources. ");
			for(Iterator it = orphans.iterator(); it.hasNext(); ) {
				resourceBundles.add(buildOrphanResourceBundle((String)it.next()));
			}
		}
				
		// Build the postprocessor for bundles 
		ResourceBundlePostProcessor processor = null;
		if(null == this.globalPostProcessorKeys)
			processor = this.chainFactory.buildDefaultProcessorChain();
		else processor = this.chainFactory.buildPostProcessorChain(globalPostProcessorKeys);
		
		// Build the postprocessor to use on resources before adding them to the bundle. 
		ResourceBundlePostProcessor unitProcessor = null;
		if(null == this.unitPostProcessorKeys)
			unitProcessor = this.chainFactory.buildDefaultUnitProcessorChain();
		
		// Build the handler
		ResourceBundlesHandler collector = new ResourceBundlesHandlerImpl(resourceBundles,resourceHandler,jawrConfig,processor,unitProcessor);
		
		// Use the cached proxy if specified when debug mode is off. 
		if(useInMemoryCache && !jawrConfig.isDebugModeOn())
			collector = new CachedResourceBundlesHandler(collector);
		
		collector.initAllBundles();
		
		return collector;
	}
	
	/**
	 * Build a Composite resource bundle using a ResourceBundleDefinition
	 * @param definition
	 * @param childBundles List<JoinableResourceBundle>
	 * @return
	 */
	private JoinableResourceBundle buildCompositeResourcebundle(ResourceBundleDefinition definition, List childBundles) {
		
		if(log.isDebugEnabled())
			log.debug("Init composite bundle with id:" +definition.getBundleId());
		
		InclusionPattern include = new InclusionPattern(definition.isGlobal(),
														definition.getInclusionOrder(),
														definition.isDebugOnly(),
														definition.isDebugNever());
		
		String prefix = null == definition.getPrefix() ? commonURLPrefix : definition.getPrefix();
		
		CompositeResourceBundle composite = new CompositeResourceBundle(definition.getBundleId(),
																		childBundles,
																		include,
																		resourceHandler,
																		prefix,
																		fileExtension,
																		jawrConfig);
		if(null != definition.getBundlePostProcessorKeys())
			composite.setBundlePostProcessor(chainFactory.buildPostProcessorChain(definition.getBundlePostProcessorKeys()));
		
		if(null != definition.getUnitaryPostProcessorKeys())
			composite.setUnitaryPostProcessor(chainFactory.buildPostProcessorChain(definition.getUnitaryPostProcessorKeys()));
		
		return composite;
	}
	
	/**
	 * Build a JoinableResourceBundle using a ResourceBundleDefinition
	 * @param definition
	 * @return
	 */
	private JoinableResourceBundle buildResourcebundle(ResourceBundleDefinition definition) {
		if(log.isDebugEnabled())
			log.debug("Init bundle with id:" +definition.getBundleId());
		
		InclusionPattern include = new InclusionPattern(definition.isGlobal(),
														definition.getInclusionOrder(),
														definition.isDebugOnly(),
														definition.isDebugNever());
		
		String prefix = null == definition.getPrefix() ? commonURLPrefix : definition.getPrefix();
		JoinableResourceBundleImpl newBundle = new JoinableResourceBundleImpl(	definition.getBundleId(),
																			fileExtension,
																			include,
																			definition.getMappings(),
																			resourceHandler,
																			prefix);
		if(null != definition.getBundlePostProcessorKeys())
			newBundle.setBundlePostProcessor(chainFactory.buildPostProcessorChain(definition.getBundlePostProcessorKeys()));
		
		if(null != definition.getUnitaryPostProcessorKeys())
			newBundle.setUnitaryPostProcessor(chainFactory.buildPostProcessorChain(definition.getUnitaryPostProcessorKeys()));
		
		return newBundle;
	}
	
	/**
	 * Build a bundle based on a mapping returned by the ResourceBundleDirMapperFactory. 
	 * @param bundleId
	 * @param pathMapping
	 * @return
	 */
	private JoinableResourceBundle buildDirMappedResourceBundle(String bundleId,String pathMapping) {
		List path = Collections.singletonList(pathMapping);
		JoinableResourceBundle newBundle = new JoinableResourceBundleImpl(	bundleId,
																			fileExtension,
																			new InclusionPattern(),
																			path,
																			resourceHandler,
																			commonURLPrefix);
		return newBundle;
	}
	
	/**
	 * Builds a single bundle containing all the paths specified. Useful to make a single bundle out of every 
	 * resource that is orphan after processing config definitions. 
	 * @param bundleName
	 * @param orphanPaths
	 * @return
	 */
	private JoinableResourceBundle buildOrphansResourceBundle(String bundleName, List orphanPaths) {
		JoinableResourceBundle newBundle = new JoinableResourceBundleImpl(	bundleName,
																			fileExtension,
																			new InclusionPattern(),
																			orphanPaths,
																			resourceHandler,
																			commonURLPrefix);
		return newBundle;
	}
	
	/**
	 * Build a non-global, single-file resource bundle. 
	 * @param orphanPath
	 * @return
	 */
	private JoinableResourceBundle buildOrphanResourceBundle(String orphanPath) {
		String mapping = orphanPath;//.startsWith("/") ? orphanPath.substring(0) : orphanPath;
		
		List paths = Collections.singletonList(mapping);
		JoinableResourceBundle newBundle = new JoinableResourceBundleImpl(	orphanPath,
											fileExtension,
											new InclusionPattern(),
											paths,
											resourceHandler,
											commonURLPrefix);
		return newBundle;
	}


	
	/**
	 * Set the type of bundle (js or css) to use for this factory. 
	 * @param bundlesType
	 */
	public void setBundlesType(String bundlesType) {
		// Set the extension for resources and bundles
		this.fileExtension = "." + bundlesType.toLowerCase();
		
		// Create the chain factory. 
		if("js".equals(bundlesType))
			this.chainFactory = new JSPostProcessorChainFactory();
		else this.chainFactory = new CSSPostProcessorChainFactory();
	}
	
	/**
	 * Set the custom bundle definitions to use. 
	 * @param bundleDefinitions
	 */
	public void setBundleDefinitions(Set bundleDefinitions) {
		this.bundleDefinitions = bundleDefinitions;
	}
	

	/**
	 * Set the base dir from which to fetch the resources. 
	 * @param baseDir
	 */
	public void setBaseDir(String baseDir) {
		this.baseDir = PathNormalizer.normalizePath(baseDir);
	}

	/**
	 * Set the keys to pass to the postprocessor factory upon processors creation. 
	 * If none specified, the default version is used. 
	 * @param globalPostProcessorKeys String Comma separated list of processor keys. 
	 */
	public void setGlobalPostProcessorKeys(String globalPostProcessorKeys) {
		this.globalPostProcessorKeys = globalPostProcessorKeys;
	}

	/**
	 * Set the keys to pass to the postprocessor factory upon unitary processors creation. 
	 * If none specified, the default version is used. 
	 * @param unitPostProcessorKeys String Comma separated list of processor keys. 
	 */
	public void setUnitPostProcessorKeys(String unitPostProcessorKeys) {
		this.unitPostProcessorKeys = unitPostProcessorKeys;
	}


	/**
	 * Set the resource handler to use for file access. 
	 * @param rsHandler
	 */
	public void setResourceHandler(ResourceHandler rsHandler) {
		this.resourceHandler = rsHandler;
	}


	/**
	 * Set wether resoures not specifically mapped to any bundle should be 
	 * joined together in a single bundle, or served separately. 
	 * @param useSingleResourceFactory boolean If true, bundles are joined together. In that case, the singleFileBundleName 
	 * must be set as well. 
	 */
	public void setUseSingleResourceFactory(boolean useSingleResourceFactory) {
		this.useSingleResourceFactory = useSingleResourceFactory;
	}


	/**
	 * Set the name for the joint orphans bundle. Must be set when useSingleResourceFactory is true. 
	 * @param singleFileBundleName
	 */
	public void setSingleFileBundleName(String singleFileBundleName) {
		if(null != singleFileBundleName)
			this.singleFileBundleName = PathNormalizer.normalizePath(singleFileBundleName);
	}


	/**
	 * If true, the mapper factory that creates bundles from all directories under baseDir will be used. 
	 * @param useDirMapperFactory
	 */
	public void setUseDirMapperFactory(boolean useDirMapperFactory) {
		this.useDirMapperFactory = useDirMapperFactory;
	}


	/**
	 * Set wether bundles will be cached in memory instead of being always read from the filesystem. 
	 * @param useInMemoryCache
	 */
	public void setUseInMemoryCache(boolean useInMemoryCache) {
		this.useInMemoryCache = useInMemoryCache;
	}


	/**
	 * Set the paths to exclude when using the dirMapper. 
	 * @param excludedDirMapperDirs
	 */
	public void setExludedDirMapperDirs(Set exludedDirMapperDirs) {
		if(null != excludedDirMapperDirs)
			this.excludedDirMapperDirs = PathNormalizer.normalizePaths(exludedDirMapperDirs);
	}


	public void setCommonURLPrefix(String commonURLPrefix) {
		this.commonURLPrefix = commonURLPrefix;
	}

	public void setJawrConfig(JawrConfig jawrConfig) {
		this.jawrConfig = jawrConfig;
	}
	
}
