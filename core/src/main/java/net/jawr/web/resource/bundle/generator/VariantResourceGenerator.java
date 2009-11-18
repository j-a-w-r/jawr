/**
 * Copyright 2009 Ibrahim Chaehoi
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

import java.util.Map;

/**
 * This interface is implemented by generators which are able to handle variant generated resource.
 * For example, resources which are dependent on the user locale or the skin defined by the user.
 *  
 * @author Ibrahim Chaehoi
 *
 */
public interface VariantResourceGenerator extends PrefixedResourceGenerator {

	/**
	 * Returns a map containing for each type of variant, the list of the available variants.
	 * 
	 * For example, if a generator is handling locale and skin variant, it must returns for a define resource,
	 * Result Map:
	 *  { 
	 *   "locale" : ["", "en", "fr", "es" ],
	 *   "skin" : ["", "skin1", "skin2"]
	 *  }
	 *  
	 * @param resourceName the resource name
	 * @return a map containing for each type of variant, the list of the available variants.
	 */
	public Map getVariants(String resourceName);
	
}
