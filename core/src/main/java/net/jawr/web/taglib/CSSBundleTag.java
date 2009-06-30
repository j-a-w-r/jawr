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
package net.jawr.web.taglib;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.CSSHTMLBundleLinkRenderer;

/**
 * JSP taglib which uses a CSSHTMLBundleLinkRenderer to render links for CSS bundles. 
 * 
 * @author Jordi Hernández Sellés
 */
public class CSSBundleTag  extends AbstractResourceBundleTag {
    
        private String media;

	/* (non-Javadoc)
	 * @see net.jawr.web.taglib.AbstractResourceBundleTag#createRenderer()
	 */
	protected BundleRenderer createRenderer() {
		if(null == pageContext.getServletContext().getAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE))
			throw new IllegalStateException("ResourceBundlesHandler not present in servlet context. Initialization of Jawr either failed or never occurred.");

		ResourceBundlesHandler rsHandler = (ResourceBundlesHandler) pageContext.getServletContext().getAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE);
		return  new CSSHTMLBundleLinkRenderer(rsHandler, this.useRandomParam, this.media);
	}

	private static final long serialVersionUID = 5087323727715427592L;

    /**
     * Set the media type to use in the css tag
     * @param media 
     */
    public void setMedia(String media) {
        this.media = media;
    }

}