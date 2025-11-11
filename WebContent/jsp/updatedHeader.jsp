<%@ taglib prefix="s" uri="/struts-tags"%>
<script  type="text/javascript">
// START: 03-Nov-2020 - Bind back button to prevent navigating out of the page with backspace
$(document).unbind('keydown').bind('keydown', function(event) {
	if (event.keyCode == 13) {
        event.preventDefault();
    }
	else if (event.keyCode === 8) {
        var doPrevent = true;
        //Chrome, FF, Safari
        if (event.target == document.body) {
            doPrevent = true;
        }
        //IE
        else {
            var nodeName = event.target.nodeName.toLowerCase();
            if (((nodeName == "input" && event.target.type == "text") || nodeName == "textarea")
              && !event.target.disabled && !event.target.readOnly) {
                doPrevent = false;
            }
        }

        if (doPrevent) {
            //Chrome, FF, Safari
            if (event.preventDefault()) {
                event.preventDefault();
            }
            //IE
            else {
                event.returnValue = false;
            }
        }
    }
    
});
//END: 03-Nov-2020
// START: 18-May-2021 -Clear action errors and messages by double clicking
$('[id^="userMsg"]').dblclick( function()
   {
           console.log("About to clear!!");
           $(this).empty();
   });
</script>

<s:url var="url" action="logout">
</s:url>
<s:url var="urlHome" action="backToStationList">
</s:url>
<div style="display: flex; flex-direction: row;">
	<div style="flex: 1; text-align: left;flex-basis:10%;flex-grow:0;">
		<img src="images/logo-usi-sine.png" alt="USI Logo"
							style="width: 194px;height: 44px;text-align: right;" />
	</div>
	<div style="flex: 1; text-align: left;flex-basis:90%;flex-grow:0;">
<s:if
	test="%{#session.userName != null && stationDetails.systemStationName != null}">
	<h1>
		User Name:
		<s:property value="%{#session.userName}" />
		| Station Name:
		<s:property value="%{stationDetails.systemStationName}" />
		| <font style="text-align: center; font-size: 10px;"><a
			href="<s:property value="%{urlHome}"/>">Home</a></font> | <font
			style="text-align: right; font-size: 10px;"><a
			href="<s:property value="%{url}"/>">Logout</a></font>
	</h1>
</s:if>
<s:elseif test="%{#session.userName != null}">
	<h1>
		User Name:
		<s:property value="%{#session.userName}" />
		| <font style="text-align: center; font-size: 10px;"><a
			href="<s:property value="%{urlHome}"/>">Home</a></font> | <font
			color="black" style="text-align: right; font-size: 10px;"><a
			href="<s:property value="%{url}"/>">Logout</a></font>
	</h1>
</s:elseif>
<s:elseif test="%{stationDetails.systemStationName != null">
	<h1>
		Station Name:
		<s:property value="%{stationDetails.systemStationName}" />
	</h1>
</s:elseif>
	<s:if
	test="%{#session.version != null}">
	<h1>
		Version: v<s:property value="%{#session.version}" />
	</h1>
	</s:if>
</div>
</div>