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
package net.java.jawr.web.resource.bundle.postprocess.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

import net.java.jawr.web.minification.JSMin;
import net.java.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.java.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;

/**
 * This postprocessor will minify a javascript bundle using Douglas Crockford's JSMin,
 * in its java implementation (see www.crockford.com and www.inconspicuous.org). 
 * 
 * @author Jordi Hernández Sellés
 */
public class JSMinPostProcessor extends
		AbstractChainedResourceBundlePostProcessor {
	

	/**
	 * Constructor for a compressor.  
	 * @param charset
	 */
	public JSMinPostProcessor() {
		super();
	}

	/* (non-Javadoc)
	 * @see net.java.jawr.web.resource.bundle.postprocess.impl.AbstractChainedResourceBundlePostProcessor#doPostProcessBundle(java.lang.StringBuffer)
	 */
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status,StringBuffer bundleString)
			throws IOException {
		Charset charset = status.getJeesConfig().getResourceCharset();
		ByteArrayInputStream bIs = new ByteArrayInputStream(bundleString.toString().getBytes(charset.name()));
		ByteArrayOutputStream bOs = new ByteArrayOutputStream();
		
		// Compress data and recover it as a byte array. 
		JSMin minifier = new JSMin(bIs,bOs);
		minifier.jsmin();
		byte[] minified = bOs.toByteArray();

		// Write the data into a string
		ReadableByteChannel chan = Channels.newChannel(new ByteArrayInputStream(minified));
        Reader rd = Channels.newReader(chan,charset.newDecoder(),-1);
        StringWriter writer = new StringWriter();
		int i;
		while((i = rd.read()) != -1)
			writer.write(i);
		
		return writer.getBuffer();
	}

}
