package com.usi.m9000.test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TestExecutor {
  ExecutorService executor = Executors.newFixedThreadPool(3);
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  ScheduledFuture<?> scheduledTask = null;
  private int hour;
  private SimpleDateFormat  dateFormat;
  long initDelay;
  
  public TestExecutor()
  {
	  dateFormat = new SimpleDateFormat ("MM/dd/yyyy HH:mm:ss");
  }
  
  public TestExecutor(int hour)
  {
	  super();
	  this.hour = hour;
	  Calendar cal = Calendar.getInstance(TimeZone.getDefault());
	  
//	  cal.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,hour,0, 0);
//	  cal.add(Calendar.DAY_OF_MONTH, 1);
	  cal.set(Calendar.HOUR_OF_DAY,hour);
	  cal.set(Calendar.MINUTE,0);
	  cal.set(Calendar.SECOND,0);
	  System.out.println("calendar "+cal.getTime());
//	  System.out.println("the difference "+(cal.getTimeInMillis()-new Date().getTime())/1000/60);
	  initDelay = (cal.getTimeInMillis()-new Date().getTime())/1000/60;
	  System.out.println("Init delay "+initDelay);
  }
  public void start() throws IOException {
    int i=0;
//    while (!executor.isShutdown())
    MyThread myThread = new MyThread(i++);
      executor.submit(myThread);
      try {
		Thread.sleep(5000);
		System.out.println("Invoking a shutdown");
	    shutdown();  
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }

  public void shutdown() throws InterruptedException {
	  System.out.println("Invoking a executor shutdown");
    executor.shutdown();
    System.out.println("Invoking a executor await termination ");
    executor.awaitTermination(30, TimeUnit.SECONDS);
    System.out.println("Invoking a executor shutdown Now");
    executor.shutdownNow();
    System.out.println("executor shutdown Now complete");
  }

  public static void main(String argv[]) throws Exception {
//    new TestExecutor().start();
//	  new TestExecutor().scheduleAlarmPush();
	  new TestExecutor(14).scheduleAlarmPush();
  }


public void scheduleAlarmPush()
{
	MyThread myThread = new MyThread(1);
      scheduledTask = scheduler.scheduleWithFixedDelay(myThread, initDelay, 1, TimeUnit.MINUTES);
}

class MyThread implements Runnable {
private int i;
  MyThread(int i) {
  this.i = i;
  }

  public void run() {
//	  int j = 0;
//	  while(true)
//	  {
//		  if ((j % 1000) == 0 )
//		  {
//			  System.out.println("I am in thread:"+i+ " j "+ j);
//		  }
//		  j++;
//	  }
	  System.out.println(new Date());
  }
}

}