<%@ include file="header.jsp"%>

<body>
	<script type="text/javascript"
		src="/M9000Master/struts/optiontransferselect.js"></script>
	<script type="text/javascript">
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
		document.getElementById("editSelectedLineGroup").value=document.getElementById("selectedLineGroup").value;
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
	function updateLineGroupName()
	{
		<% String selectedLineGroup=(String)request.getSession().getAttribute("selectedLineGroup"); %>
		var selectedLineGroup = "<%=selectedLineGroup%>"
		document.getElementById("editSelectedLineGroup").value=selectedLineGroup;
	}
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
			document.getElementById("checkBrowserClicks").value="false";
			document.getElementById("sourceTab").value=sourceValue;
			document.forms["manageLineGroups"].sourceTab.value=sourceValue;
			document.forms["manageLineGroups"].submit();
		}
		
	</script>
	<s:form theme="simple" name="manageLineGroups"
		action="manageLineGroups" method="POST">
		<s:if test="%{accessMode == 'Edit'}">
			<s:if test="#session.isDigital == true">
				<div style="text-align:left;font-weight:bold;">
					<s:a cssClass="traverseMenu" href="#"
						onclick="javascript:submitForm('StationDetails');"> StationDetails > </s:a>
					<s:a cssClass="traverseMenu" href="#" onclick="javascript:submitForm('Analogs');">Analogs  > </s:a>LineGroups > <s:a
						href="#" onclick="javascript:submitForm('Measurements');"> Measurements > </s:a>
					<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> > Virtual Measurements</s:a>
					<s:a href="#" onclick="javascript:submitForm('Events');">Events</s:a>
				</div>
			</s:if>
			<s:else>
				<div style="text-align:left;font-weight:bold;">
					<s:a cssClass="traverseMenu" href="#"
						onclick="javascript:submitForm('StationDetails');"> StationDetails > </s:a>
					<s:a cssClass="traverseMenu" href="#" onclick="javascript:submitForm('Analogs');">Analogs > </s:a>LineGroups > 
					<s:a cssClass="traverseMenu" href="#" onclick="javascript:submitForm('Measurements');"> Measurements</s:a>
					<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> > Virtual Measurements</s:a>
				</div>
			</s:else>
		</s:if>
		<s:actionerror id="userMsgError" theme="jquery"/>
		<s:actionmessage  id="userMsgInfo" theme="jquery"/>

		<table class="lineGroupsTable" style="width:50%">
			<tr>
				<td colspan="2" align="center" class="titleLabel"><div
						style="text-align:center; font-weight: bold;">
						<s:label theme="simple" value="Manage Line Groups"></s:label>
					</div></td>
			</tr>

			<s:url var="faultLocUrl" action="showLineGroupDetails"
				>
			</s:url>

			<s:if test="%{lstLineGroups != null && lstLineGroups.size()>0}">
				<tr>
					<td colspan="2" align="center">
						<fieldset>
							<div align="center">
								<s:label theme="simple"
									cssStyle="text-align: left; font-weight:bold;"
									value="LineGroup Name: "></s:label>
								&nbsp;
								<s:select cssStyle="width:15%" theme="simple"
									name="selectedLineGroup" id="selectedLineGroup"
									list="lstLineGroups" listKey="name" listValue="lineGroupName"
									label="Select a LineGroup" value="selectedLineGroup"
									onchange="javascript:show_linegroup(this);return false;">
								</s:select>
							</div>
						</fieldset>
					</td>
				</tr>
				<tr align="center">
					<td>
						<fieldset style="width: 30%">
							<s:submit theme="simple" cssClass="submit" id="btnEdit"
								name="btnEdit" value="Edit" 
								action="editLineGroup" />
							<s:submit theme="simple" cssClass="submit" id="btnDelete"
								name="btnDelete" value="Delete"
								onclick="return confirm('Are you sure you want to delete this lineGroup?')"
								action="deleteLineGroup"  />
							<s:submit theme="simple" cssClass="submit" id="btnNew"
								name="btnNew" value="New" 
								action="createLineGroup" />
						</fieldset>
					</td>
				</tr>

				<tr>
					<td colspan="2" align="center">
						 <sj:div showLoadingText="true" preload="true" id="details"
							href="%{#faultLocUrl}" listenTopics="show_linegroup_details"
							formIds="manageLineGroups" errorNotifyTopics="/unselectDetails">
						</sj:div></td>
				</tr>
				<tr>
					<td colspan="2" align="center">

						<div id="divEditMode">
							<s:submit theme="simple" cssClass="submit" name="btnSubmit"
								value="Back" action="lineGroupsBack" />
							<s:submit theme="simple" cssClass="submit" name="btnSubmit"
								value="Next" action="lineGroupsNext" />
						</div>
					</td>
				</tr>
			</s:if>
			<s:else>
				<tr>
					<td align="center"><s:label theme="simple"
							value="No line groups to display"></s:label></td>
				</tr>
				<tr>
					<td align="center">


						<div id="divEditMode">
							<s:submit theme="simple" cssClass="submit" name="btnSubmit"
								value="Back" action="lineGroupsBack" />
							<s:submit theme="simple" cssClass="submit" id="btnNew"
								name="btnNew" value="Create" 
								action="createLineGroup" />
							<s:submit theme="simple" cssClass="submit" name="btnSubmit"
								value="Next" action="lineGroupsNext" />
						</div>
					</td>
				</tr>
			</s:else>
		</table>
		<s:hidden id="sourceTab" name="sourceTab" value=""></s:hidden>
		<s:hidden id="jspFileName" name="displayFaultLocation.jsp"></s:hidden>
	</s:form>
</body>
</html>