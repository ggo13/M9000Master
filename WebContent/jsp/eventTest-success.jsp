<%@ taglib prefix="s" uri="/struts-tags"%>
<script type="text/javascript">
$(document).ready(function() {
	$.publish("stopWaitTimer");
	$( "#dlgShowEventTestStatus" ).dialog( "option", "position", { my: "top", at: "top+100", of: window } );
	$("#dlgShowEventTestStatus").css("width","800");
	$("#dlgShowEventTestStatus").css("height","700");
	if ($("#eventTestResult").val().toLowerCase().indexOf("error") != -1)
	{
		$( "#dlgShowEventTestStatus" ).dialog( "option", "title", "Event Test Failed" );
	}
	else
	{
		$( "#dlgShowEventTestStatus" ).dialog( "option", "title", "Event Test Successful" );
		$( "#dlgShowEventTestStatus" ).on('dialogclose', function(event) {
            console.log('Event test dialog closed');
           $.publish("updateEventTestDateTopic");
           });

	}
});
$('#btnCloseEventsDialog').click(function() {
	console.log("Close button clicked");
	$.publish("updateEventTestDateTopic");
	$('#dlgShowEventTestStatus').dialog('close');
});
</script>
<div id="divTextArea" style="width:100%;height:80%"><s:textarea id="eventTestResult" name="eventTestResult" cssStyle="width:100%;height:100%;font-family: 'Lucida Console', monospace;" title="Event Test Result" wrap="true" value="%{eventTestResult}"></s:textarea></div>
<s:submit theme="simple" name="btnCloseEventsDialog" value="Close" onclick="return false;"/>