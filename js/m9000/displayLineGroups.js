$(document).ready(function(){
        if (userRole != 'admin')
        {
                $("#manageLineGroups :input[type=submit]").prop("disabled", true);
        }
        if (!isDfrAddedOrRemoved)
        {
	 		$('#navMenu').show();
        }

});
	$.subscribe("/beforeEdit", function(event, widget){
	    document.getElementById('divEditMode').style.visibility='hidden';
	    document.getElementById('divSaveMode').style.visibility='visible';
	    document.getElementById('divEditLineGroup').style.visibility='visible';
	    document.getElementById('divNewLineGroup').style.visibility='hidden';
	    document.getElementById('divExistingLineGroup').style.visibility='hidden';
		});
	$.subscribe("/beforeNew", function(event, widget){
	    document.getElementById('divEditMode').style.visibility='hidden';
	    document.getElementById('divSaveMode').style.visibility='visible';
	    document.getElementById('divEditLineGroup').style.visibility='hidden';
	    document.getElementById('divNewLineGroup').style.visibility='visible';
	    document.getElementById('divExistingLineGroup').style.visibility='hidden';
	    
		});
	$.subscribe("/beforeSave", function(event, widget){
	    document.getElementById('divEditMode').style.visibility='visible';
	    document.getElementById('divSaveMode').style.visibility='hidden';
	    document.getElementById('divEditLineGroup').style.visibility='hidden';
	    document.getElementById('divNewLineGroup').style.visibility='hidden';
	    document.getElementById('divExistingLineGroup').style.visibility='visible';
	    
		});
	$.subscribe("/beforeCancel", function(event, widget){
	    document.getElementById('divEditMode').style.visibility='visible';
	    document.getElementById('divSaveMode').style.visibility='hidden';
	    document.getElementById('divEditLineGroup').style.visibility='hidden';
	    document.getElementById('divNewLineGroup').style.visibility='hidden';
	    document.getElementById('divExistingLineGroup').style.visibility='visible';
	    
		});


	$.subscribe("/beforeShow", function(event, widget){
	    var form = document.manageLineGroups;
	    var btnEdit = form.btnEdit;
	    
			var selectObj = document.getElementById("lineGroupDetails");
			if(selectObj != null)
			{
				selectAllOptionsExceptSome(selectObj, "key", "HEADERNOTRANSFER");
			}
			var selectObjRight = document.getElementById("availableChannels");
			if (selectObjRight != null)
			{
				selectAllOptionsExceptSome(selectObjRight, "key", "HEADERNOTRANSFER");
			}
			alert('Before show publishing ');
		});

	$.subscribe("/unselectDetails", function(data, request, widget){
		$.publish("show_linegroup_details");
		document.getElementById("lineGroupName").value=document.getElementById("selectedLineGroup").value;
		var selectObj = document.getElementById("lineGroupDetails");
		if(selectObj != null)
		{
			unSelectMatchingOptions(selectObj,"");
		}
		var selectObjRight = document.getElementById("availableChannels");
		if (selectObjRight != null)
		{
			unSelectMatchingOptions(selectObjRight,"");
			
		}
	    
		});
	$.subscribe("/afterNew", function(data, request, widget){
		alert('show_linegroup_details');
		$.publish("show_linegroup_details");
	});
	$.subscribe("/afterSave", function(data, request, widget){
		$.publish("update_linegroup_list");
		document.getElementById('newLineGroupName').value="";
		
	});
	function selectUnselectMatchingOptions(obj,regex,which,only,key) {
		if (window.RegExp) {
			var tempVar = 'text';
			if (arguments.length > 4) {
				tempVar = arguments[4];
			}
			
			if (which == "select") {
				var selected1=true;
				var selected2=false;
				}
			else if (which == "unselect") {
				var selected1=false;
				var selected2=true;
				}
			else {
				return;
				}
			var re = new RegExp(regex);
			if (!hasOptions(obj)) { 
				return; 
			}
			for (var i=0; i<obj.options.length; i++) {
				if (tempVar == 'key') {
					if (re.test(obj.options[i].value)) {
						obj.options[i].selected = selected1;
					}
					else if (obj.options[i].value == '') {
						obj.options[i].selected = selected1;
					}
					else {
						if (only == true) {
							obj.options[i].selected = selected2;
						}
					}
				}
				else {
					if (re.test(obj.options[i].text)) {
						obj.options[i].selected = selected1;
					}
					else {
						if (only == true) {
							obj.options[i].selected = selected2;
						}
					}
				}
				}
			}
		}

	// -------------------------------------------------------------------
	// hasOptions(obj)
	//  Utility function to determine if a select object has an options array
	// -------------------------------------------------------------------
	function hasOptions(obj) {
		if (obj!=null && obj.options!=null) { return true; }
		return false;
		}
		
	// -------------------------------------------------------------------
	// unSelectMatchingOptions(select_object,regex)
	//  This function Unselects all options that match the regular expression
	//  passed in. 
	// -------------------------------------------------------------------
	function unSelectMatchingOptions(obj,regex) {
		selectUnselectMatchingOptions(obj,regex,"unselect",false,"key");
		}
	
		function show_linegroup(field) {
			if (field != null)
			{
				var value = field.value;
			}
			$.publish("show_linegroup_details");
			}
		function modify_linegroup() {
			alert('In modify line group...');
			var selectObj = document.getElementById("lineGroupDetails");
			if(selectObj != null)
			{
				selectAllOptionsExceptSome(selectObj, "key", "HEADERNOTRANSFER");
			}
			var selectObjRight = document.getElementById("availableChannels");
			if (selectObjRight != null)
			{
				selectAllOptionsExceptSome(selectObjRight, "key", "HEADERNOTRANSFER");
				
			}
			$.publish("modify_linegroup_details");
			}
		function edit_linegroup(object) {
			if (object.value == 'Edit')
			{
				object.value='Save';
			}
			else
			{
				object.value='Edit';
			}
			$.publish("show_linegroup_details");
			}
		function submitChange() {
			document.manageLineGroups.submit();
		}
		function submitForm(sourceValue)
		{
                        if (userRole != 'admin')
                        {
                		$("#manageLineGroups :input").prop("disabled", false);
			}
			document.getElementById("checkBrowserClicks").value="false";
			var booAnalogValue = document.getElementById("booAnalog").value;
			var booDigitalValue = document.getElementById("booDigital").value;
			document.forms["manageLineGroups"].booAnalog.value=booAnalogValue;
			document.forms["manageLineGroups"].booDigital.value=booDigitalValue;
			
			document.getElementById("sourceTab").value=sourceValue;
			document.forms["manageLineGroups"].decisionLogic.value="false";
			document.forms["manageLineGroups"].sourceTab.value=sourceValue;
			document.getElementById("originTab").value="LineGroups";
			document.forms["manageLineGroups"].originTab.value="LineGroups";
			document.forms["manageLineGroups"].submit();
		}
