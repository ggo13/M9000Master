<%@ taglib prefix="s" uri="/struts-tags"%>
<html>
<h4 align="center">USI M9000 Master</h4>
<s:url var="url" action="NextToCreateOrUpdate" method="cancel"
	includeParams="none">
</s:url>
<!--	<s:if test="%{#session.userName != null}">-->
<!--		<h4 align="center">USI M9000 Master <font color="black" style="text-align: right; font-size:10px;"><a href="<s:property value="%{url}"/>">Logout</a></font></h4> -->
<!--	</s:if>-->
<!--	<s:else>-->

<!--	</s:else>-->
<s:if
	test="%{#session.userName != null && stationDetails.systemStationName != null && #session.accessMode != null}">
	<h1>
		User Name:
		<s:property value="%{#session.userName}" />
		| Station Name:
		<s:property value="%{stationDetails.systemStationName}" />
		| <font style="text-align: right; font-size: 10px;"><a
			href="<s:property value="%{url}"/>">Logout</a></font>
	</h1>
</s:if>
<s:elseif test="%{#session.userName != null}">
	<h1>
		User Name:
		<s:property value="%{#session.userName}" />
		| <font color="black" style="text-align: right; font-size: 10px;"><a
			href="<s:property value="%{url}"/>">Logout</a></font>
	</h1>
</s:elseif>
<s:elseif
	test="%{stationDetails.systemStationName != null && #session.accessMode != null}">
	<h1>
		Station Name:
		<s:property value="%{stationDetails.systemStationName}" />
	</h1>
</s:elseif>
<head>

<link href="<s:url value="/css/main.css"/>" rel="stylesheet"
	type="text/css" />
</head>
</html>