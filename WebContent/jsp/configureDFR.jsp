<div id="divList">
	<%@ include file="header.jsp"%>
	<body>
		<script>
function change_image(id, imageName)
{
	var imgTag = "<img src='images/dfr"+imageName+".jpg'/>";
	document.getElementById('imageDiv'+id).innerHTML = imgTag;
}
function verifyTotalChannels()
{
var returnValue = true;
 var totalAnalogs = ${stationDetails.systemAnalogChannelsCount};
 var totalDigitals = ${stationDetails.systemDigitalChannelsCount};
	var form = document.forms["configDfr"];
	var i = 0;
	var objPrefix = 'lstDfrDTO['+ i +'].';
	var objName = objPrefix+"dfrId";
	var analogsCount = 0;
	var digitalsCount = 0;
	var chnlConfig;
	while (form.elements[objName] != null)
	{
	   objName = objPrefix+"chnlConfigDropDownList.selectedValue";
	   chnlConfig = $('#lstChannels'+i).val();
	  console.log("value selected "+chnlConfig + " index of "+chnlConfig.indexOf("mini"));
	  if (chnlConfig.indexOf("mini") != -1)
	  {
	  	analogsCount+=12;
	  	digitalsCount+=16;
	  }
	  else if (chnlConfig.indexOf("ANEV") != -1)
	  {
	  	analogsCount+=4;
	  	digitalsCount+=16;
	  	analogsCount+=parseInt(chnlConfig.substring(0, chnlConfig.indexOf("A")));
	  	digitalsCount+=parseInt(chnlConfig.substring(chnlConfig.indexOf("A")+2, chnlConfig.indexOf("D")));
	  }
	  else 
	  {
	  	analogsCount+=parseInt(chnlConfig.substring(0, chnlConfig.indexOf("A")));
	  	digitalsCount+=parseInt(chnlConfig.substring(chnlConfig.indexOf("A")+2, chnlConfig.indexOf("D")));
	  }
	  i++;
	   objPrefix = 'lstDfrDTO['+ i +'].';
	   objName = objPrefix+"dfrId";
	}
	console.log("Final analogs count "+analogsCount+" digitals count "+digitalsCount);
	if (analogsCount != totalAnalogs || digitalsCount != totalDigitals)
	{

			if (confirm("Configured channels count doesn't match with specified count. \n    Original Channels count entered "
							+ totalAnalogs
							+ "A - "
							+ totalDigitals
							+ "D \n    Actual channels count configured "
							+ analogsCount
							+ "A - "
							+ digitalsCount
							+ "D \nDo you want to update with new configured count?")) {
						returnValue = true;
					} else {
						returnValue = false;
					}
				}
				return returnValue;
			}
		</script>
		<s:form name="configDfr" action="configDfr" method="POST"
			cssStyle="border-width: 0px;background-color:white;">
			<s:if test="%{accessMode != 'Edit'}">
				<tr>
					<td align="center"><b> Channels required: Analog Channels:
							<s:property value="%{stationDetails.systemAnalogChannelsCount}" />
							Digital Channels: <s:property
								value="%{stationDetails.systemDigitalChannelsCount}" />
					</b></td>
				</tr>
			</s:if>
			<tr>
				<td align="right" style="background-color: white;"><s:iterator
						value="lstDfrDTO" status="lstDfrDTO_stat">
						<table class="inlineTable">
							<tr>
								<s:if test="%{accessMode != 'Edit'}">
									<td colspan="2" align="center"
										style="font-weight: bold; background-color: #9999CC">Chassis
										<s:property value="%{#lstDfrDTO_stat.index+1}" />
									</td>
								</s:if>
								<s:else>
									<td colspan="2" align="center"
										style="font-weight: bold; background-color: #9999CC"><s:property
											value="%{lstDfrDTO[#lstDfrDTO_stat.index].dfrName}" /></td>
								</s:else>
							</tr>
							<tr>
								<td>
									<div id="imageDiv<s:property value='%{#lstDfrDTO_stat.index}'/>">
										<img
											src="images/dfr<s:property value="%{chnlConfigDropDownList.selectedValue}"/>.jpg"
											alt="Chassis<s:property value="%{#lstDfrDTO_stat.index}"/>" />
									</div></td>
								<td>
									<table class="wwFormTable">
										<s:textfield
											name="lstDfrDTO[%{#lstDfrDTO_stat.index}].dfrName"
											id="DFRId%{#lstDfrDTO_stat.index}" label="Chassis Id"
											size="15" value="%{dfrName}" disabled="true" />
										<s:textfield
											name="lstDfrDTO[%{#lstDfrDTO_stat.index}].ipAddress"
											id="iPAddress%{#lstDfrDTO_stat.index}"
											label="Chassis IP Address" size="13" value="%{ipAddress}"
											disabled="true" />
											<s:select
												name="lstDfrDTO[%{#lstDfrDTO_stat.index}].chnlConfigDropDownList.selectedValue"
												id="lstChannels%{#lstDfrDTO_stat.index}"
												list="channelsConfList"
												label="Select channels config"
												onchange="javascript:change_image('%{#lstDfrDTO_stat.index}', this.value);return false;"
												value="chnlConfigDropDownList.selectedValue">
											</s:select>
											<!--  
											<s:textfield 
											name="lstDfrDTO[%{#lstDfrDTO_stat.index}].serialNumber"
											id="serialNumber%{#lstDfrDTO_stat.index}"
											label="Chassis Serial Number" size="5" value="%{serialNumber}"
											/>
											<sj:datepicker id="lstDfrDTO[%{#lstDfrDTO_stat.index}].installationDate" name="installationDate%{#lstDfrDTO_stat.index}" label="Chassis Installation Date" value="" />
											-->
											<s:if
												test="%{#lstDfrDTO_stat.index > 0}">
												<s:submit id="lstBtnDelete[%{#lstDfrDTO_stat.index}]"
													name="lstBtnDelete[%{#lstDfrDTO_stat.index}]"
													cssClass="submit" value="Delete"></s:submit>
											</s:if>
									</table>
								</td>
							</tr>
						</table>
						<s:hidden theme="simple" name="lstDfrDTO[%{#lstDfrDTO_stat.index}].dfrId"
							value="%{dfrId}"></s:hidden>
						<s:hidden theme="simple" name="lstDfrDTO[%{#lstDfrDTO_stat.index}].status"
							value="%{status}"></s:hidden>
						<s:hidden theme="simple" 
							name="lstDfrDTO[%{#lstDfrDTO_stat.index}].analogChannelStart"
							value="%{analogChannelStart}"></s:hidden>
						<s:hidden theme="simple" 
							name="lstDfrDTO[%{#lstDfrDTO_stat.index}].analogChannelEnd"
							value="%{analogChannelEnd}"></s:hidden>
						<s:hidden theme="simple" 
							name="lstDfrDTO[%{#lstDfrDTO_stat.index}].digitalChannelStart"
							value="%{digitalChannelStart}"></s:hidden>
						<s:hidden theme="simple" 
							name="lstDfrDTO[%{#lstDfrDTO_stat.index}].digitalChannelEnd"
							value="%{digitalChannelEnd}"></s:hidden>
					</s:iterator></td>
			</tr>
			<tr>
				<td align="center" style="background-color: white;"><s:submit
						align="left" theme="simple" cssClass="submit" name="btnSubmit"
						value="Back" action="configDfrBack"></s:submit> <s:if
						test="%{accessMode == 'Edit'}">
						<s:submit align="center" theme="simple" cssClass="submit"  onclick="return verifyTotalChannels()"
							name="btnSubmit" value="Next" action="configDfrNext"></s:submit>
					</s:if> <s:else>
						<s:submit align="center" theme="simple" cssClass="submit" onclick="return verifyTotalChannels()"
							name="btnSubmit" value="Save" action="configDfrSave"></s:submit>
						<s:submit align="right" theme="simple" cssClass="submit"
							name="btnSubmit" value="Add a new DFR" action="addNewDFR"></s:submit>
					</s:else></td>
			</tr>
		</s:form>
	</body>
	</html>
</div>