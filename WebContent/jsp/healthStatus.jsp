<!DOCTYPE HTML>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<script src="js/m9000/healthMonitor.js"></script>
<body>
<div style="background-color:#EEEEFF;width:100%;overflow:auto">
<s:set var="nofChassisPerLine" value="5"/>
	<s:if test="%{substationHlth == null}">
		<table class="wwFormTable" style="width: 25%;height:25%" >
			<tr>
				<td align="center" style="background-color: RED">Station Master
					<b><s:property value="%{stationDetails.systemStationName}" /></b>
					is not reachable.
				</td>
			</tr>
		</table>
	</s:if>
	<s:else>
		<table class="wwFormTable" style="width:100%;height:25%">
			<tr>
				<td width="100%" class="titleLabel"
					style="background-color: #9999CC"><s:property
						value="%{substationHlth.id}" /> - <s:property
						value="%{stationDetails.systemStationName}" /> - <s:property
						value="%{substationHlth.status}" /></td>
			</tr>
			<tr height="100%">
				<td width="100%" align="center"><table class="wwFormTable" >
						<tr>
							<td colspan="2" align="center"
								style="font-weight: bold; background-color: #9999CC">Station
								Master Information</td>
						</tr>
						<s:iterator var="smAttr"
							value="substationHlth.StationMaster.AttributeArray"
							status="attrArray_stat">
							<tr id="tr_ %{#attrArray_stat.index}">
								<td style="font-weight: bold">
									<s:property value="%{#smAttr.name}" />:
								</td>
								<td>
									 <s:if test="%{#smAttr.name.indexOf('Event Test') != -1 && #smAttr.value.indexOf('STATUS') != -1}" >
                                             <span style="font-weight: bold; background-color: RED"><s:property escapeHtml="false" value="%{#smAttr.value}" /></span>
                                     </s:if>
                                     <s:else>
                                             <s:property escapeHtml="false" value="%{#smAttr.value}" />
                                     </s:else>
								</td>
							</tr>
						</s:iterator>
						<tr>
								<td style="font-weight: bold">Master Config # :</td>
								<td><s:property escapeHtml="false" value="%{currentConfigSerialNumber}" />
								</td>
						</tr>
					</table></td>
			</tr>
			<tr height="100%">
				<td colspan="10" width="100%" align="center">
					<table style="width:100%; height:100%">
						<tr height="100%">
							<s:iterator var="station" value="substationHlth.DFRArray"
								status="dfrArray_stat">
								<s:if test="%{#dfrArray_stat.count > #nofChassisPerLine && #dfrArray_stat.count % #nofChassisPerLine == 1}">
						</tr>
						<tr height="100%">
							</s:if>
							<td><table class="wwFormTable" >
									<s:if test="%{status == 'Active'}">
										<tr>
											<td colspan="2" align="center"
												style="font-weight: bold; background-color: #9999CC"><s:property
													value="%{name}" /> - <s:property value="%{status}" /></td>
										</tr>
									</s:if>
									<s:else>
										<tr>
											<td colspan="2" align="center"
												style="font-weight: bold; background-color: RED"><s:property
													value="%{name}" /> - <s:property value="%{status}" /></td>
										</tr>
										<tr>
											<td colspan="2" align="center"
												style="font-weight: bold; background-color: RED"><s:property
													value="%{statusInfo}" /></td>
										</tr>
									</s:else>
									<br>
									<s:iterator var="Attr" value="#station.AttributeArray"
										status="attrArray_stat">
										<div>
											<s:if test="%{#Attr.name == 'ConfigSerialNumber' && #Attr.value != currentConfigSerialNumber.toString()}" >
													<s:label  label="%{#Attr.name}" value="%{#Attr.value}" cssStyle="font-weight: bold; background-color: RED" tooltip="It is different than Master Config #. One possibility is user just did a 'Finish' not 'Finish and Send'."/>
											</s:if>
											<s:elseif test="%{#Attr.name == 'ConfigSerialNumber'}">
												<s:label tooltip="Configuration serial number should match with Master Config # above" label="Config #" value="%{#Attr.value}" />
											</s:elseif>
											<s:elseif test="%{#Attr.name == 'Locked' }">
												<s:if test="%{#Attr.value == 0}">
													<s:label  cssStyle="font-weight: bold; background-color: RED" label="IRIG" value="Unlocked" />
												</s:if>
												<s:else>
													<s:label label="IRIG" value="Locked" />
											</s:else>
											</s:elseif>
											<s:else>
												<s:label label="%{#Attr.name}" value="%{#Attr.value}" />
											</s:else>
										</div>
									</s:iterator>
								</table></td>
							</s:iterator>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</s:else>
</div>