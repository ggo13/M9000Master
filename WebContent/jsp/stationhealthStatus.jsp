<!DOCTYPE HTML>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<script src="js/m9000/healthMonitor.js"></script>
<s:url var="updateStationHealthURL" value="/updateStationHealthStatus.action" />
<sj:div id="div2" href="%{updateStationHealthURL}" indicator="indicator2"
	reloadTopics="updateStationHealthTopic" 
	effect="highlight" cssClass="result ui-widget-content ui-corner-all">
	<img id="indicator2" src="images/indicator.gif" alt="Loading..."
	style="display: none" />
</sj:div>
