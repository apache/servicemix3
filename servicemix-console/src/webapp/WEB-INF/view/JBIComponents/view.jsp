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
    <td class="DarkBackground" width="100%" colspan="4" align="center">Components</td>
  </tr>
  <tr>
    <th>Name</th>
    <th>Type</th>
    <th>State</th>
    <th>Action</th>
  </tr>
  <c:forEach var="component" items="${components}">
    <tr>
      <td>
        <portlet:renderURL var="componentUrl">
            <portlet:param name="mode" value="comp" />
            <portlet:param name="name" value="${component.name}" />
        </portlet:renderURL>
        <a href="${componentUrl}">
          ${component.name}
        </a> 
      </td>
      <td>${component.type}</td>
      <td>${component.state}</td>
      <td>
        <c:choose>
          <c:when test="${component.state == 'Shutdown'}">
            <a href="<portlet:actionURL><portlet:param name="action" value="start"/><portlet:param name="name" value="${component.name}"/></portlet:actionURL>">start</a>
          </c:when>
          <c:when test="${component.state == 'Stopped'}">
            <a href="<portlet:actionURL><portlet:param name="action" value="shutdown"/><portlet:param name="name" value="${component.name}"/></portlet:actionURL>">shutdown</a>&nbsp;
            <a href="<portlet:actionURL><portlet:param name="action" value="start"/><portlet:param name="name" value="${component.name}"/></portlet:actionURL>">start</a>
          </c:when>
          <c:when test="${component.state == 'Started'}">
            <a href="<portlet:actionURL><portlet:param name="action" value="stop"/><portlet:param name="name" value="${component.name}"/></portlet:actionURL>">stop</a>
          </c:when>
        </c:choose>
      </td>
    </tr>
  </c:forEach>
</table>
