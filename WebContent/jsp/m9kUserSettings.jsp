<%@ include file="header.jsp"%>
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
<s:url var="displayUserTab" action="displayUsers">
</s:url>
<s:url var="displayEmailSettingsTab" action="displayEmailSettings">
</s:url>
<s:url var="displayHierarchyTab" action="displayHierarchy">
</s:url>
<!--   
<s:url var="displaySystemSettingsTab" action="displaySystemSettings">
</s:url>
-->
<sj:tabbedpanel id="admintabs" cache="true" onChangeTopics="parentTabchange">

  <sj:tab id="users" href="%{#displayUserTab}" label="Manage Users"/>
  <sj:tab id="emails" href="%{#displayEmailSettingsTab}" label="Manage Email Settings"/>
  <!-- sj:tab id="hierarchy" href="%{#displayHierarchyTab}" label="Manage Hierarchy"/ -->
  <!--  sj:tab id="system" href="%{#displaySystemSettingsTab}" label="Manage Email Settings"/ -->

 </sj:tabbedpanel>
