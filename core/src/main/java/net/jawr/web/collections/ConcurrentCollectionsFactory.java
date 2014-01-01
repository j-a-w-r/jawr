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
package net.jawr.web.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Utility class to build a concurrent collection instance, intended to use for resource caching. 
 * If JDK is 1.5 or higher, an instance from the java.util.concurrent package is returned. 
 * Otherwise, an instance of  edu.emory.mathcs.backport.java.util.concurrent package is created.
 * If neither libraries are available, a Collections.synchronized method is invoked to create the Collection.  
 * This factory should be used during initialization only, and not as frequent runtime calls, since reflection is used to 
 * create the instances.  
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public final class ConcurrentCollectionsFactory {
	
	/** The logger */
	private static final Logger LOGGER = Logger.getLogger(ConcurrentCollectionsFactory.class);
	
	/** The flag indicating if we should log errors for concurrent hashmap creation */
	private static boolean logOnHashMap = true;
	
	/** The flag indicating if we should log errors for concurrent list creation */
	private static boolean logOnArray = true;

	/**
	 * Private Constructor. 
	 */
	private ConcurrentCollectionsFactory() {
		
	}
	
	/**
	 * Builds a ConcurrentHashMap if available either as its java.util.concurrent or 
	 * its edu.emory.mathcs.backport.java.util.concurrent implementation. If neither if found, 
	 * Collections.synchronizedMap is used to create the instance. 
	 * @return a concurrent hashmap
	 */
	public static Map buildConcurrentHashMap() {
		Class mapClass = null;
		try {
			mapClass = Class.forName("java.util.concurrent.ConcurrentHashMap");
			if(logOnHashMap){
				LOGGER.debug("Using JDK 5 ConcurrentHashMap. ");
			}
		} catch (ClassNotFoundException e) {
			if(logOnHashMap){
				LOGGER.debug("JDK 5 ConcurrentHashMap not found, attempting to use the backport version");
			}
			mapClass = getBackPortConcurrentHashMapClass();
		}

		final Map syncMap = instantiateConcurrentHashMap(mapClass);
		
		// There were far too many log messages for this. 
		logOnHashMap = false;
		return syncMap;
	}

	/**
	 * Instantiate the concurrent HashMap from the class given in parameter or create a Synchronized HashMap
	 * if the first instantiation failed
	 * @param mapClass the concurrent HashMap class
	 * @return a concurrent HashMap
	 */
	private static Map instantiateConcurrentHashMap(final Class mapClass) {
		
		Map syncMap = null;
		if(null != mapClass) {
			try {
				syncMap = (Map) mapClass.newInstance();
			} catch (InstantiationException e) {
				if(logOnHashMap){
					LOGGER.warn("InstantiationException creating ConcurrentHashMap. Using synchronized map for in-memory cache. ");
				}
			} catch (IllegalAccessException e) {
				if(logOnHashMap){
					LOGGER.warn("IllegalAccessException creating ConcurrentHashMap. Using synchronized map for in-memory cache.  ");
				}
			}
		}
		if(null == syncMap) {
			syncMap = Collections.synchronizedMap(new HashMap());
			if(logOnHashMap){
				LOGGER.warn("Synchronized map created for in-memory cache. Performance will NOT be optimal for the cache. ");
			}
		}
		return syncMap;
	}

	/**
	 * Returns the concurrent hashMap class of edu.emory.mathcs.backport 
	 * @return the concurrent hashMap class of edu.emory.mathcs.backport 
	 */
	private static Class getBackPortConcurrentHashMapClass() {
		
		Class mapClass = null;
		try {
			mapClass = Class.forName("edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap");
			if(logOnHashMap){
				LOGGER.debug("Using backport-util-concurrent ConcurrentHashMap. ");
			}
		} catch (ClassNotFoundException e1) {
			if(logOnHashMap){
				LOGGER.warn("Using synchronized map for in-memory cache. It is recommended that you use backport-util-concurrent or java 5+ to improve performance. ");	
			}
		}
		return mapClass;
	}
	
	/**
	 * Build a CopyOnWriteArrayList, intended to perform well on frequent iteration with seldom happening updates or removals. 
	 *  
	 * @return a concurrent list
	 */
	public static List buildCopyOnWriteArrayList() {
		
		Class listClass = null;
		List cowList = null;
		try {
			listClass = Class.forName("java.util.concurrent.CopyOnWriteArrayList");
			if(logOnArray){
				LOGGER.debug("Using JDK 5 CopyOnWriteArrayList. ");
			}
			
		} catch (ClassNotFoundException e) {
			if(logOnArray){
				LOGGER.debug("JDK 5 CopyOnWriteArrayList not found, attempting to use the backport version");
			}
			listClass = getBackportConcurrentListClass();
		}

		cowList = instantiateConcurrentList(listClass);
		
		// There were far too many log messages for this. 
		logOnArray = false;
		return cowList;
	}

	/**
	 * Instantiate the concurrent list from the class given in parameter or create a Synchronized ArrayList
	 * if the first instantiation failed
	 * @param listClass the concurrent list class
	 * @return a concurrent list
	 */
	private static List instantiateConcurrentList(final Class listClass) {
		
		List cowList = null;
		if(null != listClass) {
			try {
				cowList = (List) listClass.newInstance();
			} catch (InstantiationException e) {
				if(logOnArray){
					LOGGER.warn("InstantiationException creating CopyOnWriteArrayList. Using synchronized map for in-memory cache. ");
				}
			} catch (IllegalAccessException e) {
				if(logOnArray){
					LOGGER.warn("IllegalAccessException creating CopyOnWriteArrayList. Using synchronized map for in-memory cache.  ");
				}
			}
		}
		if(null == cowList) {
			cowList = Collections.synchronizedList(new ArrayList());
			if(logOnArray){
				LOGGER.warn("Synchronized List created. ");
			}
		}
		return cowList;
	}

	/**
	 * Returns the concurrent list class of edu.emory.mathcs.backport 
	 * @return the concurrent list class of edu.emory.mathcs.backport 
	 */
	private static Class getBackportConcurrentListClass() {
		
		Class listClass = null;
		try {
			listClass = Class.forName("edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArrayList");
			if(logOnArray){
				LOGGER.debug("Using backport-util-concurrent CopyOnWriteArrayList. ");
			}
		} catch (ClassNotFoundException e1) {
			if(logOnArray){
				LOGGER.warn("Using synchronized List. It is recommended that you use backport-util-concurrent or java 5+ to improve performance. ");
			}
		}
		return listClass;
	}

}
