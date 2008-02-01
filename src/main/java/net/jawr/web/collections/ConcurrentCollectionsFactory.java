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
 */
public class ConcurrentCollectionsFactory {
	private static final Logger log = Logger.getLogger(ConcurrentCollectionsFactory.class);

	/**
	 * Builds a ConcurrentHashMap if available either as its java.util.concurrent or 
	 * its edu.emory.mathcs.backport.java.util.concurrent implementation. If neither if found, 
	 * Collections.synchronizedMap is used to create the instance. 
	 * @return
	 */
	public static final Map buildConcurrentHashMap() {
		Class mapClass = null;
		Map syncMap = null;
		try {
			mapClass = Class.forName("java.util.concurrent.ConcurrentHashMap");
			if(log.isDebugEnabled())
				log.debug("Using JDK 5 ConcurrentHashMap. ");
		} catch (ClassNotFoundException e) {
			if(log.isDebugEnabled())
				log.debug("JDK 5 ConcurrentHashMap not found, attempting to use the backport version");
			try {
				mapClass = Class.forName("edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap");
				if(log.isDebugEnabled())
					log.debug("Using backport-util-concurrent ConcurrentHashMap. ");
			} catch (ClassNotFoundException e1) {
				log.warn("Using synchronized map for in-memory cache. It is recommended that you use backport-util-concurrent or java 5+ to improve performance. ");
			}
		}

		if(null != mapClass) {
			try {
				syncMap = (Map) mapClass.newInstance();
			} catch (InstantiationException e) {
				log.warn("InstantiationException creating ConcurrentHashMap. Using synchronized map for in-memory cache. ");
			} catch (IllegalAccessException e) {
				log.warn("IllegalAccessException creating ConcurrentHashMap. Using synchronized map for in-memory cache.  ");
			}
		}
		if(null == syncMap) {
			syncMap = Collections.synchronizedMap(new HashMap());
			log.warn("Synchronized map created for in-memory cache. Performance will NOT be optimal for the cache. ");
		}
		
		return syncMap;
	}
	
	/**
	 * Build a CopyOnWriteArrayList, intended to perform well on frecuent iteration with seldom happening updates or removals. 
	 *  
	 * @return
	 */
	public static final List buildCopyOnWriteArrayList() {
		Class listClass = null;
		List cowList = null;
		try {
			listClass = Class.forName("java.util.concurrent.CopyOnWriteArrayList");
			if(log.isDebugEnabled())
				log.debug("Using JDK 5 CopyOnWriteArrayList. ");
			
		} catch (ClassNotFoundException e) {
			if(log.isDebugEnabled())
				log.debug("JDK 5 CopyOnWriteArrayList not found, attempting to use the backport version");
			try {
				listClass = Class.forName("edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArrayList");
				if(log.isDebugEnabled())
					log.debug("Using backport-util-concurrent CopyOnWriteArrayList. ");
			} catch (ClassNotFoundException e1) {
				log.warn("Using synchronized List. It is recommended that you use backport-util-concurrent or java 5+ to improve performance. ");
			}
		}

		if(null != listClass) {
			try {
				cowList = (List) listClass.newInstance();
			} catch (InstantiationException e) {
				log.warn("InstantiationException creating CopyOnWriteArrayList. Using synchronized map for in-memory cache. ");
			} catch (IllegalAccessException e) {
				log.warn("IllegalAccessException creating CopyOnWriteArrayList. Using synchronized map for in-memory cache.  ");
			}
		}
		if(null == cowList) {
			cowList = Collections.synchronizedList(new ArrayList());
			log.warn("Synchronized List created. ");
		}
		
		return cowList;
	}

}
