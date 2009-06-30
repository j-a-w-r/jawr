/**
 * Copyright 2008 Jordi Hernández Sellés
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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.UrlPathHelper;

/**
 * A Spring Controller implementation which uses a JawrRequestHandler instance to provide 
 * with Jawr functionality within a Spring DispatcherServlet instance. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class JawrSpringController implements Controller, ServletContextAware, InitializingBean {
	
	private JawrRequestHandler requestHandler;
	private Map initParams;
	// Init params
	private String type;
	private String configPropertiesSourceClass;
	private String mapping;
	private String controllerMapping;
	private final UrlPathHelper helper = new UrlPathHelper();
	
	// Config
	private Properties configuration;
	
	private ServletContext context;
	private static final Logger log = Logger.getLogger(JawrSpringController.class);

	

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String requestedPath = (null == mapping) ? helper.getPathWithinApplication(request) : 
												 helper.getPathWithinServletMapping(request);
		
		if(null != controllerMapping)
			requestedPath = request.getPathInfo().substring(controllerMapping.length());
		
		requestHandler.processRequest(requestedPath, request, response);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		initParams = new HashMap(3);
		initParams.put("type",type);
		initParams.put("configPropertiesSourceClass",configPropertiesSourceClass);
		String fullMapping = "";
		if(null != mapping)
			fullMapping = mapping;
		if(null != controllerMapping)
			fullMapping = PathNormalizer.joinPaths(fullMapping, controllerMapping);
		
		initParams.put("mapping",fullMapping);
		if(log.isDebugEnabled())
			log.debug("Initializing Jawr Controller's JawrRequestHandler");
		
		if(JawrConstant.IMG_TYPE.equals(type)){
			requestHandler = new JawrImageRequestHandler(context,initParams, configuration);
		}else{
			requestHandler = new JawrRequestHandler(context,initParams, configuration);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
	 */
	public void setServletContext(ServletContext context) {
		this.context = context;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param configPropertiesSourceClass the configPropertiesSourceClass to set
	 */
	public void setConfigPropertiesSourceClass(String configPropertiesSourceClass) {
		this.configPropertiesSourceClass = configPropertiesSourceClass;
	}

	/**
	 * @param mapping the mapping to set
	 */
	public void setMapping(String mapping) {
		this.mapping = mapping;
	}

	/**
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(Properties configuration) {
		this.configuration = configuration;
	}

	/**
	 * @param controllerMapping the controllerMapping to set
	 */
	public void setControllerMapping(String controllerMapping) {
		if(controllerMapping.endsWith("/"))
			controllerMapping = controllerMapping.substring(0,controllerMapping.length()-1);
		this.controllerMapping = controllerMapping;
	}

}
