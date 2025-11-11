<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sjdt" uri="/struts-jquery-datatables-tags" %>
<script type="text/javascript" src="js/jquery.validate.js"></script>
<script type="text/javascript" src="js/additional-methods.js"></script>
<script src="js/m9000/m9kLDAPSettings.js" type="text/javascript">
</script>

<s:form id="ldapForm" cssStyle="width:80%" theme="xhtml" action="saveLDAPConfigurations">
	<s:url var="urlSaveLDAPConfigurations" action="saveLDAPConfigurations" />
 <tr><td colspan="2" ><p align="center" class="ui-widget-header">Email Server Settings<p></td></tr>
 	<sj:submit id="btnSave" value="Save" button="true" buttonText="Submit" href="%{urlSaveLDAPConfigurations}" formIds="ldapForm"
			validate="true" dataType="json" targets="x" 
			onSuccessTopics="SavedTopic" onErrorTopics="SavedErrorTopic" />	
 
	<s:checkbox name="LDAPSettingsDTO.enableLdap"
			id="enableLdap" label="Enable LDAP"
			value="%{LDAPSettingsDTO.enableLdap}" />
	<s:textfield id="ldap_ldapServerHost" name="LDAPSettingsDTO.ldapServerHost" label="Host LDAP Server" value="%{LDAPSettingsDTO.ldapServerHost}"/>
	<s:textfield id="ldap_ldapPort" name="LDAPSettingsDTO.ldapPort" label="LDAP Port" value="%{LDAPSettingsDTO.ldapPort}"/>
	<s:textfield id="ldap_ldapAdminGroup" name="LDAPSettingsDTO.ldapAdminGroup" label="Admin Group" value="%{LDAPSettingsDTO.ldapAdminGroup}"/>
	<s:textfield id="ldap_ldapGuestGroup" name="LDAPSettingsDTO.ldapGuestGroup" label="Guest Group" value="%{LDAPSettingsDTO.ldapGuestGroup}"/>

	<sj:submit id="btnSaveBottom" value="Save" button="true" buttonText="Submit" href="%{urlSaveLDAPConfigurations}" formIds="ldapForm"
			validate="true" dataType="json" targets="x" 
			onSuccessTopics="SavedTopic" onErrorTopics="SavedErrorTopic" />	
</s:form>