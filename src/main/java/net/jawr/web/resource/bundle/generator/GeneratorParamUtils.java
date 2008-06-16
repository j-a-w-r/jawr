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
 * Utilities to retrieve parameters from generator mappings. 
 * @author Jordi Hernández Sellés
 */
public class GeneratorParamUtils {

	private static final String PARENFINDER_REGEXP = ".*(\\(.*\\)).*";
	private static final String BRACKFINDER_REGEXP = ".*(\\[.*\\]).*";
	
	/**
	 * Get values in parentheses. 
	 * @param param
	 * @param defaultValue Default to return when there are no parentheses. 
	 * @return
	 */
	public static String[] getParenthesesParam(String param, String defaultValue) {
		if(param.matches(PARENFINDER_REGEXP)) {
			defaultValue = param.substring(param.indexOf('(')+1,param.indexOf(')'));

			param = param.substring(0, param.indexOf('(') ) +
			param.substring(param.indexOf(')') + 1);
		}
		return new String[]{param, defaultValue};
	}

	/**
	 * Get values in brackets. 
	 * @param param
	 * @param defaultValue Default to return when there are no parentheses. 
	 * @return
	 */
	public static String[] getBracketsParam(String param, String defaultValue) {
		if(param.matches(BRACKFINDER_REGEXP)) {
			defaultValue = param.substring(param.indexOf('[')+1,param.indexOf(']'));

			param = param.substring(0, param.indexOf('[') ) +
			param.substring(param.indexOf(']') + 1);
		}
		return new String[]{param, defaultValue};
	}
}
