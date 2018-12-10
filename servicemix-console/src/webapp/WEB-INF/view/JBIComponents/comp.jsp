<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<table width="100%">
  <tr>
    <td>
      <a href="<portlet:renderURL><portlet:param name="mode" value="comp"/><portlet:param name="action" value="refresh"/></portlet:renderURL>">Refresh</a> 
      &nbsp;
      <a href="<portlet:renderURL><portlet:param name="mode" value="list"/></portlet:renderURL>">List</a> 
    </td>     
  </tr>
  <tr>
  </tr>
</table>
