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
package net.jawr.web.resource.bundle.handler;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Map;

import net.jawr.web.collections.ConcurrentCollectionsFactory;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;

/**
 * ResourceBundlesHandler wrapper implementation that uses a ConcurrentHashMap to cache the resources. 
 * Each resource is loaded only once from the ResourceBundlesHandler, then it is stored in cache and 
 * retrieved from there in subsequent calls. Every method call not related to retrieving data is delegated
 * to the wrapped implementation. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class CachedResourceBundlesHandler implements ResourceBundlesHandler {
	
	private ResourceBundlesHandler rsHandler;
	private Map textCache;
	private Map gzipCache;

	
	/**
	 * Build a cached wrapper around the supplied ResourceBundlesHandler. 
	 * @param rsHandler
	 */
	public CachedResourceBundlesHandler(ResourceBundlesHandler rsHandler) {
		super();
		this.rsHandler = rsHandler;
		this.textCache = ConcurrentCollectionsFactory.buildConcurrentHashMap();
		this.gzipCache = ConcurrentCollectionsFactory.buildConcurrentHashMap();
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.ResourceBundlesHandler#getBundlePaths(java.lang.String)
	 */
	public ResourceBundlePathsIterator getBundlePaths(String bundleId, ConditionalCommentCallbackHandler commentCallbackHandler, String variant) {
		return rsHandler.getBundlePaths(bundleId, commentCallbackHandler,variant);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.ResourceBundlesHandler#getConfig()
	 */
	public JawrConfig getConfig() {
		return rsHandler.getConfig();
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.ResourceBundlesHandler#initAllBundles()
	 */
	public void initAllBundles() {
		rsHandler.initAllBundles();
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.ResourceBundlesHandler#resolveBundleForPath(java.lang.String)
	 */
	public JoinableResourceBundle resolveBundleForPath(String path) {
		return rsHandler.resolveBundleForPath(path);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.ResourceBundlesHandler#streamBundleTo(java.lang.String, java.io.OutputStream)
	 */
	public void streamBundleTo(String bundlePath, OutputStream out)
			throws ResourceNotFoundException {
		
		try {
			ByteBuffer gzip = (ByteBuffer) gzipCache.get(bundlePath);
			
			// If it's not cached yet
			if(null == gzip) {				
				// Stream the stored data
				ByteArrayOutputStream baOs = new ByteArrayOutputStream();
				BufferedOutputStream bfOs = new BufferedOutputStream(baOs);
				rsHandler.streamBundleTo(bundlePath,bfOs);
				
				// Copy the data into the ByteBuffer
				bfOs.close();
				gzip = ByteBuffer.wrap(baOs.toByteArray());
				
				// Cache the ByteBuffer
				gzipCache.put(bundlePath,gzip);
			}
			
			// Write bytes to the outputstream
			int max = gzip.capacity();
			for(int x = 0; x < max;x++)
				out.write(gzip.get(x)); // Use absolute get method
		
		} catch (IOException e) {
			throw new RuntimeException("Unexpected IOException writing bundle[" + bundlePath + "]");
		}

	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.ResourceBundlesHandler#writeBundleTo(java.lang.String, java.io.Writer)
	 */
	public void writeBundleTo(String bundlePath, Writer writer)
			throws ResourceNotFoundException {
		String text = (String) textCache.get(bundlePath);
		try {
			// If it's not cached yet
			if(null == text) {
				String charsetName = rsHandler.getConfig().getResourceCharset().name();
				ByteArrayOutputStream baOs = new ByteArrayOutputStream();
			    WritableByteChannel wrChannel = Channels.newChannel(baOs);
			    Writer tempWriter = Channels.newWriter(wrChannel, charsetName);
			    rsHandler.writeBundleTo(bundlePath, tempWriter);
			    text = baOs.toString(charsetName);
			    textCache.put(bundlePath,text);
			}
			
			// Write the text to the outputstream
			writer.write(text);
			writer.close();
			
		} catch (IOException e) {
			throw new RuntimeException("Unexpected IOException writing bundle[" + bundlePath + "]");
		}
	}

}
