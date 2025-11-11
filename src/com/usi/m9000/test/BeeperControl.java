package com.usi.m9000.test;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
class BeeperControl {
	private static int count = 1;
	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
	    private final ScheduledExecutorService scheduler = 
	       Executors.newScheduledThreadPool(1);

	    public void beepForAnHour() {
	        final Runnable beeper = new Runnable() {
	                public void run() { System.out.println(sdf.format(new Date())+"\tbeep"+count++); if (count == 3)
						try {
							System.out.println("About to sleep "+count);
							Thread.sleep(5000);
							System.out.println("Finished sleep "+count);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}}
	            };
	        final ScheduledFuture<?> beeperHandle = 
	            scheduler.scheduleWithFixedDelay(beeper, 10, 10, SECONDS);
	        scheduler.schedule(new Runnable() {
	                public void run() { beeperHandle.cancel(true); 
	                scheduler.shutdown();
	                //System.exit(1);
	                }
	            }, 60 * 1, SECONDS);
	    }
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BeeperControl bc = new BeeperControl();
		bc.beepForAnHour();

	}

}
