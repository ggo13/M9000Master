<!DOCTYPE HTML>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ page import="com.usi.m9000.dto.UsersDTO"%>
<% 	String stationId = new String();
	stationId = (String)session.getAttribute("stationId");
	String userRole = ((UsersDTO)session.getAttribute("userDetails")).getRole();
	String userName = ((UsersDTO)session.getAttribute("userDetails")).getUserName();
  %>
<html>
<head>
<script type="text/javascript">
function checkBrowser() {
var width=screen.width*.98;
var height=screen.height*(3/4);
var left=0;
var right=0;
navigator.browserSpecs = (function(){
		    var ua = navigator.userAgent, tem, 
			M = ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || [];
		    if(/trident/i.test(M[1])){
			tem = /\brv[ :]+(\d+)/g.exec(ua) || [];
			return {name:'IE',version:(tem[1] || '')};
		    }
		    if(M[1]=== 'Chrome'){
			tem = ua.match(/\b(OPR|Edge)\/(\d+)/);
			if(tem != null) return {name:tem[1].replace('OPR', 'Opera'),version:tem[2]};
		    }
		    M = M[2]? [M[1], M[2]]: [navigator.appName, navigator.appVersion, '-?'];
		    if((tem = ua.match(/version\/(\d+)/i))!= null)
			M.splice(1, 1, tem[1]);
		    return {name:M[0], version:M[1]};
		})();
	if (navigator.browserSpecs.name != "IE")
		{
//			alert("Java Plugin support is required to display COMTRADE viewer. Please use Internet Explorer to view COMTRADE files");
//			window.close();
//			return false;
            var fileName = "${fileName}";
            if (!fileName.startsWith("/"))
            {
                  fileName="/"+fileName;
			}
            var url="/COMTRADEViewer/v"+fileName;
            var encodedUrl = encodeURIComponent(url);
            console.log("Filename "+fileName+" url "+url+" encoded url "+encodedUrl);
            fetch("/COMTRADEViewer", { method: 'HEAD' })
            .then(function (res) {
                if (res.ok) {
                    console.log('New COMTRADE viewer available.');
                    window.open(url, "_self","status = 1,height="+height+",width="+width+",resizable = 1,left="+left+",top="+top);
                } else {
                    console.log('New COMTRADE viewer is not ready yet');
                    alert("Java Plugin support is required to display COMTRADE viewer. Please use Internet Explorer to view COMTRADE files");
                    window.close();
                    return false;
                }
            }).catch(function (err) { console.log('Error:', err);});
			
		}
}
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
<style type="text/css">
body {
    height: 100vh;
}
</style>
</head>
<body onload="checkBrowser()">
	<jsp:plugin type="applet"
		archive="M9kApplet.jar"
		code="com.usi.m9000.applets.M9kAppletComtradeViewer.class"
		codebase="." width="100%" height="100%">
		<jsp:params>
			<jsp:param name="name" value="M9000 Master"/>
			<jsp:param name="stationId" value="<%=stationId%>" />
			<jsp:param name="userRole" value="<%= userRole %>" />
			<jsp:param name="userName" value="<%= userName %>" />
			<jsp:param name="filename" value="${fileName}" />
			<jsp:param name="confInputString" value="${confInputString}" />
			<jsp:param name="datInputString" value="${datInputString}" />
			<jsp:param name="displayDataType" value="${displayDataType}" />
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
</html>