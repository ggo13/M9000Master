package com.usi.m9000.station.net;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.station.commands.M9kCommandThreads;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationUtil;

public class M9kStationCommandClient {

	private String ipAddress;
	private int port;
	private String command;
	private Socket socket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	Lock lock;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kStationCommandClient.class);
	private long start;
	private long end;
	public M9kStationCommandClient() {
		super();
		lock = new ReentrantLock();
	}

	public M9kStationCommandClient(String ipAddress, int port, String command) {
		this();
		this.ipAddress = ipAddress.trim();
		this.port = port;
		this.command = command;
//		System.out.println("no of time called "+count++);
	}
	public M9kStationCommandClient(String ipAddress, int port) {
		this();
		this.ipAddress = ipAddress.trim();
		this.port = port;
//		System.out.println("no of time called "+count++);
	}
	
	public void validate() throws M9000Exception
	{
		try {
			logger.debug("Ip address " + getIpAddress());
			logger.debug("Port " + getPort());
//			System.out.println("Command "+getCommand());
			socket = new Socket();
			 socket.connect(new InetSocketAddress(getIpAddress().trim(), getPort()),5000);
			 socket.setSoTimeout(10000);
		}
		catch (SocketTimeoutException e) {
	    	 logger.error("Socket timeout exception ", e);
	    	 throw new M9000Exception(e);
	     }
		catch (UnknownHostException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			logger.error("Host Name Unknown ", e);
			throw new M9000Exception(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Host Name Unknown ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
	    	 logger.error("Socket exception ", e);
	    	 throw new M9000Exception(e);
	     }
		finally
		{
			if (socket != null)
			{
				try {
					socket.close();
				} catch (IOException e) {
					logger.warn("Unable to close the Socket", e);
				}
				socket = null;
			}
		}
	}
	public String sendAndReceive() throws M9000Exception
	{
//		long start;
//		long end;
//		System.out.println("Ip address " + getIpAddress());
//		System.out.println("Port " + getPort());
//		System.out.println("Command "+getCommand());
//		InetSocketAddress sockaddr = null;
		if (getIpAddress() == null || getPort() == 0 || getCommand() == null)
		{
			throw new M9000Exception("Required parameters are null");
		}
		String result = "";
		StringBuffer output = new StringBuffer();
	     try{
	 		lock.lock();
	    	 logger.debug("ip adress to be sent to.. "+getIpAddress() + " port "+getPort()+" Command: "+getCommand());
//	    	 System.out.println("ip adress to be sent to.. "+getIpAddress() + " port "+getPort()+" Command: "+getCommand());
//	    	 if (socket == null)
//	    	 {
//	    		 sockaddr = new InetSocketAddress(getIpAddress().trim(), getPort());
	    		 socket = new Socket();
//	    		 System.out.println("get tcp no dleay "+socket.getTcpNoDelay());
	    	 if(getCommand().toUpperCase().indexOf("CALIBRATE") != -1)
		       {
	    		 logger.debug("Set "+M9kStationUtil.getCalibrateSocketReadTimeout() +"to socketSOTimeOut for calibration");
//		    	   socket.setSoTimeout(180000);
	    		 socket.setSoTimeout(M9kStationUtil.getCalibrateSocketReadTimeout());
		       }
		       else
		       {
		    	   logger.debug("Set"+ M9kStationUtil.getSocketReadTimeout() +"to socketSOTimeOut for every command except calibration");
//		    	   socket.setSoTimeout(10000);
		    	   socket.setSoTimeout(M9kStationUtil.getSocketReadTimeout());
		       }
	    	 socket.connect(new InetSocketAddress(getIpAddress().trim(), getPort()),M9kStationUtil.getSocketConnectionTimeout());
//	    	 }
//	       System.out.println("After socket creation..."+socket);
//	       socket = new Socket();
//	       start = System.currentTimeMillis();
//	       logger.debug("About to connect the socket "+start+" to host "+getIpAddress().trim());
//	       System.out.println("About to connect the socket to host "+getIpAddress().trim());
////	       socket.connect(sockaddr, 30000);
//	       end = System.currentTimeMillis();
//	       logger.debug("Socket connected to "+getIpAddress()+" Time Taken "+(end-start));
//	       System.out.println("Socket connected to "+getIpAddress()+" Time Taken "+(end-start));
//	       socket.setSoTimeout(10000);
	       out = new PrintWriter(socket.getOutputStream(), true);
//	       logger.debug("After Print writer..."+getIpAddress().trim());
//	       System.out.println("Command to be sent "+getCommand());
	       start = System.currentTimeMillis();
	       out.println(getCommand());
	       socket.shutdownOutput();
//	       System.out.println("After out println");
	       in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//	       logger.debug("Input stream created" + getIpAddress().trim());
//	       System.out.println(getIpAddress()+" is reachable? "+InetAddress.getByName(getIpAddress()).isReachable(10000));
	       while((result = in.readLine()) != null)
	       {
//	    	   logger.debug("Inside while "+result+" from host "+getIpAddress().trim());
//	    	   System.out.println("No of times read...");
	    	   if (output.length() > 0)
	    	   {
	    		   output.append(M9kStationConstants.NEWLINE);
	    	   }
	    	   output.append(result);
	    	   result = null;
	       }
	       end = System.currentTimeMillis();
//	       logger.debug("\n@@@ Time taken for "+getIpAddress().trim()+" is "+(end-start));
	       if ( (getIpAddress() != null && !getIpAddress().equalsIgnoreCase(M9kStationUtil.getPowerSupply())) && (end-start) > M9kStationUtil.getSocketReadWarnTime())
	       {
	    	   logger.warn("Socket Read for the command" +getCommand()+" from IpAddress "+getIpAddress()+" took more than "+ (end-start) +"millisecond(s)");
	       }
//	       System.out.println("output is "+output);
	       logger.debug("Ouput from the command sent from host "+getIpAddress().trim()+" is "+output);
//	       System.out.println("Response from the server"+output);
	     } catch (UnknownHostException e) {
//	    	 e.printStackTrace();
//	    	 logger.error("Unknown host:"+getIpAddress());
	    	 throw new M9000Exception("Host Name not known "+getIpAddress(), e);
	     } catch (SocketTimeoutException e) {
//	    	 e.printStackTrace();
	    	 if (getIpAddress() != null && !getIpAddress().equalsIgnoreCase(M9kStationUtil.getPowerSupply()))
	    	 {
//		    	 logger.error("Time out exception from "+getIpAddress()+" for command "+getCommand(), e);
		    	 throw new M9000Exception("Time out exception from "+getIpAddress()+" for command "+getCommand(),e);
	    	 }
	    	 else
	    	 {
	    		 logger.info("Power supply Warbling doesn't acknowlege by a return value. Hence we timeout. We can safely ignore this.");
	    	 }
	     }
	     catch  (IOException e) {
//	    	 e.printStackTrace();
//	    	 logger.error("IO exception connecting to "+getIpAddress(), e);
	    	 throw new M9000Exception(e);
	     }
	     catch (Exception e)
	     {
//	    	 e.printStackTrace();
//	    	 logger.error("Socket exception connecting to  "+getIpAddress(), e);
	    	 throw new M9000Exception(e);
	     }
	     finally
	     {
	 		lock.unlock();

	    	 try {
	    		 if (in != null)
	    		 {
	    			 in.close();
	    		 }
	    		 if (socket != null)
	    		 {
//	    			 logger.debug("About to close the socket for host..."+getIpAddress().trim());
	    			 socket.close();
	    			 socket = null;
	    		 }
			} catch (IOException e) {
				logger.warn("Unable to close the Socket", e);
				}
	     }
		
		return output.toString();
	}

	public byte[] sendAndReceiveBinary() throws M9000Exception
	{
		byte[] scopeData = null;
//		System.out.println("Ip address " + getIpAddress());
//		System.out.println("Port " + getPort());
//		System.out.println("Command "+getCommand());

		if (getIpAddress() == null || getPort() == 0 || getCommand() == null)
		{
			throw new M9000Exception("Required parameters are null");
		}
		InputStream is = null;
		ByteArrayOutputStream buffer = null;
	     try{
	    	 logger.debug("ip adress to be sent to.. "+getIpAddress() + " port "+getPort()+" command "+getCommand());
	       socket = new Socket();
	       socket.connect(new InetSocketAddress(getIpAddress().trim(), getPort()),5000);
//	       socket.setSoTimeout(10000);
	       socket.setSoTimeout(M9kStationUtil.getSocketReadTimeout());
	       
//	       System.out.println("After socket creation..."+socket);
//	    	 System.out.println("Socket "+socket.isClosed());
	       out = new PrintWriter(socket.getOutputStream(), true);
//	       System.out.println("After Print writer...");
	       is = socket.getInputStream();
//	       System.out.println("Command to be sent "+getCommand());
	       out.println(getCommand());
	       
	       buffer = new ByteArrayOutputStream();

	       int nRead;
	       byte[] data = new byte[16384];

	       while ((nRead = is.read(data, 0, data.length)) != -1) {
	         buffer.write(data, 0, nRead);
	       }

	       buffer.flush();

	       scopeData = buffer.toByteArray();
	       logger.debug("Length of scope data to be returned "+scopeData.length);
	     }
	     catch (SocketTimeoutException e) {
//	    	 logger.error("Socket timeout exception ", e);
	    	 throw new M9000Exception(e);
	     }
	     catch (UnknownHostException e) {
//	       System.out.println("Unknown host:"+getIpAddress());
	    	 throw new M9000Exception("Host Name not known ", e);
	     } catch  (IOException e) {
//	    	 logger.error("Socket exception ", e);
	    	 throw new M9000Exception(e);
	     }
	     catch (Exception e) {
//	    	 logger.error("Socket exception ", e);
	    	 throw new M9000Exception(e);
	     }

	     finally
	     {
	    	 try {
	    		 if (socket != null)
	    		 {
	    			 socket.close();
	    			 socket = null;
	    		 }
	    		 if (buffer != null)
	    		 {
	    			 buffer.close();
	    			 buffer = null;
	    		 }
	    		 if (in != null)
	    		 {
	    			 in.close();
	    			 in = null;
	    		 }
			} catch (IOException e) {
				logger.warn("Unable to close the Socket", e);
			}
	     }
		
		return scopeData;
	}

	public InputStream testSendAndReceiveBinary() throws M9000Exception
	{
//		System.out.println("Ip address " + getIpAddress());
//		System.out.println("Port " + getPort());
//		System.out.println("Command "+getCommand());

		if (getIpAddress() == null || getPort() == 0 || getCommand() == null)
		{
			throw new M9000Exception("Required parameters are null");
		}
		InputStream is = null;
	     try{
	    	 logger.debug("ip adress to be sent to.. "+getIpAddress() + " port "+getPort()+" command "+getCommand());
	       socket = new Socket();
	       socket.connect(new InetSocketAddress(getIpAddress().trim(), getPort()),5000);
//	       socket.setSoTimeout(10000);
	       socket.setSoTimeout(M9kStationUtil.getSocketReadTimeout());
	       
//	       System.out.println("After socket creation..."+socket);
//	    	 System.out.println("Socket "+socket.isClosed());
	       out = new PrintWriter(socket.getOutputStream(), true);
//	       System.out.println("After Print writer...");
	       is = socket.getInputStream();
//	       System.out.println("Command to be sent "+getCommand());
	       out.println(getCommand());
	       logger.debug("\n\nAfter out println "+is.available());
	       
	       
//	       System.out.println("Response from the server"+output);
	     }
	     catch (SocketTimeoutException e) {
	    	 logger.error("Socket timeout exception ", e);
	    	 throw new M9000Exception(e);
	     }
	     catch (UnknownHostException e) {
	    	 logger.error("Unknown host:"+getIpAddress());
	       logger.error("Host Name not known  ", e);
	    	 throw new M9000Exception("Host Name not known ", e);
	     } catch  (IOException e) {
	    	 logger.error("Socket exception ", e);
	    	 throw new M9000Exception(e);
	     }
	     catch (Exception e) {
	    	 logger.error("Socket exception ", e);
	    	 throw new M9000Exception(e);
	     }
	     finally
	     {
//	    	 try {
//	    		 if (socket != null)
//	    		 {
//	    			 socket.close();
//	    		 }
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	     }
		
		return is;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	
	public void closeAll()
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

	
	public static void main(String[] args)
	{
		String destIp = "195.1.1.72";
		int port = 9978;
		String command = "config";
//		String irigCommand = "irig,user=machine";
		int noOfTimes = 1;
//		System.out.println("args length "+args.length+" "+args[0]);
		if (args.length > 0)
		{
			if (args.length >= 1)
			{
				destIp = args[0];
				if (destIp.indexOf(".") == -1)
				{
					destIp = "192.168.1.101";
				}
			}
			if (args.length >= 2)
			{
				try
				{
					noOfTimes = Integer.parseInt(args[1]);
				}
				catch (Exception e) {
					e.printStackTrace();
					noOfTimes = 1;
				}
			}
		}
//		M9kStationCommandClient commandClient = null;
//		commandClient = new M9kStationCommandClient(destIp, port, command);
		ExecutorService executor = Executors.newCachedThreadPool();
		try {
			while (noOfTimes-- > 0)
			{
//				commandClient.sendAndReceive();
//				commandClient = new M9kStationCommandClient(destIp, port, irigCommand);
//				commandClient.sendAndReceive();
//				System.out.println("No of time commands sent "+noOfTimes);
				
				executor.submit(new M9kCommandThreads(destIp, port, command));
				Thread.sleep(5000);
			}
			executor.shutdown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
