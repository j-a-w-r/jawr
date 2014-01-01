/**
 * Copyright 2007-2009 Jordi Hernández Sellés, Ibrahim Chaehoi, Matt Ruby
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
package net.jawr.web.config;
/**
 * This class holds the debug override status in ThreadLocal
 * 
 * @author Matt Ruby
 */
public final class ThreadLocalDebugOverride {
	/**
	 * debugOverride will allow us to override production mode on a request by request basis.
	 * ThreadLocal is used to hold the overridden status throughout a given request.
	 */
	private static ThreadLocal debugOverride = new ThreadLocal();
	
	/**
	 * The debugOverride will be automatially set to false
	 */
	private ThreadLocalDebugOverride() {
		debugOverride.set(Boolean.FALSE);
	}
	/**
	 * Get the flag stating that production mode should be overridden
	 * @return gebugOverride Boolean
	 */
	public static Boolean getDebugOverride() {
		
		Boolean debug = (Boolean) debugOverride.get();
		if(debug == null){
			debug = Boolean.FALSE;
		}
		return debug;
	}

	/**
	 * Set the override flag that will live only for this request
	 * @param override
	 */
	public static void setDebugOverride(final Boolean override) {

		debugOverride.set(override);
	}
}
