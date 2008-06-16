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
package net.jawr.web.resource.bundle.locale;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * Defult implementation of the LocaleResolver interface. 
 * Uses request.getLocale() to determine the user's Locale. 
 * 
 * @author Jordi Hernández Sellés
 */
public class DefaultLocaleResolver implements LocaleResolver {

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.locale.LocaleResolver#resolveLocaleCode(javax.servlet.http.HttpServletRequest)
	 */
	public String resolveLocaleCode(HttpServletRequest request) {
		if(request.getLocale() != Locale.getDefault())
			return request.getLocale().toString();
		else return null;
	}

}
