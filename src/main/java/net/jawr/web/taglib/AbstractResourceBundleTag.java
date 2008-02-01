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
package net.java.jawr.web.taglib;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import net.java.jawr.web.config.JawrConfig;
import net.java.jawr.web.resource.bundle.renderer.BundleRenderer;

import org.apache.log4j.Logger;

/**
 * Abstract implementation of a tag lib component which will retrieve a Jawr config
 * object from the servlet context and use it to render bundles of resources according 
 * to its src attribute.  
 * 
 * @author Jordi Hernández Sellés
 *
 */
public abstract class AbstractResourceBundleTag extends TagSupport {
	
	private static final Logger log = Logger.getLogger(AbstractResourceBundleTag.class.getName());
	private static final String ADDED_COLLECTIONS_LOG = "net.java.jawr.web.taglib.ADDED_COLLECTIONS_LOG";
	
	private String src;
        protected BundleRenderer renderer;
        


	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {		
            
            // In some cases, the container will reuse the tag instance so the renderer can be reused. 
            // The spec says if properties are different then new instances will be created, otherwise instances are reused.  
            if(null == this.renderer)
                this.renderer = createRenderer();		
            
            try {
                HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
                renderer.renderBundleLinks( src,
                                            pageContext.getServletContext().getContextPath(),
                                            getAddedBundlesLog(request),
                                            shouldUseGZIP(request),
                                            pageContext.getOut());
            } catch (IOException ex) {
                throw new JspException("Unexpected IOException when writing script tags for path " + src,ex);
            }

            return super.doStartTag();
	}
	
	/**
	 * Retrieve or create the set to store all added bundles, which is used
	 * to avoid adding a bundle more than once during a single request. 
	 * @param request
	 * @return
	 */
	private Set getAddedBundlesLog(ServletRequest request)
	{
		Set set = null;
		if(null != request.getAttribute(ADDED_COLLECTIONS_LOG))
			set = (Set) request.getAttribute(ADDED_COLLECTIONS_LOG);
		else
		{
			set = new HashSet();
			request.setAttribute(ADDED_COLLECTIONS_LOG, set);
		}
		return set;
	}
        
        /**
         * Determines wether gzip is suitable for the current request. 
         * @param req 
         * @return 
         */
        private boolean shouldUseGZIP(HttpServletRequest req) {
            boolean rets;
            JawrConfig jeesConfig = renderer.getBundler().getConfig();
            // If gzip is completely off, return false. 
            if(!jeesConfig.isGzipResourcesModeOn())
                rets = false;
            else if(req.getHeader("Accept-Encoding") != null && 
		    req.getHeader("Accept-Encoding").indexOf("gzip") != -1 ) {
                
                // If gzip for IE6 or less is off, the user agent is checked to avoid compression. 
                if(!jeesConfig.isGzipResourcesForIESixOn()) {
                    String agent = req.getHeader("User-Agent");
                    if(log.isInfoEnabled())
                        log.info("User-Agent for this request:" +agent);                    
                    
                    if(null != agent && agent.indexOf("MSIE") != -1) {
                        rets =  agent.indexOf("MSIE 4") == -1 && 
                                agent.indexOf("MSIE 5") == -1 && 
                                agent.indexOf("MSIE 6") == -1;
                        if(log.isInfoEnabled())
                            log.info("Gzip enablement for IE executed, with result:" + rets);
                    }
                    else rets = true;
                }
                else rets = true;                
            }
            else rets = false;
            
            return rets;
        }
	
	/**
	 * Set the source of the resource or collection to retrieve. 
	 * @param src
	 */
	public void setSrc(String src) {
		this.src = src;
	}
	
	/**
	 * Retrieve the ResourceCollector from context. Each implementation will use a different key
	 * to retrieve it. 
	 * @return
	 */
	protected abstract BundleRenderer createRenderer();
}
