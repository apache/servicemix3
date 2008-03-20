<!--

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

-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<table width="100%">
  <tr>
    <td>
      <a href="<portlet:renderURL><portlet:param name="action" value="refresh"/></portlet:renderURL>">Refresh</a> 
    </td>   
    <td>
      <c:forEach var="i" begin="0" end="${count / 10}">
        <c:choose>
          <c:when test="${i == page}">
            ${i}
          </c:when>
          <c:otherwise>
            <a href="<portlet:actionURL><portlet:param name="view" value="${i}"/></portlet:actionURL>">${i}</a>
          </c:otherwise>
        </c:choose>
        &nbsp;
      </c:forEach>
    </td>  
  </tr>
  <tr>
    <td class="DarkBackground" width="100%" colspan="4" align="center">
       Message exchanges (${count})
    </td>
  </tr>
  <tr>
    <th>Id</th>
    <th>Date</th>
    <th>Status</th>
    <th>MEP</th>
  </tr>
  <c:forEach var="exchange" items="${exchanges}">
    <tr>
      <td>${exchange.id}</td>
      <td>${exchange.date}</td>
      <td>${exchange.status}</td>
      <td>${exchange.mep}</td>
    </tr>
  </c:forEach>
</table>
