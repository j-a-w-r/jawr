<html>
    <head>
        <title>Welcome to Jawr - Grails sample</title>
		<meta name="layout" content="main" />
		
		 <jawr:style src="/css/common.css" displayAlternate="true" media="all" />
		 <jawr:style src="/css/specific.css" media="all" />
		  
		 <jawr:script src="/js/common.js" />
		 <jawr:script src="/js/bundle/msg.js" />
		 
		 <style type="text/css" media="screen">

			#nav {
				margin-top:20px;
				margin-left:30px;
				width:228px;
				float:left;

			}
			.homePagePanel * {
				margin:0px;
			}
			.homePagePanel .panelBody ul {
				list-style-type:none;
				margin-bottom:10px;
			}
			.homePagePanel .panelBody h1 {
				text-transform:uppercase;
				font-size:1.1em;
				margin-bottom:10px;
			}
			.homePagePanel .panelBody {
			    background: url(images/leftnav_midstretch.png) repeat-y top;
				margin:0px;
				padding:15px;
			}
			.homePagePanel .panelBtm {
			    background: url(images/leftnav_btm.png) no-repeat top;
				height:20px;
				margin:0px;
			}

			.homePagePanel .panelTop {
			    background: url(images/leftnav_top.png) no-repeat top;
				height:11px;
				margin:0px;
			}
			h2 {
				margin-top:15px;
				margin-bottom:15px;
				font-size:1.2em;
			}
			#pageBody {
				margin-left:280px;
				margin-right:20px;
			}
		</style>
    </head>
    <body class="yui-skin-sam">
    	
    	<div id="panelMessage">
			<div class="hd">Message</div>
			<div class="bd">This is a Panel that was marked up in the document.</div>
		</div>
		<div id="nav">
			<div class="homePagePanel">
				<div class="panelTop">

				</div>
				<div class="panelBody">
					<h1>Application Status</h1>
					<ul>
						<li>App version: <g:meta name="app.version"></g:meta></li>
						<li>Grails version: <g:meta name="app.grails.version"></g:meta></li>
						<li>JVM version: ${System.getProperty('java.version')}</li>
						<li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
						<li>Domains: ${grailsApplication.domainClasses.size()}</li>
						<li>Services: ${grailsApplication.serviceClasses.size()}</li>
						<li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
					</ul>
					<h1>Installed Plugins</h1>
					<ul>
						<g:set var="pluginManager"
						       value="${applicationContext.getBean('pluginManager')}"></g:set>

						<g:each var="plugin" in="${pluginManager.allPlugins}">
							<li>${plugin.name} - ${plugin.version}</li>
						</g:each>

					</ul>
				</div>
				<div class="panelBtm">
				</div>
			</div>
		</div>
		<div id="pageBody">
	        <h1>Welcome to Jawr Grails sample application</h1>
	        <p>Congratulations, you have successfully started your first Jawr - Grails application! <br/>
	        At the moment This page demonstrates some Jawr features.<br/>

	        <div class="header">&nbsp;</div>

		<div class="exampleIntro">
			<p></p>
		</div>
		
		
		
		<div id="tabView" class="yui-navset">
		    <ul class="yui-nav">
		        <li class="selected"><a href="#tab1"><em>Internationalization</em></a></li>
		        <li><a href="#tab2"><em>Skin</em></a></li>
		        <li><a href="#tab3"><em>Generated content</em></a></li>
		        <li><a href="#tab4"><em>Generate sprite image</em></a></li>
		        <li><a href="#tab5"><em>Generate base64 image</em></a></li>
		    </ul>            
		    <div class="yui-content">
		        <div id="tab1">
		        	<p>Jawr allows you to define bundles which will change depending on the user locale </p>
		        	<button id="localeMsgButton">Display localized message</button>
		        </div>
		        <div id="tab2">
		        	<p>Jawr provides a way to define skin for your CSS bundle<br/>
		        	The skin can also vary depending of the user locale. You can check by changing the locale of your browser to French, Spain or US.
		        	</p>
		        	<form>
						Select your skin :<select name="theme" id="themeSwitcher" onchange="JAWR.skin.switchToStyle(this.value);">
							<option value="">&nbsp;</option>
							<option value="oceanBlue">Ocean Blue</option>
							<option value="greyStorm">Grey Storm</option>
						</select>
						</form>
		        </div>
		        <div id="tab3">
		        	<p>Jawr allows you to define generated content</p>
		        	<div class="mandatory">
						For this element, the style and the image associated are retrieved directly from the classpath.
					</div>
					<div style="height: 20px">
						<jawr:img src="img:/cog.png" /> This HTML image use a generated path which force the caching for the browser.  
					</div>
					<div>
						<span class="calendar">The CSS image is retrieved from the webapp, which is a classic case.</span>
					</div>
					<div>
						<span class="clock">The CSS image is retrieved from the classpath. The CSS used here is defined in the webapp under the "css" folder.<br>
						This mean that you could reference a CSS image in the classpath from a standard CSS define in a bundle.</span>
					</div>
					<div style="height: 20px">
						<jawr:image value="temp" src="/images/cog.png"/> This input image use a generated path which force the caching for the browser. 
					</div>
		        </div>
		        <div id="tab4">
		        	<p>Jawr allows you to define CSS sprite, which will be generated during the bundle processing using <a href="http://csssprites.org/">Smartsprites</a>.<br>
		        	You will find below images where the references have been replaced by a sprite which has been dynamically generated.
		        	This features requires to use Java 5 and the Smartsprites library.
		        	</p>
		        	<div class="mandatory">
						The image defined here is part of a generated sprite.
					</div>
					<div class="clock">
						  This one also
					</div>
		        </div>
		        <div id="tab5">
		        	<p>Jawr allows you to generated base64 image in your CSS and your HTML images.<br/>
		        	The major advantage of using the base64 image is that the browser don't need to make a request to the server for this image because it is embedded in the HTML page.
		        	The base64 encoded image can be used directly for all major browser, except IE versions before 8.
		        	On these browsers, for CSS images, we use MHTML.
		        	And for HTML images, instead of generating the base64 encoded data, we reference it by their normal URLs.  
		        	</p>
		        	<div class="base64Clock">
						The image defined here is part of a generated sprite.
					</div>
					<div style="height: 20px">
						  <jawr:img src="/images/cog.png" base64="true" /> Jawr has generated the base64 encoded data for this HTML image, except on IE versions before 8.
					</div>
		        </div>
		    </div>
		</div>
	</div>
<script>
(function() {
    
    YAHOO.namespace("jawr.container");

    function init() {
		var tabView = new YAHOO.widget.TabView('tabView');

		// Instantiate a Panel from markup
		YAHOO.jawr.container.panelMsg = new YAHOO.widget.Panel("panelMessage", { width:"320px", visible:false, constraintoviewport:true, modal : true, fixedcenter : true } );
		YAHOO.jawr.container.panelMsg.render();

		YAHOO.util.Event.addListener("localeMsgButton", "click", displayLocaleMsg);

		// Initialize the theme swicther select box
		var themeSwitcher = document.getElementById("themeSwitcher");
		themeSwitcher.value = JAWR.skin.getCurrentSkin();
	}

    function displayLocaleMsg(){

    	YAHOO.jawr.container.panelMsg.setBody(messages.ui.msg.hello.world());
    	YAHOO.jawr.container.panelMsg.show();
    }
	
	YAHOO.util.Event.addListener(window, "load", init);
})();
</script>
    </body>
</html>