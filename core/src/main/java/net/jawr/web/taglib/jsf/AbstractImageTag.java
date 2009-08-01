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
package net.jawr.web.taglib.jsf;

import java.io.IOException;

import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.bundle.CheckSumUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;

import org.apache.log4j.Logger;

/**
 * Abstract implementation of a facelets taglib component which will display images.  
 * 
 * @author Ibrahim Chaehoi
 *
 */
public abstract class AbstractImageTag extends UIOutput {

	/** The logger */
	private static Logger logger = Logger.getLogger(AbstractImageTag.class);
	
	/* (non-Javadoc)
	 * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
	 */
	public void encodeBegin(FacesContext context) throws IOException {
		
		// Initialize attributes
		String src = (String)getAttributes().get("src"); 
        
        // src is mandatory
        if(null == src)
        	throw new IllegalStateException("The src attribute is mandatory for this Jawr tag. ");
        
        
        render(context);
        
 		super.encodeBegin(context);
	}
	
	/**
	 * Renders the image tag
	 * @param context the faces context
	 * @throws IOException if an IO exception occurs.
	 */
	protected abstract void render(FacesContext context) throws IOException ;

	/**
	 * Returns the attribute value associated to the name passed in parameter
	 * @param attributeName the attribute name
	 * @return the attribute value
	 */
	protected Object getAttribute(String attributeName){
	
		return getAttributes().get(attributeName);
	}
	
	/**
     * Prepares the style attributes for inclusion in the component's HTML
     * tag.
     *
     * @return The prepared String for inclusion in the HTML tag.
     * @throws JspException if invalid attributes are specified
     */
    protected String prepareStyles() {
        StringBuffer styles = new StringBuffer();

        prepareAttribute(styles, "id", getAttribute("styleId"));
        prepareAttribute(styles, "style", getAttribute("style"));
        prepareAttribute(styles, "class", getAttribute("styleClass"));
        prepareAttribute(styles, "title", getAttribute("title"));
        prepareAttribute(styles, "alt", getAttribute("alt"));
        prepareInternationalization(styles);

        return styles.toString();
    }

    /**
     * Prepares the internationalization attributes, appending them to the the given
     * StringBuffer.
     *
     * @param handlers The StringBuffer that output will be appended to.
     * @since Struts 1.3.6
     */
    protected void prepareInternationalization(StringBuffer handlers) {
        prepareAttribute(handlers, "lang", getAttribute("lang"));
        prepareAttribute(handlers, "dir", getAttribute("dir"));
    }

    /**
     * Prepares the event handlers for inclusion in the component's HTML tag.
     *
     * @return The prepared String for inclusion in the HTML tag.
     */
    protected String prepareEventHandlers() {
        StringBuffer handlers = new StringBuffer();

        prepareMouseEvents(handlers);
        prepareKeyEvents(handlers);
    
        return handlers.toString();
    }

    /**
     * Prepares the mouse event handlers, appending them to the the given
     * StringBuffer.
     *
     * @param handlers The StringBuffer that output will be appended to.
     */
    protected void prepareMouseEvents(StringBuffer handlers) {
        prepareAttribute(handlers, "onclick", getAttribute("onclick"));
        prepareAttribute(handlers, "ondblclick", getAttribute("ondblclick"));
        prepareAttribute(handlers, "onmouseover", getAttribute("onmouseover"));
        prepareAttribute(handlers, "onmouseout", getAttribute("onmouseout"));
        prepareAttribute(handlers, "onmousemove", getAttribute("onmousemove"));
        prepareAttribute(handlers, "onmousedown", getAttribute("onmousedown"));
        prepareAttribute(handlers, "onmouseup", getAttribute("onmouseup"));
    }

    /**
     * Prepares the keyboard event handlers, appending them to the the given
     * StringBuffer.
     *
     * @param handlers The StringBuffer that output will be appended to.
     */
    protected void prepareKeyEvents(StringBuffer handlers) {
        prepareAttribute(handlers, "onkeydown", getAttribute("onkeydown"));
        prepareAttribute(handlers, "onkeyup", getAttribute("onkeyup"));
        prepareAttribute(handlers, "onkeypress", getAttribute("onkeypress"));
    }

    /**
     * Prepares an attribute if the value is not null, appending it to the the
     * given StringBuffer.
     *
     * @param handlers The StringBuffer that output will be appended to.
     */
    protected void prepareAttribute(StringBuffer handlers, String name,
        Object value) {
        if (value != null) {
            handlers.append(" ");
            handlers.append(name);
            handlers.append("=\"");
            handlers.append(value);
            handlers.append("\"");
        }
    }
    
    /**
     * Prepares an attribute if the value is not null, appending it to the the
     * given StringBuffer.
     *
     * @param handlers The StringBuffer that output will be appended to.
     */
    protected void prepareAttribute(StringBuffer handlers, String name,
        boolean value) {
    
    	handlers.append(" ");
        handlers.append(name);
        handlers.append("=\"");
        handlers.append(value);
        handlers.append("\"");
    
    }

    /**
	 * Prepare the image URL
	 * @param context the faces context
	 * @param results the result
     * @throws IOException if an exception occurs
	 */
	protected void prepareImageUrl(FacesContext context, StringBuffer results) throws IOException {
		
		ImageResourcesHandler imgRsHandler = (ImageResourcesHandler) context.getExternalContext().getApplicationMap().get(JawrConstant.IMG_CONTEXT_ATTRIBUTE);
		if(imgRsHandler == null){
			throw new IllegalStateException("You are using a Jawr image tag while the Jawr Image servlet has not been initialized. Initialization of Jawr Image servlet either failed or never occurred.");
		}
	
		String imgSrc = (String) getAttribute("src");
		
		String newUrl = (String) imgRsHandler.getCacheUrl(imgSrc);
		
        if(newUrl == null){
        	try {
				newUrl = CheckSumUtils.getCacheBustedUrl(imgSrc, imgRsHandler.getRsHandler(), imgRsHandler.getJawrConfig());
				imgRsHandler.addMapping(imgSrc, newUrl);
	    	} catch (IOException e) {
	    		
	    		throw new IOException("An IOException occured while processing the image '"+imgSrc+"'."+ e.getMessage());
			}
    	}
        
		if(newUrl == null){
        	newUrl = imgSrc;
        	logger.debug("No mapping found for the image : "+imgSrc);
        }
        
        String imageServletMapping = imgRsHandler.getJawrConfig().getServletMapping();
		if("".equals(imageServletMapping)){
			if(newUrl.startsWith("/")){
				newUrl = newUrl.substring(1);
			}
		}else{
			newUrl = PathNormalizer.joinDomainToPath(imageServletMapping, newUrl);
		}
        
		HttpServletResponse response = ((HttpServletResponse)context.getExternalContext().getResponse());
        prepareAttribute(results, "src", response.encodeURL(newUrl));
	}

	
}
