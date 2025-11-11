<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<script type="text/javascript">
$(document).ready(function() {
	$( "#dlgShowFaultLoc" ).dialog( "option", "position", { my: "top", at: "top+100", of: window } );
	$("#dlgShowFaultLoc").css("width","800");
	$("#dlgShowFaultLoc").css("height","700");
	if ($("#faultLocationDetails").val().indexOf("Error") == 0)
	{
		console.log("Index of FAILED: "+$("#faultLocationDetails").val().indexOf("Error"));
		$( "#dlgShowFaultLoc" ).dialog( "option", "title", "Fault Location Error Report" );
		$('#btnUpdateFaultLoc').hide();
	}
	else
	{
		$( "#dlgShowFaultLoc" ).dialog( "option", "title", "Fault Location Report" );
		$('#btnUpdateFaultLoc').show();
	}
});
$('#btnCloseFaultLocDialog').click(function() {
	console.log("Close button clicked");
	$("#dlgShowFaultLoc").empty();
	$("#dlgShowFaultLoc").html(originalContent);
	$.publish("closeFaultLocTopic");
});
$('#btnUpdateFaultLoc').click(function() {
	console.log("Apply Factors button clicked");
	$.publish("saveFaultLocTopic");
	$('#txtFaultLoc').val($("#faultLocationDetails").val());
});
</script>
<div id="divTextArea" style="width:100%;height:80%"><s:textarea id="faultLocationDetails" name="faultLocationDetails" cssStyle="width:100%;height:100%" title="Fault Location Result" wrap="true" value="%{faultLocationStatus}"></s:textarea></div>
<sj:a openDialog="dlgShowSaveFaultLocation">
	<s:submit theme="simple" name="btnUpdateFaultLoc" value="Update" onclick="return false;"/>
</sj:a>
<sj:dialog id="dlgShowSaveFaultLocation" closeOnEscape="false" width="auto" height="auto" 
							autoOpen="false" modal="true" title="Save Fault Location Details">
								<img id="indicator" 
							       	src="images/indicator.gif" 
							       	alt="Loading..." 
							       	style="display:none"/>
								<s:url var="saveNewFaultLocURL" action="saveUpdatedFaultLoc">
									<s:param name="faultLocationDetails" value="%{faultLocationStatus}"/>
								</s:url>
								    <sj:div id="divApplyCalStatus" 
								    		href="%{saveNewFaultLocURL}" 
								    		indicator="indicator"
								    		reloadTopics="saveFaultLocTopic"
										deferredLoading="true"
										formIds="NextToCreateOrUpdate"
								    		cssClass="result ui-widget-content ui-corner-all">
								    </sj:div>
						</sj:dialog>
<s:submit theme="simple" name="btnCloseFaultLocDialog" value="Close" onclick="return false;"/>
<s:hidden name="faultIds" id="faultIds"/>