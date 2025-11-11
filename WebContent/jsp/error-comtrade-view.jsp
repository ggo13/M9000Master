<!DOCTYPE HTML>
<%@ taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<title>M9000 Error</title>
</head>
<link href='<s:url value="/css/main.css"/>' rel="stylesheet"
	type="text/css" />
<body>
	<div align="center" class="errorContainer" >Error occurred</div>
		<div>
			<s:textarea cssStyle="width:50%;height:80%"  value="%{exception}" readonly="true" label="Exception name"/>
		</div>
		<div>
			<s:textarea id="errorResult" name="errorResult" readonly="true" cols="50" rows="50" cssStyle="width:50%;height:50%" title="Error Occured" wrap="true" value="%{exceptionStack}"> </s:textarea>
		</div>
		<s:if test="%{errorMessage != null && !errorMessage.isEmpty()}">
			<div align="center" class="errorContainer" >
				Exception Occurred: ${errorMessage}
			</div>
		</s:if>
		<div align="center" style="font-size:1em">
			<s:actionerror id="userMsgError" theme="jquery"/>
		</div>
</body>
</html>
