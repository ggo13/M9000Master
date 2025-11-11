package com.usi.m9000.station.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kScp;


public class M9kRestoreActiveConfig {
	
	M9kStationCommandClient commandClient;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kRestoreActiveConfig.class);

	public M9kRestoreActiveConfig() {
		super();
		logger.debug("Entered M9kRestoreActiveConfig ");
	}

	public File getActiveConfig() throws M9000Exception {
		logger.debug("RESTORE-ACTIVE-CONFIG: Entered getActiveConfig...");
		File activeConfigSqlFile = null;
		String ipAddress;
		String ipAddressPrefix = "192.168.1.";
		int port = 0;
		
		try
		{
			port = M9kStationUtil.getPort();
			for (int i = 101;i <= 105 ; i++) {
				ipAddress = ipAddressPrefix+i;
				logger.debug("Ip address " + ipAddress);
				logger.debug("Port " + port);
				try 
				{
					if (requestHealthStatus(ipAddress, port))
					{
						M9kScp m9kScp = new M9kScp(ipAddress,22,"root");
						activeConfigSqlFile = m9kScp.getActiveConfigurationFile(M9kConstants.ACTIVE_CONFIG_SQL, M9kConstants.ACTIVE_CONFIG_SQL_LOC);
						break;
					}
				}
				catch (Exception e) {
					logger.warn("failed to get active config from dfr "+ipAddress+" port: "+port, e);
				}
			}
		} catch (Exception e) {
			throw new M9000Exception("Failed to get active config from dfr ",e);
		}
		logger.debug("return active config file "+activeConfigSqlFile);
		return activeConfigSqlFile;
	}

	public String getActiveConfigText() throws M9000Exception
	{
		logger.debug("Inside getActiveConfigText...");
		StringBuilder activeConfigText = new StringBuilder();
		String line = null;
		BufferedReader bfrConfig = null;
		try
		{
			bfrConfig = new BufferedReader(new FileReader(getActiveConfig()));
			logger.debug("Config file from chassis "+bfrConfig);
			while ((line = bfrConfig.readLine()) != null)
			{
				activeConfigText.append(line);
			}
			bfrConfig.close();
			bfrConfig = null;
		}
		catch (Exception e) {
			if (bfrConfig != null)
			{
				try {
					bfrConfig.close();
				} catch (IOException e1) {
					logger.warn("Finally threw exception when closing the bufferd reader");
				}
				bfrConfig = null;
			}
		}
		logger.debug("Return config from dfr "+(activeConfigText.length() > 50 ? activeConfigText.substring(0,50):activeConfigText));
		return activeConfigText.toString();
	}
	private boolean requestHealthStatus(String destIp, int port)
	{
		String dfrStatus = null;
		boolean booStatus = false;
	     try{
	    	 commandClient = new M9kStationCommandClient(destIp, port, "PING");
	    	 dfrStatus = commandClient.sendAndReceive(); 
	    	 logger.debug("Response from the server "+destIp+" is "+dfrStatus);
		       booStatus = true;
		     
	     } 
	     catch(M9000Exception me)
	     {
	    	 booStatus = false;
	     } 
	     return booStatus;
	}
	}
