<%@ include file="header.jsp"%>
<% 	String stationId = new String();
	String faultId = new String();
	stationId = ((Integer)session.getAttribute("stationId")).toString();
	faultId = (String)session.getAttribute("FaultId");
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
	<s:actionmessage  id="userMsgInfo" theme="jquery"/>
	<s:actionerror id="userMsgError" theme="jquery"/>
	<s:form action="displayComtradeDetails" method="POST">
		<table class="wwFormTable" style="width: 100%;height:100%" >
			<tr height="100%">
				<td width="100%"><jsp:plugin type="applet"
						archive="M9kMaster.jar, jfreechart-1.0.13.jar, jcommon-1.0.16.jar, commons-logging-1.0.4.jar, mysql-connector-java-5.0.2.jar, jcalendar-1.3.3.jar"
						code="com.usi.m9000.applets.M9kComtradeViewerApplet.class"
						codebase="." width="100%" height="80%">
						<jsp:params>
							<jsp:param name="stationId" value="<%=stationId%>" />
							<jsp:param name="draggable" value="true" />
						</jsp:params>
						<jsp:fallback>
			           <p>Unable to load applet</p>
			    </jsp:fallback>
					</jsp:plugin></td>
			</tr>
		</table>
	</s:form>
</body>
