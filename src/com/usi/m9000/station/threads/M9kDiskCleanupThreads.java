package com.usi.m9000.station.threads;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.util.M9kKeyValuePair;
import com.usi.m9000.station.util.M9kStationUtil;

public class M9kDiskCleanupThreads implements Callable<M9kKeyValuePair>
{
	String dfrName;
	String ipAddress;
	int port;
	M9kStationCommandClient commandClient;
	static Logger logger = Logger.getLogger(M9kDiskCleanupThreads.class);
	private final static String command = "MONITOR";
	
	public M9kDiskCleanupThreads(String dfrName, String ipAddress, int port) {
		super();
		this.dfrName = dfrName;
		this.ipAddress = ipAddress;
		this.port = port;
	}

	@Override
	public M9kKeyValuePair call() {
		return requestHealthStatus(dfrName, ipAddress, port);
		
	}

	private M9kKeyValuePair requestHealthStatus(String dfrName, String destIp, int port)
	{
		M9kKeyValuePair dfrStatus;
	     try{
	    	 commandClient = new M9kStationCommandClient(destIp, port, command);
	    	 dfrStatus = new M9kKeyValuePair(dfrName, commandClient.sendAndReceive()); 
	    	 if (M9kStationUtil.getMapDfrMonitorStatus().get(destIp) != null && !M9kStationUtil.getMapDfrMonitorStatus().get(destIp))
	    	 {
	    		 logger.error("INFO: "+dfrName+" - "+dfrName+"( "+destIp +" ) is back up again.");
		    	 M9kStationUtil.getMapDfrMonitorStatus().put(destIp, true);
	    	 }
	    	 logger.debug("Response from the server "+dfrStatus);
	     } 
	     catch(M9000Exception me)
	     {
	    	 if (M9kStationUtil.getMapDfrMonitorStatus().get(destIp) == null || M9kStationUtil.getMapDfrMonitorStatus().get(destIp))
	    	 {
	    		 logger.error("ERROR: "+dfrName+" - "+dfrName+"( "+destIp +" ) is not reachable.",me);
	    	 }
	    	 dfrStatus = new M9kKeyValuePair(dfrName, "ERROR: "+dfrName +" is not reachable.");
	    	 M9kStationUtil.getMapDfrMonitorStatus().put(destIp, false);
	     } 
		 return dfrStatus;    
	}

}

