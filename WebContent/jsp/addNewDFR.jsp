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
                $('[id^="btnConfigure"]').prop("disabled",false);
                $('[id^="btnSend"]').prop("disabled",false);
        }
        else if (${fn:length(lstDfrDTO) < stationDetails.totalDfrsConfigured})
        {
            $('[id^="btnConfigure"]').val("Save & Edit Configuration");
            $('[id^="btnConfigure"]').prop("name","action:saveNewlyAddedDfrsAction");// Changing the action using name attribute
            $('[id^="btnConfigure"]').prop("disabled",false);
        	$('[id^="btnSend"]').hide();
        }
});

$.subscribe('addNewDfrTopic', function(event, data) {
	$('[id^="btnConfigure"]').val("Save & Edit Configuration");
	$('[id^="btnConfigure"]').prop("name","action:saveNewlyAddedDfrsAction"); // Changing the action using name attribute
	$('[id^="btnConfigure"]').prop("disabled",false);
	$('[id^="btnSend"]').prop("disabled",true);
	$('[id="btnAddDfr"]').prop("targets","div${fn:length(lstDfrDTO)}");
});
</script>
<table class="inlineTable">
	<tr>
		<td colspan="2" align="center"
			style="font-weight: bold; background-color: #9999CC"><s:property
				value="%{newlyAddedDfr.dfrName}" /></td>
	</tr>
	<tr>
		<td>
			<div id="imageDiv<s:property value='%{newlyAddedDfrIndex}'/>">
				<img
					src="images/dfr<s:property value="%{newlyAddedDfr.chnlConfigDropDownList.selectedValue}"/>.jpg"
					alt="Chassis<s:property value="%{newlyAddedDfrIndex}"/>" />
			</div>
		</td>
		<td>
			<table class="wwFormTable">
				<s:textfield name="lstDfrDTO[%{newlyAddedDfrIndex}].dfrName"
					id="DFRId%{newlyAddedDfrIndex}" label="Chassis Id" size="15"
					value="%{newlyAddedDfr.dfrName}" disabled="true" />
				<s:textfield name="lstDfrDTO[%{newlyAddedDfrIndex}].ipAddress"
					id="iPAddress%{newlyAddedDfrIndex}" label="Chassis IP Address"
					size="13" value="%{newlyAddedDfr.ipAddress}" disabled="true" />
				<s:select
					name="lstDfrDTO[%{newlyAddedDfrIndex}].chnlConfigDropDownList.selectedValue"
					id="lstChannels%{newlyAddedDfrIndex}"
					list="channelsConfList"
					label="Select channels config"
					onchange="javascript:change_image('%{newlyAddedDfrIndex}', this.value);return false;"
					value="newlyAddedDfr.chnlConfigDropDownList.selectedValue">
				</s:select>
			</table>
		</td>
	</tr>
	<tr align="right">
		<td colspan="2"><s:url var="urlDeleteDfr_#{newlyAddedDfrIndex}"
				action="deleteNewDfrEdit">
				<s:param name="deleteIndex" value="%{newlyAddedDfrIndex}" />
			</s:url> <s:submit id="sbtnDelete_%{newlyAddedDfrIndex}" cssClass="submit"
				value="Delete"
				onclick="confirmBefore(%{newlyAddedDfrIndex},'btnDelete_%{newlyAddedDfrIndex}')" />
			<sj:a cssStyle="visibility:hidden" type="button" align="right"
				theme="simple" id="btnDelete_%{newlyAddedDfrIndex}"
				href="%{urlDeleteDfr_#{newlyAddedDfrIndex}}"
				onBeforeTopics="beforeSubmitTopic"
				name="btnDelete_%{newlyAddedDfrIndex}" targets="divDfrsList"
				onCompleteTopics="deletedDfrTopic" cssClass="submit"
				value="Delete DFR%{#lstDfrDTO_stat.index +1}">Delete</sj:a></td>
	</tr>
</table>
<s:hidden theme="simple"
	name="lstDfrDTO[%{newlyAddedDfrIndex}].dfrId" value="%{newlyAddedDfr.dfrId}"></s:hidden>
<s:hidden theme="simple"
	name="lstDfrDTO[%{newlyAddedDfrIndex}].status" value="%{newlyAddedDfr.status}"></s:hidden>
<s:hidden theme="simple"
	name="lstDfrDTO[%{newlyAddedDfrIndex}].analogChannelStart"
	value="%{newlyAddedDfr.analogChannelStart}"></s:hidden>
<s:hidden theme="simple"
	name="lstDfrDTO[%{newlyAddedDfrIndex}].analogChannelEnd"
	value="%{newlyAddedDfr.analogChannelEnd}"></s:hidden>
<s:hidden theme="simple"
	name="lstDfrDTO[%{newlyAddedDfrIndex}].digitalChannelStart"
	value="%{newlyAddedDfr.digitalChannelStart}"></s:hidden>
<s:hidden theme="simple"
	name="lstDfrDTO[%{newlyAddedDfrIndex}].digitalChannelEnd"
	value="%{newlyAddedDfr.digitalChannelEnd}"></s:hidden>
<div id="div<s:property value='%{lstDfrDTO.size}'/>">
			<s:url var="urlAddDfr" action="addNewDfrEdit"/>
			<table  class="wwwFormTable"><tr><td align="right">
			<sj:submit align="right" theme="simple" cssClass="submit" href="%{urlAddDfr}"
			id="btnAddDfr" name="btnAddDfr" value="Add a new DFR" targets="div%{lstDfrDTO.size}" onCompleteTopics="addNewDfrTopic"></sj:submit>
			</td></tr></table>
</div>

