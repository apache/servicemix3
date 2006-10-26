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
<title>Endpoint ${requestContext.endpoint.name}</title>
</head>
<body>

<h2>Endpoint ${requestContext.endpoint.name}</h2>

<fieldset>
  <legend>Endpoint detail</legend>
  <table>
    <tbody>
      <tr>
        <th>Name</td>
        <td><div class="field">${requestContext.endpoint.name}</div></td>
      </tr>
      <tr>
        <th>Type</td>
        <td><div class="field">${requestContext.endpoint.type}</div></td>
      </tr>
      <tr>
        <th>Component</td>
        <td><div class="field"><a href="component.jsp?name=${requestContext.endpoint.component.name}">${requestContext.endpoint.component.name}</a></div></td>
      </tr>
      <tr>
        <th>Interfaces</th>
        <td><div class="field">
          <c:forEach items="${requestContext.endpoint.interfaces}" var="row">
            ${row}
          </c:forEach>
        </div></td>
      </tr>
    </tbody>
  </table>
</fieldset>

<fieldset>
  <legend> WSDL &nbsp; 
    <small>
       <c:if test="${requestContext.endpoint.showWsdl}">(<a href="endpoint.jsp?objectName=${requestContext.endpoint.objectName}&amp;showWsdl=false">hide</a>)</c:if>
       <c:if test="${!requestContext.endpoint.showWsdl}">(<a href="endpoint.jsp?objectName=${requestContext.endpoint.objectName}&amp;showWsdl=true">show</a>)</c:if>
    </small>
  </legend>
  <c:if test="${requestContext.endpoint.showWsdl}">
    <div id="error" style="height: 25em;"><pre>${requestContext.endpoint.wsdl}</pre></div>
  </c:if>
</fieldset>

</body>
</html>
	
