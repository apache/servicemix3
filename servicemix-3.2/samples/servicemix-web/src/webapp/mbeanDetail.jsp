<!--

    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
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
