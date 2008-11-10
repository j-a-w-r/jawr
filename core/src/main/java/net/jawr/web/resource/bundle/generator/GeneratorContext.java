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
package net.jawr.web.resource.bundle.generator;

import java.nio.charset.Charset;
import java.util.Locale;

import javax.servlet.ServletContext;

import net.jawr.web.config.JawrConfig;

/**
 * Encapsulates the parameters needed in Generators. 
 * 
 * @author Jordi Hernández Sellés
 */
public class GeneratorContext {

	private static final String PARENFINDER_REGEXP = ".*(\\(.*\\)).*";
	private static final String BRACKFINDER_REGEXP = ".*(\\[.*\\]).*";
	
	private String path;
	private JawrConfig config;
	private Locale locale;
	private String parenthesesParam;
	private String bracketsParam;
	
	/**
	 * @param config
	 * @param path
	 */
	public GeneratorContext(JawrConfig config, String path) {
		super();
		this.config = config;
		
		// init parameters, if any
		if(path.matches(PARENFINDER_REGEXP)) {
			parenthesesParam = path.substring(path.indexOf('(')+1,path.indexOf(')'));

			path = path.substring(0, path.indexOf('(') ) +
			path.substring(path.indexOf(')') + 1);
		}
		if(path.matches(BRACKFINDER_REGEXP)) {
			bracketsParam = path.substring(path.indexOf('[')+1,path.indexOf(']'));

			path = path.substring(0, path.indexOf('[') ) +
			path.substring(path.indexOf(']') + 1);
		}
		this.path = path;
	}

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the config
	 */
	public JawrConfig getConfig() {
		return config;
	}
	
	/**
	 * @return ServletContext app servletcontext
	 */
	public ServletContext getServletContext(){
		return config.getContext();
	}
	
	/**
	 * @return The charset in which to generate resources. 
	 */
	public Charset getCharset() {
		return config.getResourceCharset();
	}
	
	/**
	 * Get values in parentheses. 
	 * @param param
	 * @param defaultValue Default to return when there are no parentheses. 
	 * @return
	 */
	public String getParenthesesParam() {
		return parenthesesParam;
	}

	/**
	 * Get values in brackets. 
	 * @param param
	 * @param defaultValue Default to return when there are no parentheses. 
	 * @return
	 */
	public String getBracketsParam() {
		return bracketsParam;
	}
}
