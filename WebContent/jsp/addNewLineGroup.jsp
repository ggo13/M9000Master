<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<tr>
	<td align="center">NewLineGroup = <s:property
			value="%{#session.NewLineGroup}" /> <s:textfield
			name="selectedLineGroup" id="selectedLineGroup"
			label="Enter a New LineGroup name" labelposition="top"
			value="selectedLineGroup">
		</s:textfield>
	</td>
</tr>
<s:url var="lineUrl" action="showLineGroupDetails">
</s:url>
<tr>
	<td align="left">
		<sj:div showLoadingText="true" id="details"
			href="%{#lineUrl}" beforeNotifyTopics="/beforeShow" theme="ajax"
			listenTopics="show_linegroup_details" formIds="manageLineGroups">
		</sj:div> <s:url var="editUrl" action="updateLineGroupDetails">
			</s:url>
		<sj:div id="editDetail" href="%{#editUrl}"
				listenTopics="modify_linegroup_details" formIds="manageLineGroups"
				onSuccessTopics="/unselectDetails"
				onErrorTopics="/unselectDetails" /></td>
</tr>
