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

/**
 * @author jordi
 * Inclusion pattern for bundles. Indicates wether a bundle should be
 * considered global (a library), the order of inclusion if it is global, and
 * the behavior for debugging. 
 * 
 * @author Jordi Hernández Sellés
 */
public class InclusionPattern {
	
	private boolean includeOnDebug;
	private boolean excludeOnDebug;
	private boolean isGlobal;
	private int inclusionOrder;

	/**
	 * Create a new inclusion pattern for a bundle
	 * @param isGlobal If true, the bundle will be included before every other bundle. 
	 * @param inclusionOrder When isGlobal is true, this will set the order of inclusion of this bundle with respect to other global bundles. 
	 * @param includeOnDebug If true, this bundle will only be included in debug mode. 
	 * @param excludeOnDebug If true, this bundle will not be included in debug mode. 
	 */
	public InclusionPattern(boolean isGlobal, int inclusionOrder,
							boolean includeOnDebug, boolean excludeOnDebug) {
		super();
		this.includeOnDebug = includeOnDebug;
		this.excludeOnDebug = excludeOnDebug;
		this.isGlobal = isGlobal;
		this.inclusionOrder = inclusionOrder;
	}
        
        /**
	 * Create a new inclusion pattern for a bundle to include in debug and non-debug mode. .
	 * @param isGlobal If true, the bundle will be included before every other bundle. 
	 * @param inclusionOrder When isGlobal is true, this will set the order of inclusion of this bundle with respect to other global bundles. 
	 */
	public InclusionPattern(boolean isGlobal, int inclusionOrder) {
		
		this(isGlobal, inclusionOrder,false,false);
	}
        /**
	 * Create a new inclusion pattern for a non-global bundle.
	 */
	public InclusionPattern() {
		
		this(false, 0,false,false);
	}
	
	/**
	 * Indicates whether a bundle will only be included when debug mode is on. 
	 * @return
	 */
	public boolean isIncludeOnDebug() {
		return includeOnDebug;
	}
	
	/**
	 * Indicates whether a bundle will not be included when debug mode is on. 
	 * @return
	 */
	public boolean isExcludeOnDebug() {
		return excludeOnDebug;
	}
	
	
	/**
	 * Wether a bundle is global, if it is it will always be included on every page. 
	 * @return
	 */
	public boolean isGlobal() {
		return isGlobal;
	}
	
	/**
	 * For global bundles, states the order of inclusion. 
	 * @return
	 */
	public int getInclusionOrder() {
		return inclusionOrder;
	}
	
	
}
