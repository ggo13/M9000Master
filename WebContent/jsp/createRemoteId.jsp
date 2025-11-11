<%@ include file="header.jsp"%>
<body>
<script>
var accessMode = "<s:property value='accessMode'/>";
var isPmuEnabled = "<s:property value='stationDetails.pmuEnabled'/>";
var pdcStreamType = "<s:property value='stationDetails.pdcStreamType'/>";
var isDfrAddedOrRemoved = ${stationDetails.dfrAddedOrRemoved};
var isWettingVoltageMonitored = ${stationDetails.wettingVoltageMonitor};
</script>
<script src="js/m9000/createRemoteId.js">
	</script>
<div id="navMenu" style="display:none">
	<s:if test="%{accessMode == 'Edit'}">
		<s:if test="%{booAnalog}">
			<s:if
				test="%{lineGroupExists ||(lstLineGroups != null && lstLineGroups.size()>0)}">
				<s:if test="%{booDigital}">
					<div style="text-align:left;font-weight:bold;">
							StationDetails <s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Analogs');"> > Analogs</s:a>
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
							StationDetails <s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Analogs');"> > Analogs</s:a>
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');"> > Virtuals </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');"> > LineGroups </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');"> > Measurements</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> > Virtual Measurements</s:a>						
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('FaultLocation');"> > Fault Location</s:a>
					</div>
				</s:else>
			</s:if>
			<s:else>
				<s:if test="%{booDigital}">
					<div style="text-align:left;font-weight:bold;">
							StationDetails <s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Analogs');"> > Analogs</s:a>
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
				</s:if>
				<s:else>
					<div style="text-align:left;font-weight:bold;">
							StationDetails <s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Analogs');"> > Analogs</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualChannels');"> > Virtuals </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');"> > LineGroups </s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');"> > Measurements</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> > Virtual Measurements</s:a>
					</div>
				</s:else>
			</s:else>
		</s:if>
		<s:else>
			<div style="text-align:left;font-weight:bold;">
							StationDetails <s:a cssClass="traverseMenu" href="#"
					onclick="javascript:submitForm('Events');"> > Events</s:a>
			</div>
		</s:else>
	</s:if>
	</div>
	<s:form id="createStation" name="createStation" action="createRemoteId"
		method="POST" 
		cssStyle="width:80%">

		<tr>
			<s:if test="%{accessMode == 'Edit'}">
				<td colspan="2" class="titleLabel" style="background-color: #9999CC">Edit
					Station Details</td>
			</s:if>
			<s:else>
				<td colspan="2" class="titleLabel" style="background-color: #9999CC">New
					Station Details</td>
			</s:else>
		</tr>
		<tr id ="tblButtonsTop" style="display:none">
			<td colspan="2" align="center"><s:submit theme="simple"
					cssClass="submit" name="Back" key="label.back" action="backFromStationDetails" onclick="$('#createStation').attr('novalidate','novalidate')"/> <s:if
					test="%{accessMode == 'Edit'}">
					<s:submit theme="simple" cssClass="submit" name="btnSubmit"
						key="label.next" />
				</s:if> <s:else>
					<s:submit theme="simple" cssClass="submit" name="btnSubmit"
						key="label.create" />
				</s:else></td>
		</tr>
		<tr>
			<td colspan="2"><s:actionerror id="userMsgError" theme="jquery"/></td>
		</tr>
		<s:if test="%{accessMode == 'Edit'}">
			<s:textfield id="stationDetails.systemStationId"
				name="stationDetails.systemStationId" key="label.remoteid"
				value="%{stationDetails.systemStationId}" readonly="true" />
			<s:textfield id="stationDetails.systemStationName"
				name="stationDetails.systemStationName" required="true" size="30" requiredLabel="true"
				key="label.remotename" value="%{stationDetails.systemStationName}" />
			<s:textfield id="stationDetails.totalDfrsConfigured"
				name="stationDetails.totalDfrsConfigured" key="label.total.dfrs"
				value="%{stationDetails.totalDfrsConfigured}" readonly="true" />
			<s:textfield id="stationDetails.systemAnalogChannelsCount"
				name="stationDetails.systemAnalogChannelsCount"
				key="label.total.analog"
				value="%{stationDetails.systemAnalogChannelsCount}" disabled="true" />
			<s:textfield id="stationDetails.systemDigitalChannelsCount"
				name="stationDetails.systemDigitalChannelsCount"
				key="label.total.digital"
				value="%{stationDetails.systemDigitalChannelsCount}" disabled="true" />
			<s:hidden name="stationDetails.systemAnalogChannelsCount"
				value="%{stationDetails.systemAnalogChannelsCount}"></s:hidden>
			<s:hidden name="stationDetails.systemDigitalChannelsCount"
				value="%{stationDetails.systemDigitalChannelsCount}"></s:hidden>
			<s:hidden name="stationDetails.configStatus"
				value="%{stationDetails.configStatus}"></s:hidden>
		</s:if>
		<s:else>
			<s:textfield id="stationDetails.systemStationId"
				name="stationDetails.systemStationId" required="required" requiredLabel="true"
				key="label.remoteid" value="%{stationDetails.systemStationId}" />
			<s:textfield id="stationDetails.systemStationName"
				name="stationDetails.systemStationName" required="true" requiredLabel="true" size="30"
				key="label.remotename" value="%{stationDetails.systemStationName}" />
			<s:textfield id="stationDetails.systemAnalogChannelsCount"
				name="stationDetails.systemAnalogChannelsCount" required="true" requiredLabel="true"
				key="label.total.analog"
				value="%{stationDetails.systemAnalogChannelsCount}" />
			<s:textfield id="stationDetails.systemDigitalChannelsCount"
				name="stationDetails.systemDigitalChannelsCount" required="true" requiredLabel="true"
				key="label.total.digital"
				value="%{stationDetails.systemDigitalChannelsCount}" />
		</s:else>
		<s:if test="%{#session.HIERARCHY_INFO != null}">
			<s:url var="urlFetchLeafZone" action="fetchLeafZone" />
			<sj:select href="%{urlFetchLeafZone}"
				id="stationDetails.parentZoneId"
				name="stationDetails.parentZoneId"
				key="label.system.parentZoneId" 
				list="lstLeafZones" listKey="key"
				listValue="value" value="%{stationDetails.parentZoneId}" />
		</s:if>
		<s:url var="urlLineFreq" action="fetchLineFreq" />
		<s:url var="urlSampleRate" action="fetchSampleRate" />
		<s:url var="urlLtrSampleRate" action="fetchLtrSampleRate" />
		<div id="line_freq_sample_rates">
			<sj:select href="%{urlLineFreq}"
				id="stationDetails.systemLineFrequency"
				name="stationDetails.systemLineFrequency"
				key="label.system.linefrequency" onSuccessTopics="sampleRateList,globalExportRateList"
				onChangeTopics="sampleRateList,globalExportRateList" list="lineFreqObjList" listKey="key"
				listValue="value" value="%{stationDetails.systemLineFrequency}" />
			<sj:select href="%{urlSampleRate}"
				id="stationDetails.systemSampleRate"
				name="stationDetails.systemSampleRate" 
				key="label.system.samplerate" deferredLoading="true"
				reloadTopics="sampleRateList" list="lstSampleRate" 
				onCompleteTopics="ltrSampleRateList,debounceList,pmuDataRateList" 
				onChangeTopics="ltrSampleRateList,debounceList,pmuDataRateList"
				value="%{stationDetails.systemSampleRate}" />
			<sj:select href="%{urlLtrSampleRate}"
				id="stationDetails.systemLongTermSampleRate"
				name="stationDetails.systemLongTermSampleRate"
				key="label.system.ltsamplerate" deferredLoading="true"
				list="lstLtSampleRate" reloadTopics="ltrSampleRateList"
				value="%{stationDetails.systemLongTermSampleRate}" />
		</div>
		<!--  s:textfield name="stationDetails.ipAddress" required="true" key="label.system.ipaddress" value="%{stationDetails.ipAddress}"/> -->
		<s:url var="urlExportRate" action="fetchGlobalExportRate" />
		<sj:select href="%{urlExportRate}" id="stationDetails.exportRate"
			name="stationDetails.exportRate" 
			key="label.system.exportRate" list="lstExportRate" deferredLoading="true"
			reloadTopics="globalExportRateList" onCompleteTopics="checkAccess" value="%{stationDetails.exportRate}" />
		<s:textfield name="stationDetails.systemRecordingDeviceId"
			required="true" key="label.system.recordingdevid"
			value="%{stationDetails.systemRecordingDeviceId}" />
		<!--s:textfield name="stationDetails.systemRecordingDeviceName" required="true" key="label.system.recordingdevname" value="%{stationDetails.systemRecordingDeviceName}"/-->
		<s:textfield name="stationDetails.systemPrefaultTime" required="true" requiredLabel="true"
			key="label.system.prefaulttime" maxlength="4"
			value="%{stationDetails.systemPrefaultTime}" />
		<s:textfield name="stationDetails.systemPostfaultTime" required="true" requiredLabel="true"
			key="label.system.postfaulttime" maxlength="4"
			value="%{stationDetails.systemPostfaultTime}" />
		<s:textfield name="stationDetails.systemLtrPrefaultTime"
			required="true" requiredLabel="true" key="label.system.ltrprefaulttime" maxlength="4"
			value="%{stationDetails.systemLtrPrefaultTime}" />
		<s:textfield name="stationDetails.systemLtrPostfaultTime"
			required="true" requiredLabel="true" key="label.system.ltrpostfaulttime" maxlength="4"
			value="%{stationDetails.systemLtrPostfaultTime}" />
		<s:textfield name="stationDetails.triggerLimit" required="true" requiredLabel="true"
			key="label.system.triggerlimit"
			value="%{stationDetails.triggerLimit}" />
		<s:textfield name="stationDetails.chatterLimit" required="true" requiredLabel="true"
			key="label.system.chatterlimit"
			value="%{stationDetails.chatterLimit}" />
		<s:textfield name="stationDetails.chatterRate" required="true" requiredLabel="true"
			key="label.system.chatterrate" value="%{stationDetails.chatterRate}" />
		<s:textfield name="stationDetails.signalTooLowFactor" required="true" requiredLabel="true"
			key="label.system.signalTooLowFactor" value="%{stationDetails.signalTooLowFactor}" tooltip="Percentage of full-scale signal required to enable phase and frequency measurements."/>
		<s:url var="urlEventDebounce" action="fetchEventDebounce" />
		<sj:select href="%{urlEventDebounce}" id="stationDetails.eventDebounce"
			name="stationDetails.eventDebounce" 
			key="label.system.eventDebounce" list="lstEventDebounce" deferredLoading="true"
			reloadTopics="debounceList" value="%{stationDetails.eventDebounce}" />
		<s:select name="stationDetails.wettingVoltageMonitor" key="label.system.wettingVoltageMonitor" 
			list="#{'0':'Disable','1':'Enable'}" value="%{stationDetails.wettingVoltageMonitor}" tooltipDelay="10000" tooltip="If enabled, it will cause an alarm whenever event wetting voltage connected to event power on the CPU is not present."/>
		<s:select name="stationDetails.ps" key="label.system.ps"
			list="{'S','P'}" value="%{stationDetails.ps}" />

		<s:checkbox name="stationDetails.pmuEnabled"
			id="stationDetails.pmuEnabled" label="Enable PMU"
			onclick="javascript:updatePmu(this.checked,'%{stationDetails.pdcStreamType}');"
			value="%{stationDetails.pmuEnabled}" />
		<tr id="pmuRowDiv" align="right" style="width:100%">
			<td colspan="2" align="right" width="100%">
				<fieldset style="text-align: center;">
					<legend>
						PMU Settings
					</legend>
					<table class="configureTable" 
						style="width: 100%; border-width: 0px;align:right">
						<s:textfield id="stationDetails.pdcId"
							name="stationDetails.pdcId" 
							key="label.pmu.id" value="%{stationDetails.pdcId}" requiredLabel="true"/>
						<s:select id="stationDetails.pdcStreamType" name="stationDetails.pdcStreamType"
							key="label.pmu.pmuStreamType" list="{'udp','tcp'}"
							value="%{stationDetails.pdcStreamType}"
							onchange="javascript:updateProtocol(this.value)"/>
						<s:url var="urlPmuDataRate" action="fetchPmuDataRate" />
						<sj:select href="%{urlPmuDataRate}"
							id="stationDetails.pmuDataRate" name="stationDetails.pmuDataRate"
							 key="label.pmu.dataRate" deferredLoading="true"
							list="lstPmuDataRate" reloadTopics="pmuDataRateList"
							value="%{stationDetails.pmuDataRate}" />
						<s:textfield id="stationDetails.pdcMaxWait"
							name="stationDetails.pdcMaxWait" 
							key="label.pmu.maxwait" value="%{stationDetails.pdcMaxWait}" requiredLabel="true"/>
						<s:textfield id="tcpPort" name="stationDetails.pdcTcpPort"
							key="label.pmu.pmuPort"
							value="%{stationDetails.pdcTcpPort}" requiredLabel="true"/>
						<s:textfield id="lstOfUDPserverPorts_0"
							name="stationDetails.lstOfUDPserverPorts[0]" 
							label="UDP Port 1"
							value="%{stationDetails.lstOfUDPserverPorts[0]}" requiredLabel="true"/>
						<s:textfield id="lstOfUDPserverPorts_1"
							name="stationDetails.lstOfUDPserverPorts[1]" 
							label="UDP Port 2"
							value="%{stationDetails.lstOfUDPserverPorts[1]}" />
						<s:textfield id="lstOfUDPserverPorts_2"
							name="stationDetails.lstOfUDPserverPorts[2]" 
							label="UDP Port 3"
							value="%{stationDetails.lstOfUDPserverPorts[2]}" />
						<s:textfield id="lstOfUDPserverPorts_3"
							name="stationDetails.lstOfUDPserverPorts[3]" 
							label="UDP Port 4"
							value="%{stationDetails.lstOfUDPserverPorts[3]}" />
						<s:textfield id="lstOfUDPserverPorts_4"
							name="stationDetails.lstOfUDPserverPorts[4]" 
							label="UDP Port 5"
							value="%{stationDetails.lstOfUDPserverPorts[4]}" />
					</table>
				</fieldset>
			</td>
		</tr>
		<tr id ="tblButtonsBottom" style="display:none">
			<td colspan="2" align="center"><s:submit theme="simple"
					cssClass="submit" name="Back" key="label.back" action="backFromStationDetails" onclick="$('#createStation').attr('novalidate','novalidate')"/> <s:if
					test="%{accessMode == 'Edit'}">
					<s:submit theme="simple" cssClass="submit" name="btnSubmit"
						key="label.next" />
				</s:if> <s:else>
					<s:submit theme="simple" cssClass="submit" name="btnSubmit"
						key="label.create" />
				</s:else></td>
		</tr>
		<s:hidden id="sourceTab" name="sourceTab" value=""></s:hidden>
		<s:hidden id="booAnalog" name="booAnalog"></s:hidden>
		<s:hidden id="booDigital" name="booDigital"></s:hidden>
		<s:hidden id="lineGroupExists" name="lineGroupExists"></s:hidden>
		<s:hidden name="stationDetails.pmuId"></s:hidden>
		<s:hidden name="stationDetails.pmuPort"></s:hidden>
		<s:hidden name="stationDetails.pmuUdpPort"></s:hidden>
		<s:hidden name="stationDetails.pmuStreamType"></s:hidden>
		<s:hidden name="stationDetails.pmuPhasorMode"></s:hidden>
		<s:hidden name="stationDetails.commandServerPort"></s:hidden>
		<s:hidden id="originTab" name="originTab" value="StationDetails"></s:hidden>
		<s:hidden id="jspFileName" name="jspFileName" value="createRemoteId.jsp"></s:hidden>
	</s:form>
</body>
</html>
