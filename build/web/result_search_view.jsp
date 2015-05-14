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



<%@page contentType="text/html" pageEncoding="UTF-8"%>



<%@ page import="javax.portlet.*"%>



<%@page import="java.util.ArrayList"%>


<jsp:useBean id="listReecords" class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="globalList" class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="searchText" class="java.lang.String" scope="request"/>
<jsp:useBean id="checked" class="java.lang.String" scope="request"/>
<jsp:useBean id="offset" class="java.lang.String" scope="request"/>



<link href="<%=request.getContextPath()%>/css/main.css" type="text/css" rel="stylesheet" />
<script type='text/javascript' src='<%=request.getContextPath()%>/js/jquery-1.11.1.min.js'></script>

<script>
    var test_url='';
    var symreadyres_url='';
    var reqs='';

  //  $(document).ready(function(){

        // Test button
  //      $("#test_button").click(function(){
  //          $.ajax({
  //              type: "GET",
  //              cache: false,
  //              crossDomain: true,
  //              dataType: "json",
  //              url:  'http://192.168.56.101:8080/web/guest/aleph_page/-/aleph/json' + '?' 
  //                  + $.param({ command: 'test', testParam1: 'testParamValue1', testParam2: 'testParamValue2'}),
  //              success: function(data){
  //                  $("#table-body").append("<tr><td>url</td><td>"+data['commandRes']+"</td><td></td></tr>")
  //              },
  //              error:
  //                  function (xhr, ajaxOptions, thrownError) {
  //                  console.log(xhr.status);
  //                  console.log(thrownError);
  //                  console.log(xhr.responseText);
  //                  console.log(xhr);
  //              }
  //          }); // ajax
  //      }); // #test_button


        // Test button
   //     $("#submit_button").click(function(){
   //         $.ajax({
   //             type: "GET",
   //             cache: false,
   //             crossDomain: true,
   //             dataType: "json",
   //             url:  'http://192.168.56.101:8080/web/guest/aleph_page/-/aleph/json' + '?'
   //                 + $.param({ command: 'submit', aleph_file: 'irods://data.repo.cineca.it:1247/CINECA01/home/EUDAT_STAFF/Aleph_Test/ZD4001.52.AL'}),
   //             success: function(data){
   //                 $("#table-body").append("<tr><td>Job submit</td><td>"+data['commandRes']+"</td><td></td></tr>")
   //             },
   //             error:
   //                 function (xhr, ajaxOptions, thrownError) {
   //                 console.log(xhr.status);
   //                 console.log(thrownError);
   //                 console.log(xhr.responseText);
   //                 console.log(xhr);
   //             }
   //         }); // ajax
   //     }); // # submit_button 

   // }); // .ready





    function submitSearch()
    {
    
        document.getElementById("offset").value="0";
        // alert("OFFSET==>"+document.getElementById("offset").value);
       

        if(document.getElementById("searchDOI").checked == true)
            document.getElementById("filterSearch").value="doi";
        if(document.getElementById("searchKeyword").checked == true)
            document.getElementById("filterSearch").value="keyword";
        if(document.getElementById("searchAll").checked == true)
            document.getElementById("filterSearch").value="all";

        //var doi=document.getElementById("IDsearchText").value;
        //var type_search=document.getElementById("filterSearch").value;
                

  
        document.forms["search_form"].submit();  
  
    }
    function DisableTexBox(){
        document.getElementById("IDsearchText").disabled=true;
        document.getElementById("IDsearchText").value="";
 
    }
    function EnableTexBox(){
        if(document.getElementById("IDsearchText").disabled == true){
            // alert(document.getElementsByName("search").value);
            document.getElementById("IDsearchText").disabled=false;
        }
    }
            
          
            
            
    function ShowAbstract(id){
               
        var controlMoreInfo=true;  
                
        $("#showAbs"+id).animate({"height": "toggle"});
                
                
                
                
        if( controlMoreInfo==true ) {
                               
            $("#ImageMoreInfo").attr("src","<%=renderRequest.getContextPath()%>/images/glyphicons_214_resize_small.png" );
                    
            controlMoreInfo=false;
        }
        else {
            $("#ImageMoreInfo").attr("src","<%=renderRequest.getContextPath()%>/images/glyphicons_215_resize_full.png" );
            controlMoreInfo=true;
        }
                
    }  
    
    function moreResources()
    {
      
        var offset=document.getElementById("offset").value;
            
        var nvalue=parseInt(offset);
            
            
        document.getElementById("offset").value=nvalue+30;
              
        document.getElementById("filterSearch").value="all";              
            
                   
        if(document.getElementById("offset").value=="180"){
            document.getElementById("IDmoreResource").hidden=true;
            alert(document.getElementById("IDmoreResource").hidden);
        }
                    
                                    
                                   
        alert("VALUE MORE----> "+document.getElementById("offset").value);
        document.forms["search_form"].submit();  
            
        
                   
                                    
    }
        
    function SubmitAnalize(id){
        alert(id);
      //   $("#submit_button").click(function(){
            $.ajax({
                type: "GET",
                cache: false,
                crossDomain: true,
                dataType: "json",
                url:  'http://192.168.56.101:8080/web/guest/aleph_page/-/aleph/json' + '?'
                    + $.param({ command: 'submit', aleph_file: id}),
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
//        }); // # submit_button 

   // }); 
   }
        
</script>

This is the <b>aleph</b> portlet.








<%

     System.out.println("OFFSETTTTT=>>>"+offset);
     System.out.println("List record=>>>"+listReecords.size());
     System.out.println("List global=>>>"+globalList.size());


     ArrayList globaListNoDupl=new ArrayList();




             if (globalList.size() > 1) {
                 int k = 1;
                 int j, i = 0;
                 boolean duplicato;
            
                 Object[] repo = (Object[]) globalList.get(i);
            
            
                 globaListNoDupl.add(globalList.get(0));
            

                 for (i = 1; i < globalList.size(); i++) {

            
                      repo=(Object[]) globalList.get(i);
                      String url = (String) repo[1];
                
                     duplicato = false;

                     for (j = 0; j < i; j++) {

            
                         Object[] repo2=(Object[]) globalList.get(j);
                         String urlNew = (String) repo2[1];
                         if (url.equals(urlNew)) {
            
            
                             duplicato = true;
                         }

                     }
                     if (!duplicato) {
                         
                         globaListNoDupl.add(globalList.get(i));
                         
                         
                     }


                 }

             }

          System.out.println("List global not duplicate=>>>"+globaListNoDupl.size());

 
 
%>

<div id="container">
    <form id="search_form" action="<portlet:actionURL portletMode="view"><portlet:param name="PortletStatus" value="ACTION_SEARCH"/></portlet:actionURL>" method="post">

        <table>

            <tr>
                <td align="center" colspan="2">
                    Search for:
                    <% if(checked.equals("doi")){%>
                    <input type="radio" id="searchDOI" name="search" value="doi" onclick="EnableTexBox()" checked>DOI
                    <% }else{
                    %>
                    <input type="radio" id="searchDOI" name="search" value="doi" onclick="EnableTexBox()">DOI
                    <%}
                         if(checked.equals("keyword")){ %>

                    <input type="radio" id="searchKeyword" name="search" value="keyword"  onclick="EnableTexBox()" checked >Keyword
                    <% }else{
                    %>
                    <input type="radio" id="searchKeyword" name="search" value="keyword"  onclick="EnableTexBox()" >Keyword
                    <%}
                    if(checked.equals("all")){ 
                    %>
                    <input type="radio" id="searchAll" name="search" value="all" onclick="DisableTexBox()" checked>View All
                    <% }else{
                    %>
                    <input type="radio" id="searchAll" name="search" value="all" onclick="DisableTexBox()" >View All
                    <%}%>
                </td>
            </tr>
            <tr>
                <td>
                    <input type="text" hidden="true" id="filterSearch" name="filterSearch" />


                    <input type="text" id="IDsearchText" name="searchText"  size="70" value="<%=searchText%>">
                </td>
                <td>
                    <input  hidden="true" name="offset" id="offset" value="<%=offset%>"/>


                    <input type="button"  name="buttonSearch" value="Reserve a DOI"  onclick="submitSearch()"/>
                </td>
            </tr>
        </table>
    </form>



    <%
       
        
   if(listReecords.size()>0){
            
          
    if(listReecords.size()==1){
        String [] infoData= (String []) listReecords.get(0);
        String doi=infoData[0];
        String title=infoData[1];
        String abs=infoData[2];
        String link_data=infoData[4];
        String url_invenio=infoData[5];
    %>

    <br>
    <div id="singleRecord">
        <a href="<%=url_invenio%>" target="_blank"><%=title%></a>
        <br>
        <center><%=abs%><br>

            
            <button id="<%=link_data%>">Test</button>
            <button id="<%=link_data%>"  onclick="SubmitAnalize(this.id)">SUBMIT!</button>
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
          <!--  <input id="<%=link_data%>"  type="button" value="Analize"  value="<%=link_data%>" onclick="SubmitAnalize(this.id)">-->
        </center>
        <br>
    </div>

    <hr>
    <%
      }
    else{
         for(int i=0; i<globaListNoDupl.size(); i++){
          
            // globalList.add(listReecords.get(i));
         
             String [] infoData= (String []) globaListNoDupl.get(i);
         
             String doi=infoData[0];
             String title=infoData[1];
             String abs=infoData[2];
             String link_data=infoData[4];
             String url_invenio=infoData[5];
                 
           
                 
                
    %>

    <br>
    <table id="resource<%=i%>">
        <tr>     
            <td align="left"><a  href="<%=url_invenio%>" target="_blank"> <%=title%></a></td>
            <td align="rigth" ><a id="DateLink" class="Link" href="#" onClick="ShowAbstract(<%=i%>); return false;">Details <img id="ImageMoreInfo" class="ImageAnimation" src="<%=renderRequest.getContextPath()%>/images/glyphicons_215_resize_full.png" /></a>
            </td>
        </tr>
    </table>

    <center>        
        <div  id="showAbs<%=i%>" style="display: none;" >
            <br>
            Abstract:<%=abs%><br>
             <button id="<%=link_data%>">Test</button>
            <button id="<%=link_data%>"  onclick="SubmitAnalize(this.id)">SUBMIT!</button>
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
            
           <!-- <input type="button" id="<%=link_data%>" value="Analize" value="<%=link_data%>" onclick="SubmitAnalize(this.id)">-->
        </div>

    </center>
    <br>
    <hr>

    <%
     
 }
   int of = Integer.parseInt(offset);
   if( listReecords.size()>=30){
    %>


    <div>
        <center>

            <div  id="IDmoreResource" onclick="moreResources()" >----More Resources---</div>
        </center>
    </div>


    <%  
        
      }
         }
         
        
    %>



    <%
       
           }
        
        
       
                         
    %>  

</div>















<!--<div id="div1"><h2>Submit the job</h2></div>
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
</div>-->


