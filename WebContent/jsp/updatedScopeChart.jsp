<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sjc" uri="/struts-jquery-chart-tags"%>
<sjc:chart id="chartJq" cssStyle="width: 100%; height: 100%;"  autoResize="true" xaxisLabel="Data" xaxisLabelFontSizePixels="20" yaxisLabel="Samples" yaxisLabelFontSizePixels="20">
	<s:iterator value="lstOfMapDataSetForJQ" status="data_stat">
		<sjc:chartData id="chartJqList"
			label="%{scopeRmsValues[#data_stat.index]}"
			list="%{lstOfMapDataSetForJQ[#data_stat.index]}"
			points="{ show: %{showHideDataPoints}}" lines="{ show: true }" />
	</s:iterator>
</sjc:chart>
 <s:hidden id="updateScopeConfig" name="updateScopeConfig" value="%{updateScopeConfig}"></s:hidden>
