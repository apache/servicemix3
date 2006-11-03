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
<title>Service Unit</title>
</head>
<body>

<h2>Service Unit ${requestContext.serviceUnit.name}</h2>

<fieldset>
  <legend>Service Unit detail</legend>
  <table>
    <tbody>
      <tr>
        <th>Name</th>
        <td><div class="field">${requestContext.serviceUnit.name}</div></td>
      </tr>
      <tr>
        <th>Component</th>
        <td><div class="field"><a href="component.jsp?name=${requestContext.serviceUnit.component.name}">${requestContext.serviceUnit.component.name}</a></div></td>
      </tr>
      <tr>
        <th>Service Assembly</th>
        <td><div class="field"><a href="service-assembly.jsp?name=${requestContext.serviceUnit.serviceAssembly.name}">${requestContext.serviceUnit.serviceAssembly.name}</a></div></td>
      </tr>
    </tbody>
  </table>
</fieldset>

</body>
</html>
	
