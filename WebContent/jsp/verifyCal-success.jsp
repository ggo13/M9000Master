<%@ taglib prefix="s" uri="/struts-tags"%>
<script type="text/javascript">
$(document).ready(function() {
	resetScopeRequestFrequency();
	$.publish("stopWaitTimer");
	$( "#dlgShowInternalCalVerify" ).dialog( "option", "position", { my: "top", at: "top+100", of: window } );
	$("#dlgShowInternalCalVerify").css("width","800");
	$("#dlgShowInternalCalVerify").css("height","700");
	if ($("#verifyCalibrationResult").val().toUpperCase().indexOf("FAILED") != -1)
	{
		$( "#dlgShowInternalCalVerify" ).dialog( "option", "title", "Verify Calibration Failed" );
	}
	else
	{
		$( "#dlgShowInternalCalVerify" ).dialog( "option", "title", "Verify Calibration Passed" );
		$( "#dlgShowInternalCalVerify" ).on('dialogclose', function(event) {
            console.log('Internal verification dialog closed');
           $.publish("updateCalVerifyDateTopic");
           });

	}
	
});
$('#btnCloseVerifyCalDialog').click(function() {
	console.log("Close button clicked");
	$.publish("updateCalVerifyDateTopic");
	$('#dlgShowInternalCalVerify').dialog('close');
});
</script>
<div id="divTextArea" style="width:100%;height:80%"><s:textarea id="verifyCalibrationResult" name="verifyCalibrationResult" cssStyle="width:100%;height:100%;font-family: 'Lucida Console', monospace;" title="Verify Calibration Result" wrap="true" value="%{verifyCalibrationResult}"></s:textarea></div>
<s:submit theme="simple" name="btnCloseVerifyCalDialog" value="Close" onclick="return false;"/>