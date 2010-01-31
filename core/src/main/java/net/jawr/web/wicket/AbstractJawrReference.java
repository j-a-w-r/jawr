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
package net.jawr.web.wicket;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.BundleRendererContext;
import net.jawr.web.servlet.RendererRequestUtils;

import org.apache.log4j.Logger;
import org.apache.wicket.Response;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.util.value.IValueMap;

/**
 * This class defines the abstract class for the Jawr CSS and Stylesheet references 
 * for Wicket.
 * 
 * @autor Robert Kopaczewski (Original author) 
 * @author Ibrahim Chaehoi
 */
public abstract class AbstractJawrReference extends WebMarkupContainer {

	/** The logger */
	private static final Logger LOGGER = Logger.getLogger(AbstractJawrReference.class);

	/** The serial version UID */
	private static final long serialVersionUID = 6483803210055728200L;
	
	/** The bundle renderer */
	protected BundleRenderer renderer;
	
	/** The flag indicating if we must use the random parameter */
    protected boolean useRandomParam = true;
    
    /**
     * Constructor
     * @param id the ID
     */
    public AbstractJawrReference(String id) {
        super(id);
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.MarkupContainer#onRender(org.apache.wicket.markup.MarkupStream)
     */
    protected void onRender(MarkupStream markupStream) {
        try {
            final ComponentTag openTag = markupStream.getTag();
            final ComponentTag tag = openTag.mutable();
            final IValueMap attributes = tag.getAttributes();

            // Initialize attributes
            String useRandom = (String) attributes.get("useRandomParam");
            if (null != useRandom) {
                this.useRandomParam = Boolean.valueOf(useRandom).booleanValue();
            }

            String src = getReferencePath(attributes);
            
            // src is mandatory
            if (null == src) {
                throw new IllegalStateException("The src attribute is mandatory for this Jawr reference tag. ");
            }

            // Get an instance of the renderer.
            if (null == this.renderer || !this.renderer.getBundler().getConfig().isValid()) {
                this.renderer = createRenderer(tag);
            }

            ServletWebRequest servletWebRequest = (ServletWebRequest) getRequest();
            HttpServletRequest request = servletWebRequest.getHttpServletRequest();

            // set the debug override
 	       	RendererRequestUtils.setRequestDebuggable(request,renderer.getBundler().getConfig());
 		   
            final Response response = getResponse();
            Writer writer = new RedirectWriter(response);
            BundleRendererContext ctx = RendererRequestUtils.getBundleRendererContext(request, renderer);
			   
            renderer.renderBundleLinks(src,
            		ctx,
                    writer);
        } catch (IOException ex) {
            LOGGER.error("onRender() error : ", ex);
        }

        markupStream.skipComponent();
    }

    protected abstract String getReferencePath(final IValueMap attributes);

    /**
     * Create the tag renderer.
     * @param tag the tag
     * @return the tag renderer.
     */
    protected abstract BundleRenderer createRenderer(ComponentTag tag);
    
    
}
