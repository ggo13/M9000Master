<!DOCTYPE HTML>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sjc" uri="/struts-jquery-chart-tags"%>
<script type="text/javascript" src="js/jquery.validate.js"></script>
<%
	String stationId = new String();
	stationId = (String) session.getAttribute("stationId");
%>
<script type="text/javascript">
	var stationId = <%=stationId%>;
	var context_path = "${pageContext.request.contextPath}";
</script>
<link href="<s:url value='/css/scopeTab.css'/>" rel="stylesheet"
	type="text/css" />
<script src="js/m9000/scopeTab.js"></script>
<script type="text/javascript">
	$(document).ready( function() { 
		stopScopeLoadTimer();
		console.log("Starting SCOPE Timer from scopeTab.jsp");
		$.publish("startTimer");
});
</script>

	<div>
		<div align="center">
			<s:url var="varUpdateScopeDfrList" action="updateScopeDfrsList">
			</s:url>
			<s:select id="selectedDfrDto.dfrId" name="selectedDfrDto.dfrId"
				key="label.dfrs" href="%{varUpdateScopeDfrList}"
				onChange="javascript:updateChartDisplay();return false;"
				list="lstScopeDfrs" listKey="dfrId" listValue="scopeDisplayName"
				value="%{selectedDfrDto.dfrId}" />
			<s:checkbox theme="simple" id="scopeDataRefresh"
				name="scopeDataRefresh" value="true" label="Start/Stop"
				cssClass=".tdLabel" />
			<s:label theme="simple" name="lblScopeDataRefresh" value="Start/Stop" for="scopeDataRefresh"
				cssClass="titleLabel" />
			<s:checkbox theme="simple" id="showHideChartMenu"
				name="showHideChartMenu" value="true" label="Show/Hide Chart Menu"
				cssClass=".tdLabel" />
			<s:label theme="simple" name="lblShowHideChartMenu" value="Show/Hide Chart Menu" for="showHideChartMenu"
				cssClass="titleLabel" />
		</div>
	</div>
	<s:url var="varUpdateChartDisplaySelectedDfr" action="updateChartForSelectedDfr">
	</s:url>
	<div id="chartContainer" class="wrap" onresize="javascript:updateLegend();return true;" style="height:calc(100vh - 250px)">
		<sj:div formIds="NextToCreateOrUpdate" id="divCharts" href="%{#varUpdateChartDisplaySelectedDfr}" reloadTopics="updateChartDisplay" deferredLoading="false">
		</sj:div>
	</div>
 <s:hidden id="chartChanged" name="chartChanged" value="%{chartChanged}"></s:hidden>
