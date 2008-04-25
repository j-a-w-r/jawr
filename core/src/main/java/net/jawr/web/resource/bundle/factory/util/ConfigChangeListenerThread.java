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

import org.apache.log4j.Logger;

/**
 * A threaded compoenent that periodically checks for updates to the configuration of Jawr. 
 * 
 * @author Jordi Hernández Sellés
 */
public class ConfigChangeListenerThread extends Thread {
	private static final Logger log = Logger.getLogger(ConfigChangeListenerThread.class.getName());
	
	private long waitMillis;
	private ConfigPropertiesSource propertiesSource;
	private ConfigChangeListener listener;
	
	public ConfigChangeListenerThread(ConfigPropertiesSource propertiesSource,
			ConfigChangeListener listener, long secondsToWait ) {
		super();
		this.propertiesSource = propertiesSource;
		this.listener = listener;
		this.waitMillis = secondsToWait * 1000;
		this.setDaemon(true);
	}


	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		while(true) {
			try {
				sleep(waitMillis);
								
				/* It is painful to show a log statement every certain amount of seconds...		  
				 if(log.isDebugEnabled())
					log.debug("Verifying wether properties are changed...");
					*/
				
				if(propertiesSource.configChanged())
					listener.configChanged(propertiesSource.getConfigProperties());
				
			} catch (InterruptedException e) {
				log.error("Failure at config reloading checker thread.");
			}
		}
		
	}
	
	

}
