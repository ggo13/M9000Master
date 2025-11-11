<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
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
								<s:textfield size="30" cssStyle="font-size: 10px;" readonly="true"
									name="lstVirtualMeasurements[%{#virtual_stat.index}].virtualLogic"
									id="lstVirtualMeasurements[%{#virtual_stat.index}].virtualLogic"
									theme="simple" value="%{virtualLogic}" title="%{virtual}" />
								<sj:a openDialog="editVirtualDialog" cssStyle="width:10%" title="Edit Virtual Measurement Details" href="%{urlEditVirtualDialog#{virtual_stat.index}}" button="true" buttonIconSecondary="ui-icon-pencil"></sj:a>
						
						</td>
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
