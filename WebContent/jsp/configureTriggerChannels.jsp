<%@ include file="header.jsp"%>
<script type="text/javascript" src="js/jquery.validate.js"></script>
<script type="text/javascript" src="js/additional-methods.js"></script>
<script type="text/javascript" src="js/jquery-migrate-1.2.1.min.js"></script>

<%@ page import="com.usi.m9000.dto.StationDTO"%>
	<%
	String pmuDataRate=""+((StationDTO)session.getAttribute("stationDetails")).getPmuDataRate();
	String chatterLimit = ""+((StationDTO)session.getAttribute("stationDetails")).getChatterLimit();
	String chatterRate = ""+((StationDTO)session.getAttribute("stationDetails")).getChatterRate();
	String triggerLimit = ""+((StationDTO)session.getAttribute("stationDetails")).getTriggerLimit();
	Boolean newAdded = false;
	if (request.getAttribute("newAdded") != null)
	{
		newAdded = (Boolean)request.getAttribute("newAdded");
	}
	int addedIndex = 0;
	if (request.getAttribute("addedIndex") != null)
	{
		addedIndex = (Integer)request.getAttribute("addedIndex");
	}
	%>

<script type="text/javascript">
	var pmuDataRate = <%=pmuDataRate%>;
	var chatterLimit =  <%=chatterLimit%>;
	var chatterRate =  <%=chatterRate%>
	var triggerLimit =  <%=triggerLimit%>
	var newAdded = <%=newAdded%>
	var addedIndex = <%=addedIndex%>
	var isDfrAddedOrRemoved = ${stationDetails.dfrAddedOrRemoved};
	var totalMeasurementsCount=${lstTriggerChannels.size()};
	var lstSrcChannels = [
        <s:iterator value="lstTriggerSourceDto" status="status">
            '<s:property value="sourceValue" escapeJavaScript="true" />'<s:if test="!#status.last">,</s:if>
        </s:iterator>
    ];

	$("*").css("cursor", "progress");
</script>
<script src="js/m9000/m9k-utils.js" type="text/javascript"></script>
<script src="js/m9000/configureTriggers.js" type="text/javascript">

</script>
<style>
        /* Hide the dialog content before initialization */
        #newMeasurementsDialog{
            display: none;
        }
        a {

    		text-decoration: none;


		}	
</style>
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
							onclick="javascript:submitForm('VirtualChannels');">Virtuals</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');"> > LineGroups > </s:a>Measurements
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
							onclick="javascript:submitForm('VirtualChannels');">Virtuals</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');"> > LineGroups > </s:a>Measurements
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
							onclick="javascript:submitForm('VirtualChannels');">Virtuals</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');"> > LineGroups > </s:a>Measurements
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
							onclick="javascript:submitForm('VirtualChannels');">Virtuals</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');"> > LineGroups > </s:a>Measurements 
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> > Virtual Measurements</s:a>
					</div>
				</s:else>
			</s:else>

		</s:if>
	</div>
	<s:actionerror id="userMsgError" theme="jquery"/>
	<s:actionmessage  id="userMsgInfo" theme="jquery"/>
	<s:form id="triggerChannels" name="triggerChannels" action="triggerChannels" method="POST" cssStyle="width: 100%;align:center">
		<tbody>

			<tr style="width: 100%;align:center">
				<td colspan="10" class="titleLabel" width="100%" style="background-color: #9999CC">Measurements
					Configuration</td>
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
			<s:if
				test="%{lstTriggerChannels != null && lstTriggerChannels.size() > 0}">
				<s:if test="%{lstAffectedLineGroups != null && lstAffectedLineGroups.size()>0}">
					<div  style="color:red" class="result ui-widget-content ui-corner-all">

					Please verify measurements associated to the below linegroup(s)  
					<s:iterator value="lstAffectedLineGroups" status="lg_stat">
						<p><s:property value="%{lstAffectedLineGroups[#lg_stat.index]}"/></p>
					</s:iterator>
					</div>
				</s:if>
				<s:if test="%{lstAssociatedMeasurements != null && lstAssociatedMeasurements.size()>0}">
					<div  style="color:red" class="result ui-widget-content ui-corner-all">

					Following measurements are impacted due to configuration change
					<s:iterator value="lstAssociatedMeasurements" status="lg_stat">
						<p><s:property value="%{lstAssociatedMeasurements[#lg_stat.index].displayName}"/></p>
					</s:iterator>
					</div>
				</s:if>
			</s:if>

			
			<div id="divresult" style="display:none"/>
				<table style="width:100%">
					<tbody>
						<tr id="buttonsTop" style="display: none">
							<td align="center"><s:submit theme="simple" align="left"
									cssClass="submit" name="btnSubmit" value="Back" action="triggerChannelsBack"
									 /> 
									 <sj:a openDialog="newMeasurementsDialog">
										 <s:submit theme="simple" align="center" cssClass="submit"
											id="btnAddMeasurementTop" name="btnAddMeasurementTop"
											value="Add Measurement"/>
									 </sj:a>
									<s:submit theme="simple" align="center" cssClass="submit"
										name="btnSubmit" value="Next" action="triggerChannelsNext"
										 />
							</td>
						</tr>
					</tbody>
				</table>
			<table id="measurementTable" class="configureTable" 
				style="width: 100%;align:center">
				<thead>
					<tr style="background-color: #BCBCDC;">
						<th id="globalSelectDiv" style="width: 3%;display:none;" align="center">Select
							All <s:checkbox name="globalSelect" id="globalSelect"
								style="background-color:lightblue;" theme="simple" value=""
								onclick="selectAllToEdit(this.checked);" />
						</th>
						<th style="width: 3%" align="center">Active <s:checkbox
								name="activeCheckUncheck" id="activeCheckUncheck" theme="simple"
								onchange="updateActiveStatus(this.checked);" />
						</th>
						<s:if test="%{accessMode == 'Edit'}">
							<th style="width: 3%" align="center">Id</th>
						</s:if>
						<th style="width: 3%" align="center">Input Channel</th>
						<th style="width: 10%" style="lightblue-space: nowrap;"
							align="center" class="tooltip">Name</th>
						<th style="width: 3%" align="center">Type</th>
						<th style="width: 3%" align="center">Average</th>
						<th style="width: 3%" align="center">Harmonic</th>
						<th style="width: 1%" align="center">PMU <s:checkbox
								name="pmuCheckUncheck" id="pmuCheckUncheck" theme="simple"
								onchange="updatePmuStatus(this.checked);" />
						</th>
						<th style="width: 1%" align="center">Freq</th>
						<th style="width: 3%" align="center">Trigger</th>
						<th style="width: 3%" align="center">Trigger If Over</th>
						<th style="width: 3%" align="center">Trigger If Under</th>
						<th style="width: 3%" align="center" class="tooltip">Chatter Limit<span class="tooltiptext">Enter number of chattering triggers to record before disabling the trigger</span></th>
						<th style="width: 3%" align="center" class="tooltip">Chatter Rate<span class="tooltiptext">Enter number of triggers per second to define a chattering trigger</span></th>
						<th style="width: 3%" align="center" class="tooltip">Trigger Limit (ms)<span class="tooltiptext">Enter how long(ms) to record when a measurement goes out of limits indefinitely</span></th>
						<th style="width: 3%" align="center" class="tooltip">ROC Duration (cycles)<span class="tooltiptext">Enter the number of line cycles to perform the Rate of Change measurement over (value of 0 disables ROC)</span></th>
						<th style="width: 3%" align="center" class="tooltip">ROC -<span class="tooltiptext">Enter Negative limit for Rate of Change, ie; Hz/sec, Watts/sec, Volts/sec, etc</span></th>
						<th style="width: 3%" align="center" class="tooltip">ROC +<span class="tooltiptext">Enter Positive limit for Rate of Change, ie; Hz/sec, Watts/sec, Volts/sec, etc</span></th>
						<th style="width: 1%" align="center">Export <s:checkbox
								name="checkUncheck" id="checkUncheck" theme="simple"
								onchange="updateExportStatus(this.checked);" />
						</th>
						<th style="width: 1%" align="center" class="tooltip">DDR<s:checkbox
								name="ddrCheckUncheck" id="ddrCheckUncheck" theme="simple"
								 /><span class="tooltiptext">Check to make the corresponding measurement to be part of the DDR record</span>
						</th>	
						<th style="width: 1%" align="center" class="tooltip">Disturbance Alarm<s:checkbox
								name="dAlarmCheckUncheck" id="dAlarmCheckUncheck" theme="simple"
								 /><span class="tooltiptext">Check to make it part of disturbance alarm</span>
						</th>					
						<!-- <th style="width:10%" align="center">Export Rate</th> -->
					</tr>
				</thead>
				<tbody id="measurementBody">

					<s:if
						test="%{lstTriggerChannels != null && lstTriggerChannels.size() > 0}">
						<div id="assistRowDiv1" style="display:none">
							<tr id="assistRowDiv" style="background-color: lightblue;display:none;position:sticky;top:50px;z-index: 5;">
								<td align="center" style="background-color: #ff3333"><a id="deleteSelected" href="javascript:void(0)" ><span class="ui-icon ui-icon-trash" Title="Check measurements to delete"></span></a></td>
								<td align="center" style="background-color: lightblue"></td>
								<s:if test="%{accessMode == 'Edit'}">
									<td align="center" style="background-color: lightblue"><input id="btnReinit" type="button" Title="Re-initialize IDs to eliminate gaps in the sequence." value="Re-initialize"> </input></td>
								</s:if>
								<td align="center" style="background-color: lightblue"><s:select
										name="globalChannel" id="globalChannel" Title="Select multiple measurements below to assign channels starting from the selected channels"
										list="lstTriggerSourceDto" listKey="sourceValue"
										listValue="sourceDisplayName" theme="simple" value=""
										onchange="javascript:updateGlobalChannel(this);" /></td>
								<td style="white-space: nowrap" align="center"><s:textfield
										style="background-color:lightblue" theme="simple"
										id="globalName" size="50" value=""
										onkeyup="javascript:updateGlobalName(this.value);" />
									<input type="button" onclick="populate_default_trigger_name();return false;"
										value="Copy Analog Names"> </input>
								</td>
								<td align="center" style="background-color: lightblue"><s:select
										theme="simple" name="globalType" id="globalType"
										list="%{mapGlobalTriggerInputTypes}"
										onchange="javascript:updateGlobalAlgorithms(this.value);"
										value="" /></td>
								<td align="center" style="background-color: lightblue"><s:textfield
										style="background-color:lightblue" theme="simple"
										name="globalAverage" id="globalAverage" size="3" value=""
										onkeyup="javascript:updateAverage(this.value);" /></td>
								<td align="center" style="background-color: lightblue"><s:textfield
										style="background-color:lightblue" theme="simple"
										name="globalHarmonic" id="globalHarmonic" size="3" value=""
										onkeyup="javascript:updateHarmonic(this.value);" /></td>
								<td align="center" style="background-color: lightblue"></td>
								<td align="center" style="background-color: lightblue"></td>
								<td align="center"><s:select
										style="background-color:lightblue" theme="simple"
										name="globalStart" id="globalStart"
										list="{'Never', 'ROC', 'Over', 'Under', 'Both'}" value=""
										onchange="javascript:updateTriggersStart(this.value);" /></td>
								<td align="center"><s:textfield
										style="background-color:lightblue" theme="simple"
										name="globalTripIfOver" id="globalTripIfOver" size="3"
										value=""
										onkeyup="javascript:updateTriggersUpperLimit(this.value);" /></td>
								<td align="center"><s:textfield
										style="background-color:lightblue" theme="simple"
										name="globalTripIfUnder" id="globalTripIfUnder" size="3"
										value=""
										onkeyup="javascript:updateTriggersLowerLimit(this.value);" /></td>
								<td align="center"><s:textfield
										style="background-color:lightblue" theme="simple"
										name="globalChatterLimit" id="globalChatterLimit" size="3"
										value="" onkeyup="javascript:updateChatterLimit(this.value);" /></td>
								<td align="center"><s:textfield
										style="background-color:lightblue" theme="simple"
										name="globalChatterRate" id="globalChatterRate" size="3"
										value="" onkeyup="javascript:updateChatterRate(this.value);" /></td>
								<td align="center"><s:textfield
										style="background-color:lightblue" theme="simple"
										name="globalTriggerLimit" id="globalTriggerLimit" size="3"
										value="" onkeyup="javascript:updateTriggersLimit(this.value);" /></td>
								<td align="center"><s:textfield
										style="background-color:lightblue" theme="simple"
										name="globalDuration" id="globalDuration" size="3" value=""
										onkeyup="javascript:updateDuration(this.value);"  /></td>
								<td align="center"><s:textfield
										style="background-color:lightblue" theme="simple"
										name="globalTripRocNeg" id="globalTripRocNeg" size="3" value=""
										onkeyup="javascript:updateTripRocNeg(this.value);"  /></td>
								<td align="center"><s:textfield
										style="background-color:lightblue" theme="simple"
										name="globalTripRocPos" id="globalTripRocPos" size="3" value=""
										onkeyup="javascript:updateTripRocPos(this.value);"  /></td>
								<!--<td align="center"><s:textfield style="background-color:lightblue" theme="simple" name="globalExportRate" id="globalExportRate" size="3" value="" onkeyup="javascript:updateExportRate(this.value);"/></td>-->
								<td align="center" style="background-color: lightblue"></td>
								<td align="center" style="background-color: lightblue"></td>
								<td align="center" style="background-color: lightblue"></td>

							</tr>
						</div>

						<s:iterator value="lstTriggerChannels" status="trigger_stat">
							<s:if test="%{lstTriggerChannels[#trigger_stat.index].inputType != 'MEASUREMENT'}">
							<tr id="tr_<s:property value='%{#trigger_stat.index}'/>" style="background-color: #EEEEFF">
								<td id="td_globalSelect_<s:property value='%{#trigger_stat.index}'/>" align="center" style="background-color:lightblue;display:none;"><s:checkbox
										name="globalSelect_%{#trigger_stat.index}"
										id="globalSelect_%{#trigger_stat.index}"
										style="background-color:lightblue;display:none;" theme="simple" value="" /><a href="javascript:void(0)" onclick="deleteMeasurement({
                            id: '<s:property value="%{id}"/>',
                            type: '<s:property value="%{type}"/>',
                            measurementName: '<s:property value="%{id}"/>-<s:property value="%{inputChannelName}"/>',
                            chassis: '<s:property value="%{chassis}"/>'
                        },'<s:property value="%{#trigger_stat.index}"/>')">
                            <span class="ui-icon ui-icon-trash"></span>
                        </a></td>

								<td align="center"><s:checkbox
										name="lstTriggerChannels[%{#trigger_stat.index}].status"
										id="lstTriggerChannels[%{#trigger_stat.index}].status"
										theme="simple" value="%{status}"
										onchange="return changeState(this, this.checked);" /></td>
								<s:if test="%{accessMode == 'Edit'}">
									<td align="center"><s:label
											name="lstTriggerChannels[%{#trigger_stat.index}].id"
											id="lstTriggerChannels[%{#trigger_stat.index}].id"
											theme="simple" value="%{id}" /></td>
								</s:if>
								<s:else>
									<td align="center" style="display:none;"><s:label
											name="lstTriggerChannels[%{#trigger_stat.index}].id"
											id="lstTriggerChannels[%{#trigger_stat.index}].id"
											theme="simple" value="%{id}" /></td>
								</s:else>
									<s:hidden theme="simple" name="lstTriggerChannels[%{#trigger_stat.index}].id"></s:hidden>
								
								<td align="center"><s:fielderror>
										<s:param>lstTriggerChannels[<s:property
												value="%{#trigger_stat.index}" />].channel</s:param>
									</s:fielderror> 
									<s:select
										cssStyle="font-family:courier, courier new, serif;"
										name="lstTriggerChannels[%{#trigger_stat.index}].channel"
										id="lstTriggerChannels[%{#trigger_stat.index}].channel"
										list="lstTriggerSourceDto" listKey="sourceValue"
										listValue="sourceDisplayName" theme="simple"
										value="%{channel}"
										onchange="javascript:populate_trigger_name(this, this.value);$(this).publish('reloadTriggerTypes_%{#trigger_stat.index}',this, event);"
										disabled="%{!lstTriggerChannels[#trigger_stat.index].status}" />
								</td>
								<td align="center"><s:fielderror>
										<s:param>lstTriggerChannels[<s:property
												value="%{#trigger_stat.index}" />].name</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstTriggerChannels[%{#trigger_stat.index}].name"
										id="lstTriggerChannels[%{#trigger_stat.index}].name" size="64"
										maxlength="64" value="%{name}"
										disabled="%{!lstTriggerChannels[#trigger_stat.index].status}" /></td>
								<s:hidden theme="simple" 
									name="lstTriggerChannels[%{#trigger_stat.index}].phase"></s:hidden>
								<s:property
									value="Trigger_Type_triggerType[%{#trigger_stat.index}]" />
								<td align="center">
										<s:url var="triggerTypeUrl" action="populateTriggerTypes">
											<s:param name="reqdIndex" value="%{#trigger_stat.index}"></s:param>
										</s:url> 
								<sj:div indicator="indicator"
										errorText="Loading failed. Try refresh."
										id="triggerType%{#trigger_stat.index}"
										href="%{#triggerTypeUrl}"
										listenTopics="reloadTriggerTypes_%{#trigger_stat.index}"
										deferredLoading="true" formIds="triggerChannels" >
											<s:select theme="simple"
												name="lstTriggerChannels[%{#trigger_stat.index}].type"
												id="lstTriggerChannels[%{#trigger_stat.index}].type"
												list="%{lstTriggerChannels[#trigger_stat.index].mapTriggerInputTypes}"
												onchange="javascript:update_access(this.name, this.value);update_trigger_name(this, this.value);return false;"
												value="%{lstTriggerChannels[#trigger_stat.index].type}"
												disabled="%{!lstTriggerChannels[#trigger_stat.index].status}" />
											<s:hidden theme="simple" 
												name="lstTriggerChannels[%{#trigger_stat.index}].chassis"></s:hidden>
											<s:hidden theme="simple" 
												name="lstTriggerChannels[%{#trigger_stat.index}].inputChannelName"></s:hidden>
											<s:hidden theme="simple" 
												name="lstTriggerChannels[%{#trigger_stat.index}].inputType"></s:hidden>

									</sj:div>
									</td>
								<td align="center"><s:fielderror>
										<s:param>lstTriggerChannels[<s:property
												value="%{#trigger_stat.index}" />].average</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstTriggerChannels[%{#trigger_stat.index}].average"
										id="lstTriggerChannels[%{#trigger_stat.index}].average" size="3"
										value="%{average}"
										disabled="%{!lstTriggerChannels[#trigger_stat.index].status}" /></td>
								<td align="center"><s:fielderror>
										<s:param>lstTriggerChannels[<s:property
												value="%{#trigger_stat.index}" />].harmonic</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstTriggerChannels[%{#trigger_stat.index}].harmonic"
										id="lstTriggerChannels[%{#trigger_stat.index}].harmonic" size="3"
										value="%{harmonic}"
										disabled="%{lstTriggerChannels[#trigger_stat.index].disableHarmonic}" /></td>
								<td align="center"><s:checkbox
										name="lstTriggerChannels[%{#trigger_stat.index}].pmuStatus"
										id="lstTriggerChannels[%{#trigger_stat.index}].pmuStatus"
										theme="simple" value="%{pmuStatus}"
										onchange="return changePmuState(this, this.checked);"
										disabled="%{!lstTriggerChannels[#trigger_stat.index].status}" /></td>
								<td align="center"><s:checkbox
										name="lstTriggerChannels[%{#trigger_stat.index}].freqPmuStatus"
										id="lstTriggerChannels[%{#trigger_stat.index}].freqPmuStatus"
										theme="simple" value="%{freqPmuStatus}"
										disabled="%{!lstTriggerChannels[#trigger_stat.index].status || !lstTriggerChannels[#trigger_stat.index].pmuStatus || lstTriggerChannels[#trigger_stat.index].type != 'Frequency'}" /></td>
								<!--  td align="center">
								<s:hidden theme="simple" 
										name="lstTriggerChannels[%{#trigger_stat.index}].freqPmuStatus"></s:hidden>
								<s:if test="%{lstTriggerChannels[#trigger_stat.index].freqPmuStatus}">
									<s:radio theme="simple" id="lstTriggerChannels[%{#trigger_stat.index}].freqPmuId" name="freqPmuId" list="{#trigger_stat.index}" listValue="%{''}" value="%{#trigger_stat.index}" disabled="%{!lstTriggerChannels[#trigger_stat.index].status || !lstTriggerChannels[#trigger_stat.index].pmuStatus || lstTriggerChannels[#trigger_stat.index].type != 'Frequency'}"/>
								</s:if>
								<s:else>
									<s:radio theme="simple" id="lstTriggerChannels[%{#trigger_stat.index}].freqPmuId" name="freqPmuId" value="" list="{#trigger_stat.index}" listValue="%{''}" disabled="%{!lstTriggerChannels[#trigger_stat.index].status || !lstTriggerChannels[#trigger_stat.index].pmuStatus || lstTriggerChannels[#trigger_stat.index].type != 'Frequency'}"/>
								</s:else>
								</td -->
								<td align="center"><s:select theme="simple"
										name="lstTriggerChannels[%{#trigger_stat.index}].start"
										id="lstTriggerChannels[%{#trigger_stat.index}].start"
										list="{'Never', 'ROC', 'Over', 'Under', 'Both'}" value="%{start}"
										onchange="javascript:set_trigger(this, this.value);"
										disabled="%{!lstTriggerChannels[#trigger_stat.index].status || (lstTriggerChannels[#trigger_stat.index].type == 'Phasor')}" /></td>
								<td align="center"><s:fielderror>
										<s:param>lstTriggerChannels[<s:property
												value="%{#trigger_stat.index}" />].tripIfOver</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstTriggerChannels[%{#trigger_stat.index}].tripIfOver"
										id="lstTriggerChannels[%{#trigger_stat.index}].tripIfOver"
										size="3" value="%{tripIfOver}"
										disabled="%{(!lstTriggerChannels[#trigger_stat.index].status || lstTriggerChannels[#trigger_stat.index].disableTripOver)}" /></td>
								<td align="center"><s:fielderror>
										<s:param>lstTriggerChannels[<s:property
												value="%{#trigger_stat.index}" />].tripIfUnder</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstTriggerChannels[%{#trigger_stat.index}].tripIfUnder"
										id="lstTriggerChannels[%{#trigger_stat.index}].tripIfUnder"
										size="3" value="%{tripIfUnder}"
										disabled="%{(!lstTriggerChannels[#trigger_stat.index].status || lstTriggerChannels[#trigger_stat.index].disableTripUnder)}" /></td>
								<td align="center"><s:fielderror>
										<s:param>lstTriggerChannels[<s:property
												value="%{#trigger_stat.index}" />].chatterLimit</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstTriggerChannels[%{#trigger_stat.index}].chatterLimit"
										id="lstTriggerChannels[%{#trigger_stat.index}].chatterLimit"
										size="3" value="%{chatterLimit}"
										disabled="%{(!lstTriggerChannels[#trigger_stat.index].status || !lstTriggerChannels[#trigger_stat.index].triggerStatus)}" /></td>
								<td align="center"><s:fielderror>
										<s:param>lstTriggerChannels[<s:property
												value="%{#trigger_stat.index}" />].chatterRate</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstTriggerChannels[%{#trigger_stat.index}].chatterRate"
										id="lstTriggerChannels[%{#trigger_stat.index}].chatterRate"
										size="3" value="%{chatterRate}"
										disabled="%{(!lstTriggerChannels[#trigger_stat.index].status || !lstTriggerChannels[#trigger_stat.index].triggerStatus)}" /></td>
								<td align="center"><s:fielderror>
										<s:param>lstTriggerChannels[<s:property
												value="%{#trigger_stat.index}" />].triggerLimit</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstTriggerChannels[%{#trigger_stat.index}].triggerLimit"
										id="lstTriggerChannels[%{#trigger_stat.index}].triggerLimit"
										size="3" value="%{triggerLimit}"
										disabled="%{(!lstTriggerChannels[#trigger_stat.index].status || !lstTriggerChannels[#trigger_stat.index].triggerStatus)}" /></td>
								<td align="center"><s:fielderror>
										<s:param>lstTriggerChannels[<s:property
												value="%{#trigger_stat.index}" />].duration</s:param>
									</s:fielderror><s:textfield theme="simple"
										name="lstTriggerChannels[%{#trigger_stat.index}].duration"
										id="lstTriggerChannels[%{#trigger_stat.index}].duration"
										size="3" value="%{duration}" disabled="%{(!lstTriggerChannels[#trigger_stat.index].status || lstTriggerChannels[#trigger_stat.index].disableDuration)}"/></td>
								<td align="center"><s:fielderror>
										<s:param>lstTriggerChannels[<s:property
												value="%{#trigger_stat.index}" />].tripRocNeg</s:param>
									</s:fielderror><s:textfield theme="simple"
										name="lstTriggerChannels[%{#trigger_stat.index}].tripRocNeg"
										id="lstTriggerChannels[%{#trigger_stat.index}].tripRocNeg"
										size="3" value="%{tripRocNeg}" disabled="%{(!lstTriggerChannels[#trigger_stat.index].status || lstTriggerChannels[#trigger_stat.index].disableTripRoc)}"/></td>
								<td align="center"><s:fielderror>
										<s:param>lstTriggerChannels[<s:property
												value="%{#trigger_stat.index}" />].tripRocPos</s:param>
									</s:fielderror><s:textfield theme="simple"
										name="lstTriggerChannels[%{#trigger_stat.index}].tripRocPos"
										id="lstTriggerChannels[%{#trigger_stat.index}].tripRocPos"
										size="3" value="%{tripRocPos}" disabled="%{(!lstTriggerChannels[#trigger_stat.index].status || lstTriggerChannels[#trigger_stat.index].disableTripRoc)}"/></td>
								<td align="center"><s:checkbox
										name="lstTriggerChannels[%{#trigger_stat.index}].exportStatus"
										id="lstTriggerChannels[%{#trigger_stat.index}].exportStatus"
										theme="simple" value="%{exportStatus}"
										onchange="return changeExportState(this, this.checked);"
										disabled="%{!lstTriggerChannels[#trigger_stat.index].status || lstTriggerChannels[#trigger_stat.index].type == 'Rms'}" /></td>
								<td align="center"><s:checkbox
										name="lstTriggerChannels[%{#trigger_stat.index}].ddrStatus"
										id="lstTriggerChannels[%{#trigger_stat.index}].ddrStatus"
										theme="simple" value="%{ddrStatus}"
										onchange="return changeDdrState(this, this.checked);"
										disabled="%{!lstTriggerChannels[#trigger_stat.index].status || lstTriggerChannels[#trigger_stat.index].type == 'Rms' || !lstTriggerChannels[#trigger_stat.index].exportStatus}" /></td>
								<td align="center"><s:checkbox
										name="lstTriggerChannels[%{#trigger_stat.index}].disturbanceAlarm"
										id="lstTriggerChannels[%{#trigger_stat.index}].disturbanceAlarm"
										theme="simple" value="%{disturbanceAlarm}"
										onchange="return isAllDAlarmChecked();"
										disabled="%{!lstTriggerChannels[#trigger_stat.index].status || lstTriggerChannels[#trigger_stat.index].start == 'Never'}" /></td>
								<!--<td align="center"><s:fielderror><s:param>lstTriggerChannels[<s:property value="%{#trigger_stat.index}"/>].exportRate</s:param></s:fielderror><s:textfield theme="simple" name="lstTriggerChannels[%{#trigger_stat.index}].exportRate" id="lstTriggerChannels[%{#trigger_stat.index}].exportRate" size="3" value="%{exportRate}" disabled="%{(!lstTriggerChannels[#trigger_stat.index].status || !lstTriggerChannels[#trigger_stat.index].exportStatus || lstTriggerChannels[#trigger_stat.index].pmuStatus)}"/></td>-->
								<s:if
									test="%{lstTriggerChannels[%{#trigger_stat.index}].globalLineGroupId != null}">
									<s:hidden theme="simple" 
										name="lstTriggerChannels[%{#trigger_stat.index}].globalLineGroupId"></s:hidden>
								</s:if>
								<s:hidden theme="simple" 
									name="lstTriggerChannels[%{#trigger_stat.index}].disableHarmonic"></s:hidden>
								<s:hidden theme="simple" 
									name="lstTriggerChannels[%{#trigger_stat.index}].triggerStatus"></s:hidden>
								<s:hidden theme="simple" 
									name="lstTriggerChannels[%{#trigger_stat.index}].disableTripOver"></s:hidden>
								<s:hidden theme="simple" 
									name="lstTriggerChannels[%{#trigger_stat.index}].disableTripUnder"></s:hidden>
								<s:hidden theme="simple" 
									name="lstTriggerChannels[%{#trigger_stat.index}].disableTripRoc"></s:hidden>
								<s:hidden theme="simple" 
									name="lstTriggerChannels[%{#trigger_stat.index}].disableDuration"></s:hidden>
								<s:hidden theme="simple" id="lstTriggerChannels[%{#trigger_stat.index}].transducer"
											name="lstTriggerChannels[%{#trigger_stat.index}].transducer"></s:hidden>
							</tr>
						</s:if>
						</s:iterator>
					</s:if>
					<s:else>
						<tr>
							<td colspan="100%" style="width: 100%;" align="center">
								<p>No Measurements configured</p>
							</td>
						</tr>
					</s:else>
				</tbody>
			</table>
				<table style="width:100%">
					<tbody>
						<tr id="buttonsBottom" style="display: none">
							<td align="center"><s:submit theme="simple" align="left"
									cssClass="submit" name="btnSubmit" value="Back" action="triggerChannelsBack"
									 /> 
								<sj:a openDialog="newMeasurementsDialog">
									<s:submit theme="simple" align="center" cssClass="submit"
										id="btnAddMeasurement" name="btnAddMeasurement"
										value="Add Measurements"/>
								</sj:a>
									<s:submit id="btnBottomNext" theme="simple" align="center" cssClass="submit"
										name="btnSubmit" value="Next" action="triggerChannelsNext"
										 />
								</td>
						</tr>
					</tbody>
				</table>
				
			<s:hidden theme="simple" id="sourceTab" name="sourceTab" value=""></s:hidden>
			<s:hidden theme="simple" id="booAnalog" name="booAnalog"></s:hidden>
			<s:hidden theme="simple" id="booDigital" name="booDigital"></s:hidden>
			<s:hidden theme="simple" id="lineGroupExists" name="lineGroupExists"></s:hidden>
			<s:hidden theme="simple" id="originTab" name="originTab" value="Measurements"></s:hidden>
			<s:hidden theme="simple" id="booReorderMeasurementIds" name="booReorderMeasurementIds" value=""></s:hidden>
			<s:hidden theme="simple" id="jspFileName" name="jspFileName" value="configureTriggerChannels.jsp"></s:hidden>
		</tbody>
		<div id="dummyTarget" style="display:none;"></div>
	</s:form>
		<sj:dialog id="newMeasurementsDialog" formIds="triggerChannels" autoOpen="false" modal="true" title="Create New Measurements" position="{'my': 'right bottom', 'at': 'center', 'of': '[id=btnAddMeasurement]'}" width="500" closeTopics="closeNewDialogTopic">
		
		<s:form id="newMeasurementsForm" theme="simple" action="triggerChannelsAddMeasurements" >
				<img id="indicator" src="images/indicator.gif" alt="Loading..." style="display:none"/>
				<s:hidden id="totalMeasurementsCount" name="totalMeasurementsCount" value="%{lstTriggerChannels.size()}"/>
				<s:url var="addNewTrigger" action="triggerChannelsAddMeasurements">
				</s:url>
				<p id="successMsg"></p>
				<div id="formErrors"></div>
				<div class="type-button">
				    <!-- First row: Label and Textfield -->
				    <div class="row">
				        <s:label cssStyle="font-weight:bold;text-align:center" value="Enter the number of new measurements to add"></s:label>
				        <s:textfield name="newMeasurementsCount" id="newMeasurementsCount" size="3" value="1" />
				    </div>
				    <div class="row">
				        <s:label cssStyle="font-weight:bold;text-align:center" value="Enter the new measurement type to add"></s:label>
				        <s:select name="newMeasurementsType" id="newMeasurementsType" list="%{mapAnalogTriggerInputTypes}"/>
				    </div>
					<s:url var="addNewTrigger" action="triggerChannelsAddMeasurements">
						
					</s:url>
				    <!-- Second row: Confirm and Cancel buttons -->
				    <div class="row buttons-row">
				        <sj:submit id="btnAddNewMeasurements" indicator="indicator" value="Add" button="true" targets="dummyTarget" formIds="newMeasurementsForm"
                                            buttonText="Add" onSuccessTopics="newMeasurementsTopic"
                                            onErrorTopics="newMeasurementsErrorTopic" onBeforeTopics="showBusyCursor" onCompleteTopics="hideBusyCursor"/>
				        <sj:a onClickTopics="cancelMeasurementsTopic" button="true">Cancel</sj:a>
				    </div>
				</div>
				<h3>Please cancel or close the dialog once you are done adding new measurements</h3>
		</s:form>
		</sj:dialog>

</body>
</html>
