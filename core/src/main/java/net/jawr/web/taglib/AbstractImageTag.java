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
package net.jawr.web.taglib;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

/**
 * This tag defines the base class for HTML tags
 *  
 * @author Ibrahim Chaehoi
 *
 */
public class AbstractImageTag extends ImagePathTag {

	/** The serial version UID */
	private static final long serialVersionUID = 1085874354131806795L;

	/**
     * The property to specify where to align the image.
     */
    protected String align = null;

    /**
     * The border size around the image.
     */
    protected String border = null;

    /**
     * The image name for named images.
     */
    protected String name = null;
    
    // CSS Style Support

    /**
     * Style attribute associated with component.
     */
    private String style = null;

    /**
     * Named Style class associated with component.
     */
    private String styleClass = null;

    /**
     * Identifier associated with component.
     */
    private String styleId = null;

    /**
     * The alternate text of this element.
     */
    private String alt = null;

    /**
     * The advisory title of this element.
     */
    private String title = null;

    /**
     * The language code of this element.
     */
    private String lang = null;

    /**
     * The direction for weak/neutral text of this element.
     */
    private String dir = null;

    //  Mouse Events
    /**
     * Mouse click event.
     */
    private String onclick = null;

    /**
     * Mouse double click event.
     */
    private String ondblclick = null;

    /**
     * Mouse over component event.
     */
    private String onmouseover = null;

    /**
     * Mouse exit component event.
     */
    private String onmouseout = null;

    /**
     * Mouse moved over component event.
     */
    private String onmousemove = null;

    /**
     * Mouse pressed on component event.
     */
    private String onmousedown = null;

    /**
     * Mouse released on component event.
     */
    private String onmouseup = null;

    //  Keyboard Events

    /**
     * Key down in component event.
     */
    private String onkeydown = null;

    /**
     * Key released in component event.
     */
    private String onkeyup = null;

    /**
     * Key down and up together in component event.
     */
    private String onkeypress = null;
    
    /**
	 * @return the align
	 */
	public String getAlign() {
		return align;
	}

	/**
	 * @param align the align to set
	 */
	public void setAlign(String align) {
		this.align = align;
	}

	/**
	 * @return the border
	 */
	public String getBorder() {
		return border;
	}

	/**
	 * @param border the border to set
	 */
	public void setBorder(String border) {
		this.border = border;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the style
	 */
	public String getStyle() {
		return style;
	}

	/**
	 * @param style the style to set
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	/**
	 * @return the styleClass
	 */
	public String getStyleClass() {
		return styleClass;
	}

	/**
	 * @param styleClass the styleClass to set
	 */
	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	/**
	 * @return the styleId
	 */
	public String getStyleId() {
		return styleId;
	}

	/**
	 * @param styleId the styleId to set
	 */
	public void setStyleId(String styleId) {
		this.styleId = styleId;
	}

	/**
	 * @return the alt
	 */
	public String getAlt() {
		return alt;
	}

	/**
	 * @param alt the alt to set
	 */
	public void setAlt(String alt) {
		this.alt = alt;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the lang
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * @param lang the lang to set
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}

	/**
	 * @return the dir
	 */
	public String getDir() {
		return dir;
	}

	/**
	 * @param dir the dir to set
	 */
	public void setDir(String dir) {
		this.dir = dir;
	}

	/**
	 * @return the onclick
	 */
	public String getOnclick() {
		return onclick;
	}

	/**
	 * @param onclick the onclick to set
	 */
	public void setOnclick(String onclick) {
		this.onclick = onclick;
	}

	/**
	 * @return the ondblclick
	 */
	public String getOndblclick() {
		return ondblclick;
	}

	/**
	 * @param ondblclick the ondblclick to set
	 */
	public void setOndblclick(String ondblclick) {
		this.ondblclick = ondblclick;
	}

	/**
	 * @return the onmouseover
	 */
	public String getOnmouseover() {
		return onmouseover;
	}

	/**
	 * @param onmouseover the onmouseover to set
	 */
	public void setOnmouseover(String onmouseover) {
		this.onmouseover = onmouseover;
	}

	/**
	 * @return the onmouseout
	 */
	public String getOnmouseout() {
		return onmouseout;
	}

	/**
	 * @param onmouseout the onmouseout to set
	 */
	public void setOnmouseout(String onmouseout) {
		this.onmouseout = onmouseout;
	}

	/**
	 * @return the onmousemove
	 */
	public String getOnmousemove() {
		return onmousemove;
	}

	/**
	 * @param onmousemove the onmousemove to set
	 */
	public void setOnmousemove(String onmousemove) {
		this.onmousemove = onmousemove;
	}

	/**
	 * @return the onmousedown
	 */
	public String getOnmousedown() {
		return onmousedown;
	}

	/**
	 * @param onmousedown the onmousedown to set
	 */
	public void setOnmousedown(String onmousedown) {
		this.onmousedown = onmousedown;
	}

	/**
	 * @return the onmouseup
	 */
	public String getOnmouseup() {
		return onmouseup;
	}

	/**
	 * @param onmouseup the onmouseup to set
	 */
	public void setOnmouseup(String onmouseup) {
		this.onmouseup = onmouseup;
	}

	/**
	 * @return the onkeydown
	 */
	public String getOnkeydown() {
		return onkeydown;
	}

	/**
	 * @param onkeydown the onkeydown to set
	 */
	public void setOnkeydown(String onkeydown) {
		this.onkeydown = onkeydown;
	}

	/**
	 * @return the onkeyup
	 */
	public String getOnkeyup() {
		return onkeyup;
	}

	/**
	 * @param onkeyup the onkeyup to set
	 */
	public void setOnkeyup(String onkeyup) {
		this.onkeyup = onkeyup;
	}

	/**
	 * @return the onkeypress
	 */
	public String getOnkeypress() {
		return onkeypress;
	}

	/**
	 * @param onkeypress the onkeypress to set
	 */
	public void setOnkeypress(String onkeypress) {
		this.onkeypress = onkeypress;
	}

	/**
     * Prepares the style attributes for inclusion in the component's HTML
     * tag.
     *
     * @return The prepared String for inclusion in the HTML tag.
     * @throws JspException if invalid attributes are specified
     */
    protected String prepareStyles()
        throws JspException {
        StringBuffer styles = new StringBuffer();

        prepareAttribute(styles, "id", getStyleId());
        prepareAttribute(styles, "style", getStyle());
        prepareAttribute(styles, "class", getStyleClass());
        prepareAttribute(styles, "title", getTitle());
        prepareAttribute(styles, "alt", getAlt());
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
        prepareAttribute(handlers, "lang", getLang());
        prepareAttribute(handlers, "dir", getDir());
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
        prepareAttribute(handlers, "onclick", getOnclick());
        prepareAttribute(handlers, "ondblclick", getOndblclick());
        prepareAttribute(handlers, "onmouseover", getOnmouseover());
        prepareAttribute(handlers, "onmouseout", getOnmouseout());
        prepareAttribute(handlers, "onmousemove", getOnmousemove());
        prepareAttribute(handlers, "onmousedown", getOnmousedown());
        prepareAttribute(handlers, "onmouseup", getOnmouseup());
    }

    /**
     * Prepares the keyboard event handlers, appending them to the the given
     * StringBuffer.
     *
     * @param handlers The StringBuffer that output will be appended to.
     */
    protected void prepareKeyEvents(StringBuffer handlers) {
        prepareAttribute(handlers, "onkeydown", getOnkeydown());
        prepareAttribute(handlers, "onkeyup", getOnkeyup());
        prepareAttribute(handlers, "onkeypress", getOnkeypress());
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
	 * @param response the response
	 * @param results the result
     * @throws JspException if an exception occurs
	 */
	protected void prepareImageUrl(HttpServletResponse response, StringBuffer results) throws JspException {
		
		prepareAttribute(results, "src", getImageUrl(getSrc()));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#release()
	 */
	public void release() {
		super.release();
		
		align = null;
		border = null;
		name = null;
	    src = null;
	    style = null;
	    styleClass = null;
	    styleId = null;
	    alt = null;
	    title = null;
	    lang = null;
	    dir = null;
	    onclick = null;
	    ondblclick = null;
	    onmouseover = null;
	    onmouseout = null;
	    onmousemove = null;
	    onmousedown = null;
	    onmouseup = null;
	    onkeydown = null;
	    onkeyup = null;
	    onkeypress = null;
	}
	
	
}
