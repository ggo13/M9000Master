<%@ include file="header.jsp"%>
<%@ page import="com.usi.m9000.dto.StationDTO"%>
<%@ page import="com.usi.m9000.dto.UsersDTO"%>
<s:form theme="simple" name="frmScope" action="viewScope">
	<table class="wwFormTable" style="width: 100%;height:100%">

		<% 	Integer strStationId;
	strStationId = Integer.parseInt(""+session.getAttribute("StationId"));
	String stationName=((StationDTO)session.getAttribute("stationDetails")).getSystemStationName();
	String userRole = ((UsersDTO)session.getAttribute("userDetails")).getRole();
  %>
		<tr>
			<td width="100%" class="titleLabel" style="background-color: #9999CC">M9k
				Scope <%= strStationId %> - <%= stationName %></td>
		</tr>
		<tr>
			<td width="100%" align="right"><s:submit theme="simple"
					align="left" cssClass="submit" name="btnSubmit" value="Return"
					action="backToStationList" /></td>
		</tr>
		<tr height="100%">
			<td width="100%"><jsp:plugin type="applet"
					archive="mysql-connector-java-5.1.13-bin.jar, M9kApplet.jar, log4j-1.2.16.jar, jfreechart-1.0.13.jar, jcommon-1.0.16.jar, commons-logging-1.0.4.jar, xbean.jar, M9000XMLConfig.jar, commons-lang-2.4.jar, commons-collections-3.2.1.jar, activemq-all-5.4.2-fuse-00-00.jar"
					code="com.usi.m9000.chart.applet.M9000ChartApplet.class"
					codebase="." width="100%" height="100%">
					<jsp:params>
						<jsp:param name="StationId" value="<%= strStationId %>" />
						<jsp:param name="userRole" value="<%= userRole %>" />
						<jsp:param name="java_arguments" value="-Xmx1500m" />
						<jsp:param name="classloader_cache" value="true" />
						<jsp:param name="cache_option" value="Plugin" />
						<jsp:param name="cache_archive" value="M9kApplet.jar" />
						<jsp:param name="image" value="USI-Logo.jpg" />
						<jsp:param name="progressbar" value="true" />
						<jsp:param name="centerimage" value="true" />
						<jsp:param name="boxborder" value="true" />
						<jsp:param name="boxbgcolor" value="#EEEEFF" />
						<jsp:param name="boxmessage" value="USI SCOPE Applet is loading." />
					</jsp:params>

					<jsp:fallback>
			                 <p>Unable to load applet</p>
			           </jsp:fallback>
				</jsp:plugin></td>
		</tr>
		<tr>
			<td width="100%" align="right"><s:submit theme="simple"
					align="left" cssClass="submit" name="btnSubmit" value="Return"
					action="backToStationList" /></td>
		</tr>
	</table>
</s:form>
