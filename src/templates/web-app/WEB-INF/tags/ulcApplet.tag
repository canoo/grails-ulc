<%@ attribute name="title" required="true" %>
<%@ attribute name="style" required="true" %>
<%@ attribute name="scriptToCall" required="false"  %>
<%@ attribute name="clientId" required="false"  %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@tag import="com.ulcjava.easydeployment.server.EasyDeploymentUtilities"%>
<c:if test="${empty clientId}">
<c:set var="clientId" value="1"></c:set>
</c:if>

<%
String ua = request.getHeader("User-Agent");
String jars = "@ulc.client.libs@";
String jvmargs = "";
boolean isMac = false;
if (ua.contains("Mac") || ua.contains("PPC")) {
    isMac = true;
    jvmargs = "-d32 -Xms64m -Xmx256m";
}
%>

<%ServletContext servletContext=request.getSession().getServletContext();  %>

<script src="http://www.java.com/js/deployJava.js"></script>
<script> 
var attributes = { code:'<%=EasyDeploymentUtilities.toAppletLauncherClassName(servletContext, request) %>',
                   archive:'<%=jars %>', <c:if test="${not empty scriptToCall}">MAYSCRIPT:'true',</c:if>
                   width:640, height:480, name:'${title}', style:'${style}' }; 
var parameters = {jnlp_href: 'startApplet.jnlp',
                  <c:if test="${not empty scriptToCall}">callscript:'showApplet',</c:if>
                  'client-id':'${clientId}', scriptable:true,
                  'url-string':'<%=EasyDeploymentUtilities.toUrlStringParam(request) %>;jsessionid=<%=session.getId()%>',
                  separate_jvm:true, java_arguments: '<%=jvmargs%>'};
deployJava.runApplet(attributes, parameters, '1.5'); 
</script>
