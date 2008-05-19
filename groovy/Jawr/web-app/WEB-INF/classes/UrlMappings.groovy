class UrlMappings {
    static mappings = {
      "/index"(view:'index')
      "/**.js"(controller:'jawrJavascript')
      "/**.css"(controller:'jawrCSS')
	}
}
