<%@ taglib prefix="s" uri="/struts-tags"%>
<s:set var="noOfDigitals" value="%{32}"/>
	<s:iterator value="mapDigitalsDisplayInfo" status="digital_stat">
		<s:if test="%{#digital_stat.index == 0}">
			<div id="digitalRow[<s:property value='%{#digital_stat.index}'/>]" class="row">
		</s:if>
		<s:elseif test="%{#digital_stat.index % #noOfDigitals == 0}">
			</div>
			<div id="digitalRow[<s:property value='%{#digital_stat.index}'/>]" class="row">	
		</s:elseif>
		  <div id="digitalCol[<s:property value='%{#digital_stat.index}'/>]" class="column">
		    <b><s:property value="key"/></b>
		    <img src='${pageContext.request.contextPath}/images/bullet_ball_glass_<s:property value="value.value.toLowerCase()"/>.png' title="<s:property value="value.key"/>"/>
		  </div>
	</s:iterator>
</div>