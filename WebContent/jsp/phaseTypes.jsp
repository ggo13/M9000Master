<%@ taglib prefix="s" uri="/struts-tags"%>
<s:select theme="simple"
	name="lstAnalogChannels[%{selectedChannelIndex}].phase"
	id="lstAnalogChannels[%{selectedChannelIndex}].phase" 
	list="%{lstAnalogChannels[selectedChannelIndex].mapPhaseTypes}"
	value="%{lstAnalogChannels[selectedChannelIndex].phase}"/>