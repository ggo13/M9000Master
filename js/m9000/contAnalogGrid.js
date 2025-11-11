$(document).ready(function() {
	$("#btnShowContAnalogData").prop('disabled', true);
});
var lastSelectedRow = null;
var contAnalogGrid = $("#contAnalogGrid");
function reloadContAnalogGrid()
{
	contAnalogGrid.trigger("reloadGrid",[{current:true}]);
}
$.subscribe('oneditsuccess', function(event, data) {
	var message = event.originalEvent.response.statusText;
	$("#gridinfo").html('<p>Status: ' + message + '</p>');
});
$.subscribe('rowadd', function(event, data) {
	contAnalogGrid.jqGrid('editGridRow', "new", {
		height : 280,
		reloadAfterSubmit : false
	});
});
$.subscribe('searchgrid', function(event, data) {
	contAnalogGrid.jqGrid('searchGrid', {
		sopt : [ 'cn', 'bw', 'eq', 'ne', 'lt', 'gt', 'ew' ]
	});
});
/*
 * Subscribe Topics for Grid Load once Example
 */
$.subscribe('showloadcolumns', function(event, data) {
	$("#gridloadtable").jqGrid('columnChooser', {});
});
$.subscribe('showContAnalogColumns', function(event, data) {
	contAnalogGrid.jqGrid('columnChooser', {});
});
$.subscribe('delete', function(event, data) {
	var rowid = contAnalogGrid.jqGrid('getGridParam', 'selrow');
	var faultId = contAnalogGrid.jqGrid('getCell', rowid, 'faultId');
	contAnalogGrid.jqGrid('delRowData', faultId, {
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
		var saveSelectedRows = contAnalogGrid.jqGrid('getGridParam', 'selrow');
		if (saveSelectedRows != null)
		{
			lastSelectedRow = saveSelectedRows;
		}
		var page = contAnalogGrid.jqGrid('getGridParam', 'page');
		contAnalogGrid.data(page.toString(), saveSelectedRows);
	});
});
$.subscribe('beforeSelectRow', function(rowid, e) {
	if (!e.ctrlKey && !e.shiftKey) {
		contAnalogGrid.jqGrid('resetSelection');
	}
	else if (e.shiftKey) {
		var initialRowSelect = contAnalogGrid.jqGrid('getGridParam', 'selrow');
		alert("initialRowSelect " + initialRowSelect);
		contAnalogGrid.jqGrid('resetSelection');
		var CurrentSelectIndex = contAnalogGrid.jqGrid('getInd', rowid);
		var InitialSelectIndex = contAnalogGrid.jqGrid('getInd', initialRowSelect);
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
		$.each(contAnalogGrid.getDataIDs(), function(_, id) {
			if ((shouldSelectRow = id == startID || shouldSelectRow)) {
				contAnalogGrid.jqGrid('setSelection', id, false);
			}
			return id != endID;
		});
	}
	return true;
});
$(function() {
	$.subscribe('initContAnalogGrid', function(event, data) {
		if (userRole != 'admin') {
            $('[id^=del_contAnalogGrid]').hide();
		} else {
            $('[id^=del_contAnalogGrid]').show();
		}
		$("#errorStatus").empty();
		$("#errorStatus").hide();
		contAnalogGrid.jqGrid('setSelection', lastSelectedRow);
		$("tr.jqgrow", "#contAnalogGrid").contextMenu('rightClickMenu', {
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
				contAnalogGrid.jqGrid('resetSelection');
				contAnalogGrid.jqGrid('setSelection', rowId);
				return true;
			}
		});
		function viewFault() {
			var sel_id = contAnalogGrid.jqGrid('getGridParam', 'selrow');
			var fileName = contAnalogGrid.jqGrid('getCell', sel_id, 'fileName');
			getFileIfNotExists(fileName);
        	showComtradeViewer(fileName);
		}
		function saveFault() {
			var sel_id = contAnalogGrid.jqGrid('getGridParam', 'selrow');
			var fileName = contAnalogGrid.jqGrid('getCell', sel_id, 'fileName');
			saveComtradeFiles(fileName);
		}
		function deleteFault() {
			var rowKey = contAnalogGrid.getGridParam("selrow");
			if (rowKey) {
				contAnalogGrid.delGridRow(rowKey);
			}
			else {
				alert("No rows are selected");
			}
		}
	});
});
$.subscribe('onDpClose', function(event, data) {
	var startDate = $('#analogStartTime').datepicker('getDate');
	var endDate = -1;
	if ($("#totalTime").val())
	{
		if ($("#lstTimes").val() == "Seconds")
		{
			endDate = addSeconds(startDate, $("#totalTime").val());
		}
		else
		{
			endDate = addMinutes(startDate, $("#totalTime").val());
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
		$('#analogStartTime').datepicker('setDate', newStartDate);
		startDate = newStartDate;
	}

	$('#analogEndTime').datepicker("option", 'minDate',
	$('#analogStartTime').datepicker('getDate'));
	$('#analogEndTime').datepicker("option", 'maxDate', startDate);
	$('#analogEndTime').datepicker('setDate', endDate);
	validateBeforeContAnalogShow();
});
$.subscribe('onEndClose', function(event, data) {
	validateBeforeContAnalogShow();
});
function validateBeforeContAnalogShow()
{
var resultValid = $('#NextToCreateOrUpdate').valid();
	if (resultValid) {
		$("#btnShowContAnalogData").prop('disabled', false);
	} else {
		$("#btnShowContAnalogData").prop('disabled', true);
	}	
}
function clearTimeAndupdateEndTime()
{
        $("#totalTime").val("");
        updateEndTime();
}
function updateEndTime()
{
	if ($("#totalTime").valid())
	{
		if ($("#totalTime").val())
		{
			var endDate;
			var startDate = $('#analogStartTime').datepicker('getDate');
			if ($("#lstTimes").val() == "Seconds")
			{
				endDate = addSeconds(startDate, $("#totalTime").val());
			}
			else
			{
				endDate = addMinutes(startDate, $("#totalTime").val());
			}
			 $('#analogEndTime').datepicker('setDate', endDate);
			validateBeforeContAnalogShow();
		}
	}
}
function openContAnalogInNewWindow() {
	var status;
	var startTime = $('#analogStartTime').val();
	var endTime = $('#analogEndTime').val();
	$.ajax({
		url : '/M9000Master/checkSessionStatus',
		type : 'POST',
		dataType : 'json',
		success : function(res) {
			status = res.currentSessionStatus;
			if (res.currentSessionStatus == 'active')
			{
//				var left = (screen.width / 2);
//				left -= (700 / 2);
//				var top = (screen.height / 2);
//				top -= (550 / 2);
				var width=screen.width*.98;
				var height=screen.height*(3/4);
				var left=0;
				var right="0"
				var returnVal = window.open("/M9000Master/showContAnalogData?analogStartTime="
				+ startTime + "&analogEndTime=" + endTime, "_blank",
				"status = 1,height = "+height+",width = "+width+",resizable = 1,left="
				+ left + ",top=" + top);
				if (returnVal != null)
				{
					var wintimer = setInterval(function() {
					if (returnVal.closed) {
					    clearInterval(wintimer);
					    reloadContAnalogGrid();

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
	return false;
}

$.subscribe('showContAnalogGridErrorMessage', function(event,data) {
	$("#contAnalogStatus").html('<p>Error Occurred. Unable to fetch contAnalog data. Please check logs</p>');
});