<!DOCTYPE HTML>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<link href="<s:url value="/css/scopeTab.css"/>" rel="stylesheet"
	type="text/css" />
<script type="text/javascript">
	var startChannel = "${selectedDfrDto.analogChannelStart}";
	var endChannel = "${selectedDfrDto.analogChannelEnd}";
	var context_path = "${pageContext.request.contextPath}";
</script>
<script type="text/javascript">
	$(document).ready( function() { 
		stopScopeLoadTimer();
		$.publish("startTimer");
});
</script>
<script src="js/m9000/scopeResize.js"></script>

	<s:url var="displayJQScopeChart" action="viewJQSCOPEChart">
	</s:url>
	<div id="chartMenu" style="width:100%">
		<s:checkbox theme="simple" id="showHideLegend"
		name="showHideLegend" value="true" title="Show or Hide Legend panel next to analog chart"
		cssClass=".tdLabel" />
		<s:label theme="simple" name="lblShowHideLegend" value="Show Legend" title="Show or Hide Legend panel next to analog chart" for="showHideLegend"
		cssClass="titleLabel" />
		<s:checkbox theme="simple" id="showHideDataPoints"
		name="showHideDataPoints" fieldValue="false" title="Show or Hide Data Points in analog chart"
		cssClass=".tdLabel" />
		<s:label theme="simple" name="lblShowHideDataPoints" value="Show Datapoints" title="Show or Hide Data Points in analog chart" for="showHideDataPoints"
		cssClass="titleLabel" />
		<s:checkbox theme="simple" id="showHideControls"
		name="showHideControls" value="true" title="Show or Hide Controls on the right panel"
		cssClass=".tdLabel" />
		<s:label theme="simple" name="lblShowHideControls" value="Show Controls" title="Show or Hide Controls on the right panel" for="showHideControls"
		cssClass="titleLabel" />
	</div>
		<div id="splitPane" class="resizable resizable1">
			<div class="left-side-scope"  style="width:100%;height:100%">
				<div id="divAnalogChart" class="divAnalog" style="height:calc(100vh - 270px)">
						<div class="divAnalogRow" style="width:100%;">
							<div id="chart" class="divAnalogCell" style="width:80%">
								<sj:div id="divChartJq" theme="simple" formIds="NextToCreateOrUpdate"
										deferredLoading="false"
										href="%{#displayJQScopeChart}"
										reloadTopics="refreshScope" onErrorTopics="refreshScopeError"  onSuccessTopics="updateLegend,updateDigitalInfo,startTimer" 											
										cssStyle="width:100%;height:100%"
										cssClass="result ui-widget-content ui-corner-all chart-placeholder">
									</sj:div>
							</div>
							<div id="legendHolder" class="divAnalogCell legendContainer h-resizable-top" style="width:20%;height:100%;overflow:auto;position:absolute;top:0px;">
							</div>
						</div>
					</div>
			</div>
		</div>
		<div id="controls" class="resizable resizable2"> 
			<div class="controlsDiv">
				<s:checkbox theme="simple" id="expandCollapseAccordion"
				name="expandCollapseAccordion" value="true" title="Expand or Collapse the control menus"
				cssClass=".tdLabel" />
				<s:label theme="simple" name="lblExpandCollapseAccordion" value="Expand Menus" title="Expand or Collapse the control menus" for="expandCollapseAccordion"
				cssClass="titleLabel" />
			</div>
			<sj:accordion id="idAccordion0" heightStyle="content" animate="true" collapsible="true">
				<sj:accordionItem title="Primary or Secondary">
					<div class="controlsDiv">
						<s:radio label="Display" id="primaryOrSecondaryDisplay" name="primaryOrSecondaryDisplay" list="{'P', 'S'}" value="primaryOrSecondaryDisplay" tooltip="Select Primary or Secondary display" />
					</div>
				</sj:accordionItem>
			</sj:accordion>
			<sj:accordion id="idAccordion1" heightStyle="content" animate="true" collapsible="true">
				<sj:accordionItem title="Select Channels">
					<div class="controlsDiv">
						<sj:a openDialog="selectAnalogDialog">
							<s:textfield cssStyle="width:50%;height:3.5%;font-size:12;resize: none;" readonly="true" 
								id="strSelectedChannels"
								name="strSelectedChannels"
								value="%{strSelectedChannels}"
								label="Selected Channels"></s:textfield>
						</sj:a>
						<sj:dialog id="selectAnalogDialog" 
							buttons="{ 
					    		'OK':function() { okButtonSelectAnalogDialog(); return false;}, 
					    		'Cancel':function() { cancelSelectAnalogDialogButton(); return false;}
					    		}"
							autoOpen="false" modal="false" title="Analog Channels" width="350" >
							<table style="width:100%" class="decisionDialog">
								<tr style="width:100%;height:2%">
								        <td colspan="2" align="center"><s:label theme="simple"
								                        cssStyle="font-weight:bold;text-align:center"
								                        value="Select Channels"></s:label></td>
								</tr>
								<tr style="width:100%;height:98%">
								        <td style="width:100%;height:98%" colspan="2" align="center"><s:updownselect
								                        cssStyle="border-style: solid solid solid solid" theme="simple" size="%{selectedDfrDto.analogChnlCnt}"
								                        id="lstSelectedChannels" name="lstSelectedChannels" ondblclick="okButtonSelectAnalogDialog();"
								                        list="selectedDfrDto.mapAnalogChannelNames" listKey="key"
								                        listValue="value" allowSelectAll="false" allowMoveDown="false"
								                        allowMoveUp="false" multiple="true"></s:updownselect>
								        </td>
								</tr>
							</table>
						</sj:dialog>
					</div>
					<div class="controlsDiv">
						<s:label value="Browse Channels"></s:label>
						<s:submit theme="simple" name="btnNext" value="Next" tooltip="Display Next Channel" onclick="return false;"/>
						<s:submit theme="simple" name="btnPrevious" value="Previous" tooltip="Display Previous Channel" disabled="true" onclick="return false;"/>
					</div>
				</sj:accordionItem>
			</sj:accordion>
				<sj:accordion id="idAccordion2" heightStyle="content" animate="true" collapsible="true">
					<sj:accordionItem title="Select Cycles">
						<div class="controlsDiv">
							<s:textfield cssStyle="width:20%;height:3.5%;font-size:12;resize: none;" 
								id="requestedCycles"
								name="requestedCycles"
								value="%{requestedCycles}"
								label="Cycles" title="Select number of cycles to display"></s:textfield>
							<s:submit theme="simple" name="btnApply" value="Apply" tooltip="Click to request the set cycle" onclick="return false;"/>
						</div>
					</sj:accordionItem>
				</sj:accordion>
		 	<s:if test="%{#session.stationDetails.systemDigitalChannelsCount > 0 && #session.stationDetails.enableEventTest == true}"> 
			<sj:accordion id="idAccordion3" heightStyle="content" animate="true" collapsible="true">
				<sj:accordionItem title="Test Event Channels">
					<div class="controlsDiv">
						<sj:a openDialog="dlgShowEventTestStatus">
							<s:submit theme="simple" name="btnTestEvents" value="Test" title="Run Event Test" onclick="return false;"/>
						</sj:a>
						<s:checkbox theme="simple" id="enableTransient"
						name="enableTransient" title="Create a transient record of event test"
						cssClass=".tdLabel" />
						<sj:dialog id="dlgShowEventTestStatus" closeOnEscape="true" width="auto" height="auto" 
							autoOpen="false" modal="true" title="Event Test">
								<img id="indicator" 
							       	src="images/indicator.gif" 
							       	alt="Loading..." 
							       	style="display:none"/>
								<s:url var="eventTestUrl" action="testEvents"/>
								    <sj:div id="divEventTestStatus" 
								    		href="%{eventTestUrl}" 
								    		reloadTopics="eventTestTopic"
										deferredLoading="true"
								    		cssClass="result ui-widget-content ui-corner-all">
								    </sj:div>
						</sj:dialog>
						<s:label theme="simple" name="lblEnableTransient" value="Enable Transient" title="Create a transient record of event test" for="enableTransient"/>
					</div>
					<div class="controlsDiv">
					<s:url var="fetchLastEventTestDateUrl" action="fetchLastEventTestDate"/>
					<sj:textfield id="txtEventTestStatus" readonly="true" href="%{fetchLastEventTestDateUrl}" deferredLoading="false" reloadTopics="updateEventTestDateTopic" onAlwaysTopics="eventTestChange" name="lastEventTestDate" value="%{lastEventTestDate}"></sj:textfield>
					</div>
					<div id="divEventTestStatus" style="display:none" class="controlsDiv">
						<s:label theme="simple" id="lblEventTestStatus" value=""/>
					</div>
				</sj:accordionItem>
			</sj:accordion>
		</s:if>
			<sj:accordion id="idAccordion4" heightStyle="content" animate="true" collapsible="true">
				<sj:accordionItem title="Internal Calibration">
					<div class="controlsDiv">
						<s:url var="fetchLastCalDateUrl" action="fetchLastCalDate"/>
						<s:url var="fetchLastCalVerifiedDateUrl" action="fetchLastCalVerifiedDate"/>
							<sj:a openDialog="dlgShowInternalCal">
								<s:submit theme="simple" name="btnInternalCal" value="Calibrate" title="Run Internal Calibrate" onclick="return false;"/>
							</sj:a>
								<sj:textfield id="txtCalStatus" readonly="true" href="%{fetchLastCalDateUrl}" deferredLoading="false" reloadTopics="updateCalDateTopic" onAlwaysTopics="calChange" name="lastCalibratedDate" value="%{lastCalibratedDate}"></sj:textfield>
						<sj:dialog id="dlgShowInternalCal" closeOnEscape="true" width="auto" height="auto" 
							autoOpen="false" modal="true" title="Internal Calibration">
								<img id="indicator" 
							       	src="images/indicator.gif" 
							       	alt="Loading..." 
							       	style="display:none"/>
								<s:url var="intCalUrl" action="internalCal"/>
								    <sj:div id="divIntCalStatus" 
								    		href="%{intCalUrl}" 
								    		reloadTopics="intCalTopic"
										deferredLoading="true"
										formIds="NextToCreateOrUpdate"
								    		cssClass="result ui-widget-content ui-corner-all">
								    </sj:div>
						</sj:dialog>
					</div>
					<div id="divCalStatus" style="display:none" class="controlsDiv">
						<s:label theme="simple" id="lblCalStatus" value=""/>
					</div>
					<div class="controlsDiv">
						<sj:a openDialog="dlgShowInternalCalVerify">
							<s:submit theme="simple" name="btnInternalCalVerify" value="Verify" title="Verify Calibrate" onclick="return false;"/>
						</sj:a>
						<sj:textfield id="txtCalVerifyStatus" readonly="true" href="%{fetchLastCalVerifiedDateUrl}" deferredLoading="false" reloadTopics="updateCalVerifyDateTopic" onAlwaysTopics="calVerifyChange" name="lastCalVerifiedDate" value="%{lastCalVerifiedDate}"></sj:textfield>
						<sj:dialog id="dlgShowInternalCalVerify" closeOnEscape="true" width="auto" height="auto" 
							autoOpen="false" modal="true" title="Verify Calibration">
								<img id="indicator" 
							       	src="images/indicator.gif" 
							       	alt="Loading..." 
							       	style="display:none"/>
								<s:url var="verifyCalUrl" action="verifyCal"/>
								    <sj:div id="divVerifyCalStatus" 
								    		href="%{verifyCalUrl}" 
								    		reloadTopics="verifyCalTopic"
										deferredLoading="true"
										formIds="NextToCreateOrUpdate"
								    		cssClass="result ui-widget-content ui-corner-all">
								    </sj:div>
						</sj:dialog>
					</div>
					<div id="divCalVerifyStatus" style="display:none" class="controlsDiv">
						<s:label theme="simple" id="lblCalVerifyStatus" value=""/>
					</div>
				</sj:accordionItem>
			</sj:accordion>
			<sj:accordion id="idAccordion5" heightStyle="content" animate="true" collapsible="true">
				<sj:accordionItem title="External Calibration">

					<div class="controlsDiv">
						<s:textfield label="Desired Value" name="desiredValue"></s:textfield>
					</div>
					<div class="controlsDiv">
						<s:checkbox theme="simple" id="offsetCorrection"
						name="offsetCorrection" title="Apply offset correction to the selected channel(s)"
						cssClass=".tdLabel" />
						<s:label theme="simple" name="lblOffsetCorrection" value="Offset Correction" title="Apply offset correction to the selected channel(s)" for="offsetCorrection"/>						
					</div>
					<div class="controlsDiv">
							<sj:a openDialog="dlgShowExternalCal">
								<s:submit theme="simple" name="btnExternalCal" value="Apply" title="Run External Calibrate" onclick="return false;"/>
							</sj:a>
						<sj:dialog id="dlgShowExternalCal" closeOnEscape="true" width="auto" height="auto" 
							autoOpen="false" modal="true" title="External Calibration">
								<img id="indicator" 
							       	src="images/indicator.gif" 
							       	alt="Loading..." 
							       	style="display:none"/>
								<s:url var="extCalUrl" action="externalCal">
									<s:param name="extCalReset" value="%{false}"></s:param>
								</s:url>
								    <sj:div id="divExtCalStatus" 
								    		href="%{extCalUrl}" 
								    		reloadTopics="extCalTopic"
										deferredLoading="true"
										formIds="NextToCreateOrUpdate"
								    		cssClass="result ui-widget-content ui-corner-all">
								    </sj:div>
						</sj:dialog>
						<sj:a openDialog="dlgShowExternalCalReset">
							<s:submit theme="simple" name="btnExternalCalReset" value="Reset" title="Reset External Calibration for selected channels" onclick="return false;"/>
						</sj:a>
						<sj:dialog id="dlgShowExternalCalReset" closeOnEscape="true" width="auto" height="auto" 
							autoOpen="false" modal="true" title="Reset External Calibration">
								<img id="indicator" 
							       	src="images/indicator.gif" 
							       	alt="Loading..." 
							       	style="display:none"/>
								<s:url var="resetExtCalUrl" action="resetExternalCal">
									<s:param name="extCalReset" value="%{true}"></s:param>
								</s:url>
								    <sj:div id="divResetExtCalStatus" 
								    		href="%{resetExtCalUrl}" 
								    		reloadTopics="resetExtCalTopic"
										deferredLoading="true"
										formIds="NextToCreateOrUpdate"
								    		cssClass="result ui-widget-content ui-corner-all">
								    </sj:div>
						</sj:dialog>
					</div>
				</sj:accordionItem>
			</sj:accordion>
		</div>