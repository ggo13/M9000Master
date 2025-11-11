<%@ include file="header.jsp"%>
<script type="text/javascript" src="js/jquery.validate.js"></script>
<script type="text/javascript" src="js/additional-methods.js"></script>
<script type="text/javascript" src="js/jquery-migrate-1.2.1.min.js"></script>
<style type="text/css">
tr.spaceUnder>td {
  padding-bottom: 1em;
}
</style>
<script   type="text/javascript">
var totalDfrsConfigured = ${stationDetails.totalDfrsConfigured};
var isDfrAddedOrRemoved = ${stationDetails.dfrAddedOrRemoved};
var totalDfrsAvailable = "<s:property value='%{lstDfrDTO}'/>";
console.log("totalDfrsAvaialble "+totalDfrsAvailable);
if (typeof totalDfrsAvailable !== 'undefined')
	{
		totalDfrsAvailable = "<s:property value='%{lstDfrDTO.size()}'/>";//fn:length(lstDfrDTO);
	}
</script>
<script src="js/m9000/displayDfrs.js"  type="text/javascript"></script>
<body>

<s:if test="actionMessages!=null && actionMessages.size > 0">
    <script>
        var actionMessages="WARNING: ";
        <s:iterator value="actionMessages" >
            // Iterate the messages, and build the JS String
            actionMessages += '<s:property />' + '\n';
        </s:iterator>
        alert (actionMessages);
    </script>
</s:if>
 
		<s:actionmessage  id="userMsgInfo" theme="jquery"/>
		<s:actionerror id="userMsgError" theme="jquery"/>
	<s:form action="configureChannels" method="POST" cssStyle="border-width: 0px;background-color:white;">
	<tr>
	<td align="center" style="background-color: white;">
				<s:if
						test="%{accessMode != 'Edit'}">
						<s:submit theme="simple" cssClass="submit" type="button" name="btnBackTop"
							value="Back" action="backToConfigure"></s:submit>
						<s:submit theme="simple" cssClass="submit" name="btnConfigureTop"
							value="Configure" action="displayDfrNext"></s:submit>
					</s:if> <s:else>
						<s:submit theme="simple" cssClass="submit" type="button" name="btnBackTop"
							value="Back" action="backToStationList"></s:submit>
						<s:if
							test="%{stationDetails.configStatus == 'COMPLETE' && #session.userDetails.role != 'guest'}">
						<s:submit theme="simple" cssClass="submit" id="btnConfigureTop" name="btnConfigureTop"
							value="Edit Configuration" action="editStationDetails"></s:submit>
						<s:submit theme="simple" cssClass="submit" id="btnAdvancedSettingsTop" name="btnAdvancedSettingsTop"
							value="Advanced Settings" action="editAdvancedSettings" onclick="javascript:showBusyCursor();return true;"></s:submit>
							<s:submit theme="simple" cssClass="submit" id="btnSendTop" name="btnSendTop"
								value="Send"
								onclick="return confirm('Do you want to send the configuration?')"
								action="sendConfiguration"></s:submit>
						</s:if>
						<s:else>
						<s:submit theme="simple" cssClass="submit" name="btnConfigureTop"
							value="View Configuration"  
							action="editStationDetails"></s:submit>
							<s:submit theme="simple" cssClass="submit" id="btnAdvancedSettingsTop" name="btnAdvancedSettingsTop"
							value="Advanced Settings" action="editAdvancedSettings" onclick="javascript:showBusyCursor();return true;"></s:submit>
							<s:submit theme="simple" cssClass="submit" name="btnSendTop"
								value="Send" disabled="true"></s:submit>
						</s:else>
					</s:else>
		</td>
		</tr>
		<tr id="divDfrsList" class="spaceUnder">
	<td align="right" style="background-color: white;">
	<s:iterator value="lstDfrDTO" status="lstDfrDTO_stat">
	
	<s:if test="%{accessMode != 'Edit' || !lstDfrDTO[#lstDfrDTO_stat.index].newlyAdded}">
		<table class="inlineTable">
			<tr>
				<s:if test="%{accessMode != 'Edit'}">
					<td colspan="2" align="center"
						style="font-weight: bold; background-color: #9999CC">Chassis
						<s:property value="%{#lstDfrDTO_stat.index}+1" />
					</td>
				</s:if>
				<s:else>
					<td colspan="2" align="center"
						style="font-weight: bold; background-color: #9999CC"><s:property
							value="%{lstDfrDTO[#lstDfrDTO_stat.index].dfrName}" /></td>
				</s:else>
			</tr>
			<tr>
				<td><img
					src="images/dfr<s:property value="%{chnlConfigDropDownList.selectedValue}"/>.jpg"
					alt="Chassis<s:property value="%{#lstDfrDTO_stat.index}"/>" /></td>
				<td>
					<table class="wwFormTable">
						<s:label name="lstDfrDTO[%{#lstDfrDTO_stat.index}].dfrName"
							id="DFRId%{#lstDfrDTO_stat.index}" label="Id" value="%{dfrName}" />
						<s:hidden name="selectedDfr"
							value="%{lstDfrDTO[#lstDfrDTO_stat.index].dfrName}"></s:hidden>
							<s:hidden theme="simple" name="lstDfrDTO[%{#lstDfrDTO_stat.index}].newlyAdded" id="lstDfrDTO[%{#lstDfrDTO_stat.index}].newlyAdded"
                                                        value="%{newlyAdded}"></s:hidden>
						<s:label name="lstDfrDTO[%{#lstDfrDTO_stat.index}].ipAddress"
							id="iPAddress%{#lstDfrDTO_stat.index}" label="IPAddress"
							value="%{ipAddress}" />
						<s:label
							name="lstDfrDTO[%{#lstDfrDTO_stat.index}].chnlConfigDropDownList.selectedValue"
							id="lstChannels%{#lstDfrDTO_stat.index}" label="Channels"
							value="%{chnlConfigDropDownList.selectedValue}" />
						<s:if
							test="%{lstDfrDTO[#lstDfrDTO_stat.index].chnlConfigDropDownList.analogCnt > 0}">
							<s:label label="Analogs"
								value="[%{lstDfrDTO[#lstDfrDTO_stat.index].analogChannelStart} - %{lstDfrDTO[#lstDfrDTO_stat.index].analogChannelEnd}]"></s:label>
						</s:if>
						<s:else>
							<s:label label="Analogs " value="[Nil]"></s:label>
						</s:else>
						<s:if
							test="%{lstDfrDTO[#lstDfrDTO_stat.index].chnlConfigDropDownList.digitalCnt > 0}">
							<s:label label="Digitals"
								value="[%{lstDfrDTO[#lstDfrDTO_stat.index].digitalChannelStart} - %{lstDfrDTO[#lstDfrDTO_stat.index].digitalChannelEnd}]"></s:label>
						</s:if>
						<s:else>
							<s:label label="Digitals" value="[Nil]"></s:label>
						</s:else>
						<!-- 
						<s:textfield
											name="lstDfrDTO[%{#lstDfrDTO_stat.index}].serialNumber"
											id="serialNumber%{#lstDfrDTO_stat.index}"
											label="Chassis Serial Number" size="5" value="%{serialNumber}"
											/>
						<sj:datepicker name="lstDfrDTO[%{#lstDfrDTO_stat.index}].installationDate" label="Chassis Instllation Date" value="" />
						 -->
					</table>
				</td>
			</tr>
			<s:if
				test="%{stationDetails.configStatus == 'COMPLETE' && #session.userDetails.role != 'guest'}">			
				 <s:if test="%{accessMode == 'Edit'}">
								<tr align="right">
									<td colspan="2"><s:url
											var="urlDeleteDfr_#{lstDfrDTO_stat.index}"
											action="deleteNewDfrEdit">
											<s:param name="deleteIndex" value="%{#lstDfrDTO_stat.index}" />
										</s:url> <s:submit id="sbtnDelete_%{#lstDfrDTO_stat.index}"
											cssClass="submit" value="Delete"
											onclick="confirmBefore(%{newlyAdded},'btnDelete_%{#lstDfrDTO_stat.index}')" />
										<sj:a cssStyle="visibility:hidden" type="button" align="right"
											theme="simple" id="btnDelete_%{#lstDfrDTO_stat.index}"
											href="%{urlDeleteDfr_#{lstDfrDTO_stat.index}}"
											onBeforeTopics="beforeSubmitTopic"
											name="btnDelete_%{#lstDfrDTO_stat.index}"
											targets="divDfrsList" onCompleteTopics="deletedDfrTopic"
											cssClass="submit"
											value="Delete DFR%{#lstDfrDTO_stat.index +1}">Delete</sj:a></td>
								</tr>
				</s:if>
			</s:if>
		</table>
		</s:if>
		<s:else>
						<table class="inlineTable">
							<tr>
								<s:if test="%{accessMode != 'Edit'}">
									<td colspan="2" align="center"
										style="font-weight: bold; background-color: #9999CC">Chassis
										<s:property value="%{#lstDfrDTO_stat.index+1}" />
									</td>
								</s:if>
								<s:else>
									<td colspan="2" align="center"
										style="font-weight: bold; background-color: #9999CC"><s:property
											value="%{lstDfrDTO[#lstDfrDTO_stat.index].dfrName}" /></td>
								</s:else>
							</tr>
							<tr>
								<td>
									<div id="imageDiv<s:property value='%{#lstDfrDTO_stat.index}'/>">
										<img
											src="images/dfr<s:property value="%{chnlConfigDropDownList.selectedValue}"/>.jpg"
											alt="Chassis<s:property value="%{#lstDfrDTO_stat.index}"/>" />
									</div></td>
								<td>
									<table class="wwFormTable">
										<s:textfield
											name="lstDfrDTO[%{#lstDfrDTO_stat.index}].dfrName"
											id="DFRId%{#lstDfrDTO_stat.index}" label="Chassis Id"
											size="15" value="%{dfrName}" disabled="true" />
										<s:textfield
											name="lstDfrDTO[%{#lstDfrDTO_stat.index}].ipAddress"
											id="iPAddress%{#lstDfrDTO_stat.index}"
											label="Chassis IP Address" size="13" value="%{ipAddress}"
											disabled="true" />
											<s:select
												name="lstDfrDTO[%{#lstDfrDTO_stat.index}].chnlConfigDropDownList.selectedValue"
												id="lstChannels%{#lstDfrDTO_stat.index}"
												list="channelsConfList"
												label="Select channels config"
												onchange="javascript:change_image('%{#lstDfrDTO_stat.index}', this.value);return false;"
												value="chnlConfigDropDownList.selectedValue">
											</s:select>
											<!-- 
											<s:textfield
											name="lstDfrDTO[%{#lstDfrDTO_stat.index}].serialNumber"
											id="serialNumber%{#lstDfrDTO_stat.index}"
											label="Chassis Serial Number" size="5" value="%{serialNumber}"
											/>
						<sj:datepicker name="lstDfrDTO[%{#lstDfrDTO_stat.index}].installationDate"  label="Chassis Instllation Date" value="" />
						 -->
									<s:if
										test="%{stationDetails.configStatus == 'COMPLETE' && #session.userDetails.role != 'guest'}">
											<s:url var="urlDeleteDfr_#{lstDfrDTO_stat.index}"
												action="deleteNewDfrEdit">
												<s:param name="deleteIndex" value="%{#lstDfrDTO_stat.index}]" />
											</s:url>
											<s:submit id="sbtnDelete_%{#lstDfrDTO_stat.index}"
												cssClass="submit" value="Delete"
												onclick="confirmBefore(%{newlyAdded},'btnDelete_%{#lstDfrDTO_stat.index}')" />
											<sj:a cssStyle="visibility:hidden" type="button" align="right"
												theme="simple" id="btnDelete_%{#lstDfrDTO_stat.index}"
												href="%{urlDeleteDfr_#{lstDfrDTO_stat.index}}"
												onBeforeTopics="beforeSubmitTopic"
												name="btnDelete_%{#lstDfrDTO_stat.index}"
												targets="divDfrsList" onCompleteTopics="deletedDfrTopic"
												cssClass="submit"
												value="Delete DFR%{#lstDfrDTO_stat.index +1}">Delete</sj:a>
										</s:if>
									</table>
								</td>
							</tr>
						</table>
						<s:hidden theme="simple" name="lstDfrDTO[%{#lstDfrDTO_stat.index}].dfrId"
							value="%{dfrId}"></s:hidden>
							<s:hidden theme="simple" name="lstDfrDTO[%{#lstDfrDTO_stat.index}].newlyAdded" id="lstDfrDTO[%{#lstDfrDTO_stat.index}].newlyAdded"
                                                        value="%{newlyAdded}"></s:hidden>
						<s:hidden theme="simple" name="lstDfrDTO[%{#lstDfrDTO_stat.index}].status"
							value="%{status}"></s:hidden>
						<s:hidden theme="simple" 
							name="lstDfrDTO[%{#lstDfrDTO_stat.index}].analogChannelStart"
							value="%{analogChannelStart}"></s:hidden>
						<s:hidden theme="simple" 
							name="lstDfrDTO[%{#lstDfrDTO_stat.index}].analogChannelEnd"
							value="%{analogChannelEnd}"></s:hidden>
						<s:hidden theme="simple" 
							name="lstDfrDTO[%{#lstDfrDTO_stat.index}].digitalChannelStart"
							value="%{digitalChannelStart}"></s:hidden>
						<s:hidden theme="simple" 
							name="lstDfrDTO[%{#lstDfrDTO_stat.index}].digitalChannelEnd"
							value="%{digitalChannelEnd}"></s:hidden>
		
		</s:else>
	</s:iterator>
	<s:if
	test="%{stationDetails.configStatus == 'COMPLETE' && #session.userDetails.role != 'guest'}">
	
		<s:if test="%{accessMode == 'Edit'}">
			<div id="div<s:property value='%{lstDfrDTO.size}'/>">
				<s:url var="urlAddDfr" action="addNewDfrEdit"/>
				<table>
				<sj:submit align="right" theme="simple" cssClass="submit" href="%{urlAddDfr}"
				id="btnAddDfr" name="btnAddDfr" value="Add a new DFR" targets="div%{lstDfrDTO.size}" onCompleteTopics="addNewDfrTopic"></sj:submit>
				</table>
			</div>
		</s:if>
		</s:if>
	</td>
	</tr>
	<tr>
	<td align="center" style="background-color: white;">
			
				<s:if
						test="%{accessMode != 'Edit'}">
						<s:submit theme="simple" cssClass="submit" type="button" name="btnBackBottom"
							value="Back" action="backToConfigure"></s:submit>
						<s:submit theme="simple" cssClass="submit" name="btnConfigureBottom"
							value="Configure" action="displayDfrNext"></s:submit>
					</s:if> <s:else>
						<s:submit theme="simple" cssClass="submit" type="button" name="btnBackBottom"
							value="Back" action="backToStationList"></s:submit>
						<s:if
							test="%{stationDetails.configStatus == 'COMPLETE' && #session.userDetails.role != 'guest'}">
							
							<s:submit theme="simple" cssClass="submit" id="btnConfigureBottom" name="btnConfigureBottom"
							value="Edit Configuration" action="editStationDetails"></s:submit>
							<s:submit theme="simple" cssClass="submit" id="btnSendBottom" name="btnSendBottom"
								value="Send"
								onclick="return confirm('Do you want to send the configuration?')"
								action="sendConfiguration"></s:submit>
						</s:if>
						<s:else>
							<s:submit theme="simple" cssClass="submit" name="btnConfigureBottom"
							value="View Configuration" action="editStationDetails"></s:submit>						
							<s:submit theme="simple" cssClass="submit" name="btnSendBottom"
								value="Send" disabled="true"></s:submit>
						</s:else>
					</s:else>
		</td>
	</tr>
		<s:hidden id="jspFileName" name="jspFileName" value="displayDFR.jsp"></s:hidden>
	</s:form>
</body>
</html>
