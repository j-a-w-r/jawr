/**
 * Copyright 2009 Ibrahim Chaehoi
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
package net.jawr.web.config.jmx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletContext;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.JmxConfigException;
import net.jawr.web.servlet.JawrRequestHandler;

import org.apache.log4j.Logger;

/**
 * Utility class for JMX.
 * 
 * @author Ibrahim Chaehoi
 */
public final class JmxUtils {

	/** The logger */
	private static final Logger LOGGER = Logger.getLogger(JmxUtils.class);

	/** The context path parameter */
	private static final String CONTEXT_PATH_PARAM_NAME = "contextPath";
	
	/** The default context path used for the application if no context oath is defined */
	private static final String DEFAULT_CONTEXT_PATH_NAME = "default";

	/** The default context path used for the application if no context oath is defined */
	private static final String DEFAULT_CONTEXT_PATH = "/"+DEFAULT_CONTEXT_PATH_NAME;

	/** The getContextPath method name to retrieve the context path for servlet API 2.5 and above */
	private static final String GET_CONTEXT_PATH_METHOD = "getContextPath";

	/** The name of the factory class, which load the MBean server for Java 1.5 and above */
	private static final String JAVA_LANG_MANAGEMENT_MANAGEMENT_FACTORY_CLASSNAME = "java.lang.management.ManagementFactory";

	/** The method name to retrieve the MBean server for Java 1.5 and above */
	private static final String GET_PLATFORM_M_BEAN_SERVER_METHOD = "getPlatformMBeanServer";

	/** The java 1.4 version prefix */
	private static final String JAVA_VERSION_1_4_PREFIX = "1.4";

	/** The java version system property name */
	private static final String JAVA_VERSION_SYSTEM_PROPERTY = "java.version";

	/** The property which enables the use of JMX */
	public static final String JMX_ENABLE_FLAG_SYSTEL_PROPERTY = "com.sun.management.jmxremote";
	
	/**
	 * Constructor 
	 */
	private JmxUtils() {
		
	}
	
	/**
	 * Initialize the JMX Bean 
	 */
	public static void initJMXBean(JawrRequestHandler requestHandler, ServletContext servletContext, String resourceType, Properties configProperties) {
		
		// Skip the initialisation if no JMX jar is find.
		try {
			JmxUtils.class.getClassLoader().loadClass("javax.management.MBeanServer");
		} catch (ClassNotFoundException e1) {
			LOGGER.info("JMX API is not define in the classpath.");
			return;
		}
		
		try {

			MBeanServer mbs = JmxUtils.getMBeanServer();
			if(mbs != null){
				
				ObjectName jawrConfigMgrObjName = JmxUtils.getMBeanObjectName(servletContext, resourceType);
				JawrApplicationConfigManager appConfigMgr = (JawrApplicationConfigManager) servletContext.getAttribute(JawrConstant.JAWR_APPLICATION_CONFIG_MANAGER);
				if(appConfigMgr == null){
					appConfigMgr = new JawrApplicationConfigManager();
					servletContext.setAttribute(JawrConstant.JAWR_APPLICATION_CONFIG_MANAGER, appConfigMgr);
				}
				
				// register the jawrApplicationConfigManager if it's not already done
				ObjectName appJawrMgrObjectName = JmxUtils.getAppJawrConfigMBeanObjectName(servletContext);
				if(!mbs.isRegistered(appJawrMgrObjectName)){
					mbs.registerMBean(appConfigMgr, appJawrMgrObjectName);
				}
				
				// Create the MBean for the current Request Handler
				JawrConfigManager mbean = new JawrConfigManager(requestHandler, configProperties);
				if(mbs.isRegistered(jawrConfigMgrObjName)){
					LOGGER.warn("The MBean '"+jawrConfigMgrObjName.getCanonicalName()+"' already exists. It will be unregisterd and registered with the new JawrConfigManagerMBean.");
					mbs.unregisterMBean(jawrConfigMgrObjName);
				}
				
				// Initialize the jawrApplicationConfigManager
				if(resourceType.equals(JawrConstant.JS_TYPE)){
					appConfigMgr.setJsMBean(mbean);
				}else if(resourceType.equals(JawrConstant.CSS_TYPE)){
					appConfigMgr.setCssMBean(mbean);
				}else{
					appConfigMgr.setImgMBean(mbean);
				}
				
				mbs.registerMBean(mbean, jawrConfigMgrObjName);
			}
			
		} catch (Exception e) {
			LOGGER.error("Unable to instanciate the Jawr MBean for resource type '"+resourceType+"'", e);
		}

	}
	
	/**
	 * Returns the current MBean server or create a new one if not exist.
	 * 
	 * @return the current MBean server or create a new one if not exist.
	 */
	public static MBeanServer getMBeanServer() {

		MBeanServer mbs = null;
		
		// Check if JMX is enable
		if(System.getProperty(JMX_ENABLE_FLAG_SYSTEL_PROPERTY) == null){
			return null;
		}
		
		if (System.getProperty(JAVA_VERSION_SYSTEM_PROPERTY).startsWith(JAVA_VERSION_1_4_PREFIX)) {

			List servers = MBeanServerFactory.findMBeanServer(null);
			if (servers.size() > 0) {
				if (LOGGER.isDebugEnabled()){
					LOGGER.debug("Retrieving the JMX MBeanServer.");
				}
				
				mbs = (MBeanServer) servers.get(0);
			} else {
				if (LOGGER.isDebugEnabled()){
					LOGGER.debug("Creating the JMX MBeanServer.");
				}
				
				mbs = MBeanServerFactory.createMBeanServer();
				
			}
		} else {

			try {
				Class managementFactoryClass = JmxUtils.class.getClassLoader().loadClass(JAVA_LANG_MANAGEMENT_MANAGEMENT_FACTORY_CLASSNAME);
				Method getPlatformMBeanServerMethod = managementFactoryClass.getMethod(GET_PLATFORM_M_BEAN_SERVER_METHOD, new Class[] {});
				mbs = (MBeanServer) getPlatformMBeanServerMethod.invoke(null, null);
			} catch (Exception e) {
				LOGGER.error("Enable to get the JMX MBeanServer.");
			}
		}

		return mbs;
	}
	
	/**
	 * Returns the object name for the Jawr configuration Manager MBean
	 * @param servletContext the servelt context
	 * @param resourceType the resource type
	 * @return the object name for the Jawr configuration Manager MBean
	 * @throws Exception if an exception occurs
	 */
	public static ObjectName getMBeanObjectName(final ServletContext servletContext, final String resourceType) {
		
		String contextPath = getContextPath(servletContext);
		return getMBeanObjectName(contextPath, resourceType);
	}

	/**
	 * Returns the context path associated to the servlet context
	 * @param servletContext the servlet context
	 * @return the context path associated to the servlet context
	 * @throws Exception if an exception occurs
	 */
	public static String getContextPath(ServletContext servletContext) {
		String contextPath = null;
		
		// Get the context path
		
		// If the servlet API version is greater or equals to 2.5, use the getContextPath method
		if(servletContext.getMajorVersion() > 2 || servletContext.getMajorVersion() == 2 && servletContext.getMinorVersion() >= 5){
			
			contextPath = getContextPathForNewServletApi(servletContext);
			
		}else{ // Retrieve the context path from the init parameter or the servlet context
			contextPath = servletContext.getInitParameter(CONTEXT_PATH_PARAM_NAME);
		}
		
		if(contextPath == null){
			LOGGER.warn("No context path defined for this web application. You will face issues, if you are deploying mutiple web app, without defining the context.\n" +
					"If you are using a server with Servlet API less than 2.5, please use the context parameter 'contextPath' in your web.xml to define the context path of the application.");
			
			contextPath = DEFAULT_CONTEXT_PATH;
		}
		
		return contextPath;
	}

	/**
	 * Returns the context path using the servlet API version 2.5 method
	 * @param servletContext the servlet context
	 * @return the context path
	 */
	private static String getContextPathForNewServletApi(
			ServletContext servletContext) {
		String contextPath = null;
		try {
			Method getServletContextPathMethod = servletContext.getClass().getMethod(GET_CONTEXT_PATH_METHOD, new Class[] {});
			contextPath = (String) getServletContextPathMethod.invoke(servletContext, null);
		} catch (SecurityException e) {
			throw new JmxConfigException(e);
		} catch (NoSuchMethodException e) {
			throw new JmxConfigException(e);
		} catch (IllegalArgumentException e) {
			throw new JmxConfigException(e);
		} catch (IllegalAccessException e) {
			throw new JmxConfigException(e);
		} catch (InvocationTargetException e) {
			throw new JmxConfigException(e);
		}
		return contextPath;
	}

	/**
	 * Returns the object name for the Jawr configuration Manager MBean
	 * @param contextPath the context path
	 * @param resourceType  the resource type
	 * @return the object name for the Jawr configuration Manager MBean
	 * @throws Exception if an exception occurs
	 */
	public static ObjectName getMBeanObjectName(final String contextPath, final String resourceType) {
		
		String curCtxPath = contextPath;
		if(curCtxPath == null){
			LOGGER.warn("No context path defined for this web application. You will face issues, if you are deploying mutiple web app, without defining the context.\n" +
				"If you are using a server with Servlet API less than 2.5, please use the context parameter 'contextPath' in your web.xml to define your context path of the application.");
	
			curCtxPath = DEFAULT_CONTEXT_PATH_NAME;
		}
		
		if(curCtxPath.charAt(0) == ('/')){
			curCtxPath = curCtxPath.substring(1);
		}
		String objectNameStr = "net.jawr.web.jmx:type=JawrConfigManager,webappContext="+curCtxPath+",name="+resourceType+"MBean";
		
		return getObjectName(objectNameStr);
	}
	
	/**
	 * Returns the object name for the Jawr Application configuration Manager MBean
	 * @param servletContext the servelt context
	 * @return the object name for the Jawr configuration Manager MBean
	 * @throws Exception if an exception occurs
	 */
	public static ObjectName getAppJawrConfigMBeanObjectName(ServletContext servletContext) {
		
		String contextPath = getContextPath(servletContext);
		if(contextPath.charAt(0) == ('/')){
			contextPath = contextPath.substring(1);
		}
		String objectNameStr = "net.jawr.web.jmx:type=JawrAppConfigManager,webappContext="+contextPath;
		
		return getObjectName(objectNameStr);
	}
	
	/**
     * Create an object name from the name passed in parameter
	 * @param name the name
	 * @return the objectName
	 */
	private static ObjectName getObjectName(String name){
		
		ObjectName mBeanName = null;
		try {
			mBeanName = new ObjectName(name);
		} catch (MalformedObjectNameException e) {
			throw new JmxConfigException(e);
		} catch (NullPointerException e) {
			throw new JmxConfigException(e);
		}
		
		return mBeanName;
	}

	/**
	 * Returns true if JMX is enabled for the application
	 * @return true if JMX is enabled for the application
	 */
	public static boolean isJmxEnabled() {
		return System.getProperty(JawrConstant.JMX_ENABLE_FLAG_SYSTEL_PROPERTY) != null;
	}
}
