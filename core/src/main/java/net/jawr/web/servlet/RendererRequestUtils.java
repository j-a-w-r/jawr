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

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.config.jmx.JawrApplicationConfigManager;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.BundleRendererContext;

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
	
	/** The bundle renderer context attribute name */
	private static final String BUNDLE_RENDERER_CONTEXT_ATTR_PREFIX = "net.jawr.web.resource.renderer.BUNDLE_RENDERER_CONTEXT";
	
	/**
	 * Returns the bundle renderer context.
	 * 
	 * @param request the request
	 * @param resourceType the resource type
	 * @return the bundle renderer context.
	 */
	public static BundleRendererContext getBundleRendererContext(HttpServletRequest request, BundleRenderer renderer) {
		String bundleRendererCtxAttributeName = BUNDLE_RENDERER_CONTEXT_ATTR_PREFIX+renderer.getResourceType();
		
		BundleRendererContext ctx = (BundleRendererContext) request.getAttribute(bundleRendererCtxAttributeName);
		if(ctx == null){
			String localeKey = renderer.getBundler().getConfig().getLocaleResolver().resolveLocaleCode(request);
	         boolean isGzippable = isRequestGzippable(request,renderer.getBundler().getConfig());
	         ctx = new BundleRendererContext(request.getContextPath(), localeKey, isGzippable,
	                 isSslRequest(request));
	         request.setAttribute(bundleRendererCtxAttributeName, ctx);
		}
		
		return ctx;
		
	}

	/**
	 * Sets the bundle renderer context.
	 * 
	 * @param request the request
	 * @param resourceType the resource type
	 * @param ctx the bundle renderer context to set.
	 */
	public static void setBundleRendererContext(ServletRequest request, String resourceType, BundleRendererContext ctx) {
		String globalBundleAddedAttributeName = BUNDLE_RENDERER_CONTEXT_ATTR_PREFIX+resourceType;
		request.setAttribute(globalBundleAddedAttributeName, ctx);
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

		HttpSession session = request.getSession(false);
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

	public static String getRenderedUrl(String newUrl, JawrConfig jawrConfig,
			String contextPath, boolean sslRequest) {
		String contextPathOverride = getContextPathOverride(sslRequest, jawrConfig);
		// If the contextPathOverride is not null and we are in production mode,
		// or if we are in debug mode but we should use the contextPathOverride even in debug mode
		// then use the contextPathOverride
		if(contextPathOverride != null && 
				((jawrConfig.isDebugModeOn() && jawrConfig.isUseContextPathOverrideInDebugMode()) ||
				!jawrConfig.isDebugModeOn())) {
			
				String override = contextPathOverride;
				// Blank override, create url relative to path
				if ("".equals(override)) {
					newUrl = newUrl.substring(1);
				} else
					newUrl = PathNormalizer.joinPaths(override, newUrl);
		} else
			newUrl = PathNormalizer.joinPaths(contextPath, newUrl);
		return newUrl;
	}

	/**
	 * Returns the context path depending on the request mode (SSL or not)
	 * 
	 * @param isSslRequest teh flag indicating that the request is an SSL request
	 * @return the context path depending on the request mode
	 */
	private static String getContextPathOverride(boolean isSslRequest, JawrConfig config) {
		String contextPathOverride = null;
		if (isSslRequest) {
			contextPathOverride = config.getContextPathSslOverride();
		} else {
			contextPathOverride = config.getContextPathOverride();
		}
		return contextPathOverride;
	}
	
}
