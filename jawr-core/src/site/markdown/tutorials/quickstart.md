Getting started with Jawr
-------------------------

### Download and install

[Download Jawr from maven repository 
area](http://mvnrepository.com/artifact/net.jawr/jawr-core)
and place it within the classpath of your application (typically, under
WEB-INF/lib). Add slf4j implementation jar to the path as well.

Alternatively, if you are using Maven, add a dependency to Jawr in
your POM file:


            <dependency>
              <groupId>net.jawr</groupId>
              <artifactId>jawr-core</artifactId>
              <version>[3,]</version>
            </dependency>


### Develop your scripts and stylesheets

For the quick setup, it is assumed that you have the following
directories at the root of your application: **/js**, **/js/lib** and
**/css**. Of course these may contain more subdirs. The sample
configuration will create a bundle containing everything under
**/js/lib**, named **/bundles/lib.js**. The remaining .js files at /js
and subdirs will be served separately. As for the CSS files, a single
all-in-one bundle named **/bundles/all.css** will be used.



### Configure instances of the Jawr Servlet

Add **net.jawr.web.servlet.JawrServlet** to your web.xml descriptor. You
will need an instance for handling Javascript, and another one for CSS
stylesheets. For instance:


            <servlet>
                    <servlet-name>JavascriptServlet</servlet-name>
                    <servlet-class>net.jawr.web.servlet.JawrServlet</servlet-class>
                    
                    <!-- Location in classpath of the config file -->
                    <init-param>
                            <param-name>configLocation</param-name>
                            <param-value>/jawr.properties</param-value>
                    </init-param>
                    <load-on-startup>1</load-on-startup>
            </servlet>
            
            <servlet>
                    <servlet-name>CSSServlet</servlet-name>
                    <servlet-class>net.jawr.web.servlet.JawrServlet</servlet-class>
                    
                    <!-- Location in classpath of the config file -->
                    <init-param>
                            <param-name>configLocation</param-name>
                            <param-value>/jawr.properties</param-value>
                    </init-param>
                    <init-param>
                            <param-name>type</param-name>
                            <param-value>css</param-value>
                    </init-param>
                    <load-on-startup>1</load-on-startup>
            </servlet>
            
            ...
            
            <servlet-mapping>
                    <servlet-name>JavascriptServlet</servlet-name>
                    <url-pattern>*.js</url-pattern>
            </servlet-mapping> 
            
            <servlet-mapping>
                    <servlet-name>CSSServlet</servlet-name>
                    <url-pattern>*.css</url-pattern>
            </servlet-mapping> 


This is all the configuration required to get the Jawr servlet up and
running.


### Create the Jawr configuration file

Now you need to create the jawr.properties descriptor file. This file
should be in the application classpath in runtime, for example at the
WEB-INF/classes dir. Add this content to the file:


    # Common properties
    jawr.debug.on=false
    jawr.gzip.on=true
    jawr.gzip.ie6.on=false
    jawr.charset.name=UTF-8

    # Javascript properties and mappings
    jawr.js.bundle.basedir=/js

    # All files within /js/lib will be together in a bundle. 
    # The remaining scripts will be served separately. 
    jawr.js.bundle.lib.id=/bundles/lib.js
    jawr.js.bundle.lib.mappings=/js/lib/**

    # The /bundles/lib.js bundle is global 
    # (always imported before other scripts to pages using the taglib)
    jawr.js.bundle.lib.global=true


    # CSS properties and mappings
    jawr.css.bundle.basedir=/css

    # CSS files will be all bundled together automatically
    jawr.css.factory.use.singlebundle=true
    jawr.css.factory.singlebundle.bundlename=/bundles/all.css


### Create the Log4j configuration file

In order to see output log messages, you need to create a log4j
configuration file. For this setup, we will use a properties file which
should be in the application classpath (alongside the jawr.properties
file). The file should be named log4j.properties and the contents be as
follows:


    # Set root logger level to DEBUG and its only appender to A1.
    log4j.rootLogger=INFO, A1

    # A1 is set to be a ConsoleAppender.
    log4j.appender.A1=org.apache.log4j.ConsoleAppender

    # A1 uses PatternLayout.
    log4j.appender.A1.layout=org.apache.log4j.PatternLayout
    log4j.appender.A1.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n

    # Print messages up to level INFO
    log4j.logger.net.jawr=INFO

Note that the log level is set to INFO. The DEBUG level would ouput
verbose log messages, and you should use it only if you experience
problems with your configuration. This file will setup log4j to log only
to the system console. Please refer to the log4j documentation for
different setup options.

### Test the bundles

Create a test javascript file at the **/js** directory, named
**test.js**. Then write JSP page and add the following content:

    <%@ taglib uri="http://jawr.net/tags" prefix="jwr" %>
    <%@ page contentType="text/html;charset=UTF-8" %>
    <html>
    <head>
    <jwr:style src="/bundles/all.css" />
    <jwr:script src="/js/test.js"/> 
    </head>
    <body>
            ...
    </body>
    </html>


Deploy your application to a server and open the JSP you created. The
page should contain a link to css004/bundles/all.css, a script tag
pointing at lib001/bundles/lib.js and another one pointing at
003/js/test.js. If you visited the page with Firefox, the URl might be
prefixed by 'gzip\_'. If you open these files from the browser, you will
find they have been minified.

Finally, change the jawr.debug.on property in the descriptor file to
**true** and redeploy the application. Visit the page again and you will
see how each file is imported separately. If you have used an exploded
deployment directory instead of a WAR archived file, you can now change
any of the script or css files and see the changes immediately by
refreshing the page.
