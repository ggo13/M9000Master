<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<script type="text/javascript">
$(document).ready(function() {
	resetScopeRequestFrequency();
	$.publish("stopWaitTimer");
	$( "#dlgShowInternalCal" ).dialog( "option", "position", { my: "top", at: "top+100", of: window } );
	$("#dlgShowInternalCal").css("width","800");
	$("#dlgShowInternalCal").css("height","700");
	if ($("#internalCalibrationResult").val().indexOf("FAILED") != -1)
	{
		console.log("Index of FAILED: "+$("#internalCalibrationResult").val().indexOf("FAILED"));
		$( "#dlgShowInternalCal" ).dialog( "option", "title", "Internal Calibration Failed" );
		$('#btnApplyIntCalDialog').hide();
	}
	else
	{
		$( "#dlgShowInternalCal" ).dialog( "option", "title", "Internal Calibration Passed" );
		$('#btnApplyIntCalDialog').show();
	}
});
$('#btnCloseIntCalDialog').click(function() {
	console.log("Close button clicked");
	$('#dlgShowInternalCal').dialog('close');
});
$('#btnApplyIntCalDialog').click(function() {
	console.log("Apply Factors button clicked");
	$.publish("applyCalTopic");
	$( "#dlgShowApplyCalFactors" ).dialog( "option", "title", "Apply Cal Factors" );
	 $( "#dlgShowApplyCalFactors" ).dialog( "option", "position", { my: "center", at: "center", of: window } );	
});
</script>
<div id="divTextArea" style="width:100%;height:80%"><s:textarea id="internalCalibrationResult" name="internalCalibrationResult" cssStyle="width:100%;height:100%;font-family: 'Lucida Console', monospace;" title="Internal Calibration Result" wrap="true" value="%{internalCalibrationResult}"></s:textarea></div>
<sj:a openDialog="dlgShowApplyCalFactors">
	<s:submit theme="simple" name="btnApplyIntCalDialog" value="Apply" onclick="return false;"/>
</sj:a>
<sj:dialog id="dlgShowApplyCalFactors" closeOnEscape="false" width="auto" height="auto" 
							autoOpen="false" modal="true" title="Apply Calibration Factors">
								<img id="indicator" 
							       	src="images/indicator.gif" 
							       	alt="Loading..." 
							       	style="display:none"/>
								<s:url var="applyCalUrl" action="applyCal"/>
								    <sj:div id="divApplyCalStatus" 
								    		href="%{applyCalUrl}" 
								    		indicator="indicator"
								    		reloadTopics="applyCalTopic"
										deferredLoading="true"
										formIds="NextToCreateOrUpdate"
								    		cssClass="result ui-widget-content ui-corner-all">
								    </sj:div>
						</sj:dialog>
<s:submit theme="simple" name="btnCloseIntCalDialog" value="Close" onclick="return false;"/>