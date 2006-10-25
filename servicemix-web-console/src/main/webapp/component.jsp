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
          <!--
          <form method="post" action=${componentAction}>
            <input type="hidden" name="componentObjectName" value="${model.component.objectName}" />
            <input type="hidden" name="componentName" value="${model.component.name}" />
            <input type="hidden" name="command" value="executecomponentaction" />
            <web:actions actions="${model.component.actions}" bundle="jbicomponents" />
          </form>
          -->
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
            <a href="endpoint.jsp?name=${row.name}">${row.name}</a>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</fieldset>

</body>
</html>
	
