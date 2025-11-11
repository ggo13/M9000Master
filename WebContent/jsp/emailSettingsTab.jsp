<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sjdt" uri="/struts-jquery-datatables-tags" %>
<script
    src="/M9000Master/struts/utils.js"
    type="text/javascript">
</script>
<script
    src="/M9000Master/struts/xhtml/validation.js"
    type="text/javascript">
</script>

<script>
    /*Destroy previous instances of jQuery Dialogs  */
	$(".ui-dialog-content").each(function(i,elt){
		if ($(elt).dialog){
			  $(this).dialog('destroy').remove();		  
		}
	});
</script>

<s:url var="displayEmailSettingsTab" action="displayEmailProperties">
</s:url>
<s:url var="displayEmailsList" action="displayEmailsList">
</s:url>
<sj:tabbedpanel id="emailSettingtabs" cache="true" onChangeTopics="adminTabchange">

  <sj:tab id="emailSetting" href="%{#displayEmailSettingsTab}" label="Email Settings"/>
  <sj:tab id="emailsList" href="%{#displayEmailsList}" label=" Emails Subscription List"/>

 </sj:tabbedpanel>
