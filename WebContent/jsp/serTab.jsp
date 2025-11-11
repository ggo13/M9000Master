<!DOCTYPE HTML>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sjdt" uri="/struts-jquery-datatables-tags"%>
<%@ taglib prefix="sjg" uri="/struts-jquery-grid-tags"%>
<%@ page import="com.usi.m9000.dto.UsersDTO"%>
<%
	String stationId = new String();
	stationId = (String) session.getAttribute("stationId");
%>
<script type="text/javascript">
	var stationId =
<%=stationId%>
	;
	var context_path = "${pageContext.request.contextPath}";
</script>
<style type="text/css">
.ui-jqgrid .ui-pg-table {
	width: 400px;
	font-size: 12px;
}

.ui-jqgrid .ui-pg-input {
	height: 18px;
}
</style>
<script src="js/m9000/serGrid.js"></script>
<s:url var="fetchSerDatesUrl" action="fetchSerDatesToView">
	<s:param name="stationId" value="%{stationId}"></s:param>
</s:url>
<div class="flex-container">
	<div id="divLeftPane" class="boxLeft">
		<div>
			<s:label theme="simple" name="lblSerDates" value="Recent Dates" title="Use search functionality for more dates"
				cssClass="titleLabel" for="lstSerDates" />
			<s:submit theme="simple" cssClass="submit" style="display:none"
				id="btnClearDatesSelectionTop" name="btnClearDatesSelectionTop"
				value="Reset" title="Clears the dates selection"
				onclick="return false;" />
			<sj:select href="%{fetchSerDatesUrl}" theme="simple"
				cssStyle="width:100%;background-color:#EEEEFF;overflow:auto"
				id="lstSerDates" onCompleteTopics="afterReloadSerDates"
				reloadTopics="reloadSerDates" name="lstSerDates" multiple="true"
				size="35" list="mapSerDates" listKey="key" listValue="value"
				headerKey="-1" headerValue="--- Select a Date ---" />
		</div>
		<div>
			<s:submit theme="simple" cssClass="submit" style="display:none"
				id="btnClearDatesSelectionBottom"
				name="btnClearDatesSelectionBottom" value="Reset"
				title="Clears the dates selection" onclick="return false;" />
		</div>
	</div>
	<div id="ser" class="boxRight">
		<s:url var="fetchSerDataUrl" action="fetchSerData">
			<s:param name="stationId" value="%{stationId}"></s:param>
		</s:url>
		<s:url var="fetchActiveEventsUrl" action="fetchActiveEvents">
			<s:param name="stationId" value="%{stationId}"></s:param>
		</s:url>
		<s:url var="fetchAllAvailableSerDatesUrl" action="fetchAllAvailableSerDates">
			<s:param name="stationId" value="%{stationId}"></s:param>
		</s:url>
		<div id="serStatus" align="center" style="background-color: RED"></div>
		<div align="center">
			<s:checkbox theme="simple" name="autoSerRefresh" value="true" />
			<s:label theme="simple" name="lblAutoUpdate"
				value="Auto Refresh Enable?" cssClass="titleLabel"
				for="autoSerRefresh" />
			<s:submit theme="simple" cssClass="submit" name="btnExportSer"
				value="Export" action="getSerExports"
				title="Export SER into a csv file"></s:submit>
			<s:submit theme="simple" cssClass="submit" name="printExportConfig"
				value="Print SER Report" onClick="openSerPdf();return false;"
				title="Print Preview SER"></s:submit>
		</div>
		<sjg:grid id="serGrid" caption="SER Data" dataType="json"
			hidegrid="false" href="%{fetchSerDataUrl}" pager="true"
			toppager="true" navigatorCloneToTop="true" navigator="true"
			navigatorAdd="false" navigatorSearch="true"
			navigatorSearchOptions="{multipleSearch:true}" navigatorEdit="false"
			navigatorView="false" navigatorDelete="false" gridModel="gridModel"
			rowList="25,50,100,150,200,250,300,400,500" rowNum="25"
			rownumbers="true" autowidth="true" resizable="false"
			shrinkToFit="true" viewrecords="true" altRows="false" sortable="true"
			sortname="displayTime" sortorder="desc"
			onDblClickRowTopics="rowselect" onGridCompleteTopics="initSerGrid"
			onPagingTopics="storeSelection" onSortColTopics="storeSelection"
			hoverrows="true" scrollrows="true" editinline="false"
			onErrorTopics="showErrorMessage">
			<sjg:gridColumn name="displayTime" index="displayTime"
				title="Date-Time" key="true" sortable="true" sorttype="date" search="true" searchtype="select" searchoptions="{sopt:['eq','le','ge','lt','gt','ne'], dataUrl: '%{fetchAllAvailableSerDatesUrl}'}"
				align="center" width="70" />
			<sjg:gridColumn name="eventNum" index="eventNum" title="Event"
				align="center" sortable="true" width="25" searchtype="select"
				searchoptions="{sopt:['eq','ne'], dataUrl: '%{fetchActiveEventsUrl}'}" />
			<sjg:gridColumn name="currentStateAsString"
				index="currentStateAsString" title="Current State" sortable="true"
				align="center" width="30" search="true" searchtype="select"
				searchoptions="{sopt:['eq'], value:':ALL;0:OPEN;1:CLOSE'}" />
			<sjg:gridColumn name="status" index="status" title="Status"
				sortable="true" align="center" width="30" search="true"
				searchtype="select"
				searchoptions="{sopt:['eq'], value:':ALL;0:NORMAL;1:ABNORMAL'}" />
			<sjg:gridColumn name="lockedAsString" index="lockedAsString"
				title="Sync" sortable="true" align="center" width="25" search="true"
				searchtype="select"
				searchoptions="{sopt:['eq'], value:':ALL;1:YES;0:NO'}" />
			<sjg:gridColumn name="name" index="name" width="40"
				title="Description" sortable="true" align="left" search="true"
				searchoptions="{sopt:['cn','nc','bw','bn']}" />
		</sjg:grid>
	</div>
</div>
