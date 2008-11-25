/**
 * Copyright 2008  Jordi Hernández Sellés
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
package net.jawr.web.resource.bundle.factory.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Utilities to access resources from the classpath
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class ClassLoaderResourceUtils {

	/**
	 * Attempots to load a resource from the classpath, either usinf the caller's class loader or the current thread's 
	 * context classloader. 
	 * 
	 * @param resourcePath
	 * @param source
	 * @return
	 * @throws FileNotFoundException
	 */
	public static InputStream getResourceAsStream(String resourcePath, Object source) throws FileNotFoundException {
		
		// Try the current classloader
		InputStream is = source.getClass().getResourceAsStream(resourcePath);
		
		// Weblogic 10 likes this one better..
		if(null == is) {
			ClassLoader cl = source.getClass().getClassLoader();
			if(null != cl)
				is = cl.getResourceAsStream(resourcePath);
		}
		
		// If current classloader failed, try with the Threads context classloader. If that fails ott, the resource is either not on the 
		// classpath or inaccessible from the current context. 
		
		if(null == is) {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
		}
		
		// Try to retrieve by URL
		if(null == is) {
			try {
				URL url = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
				
				// Last chance, hack in the classloader
				if(null == url) {
					ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
					try {
						 Thread.currentThread().setContextClassLoader(source.getClass().getClassLoader());
						 url = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
					}
					finally {
						 Thread.currentThread().setContextClassLoader(threadClassLoader);
					}
					
				}
				if(null == url){
					throw new FileNotFoundException( resourcePath + " could not be found. ");
				}
				
				is = new FileInputStream(new File(url.getFile()));			
			} 
			catch (IOException e) {
				throw new FileNotFoundException( resourcePath + " could not be found. ");
			}
		}
		
		// Everything failed... exception is trown. 
		if(null == is)
			throw new FileNotFoundException( resourcePath + " could not be found. ");
		return is;
	}
	
	/**
	 * Builds a class instance using reflection, by using its classname. The class must have a zero-arg constructor. 
	 * @param classname the class to build an instance of. 
	 * @return
	 */
	public static Object buildObjectInstance(String classname) {
		Object rets = null;
		try {
			Class clazz = Class.forName(classname);
			rets = clazz.newInstance();
			
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage() 
										+ " [The custom class " 
										+ classname 
										+ " could not be instantiated, check wether it is available on the classpath and" 
										+ " verify that it has a zero-arg constructor].\n" 
										+ " The specific error message is: " + e.getClass().getName() + ":" + e.getMessage(),e);
		}
		return rets;
	}
	
	/**
	 * Builds a class instance using reflection, by using its classname. The class must have a zero-arg constructor. 
	 * @param classname the class to build an instance of. 
	 * @return
	 */
	public static Object buildObjectInstance(String classname, Object[] params) {
		Object rets = null;
		
		Class[] paramTypes = new Class[params.length];
		for(int x = 0; x < params.length; x++){
			paramTypes[x] = params[x].getClass();
		}
		
		try {
			Class clazz = Class.forName(classname);
			rets = clazz.getConstructor(paramTypes).newInstance(params);
			
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage() 
										+ " [The custom class " 
										+ classname 
										+ " could not be instantiated, check wether it is available on the classpath and" 
										+ " verify that it has a zero-arg constructor].\n" 
										+ " The specific error message is: " + e.getClass().getName() + ":" + e.getMessage(),e);
		}
		return rets;
	}
}
