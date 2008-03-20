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
<title>Component ${requestContext.component.name}</title>
</head>
<body>

<h2>Component ${requestContext.component.name}</h2>

<fieldset>
  <legend>Component detail</legend>
  <table>
    <tbody>
      <tr>
        <th>Name</th>
        <td><div class="field">${requestContext.component.name}</div></td>
      </tr>
      <tr>
        <th>Type</th>
        <td><div class="field">${requestContext.component.type}</div></td>
      </tr>
      <tr>
        <th>Status</th>
        <td><div class="field">${requestContext.component.status}</div></td>
      </tr>
      <tr>
        <th>Action</th>
        <td>
          <table class="align"><tr>
          <c:if test="${requestContext.component.status != 'Started'}">
            <td class="align"><form method="post" action="startComponent.action?view=redirect:/component.jsp?name=${requestContext.component.name}"><input type="hidden" name="name" value="${requestContext.component.name}"/><input type="submit" value="Start"/></form></td>
          </c:if> 
          <c:if test="${requestContext.component.status == 'Started'}">
            <td class="align"><form method="post" action="stopComponent.action?view=redirect:/component.jsp?name=${requestContext.component.name}"><input type="hidden" name="name" value="${requestContext.component.name}"/><input type="submit" value="Stop"/></form></td>
          </c:if> 
          <c:if test="${requestContext.component.status == 'Stopped'}">
            <td class="align"><form method="post" action="shutdownComponent.action?view=redirect:/component.jsp?name=${requestContext.component.name}"><input type="hidden" name="name" value="${requestContext.component.name}"/><input type="submit" value="Shutdown"/></form></td>
          </c:if> 
          <c:if test="${requestContext.component.status == 'Shutdown'}">
            <td class="align"><form method="post" action="uninstallComponent.action?name=${requestContext.component.name}"><input type="hidden" name="name" value="${requestContext.component.name}"/><input type="submit" value="Uninstall"/></form></td> 
          </c:if> 
          </tr></table>
        </td>
      </tr>
    </tbody>
  </table>
</fieldset>

<fieldset>
  <legend>Service Units</legend>
  <table id="componentServiceUnits" class="sortable autostripe">
    <thead>
        <tr>
            <th>Name</th>
            <th>Service Assembly</th>
        </tr>
    </thead>
    <tbody>
      <c:forEach items="${requestContext.component.serviceUnits}" var="row">
        <tr>
          <td>
            <a href="service-unit.jsp?name=${row.name}">${row.name}</a>
          </td>
          <td>
            <a href="service-assembly.jsp?name=${row.serviceAssembly.name}">${row.serviceAssembly.name}</a>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</fieldset>

<fieldset>
  <legend>Endpoints</legend>
  <table id="componentEndpoints" class="sortable autostripe">
    <thead>
        <tr>
            <th>Name</th>
        </tr>
    </thead>
    <tbody>
      <c:forEach items="${requestContext.component.endpoints}" var="row">
        <tr>
          <td>
            <a href="endpoint.jsp?objectName=${row.objectName}">${row.name}</a>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</fieldset>

</body>
</html>
	
