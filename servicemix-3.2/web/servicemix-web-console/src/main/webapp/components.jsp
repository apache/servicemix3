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
<title>Components</title>
</head>
<body>

<h2>Install component</h2>
<form method="post" action="installComponent.action" enctype="multipart/form-data">
  <input type="file" name="file" />
  <input type="submit" value="Install" />
</form> 

<h2>Components</h2>

<table id="components" class="sortable autostripe">
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Status</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    <c:forEach items="${requestContext.components}" var="row">
      <tr>
        <td><a href="component.jsp?name=${row.name}">${row.name}</a></td>
        <td>
          <c:if test="${row.type == 'service-engine'}"><img src="images/comp-type-se.gif" alt="Service Engine" /></c:if>
          <c:if test="${row.type == 'binding-component'}"><img src="images/comp-type-bc.gif" alt="Binding Component" /></c:if>
          <c:if test="${row.type == 'pojo'}"><img src="images/comp-type-pojo.gif" alt="POJO Component" /></c:if>
        </td>
        <td>${row.status}</td>
        <td>
          <table class="align"><tr>
          <c:if test="${row.status != 'Started'}">
            <td class="align"><form method="post" action="startComponent.action"><input type="hidden" name="name" value="${row.name}"/><input type="submit" value="Start"/></form></td>
          </c:if> 
          <c:if test="${row.status == 'Started'}">
            <td class="align"><form method="post" action="stopComponent.action"><input type="hidden" name="name" value="${row.name}"/><input type="submit" value="Stop"/></form></td>
          </c:if> 
          <c:if test="${row.status == 'Stopped'}">
            <td class="align"><form method="post" action="shutdownComponent.action"><input type="hidden" name="name" value="${row.name}"/><input type="submit" value="Shutdown"/></form></td>
          </c:if> 
          <c:if test="${row.status == 'Shutdown'}">
            <td class="align"><form method="post" action="uninstallComponent.action"><input type="hidden" name="name" value="${row.name}"/><input type="submit" value="Uninstall"/></form></td> 
          </c:if> 
          </tr></table>
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>



</body>
</html>
	
