package com.usi.m9000.station.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.xmlbeans.XmlOptions;

import com.usi.health.AttributeDocument.Attribute;
import com.usi.health.DFRDocument.DFR;
import com.usi.health.StationMasterDocument.StationMaster;
import com.usi.health.SubStationDocument;
import com.usi.health.SubStationDocument.SubStation;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.threads.M9kMonitorThreads;
import com.usi.m9000.station.util.M9kKeyValuePair;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;

public class M9kDFRHealthStatusProcessor {
//	static HierarchicalINIConfiguration iniConf;
	M9kStationCommandClient commandClient;
	 PropertiesConfiguration config;
	 XmlOptions xmlOptions;
	 SubStation substationHlth;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kDFRHealthStatusProcessor.class);

	public M9kDFRHealthStatusProcessor()
	{
		try {
//			iniConf = new HierarchicalINIConfiguration("station.properties");
			config = new PropertiesConfiguration("M9000.properties");
			
			xmlOptions = new XmlOptions();
			xmlOptions.setSaveOuter();
			xmlOptions.setSavePrettyPrint();
			xmlOptions.setUseDefaultNamespace();
			Map<String, String> prefixes = new HashMap<String, String>();
			prefixes.put("", "http://www.usi.com/health");
			xmlOptions.setSaveImplicitNamespaces(prefixes);

		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			logger.error(e.toString());
		};

	}

	public String getHealthStatusXml()
	{
		substationHlth = getHealthStatus();
		return substationHlth.xmlText(xmlOptions);
	}
	public SubStation getHealthStatus()
	{
//		Set<String> dfrs = iniConf.getSections();
		String ipAddress = null;
		int port = 0;
		List<DfrDTO> lstDfrs;
		DfrDTO dfrDTO = null;
		ExecutorService executor;
		List<Future<M9kKeyValuePair>> lstFutureResults;
		
		try
		{
			substationHlth = SubStationDocument.Factory.newInstance().addNewSubStation();
			substationHlth.setId(M9kStationDBUtil.getStationDetails().getSystemStationId());
			substationHlth.setName(M9kStationDBUtil.getStationDetails().getSystemStationName());
			substationHlth.setStatus("Active");

			lstDfrs = M9kStationXMLUtil.getListOfDFRs();
			executor = Executors.newFixedThreadPool(lstDfrs.size());
			lstFutureResults = new ArrayList<Future<M9kKeyValuePair>>(lstDfrs.size());
	
	//		for (String dfr : dfrs) {
			// START: 23-May-2018 To avoid concurrentModificationException convert arrayList to arrays and then loop
			DfrDTO[] arrDfrDtos = lstDfrs.toArray(new DfrDTO[lstDfrs.size()]);
			for (int i = 0; i < arrDfrDtos.length; i++) {
//			for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
//				dfrDTO = iterator.next();
				dfrDTO = arrDfrDtos[i];
			// END: 23-May-2018 
	//			if (!dfr.toUpperCase().startsWith("DFR"))
	//			{
	//				continue;
	//			}
	//			System.out.println("["+dfr+"]");
	//			ipAddress = iniConf.getString(dfr+".ip-address");
				ipAddress = dfrDTO.getIpAddress();
				port = M9kStationUtil.getPort();
	//			port = iniConf.getInt(dfr+".port");
	//			System.out.println("Id "+iniConf.getString(dfr+".id"));
	//			System.out.println("Ip address "+ipAddress);
	//			System.out.println("Port "+port);
	
	//			requestHealthStatus(ipAddress, port);
	//			executor.execute(new M9kMonitorThreads(ipAddress, port));
				lstFutureResults.add(executor.submit(new M9kMonitorThreads(dfrDTO.getDfrName(), ipAddress, port)));
			}
			M9kKeyValuePair dfrStatus= null;
//			substationHlth = SubStationDocument.Factory.newInstance().addNewSubStation();
//			substationHlth.setId(M9kStationDBUtil.getStationDetails().getStationId());
//			substationHlth.setName(M9kStationDBUtil.getStationDetails().getStationName());
//			substationHlth.setStatus("Active");
			StationMaster stationMaster = substationHlth.addNewStationMaster();
	
			updateLocalStationHealthStatus(stationMaster);
			
			for (Iterator<Future<M9kKeyValuePair>> iterator = lstFutureResults.iterator(); iterator
					.hasNext();) {
				Future<M9kKeyValuePair> future = iterator.next();
				try {
					dfrStatus = future.get(50, TimeUnit.SECONDS);
					DFR dfr = substationHlth.addNewDFR();
					dfr.setName(dfrStatus.getKey());
					if (!dfrStatus.getValue().toUpperCase().startsWith("ERROR"))
					{
						dfr.setStatus(M9kStationConstants.ACTIVE_STATUS);
						updateDfrStatus(dfr, dfrStatus);
					}
					else
					{
						dfr.setStatus(M9kStationConstants.INACTIVE_STATUS);
						dfr.setStatusInfo(dfrStatus.getValue());
					}
					
				} catch (InterruptedException e) {
					logger.error("Error waiting for the health status "+future,e);
				} catch (ExecutionException e) {
					logger.error("Error waiting for the health status "+future,e);
				} catch (TimeoutException e) {
					logger.error("Waited too long for the health status "+future,e);
				} catch (Exception e) {
						logger.error("Error in notification queue. Probably local JMS server is down.");
				}
				
				
			}
			executor.shutdown();
			boolean booComplete = executor.awaitTermination(1, TimeUnit.MINUTES);
			if (!booComplete)
			{
				logger.warn("DFR may be down as it takes too long to respond for the Health request.");
				List<Runnable> listUnfinishedProcess = executor.shutdownNow();
				for (Iterator<Runnable> iterator = listUnfinishedProcess.iterator(); iterator
						.hasNext();) {
					M9kCommandThreads runnable = (M9kCommandThreads) iterator.next();
					logger.error("DFR "+runnable.getIpAddress()+" cannnot be reached or process timed out");
					logger.debug("DFR pending health check "+runnable.getIpAddress());
					
				}
			}
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in getting health status ",e);
		}
		catch (Exception e) {
			logger.error("Error in getting health status",e);
		}
		if (substationHlth.getDFRArray() == null || substationHlth.getDFRArray().length == 0)
		{
			substationHlth.setStatus(M9kStationConstants.INACTIVE_STATUS);
		}
		logger.debug("Substationhealth xml constructed "+M9kStationXMLUtil.getSubstationHealthXmlText(substationHlth));
		return substationHlth;
	}
	
	// Health attributes from station master instead of all chassis
	private void updateLocalStationHealthStatus(StationMaster stationMaster) {
		// Local health information update
		String strDiskUsageInfo =  M9kStationUtil.getDiskUsageInformation();
		logger.debug("Disk usage info "+strDiskUsageInfo);
		Attribute diskUsageAttr = stationMaster.addNewAttribute();
		diskUsageAttr.setName("Disk Usage");
		diskUsageAttr.setValue(strDiskUsageInfo);

		Attribute lastCalDateAttr = stationMaster.addNewAttribute();
		lastCalDateAttr.setName("Calibrated");
		
		if (M9kStationXMLUtil.getSystem().getAnalogsCount() > 0)
		{
			Attribute lastCalVerifiedDateAttr = stationMaster.addNewAttribute();
			lastCalVerifiedDateAttr.setName("Cal Verified");
			lastCalDateAttr.setValue(M9kStationUtil.getLastCalDate());
			lastCalVerifiedDateAttr.setValue(M9kStationUtil.getLastVerifiedDate());
		}
		else
		{
			lastCalDateAttr.setValue("No Analog Channels to Calibrate");
		}
		
		// START: 12-June-2024 - Enable or Disable event test for incompatible old boards
		logger.debug("M9kStationXMLUtil.isEnableEventTest() "+M9kStationXMLUtil.isEnableEventTest());
		if (M9kStationXMLUtil.isEnableEventTest())
		{
			Attribute lastEvenTestDateAttr = stationMaster.addNewAttribute();
			lastEvenTestDateAttr.setName("Event Test ");
			if (M9kStationXMLUtil.getSystem().getDigitalsCount() > 0)
			{
				lastEvenTestDateAttr.setValue(M9kStationUtil.getLastEventTestDate());
			}
			else
			{
				lastEvenTestDateAttr.setValue("No Digital Channels to test");
			}
		}
		// END: 12-June-2024 - Enable or Disable event test for incompatible old boards
		try {
			java.net.InetAddress localHostInfo = java.net.InetAddress.getLocalHost();
			Attribute stationHostName = stationMaster.addNewAttribute();
			stationHostName.setName("Station Host Name");
			stationHostName.setValue(localHostInfo.getHostName());
			Attribute stationHostAddress;
			Map<String,String> mapIPAddresses = M9kStationUtil.getRelevantIpAddresses();
			if (!mapIPAddresses.isEmpty())
			{
				for (Iterator<String> iterator = mapIPAddresses.keySet().iterator(); iterator.hasNext();) {
					String interfaceName = iterator.next();
					stationHostAddress = stationMaster.addNewAttribute();
					stationHostAddress.setName("Station IP Address("+interfaceName+")");
					stationHostAddress.setValue(mapIPAddresses.get(interfaceName));
				}
			}
		} catch (Exception e) {
			logger.warn("Unable to get hostname/Address "+e);
		}

	}

	private void updateDfrStatus(DFR dfr, M9kKeyValuePair dfrStatus) throws IOException {
//		BufferedReader bfr = new BufferedReader(new StringReader(dfrStatus.getValue()));
		String strAttrArr[] = null;
		String healthValues[] = new String[2];
		strAttrArr = dfrStatus.getValue().split(M9kStationConstants.COMMA_SEPERATOR);
		String dfrUpTime = null;

		for (int i = 0; i < strAttrArr.length; i++) {
			healthValues = strAttrArr[i].split("=");
//			logger.debug("HEALTH_STATUS: health attributes read "+healthValues[0]);
			if (M9kStationUtil.getLstHealthAttr().contains(healthValues[0].trim()))
			{
//				logger.debug("HEALTH_STATUS: List containg the attr "+healthValues[0]);
				Attribute attr = dfr.addNewAttribute();
				attr.setName(healthValues[0].trim());
				if (healthValues[0].trim().toUpperCase().equals("DFRUPTIME"))
				{
					long upTime = Long.parseLong(healthValues[1]);
					int days = (int)(upTime/(24*3600));
					int hours = (int)(upTime/3600) - (days * 24*3600);
					int minutes = (int)(upTime/60)- (hours * 60);
					int seconds = (int)(upTime % 60);
					String calculatedUpTime = "";
//					System.out.println((int)(upTime/(24*3600))+" Days " + (int)(upTime/3600)+" Hours " +(int)(upTime/60)+" Minutes "+(int)(upTime % 60)+" Seconds");
					if (days > 0)
					{
						calculatedUpTime=days+" Days ";
					}
					else if (hours > 0)
					{
						calculatedUpTime+=hours+" Hours ";
					}
					else if (minutes > 0)
					{
						calculatedUpTime+=minutes+" Minutes ";
					}
					else
					{
						calculatedUpTime+=seconds+" Seconds ";
					}
					dfrUpTime=calculatedUpTime+ "[Total seconds = "+ upTime +"]";
					attr.setValue(dfrUpTime);
				}
//				else if (healthValues[0].trim().toUpperCase().equals("LOCKED"))
//				{
//					attr.setName("IRIG");
//					if (healthValues[1].trim().equalsIgnoreCase("1"))
//					{
//						attr.setValue("Locked");
//					}
//					else
//					{
//						attr.setValue("Unlocked");
//					}
//				}
				else
				{
					attr.setValue(healthValues[1].trim());
				}
				attr.setState("Normal");
				attr.setStatusInfo("No constraints defined");
			}
			
		}
//		// Local health information update
//		String strDiskUsageInfo =  M9kStationUtil.getDiskUsageInformation();
//		logger.debug("Disk usage info "+strDiskUsageInfo);
//		Attribute diskUsageAttr = dfr.addNewAttribute();
//		diskUsageAttr.setName("Disk Usage");
//		diskUsageAttr.setValue(strDiskUsageInfo);
		
		
	}
	public synchronized Map<String, Boolean> getDFRsStatus()
	{
//		Set<String> dfrs = iniConf.getSections();
		String ipAddress = null;
		Map<String, Boolean> mapHealthStatus = new HashMap<String, Boolean>();
		int port = 0;
		List<DfrDTO> lstDfrs;
		DfrDTO dfrDTO = null;
		try
		{
			lstDfrs = M9kStationXMLUtil.getListOfDFRs();
	//		for (String dfr : dfrs) {
			for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
				dfrDTO = iterator.next();
	//			if (!dfr.toUpperCase().startsWith("DFR"))
	//			{
	//				continue;
	//			}
	//			ipAddress = iniConf.getString(dfr+".ip-address");
	//			port = iniConf.getInt(dfr+".port");
				ipAddress = dfrDTO.getIpAddress();
				port = M9kStationUtil.getPort();
	
				mapHealthStatus.put(dfrDTO.getDfrName(), requestHealthStatus(ipAddress, port));
			}
		}
		catch (Exception e) {
			logger.error("Unable to get dfr status. ",e);
		}
		return mapHealthStatus;
	}
	
	private boolean requestHealthStatus(String destIp, int port)
	{
		String dfrStatus = null;
		boolean booStatus = false;
	     try{
	    	 commandClient = new M9kStationCommandClient(destIp, port, "PING");
	    	 dfrStatus = commandClient.sendAndReceive(); 
	    	 logger.debug("Response from the server "+dfrStatus);
//		       mapHealthStatus.put(dfrId, true);
		       booStatus = true;
		     
	     } 
	     catch(M9000Exception me)
	     {
//	    	 mapHealthStatus.put(dfrId, false);
	    	 booStatus = false;
	     } 
	     return booStatus;
	}
	
	
}
