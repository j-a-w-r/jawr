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
package net.java.jawr.web.taglib;

import net.java.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.java.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.java.jawr.web.resource.bundle.renderer.JavascriptHTMLBundleLinkRenderer;

/**
 * Implementation of AbstractResourceBundleTag used to render javascript bundles. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class JavascriptBundleTag extends AbstractResourceBundleTag {

	/* (non-Javadoc)
	 * @see net.java.jawr.web.taglib.AbstractResourceBundleTag#createRenderer()
	 */
	protected BundleRenderer createRenderer() {
		if(null == pageContext.getServletContext().getAttribute(ResourceBundlesHandler.JS_CONTEXT_ATTRIBUTE))
			throw new IllegalStateException("ResourceBundlesHandler not present in servlet context. Initialization of Jawr either failed or never occurred.");

		ResourceBundlesHandler rsHandler = (ResourceBundlesHandler) pageContext.getServletContext().getAttribute(ResourceBundlesHandler.JS_CONTEXT_ATTRIBUTE);
                return  new JavascriptHTMLBundleLinkRenderer(rsHandler);
	}

	private static final long serialVersionUID = 5087323727715427593L;

}
