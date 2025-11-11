$(document).ready( function() { 
	$('[id="menuImportLocal"]').click(function( event ) {
		  event.preventDefault();
		  	$(this).publish('importTopic');
		$('#mainMenuAction').blur();
	});
$('[id="btnSubmitImport"]').click(function( event ) {
                  var msg="";
                if (archType != "REMOTE")
                {
                        msg = 'You are about to restore station configuration from this computer. This will remove all existing station and restore the selected station. Do you want to continue?';
                }
                else
                {
                        msg = 'You are about to restore station configuration from this computer. This will restore new station or overwrite existing station. Do you want to continue?'
                }
                  if (!confirm(msg))
                  {
                  	event.preventDefault();
                  }
		else
		{
			$("*").css("cursor", "progress");	
		}
        });
	$('[id^="menuImportFromBackup"]').click(function( event ) {
		  event.preventDefault();
			  $(this).publish('importFromBakupTopic');
			  $(this).publish('fetchBackupFilesTopic');
		$('#mainMenuAction').blur();
	});
$('[id="btnSubmitImportBackup"]').click(function( event ) {
                  var msg="";
                if (archType != "REMOTE")
                {
                      msg = 'You are about to restore one of the autosaved station configuration. This action will overwrite the existing station. Do you want to continue?';
		}
		else
		{ 
                      msg = 'You are about to restore one of the autosaved station configuration. This will restore the selected station as new or overwrite an existing station. Do you want to continue?';
		}
                  if (!confirm(msg))
                  {
                  	event.preventDefault();
                  }
		else
		{
			$("*").css("cursor", "progress");	
		}
        });
    $('[id="importSql"]').change(function() {
        if ($(this).val())
        {
                if ($(this).val().endsWith(".sql"))
                {
                        $('[id="btnSubmitImport"]').prop("disabled", false);
                }
                else
                {
                         $('[id="btnSubmitImport"]').prop("disabled", true);
                        alert("Please select M9000 config file with .sql extension");
                }
        }
        else
        {
                $('[id="btnSubmitImport"]').prop("disabled", true);
        }
});

    $('[id^="menuRemoveStationConfig"]').click(function( event ) {
	if (!confirm('You are about to remove this station configuration without removing its data. Do you want to continue?'))
	{
		console.log("Remove cancelled...");
		event.preventDefault();	
	}
	else
	{
    		$("*").css("cursor", "progress");
	}
	$('#mainMenuAction').blur();
    });
    $('[id^="menuClearData"]').click(function( event ) {
    	if (!confirm('You are about to clear all data associated with this station. Do you want to continue?'))
    	{
    		console.log("Clear cancelled...");
    		event.preventDefault();	
    	}
    	else
    	{
        		$("*").css("cursor", "progress");
    	}
	$('#mainMenuAction').blur();
        });
    $('[id^="menuRemoveStationWithData"]').click(function( event ) {
    	if (!confirm('You are about to remove this station along with all its associated data. Do you want to continue?'))
    	{
    		console.log("Remove with data cancelled...");
    		event.preventDefault();	
    	}
    	else
    	{
        		$("*").css("cursor", "progress");
    	}
    	$('#mainMenuAction').blur();
    });
    $('[id^="menuImportResoreConfig"]').click(function( event ) {
    	console.log("Arch type "+archType);
    	var msg="";
    	if (archType != "REMOTE")
    	{
    		msg = 'It will restore the latest configuration, if available, from any of the connected chassis and will overwrite existing station, if any present. Do you want to continue?';
    	}
    	else
    	{
    		msg = 'It will restore the configuration from the station master and will overwrite the existing station configuration. Do you want to continue?'
    	}
    	if (!confirm(msg))
    	{
    		console.log("Restore config cancelled...");
    		event.preventDefault();	
    	}
    	else
    	{
        	$("*").css("cursor", "progress");
    	}
    	$('#mainMenuAction').blur();
    });
    
    $('[id="menuExportPdf"]').click(function( event ) {
	openNewWindow();
    });
    $('[id="menuExportConfig"]').click(function( event ) {
	$('#menuExportConfig').blur();
    });

    $("*").css("cursor", "default");
    $(".traverseMenu").css('cursor', 'pointer');
    $('[id="idLinkUserConfig"]').css('cursor', 'pointer');
    
});
$.subscribe('clearPriorMsgs', function(event, data) {
    console.log("AFter successful trigger clear user messages!!");
    $('[id^="userMsg"]').empty();
});

$.subscribe('viewFault', function(event,data) {
	var sel_id = faultsGrid.jqGrid('getGridParam', 'selrow'); 
	var fileName = faultsGrid.jqGrid('getCell', sel_id, 'fileName');
	showComtradeViewer(fileName);
});
$.subscribe('viewDdrAnalog', function(event,data) {
	var sel_id = ddrAnalogGrid.jqGrid('getGridParam', 'selrow');
	var fileName = ddrAnalogGrid.jqGrid('getCell', sel_id, 'fileName');
	getFileIfNotExists(fileName);
	showComtradeViewer(fileName);
});
$.subscribe('viewDdrMeasurements', function(event,data) {
	var sel_id = ddrMeasurementsGrid.jqGrid('getGridParam', 'selrow');
	var fileName = ddrMeasurementsGrid.jqGrid('getCell', sel_id, 'fileName');
	showComtradeViewer(fileName);
});
$.subscribe('viewContAnalog', function(event, data) {
	var sel_id = contAnalogGrid.jqGrid('getGridParam', 'selrow');
	var fileName = contAnalogGrid.jqGrid('getCell', sel_id, 'fileName');
	getFileIfNotExists(fileName);
	showComtradeViewer(fileName);
});
$.subscribe('viewContMeasurements', function(event, data) {
	var sel_id = contMeasurementsGrid.jqGrid('getGridParam', 'selrow'); 
	var fileName = contMeasurementsGrid.jqGrid('getCell', sel_id, 'fileName');
	getFileIfNotExists(fileName);
	showComtradeViewer(fileName);
});
