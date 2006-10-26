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
<title>Audit</title>
</head>
<body>

<h2>Audit</h2>

<table class="autostripe">
  <thead>
    <tr>
      <th>Id</th>
      <th>Date</th>
      <th>Status</th>
      <th>MEP</th>
    </tr>
  </thead>
  <tbody>
    <c:forEach var="exchange" items="${requestContext.auditor.exchanges}">
      <tr>
        <td>${exchange.id}</td>
        <td>${exchange.date}</td>
        <td>${exchange.status}</td>
        <td>${exchange.mep}</td>
      </tr>
    </c:forEach>
  </tbody>
  <tfooter>
    <tr>
      <td colspan="4">
        <c:forEach var="i" begin="0" end="${requestContext.auditor.count / 10}">
          <c:choose>
            <c:when test="${i == requestContext.auditor.page}">
              ${i}
            </c:when>
            <c:otherwise>
              <a href="audit.jsp?page=${i}">${i}</a>
            </c:otherwise>
          </c:choose>
          &nbsp;
        </c:forEach>
      </td>
    </tr>
  </tfooter>
</table>

</body>
</html>
	
