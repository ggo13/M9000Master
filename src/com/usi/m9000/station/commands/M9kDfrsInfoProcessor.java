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

public class M9kDfrsInfoProcessor {
	M9kStationCommandClient commandClient;
	Map<String, String> mapDfrs;	
//	static HierarchicalINIConfiguration iniConf;
	private final static String command = "DFRINFO";

	public M9kDfrsInfoProcessor() {
//		try {
//			iniConf = new HierarchicalINIConfiguration("station.properties");
			mapDfrs = new HashMap<String, String>();
//		} catch (ConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	public void getDfrInfo() throws M9000Exception
	{
//		Set<String> dfrs = iniConf.getSections();
		String ipAddress = null;
		List<DfrDTO> lstDfrs;
		DfrDTO dfrDTO = null;
		int port = 0;
		String dfrDetails;
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
				dfrDetails = send(ipAddress, port, command);
				mapDfrs.put(dfrDTO.getDfrName(), dfrDetails);
	
			}
			processDfrsInfo();
		}
		catch (Exception e) {
			throw new M9000Exception("Error in getDfrInfo",e);
		}

	}

	private String send(String destIp, int port, String command) throws M9000Exception {
		
		String result;
		commandClient = new M9kStationCommandClient(destIp, port, command);
		result = commandClient.sendAndReceive();
		
		return result;
	}

	private void processDfrsInfo()
	{
		String dfr;
		StringTokenizer strTok;
		for (Iterator<String> iterator = mapDfrs.keySet().iterator(); iterator.hasNext();) {
			dfr = iterator.next();
//			System.out.println("DFR Name: "+dfr);
			strTok = new StringTokenizer(mapDfrs.get(dfr), ",");
			while (strTok.hasMoreElements()) {
				String dfrParameter = (String) strTok.nextElement();
//				System.out.println(dfrParameter);
				
			}
			
		}		
	}	
	
	@SuppressWarnings("unused")
	private void printDfrsInfo(String dfrInfo)
	{
		StringTokenizer strTok = new StringTokenizer(dfrInfo, ",");
		while (strTok.hasMoreElements()) {
			String dfrParameter = (String) strTok.nextElement();
//			System.out.println(dfrParameter);
			
		}
	}

}
