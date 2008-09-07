import net.jawr.web.servlet.JawrRequestHandler;

class JawrJavascriptController {
	def defaultAction = "doGet"
	JawrRequestHandler requestHandler;
	
	
	def doGet = {
		
		if(null == requestHandler)
			requestHandler = servletContext.getAttribute("JavascriptJawrRequestHandler");
		
		// TODO usar tambien javax.servlet.forward.servlet_pathinfo si mapping != null
		requestHandler.processRequest(request['javax.servlet.forward.servlet_path'],request, response );
		
		return null;
	}
	
	
}