<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<script type="text/javascript">
$(document).ready(function() {
	console.log("SaveFaultSuccess.jsp: Div result text "+$("#divResult").text());
	if ($("#divResult").text().toUpperCase().indexOf("ERROR") != -1)
	{
		console.log("index of error "+$("#divResult").text().toUpperCase().indexOf("ERROR"));
		$( "#dlgShowSaveFaultLocation" ).dialog( "option", "title", "Error: New Fault location Details not saved" );
	}
	else
	{
		$( "#dlgShowSaveFaultLocation" ).dialog( "option", "title", "New Fault location Details Saved" );
	}
});
$('#btnCloseSaveFaultLocDialog').click(function() {
	console.log("Close button clicked");
	$.publish("updateFaultLocationTopic");
	$('#dlgShowSaveFaultLocation').dialog('destroy').remove();
	reloadFaultsGrid();
	$("#dlgShowFaultLoc").empty();
	$("#dlgShowFaultLoc").html(originalContent);
	$.publish("closeFaultLocTopic");
});
</script>
<div id="divResult" style="width:100%;height:80%"><p><s:property  value="%{saveFaultLocationStatus}"/></p></div>
<s:submit theme="simple" name="btnCloseSaveFaultLocDialog" value="Close" onclick="return false;"/>