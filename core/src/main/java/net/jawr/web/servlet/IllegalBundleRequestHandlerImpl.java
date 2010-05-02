/**
 * Copyright 2010 Ibrahim Chaehoi
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
package net.jawr.web.servlet;

import javax.servlet.http.HttpServletResponse;

/**
 * This class defines the default Illegal bundle request handler, 
 * which is used in strict mode and force Jawr to return a 404 to the client
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class IllegalBundleRequestHandlerImpl implements
		IllegalBundleRequestHandler {

	/* (non-Javadoc)
	 * @see net.jawr.web.servlet.IllegalBundleRequestHandler#canWriteContent()
	 */
	public boolean canWriteContent() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.servlet.IllegalBundleRequestHandler#writeResponseHeader(javax.servlet.http.HttpServletResponse)
	 */
	public boolean writeResponseHeader(HttpServletResponse response) {
		return false;
	}

}
