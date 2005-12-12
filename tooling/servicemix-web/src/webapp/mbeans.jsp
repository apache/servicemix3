<%@page contentType="text/html; charset=ISO-8859-1" %>
<html>
<head>
<title>MBeans</title>
<link rel="stylesheet" href="style.css" type="text/css">
<link rel="stylesheet" href="mktree.css" type="text/css">
<base target="detail">
</head>

<body>
<script src="mktree.js" language="JavaScript"></script>

<h1>MBeans</h1>

<ul class="mktree">
<jsp:include page="jmx/">
 <jsp:param name="style" value="html"/>
</jsp:include>

</ul>

</body>
</html>
