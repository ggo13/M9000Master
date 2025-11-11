<%@ taglib prefix="s" uri="/struts-tags"%>
<tr>
	<td colspan="100%" style="font-size: 12px;" align="center"><s:optiontransferselect
			cssStyle="font: 12px verdana, arial, helvetica, sans-serif;"
			doubleCssStyle="font: 12px verdana, arial, helvetica, sans-serif;"
			buttonCssClass="submit" doubleSize="12" size="12" headerKey="HEADERNOTRANSFER"
			headerValue="--- Selected Channels ---" doubleHeaderKey="HEADERNOTRANSFER"
			doubleHeaderValue="--- Available Channels -- " emptyOption="false"
			doubleEmptyOption="false" allowAddAllToLeft="false"
			allowAddAllToRight="false" allowSelectAll="false"
			allowUpDownOnLeft="false" allowUpDownOnRight="false"
			leftTitle="Virtual Channels" rightTitle="Channels List"
			id="newVirtualChannel.lstAnalogsForVirtual" name="virtualChannelDetails"
			list="newVirtualChannel.lstAnalogsForVirtual" doubleId="availableChannels"
			doubleName="availableChannels"
			doubleList="lstDfrsAnalogChannels" formName="frmNewDialog"/>
	</td>
</tr>

