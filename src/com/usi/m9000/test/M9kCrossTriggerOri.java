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
import java.util.concurrent.Executors;

import com.usi.m9000.station.commands.M9kCommandThreads;

public class M9kCrossTriggerOri {
	String triggerRequest = "";
	Socket socket = null;
	PrintWriter out = null;
	BufferedReader in = null;
	ExecutorService executor = Executors.newFixedThreadPool(10);
	long start;
	long end;
	/**
	 * 
	 */
	public M9kCrossTriggerOri() {
		// TODO Auto-generated constructor stub
	}


	/**
	 * 
	 */
	public M9kCrossTriggerOri(String triggerRequest) {
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
		M9kCrossTriggerOri m9kCrossTriggerOri = new M9kCrossTriggerOri("crossrecord,user=195.1.1.125,future=5,signature=");
		m9kCrossTriggerOri.start();
		for (int i = 0; i < 10; i++) {
//			m9kCrossTriggerOri.sendTrigger("195.1.1.72", 9978);
			m9kCrossTriggerOri.executor.execute(new M9kCommandThreads("195.1.1.72", 9978, (m9kCrossTriggerOri.getTriggerRequest()+ (i+1))));
		}
		m9kCrossTriggerOri.executor.shutdown();
		m9kCrossTriggerOri.end();
		System.out.println("\n\n\t\tTotal time taken to process "+m9kCrossTriggerOri.getTotalTimeTaken());
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
}
