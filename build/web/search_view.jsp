<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@ page import="javax.portlet.*"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<portlet:defineObjects />
<%PortletPreferences prefs = renderRequest.getPreferences();%> 




<script type="text/javascript">


    function submitSearch()
    {
    
    
      // alert(document.getElementById("offset").value);

        if(document.getElementById("searchDOI").checked == true)
            document.getElementById("filterSearch").value="doi";
        if(document.getElementById("searchKeyword").checked == true)
            document.getElementById("filterSearch").value="keyword";
        if(document.getElementById("searchAll").checked == true)
            document.getElementById("filterSearch").value="all";


  
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


</script>

<form id="search_form" action="<portlet:actionURL portletMode="view"><portlet:param name="PortletStatus" value="ACTION_SEARCH"/></portlet:actionURL>" method="post">

    <table>

        <tr>
            <td align="center" colspan="2">
                Search for:
                <input type="radio" id="searchDOI" name="search" value="doi" onclick="EnableTexBox()">DOI
                <input type="radio" id="searchKeyword" name="search" value="keyword"  onclick="EnableTexBox()">Keyword
                <input type="radio" id="searchAll" name="search" value="all" onclick="DisableTexBox()">View All
            </td>
        </tr>
        <tr>
            <td>
                <input type="text" hidden="true" id="filterSearch" name="filterSearch" />


                <input type="text" id="IDsearchText" name="searchText"  size="70">
            </td>
            <td>
                <input  hidden="true" name="offset" id="offset" value="0"/>
                <input type="button"  name="buttonSearch" value="Reserve a DOI"  onclick="submitSearch()"/>
            </td>
        </tr>
    </table>
</form>
