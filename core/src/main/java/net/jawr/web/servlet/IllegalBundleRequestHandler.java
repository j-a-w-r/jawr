package net.jawr.web.servlet;

import javax.servlet.http.HttpServletResponse;

public interface IllegalBundleRequestHandler {

	public boolean canWriteContent();
	
	public boolean writeResponseHeader(HttpServletResponse response);
	
}
