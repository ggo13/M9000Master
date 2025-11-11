$(document).ready(function(){

	 	isAllChecked();
	 	hideAssistance();
	 	$('#enableAssist').show();
	 	$('#tblButtonsTop').show();
	 	$('#tblButtonsBottom').show();
	 	if (!isDfrAddedOrRemoved)
        {
	 		$('#navMenu').show();
        }
	 	$('input:checkbox[id^="globalSelect_"]').shiftSelectable();
	 	$('input:checkbox[id$="exportStatus"]').shiftSelectable();
	 	$("[id$='inP2']").on("input", function() {
            var idStr = $(this).attr('id');
            var prefix = idStr.substring(0,idStr.indexOf(".")+1)+"range";
            if ($.isNumeric(this.value))
            {
           	 	$(jqSelector(prefix)).val(Math.abs(this.value));
			}

	 	});

//	 	$(function() {
//	        $('#analogTable tr').not(':first').hover(function() {
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
            console.log("Clicked..."+$(this).parents().siblings().html());
            $(this).parents().siblings().find('tr[id^=tr_] td').css({'background-color':'#EEEEFF','color':'black'});
            $(this).find('td').css({'background-color': '#4747a6','color':'cyan'});
            $(this).siblings().find('td').css({'background-color':'#EEEEFF','color':'black'});
    });

        $("*").css("cursor", "default");
        $(".traverseMenu").css('cursor', 'pointer');
        if (userRole != 'admin')
        {
//                alert("role "+'<s:property value="#session.userDetails.role"/>');
                $("#configureAnalogChannels :input").prop("disabled", true);
        }

$("table.configureTable tr[id^=tr_]").bind("paste", function(e){
	var clipboardText = e.originalEvent.clipboardData.getData("text/plain");;
	if (clipboardText.split(/\r\n|\r|\n/).length > 1) {
		e.stopPropagation();
		e.preventDefault();
		handlePasteEvent(e);
	}
});

	});


		function show_extshunt(obj, value) {
			//$.publish("show_harmonic");
			var objPrefix = obj.name.substring(0, obj.name.indexOf('.')+1);
				var objName = objPrefix+"extShunt";
				if (value == 'CurrentAcExternalShunt' || value == 'CurrentDcExternalShunt' || value == 'TransducerCurrentExternalShunt' || value == 'CurrentAcHallEffectExternalShunt')
				{
					document.forms["configureAnalogChannels"].elements[objName].disabled = false;
					objName = objPrefix+"disableExtSunt";
					document.forms["configureAnalogChannels"].elements[objName].value = false;
				}
				else
				{
					document.forms["configureAnalogChannels"].elements[objName].disabled = true;
					objName = objPrefix+"disableExtSunt";
					document.forms["configureAnalogChannels"].elements[objName].value = true;
				}

			}

		function show_tranducer(obj, value, index) {
            var objPrefix = obj.name.substring(0, obj.name.indexOf('.')+1);

            var primary = objPrefix+"primaryRatio";
            var secondary = objPrefix+"secondaryRatio";
            var primary_backup = "primaryRatio_backup_"+index;
            var secondary_backup = "secondaryRatio_backup_"+index;

			if (value == 'TransducerVoltage' || value == 'TransducerCurrentExternalShunt')
			{
				if (value == 'TransducerVoltage' )
				{
					$('#transducerType_'+index).html("<b>V</b>");
					$('#transducerType_'+index+"_"+index).html("<b>V</b>");
				}
				else
				{
					$('#transducerType_'+index).html("<b>A</b>");
					$('#transducerType_'+index+"_"+index).html("<b>A</b>");
				}
				console.log("Type: "+ $('#transducerType_'+index+"_"+index).html());

				$("#tr_sub_"+index).show();;
				$('#transducer_'+index).val(true);
                $('#transducer_'+index).val(true);
                console.log("ShowTransducer!! val "+objPrefix);
                
                console.log(" primary "+$(jqSelector(primary)).val());
                $(jqSelector(primary_backup)).val($(jqSelector(primary)).val());
                $(jqSelector(secondary_backup)).val($(jqSelector(secondary)).val());
                $(jqSelector(primary)).val("1");
                $(jqSelector(primary)).prop("readonly",true);
                $(jqSelector(secondary)).val("1");
                $(jqSelector(secondary)).prop("readonly",true);


			}
			else
			{
				$('#tr_sub_'+index).hide();
				console.log("hidden "+'#lstAnalogChannels['+index+'].tranducer'+$('#transducer_'+index).val());
				$('#transducer_'+index).val(false);
				if ($(jqSelector(primary_backup)).val())
				{
	                $(jqSelector(primary)).val($(jqSelector(primary_backup)).val());
                	$(jqSelector(secondary)).val($(jqSelector(secondary_backup)).val());
				}
				else
				{
					$(jqSelector(primary)).val("1");
					$(jqSelector(secondary)).val("1");
				}

                $(jqSelector(primary)).prop("readonly",false);
                $(jqSelector(secondary)).prop("readonly",false);

			}

		}
		function update_list() {
			$.publish("update_list");
			}

		function submitForm(sourceValue)
		{
                        if (userRole != 'admin')
                        {
				$("#configureAnalogChannels :input").prop("disabled", false);
			}
			document.getElementById("checkBrowserClicks").value="false";
			document.getElementById("sourceTab").value=sourceValue;
			var booAnalogValue = document.getElementById("booAnalog").value;
			var booDigitalValue = document.getElementById("booDigital").value;
			document.forms["configureAnalogChannels"].booAnalog.value=booAnalogValue;
			document.forms["configureAnalogChannels"].booDigital.value=booDigitalValue;
			document.forms["configureAnalogChannels"].sourceTab.value=sourceValue;
			document.getElementById("originTab").value="Analogs";
			document.forms["configureAnalogChannels"].originTab.value="Analogs";
			document.forms["configureAnalogChannels"].submit();
		}
		function push(value)
		{
		  alert(value);
		}
		function filter(field) {
		    return field.id == "circuitName";
		  }
		$.subscribe("/beforeLineGroups", function(event, tab, tabContainer){
			alert(tab);
			document.configureAnalogChannels.submit();
		});
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
			var form = document.forms["configureAnalogChannels"];
			var i = 0;
			var objPrefix = 'lstAnalogChannels['+ i +'].';
			var objName = objPrefix+"exportStatus";
			while (form.elements[objName] != null)
			{
				form.elements[objName].checked = true;
				i = i + 1;
				objPrefix = 'lstAnalogChannels['+ i +'].';
				objName = objPrefix+"exportStatus";
			}

		}
		function isAllChecked() {
			var form = document.forms["configureAnalogChannels"];
			var i = 0;
			var objPrefix = 'lstAnalogChannels['+ i +'].';
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
				objPrefix = 'lstAnalogChannels['+ i +'].';
				objName = objPrefix+"exportStatus";
			}

		}
		function uncheckAll() {
			var form = document.forms["configureAnalogChannels"];
			var i = 0;
			var objPrefix = 'lstAnalogChannels['+ i +'].';
			var objName = objPrefix+"exportStatus";

			while (form.elements[objName] != null)
			{
				form.elements[objName].checked = false;
				i = i + 1;
				objPrefix = 'lstAnalogChannels['+ i +'].';
				objName = objPrefix+"exportStatus";
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

			var form = document.forms["configureAnalogChannels"];
			var i = 0;
			var objPrefix = 'lstAnalogChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   $('#td_'+objName).hide();
			   $('#'+objName).hide();
			   i++;
			   objPrefix = 'lstAnalogChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}

		}
		function showAssistance()
		{
			$('#assistRowDiv').show();
			$('#globalSelectDiv').show();

			var form = document.forms["configureAnalogChannels"];
			var i = 0;
			var objPrefix = 'lstAnalogChannels['+ i +'].';
			var objName = objPrefix+"channel";

			while (form.elements[objName] != null)
			{
			   objName = "globalSelect_"+i;
			   $('#td_'+objName).show();
			   $('#'+objName).show()
			   i++;
			   objPrefix = 'lstAnalogChannels['+ i +'].';
			   objName = objPrefix+"channel";
			}

		}

	function selectAllToEdit(value) {
		var form = document.forms["configureAnalogChannels"];
		var i = 0;
		var objPrefix = 'lstAnalogChannels['+ i +'].';
		var objName = objPrefix+"channel";

		while (form.elements[objName] != null)
		{
		   objName = "globalSelect_"+i;
		   form.elements[objName].checked = value;
		   i++;
		   objPrefix = 'lstAnalogChannels['+ i +'].';
		   objName = objPrefix+"channel";
		}
	}
	function updateGlobal(fieldName, value){
		var i = 0;
		var form = document.forms["configureAnalogChannels"];
		var i = 0;
		var objPrefix = 'lstAnalogChannels['+ i +'].';
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
		   objPrefix = 'lstAnalogChannels['+ i +'].';
		   objName = objPrefix+"channel";
		}
	}

	// Seperate function for input type ao as to enable or disable ext Shunt
	function updateGlobalInputType(value){
		var i = 0;
		var form = document.forms["configureAnalogChannels"];
		var i = 0;
		var objPrefix = 'lstAnalogChannels['+ i +'].';
		var objName = objPrefix+"channel";

		while (form.elements[objName] != null)
		{
		   objName = "globalSelect_"+i;
		   if (form.elements[objName].checked)
		   {
			objName = objPrefix+"inputType";
			if (form.elements[objName].disabled == false)
			{
				form.elements[objName].value=value;
				show_extshunt(form.elements[objName], value);
			}
		   }
		   i++;
		   objPrefix = 'lstAnalogChannels['+ i +'].';
		   objName = objPrefix+"channel";
		}
	}
