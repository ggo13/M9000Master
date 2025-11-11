<%@ taglib prefix="s" uri="/struts-tags"%>
<div align="center">
	<s:label theme="simple" cssStyle="text-align: left; font-weight:bold;"
		value="Select a LineGroup: "></s:label>
	&nbsp;
	<s:select cssStyle="width:15%" theme="simple" name="selectedLineGroup"
		id="selectedLineGroup" list="lstLineGroups" listKey="name"
		listValue="lineGroupName" label="Select a LineGroup" value="selectedLineGroup"
		onchange="javascript:show_linegroup(this);return false;">
	</s:select>
</div>