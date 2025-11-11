<%@ taglib prefix="s" uri="/struts-tags"%>
<display:table name="${lstComtradeDetails}" class="wwFormTable"
	style="background-color:#9999CC;text-align:center;" id="details">
	<display:column title=" Fault Id " property="faultId" sortable="true" />
	<display:column title="Station Id" property="stationId" />
	<display:column title="Date - Time" property="displayTime"
		sortable="true" />
	<display:column title="Length" property="length" />
	<display:column title="Pre Fault" property="preFault" />
	<display:column title="Post Fault" property="postFault" />
	<display:column title="">
		<s:url url="viewComtradeUrl" action="viewComtradeDetails"
			includeParams="none">
			<s:param name="selectedIndex" value="%{#attr.details.faultId}"></s:param>
		</s:url>
		<s:a href="#"
			onclick="window.open(%{viewComtradeUrl},\"Comtrade Viewer\",\"menubar=0,status=0,width=100%,height=100%\")">View</s:a>
	</display:column>

</display:table>
