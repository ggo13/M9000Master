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
				document.getElementById("editSelectedLineGroup").value=value;
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
			document.forms["manageLineGroups"].decisionLogic.value="false";
			document.forms["manageLineGroups"].submit();
		}
		
	</script>
	<s:form theme="simple" name="manageLineGroups"
		action="manageLineGroups" method="POST">
		<s:if test="%{accessMode == 'Edit'}">
			<s:if test="#session.isDigital == true">
				<div style="text-align:left;font-weight:bold;">
					<s:a href="#" onclick="javascript:submitForm('Analogs');">Analogs </s:a> >LineGroups > <s:a
						href="#" onclick="javascript:submitForm('Measurements');">Measurements</s:a> > <s:a
						href="#" onclick="javascript:submitForm('Events');">Events</s:a>
				</div>
			</s:if>
			<s:else>
				<div style="text-align:left;font-weight:bold;">
					<s:a href="#" onclick="javascript:submitForm('Analogs');">Analogs </s:a> >LineGroups > <s:a
						href="#" onclick="javascript:submitForm('Measurements');">Measurements</s:a>
				</div>
			</s:else>
		</s:if>
		<table class="lineGroupsTable" style="width:50%">
			<tr>
				<td colspan="2" align="center" class="titleLabel"><div
						style="text-align:center; font-weight: bold;">
						<s:label theme="simple" value="Manage Line Groups"></s:label>
					</div></td>
			</tr>

			<s:url var="lineUrl" action="showLineGroupDetails">
			</s:url>

			<s:if test="%{lstLineGroups != null && lstLineGroups.size()>0}">
				<tr>
					<td colspan="2" align="center"><div id="divNewLineGroup"
							style="visibility: hidden">
							<s:label cssStyle="font-weight: bold;" theme="simple"
								value="Enter a New LineGroup name "></s:label>
						&nbsp;
						<s:textfield theme="simple" name="newLineGroupName"
								id="newLineGroupName" value="%{newLineGroupName}">
							</s:textfield>
						</div> <div id="divEditLineGroup" style="visibility: hidden">
							<s:label cssStyle="font-weight: bold;" theme="simple"
								value="Edit Line Group Name "></s:label>
						&nbsp;
						<s:textfield theme="simple" name="editSelectedLineGroup"
								id="editSelectedLineGroup" />
						</div> <s:url var="updateLineGroupLstUrl" action="manageLineGroupsUpdateList">
						</s:url> <sj:div id="divExistingLineGroup"
							href="%{#updateLineGroupLstUrl}" 
							listenTopics="update_linegroup_list" formIds="manageLineGroups">
						</sj:div></td>
				</tr>
				<tr align="left">
					<td></td>
				</tr>

				<tr>
					<td>
						<div id="divFaultDetails"
							style="text-align: center; disabled: true;">
							<s:label theme="simple" cssStyle="font-weight:bold;"
								value="Fault Location Details"></s:label>
							<table class="inlineTable" title="Fault Location Details">
								<tr>
									<td style="text-align: right;"><s:label
											cssStyle="font-weight:bold; text-align:right;" theme="simple"
											key="label.lg.autocalculate"></s:label></td>
									<td><s:select theme="simple" name="autoCalculate"
											key="label.lg.autocalculate" list="{'No','Yes'}"
											value="%{autoCalculate}" /></td>
								</tr>
								<tr>
									<td style="text-align: right;"><s:label
											cssStyle="font-weight:bold;" theme="simple"
											key="label.lg.positiveresistance"></s:label></td>
									<td><s:textfield size="3" theme="simple"
											name="positiveResistance" key="label.lg.positiveresistance"
											readonly="true" value="%{positiveResistance}" /></td>
								</tr>
								<tr>
									<td style="text-align: right;"><s:label
											cssStyle="font-weight:bold;" theme="simple"
											key="label.lg.positivereactance"></s:label></td>
									<td><s:textfield size="3" theme="simple"
											name="positiveReactance" key="label.lg.positivereactance"
											readonly="true" value="%{positiveReactance}" /></td>
								</tr>
								<tr>
									<td style="text-align: right;"><s:label
											cssStyle="font-weight:bold;" theme="simple"
											key="label.lg.zeroresistance"></s:label></td>
									<td><s:textfield size="3" theme="simple"
											name="zeroResistance" key="label.lg.zeroresistance"
											readonly="true" value="%{zeroResistance}" /></td>
								</tr>
								<tr>
									<td style="text-align: right;"><s:label
											cssStyle="font-weight:bold;" theme="simple"
											key="label.lg.zeroreactance"></s:label></td>
									<td><s:textfield size="3" theme="simple"
											name="zeroReactance" key="label.lg.zeroreactance"
											readonly="true" value="%{zeroReactance}" /></td>
								</tr>
								<tr>
									<td style="text-align: right;"><s:label
											cssStyle="font-weight:bold;" theme="simple"
											key="label.lg.linelength"></s:label></td>
									<td><s:textfield size="3" theme="simple" name="lineLength"
											key="label.lg.linelength" readonly="true"
											value="%{lineLength}" /></td>
								</tr>
							</table>
						</div>
					</td>
					<td><sj:div showLoadingText="true" preload="true" id="details"
							href="%{#lineUrl}" onBeforeTopics="/beforeShow"
							listenTopics="show_linegroup_details" formIds="manageLineGroups"
							onErrorTopics="/unselectDetails">
						</sj:div> </td>
				</tr>
				<tr>
					<td colspan="2" align="center">

						<div id="divEditMode">
							<s:submit theme="simple" cssClass="submit" name="btnSubmit"
								value="Back" action="lineGroupsBack" />
							<s:if test="%{#session.userDetails.role != 'guest'}">
								<s:url var="editUrl" action="editLineGroup">
								</s:url>
								<sj:submit theme="simple" id="editBind" href="%{#editUrl}" cssClass="submit" 
									onBeforeTopics="/beforeEdit" formIds="manageLineGroups"
									onSuccessTopics="/unselectDetails" />
								<s:url var="newUrl" action="createLineGroup">
								</s:url>
								<sj:submit theme="simple" id="newBind" href="%{#newUrl}" cssClass="submit" 
									onBeforeTopics="/beforeNew"  formIds="manageLineGroups"
									onSuccessTopics="/afterNew" />
							</s:if>
							<s:else>
								<s:submit theme="simple" cssClass="submit" id="btnEdit"
									name="btnEdit" value="Edit" disabled="true" />
								<s:submit theme="simple" cssClass="submit" id="btnNew"
									name="btnNew" value="Add Line Group" disabled="true" />
							</s:else>
							<s:submit theme="simple" cssClass="submit" name="btnSubmit"
								value="Next" action="lineGroupsNext" />
						</div>
						<div id="divSaveMode">
							<s:url var="cancelUrl" action="manageLineGroups"
								method="cancelEdit">
							</s:url>
							<sj:submit theme="simple" id="cancel" href="%{#cancelUrl}" cssClass="submit" 
								onBeforeTopics="/beforeCancel" 
								onSuccessTopics="/unselectDetails" formIds="manageLineGroups"/>

							<s:submit theme="simple" cssClass="submit" id="btnSave"
								name="btnSave" value="Save" />
							<s:url var="saveUrl" action="showLineGroupDetails"
								method="saveLineGroupDetails">
							</s:url>
							<sj:submit theme="simple" id="save" href="%{#saveUrl}"
								onBeforeTopics="/beforeSave" 
								onSuccessTopics="/afterSave" formIds="manageLineGroups" />
						</div>
					</td>
				</tr>
			</s:if>
			<s:else>
				<tr>
					<td align="center"><s:label theme="simple"
							value="There are no existing line groups"></s:label></td>
				</tr>
				<tr>
					<td align="center">
						<div id="divEditMode">
							<s:submit theme="simple" cssClass="submit" name="btnSubmit"
								value="Back" method="back" />
							<s:if test="%{#session.userDetails.role != 'guest'}">
								<s:submit theme="simple" cssClass="submit" id="btnNew"
									name="btnNew" value="Add Line Group" />
								<s:url var="newUrl" action="showLineGroupDetails"
									method="newLineGroup">
								</s:url>
								<sj:submit id="newBind" href="%{#newUrl}"
									onBeforeTopics="/beforeNew" formIds="manageLineGroups"
									onSuccessTopics="/afterNew" />
							</s:if>
							<s:else>
								<s:submit theme="simple" cssClass="submit" id="btnNew"
									name="btnNew" value="Add Line Group" disabled="true" />
							</s:else>
							<s:submit theme="simple" cssClass="submit" name="btnSubmit"
								value="Next" method="configureTriggers" />
						</div>
					</td>
				</tr>
			</s:else>
		</table>
		<s:hidden id="sourceTab" name="sourceTab" value=""></s:hidden>
		<s:hidden id="jspFileName" name="manageLineGroups.jsp"></s:hidden>
	</s:form>
</body>
</html>