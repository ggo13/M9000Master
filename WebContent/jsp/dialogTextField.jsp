<%@ taglib prefix="s" uri="/struts-tags"%>
<s:textfield theme="simple" cssStyle="width:100%" id="dialogText"
	name="dialogText"
	value="%{lstLineGroups[selectedLineGroupIndex].decisionLogic}"
	label="Decision Logic">
</s:textfield>
