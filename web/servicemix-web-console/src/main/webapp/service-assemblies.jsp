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
<title>Service Assemblies</title>
</head>
<body>

<h2>Deploy service assembly</h2>
<form method="post" action="deployServiceAssembly.action" enctype="multipart/form-data">
  <input type="file" name="file" />
  <input type="submit" value="Deploy" />
</form> 

<h2>Service Assemblies</h2>

<c:choose>
<c:when test="${empty requestContext.serviceAssemblies}">
	No service assemblies found.
</c:when>
<c:otherwise>
<table id="serviceAssemblies" class="sortable autostripe">
  <thead>
    <tr>
      <th>Name</th>
      <th>Status</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    <c:forEach items="${requestContext.serviceAssemblies}" var="row">
      <tr>
        <td><a href="service-assembly.jsp?name=${row.name}">${row.name}</a></td>
        <td>${row.status}</td>
        <td>
          <table class="align"><tr>
          <c:if test="${row.status != 'Started'}">
            <td class="align"><form method="post" action="startServiceAssembly.action"><input type="hidden" name="name" value="${row.name}"/><input type="submit" value="Start"/></form></td>
          </c:if> 
          <c:if test="${row.status == 'Started'}">
            <td class="align"><form method="post" action="stopServiceAssembly.action"><input type="hidden" name="name" value="${row.name}"/><input type="submit" value="Stop"/></form></td>
          </c:if> 
          <c:if test="${row.status == 'Stopped'}">
            <td class="align"><form method="post" action="shutdownServiceAssembly.action"><input type="hidden" name="name" value="${row.name}"/><input type="submit" value="Shutdown"/></form></td>
          </c:if> 
          <c:if test="${row.status == 'Shutdown'}">
            <td class="align"><form method="post" action="undeployServiceAssembly.action"><input type="hidden" name="name" value="${row.name}"/><input type="submit" value="Undeploy"/></form></td> 
          </c:if> 
          </tr></table>
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>
</c:otherwise>
</c:choose>

</body>
</html>
	
