var lastSelectedRow = null;
var ddrMeasurementsGrid = $("#ddrMeasurementsGrid");
function reloadDdrMeasurementsGrid()
{
	ddrMeasurementsGrid.trigger("reloadGrid",[{current:true}]);
}
$.subscribe('oneditsuccess', function(event, data){
    var message = event.originalEvent.response.statusText;
    $("#gridinfo").html('<p>Status: ' + message + '</p>');
});
$.subscribe('rowadd', function(event,data) {
    ddrMeasurementsGrid.jqGrid('editGridRow',"new",{height:280,reloadAfterSubmit:false});
});
$.subscribe('searchgrid', function(event,data) {
    ddrMeasurementsGrid.jqGrid('searchGrid', {sopt:['cn','bw','eq','ne','lt','gt','ew']} );
});
/*
	 * Subscribe Topics for Grid Loadonce Example
	 */
	$.subscribe('showloadcolumns', function(event, data) {
		$("#gridloadtable").jqGrid('columnChooser', {});
	});
$.subscribe('showDDRAnalogColumns', function(event,data) {
    ddrMeasurementsGrid.jqGrid('columnChooser',{});
});
$.subscribe('delete', function(event,data) {
    var rowid = ddrMeasurementsGrid.jqGrid('getGridParam','selrow');
    var faultId = ddrMeasurementsGrid.jqGrid('getCell', rowid, 'faultId');
    ddrMeasurementsGrid.jqGrid('delRowData',faultId, {
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
    $.subscribe('storeSelection', function(event,data) {
		var saveSelectedRows  = ddrMeasurementsGrid.jqGrid('getGridParam', 'selrow'); 
		if (saveSelectedRows != null)
		{
			lastSelectedRow = saveSelectedRows;
		}
		var page =  ddrMeasurementsGrid.jqGrid('getGridParam', 'page');
		ddrMeasurementsGrid.data(page.toString(), saveSelectedRows);
    });
});
$.subscribe('beforeSelectRow', function (rowid, e) {
    if (!e.ctrlKey && !e.shiftKey) {
        ddrMeasurementsGrid.jqGrid('resetSelection');
    }
    else if (e.shiftKey) {
        var initialRowSelect = ddrMeasurementsGrid.jqGrid('getGridParam', 'selrow');
            alert("initialRowSelect "+initialRowSelect);
        ddrMeasurementsGrid.jqGrid('resetSelection');
        var CurrentSelectIndex = ddrMeasurementsGrid.jqGrid('getInd', rowid);
        var InitialSelectIndex = ddrMeasurementsGrid.jqGrid('getInd', initialRowSelect);
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
        $.each(ddrMeasurementsGrid.getDataIDs(), function(_, id){
            if ((shouldSelectRow = id == startID || shouldSelectRow)){
              ddrMeasurementsGrid.jqGrid('setSelection', id, false);
            }
            return id != endID;                        
        });
    }
    return true;
});
$(function(){
    $.subscribe('initddrMeasurementsGrid', function(event,data) {
    	if (userRole != 'admin') {
            $('[id^=del_ddrMeasurementsGrid]').hide();
		} else {
            $('[id^=del_ddrMeasurementsGrid]').show();
		}
    	$("#errorStatus").empty();
    	$("#errorStatus").hide();
	 ddrMeasurementsGrid.jqGrid('setSelection', lastSelectedRow);
            $("tr.jqgrow", "#ddrMeasurementsGrid").contextMenu('rightClickMenu', {
                bindings: {
                    'save': function (t) {
                        saveFault();
                    },
                    'view': function (t) {
                        viewFault();
                    },
                    'del': function (t) {
                        deleteFault();
                    }
                },
                onContextMenu: function (event, menu) {
                    var rowId = $(event.target).parent("tr").attr("id")
                    ddrMeasurementsGrid.jqGrid('resetSelection');
                    ddrMeasurementsGrid.jqGrid('setSelection', rowId);
                    return true;
                }
            });
            function viewFault() {
            	var sel_id = ddrMeasurementsGrid.jqGrid('getGridParam', 'selrow');
            	var fileName = ddrMeasurementsGrid.jqGrid('getCell', sel_id, 'fileName');
            	showComtradeViewer(fileName);
			}
            function saveFault() {
            	var sel_id = ddrMeasurementsGrid.jqGrid('getGridParam', 'selrow');
            	var fileName = ddrMeasurementsGrid.jqGrid('getCell', sel_id, 'fileName');
            	saveComtradeFiles(fileName);
            	}
            function deleteFault() {
                var rowKey = ddrMeasurementsGrid.getGridParam("selrow");
                if (rowKey) {
                    ddrMeasurementsGrid.delGridRow(rowKey);
                }
                else {
                    alert("No rows are selected");
                }
            }
        });
    });
$('#autoDdrMeasurementsRefresh').change(function() {
    if($(this).is(":checked")) {
   		startTimer("ddrMeasurementsTab");
	}
   	else
   	{
   		stopTimer("ddrMeasurementsTab");
   	}
   });
function autoRefreshDdrMeasurementsEnabled()
{
	return $('#autoDdrMeasurementsRefresh').is(":checked");
}

$.subscribe('showDDRMeasurementsGridErrorMessage', function(event,data) {
	$("#ddrMeasurementsStatus").html('<p>Error Occurred. Unable to fetch DDR Measurements. Please check logs</p>');
});