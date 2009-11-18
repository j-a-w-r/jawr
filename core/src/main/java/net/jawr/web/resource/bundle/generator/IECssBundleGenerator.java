/**
 * 
 */
package net.jawr.web.resource.bundle.generator;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.iterator.ListPathsIteratorImpl;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.CSSURLPathRewriterPostProcessor;

import org.apache.log4j.Logger;

/**
 * This class defines a generator which will bundle all the CSS defines in
 * parameter. To use it you need to define your mapping like :
 * 
 * jawr.css.bundle.myBundle.id=/my-ie-bundle.css
 * jawr.css.bundle.myBundle.mappings=ieCssGen:/my-ie-bundle.css
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class IECssBundleGenerator extends AbstractCSSGenerator {

	private static final Logger log = Logger
			.getLogger(IECssBundleGenerator.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource
	 * (net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public Reader createResource(GeneratorContext context) {

		StringBuffer result = new StringBuffer();

		ResourceBundlesHandler bundlesHandler = (ResourceBundlesHandler) context
				.getServletContext().getAttribute(
						JawrConstant.CSS_CONTEXT_ATTRIBUTE);
		String variantKey = null;
		String bundlePath = PathNormalizer.removeVariantPrefixFromPath(context
				.getPath());
		if (context.getLocale() != null) {
			variantKey = context.getLocale().getDisplayName();
		}

		// Here we create a new context where the bundle name is the Jawr
		// generator CSS path
		String cssGeneratorBundlePath = PathNormalizer.concatWebPath(context
				.getConfig().getServletMapping(),
				ResourceGenerator.CSS_DEBUGPATH);
		JoinableResourceBundle tempBundle = new JoinableResourceBundleImpl(
				cssGeneratorBundlePath, null, null, null, null);

		BundleProcessingStatus tempStatus = new BundleProcessingStatus(
				tempBundle, context.getResourceReaderHandler(), context
						.getConfig());

		CSSURLPathRewriterPostProcessor postProcessor = new CSSURLPathRewriterPostProcessor();

		ResourceBundlePathsIterator it = null;
		if (bundlesHandler.isGlobalResourceBundle(bundlePath)) {
			it = new ListPathsIteratorImpl(bundlePath);
			it = bundlesHandler.getGlobalResourceBundlePaths(bundlePath, null,
					variantKey);
		} else {
			it = bundlesHandler.getBundlePaths(bundlePath, null, variantKey);
		}

		while (it.hasNext()) {
			String resourcePath = it.nextPath();
			if (resourcePath != null) {

				tempStatus.setLastPathAdded(resourcePath);
				try {
					Reader cssReader = context.getResourceReaderHandler()
							.getResource(resourcePath, true);
					StringWriter writer = new StringWriter();
					IOUtils.copy(cssReader, writer);
					StringBuffer resourceData = postProcessor
							.postProcessBundle(tempStatus, writer.getBuffer());
					result.append("/** CSS resource : " + resourcePath
							+ " **/\n");
					result.append(resourceData);
					if (it.hasNext()) {
						result.append("\n\n");
					}

				} catch (ResourceNotFoundException e) {
					log.debug("The resource '" + resourcePath
							+ "' was not found");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		// if(context.isProcessingBundle()){
		// context.getConfig().getContext().setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE,
		// imgRsHandler);
		// }
		return new StringReader(result.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.jawr.web.resource.bundle.generator.PrefixedResourceGenerator#
	 * getMappingPrefix()
	 */
	public String getMappingPrefix() {

		return GeneratorRegistry.IE_CSS_GENERATOR_PREFIX;
	}

}
