<jsp:useBean id="stationInfo" class="com.usi.m9000.config.StationInfo"
	scope="session" />
<jsp:setProperty property="stationName" name="stationInfo" />
<jsp:setProperty property="analogChannelsCount" name="stationInfo" />
<jsp:setProperty property="digitalChannelsCount" name="stationInfo" />
<html>
<head>
<title>Station Details</title>
</head>
<body>
	<p align="center">
		<font color="#000080" size="5">Hi <%= stationInfo.getUserName() %>!
			<br> Station Name: <%= stationInfo.getSystemStationName() %></font>
	</p>
	<p align="center">
		<font color="#000080" size="5">Total Channels: <%= stationInfo.getChannelsCount() %></font>
	</p>
</body>
</html>