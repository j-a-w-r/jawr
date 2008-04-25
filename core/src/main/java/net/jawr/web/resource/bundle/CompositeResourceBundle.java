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

import java.util.Iterator;
import java.util.List;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.ResourceHandler;

/**
 * This class acts as a proxy for a group of bundles which are created independently but share a common id and 
 * act as a single bundle in runtime. It allows to join bundles which have different configuration, such as 
 * different posprocessing filters. 
 * 
 * 
 * @author Jordi Hernández Sellés
 */
public class CompositeResourceBundle extends JoinableResourceBundleImpl {
	
	private List childBundles;

	/**
	 * @param name
	 * @param childBundles
	 * @param inclusionPattern
	 * @param resourceHandler
	 * @param fileExtension
	 * @param config
	 */
	public CompositeResourceBundle(	String name,
									List childBundles,
									InclusionPattern inclusionPattern,
									ResourceHandler resourceHandler,
									String fileExtension, 
									JawrConfig config) {
		
		super(name, fileExtension, inclusionPattern, resourceHandler);
		this.childBundles = childBundles;
		
		boolean debugModeOn = config.isDebugModeOn();
		
		for(Iterator it = this.childBundles.iterator();it.hasNext();) {
			JoinableResourceBundleImpl child = (JoinableResourceBundleImpl) it.next();
			
			// Skip the child as needed
			if( (debugModeOn && child.getInclusionPattern().isExcludeOnDebug()) || 
				(!debugModeOn && child.getInclusionPattern().isIncludeOnDebug()) )
				continue;
			
			this.itemPathList.addAll(child.getItemPathList());
			this.licensesPathList.addAll(child.getLicensesPathList());
			
			// If the child has no postprocessors, apply the composite's if any
			if(null == child.getBundlePostProcessor() ) {
				child.setBundlePostProcessor(this.getBundlePostProcessor());
			}
			if(null == child.getUnitaryPostProcessor() ) {
				child.setUnitaryPostProcessor(this.getUnitaryPostProcessor());
			}
		}
	}

	/**
	 * @return List<JoinableResourceBundle> The bundles which are members of this composite.
	 */
	public List getChildBundles() {
		return childBundles;
	}

}
