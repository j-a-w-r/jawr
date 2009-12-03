/**
 * Copyright 2007-2009 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web;

/**
 * The constant value for Jawr.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class JawrConstant {

	/** The URL separator */
	public static final String URL_SEPARATOR = "/";

	/** The URL separator character. */
	public static final char URL_SEPARATOR_CHAR = '/';

	/** The comma separator */
	public static final String COMMA_SEPARATOR = ",";

	/** The init parameter servlet for the resource type */
	public static final String TYPE_INIT_PARAMETER = "type";

	/** The image type */
	public static final String IMG_TYPE = "img";

	/** The js type */
	public static final String JS_TYPE = "js";

	/** The css type */
	public static final String CSS_TYPE = "css";

	/** The classpath resource prefix */
	public static final String CLASSPATH_RESOURCE_PREFIX = "jar:";

	/** The cache buster separator */
	public static final String CACHE_BUSTER_PREFIX = "cb";

	/** The Jawr application config manager attribute */
	public static final String JAWR_APPLICATION_CONFIG_MANAGER = "net.jawr.web.jmx.JAWR_APPLICATION_CONFIG_MANAGER";

	/** The javascript servlet context attribute name */
	public static final String JS_CONTEXT_ATTRIBUTE = "net.jawr.web.resource.bundle.JS_CONTEXT_ATTRIBUTE";

	/** The css servlet context attribute name */
	public static final String CSS_CONTEXT_ATTRIBUTE = "net.jawr.web.resource.bundle.CSS_CONTEXT_ATTRIBUTE";

	/** The image servlet context attribute name */
	public static final String IMG_CONTEXT_ATTRIBUTE = "net.jawr.web.resource.bundle.IMG_CONTEXT_ATTRIBUTE";

	/** The https scheme */
	public static final String HTTPS = "https";
	
	/** The https url prefix */
	public static final String HTTPS_URL_PREFIX = "https://";

	/** The http url prefix */
	public static final String HTTP_URL_PREFIX = "http://";

	/** The jawr bundle mapping properties file name for JS resources */
	public static final String JAWR_JS_MAPPING_PROPERTIES_FILENAME = "jawr-js-mapping.properties";
	
	/** The jawr bundle mapping properties file name for CSS resources */
	public static final String JAWR_CSS_MAPPING_PROPERTIES_FILENAME = "jawr-css-mapping.properties";
	
	/** The jawr bundle mapping properties file name for image resources */
	public static final String JAWR_IMG_MAPPING_PROPERTIES_FILENAME = "jawr-img-mapping.properties";
	
	/** The servlet mapping property name */
	public static final String SERVLET_MAPPING_PROPERTY_NAME = "mapping";

	/** The file URI prefix */
	public static final String FILE_URI_PREFIX = "file://";

	/** The override key parameter name */
	public static final String OVERRIDE_KEY_PARAMETER_NAME = "overrideKey";

	/** The property which enables the use of JMX */
	public static final String JMX_ENABLE_FLAG_SYSTEL_PROPERTY = "com.sun.management.jmxremote";
	
	/** The servlet temp directory property name */
	public static final String SERVLET_CONTEXT_TEMPDIR = "javax.servlet.context.tempdir";

	/** The smartsprites temporary directory */
	public static final String CSS_SMARTSPRITES_TMP_DIR = "/cssSprites/src/";

	/** The ID of the CSS sprite global preprocessor */
	public static final String GLOBAL_CSS_SMARTSPRITES_PREPROCESSOR_ID = "smartsprites";

	/** The ID of the empty global preprocessor */
	public static final String EMPTY_GLOBAL_PREPROCESSOR_ID = "none";
	
	/** The META-INF directory prefix */
	public static final String META_INF_DIR_PREFIX = "/META-INF/";

	/** The WEB-INF directory prefix */
	public static final String WEB_INF_DIR_PREFIX = "/WEB-INF/";

	
}
