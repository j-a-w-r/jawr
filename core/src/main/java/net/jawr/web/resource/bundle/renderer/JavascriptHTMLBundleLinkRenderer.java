/**
 * Copyright 2007-2009 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.renderer;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;

/**
 * Renderer that creates javascript link tags. 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class JavascriptHTMLBundleLinkRenderer extends AbstractBundleLinkRenderer implements BundleRenderer{
    
	/** The start tag */
    private static final String PRE_TAG = "<script type=\"text/javascript\" src=\"";
    
    /** The end tag */
    private static final String POST_TAG = "\" ></script>\n";
    
    /** Creates a new instance of JavascriptHTMLBundleLinkRenderer */
    public JavascriptHTMLBundleLinkRenderer(ResourceBundlesHandler bundler, boolean useRandomParam) {
        super(bundler, useRandomParam);
    }

    /* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.renderer.BundleRenderer#getResourceType()
	 */
	public String getResourceType() {
		return JawrConstant.JS_TYPE;
	}
	
    /* (non-Javadoc)
     * @see net.jawr.web.resource.bundle.renderer.AbstractBundleLinkRenderer#createBundleLink(java.lang.String, java.lang.String)
     */
    protected String renderLink(String fullPath) {
    	StringBuffer sb = new StringBuffer(PRE_TAG);
    	sb.append(fullPath)
			.append(POST_TAG); 
        return sb.toString();
    }
    
}
