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
		if (document.getElementById('divEditMode').style.visibility!='hidden')
		{
	    	document.getElementById('divSaveMode').style.visibility='hidden';
		}
	    var form = document.newEditLineGroup;
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
	
	function updateLineGroupName()
	{
		<% String selectedLineGroup=(String)request.getSession().getAttribute("selectedLineGroup"); %>
		var selectedLineGroup = "<%=selectedLineGroup%>"
		document.getElementById("lineGroupName").value=selectedLineGroup;
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
				document.getElementById("lineGroupName").value=value;
			}
			$.publish("show_linegroup_details");
			}
		function modify_linegroup() {
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
		
	</script>
	<s:form theme="simple" id="newEditLineGroup" name="newEditLineGroup"
		action="newEditLineGroupAction" method="POST">
		<s:actionerror id="userMsgError" theme="jquery"/>
		<s:actionmessage  id="userMsgInfo" theme="jquery"/>
		<table class="lineGroupsTable" style="width:50%">
			<tr>
				<td colspan="2" align="center" class="titleLabel"><div
						style="text-align:center; font-weight: bold;">
						<s:if test="%{actionType != 'EDIT'}">
							<s:label theme="simple" value="Create a Line Group"></s:label>
						</s:if>
						<s:else>
							<s:label theme="simple" value="Edit Line Group"></s:label>
						</s:else>
					</div></td>
			</tr>


			<tr>
				<td colspan="2" align="center">
					<fieldset>
						<s:if test="%{actionType != 'EDIT'}">
							<div id="divNewLineGroup">
								<s:label cssStyle="font-weight: bold;" theme="simple"
									value="Enter a New LineGroup name "></s:label>
						&nbsp;
						<s:textfield size="100" theme="simple" name="selectedLineGroup"
									id="selectedLineGroup" value="%{selectedLineGroup}">
								</s:textfield>
							</div>
						</s:if>
						<s:else>
							<div id="divEditLineGroup">
								<s:label cssStyle="font-weight: bold;" theme="simple"
									value="Edit Line Group Name "></s:label>
							&nbsp;
							<s:textfield cssStyle="width:50%;text-align: left; " size="100" theme="simple"
									name="editedLineGroup.lineGroupName" id="editedLineGroup.lineGroupName"
									value="%{editedLineGroup.lineGroupName}" />
							<s:hidden id="selectedLineGroup" name="selectedLineGroup" value="%{editedLineGroup.name}"></s:hidden>
							</div>
						</s:else>
					</fieldset>
				</td>
			</tr>
<!-- 			<s:url var="editUrl" action="updateLineGroupDetails">
			</s:url>
			<sj:div id="editDetail" href="%{#editUrl}"
				listenTopics="modify_linegroup_details" formIds="newEditLineGroup"
				onSuccessTopics="/unselectDetails"
				onErrorTopics="/unselectDetails" deferredLoading="true" cssStyle="display:none"/>
 -->
			<s:if test="%{actionType == 'EDIT'}">
				<tr>
					<td colspan="100%" align="center"><s:label theme="simple"
							value="Fault Location Details"></s:label>
						<table style="width:100%" class="inlineTable"
							title="Fault Location Details">
							<tr>
								<td style="text-align: right;width:50%"><s:label
										cssStyle="font-weight:bold; text-align:right;" theme="simple"
										key="label.lg.autocalculate"></s:label></td>
								<td style="width:50%"><s:select theme="simple"
										name="editedLineGroup.enableAutoCalc"
										key="label.lg.autocalculate" list="{'No','Yes'}"
										value="%{editedLineGroup.enableAutoCalc}" /></td>
							</tr>
							<tr>
								<td style="text-align: right;width:50%"><s:label
										cssStyle="font-weight:bold;" theme="simple"
										key="label.lg.positiveresistance"></s:label></td>
								<td style="width:50%"><s:textfield size="10" theme="simple"
										name="editedLineGroup.positiveResistance"
										key="label.lg.positiveresistance"
										value="%{editedLineGroup.positiveResistance}" /></td>
							</tr>
							<tr>
								<td style="text-align: right;width:50%"><s:label
										cssStyle="font-weight:bold;" theme="simple"
										key="label.lg.positivereactance"></s:label></td>
								<td style="width:50%"><s:textfield size="10" theme="simple"
										name="editedLineGroup.positiveReactance"
										key="label.lg.positivereactance"
										value="%{editedLineGroup.positiveReactance}" /></td>
							</tr>
							<tr>
								<td style="text-align: right;width:50%"><s:label
										cssStyle="font-weight:bold;" theme="simple"
										key="label.lg.zeroresistance"></s:label></td>
								<td style="width:50%"><s:textfield size="10" theme="simple"
										name="editedLineGroup.zeroResistance"
										key="label.lg.zeroresistance"
										value="%{editedLineGroup.zeroResistance}" /></td>
							</tr>
							<tr>
								<td style="text-align: right;width:50%"><s:label
										cssStyle="font-weight:bold;" theme="simple"
										key="label.lg.zeroreactance"></s:label></td>
								<td style="width:50%"><s:textfield size="10" theme="simple"
										name="editedLineGroup.zeroReactance"
										key="label.lg.zeroreactance"
										value="%{editedLineGroup.zeroReactance}" /></td>
							</tr>
							<tr>
								<td style="text-align: right;width:50%"><s:label
										cssStyle="font-weight:bold;" theme="simple"
										key="label.lg.linelength"></s:label></td>
								<td style="width:50%"><s:textfield size="10" theme="simple"
										name="editedLineGroup.lineMiles" key="label.lg.linelength"
										value="%{editedLineGroup.lineMiles}" /></td>
							</tr>
							<s:hidden id="editedLineGroup.decisionLogic"
								name="editedLineGroup.decisionLogic" />
							<s:hidden id="editedLineGroup.type" name="editedLineGroup.type" />
							<s:hidden id="editedLineGroup.isLocal"
								name="editedLineGroup.isLocal" />
							<s:hidden id="editedLineGroup.chassis"
								name="editedLineGroup.chassis" />
							<s:hidden id="editedLineGroup.id" name="editedLineGroup.id" />
							<s:hidden id="editedLineGroup.name" name="editedLineGroup.name" />
						</table></td>
				</tr>
				<tr>
					<td style="font-size: 12px;" align="center">
					<fieldset style="text-align: center; width: 60%">
						<legend>Add or Remove Channels</legend>
						<s:optiontransferselect
							cssStyle="font-family:courier, courier new, serif;"
							doubleCssStyle="font-family:courier, courier new, serif;"
							buttonCssClass="submit" doubleSize="12" size="12" headerKey="HEADERNOTRANSFER"
							headerValue="--- Selected Channels ---" doubleHeaderKey="HEADERNOTRANSFER"
							doubleHeaderValue="--- Available Channels -- "
							emptyOption="false" doubleEmptyOption="false"
							allowAddAllToLeft="false" allowAddAllToRight="false"
							allowSelectAll="false" allowUpDownOnLeft="false"
							allowUpDownOnRight="false" leftTitle="%{selectedLineGroup}"
							rightTitle="Channels List" id="lineGroupDetails" 
							name="lineGroupDetails" list="editedLineGroup.inputChannels" listKey="displayChannel" listValue="displayName"
							doubleId="availableChannels" doubleName="availableChannels"
							doubleList="lstAvailableAnalogChannels" doubleListKey="displayChannel" doubleListValue="displayName"
							formName="newEditLineGroup"
							addToLeftOnclick="javascript:modify_linegroup()"
							addToRightOnclick="javascript:modify_linegroup()" />
						</fieldset>
					</td>
				</tr>
				<tr>
				<td style="font-family:courier, courier new, serif;font-size: 12px;" align="center">
					<fieldset style="text-align: center; width: 60%">
						<legend>Comments</legend>
						<s:textarea theme="simple"  labelposition="top" label="Comments" tooltip="Any user comments or notes for this LineGroup"  
							name="editedLineGroup.comments" cols="98" rows="10" cssStyle="overflow: scroll; resize: none;" value="%{editedLineGroup.comments}"/>
					</fieldset>
				</td>
				</tr>
			</s:if>
			<s:else>
				<tr>
					<td colspan="100%" align="center"><s:label theme="simple"
							value="Fault Location Details"></s:label>
						<table style="width:100%" class="inlineTable"
							title="Fault Location Details">
							<tr>
								<td style="text-align: right;width:50%"><s:label
										cssStyle="font-weight:bold; text-align:right;" theme="simple"
										key="label.lg.autocalculate"></s:label></td>
								<td style="width:50%"><s:select theme="simple"
										name="lstLineGroups[%{selectedLineGroupIndex}].enableAutoCalc"
										key="label.lg.autocalculate" list="{'No','Yes'}"
										value="%{lstLineGroups[selectedLineGroupIndex].enableAutoCalc}" />
								</td>
							</tr>
							<tr>
								<td style="text-align: right;width:50%"><s:label
										cssStyle="font-weight:bold;" theme="simple"
										key="label.lg.positiveresistance"></s:label></td>
								<td style="width:50%"><s:textfield size="10" theme="simple"
										name="lstLineGroups[%{selectedLineGroupIndex}].positiveResistance"
										key="label.lg.positiveresistance"
										value="%{lstLineGroups[selectedLineGroupIndex].positiveResistance}" />
								</td>
							</tr>
							<tr>
								<td style="text-align: right;width:50%"><s:label
										cssStyle="font-weight:bold;" theme="simple"
										key="label.lg.positivereactance"></s:label></td>
								<td style="width:50%"><s:textfield size="10" theme="simple"
										name="lstLineGroups[%{selectedLineGroupIndex}].positiveReactance"
										key="label.lg.positivereactance"
										value="%{lstLineGroups[selectedLineGroupIndex].positiveReactance}" />
								</td>
							</tr>
							<tr>
								<td style="text-align: right;width:50%"><s:label
										cssStyle="font-weight:bold;" theme="simple"
										key="label.lg.zeroresistance"></s:label></td>
								<td style="width:50%"><s:textfield size="10" theme="simple"
										name="lstLineGroups[%{selectedLineGroupIndex}].zeroResistance"
										key="label.lg.zeroresistance"
										value="%{lstLineGroups[selectedLineGroupIndex].zeroResistance}" />
								</td>
							</tr>
							<tr>
								<td style="text-align: right;width:50%"><s:label
										cssStyle="font-weight:bold;" theme="simple"
										key="label.lg.zeroreactance"></s:label></td>
								<td style="width:50%"><s:textfield size="10" theme="simple"
										name="lstLineGroups[%{selectedLineGroupIndex}].zeroReactance"
										key="label.lg.zeroreactance"
										value="%{lstLineGroups[selectedLineGroupIndex].zeroReactance}" />
								</td>
							</tr>
							<tr>
								<td style="text-align: right;width:50%"><s:label
										cssStyle="font-weight:bold;" theme="simple"
										key="label.lg.linelength"></s:label></td>
								<td style="width:50%"><s:textfield size="10" theme="simple"
										name="lstLineGroups[%{selectedLineGroupIndex}].lineMiles"
										key="label.lg.linelength"
										value="%{lstLineGroups[selectedLineGroupIndex].lineMiles}" />
								</td>
							</tr>
						</table></td>
				</tr>
				<tr>
					<td style="font-size: 12px;" align="center">
					<fieldset style="text-align: center; width: 60%">
						<legend>Add or Remove Channels</legend>
						<s:optiontransferselect theme="simple"
							cssStyle="font-family:courier, courier new, serif;;"
							doubleCssStyle="font-family:courier, courier new, serif;"
							buttonCssClass="submit" doubleSize="12" size="12" headerKey="HEADERNOTRANSFER"
							headerValue="--- Selected Channels ---" doubleHeaderKey="HEADERNOTRANSFER"
							doubleHeaderValue="--- Available Channels -- "
							emptyOption="false" doubleEmptyOption="false"
							allowAddAllToLeft="false" allowAddAllToRight="false"
							allowSelectAll="false" allowUpDownOnLeft="false"
							allowUpDownOnRight="false"
							leftTitle="%{lstLineGroups[selectedLineGroupIndex].name}"
							rightTitle="Channels List" id="lineGroupDetails"
							name="lineGroupDetails"
							list="lstLineGroups[selectedLineGroupIndex].inputChannels" 
							listKey="displayChannel" listValue="displayName"
							doubleId="availableChannels" doubleName="availableChannels"
							doubleList="lstAvailableAnalogChannels"
							doubleListKey="displayChannel" doubleListValue="displayName"
							formName="newEditLineGroup"
							addToLeftOnclick="javascript:modify_linegroup()"
							addToRightOnclick="javascript:modify_linegroup()" />
					</fieldset>
					</td>
				</tr>
				<tr>
					<td style="font-family:courier, courier new, serif;font-size: 12px;" align="center">
						<fieldset style="text-align: center; width: 60%">
							<legend>Comments</legend>
							<s:textarea theme="simple" label="Comments" labelposition="top"  tooltip="Any user comments or notes for this LineGroup" 
								name="lstLineGroups[%{selectedLineGroupIndex}].comments" cols="97" rows="10" 
								cssStyle="overflow: scroll; resize: none;" value="%{lstLineGroups[selectedLineGroupIndex].comments}"/>
						</fieldset>
					</td>
				</tr>
			</s:else>

			<tr>
				<td colspan="2" align="center">

					<div id="divSaveMode">
						<s:submit theme="simple" cssClass="submit" id="btnSave"
							name="btnSave" value="Save" action="saveLineGroupDetails" />
						<s:submit theme="simple" cssClass="submit" id="btnCancel"
							name="btnCancel" value="Cancel" action="cancelEditLineGroup" />
					</div>
				</td>
			</tr>
		</table>
		<s:hidden id="sourceTab" name="sourceTab" value=""></s:hidden>
		<s:hidden id="booAnalog" name="booAnalog"></s:hidden>
		<s:hidden id="booDigital" name="booDigital"></s:hidden>
		<s:hidden id="lineGroupExists" name="lineGroupExists"></s:hidden>
		<s:hidden id="actionType" name="actionType"></s:hidden>
		<s:hidden id="jspFileName" name="NewEditLineGroups.jsp"></s:hidden>
	</s:form>
</body>
</html>
