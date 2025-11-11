package com.usi.m9000.station.threads;

import java.util.concurrent.Callable;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.util.M9kKeyValuePair;

public class M9kCalibrationThreads implements Callable<M9kKeyValuePair>
{
	String dfrName;
	String ipAddress;
	int port;
	M9kStationCommandClient commandClient;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kCalibrationThreads.class);
	private  String commandToSend;

	
	public M9kCalibrationThreads(String dfrName, String ipAddress, int port, String commandToSend) {
		super();
		this.dfrName = dfrName;
		this.ipAddress = ipAddress;
		this.port = port;
		this.commandToSend = commandToSend;
	}

	@Override
	public M9kKeyValuePair call() {
		return calibrateAndReport(dfrName, ipAddress, port);
		
	}

	private M9kKeyValuePair calibrateAndReport(String dfrName, String destIp, int port)
	{
		M9kKeyValuePair dfrStatus;
	     try{
	    	 commandClient = new M9kStationCommandClient(destIp, port, commandToSend);
	    	 dfrStatus = new M9kKeyValuePair(dfrName, commandClient.sendAndReceive()); 
	    	 logger.debug("Response from the server "+dfrStatus);
	     } 
	     catch(M9000Exception me)
	     {
	    	 logger.error("ERROR: "+dfrName+" - "+dfrName+"( "+destIp +" ) is not reachable.",me);
	    	 dfrStatus = new M9kKeyValuePair(dfrName, "ERROR: "+dfrName +" is not reachable.");
	    	 
	     } 
		 return dfrStatus;    
	}

	public String getDfrName() {
		return dfrName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public int getPort() {
		return port;
	}

}

