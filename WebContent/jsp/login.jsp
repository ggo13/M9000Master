<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<html>
<head>
<link rel="icon" type="image/png" href="images/usi-logo-64.png">
<title>USI M9000 Master</title>
<s:head/>
<sj:head jqueryui="true" />
<link href="<s:url value="/css/main.css"/>" rel="stylesheet"
	type="text/css" />
</head>
<script type="text/javascript">
	
	$(document).ready(function(){
	 	$('#userName').focus();
	});	
</script>
<style>
.form-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  height: 100vh; /* optional: set height to fill viewport */
}
</style>
<header class="header-text">
    <!--img style="width:300px;height:50px" src="images/logo-usi-sine-hi-res.gif" alt="USI Logo" -->
  </header>
<h4 align="center">USI M9000 Master</h4>

<body>
<div class="form-container">
	<s:form cssStyle="width:460px;" action="doLogin" focusElement="userName" method="POST"
		validate="false">
		<tr>
			<td class="titleLabel" style="background-color: #9999CC" colspan="2">USI M9000 Login</td>
		</tr>
		<tr>
			<td colspan="2"><s:actionerror id="userMsgError" theme="jquery"/></td>
		</tr>
		<tr>
	<td>	
		<div style="display: flex; flex-direction: row;">
		    <div style="flex: 1;margin-right: 3px;">
      <img src="images/logo-full.png" alt="USI Logo" style="width: 240px;text-align:left;" />
    		    </div>
		    <div style="flex: 1;text-align:left;">
			<s:label cssStyle="font-weight:bold;" theme="simple" for="userName" value="Username:"></s:label>	
			<s:textfield theme="simple" id="userName" name="userName" key="label.username" size="20" />
			<br/>
			<br/>
			<s:label cssStyle="font-weight:bold;" theme="simple" for="password" value="Password:"></s:label>
			<s:password theme="simple" id="password" name="password" key="label.password" />
		    </div>
  		</div>
	</td>
		</tr>
		<tr style="width:100%;text-align: center;" >
			<td colspan="2">
			<s:submit theme="simple" cssClass="submit" method="execute" key="label.login"/>
		</td>
		</tr>

	</s:form>
</div>
</body>
</html>
