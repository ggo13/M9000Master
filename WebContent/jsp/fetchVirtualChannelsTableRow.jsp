<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<tr style="background-color: #EEEEFF">
<s:url var="urlEditVirtualDialog#{newVirtualChnlIndex}" action="editVirtualChannelsDialog">
							<s:param name="selectedChannelIndex" value="%{newVirtualChnlIndex}"/>
</s:url>
<s:url var="urlDeleteVirtualChannel#{newVirtualChnlIndex}" action="deleteVirtualChannels">
							<s:param name="selectedChannelIndex" value="%{newVirtualChnlIndex}"/>
</s:url>
	<td width="5%" align="center">
	<s:label
			name="lstVirtualChannels[%{newVirtualChnlIndex}].chassisChannelNo"
			id="lstVirtualChannels[%{newVirtualChnlIndex}].chassisChannelNo"
			theme="simple" value="%{newVirtualChannel.chassisChannelNo}" />
			</td>
	<s:hidden
		name="lstVirtualChannels[%{newVirtualChnlIndex}].chassisChannelNo"></s:hidden>
	<s:hidden
		name="lstVirtualChannels[%{newVirtualChnlIndex}].channel"></s:hidden>
	<s:hidden name="lstVirtualChannels[%{newVirtualChnlIndex}].name"></s:hidden>
	<td width="34%" align="center"><s:fielderror>
			<s:param>lstVirtualChannels[<s:property
					value="%{newVirtualChnlIndex}" />].circuitName</s:param>
		</s:fielderror> 
		<s:textfield theme="simple" cssStyle="width:100%"
			name="lstVirtualChannels[%{newVirtualChnlIndex}].circuitName"
			required="true" size="64" maxlength="64" />
	</td>
	<td width="10%" align="center">
		<s:label cssStyle="width:95%"
				name="lstVirtualChannels[%{#virtual_stat.index}].virtualLogic"
				id="lstVirtualChannels[%{#virtual_stat.index}].virtualLogic"
				theme="simple" value="%{virtualLogic}" />
	<sj:a openDialog="editVirtualDialog" cssStyle="width:10%" title="Edit Virtual Channel Details" href="%{urlEditVirtualDialog#{virtual_stat.index}}" button="true" buttonIconSecondary="ui-icon-pencil"></sj:a>
	</td>
	<td width="5%" align="center">
	<s:label theme="simple"
			name="lstVirtualChannels[%{newVirtualChnlIndex}].phase"
			id="lstVirtualChannels[%{newVirtualChnlIndex}].phase"
			value="%{newVirtualChannel.phase}"/></td>
	<s:hidden
		name="lstVirtualChannels[%{newVirtualChnlIndex}].phase"></s:hidden>
	<td width="21%" align="center">
	<s:label theme="simple"
			name="lstVirtualChannels[%{newVirtualChnlIndex}].inputType"
			id="lstVirtualChannels[%{newVirtualChnlIndex}].inputType"
			value="%{newVirtualChannel.inputType}" /></td>
	<s:hidden
		name="lstVirtualChannels[%{newVirtualChnlIndex}].inputType"></s:hidden>
	<td width="7%" align="center"><s:checkbox
			name="lstVirtualChannels[%{newVirtualChnlIndex}].exportStatus"
			id="lstVirtualChannels[%{newVirtualChnlIndex}].exportStatus"
			theme="simple" value="%{newVirtualChannel.exportStatus}"
			onclick="javascript:isAllChecked();" /></td>
	<td width="5%" align="center">
			<s:submit theme="simple" cssClass="submit" id="btnDeleteVirtual%{newVirtualChnlIndex}" name="btnDeleteVirtual%{newVirtualChnlIndex}"
			onclick="confirmationDialog('WARNING: There may be lingroups and Measurements associated with this virtual channel. Are you sure you want to delete?'); return false;"
			value="Delete"/>
			<sj:a listenTopics="confirmed" formIds="configureVirtualChannels" href="%{urlDeleteVirtualChannel#{newVirtualChnlIndex}}" targets="virtualChannelsBody"></sj:a>
	</td>		
</tr>
