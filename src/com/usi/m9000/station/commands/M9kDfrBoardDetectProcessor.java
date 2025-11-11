package com.usi.m9000.station.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;

public class M9kDfrBoardDetectProcessor {
	M9kStationCommandClient commandClient;
	Map<String, String> mapBoards;	
//	static HierarchicalINIConfiguration iniConf;
	private final static String command = "BOARDDETECT";
	public M9kDfrBoardDetectProcessor() {
		super();
//		try {
//			iniConf = new HierarchicalINIConfiguration("station.properties");
			mapBoards = new HashMap<String, String>();
//		} catch (ConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public void detectBoards() throws M9000Exception
	{
//		Set<String> dfrs = iniConf.getSections();
		String ipAddress = null;
		List<DfrDTO> lstDfrs;
		DfrDTO dfrDTO = null;

		int port = 0;
		String boardDetails;
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
	
				boardDetails = send(ipAddress, port, command);
				mapBoards.put(dfrDTO.getDfrName(), boardDetails);
	
			}
			processBoardDetails();
		}
		catch (Exception e) {
			throw new M9000Exception("Error in getting board details",e);
		}

	}

	private String send(String destIp, int port, String command) throws M9000Exception {
		
		String result;
		commandClient = new M9kStationCommandClient(destIp, port, command);
		result = commandClient.sendAndReceive();
		
		return result;
	}

	private void processBoardDetails()
	{
		String dfr;
		for (Iterator<String> iterator = mapBoards.keySet().iterator(); iterator.hasNext();) {
			dfr = iterator.next();
//			System.out.println("DFR : "+dfr+" long value: "+mapBoards.get(dfr));
			printBoardDetect(Long.parseLong(mapBoards.get(dfr)));
			
		}		
	}
	private void printBoardDetect(long brddet)
	{
		brddet = brddet & 0xffffffffL;
		
		for (int i = 0; i < 4; i++) {
			System.out.println("Analog["+i+"]\t: "+(( brddet & (1 << (27-i))) > 0  ? "present" : "empty" ) );
			
		}
		System.out.println();
		for (int i = 0; i < 4; i++) {
			System.out.println("Event["+i+"]\t: "+(( brddet & (1 << (23-i))) > 0 ? "present" : "empty" ) );
			
		}
	}

}
