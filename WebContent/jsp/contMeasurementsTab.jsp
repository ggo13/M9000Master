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
	var userRole = "${session.userDetails.role}";
</script>
<script src="js/m9000/contMeasurementsGrid.js"></script>
<style type="text/css">
    .ui-jqgrid tr.jqgrow td {
        word-wrap: break-word; /* IE 5.5+ and CSS3 */
        white-space: pre-wrap; /* CSS3 */
        white-space: -moz-pre-wrap; /* Mozilla, since 1999 */
        white-space: -pre-wrap; /* Opera 4-6 */
        white-space: -o-pre-wrap; /* Opera 7 */
        overflow: hidden;
        height: auto;
        vertical-align: middle;
        padding-top: 3px;
        padding-bottom: 3px
    }
</style>
<div id="contMeasurementsStatus" align="center" style="background-color: RED">
	<s:actionerror id="userMsgError" theme="jquery"/>
	<s:actionmessage  id="userMsgInfo" theme="jquery"/>
</div>

<table class="inlineTable">
	<s:url var="fetchContDataUrl" action="fetchContMeasurementsData">
	</s:url>
	<s:url var="editContDataurl" action="editContMeasurementsData">
	</s:url>
			<tr><td colspan="2" width="50%" align="right"> 
			<s:label theme="simple" name="lblMeasurementsStartTime" value="Start Time: " for="measurementsStartTime"/>
			<sj:datepicker theme="simple" id="measurementsStartTime" name="measurementsStartTime" label="Start Time" value="today" timepicker="true" timepickerShowSecond="true" maxDate="0" timepickerFormat="HH:mm:ss"  timepickerStepMinute="1" onCompleteTopics="onMeasurementsStartTime"/></td><td colspan="2" width="50%" align="left">
			And <s:label theme="simple" name="lblMeasurementTotalTime" value="Enter time in seconds or minutes: " for="measurementTotalTime"/>
				<s:textfield theme="simple" size="5" 
								id="measurementTotalTime"
								name="measurementTotalTime"
								label="Enter time in seconds or minutes"
								title="Total seconds or minutes from the start time" onkeyup="updateMeasurementEndTime()" ></s:textfield>
			<s:select theme="simple" name="lstMeasurementTimes" id="lstMeasurementTimes" list="{'Seconds','Minutes'}" onchange="clearTimeAndupdateMeasurementEndTime()"/></td></tr>
			<tr><td colspan="2" align="right">  
			<s:label theme="simple" name="lblMeasurementsEndTime" value="End Time: " for="measurementsEndTime"/>
			<sj:datepicker theme="simple" id="measurementsEndTime" name="measurementsEndTime" label="End Time" timepicker="true" timepickerShowSecond="true" timepickerFormat="HH:mm:ss" minDate="$('#measurementsStartTime').datepicker('getDate')" maxDate="$('#measurementsStartTime').datepicker('getDate')"  onCompleteTopics="onMeasurementsEndTime"/></td></tr>
			<tr><td colspan="4" align="center">  
			<s:label theme="simple" name="lblMeasurementTypes" value="Select Any or ALL Measurement Types to fetch continuous data" cssClass="titleLabel" />
			</td></tr>
			<tr><td colspan="4" align="center"> 
				<s:url var="fetchMeasurementTypes"
				action="fetchMeasurementTypes">
			</s:url>
			<s:checkbox theme="simple" name="selectAll" value="true" label="Select All" cssClass=".tdLabel"/> 
					<s:label theme="simple" name="lblAutoUpdate" value="Select All" cssClass="titleLabel" for="selectAll"/>
			<sj:checkboxlist theme="simple" href="%{fetchMeasurementTypes}" id="measurementTypes" reloadTopics="reloadMeasurementTypes" onChangeTopics="measurementTypeChange"
				name="measurementTypes" list="lstMeasurementTypes" label="Measurement Types" formIds="NextToCreateOrUpdate" value="Rms"/></td></tr>
			<tr><td colspan="4" align="center"> <s:submit value="Show Data" theme="simple" id="btnShowContData" name="btnShowContData" onClick="openContMeasurementsNewWindow();return false;"/> </td></tr>
	<sjg:grid id="contMeasurementsGrid" caption="Continuous Measurements Data" dataType="json" 
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
		sortname="id" sortorder="desc" onDblClickRowTopics="viewContMeasurements"
		onGridCompleteTopics="initContMeasurementsGrid"  hoverrows="true" scrollrows="true"
		editurl="%{editContDataurl}" editinline="false" multiselect="true" 
		multiboxonly="true" onErrorTopics="showErrorMessage">
		<sjg:gridColumn name="id" index="id" title="Id" key="true"
			formatter="integer" sortable="true" sorttype="int" search="true"
			searchoptions="{sopt:['eq','lt','gt']}" align="center" width="10"/>
		<sjg:gridColumn name="contDataType" index="contDataType" title="Exported Measurements"
			align="left" sortable="false" width="50"/>
		<sjg:gridColumn name="startDateTime" index="startDateTime"
			title="Start Date/Time" sortable="true" formatter="date" formatoptions="{newformat : 'Y-m-d H:i:s', srcformat : 'Y-m-d H:i:s'}" align="center" />
		<sjg:gridColumn name="endDateTime" index="endDateTime"
			title="End Date/Time" sortable="true" formatter="date" formatoptions="{newformat : 'Y-m-d H:i:s', srcformat : 'Y-m-d H:i:s'}" align="center" />
		<sjg:gridColumn name="fileName" index="fileName" title="file_name"
			hidden="true" hidedlg="true" align="left" sortable="false" />
	</sjg:grid>
</table>