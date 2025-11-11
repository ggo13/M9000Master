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
<script src="js/m9000/ddrMeasurementsGrid.js"></script>
<div id="ddrMeasurements">
	<s:url var="fetchDdrMeasurementsDataUrl" action="fetchDdrMeasurementsData">
		<s:param name="stationId" value="%{stationId}"></s:param>
	</s:url>
	<s:url var="editDdrMeasurementsDataurl" action="editDdrMeasurementsData">
		<s:param name="stationId" value="%{stationId}"></s:param>
	</s:url>
	<div id="ddrMeasurementsStatus" align="center" style="background-color: RED"></div>
	<div align="center">
		<s:checkbox theme="simple" name="autoDdrMeasurementsRefresh" value="true"
			label="Auto Refresh" cssClass=".tdLabel" />
		<s:label theme="simple" name="lblAutoUpdate" value="Auto Refresh Enable?" cssClass="titleLabel" for="autoDdrMeasurementsRefresh"/>
	</div>
	<sjg:grid id="ddrMeasurementsGrid" caption="DDR Measurements Data"
		dataType="json" hidegrid="false" href="%{fetchDdrMeasurementsDataUrl}"
		pager="true" toppager="true" navigatorCloneToTop="true"
		navigator="true" navigatorAdd="false"
		navigatorSearch="true"
	    navigatorSearchOptions="{multipleSearch:true}"
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
		sortname="faultId" sortorder="desc" onDblClickRowTopics="viewDdrMeasurements"
		onGridCompleteTopics="initddrMeasurementsGrid" onPagingTopics="storeSelection"
		onSortColTopics="storeSelection" hoverrows="true" scrollrows="true"
		editurl="%{editDdrMeasurementsDataurl}" editinline="false"
		multiselect="true" multiboxonly="true" onErrorTopics="showErrorMessage"
		>
		<sjg:gridColumn name="faultId" index="faultId" title="Id" key="true"
			formatter="integer" sortable="true" sorttype="int" search="true"
			searchoptions="{sopt:['eq','lt','gt']}" align="center" width="30"/>
		<sjg:gridColumn name="displayTime" index="displayTime"
			title="Date-Time" sortable="true" align="center" width="70" search="true" searchoptions="{sopt:['eq','ne','lt','gt'], dataInit:datePick, attr:{title:'Your Search Date'}}"/>
		<sjg:gridColumn name="length" index="length" title="Length"
			sortable="true" align="center" width="20" search="true" searchoptions="{sopt:['eq','lt','gt']}"/>
		<sjg:gridColumn name="fileName" index="fileName" title="file_name"
			hidden="true" hidedlg="true" align="left" sortable="false" />
	</sjg:grid>
</div>
