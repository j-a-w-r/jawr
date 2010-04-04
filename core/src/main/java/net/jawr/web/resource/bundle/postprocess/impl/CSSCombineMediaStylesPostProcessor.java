/**
 * Copyright 2009 Gerben Jorna
 */
package net.jawr.web.resource.bundle.postprocess.impl;

import java.io.IOException;

import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * This class defines the post processor which combines style sheets for different media types
 * into a single file, surrounding them with a @media block.
 * 
 * Information about css media types: http://www.w3.org/TR/CSS21/media.html
 * 
 * @author Gerben Jorna
 * 
 */
public class CSSCombineMediaStylesPostProcessor extends
		AbstractChainedResourceBundlePostProcessor {

	private static final Logger LOGGER = Logger.getLogger(CSSCombineMediaStylesPostProcessor.class);

	protected static final String CSS_MEDIA_RULE = "@media";
	
	protected static final String CSS_MEDIA_RULE_OPEN = "{";
	
	protected static final String CSS_MEDIA_RULE_CLOSE = "}";
	
	/**
	 * Constructor
	 * @param id the Id of the post processor
	 */
	public CSSCombineMediaStylesPostProcessor() {
		super("combineCSSMediaStyles");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor#doPostProcessBundle(net.jawr.web.resource.bundle.postprocess
	 * .BundleProcessingStatus, java.lang.StringBuffer)
	 */
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status, StringBuffer bundleData) throws IOException {
		LOGGER.info("Post processing file '" + status.getLastPathAdded() + "'");
		
		String bundleMediaTypePropertyName = "jawr.css.bundle." + status.getCurrentBundle().getName() + ".media";
		String bundleMediaType = (String)status.getJawrConfig().getProperty(bundleMediaTypePropertyName);
		if (bundleMediaType == null) {
			LOGGER.warn("no bundle media type provided; use 'screen'");
			bundleMediaType = "screen";
		}

		LOGGER.info("bundle media type: " + bundleMediaType);
		
		StringBuffer sb = new StringBuffer(CSS_MEDIA_RULE + " " + bundleMediaType + " " + CSS_MEDIA_RULE_OPEN + StringUtils.LINE_SEPARATOR);
		sb.append(bundleData);
		sb.append(CSS_MEDIA_RULE_CLOSE + StringUtils.LINE_SEPARATOR + StringUtils.LINE_SEPARATOR);
		
		LOGGER.info("Postprocessing finished");
		return sb;
	}
}
