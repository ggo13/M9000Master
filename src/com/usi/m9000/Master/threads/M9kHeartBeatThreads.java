package com.usi.m9000.Master.threads;

import java.util.concurrent.Callable;

import com.usi.m9000.station.util.M9kKeyValuePair;
import com.usi.m9000.util.M9kMessagesUtil;

public class M9kHeartBeatThreads implements Callable<M9kKeyValuePair>
{
	int stationId;
	String stationName;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kHeartBeatThreads.class);

	
	public M9kHeartBeatThreads(int stationId, String stationName) {
		super();
		this.stationId = stationId;
		this.stationName = stationName;
		logger.debug("station Id "+stationId+" station Name "+stationName);
	}

	@Override
	public M9kKeyValuePair call() {
		return heartBeatReport();
		
	}

	private M9kKeyValuePair heartBeatReport()
	{
		M9kKeyValuePair stationStatus;
	     try{
	    	 logger.debug("Station Id "+stationId);
	    	 long timeTolive = M9kMessagesUtil.getMqResponseWaitTime();
//	    	 M9kMessagesUtil.setMqResponseWaitTime(1000L);
	    	 String stationHealthStatus = M9kMessagesUtil.sendSynchMessage(""+stationId, "HEARTBEAT", "HEARTBEAT");
//	    	 M9kMessagesUtil.setMqResponseWaitTime(timeTolive);
	    	 stationStatus = new M9kKeyValuePair(stationId+"-"+stationName, stationHealthStatus); 
//	    	 logger.info("Response from the server "+stationStatus.getKey());
	     } 
	     catch(Exception me)
	     {
	    	 logger.error("ERROR: "+stationId+" - "+stationName+ " is not reachable.",me);
	    	 stationStatus = new M9kKeyValuePair(stationId+" - "+stationName, "ERROR: "+stationId+"-"+stationName +" is not reachable.");
	    	 
	     } 
		 return stationStatus;    
	}

	public int getStationId() {
		return stationId;
	}

	public String getStationName() {
		return stationName;
	}

}

