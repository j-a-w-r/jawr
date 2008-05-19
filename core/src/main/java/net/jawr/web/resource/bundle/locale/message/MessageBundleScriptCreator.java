/**
 * Copyright 2007-2008 Jordi Hernández Sellés
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
package net.jawr.web.resource.bundle.locale.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;

import org.apache.log4j.Logger;


/**
 * Creates a script which holds the data from a message bundle(s). The script is such that properties can be accessed as functions
 * (i.e.: alert(com.mycompany.mymessage()); ). 
 * 
 * @author Jordi Hernández Sellés
 */
public class MessageBundleScriptCreator {
	private static final Logger log = Logger.getLogger(MessageBundleScriptCreator.class.getName());
	private static final String SCRIPT_TEMPLATE = "/net/jawr/web/resource/bundle/message/messages.js";
	private static StringBuffer template;
	private String configParam;
	private String namespace = "messages";
	private String filter;
	private Properties props;
	private static final String PARENFINDER_REGEXP = ".*(\\(.*\\)).*";
	private static final String BRACKFINDER_REGEXP = ".*(\\[.*\\]).*";
	private List filterList;
	
	
	public MessageBundleScriptCreator(String configParam) {
		super();
		if(null == template)
			template = loadScriptTemplate();
		
		props = new Properties();
		
		// Set the namespace
		if(configParam.matches(PARENFINDER_REGEXP)) {
			namespace = configParam.substring(configParam.indexOf('(')+1,configParam.indexOf(')'));
			configParam = configParam.substring(configParam.indexOf(')')+1);
		}
		// Set the filter
		if(configParam.matches(BRACKFINDER_REGEXP)) {
			filter = configParam.substring(configParam.indexOf('[')+1,configParam.indexOf(']'));
			StringTokenizer tk = new StringTokenizer(filter,"\\|");
			filterList = new ArrayList();
			while(tk.hasMoreTokens())
				filterList.add(tk.nextToken());
			configParam = configParam.substring(configParam.indexOf(']')+1);
		}
		
		this.configParam = configParam;
	}
	
	
	/**
	 * Loads a template containing the functions which convert properties into methods. 
	 * @return
	 */
	private StringBuffer loadScriptTemplate() {
		StringWriter sw = new StringWriter();
		try {
			InputStream is = ClassLoaderResourceUtils.getResourceAsStream(SCRIPT_TEMPLATE,this);
			int i;
			while((i = is.read()) != -1) {
				sw.write(i);
			}
		} catch (IOException e) {
			log.fatal("a serious error occurred when initializing MessageBundleScriptCreator");
			throw new RuntimeException("Classloading issues prevent loading the message template to be loaded. ");
		}
		
		return sw.getBuffer();
	}


	/**
	 * Loads the message resource bundles specified and uses a BundleStringJasonifier to generate the properties. 
	 * @return
	 */
	public Reader createScript(){
		Locale locale = null;
		if(configParam.indexOf('@') != -1){
			String localeKey = configParam.substring(configParam.indexOf('@')+1);
			configParam = configParam.substring(0,configParam.indexOf('@'));
			
			// Resourcebundle should be doing this for me...
			String[] params = localeKey.split("_");			
			switch(params.length) {
				case 3:
					locale = new Locale(params[0],params[1],params[2]);
					break;
				case 2: 
					locale = new Locale(params[0],params[1]);
					break;
				default:
					locale = new Locale(localeKey);
			}
		}
		String[] names = configParam.split("\\|");
		for(int x = 0;x < names.length; x++) {
			ResourceBundle bundle;
			
			if(null != locale)
				bundle = ResourceBundle.getBundle(names[x],locale);
			else bundle = ResourceBundle.getBundle(names[x]);
			
			Enumeration keys = bundle.getKeys();
			while(keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				
				if(matchesFilter(key))
					props.put(key, bundle.getString(key));
			}
		}
		BundleStringJasonifier bsj = new BundleStringJasonifier(props);
		String script = template.toString();
		String messages = bsj.serializeBundles().toString();
		script = script.replaceFirst("@namespace", namespace);
		script = script.replaceFirst("@messages", messages);
		
		return new StringReader(script);
	}
	
	/**
	 * Determines wether a key matches any of the set filters. 
	 * @param key
	 * @return
	 */
	private boolean matchesFilter(String key) {
		boolean rets = (null == filterList);
		if(!rets) {
			for(Iterator it = filterList.iterator();it.hasNext() && !rets; )
				rets = key.startsWith((String)it.next());
		}
		return rets;
			
	}
	
}
