$(document).ready(function () {
	$('input[type="text"]').get(1).focus();
	$("#advancedSettings").validate({
		rules: {
			"stationDetails.triggerDuration": { required: true, number: true, min: 10 },
			"stationDetails.contOscDaysToRetain": { required: true, number: true, min: 1 },
			"stationDetails.contMeasurementsDaysToRetain": { required: true, number: true, min: 1 },
			"stationDetails.contOscExportTimeLimit": { required: true, number: true, min: 1 },
			"stationDetails.contMeasurementsExportTimeLimit": { required: true, number: true, min: 1 }
		},
		messages: {
			"stationDetails.triggerDuration": { required: "Cannot be blank", number: "Numeric field", min: "Minimum 10 seconds" },
			"stationDetails.contOscDaysToRetain": { required: "Cannot be blank", number: "Numeric field", min: "Minimum 1 day" },
			"stationDetails.contMeasurementsDaysToRetain": { required: "Cannot be blank", number: "Numeric field", min: "Minimum 1 day" },
			"stationDetails.contOscExportTimeLimit": { required: "Cannot be blank", number: "Numeric field", min: "Minimum 1 Second" },
			"stationDetails.contMeasurementsExportTimeLimit": { required: "Cannot be blank", number: "Numeric field", min: "Minimum 1 Second" }
		}
	});
	if (userRole != 'admin') {
		$("#advancedSettings :input").prop("disabled", true);
		$('[id^=btnCancel]').prop("disabled", false);
	}
});


function updateRelaySettings(value) {
	if (value == '1') {
		$('#row_wetting_voltage').show();
	}
	else {
		$('#row_wetting_voltage').hide();
	}
}

function warnMeasurementsDays(obj, value) {
	if (value > 30) {
		if (!document.getElementById('spanmsgMeasurements')) {
			$(obj).after("<span id='spanmsgMeasurements' class='fixed' style='font-weight:bold;background-color:yellow;foreground-color:white'>Ensure enough disk space when retaining days more than 30 days</span>")
			$('#spanmsgMeasurements').show(0).delay("10000").hide(0);
		}
		else {
			$('#spanmsgMeasurements').show(0).delay("10000").hide(0);
		}
	}
	else {
		if (document.getElementById('spanmsgMeasurements')) {
			$('#spanmsgMeasurements').hide();
		}
	}
}

function warnOscDays(obj, value) {
	if (value > 5) {
		if (!document.getElementById('spanmsgOsc')) {
			$(obj).after("<span id='spanmsgOsc' class='fixed' style='font-weight:bold;background-color:yellow;foreground-color:white'>Ensure enough disk space when retaining days more than 5 days</span>")
			$('#spanmsgOsc').show(0).delay("10000").hide(0);
		}
		else {
			$('#spanmsgOsc').show(0).delay("10000").hide(0);
		}
	}
	else {
		if (document.getElementById('spanmsgOsc')) {
			$('#spanmsgOsc').hide();
		}
	}
}

function warnOscExportLimit(obj, value) {
	if (value > 600) {
		if (!document.getElementById('spanmsgOscLimit')) {
			$(obj).after("<span id='spanmsgOscLimit' class='fixed' style='font-weight:bold;background-color:yellow;foreground-color:white'>Exporting large amount of data might make browser unresponsive</span>")
			$('#spanmsgOscLimit').show(0).delay("10000").hide(0);
		}
		else {
			$('#spanmsgOscLimit').show(0).delay("10000").hide(0);
		}
	}
	else {
		if (document.getElementById('spanmsgOscLimit')) {
			$('#spanmsgOscLimit').hide();
		}
	}
}

function warnMeasurementExportLimit(obj, value) {
	if (value > 600) {
		if (!document.getElementById('spanmsgMeasurementLimit')) {
			$(obj).after("<span id='spanmsgMeasurementLimit' class='fixed' style='font-weight:bold;background-color:yellow;foreground-color:white'>Exporting large amount of data might make browser unresponsive</span>")
			$('#spanmsgMeasurementLimit').show(0).delay("10000").hide(0);
		}
		else {
			$('#spanmsgMeasurementLimit').show(0).delay("10000").hide(0);
		}
	}
	else {
		if (document.getElementById('spanmsgMeasurementLimit')) {
			$('#spanmsgMeasurementLimit').hide();
		}
	}
}

function showBusyCursor() {
	$("*").css("cursor", "progress");
}