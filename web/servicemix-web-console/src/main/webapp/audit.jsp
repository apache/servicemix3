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

<fieldset>
  <legend>Service</legend>
  <table class="align"><tr>
  <c:if test="${requestContext.auditor.status == 'Started'}">
    <td class="align"><form method="post" action="stopAuditor.action"><input type="submit" value="Stop"/></form></td>
  </c:if> 
  <c:if test="${requestContext.auditor.status == 'Stopped'}">
    <td class="align"><form method="post" action="startAuditor.action"><input type="submit" value="Start"/></form></td>
  </c:if> 
  </tr></table>
</fieldset>

<fieldset>
  <legend>Exchanges</legend>
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
          <td><a href="audit.jsp?page=${requestContext.auditor.page}&amp;exchangeId=${exchange.id}">${exchange.id}</a></td>
          <td>${exchange.date}</td>
          <td>${exchange.status}</td>
          <td>${exchange.mep}</td>
        </tr>
      </c:forEach>
    </tbody>
    <tfooter>
      <tr>
        <td colspan="4">
          <c:forEach var="i" begin="0" end="${(requestContext.auditor.count - 1) / 10}">
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
</fieldset>

<c:if test="${requestContext.auditor.selectedExchange != null}">
<fieldset>
  <legend>Exchange details</legend>
  <table>
    <tbody>
      <tr>
        <th>Id</th>
        <td>${requestContext.auditor.selectedExchange.id}</td>
      </tr>
      <tr>
        <th>Date</th>
        <td>${requestContext.auditor.selectedExchange.date}</td>
      </tr>
      <tr>
        <th>Status</th>
        <td>${requestContext.auditor.selectedExchange.status}</td>
      </tr>
      <tr>
        <th>Mep</th>
        <td>${requestContext.auditor.selectedExchange.mep}</td>
      </tr>
      <tr>
        <th>Properties</th>
        <td><pre>${requestContext.auditor.selectedExchange.properties}</pre></td>
      </tr>
      <tr>
        <th>Endpoint</th>
        <td><pre>${requestContext.auditor.selectedExchange.endpoint}</pre></td>
      </tr>
    </tbody>
  </table>
  <c:if test="${requestContext.auditor.selectedExchange.in != null}">
    <fieldset>
      <legend>In</legend>
      <table>
        <tbody>
        <tr>
          <th>Properties</th>
          <td><pre>${requestContext.auditor.selectedExchange.in.properties}</pre></td>
        </tr>
        <tr>
          <th>Content</th>
          <td><pre>${requestContext.auditor.selectedExchange.in.content}</pre></td>
        </tr>
        </tbody>
      </table>
    </fieldset>
  </c:if>
  <c:if test="${requestContext.auditor.selectedExchange.out != null}">
    <fieldset>
      <legend>Out</legend>
      <table>
        <tbody>
        <tr>
          <th>Properties</th>
          <td><pre>${requestContext.auditor.selectedExchange.out.properties}</pre></td>
        </tr>
        <tr>
          <th>Content</th>
          <td><pre>${requestContext.auditor.selectedExchange.out.content}</pre></td>
        </tr>
        </tbody>
      </table>
    </fieldset>
  </c:if>
  <c:if test="${requestContext.auditor.selectedExchange.fault != null}">
    <fieldset>
      <legend>Fault</legend>
      <table>
        <tbody>
        <tr>
          <th>Properties</th>
          <td><pre>${requestContext.auditor.selectedExchange.fault.properties}</pre></td>
        </tr>
        <tr>
          <th>Content</th>
          <td><pre>${requestContext.auditor.selectedExchange.fault.content}</pre></td>
        </tr>
        </tbody>
      </table>
    </fieldset>
  </c:if>
</fieldset>
</c:if>

</body>
</html>
	
