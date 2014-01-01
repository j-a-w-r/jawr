// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]
// The default codec used to encode data with ${}
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.converters.encoding="UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder=false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable fo AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://www.changeme.com"
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
    }

}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}


    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
	       'org.codehaus.groovy.grails.web.pages', //  GSP
	       'org.codehaus.groovy.grails.web.sitemesh', //  layouts
	       'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
	       'org.codehaus.groovy.grails.web.mapping', // URL mapping
	       'org.codehaus.groovy.grails.commons', // core / classloading
	       'org.codehaus.groovy.grails.plugins', // plugins
	       'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
	       'org.springframework',
	       'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
	
	debug	'net.jawr'
}

// Common properties
jawr.debug.on=true
jawr.gzip.on=false
jawr.charset.name='UTF-8'
jawr.use.bundle.mapping=false
jawr.factory.use.orphans.mapper=false
jawr.debug.ie.force.css.bundle=true
jawr.strict.mode=false
jawr.bundle.hashcode.generator=MD5

// Custom Generator
jawr.custom.generators='net.jawr.resource.generator.SampleImageGenerator'

// Custom Post processors
jawr.custom.postprocessors.sample.class='net.jawr.resource.postprocessor.SamplePostProcessor'
jawr.custom.postprocessors.sample2.class='net.jawr.resource.postprocessor.SamplePostProcessor2'

// Javascript properties and mappings
jawr.js.bundle.basedir='/js/'

jawr.js.bundle.one.id='/js/bundle/main.js'
jawr.js.bundle.one.mappings='/js/global/**,/js/index/'

jawr.js.bundle.two.id='/js/bundle/msg.js'
jawr.js.bundle.two.mappings='messages:grails-app.i18n.messages'

jawr.js.bundle.common.id='/js/common.js'
jawr.js.bundle.common.mappings='/js/yui/yahoo-dom-event/yahoo-dom-event.js,/js/yui/element/element.js,/js/yui/tabview/tabview.js,/js/yui/container/container.js,skinSwitcher:switcher.js'

// CSS properties and mappings

// Comment the following line to disable the sprite generation or if you are running the application with a Java 1.4
jawr.css.bundle.factory.global.preprocessors='smartsprites'

jawr.css.skin.default.root.dirs='/css/themes/oceanBlue/en_US'
jawr.csslinks.flavor='html'

jawr.css.bundle.common.id='/css/common.css'
jawr.css.bundle.common.mappings='/js/yui/fonts/fonts-min.css,skin:/css/themes/oceanBlue/en_US/theme.css,skin:/css/themes/oceanBlue/en_US/tabview.css,skin:/css/themes/oceanBlue/en_US/container.css'
jawr.css.bundle.common.filepostprocessors='csspathrewriter'

jawr.css.bundle.specific.id='/css/specific.css'
jawr.css.bundle.specific.mappings='jar:fwk/css/temp.css,/css/one.css'
jawr.css.bundle.specific.filepostprocessors='none'
jawr.css.bundle.specific.bundlepostprocessors='cssminify,base64ImageEncoder'

jawr.css.classpath.handle.image=true
jawr.image.hash.algorithm='MD5'
