<!DOCTYPE HTML>
<%@ taglib prefix="s" uri="/struts-tags"%>
<br>
<div id="divInfo" align="center" style="margin:10px;">
	<s:iterator value="lstLineGroups[selectedLineGroupIndex].mapLinegroupWarningMessages" var="mapMsg">
		<span style='color:rgb(71, 71, 166);border:5px solid'>${mapMsg.value}</span>
		<br><br>
	</s:iterator>
</div>
<div style="text-align:center;font-weight:bold"><s:property value="%{lstLineGroups[selectedLineGroupIndex].lineGroupName}"/></div>
	<div id="divFaultDetails" align="left" style="width:60%;text-align: center; disabled: true;">
				<s:label theme="simple" value="Fault Location Details"></s:label>
				<table class="inlineTable" title="Fault Location Details">
					<tr>
						<td style="text-align: right;width:50%"><s:label
								cssStyle="font-weight:bold; text-align:right;" theme="simple"
								key="label.lg.autocalculate"></s:label></td>
						<td><s:textfield size="10" theme="simple"
								name="lstLineGroups[%{selectedLineGroupIndex}].enableAutoCalc"
								value="%{lstLineGroups[selectedLineGroupIndex].enableAutoCalc}"
								readonly="true" /></td>
					</tr>
					<tr>
						<td style="text-align: right;"><s:label
								cssStyle="font-weight:bold;" theme="simple"
								key="label.lg.positiveresistance"></s:label></td>
						<td><s:textfield size="10" theme="simple"
								name="lstLineGroups[%{selectedLineGroupIndex}].positiveResistance"
								key="label.lg.positiveresistance" readonly="true"
								value="%{lstLineGroups[selectedLineGroupIndex].positiveResistance}" />
						</td>
					</tr>
					<tr>
						<td style="text-align: right;"><s:label
								cssStyle="font-weight:bold;" theme="simple"
								key="label.lg.positivereactance"></s:label></td>
						<td><s:textfield size="10" theme="simple"
								name="lstLineGroups[%{selectedLineGroupIndex}].positiveReactance"
								key="label.lg.positivereactance" readonly="true"
								value="%{lstLineGroups[selectedLineGroupIndex].positiveReactance}" />
						</td>
					</tr>
					<tr>
						<td style="text-align: right;"><s:label
								cssStyle="font-weight:bold;" theme="simple"
								key="label.lg.zeroresistance"></s:label></td>
						<td><s:textfield size="10" theme="simple"
								name="lstLineGroups[%{selectedLineGroupIndex}].zeroResistance"
								key="label.lg.zeroresistance" readonly="true"
								value="%{lstLineGroups[selectedLineGroupIndex].zeroResistance}" />
						</td>
					</tr>
					<tr>
						<td style="text-align: right;"><s:label
								cssStyle="font-weight:bold;" theme="simple"
								key="label.lg.zeroreactance"></s:label></td>
						<td><s:textfield size="10" theme="simple"
								name="lstLineGroups[%{selectedLineGroupIndex}].zeroReactance"
								key="label.lg.zeroreactance" readonly="true"
								value="%{lstLineGroups[selectedLineGroupIndex].zeroReactance}" />
						</td>
					</tr>
					<tr>
						<td style="text-align: right;"><s:label
								cssStyle="font-weight:bold;" theme="simple"
								key="label.lg.linelength"></s:label></td>
						<td><s:textfield size="10" theme="simple"
								name="lstLineGroups[%{selectedLineGroupIndex}].lineMiles"
								key="label.lg.linelength" readonly="true"
								value="%{lstLineGroups[selectedLineGroupIndex].lineMiles}" /></td>
					</tr>
				</table>
			</div>
		<div align="center">
			<fieldset style="text-align: center; width: 60%">
				<legend>Channels list</legend>
					<s:select cssStyle="font-family:courier, courier new, serif;border-style: solid solid solid solid;width:100%"
						theme="simple" name="lineGroupDetails" size="10"
						list="lstLineGroups[selectedLineGroupIndex].inputChannels"
						></s:select>
			</fieldset>
		</div>
				<div align="center">
				<fieldset style="text-align: center; width: 60%">
				<legend>Comments</legend>
						<s:textarea theme="simple" labelposition="top" label="Comments" tooltip="Any user comments or notes for this LineGroup" 
							readonly="true" name="lstLineGroups[%{selectedLineGroupIndex}].comments" cols="104" rows="10" cssStyle="overflow: scroll; resize: none;" value="%{lstLineGroups[selectedLineGroupIndex].comments}"/>
				</fieldset>
				</div>
		
