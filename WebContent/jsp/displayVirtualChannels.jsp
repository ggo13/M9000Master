<%@ include file="header.jsp"%>

<script type="text/javascript" src="js/jquery.validate.js"></script>
<script type="text/javascript" src="js/additional-methods.js"></script>
<script type="text/javascript" src="js/jquery-migrate-1.2.1.min.js"></script>
<script>
	var isDfrAddedOrRemoved = ${stationDetails.dfrAddedOrRemoved};
</script>
<body>
	<script type="text/javascript"
		src="/M9000Master/struts/optiontransferselect.js"></script>
	<script src="js/m9000/displayVirtualChannels.js" type="text/javascript">
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
							onclick="javascript:submitForm('Analogs');">Analogs > </s:a>Virtuals > 
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');">LineGroups</s:a><s:a
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
							onclick="javascript:submitForm('Analogs');">Analogs > </s:a>Virtuals > 
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');">LineGroups</s:a><s:a
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
							onclick="javascript:submitForm('Analogs');">Analogs > </s:a>Virtuals > 
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');">LineGroups</s:a><s:a
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
							onclick="javascript:submitForm('Analogs');">Analogs > </s:a>Virtuals > 
							<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('LineGroups');">LineGroups</s:a><s:a
							cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('Measurements');"> > Measurements</s:a>
						<s:a cssClass="traverseMenu" href="#"
							onclick="javascript:submitForm('VirtualMeasurements');"> > Virtual Measurements</s:a>
					</div>
				</s:else>
			</s:else>
		</s:if>
	</div>
	<s:form id="configureVirtualChannels"
		action="configureVirtualChannels" method="POST" cssStyle="width: 100%">
		<tbody>
		<s:actionerror id="userMsgError" theme="jquery"/>
		<s:actionmessage  id="userMsgInfo" theme="jquery"/>

		<tr style="width:100%" >
			<td width="100%" class="titleLabel"
				style="background-color: #9999CC">Virtual Channels Configuration</td>
		</tr>
		<s:if test="%{lstVirtualChannels != null && lstVirtualChannels.size()>0}">
		<table style="width:100%">
			<tbody>
				<tr id="buttonsTop" style="display: none">
				<td colspan="2" align="center">

					<div id="divEditMode_Top">
						<s:submit theme="simple" cssClass="submit" name="btnBack_Top"
							value="Back" action="virtualChannelsBack" />
						<sj:a openDialog="newVirtualDialog">
							<s:submit theme="simple" cssClass="submit" id="btnNew"
								name="btnNew" value="Create"/>
						</sj:a>
						<s:submit theme="simple"
						align="center" cssClass="submit" name="btnNext_Top" value="Next"
						action="virtualChannelsNext" />
					</div>
				</td>
				</tr>
			</tbody>
			</table>
		</s:if>
		<table id="virtualChannelsTable" class="configureTable" style="width: 100%;align:center">
		<thead>
							<tr style="background-color: #BCBCDC;">
								<th style="width: 10%" align="center">Virtual Channel No</th>
								<th style="width: 35%" align="center">Description</th>
								<th style="width: 10%" align="center">Virtual Logic</th>
								<th style="width: 5%" align="center">Phase</th>
								<th style="width: 10%" align="center">Input Type</th>
								<th style="width: 5%" align="center">Export <s:checkbox
										name="CheckUncheck" id="CheckUncheck" theme="simple"
										onclick="changeState(this.checked);" />
								</th>
								<th style="width: 5%" align="center">Delete</th>
							</tr>
		</thead>
		<tbody id="virtualChannelsBody">
		<s:if test="%{lstVirtualChannels != null && lstVirtualChannels.size()>0}">
					<tr>
					<td>
					<sj:div reloadTopics="updateVirtualTable">
					<s:iterator value="lstVirtualChannels" status="virtual_stat">
						<s:url var="urlEditVirtualDialog#{virtual_stat.index}" action="editVirtualChannelsDialog" escapeAmp="false">
							<s:param name="selectedChannelIndex" value="%{#virtual_stat.index}"/>
							<s:param name="booAnalog" value="%{booAnalog}" />
							<s:param name="booDigital" value="%{booDigital}"/>

						</s:url>
						<s:url var="urlDeleteVirtualChannel#{virtual_stat.index}" action="deleteVirtualChannels">
								<s:param name="selectedChannelIndex" value="%{#virtual_stat.index}"/>
						</s:url>
										<tr style="background-color: #EEEEFF">
											<td width="5%" align="center">
											<s:label
													name="lstVirtualChannels[%{#virtual_stat.index}].virtualChannelNo"
													id="lstVirtualChannels[%{#virtual_stat.index}].virtualChannelNo"
													theme="simple" value="SUM%{virtualChannelNo}" />
													</td>
											<s:hidden theme="simple"
												name="lstVirtualChannels[%{#virtual_stat.index}].virtualChannelNo"></s:hidden>
											<s:hidden theme="simple"
												name="lstVirtualChannels[%{#virtual_stat.index}].channel"></s:hidden>
											<s:hidden theme="simple" name="lstVirtualChannels[%{#virtual_stat.index}].name"></s:hidden>
											<td width="30%" align="center">
													<s:textfield theme="simple" cssStyle="width:95%" 
													name="lstVirtualChannels[%{#virtual_stat.index}].circuitName"
													required="true" size="64" maxlength="64" />
											</td>
											<td width="15%" align="right">
												<s:label cssStyle="width:95%"
														name="lstVirtualChannels[%{#virtual_stat.index}].virtualLogic"
														id="lstVirtualChannels[%{#virtual_stat.index}].virtualLogic"
														theme="simple" value="%{virtualLogic}" title="%{lstVirtualChannels[#virtual_stat.index].virtualLogicToolTip}" />
											<sj:a openDialog="editVirtualDialog" cssStyle="width:10%" title="Edit Virtual Channel Details" href="%{urlEditVirtualDialog#{virtual_stat.index}}" button="true" buttonIconSecondary="ui-icon-pencil"></sj:a>
											</td>
											<td width="5%" align="center">
                                            	<s:select theme="simple" name="lstVirtualChannels[%{#virtual_stat.index}].phase"
                                                          id="lstVirtualChannels[%{#virtual_stat.index}].phase"
                                                          list="{'NA','A', 'B', 'C', 'N'}" value="%{phase}" />
                                            </td>
                                              <td width="11%" align="center"><s:label theme="simple"
                                                              name="lstVirtualChannels[%{#virtual_stat.index}].inputType"
                                                              id="lstVirtualChannels[%{#virtual_stat.index}].inputType"
                                                              value="%{inputType}" /></td>
                                              <s:hidden theme="simple"
                                                              name="lstVirtualChannels[%{#virtual_stat.index}].inputType"></s:hidden>
											<td width="7%" align="center"><s:checkbox
													name="lstVirtualChannels[%{#virtual_stat.index}].exportStatus"
													id="lstVirtualChannels[%{#virtual_stat.index}].exportStatus"
													theme="simple" value="%{exportStatus}"
													onclick="javascript:isAllChecked();" /></td>
											<td width="5%" align="center">
											<s:submit theme="simple" cssClass="submit" id="%{#virtual_stat.index}" name="btnDeleteVirtual"
											onclick="confirmationDialog(this.id,'WARNING: There may be lingroups and Measurements associated with this virtual channel. Are you sure you want to delete?');return false;"
											value="Delete"/>
											<sj:a listenTopics="confirmed%{#virtual_stat.index}" formIds="configureVirtualChannels" href="%{urlDeleteVirtualChannel#{virtual_stat.index}}" targets="virtualChannelsBody"></sj:a>
											</td>
											</tr>
					</s:iterator>
					</sj:div>
					</td>
					</tr>
				</s:if>
				<s:else >
					<tr id="idEmptyRow">
						<td colspan="5" align="center"><s:label theme="simple" 
							value="No virtual channels to display"></s:label></td>
					</tr>
				</s:else>
			</tbody>
			</table>
			<table style="width:100%">
			<tbody>
				<tr id="buttonsBottom" style="display: none">
				<td colspan="2" align="center">

					<div id="divEditMode_Bottom">
						<s:submit theme="simple" cssClass="submit" name="btnBack_Bottom"
							value="Back" action="virtualChannelsBack" />
						<sj:a openDialog="newVirtualDialog">
							<s:submit theme="simple" cssClass="submit" id="btnNew"
								name="btnNew" value="Create"/>
						</sj:a>
						<s:submit theme="simple"
						align="center" cssClass="submit" name="btnNext_Bottom" value="Next"
						action="virtualChannelsNext" />
					</div>
				</td>
				</tr>
			</tbody>
			</table>

		<s:url var="urlCreateVirtualChannel" action="virtualChannelsCreate">
		</s:url>

		<s:url var="urlNewVirtualDialog" action="newVirtualChannelsDialog" escapeAmp="false">
			<s:param name="booAnalog" value="%{booAnalog}" />
			<s:param name="booDigital" value="%{booDigital}"/>
		</s:url>

		<sj:dialog id="newVirtualDialog" formIds="configureVirtualChannels" autoOpen="false" modal="true" title="New Virtual Channel Configuration" width="900" href="%{urlNewVirtualDialog}" closeTopics="closeNewDialogTopic">
		</sj:dialog>
		
		<sj:dialog id="editVirtualDialog" formIds="configureVirtualChannels" closeTopics="closeEditDialogTopic"
		autoOpen="false" modal="true" title="Edit Virtual Channel Configuration" width="900">
		</sj:dialog>

		<s:hidden id="sourceTab" name="sourceTab" value=""></s:hidden>
		<s:hidden id="originTab" name="originTab" value=""></s:hidden>
		<s:hidden id="booAnalog" name="booAnalog"></s:hidden>
		<s:hidden id="booDigital" name="booDigital"></s:hidden>
		<s:hidden id="virtualChannelsExists" name="virtualChannelsExists"></s:hidden>
		<s:hidden id="jspFileName" name="jspFileName" value="displayVirtualChannels.jsp"></s:hidden>
		<s:hidden id="originTab" name="originTab" value="Virtuals"></s:hidden>
		<s:hidden id="transducerModified" name="transducerModified"></s:hidden>
	</tbody>
	</s:form>
	
</body>
</html>
