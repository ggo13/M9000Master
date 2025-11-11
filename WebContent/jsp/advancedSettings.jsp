<%@ include
	file="header.jsp"
	%>
	<!-- <script type="text/javascript"
			src="js/jquery.validate.js"></script>
	<script type="text/javascript"
			src="js/additional-methods.js"></script>
	<script type="text/javascript"
			src="js/jquery-migrate-1.2.1.min.js"></script>

	<script src="js/m9000/advancedSettings.js"></script> -->
	<script type="text/javascript"
			src="js/jquery.validate.js"></script>
	<script type="text/javascript"
			src="js/additional-methods.js"></script>

	<body>
		<s:url var="displayAdvancedChannelSettings"
			   action="displayAdvancedChannelSettings">
		</s:url>
		<s:url var="displayDnp3Configuration"
			   action="displayDnp3Configuration">
		</s:url>
		<sj:tabbedpanel id="advancedSettingsTabs"
						cache="true"
						onChangeTopics="parentTabchange">
			<sj:tab id="advancedDetails"
					href="%{#displayAdvancedChannelSettings}"
					label="Advanced Details" />
			<sj:tab id="dnp3"
					href="%{#displayDnp3Configuration}"
					label="DNP3 Config" />
		</sj:tabbedpanel>
	</body>

	</html>