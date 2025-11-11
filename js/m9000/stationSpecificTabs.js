var faultTimer = null;
var serTimer = null;
var ddrAnalogTimer = null;
var ddrMeasurementsTimer = null;
var scopeTimer = null;
var alarmsTimer = null;
var healthTimer = null;
var reloadScopeTabTimer = null;
$(document).ready( function() { 
	navigator.browserSpecs = (function(){
		    var ua = navigator.userAgent, tem, 
			M = ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || [];
		    if(/trident/i.test(M[1])){
			tem = /\brv[ :]+(\d+)/g.exec(ua) || [];
			return {name:'IE',version:(tem[1] || '')};
		    }
		    if(M[1]=== 'Chrome'){
			tem = ua.match(/\b(OPR|Edge)\/(\d+)/);
			if(tem != null) return {name:tem[1].replace('OPR', 'Opera'),version:tem[2]};
		    }
		    M = M[2]? [M[1], M[2]]: [navigator.appName, navigator.appVersion, '-?'];
		    if((tem = ua.match(/version\/(\d+)/i))!= null)
			M.splice(1, 1, tem[1]);
		    return {name:M[0], version:M[1]};
		})();

    $("*").css("cursor", "default");
    $(".traverseMenu").css('cursor', 'pointer');
    $('[id="idLinkUserConfig"]').css('cursor', 'pointer');
    
	 });
$.subscribe('tabchange', function(event, data) {
	var oldTab = event.originalEvent.ui.oldTab.attr("id");
	stopTimer(oldTab);
	var newTab = event.originalEvent.ui.newTab.attr("id");
	startTimer(newTab);
	if (newTab === "contAnalogTab")
	{
		if ((typeof reloadContAnalogGrid === 'function'))
		{
			reloadContAnalogGrid();
		}
	} else if (newTab === "contMeasurementsTab")
	{
		if ((typeof reloadContMeasurementsGrid === 'function'))
		{
			reloadContMeasurementsGrid();
		}
	}
});
function startTimer(tabId)
{
	switch(tabId)
		{
			case "faultTab":
				if (autoRefreshFaultsEnabled() && faultTimer === null) // only refresh when Checkbox is checked
				{
					faultTimer = setInterval(
					function() {
					    reloadFaultsGrid();
					},
					10000); // 10 Seconds
				}
				break;
			case "serTab":
				if (((typeof autoRefreshSerEnabled !== 'function') || autoRefreshSerEnabled()) && serTimer === null) // only refresh when Checkbox is checked or first time it loads
				{
					serTimer = setInterval(
					function() {
					    reloadSerGrid();
					},
					10000); // 10 Seconds
				}
				break;
			case "ddrAnalogTab":
				if (((typeof autoRefreshDdrAnalogEnabled !== 'function') || autoRefreshDdrAnalogEnabled()) && ddrAnalogTimer === null) // only refresh when Checkbox is checked or first time it loads
				{
					ddrAnalogTimer = setInterval(
					function() {
					    reloadDdrAnalogGrid();
					},
					10000); // 10 Seconds
				}
				break;
			case "ddrMeasurementsTab":
				if (((typeof autoRefreshDdrMeasurementsEnabled !== 'function') || autoRefreshDdrMeasurementsEnabled()) && ddrMeasurementsTimer === null) // only refresh when Checkbox is checked or first time it loads
				{
					ddrMeasurementsTimer = setInterval(
					function() {
					    reloadDdrMeasurementsGrid();
					},
					10000); // 10 Seconds
				}
				break;
			case "scopeTab":
				console.log("Existing scopeTimer?? "+scopeTimer);
				if (((typeof autoRefreshScopeEnabled !== 'function') || autoRefreshScopeEnabled())  && typeof refreshScope === 'function' && scopeTimer === null) // only refresh when Checkbox is checked
				{
					scopeTimer = setInterval(
					function() {
						refreshScope();
					},
					1000); // 1 second
					console.log("Starting scopeTimer "+scopeTimer);
				}
				break;
			case "alarmsTab":
				if (((typeof autoRefreshAlarmsEnabled !== 'function') || autoRefreshAlarmsEnabled()) && alarmsTimer === null) // only refresh when Checkbox is checked
				{
					alarmsTimer = setInterval(
					function() {
						reloadAlarmsGrid();
					},
					10000); // 10 seconds
				}
				break;
			case "healthMonitorTab":
				if ( healthTimer === null)
				{
					healthTimer = setInterval(
					function() {
						updateHealthStatus();
					},
					10000); // 10 seconds
				}
			default :
			;
		}
}
function stopTimer(tabId)
{
	switch(tabId)
	{
		case "faultTab":
			if (faultTimer != null)
			{
				clearInterval(faultTimer);
				faultTimer = null;
			}
			break;
		case "serTab":
			if (serTimer != null)
			{
				clearInterval(serTimer);
				serTimer = null;
			}
			break;
		case "ddrAnalogTab":
			if (ddrAnalogTimer != null)
			{
				clearInterval(ddrAnalogTimer);
				ddrAnalogTimer = null;
			}
			break;
		case "ddrMeasurementsTab":
			if (ddrMeasurementsTimer != null)
			{
				clearInterval(ddrMeasurementsTimer);
				ddrMeasurementsTimer = null;
			}
			break;
		case "scopeTab":
			if (scopeTimer != null)
			{
				console.log("Stopping SCOPE timer "+scopeTimer);
				clearInterval(scopeTimer);
				scopeTimer = null;
			}
			break;
		case "alarmsTab":
			if (alarmsTimer != null)
			{
				clearInterval(alarmsTimer);
				alarmsTimer = null;
			}
			break;
		case "healthMonitorTab":
			if (healthTimer != null)
			{
				clearInterval(healthTimer);
				healthTimer = null;
			}
			break;
		default:
			;
	}
}
function stopAllTimers()
{
	if (faultTimer != null)
	{
		clearInterval(faultTimer);
		faultTimer = null;
	}
	if (serTimer != null)
	{
		clearInterval(serTimer);
		serTimer = null;
	}
	if (ddrAnalogTimer != null)
	{
		clearInterval(ddrAnalogTimer);
		ddrAnalogTimer = null;
	}
	if (ddrMeasurementsTimer != null)
	{
		clearInterval(ddrMeasurementsTimer);
		ddrMeasurementsTimer = null;
	}
	if (scopeTimer != null)
	{
		console.log("Stopping SCOPE timer "+scopeTimer);
		clearInterval(scopeTimer);
		scopeTimer = null;
	}
	if (alarmsTimer != null)
	{
		clearInterval(alarmsTimer);
		alarmsTimer = null;
	}
	if (healthTimer != null)
	{
		clearInterval(healthTimer);
		healthTimer = null;
	}

}
function slowdownScopeRequestFrequency() // during calibration
{
	stopTimer('scopeTab');
	scopeTimer = setInterval(
			function() {
				refreshScope();
			},
			5000); // 1 second
}
function resetScopeRequestFrequency()
{
	stopTimer('scopeTab');
	startTimer('scopeTab');
}
function showComtradeViewer(fileName)
{
	var width=screen.width*.98;
	var height=screen.height*(3/4);
	var left=0;
	var right=0;

	if (navigator.browserSpecs.name === "IE" || ((navigator.browserSpecs.name === "Firefox") && (navigator.browserSpecs.version < 53)))
		{
//			var sel_id = grid.jqGrid('getGridParam', 'selrow'); 
//			var faultId = grid.jqGrid('getCell', sel_id, 'faultId'); 
//			var fileName = grid.jqGrid('getCell', sel_id, 'fileName');
		//	 var left = (screen.width/2);
		//      left -= (700/2);
		//      var top = (screen.height/2);
		//      top -=(550/2);	
			window.open("/M9000Master/viewComtrade?fileName="+fileName+"&stationId="+stationId, "_blank","status = 1,height="+height+",width="+width+",resizable = 1,left="+left+",top="+top); 
		}
	else
		{
//			alert("Java Plugin support is required to display COMTRADE viewer. Please use Internet Explorer to view COMTRADE files");
		// START: 19-Aug-2020 -- INtegrate with new comtrade viewer
//			if (!fileName.startsWith("/"))
//				{
//					fileName="/"+fileName;
//				}
//	        var url="/COMTRADEViewer"+fileName;
//	        var encodedUrl = encodeURIComponent(url);
//	        console.log("Filename "+fileName+" url "+url+" encoded url "+encodedUrl);
//	        window.open(url, "_blank","status = 1,height="+height+",width="+width+",resizable = 1,left="+left+",top="+top);
		
        if (!fileName.startsWith("/"))
        {
              fileName="/"+fileName;
		}
        var url="/COMTRADEViewer/v"+fileName;
        var encodedUrl = encodeURIComponent(url);
        console.log("Filename "+fileName+" url "+url+" encoded url "+encodedUrl);
        fetch("/COMTRADEViewer", { method: 'HEAD' })
        .then(function (res) {
            if (res.ok) {
                console.log('New COMTRADE viewer available.');
                window.open(url, "_blank","status = 1,height="+height+",width="+width+",resizable = 1,left="+left+",top="+top);
            } else {
                console.log('New COMTRADE viewer is not ready yet');
                alert("Java Plugin support is required to display COMTRADE viewer. Please use Internet Explorer to view COMTRADE files");
                window.close();
                return false;
            }
        }).catch(function (err) { console.log('Error:', err);});

        
		// END: 19-Aug-2020

		}
}
function saveComtradeFiles(fileName)
{
//	var sel_id = grid.jqGrid('getGridParam', 'selrow'); 
//	var faultId = grid.jqGrid('getCell', sel_id, 'faultId'); 
//	var fileName = grid.jqGrid('getCell', sel_id, 'fileName'); 
//	window.location = "/M9000Master/getZipFile?fileName="+fileName+"&stationId="+stationId;	
    downloadFile("/M9000Master/getZipFile?fileName="+fileName+"&stationId="+stationId);
}
// // added to avoid session timeout when savefile was the first action. All browsers but IE fix
//function downloadFile(filePath){
//    var link=document.createElement('a');
//    link.href = filePath;
//    link.download = filePath.substr(filePath.lastIndexOf('/') + 1);
//    link.click();
//}
// added to avoid session timeout when savefile was the first action. IE fix 
function downloadFile(url){
    var oIframe = window.document.createElement('iframe');
    var $body = jQuery(document.body);
    var $oIframe = jQuery(oIframe).attr({
        src: url,
        style: 'display:none'
    });
    $body.append($oIframe);

}

function getFileIfNotExists(fileName)
{
	$.ajax({
    	url: "/M9000Master/getFile?fileName="+fileName+"&stationId="+stationId,
    	async: false,
    	success: function(data){ 
    	    console.log(data);
    	},
    	error: function(){
    		alert("There was an error.");
    	}
    });
}
function addMinutes(date, minutes) {
	return new Date(date.getTime() + minutes * 60000);
}
function addSeconds(date, seconds) {
	return new Date(date.getTime() + seconds * 1000);
}
if ($.validator != null)
{
	$.validator.addMethod("analogGreaterThan",
	function(value, element, params) {
		if (!/Invalid|NaN/.test(new Date(value))) {
			return new Date(value) > new Date($(params).val());
		}
		return isNaN(value) && isNaN($(params).val())
		|| (Number(value) > Number($(params).val()));
	}, 'Must be greater than {0}.');
	$.validator.addMethod("analogTimediff",
	function(value, element, params) {
		console.log("contAnalogLimit during validation "+contAnalogLimit);
		var limit = contAnalogLimit * 1000;
		if (!/Invalid|NaN/.test(new Date(value))) {
			return ((new Date(value) - new Date($(params).val())) <= limit);
		}
		return isNaN(value) && isNaN($(params).val())
		|| ((new Date(value) - new Date($(params).val())) <= limit);
	}, 'Please request less than '+(contAnalogLimit/60)+' minutes .');
	$.validator.addMethod("futureDate",
	function(value, element) {
		var now = new Date();
		var myDate = new Date(value);
		var currentTime= now.getTime();
		var myTime = myDate.getTime();
		console.log("Validating future date now: "+now+" myDate "+myDate+" currentTIme "+currentTime+" myTime "+myTime);
	    return this.optional(element) || (myDate <= now && ((myTime-currentTime) < 0));
	}, 'Date cannot be in future');
	$.validator.addMethod("measurementsGreaterThan",
			function(value, element, params) {
				if (!/Invalid|NaN/.test(new Date(value))) {
					return new Date(value) > new Date($(params).val());
				}
				return isNaN(value) && isNaN($(params).val())
				|| (Number(value) > Number($(params).val()));
			}, 'Must be greater than {0}.');
			$.validator.addMethod("measurementsTimediff",
			function(value, element, params) {
				var limit = contDataLimit * 1000;
				if (!/Invalid|NaN/.test(new Date(value))) {
					return ((new Date(value) - new Date($(params).val())) <= limit);
				}
				return isNaN(value) && isNaN($(params).val())
				|| ((new Date(value) - new Date($(params).val())) <= limit);
			}, 'Please request less than ' + (contDataLimit / 60) + ' minutes .');

	$("#NextToCreateOrUpdate").validate({
		rules : {
			"analogEndTime" : {
				analogGreaterThan : "#analogStartTime",
				analogTimediff : "#analogStartTime"
			},
			"measurementsEndTime" : {
				measurementsGreaterThan : "#measurementsStartTime",
				measurementsTimediff : "#measurementsStartTime"
			},
			"measurementTypes" : {
				required: true
			},
			"analogStartTime" : {
				required : true
			},
			"measurementsStartTime" : {
				required : true
			},
		        requestedCycles: {required: true,number:true,min:1,max:10},
			desiredValue: {number:true},
			measurementTotalTime: {number:true,min:1},
			totalTime: {number:true,min:1}
		},
	        messages: {
	            'measurementTypes': {
	                required: "You must check at least 1 box",
	            },
		        requestedCycles: {required: "Cannot be blank",number:"Numeric field",min: "Minimum 1 cycle",max: "Maximum 10 cycles"},
			desiredValue: {number:"Numeric field"},
			measurementTotalTime: {number:"Numeric field",min: "Minimum value of 1"},
			totalTime: {number:"Numeric field",min: "Minimum value of 1"}
	        }
	});
}
var datePick = function(elem)
{
	$(elem).datepicker();
	$('#ui-datepicker-div').css("z-index", 2000);
}
function reloadScopeTab()
{
	console.log("Reload the scope...");
	$.publish('reloadScopeTab');
}
function startScopeLoadTimer()
{
	console.log("Starting the scope reload timer...");
	if (reloadScopeTabTimer == null)
	{
		reloadScopeTabTimer = setInterval(
		function() {
			reloadScopeTab();
		},
		60000);
	}
}
function stopScopeLoadTimer()
{
	console.log("Stopping the scope reload timer...");
	if (reloadScopeTabTimer != null)
	{
		clearInterval(reloadScopeTabTimer);
	}
}
$.subscribe('refreshScopeError', function(event, data) {
	console.log("refreshScopeError published and hence stopping scopeTab"+scopeTimer);
	stopTimer("scopeTab");
});
$.subscribe('showErrorMessage', function(event,data) {
	$("#errorStatus").show();
	$("#errorStatus").html('<b>ERROR: Trouble accessing data. May be due to Session Timeout or Server Restart or due to database connectivity. Try clicking the Home link on right top corner. If error persists, please check logs</b>');
});

$.subscribe('eventTestChange', function(event, data) {
        console.log("TestEVS SCOPETAB text Change data "+$("#txtEventTestStatus").val()+" event "+event.innerHTML);
        if ($("#txtEventTestStatus").val().indexOf("STATUS") != -1)
        {
			$("#lblEventTestStatus").text($("#txtEventTestStatus").val().substring($("#txtEventTestStatus").val().indexOf("STATUS")));
			$("#lblEventTestStatus").css({"background-color": "#fef1ec","color":"red"});
			$("#txtEventTestStatus").val($("#txtEventTestStatus").val().substring(0,$("#txtEventTestStatus").val().indexOf("STATUS")));
			$("#divEventTestStatus").show();
		}
		else if ($("#divEventTestStatus").is(":hidden"))
		{
			$("#divEventTestStatus").hide();
			$("#lblEventTestStatus").css({"background-color": "white","color":"black"});
			$("#lblEventTestStatus").text("");
		}
        
});

$.subscribe('CalChange', function(event, data) {
        console.log("Calibration text Change data "+$("#txtCalStatus").val()+" event "+event.innerHTML);
        if ($("#txtCalStatus").val().indexOf("STATUS") != -1)
        {
			$("#lblCalStatus").text($("#txtCalStatus").val().substring($("#txtCalStatus").val().indexOf("STATUS")));
			$("#lblCalStatus").css({"background-color": "#fef1ec","color":"red"});
			$("#txtCalStatus").val($("#txtCalStatus").val().substring(0,$("#txtCalStatus").val().indexOf("STATUS")));
			$("#divCalStatus").show();
		}
		else if ($("#divCalStatus").is(":hidden"))
		{
			$("#divCalStatus").hide();
			$("#lblCalStatus").css({"background-color": "white","color":"black"});
			$("#lblCalStatus").text("");
		}
        
});

$.subscribe('CalVerifyChange', function(event, data) {
        console.log("Calibration text Change data "+$("#txtCalVerifyStatus").val()+" event "+event.innerHTML);
        if ($("#txtCalVerifyStatus").val().indexOf("STATUS") != -1)
        {
			$("#lblCalVerifyStatus").text($("#txtCalVerifyStatus").val().substring($("#txtCalVerifyStatus").val().indexOf("STATUS")));
			$("#lblCalVerifyStatus").css({"background-color": "#fef1ec","color":"red"});
			$("#txtCalVerifyStatus").val($("#txtCalVerifyStatus").val().substring(0,$("#txtCalVerifyStatus").val().indexOf("STATUS")));
			$("#divCalVerifyStatus").show();
		}
		else if ($("#divCalVerifyStatus").is(":hidden"))
		{
			$("#divCalVerifyStatus").hide();
			$("#lblCalVerifyStatus").css({"background-color": "white","color":"black"});
			$("#lblCalVerifyStatus").text("");
		}
        
});
