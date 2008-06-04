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

import java.io.OutputStream;
import java.io.Writer;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;

/**
 * Main interface to work with resource bundles. It helps in resolving groups of resources
 * wich are served as a single one, and provides methods to generate urls that point to either
 * the full bundle or its individual resources. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public interface ResourceBundlesHandler {
	
	public static final String JS_CONTEXT_ATTRIBUTE  = "net.jawr.web.resource.bundle.JS_CONTEXT_ATTRIBUTE";
	public static final String CSS_CONTEXT_ATTRIBUTE = "net.jawr.web.resource.bundle.CSS_CONTEXT_ATTRIBUTE";
	
	/**
	 * Determines which bundle corresponds to a path. The path may be
	 * a directory or file path. This path will not include any prefix, it is intended
	 * to be the path normally used for a tag library. 
	 * @param path
	 * @return String The bundle ID that can be used to retrieve it.  
	 */
	public JoinableResourceBundle resolveBundleForPath(String path);
	
	/**
	 * Returns an ordered list of the paths to use when accesing a resource bundle. 
	 * Each implementation may return one or several paths depending on wether all resources
	 * are unified into one or several bundles. The paths returned should include the prefix
	 * that uniquely identify the bundle contents. 
	 * 
	 * @param bundleId
	 * @return
	 */
	public ResourceBundlePathsIterator getBundlePaths(String bundleId, 
														ConditionalCommentCallbackHandler commentCallbackHandler, 
														String variantKey);
	
	/**
	 * Writes data using the supplied writer, representing a unified bundle of resources. 
	 * @param bundlePath
	 * @return
	 */
	public void writeBundleTo(String bundlePath, Writer writer) throws ResourceNotFoundException;
	
	/**
	 * Writes the bytes of a bundle to the specified OutputStream. The outputstream is returned without closing or flushing. 
	 * @param bundlePath
	 * @param out
	 */
	public void streamBundleTo(String bundlePath, OutputStream out) throws ResourceNotFoundException;
	
	
	/**
	 * Generates all file bundles so that they will be ready to attend requests. 
	 */
	public void initAllBundles();
	
	/**
	 * Retrieves the configuration for this bundler
	 * @return
	 */
	public JawrConfig getConfig();
	
	
}
