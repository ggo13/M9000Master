<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sjdt" uri="/struts-jquery-datatables-tags"%>
<script type="text/javascript"
        src="js/jquery.validate.js"></script>
<script type="text/javascript"
        src="js/additional-methods.js"></script>
<script type="text/javascript"
        src="js/jquery-migrate-1.2.1.min.js"></script>
<script src="js/m9000/advancedSettings.js">
</script>

<script>
    var userRole = "<s:property value='#session.userDetails.role'/>";
    var accessMode = "<s:property value='accessMode'/>";
    var isPmuEnabled = "<s:property value='stationDetails.pmuEnabled'/>";
    var pdcStreamType = "<s:property value='stationDetails.pdcStreamType'/>";
    var isDfrAddedOrRemoved = ${ stationDetails.dfrAddedOrRemoved };
    var isWettingVoltageMonitored = ${ stationDetails.wettingVoltageMonitor };
</script>

<div style="display: flex; justify-content: center;">
    <s:form id="advancedSettings"
            name="advancedSettings"
            action="saveAdvancedSettings"
            method="POST"
            cssStyle="width:80%">
        <tr>
            <td colspan="2"
                class="titleLabel"
                style="background-color: #9999CC">Edit
                Advanced Details/Channel Config</td>
        </tr>
        <tr id="tblButtonsTop">
            <td colspan="2"
                align="center">
                <s:submit theme="simple"
                          formnovalidate="formnovalidate"
                          cssClass="submit"
                          id="btnCancel_Top"
                          name="Cancel"
                          key="label.cancel"
                          action="backFromStationDetails"
                          onclick="$('#advancedSettings').attr('novalidate','novalidate')" />
                <s:submit theme="simple"
                          cssClass="submit"
                          name="btnSave"
                          key="label.save"
                          action="saveAdvancedSettings"
                          onclick="javascript:showBusyCursor();return true;" />
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <s:actionerror id="userMsgError"
                               theme="jquery" />
            </td>
        </tr>
        <s:select name="stationDetails.enableEventTest"
                  key="label.system.eventTestCapability"
                  list="#{'true':'Enable','false':'Disable'}"
                  value="%{stationDetails.enableEventTest}"
                  tooltipDelay="10000"
                  tooltip="Disable for event boards that do not support event self test" />
        <s:textfield name="stationDetails.triggerDuration"
                     required="true"
                     requiredLabel="true"
                     tooltip="Time Duration to activate Trigger LED and Relay"
                     key="label.system.triggerDuration"
                     value="%{stationDetails.triggerDuration}" />
        <s:textfield id="stationDetails.contOscDaysToRetain"
                     name="stationDetails.contOscDaysToRetain"
                     required="true"
                     requiredLabel="true"
                     tooltip="Continuous Oscillography Data retention period in days before purging"
                     key="label.system.contOscDaysToRetain"
                     value="%{stationDetails.contOscDaysToRetain}"
                     onkeyup="javascript:warnOscDays(this, this.value)" />
        <s:textfield id="stationDetails.contMeasurementsDaysToRetain"
                     name="stationDetails.contMeasurementsDaysToRetain"
                     required="true"
                     requiredLabel="true"
                     tooltip="Continuous Measurements Data retention period in days before purging"
                     key="label.system.contMeasurementsDaysToRetain"
                     value="%{stationDetails.contMeasurementsDaysToRetain}"
                     onkeyup="javascript:warnMeasurementsDays(this, this.value)" />
        <s:textfield id="stationDetails.contOscExportTimeLimit"
                     name="stationDetails.contOscExportTimeLimit"
                     required="true"
                     requiredLabel="true"
                     tooltip="Export Continuous Oscillography Data time limit for 'Continuous Oscillography' tab in Seconds"
                     key="label.system.contOscExportTimeLimit"
                     value="%{stationDetails.contOscExportTimeLimit}"
                     onkeyup="javascript:warnOscExportLimit(this, this.value)" />
        <s:textfield id="stationDetails.contMeasurementsExportTimeLimit"
                     name="stationDetails.contMeasurementsExportTimeLimit"
                     required="true"
                     requiredLabel="true"
                     tooltip="Export Continuous Measurements Data time limit for 'Continuous Measurements' tab in Seconds"
                     key="label.system.contMeasurementsExportTimeLimit"
                     value="%{stationDetails.contMeasurementsExportTimeLimit}"
                     onkeyup="javascript:warnMeasurementExportLimit(this, this.value)" />
        <s:textfield name="stationDetails.ddrDeckTimeLimit"
                     required="true"
                     requiredLabel="true"
                     tooltip="Time limit for DDR Record's length so that it doesn't get huge. All DDR records created will be in chunks of this size of specified time."
                     key="label.system.ddrDeckTimeLimit"
                     value="%{stationDetails.ddrDeckTimeLimit}" />

        <s:iterator value="lstAlarmNames"
                    status="alarm_stat">
            <s:if test="%{#alarm_stat.index == 0}">
                <tr id="alarmsRelayMapping">
                    <td colspan="2">
                        <fieldset style="text-align: center;">
                            <legend>
                                Alarms To Relays Mapping
                            </legend>
                            <div>
                                <table id="table_"
                                       class="configureTable"
                                       style="width: 89.5%; border-width: 0px;">
            </s:if>
            <tr id="row_%{#alarm_stat.index}">
                <td align="right"
                    width="61%">
                    <s:label theme="simple"
                             cssStyle="font-weight:bold"
                             name="stationDetails.lbl_%{#alarm_stat.index}"
                             value="'%{lstAlarmNames[#alarm_stat.index]}':" />
                </td>
                <td align="left">
                    <s:select theme="simple"
                              id="stationDetails.lstAlarmRelaysMapping[%{#alarm_stat.index}]"
                              name="stationDetails.lstAlarmRelaysMapping[%{#alarm_stat.index}]"
                              label="'%{lstAlarmNames[#alarm_stat.index]}'"
                              list="lstRelays"
                              value="%{stationDetails.lstAlarmRelaysMapping[#alarm_stat.index]}" />
                </td>
            </tr>
            <s:if test="%{#alarm_stat.index == 7}">
                </table>
                </div>
                </fieldset>
                </td>
                </tr>
            </s:if>
        </s:iterator>

        <tr id="tblButtonsBottom">
            <td colspan="2"
                align="center">
                <s:submit theme="simple"
                          formnovalidate="formnovalidate"
                          cssClass="submit"
                          id="btnCancel_bottom"
                          name="CancelBottom"
                          key="label.cancel"
                          action="backFromStationDetails"
                          onclick="$('#advancedSettings').attr('novalidate','novalidate')" />
                <s:submit theme="simple"
                          cssClass="submit"
                          name="btnSaveBottom"
                          key="label.save"
                          action="saveAdvancedSettings"
                          onclick="javascript:showBusyCursor();return true;" />
            </td>
        </tr>
        <s:hidden id="jspFileName"
                  name="jspFileName"
                  value="advancedSettings.jsp"></s:hidden>
    </s:form>
</div>

</html>