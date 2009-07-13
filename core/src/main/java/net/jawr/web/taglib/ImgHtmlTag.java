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
public class ImgHtmlTag extends AbstractImageTag {

	// -------------------------------------------------------------

	/**
	 * The serial version UID
	 */
	private static final long serialVersionUID = -6048102958207543073L;

	/**
	 * The image height.
	 */
	protected String height = null;

	/**
	 * The horizontal spacing around the image.
	 */
	protected String hspace = null;

	/**
	 * Server-side image map declaration.
	 */
	protected String ismap = null;

	/**
	 * Client-side image map declaration.
	 */
	protected String usemap = null;

	/**
	 * The vertical spacing around the image.
	 */
	protected String vspace = null;

	/**
	 * The image width.
	 */
	protected String width = null;

	// ----------------------------------------------------- Constructor

	public ImgHtmlTag() {

	}

	public String getHeight() {
		return (this.height);
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getHspace() {
		return (this.hspace);
	}

	public void setHspace(String hspace) {
		this.hspace = hspace;
	}

	public String getIsmap() {
		return (this.ismap);
	}

	public void setIsmap(String ismap) {
		this.ismap = ismap;
	}

	public String getUsemap() {
		return (this.usemap);
	}

	public void setUsemap(String usemap) {
		this.usemap = usemap;
	}

	public String getVspace() {
		return (this.vspace);
	}

	public void setVspace(String vspace) {
		this.vspace = vspace;
	}

	public String getWidth() {
		return (this.width);
	}

	public void setWidth(String width) {
		this.width = width;
	}

	// --------------------------------------------------------- Public Methods

	/**
	 * Render the beginning of the IMG tag.
	 * 
	 * @throws JspException if a JSP exception has occurred
	 */
	public int doStartTag() throws JspException {

		return super.doStartTag();
	}

	/**
	 * Render the IMG tag.
	 * 
	 * @throws JspException if a JSP exception has occurred
	 */
	public int doEndTag() throws JspException {
		// Generate the name definition or image element
		HttpServletResponse response = (HttpServletResponse) pageContext
				.getResponse();
		StringBuffer results = new StringBuffer("<img");

		prepareImageUrl(response, results);

		prepareAttribute(results, "name", getName());
		prepareAttribute(results, "height", getHeight());
		prepareAttribute(results, "width", getWidth());
		prepareAttribute(results, "align", getAlign());
		prepareAttribute(results, "border", getBorder());
		prepareAttribute(results, "hspace", getHspace());
		prepareAttribute(results, "vspace", getVspace());
		prepareAttribute(results, "ismap", getIsmap());
		prepareAttribute(results, "usemap", getUsemap());
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
	 * Release any acquired resources.
	 */
	public void release() {
		super.release();

		border = null;
		height = null;
		hspace = null;
		name = null;
		ismap = null;
		src = null;
		usemap = null;
		vspace = null;
		width = null;
	}

	// ------------------------------------------------------ Protected Methods

	/**
	 * Return the specified src URL, modified as necessary with optional request parameters.
	 * 
	 * @param url The URL to be modified (or null if this url will not be used)
	 * @throws JspException if an error occurs preparing the URL
	 */
	protected String url(String url) throws JspException {
		if (url == null) {
			return (url);
		}

		// Start with an unadorned URL as specified
		StringBuffer src = new StringBuffer(url);

		// Return the final result
		return (src.toString());
	}
}
