<%@ taglib prefix="s" uri="/struts-tags"%>
<% 	String stationId = new String();
	String faultId = new String();
	stationId = (String)session.getAttribute("stationId");

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
	<script src="http://www.java.com/js/deployJava.js"></script>
	<script>
    alert('About to deploy ');
        var attributes = { code:'com.usi.m9000.applets.M9kComtradeViewerApplet.class',
            archive:'M9kApplet.jar,jfreechart-1.0.14.jar, log4j-1.2.16.jar, jcommon-1.0.16.jar, commons-logging-1.1.1.jar, mysql-connector-java-5.1.13-bin.jar, jcalendar-1.3.3.jar, xbean.jar, M9000XMLConfig.jar, activemq-all-5.4.2-fuse-00-00.jar' ,
            width:'800', height:'800'} ;
        var parameters = {jnlp_href: 'M9kMasterApplet.jnlp',
        		image: 'USI-Logo.jpg',
        		 boxbgcolor: '#EEEEFF', 
                 boxborder: 'true', 
                 centerimage: 'true',
        		stationId: '<%=stationId%>'} ;
        deployJava.runApplet(attributes, parameters, '1.5');
    </script>

	<applet code='com.usi.m9000.applets.M9kComtradeViewerApplet'
		jnlp_href='M9kMasterApplet.jnlp' , 
        width=100%, height=100% />
</body>
