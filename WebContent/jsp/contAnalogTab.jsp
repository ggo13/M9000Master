<!DOCTYPE HTML>
<%@ taglib prefix="s" uri="/struts-tags"%> <%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sjdt" uri="/struts-jquery-datatables-tags"%>
<%@ taglib prefix="sjg" uri="/struts-jquery-grid-tags"%>
<%@ page import="com.usi.m9000.dto.UsersDTO"%>
<% 	String stationId = new String();
	stationId = (String)session.getAttribute("stationId");
  %>
<script type="text/javascript">
	var stationId = <%=stationId%>;
	var userRole = "${session.userDetails.role}";
</script>
<script src="js/m9000/contAnalogGrid.js"></script>
<script>
</script>
<div id="contAnalog">
	<s:url var="fetchContDataUrl" action="fetchContAnalogData">
	</s:url>
	<s:url var="editContDataurl" action="editContAnalogData">
	</s:url>
	<s:url var="showContDataurl" action="showContAnalogData">
	</s:url>
	<div id="contAnalogStatus" align="center" style="background-color: RED">
		<s:actionerror id="userMsgError" theme="jquery"/>
		<s:actionmessage  id="userMsgInfo" theme="jquery"/>
	</div>
			<table class="inlineTable" border=0 cellspacing=0 cellpadding=0>
			<tr><td colspan="2" width="50%" align="right">
			<s:label theme="simple" name="lblAnalogStartTime" value="Start Time: " for="analogStartTime"/>
			<sj:datepicker id="analogStartTime" theme="simple" name="analogStartTime" label="Start Time" value="today" timepicker="true" timepickerShowSecond="true" maxDate="0" timepickerFormat="HH:mm:ss"
			timepickerStepMinute="1" onCompleteTopics="onDpClose"/></td><td colspan="2" width="50%" align="left">
			And <s:label theme="simple" name="lblTotalTime" value="Enter time in seconds or minutes: " for="totalTime"/>
				<s:textfield theme="simple" size="5" 
								id="totalTime"
								name="totalTime"
								label="Enter number of seconds or minutes from start time "
								title="Total seconds or minutes from the start time" onkeyup="updateEndTime()" ></s:textfield>
			<s:select theme="simple" name="lstTimes" id="lstTimes" list="{'Seconds','Minutes'}" onchange="clearTimeAndupdateEndTime()"/></td></tr>
			<tr><td colspan="2" align="right"> 
			<s:label theme="simple" name="lblAnalogEndTime" value="End Time: " for="analogEndTime"/>
			<sj:datepicker id="analogEndTime" name="analogEndTime" onBeforeShowDayTopics = "beforeDatepickerShow" label="End Time" timepicker="true" timepickerShowSecond="true" timepickerFormat="HH:mm:ss"
			minDate="$('#analogStartTime').datepicker('getDate')" maxDate="$('#analogStartTime').datepicker('getDate')"  onCompleteTopics="onEndClose"/></td></tr>
			<tr><td colspan="2" align="right"> <s:submit value="Show Data" theme="simple" id="btnShowContAnalogData" name="btnShowContAnalogData" onClick="openContAnalogInNewWindow();return false;"/> </td></tr>
	<sjg:grid id="contAnalogGrid" caption="Continuous Oscillography Data" dataType="json" 
		hidegrid="false" href="%{fetchContDataUrl}" pager="true"
		toppager="true" navigatorCloneToTop="true" navigator="true"
		navigatorAdd="false" navigatorSearch="false"
		navigatorEdit="false" navigatorView="false" navigatorDelete="true"
		navigatorDeleteOptions="{
	                        height:280,
	                        reloadAfterSubmit:true,
	                                afterSubmit:function(response, postdata) {
	                                return isError(response.responseText);
	                                 }
	                        }"
		gridModel="gridModel" rowList="25,50,100,150,200,250,300" rowNum="25"
		rownumbers="true" autowidth="true" resizable="false"
		shrinkToFit="true" viewrecords="true" altRows="false" sortable="true"
		sortname="id" sortorder="desc" onDblClickRowTopics="viewContAnalog"
		onGridCompleteTopics="initContAnalogGrid"  hoverrows="true" scrollrows="true"
		editurl="%{editContDataurl}" editinline="false" multiselect="true"
		multiboxonly="true" onErrorTopics="showErrorMessage">
		<sjg:gridColumn name="id" index="id" title="Id" key="true"
			formatter="integer" sortable="true" sorttype="int" search="true"
			searchoptions="{sopt:['eq','lt','gt']}" align="center" width="10"/>
		<sjg:gridColumn name="contDataType" index="contDataType" title="Cont. Data Type"
			align="center" sortable="false" width="30"/>
		<sjg:gridColumn name="startDateTime" index="startDateTime"
			title="Start Date/Time" sortable="true" formatter="date" formatoptions="{newformat : 'Y-m-d H:i:s', srcformat : 'Y-m-d H:i:s'}" align="center" width="70"/>
		<sjg:gridColumn name="endDateTime" index="endDateTime"
			title="End Date/Time" sortable="true" formatter="date" formatoptions="{newformat : 'Y-m-d H:i:s', srcformat : 'Y-m-d H:i:s'}" align="center" />
		<sjg:gridColumn name="fileName" index="fileName" title="file_name"
			hidden="true" hidedlg="true" align="left" sortable="false" width="70"/>
	</sjg:grid>
</div>
