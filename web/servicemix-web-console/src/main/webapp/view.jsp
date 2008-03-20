<%--
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
--%>
<html>
<head>
<title>View</title>
</head>
<body>

<%
Throwable dotException = null;
try {
    org.apache.servicemix.web.view.Dot.initialize();
} catch (Throwable t) {
    dotException = t;
}
if (dotException != null) {
%>
<br></br>
Unable to run dot: <%=dotException.getMessage()%>
<br></br>
Check that the dot executable is available in the path or install it from <a href="http://www.graphviz.org/">http://www.graphviz.org/</a>.
<%
} else {
%>

<h2>View</h2>

<fieldset style="height:25em">
  <legend>Endpoints &nbsp; <small>(<a href="dot-endpoints.svg">full page</a>)</small></legend>
  <object width="100%" height="90%" type="image/svg+xml" codetype="image/svg+xml" data="dot-endpoints.svg">
    <span style="background-color:#FFFF20;padding-top:1pt;padding-bottom:1pt;">
      This browser can't display the SVG file <a href="dot-endpoints.svg">dot-endpoints.svg</a>.
      <br/>
      Please use newer versions of <a href="http://www.mozilla.com/firefox/">Firefox</a> or install 
      the <a href="http://www.adobe.com/svg/viewer/install/main.html">Adobe SVG Viewer</a> for your
      browser.
    </span>
  </object>
</fieldset>

<fieldset style="height:25em">
  <legend>Flow &nbsp; <small>(<a href="dot-flow.svg">full page</a>)</small></legend>
  <object width="100%" height="90%" type="image/svg+xml" codetype="image/svg+xml" data="dot-flow.svg">
    <span style="background-color:#FFFF20;padding-top:1pt;padding-bottom:1pt;">
      This browser can't display the SVG file <a href="dot-flow.svg">dot-flow.svg</a>.
      <br/>
      Please use newer versions of <a href="http://www.mozilla.com/firefox/">Firefox</a> or install 
      the <a href="http://www.adobe.com/svg/viewer/install/main.html">Adobe SVG Viewer</a> for your
      browser.
    </span>
  </object>
</fieldset>

<%
}
%>
</body>
</html>
	
