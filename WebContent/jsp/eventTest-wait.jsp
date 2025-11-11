<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<h2>Please wait ...</h2>
<div style="width:400;height:100">
	<sj:progressbar id="progressbarWait"  value="%{eventTestStatus}"/>
</div>
