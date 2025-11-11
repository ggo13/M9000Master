var lastSelectedRow = null;
$(document).ready(function(){
		isAllMeasurementsEnabled();
	 	isAllExportsChecked();
	 	isAllPmuChecked();
		isAllDdrsChecked();
	 	hideAssistance();
	 	$('#enableAssist').show();
	 	$('#buttonsTop').show();
	 	$('#buttonsBottom').show();
	 	if (!isDfrAddedOrRemoved)
        {
	 		$('#navMenu').show();
        }
	 	setFocus();

	enableShiftSelectCheckboxes();
	handleDurationInput();
	highlightSelectedRows();
	$('[id$="AddMeasurement"]').click(function(e){
		    e.preventDefault();
		    $.ajax({
		    	type : "POST",
		     url: "/M9000Master/triggerChannelsAddMeasurements.action",
		     data:$("#frmConfigureVirtualMeasurements").serialize(),
		     success : function (data, status, request){
		     	// alert("On click event "+data);
		     	$('#measurementTable tbody').append(data);
		     	enableShiftSelectCheckboxes();
		     	handleDurationInput();
		     	highlightSelectedRows();
			var form = document.forms["frmConfigureVirtualMeasurements"];
			if (form.elements['enableAssist'].checked)
			{
				showAssistance();
			}
			else
			{
				hideAssistance();
			}
			$('#AddMeasurement').focus();

		      },
		      error : function(data, status, request){
                       alert ("Issues!"+data);
                   }
		   });
		});

	$('#frmConfigureVirtualMeasurements').submit(function(e) {
		     $("#frmConfigureVirtualMeasurements :disabled").removeAttr('disabled');
		});
	$("body").css("cursor", "default");
	$(".traverseMenu").css('cursor', 'pointer');
	if (userRole != 'admin')
        {
                $("#frmConfigureVirtualMeasurements :input").prop("disabled", true);
        }

	$("table.configureTable tr[id^=tr_]").bind("paste", function(e) {
		var clipboardText = e.originalEvent.clipboardData.getData("text/plain");;
		if (clipboardText.split(/\r\n|\r|\n/).length > 1) {
			e.stopPropagation();
			e.preventDefault();
			handlePasteEvent(e);
		}
	});

	});

//$(function() {
//$('#measurementTable tr').not(':first').hover(function() {
//  $(this).css('background-color', '#FFFF99');
//  $(this).contents('td').css({'border': '1px solid #bdbdeb', 'border-left': 'none', 'border-right': 'none'});
//  $(this).contents('td:first').css('border-left', '1px solid #bdbdeb');
//  $(this).contents('td:last').css('border-right', '1px solid #bdbdeb');
//},
//function() {
//  $(this).css('background-color', '#FFFFFF');
//  $(this).contents('td').css('border', 'none');
//});
//});


function enableShiftSelectCheckboxes()
{
			$('input:checkbox[id^="globalSelect"]').shiftSelectable();
			$('input:checkbox[id$="status"]').shiftSelectable();
			$('input:checkbox[id$="pmuStatus"]').shiftSelectable();
			$('input:checkbox[id$="freqPmuStatus"]').shiftSelectable();
			$('input:checkbox[id$="exportStatus"]').shiftSelectable();
			$('input:checkbox[id$="ddrStatus"]').shiftSelectable();
			$('input:checkbox[id$="disturbanceAlarm"]').shiftSelectable();
			$('input:checkbox[id$="freqPmuStatus"]').change(function(){
				if ($(this).is(':enabled')) {
					$(this).attr("disabled", false);
				}
				});

}

function handleDurationInput()
{
	$("[id$='duration']").on("input", function() {
		var form = document.forms["frmConfigureVirtualMeasurements"];
	        var idStr = $(this).attr('id');
			console.log("ROC duration "+this.value+" id: "+idStr);
	        var rocNegLimit = idStr.substring(0,idStr.indexOf(".")+1)+"tripRocNeg";
	        var rocPosLimit = idStr.substring(0,idStr.indexOf(".")+1)+"tripRocPos";
	        var disableTripRoc = idStr.substring(0,idStr.indexOf(".")+1)+"disableTripRoc";
	        console.log("disableTripRoc "+disableTripRoc+" val "+form.elements[disableTripRoc].value);
	        if ($.isNumeric(this.value) && parseInt(this.value) > 0)
	        {
	       	 	$(jqSelector(rocNegLimit)).prop("disabled",false);
	       	 	$(jqSelector(rocPosLimit)).prop("disabled",false);
	       	 	form.elements[disableTripRoc].value=false;
			}
	        else
	        {
	       	 	$(jqSelector(rocNegLimit)).prop("disabled",true);
	       	 	$(jqSelector(rocPosLimit)).prop("disabled",true);
	       	 	form.elements[disableTripRoc].value=true;
	        }

	 	});

}

function highlightSelectedRows()
{
	$("#measurementTable tr").click(function() {
		$(this).find('td').css({'background-color': '#4747a6','color':'cyan'});
	    if (lastSelectedRow != null && !$(this).is(lastSelectedRow))
	    {
	             lastSelectedRow.find('td').css({'background-color':'#EEEEFF','color':'black'});
	    }
	    lastSelectedRow = $(this);

	//	$(this).siblings().find('td').css({'background-color':'#EEEEFF','color':'black'});
	});

}

	function populate_default_trigger_name()
	{
		if(confirm("This will populate descriptions with default values . Do you wish to continue?")){
			var iIndex = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var sourceValue,sourceName,secondIndex,sourceTriggerName,objName,objId,newName,index,prefix;
			var objPrefix = 'lstTriggerChannels['+ iIndex +'].';
			var obj= objPrefix+"channel";
			var objName = objPrefix+"name";
			console.log("First DEBUG-M: obj "+form.elements[obj].value);
			while (form.elements[obj] != null)
			{
				 objName = "globalSelect_"+iIndex;
                           if (form.elements[objName].checked)
                           {

                               sourceValue = form.elements[obj].value;
                               console.log("DEBUG-M: source value  "+sourceValue);
                               index = sourceValue.indexOf("ANALOG")-1;
                               prefix = 'A';
                               if (index < 0)
                               {
                                       index = sourceValue.indexOf("LINEGROUP")-1;
                                       prefix = '';
                               }
                               sourceName = sourceValue.substring(0, index);
                               console.log("sourceName: "+sourceName);
                               secondIndex = sourceValue.indexOf("-", index+1);
                               sourceTriggerName = sourceValue.substring(secondIndex+1);
                               console.log("DEBUG-M: sourceTriggerName "+sourceTriggerName);
                               objName = objPrefix+"name";
                               objId = objPrefix+"type";
                               // newName = prefix+sourceName+" - "+sourceTriggerName+" - "+form.elements[objId].value;
                               newName = sourceTriggerName+" - "+form.elements[objId].value;
                               // if (index < 0)
                               // {
                       //              index = sourceValue.indexOf("LINEGROUP")-1;
                       //              prefix = '';
                       //      }
                       //      if (sourceName == sourceTriggerName)
                       //      {
                       //              newName = prefix+sourceName+" - "+form.elements[objId].value;
                       //      }
                               console.log("DEBUG-M: new name "+newName);
                               form.elements[objName].value= newName;
                          }
			iIndex = iIndex + 1;
			objPrefix = 'lstTriggerChannels['+ iIndex +'].';
			objName = objPrefix+"name";
			obj=objPrefix+"channel";
			}
		}
	}
	function hideAssistance()
	{
		$('#assistRowDiv').hide();
		$('#globalSelectDiv').hide();
		 // $('#measurementTable tr td:nth-child(1)').hide();
		var form = document.forms["frmConfigureVirtualMeasurements"];
		var i = 0;
		var objPrefix = 'lstTriggerChannels['+ i +'].';
		var objName = objPrefix+"channel";

		while (form.elements[objName] != null)
		{
		   objName = "globalSelect_"+i;
		   $('#td_'+objName).hide();

		   $('#'+objName).hide();
		   i++;
		   objPrefix = 'lstTriggerChannels['+ i +'].';
		   objName = objPrefix+"channel";
		}

	}
	function showAssistance()
	{
		$('#assistRowDiv').show();
		$('#globalSelectDiv').show();
 		// $('#measurementTable tr td:nth-child(1)').show();
		var form = document.forms["frmConfigureVirtualMeasurements"];
		var i = 0;
		var objPrefix = 'lstTriggerChannels['+ i +'].';
		var objName = objPrefix+"channel";

		while (form.elements[objName] != null)
		{
		   objName = "globalSelect_"+i;
		   $('#td_'+objName).show();
		   $('#'+objName).show();
		   i++;
		   objPrefix = 'lstTriggerChannels['+ i +'].';
		   objName = objPrefix+"channel";
		}

	}
	function show_triggerType(obj) {
		obj = obj.name.substring(0, obj.name.indexOf('.'));
		name = "Trigger_Type_"+obj;
		$.publish(name);

	}

	function set_trigger(obj, value) {
		var form = document.forms["frmConfigureVirtualMeasurements"];
		var objPrefix = obj.name.substring(0, obj.name.indexOf('.')+1);
		var objTripOver = objPrefix+"tripIfOver";
		var objTripUnder = objPrefix+"tripIfUnder";
		var objName;
		if (value == 'Never')
		{
			objName = objPrefix+"triggerStatus";
			form.elements[objName].value = false;

			objName = objPrefix+"chatterLimit";
			form.elements[objName].disabled = true;
			form.elements[objName].value='';
			objName = objPrefix+"chatterRate";
			form.elements[objName].disabled = true;
			form.elements[objName].value='';
			objName = objPrefix+"triggerLimit";
			form.elements[objName].disabled = true;
			form.elements[objName].value='';
			objName = objPrefix+"disableTripOver";
			form.elements[objName].value=true;
			objName = objPrefix+"disableTripUnder";
			form.elements[objName].value=true;
			form.elements[objTripOver].disabled = true;
			form.elements[objTripUnder].disabled = true;
			objName = objPrefix+"duration";
			form.elements[objName].disabled = true;
			objName = objPrefix+"tripRocNeg";
			form.elements[objName].disabled = true;
			objName = objPrefix+"tripRocPos";
			form.elements[objName].disabled = true;
			objName = objPrefix+"disturbanceAlarm";
			form.elements[objName].disabled = true;

		}
		else
		{
			if (value == 'Over')
			{
				form.elements[objTripOver].disabled = false;
				form.elements[objTripUnder].disabled = true;
				form.elements[objTripUnder].value = '';
				objName = objPrefix+"disableTripOver";
				form.elements[objName].value=false;
				objName = objPrefix+"disableTripUnder";
				form.elements[objName].value=true;

			}
			else if (value == 'Under')
			{
				form.elements[objTripOver].disabled = true;
				form.elements[objTripOver].value = '';
				form.elements[objTripUnder].disabled = false;
				objName = objPrefix+"disableTripOver";
				form.elements[objName].value=true;
				objName = objPrefix+"disableTripUnder";
				form.elements[objName].value=false;

			}
			else if (value == 'Both')
			{
				form.elements[objTripOver].disabled = false;
				form.elements[objTripUnder].disabled = false;
				objName = objPrefix+"disableTripOver";
				form.elements[objName].value=false;
				objName = objPrefix+"disableTripUnder";
				form.elements[objName].value=false;

			}
			else if (value == 'ROC')
			{
				form.elements[objTripOver].disabled = true;
				form.elements[objTripUnder].disabled = true;
				objName = objPrefix+"disableTripOver";
				form.elements[objName].value=true;
				objName = objPrefix+"disableTripUnder";
				form.elements[objName].value=true;
			}
			var duration = form.elements[objPrefix+"duration"].value;
			form.elements[objPrefix+"duration"].disabled = false;
			objName = objPrefix+"disableDuration";
			form.elements[objName].value=false;
			console.log("ROC duration value "+duration);
			if (duration > 0)
			{
				form.elements[objPrefix+"tripRocNeg"].disabled = false;
				form.elements[objPrefix+"tripRocPos"].disabled = false;
				objName = objPrefix+"disableTripRoc";
				form.elements[objName].value=false;
			}
			else
			{
				form.elements[objPrefix+"tripRocNeg"].disabled = true;
				form.elements[objPrefix+"tripRocPos"].disabled = true;
				objName = objPrefix+"disableTripRoc";
				form.elements[objName].value=true;
			}


				objName = objPrefix+"triggerStatus";
				form.elements[objName].value = true;

			objName = objPrefix+"chatterLimit";
			form.elements[objName].disabled = false;
			form.elements[objName].value=chatterLimit
			objName = objPrefix+"chatterRate";
			form.elements[objName].disabled = false;
			form.elements[objName].value=chatterRate
			objName = objPrefix+"triggerLimit";
			form.elements[objName].disabled = false;
			form.elements[objName].value=triggerLimit
			objName = objPrefix+"disturbanceAlarm";
			form.elements[objName].disabled = false;

		}
	}


	function populate_trigger_name(obj, sourceValue)
	{
		var form = document.forms["frmConfigureVirtualMeasurements"];
		var objPrefix = obj.name.substring(0, obj.name.indexOf('.')+1);
		var index = sourceValue.indexOf("ANALOG")-1;
		var prefix = 'A';
		if (index < 0)
		{
			index = sourceValue.indexOf("LINEGROUP")-1;
			prefix = '';
		}
		var sourceName = sourceValue.substring(0, index);
		var secondIndex = sourceValue.indexOf("-", index+1);
		var sourceTriggerName = sourceValue.substring(secondIndex+1);
		var objName = objPrefix+"name";
		var objId = objPrefix+"type";
		var newName = prefix+sourceName+" - "+sourceTriggerName+" - "+form.elements[objId].value;
		if (sourceName == sourceTriggerName)
		{
			newName = prefix+sourceName+" - "+form.elements[objId].value;
		}
		form.elements[objName].value= newName;
	}

	function update_trigger_name(obj, value)
	{
		var form = document.forms["frmConfigureVirtualMeasurements"];
		var objPrefix = obj.name.substring(0, obj.name.indexOf('.')+1);

		var objName = objPrefix+"name";
		var oldVal = form.elements[objName].value
		var index = oldVal.lastIndexOf("-");
		var sourceTriggerName = oldVal.substring(0, index).trim();

		var newName = sourceTriggerName+" - "+value;
		form.elements[objName].value= newName;
	}

	function update_access(obj, value) {
		//dojo.event.topic.publish("show_harmonic");
		var objPrefix = obj.name.substring(0, obj.name.indexOf('.')+1);
		var form = document.forms["frmConfigureVirtualMeasurements"];

			if (value == 'Delete')
			{
				disable(objPrefix);
				form.elements[objPrefix+"status"].checked = false;
				form.elements[objPrefix+"status"].disabled = true;
				return;
			}
			if (form.elements[objPrefix+"status"].disabled == true)
			{
				form.elements[objPrefix+"status"].checked = true;
				form.elements[objPrefix+"status"].disabled = false;
				enable(objPrefix);
			}

			if (value == 'Magnitude' || value == 'Harmonic')
			{
				form.elements[objPrefix+"harmonic"].disabled = false;
				form.elements[objPrefix+"disableHarmonic"].value = false;
			}
			else
			{
				form.elements[objPrefix+"harmonic"].disabled = true;
				form.elements[objPrefix+"disableHarmonic"].value = true;
			}
			var index = obj.name.substring(obj.name.indexOf('[')+1, obj.name.indexOf(']'));
			if (value == 'Frequency')
			{
				if (form.elements[objPrefix+"pmuStatus"].checked == true)
				{
					form.elements[objPrefix+"freqPmuStatus"].disabled = false;
				}
			}
			else
			{
				form.elements[objPrefix+"freqPmuStatus"].checked = false;
				form.elements[objPrefix+"freqPmuStatus"].disabled = true;
			}

			if (value == 'Rms')
			{
				form.elements[objPrefix+"exportStatus"].disabled = true;
				form.elements[objPrefix+"exportStatus"].checked= true;

				form.elements[objPrefix+"ddrStatus"].disabled = true;
				form.elements[objPrefix+"ddrStatus"].checked= true;
			}
			else
			{
				form.elements[objPrefix+"exportStatus"].disabled = false;
				form.elements[objPrefix+"exportStatus"].checked = false;

				form.elements[objPrefix+"ddrStatus"].disabled = true;
				form.elements[objPrefix+"ddrStatus"].checked= false;
			}

			if (value == 'Phasor')
			{
				objName = objPrefix+"start";
				form.elements[objName].disabled = true;
				form.elements[objName].value = 'Never';
				var objName = objPrefix+"duration";
				form.elements[objName].disabled = true;
				form.elements[objName].value='';
				objName = objPrefix+"tripRocNeg";
				form.elements[objName].disabled = true;
				objName = objPrefix+"tripRocPos";
				form.elements[objName].disabled = true;
				form.elements[objName].value='';
				objName = objPrefix+"triggerStatus";
				form.elements[objName].value = false;

				objName = objPrefix+"chatterLimit";
				form.elements[objName].disabled = true;
				form.elements[objName].value='';
				objName = objPrefix+"chatterRate";
				form.elements[objName].disabled = true;
				form.elements[objName].value='';
				objName = objPrefix+"triggerLimit";
				form.elements[objName].disabled = true;
				form.elements[objName].value='';
				objName = objPrefix+"tripIfOver";
				form.elements[objName].disabled = true;
				form.elements[objName].value='';
				objName = objPrefix+"tripIfUnder";
				form.elements[objName].disabled = true;
				form.elements[objName].value='';
				objName = objPrefix+"disableTripOver";
				form.elements[objName].value=true;
				objName = objPrefix+"disableTripUnder";
				form.elements[objName].value=true;
				objName = objPrefix+"disableTripRoc";
				form.elements[objName].value=true;
				objName = objPrefix+"disableDuration";
				form.elements[objName].value=true;
				objName = objPrefix+"disturbanceAlarm";
				form.elements[objName].disabled = true;
			}
			else
			{
				objName = objPrefix+"start";
				form.elements[objName].disabled = false;
			}
		}
	function updateActiveStatus(value) {
		if (value==true)
		{
			checkAllActiveStatus();
		}
		else
		{
			uncheckAllActiveStatus();
		}
	}

	function checkAllActiveStatus() {
		var form = document.forms["frmConfigureVirtualMeasurements"];
		var i = 0;
		var objPrefix = 'lstTriggerChannels['+ i +'].';
		var objName = objPrefix+"status";
		while (form.elements[objName] != null)
		{
			if (form.elements[objName].disabled == false && form.elements[objName].checked == false)
			{
				form.elements[objName].checked=true;
				enable(objPrefix);
			}
			i = i + 1;
			objPrefix = 'lstTriggerChannels['+ i +'].';
			objName = objPrefix+"status";
		}
	}
	function uncheckAllActiveStatus() {
		var form = document.forms["frmConfigureVirtualMeasurements"];
		var i = 0;
		var objPrefix = 'lstTriggerChannels['+ i +'].';
		var objName = objPrefix+"status";
		while (form.elements[objName] != null)
		{
			if (form.elements[objName].disabled == false && form.elements[objName].checked == true)
			{
				form.elements[objName].checked=false;
				disable(objPrefix);
			}
			i = i + 1;
			objPrefix = 'lstTriggerChannels['+ i +'].';
			objName = objPrefix+"status";
		}
	}

	function isAllMeasurementsEnabled() {
		var form = document.forms["frmConfigureVirtualMeasurements"];
		var i = 0;
		var objPrefix = 'lstTriggerChannels['+ i +'].';
		var objName = objPrefix+"status";

		if (form.elements[objName] != null)
		{
			form.elements["activeCheckUncheck"].checked=true;
		}
		while (form.elements[objName] != null)
		{
			if (form.elements[objName].checked == false)
			{
				form.elements["activeCheckUncheck"].checked=false;
				break;
			}
			i = i + 1;
			objPrefix = 'lstTriggerChannels['+ i +'].';
			objName = objPrefix+"status";
		}

	}
		function changeState(obj, value) {
			var objNamePrefix = obj.name.substring(0, obj.name.indexOf('.')+1);
			if (value==true)
			{
				enable(objNamePrefix);
			}
			else
			{
				disable(objNamePrefix);
			}
			isAllMeasurementsEnabled();
		}
		function enableAllBeforeSubmit()
		{
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"name";

			while (form.elements[objName] != null)
			{
				enable(objPrefix);
				objName = objPrefix+"exportStatus";
				form.elements[objName].disabled = false;
				objName = objPrefix+"ddrStatus";
				form.elements[objName].disabled = false;
				objName = objPrefix+"disturbanceAlarm";
				form.elements[objName].disabled = false;
				i = i + 1;
				objPrefix = 'lstTriggerChannels['+ i +'].';
				objName = objPrefix+"name";
			}
		}
		function disable(objPrefix) {
			var objName = objPrefix+"name";
			var form = document.forms["frmConfigureVirtualMeasurements"];
			form.elements[objName].disabled = true;
			objName = objPrefix+"channel";
			form.elements[objName].disabled = true;
			objName = objPrefix+"phase";
			form.elements[objName].disabled = true;
			objName = objPrefix+"type";
			if (form.elements[objName].value != 'Delete')
			{
				form.elements[objName].disabled = true;
			}
			objName = objPrefix+"start";
			form.elements[objName].disabled = true;
			objName = objPrefix+"duration";
			form.elements[objName].disabled = true;
			objName = objPrefix+"tripIfOver";
			form.elements[objName].disabled = true;
			objName = objPrefix+"tripIfUnder";
			form.elements[objName].disabled = true;
			objName = objPrefix+"disableTripOver";
			form.elements[objName].value=true;
			objName = objPrefix+"disableTripUnder";
			form.elements[objName].value=true;
			objName = objPrefix+"disableTripRoc";
			form.elements[objName].value=true;
			objName = objPrefix+"disableDuration";
			form.elements[objName].value=true;

			objName = objPrefix+"triggerStatus";
			form.elements[objName].value = false;

			objName = objPrefix+"chatterLimit";
			form.elements[objName].disabled = true;
			objName = objPrefix+"chatterRate";
			form.elements[objName].disabled = true;
			objName = objPrefix+"triggerLimit";
			form.elements[objName].disabled = true;
			objName = objPrefix+"harmonic";
			form.elements[objName].disabled = true;
			objName = objPrefix+"average";
			form.elements[objName].disabled = true;
			objName = objPrefix+"pmuStatus";
			form.elements[objName].disabled = true;
			var index = objPrefix.substring(objPrefix.indexOf('[')+1, objPrefix.indexOf(']'));
			objName = objPrefix+"freqPmuStatus";
			form.elements[objName].disabled = true;
			objName = objPrefix+"exportStatus";
			form.elements[objName].disabled = true;
			objName = objPrefix+"ddrStatus";
			form.elements[objName].disabled = true;
			objName = objPrefix+"disturbanceAlarm";
			form.elements[objName].disabled = true;
		}

		function enable(objPrefix) {
			var objName = objPrefix+"name";
			var form = document.forms["frmConfigureVirtualMeasurements"];
			form.elements[objName].disabled = false;
			var objChannelName = objPrefix+"channel";
			form.elements[objChannelName].disabled = false;
			var value = form.elements[objChannelName].value;
			var type = value.substring(value.indexOf('-')+1);
			objName = objPrefix+"phase";
			form.elements[objName].disabled = false;
			var objTypeName = objPrefix+"type";
			form.elements[objTypeName].disabled = false;
			objName = objPrefix+"start";
			form.elements[objName].disabled = false;
			if (form.elements[objName].value != 'Never' )
			{
				if (form.elements[objName].value == 'Over' || form.elements[objName].value == 'Both')
				{
					form.elements[objPrefix+"tripIfOver"].disabled = false;
					objName = objPrefix+"disableTripOver";
					form.elements[objName].value=false;

				}
				if (form.elements[objName].value == 'Under' || form.elements[objName].value == 'Both')
				{
					form.elements[objPrefix+"tripIfUnder"].disabled = false;
					objName = objPrefix+"disableTripUnder";
					form.elements[objName].value=false;

				}

				// START: 29-July-2015 Enable ROC and duration for all conditions except for never
				var duration = form.elements[objPrefix+"duration"].value;
				form.elements[objPrefix+"duration"].disabled = false;
				objName = objPrefix+"disableDuration";
				form.elements[objName].value=false;

				if (duration)
				{
					if (Number.isInteger(duration) && parseInt(duration) > 0)
					{
						form.elements[objPrefix+"tripRocNeg"].disabled = false;
						form.elements[objPrefix+"tripRocPos"].disabled = false;
						objName = objPrefix+"disableTripRoc";
						form.elements[objName].value=true;
					}
				}



//				objName = objPrefix+"duration";
//				form.elements[objName].disabled = false;
//				objName = objPrefix+"tripIfGt";
//				form.elements[objName].disabled = false;
				objName = objPrefix+"triggerStatus";
				form.elements[objName].value = true;
				objName = objPrefix+"chatterLimit";
				form.elements[objName].disabled = false;
				objName = objPrefix+"chatterRate";
				form.elements[objName].disabled = false;
				objName = objPrefix+"triggerLimit";
				form.elements[objName].disabled = false;
				objName = objPrefix+"disturbanceAlarm";
				form.elements[objName].disabled = false;
			}
			objName = objPrefix+"harmonic";
			if (form.elements[objTypeName].value == 'Magnitude'  || form.elements[objTypeName].value == 'Harmonic')
			{
				form.elements[objName].disabled = false;
				form.elements[objPrefix+"disableHarmonic"].value = false;
			}
			objName = objPrefix+"average";
			form.elements[objName].disabled = false;
			objName = objPrefix+"pmuStatus";
			form.elements[objName].disabled = false;
			objName = objPrefix+"exportStatus";
			if (form.elements[objPrefix+"type"].value == 'Rms')
			{
				form.elements[objName].disabled = true;
				form.elements[objName].checked=true;

				form.elements[objPrefix+"ddrStatus"].disabled = true;
				form.elements[objPrefix+"ddrStatus"].checked=true;

			}
			else
			{
				form.elements[objName].disabled = false;
				form.elements[objPrefix+"ddrStatus"].disabled = false;

			}
			isAllExportsChecked();
			isAllDdrsChecked();
//			if (form.elements[objName].checked == false)
//			{
//				objName = objPrefix+"exportStatus";
//				form.elements[objName].disabled = false;
//				if (form.elements[objName].checked == true)
//				{
//					objName = objPrefix+"exportRate";
//					form.elements[objName].disabled = false;
//				}
//
//			}
//			else
			if (form.elements[objName].checked == true)
			{
				var index = objPrefix.substring(objPrefix.indexOf('[')+1, objPrefix.indexOf(']'));
				if (form.elements[objPrefix+"type"].value == 'Frequency')
				{
					form.elements[objPrefix+"freqPmuStatus"].disabled = false;
				}
				else
				{
					form.elements[objPrefix+"freqPmuStatus"].disabled = true;
				}
			}

		}

		function submitForm(sourceValue)
		{
			document.getElementById("checkBrowserClicks").value="false";
			// START: 14-Sept-2020 - Release 1.0.9.3 uses jquery to enable disabled components. 
			// It didn't work here. Explicitly enabling here
//			enableAllBeforeSubmit();
			$("#frmConfigureVirtualMeasurements :disabled").removeAttr('disabled');
			// END: 14-Sept-2020
			var booAnalogValue = document.getElementById("booAnalog").value;
			var booDigitalValue = document.getElementById("booDigital").value;
			document.forms["frmConfigureVirtualMeasurements"].booAnalog.value=booAnalogValue;
			document.forms["frmConfigureVirtualMeasurements"].booDigital.value=booDigitalValue;
			document.getElementById("sourceTab").value=sourceValue;
			document.forms["frmConfigureVirtualMeasurements"].sourceTab.value=sourceValue;
			document.getElementById("originTab").value="VirtualMeasurements";
			document.forms["frmConfigureVirtualMeasurements"].originTab.value="VirtualMeasurements";
			document.forms["frmConfigureVirtualMeasurements"].submit();
		}

		function updateExportStatus(value) {
			if (value==true)
			{
				checkAllExports();
			}
			else
			{
				uncheckAllExports();
			}
		}

		function checkAllExports() {
			var form = document.forms["frmConfigureVirtualMeasurements"];

			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";
			while (form.elements[objName] != null)
			{
//				if (form.elements[objName].disabled == false && form.elements[objPrefix+"pmuStatus"].checked == false)
				if (form.elements[objName].disabled == false && form.elements[objPrefix+"exportStatus"].disabled == false)
				{
					objName = objPrefix+"exportStatus";
					form.elements[objName].checked = true;
//					objName = objPrefix+"exportRate";
//					form.elements[objName].disabled = false;
				}
				i = i + 1;
				objPrefix = 'lstTriggerChannels['+ i +'].';
				objName = objPrefix+"channel";
			}

		}
		function uncheckAllExports() {
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
//				if (form.elements[objName].disabled == false  && form.elements[objPrefix+"pmuStatus"].checked == false)
				if (form.elements[objName].disabled == false && form.elements[objPrefix+"exportStatus"].disabled == false)
				{
					objName = objPrefix+"exportStatus";
					form.elements[objName].checked = false;
//					objName = objPrefix+"exportRate";
//					form.elements[objName].disabled = true;
				}
				i = i + 1;
				objPrefix = 'lstTriggerChannels['+ i +'].';
				objName = objPrefix+"channel";
			}
		}
		function isAllExportsChecked() {
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"exportStatus";

			if (form.elements[objName] != null)
			{
				form.elements["checkUncheck"].checked=true;
			}
			while (form.elements[objName] != null)
			{
				if (form.elements[objName].checked == false)
				{
					form.elements["checkUncheck"].checked=false;
					break;
				}
				i = i + 1;
				objPrefix = 'lstTriggerChannels['+ i +'].';
				objName = objPrefix+"exportStatus";
			}

		}
		function changeExportState(obj, value) {
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var objNamePrefix = obj.name.substring(0, obj.name.indexOf('.')+1);
			var objName = objNamePrefix+"ddrStatus";
			if (value==true)
			{
				form.elements[objName].disabled = false;
			}
			else
			{
				form.elements[objName].checked = false;
				form.elements[objName].disabled = true;
			}
			isAllExportsChecked();
		}

		function updatePmuStatus(value) {
			if (value==true)
			{
				checkAllPmus();
			}
			else
			{
				uncheckAllPmus();
			}
		}

		function checkAllPmus() {
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";
			while (form.elements[objName] != null)
			{
				if (form.elements[objName].disabled == false)
				{
					form.elements[objPrefix+"pmuStatus"].checked = true;
//					objName = objPrefix+"exportStatus";
//					form.elements[objName].checked = true;
//					form.elements[objName].disabled = true;
//					objName = objPrefix+"exportRate";
//					form.elements[objName].value=dataRate;
//					form.elements[objName].disabled = true;
					if (form.elements[objPrefix+"type"].value == 'Frequency')
					{
					 form.elements[objPrefix+"freqPmuStatus"].disabled = false;
					}
				}
				i = i + 1;
				objPrefix = 'lstTriggerChannels['+ i +'].';
				objName = objPrefix+"channel";
			}

		}
		function uncheckAllPmus() {
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
				if (form.elements[objName].disabled == false)
				{
					form.elements[objPrefix+"pmuStatus"].checked = false;
//					objName = objPrefix+"exportStatus";
//					form.elements[objName].disabled = false;
//					objName = objPrefix+"exportRate";
//					form.elements[objName].disabled = false;
					form.elements[objPrefix+"freqPmuStatus"].disabled = true;
				}
				i = i + 1;
				objPrefix = 'lstTriggerChannels['+ i +'].';
				objName = objPrefix+"channel";
			}
		}

		function isAllPmuChecked() {
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"pmuStatus";

			if (form.elements[objName] != null)
			{
				form.elements["pmuCheckUncheck"].checked=true;
			}
			while (form.elements[objName] != null)
			{
				if (form.elements[objPrefix+"channel"].disabled == false && form.elements[objName].checked == false)
				{
					form.elements["pmuCheckUncheck"].checked=false;
					break;
				}
				i = i + 1;
				objPrefix = 'lstTriggerChannels['+ i +'].';
				objName = objPrefix+"pmuStatus";
			}

		}
		function changePmuState(obj, value) {
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var objNamePrefix = obj.name.substring(0, obj.name.indexOf('.')+1);
			var index = obj.name.substring(obj.name.indexOf('[')+1, obj.name.indexOf(']'));
			if (value==true)
			{
//				form.elements[objNamePrefix+"exportStatus"].checked=true;
//				form.elements[objNamePrefix+"exportStatus"].disabled = true;
//				form.elements[objNamePrefix+"exportRate"].value=<%=pmuDataRate%>;
//				form.elements[objNamePrefix+"exportRate"].disabled = true;
				if (form.elements[objNamePrefix+"type"].value == 'Frequency')
				{
					form.elements[objNamePrefix+"freqPmuStatus"].disabled = false;
				}
			}
			else
			{
//				form.elements[objNamePrefix+"exportStatus"].disabled = false;
//				form.elements[objNamePrefix+"exportStatus"].checked=false;
//				form.elements[objNamePrefix+"exportRate"].disabled = true;
//				form.elements[objNamePrefix+"exportRate"].value='';
				form.elements[objNamePrefix+"freqPmuStatus"].disabled = true;
			}
			isAllPmuChecked();
		}

		function updateDdrStatus(value) {
			if (value==true)
			{
				checkAllDdrs();
			}
			else
			{
				uncheckAllDdrs();
			}
		}

		function checkAllDdrs() {
			var form = document.forms["frmConfigureVirtualMeasurements"];

			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";
			while (form.elements[objName] != null)
			{
//				if (form.elements[objName].disabled == false && form.elements[objPrefix+"pmuStatus"].checked == false)
				if (form.elements[objName].disabled == false && form.elements[objPrefix+"ddrStatus"].disabled == false)
				{
					objName = objPrefix+"ddrStatus";
					form.elements[objName].checked = true;
//					objName = objPrefix+"exportRate";
//					form.elements[objName].disabled = false;
				}
				i = i + 1;
				objPrefix = 'lstTriggerChannels['+ i +'].';
				objName = objPrefix+"channel";
			}

		}
		function uncheckAllDdrs() {
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
//				if (form.elements[objName].disabled == false  && form.elements[objPrefix+"pmuStatus"].checked == false)
				if (form.elements[objName].disabled == false && form.elements[objPrefix+"ddrStatus"].disabled == false)
				{
					objName = objPrefix+"ddrStatus";
					form.elements[objName].checked = false;
//					objName = objPrefix+"exportRate";
//					form.elements[objName].disabled = true;
				}
				i = i + 1;
				objPrefix = 'lstTriggerChannels['+ i +'].';
				objName = objPrefix+"channel";
			}
		}
		function isAllDdrsChecked() {
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"ddrStatus";

			if (form.elements[objName] != null)
			{
				form.elements["ddrCheckUncheck"].checked=true;
			}
			while (form.elements[objName] != null)
			{
				if (form.elements[objName].checked == false)
				{
					form.elements["ddrCheckUncheck"].checked=false;
					break;
				}
				i = i + 1;
				objPrefix = 'lstTriggerChannels['+ i +'].';
				objName = objPrefix+"ddrStatus";
			}

		}
		function changeDdrState(obj, value) {
//			var form = document.forms["frmConfigureVirtualMeasurements"];
//			var objNamePrefix = obj.name.substring(0, obj.name.indexOf('.')+1);
//			var objName = objNamePrefix+"exportRate";
//			if (value==true)
//			{
//				form.elements[objName].disabled = false;
//			}
//			else
//			{
//				form.elements[objName].disabled = true;
//			}
			isAllDdrsChecked();
		}

		function confirmationDialog(msg)
		{
			var retVal = confirm(msg);
			// START: 14-Sept-2020 - JQUERY code on the ready takes care of enabling before submit
//			if (retVal == true)
//			{
//				enableAllBeforeSubmit();
////				document.body.style.cursor='wait';
//
//			}
			// END: 14-Sept-2020
			return retVal;
		}
		function confirmationDialog(index, msg)
 		{
 			var retVal = confirm(msg);
 			if (retVal == true)
 			{
 				$.publish("confirmed"+index);
 				
 			}
 			return retVal;
 		}

		  function setFocus() {
			  var form = document.forms["frmConfigureVirtualMeasurements"];
			  var objName = 'lstTriggerChannels['+ addedIndex +'].name';
			  if (newAdded == true)
				{
				  form.elements[objName].focus();
				}
		  }


		$.subscribe("addNewMeasurementToTableTopic", function(event,data){
			alert("Event "+event.innerHTML+ "Data "+data);
			if(data == null)
			{
				return;
			}
			var startIndex = data.innerHTML.indexOf("<tr");
			var lastIndex = data.innerHTML.lastIndexOf("</tr>");
			$('#addMeasurement').focus();
			var newRow=data.innerHTML.substring(startIndex,lastIndex);
			alert("New row "+newRow);
			$('#measurementTable tbody').append(newRow);
//			$(data).appendTo(measurementTable);
			// var objName = 'globalSelect_'+ "<%=addedIndex%>";
			var form = document.forms["frmConfigureVirtualMeasurements"];
			if (form.elements['enableAssist'].checked)
			{
				showAssistance();
			}
			else
			{
				hideAssistance();
			}
			$('#addMeasurement').focus();
		});

		function updateGlobalName(value){
		var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
			   	objName = objPrefix+"name";
			   	if (form.elements[objName].disabled == false)
				{
			   		form.elements[objName].value=value;
			   	}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}

		function updateGlobalChannel(value){
			var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
				objName = objPrefix+"channel";
				if (form.elements[objName].disabled == false)
				{
					form.elements[objName].value=value;
					//show_triggerType(form.elements[objName]);
					$.publish("reloadTriggerType_"+i);
				}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}


		function updateGlobalAlgorithms(value){
			var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
				objName = objPrefix+"type";
				if (form.elements[objName].disabled == false)
				{
					var oldVal = form.elements[objName].value;
					form.elements[objName].value=value;
					if (form.elements[objName].value == null || form.elements[objName].value == '')
					{
						form.elements[objName].value = oldVal;
					}
					else
					{
						update_access(form.elements[objName], value);
						update_trigger_name(form.elements[objName], value);
					}
				}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}


		function updateTriggersStart(value){
		var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
				objName = objPrefix+"start";
				if (form.elements[objName].disabled == false)
				{
					form.elements[objName].value=value;
					set_trigger(form.elements[objName], value);

				}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}

		function updateTriggersUpperLimit(value){
			var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
				objName = objPrefix+"tripIfOver";
				if (form.elements[objName].disabled == false)
				{
					form.elements[objName].value=value;
				}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}

		function updateAverage(value){
			var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
				objName = objPrefix+"average";
				if (form.elements[objName].disabled == false)
				{
					form.elements[objName].value=value;
				}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}

		function updateHarmonic(value){
			var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
				objName = objPrefix+"harmonic";
				if (form.elements[objName].disabled == false)
				{
					form.elements[objName].value=value;
				}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}


		function updateTriggersLowerLimit(value){
			var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
				objName = objPrefix+"tripIfUnder";
				if (form.elements[objName].disabled == false)
				{
					form.elements[objName].value=value;
				}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}

		function updateChatterLimit(value){
			var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
				objName = objPrefix+"chatterLimit";
				if (form.elements[objName].disabled == false)
				{
					form.elements[objName].value=value;
				}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}

		function updateChatterRate(value){
			var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
				objName = objPrefix+"chatterRate";
				if (form.elements[objName].disabled == false)
				{
					form.elements[objName].value=value;
				}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}

		function updateTriggersLimit(value){
			var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
				objName = objPrefix+"triggerLimit";
				if (form.elements[objName].disabled == false)
				{
					form.elements[objName].value=value;
				}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}

		function updateExportRate(value){
			var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
				objName = objPrefix+"exportRate";
				if (form.elements[objName].disabled == false)
				{
					form.elements[objName].value=value;
				}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}

		function updateDuration(value){
			var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
				objName = objPrefix+"duration";
				if (form.elements[objName].disabled == false)
				{
					form.elements[objName].value=value;
				}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}

		function updateTripRocNeg(value){
			var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
				objName = objPrefix+"tripRocNeg";
				if (form.elements[objName].disabled == false)
				{
					form.elements[objName].value=value;
				}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}

		function updateTripRocPos(value){
			var i = 0;
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   if (form.elements[objName].checked)
			   {
				objName = objPrefix+"tripRocPos";
				if (form.elements[objName].disabled == false)
				{
					form.elements[objName].value=value;
				}
			   }
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
		}


		function updateAssistance(value) {
			if (value==true)
			{
				showAssistance();
			}
			else
			{
				hideAssistance();
			}
		}

		function selectAllToEdit(value) {
			var form = document.forms["frmConfigureVirtualMeasurements"];
			var i = 0;
			var objPrefix = 'lstTriggerChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   form.elements[objName].checked = value;
			   i++;
			   objPrefix = 'lstTriggerChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}
			
			function cancelNew(){
	     	      //$('#newVirtualDialog').dialog('close');
	     	      try
		      	     {
		      	     	 $.publish("closeNewDialogTopic");
		      	     }
		      catch(err) {
		      	         document.getElementById("demo").innerHTML = err.message;
			}
	     	      return false;
	     }
		}
		
		$("#dAlarmCheckUncheck").click(function() {
		var checkBoxes = $("input[type='checkbox'][name$='disturbanceAlarm']:not(:disabled)");
		checkBoxes.prop("checked", this.checked);
	});
