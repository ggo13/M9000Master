<%@ include file="header.jsp"%>

<body>
<script>
	var isDfrAddedOrRemoved = ${stationDetails.dfrAddedOrRemoved};
</script>
	<script type="text/javascript"
		src="/M9000Master/struts/optiontransferselect.js"></script>
<script type="text/javascript">
var mixedVoltageChannels="false";
</script>
	<script src="js/m9000/displayLineGroups.js" type="text/javascript">
	</script>
	<s:form theme="simple" name="manageLineGroups"
		action="manageLineGroups" method="POST">
		<div id="navMenu" style="display:none">
		<s:if test="%{accessMode == 'Edit'}">
			<s:if test="%{booDigital}">
				<s:if
					test="%{lineGroupExists ||(lstLineGroups != null && lstLineGroups.size()>0)}">
					<div style="text-align:left;font-weight:bold;">
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('StationDetails');"> StationDetails > </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Analogs');">Analogs > </s:a>
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');">Virtuals > </s:a>LineGroups<s:a
							cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');"> > Measurements</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> > Virtual Measurements</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Events');"> > Events</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('FaultLocation');"> > Fault Location</s:a>
					</div>
				</s:if>
				<s:else>
					<div style="text-align:left;font-weight:bold;">
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('StationDetails');"> StationDetails > </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Analogs');">Analogs > </s:a>
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');">Virtuals > </s:a>LineGroups<s:a
							cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');"> > Measurements</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> > Virtual Measurements</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Events');"> > Events</s:a>
					</div>
				</s:else>
			</s:if>
			<s:else>
				<s:if
					test="%{lineGroupExists ||(lstLineGroups != null && lstLineGroups.size()>0)}">
					<div style="text-align:left;font-weight:bold;">
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('StationDetails');"> StationDetails > </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Analogs');">Analogs > </s:a>
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');">Virtuals > </s:a>LineGroups<s:a
							cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');"> > Measurements</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> > Virtual Measurements</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('FaultLocation');"> > Fault Location</s:a>
					</div>
				</s:if>
				<s:else>
					<div style="text-align:left;font-weight:bold;">
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('StationDetails');"> StationDetails > </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Analogs');">Analogs > </s:a>
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');">Virtuals > </s:a>LineGroups<s:a
							cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');"> > Measurements</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> > Virtual Measurements</s:a>
					</div>
				</s:else>
			</s:else>
		</s:if>
		</div>
		<s:actionerror id="userMsgError" theme="jquery"/>
		<s:actionmessage  id="userMsgInfo" theme="jquery"/>
			<s:if test="%{lstAffectedLineGroups != null && lstAffectedLineGroups.size()>0}">
				<div style="color:red" class="result ui-widget-content ui-corner-all">
					One or more linegroups might be affected either by change in phase of a channel or deleting the associated virtual channel. 
					<s:iterator value="lstAffectedLineGroups" status="lg_stat">
						<p><s:property value="%{lstAffectedLineGroups[#lg_stat.index]}"/><p>
					</s:iterator>
				</div>
			
			</s:if>
		<table class="lineGroupsTable" style="width:80%">
			<tr>
				<td colspan="2" align="center" class="titleLabel"><div
						style="text-align:center; font-weight: bold;">
						<s:label theme="simple" value="View Line Groups"></s:label>
					</div></td>
			</tr>


			<s:if test="%{lstLineGroups != null && lstLineGroups.size()>0}">
				<tr>
					<td colspan="2" align="center">

						<div id="divEditMode">
							<s:submit theme="simple" cssClass="submit" name="btnBack_Bottom"
								value="Back" action="lineGroupsBack" />
							<s:submit theme="simple" cssClass="submit" name="btnNext_Bottom"
								value="Next" action="lineGroupsNext" />
						</div>
					</td>
				</tr>
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
									list="lstLineGroups" listKey="name" listValue="displayName"
									label="Select a LineGroup" value="selectedLineGroup"
									onchange="javascript:show_linegroup(this);return false;">
								</s:select>
							</div>
						</fieldset>
					</td>
				</tr>
				<tr align="center">
					<td colspan="2">
						<fieldset style="width: 30%">
								<s:submit theme="simple" cssClass="submit" id="btnNew"
									name="btnNew" value="New" action="createLineGroup" title="Create a new Line Group"
									/>
								<s:submit theme="simple" cssClass="submit" id="btnEdit" title="Edit current Group"
									name="btnEdit" value="Edit" action="editLineGroup"
									 />
								<s:submit theme="simple" cssClass="submit" id="btnDelete" title="Delete the current Line Group"
									name="btnDelete" onclick="return confirm('WARNING: Deleting this linegroup will remove all the measurements associated with it. Are you sure you want to delete?')" value="Delete"
									action="deleteLineGroup"  />
						</fieldset>
					</td>
				</tr>
				<tr align="left">
					<td></td>
				</tr>
				<tr>
					<td align="center"><s:url var="lineUrl"
							action="displayLineGroupDetails">
						</s:url> 
						<sj:div showLoadingText="true" preload="true" id="details"
							href="%{#lineUrl}" listenTopics="show_linegroup_details"
							formIds="manageLineGroups" errorNotifyTopics="/unselectDetails">
						</sj:div></td>
				</tr>
				<tr>
					<td colspan="2" align="center">

						<div id="divEditMode">
							<s:submit theme="simple" cssClass="submit" name="btnBack_Bottom"
								value="Back" action="lineGroupsBack" />
							<s:submit theme="simple" cssClass="submit" name="btnNext_Bottom"
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
									name="btnNew" value="Create" action="createLineGroup"
									 />
							<s:submit theme="simple" cssClass="submit" name="btnSubmit"
								value="Next" action="lineGroupsNext" />
						</div>
					</td>
				</tr>
			</s:else>
		</table>
		<s:hidden id="sourceTab" name="sourceTab" value=""></s:hidden>
		<s:hidden id="decisionLogic" name="decisionLogic" value=""></s:hidden>
		<s:hidden id="booAnalog" name="booAnalog"></s:hidden>
		<s:hidden id="booDigital" name="booDigital"></s:hidden>
		<s:hidden id="lineGroupExists" name="lineGroupExists"></s:hidden>
		<s:hidden id="originTab" name="originTab" value="LineGroups"></s:hidden>
		<s:hidden id="jspFileName" name="jspFileName" value="displayLineGroups.jsp"></s:hidden>
	</s:form>
</body>
</html>
