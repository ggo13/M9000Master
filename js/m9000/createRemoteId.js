$(document).ready(function(){
        // alert(" pmu enabled? "+$('input:checkbox[name="stationDetails.pmuEnabled"]').is(':checked'));
        // alert('<s:property value="stationDetails.pdcStreamType"/>');
        if (isPmuEnabled == 'true')
	{
                showPmuDetails(pdcStreamType);
	}
        else
	{
                $('#pmuRowDiv').hide();
        }
        focus();
});

function focus()
{
	// alert("access mode "+accessMode+" is pmu enabled? "+isPmuEnabled+" pdcStreamType "+pdcStreamType);
		if(accessMode == 'Edit')
		{
			$('input[type="text"]').get(1).focus(); 
		}
	else
	{
		$("input:text:visible:first").focus();
	}

}
function showPmuDetails(streamType)
		{
				$('#pmuRowDiv').show();	
				if (streamType=='tcp')
				{
					enableTCP();
				}
				else
				{
					enableUDP();
				}
		}

		function show_sampleRate() {
		$.publish("sample_rate");
		}

		function filter(field) {
		    return field.name == "stationDetails.systemLineFrequency";
		  }
		
		function show_ltrSampleRate() {
			$.publish("ltr_sample_rate");
			}

			function filterLtr(field) {
			    return field.name == "stationDetails.systemSampleRate";
			  }
			
			function submitForm(sourceValue)
			{
				 if (userRole != 'admin')
        			{
					// $("#createStation :disabled").removeAttr('disabled');
					$("#createStation :input").prop("disabled", false);
					$("#stationDetails\\.systemAnalogChannelsCount").prop("disabled", true);
					$("#stationDetails\\.systemDigitalChannelsCount").prop("disabled", true);
				}
				document.getElementById("checkBrowserClicks").value="false";
				document.getElementById("sourceTab").value=sourceValue;
				var booAnalogValue = document.getElementById("booAnalog").value;
				var booDigitalValue = document.getElementById("booDigital").value;
				document.forms["createStation"].booAnalog.value=booAnalogValue;
				document.forms["createStation"].booDigital.value=booDigitalValue;
				document.forms["createStation"].sourceTab.value=sourceValue;
				document.getElementById("originTab").value="StationDetails";
				document.forms["createStation"].originTab.value="StationDetails";
				document.forms["createStation"].submit();
			}

        $.subscribe('checkAccess', function(event, data) {
                $('#tblButtonsTop').show();
                $('#tblButtonsBottom').show();
                if (!isDfrAddedOrRemoved)
                {
                	$('#navMenu').show();
                }
        if (userRole != 'admin')
        {
//                alert("role "+'<s:property value="#session.userDetails.role"/>');
                $("#createStation :input").prop("disabled", true);
        }
        });
 
        $('#createStation').submit(function(e) {
                //     $("#createStation :disabled").removeAttr('disabled');
                        $("#createStation :input").prop("disabled", false);
                });
function updatePmu(enablePmu,streamType) {
			if (enablePmu==true)
			{
				showPmuDetails(streamType);
				// $('#stationDetails.pdcId').prop('required', true);
				// $('#stationDetails.pdcMaxWait').prop('required', true);
				// $('#stationDetails.pdcTcpPort').prop('required', true);
				// $('#stationDetails.lstOfUDPserverPorts_0').prop('required', true);
			}
			else
			{
				$('#pmuRowDiv').hide();
				// $('#stationDetails.pdcId').prop('required', false);
				// $('#stationDetails.pdcMaxWait').prop('required', false);
				// $('#stationDetails.pdcTcpPort').prop('required', false);
				// $('#stationDetails.lstOfUDPserverPorts_0').prop('required', false);
			}
		}
		
function enableUDP()
{
	$('#tcpPort').prop("disabled", true); // Disable tcp
	$('#lstOfUDPserverPorts_0').prop("disabled", false);
	$('#lstOfUDPserverPorts_1').prop("disabled", false);
	$('#lstOfUDPserverPorts_2').prop("disabled", false);
	$('#lstOfUDPserverPorts_3').prop("disabled", false);
	$('#lstOfUDPserverPorts_4').prop("disabled", false);
//	for (var i =1; i < arrPorts.length;i++) {
//		$('#lstOfUDPserverPorts_'+i).prop("disabled", false);
//	}

}

function enableTCP()
{
	$('#tcpPort').prop("disabled", false);
	$('#lstOfUDPserverPorts_0').prop("disabled", true);
	$('#lstOfUDPserverPorts_1').prop("disabled", true);
	$('#lstOfUDPserverPorts_2').prop("disabled", true);
	$('#lstOfUDPserverPorts_3').prop("disabled", true);
	$('#lstOfUDPserverPorts_4').prop("disabled", true);
//	for (var i =1; i < arrPorts.length;i++) {
//		$('#lstOfUDPserverPorts_'+i).prop("disabled", true);
//	}
}
		
function updateProtocol(value) {
			if (value=='udp')
			{
				enableUDP()
			}
			else
			{
				enableTCP();
			}
		}

function updateRelaySettings(value)
{
	if (value=='1')
	{
		$('#row_wetting_voltage').show();	
	}
	else
	{
		$('#row_wetting_voltage').hide();
	}
}

function warnMeasurementsDays(obj, value)
{
	if (value > 30)
	{
		if (!document.getElementById('spanmsgMeasurements')) {
			$(obj).after("<span id='spanmsgMeasurements' class='fixed' style='font-weight:bold;background-color:yellow;foreground-color:white'>Ensure enough disk space when retaining days more than 30 days</span>")
			$('#spanmsgMeasurements').show(0).delay("10000").hide(0);
        }
        else {                    
        	$('#spanmsgMeasurements').show(0).delay("10000").hide(0);
        }
	}
	else
	{
		if (document.getElementById('spanmsgMeasurements'))
		{
			$('#spanmsgMeasurements').hide();
		}
	}
}

function warnOscDays(obj, value)
{
	if (value > 5)
	{
		if (!document.getElementById('spanmsg')) {
			$(obj).after("<span id='spanmsgOsc' class='fixed' style='font-weight:bold;background-color:yellow;foreground-color:white'>Ensure enough disk space when retaining days more than 5 days</span>")
			$('#spanmsgOsc').show(0).delay("10000").hide(0);
		}
		else {                    
			$('#spanmsgOsc').show(0).delay("10000").hide(0);
		}
	}
	else
	{
		if (document.getElementById('spanmsgOsc'))
		{
			$('#spanmsgOsc').hide();
		}
	}

}
