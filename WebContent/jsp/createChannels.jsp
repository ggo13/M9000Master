<%@ include file="header.jsp"%>
<jsp:useBean id="stationInfo" scope="session"
	class="com.usi.m9000.config.StationInfo" />
<jsp:setProperty property="userName" name="stationInfo" />
<body>
	<s:form action="createRemoteId" method="POST">
		<tr>
			<td colspan="2">Create Remote Id</td>
		</tr>
		<tr>
			<td colspan="2"><s:actionerror id="userMsgError" theme="jquery"/> <s:fielderror /></td>
		</tr>
		<s:textfield name="stationName" label="Enter Remote Id: " />
		<s:submit value="Create" align="center" />

	</s:form>
</body>
</html>