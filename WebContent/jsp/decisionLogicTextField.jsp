<%@ taglib prefix="s" uri="/struts-tags"%>
<s:textfield cssStyle="width:95%" formIds="manageLineGroups"
	id="txtDecisionLogic" size="80"
	name="lstLineGroups[%{selectedLineGroupIndex}].decisionLogic"
	value="%{lstLineGroups[selectedLineGroupIndex].decisionLogic}"></s:textfield>