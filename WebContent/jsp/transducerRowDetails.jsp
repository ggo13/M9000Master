<!DOCTYPE HTML>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<tr id="tr_sub_<s:property value='%{analogChannelIndex}'/>"><td colspan='9' align='center'><table class='configureSubrow'>
												<tr><th>InP1</th><th>OutP1</th><th>InP2</th><th>OutP2</th><th>Units</th><tr>
												<tr><td align="center"><s:textfield 
												name="lstAnalogChannels[%{analogChannelIndex}].inP1"
												id="lstAnalogChannels[%{analogChannelIndex}].inP1"
												required="true" size="3" value="Input1" /></td><td align="center"  style="border-right:solid 2px black;"><s:textfield 
												name="lstAnalogChannels[%{analogChannelIndex}].outP1"
												id="lstAnalogChannels[%{analogChannelIndex}].outP1"
												required="true" size="3" value="Output1" /></td><td align="center"><s:textfield 
												name="lstAnalogChannels[%{analogChannelIndex}].inP2"
												id="lstAnalogChannels[%{analogChannelIndex}].inP2"
												required="true" size="3" value="Input2" /></td><td align="center" style="border-right:solid 2px black;"><s:textfield 
												name="lstAnalogChannels[%{analogChannelIndex}].outP2"
												id="lstAnalogChannels[%{analogChannelIndex}].outP2"
												required="true" size="3" value="Output2" /></td><td align="center"><s:textfield 
												name="lstAnalogChannels[%{analogChannelIndex}].units"
												id="lstAnalogChannels[%{analogChannelIndex}].units"
												required="true" size="3" value="Units" /></td></tr></table></td></tr>