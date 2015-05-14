<%
/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<portlet:resourceURL var="resURL" id="edit.jsp" escapeXml="false" />
<portlet:actionURL   var="actURL" escapeXml="false" />

<%
  javax.portlet.PortletURL actionURL1 = renderResponse.createActionURL();
  actionURL1.setPortletMode (javax.portlet.PortletMode.VIEW);
  actionURL1.setParameter ("PortletStatus","ACTION_MAIN");
%>

<link href="<%=request.getContextPath()%>/css/main.css" type="text/css" rel="stylesheet" />
<script type='text/javascript' src='<%=request.getContextPath()%>/js/jquery-1.11.1.min.js'></script>

<script>
$(document).ready(function(){
  // ...
}); // ready
</script>

This is the <b>aleph</b> portlet.

<div id="div1"><h2>aleph portlet configuration</h2></div>
<button id="goBack" onclick="location.href='<%= actionURL1.toString() %>'">Back</button>

<div class="table">
  <table id="one-column-emphasis">
    <colgroup>
    	<col>
    </colgroup>
    <thead>
      <tr>
       <th scope="col">Pref Name</th>
       <th scope="col">Pref Type</th>
      </tr>
    </thead>
    <tbody id="table-body">
      <tr><td>prefName1</td><td id="prefName1">prefValue1</td></tr>
      <tr><td>prefName2</td><td id="prefName2">prefValue2</td></tr>
    </tbody>
  </table>
</div>


