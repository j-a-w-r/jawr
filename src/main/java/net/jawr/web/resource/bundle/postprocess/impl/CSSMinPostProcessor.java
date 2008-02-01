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
package net.java.jawr.web.resource.bundle.postprocess.impl;

import java.io.IOException;

import net.java.jawr.web.minification.CSSMinifier;
import net.java.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.java.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;

/**
 * Preforms minification on CSS files by removing newlines, expendable whitespace and comments. 
 * 
 * @author Jordi Hernández Sellés
 */
public class CSSMinPostProcessor extends AbstractChainedResourceBundlePostProcessor {
	
	private CSSMinifier minifier;
	
	public CSSMinPostProcessor() {
		this.minifier = new CSSMinifier();
	}
	
	/* (non-Javadoc)
	 * @see net.java.jawr.web.resource.bundle.postprocess.impl.AbstractChainedResourceBundlePostProcessor#doPostProcessBundle(net.java.jawr.web.resource.bundle.postprocess.BundleProcessingStatus, java.lang.StringBuffer)
	 */
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status, StringBuffer bundleData) throws IOException {
		return minifier.minifyCSS(bundleData);
	}

}
