<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<script>
$("#showMsg").bind("click", (function () {
	$("#showMsg").hide();
}));
</script>
<s:if test="%{infoMessage != null}">
	<div id="showMsg"
		style="text-align:center;width:100%;font-weight:bold;color:blue">
		<s:property value="%{infoMessage}" />
	</div>
</s:if>
<s:elseif test="%{errorMessage != null}">
	<div id="showMsg"
		style="text-align:center;width:100%;font-weight:bold;color:red">
		<s:property value="%{errorMessage}" />
	</div>
</s:elseif>