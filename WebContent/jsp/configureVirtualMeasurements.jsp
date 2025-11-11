<%@ include file="header.jsp"%>
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
	if (request.getAttribute("#virtual_stat.index") != null)
	{
		addedIndex = (Integer)request.getAttribute("#virtual_stat.index");
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
	$("*").css("cursor", "progress");
</script>
<script src="js/m9000/m9k-utils.js" type="text/javascript"></script>
<script src="js/m9000/configureVirtualMeasurements.js" type="text/javascript">

</script>
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
							onclick="javascript:submitForm('LineGroups');"> > LineGroups > </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');"> Measurements > </s:a>Virtual Measurements<s:a
							cssClass="traverseMenu" href="#"
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
							onclick="javascript:submitForm('LineGroups');"> > LineGroups > </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');"> Measurements > </s:a>Virtual Measurements<s:a
							cssClass="traverseMenu" href="#"
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
							onclick="javascript:submitForm('LineGroups');"> > LineGroups > </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');"> Measurements > </s:a>Virtual Measurements<s:a
							cssClass="traverseMenu" href="#"
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
							onclick="javascript:submitForm('LineGroups');"> > LineGroups > </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');"> Measurements > </s:a>Virtual Measurements 
					</div>
				</s:else>
			</s:else>

		</s:if>
	</div>
	<s:if test="%{lstVirtualMeasurements != null && lstVirtualMeasurements.size() > 0}">
	<s:actionerror id="userMsgError" theme="jquery"/>
	<s:actionmessage  id="userMsgInfo" theme="jquery"/>
	</s:if>
	<s:form id="frmConfigureVirtualMeasurements" name="frmConfigureVirtualMeasurements" action="virtualMeasurementsAction" method="POST" cssStyle="width: 100%">
		<tbody>

			<tr style="width:100%" >
			<td width="100%" class="titleLabel"
							style="background-color: #9999CC">Virtual Measurements Configuration</td>
			</tr>
			<s:if
				test="%{lstVirtualMeasurements != null && lstVirtualMeasurements.size() > 0}">
				<s:if test="%{lstAffectedLineGroups != null && lstAffectedLineGroups.size()>0}">
				<div  style="color:red" class="result ui-widget-content ui-corner-all">

					Please verify Virtual Measurements associated to the below linegroup(s)  
					<s:iterator value="lstAffectedLineGroups" status="lg_stat">
						<p><s:property value="%{lstAffectedLineGroups[#lg_stat.index]}"/></p>
					</s:iterator>
				</div>
				</s:if>
			</s:if>

			<div id="divresult" style="display:none"/>
				<table style="width:100%">
					<tbody>
						<tr id="buttonsTop" style="display: none">
							<td align="center"><s:submit theme="simple" align="left"
									cssClass="submit" name="btnSubmit" value="Back" action="virtualMeasurementsBackToTriggers"
									 /> 
									 <sj:a openDialog="newVirtualDialog">	
									 <s:submit theme="simple" align="center" cssClass="submit"
										id="topCreateVirtualMeasurement" name="createVirtualMeasurement"
										value="Create"/>
									</sj:a>
									<s:if
									test="%{booDigital ||(lstLineGroups != null && lstLineGroups.size()>0)}">
									<s:submit theme="simple" align="center" cssClass="submit"
										name="btnSubmit" value="Next" action="saveVirtualMeasurementsAndNext"
										 />
								</s:if> <s:else>
									<s:submit theme="simple" align="center" cssClass="submit"
											name="btnSubmit" value="Finish" action="virtualMeasurementsFinish"
											onclick="return confirm('Do you want to save and apply the current changes to the station master?');" />
									<s:submit theme="simple" align="center" cssClass="submit"
											name="btnSubmit" value="Finish And Send"
											action="virtualMeasurementsSend"
											onclick="return confirm('Do you want to save and apply the current changes to the station master?');" />
								</s:else></td>
						</tr>
					</tbody>
				</table>
			<table id="virtualMeasurementTable" class="configureTable" 
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
						<th style="width: 15%" style="lightblue-space: nowrap;"
							align="center" class="tooltip">Name</th>
						<th colspan="2" style="width: 10%" align="center">Virtual Logic</th>
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
								onchange="updateDdrStatus(this.checked);" /><span class="tooltiptext">Check to make the corresponding measurement to be part of the DDR record</span>
						</th>	
						<th style="width: 1%" align="center" class="tooltip">Disturbance Alarm<s:checkbox
								name="dAlarmCheckUncheck" id="dAlarmCheckUncheck" theme="simple"
								/><span class="tooltiptext">Check to make it part of disturbance alarm</span>
						</th>
						<th style="width: 5%" align="center">Delete</th>					
						<!-- <th style="width:10%" align="center">Export Rate</th> -->
					</tr>
				</thead>
				<tbody id="virtualMeasurementBody">

					<s:if
						test="%{lstVirtualMeasurements != null && lstVirtualMeasurements.size() > 0}">
						<s:iterator value="lstVirtualMeasurements" status="virtual_stat">
							<s:url var="urlEditVirtualDialog#{virtual_stat.index}" action="editVirtualMeasurementsDialog" escapeAmp="false">
								<s:param name="selectedVirtualIndex" value="%{#virtual_stat.index}"/>
							</s:url>
							<s:url var="urlDeleteVirtualChannel#{virtual_stat.index}" action="deleteVirtualMeasurement">
									<s:param name="selectedVirtualIndex" value="%{#virtual_stat.index}"/>
							</s:url>
						
							<tr id="tr_<s:property value='%{#virtual_stat.index}'/>" style="background-color: #EEEEFF">
								<td id="td_globalSelect_<s:property value='%{#virtual_stat.index}'/>" align="center" style="background-color:lightblue;display:none;"><s:checkbox
										name="globalSelect_%{#virtual_stat.index}"
										id="globalSelect_%{#virtual_stat.index}"
										style="background-color:lightblue;display:none;" theme="simple" value="" /></td>

								<td align="center"><s:checkbox
										name="lstVirtualMeasurements[%{#virtual_stat.index}].status"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].status"
										theme="simple" value="%{status}"
										onchange="return changeState(this, this.checked);" /></td>
								<s:if test="%{accessMode == 'Edit'}">
									<td align="center"><s:label
											name="lstVirtualMeasurements[%{#virtual_stat.index}].id"
											id="lstVirtualMeasurements[%{#virtual_stat.index}].id"
											theme="simple" value="%{id}" /></td>
									<s:hidden theme="simple" name="lstVirtualMeasurements[%{#virtual_stat.index}].id"></s:hidden>
								</s:if>
								<td align="center"><s:fielderror>
										<s:param>lstVirtualMeasurements[<s:property
												value="%{#virtual_stat.index}" />].name</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstVirtualMeasurements[%{#virtual_stat.index}].name"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].name" size="50"
										maxlength="64" value="%{name}"
										disabled="%{!lstVirtualMeasurements[#virtual_stat.index].status}" /></td>
								<td colspan="2" align="center">
										<s:fielderror>
										<s:param>lstVirtualMeasurements[<s:property
												value="%{#virtual_stat.index}" />].virtualLogic</s:param>
									</s:fielderror>
											
										<s:textfield size="30" cssStyle="font-size: 10px;" readonly="true"
											name="lstVirtualMeasurements[%{#virtual_stat.index}].virtualLogic"
											id="lstVirtualMeasurements[%{#virtual_stat.index}].virtualLogic"
											theme="simple" value="%{virtualLogic}" title="%{virtual}" />
										<sj:a openDialog="editVirtualDialog" cssStyle="width:10%" title="Edit Virtual Measurement Details" href="%{urlEditVirtualDialog#{virtual_stat.index}}" button="true" buttonIconSecondary="ui-icon-pencil"></sj:a>
								
								</td>
								<s:hidden theme="simple" 
									name="lstVirtualMeasurements[%{#virtual_stat.index}].chassis"></s:hidden>
								<s:hidden theme="simple" 
									name="lstVirtualMeasurements[%{#virtual_stat.index}].phase"></s:hidden>
								<s:property
									value="Trigger_Type_triggerType[%{#virtual_stat.index}]" />
								<td align="center"><s:fielderror>
										<s:param>lstVirtualMeasurements[<s:property
												value="%{#virtual_stat.index}" />].average</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstVirtualMeasurements[%{#virtual_stat.index}].average"
										id="lstVirtualMeasurements[#virtual_stat.index].average" size="3"
										value="%{average}"
										disabled="%{!lstVirtualMeasurements[#virtual_stat.index].status}" /></td>
								<td align="center"><s:fielderror>
										<s:param>lstVirtualMeasurements[<s:property
												value="%{#virtual_stat.index}" />].harmonic</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstVirtualMeasurements[%{#virtual_stat.index}].harmonic"
										id="lstVirtualMeasurements[#virtual_stat.index].harmonic" size="3"
										value="%{harmonic}"
										disabled="%{lstVirtualMeasurements[#virtual_stat.index].disableHarmonic}" /></td>
								<td align="center"><s:checkbox
										name="lstVirtualMeasurements[%{#virtual_stat.index}].pmuStatus"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].pmuStatus"
										theme="simple" value="%{pmuStatus}"
										onchange="return changePmuState(this, this.checked);"
										disabled="%{!lstVirtualMeasurements[#virtual_stat.index].status}" /></td>
								<td align="center"><s:checkbox
										name="lstVirtualMeasurements[%{#virtual_stat.index}].freqPmuStatus"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].freqPmuStatus"
										theme="simple" value="%{freqPmuStatus}"
										disabled="%{!lstVirtualMeasurements[#virtual_stat.index].status || !lstVirtualMeasurements[#virtual_stat.index].pmuStatus || lstVirtualMeasurements[#virtual_stat.index].type != 'Frequency'}" /></td>
								<!--  td align="center">
								<s:hidden theme="simple" 
										name="lstVirtualMeasurements[%{#virtual_stat.index}].freqPmuStatus"></s:hidden>
								<s:if test="%{lstVirtualMeasurements[#virtual_stat.index].freqPmuStatus}">
									<s:radio theme="simple" id="lstVirtualMeasurements[%{#virtual_stat.index}].freqPmuId" name="freqPmuId" list="{#virtual_stat.index}" listValue="%{''}" value="%{#virtual_stat.index}" disabled="%{!lstVirtualMeasurements[#virtual_stat.index].status || !lstVirtualMeasurements[#virtual_stat.index].pmuStatus || lstVirtualMeasurements[#virtual_stat.index].type != 'Frequency'}"/>
								</s:if>
								<s:else>
									<s:radio theme="simple" id="lstVirtualMeasurements[%{#virtual_stat.index}].freqPmuId" name="freqPmuId" value="" list="{#virtual_stat.index}" listValue="%{''}" disabled="%{!lstVirtualMeasurements[#virtual_stat.index].status || !lstVirtualMeasurements[#virtual_stat.index].pmuStatus || lstVirtualMeasurements[#virtual_stat.index].type != 'Frequency'}"/>
								</s:else>
								</td -->
								<td align="center"><s:select theme="simple"
										name="lstVirtualMeasurements[%{#virtual_stat.index}].start"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].start"
										list="{'Never', 'ROC', 'Over', 'Under', 'Both'}" value="%{start}"
										onchange="javascript:set_trigger(this, this.value);"
										disabled="%{!lstVirtualMeasurements[#virtual_stat.index].status || (lstVirtualMeasurements[#virtual_stat.index].type == 'Phasor')}" /></td>
								<td align="center"><s:fielderror>
										<s:param>lstVirtualMeasurements[<s:property
												value="%{#virtual_stat.index}" />].tripIfOver</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstVirtualMeasurements[%{#virtual_stat.index}].tripIfOver"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].tripIfOver"
										size="3" value="%{tripIfOver}"
										disabled="%{(!lstVirtualMeasurements[#virtual_stat.index].status || lstVirtualMeasurements[#virtual_stat.index].disableTripOver)}" /></td>
								<td align="center"><s:fielderror>
										<s:param>lstVirtualMeasurements[<s:property
												value="%{#virtual_stat.index}" />].tripIfUnder</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstVirtualMeasurements[%{#virtual_stat.index}].tripIfUnder"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].tripIfUnder"
										size="3" value="%{tripIfUnder}"
										disabled="%{(!lstVirtualMeasurements[#virtual_stat.index].status || lstVirtualMeasurements[#virtual_stat.index].disableTripUnder)}" /></td>
								<td align="center"><s:fielderror>
										<s:param>lstVirtualMeasurements[<s:property
												value="%{#virtual_stat.index}" />].chatterLimit</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstVirtualMeasurements[%{#virtual_stat.index}].chatterLimit"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].chatterLimit"
										size="3" value="%{chatterLimit}"
										disabled="%{(!lstVirtualMeasurements[#virtual_stat.index].status || !lstVirtualMeasurements[#virtual_stat.index].triggerStatus)}" /></td>
								<td align="center"><s:fielderror>
										<s:param>lstVirtualMeasurements[<s:property
												value="%{#virtual_stat.index}" />].chatterRate</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstVirtualMeasurements[%{#virtual_stat.index}].chatterRate"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].chatterRate"
										size="3" value="%{chatterRate}"
										disabled="%{(!lstVirtualMeasurements[#virtual_stat.index].status || !lstVirtualMeasurements[#virtual_stat.index].triggerStatus)}" /></td>
								<td align="center"><s:fielderror>
										<s:param>lstVirtualMeasurements[<s:property
												value="%{#virtual_stat.index}" />].triggerLimit</s:param>
									</s:fielderror> <s:textfield theme="simple"
										name="lstVirtualMeasurements[%{#virtual_stat.index}].triggerLimit"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].triggerLimit"
										size="3" value="%{triggerLimit}"
										disabled="%{(!lstVirtualMeasurements[#virtual_stat.index].status || !lstVirtualMeasurements[#virtual_stat.index].triggerStatus)}" /></td>
								<td align="center"><s:fielderror>
										<s:param>lstVirtualMeasurements[<s:property
												value="%{#virtual_stat.index}" />].duration</s:param>
									</s:fielderror><s:textfield theme="simple"
										name="lstVirtualMeasurements[%{#virtual_stat.index}].duration"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].duration"
										size="3" value="%{duration}" disabled="%{(!lstVirtualMeasurements[#virtual_stat.index].status || lstVirtualMeasurements[#virtual_stat.index].disableDuration)}"/></td>
								<td align="center"><s:fielderror>
										<s:param>lstVirtualMeasurements[<s:property
												value="%{#virtual_stat.index}" />].tripRocNeg</s:param>
									</s:fielderror><s:textfield theme="simple"
										name="lstVirtualMeasurements[%{#virtual_stat.index}].tripRocNeg"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].tripRocNeg"
										size="3" value="%{tripRocNeg}" disabled="%{(!lstVirtualMeasurements[#virtual_stat.index].status || lstVirtualMeasurements[#virtual_stat.index].disableTripRoc)}"/></td>
								<td align="center"><s:fielderror>
										<s:param>lstVirtualMeasurements[<s:property
												value="%{#virtual_stat.index}" />].tripRocPos</s:param>
									</s:fielderror><s:textfield theme="simple"
										name="lstVirtualMeasurements[%{#virtual_stat.index}].tripRocPos"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].tripRocPos"
										size="3" value="%{tripRocPos}" disabled="%{(!lstVirtualMeasurements[#virtual_stat.index].status || lstVirtualMeasurements[#virtual_stat.index].disableTripRoc)}"/></td>
								<td align="center"><s:checkbox
										name="lstVirtualMeasurements[%{#virtual_stat.index}].exportStatus"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].exportStatus"
										theme="simple" value="%{exportStatus}"
										onchange="return changeExportState(this, this.checked);"
										disabled="%{!lstVirtualMeasurements[#virtual_stat.index].status || lstVirtualMeasurements[#virtual_stat.index].type == 'Rms'}" /></td>
								<td align="center"><s:checkbox
										name="lstVirtualMeasurements[%{#virtual_stat.index}].ddrStatus"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].ddrStatus"
										theme="simple" value="%{ddrStatus}"
										onchange="return changeDdrState(this, this.checked);"
										disabled="%{!lstVirtualMeasurements[#virtual_stat.index].status || lstVirtualMeasurements[#virtual_stat.index].type == 'Rms' || !lstVirtualMeasurements[#virtual_stat.index].exportStatus}" /></td>
								<td align="center"><s:checkbox
										name="lstVirtualMeasurements[%{#virtual_stat.index}].disturbanceAlarm"
										id="lstVirtualMeasurements[%{#virtual_stat.index}].disturbanceAlarm"
										theme="simple" value="%{disturbanceAlarm}"
										onchange="return isAllDAlarmChecked();"
										disabled="%{!lstVirtualMeasurements[#virtual_stat.index].status || lstVirtualMeasurements[#virtual_stat.index].start == 'Never'}" /></td>
								<td width="5%" align="center">
											<s:submit theme="simple" cssClass="submit" id="%{#virtual_stat.index}" name="btnDeleteVirtual" action="%{urlDeleteVirtualChannel#{virtual_stat.index}}"
											onclick="confirmationDialog(this.id,'Are you sure, you want to delete?');return false;"
											value="Delete"/>
											<sj:a listenTopics="confirmed%{#virtual_stat.index}" formIds="frmConfigureVirtualMeasurements" href="%{urlDeleteVirtualChannel#{virtual_stat.index}}" targets="virtualMeasurementBody"></sj:a>
											</td>
								<!--<td align="center"><s:fielderror><s:param>lstVirtualMeasurements[<s:property value="%{#virtual_stat.index}"/>].exportRate</s:param></s:fielderror><s:textfield theme="simple" name="lstVirtualMeasurements[%{#virtual_stat.index}].exportRate" id="lstVirtualMeasurements[%{#virtual_stat.index}].exportRate" size="3" value="%{exportRate}" disabled="%{(!lstVirtualMeasurements[#virtual_stat.index].status || !lstVirtualMeasurements[#virtual_stat.index].exportStatus || lstVirtualMeasurements[#virtual_stat.index].pmuStatus)}"/></td>-->
								<s:if
									test="%{lstVirtualMeasurements[%{#virtual_stat.index}].globalLineGroupId != null}">
									<s:hidden theme="simple" 
										name="lstVirtualMeasurements[%{#virtual_stat.index}].globalLineGroupId"></s:hidden>
								</s:if>
								<s:hidden theme="simple" 
									name="lstVirtualMeasurements[%{#virtual_stat.index}].disableHarmonic"></s:hidden>
								<s:hidden theme="simple" 
									name="lstVirtualMeasurements[%{#virtual_stat.index}].triggerStatus"></s:hidden>
								<s:hidden theme="simple" 
									name="lstVirtualMeasurements[%{#virtual_stat.index}].disableTripOver"></s:hidden>
								<s:hidden theme="simple" 
									name="lstVirtualMeasurements[%{#virtual_stat.index}].disableTripUnder"></s:hidden>
								<s:hidden theme="simple" 
									name="lstVirtualMeasurements[%{#virtual_stat.index}].disableTripRoc"></s:hidden>
								<s:hidden theme="simple" 
									name="lstVirtualMeasurements[%{#virtual_stat.index}].disableDuration"></s:hidden>
								<s:hidden theme="simple" id="lstVirtualMeasurements[%{#virtual_stat.index}].transducer"
											name="lstVirtualMeasurements[%{#virtual_stat.index}].transducer"></s:hidden>
								<s:hidden theme="simple" id="lstVirtualMeasurements[%{#virtual_stat.index}].type"
											name="lstVirtualMeasurements[%{#virtual_stat.index}].type" value="VirtualMeasurement"></s:hidden>
							</tr>

						</s:iterator>
					</s:if>
					<s:else>
						<tr>
							<td colspan="100%" style="width: 100%;" align="center">
								<p>No Virtual Measurements configured.</p>
							</td>
						</tr>
					</s:else>
				</tbody>
			</table>
				<table style="width:100%">
					<tbody>
						<tr id="buttonsBottom" style="display: none">
							<td align="center"><s:submit theme="simple" align="left"
									cssClass="submit" name="btnSubmit" value="Back" action="virtualMeasurementsBackToTriggers"
									 /> 
								 <sj:a openDialog="newVirtualDialog">					
										<s:submit theme="simple" align="center" cssClass="submit"
											id="createVirtualMeasurement" name="createVirtualMeasurement"
											value="Create"/>
								</sj:a>
								 <s:if
									test="%{booDigital ||(lstLineGroups != null && lstLineGroups.size()>0)}">
									<s:submit theme="simple" align="center" cssClass="submit"
										name="btnSubmit" value="Next" action="saveVirtualMeasurementsAndNext"
										 />
								</s:if> 
								<s:else>
										<s:submit theme="simple" align="center" cssClass="submit"
											name="btnSubmit" value="Finish" action="virtualMeasurementsFinish"
											onclick="return confirm('Do you want to save and apply the current changes to the station master?');" />
										<s:submit theme="simple" align="center" cssClass="submit"
											name="btnSubmit" value="Finish And Send"
											action="virtualMeasurementsSend"
											onclick="return confirm('Do you want to save and apply the current changes to the station master?');" />
								</s:else></td>
						</tr>
					</tbody>
				</table>
				
			<s:url var="urlNewVirtualDialog" action="newVirtualMeasurementsDialog" escapeAmp="false">
				<s:param name="booAnalog" value="%{booAnalog}" />
				<s:param name="booDigital" value="%{booDigital}"/>
			</s:url>

		<sj:dialog id="newVirtualDialog" formIds="frmConfigureVirtualMeasurements" autoOpen="false" modal="true" title="New Virtual Measurement Configuration" width="900" href="%{urlNewVirtualDialog}" closeTopics="closeNewDialogTopic">
		</sj:dialog>
		
		<sj:dialog id="editVirtualDialog" formIds="frmConfigureVirtualMeasurements" closeTopics="closeEditDialogTopic"
		autoOpen="false" modal="true" title="Edit Virtual Measurement Configuration" width="900">
		</sj:dialog>
			
			<s:hidden theme="simple" id="sourceTab" name="sourceTab" value=""></s:hidden>
			<s:hidden theme="simple" id="booAnalog" name="booAnalog"></s:hidden>
			<s:hidden theme="simple" id="booDigital" name="booDigital"></s:hidden>
			<s:hidden theme="simple" id="lineGroupExists" name="lineGroupExists"></s:hidden>
			<s:hidden id="originTab" name="originTab" value="VirtualMeasurements"></s:hidden>
			<s:hidden theme="simple" id="jspFileName" name="jspFileName" value="configureVirtualMeasurements.jsp"></s:hidden>
		</tbody>
	</s:form>
	<img id="indicator" src="images/indicator.gif" alt="Loading..." style="display:none"/>

</body>
</html>
