<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sjdt" uri="/struts-jquery-datatables-tags"%>
<%@ taglib prefix="sjg" uri="/struts-jquery-grid-tags"%>
<%@ page import="com.usi.m9000.dto.UsersDTO"%>
<%@ page import="com.usi.m9000.dto.StationDTO"%>
<% 	String stationId = new String();
	stationId = (String)session.getAttribute("stationId");
	String userRole = ((UsersDTO)session.getAttribute("userDetails")).getRole();
	String userName = ((UsersDTO)session.getAttribute("userDetails")).getUserName();
//	Integer contAnalogLimit = (Integer) request.getAttribute("contAnalogTimeLimit");
//	Integer contDataLimit = (Integer) request.getAttribute("contDataTimeLimit");
	Integer contAnalogLimit = ((StationDTO) session.getAttribute("stationDetails")).getContOscExportTimeLimit();
	Integer contDataLimit = ((StationDTO) session.getAttribute("stationDetails")).getContMeasurementsExportTimeLimit();

  %>
<body>
<script type="text/javascript">
	var stationId = <%=stationId%>;
	var contAnalogLimit =  <%=contAnalogLimit%>;
	var contDataLimit =  <%=contDataLimit%>
	console.log("Cont Osc data limit "+contAnalogLimit);
	console.log("Cont Measurement data limit "+contDataLimit);
</script>
<style type="text/css">
table.ui-pg-table {
width:500px;
font-size: 12px;
}
.ui-jqgrid .ui-pg-table td {
 font-size: 10px;
}
.ui-jqgrid .ui-pg-input {
  height:18px;
}
.ui-jqgrid .ui-pg-button {
  width:15px;
}
</style>
<script type="text/javascript" src="js/jquery.validate.js"></script>
<script type="text/javascript" src="js/additional-methods.js"></script>
<script src="js/m9000/stationSpecificTabs.js"></script>
<div id="changepanel"></div>
<div id="infopanel"></div>
<div class="contextMenu" id="rightClickMenu" style="display:none; width:400px;">
        <ul style="width: 400px; font-size: 65%;">
            <li id="view">
                <span class="ui-icon ui-icon-plus" style="float:left"></span>
                <span style="font-size:130%; font-family:Verdana">View</span>
            </li>
            <li id="save">
                <span class="ui-icon ui-icon-pencil" style="float:left"></span>
                <span style="font-size:130%; font-family:Verdana">Save</span>
            </li>                
        </ul>
    </div>
   <s:url var="displayFaultsTab" action="displayFaultsData">
   	<s:param name="stationId" value="%{stationId}"></s:param>
   </s:url>
   <s:url var="displaySerTab" action="displaySerData">
   	<s:param name="stationId" value="%{stationId}"></s:param>
   </s:url>
   <s:url var="displayDDRAnalogTabs" action="displayDdrAnalogData">
   	<s:param name="stationId" value="%{stationId}"></s:param>
   </s:url>
   <s:url var="displayDDRMeasurementsTabs" action="displayDdrMeasurementsData">
   	<s:param name="stationId" value="%{stationId}"></s:param>
   </s:url>
	<s:url var="displayContAnalogTab" action="displayContAnalogData">
   	<s:param name="stationId" value="%{stationId}"></s:param>
   </s:url>
   <s:url var="displayContMeasurementsTab" action="displayContMeasurementsData">
   	<s:param name="stationId" value="%{stationId}"></s:param>
   </s:url>
   <s:url var="displayAlarmsTab" action="displayAlarmsData">
   	<s:param name="stationId" value="%{stationId}"></s:param>
   </s:url>
    <s:url var="displayScopeTab" action="displayScopeTab">
   		<s:param name="stationId" value="%{stationId}"></s:param>
   </s:url>
    <s:url var="loadScopeTab" action="loadScopeTab">
   		<s:param name="stationId" value="%{stationId}"></s:param>
   </s:url>

    <s:url var="displayScopeTabJF" action="displayScopeTabJF">
   		<s:param name="stationId" value="%{stationId}"></s:param>
   </s:url>
    <s:url var="displayHealthMonitor" action="displayHealthMonitor">
   		<s:param name="stationId" value="%{stationId}"></s:param>
   </s:url>
      <s:url var="displayDnp3Config" action="displayDnp3Config">
   		<!-- <s:param name="stationId" value="%{stationId}"></s:param> -->
   </s:url>
   <div id="errorStatus" align="center" class="errorContainer" style="display: none"><s:actionerror id="userMsgError" theme="jquery"/> </div>
   <sj:tabbedpanel id="localtabs" cache="true" cssStyle="width:100%;height:100%; margin-left: auto; margin-right: auto;" onChangeTopics="tabchange">
		<sj:tab id="faultTab" href="%{#displayFaultsTab}" label="DFR"/>
		<sj:tab id="serTab" href="%{#displaySerTab}" label="SER"/>
		<sj:tab id="ddrAnalogTab" href="%{#displayDDRAnalogTabs}" label="DDR Oscillography"/>
		<sj:tab id="ddrMeasurementsTab" href="%{#displayDDRMeasurementsTabs}" label="DDR Measurements"/>
		<sj:tab id="contAnalogTab" href="%{#displayContAnalogTab}" label="Continuous Oscillography"/>
		<sj:tab id="contMeasurementsTab" href="%{#displayContMeasurementsTab}" label="Continuous Measurements"/>
		<sj:tab id="scopeTab" href="%{#loadScopeTab}" label="SCOPE"/>
		<sj:tab id="alarmsTab" href="%{#displayAlarmsTab}" label="Alarm Logs"/>
		<sj:tab id="healthMonitorTab" href="%{#displayHealthMonitor}" label="Health Monitor"/>
	</sj:tabbedpanel>
</body>