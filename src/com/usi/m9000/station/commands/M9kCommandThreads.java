package com.usi.m9000.station.commands;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.station.net.M9kStationCommandClient;

public class M9kCommandThreads implements Runnable {

	private M9kStationCommandClient commandClient;
	private String ipAddress;
	private int port;
	private String commandToSend;
	long start;
	long end;

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kCommandThreads.class);
	
	public M9kCommandThreads(String ipAddress, int port, String commandToSend) {
		super();
		this.ipAddress = ipAddress;
		this.port = port;
		this.commandToSend = commandToSend;
	}

	@Override
	public void run() {
		try
		{
			start = System.currentTimeMillis();
			send(ipAddress, port, commandToSend);
			end = System.currentTimeMillis();
			logger.debug("\t\t\tThread complete for the command "+commandToSend+" Time Taken "+(end-start));
		}catch (Exception e) {
			logger.error("Dfr "+ipAddress+" Not reachable. ", e);
//			System.out.println("Dfr "+ipAddress+" Not reachable. "+ e);
		}
		
	}

	private void send(String destIp, int port, String command) throws M9000Exception {
		
		commandClient = new M9kStationCommandClient(destIp, port, command);
		commandClient.sendAndReceive();
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

	public String getCommandToSend() {
		return commandToSend;
	}

	public void setCommandToSend(String commandToSend) {
		this.commandToSend = commandToSend;
	}
}
