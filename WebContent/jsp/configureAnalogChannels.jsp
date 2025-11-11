<%@ include file="header.jsp"%>
<script  type="text/javascript">
	$("*").css("cursor", "progress");
	var isDfrAddedOrRemoved = ${stationDetails.dfrAddedOrRemoved};
</script>
<script src="js/m9000/m9k-utils.js" type="text/javascript"></script>
<script src="js/m9000/configureAnalogs.js" type="text/javascript"></script>
<s:actionerror id="userMsgError" theme="jquery"/>
<s:form theme="simple" name="configureAnalogChannels"
	id="configureAnalogChannels" action="configureAnalogChannels"
	method="POST">
	<div id="navMenu" style="display:none">
		<s:if test="%{accessMode == 'Edit'}">
			<s:if test="%{booDigital}">
				<s:if
					test="%{lineGroupExists ||(lstLineGroups != null && lstLineGroups.size()>0)}">
					<div style="text-align:left;font-weight:bold;">
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('StationDetails');"> StationDetails </s:a> > Analogs
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');"> > Virtuals </s:a>
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');"> > LineGroups </s:a>
						<s:a cssClass="traverseMenu" href="#"
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
							onclick="javascript:submitForm('StationDetails');"> StationDetails </s:a> > Analogs
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');"> > Virtuals </s:a>
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');"> > LineGroups </s:a>
						<s:a cssClass="traverseMenu" href="#"
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
							onclick="javascript:submitForm('StationDetails');"> StationDetails </s:a> > Analogs
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');"> > Virtuals </s:a>
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');">>LineGroups </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');">> Measurements</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> > Virtual Measurements</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('FaultLocation');"> > Fault Location</s:a>
					</div>
				</s:if>
				<s:else>
					<div style="text-align:left;font-weight:bold;">
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('StationDetails');"> StationDetails </s:a> > Analogs
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');"> > Virtuals </s:a>
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');">>LineGroups </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');">> Measurements</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> > Virtual Measurements</s:a>
					</div>
				</s:else>
			</s:else>
		</s:if>
	</div>
	<div>
		<table style="width:100%">
			<tr id ="tblButtonsTop" style="display:none">
			<td align="center"><s:submit theme="simple" align="left"
					cssClass="submit" name="btnSubmit" value="Back"
					action="analogChannelsBack" /> <s:submit theme="simple"
					align="center" cssClass="submit" name="btnSubmit" value="Next" action="analogChannelsNext" /> 
			</td>
			</tr>
		</table>
	</div>

	<table id="analogTable" class="configureTable" 
		style="width: 100%;align:center">
		<tr>
			<td colspan="10" width="100%" class="titleLabel"
				style="background-color: #9999CC">Analog Channels Configuration</td>
		</tr>
		<tr>
			<td colspan="10" width="100%" align="center"
				style="background-color: #BCBCDC"><s:checkbox
					name="enableAssist" theme="simple" id="enableAssist"
					label="Enable Assistance"
					tooltip="Enables assistance in entering or modifying data" value=""
					onclick="updateAssistance(this.checked);" cssStyle="display:none" />
				<s:label theme="simple" for="enableAssist" value="Enable Assistance"/></td>
		</tr>
		<tr style="background-color: #BCBCDC;">
			<th id="globalSelectDiv" align="center" style="display:none;">Select All <s:checkbox
					name="globalSelect" id="globalSelect"
					style="background-color:lightblue" theme="simple" value=""
					onclick="selectAllToEdit(this.checked);" />
			</th>
			<th style="width: 5%" align="center">Channels#</th>
			<th style="width: 34%" align="center">Description</th>
			<th style="width: 5%" align="center">Phase</th>
			<th style="width: 20%" align="center">Input Type</th>
			<th style="width: 8%" align="center">Primary</th>
			<th style="width: 8%" align="center">Secondary</th>
			<th style="width: 8%" align="center">Full Scale</th>
			<th style="width: 8%" align="center">External Shunt(Ohms)</th>
			<th style="width: 5%" align="center">Export <s:checkbox
					name="CheckUncheck" id="CheckUncheck" theme="simple"
					onclick="changeState(this.checked);" />
			</th>
		</tr>
		<s:set var="chassisName" value="%{lstAnalogChannels[0].chassis}" />
		<div id="assistRowDiv1" style="display:none;">
			<tr id="assistRowDiv" style="background-color: lightblue;display:none;position:sticky;top:50px;z-index: 5;">
				<td align="center" style="background-color: lightblue"></td>
				<td align="center" style="background-color: lightblue"></td>
				<td style="space: nowrap" align="center"><s:textfield
						cssStyle="width:100%;background-color:lightblue" theme="simple"
						id="globalName" value=""
						onkeyup="javascript:updateGlobal('circuitName', this.value);" /></td>
				<td align="center" style="background-color: lightblue"><s:select
						theme="simple" name="globalPhase" id="globalPhase"
						list="{'NA','A', 'B', 'C', 'N','AB','BC','CA'}"
						onchange="javascript:updateGlobal('phase',this.value);" value="" />
				</td>
				<td align="center" style="background-color: lightblue"><s:select
						theme="simple" name="globalInputType" id="globalInputType"
						list="#session.mapAnalogInputType" value=""
						onchange="javascript:updateGlobalInputType(this.value);" /></td>

				<td align="center" style="background-color: lightblue"><s:textfield
						style="background-color:lightblue" theme="simple"
						name="globalPrimaryRatio" id="globalPrimaryRatio" size="3"
						value=""
						onkeyup="javascript:updateGlobal('primaryRatio',this.value);" /></td>
				<td align="center" style="background-color: lightblue"><s:textfield
						style="background-color:lightblue" theme="simple"
						name="globalSecondaryRatio" id="globalSecondaryRatio" size="3"
						value=""
						onkeyup="javascript:updateGlobal('secondaryRatio',this.value);" /></td>
				<td align="center"><s:textfield
						style="background-color:lightblue" theme="simple"
						name="globalRange" id="globalRange" size="3" value=""
						onkeyup="javascript:updateGlobal('range',this.value);" /></td>
				<td align="center"><s:textfield
						style="background-color:lightblue" theme="simple"
						name="globalExtShunt" id="globalExtShunt" size="3" value=""
						onkeyup="javascript:updateGlobal('extShunt', this.value);" /></td>
				<td align="center" style="background-color: lightblue"></td>

			</tr>
		</div>
		<s:iterator value="lstAnalogChannels" status="analog_stat">		
			<s:if test="%{#analog_stat.index == 0}">
				<tr id="dataRow">
					<td colspan="10" width="100%">
						<fieldset style="text-align: center;">
							<legend>
								<s:property value="%{#chassisName}" />
							</legend>
							<div>
								<table id="table_<s:property value='%{#chassisName}'/>" class="configureTable" align="center"
									style="width: 100%; border-width: 0px;">
									</s:if>
									<s:elseif
										test="%{#chassisName != lstAnalogChannels[#analog_stat.index].chassis}">
								</table>
							</div>
						</fieldset>
					</td>
				</tr>
				<tr id="dataRow">
					<td colspan="10" width="100%">
						<fieldset style="text-align: center;">
							<s:set var="chassisName"
								value="%{lstAnalogChannels[#analog_stat.index].chassis}" />
							<legend>
								<s:property value="%{#chassisName}" />
							</legend>
							<div>
								<table id="table_<s:property value='%{#chassisName}'/>" class="configureTable" 
									style="width: 100%; border-width: 0px;align:center">
									</s:elseif>
									<tr id="tr_<s:property value='%{#analog_stat.index}'/>" style="background-color: #EEEEFF">
										<td id="td_globalSelect_<s:property value='%{#analog_stat.index}'/>" align="center" style="background-color:lightblue;display:none;"><s:checkbox
												name="globalSelect_%{#analog_stat.index}"
												id="globalSelect_%{#analog_stat.index}"
												style="background-color:lightblue;display:none;" theme="simple" value="" /></td>
										<td width="5%" align="center"><s:label
												name="lstAnalogChannels[%{#analog_stat.index}].displayChannel"
												id="lstAnalogChannels[%{#analog_stat.index}].displayChannel"
												theme="simple" value="%{displayChannel}" /></td>
										<s:hidden
											name="lstAnalogChannels[%{#analog_stat.index}].channel"></s:hidden>
										<s:hidden
											name="lstAnalogChannels[%{#analog_stat.index}].chassis"></s:hidden>
										<s:hidden name="lstAnalogChannels[%{#analog_stat.index}].name"></s:hidden>
										<!--								<td colspan="2" align="center"><s:textfield theme="simple" name="lstAnalogChannels[%{#analog_stat.index}].circuitName" id="circuitName" value="%{circuitName}"/> </td>-->
										<td width="34%" align="center"><s:fielderror>
												<s:param>lstAnalogChannels[<s:property
														value="%{#analog_stat.index}" />].circuitName</s:param>
											</s:fielderror> <s:textfield cssStyle="width:100%" id="lstAnalogChannels[%{#analog_stat.index}].circuitName"
												name="lstAnalogChannels[%{#analog_stat.index}].circuitName"
												required="true" size="64" maxlength="64" /></td>
										<td width="5%" align="center">
											<s:url var="urlPhaseTypes#{analog_stat.index}" action="populatePhaseTypesAction">
												<s:param name="selectedChannelIndex" value="%{#analog_stat.index}"/>
											</s:url>
											<sj:div indicator="indicator"
											errorText="Loading failed. Try refresh."
											id="phaseType%{#analog_stat.index}"
											href="%{urlPhaseTypes#{analog_stat.index}}"
											listenTopics="reloadPhaseTypes_%{#analog_stat.index}"
											deferredLoading="true" formIds="configureAnalogChannels" >
												<s:select theme="simple"
													name="lstAnalogChannels[%{#analog_stat.index}].phase"
													id="lstAnalogChannels[%{#analog_stat.index}].phase" 
													list="%{lstAnalogChannels[#analog_stat.index].mapPhaseTypes}"
													value="%{phase}"/>
											</sj:div>
										</td>
										<td width="21%" align="center">
										<s:select theme="simple"
												name="lstAnalogChannels[%{#analog_stat.index}].inputType"
												id="lstAnalogChannels[%{#analog_stat.index}].inputType"
												onchange="javascript:show_extshunt(this, this.value);show_tranducer(this, this.value, '%{#analog_stat.index}'); $(this).publish('reloadPhaseTypes_%{#analog_stat.index}',this, event);return false;"
												list="#session.mapAnalogInputType" value="%{inputType}" /></td>
										<td width="8%" align="center"><s:fielderror>
												<s:param>lstAnalogChannels[<s:property
														value="%{#analog_stat.index}" />].primaryRatio</s:param>
											</s:fielderror> <s:textfield theme="simple"
												name="lstAnalogChannels[%{#analog_stat.index}].primaryRatio"
												id="lstAnalogChannels[%{#analog_stat.index}].primaryRatio"
												required="true" size="3" value="%{primaryRatio}" readonly="%{transducer}" /></td>
										<td width="8%" align="center"><s:fielderror>
												<s:param>lstAnalogChannels[<s:property
														value="%{#analog_stat.index}" />].secondaryRatio</s:param>
											</s:fielderror> <s:textfield theme="simple"
												name="lstAnalogChannels[%{#analog_stat.index}].secondaryRatio"
												id="lstAnalogChannels[%{#analog_stat.index}].secondaryRatio"
												required="true" size="3" value="%{secondaryRatio}" readonly="%{transducer}" /></td>
										<td width="8%" align="center"><s:fielderror>
												<s:param>lstAnalogChannels[<s:property
														value="%{#analog_stat.index}" />].range</s:param>
											</s:fielderror> <s:textfield theme="simple"
												name="lstAnalogChannels[%{#analog_stat.index}].range"
												id="lstAnalogChannels[%{#analog_stat.index}].range"
												required="true" size="3" value="%{range}" /></td>
										<td width="8%" align="center"><s:fielderror>
												<s:param>lstAnalogChannels[<s:property
														value="%{#analog_stat.index}" />].extShunt</s:param>
											</s:fielderror> <s:textfield theme="simple"
												name="lstAnalogChannels[%{#analog_stat.index}].extShunt"
												id="lstAnalogChannels[%{#analog_stat.index}].extShunt"
												size="3" value="%{extShunt}" disabled="%{disableExtSunt}" /></td>
										<td width="7%" align="center"><s:checkbox
												name="lstAnalogChannels[%{#analog_stat.index}].exportStatus"
												id="lstAnalogChannels[%{#analog_stat.index}].exportStatus"
												theme="simple" value="%{exportStatus}"
												onclick="javascript:isAllChecked();" /></td>
										<s:hidden
											name="lstAnalogChannels[%{#analog_stat.index}].disableExtSunt"></s:hidden>
											<s:hidden id="transducer_%{#analog_stat.index}"
											name="lstAnalogChannels[%{#analog_stat.index}].transducer"></s:hidden>
											<s:hidden id="primaryRatio_backup_%{#analog_stat.index}"></s:hidden>
											<s:hidden id="secondaryRatio_backup_%{#analog_stat.index}"></s:hidden>
									</tr>
									<s:if test="%{transducer}">
									<tr id="tr_sub_<s:property value='%{#analog_stat.index}'/>"><td colspan='9' align='center'>
									<s:fielderror> 
										<s:param>lstAnalogChannels[<s:property value="%{#analog_stat.index}" />].outP1</s:param>
									</s:fielderror>
									<s:fielderror>
										<s:param>lstAnalogChannels[<s:property value="%{#analog_stat.index}" />].outP2</s:param>
									</s:fielderror>
									<s:fielderror> 
										<s:param>lstAnalogChannels[<s:property value="%{#analog_stat.index}" />].inP1</s:param>
									</s:fielderror>
									<s:fielderror>
										<s:param>lstAnalogChannels[<s:property value="%{#analog_stat.index}" />].inP2</s:param>
									</s:fielderror>
									<s:fielderror>
										<s:param>lstAnalogChannels[<s:property value="%{#analog_stat.index}" />].transducerUnits</s:param>
									</s:fielderror>
									<table class='configureSubrow'>
												<caption><b>Transducer Properties for channel A<s:property value='%{#analog_stat.index + 1}'/></b></caption>
												<colgroup span="2"></colgroup>
												<colgroup span="2"></colgroup>
												<tr><th colspan="2" scope="colgroup">Input Range</th><th colspan="2" scope="colgroup">Output Range</th><th rowspan="3" class="tooltip">Units<span class="tooltiptext">Transducer Units like Hz, Watts, Torque etc.</span></th><tr>
												<tr>
												    <th scope="col">In1</th>
												    <th scope="col">In2</th>
												    <th scope="col">Out1</th>
												    <th scope="col"  class="tooltip">Out2<span class="tooltiptext">This updates the Full Scale/Range field above as well</span></th>
  												</tr>
												<tr><td align="center"><s:textfield 
												name="lstAnalogChannels[%{#analog_stat.index}].outP1"
												id="lstAnalogChannels[%{#analog_stat.index}].outP1"
												 size="5" value="%{outP1}" />
												 </td><td align="center" ><s:textfield 
												name="lstAnalogChannels[%{#analog_stat.index}].outP2"
												id="lstAnalogChannels[%{#analog_stat.index}].outP2"
												 size="5" value="%{outP2}" /></td><td align="center"><table><tr><td><s:textfield 
												name="lstAnalogChannels[%{#analog_stat.index}].inP1"
												id="lstAnalogChannels[%{#analog_stat.index}].inP1"
												 size="5" value="%{inP1}" /></td><td>
												 <div style="float:right;text-align:left;font-size:12" id="transducerType_<s:property value='%{#analog_stat.index}'/>">
												<s:if test="%{inputType == 'TransducerVoltage'}">
												 	<b>V</b>
												 </s:if>
												 <s:else>
												 	<b>A</b>
												 </s:else>												 
												 </div></td></tr></table>
												 </td><td align="center"><table><tr><td><s:textfield 
												name="lstAnalogChannels[%{#analog_stat.index}].inP2"
												id="lstAnalogChannels[%{#analog_stat.index}].inP2"
												 size="5" value="%{inP2}" /></td><td>
												 <div style="float:right;text-align:left;font-size:12" id="transducerType_<s:property value='%{#analog_stat.index}'/>">
												<s:if test="%{inputType == 'TransducerVoltage'}">
												 	<b>V</b>
												 </s:if>
												 <s:else>
												 	<b>A</b>
												 </s:else>												 
												 </div></td></tr></table>
												 </td><td align="center"><s:textfield 
												name="lstAnalogChannels[%{#analog_stat.index}].transducerUnits"
												id="lstAnalogChannels[%{#analog_stat.index}].transducerUnits"
												 size="8" maxlength="8" value="%{transducerUnits}" /></td></tr></table></td></tr>
									</s:if>
									<s:else>
									<tr id="tr_sub_<s:property value='%{#analog_stat.index}'/>" style="display:none"><td colspan='9' align='center'><table class='configureSubrow'>
												<caption><b>Transducer Properties for channel A<s:property value='%{#analog_stat.index + 1}'/></b></caption>
												<colgroup span="2"></colgroup>
												<colgroup span="2"></colgroup>
												<tr><th colspan="2" scope="colgroup">Input Range</th><th colspan="2" scope="colgroup">Output Range</th><th rowspan="3" class="tooltip">Units<span class="tooltiptext">Transducer Units like Hz, Watts, Torque etc.</span></th><tr>
												<tr>
												    <th scope="col">In1</th>
												    <th scope="col">In2</th>
												    <th scope="col">Out1</th>
												    <th scope="col"  class="tooltip">Out2<span class="tooltiptext">This updates the Full Scale/Range field above as well</span></th>
  												</tr>
												<tr><td align="center"><s:textfield 
												name="lstAnalogChannels[%{#analog_stat.index}].outP1"
												id="lstAnalogChannels[%{#analog_stat.index}].outP1"
												 size="5" value="" /></td><td align="center" ><s:fielderror>
												<s:param>lstAnalogChannels[<s:property
														value="%{#analog_stat.index}" />].outP2</s:param>
											</s:fielderror><s:textfield 
												name="lstAnalogChannels[%{#analog_stat.index}].outP2"
												id="lstAnalogChannels[%{#analog_stat.index}].outP2"
												 size="5" value="" /></td><td align="center"><table><tr><td><s:textfield 
												name="lstAnalogChannels[%{#analog_stat.index}].inP1"
												id="lstAnalogChannels[%{#analog_stat.index}].inP1"
												 size="5" value="" /></td><td>
												 <div style="float:right;text-align:left;font-size:12" id="transducerType_<s:property value='%{#analog_stat.index}'/>">
												 </div></td></tr></table>
												 </td><td align="center"><table><tr><td><s:textfield 
												name="lstAnalogChannels[%{#analog_stat.index}].inP2"
												id="lstAnalogChannels[%{#analog_stat.index}].inP2"
												 size="5" value="" /></td><td>
												 <div style="float:right;text-align:left;font-size:12" id="transducerType_<s:property value='%{#analog_stat.index}'/>_<s:property value='%{#analog_stat.index}'/>">
												 </div></td></tr></table>
												 </td><td align="center"><s:textfield 
												name="lstAnalogChannels[%{#analog_stat.index}].transducerUnits"
												id="lstAnalogChannels[%{#analog_stat.index}].transducerUnits"
												 size="8" maxlength="8" value="%{transducerUnits}" /></td></tr></table></td></tr>
									</s:else>
									
									</s:iterator>
								</table>
							</div>
						</fieldset>
					</td>
				</tr>
	</table>
	<div>
		<table style="width:100%;">
			<tr id="tblButtonsBottom" style="display: none">
				<td align="center"><s:submit theme="simple" align="left"
						cssClass="submit" name="btnSubmit" value="Back"
						action="analogChannelsBack" /> <s:submit theme="simple"
						align="center" cssClass="submit" name="btnSubmit" value="Next" action="analogChannelsNext"
						 />
				</td>
			</tr>
		</table>
	</div>
	<!--
		
		<display:table name="lstAnalogChannels" >
			<display:column title=" Channel# " property="channel"/>
			<display:column title="ID" property="name" />
			<display:column title="Description" property="circuitName" />
			<display:column title="Phase" property="phase" />
			<display:column title="I/P Type" property="inputType" />
			<display:column title="PrimaryRatio" property="primaryRatio" />
			<display:column title="SecondaryRatio" property="secondaryRatio" />
			<display:column title="Full Scale" property="range" />
		</display:table>
		
	 -->
	<s:hidden id="sourceTab" name="sourceTab" value=""></s:hidden>
	<s:hidden id="booAnalog" name="booAnalog"></s:hidden>
	<s:hidden id="booDigital" name="booDigital"></s:hidden>
	<s:hidden id="lineGroupExists" name="lineGroupExists"></s:hidden>
	<s:hidden id="originTab" name="originTab" value="Analogs"></s:hidden>
	<s:hidden id="jspFileName" name="jspFileName" value="configureAnalogChannels.jsp"></s:hidden>
</s:form>
</body>
</html>
