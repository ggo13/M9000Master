$.subscribe('updateLegend', function(event, data) {
	if($('.legend').contents().length > 0)
	{
		$('#legendHolder').empty();
		$('.legend').contents().appendTo($('#legendHolder'));
		$('.legendContainer table').removeAttr("style");
	//	$('.legendContainer table').addClass('legendHorizontal');
	//	$('.legendContainer').css("max-height", "calc(100vh - 150px)");
		$('.legendContainer table').css("font-size", "12");
		$('.legendContainer table').css("background-color", "#435094");
		$('#legendHolder').find('div').first().remove();
		$('#divChartJq').addClass("chart-placeholder");
		$('div.legend').remove();
	}
});


$("#chartContainer").mouseup(function() {
$('div.legend').hide();
	updateLegend();
});
$("#chartContainer").mousemove(function(e){
 if(e.which==1)
 {
   $('div.legend').hide();
 }
});
$("#chartContainer").mousedown(function(e){
	$('div.legend').hide();
});
$.subscribe('startTimer', function(event, data) {
	if (scopeTimer === null) {
		startTimer("scopeTab");
	}
});
$.subscribe('adjustHeight', function(event, data) {
	$('.wrap').css("height", "100%");
});
$.subscribe('adjustDigitalOnlyDisplay', function(event, data) {
	$('div[id^="digitalRow"]').css("padding", "4em");
	$('div[id^="digitalCol"]').css("padding", "0.6em");
	$('div[id^="digitalCol"]').css("font-size", "10");
});
$('#scopeDataRefresh').change(function() {
	if ($(this).is(":checked")) {
		startTimer("scopeTab");
	}
	else
	{
		stopTimer("scopeTab");
	}
});
function updateChartDisplay()
{
	$('#strSelectedChannels').val("");
	$('#selectAnalogDialog').dialog('destroy').remove();
	$('#dlgShowEventTestStatus').dialog('destroy').remove();
	$('#dlgShowInternalCal').dialog('destroy').remove();
	$('#dlgShowInternalCalVerify').dialog('destroy').remove();
	$('#dlgShowExternalCal').dialog('destroy').remove();
	$('#dlgShowExternalCalReset').dialog('destroy').remove();
	$.publish("updateChartDisplay");
	setChartChanged();
}
function refreshScope()
{
        $.publish("refreshScope");
        $.publish("refreshDigitalScope");
}
function updateLegend()
{
        $.publish("updateLegend");
}
function autoRefreshScopeEnabled()
{
	return $('#scopeDataRefresh').is(":checked");
}
$('#showHideChartMenu').change(function() {
	if ($(this).is(":checked")) {
		$('#chartMenu').show();
	}
	else
	{
		$('#chartMenu').hide();
	}
});
