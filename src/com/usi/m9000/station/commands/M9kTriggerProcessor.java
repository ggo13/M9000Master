package com.usi.m9000.station.commands;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.station.M9kLED;
import com.usi.m9000.station.Exception.M9kThreadExceptionHandler;
import com.usi.m9000.station.dto.ProcessDatDTO;
import com.usi.m9000.station.dto.TriggersScheduleDTO;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;


public class M9kTriggerProcessor {
	String triggerRequest = "";
	String sourceDfr;
	private long startTime = -1L;
	private long endTime = -1L;
//	static String hostName;
	
	M9kStationCommandClient commandClient;
	Future<?> crossRecoredTask = null;
	ExecutorService threadHandlerExecutor;
	private M9kDFRHealthStatusProcessor m9kDFRHealthStatusProcessor;
//	static HierarchicalINIConfiguration iniConf;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kTriggerProcessor.class);

	static 
	{
//		try {
//			iniConf = new HierarchicalINIConfiguration("station.properties");
////			java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
////			hostName = localMachine.getHostName();
//		} catch (ConfigurationException e) {
//			logger.error("Problem in reading configuration file: station.properties ", e);
//		}

	}
	public M9kTriggerProcessor() {
		super();
		logger.debug("Entered M9kTriggerProcessor ");
		threadHandlerExecutor =  Executors.newCachedThreadPool();
		m9kDFRHealthStatusProcessor = new M9kDFRHealthStatusProcessor();
	}

	
	public M9kTriggerProcessor(String sourceDfr, long startTime, long endTime) {
		this();
		this.sourceDfr = sourceDfr;
		this.startTime = startTime;
		this.endTime = endTime;
	}


	/**
	 * 
	 */
//	public M9kTriggerHandler(String sourceDfr, long startTime, long endTime) {
//		this.triggerRequest = triggerRequest;
//    	try {
//			iniConf = new HierarchicalINIConfiguration("station.properties");
//		} catch (ConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

//	public static void main(String args[])
//	{
//		java.text.SimpleDateFormat simpleDtFormat = new java.text.SimpleDateFormat("MM/dd/yyyy - HH:mm:ss.SS");
//		long startTime = (System.currentTimeMillis()-18000000)*1000 ;
//		long endTime =  (System.currentTimeMillis()+100-18000000)*1000;
//		
//		System.out.println("Start time in UNIX "+ startTime+" and in human readable form "+simpleDtFormat.format(startTime/1000));
//		System.out.println("End time in UNIX "+ endTime+" and in human readable form "+simpleDtFormat.format(endTime/1000));
//		
//		M9kTriggerHandler m9kCrossTrigger = new M9kTriggerHandler();
//		try {
//			m9kCrossTrigger.send("195.1.1.72", 9977, m9kCrossTrigger.getTriggerNowCommand("195.1.1.72"));
//		} catch (M9000Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	

	@SuppressWarnings("unused")
	private void send(String destIp, int port, String command) throws M9000Exception {
		
		commandClient = new M9kStationCommandClient(destIp, port, command);
		commandClient.sendAndReceive();
	}
	

	public boolean triggerNow() throws M9000Exception {
		logger.debug("TRIGGER-NOW: Entered TriggerNow");
		M9kStationCommandClient commandClient;
		boolean booResult = false;
//		Set<String> dfrs = iniConf.getSections();
		String ipAddress = null;
		DfrDTO dfrDTO = null;
		int port = 0;
		List<DfrDTO> lstDfrs;
//		ExecutorService executor;
		
		try
		{
			lstDfrs = M9kStationXMLUtil.getListOfDFRs();
//			executor = Executors.newFixedThreadPool(1);
			port = M9kStationUtil.getPort();
			Map<String, Boolean> mapDfrsStatus = m9kDFRHealthStatusProcessor.getDFRsStatus();
	//		for (String dfr : dfrs) {
			for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
				dfrDTO = iterator.next();
				
	//			if (!dfr.toUpperCase().startsWith("DFR"))
	//			{
	//				continue;
	//			}
	//			logger.debug("[" + dfr + "]");
	//			ipAddress = iniConf.getString(dfr + ".ip-address");
				ipAddress = dfrDTO.getIpAddress();
	//			port = iniConf.getInt(dfr + ".port");
	//			logger.debug("Id " + iniConf.getString(dfr + ".id"));
				logger.debug("Ip address " + ipAddress);
				logger.debug("Port " + port);
				try 
				{
					if (mapDfrsStatus.get(dfrDTO.getDfrName()))
					{
//						executor.execute(new M9kCommandThreads(ipAddress, port, getTriggerNowCommand(ipAddress)));
						commandClient = new M9kStationCommandClient(ipAddress, port, getTriggerNowCommand(ipAddress));
						M9kLED.setTestTrigger(true);
						String result = commandClient.sendAndReceive();
						logger.debug("Trigger now request output "+result);
						booResult = true;
						break;
					}
				}
				catch (Exception e) {
					logger.warn("failed to trigger dfr "+ipAddress+" port: "+port, e);
					booResult = false;
//					throw e;
				}
	//			try {
	//				send(ipAddress, port, getTriggerNowCommand(ipAddress));
	//			} catch (Exception e) {
	//				// TODO Auto-generated catch block
	//				logger.error("failed to trigger dfr "+ipAddress+" port: "+port, e);
	//			}
	
			}
//		logger.debug("Executor shutdown");
//		executor.shutdown();
//		logger.debug("After executor shutdown..About to wait");
//		boolean booComplete = executor.awaitTermination(1, TimeUnit.MINUTES);
//		logger.debug("After await termination flag status "+booComplete);
//		if (!booComplete)
//		{
//			List<Runnable> listUnfinishedProcess = executor.shutdownNow();
//			for (Iterator<Runnable> iterator = listUnfinishedProcess.iterator(); iterator
//					.hasNext();) {
//				M9kCommandThreads runnable = (M9kCommandThreads) iterator.next();
//				logger.error("DFR "+runnable.getIpAddress()+" cannot be triggered or process timed out");
//				System.out.println("DFR pending trigger "+runnable.getIpAddress());
//				
//			}
//		}
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
////			e.printStackTrace();
//			logger.error("Error in trigger ",e);
//		}
		} catch (Exception e) {
			booResult = false;
			throw new M9000Exception("Failed to trigger now",e);
		}
		return booResult;
	}
	
	//TODO: Cross Trigger Implementation
	public void crossTrigger(List<ProcessDatDTO> lstDfrsToTrigger) throws M9000Exception {
		ExecutorService executor = Executors.newFixedThreadPool(lstDfrsToTrigger.size());

		for (Iterator<ProcessDatDTO> iterator = lstDfrsToTrigger.iterator(); iterator
				.hasNext();) {
			ProcessDatDTO processDatDTO = iterator.next();
			try
			{
				executor.execute(new M9kCommandThreads(processDatDTO.getIpAddress(), processDatDTO.getPort(), getCrossTriggerCommand(processDatDTO)));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("failed to trigger dfr "+processDatDTO.getIpAddress()+" port: "+processDatDTO.getPort(), e);
			}

		}
		logger.debug("Executor shutdown for cross Triggers");
		executor.shutdown();

		try {
			logger.debug("After executor shutdown..About to wait");
			boolean booComplete = executor.awaitTermination(1, TimeUnit.MINUTES);
			logger.debug("After await termination flag status "+booComplete);
			if (!booComplete)
			{
				List<Runnable> listUnfinishedProcess = executor.shutdownNow();
				for (Iterator<Runnable> iterator = listUnfinishedProcess.iterator(); iterator
						.hasNext();) {
					M9kCommandThreads runnable = (M9kCommandThreads) iterator.next();
					logger.error("DFR "+runnable.getIpAddress()+" cannot be triggered or process timed out");
					logger.debug("DFR pending trigger "+runnable.getIpAddress());
					
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			logger.error("Error in trigger ",e);
		}

	}

	public void crossTrigger(ProcessDatDTO dfrToTrigger) throws M9000Exception {
		ExecutorService executor = Executors.newFixedThreadPool(1);

			try
			{
				executor.execute(new M9kCommandThreads(dfrToTrigger.getIpAddress(), dfrToTrigger.getPort(), getCrossTriggerCommand(dfrToTrigger)));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("failed to trigger dfr "+dfrToTrigger.getIpAddress()+" port: "+dfrToTrigger.getPort(), e);
			}
		logger.debug("Executor shutdown for cross Triggers");
		executor.shutdown();

		try {
			logger.debug("After executor shutdown..About to wait");
			boolean booComplete = executor.awaitTermination(1, TimeUnit.MINUTES);
			logger.debug("After await termination flag status "+booComplete);
			if (!booComplete)
			{
				List<Runnable> listUnfinishedProcess = executor.shutdownNow();
				for (Iterator<Runnable> iterator = listUnfinishedProcess.iterator(); iterator
						.hasNext();) {
					M9kCommandThreads runnable = (M9kCommandThreads) iterator.next();
					logger.error("DFR "+runnable.getIpAddress()+" cannot be triggered or process timed out");
					logger.debug("DFR pending trigger "+runnable.getIpAddress());
					
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			logger.error("Error in trigger ",e);
		}

	}

	public void crossTriggerAll(TriggersScheduleDTO triggerSchedule) throws M9000Exception {
//		Set<String> dfrs = iniConf.getSections();
		String ipAddress = null;
		int port = 0;
		List<DfrDTO> lstDfrs;
		DfrDTO dfrDTO = null;
		ExecutorService executor;
		String crossRecordCmd = getCrossTriggerCommandForAll(triggerSchedule);
		Map<String, Boolean> mapDfrsStatus = m9kDFRHealthStatusProcessor.getDFRsStatus();
		logger.debug(" mapDfrsStatus "+mapDfrsStatus);
		try
		{
			lstDfrs = M9kStationXMLUtil.getListOfDFRs();
			executor = Executors.newFixedThreadPool(lstDfrs.size());
//			for (String dfr : dfrs) {
			for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
				dfrDTO = iterator.next();
//				if (!dfr.toUpperCase().startsWith("DFR") || !mapDfrsStatus.get(dfr))
//				{
//					continue;
//				}
//				logger.debug("[" + dfr + "]");
//				ipAddress = iniConf.getString(dfr + ".ip-address");
				if (!mapDfrsStatus.get(dfrDTO.getDfrName()))
				{
					logger.error(dfrDTO.getDfrName()+" is down. Cross record request is not sent.");
					continue;
				}
				ipAddress = dfrDTO.getIpAddress();
//				port = iniConf.getInt(dfr + ".port");
				port = M9kStationUtil.getPort();
//				logger.debug("Id " + iniConf.getString(dfr + ".id"));
//				logger.debug("Ip address " + ipAddress);
//				logger.debug("Port " + port);
				crossRecoredTask = executor.submit(new M9kCommandThreads(ipAddress, port, crossRecordCmd));
				threadHandlerExecutor.execute(new M9kThreadExceptionHandler(crossRecoredTask));
			}
			executor.shutdown();
		}
		catch (Exception e) {
			throw new M9000Exception("Cross trigger of all dfrs failed.",e);
		}

		try {
			logger.debug("After executor shutdown..About to wait");
			boolean booComplete = executor.awaitTermination(10, TimeUnit.SECONDS);
			logger.debug("After await termination flag status "+booComplete);
			if (!booComplete)
			{
				logger.warn("DFR may be down as it takes too long to respond for the cross record request.");
				List<Runnable> listUnfinishedProcess = executor.shutdownNow();
				for (Iterator<Runnable> iterator = listUnfinishedProcess.iterator(); iterator
						.hasNext();) {
					M9kCommandThreads runnable = (M9kCommandThreads) iterator.next();
					logger.error("DFR "+runnable.getIpAddress()+" cannot be triggered or process timed out");
					logger.debug("DFR pending trigger "+runnable.getIpAddress());
					
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			logger.error("Error in cross trigger ",e);
		}
		catch (Exception e) {
			logger.error("Error in cross trigger ",e);
		}

	}

	private String getCrossTriggerCommand(ProcessDatDTO processDatDTO)
	{
		StringBuffer commandBuffer = new StringBuffer();	
		commandBuffer.append("crossrecord,host="+M9kStationXMLUtil.getSubstationHostName()+",start=");
		commandBuffer.append(processDatDTO.getStartTime());
		commandBuffer.append(",stop=");
		commandBuffer.append(processDatDTO.getEndTime());
		return commandBuffer.toString();
	}

	private String getCrossTriggerCommandForAll(TriggersScheduleDTO triggerSchedule)
	{
		StringBuffer commandBuffer = new StringBuffer();	
		commandBuffer.append("crossrecord,host="+M9kStationXMLUtil.getSubstationHostName()+",start=");
		commandBuffer.append(triggerSchedule.getStartTime());
		commandBuffer.append(",stop=");
		commandBuffer.append(triggerSchedule.getStopTime());
		commandBuffer.append(",signature=");
		commandBuffer.append(triggerSchedule.getSignature());
		logger.debug("Command to be executed for cross record "+commandBuffer);
		return commandBuffer.toString();
		
	}
	private String getTriggerNowCommand(String host)
	{
		String command;
//		java.text.SimpleDateFormat simpleDtFormat = new java.text.SimpleDateFormat("MM/dd/yyyy - HH:mm:ss.SS");
		if (startTime == -1)
		{
//			startTime = (System.currentTimeMillis()-18000000)*1000 ;
			startTime = (System.currentTimeMillis())*1000 ;
		}
		if (endTime == -1)
		{
//			endTime =  (System.currentTimeMillis()+100-18000000)*1000;
			endTime =  (System.currentTimeMillis()+100)*1000;
		}
		
//		logger.debug("System.currentTimeMillis() in station "+System.currentTimeMillis());
//		logger.debug("Start time in UNIX "+ startTime+" and in human readable form "+simpleDtFormat.format(startTime/1000));
//		logger.debug("End time in UNIX "+ endTime+" and in human readable form "+simpleDtFormat.format(endTime/1000));
		logger.debug("Triggering host: "+host);
		//TODO: Discrepancy in the EPOCH time. Hence sending trigger request without time.
//		command = "crossrecord,host="+host+",start="+startTime+",stop="+endTime;
		command = "crossrecord,host="+M9kStationXMLUtil.getSubstationHostName()+",mode=Triggers";
		return command;
	}
	/**
	 * @return the triggerRequest
	 */
	public String getTriggerRequest() {
		return triggerRequest;
	}


	/**
	 * @param triggerRequest the triggerRequest to set
	 */
	public void setTriggerRequest(String triggerRequest) {
		this.triggerRequest = triggerRequest;
	}

	public String getSourceDfr() {
		return sourceDfr;
	}

	public void setSourceDfr(String sourceDfr) {
		this.sourceDfr = sourceDfr;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
}
