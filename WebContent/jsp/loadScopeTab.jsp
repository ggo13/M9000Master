<!DOCTYPE HTML>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%
	String stationId = new String();
	stationId = (String) session.getAttribute("stationId");
%>
<script type="text/javascript">
	var stationId = <%=stationId%>;
	var context_path = "${pageContext.request.contextPath}";
</script>

<script src="js/m9000/scopeTab.js"></script>
<s:url var="displayScopeTab" action="displayScopeTab">
	<s:param name="stationId" value="%{stationId}"></s:param>
</s:url>

<sj:div id="loadScopeTab" href="%{displayScopeTab}" indicator="indicatorLoadScopeTab"
	reloadTopics="reloadScopeTab" 
	effect="highlight" effectOptions="{color:'#9999CC'}" effectDuration="3000" cssClass="result ui-widget-content ui-corner-all">
	<img id="indicatorLoadScopeTab" src="images/indicator.gif" alt="Loading..."
	style="display: none" />
</sj:div>
