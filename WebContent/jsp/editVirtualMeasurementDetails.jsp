<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<% 	String stationId = new String();
	stationId = (String)session.getAttribute("stationId");
  %>
<script type="text/javascript">
	var stationId = <%=stationId%>;
	var context_path = "${pageContext.request.contextPath}";
</script>
<style>
.horizontalDiv div {
    float: left;
    clear: none;
}
</style>

<script type="text/javascript" src="js/jquery.validate.js"></script>
<script type="text/javascript" src="js/additional-methods.js"></script>
<script type="text/javascript">
$("form input[type='radio']").change(function(){ // or $("form :radio")
    $.publish("newMeasurementsList");
});
function populateDecisionLogic(triggerValue)
{
	var existDecisionLogic = $('#virtualLogic').val();
	if (triggerValue == 'Add')
	{
		var trigger = $('#events').val();
		var virtualScale = $('#virtualScale').val();
		if (virtualScale == '')
		{
			virtualScale = 1;
		}
		console.log("values trigger "+trigger+" virt scale "+virtualScale);
		//document.getElementById("virtualLogic").value=existDecisionLogic+""+triggerValue;
		if (existDecisionLogic == '')
		{
			$('#virtualLogic').val("(("+virtualScale+ " * " +trigger+"))");
		}
		else
		{
			var globalScaleIfAny = '';
			if (!existDecisionLogic.trim().endsWith("))"))
			{
				globalScaleIfAny = existDecisionLogic.substring(existDecisionLogic.lastIndexOf("))")+2);	
			} 
			existDecisionLogic = existDecisionLogic.substring(0, existDecisionLogic.indexOf("))")+1) + " + ("+virtualScale+ " * " +trigger+"))"+globalScaleIfAny;
			$('#virtualLogic').val(existDecisionLogic);
		}
		$('#globalScale').prop("disabled", false);
		$('#btnDelete').prop("disabled", false);
         	$('#btnClear').prop("disabled", false);
	}
	else if (triggerValue == 'Delete')
	{
		if (existDecisionLogic != '')
		{
			var lastIndex = existDecisionLogic.lastIndexOf("+");
			if (lastIndex == -1)
			{
				$('#virtualLogic').val('');
				$('#globalScale').val('');
				$('#globalScale').prop("disabled", true);
				$('#btnDelete').prop("disabled", true);
         			$('#btnClear').prop("disabled", true);
			}
			else
			{
				var globalScaleIfAny = ')';
				if (!existDecisionLogic.trim().endsWith("))"))
				{
					globalScaleIfAny = existDecisionLogic.substring(existDecisionLogic.lastIndexOf("))")+1);	
				} 
				existDecisionLogic = existDecisionLogic.substring(0,lastIndex).trim()+globalScaleIfAny;
				
				$('#virtualLogic').val(existDecisionLogic.trim());
			}
		}
	}
	else if (triggerValue == 'Clear')
	{
		$('#virtualLogic').val('');
		$('#globalScale').val('');
		$('#globalScale').prop("disabled", true);
		$('#btnDelete').prop("disabled", true);
         	$('#btnClear').prop("disabled", true);
	}
		//$('#frmNewDialog').valid();
}

function updateGlobalScale(globalScale)
{
	var existDecisionLogic = $('#virtualLogic').val();
	var virtualLogicWithoutScale = existDecisionLogic.substring(0, existDecisionLogic.lastIndexOf("))")+2);
	console.log("Updated scale "+globalScale+" original "+virtualLogicWithoutScale);
	if ($('#globalScale').valid())
	{
		if (globalScale != '')
		{
			existDecisionLogic = virtualLogicWithoutScale +" * " + globalScale;
			$('#virtualLogic').val(existDecisionLogic.trim());
		}
		else
		{
			if (!existDecisionLogic.trim().endsWith("))"))
			{
				$('#virtualLogic').val(existDecisionLogic.substring(0, existDecisionLogic.lastIndexOf("))")+2));	
			}
		}
	}
}

function enableAdd()
{
	$('#btnAdd').prop("disabled", false);
}
function clearDecisionLogic()
{
        document.getElementById("virtualLogic").value="";
        $('#frmNewDialog').valid();
        
}
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
		      description: {
			required: true,
			regex: /^[^,]+$/
		      },
		      virtualLogic: {
				required: true
				},
		      globalScale: {
				number: true,
				nonzero: true
		      },
		      virtualScale: {
					number: true,
					nonzero: true
			      }
		  },
	  messages: {
	      description: {
		required: 'Description is required'
	      },
	      virtualLogic: {
			required: 'Virtual Measurement Logic is required'
		      }
             }
         });
     var dfrVal;
	 $("#selectedDfr").change(function() {
	   if ($("#virtualLogic").val() != "")
		   {
		   var newVal = $(this).val();
		   if (!confirm("You will loose current 'Virtual Measurement Logic', if you change DFRs. Do you want to proceed? ")) {
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
		   $('#globalScale').val('');
		  }
	});
         
});	
	  

</script>
<div id="divNewVirtuals">
		<s:form id="frmNewDialog" action="saveVirtualMeasurement" method="post" cssStyle="height:100%;width:100%"  theme="xhtml">
				<tr>
					<td colspan="2" class="titleLabel" style="background-color: #9999CC">
						<s:label theme="simple"
							cssStyle="text-align: left; font-weight:bold;"
							value="Virtual Measurement Details"></s:label>
					</td>
				<tr>

				<tr>
					<td colspan="2" align="left">
						<s:select id="selectedDfr"
							name="selectedDfr" onchange="$(this).publish('newMeasurementsList',this,event)"
							key="label.dfrs"  list="lstAnalogDfrs" listKey="dfrName"
							listValue="dfrName" value="%{selectedDfr}"/>
						
						<s:textfield cssStyle="width:95%" 
								id="description"
								name="description"
								value="%{description}"
								label="Virtual Measurement Description"></s:textfield>
						
					</td>
				</tr>
				
				<tr>

									<td colspan="2" align="center">
									<fieldset style="text-align: center;">
											<legend>
												Virtual Measurement
											</legend>

									<div>
										<div style="">
											<div>
												<s:label theme="simple" for="virtualLogic" value="Virtual Measurement Logic"/>
											</div>
											<div>
												<s:textfield  theme="simple" cssStyle="width:75%" 
													id="virtualLogic"
													name="virtualLogic"
													value="%{virtualLogic}" readonly="true" onclick="alert('Please use bottom section to add virtual logic');"
													tooltip="User created virtual logic. Please use bottom section to add virtual logic"
													></s:textfield>
											</div>
										</div>
										<div>
											<div>
												<s:label theme="simple" for="globalScale" value="Global Scale"/>
											</div>
											<div>
												<s:textfield  theme="simple" cssStyle="width:20%" 
													id="globalScale"
													name="globalScale"
													value="%{globalScale}" onkeyup="updateGlobalScale(this.value);"
													></s:textfield>
											</div>
										</div>
									</div>
									</fieldset>
									<fieldset style="text-align: center;">
									<legend>
										Select and Add Input Measurement and scale
									</legend>
									<div>
											<s:radio theme="simple" label="Select a Measurement Type " id="measurementType" name="measurementType" 
											list="lstMeasurementTypes" value="%{measurementType}"/>									
									</div>
									<s:url var="urlupdateDfrMeasurementsList" action="fetchDfrMeasurements">
									</s:url>
										<sj:div id="divNewSelectMeasurements"  formIds="frmNewDialog" href="%{urlupdateDfrMeasurementsList}" listenTopics="newMeasurementsList" deferredLoading="false">
										</sj:div>
									<div style="margin:1em;">
	
									<s:submit theme="simple" cssStyle="wih:10%" cssClass="submit" id="btnAdd" name="btnAdd"
										value="Add"
										onclick="javascript:populateDecisionLogic(this.value);return false;" />
									<s:submit theme="simple" cssStyle="width:10%" cssClass="submit" id="btnDelete" name="btnDelete"
											value="Delete"
										onclick="javascript:populateDecisionLogic(this.value);return false;" />
									<s:submit theme="simple" cssStyle="width:10%" cssClass="submit" id="btnClear" name="btnClear"
											value="Clear"
										onclick="javascript:populateDecisionLogic(this.value);return false;" />
									</div>
									</fieldset>									
									</td>

               			 </tr>
               			 <tr>
					<td colspan="2" align="center">

						<div id="divCreateVirtualChannels">
							<s:submit theme="simple" cssClass="submit" name="btnSave"
								value="Save"  action="saveVirtualMeasurement"/>
							<s:submit theme="simple" cssClass="submit" name="btnCancel"
								value="Cancel" onclick="$.publish('closeEditDialogTopic');return false;" />
						</div>
					</td>
				</tr>
		<s:hidden id="mode" name="mode" value="edit"/>	
		<s:hidden id="booAnalog" name="booAnalog"></s:hidden>
		<s:hidden id="booDigital" name="booDigital"></s:hidden>
		<s:hidden id="selectedVirtualIndex" name="selectedVirtualIndex"></s:hidden>
		<s:hidden id="lineGroupExists" name="lineGroupExists"></s:hidden>		
			</s:form>
	</div>
