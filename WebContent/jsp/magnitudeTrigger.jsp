<%@ taglib prefix="s" uri="/struts-tags"%>
<s:if test="%{selectedIndex != null}">
	<s:textfield theme="simple"
		name="lstTriggerChannels[%{selectedIndex}].harmonic"
		id="lstTriggerChannels[%{selectedIndex}].harmonic"
		value="%{lstTriggerChannels[reqdIndex].harmonic}"
		disabled="lstTriggerChannels[reqdIndex].disableHarmonic" />
</s:if>