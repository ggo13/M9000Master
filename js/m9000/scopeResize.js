$(document).ready(function(){
if (userRole != 'admin')
        {
                $('[name^="btnExternalCal"]').prop("disabled", true);
        }
});
$(document).on("keypress", "form", function(event) { 
    return event.keyCode != 13;
});
$(function () 
{
    $(".resizable1").resizable(
    {
        autoHide: true,
	minWidth: 500,
        handles: 'e',
        resize: function(e, ui) 
        {
            var parent = ui.element.parent();
            var remainingSpace = parent.width() - ui.element.outerWidth(),
                divTwo = ui.element.next(),
                divTwoWidth = (remainingSpace - (divTwo.outerWidth() - divTwo.width()))/parent.width()*100+"%";
                divTwo.width(divTwoWidth);
        },
        stop: function(e, ui) 
        {
            var parent = ui.element.parent();
            ui.element.css(
            {
                width: ui.element.width()/parent.width()*100+"%",
            });
        }
    });
});
$(function () 
{
    var bottomElem = $(".h-resizable-bottom");
    var bottomElemOriginalHeight = bottomElem.height();
    $(".h-resizable-top").resizable(
    {
        autoHide: false,
	minHeight: 300,
        handles: 's',
        resize: function(e, ui) 
        {
            bottomElem.height(bottomElemOriginalHeight - (ui.element.outerHeight() - ui.originalSize.height));
        },
        stop: function(e, ui) 
        {
            bottomElemOriginalHeight = bottomElem.height();
        },
        maxHeight: $(".h-resizable-top").height()
    });
});
function okButtonSelectAnalogDialog(){
	var selectedChannelsFromList = $('#lstSelectedChannels').val();
	$('#strSelectedChannels').val(selectedChannelsFromList);
	setChartChanged();
	$('#selectAnalogDialog').dialog('close');
	if (selectedChannelsFromList && selectedChannelsFromList.indexOf(",") < 0)
	{
		if (parseInt(selectedChannelsFromList) > parseInt(startChannel))
		{
			$('#btnPrevious').prop("disabled",false);
		}
		else 
		{
			$('#btnPrevious').prop("disabled",true);
		}
		if (parseInt(selectedChannelsFromList) < parseInt(endChannel))
		{
			$('#btnNext').prop("disabled",false);
		}
		else 
		{
			$('#btnNext').prop("disabled",true);
		}
	}
};
function cancelSelectAnalogDialogButton(){
	$('#selectAnalogDialog').dialog('close');
};
$('#btnNext').click(function() {
	var selectedChannel = $('#strSelectedChannels').val().trim();
	if(!selectedChannel || selectedChannel.indexOf(",") > -1)
	{
		$('#strSelectedChannels').val(startChannel);
		setChartChanged();
	}
	else 
	{
		if (parseInt(selectedChannel) < parseInt(endChannel))
		{
			$('#strSelectedChannels').val((parseInt(selectedChannel)+1));
			setChartChanged();
			if ((parseInt(selectedChannel)+1) == parseInt(endChannel))
			{
				$('#btnNext').prop("disabled",true);
			}
		}
		else
		{
			$('#btnNext').prop("disabled",true);	
		}
		$('#btnPrevious').prop("disabled",false);
	}
});
$('#btnPrevious').click(function() {
	var selectedChannel = $('#strSelectedChannels').val().trim();
	if(selectedChannel && selectedChannel.indexOf(",") == -1)
	{
		if (parseInt(selectedChannel) > parseInt (startChannel))
		{
			$('#strSelectedChannels').val((parseInt(selectedChannel)-1));
			setChartChanged();
			if ((parseInt(selectedChannel)-1) == parseInt(startChannel))
			{
				$('#btnPrevious').prop("disabled",true);
			}
		}
		else
		{
			$('#btnPrevious').prop("disabled",true);	
		}
		$('#btnNext').prop("disabled",false);
	}
});
$('#btnApply').click(function() {
if ($('#requestedCycles').valid())
{
	console.log("validated successfully...");
	setChartChanged();
}
});
var waitTimer;
$('#btnTestEvents').click(function() {
	console.log("Test button clicked");
	if(confirm("The DFR will be off-line while testing events. Do you wish to continue?")){
		$.publish("eventTestTopic");
		 $( "#dlgShowEventTestStatus" ).dialog( "option", "title", "Event Test" );
		 $( "#dlgShowEventTestStatus" ).dialog( "option", "position", { my: "center", at: "center", of: window } );	
		waitTimer = setInterval(
						function() {
							console.log("wait timer is on");
							$.publish("eventTestTopic");
						},
						1000); // 1 second
	}
	else
	{
		return false;
	}
});
$.subscribe('stopWaitTimer', function(event, data) {
	console.log("Stopping wait timer");
	clearInterval(waitTimer);
});
$('#btnCloseEventsDialog').click(function() {
	console.log("Close button clicked");
	$('#dlgShowEventTestStatus').dialog('destroy');
});
$('#btnInternalCal').click(function() {
	console.log("Internal Calibration button clicked");
	if(confirm("The DFR will be off-line while calibrating. Do you wish to continue?")){
		slowdownScopeRequestFrequency();
		$.publish("intCalTopic");
		$( "#dlgShowInternalCal" ).dialog( "option", "title", "Internal Calibration" );
		 $( "#dlgShowInternalCal" ).dialog( "option", "position", { my: "center", at: "center", of: window } );	
		waitTimer = setInterval(
						function() {
							console.log("wait timer is on");
							$.publish("intCalTopic");
						},
						1000); // 1 second
	}
	else
	{
		return false;
	}
});
$('#btnInternalCalVerify').click(function() {
	console.log("Verify Calibration button clicked");
	if(confirm("The DFR will be off-line while calibrating. Do you wish to continue?")){
		slowdownScopeRequestFrequency();
		$.publish("verifyCalTopic");
		$( "#dlgShowInternalCalVerify" ).dialog( "option", "title", "Verify Calibration" );
		 $( "#dlgShowInternalCalVerify" ).dialog( "option", "position", { my: "center", at: "center", of: window } );	
		waitTimer = setInterval(
						function() {
							console.log("wait timer is on");
							$.publish("verifyCalTopic");
						},
						1000); // 1 second
	}
	else
	{
		return false;
	}
});
$('#btnExternalCal').click(function() {
	if (($('#desiredValue').valid() && ($('#desiredValue').val().length >= 1)) || $('#offsetCorrection').is(":checked"))
	{
		console.log("External Calibration apply button clicked");
		if(confirm("Do you wish to apply external calibration to the selected channel(s)?")){
			$.publish("extCalTopic");
			$( "#dlgShowExternalCal" ).dialog( "option", "title", "External Calibration" );
			 $( "#dlgShowExternalCal" ).dialog( "option", "position", { my: "center", at: "center", of: window } );	
		}
		else
		{
			return false;
		}
	}
	else
	{
		alert("Enter either a desired value or check offset correction or both.");
		return false;
	}
});
$('#btnExternalCalReset').click(function() {
	console.log("Reset External Calibration apply button clicked");
	if(confirm("Do you wish to reset external calibration to the selected channel(s)?")){
		$.publish("resetExtCalTopic");
		$( "#dlgShowExternalCalReset" ).dialog( "option", "title", "Reset External Calibration" );
		 $( "#dlgShowExternalCalReset" ).dialog( "option", "position", { my: "center", at: "center", of: window } );	
	}
	else
	{
		return false;
	}
});
$('#requestedCycles').keyup(function (e) {
    if (e.keyCode === 13 && $('#requestedCycles').valid()) {
       console.log("Enter key validated successfully...");
	setChartChanged();
    }
    else if ($('#requestedCycles').valid())
    {
	$("#btnApply").prop('disabled', false);
    }
    else
    {
	$("#btnApply").prop('disabled', true);
    }
  });
function setChartChanged()
{
	console.log("Chart Changed...");
	$('#chartChanged').val("true");
}

function setUpdateScopeConfig()
{
	console.log("idUpdateScopeConfig set to true...");
	$('#updateScopeConfig').val("true");
}

$('#expandCollapseAccordion').change(function() {
	if ($(this).is(":checked")) {
		console.log("To expand...");
		$('div[id^="idAccordion"]').accordion("option","active",0);
	}
	else
	{
		$('div[id^="idAccordion"]').accordion("option",{active: false},0);
	}
});
$('#showHideAnalog').change(function() {
	if ($(this).is(":checked")) {
		$('#divAnalogChart').show();
		$('#showHideLegend').prop("disabled",false);
		$('#showHideDigital').prop("disabled",false);
	}
	else
	{
		$('#divAnalogChart').hide();
		$('#showHideLegend').prop("disabled",true);
		$('#showHideDigital').prop("disabled",true);
	}
});
$('#showHideLegend').change(function() {
	if ($(this).is(":checked")) {
		$('#legendHolder').show();
	}
	else
	{
		$('#legendHolder').hide();
	}
});
$('#showHideDataPoints').change(function() {
	setChartChanged();
	if ($(this).is(":checked")) {
		console.log("show data points checked");
	}
	else
	{
		console.log("show data points unchecked");
	}
});
var oldAnalogChartHeight;
$('#showHideDigital').change(function() {
	if ($(this).is(":checked")) {
		$('#divAnalogChart').removeAttr("style");
		$('#divAnalogChart').addClass("h-resizable h-resizable-top");
		if (oldAnalogChartHeight > 0)
		{
			$('#divAnalogChart').css("height",oldAnalogChartHeight);
		}
		else
		{
			$('#divAnalogChart').css("height","calc(100vh - 270px)");
		}
		$('#divDigital').show();
		$('#showHideAnalog').prop("disabled",false);
	}
	else
	{
		oldAnalogChartHeight = $('#divAnalogChart').outerHeight();
		console.log("Old Analog Chart width "+oldAnalogChartHeight);
		$('#divDigital').hide();
		$('#divAnalogChart').removeClass("h-resizable h-resizable-top");
		//$('#divAnalogChart').css("height","calc(100vh - 270px)");
		$('#divAnalogChart').css("height","100%");
		$('#showHideAnalog').prop("disabled",true);
	}
});
var oldChartWidth;
var controlWidth;
$('#showHideControls').change(function() {
	if ($(this).is(":checked")) {
		$('#splitPane').removeAttr("style");
		$('#splitPane').addClass("resizable resizable1");
		if (oldChartWidth > 0)
		{
			console.log("Old Chart width after show "+oldChartWidth);
			$('#splitPane').css("width", oldChartWidth);
			$('#controls').css("width", controlWidth);
		}
		$('#controls').show();
	}
	else
	{
		oldChartWidth = $('#splitPane').innerWidth();
		console.log("Old Chart width "+oldChartWidth);
		controlWidth = $('#controls').outerWidth();
		$('#controls').hide();
		$('#splitPane').removeClass("resizable resizable1");
		$('#splitPane').css("width", "100%");
	}
});

$("input[name='primaryOrSecondaryDisplay']").change(function(){
    
    setChartChanged();
    });
