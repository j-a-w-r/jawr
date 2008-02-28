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

import java.util.List;
import java.util.Set;

import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;

/**
 * Represents a group of related resources which will be referred to by 
 * a single name.  
 * 
 * @author Jordi Hernández Sellés
 */
public interface JoinableResourceBundle {
	
	public static final String LICENSES_FILENAME = ".license";
	public static final String SORT_FILE_NAME = ".sorting";
		
	/**
	 * Returns the name for this bundle. It will normally end with .js, 
	 * since it will be used to refer to the bundle in URLs. 
	 * @return
	 */
	public String getName();
	
	/**
	 * Get the InclusionPattern to determine when/if this bundle should be
	 * included with current configuration. 
	 * @return
	 */
	public InclusionPattern getInclusionPattern();
	
	/**
	 * Returns an ordered list with all the items pertaining to this bundle. 
	 * @return
	 */
	public List getItemPathList();
	
	/**
	 * Returns a set with the license files to include with this bundle. 
	 * @return
	 */
	public Set getLicensesPathList();
	
	/**
	 * Determines if an item path belongs to the bundle. 
	 * @param itemPath
	 * @return
	 */
	public boolean belongsToBundle(String itemPath);
        
    /**
     * Get the URL prefix for this Bundle. It is used to force redownloading
     * when needed. 
     * @return 
     */
    public String getURLPrefix();
    
    /**
     * Get the postprocessor to use in resources before adding them to the bundle
     * @return
     */
    public ResourceBundlePostProcessor getUnitaryPostProcessor();
    
    
    /**
     * Get the postprocesor to use once all files are joined. 
     * @return
     */
    public ResourceBundlePostProcessor getBundlePostProcessor();
	
}
