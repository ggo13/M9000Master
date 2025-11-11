package com.usi.m9000.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.usi.m9000.station.commands.M9kCommandThreads;

public class M9kCrossTrigger {
	String triggerRequest = "";
	Socket socket = null;
	PrintWriter out = null;
	BufferedReader in = null;
	ExecutorService executor;
	long start;
	long end;
	int noOfThreads;
	int noOfCrossRecords;
	int future;
	int length;
	/**
	 * 
	 */
	public M9kCrossTrigger() {
		// TODO Auto-generated constructor stub
		future = -1;
		length = -1;
		noOfThreads = 5;
		noOfCrossRecords=50;
	}


	/**
	 * 
	 */
	public M9kCrossTrigger(String triggerRequest) {
		this.triggerRequest = triggerRequest;
	}

	public void start()
	{
		start = System.currentTimeMillis();
	}
	
	public void end()
	{
		end = System.currentTimeMillis();
	}

	public long getTotalTimeTaken()
	{
		return (end-start);
	}
	public static void main(String args[])
	{
		int index;
		String attr;
		String val;
		System.out.println("Parameters: [threads=n1] [records=n2] [future=n3] [length=n4]");
		
		M9kCrossTrigger m9kCrossTrigger = new M9kCrossTrigger("crossrecord,user=195.1.1.125");
		if (args.length > 0)
		{
			for (int i = 0; i < args.length; i++) {
				if ((index = args[i].indexOf("=")) > -1)
				{
					attr = args[i].substring(0, index);
					val = args[i].substring(index+1);
					if (attr.equalsIgnoreCase("Threads") || attr.toUpperCase().startsWith("THR"))
					{
						System.out.println("Total number of threads "+val);
						m9kCrossTrigger.setNoOfThreads(Integer.parseInt(val));
					}
					else if (attr.equalsIgnoreCase("records") || attr.toUpperCase().indexOf("CROSS") != -1 || attr.toUpperCase().startsWith("REC"))
					{
						System.out.println("Total number of cross record request "+val);
						m9kCrossTrigger.setNoOfCrossRecords(Integer.parseInt(val));
					}
					else if (attr.equalsIgnoreCase("future") || attr.toUpperCase().startsWith("FUT"))
					{
						m9kCrossTrigger.setFuture(Integer.parseInt(val));
					}
					else if (attr.equalsIgnoreCase("length") || attr.toUpperCase().startsWith("LEN"))
					{
						m9kCrossTrigger.setLength(Integer.parseInt(val));
					}
				}
			}
		}
//		M9kCrossTrigger m9kCrossTrigger = new M9kCrossTrigger("crossrecord,user=195.1.1.125,future=5,signature=");
		m9kCrossTrigger.start();
		String params = m9kCrossTrigger.getParams();
//		m9kCrossTrigger.executor = Executors.newFixedThreadPool(m9kCrossTrigger.getNoOfThreads());
		m9kCrossTrigger.executor = new ThreadPoolExecutor(2, 2, 30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()); 
		((ThreadPoolExecutor)m9kCrossTrigger.executor).allowCoreThreadTimeOut(true);
		for (int i = 0; i < m9kCrossTrigger.getNoOfCrossRecords(); i++) {
//			m9kCrossTrigger.sendTrigger("195.1.1.72", 9978);
			
			m9kCrossTrigger.executor.execute(new M9kCommandThreads("195.1.1.72", 9978, (m9kCrossTrigger.getTriggerRequest()+params+ (i+1))));
		}
//		m9kCrossTrigger.executor.shutdown();
		m9kCrossTrigger.end();
		System.out.println("\n\n\t\tTotal time taken to process "+m9kCrossTrigger.getTotalTimeTaken());
	}
	
	public void sendTrigger(String destIp, int port){
		
		//Create socket connection
		     try{
		       socket = new Socket(destIp, port);
		       out = new PrintWriter(socket.getOutputStream(), true);
		       in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		       
		       out.println(getTriggerRequest());
		       System.out.println("Response from the server"+in.readLine());
		     } catch (UnknownHostException e) {
		       System.out.println("Unknown host:"+destIp);
		    	 e.printStackTrace();
		     } catch  (IOException e) {
		       System.out.println("No I/O");
		    	 e.printStackTrace();
		     }
		     finally
		     {
		    	 try {
		    		 if (socket != null)
		    		 {
		    			 socket.close();
		    		 }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		     }
		  }
	
	public void getDatabaseConnection()
	{
		Statement stmt = null;
	      ResultSet rs = null;
	      String startDate = "2010-10-06 7:30:20";
	      try
	      {
		      //Register the JDBC driver for MySQL.
		      Class.forName("com.mysql.jdbc.Driver");
	
		      //Define URL of database server for
		      // database named JunkDB on the localhost
		      // with the default port number 3306.
		      String url =
		            "jdbc:mysql://195.1.1.85:3306/m9000";
		      Connection con =
	            DriverManager.getConnection(
	               url,"dfr", "usi");
		    stmt = con.createStatement();
		    //Query the database, storing the result
		   // in an object of type ResultSet
		    String sql = "select * from dat_staging ";
		    System.out.println("About to execute "+sql);
		    rs = stmt.executeQuery(sql);
	      }catch (Exception e) {
			e.printStackTrace();
		}
	      finally{
	    	  if (rs != null)
	    	  {
	    		  try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		  rs = null;
	    	  }
	    	  if (stmt != null)
	    	  {
	    		  try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		  stmt = null;
	    	  }
	      }
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


	public int getNoOfThreads() {
		return noOfThreads;
	}


	public void setNoOfThreads(int noOfThreads) {
		this.noOfThreads = noOfThreads;
	}


	public int getNoOfCrossRecords() {
		return noOfCrossRecords;
	}


	public void setNoOfCrossRecords(int noOfCrossRecords) {
		this.noOfCrossRecords = noOfCrossRecords;
	}


	public int getFuture() {
		return future;
	}


	public void setFuture(int future) {
		this.future = future;
	}


	public int getLength() {
		return length;
	}


	public void setLength(int length) {
		this.length = length;
	}
	
	private String getParams()
	{
		StringBuffer strParams = new StringBuffer();
		if (getFuture() >  0)
		{
			strParams.append(",future="+getFuture());
		}
		else if (getLength() > 0)
		{
			strParams.append(",length="+getLength());
		}
		strParams.append(",signature=");
		return strParams.toString();
	}
}
