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

	/** The init parameter servlet for the resource type */
	public static final String TYPE_INIT_PARAMETER = "type";

	/** The image type */
	public static final String IMG_TYPE = "img";
	
	/** The js type */
	public static final String JS_TYPE = "js";
	
	/** The css type */
	public static final String CSS_TYPE = "css";
	
	/** The cache buster separator */
	public static final String CACHE_BUSTER_PREFIX = "cb";
	
	/** The cache buster separator */
	public static final String CLASSPATH_CACHE_BUSTER_PREFIX = "cpCb";
	
	/** The image context attrbute */
	public static final String IMG_CONTEXT_ATTRIBUTE = "net.jawr.web.resource.bundle.IMG_CONTEXT_ATTRIBUTE";
	
	/** The classpath resource prefix */
	public static final String CLASSPATH_RESOURCE_PREFIX = "jar:";
}
