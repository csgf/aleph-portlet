<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@page import="javax.portlet.*"%>
<%@page import="it.infn.ct.iservices.*"%>
<%@taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>
<%@taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>

<portlet:defineObjects />
<%          PortletPreferences prefs = renderRequest.getPreferences();
            String tabNames = "Analize,VM Login";
            String tabs1 = ParamUtil.getString(request, "tabs1", "Analize");
            PortletURL url = renderResponse.createRenderURL();
            pageContext.setAttribute("tabs1", tabs1);
%>

<jsp:useBean id="param"            class="java.lang.String"     scope="request"/>
<jsp:useBean id="searchText"       class="java.lang.String"     scope="request"/>
<jsp:useBean id="checked"          class="java.lang.String"     scope="request"/>
<jsp:useBean id="portalHost"       class="java.lang.String"     scope="request"/>
<jsp:useBean id="portletPage"      class="java.lang.String"     scope="request"/>
<jsp:useBean id="urlPage"          class="java.lang.String"     scope="request"/>
<jsp:useBean id="isAlephVMEnabled" class="java.lang.Boolean"    scope="request"/>
<jsp:useBean id="iSrv"             class="it.infn.ct.iservices" scope="request"/>
<jsp:useBean id="serviceDesc"      class="java.lang.String"     scope="request"/>
<jsp:useBean id="guacamole_page"   class="java.lang.String"     scope="request"/>

        <div id="container">
            <script type="text/javascript">
                scrollToPosition();

                function submitSearch()
                {
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
                $.getJSON('<%=portalHost%>/<%=portletPage%>/-/aleph/json?command=allocinfo', function(json) {
        var s="";
        for(i=0; i<json.allocInfo.length; i++){
            var id = json.allocInfo[i].allocId;
            if (json.allocInfo[i].allocState == "RUNNING"){
            var status =  "<img src=<%=renderRequest.getContextPath()%>/images/vm-run.png width=48px height=48px>";
                }
               else 
                   {
                   var status =  "<img src=<%=renderRequest.getContextPath()%>/images/standby.png width=48px height=48px>";
                   }
            var srvUUID = json.allocInfo[i].srvUUID;
            var data1= json.allocInfo[i].allocTs;
            var data2= json.allocInfo[i].allocExpTs;
            //ssh url 
            // http://90.147.74.76:8080/guacamole-0.9.1/client.xhtml?id=c%2Fssh%3A%2F%2Falephusr%4090.147.74.85%3A22
            // Nota che id e' la stringa che si costruisce dai dati di accesso: <proto>://<username>@<IP>:<port>
            if(json.allocInfo[i].accInfo != null && json.allocInfo[i].accInfo.length > 0){
        s += "<tr><td><a class='view-more-info'><img src=<%=renderRequest.getContextPath()%>/images/moreinfo.png ></a></td><td>"  + srvUUID +  "</td><td>" + data1 +"</td><td class='status'>" + status + "</td>";
                var url = ""; 
                for(j=0; j<json.allocInfo[i].accInfo.length; j++){
                    var ip= json.allocInfo[i].accInfo[j].ip;
                    var workgroup =  json.allocInfo[i].accInfo[j].workgroup;
                    var username = json.allocInfo[i].accInfo[j].username;
                    var password = json.allocInfo[i].accInfo[j].password ;
                    var port = json.allocInfo[i].accInfo[j].port ;
                    var proto = json.allocInfo[i].accInfo[j].proto;
                    if (proto == 'ssh') 
                        url += "<a href=<%=portalHost%>/<%=guacamole_page%>/#/client/c/ssh_" + srvUUID + " target=\"_blank\"><img src=<%=renderRequest.getContextPath()%>/images/ssh.png></a>";
                    if(proto == 'vnc')
                        url += "<a href=<%=portalHost%>/<%=guacamole_page%>/#/client/c/vnc_" + srvUUID + " target=\"_blank\"><img src=<%=renderRequest.getContextPath()%>/images/ssh.png></a>";
                }
                s += "<td>"+ url +"</td>";
                s += "</tr>";
                s += "<tr class=moreinfo><td colspan=5>Additional information<br>ip:" + ip + "<br>expiration date:" + data2 +"<br>username:" + username+ "</td></tr>";
             }
             else 
             { s += "<tr><td></td><td>"  + srvUUID +  "</td><td>" + data1 +"</td><td class='status'>" + status + "</td><td></td></tr>";}
        }
        if (s !=null  && s!=""){ 
            $('.tr').append(s);
        }
        else {
            s = "<tr><td></td><td></td><td></td><td></td><td></td></tr>";
            $('.tr').append(s);
        }
        $('.view-more-info').click(function(){
                 $(this).closest('tr').next('tr').toggle();
        });
    }); 
            </script>
<liferay-ui:tabs
        names="<%= tabNames%>"
        url="<%= url.toString()%>"
        />
<c:choose>
    <c:when test="${tabs1 == 'Analize'}" >
            <form id="search_form" action="<portlet:actionURL portletMode="view"><portlet:param name="PortletStatus" value="ACTION_DOI"/></portlet:actionURL>" method="post">

                    <table>
                        <tr>
                            <td align="center" colspan="2">
                                Search for:
                            <% if (checked.equals("doi")) {%>
                            <input type="radio" id="searchDOI" name="search" value="doi" onclick="EnableTexBox()" checked>DOI
                            <% } else {
                            %>
                            <input type="radio" id="searchDOI" name="search" value="doi" onclick="EnableTexBox()">DOI
                            <%}
                                if (checked.equals("keyword")) {%>

                            <input type="radio" id="searchKeyword" name="search" value="keyword"  onclick="EnableTexBox()" checked >Keyword
                            <% } else {
                            %>
                            <input type="radio" id="searchKeyword" name="search" value="keyword"  onclick="EnableTexBox()" >Keyword
                            <%}
                                if (checked.equals("all")) {
                            %>
                            <input type="radio" id="searchAll" name="search" value="all" onclick="DisableTexBox()" checked>View All
                            <% } else {
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
                            <input type="button"  name="buttonSearch" value="Search"  onclick="submitSearch()"/>
                        </td>
                    </tr>
                </table>
            </form>
            <br><br>
            <div>
                <table id="example" class="display" cellspacing="0" width="100%">
                    <thead>
                        <tr>
                            <th></th>
                            <th>Title</th>
                            <th>Type Analisys</th>
                            <th>Action</th>
                           
                        </tr>
                    </thead>

                    <tfoot>
                        <tr>
                            <th></th>
                            <th>Title</th>
                            <th>Type Analisys</th>
                            <th>Action</th>
                            
                        </tr>
                    </tfoot>
                </table>

            </div>
        </div>

    </c:when>

    <c:when test="${tabs1 == 'VM Login'}" >
        <p><%=serviceDesc%></p>
        <% if(isAlephVMEnabled) { %>
        <p>By clicking on 'Start new VM' button you will create a new ALEPH virtual machine that you can access for about 72 hours. You will be notified via e-mail about the necessary credentials to access the newly instatiated VM. You cannot start more than one ALEPH instance until the ALEPH VM expires.</p>
        <a href="#" onclick="submitVM()"><img src="<%=renderRequest.getContextPath()%>/images/login.png"></a>
        <h3>- VM INFO -</h3>
        <table id="vminfo" class="gradient-style">
            <thead>
                <tr>
                    <th></th> 
                     <th>
                        Server UUID
                    </th>
                    <th>
                        StartTime
                    </th>
                    <th>
                        Status
                    </th>
                    <th>
                        Connection
                    </th>

                </tr>
            </thead>
            <tbody class="tr">
            </tbody>
        </table>
        <% } else {%>
             <p>It seems that you don't have the rights to execute an ALEPH Virtual Machine. If you are interested in accessing the ALEPH VM, please contact the site <a href="mailto:credentials@ct.infn.it"><b>administrator</b></a>.</p>
        <a href="#"><img src="<%=renderRequest.getContextPath()%>/images/no-login.png"></a>
            <% } %>
        </c:when>
        <c:otherwise>
        No tabs
    </c:otherwise>
</c:choose>                

            <script type="text/javascript">
<!-- SubmitVM() begin -->
<!-- Tab VM Login selected form result_search_view; redirects to the VM Login of search_view page -->
<!-- SubmitVM() end -->
                /* Formatting function for row details - modify as you need */
                var link="";
                function format ( d ) {
                    // `d` is the original data object for the row
                    return d.abs;
                }
            
                $(document).ready(function() {
                    table = $('#example').DataTable( {
                        "ajax": "<%=renderRequest.getContextPath()%>/datatable/exampleJson.txt",
                        "columns": [
                            {
                                "class":          'details-control',
                                "orderable":      false,
                                "data":           null,
                                "defaultContent": ''
                            },
                            {"data": "title" },
                            {"data": "type_analisys" },
                            {"data":"action"}
                          
                        ],
                        "order": [[1, 'asc']]
                    
                    } );
     
                    // Add event listener for opening and closing details
                    $('#example tbody').on('click', 'td.details-control', function () {
                        var tr = $(this).closest('tr');

                        var row = table.row( tr );

                        link=row.data().link_data;
                        //alert(link);

                        if ( row.child.isShown() ) {
                            // This row is already open - close it
                            tr.removeClass( 'details' );
                            row.child.hide();
                            tr.removeClass('shown');
                        }
                        else {
                            // Open this row
                            tr.addClass( 'details' );
                            row.child( format(row.data()) ).show();
                            tr.addClass('shown');
                            //alert(row.data().link_data);
                        }
                    } );                     
                } );

                function submitAction(id){
                    $.getJSON('<%=renderRequest.getContextPath()%>/datatable/exampleJson.txt', function(json) {
                        var selcombo=document.getElementById(id+"sel").value;
                        $.ajax({
                                type: "GET",
                                cache: false,
                                crossDomain: true,
                                dataType: "json",
                                url:  '<%=portalHost%>/<%=portletPage%>/-/aleph/json' + '?' + $.param({ command: 'submit', aleph_file: json.data[id].link_data, aleph_alg: selcombo }),
                                success: function(data){
                                alert("Analysis job successfully submitted; click on MyJobs to check its status");
                                },
                                error: function (xhr, ajaxOptions, thrownError) {
                                   console.log(xhr.status);
                                   console.log(thrownError);
                                   console.log(xhr.responseText);
                                   console.log(xhr);
                                }
                               }); // ajax
                    });
                }
                function submitVM(id){
                    $.getJSON('<%=renderRequest.getContextPath()%>/datatable/exampleJson.txt', function(json) {
                        var realId = id.substr(3);
                        $.ajax({
                                type: "GET",
                                cache: false,
                                crossDomain: true,
                                dataType: "json",
                                url:  '<%=portalHost%>/<%=portletPage%>/-/aleph/json' + '?' + $.param({ command: 'submit', aleph_file: json.data[realId].link_data}),
                                success: function(data){
                                       if(data.commandRes == "OK")
                                          alert("An instance of ALEPH VM is being started; you will be notified by email as soon as the machine will be available. This operation may require some time to complete.");
                                       else alert("FAILED: "+data.commandInfo);
                                },
                                error: function (xhr, ajaxOptions, thrownError) {
                                   console.log(xhr.status);
                                   console.log(thrownError);
                                   console.log(xhr.responseText);
                                   console.log(xhr);
                                }
                               }); // ajax
                        });
                } 
            </script>

