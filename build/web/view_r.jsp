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
<portlet:resourceURL var="portletURL" id="view.jsp" escapeXml="false" />

<link href="<%=request.getContextPath()%>/css/main.css" type="text/css" rel="stylesheet" />
<script type='text/javascript' src='<%=request.getContextPath()%>/js/jquery-1.11.1.min.js'></script>

<script>
var test_url='';
var symreadyres_url='';
var reqs='';

$(document).ready(function(){

  // Test button
  $("#test_button").click(function(){
    $.ajax({
             type: "GET",
             cache: false,
             crossDomain: true,
             dataType: "json",
             url:  'http://192.168.56.101:8080/web/guest/aleph_page/-/aleph/json' + '?' 
                 + $.param({ command: 'test', testParam1: 'testParamValue1', testParam2: 'testParamValue2'}),
             success: function(data){
                $("#table-body").append("<tr><td>url</td><td>"+data['commandRes']+"</td><td></td></tr>")
             },
             error:
                 function (xhr, ajaxOptions, thrownError) {
                 console.log(xhr.status);
                 console.log(thrownError);
                 console.log(xhr.responseText);
                 console.log(xhr);
             }
         }); // ajax
  }); // #test_button


   // Test button
  $("#submit_button").click(function(){
    $.ajax({
             type: "GET",
             cache: false,
             crossDomain: true,
             dataType: "json",
             url:  'http://192.168.56.101:8080/web/guest/aleph_page/-/aleph/json' + '?'
                 + $.param({ command: 'submit', aleph_file: 'irods://data.repo.cineca.it:1247/CINECA01/home/EUDAT_STAFF/Aleph_Test/ZD4001.52.AL'}),
             success: function(data){
                $("#table-body").append("<tr><td>Job submit</td><td>"+data['commandRes']+"</td><td></td></tr>")
             },
             error:
                 function (xhr, ajaxOptions, thrownError) {
                 console.log(xhr.status);
                 console.log(thrownError);
                 console.log(xhr.responseText);
                 console.log(xhr);
             }
         }); // ajax
  }); // # submit_button 

}); // .ready
</script>

This is the <b>aleph</b> portlet.

<div id="div1"><h2>Submit the job</h2></div>
<button id="test_button">Test</button>
<button id="submit_button">SUBMIT!</button>
<div class="table">
  <table id="one-column-emphasis">
    <colgroup>
    	<col>
    </colgroup>
    <thead>
      <tr>
       <th scope="col">Request Type</th>
       <th scope="col">Request Status</th>
       <th scope="col">Request URL</th>
      </tr>
    </thead>
    <tbody id="table-body">
    </tbody>
  </table>
</div>


