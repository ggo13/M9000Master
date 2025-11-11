<%@ taglib prefix="s" uri="/struts-tags"%>
<s:select theme="simple" name="lstTriggerChannels[%{reqdIndex}].type"
	id="lstTriggerChannels[%{reqdIndex}].type"
	list="%{lstTriggerChannels[reqdIndex].mapTriggerInputTypes}"
	onchange="javascript:update_access(this.name, this.value);update_trigger_name(this, this.value);return false;"
	value="%{lstTriggerChannels[reqdIndex].type}"
	disabled="%{!lstTriggerChannels[reqdIndex].status}" />
<s:hidden theme="simple" name="lstTriggerChannels[%{reqdIndex}].inputChannelName"></s:hidden>
<s:hidden name="lstTriggerChannels[%{reqdIndex}].inputType"></s:hidden>

<s:hidden theme="simple" name="lstTriggerChannels[%{reqdIndex}].chassis"></s:hidden>
