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
package net.jawr.web.taglib.jsf;

import javax.faces.context.FacesContext;

import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.CSSHTMLBundleLinkRenderer;

/**
 * Facelets taglib which uses a CSSHTMLBundleLinkRenderer to render links for CSS bundles. 
 * 
 * @author Jordi Hernández Sellés
 */
public class CSSBundleTag extends AbstractResourceBundleTag {

	/* (non-Javadoc)
	 * @see net.jawr.web.taglib.jsf.AbstractResourceBundleTag#createRenderer(javax.faces.context.FacesContext)
	 */
	protected BundleRenderer createRenderer(FacesContext context) {
		Object handler = context.getExternalContext().getApplicationMap().get(ResourceBundlesHandler.CSS_CONTEXT_ATTRIBUTE);
		if(null == handler)
			throw new IllegalStateException("ResourceBundlesHandler not present in servlet context. Initialization of Jawr either failed or never occurred.");

		ResourceBundlesHandler rsHandler = (ResourceBundlesHandler) handler;
		String media = (String)getAttributes().get("media"); 
		
        return  new CSSHTMLBundleLinkRenderer(rsHandler, this.useRandomParam, media);
	}

}
