<html>
    <head>
        <title><g:layoutTitle default="Grails" /></title>
        <link rel="shortcut icon" href="<jawr:imagePath src="/images/favicon.ico" />" type="image/x-icon" />
        <g:layoutHead />
    	
    </head>
    <body class="yui-skin-sam">
    	<div id="spinner" class="spinner" style="display:none;">
            <img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
        </div>
        <div id="grailsLogo" class="logo"><a href="https://jawr.dev.java.net/"><jawr:img src="/images/logo-small.png" alt="Jawr" border="0" /></a>&nbsp;&nbsp;&nbsp;<a href="http://grails.org"><jawr:img src="/images/grails_logo.png" alt="Grails" border="0" /></a></div>
        <g:layoutBody />
    </body>
</html>