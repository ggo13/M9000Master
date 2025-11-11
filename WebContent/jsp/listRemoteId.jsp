<%@ include file="header.jsp"%>
<jsp:useBean id="stationInfo" scope="session"
	class="com.usi.m9000.config.StationInfo" />
<jsp:setProperty property="userName" name="stationInfo" />
<% session.setAttribute("stationInfo", stationInfo);%>
<body>
	<s:form action="createRemoteId" method="POST"
		focusElement="createRemoteId_stationName">
		<tr>
			<td colspan="2">Create Remote Id</td>
		</tr>
		<tr>
			<td colspan="2"><s:actionerror id="userMsgError" theme="jquery"/> <s:fielderror /></td>
		</tr>
		<s:textfield name="stationName" label="Enter Remote Id: " />
		<s:textfield name="analogChannelsCount"
			label="Enter Total Analog Channels: " />
		<s:textfield name="digitalChannelsCount"
			label="Enter Total Digital Channels: " />
		<s:submit cssClass="submit" value="Create" align="center" />

	</s:form>
</body>
</html>