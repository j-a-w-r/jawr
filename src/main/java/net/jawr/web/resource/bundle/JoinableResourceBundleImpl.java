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
package net.jawr.web.resource.bundle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jawr.web.collections.ConcurrentCollectionsFactory;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ResourceHandler;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.sorting.SortFileParser;

import org.apache.log4j.Logger;

/**
 * Basic implementation of JoinableResourceBundle. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class JoinableResourceBundleImpl implements JoinableResourceBundle {
	
	private static final Logger log = Logger.getLogger(JoinableResourceBundle.class.getName());
	
	private InclusionPattern inclusionPattern;
	private List pathMappings;
	private String name;
	private ResourceHandler resourceHandler;
	protected List itemPathList;
	protected Set licensesPathList;
	private String fileExtension;
	private String urlPrefix;
	private String explorerConditionalExpression;
	
	private Map prefixMap;
	
	protected List localeVariantKeys;
	
	
	private ResourceBundlePostProcessor unitaryPostProcessor;
	private ResourceBundlePostProcessor bundlePostProcessor;
	

	/**
	 * Protected access constructor, which omits the mappings parameter. 
	 * 
	 * @param name String Unique name for this bundle.
     * @param fileExtension String File extensions for this bundle.
     * @param inclusionPattern InclusionPattern Strategy for including this bundle.
     * @param resourceHandler ResourceHandler Used to access the files and folders.
     * @param urlPrefix The prefix to include in URLs whenever this bundle is included in a link. 
	 */
	protected JoinableResourceBundleImpl(	String name, 
			String fileExtension,
			InclusionPattern inclusionPattern,
			ResourceHandler resourceHandler) {
		super();

		this.inclusionPattern = inclusionPattern;
		
		this.name = PathNormalizer.asPath(name);
		this.resourceHandler = resourceHandler;
		this.itemPathList = ConcurrentCollectionsFactory.buildCopyOnWriteArrayList();
		this.licensesPathList = new HashSet();
        this.fileExtension = fileExtension;
        prefixMap = ConcurrentCollectionsFactory.buildConcurrentHashMap();
        
	}	
	
	
    /**
     * 
     * @param name String Unique name for this bundle.
     * @param fileExtension String File extensions for this bundle.
     * @param inclusionPattern InclusionPattern Strategy for including this bundle.
     * @param pathMappings Set Strings representing the folders or files to include, possibly with wildcards.
     * @param resourceHandler ResourceHandler Used to access the files and folders.
     * @param urlPrefix The prefix to include in URLs whenever this bundle is included in a link. 
     */
     public JoinableResourceBundleImpl(	String name, 
        									String fileExtension,
        									InclusionPattern inclusionPattern,
        									List pathMappings, 
											ResourceHandler resourceHandler) {
		this(name, fileExtension, inclusionPattern, resourceHandler);
      
		if(log.isDebugEnabled())
			log.debug("Adding mapped files for bundle " + getName());
		this.pathMappings = pathMappings;
		
		initPathList();
		if(log.isDebugEnabled())
			log.debug("Added " + this.itemPathList .size() + " files and " + licensesPathList.size() + " licenses for the bundle " + getName());
		
	}
	


	/**
	 * Detects all files that belong to this bundle and adds them to the 
	 * items path list. 
	 */
	private void initPathList()	{
		if(log.isDebugEnabled())
			log.debug("Creating bundle path List for " + getName());
		
		for(Iterator it = pathMappings.iterator();it.hasNext();)
		{
			String pathMapping = (String) it.next();

			// Handle generated resources
			if(resourceHandler.isResourceGenerated(pathMapping)){
				itemPathList.add(pathMapping);
			}
			// path ends in /, the folder is included without subfolders
			else if(pathMapping.endsWith("/")) {
				addItemsFromDir(pathMapping,false);
			}
			// path ends in /, the folder is included with all subfolders
			else if(pathMapping.endsWith("/**")){
				addItemsFromDir(pathMapping.substring(0,pathMapping.lastIndexOf("**")),true);
			}
			else if(pathMapping.endsWith(fileExtension)){
				itemPathList.add(PathNormalizer.asPath(pathMapping));
			}
			else if(pathMapping.endsWith(LICENSES_FILENAME)){
				licensesPathList.add(PathNormalizer.asPath(pathMapping));
			}
			else log.warn("Wrong mapping [" + pathMapping  
							+ "] for bundle [" 
							+ this.name 
							+ "]. Please check configuration. ");
		}
		if(log.isDebugEnabled())
			log.debug("Finished creating bundle path List for " + getName());
	}

	/**
	 * Adds all the resources within a path to the item path list. 
	 * @param dirName
	 * @param addSubDirs boolean If subfolders will be included. In such case, 
	 * 							 every folder below the path is included.  
	 */
	private void addItemsFromDir(String dirName, boolean addSubDirs)
	{
		Set resources = resourceHandler.getResourceNames(dirName);
		
			
		if(log.isDebugEnabled()) {
			log.debug("Adding " + resources.size() + " resources from path [" + dirName + "] to bundle " + getName());
		}

		
		// If the directory contains a sorting file, it is used to order the resources. 
		if(resources.contains(SORT_FILE_NAME) || resources.contains("/" + SORT_FILE_NAME)) {
			SortFileParser parser;
			try {
				String sortFilePath = PathNormalizer.joinPaths(dirName, SORT_FILE_NAME);
				parser = new SortFileParser(resourceHandler.getResource(sortFilePath),
															resources,
															dirName);
			} catch (ResourceNotFoundException e) {
				throw new RuntimeException("Unexpected ResourceNotFoundException when reading a sorting file[" + e.getRequestedPath() + "]",e);
			}
			List sortedResources = parser.getSortedResources();
			for(Iterator it = sortedResources.iterator();it.hasNext();) {
				String resourceName = (String) it.next();
				
				// Add subfolders or files
				if(resourceName.endsWith(fileExtension)){
					itemPathList.add(PathNormalizer.asPath(resourceName));
					if(log.isDebugEnabled())
						log.debug("Added to item path list from the sorting file:" + resourceName);
				}
				else if(addSubDirs && resourceHandler.isDirectory(resourceName))
					addItemsFromDir(resourceName,true);
			}		
		}
		
		// Add licenses file
		if(resources.contains(LICENSES_FILENAME) || resources.contains("/" + LICENSES_FILENAME)) {
			licensesPathList.add(PathNormalizer.joinPaths(dirName, LICENSES_FILENAME));
		}
		
		// Add remaining resources (remaining after sorting, or all if no sort file present)
		List folders = new ArrayList();
		for(Iterator it = resources.iterator();it.hasNext();) {
			String resourceName = (String) it.next();
			String resourcePath = PathNormalizer.joinPaths(dirName, resourceName);
			if(resourceName.endsWith(fileExtension)) {
				itemPathList.add(PathNormalizer.asPath(resourcePath));

				if(log.isDebugEnabled())
					log.debug("Added to item path list:" + PathNormalizer.asPath(resourcePath));
			}
			else if(addSubDirs && resourceHandler.isDirectory(resourcePath))
				folders.add(resourceName);
		}
		
		// Add subfolders if requested. Subfolders are added last unless specified in sorting file. 
		if(addSubDirs)
		{
			for(Iterator it = folders.iterator();it.hasNext();) {
				String folderName = (String) it.next();
				addItemsFromDir(PathNormalizer.joinPaths(dirName, folderName),true);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#belongsTobundle(java.lang.String)
	 */
	public boolean belongsToBundle(String itemPath) {		
		return itemPathList.contains(itemPath);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getInclusionPattern()
	 */
	public InclusionPattern getInclusionPattern() {
		return this.inclusionPattern;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getItemPathList()
	 */
	public List getItemPathList() {
		return itemPathList;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getItemPathList(java.lang.String)
	 */
	public List getItemPathList(String variantKey) {
		if(null == variantKey)
			return itemPathList;
		
		List rets = new ArrayList();
		for(Iterator it = itemPathList.iterator();it.hasNext();){
			String path = (String) it.next();			
			if(resourceHandler.isResourceGenerated(path)){
				rets.add(path + '@' + variantKey);
			}
			else rets.add(path);
		}
		return rets; 
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getName()
	 */
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getLicensesPathList()
	 */
	public Set getLicensesPathList() {
		return this.licensesPathList;
	}

    /* (non-Javadoc)
	* @see net.jawr.web.resource.bundle.JoinableResourceBundle#getURLPrefix()
	*/
    public String getURLPrefix(String variantKey) {
    	if(null == this.urlPrefix)
    		throw new IllegalStateException("The bundleDataHashCode must be set before accessing the url prefix.");
    	
    	// Resolves the locale key like resourcebundle does
    	if(null != variantKey && null != this.localeVariantKeys) {
    		String key = getAvailableLocaleVariant(variantKey);
    		if(null != key)
    			return prefixMap.get(variantKey) + "." + key + "/";
    	}
    	return this.urlPrefix + "/";
    }

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#setBundleDataHashCode(int)
	 */
	public void setBundleDataHashCode(String variantKey, int bundleDataHashCode) {		
		String prefix;
		// Since this numbre is used as part of urls, the -sign is converted to 'N'
		if(bundleDataHashCode < 0){
			prefix = "N" + bundleDataHashCode*-1;
		}
		else prefix = ""+bundleDataHashCode;
		
		if(null == variantKey){
			this.urlPrefix = prefix;
		}
		else {
			prefixMap.put(variantKey, prefix);
		}
	}   
	
    /**
     * Resolves a registered path from a locale key, using the same algorithm used to 
     * locate ResourceBundles. 
     *  
     * @param variantKey
     * @return
     */
    private String getAvailableLocaleVariant(String variantKey) {
    	String key = null;
    	if(this.localeVariantKeys.contains(variantKey)){
    		key = variantKey;
    	}
    	else {
    		String subVar = variantKey;
    		while(subVar.indexOf('_') != -1) {
    			subVar = subVar.substring(0,subVar.lastIndexOf('_'));
    			if(this.localeVariantKeys.contains(subVar)){
        			key = subVar;
        		}
    		}
    	}
    	return key;
    }
    

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getUnitaryPostProcessor()
	 */
	public ResourceBundlePostProcessor getUnitaryPostProcessor() {
		return unitaryPostProcessor;
	}



	/**
	 * @param unitaryPostProcessor
	 */
	public void setUnitaryPostProcessor(
			ResourceBundlePostProcessor unitaryPostProcessor) {
		this.unitaryPostProcessor = unitaryPostProcessor;
	}



	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getBundlePostProcessor()
	 */
	public ResourceBundlePostProcessor getBundlePostProcessor() {
		return bundlePostProcessor;
	}



	public void setBundlePostProcessor(
			ResourceBundlePostProcessor bundlePostProcessor) {
		this.bundlePostProcessor = bundlePostProcessor;
	}


	


	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getExplorerConditionalExpression()
	 */
	public String getExplorerConditionalExpression() {
		return explorerConditionalExpression;
	}


	/**
	 * Set the conditional comment expression. 
	 * @param explorerConditionalExpression
	 */
	public void setExplorerConditionalExpression(
			String explorerConditionalExpression) {
		this.explorerConditionalExpression = explorerConditionalExpression;
	}
	
	/**
	 * Set the list of variants for localized resources
	 * @param localeVariantKeys
	 */
	public void setLocaleVariantKeys(List localeVariantKeys) {
		this.localeVariantKeys = localeVariantKeys;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#getLocaleVariantKeys()
	 */
	public List getLocaleVariantKeys(){
		return this.localeVariantKeys;
	}
}
