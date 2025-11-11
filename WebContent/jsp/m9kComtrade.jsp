<%@ taglib prefix="s" uri="/struts-tags"%>
<% 	String fileName = new String();
	String faultId = new String();
	fileName = (String)session.getAttribute("FileName");
	faultId = (String)session.getAttribute("FaultId");
  %>
<s:form theme="simple" name="frmComtrade" action="viewComtrade">
	<table class="wwFormTable" style="width: 100%;height:100%">
		<s:actionerror id="userMsgError" theme="jquery"/>
		<tr>
			<td width="100%" class="titleLabel" style="background-color: #9999CC">M9k
				Comtrade Viewer</td>
		</tr>
		<tr>
			<td width="100%" class="titleLabel" style="background-color: #EEEEFF">Fault
				Id: <s:property value="%{faultId}" />
			</td>
		</tr>
		<tr>
			<td width="100%" align="right"><s:url var="fetchComtrade"
					method="fetchComtradeFiles" /> <s:submit theme="simple"
					align="left" cssClass="submit" name="btnSubmit" value="Close"
					onClick="window.close();" /></td>
		</tr>
		<tr height="100%">
			<td width="100%"><jsp:plugin type="applet"
					archive="M9kMaster.jar, jfreechart-1.0.13.jar, jcommon-1.0.16.jar, commons-logging-1.0.4.jar"
					code="com.usi.m9000.chart.applet.M9kChartAppletComtrade.class"
					codebase="." width="100%" height="100%">
					<jsp:params>
						<jsp:param name="FileName" value="<%=fileName%>" />
					</jsp:params>
					<jsp:fallback>
			           <p>Unable to load applet</p>
			    </jsp:fallback>
				</jsp:plugin></td>
		</tr>
		<tr>
			<td width="100%" align="right"><s:submit theme="simple"
					align="left" cssClass="submit" name="btnSubmit" value="Close"
					onClick="window.close();" /></td>
		</tr>
	</table>
</s:form>
