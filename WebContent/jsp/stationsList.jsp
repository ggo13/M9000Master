<%@ include file="header.jsp"%>
<%@ page import="com.usi.m9000.dto.UsersDTO"%>
<%
	UsersDTO userRole = (UsersDTO) session.getAttribute("userDetails");
%>
<body>
	<style>
a {
	text-decoration: none
}
;
</style>
	<script src="js/m9000/stationsList.js"></script>
	<script src="js/m9000/stationSpecificTabs.js"></script>
	<script type="text/javascript">
		var archType = "${session.ARCH_TYPE}";

		function validate() {
			var form = document.forms["NextToCreateOrUpdate"];
			var objPrefix = "stationId[";
			var index = 0;
			var objName = objPrefix + index + "]";
			while (form.elements[objName] != null) {
				alert(form.elements[objName].value + " checked? "
						+ form.elements[objName].checked);
				objName = objPrefix + (++index) + "]";
			}
		}
		function show_faults(obj, value) {
			//		var retVal = confirm("Changing to a different station will close all open analysis screen associated with current station. Are you sure want to change the station?");
			//		if (retVal == true)
			//		{
			//	alert("station id changed to "+ document.forms["NextToCreateOrUpdate"].elements['stationId'].value);
			$('[id^="userMsg"]').empty();
			$('[id^="showMessages"]').empty();
			stopAllTimers();
			$.publish("comtrade_faults");
			//		}
			//		return retVal;
		}
		$.subscribe('showFaultsTopic', function(event, data) {
			stopAllTimers();
			if ($('#stationId').val() == null)
				{
				$('[id^="trButtons"]').hide();
				}
			else
				{
				$('[id^="trButtons"]').show();
				}
			$.publish("comtrade_faults");
		});

		$.subscribe('showFaultsTopicAndClearMessage', function(event, data) {
			$('[id^="userMsg"]').empty();
			$('[id^="showMessages"]').empty();
			stopAllTimers();
			if ($('#stationId').val() == null)
				{
				$('[id^="trButtons"]').hide();
				}
			else
				{
				$('[id^="trButtons"]').show();
				}
			$.publish("comtrade_faults");
		});

		function filter(field) {
			return field.name == "stationDetails.systemLineFrequency";
		}
		$().ajaxStart(function() {
			$('body').css('cursor', 'wait');
		});
		$().ajaxStop(function() {
			$('body').css('cursor', 'auto');
		});
		function openNewWindow() {
			var status;
			$.ajax({
				url : '<s:url action="checkSessionStatus"/>',
				type : 'POST',
				dataType : 'json',
				success : function(res) {
					status = res.currentSessionStatus;
					if (res.currentSessionStatus == 'active') {
						var left = (screen.width / 2);
						left -= (700 / 2);
						var top = (screen.height / 2);
						top -= (550 / 2);
						window.open("/M9000Master/stationConfigReport",
								"_blank",
								"status = 1,height = 550,width = 700,resizable = 1,left="
										+ left + ",top=" + top);
					} else {
						window.location = "/M9000Master/logout";
					}
				},
				error : function(jqXhr, textStatus, errorThrown) {
					window.location = "/M9000Master/logout";
				}
			});
			return false;
		}
		$.subscribe("/checkSessionBefore", function(event, widget) {
			var status = null;
			$.ajax({
				url : '<s:url action="checkSessionStatus"/>',
				type : 'POST',
				dataType : 'json',
				success : function(res) {
					console.log(res.currentSessionStatus);
					$status = res.currentSessionStatus;
					if (res.currentSessionStatus == "expired") {
						event.cancel = true;
						window.location = "/M9000Master/logout";
					}
				},
				error : function(jqXhr, textStatus, errorThrown) {
					$status = "ERROR";
					event.cancel = true;
					window.location = "/M9000Master/logout";
				}
			}).done(function(response) {
				if ($status == null) {
					event.cancel = true;
				}
			});
			if ($status == null) {
				event.cancel = true;
			}
		});
		$
				.subscribe(
						"importErrorTopic",
						function(event, ui) {
							if (typeof event.originalEvent.request.responseJSON !== 'undefined'
									&& typeof event.originalEvent.request.responseJSON.actionErrors !== 'undefined') {
								alert(event.originalEvent.request.responseJSON.actionErrors[0]);
							} else {
								window.location = "/M9000Master/logout?customActionError=Session timed out";
							}
						});
		$.subscribe('importSuccessTopic', function(event, data) {
			window.location = "/M9000Master/backToStationList";
		});

		function confirmBeforeRestore() {
			if (confirm('It will attempt to import the latest config from one of the connected chassis. Do you want to continue?')) {
				$('*').css('cursor', 'wait');
				return true;
			} else {
				return false;
			}
		}
	</script>

	<s:form action="NextToCreateOrUpdate" method="POST" validate="false"
		cssStyle="width:100%;height:100%;">
		<s:fielderror />
		<s:actionerror id="userMsgError" theme="jquery" />
		<s:actionmessage id="userMsgInfo" theme="jquery" />
		<s:url var="urlNew" action="NextToCreateOrUpdate" />
		<s:url var="urlEdit" action="editConfiguration">
			<s:param name="stationId" value="%{stationDetails.systemStationId}"></s:param>
		</s:url>
		<s:url var="urlRemove" action="removeStationAction">
		</s:url>
		<s:url var="urlTiggerNow" action="updateComtradeFaults" />
		<s:url var="urlSaveConfigFile" action="saveConfigFile" />
		<s:url var="urlRestoreConfig" action="restoreActiveConfig" />
		<s:url var="urlCfgReport" action="stationConfigReport" />
		<s:url var="urlStationCleanupAction" action="stationCleanupAction" />
		<s:url var="urlRemoveStationWithDataAction"
			action="removeStationWithDataAction">
		</s:url>
		<s:url var="urlAdmin" action="admin" />
		<s:url var="urlComtradeFaults" action="showComtradeFaults">
		</s:url>
		<s:url var="urlRestoreActiveConfigAction"
			action="restoreActiveConfigAction">
		</s:url>
		<img id="indicator" src="images/indicator.gif" style="display: none" />
		<div id="showMessages"></div>
		<s:if
			test="%{lstAvailableStations != null && lstAvailableStations.size() > 0}">
			<s:if test="%{#session.HIERARCHY_INFO != null}">
				<tr>
					<td class="titleLabel" style="background-color: #9999CC">Welcome
						to ${session.HIERARCHY_INFO.rootZoneName} <s:if
							test="%{#session.userDetails.role != 'guest'}">
							<s:a id="idLinkUserConfig" cssClass="highlighted-link"
								cssStyle="float:right" href="%{urlAdmin}">Administration</s:a>
						</s:if>
					</td>
				</tr>
			</s:if>
			<s:else>
				<tr>
					<td class="titleLabel" style="background-color: #9999CC">M9000 SubStation(s)
						<s:if test="%{#session.userDetails.role != 'guest'}">
							<s:a id="idLinkUserConfig" cssClass="highlighted-link"
								cssStyle="float:right" href="%{urlAdmin}">Administration</s:a>
						</s:if>
					</td>
				</tr>
			</s:else>
			<tr id="trHeaderSelect">
				<s:if test="%{#session.HIERARCHY_INFO != null}">
					<td style="text-align: center; width: 25%;" class="tdLabel"><s:iterator
							status="status" begin="0"
							end="%{#session.HIERARCHY_INFO.totalLevels-1}">
							<s:url var="urlFetchChildZones#{status.count}"
								action="fetchChildZones">
								<s:param name="parentZoneLevel" value="%{#status.index}"/>
							</s:url>
							<s:if test="%{#status.count == 1}">
								<s:label theme="simple">Filter by zone-<s:property value="%{#status.count}"/>:  </s:label>
								<sj:select href="%{urlFetchChildZones#{status.count}}"
									parentTheme="simple" id="zoneIds%{#status.index}"  formIds="NextToCreateOrUpdate"
									name="zoneIds[%{#status.index}]" label="Select a zone"
									onSuccessTopics="reloadZone%{#status.count+1}"
									onChangeTopics="reloadZone%{#status.count+1}"
									list="lstChildZones" listKey="key" listValue="value"
									value="%{zoneIds[#status.count-1]}" />
							</s:if>
							<s:else>
								<s:label theme="simple">Filter by zone-<s:property value="%{#status.count}"/>:  </s:label>
								<sj:select href="%{urlFetchChildZones#{status.count}}"
									parentTheme="simple" id="zoneIds%{#status.index}"
									name="zoneIds[%{#status.index}]" label="Select a zone" deferredLoading="true" formIds="NextToCreateOrUpdate"
									onSuccessTopics="reloadZone%{#status.count+1}"
									onErrorTopics="hideButtonsTopic"
									reloadTopics="reloadZone%{#status.count}"
									onChangeTopics="reloadZone%{#status.count+1}"
									list="lstChildZones" listKey="key" listValue="value"
									value="%{zoneIds[#status.count-1]}" />
							</s:else>
							<s:label theme="simple" />
						</s:iterator>
						<s:url var="urlFilterStationsList" action="fetchFilteredStationsList"></s:url> 
						<s:label theme="simple">Station Id: </s:label> <sj:select href="%{urlFilterStationsList}"
									parentTheme="simple" id="stationId" name="stationId" deferredLoading="true" formIds="NextToCreateOrUpdate"
									reloadTopics="reloadZone%{#session.HIERARCHY_INFO.totalLevels+1}"
									onSuccessTopics="showFaultsTopic"
									onChangeTopics="showFaultsTopicAndClearMessage"
									list="lstFilteredStations" listKey="systemStationId"
									listValue="stationDisplayName"
							value="%{stationDetails.systemStationId}" />
						</td>
				</s:if>
				<s:else>
					<td style="text-align: center;" class="tdLabel"><s:label
							theme="simple">Station Id: </s:label> <s:select theme="simple"
							label="Station Id" id="stationId" name="stationId"
							list="lstAvailableStations" listKey="systemStationId"
							listValue="stationDisplayName"
							onchange="javascript:show_faults(this, this.value);" 
							value="%{stationDetails.systemStationId}" /></td>
				</s:else>
			</tr>
			<tr id="trButtons">
				<td id="tdButtons" style="width: 100%;" align="center">
					<table class="inlineTable">
						<tr align="right">
							<td width="50%">
							<s:if test="%{#session.userDetails.role != 'guest'}">
								<s:if
										test="%{#session.ARCH_TYPE != null && #session.ARCH_TYPE == 'REMOTE'}">
										<s:submit theme="simple" cssClass="submit" name="btnCreateTop"
											key="label.create.station" action="NextToCreateOrUpdate"
											formnovalidate="formnovalidate" title="Create a new station" />
										<td style="border-left: double;" width="1px"/>
									</s:if> <s:submit theme="simple" cssClass="submit" name="btnEditTop"
										key="label.edit.station" action="editConfiguration"
										formnovalidate="formnovalidate"
										title="Select and Edit any station" /> 
							</s:if> 
							<s:else>
										<s:submit theme="simple"
												cssClass="submit" name="btnEditTop"
												key="label.view.station" action="editConfiguration"
												formnovalidate="formnovalidate"
												onclick="return confirm('WARNING: Read Only Access. You need to be an admin to modify configuration.')"
												title="Select and Edit any station" /> 
							</s:else>
							</td>
												<td style="border-left: double;" width="1px"><sj:submit
														parentTheme="simple" cssClass="submit"
														id="btnTriggerNowTop" href="%{#urlTiggerNow}"
														indicator="indicator" name="btnTriggerNow"
														value="Trigger Now" title="Test Trigger the station now"
														beforeNotifyTopics="/checkSessionBefore"
														onSuccessTopics="clearPriorMsgs" targets="showMessages" />
												</td>
												<td style="border-left: double;" width="1px"></td>
												<s:if test="%{#session.userDetails.role != 'guest'}">
													<s:if
														test="%{#session.ARCH_TYPE != null && #session.ARCH_TYPE == 'REMOTE'}">
														<td id="tdImportMenu" width="700px"
															style="text-align: left; padding-right: 34%">
													</s:if>
													<s:else>
														<td id="tdImportMenu" width="700px"
															style="text-align: left; padding-right: 38%">
													</s:else>
													<sj:menu id="mainMenuAction" cssStyle="width:100%;"
														cssClass="submit">
														<sj:menuItem id="actionMenu"
															cssStyle="width:100%;background-color: #9999CC;color:black"
															title="Config">
															<sj:menu id="mainSubMenu">
																<sj:menuItem id="exportMenu" title="Backup To">
																	<sj:menu id="subMenuExport" cssStyle="width:159px">
																		<sj:menuItem id="menuExportConfig"
																			title="This Computer (SQL)"
																			href="%{urlSaveConfigFile}" />
																		<sj:menuItem id="menuExportPdf"
																			title="This Computer (PDF)" />
																	</sj:menu>
																</sj:menuItem>
																<sj:menuItem id="importMenu" title="Restore From">
																	<sj:menu id="subMenuImport" cssStyle="width:171px">
																		<sj:menuItem id="menuImportLocal" title="Browse..." />
																		<sj:menuItem id="menuImportFromBackup"
																			title="Previous Autosave..." />
																		<s:if
																			test="%{#session.ARCH_TYPE != null && #session.ARCH_TYPE == 'REMOTE'}">
																			<sj:menuItem id="menuImportResoreConfig"
																				title="Station Master..."
																				href="%{urlRestoreActiveConfigAction}" />
																		</s:if>
																		<s:else>
																			<sj:menuItem id="menuImportResoreConfig"
																				title="DME System Chassis..."
																				href="%{urlRestoreActiveConfigAction}" />
																		</s:else>
																	</sj:menu>
																</sj:menuItem>
																<sj:menuItem id="removeMenu" title="Remove">
																	<sj:menu id="subMenuRemove" cssStyle="width:172px">
																		<sj:menuItem id="menuRemoveStationConfig"
																			title="Station (Leave Data)..." href="%{urlRemove}" />
																		<sj:menuItem id="menuClearData"
																			title="Data (Leave Station)..."
																			tooltip="It cleans up data to start fresh"
																			href="%{urlStationCleanupAction}" />
																		<sj:menuItem id="menuRemoveStationWithData"
																			title="Station and Data..."
																			href="%{urlRemoveStationWithDataAction}" />
																	</sj:menu>
																</sj:menuItem>
															</sj:menu>
														</sj:menuItem>
													</sj:menu>
													</td>
												</s:if>
												<s:else>
													<td id="tdImportMenu" width="700px"
														style="text-align: left; padding-right: 39%"><sj:menu
															id="mainMenuAction" cssStyle="width:100%;"
															cssClass="submit">
															<sj:menuItem id="actionMenu"
																cssStyle="width:100%;background-color: #9999CC;color:black"
																title="Backup To">
																<sj:menu id="mainSubMenu">
																	<sj:menuItem id="exportMenu" title="Export">
																		<sj:menu id="subMenuExport" cssStyle="width:153px">
																			<sj:menuItem id="menuExportConfig"
																				title="This PC (SQL)" href="%{urlSaveConfigFile}" />
																			<sj:menuItem id="menuExportPdf" title="This PC (PDF)" />
																		</sj:menu>
																	</sj:menuItem>
																</sj:menu>
															</sj:menuItem>
														</sj:menu></td>
												</s:else>
												</td>
											</tr>
										</table></td>
							</tr>
							<tr id="trFaultsData" height="100%">
							<s:if test="%{#session.HIERARCHY_INFO != null}">
								<td id="tdFaultsData"><sj:div showLoadingText="true"
										id="faults" href="%{urlComtradeFaults}" deferredLoading="true"
										listenTopics="comtrade_faults"
										onCompleteTopics="change_station"
										formIds="NextToCreateOrUpdate" cssStyle="height:100%;">
									</sj:div></td>
							</s:if>
							<s:else>
							<td id="tdFaultsData"><sj:div showLoadingText="true"
										id="faults" href="%{urlComtradeFaults}"
										listenTopics="comtrade_faults"
										onCompleteTopics="change_station"
										formIds="NextToCreateOrUpdate" cssStyle="height:100%;">
									</sj:div></td>
							</s:else>
							</tr>
							</s:if>
							<s:else>
								<table class="configureTable" style="width: 100%;">
									<tr>
										<td class="titleLabel" style="background-color: #9999CC">M9000 SubStation(s)
											<s:if test="%{#session.userDetails.role != 'guest'}">
												<s:a id="idLinkUserConfig" cssClass="highlighted-link"
													cssStyle="float:right" href="%{urlAdmin}">User Configuration</s:a>
											</s:if>
										</td>
									</tr>
									<s:if test="%{#session.userDetails.role != 'guest'}">
										<tr style="height: 50%;">
											<td align="center" style="color: darkblue;"><b>No
													existing stations. Create New one.</b></td>
										</tr>
										<tr id="trButtons">
											<td id="tdButtons" style="width: 100%;" align="center">
												<table class="inlineTable">
													<tr align="right">
														<td width="45%"><s:submit theme="simple"
																cssClass="submit" name="btnCreateTop"
																key="label.create.station" action="NextToCreateOrUpdate"
																formnovalidate="formnovalidate"
																title="Create a new station" /></td>
														<td style="border-left: double;" width="1px"></td>
														<td id="tdImportMenu"
															style="text-align: left; padding-right: 48%"><sj:menu
																id="mainMenuImport" cssStyle="width:100%;"
																cssClass="submit">
																<sj:menuItem id="importMenu"
																	cssStyle="width:100%;background-color: #9999CC;color:black"
																	title="Restore From">
																	<sj:menu id="subMenuImport" cssStyle="width:171px">
																		<sj:menuItem id="menuImportLocal" title="Browse..." />
																		<sj:menuItem id="menuImportFromBackup"
																			title="Previous Autosave..." />
																		<s:if
																			test="%{#session.ARCH_TYPE == null || #session.ARCH_TYPE != 'REMOTE'}">
																			<sj:menuItem id="menuImportResoreConfig"
																				title="DME System Chassis..."
																				href="%{urlRestoreActiveConfigAction}" />
																		</s:if>
																	</sj:menu>
																</sj:menuItem>
															</sj:menu></td>
													</tr>
												</table>
											</td>
										</tr>
									</s:if>
									<s:else>
										<tr style="height: 50%;">
											<td align="center" style="color: darkblue;"><b>No
													Existing Stations. You need Administrator privilege to
													create or import new station</b></td>
										</tr>
									</s:else>
								</table>
							</s:else>
							<s:hidden id="jspFileName" name="stationsList.jsp"></s:hidden>
							</s:form>
							<img id="indicator" src="images/indicator.gif" alt="Loading..."
								style="display: none" />
							<sj:dialog id="importStationDialog" width="auto" height="auto"
								autoOpen="false" modal="true" title="Browse..."
								openTopics="importTopic" closeTopics="importSuccessTopic">
								<s:form id="frmImportStation" enctype="multipart/form-data"
									action="importStationAction">
									<tr>
										<td><s:file theme="simple" id="importSql"
												name="importSql"
												title="Select the config sql file to import" /></td>
									</tr>
									<tr>
										<td align="center"><s:submit theme="simple"
												cssClass="submit" id="btnSubmitImport"
												name="btnSubmitImport" value="Import" disabled="true" /></td>
									</tr>
								</s:form>
							</sj:dialog>

							<sj:dialog id="dlgBackupConfigList" autoOpen="false" modal="true"
								title="Restore From Autosaved Configs" closeOnEscape="true"
								width="auto" height="auto" openTopics="importFromBakupTopic"
								closeTopics="importSuccessTopic">
								<s:form id="frmImportFromBackup" enctype="multipart/form-data"
									action="importFromBackupAction">
									<s:url var="urlfetchBackupLst" action="fetchBackupLst" />
									<tr>
										<td colspan="2" align="center"><s:label theme="simple"
												cssStyle="font-weight:bold;text-align:center"
												value="Select a Autosaved file to Restore"></s:label></td>
									</tr>
									<tr>
										<td colspan="2" align="center"><sj:div
												deferredLoading="true" showLoadingText="true"
												id="divBackupFiles" listenTopics="fetchBackupFilesTopic"
												href="%{urlfetchBackupLst}" formIds="frmImportFromBackup">
											</sj:div></td>
									</tr>
									<tr>
										<td align="center"><s:submit theme="simple"
												cssClass="submit" id="btnSubmitImportBackup"
												name="btnSubmitImportBackup" value="Import" disabled="true" />
										</td>
									</tr>
								</s:form>
							</sj:dialog>
</body>
</html>
