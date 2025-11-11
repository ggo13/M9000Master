var isDirty = false;
$(document).ready(function(){
	$('[id^="btnSave"]').prop("disabled", true );
		if ($('#enableEmail').is(':checked'))
		{
			console.log("Enable all as enable email is checked");
			enableEmailConfigurations();
		}
		else
		{
			console.log("Disable all as enable email is unchecked");
			disableEmailConfigurations();
		}
		
		$('#emailForm').data('initial-state', $('#emailForm').serialize());

	    $(window).on('beforeunload', function() {
		console.log("isDirty? "+isDirty);
	        if (isDirty === true && $('#emailForm').serialize() != $('#emailForm').data('initial-state')){
			console.log("Inside IF isDirty? "+isDirty);
	            return 'You have unsaved changes which will not be saved.'
	        }
	    });
	$('#emailForm').find("input,button,textarea").on("keyup change",function() {
		console.log("Input change tracked..."+$('[id^="btnSave"]').html()+" ???"+$('.formButton').html());
		if ($('#emailForm').serialize() != $('#emailForm').data('initial-state'))
		{
			$('[id^="btnSave"]').prop("disabled", false );
			$('[id^="btnSave"]').removeClass("ui-button-disabled ui-state-disabled");
		}
		else
		{
			$('[id^="btnSave"]').prop("disabled", true );
			$('[id^="btnSave"]').addClass("ui-button-disabled ui-state-disabled");
		}
		
		isDirty=true;
	});
	});	


	$("#enableEmail").change(function(){
		if ($('#enableEmail').is(':checked'))
		{
			console.log("Enable all as enable email is checked");
			enableEmailConfigurations();
		}
		else
		{
			console.log("Disable all as enable email is unchecked");
			disableEmailConfigurations();
		}
	});
	
	$("#email_enableDailyStatusEmails").change(function(){
		if ($('#email_enableDailyStatusEmails').is(':checked'))
		{
			console.log("Enable daily status properties as it is checked");
			$('[id^="eds_"]').prop("readonly", false );
			$('[id^="eds_"]').removeClass("ui-button-disabled ui-state-disabled");
//			$('[name="emailReportsSettingsDTO.dailyStatusReportTime"]').closest('tr').find("input,button,textarea").prop("disabled", false );
			$(".ui-datepicker-trigger").prop("disabled", false );
		}
		else
		{
			console.log("Disable daily status properties as it is unchecked");
			$('[id^="eds_"]').prop("readonly", true );
			$('[id^="eds_"]').addClass("ui-button-disabled ui-state-disabled");
//			$('[name="emailReportsSettingsDTO.dailyStatusReportTime"]').closest('tr').find("input,button,textarea").prop("disabled", true );
			$(".ui-datepicker-trigger").prop("disabled", true );
		}
	});
	$("#email_enableSerEmail").change(function(){
		if ($('#email_enableSerEmail').is(':checked'))
		{
			console.log("Enable ser  email properties as it is checked");
			$('[id^="emailser_"]').prop("readonly", false );
			$('[id^="emailser_"]').removeClass("ui-button-disabled ui-state-disabled");
		}
		else
		{
			console.log("Disable ser properties as it is unchecked");
			$('[id^="emailser_"]').prop("readonly", true );
			$('[id^="emailser_"]').addClass("ui-button-disabled ui-state-disabled");
		}
	})
	$("#email_enableFaultEmail").change(function(){
		if ($('#email_enableFaultEmail').is(':checked'))
		{
			console.log("Enable fault email properties as it is checked");
			$('[id^="emailfault_"]').prop("readonly", false );
			$('[id^="emailfault_"]').removeClass("ui-button-disabled ui-state-disabled");
			if ($('#emailfault_enableFaultsWithAttachment').is(':checked'))
			{
				$('#email_fault_faultEmailAttachementSizeLimit').prop("readonly", false );
				$('#email_fault_faultEmailAttachementSizeLimit').removeClass("ui-button-disabled ui-state-disabled");
			}
			else
			{
				$('#email_fault_faultEmailAttachementSizeLimit').prop("readonly", true );
				$('#email_fault_faultEmailAttachementSizeLimit').removeClass("ui-button-disabled ui-state-disabled");
			}
		}
		else
		{
			console.log("Disable fault properties as it is unchecked");
			$('[id^="emailfault_"]').prop("readonly", true );
			$('[id^="emailfault_"]').addClass("ui-button-disabled ui-state-disabled");
		}
	});
	function enableEmailConfigurations()
	{
		// Enable the manage emails tab as well
		$('#emailSettingtabs').tabs('enable', 1);
		$('[id^="email_"]').prop("readonly", false );
		$('[id^="email_"]').removeClass("ui-button-disabled ui-state-disabled");
		if ($('#email_enableDailyStatusEmails').is(':checked'))
		{
			$('[id^="eds_"]').prop("readonly", false );
			$('[id^="eds_"]').removeClass("ui-button-disabled ui-state-disabled");
		}
		else
		{
			$('[id^="eds_"]').prop("readonly", true );
			$('[id^="eds_"]').addClass("ui-button-disabled ui-state-disabled");
		}
		if ($('#email_enableSerEmail').is(':checked'))
		{
			$('[id^="emailser_"]').prop("readonly", false );
			$('[id^="emailser_"]').removeClass("ui-button-disabled ui-state-disabled");
			$(".ui-datepicker-trigger").prop("disabled", false );
		}
		else
		{
			$('[id^="emailser_"]').prop("readonly", true );
			$('[id^="emailser_"]').removeClass("ui-button-disabled ui-state-disabled");
			$(".ui-datepicker-trigger").prop("disabled", true );
		}
		if ($('#email_enableFaultEmail').is(':checked'))
		{
			$('[id^="emailfault_"]').prop("readonly", false );
			$('[id^="emailfault_"]').removeClass("ui-button-disabled ui-state-disabled");
		}
		else
		{
			$('[id^="emailfault_"]').prop("readonly", true );
			$('[id^="emailfault_"]').addClass("ui-button-disabled ui-state-disabled");
		}
	}
	function disableEmailConfigurations()
	{
		// Disable the manage emails tab as well
		$('#emailSettingtabs').tabs('disable', 1);
		
		$('[id^="email_"]').prop("readonly", true);
		$('[id^="email_"]').addClass("ui-button-disabled ui-state-disabled");
		$('[id^="eds_"]').prop("readonly", true );
		$('[id^="eds_"]').addClass("ui-button-disabled ui-state-disabled");
		$('[id^="emailser_"]').prop("readonly", true );
		$('[id^="emailser_"]').addClass("ui-button-disabled ui-state-disabled");
		$(".ui-datepicker-trigger").prop("disabled", true );
		$('[id^="emailfault_"]').prop("readonly", true );
		$('[id^="emailfault_"]').addClass("ui-button-disabled ui-state-disabled");
	}
	$.subscribe(
			"SavedErrorTopic",
			function(event, ui) {
				if (typeof event.originalEvent.request.responseJSON !== 'undefined'
						&& typeof event.originalEvent.request.responseJSON.actionErrors !== 'undefined') {
					alert(event.originalEvent.request.responseJSON.actionErrors[0]);
				} else {
					window.location = "/M9000Master/logout?customActionError=Session timed out";
				}
			});
	$.subscribe('SavedTopic', function(event, data) {
		$('[id^="btnSave"]').prop("disabled", true );
		$('[id^="btnSave"]').addClass("ui-button-disabled ui-state-disabled");
		isDirty=false;
		alert("Email settings updated successfully! Please verify emails list as well");
		if ($('#enableEmail').is(':checked'))
		{
			$('#emailSettingtabs').tabs('option','active', 1);
		}
		
	});
$.subscribe('adminTabchange', function(event, data) {
	var oldTab = event.originalEvent.ui.oldTab.attr("id");
	var newTab = event.originalEvent.ui.newTab.attr("id");
	console.log("Old tab "+oldTab+" New Tab "+newTab);
	if (oldTab === "emailSetting")
	{
		if (isDirty === true && $('#emailForm').serialize() != $('#emailForm').data('initial-state')){
                        console.log("Inside Tab Change isDirty? "+isDirty);
                    if (!confirm( 'You have unsaved changes. Do you want to still leave the tab?'))
			{
				$('#emailSettingtabs').tabs('option','active', 0);
			}
                }	
	}
});

