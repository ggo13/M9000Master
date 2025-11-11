var lastSelectedRow = null;
var alarmsGrid = $("#alarmsLogGrid");
function reloadAlarmsGrid()
{
	alarmsGrid.trigger("reloadGrid",[{current:true}]);
}
$.subscribe('oneditsuccess', function(event, data){
    var message = event.originalEvent.response.statusText;
    $("#gridinfo").html('<p>Status: ' + message + '</p>');
});
$.subscribe('rowadd', function(event,data) {
    alarmsGrid.jqGrid('editGridRow',"new",{height:280,reloadAfterSubmit:false});
});
$.subscribe('searchgrid', function(event,data) {
    alarmsGrid.jqGrid('searchGrid', {sopt:['cn','bw','eq','ne','lt','gt','ew']} );
});
/*
	 * Subscribe Topics for Grid Loadonce Example
	 */
	$.subscribe('showloadcolumns', function(event, data) {
		$("#gridloadtable").jqGrid('columnChooser', {});
	});
$.subscribe('showAlarmsLogColumns', function(event,data) {
    alarmsGrid.jqGrid('columnChooser',{});
});
$.subscribe('delete', function(event,data) {
    var rowid = alarmsGrid.jqGrid('getGridParam','selrow');
    var faultId = alarmsGrid.jqGrid('getCell', rowid, 'faultId');
    alarmsGrid.jqGrid('delRowData',faultId, {
    	height:280,
		reloadAfterSubmit:false,
		afterSubmit:function(data,postd){
			console.log(data);
			console.log(postd);
			return {0:true};
		},
		afterComplete:function(data,postd){
			return true;
		},
		url:'/?r=user/delete',
		msg: "you msg"
    });
});
$(function(){
    $.subscribe('rowselect', function(event,data) {
		var sel_id = alarmsGrid.jqGrid('getGridParam', 'selrow'); 
		var faultId = alarmsGrid.jqGrid('getCell', sel_id, 'faultId'); 
		alert(faultId);
    });
});
$.subscribe('beforeSelectRow', function (rowid, e) {
    if (!e.ctrlKey && !e.shiftKey) {
        alarmsGrid.jqGrid('resetSelection');
    }
    else if (e.shiftKey) {
        var initialRowSelect = alarmsGrid.jqGrid('getGridParam', 'selrow');
            alert("initialRowSelect "+initialRowSelect);
        alarmsGrid.jqGrid('resetSelection');
        var CurrentSelectIndex = alarmsGrid.jqGrid('getInd', rowid);
        var InitialSelectIndex = alarmsGrid.jqGrid('getInd', initialRowSelect);
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
        $.each(alarmsGrid.getDataIDs(), function(_, id){
            if ((shouldSelectRow = id == startID || shouldSelectRow)){
              alarmsGrid.jqGrid('setSelection', id, false);
            }
            return id != endID;                        
        });
    }
    return true;
});
$('#autoAlarmsLogRefresh').change(function() {
    if($(this).is(":checked")) {
   		startTimer("alarmsTab");
	}
   	else
   	{
   		stopTimer("alarmsTab");
   	}
   });
function autoRefreshAlarmsEnabled()
{
	return $('#autoAlarmsLogRefresh').is(":checked");
}
function formatStatusImage(cellvalue, options, row) {
	var imageToReturn = "/images/bullet_ball_glass_"+cellvalue.toLowerCase()+".png";
	if (cellvalue == 'OFF')
	{
		imageToReturn = "/images/bullet_ball_glass_grey.png";
	}
	 return "<img src='"+context_path+""+imageToReturn+"' />";
	}

$.subscribe('initAlarmsLogGrid', function(event,data) {
	$("#errorStatus").empty();
	$("#errorStatus").hide();
});

$.subscribe('showAlarmsGridErrorMessage', function(event,data) {
	$("#alarmsErrorStatus").html('<p>Error Occurred. Unable to fetch Alarms Log Details. Please check logs</p>');
});