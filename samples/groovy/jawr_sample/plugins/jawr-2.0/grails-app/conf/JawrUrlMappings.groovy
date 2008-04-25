/**
 * The defaul Jawr configuration will serve all js and css files, but it requires 
 * grails.mime.file.extensions to be set to false in Config.groovy. 
 */
class JawrUrlMappings {
    static mappings = {
      "/**.js"(controller:'jawrJavascript')
      "/**.css"(controller:'jawrCSS')
	}
}
