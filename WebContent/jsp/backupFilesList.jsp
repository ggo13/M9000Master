<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<script>
$(document).ready( function() { 
$('[id^="selectedBackupFile"]').change(function( event ) {
	console.log("List selected...");
	  $('[id^="btnSubmitImportBackup"]').prop('disabled',false);
});
});
</script>
<s:if test="%{mapBackupFiles.size > 0}"> 
<s:updownselect
                        theme="simple" size="10" 
                        id="selectedBackupFile" name="selectedBackupFile" 
                        list="mapBackupFiles" listKey="key" listValue="value" allowSelectAll="false" allowMoveDown="false"
                        allowMoveUp="false" multiple="false"></s:updownselect>
</s:if>
<s:else>
        	No backup files available to import
</s:else>
