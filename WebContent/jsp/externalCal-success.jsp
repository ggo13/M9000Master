<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<script type="text/javascript">
$(document).ready(function() {
	console.log("Div result text "+$("#divResult").text());
	if ($("#divResult").text().toUpperCase().indexOf("FAIL") != -1)
	{
		console.log("index of error "+$("#divResult").text().toUpperCase().indexOf("FAIL"));
		$( "#dlgShowExternalCal" ).dialog( "option", "title", "ERROR: External Calibration Failed" );
	}
	else
	{
		$( "#dlgShowExternalCal" ).dialog( "option", "title", "SUCCESS: External Calibration  Passed" );
	}
});
$('#btnCloseExtCalDialog').click(function() {
	console.log("Close Ext cal button clicked");
	setChartChanged();
	setUpdateScopeConfig();
	$('#dlgShowExternalCal').dialog('close');
});
</script>
<div id="divResult" style="width:100%;height:80%"><p><s:property  value="%{externalCalibrationResult}"/></p></div>
<s:submit theme="simple" name="btnCloseExtCalDialog" value="Close" onclick="return false;"/>