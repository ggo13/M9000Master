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
		        alert("DFR Saved");
		        }
		    function saveAll() {
		        alert("All DFR Saved");
		        }
		</script>
	<table>
		<tbody>
			<tr>
				<td colspan="2">
					<p align="left">
						<font size="3" style="font-weight: bold;">User:&nbsp</font><font
							color="green" size="4"><%= stationInfo.getUserName() %></font>
					</p>
					<p align="left">
						<font size="3" style="font-weight: bold;">Station
							Name:&nbsp</font><font color="green" size="4"><%= stationInfo.getSystemStationName() %></font>
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
				</td>
			</tr>
		</tbody>
	</table>
	<h2 align="center">DFR Configurations</h2>

	<div id="container">
		<s:iterator value="dfrCounter" status="status">
			<s:if test="%{#status.index % 3 == 0}">
				<div id="row">
					<div id="col">
						<s:form action="doLogin" method="POST">

							<tr>
								<td colspan="2" align="center"><b>DFR <s:property /></b></td>
							</tr>
							<tr>
								<td colspan="2"><s:actionerror id="userMsgError" theme="jquery"/> <s:fielderror /></td>
							</tr>
							<s:textfield name="DFRId%{#status.index}"
								id="DFRId%{#status.index}" label="DFR Id"
								value="DFRId%{#status.index}" />
							<s:textfield name="IPAddress%{#status.index}"
								id="IPAddress%{#status.index}" label="DFR IP Address"
								value="127.0.0.1" />

							<s:select list="analogCnt" listKey="value" listValue="key"
								name="lstAnalog" label="Select Analog channels">
							</s:select>

							<s:select list="digitalCnt" listKey="value" listValue="key"
								name="lstDigital" label="Select Digital channels">
							</s:select>
							<s:submit align="center" name="save" value="Save DFR"
								onclick="javascript:save()"></s:submit>
						</s:form>
					</div>
					<s:if test="%{#status.index == 0}">
				</div>
			</s:if>

			</s:if>
			<s:else>
				<s:div id="col">
					<s:form action="doLogin" method="POST">

						<tr>
							<td colspan="2" align="center"><b>DFR <s:property /></b></td>
						</tr>
						<tr>
							<td colspan="2"><s:actionerror id="userMsgError" theme="jquery"/> <s:fielderror /></td>
						</tr>
						<s:textfield name="DFRId%{#status.index}"
							id="DFRId%{#status.index}" label="DFR Id"
							value="DFRId%{#status.index}" />
						<s:textfield name="IPAddress%{#status.index}"
							id="IPAddress%{#status.index}" label="DFR IP Address"
							value="127.0.0.1" />

						<s:select list="analogCnt" listKey="value" listValue="key"
							name="lstAnalog" label="Select Analog channels">
						</s:select>

						<s:select list="digitalCnt" listKey="value" listValue="key"
							name="lstDigital" label="Select Digital channels">
						</s:select>
						<s:submit align="center" name="save" value="Save DFR"
							onclick="javascript:save()"></s:submit>
					</s:form>
				</s:div>
			</s:else>
		</s:iterator>
	</div>
	<div align="center">
		<s:submit theme="simple" name="test" value="Add a new DFR"
			onclick="javascript:test()"></s:submit>
		<s:submit theme="simple" name="saveAll" value="Save All"
			onclick="javascript:saveAll()"></s:submit>
	</div>
</body>
</html>