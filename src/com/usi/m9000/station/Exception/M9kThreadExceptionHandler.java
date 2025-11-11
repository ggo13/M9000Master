/**
 * 
 */
package com.usi.m9000.station.Exception;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author sramasamy
 *
 */
public class M9kThreadExceptionHandler implements Runnable {
	
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kThreadExceptionHandler.class);
	private Future<?> schedulerHandle = null;
	/**
	 * 
	 */
	public M9kThreadExceptionHandler(Future<?> schedulerHandle) {
		this.schedulerHandle = schedulerHandle;
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			logger.debug("Future object's get() returns "+schedulerHandle.get());
			} catch (ExecutionException e) {
			  logger.error("Exception occured in one of the thread during fault processing ",e);
			} catch (InterruptedException e) {
				logger.error("Exception occured in one of the thread during fault processing ",e);
			}
			catch (CancellationException e) {
				logger.error("Watcher thread has been cancelled", e);
	        }
			catch (Exception e) {
				logger.error("Exception occured in one of the thread during fault processing ",e);
			}
	}

}
