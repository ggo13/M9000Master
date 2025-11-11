<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<link href="<s:url value="/css/scopeTab.css"/>" rel="stylesheet"
	type="text/css" />
<script type="text/javascript">
	$(document).ready( function() { 
		stopScopeLoadTimer();
		$.publish("startTimer");
});
</script>
<script src="js/m9000/scopeResize.js"></script>
	<s:url var="displayScopeDigitalChart" action="viewSCOPEDigitalChart">
	</s:url>
	<div>
		<div id="chartMenu" style="width:100%">
				<s:checkbox theme="simple" id="showHideControls"
				name="showHideControls" value="true" title="Show or Hide Controls on the right panel"
				cssClass=".tdLabel" />
				<s:label theme="simple" name="lblShowHideControls" value="Show Controls" title="Show or Hide Controls on the right panel" for="showHideControls"
				cssClass="titleLabel" />
		</div>
	</div>
<div class="resizable resizable1">
		<div id="divDigitalChart"></div>
		<div id="splitPane" style="overflow:auto;height:80%;" class="left-side-scope" >
				<sj:div formIds="NextToCreateOrUpdate" cssStyle="float:center;text-align:center;" id="divDigital" deferredLoading="false" href="%{#displayScopeDigitalChart}" reloadTopics="refreshDigitalScope" onErrorTopics="refreshScopeError" onSuccessTopics="startTimer,adjustDigitalOnlyDisplay" targets="divDigitalChart"></sj:div>
		</div>
</div>
<div id="controls" class="resizable resizable2">  
	<s:if test="%{#session.stationDetails.enableEventTest == true}"> 
	<sj:accordion id="idAccordion1" heightStyle="content" animate="true" collapsible="true">
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
 	<s:if test="%{#session.stationDetails.systemAnalogChannelsCount > 0}"> 
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
						<sj:dialog id="dlgShowInternalCalVerify"  closeOnEscape="true" width="auto" height="auto" 
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
	</s:if>
</div>
