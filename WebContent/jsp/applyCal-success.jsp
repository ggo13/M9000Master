<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<script type="text/javascript">
$(document).ready(function() {
	console.log("Div resilt text "+$("#divResult").text());
	if ($("#divResult").text().toUpperCase().indexOf("ERROR") != -1)
	{
		console.log("index of error "+$("#divResult").text().toUpperCase().indexOf("ERROR"));
		$( "#dlgShowApplyCalFactors" ).dialog( "option", "title", "Apply Cal Factors Failed" );
	}
	else
	{
		$( "#dlgShowApplyCalFactors" ).dialog( "option", "title", "Apply Cal Factors Passed" );
		$( "#dlgShowApplyCalFactors" ).on('dialogclose', function(event) {
            console.log('Internal calibration dialog closed');
           $.publish("updateCalDateTopic");
           $('#dlgShowInternalCal').dialog('close');
           });
	}
});
$('#btnCloseApplyCalDialog').click(function() {
	console.log("Close button clicked");
	$.publish("updateCalDateTopic");
	$('#dlgShowApplyCalFactors').dialog('destroy').remove();
	$('#dlgShowInternalCal').dialog('close');
});
</script>
<div id="divResult" style="width:100%;height:80%"><p><s:property  value="%{applyCalFactorsResult}"/></p></div>
<s:submit theme="simple" name="btnCloseApplyCalDialog" value="Close" onclick="return false;"/>