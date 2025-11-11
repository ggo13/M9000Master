<!DOCTYPE HTML>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sjdt" uri="/struts-jquery-datatables-tags"%>
<%@ taglib prefix="sjg" uri="/struts-jquery-grid-tags"%>
<%@ page import="com.usi.m9000.dto.UsersDTO"%>
<% 	String stationId = new String();
	stationId = (String)session.getAttribute("stationId");
  %>
<script type="text/javascript">
	var stationId = <%=stationId%>;
	var userRole = "${session.userDetails.role}";
</script>
<style type="text/css">
table.ui-pg-table {
width:500px;
font-size: 12px;
}
.ui-jqgrid .ui-pg-table td {
 font-size: 10px;
}
.ui-jqgrid .ui-pg-input {
  height:18px;
}
</style>
<script src="js/jquery.splitter-0.14.0.js"></script>
<script src="js/m9000/faultsGrid.js"></script>
<link href="css/jquery.splitter.css" rel="stylesheet"/>
<div class="contextMenu" id="faultMenu" style="display:none; width:400px;">
        <ul style="width: 400px; font-size: 65%;">
            <li id="view">
                <span class="ui-icon ui-icon-plus" style="float:left"></span>
                <span style="font-size:130%; font-family:Verdana">View Fault</span>
            </li>
            <li id="save">
                <span class="ui-icon ui-icon-pencil" style="float:left"></span>
                <span style="font-size:130%; font-family:Verdana">Save Fault</span>
            </li>                
            <li id="faultLoc">
                <span class="ui-icon ui-icon-arrowreturnthick-1-w" style="float:left"></span>
                <span style="font-size:130%; font-family:Verdana">Calculate fault location</span>
            </li>
        </ul>
    </div>

<table style="width:100%;height:90%;">
	<tr style="height:100%;"><td style="width:5%;height:726;">
		<div id="divLeftPane"> 
			 <div>
			   <s:label theme="simple" name="lblAbnormalEvents" value="Abnormal Events List" cssClass="titleLabel"/>
			   <s:select theme="simple" cssStyle="height:100%;width:660;background-color:#EEEEFF"
			    id="itemSelect"
			    name="activeEvents"
			    multiple="true"
			    list="#{''}"
			    />
			</div>
			<div>
			<s:url var="fetchFaultLocDetailsUrl" action="fetchFaultData">
			</s:url>
			   <s:label theme="simple" name="lblFaultLocation" value="Fault location Details" cssClass="titleLabel"/>
				   <s:textarea theme="simple" name="txtFaultLoc" cssStyle="height:100%;width:660;background-color:#EEEEFF;resize: none;"  readonly = "true"/>
			</div>
		</div>
	</td>
		<td style="vertical-align:top">
<div id="fault">
	<s:url var="fetchFaultDataUrl" action="fetchFaultData">
	<s:param name="stationId" value="%{stationId}"></s:param>
	</s:url>
	<s:url var="editurl" action="editFaults">
	<s:param name="stationId" value="%{stationId}"></s:param>
	</s:url>
	<s:url var="fetchAllEventsUrl" action="fetchAllEvents">
			</s:url>
	<s:url var="fetchLineGroupsUrl" action="fetchLineGroups">
			</s:url>

	<div id="faultStatus" align="center" style="background-color: RED"><s:actionerror id="userMsgError" theme="jquery"/> </div>
	<div align="center">
	     				<s:checkbox theme="simple" name="autoFaultsRefresh" value="true" label="Auto Refresh" cssClass=".tdLabel"/> 
					<s:label theme="simple" name="lblAutoUpdate" value="Auto Update Enable?" cssClass="titleLabel" for="autoFaultsRefresh"/>
	 			</div>
	<sjg:grid cssStyle="height:100%"
	  id="faultsGrid" 
	  caption="DFR Fault Data"
	  dataType="json"
	  hidegrid="false"
	  href="%{fetchFaultDataUrl}"
		pager="true"
		toppager="true"
		navigatorCloneToTop="true"
	    navigator="true"
	    navigatorAdd="false"
   		navigatorSearch="true"
	    navigatorSearchOptions="{multipleSearch:true}"
	    navigatorEdit="false"
	    navigatorView="false"
	    navigatorDelete="true"
		navigatorDeleteOptions="{
	                        height:280,
	                        reloadAfterSubmit:true,
	                                afterSubmit:function(response, postdata) {
	                                return isError(response.responseText);
	                                 }
	                        }"
	    gridModel="gridModel"
				  rowList="25,50,100,150,200,250,300"
	  rowNum="25"
	  rownumbers="true"
	  autowidth="true"
	  resizable="false"
	  shrinkToFit="true"
	  viewrecords="true"
	    altRows="false"
		sortable="true"
		sortname="faultId"
		sortorder="desc"
		onDblClickRowTopics="viewFault"
		onGridCompleteTopics="initFaultsGrid"
		onErrorTopics="showErrorMessage"
		onPagingTopics="storeSelection"
		onSortColTopics="storeSelection"
  		onSelectRowTopics="updateActiveEvents"
		hoverrows="true"
		scrollrows="true"
		editurl="%{editurl}"
					editinline="false"
		multiselect="true"
		multiboxonly="true"
		errorElementId="faultStatus"
		errorText="Unable to fetch fault data. Please check database connection."
	>
	<s:actionerror id="userMsgError" theme="jquery"/>
	<sjg:gridColumn name="faultId" index="faultId" title="Fault Id"  key="true" formatter="integer" sortable="true" sorttype="int" search="true" searchoptions="{sopt:['eq','lt','gt']}"  align="center" width="50"/>
	<sjg:gridColumn name="displayTime" index="displayTime" title="Date-Time" sortable="true" align="center" width="150" search="true" searchoptions="{sopt:['eq','ne','lt','gt'], dataInit:datePick, attr:{title:'Your Search Date'}}"/>
	<sjg:gridColumn name="length" index="length" title="Length" sortable="true"  align="center" width="40" search="true" searchoptions="{sopt:['eq','lt','gt']}"/>
	<sjg:gridColumn name="faultLogic" index="faultLogic" title="Fault Logic" sortable="true"  align="center" width="60" search="true" searchtype="select" searchoptions="{sopt:['eq'],value:':ALL;1:TRUE;0:FALSE'}"/>
	<sjg:gridColumn name="userComments" index="userComments"  title="Comments" align="left" sortable="false"  search="true" searchoptions="{sopt:['cn','nc','bw','bn']}" />
	<sjg:gridColumn name="activeEvents" index="activeEvents"  title="Active Events" hidden="true" hidedlg="true" align="left" sortable="false" searchtype="select" searchoptions="{searchhidden: true,sopt:['cn','nc'], dataUrl: '%{fetchAllEventsUrl}'}"/>
	<sjg:gridColumn name="line_groups" index="line_groups"  title="Line Groups" hidden="true" hidedlg="true" align="left" sortable="false" searchtype="select" searchoptions="{searchhidden: true,sopt:['cn','nc'], dataUrl: '%{fetchLineGroupsUrl}'}"/>
	<sjg:gridColumn name="faultLocationDetails" index="faultLocationDetails"  title="Fault Location Details" hidden="true" hidedlg="true" align="left" sortable="false" search="true" searchtype="select" searchoptions="{searchhidden: true,sopt:['eq','ne'],value:':EMPTY'}"/>
	<sjg:gridColumn name="fileName" index="fileName"  title="file_name" hidden="true" hidedlg="true" align="left" sortable="false" width="60"/>
	</sjg:grid>
</div>
<sj:dialog id="dlgShowFaultLoc" width="auto" height="auto" openTopics="dlgFaultLocationTopic" onBeforeTopics="beforeFaultLocCalc"
							autoOpen="false" modal="true" title="Event Test" closeTopics="closeFaultLocTopic">
		<s:url var="faultLocUrl" action="calculateFaultLoc"/>
		    <sj:div id="divFaultLocationStatus" 
		    		href="%{faultLocUrl}" 
		    		reloadTopics="faultLocTopic"
		    		deferredLoading="true"
				formIds="NextToCreateOrUpdate"
				indicator="indicator1"
		    		cssClass="result ui-widget-content ui-corner-all">
				<img id="indicator1"  src="images/indicator.gif" alt="Loading..." style="display:none"/>
		    </sj:div>
	</sj:dialog>
		</td>
	</tr>
</table>
<s:hidden name="faultIds" id="faultIds"/>