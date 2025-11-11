var originalContent;
var lastSelectedRow = null;
var faultsGrid = $("#faultsGrid");
$(document).ready( function() { 
       startTimer("faultTab");
       $('#divLeftPane').css({
	       width: 300,
	       height: '100%'
	   }).split({
	       orientation: 'horizontal',
	       limit: 25,
		position: '50%',
	       percent: true
	   });
 });
function reloadFaultsGrid()
{
	faultsGrid.trigger("reloadGrid",[{current:true}]);
}
$.subscribe('oneditsuccess', function(event, data){
    var message = event.originalEvent.response.statusText;
    $("#gridinfo").html('<p>Status: ' + message + '</p>');
});
$.subscribe('rowadd', function(event,data) {
    faultsGrid.jqGrid('editGridRow',"new",{height:280,reloadAfterSubmit:false});
});
$.subscribe('searchgrid', function(event,data) {
    faultsGrid.jqGrid('searchGrid', {sopt:['cn','bw','eq','ne','lt','gt','ew']} );
});
/*
	 * Subscribe Topics for Grid Loadonce Example
	 */
	$.subscribe('showloadcolumns', function(event, data) {
		$("#gridloadtable").jqGrid('columnChooser', {});
	});
$.subscribe('showFaultsColumns', function(event,data) {
    faultsGrid.jqGrid('columnChooser',{});
});
$.subscribe('delete', function(event,data) {
    var rowid = faultsGrid.jqGrid('getGridParam','selrow');
    var faultId = faultsGrid.jqGrid('getCell', rowid, 'faultId');
    faultsGrid.jqGrid('delRowData',faultId, {
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
    
$.subscribe('updateActiveEvents', function(event,data) {
		var sel_id = faultsGrid.jqGrid('getGridParam', 'selrow'); 
		 if (sel_id != null)
         {
                 lastSelectedRow = sel_id;
         }
		var activeEvents = faultsGrid.jqGrid('getCell', sel_id, 'activeEvents'); 
		var faultLoc = faultsGrid.jqGrid('getCell', sel_id, 'faultLocationDetails'); 
		$('#txtFaultLoc').val(faultLoc);
	if (activeEvents != null && activeEvents != '')
	{
		var fields = activeEvents.split("\n");
	//		$('#itemSelect').html(fields);
		var $select = $('#itemSelect');
	    $select.find('option').remove();
	    $.each(fields, function(index, value) {
	      $('<option>').val(value).text(value).appendTo($select);
	    });
	}
	else
	{
		$("#itemSelect").empty();
	}
    });	
    $.subscribe('storeSelection', function(event,data) {
		var saveSelectedRows  = faultsGrid.jqGrid('getGridParam', 'selrow'); 
		if (saveSelectedRows != null)
		{
			lastSelectedRow = saveSelectedRows;
		}
		var page =  faultsGrid.jqGrid('getGridParam', 'page');
		faultsGrid.data(page.toString(), saveSelectedRows);
    });
});
$.subscribe('beforeSelectRow', function (rowid, e) {
    if (!e.ctrlKey && !e.shiftKey) {
        faultsGrid.jqGrid('resetSelection');
    }
    else if (e.shiftKey) {
        var initialRowSelect = faultsGrid.jqGrid('getGridParam', 'selrow');
            alert("initialRowSelect "+initialRowSelect);
        faultsGrid.jqGrid('resetSelection');
        var CurrentSelectIndex = faultsGrid.jqGrid('getInd', rowid);
        var InitialSelectIndex = faultsGrid.jqGrid('getInd', initialRowSelect);
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
        $.each(faultsGrid.getDataIDs(), function(_, id){
            if ((shouldSelectRow = id == startID || shouldSelectRow)){
              faultsGrid.jqGrid('setSelection', id, false);
            }
            return id != endID;                        
        });
    }
    return true;
});
$.subscribe('showFaultGridErrorMessage', function(event,data) {
	$("#faultStatus").html('<b>Error in getting faults. Reasons may be due to Session Timeout or Server Restart or an Error Occurred due to database connectivity. Try clicking the Home link on right top corner. If error still persists, Please check logs</b>');
});
$(function(){
    $.subscribe('initFaultsGrid', function(event,data) {
    	$("#errorStatus").empty();
    	$("#errorStatus").hide();
//    	var message = event.originalEvent.status;
//    	console.log("data "+data.innerHtml+" event "+event);
//    	if (message != null && message != '')
//    	{
//    		console.log("event.originalEvent.response "+event.originalEvent.response);
//    		console.log("event.originalEvent.status "+event.originalEvent.status);
//    		$("#faultStatus").html('<p>Status: ' + message + '</p>');
//    	}
    console.log("initFaultGrid Role "+userRole);
        if (userRole != 'admin') {
                $('[id^=del_faultsGrid]').hide();
         } else {
                $('[id^=del_faultsGrid]').show();
        }	
        var selRowIds = faultsGrid.jqGrid("getGridParam", "selarrrow");
        $.publish("updateActiveEvents");
            $("tr.jqgrow", "#faultsGrid").contextMenu('faultMenu', {
                bindings: {
                    'save': function (t) {
                        saveFault();
                    },
                    'view': function (t) {
                        viewFault();
                    },
                    'del': function (t) {
                        deleteFault();
                    },
                    'faultLoc': function (t) {
                    	calculateFaultLocation();
                    }
                },
                onContextMenu: function (event, menu) {
                    var rowId = $(event.target).parent("tr").attr("id")
                    faultsGrid.jqGrid('resetSelection');
                    faultsGrid.jqGrid('setSelection', rowId);
                    return true;
                }
            });
            function viewFault() {
            	var sel_id = faultsGrid.jqGrid('getGridParam', 'selrow'); 
		console.log("View Selected id "+sel_id);
            	var fileName = faultsGrid.jqGrid('getCell', sel_id, 'fileName');
            	showComtradeViewer(fileName);
			}
            function saveFault() {
            	var sel_id = faultsGrid.jqGrid('getGridParam', 'selrow'); 
		console.log("Save Selected id "+sel_id);
            	var fileName = faultsGrid.jqGrid('getCell', sel_id, 'fileName');
            	saveComtradeFiles(fileName);
            }
            function deleteFault() {
                var rowKey = faultsGrid.jqGrid('getGridParam', 'selrow');
                if (rowKey) {
                    faultsGrid.delGridRow(rowKey);
                }
                else {
                    alert("No rows are selected");
                }
            }
            function calculateFaultLocation() {
				console.log("Fault Location button clicked");
				var rowKey = faultsGrid.jqGrid('getGridParam', 'selrow');
				console.log("Fault location for id "+rowKey);
				$("#faultIds").val(rowKey);
				$.publish("dlgFaultLocationTopic");
				$.publish("faultLocTopic");
            }
        });
    });
$('#autoFaultsRefresh').change(function() {
    if($(this).is(":checked")) {
   		startTimer("faultTab");
	}
   	else
   	{
   		stopTimer("faultTab");
   	}
   });
function autoRefreshFaultsEnabled()
{
	return $('#autoFaultsRefresh').is(":checked");
}
$.subscribe('beforeFaultLocCalc', function (rowid, e) {
	originalContent = $("#dlgShowFaultLoc").html();
	                                console.log("original content "+originalContent);
	});

function isError(text) {
	if(text.indexOf('ERROR') >= 0) { 
		return [false, text]; 
	}
	return [true,''];
}
$('#btnFaultLoc').click(function() {
	console.log("Internal Calibration button clicked");
		$.publish("faultLocTopic");
		$( "#dlgShowFaultLoc" ).dialog( "option", "title", "Internal Calibration" );
		 $( "#dlgShowFaultLoc" ).dialog( "option", "position", { my: "center", at: "center", of: window } );	
});