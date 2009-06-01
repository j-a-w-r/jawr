<%@ taglib uri="http://jawr.net/tags" prefix="jwr" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>    
 <jwr:style src="/bundles/one.css" media="all" useRandomParam="false" />
 <jwr:style src="/bundles/two.css" media="print"  useRandomParam="false" />
 <jwr:script src="/bundles/global.js"/>
	<script>
		jawr.index();
	</script>
</head>
<body>
<h2>Hello World!</h2>
</body>
</html>
