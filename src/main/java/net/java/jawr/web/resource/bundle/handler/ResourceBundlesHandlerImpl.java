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
package net.java.jawr.web.resource.bundle.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.java.jawr.web.collections.ConcurrentCollectionsFactory;
import net.java.jawr.web.config.JawrConfig;
import net.java.jawr.web.exception.ResourceNotFoundException;
import net.java.jawr.web.resource.ResourceHandler;
import net.java.jawr.web.resource.bundle.CompositeResourceBundle;
import net.java.jawr.web.resource.bundle.JoinableResourceBundle;
import net.java.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.java.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.java.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.java.jawr.web.resource.bundle.sorting.GlobalResourceBundleComparator;

import org.apache.log4j.Logger;

/**
 * Default implementation of ResourceBundlesHandler
 * 
 * @author Jordi Hernández Sellés
 */
public class ResourceBundlesHandlerImpl implements ResourceBundlesHandler {
	
	private static final Logger log = Logger.getLogger(ResourceBundlesHandler.class);
	
	/**
	 * The bundles that this collector manages. 
	 */
	private List bundles;
	
	/**
	 * Global bundles, to include in every page
	 */
	private List globalBundles;
	
	/**
	 * Bundles to include upon request
	 */
	private List contextBundles;

	private ResourceHandler resourceHandler;
	private JawrConfig config;
	
	private ResourceBundlePostProcessor postProcessor;
	private ResourceBundlePostProcessor unitaryPostProcessor;
	
	/**
	 * Build a ResourceBundlesHandler. 
	 * @param bundles List The JoinableResourceBundles to use for this handler. 
	 * @param resourceHandler The file system access handler. 
	 * @param config Configuration for this handler. 
	 */
	public ResourceBundlesHandlerImpl(	List bundles,
										ResourceHandler resourceHandler, 
										JawrConfig config) {
		this(bundles,resourceHandler,config,null,null);
	}

	
	/**
	 * Build a ResourceBundlesHandler which will use the specified postprocessor.  
	 * @param bundles List The JoinableResourceBundles to use for this handler. 
	 * @param resourceHandler The file system access handler. 
	 * @param config Configuration for this handler. 
	 * @param postProcessor
	 */
	public ResourceBundlesHandlerImpl(	List bundles,
									ResourceHandler resourceHandler, 
									JawrConfig config,
									ResourceBundlePostProcessor postProcessor,
									ResourceBundlePostProcessor unitaryPostProcessor) {
		super();
		this.resourceHandler = resourceHandler;
		this.config = config;
		this.postProcessor = postProcessor;
		this.unitaryPostProcessor = unitaryPostProcessor;
		this.bundles = ConcurrentCollectionsFactory.buildCopyOnWriteArrayList();
		this.bundles.addAll(bundles);
		splitBundlesByType(bundles);
	}
	
	/**
	 * Splits the bundles in two lists, one for global lists and other for
	 * the remaining bundles.  
	 */
	private void splitBundlesByType(List bundles)
	{
		// Temporary lists (CopyOnWriteArrayList does not support sort())
		List tmpGlobal = new ArrayList();
		List tmpContext = new ArrayList();
		
		for(Iterator it = bundles.iterator();it.hasNext();)	{
			JoinableResourceBundle bundle = (JoinableResourceBundle) it.next();
			
			// Exclude/include debug only scripts
			if(config.isDebugModeOn() && bundle.getInclusionPattern().isExcludeOnDebug())
				continue;
			else if(!config.isDebugModeOn() && bundle.getInclusionPattern().isIncludeOnDebug())
				continue;
			
			if(bundle.getInclusionPattern().isGlobal())
				tmpGlobal.add(bundle);
			else
				tmpContext.add(bundle);
		}
		
		// Sort the global bundles
		Collections.sort(tmpGlobal, new GlobalResourceBundleComparator());
		
		globalBundles = ConcurrentCollectionsFactory.buildCopyOnWriteArrayList();
		globalBundles.addAll(tmpGlobal);
		
		contextBundles = ConcurrentCollectionsFactory.buildCopyOnWriteArrayList();
		contextBundles.addAll(tmpContext);
	}

	/* (non-Javadoc)
	 * @see net.java.jawr.web.resource.bundle.ResourceCollector#getBundlePaths(java.lang.String)
	 */
	public List getBundlePaths(String bundleId) {
		List paths = new ArrayList();
		boolean returnAfterGlobals = false;
		for(Iterator it = globalBundles.iterator();it.hasNext();)	{
			JoinableResourceBundle bundle = (JoinableResourceBundle) it.next();
			
			// Add separate files or joined bundle file according to debug mode. 
			if(getConfig().isDebugModeOn())
				paths.addAll(bundle.getItemPathList());
			else paths.add(PathNormalizer.joinPaths(bundle.getURLPrefix(),bundle.getName()));
			
			// If the bundle requested was this, return 
			if(bundle.getName().equals(bundleId))
				returnAfterGlobals = true;
		}
		if(!returnAfterGlobals)	{
			for(Iterator it = contextBundles.iterator();it.hasNext();)	{
				JoinableResourceBundle bundle = (JoinableResourceBundle) it.next();
				if(bundle.getName().equals(bundleId)){
					
					// Add separate files or joined bundle file according to debug mode. 
					if(getConfig().isDebugModeOn())
						paths.addAll(bundle.getItemPathList());
					else paths.add(PathNormalizer.joinPaths(bundle.getURLPrefix(),bundle.getName()));
					
					break;
				}		
			}
		}
		return paths;
	}
	
	
	
	/**
	 * Removes the URL preffix defined in the configuration from a path. 
	 * @param path
	 * @return
	 */
	private String removePreffixFromPath(String path) {
		// Remove first slash
        path = path.substring(1,path.length());
        // Remove up to second slash
        path = path.substring(path.indexOf("/"),path.length());
		return path;
	}
	
	/* (non-Javadoc)
	 * @see net.java.jawr.web.resource.bundle.ResourceCollector#writeBundleTo(java.lang.String, java.io.Writer)
	 */
	public void writeBundleTo(String bundlePath, Writer writer)  throws ResourceNotFoundException{
		Reader rd;
		
		// Prefixes are used only in production mode
		if(!this.config.isDebugModeOn())
			bundlePath = removePreffixFromPath(bundlePath);
		
		// If debug mode is on, resources are retrieved one by one. 
		if(config.isDebugModeOn())
			rd = resourceHandler.getResource(bundlePath);
		else rd = resourceHandler.getResourceBundleReader(bundlePath);
		
		int readChar;
	    try {
			while( (readChar = rd.read()) != -1 ){
				writer.write(readChar);
			}
			rd.close();
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Unexpected IOException writing bundle[" + bundlePath + "]");
		}
	}

	/* (non-Javadoc)
	 * @see net.java.jawr.web.resource.bundle.ResourceBundlesHandler#streamBundleTo(java.lang.String, java.io.OutputStream)
	 */
	public void streamBundleTo(String bundlePath, OutputStream out)  throws ResourceNotFoundException{
		bundlePath = removePreffixFromPath(bundlePath);
		FileChannel data = resourceHandler.getResourceBundleChannel(bundlePath);
		try {
			WritableByteChannel channel = Channels.newChannel(out);
			data.transferTo(0, data.size(), channel);
			data.close();
			channel.close();
		} catch (IOException e) {
			throw new RuntimeException("Unexpected IOException writing bundle [" + bundlePath + "]");
		}
	}

	/* (non-Javadoc)
	 * @see net.java.jawr.web.resource.bundle.ResourceCollector#getConfig()
	 */
	public JawrConfig getConfig() {
		return config;
	}

	/* (non-Javadoc)
	 * @see net.java.jawr.web.resource.bundle.ResourceCollector#initAllBundles()
	 */
	public void initAllBundles() {
		
		// Run through every bundle		
		for(Iterator itCol = bundles.iterator();itCol.hasNext();){
			JoinableResourceBundle bundle = (JoinableResourceBundle) itCol.next();
			StringBuffer store = null;
			
			// If this is a composite bundle create each independent bundle
			if(bundle instanceof CompositeResourceBundle) {
				CompositeResourceBundle composite = (CompositeResourceBundle) bundle;
				store = new StringBuffer();
				for(Iterator it = composite.getChildBundles().iterator();it.hasNext();) {
					JoinableResourceBundle childbundle = (JoinableResourceBundle) it.next();
					store.append(joinandPostprocessBundle(childbundle));					
				}
			}
			else store = joinandPostprocessBundle(bundle);	
			
			// Store the collected resources as a single file, both in text and gzip formats. 
			resourceHandler.storeBundle(bundle.getName(),store);
		}
	}


	/**
	 * Reads all the members of a bundle and executes all associated posprocessors. 
	 * @param bundle JoinableResourceBundle
	 * @return
	 */
	private StringBuffer joinandPostprocessBundle(JoinableResourceBundle bundle) {
		
		// Don't bother with the bundle if it is excluded because of the inclusion pattern
		if( (bundle.getInclusionPattern().isExcludeOnDebug() && config.isDebugModeOn()) ||
			(bundle.getInclusionPattern().isIncludeOnDebug() && !config.isDebugModeOn()) )
			return new StringBuffer();
		
		StringBuffer bundleData = new StringBuffer();
		StringBuffer store = null;
		
		BundleProcessingStatus status = new BundleProcessingStatus(bundle,resourceHandler,config);
		
		try {
			// Run through all the files belonging to the bundle
			for(Iterator it = bundle.getItemPathList().iterator();it.hasNext();){

				// File is first created in memory using a stringwriter. 
				StringWriter writer = new StringWriter(); 
				BufferedWriter bwriter = new BufferedWriter(writer);
				
				String path = (String) it.next();
				if(log.isDebugEnabled())
					log.debug("Adding file [" + path + "] to bundle " + bundle.getName());
				
				// Get a reader on the resource, with appropiate encoding
				Reader rd;
				try {
					rd = resourceHandler.getResource(path);
				} 
				catch (ResourceNotFoundException e) {
					// If a mapped file does not exist, a warning is issued and process continues normally. 
					log.warn("A mapped resource was not found: [" + path +"]. Please check your configuration");
					continue;
				}
				
				// Update the status. 
				status.setLastPathAdded(path);
				
				// Make a buffered reader, to read line by line. 
				BufferedReader bRd = new BufferedReader(rd);
				String line;
				
				// Write each line and the corresponding new line. 
		    	while( (line = bRd.readLine()) != null) {
		    		bwriter.write(line);
		    		bwriter.newLine();
		    	}
		    	bRd.close();			
				bwriter.close();		
				
				// Do unitary postprocessing. 
				if(null != bundle.getUnitaryPostProcessor()) {
					StringBuffer resourceData = bundle.getUnitaryPostProcessor().postProcessBundle(status, writer.getBuffer());
					bundleData.append(resourceData);
				}
				else if(null != this.unitaryPostProcessor) {
					if(log.isDebugEnabled())
						log.debug("POSTPROCESSING UNIT:" +  status.getLastPathAdded()); 
					StringBuffer resourceData = this.unitaryPostProcessor.postProcessBundle(status, writer.getBuffer());
					bundleData.append(resourceData);
				}
				else bundleData.append(writer.getBuffer());
			}
			
			
			// Post process bundle as needed
			if(null != bundle.getBundlePostProcessor())
				store = bundle.getBundlePostProcessor().postProcessBundle(status, bundleData);
			else if(null != this.postProcessor)
				store = this.postProcessor.postProcessBundle(status, bundleData);
			else store = bundleData;
			
		} catch (IOException e) {
			throw new RuntimeException("Unexpected IOException generating collected file [" + bundle.getName() + "].");
		}
		return store;
	}

	/* (non-Javadoc)
	 * @see net.java.jawr.web.resource.bundle.ResourceCollector#resolveBundleForPath(java.lang.String)
	 */
	public String resolveBundleForPath(String path) {
		
		String bundleId = null;
		for(Iterator it = bundles.iterator();it.hasNext();)	{
			JoinableResourceBundle bundle = (JoinableResourceBundle) it.next();
			if(bundle.getName().equals(path) || bundle.belongsToBundle(path)) {
				bundleId = bundle.getName();
				break;
			}
		}
		return bundleId;
	}

}
