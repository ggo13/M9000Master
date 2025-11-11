$(document).ready(function() {

	isAllDfrChecked();
	isAllSerChecked();
	hideAssistance();
	//		 	hideTriggersForNow();
	$('#enableAssist').show();
	$('#tblButtonsTop').show();
	$('#tblButtonsBottom').show();
	if (!isDfrAddedOrRemoved) {
		$('#navMenu').show();
	}
	$('input:checkbox[id^="globalSelect_"]').shiftSelectable();
	$('input:checkbox[id$="ser"]').shiftSelectable();
	$('input:checkbox[id$="dfr"]').shiftSelectable();
	$('input:checkbox[id$="pmu"]').shiftSelectable();
	$('input:checkbox[id$="debounce"]').shiftSelectable();

	//		$(function() {
	//	        $('#eventsTable tr').not(':first').hover(function() {
	////	            $(this).css('background-color', '#FFFF99');
	//	            $(this).contents('td').css({'border': '1px solid #bdbdeb', 'border-left': 'none', 'border-right': 'none'});
	//	            $(this).contents('td:first').css('border-left', '1px solid #bdbdeb');
	//	            $(this).contents('td:last').css('border-right', '1px solid #bdbdeb');
	//	        },
	//	        function() {
	////	            $(this).css('background-color', '#FFFFFF');
	//	            $(this).contents('td').css('border', 'none');
	//	        });
	//	    });


	$("table.configureTable tr[id^=tr_]").click(function() {
		console.log("Clicked..." + $(this).parents().siblings().html());
		$(this).parents().siblings().find('td').css({ 'background-color': '#EEEEFF', 'color': 'black' });
		$(this).find('td').css({ 'background-color': '#4747a6', 'color': 'cyan' });
		$(this).siblings().find('td').css({ 'background-color': '#EEEEFF', 'color': 'black' });
	});

	$("*").css("cursor", "default");
	$(".traverseMenu").css('cursor', 'pointer');
	if (userRole != 'admin') {
		$("#configureEventChannels :input").prop("disabled", true);
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

	function updateDfrStartStatus(obj, value)
	{
		var objPrefix = obj.name.substring(0, obj.name.indexOf('.')+1);
		changeDfrStartStatus(objPrefix, value);
	}
	function changeDfrStartStatus(objPrefix, value)
	{
		var objName = objPrefix+"dfrStart";
		if (value == true)
		{
			document.forms["configureEventChannels"].elements[objName].disabled = false;
		}
		else
		{
			document.forms["configureEventChannels"].elements[objName].disabled = true;
		}
	}
/*		function change_status(obj, value) {
			var objPrefix = obj.name.substring(0, obj.name.indexOf('.')+1);
			update_trigger_status(objPrefix, value);
		}
		function update_trigger_status(objPrefix, value)
		{
			if (value == 'DFROnly')
			{
				var objName = objPrefix+"serRun";
				document.forms["configureEventChannels"].elements[objName].disabled = true;
				document.forms["configureEventChannels"].elements[objName].value="0";
				objName = objPrefix+"dfrStart";
				document.forms["configureEventChannels"].elements[objName].disabled = false;
				if (document.forms["configureEventChannels"].elements[objName].value != 'Disabled')
				{
					objName = objPrefix+"triggerStatus";
					document.forms["configureEventChannels"].elements[objName].disabled = false;
				}
			}
			else if (value == 'SEROnly')
			{
				var objName = objPrefix+"serRun";
				document.forms["configureEventChannels"].elements[objName].disabled = false;
				document.forms["configureEventChannels"].elements[objName].value="1";
				objName = objPrefix+"dfrStart";
				document.forms["configureEventChannels"].elements[objName].disabled = true;
				objName = objPrefix+"triggerStatus";
				document.forms["configureEventChannels"].elements[objName].disabled = true;
			}
			else
			{
				var objName = objPrefix+"serRun";
				document.forms["configureEventChannels"].elements[objName].disabled = false;
				document.forms["configureEventChannels"].elements[objName].value="1";
				objName = objPrefix+"dfrStart";
				document.forms["configureEventChannels"].elements[objName].disabled = false;
				if (document.forms["configureEventChannels"].elements[objName].value != 'Disabled')
				{
					objName = objPrefix+"triggerStatus";
					document.forms["configureEventChannels"].elements[objName].disabled = false;
				}
			}

		}

		function change_ser(obj, value) {
			var objPrefix = obj.name.substring(0, obj.name.indexOf('.')+1);
			var objName = objPrefix+"dfrSer";
			if (value == '0')
			{
				document.forms["configureEventChannels"].elements[objName].value="DFROnly";
				obj.disabled = true;
			}
		}

		function change_triggerstatus(obj, value) {
			var objPrefix = obj.name.substring(0, obj.name.indexOf('.')+1);
			var objName = objPrefix+"triggerStatus";
			if (value == 'Disabled')
			{
				document.forms["configureEventChannels"].elements[objName].disabled = true;
			}
			else
			{
				document.forms["configureEventChannels"].elements[objName].disabled = false;
			}
		}	*/

		function submit()
		{
		  document.configureChannels.submit();
		}
		function filter(field) {
		    return field.id == "circuitName";
		  }

		function submitForm(sourceValue)
		{
 	               	if (userRole != 'admin')
        	       	{
				$("#configureEventChannels :input").prop("disabled", false);
			}
			document.getElementById("checkBrowserClicks").value="false";
			var booAnalogValue = document.getElementById("booAnalog").value;
			var booDigitalValue = document.getElementById("booDigital").value;
			document.forms["configureEventChannels"].booAnalog.value=booAnalogValue;
			document.forms["configureEventChannels"].booDigital.value=booDigitalValue;

			document.getElementById("sourceTab").value=sourceValue;
			document.forms["configureEventChannels"].sourceTab.value=sourceValue;
			document.getElementById("originTab").value="Events";
			document.forms["configureEventChannels"].originTab.value="Events";
			document.forms["configureEventChannels"].submit();
		}
		function changeDfrState(value) {
			if (value==true)
			{
				checkAllDfr();
			}
			else
			{
				uncheckAllDfr();
			}
		}

		function changeSerState(value) {
			if (value==true)
			{
				checkAllSer();
			}
			else
			{
				uncheckAllSer();
			}
		}

		function isAllDfrChecked() {
			var form = document.forms["configureEventChannels"];
			var i = 0;
			var objPrefix = 'lstEventChannels['+ i +'].';
			var objName = objPrefix+"dfr";
			form.elements["dfrCheckUncheck"].checked=true;
			while (form.elements[objName] != null)
			{
				if (form.elements[objName].checked == false)
				{
					form.elements["dfrCheckUncheck"].checked=false;
					break;
				}
				i = i + 1;
				objPrefix = 'lstEventChannels['+ i +'].';
				objName = objPrefix+"dfr";
			}

		}
		function isAllSerChecked() {
			var form = document.forms["configureEventChannels"];
			var i = 0;
			var objPrefix = 'lstEventChannels['+ i +'].';
			var objName = objPrefix+"ser";
			form.elements["serCheckUncheck"].checked=true;
			while (form.elements[objName] != null)
			{
				if (form.elements[objName].checked == false)
				{
					form.elements["serCheckUncheck"].checked=false;
					break;
				}
				i = i + 1;
				objPrefix = 'lstEventChannels['+ i +'].';
				objName = objPrefix+"ser";
			}

		}

		function checkDfrAll() {
			var form = document.forms["configureEventChannels"];
			var i = 0;
			var objPrefix = 'lstEventChannels['+ i +'].';
			var objName = objPrefix+"triggerStatus";
			while (form.elements[objName] != null)
			{
				form.elements[objName].checked = true;
				i = i + 1;
				objPrefix = 'lstEventChannels['+ i +'].';
				objName = objPrefix+"triggerStatus";
			}

		}

		function isAllChecked() {
			var form = document.forms["configureEventChannels"];
			var i = 0;
			var objPrefix = 'lstEventChannels['+ i +'].';
			var objName = objPrefix+"triggerStatus";
			form.elements["CheckUncheck"].checked=true;
			while (form.elements[objName] != null)
			{
				if (form.elements[objName].checked == false)
				{
					form.elements["CheckUncheck"].checked=false;
					break;
				}
				i = i + 1;
				objPrefix = 'lstEventChannels['+ i +'].';
				objName = objPrefix+"triggerStatus";
			}

		}

		function checkAllDfr() {
			var form = document.forms["configureEventChannels"];
			var i = 0;
			var objPrefix = 'lstEventChannels['+ i +'].';
			var objName = objPrefix+"dfr";
			while (form.elements[objName] != null)
			{
				form.elements[objName].checked = true;
				changeDfrStartStatus(objPrefix, true);
				i = i + 1;
				objPrefix = 'lstEventChannels['+ i +'].';
				objName = objPrefix+"dfr";
			}

		}


		function uncheckAllDfr() {
			var form = document.forms["configureEventChannels"];
			var i = 0;
			var objPrefix = 'lstEventChannels['+ i +'].';
			var objName = objPrefix+"dfr";

			while (form.elements[objName] != null)
			{
				form.elements[objName].checked = false;
				changeDfrStartStatus(objPrefix, false)
				i = i + 1;
				objPrefix = 'lstEventChannels['+ i +'].';
				objName = objPrefix+"dfr";
			}
		}

		function checkAllSer() {
			var form = document.forms["configureEventChannels"];
			var i = 0;
			var objPrefix = 'lstEventChannels['+ i +'].';
			var objName = objPrefix+"ser";
			while (form.elements[objName] != null)
			{
				form.elements[objName].checked = true;
				i = i + 1;
				objPrefix = 'lstEventChannels['+ i +'].';
				objName = objPrefix+"ser";
			}

		}


		function uncheckAllSer() {
			var form = document.forms["configureEventChannels"];
			var i = 0;
			var objPrefix = 'lstEventChannels['+ i +'].';
			var objName = objPrefix+"ser";

			while (form.elements[objName] != null)
			{
				form.elements[objName].checked = false;
				i = i + 1;
				objPrefix = 'lstEventChannels['+ i +'].';
				objName = objPrefix+"ser";
			}
		}

		// Assistance
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

		function hideAssistance()
		{
			$('#assistRowDiv').hide();
			$('#globalSelectDiv').hide();

			var form = document.forms["configureEventChannels"];
			var i = 0;
			var objPrefix = 'lstEventChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   $('#td_'+objName).hide();
			   $('#'+objName).hide();
			   i++;
			   objPrefix = 'lstEventChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}

		}
		function showAssistance()
		{
			$('#assistRowDiv').show();
			$('#globalSelectDiv').show();

			var form = document.forms["configureEventChannels"];
			var i = 0;
			var objPrefix = 'lstEventChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   $('#td_'+objName).show();
			   $('#'+objName).show();
			   i++;
			   objPrefix = 'lstEventChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}

		}

	function hideTriggersForNow()
	{
		$('#triggerHeader').hide();

		var form = document.forms["configureEventChannels"];
		var i = 0;
		var objPrefix = 'lstEventChannels['+ i +'].';
		var objName = objPrefix+"channel";

		while (form.elements[objName] != null)
		{
		   objName = "triggerStatus_"+i;
		   $('#'+objName).hide();
		   i++;
		   objPrefix = 'lstEventChannels['+ i +'].';
		   objName = objPrefix+"channel";
		}

	}

	function selectAllToEdit(value) {
		var form = document.forms["configureEventChannels"];
		var i = 0;
		var objPrefix = 'lstEventChannels['+ i +'].';
		var objName = objPrefix+"channel";

		while (form.elements[objName] != null)
		{
		   objName = "globalSelect_"+i;
		   form.elements[objName].checked = value;
		   i++;
		   objPrefix = 'lstEventChannels['+ i +'].';
		   objName = objPrefix+"channel";
		}
	}
	function updateGlobal(fieldName, value){
		var i = 0;
		var form = document.forms["configureEventChannels"];
		var i = 0;
		var objPrefix = 'lstEventChannels['+ i +'].';
		var objName = objPrefix+"channel";

		while (form.elements[objName] != null)
		{
		   objName = "globalSelect_"+i;
		   if (form.elements[objName].checked)
		   {
			objName = objPrefix+fieldName;
			if (form.elements[objName].disabled == false)
			{
				form.elements[objName].value=value;
			}
		   }
		   i++;
		   objPrefix = 'lstEventChannels['+ i +'].';
		   objName = objPrefix+"channel";
		}
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
		var form = document.forms["configureEventChannels"];
		var i = 0;
		var objPrefix = 'lstEventChannels['+ i +'].';
		var objName = objPrefix+"channel";
		while (form.elements[objName] != null)
		{
			form.elements[objPrefix+"pmu"].checked = true;
			i = i + 1;
			objPrefix = 'lstEventChannels['+ i +'].';
			objName = objPrefix+"channel";
		}

	}
	function uncheckAllPmus() {
		var form = document.forms["configureEventChannels"];
		var i = 0;
		var objPrefix = 'lstEventChannels['+ i +'].';
		var objName = objPrefix+"channel";

		while (form.elements[objName] != null)
		{
			form.elements[objPrefix+"pmu"].checked = false;
			i = i + 1;
			objPrefix = 'lstEventChannels['+ i +'].';
			objName = objPrefix+"channel";
		}
	}

	function checkAllPmu(value) {
		var form = document.forms["configureEventChannels"];
		var i = 0;
		var objPrefix = 'lstEventChannels['+ i +'].';
		var objName = objPrefix+"pmu";
		if (value == true)
		{
			if (form.elements[objName] != null)
			{
				form.elements["pmuCheckUncheck"].checked=true;
			}
			while (form.elements[objName] != null)
			{
				if (form.elements[objName].checked == false)
				{
					form.elements["pmuCheckUncheck"].checked=false;
					break;
				}
				i = i + 1;
				objPrefix = 'lstEventChannels['+ i +'].';
				objName = objPrefix+"pmu";
			}
		}
		else
		{
			form.elements["pmuCheckUncheck"].checked=false;
		}

	}


	function updateDebounceStatus(value) {
		if (value==true)
		{
			checkAllDebounce();
		}
		else
		{
			uncheckAllDebounce();
		}
	}

	function checkAllDebounce() {
		var form = document.forms["configureEventChannels"];
		var i = 0;
		var objPrefix = 'lstEventChannels['+ i +'].';
		var objName = objPrefix+"channel";
		while (form.elements[objName] != null)
		{
			form.elements[objPrefix+"debounce"].checked = true;
			i = i + 1;
			objPrefix = 'lstEventChannels['+ i +'].';
			objName = objPrefix+"channel";
		}

	}
	function uncheckAllDebounce() {
		var form = document.forms["configureEventChannels"];
		var i = 0;
		var objPrefix = 'lstEventChannels['+ i +'].';
		var objName = objPrefix+"channel";

		while (form.elements[objName] != null)
		{
			form.elements[objPrefix+"debounce"].checked = false;
			i = i + 1;
			objPrefix = 'lstEventChannels['+ i +'].';
			objName = objPrefix+"channel";
		}
	}

	function verifyAllDebounce(value) {
		var form = document.forms["configureEventChannels"];
		var i = 0;
		var objPrefix = 'lstEventChannels['+ i +'].';
		var objName = objPrefix+"debounce";
		if (value == true)
		{
			if (form.elements[objName] != null)
			{
				form.elements["debounceCheckUncheck"].checked=true;
			}
			while (form.elements[objName] != null)
			{
				if (form.elements[objName].checked == false)
				{
					form.elements["debounceCheckUncheck"].checked=false;
					break;
				}
				i = i + 1;
				objPrefix = 'lstEventChannels['+ i +'].';
				objName = objPrefix+"debounce";
			}
		}
		else
		{
			form.elements["debounceCheckUncheck"].checked=false;
		}

	}
