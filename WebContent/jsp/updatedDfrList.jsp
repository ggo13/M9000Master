<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>

<div id="divList">
	<s:iterator value="lstDfrDTO" status="lstDfrDTO_stat">
		<table class="inlineTable">
			<tr>
				<td colspan="2" align="center" style="background-color: #9999CC"><b>DFR
						<s:property value="%{#lstDfrDTO_stat.index}" />
				</b></td>
			</tr>
			<s:textfield name="lstDfrDTO[%{#lstDfrDTO_stat.index}].dfrName"
				id="DFRId%{#lstDfrDTO_stat.index}" label="DFR Id"
				labelposition="top" value="%{dfrName}" />
			<s:textfield name="lstDfrDTO[%{#lstDfrDTO_stat.index}].ipAddress"
				id="iPAddress%{#lstDfrDTO_stat.index}" label="DFR IP Address"
				labelposition="top" value="%{ipAddress}" />
			<s:hidden name="lstDfrDTO[%{#lstDfrDTO_stat.index}].status"
				value="%{status}"></s:hidden>

			<s:if test="%{accessMode == 'Edit'}">
				<tr>
					<td align="center"><s:label
							name="lstDfrDTO[%{#lstDfrDTO_stat.index}].chnlConfigDropDownList.selectedValue"
							id="lstChannels%{#lstDfrDTO_stat.index}"
							label="Selected channels combination"
							value="%{chnlConfigDropDownList.selectedValue}" /> <s:hidden
							name="lstDfrDTO[%{#lstDfrDTO_stat.index}].chnlConfigDropDownList.selectedValue"
							value="%{chnlConfigDropDownList.selectedValue}"></s:hidden></td>
				</tr>
			</s:if>
			<s:else>
				<s:select
					name="lstDfrDTO[%{#lstDfrDTO_stat.index}].chnlConfigDropDownList.selectedValue"
					id="lstChannels%{#lstDfrDTO_stat.index}"
					list="chnlConfigDropDownList.channelsConfList"
					label="Select channels combination" labelposition="top"
					value="chnlConfigDropDownList.selectedValue">
				</s:select>
				<s:if test="%{#lstDfrDTO_stat.index > minimumDFRCount-1}">
					<s:url var="deleteUrl" action="configDfr" method="deleteDfr">
						<s:param name="dfrIndex" value="%{#lstDfrDTO_stat.index}"></s:param>
					</s:url>
					<sj:submit id="btnDelete%{#lstDfrDTO_stat.index}" targets="divList"
						href="%{#deleteUrl}" events="onclick" executeScripts="true" 
						name="btnDelete%{#lstDfrDTO_stat.index}" cssClass="submit"
						value="Delete"></sj:submit>
									URL: <s:property value="%{deleteUrl}" />
				</s:if>
			</s:else>

		</table>
	</s:iterator>
</div>