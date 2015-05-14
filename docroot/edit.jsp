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
<jsp:useBean id="portalHost"  class="java.lang.String"                     scope="request"/>
<jsp:useBean id="portletPage" class="java.lang.String"                     scope="request"/>

<%
  javax.portlet.PortletURL actionURL1 = renderResponse.createActionURL();
  actionURL1.setPortletMode (javax.portlet.PortletMode.VIEW);
  actionURL1.setParameter ("PortletStatus","ACTION_INPUT");
%>

<link href="<%=request.getContextPath()%>/css/main.css" type="text/css" rel="stylesheet" />
<script type='text/javascript' src='<%=request.getContextPath()%>/js/jquery-1.11.1.min.js'></script>

<script>
$(document).ready(function(){
  // ...
  getPrefs();
}); // ready
  function setPrefs(){
      var prefvalues = "";
      // var prefparams = { "command": "setprefs", "prefs": [] } 
      var allpref_inputs = document.querySelectorAll("input[id=pref_input]");
      for(var i=0; i<allpref_inputs.length; i++) {
          prefvalues += allpref_inputs[i].name + "=" + allpref_inputs[i].value + ";"
      }
      var prefparams = { "command": "setprefs", "prefs": prefvalues }
      $.ajax({
               type: "GET",
               cache: false,
               crossDomain: true,
               dataType: "json",
               url:  '<%=portalHost%>/<%=portletPage%>/-/aleph/json', 
               data: prefparams,
               contentType: 'application/json',
               success: function(data){
               alert("New preference values successfully sent");
               },
               eror: function (xhr, ajaxOptions, thrownError) {
                   console.log(xhr.status);
                   console.log(thrownError);
                   console.log(xhr.responseText);
                   console.log(xhr);
               }
             }); // ajax
  }
  function getPrefs(){
    var prefparams = { "command": "getprefs" }
    $.ajax({
               type: "GET",
               cache: false,
               crossDomain: true,
               dataType: "json",
               url:  '<%=portalHost%>/<%=portletPage%>/-/aleph/json', 
               data: prefparams,
               contentType: 'application/json',
               success: function(data){
                $('#table-body').empty()
                for(key in data) {
                    if(key != 'commandValue' && key != 'commandRes')
                      $('#table-body').append('<tr><td>'+key+'</td><td><input id="pref_input" type="text" name="'+key+'" value="'+data[key]+'"/></td>');
                }
             },
               eror: function (xhr, ajaxOptions, thrownError) {
                   console.log(xhr.status);
                   console.log(thrownError);
                   console.log(xhr.responseText);
                   console.log(xhr);
               }
           }); // ajax
  }
  function resetPrefs() {
    var prefparams = { "command": "resetprefs" }
    $.ajax({
               type: "GET",
               cache: false,
               crossDomain: true,
               dataType: "json",
               url:  '<%=portalHost%>/<%=portletPage%>/-/aleph/json', 
               data: prefparams,
               contentType: 'application/json',
               success: function(data){
                   alert("Preference values have been set to default values");
                   getPrefs();
               },
               eror: function (xhr, ajaxOptions, thrownError) {
                   console.log(xhr.status);
                   console.log(thrownError);
                   console.log(xhr.responseText);
                   console.log(xhr);
               }
           }); // ajax
  }
</script>

This is the <b>aleph</b> portlet.

<div id="div1"><h2>aleph portlet configuration</h2></div>

<div class="table">
  <table id="one-column-emphasis">
    <colgroup>
    	<col>
    </colgroup>
    <thead>
      <tr>
       <th scope="col">Pref. Name</th>
       <th scope="col">Pref. Value</th>
      </tr>
    </thead>
    <tbody id="table-body">
    </tbody>
  </table>
  <table>
  <tr><td><button id="goBack"   onclick="location.href='<%= actionURL1.toString() %>'">Back</button></td>
      <td><button id="setPrefs" onclick="setPrefs()">Set preferences</button></td>
      <td><button id="reset" onclick="resetPrefs()">Reset</button></td>
  </tr>
  </table>
</div>
