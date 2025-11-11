<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>

<link href="css/main.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="js/jquery.validate.js"></script>
<script type="text/javascript" src="js/additional-methods.js"></script>
<script type="text/javascript">
var userRole = "<s:property value='#session.userDetails.role'/>";
$(document).ready( function() {

	$.validator.addMethod(
	    "regex",
	    function(value, element, regexp) {
		var check = false;
		return this.optional(element) || regexp.test(value);
	    },
	    "Special Characters not allowed: , "
	);
	$.validator.addMethod(
		    "regexVirtualLogic",
		    function(value, element, regexp) {
		        var check = false;
		        return this.optional(element) || regexp.test(value);
		    },
		    "Invalid virtual logic "
		);
	$.validator.addMethod(
		    "nonzero",
		    function(value, element, regexp) {
		        return (!(parseFloat(value) === 0));
		    },
		    "Non zero value please!"
		);
         var validator = $("#frmEditDialog").validate({
             rules: {
                 "editedVirtualChannel.circuitName": {
                 	required: true,
                 	regex: /^[^,]+$/
                 },
                 "virtualLogic": {
                  	required: true                  	
                  },
			      "editedVirtualChannel.virtualScale": {
						number: true,
						nonzero: true
				      }
             },
             messages: {
                 "editedVirtualChannel.circuitName": {
                 	required: 'Channel description is required'
                 },
             "editedVirtualChannel.virtualLogic": {
              	required: 'Virtual Channel Logic is required'
              }
             }
         });

         $("input[name^='inputType']").click(function() {
             var type = $(this).val();
             console.log("Input type..."+type);
			if (type == 'V')
				{
	             $("#rowIPhase").hide();
	             $("#rowVPhase").show();					
			$("input[id='radioVPhase${phase}']").val("${phase}");
			$("input[id='radioVPhase${phase}']").prop("checked",'checked');
				}
			else
				{
	             $("#rowVPhase").hide();
	             $("#rowIPhase").show();					
			$("input[id='radioIPhase${phase}']").val("${phase}");
			$("input[id='radioIPhase${phase}']").prop("checked",'checked');
				}
         });
         console.log("Value of type now "+'${inputType}'+" current phase "+'${phase}');
         if ('${inputType}' == 'V')
         {
              $("#rowIPhase").hide();
              $("#rowVPhase").show();                                     
			$("input[id='radioVPhase${phase}']").val("${phase}");
			$("input[id='radioVPhase${phase}']").prop("checked",'checked');
         }
         else
         {
              $("#rowVPhase").hide();
              $("#rowIPhase").show();                                     
			$("input[id='radioIPhase${phase}']").val("${phase}");
			$("input[id='radioIPhase${phase}']").prop("checked",'checked');
         }
});	


$.subscribe('checkAccess', function(event, data) {
if (userRole != 'admin')
        {
                $("#frmEditDialog :input").prop("disabled", true);
        }

});
$("form input[type='radio']").change(function(){ // or $("form :radio")
    $.publish("editChannelsList");
});

</script>
	
<s:fielderror/>
<s:actionerror id="userMsgError" theme="jquery"/>
<p id="demo"></p>
		<div id="divEditVirtuals">
		<s:form id="frmEditDialog" action="saveVirtualChannels" method="post" cssStyle="width:100%" theme="xhtml">
				<tr>
					<td colspan="2" class="titleLabel" style="background-color: #9999CC">
						<s:label theme="simple"
							cssStyle="text-align: left; font-weight:bold;"
							value="Virtual Channel Details"></s:label>
					</td>
				<tr>

				<tr>
				
					<td colspan="2" align="left">
						<s:label 
							name="selectedDfr"
							key="label.selected.dfr"  value="%{selectedDfr}"/>
						<s:textfield cssStyle="width:95%" 
								id="editedVirtualChannel.circuitName"
								name="editedVirtualChannel.circuitName"
								value="%{editedVirtualChannel.circuitName}"
								label="Virtual Channel Description"></s:textfield>
					</td>
				</tr>
				<tr id="rowVPhase" style="display:none">
					<td align="right">
						<s:label theme="simple" for="radioVPhase" class="tdlabel" value="Select Phase: "></s:label>
					</td>
					<td >
						<div>
						<s:radio cssStyle="font-family:courier, courier new, serif;" id="radioVPhase" theme="simple"  name="phase" list="#{'A':'Phase A ','B':'Phase B ','C':'Phase C ','N':'Neutral'}"  value="%{phase}"/>
						</div>
						<div>
						<s:radio cssStyle="font-family:courier, courier new, serif;" id="radioVPhase" theme="simple"  name="phase" list="#{'AB':'Phase AB','BC':'Phase BC','CA':'Phase CA'}" value="%{phase}" />
						</div>
					</td>
				</tr>
				<tr id="rowIPhase" style="display:none">
					<td align="right">
						<s:label theme="simple" for="radioIPhase" class="tdlabel" value="Select Phase: "></s:label>
					</td>
					<td>
						<s:radio cssStyle="font-family:courier, courier new, serif;" id="radioIPhase" theme="simple" name="phase" list="#{'A':'Phase A','B':'Phase B','C':'Phase C','N':'Neutral'}" value="%{phase}" />
					</td>
				</tr>
				<tr>
					<td>
						<s:radio cssStyle="font-family:courier, courier new, serif;" label="Select Input Type " name="inputType" list="#{'V':'Voltage','I':'Current'}" value="%{inputType}" />
					</td>
				</tr>
				<tr>
					<td colspan="2" align="center">
						<s:url var="urlupdateOptionSelectionList" action="fetchOptionSelectEdit" >
						</s:url>
						<sj:div id="divSelectChannels"  formIds="frmEditDialog" href="%{urlupdateOptionSelectionList}" listenTopics="editChannelsList" onCompleteTopics="checkAccess" deferredLoading="false">
						</sj:div>
					</td>
				</tr>
                <tr>
                        <td colspan="2" align="left">
                                <s:textfield cssStyle="width:25%" 
                                                id="editedVirtualChannel.virtualScale"
                                                name="editedVirtualChannel.virtualScale"
                                                value="%{editedVirtualChannel.virtualScale}" onkeyup="updateVirtualScale(this, this.value);"
                                                label="Scale" tooltip="Multiplies final virtual channel result by this amount">
                                </s:textfield>
                        </td>
                </tr>						
				<tr>
					<td colspan="2" align="center">

						<div id="divEditVirtualChannels">
						<s:url var="urlSaveEditvirtual"  action="saveVirtualChannels" >
						</s:url>
						<s:submit theme="simple" cssClass="submit" id="bindSaveVirtuals" action="saveVirtualChannels"
		    				 value="Save" />
						<s:submit  id="btnCancel" theme="simple" cssClass="submit" name="btnCancel" value="Cancel" onclick="javascript: return cancelEdit();" />
							
						</div>
					</td>
				</tr>
				<s:hidden id="selectedChannelIndex" name="selectedChannelIndex" value="%{selectedChannelIndex}"/>
				<s:hidden id="mode" name="mode" value="edit"/>
				<s:hidden id="booAnalog" name="booAnalog"></s:hidden>
				<s:hidden id="booDigital" name="booDigital"></s:hidden>
				<s:hidden id="lineGroupExists" name="lineGroupExists"></s:hidden>
			</s:form>
		</div>
	
