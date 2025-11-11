<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sjdt" uri="/struts-jquery-datatables-tags" %>
<script type="text/javascript" src="js/jquery.validate.js"></script>
<script type="text/javascript" src="js/additional-methods.js"></script>
<script src="js/m9000/m9kEmailSettings.js" type="text/javascript">
</script>

<s:form id="emailForm" cssStyle="width:80%" theme="xhtml" action="saveEmailConfigurations">
	<s:url var="urlSaveEmailConfigurations" action="saveEmailConfigurations" />
 <tr><td colspan="2" ><p align="center" class="ui-widget-header">Email Server Settings<p></td></tr>
 	<sj:submit id="btnSave" value="Save" button="true" buttonText="Submit" href="%{urlSaveEmailConfigurations}" formIds="emailForm"
			validate="true" dataType="json" targets="x" 
			onSuccessTopics="SavedTopic" onErrorTopics="SavedErrorTopic" />	
 
	<s:checkbox name="emailSettingsDTO.enableEmail"
			id="enableEmail" label="Enable Email"
			value="%{emailSettingsDTO.enableEmail}" />
	<s:textfield id="email_emailServerHost" name="emailSettingsDTO.emailServerHost" label="Host Email Server" value="%{emailSettingsDTO.emailServerHost}"/>
	<s:textfield id="email_smtpPort" name="emailSettingsDTO.emailSmtpPort" label="SMTP Port" value="%{emailSettingsDTO.emailSmtpPort}"/>
	<s:textfield id="email_fromEmail" name="emailSettingsDTO.fromEmail" label="From Email" value="%{emailSettingsDTO.fromEmail}"/>
	<s:password showPassword="true" id="email_emailServerPassword" name="emailSettingsDTO.emailServerPassword" label="Email Password" value="%{emailSettingsDTO.emailServerPassword}"/>
	 <tr><td colspan="2" ><p align="center" class="ui-widget-header">Daily Status Report settings<p></td></tr>
	<s:checkbox name="emailReportsSettingsDTO.enableDailyStatusEmails"
			id="email_enableDailyStatusEmails" label="Enable Daily Status Email"
			value="%{emailReportsSettingsDTO.enableDailyStatusEmails}" />

	<s:textfield id="eds_dailyStatusReportTitle" name="emailReportsSettingsDTO.dailyStatusReportTitle" label="Daily Status Report Title" value="%{emailReportsSettingsDTO.dailyStatusReportTitle}"/>
	<s:textfield id="eds_stationSpecificReportTitle" name="emailReportsSettingsDTO.stationSpecificStatusReportTitle" label="Station Specific Status Alarms Report Title" value="%{emailReportsSettingsDTO.stationSpecificStatusReportTitle}"/>
	<sj:datepicker id="eds_dailyStatusReportTime" name="emailReportsSettingsDTO.dailyStatusReportTime" tooltip="REQUIRES RESTART OF WEB SERVER" label="Report Time of day" timepicker="true" timepickerOnly="true" value="%{emailReportsSettingsDTO.dailyStatusReportTime}"/>
	<s:textfield id="eds_dailyStatusRepeatInterval" name="emailReportsSettingsDTO.dailyStatusRepeatInterval" tooltip="REQUIRES RESTART OF WEB SERVER" label="Status Report Run Frequency in Minutes" value="%{emailReportsSettingsDTO.dailyStatusRepeatInterval}"/>
	<tr><td colspan="2" ><p align="center" class="ui-widget-header">SER Report settings<p></td></tr>
	<s:checkbox name="emailReportsSettingsDTO.enableSerEmail"
			id="email_enableSerEmail" label="Enable SER Email"
			value="%{emailReportsSettingsDTO.enableSerEmail}" />
	<s:select id="emailser_serEmailFileType" name="emailReportsSettingsDTO.serEmailFileType" list="{'PDF','CSV'}" label="emailReportsSettingsDTO.serEmailFileType" />
	<s:textfield id="emailser_serEmailAttachementSizeLimit" name="emailReportsSettingsDTO.serEmailAttachementSizeLimit" label="SER Email Attachment Size Limit (MB)" value="%{emailReportsSettingsDTO.serEmailAttachementSizeLimit}"/>
	<s:textfield id="emailser_serRunFrequency" name="emailReportsSettingsDTO.serRunFrequency" tooltip="REQUIRES RESTART OF WEB SERVER" label="SER Report Run Frequency in Minutes" value="%{emailReportsSettingsDTO.serRunFrequency}"/>
	<tr><td colspan="2" ><p align="center" class="ui-widget-header">Fault Email settings<p></td></tr>
		<s:checkbox name="emailReportsSettingsDTO.enableFaultEmail"
				id="email_enableFaultEmail" label="Enable Fault Email"
				value="%{emailReportsSettingsDTO.enableFaultEmail}" />
	
		<s:checkbox name="emailReportsSettingsDTO.enableFaultsBooleanLogicFilter"
		id="emailfault_enableFaultsBooleanLogicFilter" label="Enable Boolean Logic Filter"
		value="%{emailReportsSettingsDTO.enableFaultsBooleanLogicFilter}" />
		<s:checkbox name="emailReportsSettingsDTO.enableFaultsWithAttachment"
		id="emailfault_enableFaultsWithAttachment" label="Enable Faults Attachments"
		value="%{emailReportsSettingsDTO.enableFaultsWithAttachment}" />
		<s:textfield id="email_fault_faultEmailAttachementSizeLimit" name="emailReportsSettingsDTO.faultEmailAttachementSizeLimit" label="Fault Email Attachment Size Limit (MB)" value="%{emailReportsSettingsDTO.faultEmailAttachementSizeLimit}"/>
		<s:textfield id="emailfault_faultsEmailDailyLimit" name="emailReportsSettingsDTO.faultsEmailDailyLimit" label="Faults Email Daily Limit" value="%{emailReportsSettingsDTO.faultsEmailDailyLimit}"/>
 	<tr><td colspan="2" ><p align="center" class="ui-widget-header">Configuration Change Email Settings<p></td></tr>
 	<s:checkbox name="emailReportsSettingsDTO.enableConfigChangeEmail"
				id="email_enableConfigChangeEmail" label="Enable Configuration Change email"
				value="%{emailReportsSettingsDTO.enableConfigChangeEmail}" />
 	<tr><td colspan="2" ><p align="center" class="ui-widget-header">Master Health Polling Status<p></td></tr>
 	<s:checkbox name="emailReportsSettingsDTO.masterHealthStatusPoll"
				id="email_masterHealthStatusPoll" label="Enable Master Health Poll"
				value="%{emailReportsSettingsDTO.masterHealthStatusPoll}" />
	<sj:submit id="btnSaveBottom" value="Save" button="true" buttonText="Submit" href="%{urlSaveEmailConfigurations}" formIds="emailForm"
			validate="true" dataType="json" targets="x" 
			onSuccessTopics="SavedTopic" onErrorTopics="SavedErrorTopic" />	
</s:form>