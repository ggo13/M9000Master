$(document).ready(function() {
	$("#btnShowContData").prop('disabled', true);
$('#selectAll').prop("checked", false);
$(".measurementRefresh").click(function(ev){
    $.publish("reloadMeasurementTypes");
})
$('#selectAll').change(function () {    
 var status = this.checked;
	console.log("Select all clicked..."+status);
	console.log("Before checkboxlist: "+$('input:checkbox[name="measurementTypes"]').prop('checked'));
    $('input:checkbox[name="measurementTypes"]').each(function() {
	console.log("Inside..."+$(this).prop('checked'));
		$(this).prop('checked', status);
	});   
$('input:checkbox[name="measurementTypes"]').prop("checked", status).checkboxradio();
$('input:checkbox[name="measurementTypes"]').checkboxradio('refresh'); 
	console.log("After checkboxlist: "+$('input:checkbox[name="measurementTypes"]').prop('checked'));
validateBeforeContMeasurementShow();
 });
});
$("#measurementTypes").change(function(){
	var totalMeasurementTypes = $('input:checkbox[name^="measurementTypes"]').map(function() {
		    return this.value;
		}).get();
	var selectedMeasurementTypes = $('input:checkbox[name^="measurementTypes"]:checked').map(function() {
		    return this.value;
		}).get();
		if(totalMeasurementTypes.length > selectedMeasurementTypes.length) 
		{
			console.log("It is NOT equal");
			$("#selectAll").prop("checked", false);
			if ($('#NextToCreateOrUpdate').valid())
			{
				$("#btnShowContData").prop('disabled', false);	
			}
		}
		else if(totalMeasurementTypes.length == selectedMeasurementTypes.length) 
		{
			console.log("It is equal");
			$("#selectAll").prop("checked", true);
			if ($('#NextToCreateOrUpdate').valid())
			{
				$("#btnShowContData").prop('disabled', false);	
			}
		}
		else if(selectedMeasurementTypes == 0)
		{
			$("#btnShowContData").prop('disabled', true);
 		}
validateBeforeContMeasurementShow();
});
var lastSelectedRow = null;
var contMeasurementsGrid = $("#contMeasurementsGrid");
function reloadContMeasurementsGrid()
{
	contMeasurementsGrid.trigger("reloadGrid",[{current:true}]);
}
$.subscribe('oneditsuccess', function(event, data) {
	var message = event.originalEvent.response.statusText;
	$("#gridinfo").html('<p>Status: ' + message + '</p>');
});
$.subscribe('rowadd', function(event, data) {
	contMeasurementsGrid.jqGrid('editGridRow', "new", {
		height : 280,
		reloadAfterSubmit : false
	});
});
$.subscribe('searchgrid', function(event, data) {
	contMeasurementsGrid.jqGrid('searchGrid', {
		sopt : [ 'cn', 'bw', 'eq', 'ne', 'lt', 'gt', 'ew' ]
	});
});
/*
 * Subscribe Topics for Grid Loadonce Example
 */
$.subscribe('showloadcolumns', function(event, data) {
	$("#gridloadtable").jqGrid('columnChooser', {});
});
$.subscribe('showDDRAnalogColumns', function(event, data) {
	contMeasurementsGrid.jqGrid('columnChooser', {});
});
$.subscribe('delete', function(event, data) {
	var rowid = contMeasurementsGrid.jqGrid('getGridParam', 'selrow');
	var faultId = contMeasurementsGrid.jqGrid('getCell', rowid, 'faultId');
	contMeasurementsGrid.jqGrid('delRowData', faultId, {
		height : 280,
		reloadAfterSubmit : false,
		afterSubmit : function(data, postd) {
			console.log(data);
			console.log(postd);
			return {
				0 : true
			};
		},
		afterComplete : function(data, postd) {
			return true;
		},
		url : '/?r=user/delete',
		msg : "you msg"
	});
});
$(function() {
	$.subscribe('storeSelection', function(event, data) {
		var saveSelectedRows = contMeasurementsGrid.jqGrid('getGridParam', 'selrow');
		if (saveSelectedRows != null)
		{
			lastSelectedRow = saveSelectedRows;
		}
		var page = contMeasurementsGrid.jqGrid('getGridParam', 'page');
		contMeasurementsGrid.data(page.toString(), saveSelectedRows);
	});
});
$.subscribe('beforeSelectRow', function(rowid, e) {
	if (!e.ctrlKey && !e.shiftKey) {
		contMeasurementsGrid.jqGrid('resetSelection');
	}
	else if (e.shiftKey) {
		var initialRowSelect = contMeasurementsGrid.jqGrid('getGridParam', 'selrow');
		alert("initialRowSelect " + initialRowSelect);
		contMeasurementsGrid.jqGrid('resetSelection');
		var CurrentSelectIndex = contMeasurementsGrid.jqGrid('getInd', rowid);
		var InitialSelectIndex = contMeasurementsGrid.jqGrid('getInd', initialRowSelect);
		var startID = "";
		var endID = "";
		if (CurrentSelectIndex > InitialSelectIndex) {
			startID = initialRowSelect;
			endID = rowid;
		}
		else {
			startID = rowid;
			endID = initialRowSelect;
		}
		var shouldSelectRow = false;
		$.each(contMeasurementsGrid.getDataIDs(), function(_, id) {
			if ((shouldSelectRow = id == startID || shouldSelectRow)) {
				contMeasurementsGrid.jqGrid('setSelection', id, false);
			}
			return id != endID;
		});
	}
	return true;
});
$(function() {
	$.subscribe('initContMeasurementsGrid', function(event, data) {
		if (userRole != 'admin') {
            $('[id^=del_contMeasurementsGrid]').hide();
		} else {
            $('[id^=del_contMeasurementsGrid]').show();
		}
		$("#errorStatus").empty();
		$("#errorStatus").hide();
		contMeasurementsGrid.jqGrid('setSelection', lastSelectedRow);
		$("tr.jqgrow", "#contMeasurementsGrid").contextMenu('rightClickMenu', {
			bindings : {
				'save' : function(t) {
					saveFault();
				},
				'view' : function(t) {
					viewFault();
				},
				'del' : function(t) {
					deleteFault();
				}
			},
			onContextMenu : function(event, menu) {
				var rowId = $(event.target).parent("tr").attr("id")
				contMeasurementsGrid.jqGrid('resetSelection');
				contMeasurementsGrid.jqGrid('setSelection', rowId);
				return true;
			}
		});
		function viewFault() {
			var sel_id = contMeasurementsGrid.jqGrid('getGridParam', 'selrow');
			var fileName = contMeasurementsGrid.jqGrid('getCell', sel_id, 'fileName');
			getFileIfNotExists(fileName);
        	showComtradeViewer(fileName);
		}
		function saveFault() {
			var sel_id = contMeasurementsGrid.jqGrid('getGridParam', 'selrow');
			var fileName = contMeasurementsGrid.jqGrid('getCell', sel_id, 'fileName');
			saveComtradeFiles(fileName);
		}
		function deleteFault() {
			var rowKey = contMeasurementsGrid.getGridParam("selrow");
			if (rowKey) {
				contMeasurementsGrid.delGridRow(rowKey);
			}
			else {
				alert("No rows are selected");
			}
		}
	});
	$.subscribe('reloadContMeasurementsGrid', function(event, data) {
		reloadContMeasurementsGrid();
	});
});
$.subscribe('onMeasurementsStartTime', function(event, data) {
	//    $('#measurementsEndTime').datepicker( "option" , 'minDate',event.originalEvent.dateText ); 
	var startDate = $('#measurementsStartTime').datepicker('getDate');
	var endDate = -1;
	if ($("#measurementTotalTime").val())
	{
		if ($("#lstMeasurementTimes").val() == "Seconds")
		{
			endDate = addSeconds(startDate, $("#measurementTotalTime").val());
		}
		else
		{
			endDate = addMinutes(startDate, $("#measurementTotalTime").val());
		}
	}
	else
	{
		endDate = addMinutes(startDate, 1);		
	}

	if (endDate > new Date())
	{
		endDate = startDate;
		var newStartDate = new Date(startDate.getTime() - 1 * 60000);
		$('#measurementsStartTime').datepicker('setDate', newStartDate);
		startDate = newStartDate;
	}

	$('#measurementsEndTime').datepicker("option", 'minDate',	$('#measurementsStartTime').datepicker('getDate'));
	$('#measurementsEndTime').datepicker("option", 'maxDate', startDate);
	$('#measurementsEndTime').datepicker('setDate', endDate);
	validateBeforeContMeasurementShow();
});
$.subscribe('onMeasurementsEndTime', function(event, data) {
	validateBeforeContMeasurementShow();
});
function validateBeforeContMeasurementShow()
{
	var resultValid = $('#NextToCreateOrUpdate').valid();
	if (resultValid) {
		$("#btnShowContData").prop('disabled', false);
	} else {
		$("#btnShowContData").prop('disabled', true);
	}
}

function clearTimeAndupdateMeasurementEndTime()
{
        $("#measurementTotalTime").val("");
        updateMeasurementEndTime();
}

function updateMeasurementEndTime()
{
	if ($("#measurementTotalTime").valid())
	{
		if ($("#measurementTotalTime").val())
		{
			var endDate;
			var startDate = $('#measurementsStartTime').datepicker('getDate');
			if ($("#lstMeasurementTimes").val() == "Seconds")
			{
				endDate = addSeconds(startDate, $("#measurementTotalTime").val());
			}
			else
			{
				endDate = addMinutes(startDate, $("#measurementTotalTime").val());
			}
			 $('#measurementsEndTime').datepicker('setDate', endDate);
			validateBeforeContMeasurementShow();
		}
	}
}
$.subscribe('measurementTypeChange', function(event, data) {
	console.log("event "+event.target);
});
function openContMeasurementsNewWindow() {
	if ($('#NextToCreateOrUpdate').valid())
	{
		var status;
		var startTime = $('#measurementsStartTime').val();
		var endTime = $('#measurementsEndTime').val();
		var measurementTypes = $('input:checkbox[name^="measurementTypes"]:checked').map(function() {
		    return this.value;
		}).get();
		console.log("Measurement Types "+measurementTypes);
		$.ajax({
			url : '/M9000Master/checkSessionStatus',
			type : 'POST',
			dataType : 'json',
			success : function(res) {
				status = res.currentSessionStatus;
				if (res.currentSessionStatus == 'active')
				{
//					var left = (screen.width / 2);
//					left -= (700 / 2);
//					var top = (screen.height / 2);
//					top -= (550 / 2);
					var width=screen.width*.98;
					var height=screen.height*(3/4);
					var left=0;
					var right="0"
					var returnVal = window.open(
							"/M9000Master/showContMeasurementsData?measurementsStartTime="
							+ startTime + "&measurementsEndTime=" + endTime + "&measurementTypes=" + measurementTypes,
							"_blank",
							"status = 1,height = "+height+",width = "+width+",resizable = 1,left="
							+ left + ",top=" + top);
					if (returnVal != null)
					{
						var wintimer = setInterval(function() {
						if (returnVal.closed) {
						    clearInterval(wintimer);
						    reloadContMeasurementsGrid();

						console.log("Analysis Window Closed...");
						}
					    }, 500);
					}

				}
				else
				{
					window.location = "/M9000Master/logout";
				}
			},
			error : function(jqXhr, textStatus, errorThrown) {
				window.location = "/M9000Master/logout";
			}
		});
	}
	return false;
}

$.subscribe('showContMeasurementsGridErrorMessage', function(event,data) {
	$("#contMeasurementsStatus").html('<p>Error Occurred. Unable to fetch cont Measurements data. Please check logs</p>');
});