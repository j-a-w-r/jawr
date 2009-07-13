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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

/**
 * This class defines the image tag.
 * 
 * This implementation is based on the Struts image tag.
 * 
 * @author Ibrahim Chaehoi
 */
public class ImageInputTag extends AbstractImageTag {

	// ------------------------------------------------------------- Properties

	/**
	 * The serial version UID
	 */
	private static final long serialVersionUID = -3608810516737758870L;

	/**
	 * Access key character.
	 */
	protected String accesskey = null;

	/**
	 * Tab index value.
	 */
	protected String tabindex = null;

	/**
	 * The input value value.
	 */
	protected String value = null;
	
	/**
	 * The disabled property.
	 */
	protected boolean disabled = false;
	
	/**
	 * The onfocus property.
	 */
	protected String onfocus = null;
	
	/**
	 * The onblur property.
	 */
	protected String onblur = null;
	
	/**
	 * The onchange property.
	 */
	protected String onchange = null;
	
	// --------------------------------------------------------- Constructor

	public ImageInputTag() {
		super();
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the accesskey
	 */
	public String getAccesskey() {
		return accesskey;
	}

	/**
	 * @param accesskey the accesskey to set
	 */
	public void setAccesskey(String accesskey) {
		this.accesskey = accesskey;
	}

	/**
	 * @return the tabindex
	 */
	public String getTabindex() {
		return tabindex;
	}

	/**
	 * @param tabindex the tabindex to set
	 */
	public void setTabindex(String tabindex) {
		this.tabindex = tabindex;
	}

	/**
	 * @return the disabled
	 */
	public boolean getDisabled() {
		return disabled;
	}

	/**
	 * @param disabled the disabled to set
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	/**
	 * @return the focus
	 */
	public String getOnfocus() {
		return onfocus;
	}

	/**
	 * @param focus the focus to set
	 */
	public void setOnfocus(String focus) {
		this.onfocus = focus;
	}

	/**
	 * @return the onblur
	 */
	public String getOnblur() {
		return onblur;
	}

	/**
	 * @param onblur the onblur to set
	 */
	public void setOnblur(String onblur) {
		this.onblur = onblur;
	}

	/**
	 * @return the onchange
	 */
	public String getOnchange() {
		return onchange;
	}

	/**
	 * @param onchange the onchange to set
	 */
	public void setOnchange(String onchange) {
		this.onchange = onchange;
	}

	/**
     * Prepares the keyboard event handlers, appending them to the the given
     * StringBuffer.
     *
     * @param handlers The StringBuffer that output will be appended to.
     */
    protected void prepareKeyEvents(StringBuffer handlers) {
        
    	super.prepareKeyEvents(handlers);
        prepareAttribute(handlers, "onchange", getOnchange());
        prepareAttribute(handlers, "onfocus", getOnfocus());
        prepareAttribute(handlers, "onblur", getOnblur());
    }
    
	/**
	 * Render the IMG tag.
	 * 
	 * @throws JspException if a JSP exception has occurred
	 */
	public int doEndTag() throws JspException {
		// Generate the name definition or image element
		HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
		StringBuffer results = new StringBuffer(getElementOpen());

		prepareImageUrl(response, results);

		prepareAttribute(results, "name", getName());
		prepareAttribute(results, "align", getAlign());
		prepareAttribute(results, "border", getBorder());
		prepareAttribute(results, "value", getValue());
		prepareAttribute(results, "accesskey", getAccesskey());
		prepareAttribute(results, "tabindex", getTabindex());
		prepareAttribute(results, "disabled", getDisabled());
		results.append(prepareStyles());
		results.append(prepareEventHandlers());
		results.append(" />");

		try {
			pageContext.getOut().write(results.toString());
		} catch (IOException e) {
			throw new JspException(e);
		}

		return (EVAL_PAGE);
	}

	/**
	 * Render the opening element.
	 * 
	 * @return The opening part of the element.
	 */
	protected String getElementOpen() {
		return "<input type=\"image\"";
	}

	/**
	 * Release any acquired resources.
	 */
	public void release() {
		super.release();
		accesskey = null;
		tabindex = null;
		value = null;
		onchange = null;
        onblur = null;
        onfocus = null;
        disabled = false;
	}

}
