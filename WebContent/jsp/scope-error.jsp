<!DOCTYPE HTML>
<%@ taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<title>M9000 Error</title>
</head>
<script src="js/m9000/scopeTab.js"></script>
<script type="text/javascript">
$(document).ready( function() {
	stopTimer("scopeTab");
	startScopeLoadTimer();
});
</script>

<body>

	<div style="background-color:RED;"><p align="center">Error occurred. Please try again. If error	persists contact system administrator.</p></div>
		
		<s:if test="%{exception != null && !exception.isEmpty()}">
		<div>
			<s:textfield size="100" value="%{exception}" readonly="true" label="Exception name"/>
		</div>
		<div>
			<s:textarea id="errorResult" name="errorResult" readonly="true" cssStyle="width:100%;height:80%" title="Error Occured" wrap="true" value="%{exceptionStack}"> </s:textarea>
		</div>
		</s:if>
		<s:if test="%{errorMessage != null && !errorMessage.isEmpty()}">
			<div align="center" style="font:bold">
				Exception Occurred: ${errorMessage}
			</div>
		</s:if>
</body>
</html>