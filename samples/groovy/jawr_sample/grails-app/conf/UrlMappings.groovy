class UrlMappings {
    static mappings = {
      "/$controller/$action?/$id?"{
	      constraints {
			 // apply constraints here
		  }
	  }
	  "/script/**"(controller:'jawrJavascript')
    "/style/**"(controller:'jawrCSS')
	  "500"(view:'/error')
	}
}
