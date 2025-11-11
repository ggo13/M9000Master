var isDirty = false;
$(document).ready(function(){
	$('[id^="btnSave"]').prop("disabled", true );
		if ($('#enableLDAP').is(':checked'))
		{
			console.log("Enable all as enable email is checked");
			enableLDAPConfigurations();
		}
		else
		{
			console.log("Disable all as enable email is unchecked");
			disableLDAPConfigurations();
		}
		
		$('#ldapForm').data('initial-state', $('#ldapForm').serialize());

	    $(window).on('beforeunload', function() {
		console.log("isDirty? "+isDirty);
	        if (isDirty === true && $('#ldapForm').serialize() != $('#ldapForm').data('initial-state')){
			console.log("Inside IF isDirty? "+isDirty);
	            return 'You have unsaved changes which will not be saved.'
	        }
	    });
	$('#ldapForm').find("input,button,textarea").on("keyup change",function() {
		console.log("Input change tracked..."+$('[id^="btnSave"]').html()+" ???"+$('.formButton').html());
		if ($('#ldapForm').serialize() != $('#ldapForm').data('initial-state'))
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


	$("#enableLDAP").change(function(){
		if ($('#enableLDAP').is(':checked'))
		{
			console.log("Enable all as enable ldap is checked");
			enableLDAPConfigurations();
		}
		else
		{
			console.log("Disable all as enable ldap is unchecked");
			disableLDAPConfigurations();
		}
	});
	
	function enableEmailConfigurations()
	{
		// Enable the manage emails tab as well
		$('[id^="ldap_"]').prop("readonly", false );
		$('[id^="ldap_"]').removeClass("ui-button-disabled ui-state-disabled");
	}
	function disableEmailConfigurations()
	{
		$('[id^="ldap_"]').prop("readonly", true);
		$('[id^="ldap_"]').addClass("ui-button-disabled ui-state-disabled");
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
		alert("LDAP settings updated successfully!");
	});
