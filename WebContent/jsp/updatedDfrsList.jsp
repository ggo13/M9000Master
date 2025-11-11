<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sjdt" uri="/struts-jquery-datatables-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<script type="text/javascript">
$.subscribe('deletedDfrTopic', function(event, data) {
        console.log("size..."+${fn:length(lstDfrDTO)});
        
        if (${fn:length(lstDfrDTO) == 0})
        {
            $('[id^="btnConfigure"]').val("Edit Configuration");
            $('[id^="btnConfigure"]').prop("name","action:saveNewlyAddedDfrsAction");// Changing the action using name attribute
            $('[id^="btnConfigure"]').prop("disabled",true);
            $('[id^="btnSend"]').prop("disabled",true);        	
        }
        else if (!${stationDetails.dfrAddedOrRemoved} && ${fn:length(lstDfrDTO) == stationDetails.totalDfrsConfigured})
        {
                $('[id^="btnConfigure"]').val("Edit Configuration");
                $('[id^="btnConfigure"]').prop("name","action:editStationDetails");// Changing the action using name attribute
                $('[id^="btnSend"]').prop("disabled",false);
        }
        else if (${fn:length(lstDfrDTO) < stationDetails.totalDfrsConfigured})
        {
            $('[id^="btnConfigure"]').val("Save & Edit Configuration");
            $('[id^="btnConfigure"]').prop("name","action:saveNewlyAddedDfrsAction");// Changing the action using name attribute
        	$('[id^="btnSend"]').hide();
        }
});
</script>
	<td align="right" style="background-color: white;">
	<s:iterator value="lstDfrDTO" status="lstDfrDTO_stat">
	<s:if test="%{accessMode eq 'Edit' && !lstDfrDTO[#lstDfrDTO_stat.index].newlyAdded}">
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
					</table>
				</td>
			</tr>
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
							name="btnDelete_%{#lstDfrDTO_stat.index}" targets="divDfrsList"
							onCompleteTopics="deletedDfrTopic" cssClass="submit"
							value="Delete DFR%{#lstDfrDTO_stat.index +1}">Delete</sj:a></td>
				</tr>
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
									</table>
								</td>
							</tr>
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
							name="btnDelete_%{#lstDfrDTO_stat.index}" targets="divDfrsList"
							onCompleteTopics="deletedDfrTopic" cssClass="submit"
							value="Delete DFR%{#lstDfrDTO_stat.index +1}">Delete</sj:a></td>
				</tr>
			</table>
						<s:hidden theme="simple" name="lstDfrDTO[%{#lstDfrDTO_stat.index}].dfrId"
							value="%{dfrId}"></s:hidden>
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
	<s:if test="%{accessMode == 'Edit'}">
		<div id="div<s:property value='%{lstDfrDTO.size}'/>" align="right">
			<s:url var="urlAddDfr" action="addNewDfrEdit"/>
			<table  class="wwwFormTable"><tr><td align="right">
			<sj:submit align="right" theme="simple" cssClass="submit" href="%{urlAddDfr}"
			id="btnAddDfr" name="btnAddDfr" value="Add a new DFR" targets="div%{lstDfrDTO.size}" onCompleteTopics="addNewDfrTopic"></sj:submit>
			</td></tr></table>
		</div>
	</s:if>
	</td>

