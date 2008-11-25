/**
 * Copyright 2008 Jordi Hernández Sellés
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
package net.jawr.web.resource.bundle.generator;


/**
 * Abstract implementation of ResourceGenerator with a default return value for the 
 * getMappingPrefix method. 
 * 
 * @author Jordi Hernández Sellés
 */
public abstract class AbstractCSSGenerator implements ResourceGenerator {

	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#getMappingPrefix()
	 */
	public String getDebugModeRequestPath() {
		return ResourceGenerator.CSS_DEBUGPATH;
	}

}
