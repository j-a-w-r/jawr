import net.jawr.web.servlet.JawrRequestHandler;

class JawrCSSController {
	def defaultAction = "doGet"
	JawrRequestHandler requestHandler;
		
		 
		def doGet = {
			
			if(null == requestHandler)
				requestHandler = servletContext.getAttribute("CSSJawrRequestHandler");
			
			requestHandler.processRequest(request['javax.servlet.forward.servlet_path'],request, response );
			
			return null;
		}
}