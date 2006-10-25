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
<title>Shared Libraries</title>
</head>
<body>

<h2>Install shared library</h2>
<form method="post" action="installSharedLibrary.action" enctype="multipart/form-data">
  <input type="file" name="file" />
  <input type="submit" value="Install" />
</form> 

<h2>Shared Libraries</h2>

<table id="sharedLibraries" class="sortable autostripe">
  <thead>
    <tr>
      <th>Name</th>
    </tr>
  </thead>
  <tbody>
    <c:forEach items="${requestContext.sharedLibraries}" var="row">
      <tr>
        <td><a href="shared-library.jsp?name=${row.name}">${row.name}</a></td>
      </tr>
    </c:forEach>
  </tbody>
</table>

</body>
</html>
	
