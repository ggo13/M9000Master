<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<script type="text/javascript">
$(document).ready(function() {
	console.log("Div result text "+$("#divResult").text());
	if ($("#divResult").text().toUpperCase().indexOf("ERROR") != -1)
	{
		console.log("index of error "+$("#divResult").text().toUpperCase().indexOf("ERROR"));
		$( "#dlgShowExternalCalReset" ).dialog( "option", "title", "ERROR: Reset Ext. Cal Failed" );
	}
	else
	{
		$( "#dlgShowExternalCalReset" ).dialog( "option", "title", "SUCCESS: Reset Ext. Cal" );
	}
});
$('#btnCloseExtCalResetDialog').click(function() {
	console.log("Close Reset Ext button clicked...");
	setChartChanged();
	setUpdateScopeConfig();
	$('#dlgShowExternalCalReset').dialog('close');
});
</script>
<div id="divResult" style="width:100%;height:80%"><p><s:property  value="%{externalCalibrationResult}"/></p></div>
<s:submit theme="simple" name="btnCloseExtCalResetDialog" value="Close" onclick="return false;"/>