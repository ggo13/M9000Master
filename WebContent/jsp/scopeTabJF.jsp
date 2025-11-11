<%@ taglib prefix="s" uri="/struts-tags"%>

<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>

<% 	String stationId = new String();

	stationId = (String)session.getAttribute("stationId");

  %>

<script type="text/javascript">

	var stationId = <%=stationId%>;

</script>
<script src="js/m9000/scopeTabJF.js"></script>

    <s:url var="displayScopeChart" action="viewSCOPEChart">

   	<s:param name="stationId" value="%{stationId}"></s:param>

   </s:url>
<s:label theme="simple" name="lblAutoUpdate"
			value="Start/Stop" cssClass="titleLabel" />



		<s:checkbox theme="simple" id="jfScopeDataRefresh"
			name="jfScopeDataRefresh" value="true" label="Start/Stop"
			cssClass=".tdLabel" />   
<img id="imgScopeChart" src="<s:property value='%{#displayScopeChart}'/>"/>