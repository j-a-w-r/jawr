/**
 * Copyright 2007-2010 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.jawr.web.collections.ConcurrentCollectionsFactory;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.variant.VariantUtils;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.sorting.SortFileParser;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * Basic implementation of JoinableResourceBundle.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public class JoinableResourceBundleImpl implements JoinableResourceBundle {

	/** The logger */
	private static final Logger LOGGER = Logger
			.getLogger(JoinableResourceBundleImpl.class);

	/** The name of the bundle used in the configuration properties */
	private String name;

	/** The ID for this bundle. The URL, which will identify the bundle. */
	private String id;

	/** The inclusion pattern */
	private InclusionPattern inclusionPattern;

	/** The list of path mappings. It could contains directory mapping like 'myPath/**' */
	private List pathMappings;

	/** The final item path list containing all the resource linked to this bundle */
	protected List itemPathList;

	/** The resource reader handle */
	private ResourceReaderHandler resourceReaderHandler;
	
	/** The generator Registry */
	private GeneratorRegistry generatorRegistry;

	/** The licence path list */
	protected Set licensesPathList;

	/** The file extension */
	private String fileExtension;

	/** The URL prefix */
	private String urlPrefix;

	/** The IE conditional expression */
	private String explorerConditionalExpression;

	/** The alternate URL for the bundle */
	private String alternateProductionURL;

	/** The prefix mapping for locale variant version */
	private Map prefixMap;

	/** The map of variants */
	protected Map variants;
	
	/** The list of variant keys */
	protected List variantKeys;
	
	/** The list of bundle dependencies */
	protected List dependencies;

	/** The file post processor */
	private ResourceBundlePostProcessor unitaryPostProcessor;

	/** The bundle post processor */
	private ResourceBundlePostProcessor bundlePostProcessor;

	/**
	 * Protected access constructor, which omits the mappings parameter.
	 * 
	 * @param id the ID for this bundle.
	 * @param name String Unique name for this bundle.
	 * @param fileExtension String File extensions for this bundle.
	 * @param inclusionPattern InclusionPattern Strategy for including this bundle.
	 * @param resourceReaderHandler ResourceHandler Used to access the files and folders.
	 * @param generatorRegistry The generator registry.
	 */
	public JoinableResourceBundleImpl(String id, String name,
			String fileExtension, InclusionPattern inclusionPattern,
			ResourceReaderHandler resourceReaderHandler, GeneratorRegistry generatorRegistry) {
		super();

		this.inclusionPattern = inclusionPattern;

		this.id = PathNormalizer.asPath(id);
		this.name = name;
		this.resourceReaderHandler = resourceReaderHandler;
		this.generatorRegistry = generatorRegistry;
		this.itemPathList = ConcurrentCollectionsFactory
				.buildCopyOnWriteArrayList();
		this.licensesPathList = new HashSet();
		this.fileExtension = fileExtension;
		prefixMap = ConcurrentCollectionsFactory.buildConcurrentHashMap();

	}

	/**
	 * Constructor
	 * 
	 * @param id the ID of this bundle
	 * @param name Unique name for this bundle.
	 * @param fileExtension File extensions for this bundle.
	 * @param inclusionPattern Strategy for including this bundle.
	 * @param pathMappings Set Strings representing the folders or files to include, possibly with wildcards.
	 * @param resourceReaderHandler Used to access the files and folders.
	 * @param generatorRegistry the generator registry
	 */
	public JoinableResourceBundleImpl(String id, String name,
			String fileExtension, InclusionPattern inclusionPattern,
			List pathMappings, ResourceReaderHandler resourceReaderHandler, GeneratorRegistry generatorRegistry) {
		this(id, name, fileExtension, inclusionPattern, resourceReaderHandler, generatorRegistry);

		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("Adding mapped files for bundle " + id);
		}
		this.pathMappings = pathMappings;

		initPathList();
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("Added " + this.itemPathList.size() + " files and "
					+ licensesPathList.size() + " licenses for the bundle "
					+ id);
		}
	
	}

	/**
	 * Detects all files that belong to this bundle and adds them to the items path list.
	 */
	private void initPathList() {
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("Creating bundle path List for " + this.id);
		}
		
		for (Iterator it = pathMappings.iterator(); it.hasNext();) {
			String pathMapping = (String) it.next();
			boolean isGeneratedPath = generatorRegistry.isPathGenerated(pathMapping);
			// Handle generated resources
			// path ends in /, the folder is included without subfolders
			if (pathMapping.endsWith("/")) {
				addItemsFromDir(pathMapping, false);
			}
			// path ends in /, the folder is included with all subfolders
			else if (pathMapping.endsWith("/**")) {
				addItemsFromDir(pathMapping.substring(0, pathMapping
						.lastIndexOf("**")), true);
			} else if (pathMapping.endsWith(fileExtension)) {
				itemPathList.add(asPath(pathMapping, isGeneratedPath));
			} else if (generatorRegistry.isPathGenerated(pathMapping)) {
				itemPathList.add(pathMapping);
			}else if (pathMapping.endsWith(LICENSES_FILENAME)) {
				licensesPathList.add(asPath(pathMapping, isGeneratedPath));
			} else
				LOGGER.warn("Wrong mapping [" + pathMapping + "] for bundle ["
						+ this.name + "]. Please check configuration. ");
		}
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("Finished creating bundle path List for " + this.id);
		}
	}

	/**
	 * Adds all the resources within a path to the item path list.
	 * 
	 * @param dirName
	 * @param addSubDirs boolean If subfolders will be included. In such case, every folder below the path is included.
	 */
	private void addItemsFromDir(String dirName, boolean addSubDirs) {
		Set resources = resourceReaderHandler.getResourceNames(dirName);
		boolean isGeneratedPath = generatorRegistry.isPathGenerated(dirName);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding " + resources.size() + " resources from path ["
					+ dirName + "] to bundle " + getId());
		}

		// If the directory contains a sorting file, it is used to order the resources.
		if (resources.contains(SORT_FILE_NAME)
				|| resources.contains("/" + SORT_FILE_NAME)) {
			
			String sortFilePath = joinPaths(dirName,
					SORT_FILE_NAME, isGeneratedPath);
			
			Reader reader;
			try {
				reader = resourceReaderHandler.getResource(sortFilePath);
			} catch (ResourceNotFoundException e) {
				throw new BundlingProcessException(
						"Unexpected ResourceNotFoundException when reading a sorting file["
								+ sortFilePath + "]", e);
			}
			
			SortFileParser parser = new SortFileParser(reader, resources, dirName);

			List sortedResources = parser.getSortedResources();
			for (Iterator it = sortedResources.iterator(); it.hasNext();) {
				String resourceName = (String) it.next();

				// Add subfolders or files
				if (resourceName.endsWith(fileExtension)) {
					itemPathList.add(asPath(resourceName, isGeneratedPath));
					if (LOGGER.isDebugEnabled())
						LOGGER
								.debug("Added to item path list from the sorting file:"
										+ resourceName);
				} else if (addSubDirs
						&& resourceReaderHandler.isDirectory(resourceName))
					addItemsFromDir(resourceName, true);
			}
		}

		// Add licenses file
		if (resources.contains(LICENSES_FILENAME)
				|| resources.contains("/" + LICENSES_FILENAME)) {
			licensesPathList.add(joinPaths(dirName,
					LICENSES_FILENAME, isGeneratedPath));
		}

		// Add remaining resources (remaining after sorting, or all if no sort file present)
		List folders = new ArrayList();
		for (Iterator it = resources.iterator(); it.hasNext();) {
			String resourceName = (String) it.next();
			String resourcePath = joinPaths(dirName,
					resourceName, isGeneratedPath);
			if (resourceName.endsWith(fileExtension)) {
				itemPathList.add(asPath(resourcePath, isGeneratedPath));

				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Added to item path list:"
							+ asPath(resourcePath, isGeneratedPath));
			} else if (addSubDirs
					&& resourceReaderHandler.isDirectory(resourcePath))
				folders.add(resourceName);
		}

		// Add subfolders if requested. Subfolders are added last unless specified in sorting file.
		if (addSubDirs) {
			for (Iterator it = folders.iterator(); it.hasNext();) {
				String folderName = (String) it.next();
				addItemsFromDir(joinPaths(dirName, folderName, isGeneratedPath),
						true);
			}
		}
	}

	/**
	 * Normalizes a path and adds a separator at its start, if it's not a generated resource. 
	 * @param path the path
	 * @param generatedResource the flag indicating if the resource has been generated
	 * @return the normalized path
	 */
	private String asPath(String path, boolean generatedResource){
		
		String result = path;
		if(!generatedResource){
			result = PathNormalizer.asPath(path);
		}
		return result;
	}
	
	/**
	 * Normalizes two paths and joins them as a single path. 
	 * @param prefix the path prefix
	 * @param path the path
	 * @param generatedResource the flag indicating if the resource has been generated
	 * @return the normalized path
	 */
	private String joinPaths(String dirName, String folderName, boolean generatedResource){
		
		String result = null;
		if(generatedResource){
			result = PathNormalizer.joinDomainToPath(dirName, folderName);
		}else{
			result = PathNormalizer.joinPaths(dirName, folderName);
		}
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getId()
	 */
	public String getId() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#isComposite()
	 */
	public boolean isComposite() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getUnitaryPostProcessor()
	 */
	public ResourceBundlePostProcessor getUnitaryPostProcessor() {
		return unitaryPostProcessor;
	}

	/**
	 * Sets the unitary post processor
	 * 
	 * @param unitaryPostProcessor the unitary post processor
	 */
	public void setUnitaryPostProcessor(
			ResourceBundlePostProcessor unitaryPostProcessor) {
		this.unitaryPostProcessor = unitaryPostProcessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getBundlePostProcessor()
	 */
	public ResourceBundlePostProcessor getBundlePostProcessor() {
		return bundlePostProcessor;
	}

	/**
	 * Sets the bundle post processor
	 * 
	 * @param bundlePostProcessor the post processor to set
	 */
	public void setBundlePostProcessor(
			ResourceBundlePostProcessor bundlePostProcessor) {
		this.bundlePostProcessor = bundlePostProcessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getExplorerConditionalExpression()
	 */
	public String getExplorerConditionalExpression() {
		return explorerConditionalExpression;
	}

	/**
	 * Set the conditional comment expression.
	 * 
	 * @param explorerConditionalExpression
	 */
	public void setExplorerConditionalExpression(
			String explorerConditionalExpression) {
		this.explorerConditionalExpression = explorerConditionalExpression;
	}

	/**
	 * Set the list of variants for variant resources
	 * 
	 * @param variantSets
	 */
	public void setVariantSets(Map variantSets) {
		
		if(variantSets != null){
			this.variants = new TreeMap(variantSets);
			variantKeys = VariantUtils.getAllVariantKeys(this.variants);	
		}
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getVariants()
	 */
	public Map getVariants() {
		
		return variants;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getLocaleVariantKeys()
	 */
	public List getVariantKeys() {
		
		return variantKeys;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getAlternateProductionURL()
	 */
	public String getAlternateProductionURL() {
		return this.alternateProductionURL;
	}

	/**
	 * Sets the alternate production URL
	 * 
	 * @param alternateProductionURL the alternateProductionURL to set
	 */
	public void setAlternateProductionURL(String alternateProductionURL) {
		this.alternateProductionURL = alternateProductionURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#belongsTobundle(java.lang.String)
	 */
	public boolean belongsToBundle(String itemPath) {
		return itemPathList.contains(itemPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getInclusionPattern()
	 */
	public InclusionPattern getInclusionPattern() {
		return this.inclusionPattern;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setMappings(java.util.List)
	 */
	public void setMappings(List pathMappings) {

		this.pathMappings = pathMappings;
		initPathList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getItemPathList()
	 */
	public List getItemPathList() {
		return itemPathList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getItemPathList(java.lang.String)
	 */
	public List getItemPathList(Map variants) {
		if (variants == null || variants.isEmpty())
			return itemPathList;

		List rets = new ArrayList();
		
		for (Iterator it = itemPathList.iterator(); it.hasNext();) {
			String path = (String) it.next();
			if (generatorRegistry.isPathGenerated(path)) {
				Set variantTypes = generatorRegistry.getGeneratedResourceVariantTypes(path);
				String variantKey = VariantUtils.getVariantKey(variants, variantTypes);
				if(StringUtils.isNotEmpty(variantKey)){
					rets.add(VariantUtils.getVariantBundleName(path, variantKey));
				}else{
					rets.add(path);
				}
			} else{
				rets.add(path);
			}
		}
		return rets;
	}

	/**
	 * Sets the bundle dependencies
	 * @param dependencies the bundle dependencies
	 */
	public void setDependencies(List dependencies) {
		this.dependencies = dependencies;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getDependencies()
	 */
	public List getDependencies() {
		return dependencies;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getLicensesPathList()
	 */
	public Set getLicensesPathList() {
		return this.licensesPathList;
	}

	/**
	 * Sets the licence path list
	 * 
	 * @param licencePathList the list to set
	 */
	public void setLicensesPathList(Set licencePathList) {
		this.licensesPathList = licencePathList;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getURLPrefix(java.util.Map)
	 */
	public String getURLPrefix(Map variants) {
		
		if (null == this.urlPrefix)
			throw new IllegalStateException(
					"The bundleDataHashCode must be set before accessing the url prefix.");

		if (variants != null && !variants.isEmpty()) {
			String key = getAvailableVariant(variants);
			if (StringUtils.isNotEmpty(key)){
				return prefixMap.get(key) + "." + key + "/";
			}
		}
		return this.urlPrefix + "/";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getBundleDataHashCode()
	 */
	public String getBundleDataHashCode(String variantKey) {
		if (StringUtils.isEmpty(variantKey)) {
			return this.urlPrefix;
		} else {
			return (String) prefixMap.get(variantKey);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setBundleDataHashCode(java.lang.String, int)
	 */
	public void setBundleDataHashCode(String variantKey, int bundleDataHashCode) {
		String prefix;
		// Since this number is used as part of urls, the -sign is converted to 'N'
		if (bundleDataHashCode < 0) {
			prefix = "N" + bundleDataHashCode * -1;
		} else
			prefix = Integer.toString(bundleDataHashCode);

		setBundleDataHashCode(variantKey, prefix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setBundleDataHashCode(java.lang.String, java.lang.String)
	 */
	public void setBundleDataHashCode(String variantKey,
			String bundleDataHashCode) {

		String prefix = bundleDataHashCode;

		if (null == variantKey) {
			this.urlPrefix = prefix;
		} else {
			prefixMap.put(variantKey, prefix);
		}
	}

	/**
	 * Resolves a registered path from a variant key.
	 * 
	 * @param variantKey the requested variant key
	 * @return the variant key to use
	 */
	private String getAvailableVariant(Map curVariants) {
		
		String variantKey = null;
		if(variants != null){
			Map availableVariants = generatorRegistry.getAvailableVariantMap(variants, curVariants);
			variantKey = VariantUtils.getVariantKey(availableVariants);
		}
		
		return variantKey;
	}
	
}
