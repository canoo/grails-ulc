<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html; charset=UTF-8" %><%@ taglib tagdir="/WEB-INF/tags" prefix="h" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page import="com.ulcjava.easydeployment.server.EasyDeploymentUtilities"%><%@page import="com.ulcjava.easydeployment.server.DeploymentParameter"%>
<%
    DeploymentParameter parameter = new DeploymentParameter(request.getLocale());
%>
<c:set var="iconURL" value='<%= EasyDeploymentUtilities.toFullURLIfNotNull(request, parameter.getFavIcon(),"") %>'></c:set>
<c:set var="splashURL" value='<%= EasyDeploymentUtilities.toFullURLIfNotNull(request, parameter.getSplash(), "") %>'></c:set>
<html>
<head profile="http://www.w3.org/2005/10/profile">
<c:if test="${not empty iconURL}">
     <link rel="icon"  href="${iconURL}">
</c:if>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" >
<title><%=parameter.getTitle() %></title>
<style type="text/css" media="screen">
html,body {
    margin:0;
    padding:0;
    height:100%;
}
#appletdiv {
    width:100%;
    height:100%;
<c:choose>
<c:when test="${not empty splashURL}"> 
    top:11000px;
</c:when> 
<c:otherwise>
    top:0px;
</c:otherwise>
</c:choose>
    left:0px;
    z-index:10;
    position:absolute;
    background:#FFFFFF;
    margin:0;
}
</style>    

<c:if test="${not empty splashURL}">
<c:set var="showScript" value="showApplet"></c:set>
<script type="text/javascript">
<!--
function showApplet() {
    var splashDiv = document.getElementById("splash");
    splashDiv.style.visibility = "hidden";
    var appletDiv = document.getElementById("appletdiv");
    appletDiv.style.top = "0px";
}
// -->
</script>
</c:if>

</head>
<body><div id="appletdiv"><h:ulcApplet title="<%=parameter.getTitle() %>" style="position: absolute; left: 0px; top: 0px; margin: 0px;  width: 100%; height: 100%;" 
scriptToCall="${showScript}"></h:ulcApplet></div><c:if test="${not empty splashURL}"><div style="  z-index: 100; left: 0px; top: 0px;  width: 100%; height: 100%;" id="splash"><table width="100%" height="100%"><tr><td align="center" valign="middle"><img  src="${splashURL}" ></td></tr></table></div></c:if></body></html>
