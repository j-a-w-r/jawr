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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This interface defines the methods of the Handler for illegal requested bundles,
 * the bundle which are requested with a wrong hashcode.
 * 
 * @author Ibrahim Chaehoi
 */
public interface IllegalBundleRequestHandler {

	/**
	 * This method should return true if Jawr should send back the content of the bundle.
	 * @param requestedPath the requested path
	 * @param request the request
	 * @return true if if Jawr should send back the content of the bundle.
	 */
	boolean canWriteContent(String requestedPath, HttpServletRequest request);
	
	/**
	 * This method can update the response header and 
	 * returns true if Jawr must write the response header or false otherwise.
	 * @param response the response
	 * @return true if Jawr must write the response header
	 */
	boolean writeResponseHeader(HttpServletResponse response);
	
}
