var lastSelectedRow = null;
var ddrAnalogGrid = $("#ddrAnalogsGrid"); 
function reloadDdrAnalogGrid()
{
	ddrAnalogGrid.trigger("reloadGrid",[{current:true}]);
}
$.subscribe('oneditsuccess', function(event, data){
    var message = event.originalEvent.response.statusText;
    $("#gridinfo").html('<p>Status: ' + message + '</p>');
});
$.subscribe('rowadd', function(event,data) {
    ddrAnalogGrid.jqGrid('editGridRow',"new",{height:280,reloadAfterSubmit:false});
});
$.subscribe('searchgrid', function(event,data) {
    ddrAnalogGrid.jqGrid('searchGrid', {sopt:['cn','bw','eq','ne','lt','gt','ew']} );
});
/*
	 * Subscribe Topics for Grid Loadonce Example
	 */
	$.subscribe('showloadcolumns', function(event, data) {
		$("#gridloadtable").jqGrid('columnChooser', {});
	});
$.subscribe('showDDRAnalogColumns', function(event,data) {
    ddrAnalogGrid.jqGrid('columnChooser',{});
});
$.subscribe('delete', function(event,data) {
    var rowid = ddrAnalogGrid.jqGrid('getGridParam','selrow');
    var faultId = ddrAnalogGrid.jqGrid('getCell', rowid, 'faultId');
    ddrAnalogGrid.jqGrid('delRowData',faultId, {
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
		var saveSelectedRows  = ddrAnalogGrid.jqGrid('getGridParam', 'selrow'); 
		if (saveSelectedRows != null)
		{
			lastSelectedRow = saveSelectedRows;
		}
		var page =  ddrAnalogGrid.jqGrid('getGridParam', 'page');
		ddrAnalogGrid.data(page.toString(), saveSelectedRows);
    });
});
$.subscribe('beforeSelectRow', function (rowid, e) {
    if (!e.ctrlKey && !e.shiftKey) {
        ddrAnalogGrid.jqGrid('resetSelection');
    }
    else if (e.shiftKey) {
        var initialRowSelect = ddrAnalogGrid.jqGrid('getGridParam', 'selrow');
            alert("initialRowSelect "+initialRowSelect);
        ddrAnalogGrid.jqGrid('resetSelection');
        var CurrentSelectIndex = ddrAnalogGrid.jqGrid('getInd', rowid);
        var InitialSelectIndex = ddrAnalogGrid.jqGrid('getInd', initialRowSelect);
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
        $.each(ddrAnalogGrid.getDataIDs(), function(_, id){
            if ((shouldSelectRow = id == startID || shouldSelectRow)){
              ddrAnalogGrid.jqGrid('setSelection', id, false);
            }
            return id != endID;                        
        });
    }
    return true;
});
$(function(){
    $.subscribe('initddrAnalogsGrid', function(event,data) {
    	if (userRole != 'admin') {
            $('[id^=del_ddrAnalogsGrid]').hide();
		} else {
            $('[id^=del_ddrAnalogsGrid]').show();
		}
    	$("#errorStatus").empty();
    	$("#errorStatus").hide();
	 ddrAnalogGrid.jqGrid('setSelection', lastSelectedRow);
            $("tr.jqgrow", "#ddrAnalogsGrid").contextMenu('rightClickMenu', {
                bindings: {
                    'save': function (t) {
                        saveFault();
                    },
                    'view': function (t) {
                        viewFault();
                    }
                },
                onContextMenu: function (event, menu) {
                    var rowId = $(event.target).parent("tr").attr("id")
                    ddrAnalogGrid.jqGrid('resetSelection');
                    ddrAnalogGrid.jqGrid('setSelection', rowId);
                    return true;
                }
            });
            function viewFault() {
            	var sel_id = ddrAnalogGrid.jqGrid('getGridParam', 'selrow'); 
            	var fileName = ddrAnalogGrid.jqGrid('getCell', sel_id, 'fileName');
            	console.log("Calling Get file if not exist..");
            	getFileIfNotExists(fileName);
            	console.log("Calling show comtrade viewer..");
            	showComtradeViewer(fileName);
			}
            function saveFault() {
            	var sel_id = ddrAnalogGrid.jqGrid('getGridParam', 'selrow');
            	var fileName = ddrAnalogGrid.jqGrid('getCell', sel_id, 'fileName');
            	saveComtradeFiles(fileName);
            }
            function deleteFault() {
                var rowKey = ddrAnalogGrid.getGridParam("selrow");
                if (rowKey) {
                    ddrAnalogGrid.delGridRow(rowKey);
                }
                else {
                    alert("No rows are selected");
                }
            }
        });
    });
$('#autoDdrAnalogRefresh').change(function() {
    if($(this).is(":checked")) {
   		startTimer("ddrAnalogTab");
	}
   	else
   	{
   		stopTimer("ddrAnalogTab");
   	}
   });
function autoRefreshDdrAnalogEnabled()
{
	return $('#autoDdrAnalogRefresh').is(":checked");
}

$.subscribe('showDDRAnalogGridErrorMessage', function(event,data) {
	$("#ddrAnalogStatus").html('<p>Unable to fetch DDR Analog data. Either Session Timedout or an Error Occurred. Try clicking the Home link on right top corner to resolve session timeout. Please check logs</p>');
});