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
<title>Service Assembly</title>
</head>
<body>

<h2>Service Assembly ${requestContext.serviceAssembly.name}</h2>

<fieldset>
  <legend>Service Assembly detail</legend>
  <table>
    <tbody>
      <tr>
        <th>Name</th>
        <td><div class="field">${requestContext.serviceAssembly.name}</div></td>
      </tr>
      <tr>
        <th>Description</th>
        <td><div class="field">${requestContext.serviceAssembly.description}</div></td>
      </tr>
      <tr>
        <th>Status</th>
        <td><div class="field">${requestContext.serviceAssembly.status}</div></td>
      </tr>
      <tr>
        <th>Actions</th>
        <td>
          <table class="align"><tr>
          <c:if test="${requestContext.serviceAssembly.status != 'Started'}">
            <td class="align"><form method="post" action="startServiceAssembly.action?view=redirect:/service-assembly.jsp?name=${requestContext.serviceAssembly.name}"><input type="hidden" name="name" value="${requestContext.serviceAssembly.name}"/><input type="submit" value="Start"/></form></td>
          </c:if> 
          <c:if test="${requestContext.serviceAssembly.status == 'Started'}">
            <td class="align"><form method="post" action="stopServiceAssembly.action?view=redirect:/service-assembly.jsp?name=${requestContext.serviceAssembly.name}"><input type="hidden" name="name" value="${requestContext.serviceAssembly.name}"/><input type="submit" value="Stop"/></form></td>
          </c:if> 
          <c:if test="${requestContext.serviceAssembly.status == 'Stopped'}">
            <td class="align"><form method="post" action="shutdownServiceAssembly.action?view=redirect:/service-assembly.jsp?name=${requestContext.serviceAssembly.name}"><input type="hidden" name="name" value="${requestContext.serviceAssembly.name}"/><input type="submit" value="Shutdown"/></form></td>
          </c:if> 
          <c:if test="${requestContext.serviceAssembly.status == 'Shutdown'}">
            <td class="align"><form method="post" action="undeployServiceAssembly.action?name=${requestContext.serviceAssembly.name}"><input type="hidden" name="name" value="${requestContext.serviceAssembly.name}"/><input type="submit" value="Undeploy"/></form></td> 
          </c:if> 
          </tr></table>
        </td>
      </tr>
    </tbody>
  </table>
</fieldset>

<fieldset>
  <legend>Service Units</legend>
  <table id="serviceAssemblyServiceUnits" class="sortable autostripe">
    <thead>
        <tr>
            <th>Name</th>
            <th>Component</th>
        </tr>
    </thead>
    <tbody>
      <c:forEach items="${requestContext.serviceAssembly.serviceUnits}" var="row">
        <tr>
          <td>
            <a href="service-unit.jsp?name=${row.name}">${row.name}</a>
          </td>
          <td>
            <a href="component.jsp?name=${row.component.name}">${row.component.name}</a>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</fieldset>

</body>
</html>
	
