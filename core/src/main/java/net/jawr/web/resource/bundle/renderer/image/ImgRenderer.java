/**
 * Copyright 2009 Jordi Hernández
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
package net.jawr.web.resource.bundle.renderer.image;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * Renders an img or input type img tag in HTML. 
 * 
 * @author Jordi Hernandez
 *
 */
public class ImgRenderer {
	
	private static final String IMG_TAG_START = "<img ";
	private static final String INPUT_TAG_START = "<input type=\"image\" ";
	
	private String tagStart;
	
	/**
	 * @param isPlainImage If true, will render an IMG tag, otherwise an input type="image" is rendered instead
	 */
	public ImgRenderer(boolean isPlainImage) {
		this.tagStart = isPlainImage ? IMG_TAG_START : INPUT_TAG_START;
	}
	
	/**
	 * Render the actual tag
	 * @param imgSource Source of the image
	 * @param attributes Attributes for the tag. 
	 * @param writer Writer to render the HTML into. Will NOT be closed or flushed. 
	 * 
	 * @throws IOException
	 */
	public void renderImage(String imgSource, Map attributes, final Writer writer) throws IOException{
		StringBuffer sb = new StringBuffer(tagStart);
		sb.append("src=\"").append(imgSource).append("\" ");
		for(Iterator it = attributes.keySet().iterator();it.hasNext();) {
			String key = (String) it.next();
			sb.append(key)
				.append("=\"")
				.append(attributes.get(key))
				.append("\" ");
			
		}
		sb.append("/>");
		
		writer.write(sb.toString());
		
	}

}
