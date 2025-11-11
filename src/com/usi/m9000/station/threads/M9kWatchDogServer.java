/**
 * 
 */
package com.usi.m9000.station.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.usi.m9000.station.Exception.M9kThreadExceptionHandler;
import com.usi.m9000.station.util.M9kStationUtil;

/**
 * @author sramasamy
 *
 */
public class M9kWatchDogServer {
	private static int clientsCount;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	ScheduledFuture<?> scheduledTask = null;
	private ExecutorService threadHandlerExecutor;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kWatchDogServer.class);
	
	public M9kWatchDogServer()
	{
		threadHandlerExecutor =  Executors.newCachedThreadPool();
		M9kStationUtil.enableWatchDog();
	}
	public void scheduleWatchDog()
	{
		  final Runnable taskPerformer = new Runnable() {
              public void run() {
            	  try
            	  {
            		  // Command client invocation
            		  logger.debug("About to pet the watch dog");
            		  M9kStationUtil.petWatchDog();
            		  logger.debug("Petting complete");
            	  }
            	  catch(RuntimeException re)
            	  {
            		  logger.error("Error in petting watch dog ",re);
            	  }
            	  catch(Exception e)
            	  {
            		  logger.error("Error in petting watch dog ",e);
            	  }
              }
              
          };
          scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, 0, 30, TimeUnit.SECONDS);
          threadHandlerExecutor.execute(new M9kThreadExceptionHandler(scheduledTask));
          // Add a shutdown hook to stop the thread pools gracefully when the application exits.  
          Runtime.getRuntime().addShutdownHook(new Thread("[ThreadPool-Shutdown]") {  
                
              @Override  
              public void run() {  
            	  logger.debug("Inside shutdown hook of watchdog server.");
                  if(scheduler != null && !scheduler.isShutdown()) {  
                	  scheduler.shutdownNow();  
                  }  
                    
              }  
          });  
	}
	
	public void register()
	{
		clientsCount++;
	}
	
	public void unregister()
	{
		clientsCount--;
	}

}
