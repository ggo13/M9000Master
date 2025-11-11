<!DOCTYPE HTML>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sjdt" uri="/struts-jquery-datatables-tags"%>
<%@ taglib prefix="sjg" uri="/struts-jquery-grid-tags"%>
<%@ page import="com.usi.m9000.dto.UsersDTO"%>
<% 	String stationId = new String();
	stationId = (String)session.getAttribute("stationId");
  %>
<script type="text/javascript">
	var stationId = <%=stationId%>;
	var context_path = "${pageContext.request.contextPath}";
</script>
<script src="js/m9000/alarmsLogGrid.js"></script>
<div id="alarmsLog">
	<s:url var="fetchAlarmsLogDataUrl" action="fetchAlarmsData">
		<s:param name="stationId" value="%{stationId}"></s:param>
	</s:url>
	<div id="alarmsErrorStatus" align="center" style="background-color: RED"></div>
	<div align="center">
		<s:checkbox theme="simple"  name="autoAlarmsLogRefresh" value="true"
			label="Auto Refresh" cssClass=".tdLabel" />
		<s:label theme="simple" name="lblAutoUpdate" value="Auto Refresh Enable?" cssClass="titleLabel" for="autoAlarmsLogRefresh"/>
		<s:submit theme="simple"
			cssClass="submit" name="btnExportAlarms" value="Export"
			action="getAlarmExports"
			title="Export Alarms log into a csv file"></s:submit>
	</div>
	<sjg:grid id="alarmsLogGrid" caption="Alarms Log Data" dataType="json"
		hidegrid="false" href="%{fetchAlarmsLogDataUrl}" pager="true"
		toppager="true" navigatorCloneToTop="true" navigator="true"
		navigatorAdd="false" navigatorSearch="true"
	    navigatorSearchOptions="{multipleSearch:true}"
		navigatorEdit="false" navigatorView="false" navigatorDelete="false"
		gridModel="gridModel" rowList="25,50,100,150,200,250,300" rowNum="25"
		rownumbers="true" autowidth="true" resizable="false"
		shrinkToFit="true" viewrecords="true" altRows="false" sortable="true"
		sortname="alarmId" sortorder="desc" onDblClickRowTopics="rowselect"
		onGridCompleteTopics="initAlarmsLogGrid" onPagingTopics="storeSelection"
		onSortColTopics="storeSelection" hoverrows="true" scrollrows="true"
		editinline="false" onErrorTopics="showErrorMessage"
		>
		<sjg:gridColumn name="alarmId" index="alarmId" title="id" key="true"
			sortable="true" search="true"
			searchoptions="{sopt:['eq','lt','gt']}" align="center" width="20"/>
		<sjg:gridColumn name="alarmTime" index="alarmTime" title="Alarm Time" key="true"
			sortable="true" formatter="date" formatoptions="{newformat : 'Y-m-d H:i:s', srcformat : 'Y-m-d H:i:s'}" sorttype="date" search="true" searchoptions="{sopt:['eq','ne','lt','gt'], dataInit:datePick, attr:{title:'Your Search Date'}}" align="center" width="70"/>
		<sjg:gridColumn name="ledName" index="ledName" title="Name"
			align="center" sortable="true" width="50" searchtype="select" searchoptions="{sopt:['eq','ne'], value: ':ALL;COMMUNICATION:COMMUNICATION;ONLINE:ONLINE;DISK:DISK;POWER:POWER;TEMPERATURE:TEMPERATURE;CLOCK_SYNC:CLOCK_SYNC;TRIGGER:TRIGGER;USER_ACTION:USER_ACTION'}"/>
		<sjg:gridColumn name="relays" index="relays"
			title="Relays" sortable="true" align="center" width="50" searchtype="select" searchoptions="{sopt:['eq','ne'],value:':ALL;RELAY_1:RELAY_1;RELAY_2:RELAY_2;RELAY_3:RELAY_3;RELAY_4:RELAY_4;RELAY_5:RELAY_5;RELAY_6:RELAY_6;RELAY_7:RELAY_7;RELAY_8:RELAY_8;No Relays set or reset:No Relays set or reset'}"/>
		<sjg:gridColumn name="alarmDescription" index="alarmDescription" title="Description"
			sortable="true" align="center" search="true" searchoptions="{sopt:['cn','nc','bw','bn']}"/>
		<sjg:gridColumn name="ledStatus" index="ledStatus" title="Status"
			formatter="formatStatusImage" sortable="true" align="center" width="25" search="true" searchtype="select" searchoptions="{sopt:['eq','ne'], value:':ALL;GREEN:GREEN;RED:RED;WARBLE:WARBLE;YELLOW:YELLOW;OFF:OFF'}"/>
	</sjg:grid>
</div>
