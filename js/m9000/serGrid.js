var lastSelectedRow = null;
var serGrid = $("#serGrid");
$(document).ready( function() { 
	console.log("SER Page complete...");
});
function reloadSerGrid()
{
	serGrid.trigger("reloadGrid",[{current:true}]);
}

$.subscribe('afterReloadSerDates', function(event, data){ 
    var lstSize = $("#lstSerDates option").length;
	if (lstSize > 1)
	{
		$("#btnClearDatesSelectionTop, #btnClearDatesSelectionBottom").show();
	}
	else
	{
		$("#btnClearDatesSelectionTop, #btnClearDatesSelectionBottom").hide();
	}
	$('#lstSerDates').prop('selectedIndex', 0);
	// $("#lstSerDates option").each(function() {
	//        $(this).attr('title',$(this).text());
	// });
});

$.subscribe('oneditsuccess', function(event, data){
    var message = event.originalEvent.response.statusText;
    $("#gridinfo").html('<p>Status: ' + message + '</p>');
});
$.subscribe('rowadd', function(event,data) {
    serGrid.jqGrid('editGridRow',"new",{height:280,reloadAfterSubmit:false});
});
$.subscribe('searchgrid', function(event,data) {
    serGrid.jqGrid('searchGrid', {sopt:['cn','bw','eq','ne','lt','gt','ew']} );
});
/*
	 * Subscribe Topics for Grid Loadonce Example
	 */
	$.subscribe('showloadcolumns', function(event, data) {
		$("#gridloadtable").jqGrid('columnChooser', {});
	});
$.subscribe('showSerColumns', function(event,data) {
    serGrid.jqGrid('columnChooser',{});
});
$.subscribe('delete', function(event,data) {
    var rowid = serGrid.jqGrid('getGridParam','selrow');
    var faultId = serGrid.jqGrid('getCell', rowid, 'faultId');
    serGrid.jqGrid('delRowData',faultId, {
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

$.subscribe('beforeSelectRow', function (rowid, e) {
    if (!e.ctrlKey && !e.shiftKey) {
        serGrid.jqGrid('resetSelection');
    }
    else if (e.shiftKey) {
        var initialRowSelect = serGrid.jqGrid('getGridParam', 'selrow');
            alert("initialRowSelect "+initialRowSelect);
        serGrid.jqGrid('resetSelection');
        var CurrentSelectIndex = serGrid.jqGrid('getInd', rowid);
        var InitialSelectIndex = serGrid.jqGrid('getInd', initialRowSelect);
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
        $.each(serGrid.getDataIDs(), function(_, id){
            if ((shouldSelectRow = id == startID || shouldSelectRow)){
              serGrid.jqGrid('setSelection', id, false);
            }
            return id != endID;                        
        });
    }
    return true;
});
$(function(){
    $.subscribe('initSerGrid', function(event,data) {
    	if (!$("#errorStatus").is(':empty')){
    		console.log("Dates list is empty. Reloading..");
			 $('#btnExportSer').prop("disabled",true);
                $('#printExportConfig').prop("disabled",true);
    		$.publish('reloadSerDates');
    	}
    	else{
    		 $('#btnExportSer').prop("disabled",false);
             $('#printExportConfig').prop("disabled",false);
		}

        	$("#errorStatus").empty();
    	$("#errorStatus").hide();
    	if (lastSelectedRow == null)
        {
                var ids = serGrid.jqGrid('getDataIDs');
                lastSelectedRow = ids[0];
        }
		var records = serGrid.jqGrid("getGridParam", "records");
        console.log("Total records..."+records);
        if (records < 1)
        {
                $('#btnExportSer').prop("disabled",true);
                $('#printExportConfig').prop("disabled",true);
        }
        else
        {
                $('#btnExportSer').prop("disabled",false);
                $('#printExportConfig').prop("disabled",false);
        }
        var selRowIds = serGrid.jqGrid("getGridParam", "selarrrow");
        if ($.inArray(lastSelectedRow, selRowIds) < 0) {
                serGrid.jqGrid('setSelection', lastSelectedRow, true);
        }
        });
});
$('#autoSerRefresh').change(function() {
    if($(this).is(":checked")) {
   		startTimer("serTab");
	}
   	else
   	{
   		stopTimer("serTab");
   	}
   });
function autoRefreshSerEnabled()
{
	return $('#autoSerRefresh').is(":checked");
}

function openSerPdf(){
  	var status;
  	$.ajax({
	          url: '/M9000Master/checkSessionStatus',
	            type: 'POST',
	            dataType: 'json',           
	            success:function (res) {
        	status = res.currentSessionStatus;
		  if (res.currentSessionStatus == 'active')
		  {
		      var left = (screen.width/2);
		      left -= (700/2);
		      var top = (screen.height/2);
		      top -=(550/2);			 
		      window.open("/M9000Master/serReport", "_blank","status = 1,height = 550,width = 700,resizable = 1,left="+left+",top="+top); 
		  }
		  else
		  {
			window.location="/M9000Master/logout" ;
		  }
        	},
	            error:function(jqXhr, textStatus, errorThrown){
			window.location="/M9000Master/logout" ;
	            }
    });    
	return false;
  }	  

$("#btnClearDatesSelectionTop, #btnClearDatesSelectionBottom").click(function() {
	console.log("User clears the date selection");
	$.publish('reloadSerDates');
	$('#lstSerDates').prop('selectedIndex', 0);
	serGrid.jqGrid('setGridParam',{ postData:{ lstSelectedSerDates: ""}})
	reloadSerGrid();
});

$("#lstSerDates").change(function() {
	console.log("date selection "+$("#lstSerDates").val());
	serGrid.jqGrid('setGridParam',{ postData:{ lstSelectedSerDates: ""}})
	serGrid.jqGrid('setGridParam',{ postData:{ lstSelectedSerDates: $("#lstSerDates").val()}})
	reloadSerGrid();
});

$.subscribe('showSerGridErrorMessage', function(event,data) {
	$("#serStatus").html('<p>Unable to fetch SER. Either Session Timedout or an Error Occurred. Try clicking the Home link on right top corner to resolve session timeout.Please check logs</p>');
});
