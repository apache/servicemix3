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
