<%@ include file="header.jsp"%>

<body>
	<script type="text/javascript"
		src="/M9000Master/struts/optiontransferselect.js"></script>
	<script type="text/javascript">
	$.subscribe("checkUserAccess", function(event, widget){
	console.log("USER-ROLE "+userRole);
		if (userRole != 'admin')
		{
			$("[id^='divEditMode'] :input").prop("disabled", true);
			$("#showDecisionLogic :input").prop("disabled", true);
			$("#aOpendialog").prop("disabled", true);
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
			$.publish("show_linegroup_decision_logic");
			$.publish("show_Dialog_text");
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
			var booAnalogValue = document.getElementById("booAnalog").value;
			var booDigitalValue = document.getElementById("booDigital").value;
			document.forms["manageLineGroups"].booAnalog.value=booAnalogValue;
			document.forms["manageLineGroups"].booDigital.value=booDigitalValue;
			
			document.getElementById("sourceTab").value=sourceValue;
			document.forms["manageLineGroups"].sourceTab.value=sourceValue;
			document.forms["manageLineGroups"].decisionLogic.value="true";
			document.forms["manageLineGroups"].submit();
		}
		
		function populateDecisionLogic(triggerValue)
		{
			var existDecisionLogic = document.getElementById("dialogText").value;
			if (existDecisionLogic != '')
				{
				existDecisionLogic=existDecisionLogic+" ";
				}
			document.getElementById("dialogText").value=existDecisionLogic+""+triggerValue;
		}
		  function okButton(){
			  document.forms["manageLineGroups"].txtDecisionLogic.value = document.getElementById("dialogText").value; 
		       $('#mybuttondialog').dialog('close');
		  };
		     function cancelButton(){
		    	 $.publish("show_Dialog_text");
		      $('#mybuttondialog').dialog('close');
		     };
	</script>
	<s:if test="%{accessMode == 'Edit'}">
		<s:if test="%{booDigital}">
			<div style="text-align:left;font-weight:bold;">
				<s:a cssClass="traverseMenu" href="#"
					onclick="javascript:submitForm('StationDetails');"> StationDetails > </s:a>
				<s:a cssClass="traverseMenu" href="#"
					onclick="javascript:submitForm('Analogs');">Analogs > </s:a>
				<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');">Virtuals > </s:a>
				<s:a cssClass="traverseMenu" href="#"
					onclick="javascript:submitForm('LineGroups');">LineGroups > </s:a>
				<s:a cssClass="traverseMenu" href="#"
					onclick="javascript:submitForm('Measurements');">Measurements > </s:a>
				<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> Virtual Measurements</s:a>
				<s:a cssClass="traverseMenu" href="#"
					onclick="javascript:submitForm('Events');">Events > </s:a>FaultLocation 
					</div>
		</s:if>
		<s:else>
			<div style="text-align:left;font-weight:bold;">
				<s:a cssClass="traverseMenu" href="#"
					onclick="javascript:submitForm('StationDetails');"> StationDetails > </s:a>
				<s:a cssClass="traverseMenu" href="#"
					onclick="javascript:submitForm('Analogs');">Analogs > </s:a>
				<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');">Virtual Channels > </s:a>
				<s:a cssClass="traverseMenu" href="#"
					onclick="javascript:submitForm('LineGroups');">LineGroups > </s:a>
				<s:a cssClass="traverseMenu" href="#"
					onclick="javascript:submitForm('Measurements');">Measurements > </s:a>
				<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> Virtual Measurements</s:a>FaultLocation
					</div>
		</s:else>
	</s:if>

	<s:form theme="simple" name="manageLineGroups"
		action="manageLineGroups" method="POST">
		<s:actionerror id="userMsgError" theme="jquery"/>
		<s:actionmessage  id="userMsgInfo" theme="jquery"/>

		<table class="lineGroupsTable" style="width:80%">
			<tr>
				<td colspan="2" align="center" class="titleLabel"><div
						style="text-align:center; font-weight: bold;">
						<s:label theme="simple" value=" Fault Location Decision Logic"></s:label>
					</div></td>
			</tr>

				<tr>
					<td colspan="2" align="center">

						<div id="divEditModeTop">
								<s:submit theme="simple" cssClass="submit" name="btnBackTop"
								value="Back" action="faultLocDecisionLogicBack" />
								<s:submit theme="simple" cssClass="submit" name="btnFinishTop"
									value="Finish"
									onclick="return confirm('Do you want to save the current changes?')"
									action="faultLocDecisionLogicSave" />
								<s:submit theme="simple" cssClass="submit" name="btnSendTop"
									value="Finish And Send"
									onclick="return confirm('Do you want to save and send the current changes to the station master?')"
									action="faultLocDecisionLogicSaveAndSend" />
						</div>
					</td>
				</tr>

			<s:if test="%{lstLineGroups != null && lstLineGroups.size()>0}">
				<tr>
					<td colspan="2" align="center">
						<fieldset>
							<div align="center">
								<s:label theme="simple"
									cssStyle="text-align: left; font-weight:bold;"
									value="LineGroup Name: "></s:label>
								&nbsp;
								<s:select cssStyle="width:50%" theme="simple"
									name="selectedLineGroup" id="selectedLineGroup"
									list="lstLineGroups" listKey="name" listValue="lineGroupName"
									label="Select a LineGroup" value="selectedLineGroup"
									onchange="javascript:show_linegroup(this);return false;">
								</s:select>
							</div>
						</fieldset>
					</td>
				</tr>
				<tr align="left">
					<td></td>
				</tr>
				<tr>
					<td align="center"><s:hidden
							name="lstLineGroups[%{selectedLineGroupIndex}].name"></s:hidden>
						<s:hidden name="lstLineGroups[%{selectedLineGroupIndex}].local"></s:hidden>
						<s:hidden name="lstLineGroups[%{selectedLineGroupIndex}].chassis"></s:hidden>
						<s:label cssStyle="font:bold" value="Decision Logic"></s:label> 
							<s:url var="decisionLogicUrl" action="displayDecisionLogic">
							</s:url>
							<sj:a id="aOpendialog" openDialog="mybuttondialog">
								<sj:div showLoadingText="true" preload="false"
									id="showDecisionLogic" href="%{#decisionLogicUrl}"
									listenTopics="show_linegroup_decision_logic" formIds="manageLineGroups"  onCompleteTopics="checkUserAccess">
									<s:textfield cssStyle="width:95%" formIds="manageLineGroups"
										id="txtDecisionLogic"
										name="lstLineGroups[%{selectedLineGroupIndex}].decisionLogic"
										value="%{lstLineGroups[selectedLineGroupIndex].decisionLogic}"
										label="Decision Logic"></s:textfield>
								</sj:div>
							</sj:a>
						<sj:dialog id="mybuttondialog" formIds="manageLineGroups"
							buttons="{ 
    		'OK':function() { okButton(); return false;}, 
    		'Cancel':function() { cancelButton(); return false;}
    		}"
							autoOpen="false" modal="true" title="Decision Logic" width="500">
							<table style="width:100%" class="decisionDialog">
								<tr>
									<td width="100%" align="center"><s:label
											cssStyle="font:bold;" value="Decision Logic"></s:label> <s:url
											var="dialogTextUrl" action="displayDialog">
										</s:url> <sj:div showLoadingText="true" preload="false"
											id="showDialogTextField" href="%{#dialogTextUrl}"
											listenTopics="show_Dialog_text" formIds="manageLineGroups">
											<s:textfield theme="simple" cssStyle="width:100%"
												id="dialogText" name="dialogText"
												value="%{lstLineGroups[selectedLineGroupIndex].decisionLogic}"
												label="Decision Logic">
											</s:textfield>
										</sj:div></td>
								</tr>
								<tr>

									<td colspan="2" align="center"><s:label
											cssStyle="font-family:courier, courier new, serif;font:bold" value="Select Triggers"></s:label> 
											<s:select cssStyle="font-family:courier, courier new, serif;border-style: solid solid solid solid"
											theme="simple" name="events" size="10"
											list="lstAllTriggerChannels" listKey="triggerKey"
											listValue="displayName" multiple="false"
											onclick="javascript:populateDecisionLogic(this.value);"></s:select>
										<s:submit theme="simple" cssClass="submit" name="btnSubmit"
											value="("
											onclick="javascript:populateDecisionLogic(this.value);return false;" />
										<s:submit theme="simple" cssClass="submit" name="btnSubmit"
											value=")"
											onclick="javascript:populateDecisionLogic(this.value);return false;" />
										<s:submit theme="simple" cssClass="submit" name="btnSubmit"
											value="AND"
											onclick="javascript:populateDecisionLogic(this.value);return false;" />
										<s:submit theme="simple" cssClass="submit" name="btnSubmit"
											value="OR"
											onclick="javascript:populateDecisionLogic(this.value);return false;" />
									</td>
								</tr>
							</table>
						</sj:dialog></td>
				</tr>
				<tr>
					<td align="center"><s:url var="lineUrl"
							action="displayLineGroupDetails">
						</s:url> <sj:div showLoadingText="true" preload="true" id="details"
							href="%{#lineUrl}" listenTopics="show_linegroup_details"
							formIds="manageLineGroups" errorNotifyTopics="/unselectDetails">
						</sj:div></td>
				</tr>
				<br>
				<tr>
					<td colspan="2" align="center">

						<div id="divEditMode">
								<s:submit theme="simple" cssClass="submit" name="btnBackBottom"
								value="Back" action="faultLocDecisionLogicBack" />
								<s:submit theme="simple" cssClass="submit" name="btnFinishBottom"
									value="Finish"
									onclick="return confirm('Do you want to save the current changes?')"
									action="faultLocDecisionLogicSave" />
								<s:submit theme="simple" cssClass="submit" name="btnSendBottom"
									value="Finish And Send"
									onclick="return confirm('Do you want to save and send the current changes to the station master?')"
									action="faultLocDecisionLogicSaveAndSend" />
						</div>
					</td>
				</tr>
			</s:if>
		</table>
		<s:hidden id="sourceTab" name="sourceTab" value=""></s:hidden>
		<s:hidden id="booAnalog" name="booAnalog"></s:hidden>
		<s:hidden id="booDigital" name="booDigital"></s:hidden>
		<s:hidden id="lineGroupExists" name="lineGroupExists"></s:hidden>
		<s:hidden id="decisionLogic" name="decisionLogic" value=""></s:hidden>
		<s:hidden id="jspFileName" name="decisionLogicFaultLoc.jsp"></s:hidden>
	</s:form>
</body>
</html>
