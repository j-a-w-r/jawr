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
package net.jawr.web.resource.bundle.generator.dwr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;

import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.generator.AbstractJavascriptGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;

import org.apache.log4j.Logger;
import org.directwebremoting.Container;
import org.directwebremoting.extend.CreatorManager;
import org.directwebremoting.extend.EnginePrivate;
import org.directwebremoting.extend.Remoter;
import org.directwebremoting.impl.DefaultCreatorManager;
import org.directwebremoting.impl.StartupUtil;
import org.directwebremoting.servlet.EngineHandler;
import org.directwebremoting.servlet.PathConstants;

/**
 * @author Jordi Hernández Sellés
 */
public class DWR3BeanGenerator extends AbstractJavascriptGenerator implements ResourceGenerator {

	private static final Logger log = Logger.getLogger(DWR3BeanGenerator.class.getName());

	// Mapping keys
	private static final String ALL_INTERFACES_KEY = "_**";
	
	private static final String ENGINE_KEY = "_engine";
	private static final String UTIL_KEY = "_util";
	private static final String AUTH_KEY = "_auth";
	private static final String WEBWORK_KEY = "_actionutil";
	private static final String BAYEUX_KEY = "_bayeux";
	private static final String GI_KEY = "_gi";
	
	// Path to DWR javascript files
	private static final String ENGINE_PATH = "org/directwebremoting/engine.js";
	private static final String UTIL_PATH = "org/directwebremoting/ui/servlet/util.js";
	private static final String AUTH_PATH = "org/directwebremoting/auth/auth.js";
	private static final String WEBWORK_PATH = "org/directwebremoting/webwork/DWRActionUtil.js";
	private static final String GI_PATH = "org/directwebremoting/gi.js";
	private static final String BAYEUX_PATH = "org/directwebremoting/dwr-bayeux.js";
	
	// Convenience map to avoid many if-elses later
	private static final Map<String,String> dwrLibraries = new HashMap<String,String>(3);
	
	// Script replacement to refer to a javascript var that JAWR creates
	private static final String JS_PATH_REF = "'+JAWR.jawr_dwr_path+'";
	private static final String JS_CTX_PATH = "'+JAWR.app_context_path+'/";
	
	// Some names of init-params in DWR servlets
	private static final String DWR_MAPPING_PARAM = "jawr_mapping";
	private static final String DWR_OVERRIDEPATH_PARAM = "overridePath";
		
	// This is a script portion stored in a static method of DWR. We store it to remove it, so it is not replicated many times.  
	private static String ENGINE_INIT;
	
	static {
		ENGINE_INIT = EnginePrivate.getEngineInitScript();
		dwrLibraries.put(UTIL_KEY, UTIL_PATH);
		dwrLibraries.put(AUTH_KEY, AUTH_PATH);
		dwrLibraries.put(WEBWORK_KEY, WEBWORK_PATH);
		dwrLibraries.put(BAYEUX_KEY, BAYEUX_PATH);
		dwrLibraries.put(GI_KEY, GI_PATH);
	}
	
	
	public DWR3BeanGenerator() {
		super();
	}



	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public Reader createResource(GeneratorContext context) {
		StringBuffer data = null;
		if(ENGINE_KEY.equals(context.getPath())) {
			data = buildEngineScript(readDWRScript(ENGINE_PATH),context.getServletContext());
		}
		else if(dwrLibraries.containsKey(context.getPath())){
			data = readDWRScript((String)dwrLibraries.get(context.getPath()));
		}
		else if(ALL_INTERFACES_KEY.equals(context.getPath())) {
			data = new StringBuffer(ENGINE_INIT);
			data.append(getAllPublishedInterfaces(context.getServletContext()));
		}
		else {
			data = new StringBuffer(ENGINE_INIT);
			StringTokenizer tk = new StringTokenizer(context.getPath(),"|");
			while(tk.hasMoreTokens()) {
				data.append(getInterfaceScript(tk.nextToken(),context.getServletContext()));
			}
		}
		
		return new StringReader(data.toString());
	}
	
	
	
	
	/**
	 * Performs replacement on the engine.js script from DWR. 
	 * Mainly copies what DWR does, only at startup. A couple params are actually
	 * replaced to references to javascript vars that jawr will create on the page. 
	 * 
	 * @param engineScript
	 * @return
	 */
	private StringBuffer buildEngineScript(StringBuffer engineScript,ServletContext servletContext) {
		
		List<Container> containers = StartupUtil.getAllPublishedContainers(servletContext);
		
		for(Container container :containers ) {
			String engineHandlerUrl = (String) container.getBean("engineHandlerUrl");
			EngineHandler engHandler = (EngineHandler) container.getBean(PathConstants.PATH_PREFIX + engineHandlerUrl);
			Map<String, String> replace = engHandler.getSearchReplacePairs();
			
			// Adds replacement for the DWR servlet path. 
			replace.put("${pathToDwrServlet}","\"+JAWR.jawr_dwr_path+\"");
			
			
			String template = engineScript.toString();
			if (replace != null)
	        {
	            for (Map.Entry<String, String> entry : replace.entrySet())
	            {
	                String search = entry.getKey();
	                if (template.contains(search))
	                {
	                    template = template.replace(search, entry.getValue());
	                }
	            }
	        }
			return new StringBuffer(template);
		}
		return null;
		
	}
	
	/**
	 * Read a DWR utils script from the classpath. 
	 * @param classpath
	 * @return
	 */
	private StringBuffer readDWRScript(String classpath) {
		StringBuffer sb = null;
		try {
			InputStream is = ClassLoaderResourceUtils.getResourceAsStream(classpath, this);
			ReadableByteChannel chan = Channels.newChannel(is);
			Reader r = Channels.newReader(chan,"utf-8");
			StringWriter sw = new StringWriter();
			int i = 0;
			while((i = r.read()) != -1) {
				sw.write(i);
			}
			sb = sw.getBuffer();
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return sb;
	}
	
	/**
	 * Returns a script with a specified DWR interface
	 * @param basePath
	 * @return
	 */
	private StringBuffer getInterfaceScript(String scriptName,ServletContext servletContext) {
		StringBuffer sb = new StringBuffer(ENGINE_INIT);

		// List all containers to find all DWR interfaces
		List<Container> containers = StartupUtil.getAllPublishedContainers(servletContext);
		boolean found = false;
		for(Container container :containers) {
			
			// The creatormanager holds the list of beans
			CreatorManager ctManager = (CreatorManager) container.getBean(CreatorManager.class.getName());
			if( null != ctManager ) {
				// The remoter builds interface scripts. 
				Remoter remoter = (Remoter) container.getBean(Remoter.class.getName());
				String path = getPathReplacementString(container);
				try {
					String script = remoter.generateInterfaceScript(scriptName, true, path);
					// Must remove the engine init script to avoid unneeded duplication
					script = removeEngineInit(script);
					sb.append(script);
					found = true;
					break;
				}
				catch(SecurityException ex){throw new RuntimeException(ex); }
			}
		}
		if(!found)
			throw new IllegalArgumentException("The DWR bean named '" + scriptName + "' was not found in any DWR configuration instance."); 
		
		return sb;
	}



	/**
	 * Gets the appropiate path replacement string for a DWR container
	 * @param container
	 * @return
	 */
	private String getPathReplacementString(Container container) {
		String path = JS_PATH_REF;
		if(null != container.getBean(DWR_OVERRIDEPATH_PARAM)) {
			path = (String) container.getBean(DWR_OVERRIDEPATH_PARAM);
		}
		else if(null != container.getBean(DWR_MAPPING_PARAM)) {
			path = JS_CTX_PATH + container.getBean(DWR_MAPPING_PARAM);
		}
		return path;
	}
	
	/**
	 * Returns a script with all the DWR interfaces available in the servletcontext
	 * @param basePath
	 * @return
	 */
	private StringBuffer getAllPublishedInterfaces(ServletContext servletContext) {
		
		StringBuffer sb = new StringBuffer();

		// List all containers to find all DWR interfaces
		List<Container> containers = StartupUtil.getAllPublishedContainers(servletContext);
		for(Container container : containers) {
			
			// The creatormanager holds the list of beans
			CreatorManager ctManager = (CreatorManager) container.getBean(CreatorManager.class.getName());
			
			if(null != ctManager) {
				// The remoter builds interface scripts. 
				Remoter remoter = (Remoter) container.getBean(Remoter.class.getName());
				
				String path = getPathReplacementString(container);
				boolean debugMode = ctManager.isDebug();
				Collection<String> creators = null;
				if(!(ctManager instanceof DefaultCreatorManager)) {
					if(!debugMode)
						log.warn("The current creatormanager is a custom implementation [" 
								+ ctManager.getClass().getName() 
								+ "]. Debug mode is off, so the mapping dwr:_** is likely to trigger a SecurityException." +
								" Attempting to get all published creators..." );
					creators = ctManager.getCreatorNames(false);
					
				}
				else {	
					DefaultCreatorManager dfCreator = (DefaultCreatorManager) ctManager;					
					try 
					{
						dfCreator.setDebug(true);
						creators = ctManager.getCreatorNames(false);
					}
					finally{
						// restore debug mode no matter what
						dfCreator.setDebug(debugMode);
					}
				}
				for(String name : creators) {
					if(log.isDebugEnabled())
						log.debug("_** mapping: generating found interface named: " + name);
					
					String script = remoter.generateInterfaceScript(name, false, path);
					// Must remove the engine init script to avoid unneeded duplication
					script = removeEngineInit(script);
					sb.append(script);
				}
				// Generates all DTOs separately to avoid duplication. 
				sb.append("; ").append(remoter.generateAllDtoScripts());
			}
		}
		return sb;
	}
	
	/**
	 * Removes the engine init script so that it is not repeated unnecesarily. 
	 * @param script
	 * @return
	 */
	private String removeEngineInit(String script) {
		int start = script.indexOf(ENGINE_INIT);
		int end = start + ENGINE_INIT.length();
		StringBuffer rets = new StringBuffer();
		
		if(start > 0) {
			rets.append(script.substring(0, start)).append("\n");
		}
		rets.append(script.substring(end));
		
		
		return rets.toString();
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#getMappingPrefix()
	 */
	public String getMappingPrefix() {
		return GeneratorRegistry.DWR_BUNDLE_PREFIX;
	}
	

}
