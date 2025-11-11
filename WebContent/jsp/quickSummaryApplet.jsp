<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page import="com.usi.m9000.dto.UsersDTO"%>
<% 	String stationId = new String();
	stationId = (String)session.getAttribute("stationId");
	String userRole = ((UsersDTO)session.getAttribute("userDetails")).getRole();
	String userName = ((UsersDTO)session.getAttribute("userDetails")).getUserName();
  %>
<body>
	<script type="text/javascript">
function openWindow(url, name, percent) {
    var w = 630, h = 440; // default sizes
    if (window.screen) {
        w = window.screen.availWidth * percent / 100;
        h = window.screen.availHeight * percent / 100;
    }

    var win = window.open(url,name,'resizable=1,copyhistory=yes,width='+w+',height='+h);
    win.moveTo(0,0);
    win.focus();
}
</script>

	<jsp:plugin type="applet"
		archive="M9kApplet.jar,dynamicreports-core-4.1.0.jar,jasperreports-6.2.0.jar,jdtcore-3.1.0.jar,poi-3.10.1.jar,itext-2.1.7.js4.jar"
		code="com.usi.m9000.applets.M9kAppletComtradeViewer.class"
		codebase="." width="100%" height="100%">
		<jsp:params>
			<jsp:param name="name" value="M9000 Master"/>
			<jsp:param name="stationId" value="<%=stationId%>" />
			<jsp:param name="userRole" value="<%= userRole %>" />
			<jsp:param name="userName" value="<%= userName %>" />
			<jsp:param name="filename" value="<s:property value='%{filename}'/>" />
			<jsp:param name="confInputString" value="<s:property value='%{confInputString}'/>" />
			<jsp:param name="datInputString" value="<s:property value='%{datInputString}'/>" />
			<jsp:param name="displayDataType" value="<s:property value='%{displayDataType}'/>" />
			<jsp:param name="java_arguments"
				value="-Xmx1500m -Djnlp.packEnabled=true" />
			<jsp:param name="classloader_cache" value="true" />
			<jsp:param name="cache_option" value="Plugin" />
			<jsp:param name="cache_archive" value="M9kApplet.jar" />
			<jsp:param name="image" value="USI-Logo.jpg" />
			<jsp:param name="progressbar" value="true" />
			<jsp:param name="centerimage" value="true" />
			<jsp:param name="boxborder" value="true" />
			<jsp:param name="boxbgcolor" value="#EEEEFF" />
			<jsp:param name="boxmessage" value="USI Applet is loading." />
		</jsp:params>
		<jsp:fallback>
			           <p>Unable to load applet</p>
			    </jsp:fallback>
	</jsp:plugin>
</body>
