<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<table width="100%">
  <tr>
    <td>
      <a href="<portlet:renderURL><portlet:param name="action" value="refresh"/></portlet:renderURL>">Refresh</a> 
    </td>     
  </tr>
  <tr>
    <td class="DarkBackground" width="100%" colspan="4" align="center">JBI Container</td>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap>State</td>
    <td class="LightBackground" width="80%" colspan="3">${state}</td>
  </tr>
  <tr>
    <td class="MediumBackground">Info</td>
    <td class="MediumBackground" colspan="3">${info}</td>
  </tr>
  <tr/>
  <tr>
    <td class="DarkBackground" width="100%" colspan="4" align="center">Services</td>
  </tr>
  <tr>
    <th>Name</th>
    <th>Description</th>
    <th>State</th>
    <th>Action</th>
  </tr>
  <c:forEach var="service" items="${services}">
    <tr>
      <td>${service.name}</td>
      <td>${service.description}</td>
      <td>${service.state}</td>
      <td>
        <c:choose>
          <c:when test="${service.state == 'Shutdown'}">
            <a href="<portlet:actionURL><portlet:param name="action" value="start"/><portlet:param name="name" value="${service.name}"/></portlet:actionURL>">start</a>
          </c:when>
          <c:when test="${service.state == 'Stopped'}">
            <a href="<portlet:actionURL><portlet:param name="action" value="shutdown"/><portlet:param name="name" value="${service.name}"/></portlet:actionURL>">shutdown</a>&nbsp;
            <a href="<portlet:actionURL><portlet:param name="action" value="start"/><portlet:param name="name" value="${service.name}"/></portlet:actionURL>">start</a>
          </c:when>
          <c:when test="${service.state == 'Running'}">
            <a href="<portlet:actionURL><portlet:param name="action" value="stop"/><portlet:param name="name" value="${service.name}"/></portlet:actionURL>">stop</a>
          </c:when>
        </c:choose>
      </td>
    </tr>
  </c:forEach>
</table>
