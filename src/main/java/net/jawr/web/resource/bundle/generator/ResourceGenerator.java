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
package net.jawr.web.resource.bundle.generator;

import java.io.Reader;
import java.nio.charset.Charset;

/**
 * A ResourceGenerator is acomponent that generates script or CSS dynamically, instead of reading 
 * it from the contents of a WAR file. It is used for creating resources programatically or to 
 * retrieve them from sources outsied the scope of a WAR file. 
 * 
 * @author  Jordi Hernández Sellés
 *
 */
public interface ResourceGenerator {

	/**
	 * Create a reader on a generated resource (any script not read from the war file 
	 * structure). 
	 * 
	 * @param path
	 * @param charset
	 * @return
	 */
	public Reader createResource(String path,Charset charset);
}
