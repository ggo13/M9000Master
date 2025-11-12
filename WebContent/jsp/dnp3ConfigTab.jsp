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
                                <label>Transport Method</label>
                                <div class="outstation-checkbox-container">
                                    <div class="outstation-checkbox">
                                        <input type="checkbox" />
                                        <span>TCP/IP</span>
                                    </div>
                                    <div class="outstation-checkbox">
                                        <input type="checkbox" />
                                        <span>Serial</span>
                                    </div>
                                </div>
                            </div>
                            <div class="outstation-form-item">
                                <label>Port Number</label>
                                <input type="number" />
                            </div>
                            <div class="outstation-form-item">
                                <label>Fault Location Time Limit</label>
                                <select>
                                    <option>5 minutes</option>
                                    <option>10 minutes</option>
                                    <option>30 minutes</option>
                                    <option>1 hour</option>
                                    <option>6 hours</option>
                                    <option>12 hours</option>
                                    <option>24 hours</option>
                                </select>
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