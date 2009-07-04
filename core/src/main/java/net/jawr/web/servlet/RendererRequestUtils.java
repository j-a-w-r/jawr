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
package net.jawr.web.servlet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.config.jmx.JawrApplicationConfigManager;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;

import org.apache.log4j.Logger;

/**
 * Utilities for tag rendering components, which help in handling request lifecycle aspects.
 * 
 * @author Jordi Hernández Sellés
 * @author Matt Ruby
 * @author Ibrahim Chaehoi
 * 
 */
public class RendererRequestUtils {
	
	/** The logger */
	private static final Logger log = Logger.getLogger(RendererRequestUtils.class.getName());
	
	/** The added collection log */
	private static final String ADDED_COLLECTIONS_LOG = "net.jawr.web.taglib.ADDED_COLLECTIONS_LOG";
	
	/** The added collection log */
	private static final String GLOBAL_BUNDLE_ADDED_PREFIX = "net.jawr.web.taglib.GLOBAL_BUNDLE_ADDED";
	
	/**
	 * Retrieve or create the set to store all added bundles, which is used to avoid adding a bundle more than once during a single request.
	 * 
	 * @param request
	 * @return
	 */
	public static Set getAddedBundlesLog(ServletRequest request) {
		Set set = null;
		if (null != request.getAttribute(ADDED_COLLECTIONS_LOG))
			set = (Set) request.getAttribute(ADDED_COLLECTIONS_LOG);
		else {
			set = new HashSet();
			request.setAttribute(ADDED_COLLECTIONS_LOG, set);
		}
		return set;
	}
	
	/**
	 * Check if the global bundles has been already added.
	 * 
	 * @param request the request
	 * @return true if the global bundles has been already added.
	 */
	public static boolean isGlobalBundleAdded(ServletRequest request, String resourceType) {
		boolean added = false;
		String globalBundleAddedAttributeName = GLOBAL_BUNDLE_ADDED_PREFIX+resourceType;
		if (null != request.getAttribute(globalBundleAddedAttributeName))
			added = ((Boolean) request.getAttribute(globalBundleAddedAttributeName)).booleanValue();
		else {
			request.setAttribute(globalBundleAddedAttributeName, Boolean.FALSE);
		}
		return added;
	}

	/**
	 * Sets the flag indicating if the global bundles has been added.
	 * 
	 * @param request the request
	 * @param resourceType the resource type
	 * @param added the flag to set
	 */
	public static void setGlobalBundleAdded(ServletRequest request, String resourceType, boolean added) {
		String globalBundleAddedAttributeName = GLOBAL_BUNDLE_ADDED_PREFIX+resourceType;
		request.setAttribute(globalBundleAddedAttributeName, new Boolean(added));
	}
	
	/**
	 * Determines wether gzip is suitable for the current request given the current config.
	 * 
	 * @param req
	 * @param jawrConfig
	 * @return
	 */
	public static boolean isRequestGzippable(HttpServletRequest req, JawrConfig jeesConfig) {
		boolean rets;
		// If gzip is completely off, return false.
		if (!jeesConfig.isGzipResourcesModeOn())
			rets = false;
		else if (req.getHeader("Accept-Encoding") != null && req.getHeader("Accept-Encoding").indexOf("gzip") != -1) {

			// If gzip for IE6 or less is off, the user agent is checked to avoid compression.
			if (!jeesConfig.isGzipResourcesForIESixOn()) {
				String agent = req.getHeader("User-Agent");
				if (log.isDebugEnabled())
					log.debug("User-Agent for this request:" + agent);

				if (null != agent && agent.indexOf("MSIE") != -1) {
					rets = agent.indexOf("MSIE 4") == -1 && agent.indexOf("MSIE 5") == -1 && agent.indexOf("MSIE 6") == -1;
					if (log.isDebugEnabled())
						log.debug("Gzip enablement for IE executed, with result:" + rets);
				} else
					rets = true;
			} else
				rets = true;
		} else
			rets = false;
		return rets;
	}

	/**
	 * Determines wether to override the debug settings. Sets the debugOverride status on ThreadLocalJawrContext
	 * 
	 * @param req the request
	 * @param jawrConfig the jawr config
	 * 
	 */
	public static void setRequestDebuggable(HttpServletRequest req, JawrConfig jawrConfig) {

		// make sure we have set an overrideKey
		// make sure the overrideKey exists in the request
		// lastly, make sure the keys match
		if (jawrConfig.getDebugOverrideKey().length() > 0 && null != req.getParameter(JawrConstant.OVERRIDE_KEY_PARAMETER_NAME)
				&& jawrConfig.getDebugOverrideKey().equals(req.getParameter(JawrConstant.OVERRIDE_KEY_PARAMETER_NAME))) {
			ThreadLocalJawrContext.setDebugOverriden(true);
		} else {
			ThreadLocalJawrContext.setDebugOverriden(false);
		}

		// Inherit the debuggable property of the session if the session is a debuggable one
		inheritSessionDebugProperty(req);

	}

	/**
	 * Sets a request debuggable if the session is a debuggable session.
	 * 
	 * @param req the request
	 */
	public static void inheritSessionDebugProperty(HttpServletRequest request) {

		HttpSession session = request.getSession();
		if (session != null) {
			String sessionId = session.getId();

			JawrApplicationConfigManager appConfigMgr = (JawrApplicationConfigManager) session.getServletContext().getAttribute(
					JawrConstant.JAWR_APPLICATION_CONFIG_MANAGER);

			// If the session ID is a debuggable session ID, activate debug mode for the request.
			if (appConfigMgr != null && appConfigMgr.isDebugSessionId(sessionId)) {
				ThreadLocalJawrContext.setDebugOverriden(true);
			}
		}
	}
	
	/**
	 * Returns true if the request URL is a SSL request (https://) 
	 * @param request the request
	 * @return true if the request URL is a SSL request
	 */
	public static boolean isSslRequest(HttpServletRequest request) {
		
		String requestUrl = request.getRequestURL().toString();
		return requestUrl.toLowerCase().startsWith(JawrConstant.HTTPS_URL_PREFIX);
	}

	
}
