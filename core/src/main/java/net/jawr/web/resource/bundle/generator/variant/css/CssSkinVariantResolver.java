/**
 * Copyright 2010 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.variant.css;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.generator.variant.VariantResolver;
import net.jawr.web.resource.bundle.generator.variant.VariantSet;

/**
 * The skin variant resolver is used to determine the current skin from the cookie
 * set in the request.
 *  
 * @author Ibrahim Chaehoi
 *
 */
public class CssSkinVariantResolver implements VariantResolver {

	/** the default skin */
	private final String defaultSkin;
	
	/** the cookie name for the skin */
	private final String skinCookieName;
	
	/**
	 * Constructor
	 * @param defaultSkin the default skin
	 * @param skinCookieName the cookie name for the skin
	 */
	public CssSkinVariantResolver(final String defaultSkin, final String skinCookieName) {
		
		this.defaultSkin = defaultSkin;
		this.skinCookieName = skinCookieName;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.variant.VariantResolver#getVariantType()
	 */
	public String getVariantType() {
		
		return JawrConstant.SKIN_VARIANT_TYPE;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.variant.VariantResolver#resolveVariant(javax.servlet.http.HttpServletRequest)
	 */
	public String resolveVariant(HttpServletRequest request) {
		
		Cookie[] cookies = request.getCookies();
		String skin = defaultSkin;
		if(cookies != null){
			int nbCookies = cookies.length;
			for (int i = 0; i < nbCookies; i++) {
				Cookie cookie = cookies[i];
				if(cookie.getName().equals(skinCookieName)){
					skin = cookie.getValue();
				}
			}
		}
		return skin;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.variant.VariantResolver#getAvailableVariant(java.lang.String, java.util.Collection)
	 */
	public String getAvailableVariant(String variant, VariantSet variantSet) {
		
		String result = null;
		if(variantSet.contains(variant)){
			result = variant;
		}
		return result;
	}

}
