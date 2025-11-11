<%@ taglib prefix="s" uri="/struts-tags"%>
<s:label theme="simple" key="label.system.ltsamplerate"></s:label>
<s:label theme="simple">: </s:label>
<s:select theme="simple" cssStyle="text-align:left;"
	name="stationDetails.systemLongTermSampleRate"
	key="label.system.ltsamplerate" list="lstLtSampleRate"
	value="%{stationDetails.systemLongTermSampleRate}" />
