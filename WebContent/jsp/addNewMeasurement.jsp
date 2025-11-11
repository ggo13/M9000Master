<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<s:iterator value="lstNewMeasurements" status="trigger_stat">
		<tr id="tr_<s:property value='%{#trigger_stat.index+totalMeasurementsCount}'/>" style="background-color: #EEEEFF">
			<td id="td_globalSelect_<s:property value='%{#trigger_stat.index+totalMeasurementsCount}'/>" align="center" style="background-color:lightblue;display:none;"><s:checkbox
					name="globalSelect_%{#trigger_stat.index+totalMeasurementsCount}"
					id="globalSelect_%{#trigger_stat.index+totalMeasurementsCount}"
					style="background-color:lightblue;display:none;" theme="simple" value="" /><a href="javascript:void(0)" onclick="deleteMeasurement({
                            id: '<s:property value="%{id}"/>',
                            type: '<s:property value="%{type}"/>',
                            measurementName: '<s:property value="%{id}"/>-<s:property value="%{inputChannelName}"/>',
                            chassis: '<s:property value="%{chassis}"/>'
                        },'<s:property value="%{#trigger_stat.index+totalMeasurementsCount}"/>')">
                            <span class="ui-icon ui-icon-trash"></span>
                        </a></td>

			<td align="center"><s:checkbox
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].status"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].status"
					theme="simple" value="%{lstNewMeasurements[#trigger_stat.index].status}"
					onchange="return changeState(this, this.checked);" /></td>
			<s:if test="%{accessMode == 'Edit'}">
				<td align="center"><s:label
						name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].id"
						id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].id"
						theme="simple" value="%{lstNewMeasurements[#trigger_stat.index].id}" /> </td>
			</s:if>
			<s:else>
				<td align="center" style="display:none;"><s:label
						name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].id"
						id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].id"
						theme="simple" value="%{id}" /></td>
			</s:else>
				<s:hidden theme="simple" name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].id" value="%{lstNewMeasurements[#trigger_stat.index].id}"></s:hidden>
			<td align="center">
				<s:select
					cssStyle="font-family:courier, courier new, serif;"
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].channel"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].channel"
					list="lstTriggerSourceDto" listKey="sourceValue"
					listValue="sourceDisplayName" theme="simple"
					value="%{lstNewMeasurements[#trigger_stat.index].channel}"
					onchange="javascript:populate_trigger_name(this, this.value);$(this).publish('reloadTriggerTypes_%{#trigger_stat.index+totalMeasurementsCount}',this, event);"
					disabled="%{!lstNewMeasurements[#trigger_stat.index].status}" />
			</td>
			<td align="center"><s:fielderror>
					<s:param>lstTriggerChannels[<s:property
							value="%{#trigger_stat.index+totalMeasurementsCount}" />].name</s:param>
				</s:fielderror> <s:textfield theme="simple"
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].name"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].name" size="64"
					maxlength="64" value="%{lstNewMeasurements[#trigger_stat.index].name}"
					disabled="%{!lstNewMeasurements[#trigger_stat.index].status}" /></td>
			<s:hidden theme="simple" 
				name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].phase" value="%{lstNewMeasurements[#trigger_stat.index].phase}"></s:hidden>
			<s:property
				value="Trigger_Type_triggerType[%{#trigger_stat.index+totalMeasurementsCount}]" />
			<td align="center">
					<s:url var="triggerTypeUrl" action="populateTriggerTypes">
						<s:param name="reqdIndex" value="%{#trigger_stat.index+totalMeasurementsCount}"></s:param>
					</s:url> 
			<sj:div indicator="indicator"
					errorText="Loading failed. Try refresh."
					id="triggerType%{#trigger_stat.index+totalMeasurementsCount}"
					href="%{#triggerTypeUrl}"
					listenTopics="reloadTriggerTypes_%{#trigger_stat.index+totalMeasurementsCount}"
					deferredLoading="true" formIds="triggerChannels" >
						<s:select theme="simple"
							name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].type"
							id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].type"
							list="%{lstNewMeasurements[#trigger_stat.index].mapTriggerInputTypes}"
							onchange="javascript:update_access(this.name, this.value);update_trigger_name(this, this.value);return false;"
							value="%{lstNewMeasurements[#trigger_stat.index].type}"
							disabled="%{!lstNewMeasurements[#trigger_stat.index].status}" />
						<s:hidden theme="simple" 
							name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].chassis" value="%{lstNewMeasurements[#trigger_stat.index].chassis}"></s:hidden>
						<s:hidden theme="simple" 
							name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].inputChannelName" value="%{lstNewMeasurements[#trigger_stat.index].inputChannelName}"></s:hidden>
						<s:hidden theme="simple" 
							name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].inputType" value="%{lstNewMeasurements[#trigger_stat.index].inputType}"></s:hidden>

				</sj:div>
				</td>
			<td align="center"><s:fielderror>
					<s:param>lstTriggerChannels[<s:property
							value="%{#trigger_stat.index+totalMeasurementsCount}" />].average</s:param>
				</s:fielderror> <s:textfield theme="simple"
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].average"
					id="lstNewMeasurements[#trigger_stat.index].average" size="3"
					value="%{lstNewMeasurements[#trigger_stat.index].average}"
					disabled="%{!lstNewMeasurements[#trigger_stat.index].status}" /></td>
			<td align="center"><s:fielderror>
					<s:param>lstTriggerChannels[<s:property
							value="%{#trigger_stat.index+totalMeasurementsCount}" />].harmonic</s:param>
				</s:fielderror> <s:textfield theme="simple"
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].harmonic"
					id="lstNewMeasurements[#trigger_stat.index].harmonic" size="3"
					value="%{lstNewMeasurements[#trigger_stat.index].harmonic}"
					disabled="%{lstNewMeasurements[#trigger_stat.index].disableHarmonic}" /></td>
			<td align="center"><s:checkbox
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].pmuStatus"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].pmuStatus"
					theme="simple" value="%{lstNewMeasurements[#trigger_stat.index].pmuStatus}"
					onchange="return changePmuState(this, this.checked);"
					disabled="%{!lstNewMeasurements[#trigger_stat.index].status}" /></td>
			<td align="center"><s:checkbox
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].freqPmuStatus"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].freqPmuStatus"
					theme="simple" value="%{lstNewMeasurements[#trigger_stat.index].freqPmuStatus}"
					disabled="%{!lstNewMeasurements[#trigger_stat.index].status || !lstNewMeasurements[#trigger_stat.index].pmuStatus || lstNewMeasurements[#trigger_stat.index].type != 'Frequency'}" /></td>
			<!--  td align="center">
			<s:hidden theme="simple" 
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].freqPmuStatus"></s:hidden>
			<s:if test="%{lstNewMeasurements[#trigger_stat.index].freqPmuStatus}">
				<s:radio theme="simple" id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].freqPmuId" name="freqPmuId" list="{#trigger_stat.index}" listValue="%{''}" value="%{#trigger_stat.index}" disabled="%{!lstNewMeasurements[#trigger_stat.index].status || !lstNewMeasurements[#trigger_stat.index].pmuStatus || lstNewMeasurements[#trigger_stat.index].type != 'Frequency'}"/>
			</s:if>
			<s:else>
				<s:radio theme="simple" id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].freqPmuId" name="freqPmuId" value="" list="{#trigger_stat.index}" listValue="%{''}" disabled="%{!lstNewMeasurements[#trigger_stat.index].status || !lstNewMeasurements[#trigger_stat.index].pmuStatus || lstNewMeasurements[#trigger_stat.index].type != 'Frequency'}"/>
			</s:else>
			</td -->
			<td align="center"><s:select theme="simple"
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].start"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].start"
					list="{'Never', 'ROC', 'Over', 'Under', 'Both'}" value="%{lstNewMeasurements[#trigger_stat.index].start}"
					onchange="javascript:set_trigger(this, this.value);"
					disabled="%{!lstNewMeasurements[#trigger_stat.index].status || (lstNewMeasurements[#trigger_stat.index].type == 'Phasor')}" /></td>
			<td align="center"><s:fielderror>
					<s:param>lstTriggerChannels[<s:property
							value="%{#trigger_stat.index+totalMeasurementsCount}" />].tripIfOver</s:param>
				</s:fielderror> <s:textfield theme="simple"
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].tripIfOver"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].tripIfOver"
					size="3" value="%{lstNewMeasurements[#trigger_stat.index].tripIfOver}"
					disabled="%{(!lstNewMeasurements[#trigger_stat.index].status || lstNewMeasurements[#trigger_stat.index].disableTripOver)}" /></td>
			<td align="center"><s:fielderror>
					<s:param>lstTriggerChannels[<s:property
							value="%{#trigger_stat.index+totalMeasurementsCount}" />].tripIfUnder</s:param>
				</s:fielderror> <s:textfield theme="simple"
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].tripIfUnder"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].tripIfUnder"
					size="3" value="%{lstNewMeasurements[#trigger_stat.index].tripIfUnder}"
					disabled="%{(!lstNewMeasurements[#trigger_stat.index].status || lstNewMeasurements[#trigger_stat.index].disableTripUnder)}" /></td>
			<td align="center"><s:fielderror>
					<s:param>lstTriggerChannels[<s:property
							value="%{#trigger_stat.index+totalMeasurementsCount}" />].chatterLimit</s:param>
				</s:fielderror> <s:textfield theme="simple"
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].chatterLimit"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].chatterLimit"
					size="3" value="%{lstNewMeasurements[#trigger_stat.index].chatterLimit}"
					disabled="%{(!lstNewMeasurements[#trigger_stat.index].status || !lstNewMeasurements[#trigger_stat.index].triggerStatus)}" /></td>
			<td align="center"><s:fielderror>
					<s:param>lstTriggerChannels[<s:property
							value="%{#trigger_stat.index+totalMeasurementsCount}" />].chatterRate</s:param>
				</s:fielderror> <s:textfield theme="simple"
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].chatterRate"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].chatterRate"
					size="3" value="%{lstNewMeasurements[#trigger_stat.index].chatterRate}"
					disabled="%{(!lstNewMeasurements[#trigger_stat.index].status || !lstNewMeasurements[#trigger_stat.index].triggerStatus)}" /></td>
			<td align="center"><s:fielderror>
					<s:param>lstTriggerChannels[<s:property
							value="%{#trigger_stat.index+totalMeasurementsCount}" />].triggerLimit</s:param>
				</s:fielderror> <s:textfield theme="simple"
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].triggerLimit"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].triggerLimit"
					size="3" value="%{lstNewMeasurements[#trigger_stat.index].triggerLimit}"
					disabled="%{(!lstNewMeasurements[#trigger_stat.index].status || !lstNewMeasurements[#trigger_stat.index].triggerStatus)}" /></td>
			<td align="center"><s:fielderror>
					<s:param>lstTriggerChannels[<s:property
							value="%{#trigger_stat.index+totalMeasurementsCount}" />].duration</s:param>
				</s:fielderror><s:textfield theme="simple"
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].duration"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].duration"
					size="3" value="%{lstNewMeasurements[#trigger_stat.index].duration}" disabled="%{(!lstNewMeasurements[#trigger_stat.index].status || lstNewMeasurements[#trigger_stat.index].disableDuration)}"/></td>
			<td align="center"><s:fielderror>
					<s:param>lstTriggerChannels[<s:property
							value="%{#trigger_stat.index+totalMeasurementsCount}" />].tripRocNeg</s:param>
				</s:fielderror><s:textfield theme="simple"
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].tripRocNeg"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].tripRocNeg"
					size="3" value="%{lstNewMeasurements[#trigger_stat.index].tripRocNeg}" disabled="%{(!lstNewMeasurements[#trigger_stat.index].status || lstNewMeasurements[#trigger_stat.index].disableTripRoc)}"/></td>
			<td align="center"><s:fielderror>
					<s:param>lstTriggerChannels[<s:property
							value="%{#trigger_stat.index+totalMeasurementsCount}" />].tripRocPos</s:param>
				</s:fielderror><s:textfield theme="simple"
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].tripRocPos"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].tripRocPos"
					size="3" value="%{lstNewMeasurements[#trigger_stat.index].tripRocPos}" disabled="%{(!lstNewMeasurements[#trigger_stat.index].status || lstNewMeasurements[#trigger_stat.index].disableTripRoc)}"/></td>
			<td align="center"><s:checkbox
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].exportStatus"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].exportStatus"
					theme="simple" value="%{lstNewMeasurements[#trigger_stat.index].exportStatus}"
					onchange="return changeExportState(this, this.checked);"
					disabled="%{!lstNewMeasurements[#trigger_stat.index].status || lstNewMeasurements[#trigger_stat.index].type == 'Rms'}" /></td>
			<td align="center"><s:checkbox
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].ddrStatus"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].ddrStatus"
					theme="simple" value="%{lstNewMeasurements[#trigger_stat.index].ddrStatus}"
					onchange="return changeDdrState(this, this.checked);"
					disabled="%{!lstNewMeasurements[#trigger_stat.index].status || lstNewMeasurements[#trigger_stat.index].type == 'Rms' || !lstNewMeasurements[#trigger_stat.index].exportStatus}" /></td>
			<td align="center"><s:checkbox
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].disturbanceAlarm"
					id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].disturbanceAlarm"
					theme="simple" value="%{disturbanceAlarm}"
					onchange="return isAllDAlarmChecked();"
					disabled="%{!lstNewMeasurements[#trigger_stat.index].status || lstNewMeasurements[#trigger_stat.index].start == 'Never'}" /></td>
			<!--<td align="center"><s:fielderror><s:param>lstNewMeasurements[<s:property value="%{#trigger_stat.index}"/>].exportRate</s:param></s:fielderror><s:textfield theme="simple" name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].exportRate" id="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].exportRate" size="3" value="%{exportRate}" disabled="%{(!lstNewMeasurements[#trigger_stat.index].status || !lstNewMeasurements[#trigger_stat.index].exportStatus || lstNewMeasurements[#trigger_stat.index].pmuStatus)}"/></td>-->
			<s:if
				test="%{lstNewMeasurements[%{#trigger_stat.index}].globalLineGroupId != null}">
				<s:hidden theme="simple" 
					name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].globalLineGroupId" value="%{lstNewMeasurements[#trigger_stat.index].globalLineGroupId}"></s:hidden>
			</s:if>
			
			<s:hidden theme="simple" 
				name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].disableHarmonic" value="%{disableHarmonic}"></s:hidden>
			<s:hidden theme="simple" 
				name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].triggerStatus" value="%{triggerStatus}"></s:hidden>
			<s:hidden theme="simple" 
				name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].disableTripOver" value="%{disableTripOver}"></s:hidden>
			<s:hidden theme="simple" 
				name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].disableTripUnder" value="%{disableTripUnder}"></s:hidden>
			<s:hidden theme="simple" 
				name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].disableTripRoc" value="%{disableTripRoc}"></s:hidden>
			<s:hidden theme="simple" 
				name="lstTriggerChannels[%{#trigger_stat.index+totalMeasurementsCount}].disableDuration" value="%{disableDuration}"></s:hidden>
		</tr>
</s:iterator>