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
package net.jawr.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 *
 * Main Jawr servlet. Maps logical URLs to script bundles, which are generated on the fly (may 
 * be cached), and served as a single javascript file. 
 * 
 * 
 * @author Jordi Hernández Sellés
 */
public class JawrServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(JawrServlet.class);

	private static final long serialVersionUID = -7628137098513776342L;
	
	private JawrRequestHandler requestHandler;

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() throws ServletException {
		try {
			requestHandler = new JawrRequestHandler(getServletContext(),getServletConfig());
		}catch (ServletException e) {
			log.fatal("Jawr servlet with name" +  getServletConfig().getServletName() +" failed to initialize properly. ");
			log.fatal("Cause:" + e.getMessage());
			throw e;
		}catch (RuntimeException e) {
			log.fatal("Jawr servlet with name" +  getServletConfig().getServletName() +" failed to initialize properly. ");
			log.fatal("Cause: " + e.getClass().getName() +" [" + e.getMessage() +"]");
			throw new ServletException(e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		requestHandler.doGet(req, resp);
	}
     
}
