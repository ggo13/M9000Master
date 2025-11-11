<%@ taglib prefix="s" uri="/struts-tags"%>
<table style="width:100%">
	<tr>
		<td colspan="100%" align="center">
			<div id="divFaultDetails" style="text-align: center; disabled: true;">
				<s:label theme="simple" value="Fault Location Details"></s:label>
				<table class="inlineTable" title="Fault Location Details">
					<tr>
						<td style="text-align: right;"><s:label
								cssStyle="font-weight:bold; text-align:right;" theme="simple"
								key="label.lg.autocalculate"></s:label></td>
						<td><s:select theme="simple"
								name="lstLineGroups[%{selectedLineGroupIndex}].enableAutoCalc"
								key="label.lg.autocalculate" list="{'No','Yes'}"
								value="%{lstLineGroups[selectedLineGroupIndex].enableAutoCalc}" />
						</td>
					</tr>
					<tr>
						<td style="text-align: right;"><s:label
								cssStyle="font-weight:bold;" theme="simple"
								key="label.lg.positiveresistance"></s:label></td>
						<td><s:textfield size="3" theme="simple"
								name="lstLineGroups[%{selectedLineGroupIndex}].positiveResistance"
								key="label.lg.positiveresistance"
								value="%{lstLineGroups[selectedLineGroupIndex].positiveResistance}" />
						</td>
					</tr>
					<tr>
						<td style="text-align: right;"><s:label
								cssStyle="font-weight:bold;" theme="simple"
								key="label.lg.positivereactance"></s:label></td>
						<td><s:textfield size="3" theme="simple"
								name="lstLineGroups[%{selectedLineGroupIndex}].positiveReactance"
								key="label.lg.positivereactance"
								value="%{lstLineGroups[selectedLineGroupIndex].positiveReactance}" />
						</td>
					</tr>
					<tr>
						<td style="text-align: right;"><s:label
								cssStyle="font-weight:bold;" theme="simple"
								key="label.lg.zeroresistance"></s:label></td>
						<td><s:textfield size="3" theme="simple"
								name="lstLineGroups[%{selectedLineGroupIndex}].zeroResistance"
								key="label.lg.zeroresistance"
								value="%{lstLineGroups[selectedLineGroupIndex].zeroResistance}" />
						</td>
					</tr>
					<tr>
						<td style="text-align: right;"><s:label
								cssStyle="font-weight:bold;" theme="simple"
								key="label.lg.zeroreactance"></s:label></td>
						<td><s:textfield size="3" theme="simple"
								name="lstLineGroups[%{selectedLineGroupIndex}].zeroReactance"
								key="label.lg.zeroreactance"
								value="%{lstLineGroups[selectedLineGroupIndex].zeroReactance}" />
						</td>
					</tr>
					<tr>
						<td style="text-align: right;"><s:label
								cssStyle="font-weight:bold;" theme="simple"
								key="label.lg.linelength"></s:label></td>
						<td><s:textfield size="3" theme="simple"
								name="lstLineGroups[%{selectedLineGroupIndex}].lineMiles"
								key="label.lg.linelength"
								value="%{lstLineGroups[selectedLineGroupIndex].lineMiles}" /></td>
					</tr>
				</table>
			</div>
		</td>
	</tr>
	<tr>
		<td style="font-size: 12px;" align="center"><s:optiontransferselect
				cssStyle="font: 12px verdana, arial, helvetica, sans-serif;"
				doubleCssStyle="font: 12px verdana, arial, helvetica, sans-serif;"
				buttonCssClass="submit" doubleSize="12" size="12" headerKey="HEADERNOTRANSFER"
				headerValue="--- Selected Channels ---" doubleHeaderKey="HEADERNOTRANSFER"
				doubleHeaderValue="--- Available Channels -- " emptyOption="false"
				doubleEmptyOption="false" allowAddAllToLeft="false"
				allowAddAllToRight="false" allowSelectAll="false"
				allowUpDownOnLeft="false" allowUpDownOnRight="false"
				leftTitle="%{lstLineGroups[selectedLineGroupIndex].name}"
				rightTitle="Channels List" id="lineGroupDetails"
				name="lineGroupDetails"
				list="lstLineGroups[selectedLineGroupIndex].inputChannels"
				doubleId="availableChannels" doubleName="availableChannels"
				doubleList="lstAvailableAnalogChannels" formName="newEditLineGroup"
				addToLeftOnclick="javascript:modify_linegroup()"
				addToRightOnclick="javascript:modify_linegroup()" /></td>
		<td>
			<s:textarea labelposition="top" label="Comments"  tooltip="Any user comments or notes for this LineGroup"  name="lstLineGroups[%{selectedLineGroupIndex}].comments" cols="30" rows="10" cssStyle="overflow: scroll; resize: none;" value="%{editedLineGroup.comments}"/>
		</td>
	</tr>
</table>