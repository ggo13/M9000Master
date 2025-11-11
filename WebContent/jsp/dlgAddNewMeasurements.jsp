<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>

<link href="css/main.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="js/jquery.validate.js"></script>
<script type="text/javascript" src="js/additional-methods.js"></script>
<script type="text/javascript">
var userRole = "<s:property value='#session.userDetails.role'/>";
$(document).ready( function() {

	$.validator.addMethod("numericOnly", function(value, element) {
        return this.optional(element) || /^\d+$/.test(value);
    }, "Please enter only numeric digits (0-9).");
    
       $.validator.addMethod("range1to100", function(value, element) {
        let number = parseInt(value, 10);
        return this.optional(element) || (!isNaN(number) && number >= 1 && number <= 100);
    }, "Please enter a number between 1 and 100.");
    
    
    $("#newMeasurementsForm").validate({
            rules: {
                newMeasurementsCount: {
                    required: true,
                    numericOnly: true,
                    range1to100: true  // Apply custom rule
                }
            },
            messages: {
                newMeasurementsCount: {
                    required: "This field is required.",
                    umericOnly: "Please enter valid numeric value.",
                    range1to100: "Please enter a valid number between 1 and 100."
                }
            }
         });
    $("#newMeasurementsCount").on("keyup change", function() {
    	$("#btnAddNewMeasurements").prop("disabled", !$("#newMeasurementsForm").valid());
});
});
</script>
	
<s:fielderror/>
<s:actionerror id="userMsgError" theme="jquery"/>
		<div id="divAddMeasurements">
		<s:form id="newMeasurementsForm" theme="simple" action="triggerChannelsAddMeasurements" >
				<img id="indicator" src="images/indicator.gif" alt="Loading..." style="display:none"/>
				<s:hidden id="totalMeasurementsCount" name="totalMeasurementsCount" value="%{lstTriggerChannels.size()}"/>
				<s:url var="addNewTrigger" action="triggerChannelsAddMeasurements">
				</s:url>
				<div class="type-button">
				    <!-- First row: Label and Textfield -->
				    <div class="row">
				        <s:label cssStyle="font-weight:bold;text-align:center" value="Enter the number of new measurements to add"></s:label>
				        <s:textfield name="newMeasurementsCount" id="newMeasurementsCount" size="3" value="1" />
				    </div>
				    <div class="row">
				        <s:label cssStyle="font-weight:bold;text-align:center" value="Enter the new measurement type to add"></s:label>
				        <s:select name="newMeasurementsType" id="newMeasurementsType" list="%{mapAnalogTriggerInputTypes}"/>
				    </div>
					<s:url var="addNewTrigger" action="triggerChannelsAddMeasurements">
						
					</s:url>
				    <!-- Second row: Confirm and Cancel buttons -->
				    <div class="row buttons-row">
				        <sj:submit id="btnAddNewMeasurements" indicator="indicator" value="Add" button="true" targets="dummyTarget" href="%{addNewTrigger}" formIds="newMeasurementsForm" onclick="javascript:return isFromValid();"
				            buttonText="Add" onSuccessTopics="newMeasurementsTopic"
				            onErrorTopics="newMeasurementsErrorTopic" onBeforeTopics="showBusyCursor" onCompleteTopics="hideBusyCursor"/>
				        <sj:a onClickTopics="cancelMeasurementsTopic" button="true">Cancel</sj:a>
				    </div>
				</div>
		</s:form>

		</div>
	
