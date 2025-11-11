<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>

<div>
	<div style="margin:1em;">
		<s:select formIds="frmNewDialog" 
			cssStyle="font-family:courier, courier new, serif;border-style: solid solid solid solid;width:60%; overflow-x: auto;" 
			id="events" name="events" size="10" onclick="javascript:enableAdd();return false;" 
			list="lstDfrsMeasurements" listKey="measurementKey"
			listValue="displayNameForVirtualMeasurments" label="Select Input Measurement" labelposition="top"
			></s:select>

		<s:textfield cssStyle="width:10%" 
				id="virtualScale"
				name="virtualScale"
				value="1"
				label="Scale" tooltip="Multiplies final virtual channel result by this amount"
				 >
		</s:textfield>
	</div>
</div>
