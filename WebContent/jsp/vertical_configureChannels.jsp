<%@ include file="header.jsp"%>
<jsp:useBean id="stationInfo" class="com.usi.m9000.config.StationInfo"
	scope="session" />
<jsp:setProperty property="stationName" name="stationInfo" />

<body>
	<script type="text/javascript">
		    function test() {
		        alert("New DFR Added");
		        }
		    function save() {
		        alert("All DFR Saved");
		        }
		    function setAnalogChannel(obj) {
			       alert("Set Analog channels "+obj.options[obj.selectedIndex].value);
			       Session["item"] = obj.options[obj.selectedIndex].value;
		        }
		</script>
	<tr>
		<td colspan="2">
			<p align="left">
				<font size="3" style="font-weight: bold;">User:&nbsp</font><font
					color="green" size="4"><%= stationInfo.getUserName() %></font>
			</p>
			<p align="left">
				<font size="3" style="font-weight: bold;">Station Name:&nbsp</font><font
					color="green" size="4"><%= stationInfo.getSystemStationName() %></font>
			</p>
			<p align="left">
				<font size="3" style="font-weight: bold;">Analog
					Channels:&nbsp</font><font color="green" size="4"><%= stationInfo.getAnalogChannelsCount() %>
				</font>
			</p>
			<p align="left">
				<font size="3" style="font-weight: bold;">Digital
					Channels:&nbsp</font><font color="green" size="4"><%= stationInfo.getDigitalChannelsCount() %>
				</font>
			</p>
			<p align="left">
				<font size="3" style="font-weight: bold;">Total
					Channels:&nbsp</font><font color="green" size="4"><%= stationInfo.getChannelsCount() %></font>
			</p>
			<p align="left">
				<font size="3" style="font-weight: bold;">Minimum number of
					DFRs required:&nbsp</font> <font color="green" size="4"><s:property
						value="%{dfrCounter.last + 1}" /></font>
			</p>
			<h2 align="center">DFR Configurations</h2>
		</td>
	</tr>
	<s:form action="doLogin" method="POST">
		<tr>
			<td colspan="2"><s:actionerror id="userMsgError" theme="jquery"/> <s:fielderror /></td>
		</tr>
		<s:iterator value="dfrCounter" status="status">

			<tr>
				<td colspan="2" align="center" bgcolor="darkgreen"><font
					color="white"><b>DFR <s:property /></b></font></td>
			</tr>
			<s:textfield name="DFRId%{#status.index}" id="DFRId%{#status.index}"
				label="DFR Id" value="DFRId%{#status.index}" />
			<s:textfield name="IPAddress%{#status.index}"
				id="IPAddress%{#status.index}" label="DFR IP Address"
				value="127.0.0.1" />

			<s:select list="analogCnt" listKey="value" listValue="key"
				name="lstAnalog" label="Select Analog channels"
				onchange="javascript:setAnalogChannel(this)">
			</s:select>

			<s:select list="digitalCnt" listKey="value" listValue="key"
				name="lstDigital" label="Select Digital channels">
			</s:select>
		</s:iterator>

	</s:form>
	<div align="center">
		<s:submit theme="simple" name="test" value="Add a new DFR"
			onclick="javascript:test()"></s:submit>
		<s:submit theme="simple" name="saveAll" value="Save"
			onclick="javascript:save()"></s:submit>
	</div>
</body>
</html>