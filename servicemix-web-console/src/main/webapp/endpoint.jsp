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
        <td>Name</td>
        <td><div class="field">${requestContext.endpoint.name}</div></td>
      </tr>
      <tr>
        <td>Type</td>
        <td><div class="field">${requestContext.endpoint.type}</div></td>
      </tr>
      <tr>
        <td>Component</td>
        <td><div class="field"><a href="component.jsp?name=${requestContext.endpoint.component.name}">${requestContext.endpoint.component.name}</a></div></td>
      </tr>
    </tbody>
  </table>
</fieldset>

</body>
</html>
	
