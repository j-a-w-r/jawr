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
package net.jawr.web.resource.bundle.renderer;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;

/**
 * Abstract base class for implementations of a link renderer. 
 * 
 * @author Jordi Hernández Sellés
 */
public abstract class AbstractBundleLinkRenderer implements BundleRenderer {
    
    private ResourceBundlesHandler bundler;
    
    private boolean useRandomParam = true;
    
    /** Creates a new instance of AbstractBundleLinkRenderer
     * @param bundler ResourceBundlesHandler Handles resolving of paths. 
     */
    protected AbstractBundleLinkRenderer(ResourceBundlesHandler bundler, boolean useRandomParam) {
        this.bundler = bundler;
        this.useRandomParam = useRandomParam;
    }
    
    /**
     * Render a link or group of links to the specified resource, included global bundles if applies. 
     * @param requestedPath String Path that identifies a resource bundle id or one of its members. 
     * @param contextPath String The context path to prepend to the URL. 
     * @param includedBundles Set A set of names previously added. None of them will be written out as links to avoid duplication. 
     * @param useGzip boolean If true, the gzip prefix is added to the URLs so the link will point to the gzipped version. 
     * @param out Writer Writer to output the tags, typically a JSPWriter.
     * @throws java.io.IOException 
     */
    public void renderBundleLinks(  String requestedPath,
                                    String contextPath,
                                    final Set includedBundles, 
                                    boolean useGzip, 
                                    Writer out ) throws IOException {
        
    	boolean debugOn = bundler.getConfig().isDebugModeOn();
        String bundleId = bundler.resolveBundleForPath(requestedPath);
		
        // Retrieve the name or names of bundle(s) that belong to/with the requested path. 
        List resourceNames = bundler.getBundlePaths(bundleId);

        if( debugOn ) {
                addComment("Start adding members resolved by '" + requestedPath + "'. Bundle id is: '" + bundleId + "'",out);
        }

        // Add resources to the page as links. 
        for(Iterator it = resourceNames.iterator();it.hasNext();)
        {
                String resourceName = (String) it.next();

                // If the resource had been added before, it will not be included again. 
                if(includedBundles.add(resourceName)){
                		// In debug mode, all the resources are included separately and use a random parameter to avoid caching. 
                		// If useRandomParam is set to false, the links are created without the random parameter. 
                        if( debugOn && useRandomParam) {
                                int random = new Random().nextInt();
                                if(random < 0)
                                	random*=-1;
                                out.write(createBundleLink(resourceName + "?d=" + random,contextPath));
                        }
                        else if(useGzip)
                            out.write(createGzipBundleLink(resourceName,contextPath));
                        else 
                            out.write(createBundleLink(resourceName,contextPath));
                }
                else if( debugOn ) {
                        addComment("Skipping resource '" + resourceName +"' since it is already included in the page.",out);
                }
        }
        if( debugOn ) {
                addComment("Finished adding members resolved by " + requestedPath,out);
        }
    }
    
    
    
    /**
     * Adds an HTML comment to the output stream. 
     * @param commentText
     * @param out Writer
     * @throws IOException
     */
    protected final void addComment(String commentText, Writer out)  throws IOException {
    	StringBuffer sb = new StringBuffer("<!-- ");
            sb.append(commentText).append(" -->").append("\n");
            out.write(sb.toString());
    }
    
    /**
     * Creates a link to a bundle in the page, prepending the gzip prefix to its identifier. 
     * @param resourceName
     * @param contextPath
     * @return String
     */
    protected String createGzipBundleLink(String resourceName, String contextPath) {
        // remove '/' from start of name
        resourceName = resourceName.substring(1,resourceName.length());
        return createBundleLink(BundleRenderer.GZIP_PATH_PREFIX + resourceName,contextPath);
    }
    
    protected String createBundleLink(String bundleId, String contextPath) {
    	
    	String fullPath = PathNormalizer.joinPaths(bundler.getConfig().getServletMapping(), bundleId);
    	
    	// If context path is overriden..
    	if(null != bundler.getConfig().getContextPathOverride()) {    		
    		String override = bundler.getConfig().getContextPathOverride();
    		// Blank override, create url relative to path
    		if("".equals(override)) {
    			fullPath = fullPath.substring(1);
    		}
    		else fullPath = PathNormalizer.joinPaths(override,fullPath);
    	}    		
    	else
    		fullPath = PathNormalizer.joinPaths(contextPath,fullPath);
    	
    	return renderLink(fullPath);
    }
    
    /**
     * Creates a link to a bundle in the page, using its identifier. 
     * @param bundleId
     * @param contextPath
     * @return String
     */
    protected abstract String renderLink(String fullPath);

    /**
     * @return ResourceBundlesHandler The resources handler.
     */
    public ResourceBundlesHandler getBundler() {
        return bundler;
    }

    
}
