


jawr.debug.on=false;
jawr.gzip.on=false
jawr.config.reload.interval='10'
jawr.gzip.ie6.on=false
jawr.charset.name='UTF-8'
jawr.js.use.cache=true;
jawr.js.bundle.basedir='/js'

jawr.js.bundle.names='yui'

jawr.js.bundle.yui.id='/bundles/yui.js'
jawr.js.bundle.yui.mappings='/js/**'

jawr.css.use.cache=true;
jawr.css.bundle.basedir='/js'

jawr.css.bundle.names='all'
jawr.css.bundle.all.mappings='/css/**'
jawr.css.bundle.all.id='/bundles/all.css'


grails.mime.file.extensions = false;
	
log4j {
    appender.stdout = "org.apache.log4j.ConsoleAppender"
    appender.'stdout.layout'="org.apache.log4j.PatternLayout"
    
    
    rootLogger="debug,stdout"
    logger {
        jawr="debug,stdout"
        org="fatal,stdout"
    }
}

