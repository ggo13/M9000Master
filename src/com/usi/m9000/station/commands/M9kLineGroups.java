package com.usi.m9000.station.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;

public class M9kLineGroups {
	M9kStationCommandClient commandClient;
	Map<String, String> mapDfrsLineGroups;	
//	static HierarchicalINIConfiguration iniConf;
	private final static String command = "LINEGROUPS";

	public M9kLineGroups() {
//		try {
//			iniConf = new HierarchicalINIConfiguration("station.properties");
			mapDfrsLineGroups = new HashMap<String, String>();
//		} catch (ConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	public void getLineGroupsInfo() throws M9000Exception
	{
//		Set<String> dfrs = iniConf.getSections();
		String ipAddress = null;
		int port = 0;
		List<DfrDTO> lstDfrs;
		DfrDTO dfrDTO = null;
		String lineGroupInfo;
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
	//			System.out.println("[" + dfr + "]");
	//			ipAddress = iniConf.getString(dfr + ".ip-address");
	//			port = iniConf.getInt(dfr + ".port");
	//			System.out.println("Id " + iniConf.getString(dfr + ".id"));
	//			System.out.println("Ip address " + ipAddress);
	//			System.out.println("Port " + port);
				ipAddress = dfrDTO.getIpAddress();
				port = M9kStationUtil.getPort();
	
				lineGroupInfo = send(ipAddress, port, command);
				mapDfrsLineGroups.put(dfrDTO.getDfrName(), lineGroupInfo);
	
			}
			processLineGroupsInfo();
		}
		catch (Exception e) {
			throw new M9000Exception("Error in getting linegroup info",e);
		}

	}

	private String send(String destIp, int port, String command) throws M9000Exception {
		
		String result;
		commandClient = new M9kStationCommandClient(destIp, port, command);
		result = commandClient.sendAndReceive();
		
		return result;
	}

	private void processLineGroupsInfo()
	{
		String dfr;
		for (Iterator<String> iterator = mapDfrsLineGroups.keySet().iterator(); iterator.hasNext();) {
			dfr = iterator.next();
//			System.out.println("DFR Name: "+dfr);
			printLineGroupsInfo(mapDfrsLineGroups.get(dfr));
			
		}		
	}	
	
	private void printLineGroupsInfo(String lineGroupsInfo)
	{
		StringTokenizer strTok = new StringTokenizer(lineGroupsInfo, ",");
		while (strTok.hasMoreElements()) {
			String dfrParameter = (String) strTok.nextElement();
//			System.out.println(dfrParameter);
			
		}
	}

}
