<%@ include file="header.jsp"%>
<script  type="text/javascript">
	$("*").css("cursor", "progress");
	var isDfrAddedOrRemoved = ${stationDetails.dfrAddedOrRemoved};
</script>
<s:url var="url" action="triggerChannels" includeParams="none" />
<s:url var="currenturl" includeParams="get" escapeAmp="false" />
<s:url var="analogUrl" action="configureChannels" includeParams="none">
</s:url>
<script src="js/m9000/m9k-utils.js" type="text/javascript"></script>
<script src="js/m9000/configureEvents.js" type="text/javascript"></script>
<s:actionerror id="userMsgError" theme="jquery"/>
<s:form theme="simple" name="configureEventChannels"
	id="configureEventChannels" action="configureEventChannels"
	method="post">
	<div id="navMenu" style="display:none">
		<s:if test="%{accessMode == 'Edit'}">
			<s:if test="%{booAnalog}">
				<s:if
					test="%{lineGroupExists ||(lstLineGroups != null && lstLineGroups.size()>0)}">
					<div style="text-align:left;font-weight:bold;">
						<s:a id="stationDetails" cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('StationDetails');"> StationDetails > </s:a>
						<s:a id="Analogs" cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Analogs');"> Analogs > </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');">Virtuals > </s:a>
						<s:a id="LineGroups" cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');">LineGroups > </s:a>
						<s:a id="Measurements" cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');"> Measurements > </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> Virtual Measurements > </s:a>Events
						<s:a id="FaultLocation" cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('FaultLocation');"> > Fault Location</s:a>
					</div>
				</s:if>
				<s:else>
					<div style="text-align:left;font-weight:bold;">
						<s:a id="stationDetails" cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('StationDetails');"> StationDetails > </s:a>
						<s:a id="Analogs" cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Analogs');"> Analogs > </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');">Virtuals > </s:a>
						<s:a id="LineGroups" cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');">LineGroups > </s:a>
						<s:a id="Measurements" cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');"> Measurements > </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> Virtual Measurements > </s:a>Events 
						</div>
				</s:else>
				<s:else>
					<div style="text-align:left;font-weight:bold;">
						<s:a id="stationDetails" cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('StationDetails');"> StationDetails > </s:a>
						<s:a id="Analogs" cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Analogs');"> Analogs > </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');">Virtuals > </s:a>
						<s:a id="LineGroups" cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');">LineGroups > </s:a>
						<s:a id="Measurements" cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');"> Measurements > </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> Virtual Measurements > </s:a>Events 
						</div>
				</s:else>
			</s:if>
			<s:else>
				<div style="text-align:left;font-weight:bold;">
					<s:a id="stationDetails" cssClass="traverseMenu" href="#"
						onclick="javascript:submitForm('StationDetails');"> StationDetails > </s:a>Events 
				</div>
			</s:else>
		</s:if>
	</div>
	<div id="divButtons">
		<s:if test="%{lstLineGroups != null && lstLineGroups.size()>0}">
			<table id="tblButtons" style="width:100%">
				<tr id="tblButtonsTop" style="display: none">
					<td align="center"><s:submit theme="simple" align="left"
							cssClass="submit" name="btnSubmit" value="Back" action="configureEventChannelsBack" />
						<s:submit theme="simple" align="center" cssClass="submit"
							name="btnSubmit" value="Next" action="configureEventChannelsNext" /></td>
				</tr>
			</table>
		</s:if>
		<s:else>
			<table id="tblButtons" style="width:100%;">
				<tr id="tblButtonsTop" style="display: none">
					<td align="center"><s:submit theme="simple" align="left"
							cssClass="submit" name="btnSubmit" value="Back" action="configureEventChannelsBack"/>
							<s:submit theme="simple" align="center" cssClass="submit"
								name="btnSubmit" value="Finish"
								onclick="return confirm('Do you want to save the current changes?')"
								action="configureEventChannelsNext" />
							<s:submit theme="simple" align="center" cssClass="submit"
								name="btnSubmit" value="Finish And Send"
								onclick="return confirm('Do you want to save and send the current changes to the station master?')"
								action="configureEventChannelsSaveAndSend" />
					</td>
				</tr>
			</table>
		</s:else>
	</div>

	<table id="eventsTable" class="configureTable" style="width: 100%;align:center">
		<tr>
			<th colspan="9" class="titleLabel" style="background-color: #9999CC">Event
				Channels Configuration</th>
		</tr>
		<tr>
			<td colspan="9" width="100%" align="center"
				style="background-color: #BCBCDC"><s:checkbox
					name="enableAssist" theme="simple" id="enableAssist"
					label="Enable Assistance"
					tooltip="Enables assistance in entering or modifying data" value=""
					onclick="updateAssistance(this.checked);" cssStyle="display:none" />
				<s:label theme="simple" for="enableAssist" value="Enable Assistance"/></td>
		</tr>
		<tr style="background-color: #BCBCDC;">
			<th id="globalSelectDiv" style="width: 3%;display:none" align="center">Select All <s:checkbox
					name="globalSelect" id="globalSelect"
					style="background-color:lightblue;" theme="simple" value=""
					onclick="selectAllToEdit(this.checked);" />
			</th>
			<th style="width: 5%" align="center">Event#</th>
			<th style="width: 25%" align="center">Description</th>
			<th style="width: 12%" align="center">Normal State</th>
			<th style="width: 9%" align="center">DFR <s:checkbox
					name="dfrCheckUncheck" id="dfrCheckUncheck" theme="simple"
					onclick="changeDfrState(this.checked);" /></th>
			<th style="width: 10%" align="center">DFR Trigger</th>
			<th style="width: 5%" align="center">SER <s:checkbox
					name="serCheckUncheck" id="serCheckUncheck" theme="simple"
					onclick="changeSerState(this.checked);" /></th>
			<th style="width: 5%" align="center">PMU <s:checkbox
								name="pmuCheckUncheck" id="pmuCheckUncheck" theme="simple"
								onclick="updatePmuStatus(this.checked);" />
			<th style="width: 5%" align="center">Debounce <s:checkbox
								name="debounceCheckUncheck" id="debounceCheckUncheck" theme="simple"
								onclick="updateDebounceStatus(this.checked);" />
		</tr>
		<s:set var="chassisName" value="%{lstEventChannels[0].chassis}" />
		<div id="assistRowDiv1" style="display:none">
			<tr id="assistRowDiv" style="background-color: lightblue;display:noneposition:sticky;top:50px;z-index: 5;">
				<td align="center" style="background-color: lightblue"></td>
				<td align="center" style="background-color: lightblue"></td>
				<td style="space: nowrap" align="center" width="20%"><s:textfield
						cssStyle="width:100%;background-color:lightblue" theme="simple"
						id="globalName" size="65" value=""
						onkeyup="javascript:updateGlobal('description', this.value);" /></td>
				<td align="center" style="background-color: lightblue"><s:select
						theme="simple" name="globalNormalState" id="globalNormalState"
						list="#{'0':'Open','1':'Close'}"
						onchange="javascript:updateGlobal('normalState',this.value);"
						value="" /></td>
				<td align="center" style="background-color: lightblue"></td>
				<td align="center" style="background-color: lightblue"><s:select
						theme="simple" name="globalDfrStart" id="globalDfrStart"
						list="#session.lstDfrStart"
						onchange="javascript:updateGlobal('dfrStart',this.value);"
						value="" /></td>
				<td align="center" style="background-color: lightblue"></td>
				<td align="center" style="background-color: lightblue"></td>
				<td align="center" style="background-color: lightblue"></td>

			</tr>
		</div>
		<s:iterator var="iterateEvents" value="lstEventChannels"
			status="event_stat">
			<s:if test="%{#event_stat.index == 0}">
				<tr>
					<td colspan="9" width="100%">
						<fieldset style="text-align: center;">
							<legend>
								<s:property value="%{#chassisName}" />
							</legend>
							<div>
								<table id="<s:property value='%{#chassisName}'/>"
									class="configureTable" align="center"
									style="width: 100%; border-width: 0px;">
									</s:if>
									<s:elseif
										test="%{#chassisName != lstEventChannels[#event_stat.index].chassis}">
								</table>
							</div>
						</fieldset>
					</td>
				</tr>
				<tr>
					<td colspan="10" width="100%">
						<fieldset style="text-align: center;">
							<s:set var="chassisName"
								value="%{lstEventChannels[#event_stat.index].chassis}" />
							<legend>
								<s:property value="%{#chassisName}" />
							</legend>
							<div>
								<table id="<s:property value='%{#chassisName}'/>"
									class="configureTable" align="center"
									style="width: 100%; border-width: 0px;">
									</s:elseif>
									<tr id="tr_<s:property value='%{#analog_stat.index}'/>" style="background-color: #EEEEFF">
										<td id="td_globalSelect_<s:property value='%{#event_stat.index}'/>" align="center" style="background-color:lightblue;display:none;"><s:checkbox
												name="globalSelect_%{#event_stat.index}"
												id="globalSelect_%{#event_stat.index}"
												style="background-color:lightblue" theme="simple" value="" /></td>
										<td align="center" width="8%"><s:label
												name="lstEventChannels[%{#event_stat.index}].displayChannel"
												id="lstEventChannels[%{#event_stat.index}].displayChannel"
												theme="simple" value="%{displayChannel}" /></td>
										<s:hidden
											name="lstEventChannels[%{#event_stat.index}].channel"></s:hidden>
										<s:hidden
											name="lstEventChannels[%{#event_stat.index}].chassis"></s:hidden>
										<s:hidden name="lstEventChannels[%{#event_stat.index}].name"></s:hidden>
										<s:url var="descriptionUrl" action="updateEventDescriptions"
											includeParams="none">
											<s:param name="selectedIndex" value="%{#event_stat.index}"></s:param>
											<s:param name="mimetype" value="text/json-comment-filtered"></s:param>
										</s:url>
										<td align="center" width="31%"><s:fielderror>
												<s:param>lstEventChannels[<s:property
														value="%{#event_stat.index}" />].description</s:param>
											</s:fielderror> <s:textfield cssStyle="width:100%"
												name="lstEventChannels[%{#event_stat.index}].description"
												id="lstEventChannels[%{#event_stat.index}].description"
												required="true" size="64" maxlength="64" tooltip="%{description}" /></td>
										<td align="center" width="19%"><s:select theme="simple"
												name="lstEventChannels[%{#event_stat.index}].normalState"
												id="lstEventChannels[%{#event_stat.index}].normalState"
												list="#{'0':'Open','1':'Close'}" value="%{normalState}" /></td>
										<td align="center" width="9%"><s:checkbox
												name="lstEventChannels[%{#event_stat.index}].dfr"
												id="lstEventChannels[%{#event_stat.index}].dfr"
												theme="simple" value="%{dfr}"
												onchange="javascript:updateDfrStartStatus(this,this.checked);isAllDfrChecked();" /></td>
										<td align="center" width="15%"><s:select theme="simple"
												name="lstEventChannels[%{#event_stat.index}].dfrStart"
												id="lstEventChannels[%{#event_stat.index}].dfrStart"
												list="#session.lstDfrStart" value="%{dfrStart}"
												disabled="%{disableDfrStart || !lstEventChannels[#event_stat.index].dfr}" /></td>
										<td align="center" width="6%"><s:checkbox
												name="lstEventChannels[%{#event_stat.index}].ser"
												id="lstEventChannels[%{#event_stat.index}].ser"
												theme="simple" value="%{ser}"
												onclick="javascript:isAllSerChecked();" /></td>
										<td align="center" width="7%"><s:checkbox
												name="lstEventChannels[%{#event_stat.index}].pmu"
												id="lstEventChannels[%{#event_stat.index}].pmu"
												theme="simple" value="%{pmu}"
												onclick="javascript:checkAllPmu(this.checked);" /></td>
										<td align="center" width="7%"><s:checkbox
												name="lstEventChannels[%{#event_stat.index}].debounce"
												id="lstEventChannels[%{#event_stat.index}].debounce"
												theme="simple" value="%{debounce}"
												onclick="javascript:verifyAllDebounce(this.checked);" /></td>
												
									</tr>
									</s:iterator>
								</table>
							</div>
						</fieldset>
					</td>
				</tr>
	</table>
	<div id="divButtons">
		<s:if test="%{lstLineGroups != null && lstLineGroups.size()>0}">
			<table id="tblButtons" style="width:100%;">
				<tr id="tblButtonsBottom" style="display: none">
					<td align="center"><s:submit theme="simple" align="left"
							cssClass="submit" name="btnSubmit" value="Back" action="configureEventChannelsBack"/>
						<s:submit theme="simple" align="center" cssClass="submit"
							name="btnSubmit" value="Next" action="configureEventChannelsNext" /></td>
				</tr>
			</table>
		</s:if>
		<s:else>
			<table id="tblButtons" style="width:100%;">
				<tr id="tblButtonsBottom" style="display: none">
					<td align="center"><s:submit theme="simple" align="left"
							cssClass="submit" name="btnSubmit" value="Back" action="configureEventChannelsBack"/>
							<s:submit theme="simple" align="center" cssClass="submit"
								name="btnSubmit" value="Finish"
								onclick="return confirm('Do you want to save the current changes?')"
								action="configureEventChannelsNext" />
							<s:submit theme="simple" align="center" cssClass="submit"
								name="btnSubmit" value="Finish And Send"
								onclick="return confirm('Do you want to save and send the current changes to the station master?')"
								action="configureEventChannelsSaveAndSend" />
					</td>
				</tr>
			</table>
		</s:else>
	</div>
	<s:hidden id="sourceTab" name="sourceTab" value=""></s:hidden>
	<s:hidden id="booAnalog" name="booAnalog"></s:hidden>
	<s:hidden id="booDigital" name="booDigital"></s:hidden>
	<s:hidden id="lineGroupExists" name="lineGroupExists"></s:hidden>
	<s:hidden id="originTab" name="originTab" value="Events"></s:hidden>
	<s:hidden id="jspFileName" name="jspFileName" value="configureEventChannels.jsp"></s:hidden>
</s:form>
</body>
</html>
