<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<s:url var="urlSampleRate" action="fetchLtrSampleRate" />
<s:select cssStyle="text-align:left;"
	name="stationDetails.systemSampleRate" key="label.system.samplerate"
	list="lstSampleRate"
	onchange="javascript:show_ltrSampleRate();return false;"
	value="%{stationDetails.systemSampleRate}" />
<sj:div showLoadingText="true" id="sampleRate" href="%{urlSampleRate}"
	listenTopics="ltr_sample_rate" preload="false" formIds="createStation"
	formFilter="filterLtr" indicator="indicator1">
	<s:select cssStyle="text-align:left;"
		name="stationDetails.systemLongTermSampleRate"
		key="label.system.ltsamplerate" list="lstLtSampleRate"
		value="%{stationDetails.systemLongTermSampleRate}" />
	<img id="indicator1"
		src="${pageContext.request.contextPath}/images/indicator.gif"
		style="display: none" />
</sj:div>
