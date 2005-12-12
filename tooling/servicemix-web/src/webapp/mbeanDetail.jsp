<%@page contentType="text/html; charset=ISO-8859-1" %>
<html>
<head>
<title>MBean Details</title>
<link rel="stylesheet" href="style.css" type="text/css">
</head>

<body>

<h1>MBean Details</h1>

<h2>MBean Attributes</h2>

<jsp:include page="jmx/">
 <jsp:param name="view" value="attributes"/>
 <jsp:param name="style" value="html"/>
</jsp:include>

<h2>ObjectName Properties</h2>

<jsp:include page="jmx/">
 <jsp:param name="view" value="properties"/>
 <jsp:param name="style" value="html"/>
</jsp:include>

</body>
</html>
