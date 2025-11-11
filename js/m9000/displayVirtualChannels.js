	$(document).ready(function(){
		isAllChecked();
		$('#buttonsTop').show();
		$('#buttonsBottom').show();
		if (!isDfrAddedOrRemoved)
        {
			$('#navMenu').show();
        }
		$('#btnNew').focus();
	$.validator.addMethod(
		    "regex1",
		    function(value, element, regexp) {
			var check = false;
			return this.optional(element) || regexp.test(value);
		    },
		    "Special Characters not allowed: , "
		);
	
	         var validator = $("#configureVirtualChannels").validate();
		 $('[name$="circuitName"]').each(function() {
		     $(this).rules('add', {
			 required: true,
			 regex1: /^[^,]+$/,
			 maxlength: 64,
			 messages:{
				required: 'Channel description is required',
				maxlength: 'Channel description cannot be more than 64 characters'
				}
		     });
		 });
		 
	 $("#virtualChannelsTable tr").click(function() {
		$(this).find('td').css({'background-color': '#4747a6','color':'cyan'});
		$(this).siblings().find('td').css({'background-color':'#EEEEFF','color':'black'});
	});

	if (userRole != 'admin')
        {
                $("#configureVirtualChannels :input").prop("disabled", true);
        }

	});	
	function submitForm(sourceValue)
	{
                if (userRole != 'admin')
                { 
                	$("#configureVirtualChannels :input").prop("disabled", false);
		}
		document.getElementById("checkBrowserClicks").value="false";
		document.getElementById("sourceTab").value=sourceValue;
		var booAnalogValue = document.getElementById("booAnalog").value;
		var booDigitalValue = document.getElementById("booDigital").value;
		document.forms["configureVirtualChannels"].booAnalog.value=booAnalogValue;
		document.forms["configureVirtualChannels"].booDigital.value=booDigitalValue;
		document.forms["configureVirtualChannels"].sourceTab.value=sourceValue;
		document.getElementById("originTab").value="Virtuals";
		document.forms["configureVirtualChannels"].originTab.value="Virtuals";
		document.forms["configureVirtualChannels"].submit();
	}
	$.subscribe("selectAllValuesToSubmit", function(data){
	var list = document.getElementById("lstSelectedVirtualChannels");
	selectAllOptionsExceptSome(list, "key", "HEADERNOTRANSFER");
//	for (var i = 0; i < list.options.length; i++) 
//	  {
	   //alert(list.options[i].value)
//	   list.options[i].selected = true;
//	  }
	});
	function okButton(){
	       $('#newVirtualDialog').dialog('close');
	       $.publish("submitSave");
	  }
	  function enableBeforeSubmit()
		{
		var list = document.getElementById("lstSelectedVirtualChannels");
		var returnValue = true;
		if (list != null && list.length > 2)
		{
			selectAllOptionsExceptSome(list, "key", "HEADERNOTRANSFER");
		}
		else
		{
			alert("Error message: Select at least 2 Analog channels");
			returnValue = false;
		}
//		for (var i = 0; i < list.options.length; i++) 
//		  {
		   //alert(list.options[i].value)
//		   list.options[i].selected = true;
//		  }	
		return returnValue;
	}
	    
	function selectAllOptionsList()
	{
		    var list = document.getElementById("lstSelectedVirtualChannels");
		selectAllOptionsExceptSome(list, "key", "HEADERNOTRANSFER");
	}
	
	   function cancelEdit(){
	     try
	     {
	     	 $.publish("closeEditDialogTopic");
	     }
	     catch(err) {
	         document.getElementById("demo").innerHTML = err.message;
		}
	      return false;
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
	     
	     $.subscribe("refreshVirtualTable", function(data){
	    	 $('#idEmptyRow').hide();
		        $('#virtualChannelsTable > tbody:last').append(data);
		        $('#newVirtualDialog').dialog('close');
		        
		    });
	     
	     $.subscribe("updateVirtualTable", function(data){
		        $('#editVirtualDialog').dialog('close');
		        
		    });
	     $.subscribe("errorSave", function(data){
	    	 $('#divEditVirtuals').html(data);	        
		    });
	       $.subscribe('beforeDelete',function(data){  
	      event.originalEvent.options.submit=false; 
	     	    	 if(confirm("Linegroups and Measurements using this virtual channels will get affected. Do you want to delete this now?"))
	     	    	 {
	     	    	     $.publish('confirmed');  
	     	    	  }
	    	 });
	    	 
	    	 function confirmationDialog(index, msg)
		 		{
		 			var retVal = confirm(msg);
		 			if (retVal == true)
		 			{
		 				$.publish("confirmed"+index);
		 				
		 			}
		 			return retVal;
		}
		function changeState(value) {
			if (value==true)
			{
				checkAll();
			}
			else
			{
				uncheckAll();
			}
		}
		
		function checkAll() {
			var form = document.forms["configureVirtualChannels"];
			var i = 0;
			var objPrefix = 'lstVirtualChannels['+ i +'].';
			var objName = objPrefix+"exportStatus";
			while (form.elements[objName] != null)
			{
				form.elements[objName].checked = true;
				i = i + 1;
				objPrefix = 'lstVirtualChannels['+ i +'].';
				objName = objPrefix+"exportStatus";
			}

		}
		function isAllChecked() {
			var form = document.forms["configureVirtualChannels"];
			var i = 0;
			var objPrefix = 'lstVirtualChannels['+ i +'].';
			var objName = objPrefix+"exportStatus";
			form.elements["CheckUncheck"].checked=true;
			while (form.elements[objName] != null)
			{
				if (form.elements[objName].checked == false)
				{
					form.elements["CheckUncheck"].checked=false;
					break;
				}
				i = i + 1;
				objPrefix = 'lstVirtualChannels['+ i +'].';
				objName = objPrefix+"exportStatus";
			}

		}
		function uncheckAll() {
			var form = document.forms["configureVirtualChannels"];
			var i = 0;
			var objPrefix = 'lstVirtualChannels['+ i +'].';			
			var objName = objPrefix+"exportStatus";

			while (form.elements[objName] != null)
			{
				form.elements[objName].checked = false;
				i = i + 1;
				objPrefix = 'lstVirtualChannels['+ i +'].';
				objName = objPrefix+"exportStatus";
			}
		}
		
	
		function populateDecisionLogic(triggerValue)
		{
		        var existDecisionLogic = $('#virtualLogic').val();
		        if (existDecisionLogic != '')
		        {
		           existDecisionLogic=existDecisionLogic+" ";
		        }
		    	var existDecisionLogic = $('#virtualLogic').val();
		    	if (triggerValue == 'Add')
		    	{
		    		var trigger = $('#analogs').val();
		    		console.log("values trigger "+trigger+" virt scale "+$("#virtualScale").val());
		    		//document.getElementById("virtualLogic").value=existDecisionLogic+""+triggerValue;
		    		if (existDecisionLogic == '')
		    		{
		    			$('#virtualLogic').val(trigger);
		    		}
		    		else
		    		{
		    			var virtualScaleIfAny = '';
		    			if (existDecisionLogic.trim().startsWith("("))
		    			{
		    				virtualScaleIfAny = existDecisionLogic.substring(existDecisionLogic.lastIndexOf(")")+1);	
		        			existDecisionLogic = existDecisionLogic.substring(0, existDecisionLogic.indexOf(")")) + " + "+trigger+")"+virtualScaleIfAny;
		    			}
		    			else
		    			{
		    				existDecisionLogic=existDecisionLogic+" + "+trigger;
		    			}
		    			$('#virtualLogic').val(existDecisionLogic);
		    		}
		    		$('#virtualScale').prop("disabled", false);
		    		$('#btnDelete').prop("disabled", false);
		             	$('#btnClear').prop("disabled", false);
		    	}
		    	else if (triggerValue == 'Subtract')
		    	{
		    		var trigger = $('#analogs').val();
		    		console.log("values trigger "+trigger+" virt scale "+$("#virtualScale").val());
		    		//document.getElementById("virtualLogic").value=existDecisionLogic+""+triggerValue;
		    		if (existDecisionLogic == '')
		    		{
		    			$('#virtualLogic').val(" - "+trigger);
		    		}
		    		else
		    		{
		    			var virtualScaleIfAny = '';
		    			if (existDecisionLogic.trim().startsWith("("))
		    			{
		    				virtualScaleIfAny = existDecisionLogic.substring(existDecisionLogic.lastIndexOf(")")+1);	
		        			existDecisionLogic = existDecisionLogic.substring(0, existDecisionLogic.indexOf(")")) + " - "+trigger+")"+virtualScaleIfAny;
		    			} 
		    			else
		    			{
		    				existDecisionLogic=existDecisionLogic+" - "+trigger;
		    			}
		    			$('#virtualLogic').val(existDecisionLogic);
		    		}
		    		$('#virtualScale').prop("disabled", false);
		    		$('#btnDelete').prop("disabled", false);
		            $('#btnClear').prop("disabled", false);
		    	}
		    	else if (triggerValue == 'Delete')
		    	{
		    		if (existDecisionLogic != '')
		    		{
		    			var lastIndex = existDecisionLogic.lastIndexOf(")"); // Indicates virtual scale presence in logic
		    			if (lastIndex == -1) // Logic without virtual scale
		    			{
							var requiredIndex = getLastIndexOfAddOrSubtract(existDecisionLogic);
		    				if (requiredIndex > 1)
		   					{
		        				existDecisionLogic = existDecisionLogic.substring(0,requiredIndex).trim();   					
		    					$('#virtualLogic').val(existDecisionLogic);
		   					}
		    				else // Deleted the last value
		   					{
		        				$('#virtualLogic').val('');
		        				$( "input[name$='virtualScale']" ).val('');
		        				$('#btnDelete').prop("disabled", true);
		                 		$('#btnClear').prop("disabled", true);
		   					}
		    			}
		    			else
		   				{
		   					var virtualScaleStr = existDecisionLogic.substring(lastIndex+1);
		   					var logicWithoutScale = existDecisionLogic.substring(existDecisionLogic.indexOf("(")+1,lastIndex);
		   					var requiredIndex = getLastIndexOfAddOrSubtract(logicWithoutScale);
		    				if (requiredIndex > 1)
		   					{
		        				existDecisionLogic = existDecisionLogic.substring(0,requiredIndex).trim();   					
		    					$('#virtualLogic').val(existDecisionLogic+")"+virtualScaleStr);
		   					}
		    				else // Deleted the last value
		   					{
		        				$('#virtualLogic').val('');
		        				$( "input[name$='virtualScale']" ).val('');
		        				$('#btnDelete').prop("disabled", true);
		                 		$('#btnClear').prop("disabled", true);
		   					}
		   				}
		    		}
		    	}
		    	else if (triggerValue == 'Clear')
		    	{
		    		$('#virtualLogic').val('');
		    		$( "input[name$='virtualScale']" ).val('');
		    	}

		}
		function getLastIndexOfAddOrSubtract(virtualLogic){
			var lastIndexAdd = virtualLogic.lastIndexOf("+"); 
			var lastIndexSub = virtualLogic.lastIndexOf("-");
			var requiredIndex = -1;
			if (lastIndexAdd > lastIndexSub)
				{
				requiredIndex = lastIndexAdd;
				}
			else
			{
				requiredIndex = lastIndexSub; 
			}

			return requiredIndex;
		}
		
		function enableOperations()
		{
			$('#btnAdd').prop("disabled", false);
			$('#btnSub').prop("disabled", false);
		}
		function updateVirtualScale(currObj, virtualScale)
		{
			var existDecisionLogic = $('#virtualLogic').val();
			var lastIndex = existDecisionLogic.lastIndexOf(")"); // Indicates virtual scale presence in logic
			var virtualLogicWithoutScale = existDecisionLogic;
			if (lastIndex != -1)
			{
				virtualLogicWithoutScale = existDecisionLogic.substring(0, lastIndex+1);
			}
			console.log("Updated scale "+virtualScale+" original "+virtualLogicWithoutScale);
			if ($(currObj).valid())
			{
				if (virtualScale != '')
				{
					if (lastIndex == -1) // Adding paranthesis before adding virtual scale
					{
						virtualLogicWithoutScale="("+existDecisionLogic+")";
					}
					existDecisionLogic = virtualLogicWithoutScale +" * " + virtualScale;
					$('#virtualLogic').val(existDecisionLogic.trim());
				}
				else
				{
					if (lastIndex != -1)
					{
						$('#virtualLogic').val(existDecisionLogic.substring( existDecisionLogic.indexOf("(")+1,lastIndex));	
					}
				}
			}
		}