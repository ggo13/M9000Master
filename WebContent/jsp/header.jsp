<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sjdt" uri="/struts-jquery-datatables-tags"%>
<%
int timeout = (Integer)session.getAttribute("session-timeout-secs");
//response.setHeader("Refresh", timeout + "; URL = /M9000Master/NextToCreateOrUpdate!cancel?customActionError='Session timed out'");
%>
<head>
<link rel="icon" type="image/png" href="images/usi-logo-64.png">
<s:head/>
<div id="idletimeout">
	You will be logged off in <span><!-- countdown place holder --></span>&nbsp;seconds due to inactivity. 
	<a id="idletimeout-resume" href="#">Click here to continue using this web application</a>.
</div>
<title>USI M9000 Master</title>
<style type="text/css">
#idletimeout { background:#CC5100; border:3px solid #FF6500; color:#fff; font-family:arial, sans-serif; text-align:center; font-size:12px; padding:10px; position:relative; top:0px; left:0; right:0; z-index:100000; display:none; }
#idletimeout a { color:#fff; font-weight:bold }
#idletimeout span { font-weight:bold }
h4 {
  margin: 0 0 9px 0;
}
</style>
<h4 align="center">USI M9000 Master</h4>
</head>
<!--	<s:if test="%{#session.userName != null}">-->
<!--		<h4 align="center">USI M9000 Master <font color="black" style="text-align: right; font-size:10px;"><a href="<s:property value="%{url}"/>">Logout</a></font></h4> -->
<!--	</s:if>-->
<!--	<s:else>-->

<!--	</s:else>-->
<head>
<sj:head jqueryui="true" loadAtOnce="true"
    	compressed="true"
    	jquerytheme="m9000"
    	customBasepath="themes"
    	loadFromGoogle="false"
    	debug="true"/>
<link href="<s:url value="/css/main.css"/>" rel="stylesheet"
	type="text/css" />
</head>
<html>
<body>

<script>
    history.forward();
</script>


<script src="js/jquery.contextmenu.js" type="text/javascript"></script>

<script src="js/jquery.idletimer.js" type="text/javascript"></script>
<script src="js/jquery.idletimeout.js" type="text/javascript"></script>
<script type="text/javascript">
var userRole = "<s:property value='#session.userDetails.role'/>";
$.idleTimeout('#idletimeout', '#idletimeout a', {
	idleAfter: <%=timeout%>,
	pollingInterval: 60,
	keepAliveURL: '<s:url action="checkSessionStatus" includeParams="none"/>',
	serverResponseEquals: 'active',
	onTimeout: function(){
		$(this).slideUp();
		window.location = "/M9000Master/logout?customActionError=Session timed out";
	},
	onIdle: function(){
		$(this).slideDown(); // show the warning bar
		document.body.scrollTop = document.documentElement.scrollTop = 0;
	},
	onCountdown: function( counter ){
		$(this).find("span").html( counter ); // update the counter
	},
	onResume: function(){
		$(this).slideUp(); // hide the warning bar
	}
});

$.fn.shiftSelectable = function() {
	var lastChecked,
	    $boxes = this;

	$boxes.click(function(evt) {
	    if(!lastChecked) {
	        lastChecked = this;
	        return;
	    }

	    if(evt.shiftKey) {
	        var start = $boxes.index(this),
	            end = $boxes.index(lastChecked);
	     
	        var startIndex = Math.min(start, end),endIndex = Math.max(start, end);
	        for (var i = startIndex; i <= endIndex; i++) {
            	$boxes[i].checked = lastChecked.checked;
            	$($boxes[i]).trigger("change");
        	}
	    }

	    lastChecked = this;
	});
};

</script>

<script type="text/javascript">
document.onclick = myClickHandler;
function myClickHandler() {
      document.getElementById("checkBrowserClicks").value="false";
      
    }
//  $(window).bind('beforeunload', function(){
$(window).on('unload', function(){
   if(document.getElementById("checkBrowserClicks").value=="true")
   {
	    $.ajax({
			/* type : "POST", */
			url :  "<s:url action="logout"/>"
		});
   }
 });

// $(window).bind('unload',function(){
//	 console.log("Inside unload checkBrowserClicks value? "+document.getElementById("checkBrowserClicks").value);
//	 if(document.getElementById("checkBrowserClicks").value=="true")
//	   {
//	    $.ajax({
//					/* type : "POST", */
//					url :  "<s:url action="logout"/>",
//				});
//	}	 
//	});
</script>
<input type="hidden" value="true" id="checkBrowserClicks"/>
<s:url var="urlHeaderDetails" action="updateHeader" />
<sj:div href="%{urlHeaderDetails}" listenTopics="change_station"
	preload="false">
</sj:div>
