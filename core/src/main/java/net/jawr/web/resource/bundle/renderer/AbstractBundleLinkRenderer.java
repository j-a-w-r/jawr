/**
 * Copyright 2007-2009 Jordi Hernández Sellés, Matt Ruby, Ibrahim Chaehoi
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
import java.util.Random;

import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.dwr.DWRParamWriter;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;
import net.jawr.web.servlet.RendererRequestUtils;

/**
 * Abstract base class for implementations of a link renderer.
 * 
 * @author Jordi Hernández Sellés
 * @author Matt Ruby
 * @author Ibrahim Chaehoi
 */
public abstract class AbstractBundleLinkRenderer implements BundleRenderer {

	/** The ID of the DWR script path */
	private static final String ID_SCRIPT_DWR_PATH = "__dwr_path__";

	/** The resource bundles handler */
	protected ResourceBundlesHandler bundler;

	/** The flag indicating if we must use the random parameter */
	private boolean useRandomParam = true;

	
	/**
	 * Creates a new instance of AbstractBundleLinkRenderer
	 * 
	 * @param bundler ResourceBundlesHandler Handles resolving of paths.
	 */
	protected AbstractBundleLinkRenderer(ResourceBundlesHandler bundler, boolean useRandomParam) {
		this.bundler = bundler;
		this.useRandomParam = useRandomParam;
	}

	/**
	 * Returns the resource bundles handler
	 * @return the resource bundles handler
	 */
	public ResourceBundlesHandler getBundler() {
		return bundler;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.renderer.BundleRenderer#renderBundleLinks(java.lang.String, net.jawr.web.resource.bundle.renderer.BundleRendererContext, javax.servlet.jsp.Writer)
	 */
	public void renderBundleLinks(String requestedPath, BundleRendererContext ctx,
			Writer out) throws IOException {
		
		boolean debugOn = bundler.getConfig().isDebugModeOn();
		JoinableResourceBundle bundle = bundler.resolveBundleForPath(requestedPath);

		if (null == bundle)
			return;

		// If the global bundles had been added before, it will not be included again.
		if(!ctx.isGlobalBundleAdded()){
			
			if (debugOn) {
				addComment("Start adding global members.", out);
			}
			
			ResourceBundlePathsIterator resourceBundleIterator = bundler.getGlobalResourceBundlePaths(new ConditionalCommentRenderer(out), ctx.getVariantKey());

			renderBundleLinks(resourceBundleIterator,
					ctx, debugOn, out);
			ctx.setGlobalBundleAdded(true);
			if (debugOn) {
				addComment("Finished adding global members.", out);
			}

		}
		
		// If there is a fixed URL for production mode it is rendered and method returns.  
    	if(!debugOn && null != bundle.getAlternateProductionURL()){
    		if(ctx.getIncludedBundles().add(bundle.getId()))
    			out.write(renderLink(bundle.getAlternateProductionURL()));
    		return;
    	}
    	
        if (debugOn) {
			addComment("Start adding members resolved by '" + requestedPath + "'. Bundle id is: '" + bundle.getId() + "'", out);
		}

		// If DWR is being used, add a path var to the page
		if (null != bundler.getConfig().getDwrMapping() && ctx.getIncludedBundles().add(ID_SCRIPT_DWR_PATH)) {

			String contextPath = ctx.getContextPath();
			StringBuffer sb = DWRParamWriter.buildRequestSpecificParams(contextPath, PathNormalizer.joinPaths(contextPath, bundler.getConfig()
					.getDwrMapping()));
			out.write(sb.toString());
		}

		// Include the bundle if it has not been included yet
		if(ctx.getIncludedBundles().add(bundle.getId())){
			// Retrieve the name or names of bundle(s) that belong to/with the requested path.
			ResourceBundlePathsIterator it = bundler.getBundlePaths(bundle.getId(), new ConditionalCommentRenderer(out), ctx.getVariantKey());
			renderBundleLinks(it, ctx, debugOn, out);
		}else{
			if (debugOn) {
				addComment("The bundle '" + bundle.getId() + "' is already included in the page.", out);
			}
		}
		if (debugOn) {
			addComment("Finished adding members resolved by " + requestedPath, out);
		}
	}

	

	/**
	 * Renders the bundle links for the resource iterator passed in parameter
	 * @param it the iterator on the bundles 
	 * @param contextPath the context path
	 * @param includedBundles the included bundles
	 * @param useGzip the flag indicating if we use gzip or not
	 * @param isSslRequest the flag indicating if it's an SSL request or not
	 * @param debugOn the flag indicating if we are in debug mode or not
	 * @param out the output writer
	 * @throws IOException if an IO exception occurs
	 */
	private void renderBundleLinks(ResourceBundlePathsIterator it,
			BundleRendererContext ctx, boolean debugOn, Writer out) throws IOException {
	
		String contextPath = ctx.getContextPath();
		boolean useGzip = ctx.isUseGzip();
		boolean isSslRequest = ctx.isSslRequest();
		
		// Add resources to the page as links.
		while (it.hasNext()) {
			String resourceName = it.nextPath();

			if( resourceName != null){
				// In debug mode, all the resources are included separately and use a random parameter to avoid caching.
				// If useRandomParam is set to false, the links are created without the random parameter.
				if (debugOn && useRandomParam && !bundler.getConfig().getGeneratorRegistry().isPathGenerated(resourceName)) {
					int random = new Random().nextInt();
					if (random < 0)
						random *= -1;
					out.write(createBundleLink(resourceName + "?d=" + random, contextPath, isSslRequest));
				} else if (!debugOn && useGzip){
					out.write(createGzipBundleLink(resourceName, contextPath, isSslRequest));
				}else{
					out.write(createBundleLink(resourceName, contextPath, isSslRequest));
				}
				if(!ctx.getIncludedResources().add(resourceName)){
					addComment("The resource '" + resourceName + "' is already included in the page.", out);
				}
			}
		}
	}

	/**
	 * Adds an HTML comment to the output stream.
	 * 
	 * @param commentText the comment
	 * @param out Writer
	 * @throws IOException if an IO exception occurs
	 */
	protected final void addComment(String commentText, Writer out) throws IOException {
		StringBuffer sb = new StringBuffer("<script type=\"text/javascript\">/* ");
		sb.append(commentText).append(" */</script>").append("\n");
		out.write(sb.toString());
	}

	/**
	 * Creates a link to a bundle in the page, prepending the gzip prefix to its identifier.
	 * 
	 * @param resourceName the resource name
	 * @param contextPath the context path
	 * @return the link to the gzip bundle in the page
	 */
	protected String createGzipBundleLink(String resourceName, String contextPath, boolean isSslRequest) {
		// remove '/' from start of name
		resourceName = resourceName.substring(1, resourceName.length());
		return createBundleLink(BundleRenderer.GZIP_PATH_PREFIX + resourceName, contextPath, isSslRequest);
	}

	/**
	 * Creates a link to a bundle in the page.
	 * 
	 * @param bundleId the bundle ID
	 * @param contextPath the context path
	 * @return the link to a bundle in the page
	 */
	protected String createBundleLink(String bundleId, String contextPath, boolean isSslRequest) {

		// When debug mode is on and the resource is generated the path must include a parameter
		if (bundler.getConfig().isDebugModeOn() && bundler.getConfig().getGeneratorRegistry().isPathGenerated(bundleId)) {
			bundleId = PathNormalizer.createGenerationPath(bundleId, bundler.getConfig().getGeneratorRegistry());
		}
		String fullPath = PathNormalizer.joinPaths(bundler.getConfig().getServletMapping(), bundleId);

		fullPath = RendererRequestUtils.getRenderedUrl(fullPath, bundler.getConfig(), contextPath, isSslRequest);
		
		// allow debugOverride to pass through on the generated urls
		if (ThreadLocalJawrContext.isDebugOverriden()) {
			fullPath = PathNormalizer.addGetParameter(fullPath, "overrideKey", bundler.getConfig().getDebugOverrideKey());
		}

		return renderLink(fullPath);
	}

	/**
	 * Creates a link to a bundle in the page, using its identifier.
	 * 
	 * @param fullPath the full path
	 * @return a link to a bundle in the page
	 */
	protected abstract String renderLink(String fullPath);

}
