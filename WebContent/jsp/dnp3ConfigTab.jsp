<%@ taglib
    prefix="s"
    uri="/struts-tags"
    %>
    <%@ taglib
        prefix="sj"
        uri="/struts-jquery-tags"
        %>
        <link href="<s:url value='/css/dnp3Configuration.css'/>"
              rel="stylesheet"
              type="text/css" />
        <script type="text/javascript"
                src="js/m9000/dnp3ConfigTab.js"></script>
        <div class="dnp3-container">
            <div class="dnp3-configuration-container">
                <div class="dnp3-configuration-header">
                    <h2>DNP3 Configuration</h2>
                </div>
                <s:form id="dnp3ConfigForm"
                        action="postDnp3Configuration"
                        method="post"
                        cssClass="dnp3-configuration-form"
                        theme="simple">
                    <div class="outstation-detail-container">
                        <h3>Outstation Details</h3>
                        <div class="outstation-form-body">
                            <div class="outstation-form-item">
                                <label class="outstation-form-item-label" for="dnp3OutstationDetail.transportMethod">Transport Method</label>
                                <div class="outstation-radio-container">
                                    <s:radio name="dnp3OutstationDetail.transportMethod"
                                            list="{'TCP/IP', 'Serial'}"
                                            label="Transport Method" />
                                </div>
                            </div>
                            <div class="outstation-form-item tcp-port-container">
                                <label class="outstation-form-item-label" for="dnp3OutstationDetail.portNumber">Port Number</label>
                                <s:textfield name="dnp3OutstationDetail.portNumber" label="Port Number" />
                            </div>
                            <div class="outstation-form-item serial-port-container">
                                <label class="outstation-form-item-label" for="dnp3OutstationDetail.serialPortPath">Serial Port</label>
                                <s:select name="dnp3OutstationDetail.serialPortPath"
                                        list="lstSerialPorts"
                                        listKey="systemPortPath"
                                        listValue="descriptivePortName"
                                />
                            </div>
                            <div class="outstation-form-item serial-port-container">
                                <label class="outstation-form-item-label" for="dnp3OutstationDetail.baudRate">Baud Rate</label>
                                <s:select 
                                    name="dnp3OutstationDetail.baudRate"
                                    label="Baud Rate"
                                    list="#{'300':'300','600':'600','1200':'1200','2400':'2400','4800':'4800','9600':'9600',
                                            '19200':'19200','28800':'28800','38400':'38400','57600':'57600','115200':'115200'}"
                                />
                            </div>
                            <div class="outstation-form-item">
                                <label class="outstation-form-item-label" for="dnp3OutstationDetail.faultLocationTimeLimitInSeconds">Fault Location Time Limit</label>
                                <s:select 
                                    name="dnp3OutstationDetail.faultLocationTimeLimitInSeconds"
                                    label="Fault Location Time Limit"
                                    list="#{'300':'5 minutes','600':'10 minutes','1800':'30 minutes','3600':'1 hour','21600':'6 hours','43200':'12 hours','86400':'24 hours'}"
                                />
                            </div>
                        </div>
                    </div>
                    <div class="channel-form-container">
                        <h3>Channel Assignment</h3>
                        <div class="form-label-container">
                            <div class="dnp3-index-value">Index</div>
                            <span>Channel</span>
                            <span>Source Type</span>
                        </div>
                        <div class="form-body">
                            <s:iterator value="lstDnp3Configurations"
                                        status="rowStatus">
                                <div class="form-input-row"
                                     id="dnp3-configuration-item">
                                    <s:textfield name="lstDnp3Configurations[%{#rowStatus.index}].dnpIndex"
                                                 cssClass="dnp3-index-value"
                                                 value="%{dnpIndex}" />
                                    <s:select name="lstDnp3Configurations[%{#rowStatus.index}].dnpChannel"
                                              list="exportList"
                                              listKey="id"
                                              listValue="exportName"
                                              headerKey=""
                                              headerValue=""
                                              value="%{dnpChannel}" />
                                    <s:select name="lstDnp3Configurations[%{#rowStatus.index}].sourceType"
                                              list="lstDnp3SourceTypes"
                                              listKey="sourceType"
                                              listValue="sourceType"
                                              headerKey=""
                                              headerValue=""
                                              value="%{sourceType}" />
                                    <button class="delete-row-btn"
                                            type="button">Delete</button>
                                </div>
                            </s:iterator>
                        </div>
                    </div>
                    <div class="form-footer">
                        <button id="add-row-btn"
                                type="button">Add Mapping</button>
                        <s:submit value="Save"
                                  cssClass="save-button" />
                    </div>
                </s:form>
            </div>
            <!-- <div class="dnp3-control-container">
                <h2>Measurement Details</h2>
                <s:iterator value="exportList">
                    <div class="measurement-container">
                        <div>
                            id:
                            <s:property value="id" />
                        </div>
                        <div>
                            Name:
                            <s:property value="name" />
                        </div>
                        <div>
                            Measurement:
                            <s:property value="exportName" />
                        </div>
                        <div>
                            Units:
                            <s:property value="units" />
                        </div>
                        <div>
                            Sample Rate:
                            <s:property value="sampleRate" />
                        </div>
                    </div>
                </s:iterator>
            </div> -->
        </div>