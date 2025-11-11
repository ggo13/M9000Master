package com.usi.m9000.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.station.commands.M9kCommandThreads;
import com.usi.m9000.station.threads.M9kMonitorThreads;
import com.usi.m9000.station.util.M9kKeyValuePair;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;

public class TestMonitorTime {
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	ScheduledFuture<?> scheduledTask = null;
	public void monitorDfrs()
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

			lstDfrs = M9kStationXMLUtil.getListOfDFRs();
			executor = Executors.newFixedThreadPool(lstDfrs.size());
			lstFutureResults = new ArrayList<Future<M9kKeyValuePair>>(lstDfrs.size());
	
	//		for (String dfr : dfrs) {
			for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
				dfrDTO = iterator.next();
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
			
			for (Iterator<Future<M9kKeyValuePair>> iterator = lstFutureResults.iterator(); iterator
					.hasNext();) {
				Future<M9kKeyValuePair> future = iterator.next();
				try {
					dfrStatus = future.get(30, TimeUnit.SECONDS);
					System.out.println("DFr Status "+dfrStatus);
				} catch (InterruptedException e) {
					System.out.println("Error waiting for the health status "+future+e);
				} catch (ExecutionException e) {
					System.out.println("Error waiting for the health status "+future+e);
				} catch (TimeoutException e) {
					System.out.println("Waited too long for the health status "+future+e);
				} catch (Exception e) {
						System.out.println("Error in notification queue. Probably local JMS server is down.");
				}
				
				
			}
			executor.shutdown();
			boolean booComplete = executor.awaitTermination(1, TimeUnit.MINUTES);
			if (!booComplete)
			{
				System.out.println("DFR may be down as it takes too long to respond for the Health request.");
				List<Runnable> listUnfinishedProcess = executor.shutdownNow();
				for (Iterator<Runnable> iterator = listUnfinishedProcess.iterator(); iterator
						.hasNext();) {
					M9kCommandThreads runnable = (M9kCommandThreads) iterator.next();
					System.out.println("DFR "+runnable.getIpAddress()+" cannnot be reached or process timed out");
					System.out.println("DFR pending health check "+runnable.getIpAddress());
					
				}
			}
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error in getting health status "+e);
		}
		catch (Exception e) {
			System.out.println("Error in getting health status"+e);
		}
	
	}
	
	public void scheduleMonitorService()
	{
		  final Runnable taskPerformer = new Runnable() {
              public void run() {
            	  monitorDfrs();
              }
          };
          scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, 0, 10, TimeUnit.SECONDS);

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestMonitorTime test = new TestMonitorTime();
		test.scheduleMonitorService();

	}

}
