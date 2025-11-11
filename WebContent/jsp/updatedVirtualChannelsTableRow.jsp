<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
		<s:if test="%{lstVirtualChannels != null && lstVirtualChannels.size()>0}">
					<tr>
					<td>
					<s:iterator value="lstVirtualChannels" status="virtual_stat">
						<s:url var="urlEditVirtualDialog#{virtual_stat.index}" action="editVirtualChannelsDialog" escapeAmp="false">
							<s:param name="selectedChannelIndex" value="%{#virtual_stat.index}"/>
							<s:param name="booAnalog" value="%{booAnalog}" />
							<s:param name="booDigital" value="%{booDigital}"/>

						</s:url>
						<s:url var="urlDeleteVirtualChannel#{virtual_stat.index}" action="deleteVirtualChannels">
								<s:param name="selectedChannelIndex" value="%{#virtual_stat.index}"/>
						</s:url>
					
										<tr style="background-color: #EEEEFF">
											<td width="5%" align="center">
											<s:label
													name="lstVirtualChannels[%{#virtual_stat.index}].virtualChannelNo"
													id="lstVirtualChannels[%{#virtual_stat.index}].virtualChannelNo"
													theme="simple" value="SUM%{virtualChannelNo}" />
													</td>
											<s:hidden theme="simple"
												name="lstVirtualChannels[%{#virtual_stat.index}].virtualChannelNo"></s:hidden>
											<s:hidden theme="simple"
												name="lstVirtualChannels[%{#virtual_stat.index}].channel"></s:hidden>
											<s:hidden theme="simple" name="lstVirtualChannels[%{#virtual_stat.index}].name"></s:hidden>
											<td width="34%" align="center">
													<s:textfield theme="simple" cssStyle="width:95%"
													name="lstVirtualChannels[%{#virtual_stat.index}].circuitName"
													required="true" size="64" maxlength="64" />
											</td>
											<td width="10%" align="center">
												<s:label cssStyle="width:95%"
														name="lstVirtualChannels[%{#virtual_stat.index}].virtualLogic"
														id="lstVirtualChannels[%{#virtual_stat.index}].virtualLogic"
														theme="simple" value="%{virtualLogic}" title="%{lstVirtualChannels[#virtual_stat.index].virtualLogicToolTip}"/>
											<sj:a openDialog="editVirtualDialog" cssStyle="width:10%" title="Edit Virtual Channel Details" href="%{urlEditVirtualDialog#{virtual_stat.index}}" button="true" buttonIconSecondary="ui-icon-pencil"></sj:a>
											</td>
											
											<td width="5%" align="center"><s:label theme="simple"
													name="lstVirtualChannels[%{#virtual_stat.index}].phase"
													id="lstVirtualChannels[%{#virtual_stat.index}].phase"
													value="%{phase}" /></td>
											<s:hidden theme="simple"
													name="lstVirtualChannels[%{#virtual_stat.index}].phase"></s:hidden>
											<td width="11%" align="center"><s:label theme="simple"
													name="lstVirtualChannels[%{#virtual_stat.index}].inputType"
													id="lstVirtualChannels[%{#virtual_stat.index}].inputType"
													value="%{inputType}" /></td>
											<s:hidden theme="simple"
													name="lstVirtualChannels[%{#virtual_stat.index}].inputType"></s:hidden>
											<td width="7%" align="center"><s:checkbox
													name="lstVirtualChannels[%{#virtual_stat.index}].exportStatus"
													id="lstVirtualChannels[%{#virtual_stat.index}].exportStatus"
													theme="simple" value="%{exportStatus}"
													onclick="javascript:isAllChecked();" /></td>
											<td width="5%" align="center">
											<s:submit theme="simple" cssClass="submit" id="%{#virtual_stat.index}" name="btnDeleteVirtual"
											onclick="confirmationDialog(this.id,'WARNING: There may be lingroups and Measurements associated with this virtual channel. Are you sure you want to delete?');return false;"
											value="Delete"/>
											<sj:a listenTopics="confirmed%{#virtual_stat.index}" formIds="configureVirtualChannels" href="%{urlDeleteVirtualChannel#{virtual_stat.index}}" targets="virtualChannelsBody"></sj:a>
											</td>
										</tr>
					</s:iterator>
					</td>
					</tr>
				</s:if>
				<s:else >
					<tr id="idEmptyRow">
						<td colspan="5" align="center"><s:label theme="simple" 
							value="No virtual channels to display"></s:label></td>
					</tr>
				</s:else>

