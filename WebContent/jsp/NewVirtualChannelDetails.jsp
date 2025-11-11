<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>

<script type="text/javascript" src="js/jquery.validate.js"></script>
<script type="text/javascript" src="js/additional-methods.js"></script>
<script type="text/javascript">

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

         var validator = $("#frmNewDialog").validate({
             rules: {
		      circuitName: {
			required: true,
			regex: /^[^,]+$/
		      },
		      virtualLogic: {
				required: true
			      },
			      virtualScale: {
						number: true,
						nonzero: true
				      }
		  },
	  messages: {
	      circuitName: {
		required: 'Channel description is required'
	      },
	      virtualLogic: {
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



$.subscribe("selectAllValuesToSubmit", function(data){
	alert(data);
	var list = document.getElementById("lstSelectedVirtualChannels");
	selectAllOptionsExceptSome(list, "key", "HEADERNOTRANSFER");
	});
	  
$("form input[type='radio']").change(function(){ // or $("form :radio")
    $.publish("newChannelsList");
});

$("#selectedDfr").change(function() {
	 if ($("#virtualLogic").val() != "")
	   {
		   var newVal = $(this).val();
		   if (!confirm("You will loose current 'Virtual Logic', if you change DFRs. Do you want to proceed? ")) {
		   	if (typeof dfrVal === 'undefined')
		   	{
		   		console.log("if part");
			     dfrVal = $(this).find("option:first-child").val();
			}
			$(this).val(dfrVal); //set back
		     return;                  //abort!
		   }
		   dfrVal = newVal;       //store new value for next time
		   $('#virtualLogic').val('');
		   $('#virtualScale').val('');
	   }
});
</script>
<div id="divNewVirtuals">
		<s:form id="frmNewDialog" action="createVirtualChannels" method="post" cssStyle="width:100%"  theme="xhtml">
				<tr>
					<td colspan="2" class="titleLabel" style="background-color: #9999CC">
						<s:label theme="simple"
							cssStyle="text-align: left; font-weight:bold;"
							value="Virtual Channel Details"></s:label>
					</td>
				<tr>

				<tr>
					<td colspan="2" align="left">
						<s:select id="selectedDfr" cssStyle="font-family:courier, courier new, serif;"
							name="selectedDfr"
							key="label.dfrs"  list="lstAnalogDfrs" listKey="dfrName"
							listValue="dfrName" value="%{selectedDfr}"  onchange="$(this).publish('newChannelsList',this,event)"/>
						
						<s:textfield cssStyle="width:95%" 
								id="circuitName"
								name="circuitName"
								value="%{circuitName}"
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
						<s:url var="urlupdateOptionSelectionList" action="fetchOptionSelectNew">
						</s:url>
						<sj:div id="divNewSelectChannels"  formIds="frmNewDialog" href="%{#urlupdateOptionSelectionList}" listenTopics="newChannelsList" deferredLoading="false">
						</sj:div>
					</td>
				</tr>
				<tr>
				<tr>
                        <td colspan="2" align="left">
                                <s:textfield cssStyle="width:25%" 
                                                id="virtualScale"
                                                name="virtualScale"
                                                value="%{virtualScale}" disabled="true" onkeyup="updateVirtualScale(this, this.value);"
                                                label="Scale" tooltip="Multiplies final virtual channel result by this amount">
                                </s:textfield>
                        </td>
                </tr>
					<td colspan="2" align="center">

						<div id="divCreateVirtualChannels">
						<s:url var="urlsaveNewvirtual" action="createVirtualChannels"  />
							<s:submit theme="simple" cssClass="submit" name="btnCreate"
								value="Create"  action="createVirtualChannels"   />
							<s:submit theme="simple" cssClass="submit" name="btnCancel"
								value="Cancel" onclick="return cancelNew();" />
						</div>
					</td>
				</tr>
		<s:hidden id="mode" name="mode" value="new"/>		
		<s:hidden id="booAnalog" name="booAnalog"></s:hidden>
		<s:hidden id="booDigital" name="booDigital"></s:hidden>
		<s:hidden id="lineGroupExists" name="lineGroupExists"></s:hidden>		
			</s:form>
	</div>
