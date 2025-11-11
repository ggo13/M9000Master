package com.usi.m9000.station;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

import com.usi.m9000.dao.MySqlDfrDetailsDAO;
import com.usi.m9000.station.commands.M9kStationCreateComtradeFilesFromDB;
import com.usi.m9000.station.consumers.M9kConfigRestoreRequestHandler;
import com.usi.m9000.station.consumers.M9kMasterFileRequestHandler;
import com.usi.m9000.station.consumers.M9kMasterRequestHandler;
import com.usi.m9000.station.threads.M9kAlarmDataPushTask;
import com.usi.m9000.station.threads.M9kComtradeDataPushTask;
import com.usi.m9000.station.threads.M9kComtradeDataQuickSummaryPushTask;
import com.usi.m9000.station.threads.M9kContDataPushTask;
import com.usi.m9000.station.threads.M9kFaultDataProcessAndMergeTask;
import com.usi.m9000.station.threads.M9kLTRDataPushTask;
import com.usi.m9000.station.threads.M9kReportsDataPushTask;
import com.usi.m9000.station.threads.M9kSERDataPushTask;
import com.usi.m9000.station.util.LedName;
import com.usi.m9000.station.util.LedState;
import com.usi.m9000.station.util.M9kSeverityLevels;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;

public class M9kStationLaunch {
//	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kStationLaunch.class);
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kStationLaunch.class);
	static HierarchicalINIConfiguration iniConf;
	boolean watchDogEnable = false;
	String faultDataTransfer = M9kStationConstants.DISABLE;
	String ltFaultDataTransfer = M9kStationConstants.DISABLE;
	String contDataTransfer = M9kStationConstants.DISABLE;
	String contAnalogDataTransfer = M9kStationConstants.DISABLE;
	String serDataTransfer = M9kStationConstants.DISABLE;
	String alarmDataTransfer = M9kStationConstants.DISABLE;
	String reportsDataTransfer = M9kStationConstants.DISABLE;
	String performDbSetUp = M9kStationConstants.DISABLE;
	boolean ledPolling = true;
	boolean createComtrade = true;
	String masterType = "local";
	M9kMasterRequestHandler masterHandler;
	M9kMasterFileRequestHandler masterFileRequestHandler;
	static M9kConfigRestoreRequestHandler m9kConfigRestoreRequestHandler = null;
	public M9kStationLaunch() {
		super();
		// START: 06-Feb-2020 - Wait until the station config is available
		boolean stationAvailable = true;
		// END: 06-Feb-2020
		try {
			 System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
			iniConf = new HierarchicalINIConfiguration("station.properties");
			// START: 17-June-2021 - Listen to hostname queue no matter what
			m9kConfigRestoreRequestHandler = M9kConfigRestoreRequestHandler.getInstance();
			m9kConfigRestoreRequestHandler.start();
			// END: 17-June-2021
			// START: 06-Feb-2020 - Wait until the station config is available
			do
			{
				stationAvailable = 	M9kStationXMLUtil.initXml(null);
				logger.debug("Is station available..."+stationAvailable+" m9kConfigRestoreRequestHandler "+m9kConfigRestoreRequestHandler);
//				if (!stationAvailable && m9kConfigRestoreRequestHandler == null)
//				{
//					logger.debug("Listening to the DEBUG queue of host name...");
//					m9kConfigRestoreRequestHandler = new M9kConfigRestoreRequestHandler();
//					m9kConfigRestoreRequestHandler.start();
//				}
				if (!stationAvailable)
				{
					logger.debug("Waiting for 10 seconds before continuing...");	
					Thread.sleep(10000);
				}
			} while(!stationAvailable);
			// END: 06-Feb-2020
//			String prop = System.getProperty("log4j.configuration");
//			System.out.println("Prop "+prop);
//		    if (prop == null) prop = "log4j2.xml";
//		    URL log4jConfig = Loader.getResource(prop,ClassLoader.getSystemClassLoader());
//		    System.out.println("Get protocol "+log4jConfig.getProtocol());
//		    if (log4jConfig.getProtocol().equalsIgnoreCase("file")) {
//		    	System.out.println("log4jConfig.getFile() "+log4jConfig.getFile());
//		    	DOMConfigurator.configureAndWatch(log4jConfig.getFile(), 10000);
//		    }
//		    else {
//		        // cannot monitor if file changed because URL is not a file
//		    }

		    // Update dfr_details table whenever station master starts up with the config
		    try
		    {
		    	MySqlDfrDetailsDAO mySqlDfrDetailsDAO = new MySqlDfrDetailsDAO();
		    	mySqlDfrDetailsDAO.createOrUpdateDfrDetails();
		    }
		    catch (Exception e)
		    {
		    	logger.warn("Issue with update of dfr_details table during station master software launch",e);
		    }

			readInitParameters();
			try
			{
				if (performDbSetUp.equalsIgnoreCase(M9kStationConstants.ENABLE))
				{
					if (M9kStationUtil.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
					{
						setUpDatabaseTriggersAndTables();
					}
					else
					{
						disableFaultsTransfer();
						disableLtrDataTransfer();
						disableSerDataTransfer();
						disableAlarmDataTransfer();
						disableReportsDataTransfer();
						disableContComtradeDataTransfer();
					}
				}
			}
			catch(Exception e)
			{
				logger.error("Error in creating required tables for data transfer. ");
			}
			Runtime.getRuntime().addShutdownHook(new Thread() {
			    public void run() { 
			    	M9kStationUtil.setWatchDogEnabled(false);
			    	M9kLED.resetLed();
			    	logger.info("Exiting the station master software");
			    	M9kLED.setLedWithRelay(LedName.ONLINE, LedState.RED, "Station Software stopped running");
			    	try {
						M9kStationUtil.sendNotification(M9kSeverityLevels.FATAL.name(), "Station Software stopped running");
					} catch (Exception e) {
						logger.error("Error in notifying shutdown action to Master station",e);
					}
			    	M9kStationDBUtil.close(); 
			    }
			});		
		} catch (ConfigurationException e) {
			logger.error("Configuration exception ", e);
		}catch (Exception e) {
			logger.error("Exception in the station Launch module", e);
		}

		// BrokerService broker = new BrokerService();
		//
		// // configure the broker
		// try {
		// broker.addConnector("tcp://localhost:61616");
		// broker.start();
		//
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	
	public void launchStationApps() throws Exception
	{
		// Launching the watchdog server
//		if (isWatchDogEnable())
//		{
//			logger.info("Launching watchdog services...");
//			M9kWatchDogServer m9kWatchDogServer = new M9kWatchDogServer();
//			m9kWatchDogServer.scheduleWatchDog();
//			logger.info("Launching watchdog services...Done");
//		}
//		else
//		{
//			M9kStationUtil.disableWatchDog();
//		}

		boolean booFaultPushStarted = false;
		boolean booFaultDataPull = false;
		try
		{
			File file = new File("/data/m9k/log/");
			file.mkdirs();
			file.setWritable(true, false);
			file.setExecutable(true,false);
			file.setReadable(true, false);
			file = new File("/data/m9k/log/StationMaster");
			file.mkdirs();
			file.setWritable(true, false);
			file.setExecutable(true,false);
			file.setReadable(true, false);

		}
		catch(Exception e)
		{
			logger.error("Error in creating required log directories",e);
		}
		// Initial reset before begin
		M9kLED m9kLed = M9kLED.getInstance();

		M9kLED.resetLed();
		if (M9kStationUtil.isPowerSupplyAvailable())
		{
			M9kLED.resetTriggerStatus();
			// START: 16-May-2024 - Reset Disturbance Alarm Status
			M9kLED.resetDisturbanceAlarmStatus();
		}
		// First launch the Master Request handler
		logger.info("Starting Master Request Handler...");
		try {
			masterHandler = M9kMasterRequestHandler.getInstance();
			masterHandler.start();
			
			logger.info("Starting Master Request Handler...Done");
			System.out.println("Waiting for Master requests");
			
			masterFileRequestHandler = M9kMasterFileRequestHandler.getInstance();
			masterFileRequestHandler.start();
			
			logger.info("Starting Master File Request Handler...Done");
			System.out.println("Waiting for Master File requests");
			
		} catch (Exception e) {
			logger.error("Error in starting Master Request Handler ",e);
			return;
		}
//		System.out.println("Started Listening for requests from Master"+masterHost);
//		logger.info("Started Listening for requests from Master"+masterHost);
//		logger.info("Starting Master DFR Health Check Task...");
//		M9kDFRHealthStatusProcessor healthCheck = new M9kDFRHealthStatusProcessor();
//		healthCheck.scheduleHealthCheck();
//		logger.info("Starting Master DFR Health Check Task...Done");
//		System.out.println("Started DFR health check");
		// setting watchdog info to M9kStationUtil to enable or disable and then petting if it is enabled
		M9kStationUtil.setWatchDogEnabled(isWatchDogEnabled());
		logger.info("Starting Fault Data Process task...");
		M9kFaultDataProcessAndMergeTask processFaults = new M9kFaultDataProcessAndMergeTask();
		processFaults.scheduleFaultDataProcess();
		logger.info("Starting Fault Data Process task...Done");
		
		if (getFaultDataTransfer().equalsIgnoreCase(M9kStationConstants.PUSH))
		{
			booFaultPushStarted = true;
			logger.info("Starting Fault Data push task...");
			M9kComtradeDataPushTask pushComtrade = new M9kComtradeDataPushTask();
			pushComtrade.schedulePush();
			logger.info("Starting Fault Data push task...Done");
			System.out.println("Polling for Fault record updates and pushes to the Master");
		}
		else if (getFaultDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL))
		{
			booFaultDataPull = true;
			logger.info("Starting Fault Data Quick summary Push task...");
			M9kComtradeDataQuickSummaryPushTask pushComtrade = new M9kComtradeDataQuickSummaryPushTask();
			pushComtrade.schedulePush();
			logger.info("Starting Fault Data Quick summary push task...Done");
			System.out.println("Polling for Fault record  Quick summary updates and pushes to the Master");
		}
		else
		{
			logger.info("Fault Data transfer is disabled in station.properties file in lib directory");
			System.out.println("Fault Data transfer is disabled in station.properties file in lib directory");
		}

		if (getSerDataTransfer().equalsIgnoreCase(M9kStationConstants.PUSH) || getSerDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL))
		{
			logger.info("Starting SER Data push task...");
			M9kSERDataPushTask pushSer = new M9kSERDataPushTask();
			pushSer.scheduleSerPush();
			logger.info("Starting SER Data push task...Done");
			System.out.println("Polling for SER updates and pushes to the Master");
		}
		else
		{
			logger.info("SER Push is disabled in station.properties file in lib directory");
			System.out.println("SER Push is disabled in station.properties file in lib directory");
		}
		
		if (getAlarmDataTransfer().equalsIgnoreCase(M9kStationConstants.PUSH) || getAlarmDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL))
		{
			logger.info("Starting Alarm Data push task...");
			M9kAlarmDataPushTask pushAlarms = new M9kAlarmDataPushTask();
			pushAlarms.scheduleAlarmPush();
			logger.info("Starting Alarm Data push task...Done");
			System.out.println("Polling for Alarm updates and pushes to the Master");
		}
		else
		{
			logger.info("Alarm Push is disabled in station.properties file in lib directory");
			System.out.println("Alarm Push is disabled in station.properties file in lib directory");
		}
		
		if (getReportsDataTransfer().equalsIgnoreCase(M9kStationConstants.PUSH) || getReportsDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL))
		{
			logger.info("Starting Reports Data push task...");
			M9kReportsDataPushTask pushReports = new M9kReportsDataPushTask();
			pushReports.scheduleReportsPush();
			logger.info("Starting Reports Data push task...Done");
			System.out.println("Polling for Reports updates and pushes to the Master");
		}
		else
		{
			logger.info("Alarm Push is disabled in station.properties file in lib directory");
			System.out.println("Alarm Push is disabled in station.properties file in lib directory");
		}

		// START: 16-DEC-2014 Commented continuous analog push and implemented cont comtrade data push that pushes all contnuous data 
//		if (getContAnalogDataTransfer().equalsIgnoreCase(M9kStationConstants.PUSH) || getContAnalogDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL))
//		{
//			logger.info("Starting Continuous Analog Data push task...");
//			M9kContAnalogDataPushTask pushContAnalog = new M9kContAnalogDataPushTask();
//			pushContAnalog.schedulePush();
//			logger.info("Starting Continuous Analog Data push task...Done");
//			System.out.println("Polling for Continuous Analog data updates and pushes to the Master");
//		}
//		else
//		{
//			logger.info("Continuous Analog data Push is disabled in station.properties file in lib directory");
//			System.out.println("Continuous Analog data Push is disabled in station.properties file in lib directory");
//		}
		
		if (getContDataTransfer().equalsIgnoreCase(M9kStationConstants.PUSH) || getContDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL))
		{
			logger.info("Starting Continuous Data push task...");
			M9kContDataPushTask pushCont = new M9kContDataPushTask();
			pushCont.schedulePush();
			logger.info("Starting Continuous Comtrade Data push task...Done");
			System.out.println("Polling for Continuous Comtrade data updates and pushes to the Master");
		}
		else
		{
			logger.info("Continuous Comtrade data Push is disabled in station.properties file in lib directory");
			System.out.println("Continuous Comtrade data Push is disabled in station.properties file in lib directory");
		}
		// END: 16-DEC-2014
		if (getLtFaultDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL) || getLtFaultDataTransfer().equalsIgnoreCase(M9kStationConstants.PUSH))
		{
			logger.info("Starting Long Term Data push task...");
			M9kLTRDataPushTask pushLTRData = new M9kLTRDataPushTask();
			pushLTRData.schedulePush();
			logger.info("Starting Long Term Data Data push task...Done");
			System.out.println("Polling for Long Term Data updates and pushes to the Master");
		}
		else
		{
			logger.info("Long Term data Push is disabled in station.properties file in lib directory");
			System.out.println("Long Term data Push is disabled in station.properties file in lib directory");
		}
		
		if (isLedPolling())
//		if (M9kStationUtil.isPowerSupplyAvailable())
		{
			System.out.println("About to poll for health and update LED");
			m9kLed.schedulePolling();
		}
		else
		{
			logger.info("Either LED Update is disabled in station.properties file in lib directory or Power Supply is unavailable");
			System.out.println("Either LED Update is disabled in station.properties file in lib directory or Power Supply is unavailable");
		}
		
//		if (isCreateComtrade() || getFaultDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL) || getFaultDataTransfer().equalsIgnoreCase(M9kStationConstants.DISABLE))
		// START: 21-Jan-2020 - Release v1.0.9.2 - To enable fault creation only if fault is not pushed to remote.
//		if (M9kStationUtil.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_LOCAL) || !booFaultPushStarted)
		if ((M9kStationUtil.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_LOCAL) && !booFaultPushStarted) || (booFaultDataPull))
		// END: 21-Jan-2020
		{
				System.out.println("About to start comtrade file creation process");
				M9kStationCreateComtradeFilesFromDB createComtrade = new M9kStationCreateComtradeFilesFromDB();
					createComtrade.scheduleCreateComtradeFiles();
		}
		else
		{

			logger.info("Comtrade file creation is part of ComtradeDataPushTask");
			System.out.println("Comtrade file creation is part of ComtradeDataPushTask");

//			logger.info("Comtrade file creation is disabled as fault data are pushed in station.properties file in lib directory");
//			System.out.println("Comtrade file creation is disabled as fault data are pushed in station.properties file in lib directory");
		}
		if (!M9kStationUtil.isPowerSupplyAvailable())
		{
			m9kLed.updateLeds(); // In case power supply/Alarm module is not reachable still log the alarms log 
		}
	}
	
//	@SuppressWarnings("unused")
//	private boolean isWatchDogEnabled()
//	{
//		boolean booEnabled = false;
//		String watchDogStatus = M9kStationConstants.DISABLE;
//		Set<String> dfrs = iniConf.getSections();
//		for (String dfr : dfrs) {
//			if (!dfr.toUpperCase().startsWith("DFR"))
//			{
//				watchDogStatus = iniConf.getString(dfr+".watch-dog", "DISABLE");
//				if (watchDogStatus.equalsIgnoreCase(M9kStationConstants.ENABLE))
//				{
//					booEnabled = true;
//				}
//				break;
//			}
//		}
//		return booEnabled;
//	}
	private void readInitParameters()
	{
		String status = M9kStationConstants.DISABLE;
//		Set<String> dfrs = iniConf.getSections();
//		for (String dfr : dfrs) {
//			if (dfr.toUpperCase().equalsIgnoreCase("STATION"))
//			{
				status = iniConf.getString("station.watch-dog", "DISABLE");
				if (status.equalsIgnoreCase(M9kStationConstants.ENABLE))
				{
					setWatchDogEnable(true);
				}
				faultDataTransfer = iniConf.getString("station.fault-data-transfer", M9kStationConstants.DISABLE);

				ltFaultDataTransfer = iniConf.getString("station.lt-fault-data-transfer", M9kStationConstants.DISABLE);

				contDataTransfer = iniConf.getString("station.cont-data-transfer", M9kStationConstants.DISABLE);

				contAnalogDataTransfer = iniConf.getString("station.cont-analog-data-transfer", M9kStationConstants.DISABLE);

				serDataTransfer = iniConf.getString("station.ser-data-transfer", M9kStationConstants.DISABLE);
				logger.debug("serDataTransfer read "+serDataTransfer);
				alarmDataTransfer = iniConf.getString("station.alarm-data-transfer", M9kStationConstants.DISABLE);
				reportsDataTransfer = iniConf.getString("station.reports-data-transfer", M9kStationConstants.DISABLE);
				status = iniConf.getString("station.led-polling", "ENABLE");
				if (status.equalsIgnoreCase(M9kStationConstants.DISABLE))
				{
					setLedPolling(false);
				}
				performDbSetUp = iniConf.getString("station.perform-db-setup", M9kStationConstants.DISABLE);
				// Faults are always created in station master and pushed on demand to master
//				masterType = iniConf.getString("station.master-type", "local");
				status = iniConf.getString("station.create-comtrade", "ENABLE");
				if (status.equalsIgnoreCase(M9kStationConstants.DISABLE))// && masterType.equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
				{
					setCreateComtrade(false);
				}
				
				
//				break;
//			}
//		}
	}
	
	public void setUpDatabaseTriggersAndTables()
	{
//		setUpFederatedLink();
		// Faults transfer setup
		if (getFaultDataTransfer().equalsIgnoreCase(M9kStationConstants.PUSH))
		{
			setUpFaultsPush(); // Transfers dat_staging table to remote master
		}
		else if (getFaultDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL))
		{
			setUpFaultsPull();// Transfers comtrade_details table to remote master
		}
		else
		{
			disableFaultsTransfer();
		}
		
		// Ltr Faults transfer setup
		if (getLtFaultDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL) || getLtFaultDataTransfer().equalsIgnoreCase(M9kStationConstants.PUSH))
		{
			setUpLtrDataPull();
		}
		else
		{
			disableLtrDataTransfer();
		}
		
		// SER transfer setup
		if (getSerDataTransfer().equalsIgnoreCase(M9kStationConstants.PUSH) || getSerDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL))
		{
			setUpSerDataPush();
		}
		else
		{
			disableSerDataTransfer();
		}

		if (getAlarmDataTransfer().equalsIgnoreCase(M9kStationConstants.PUSH) || getAlarmDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL))
		{
			setUpAlarmDataPush();
		}
		else
		{
			disableAlarmDataTransfer();
		}
		
		if (getReportsDataTransfer().equalsIgnoreCase(M9kStationConstants.PUSH) || getReportsDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL))
		{
			setUpReportsDataPush();
		}
		else
		{
			disableReportsDataTransfer();
		}

		// START: 16-DEC-2014 Transfers all the continuous comtrade data files liste created
		// Cont comtrade data transfer setup - All continuous data file list are transfered
		if (getContDataTransfer().equalsIgnoreCase(M9kStationConstants.PUSH) || getContDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL))
		{
			setUpContComtradeDataPush();
		}
		else
		{
			disableContComtradeDataTransfer();
		}
		
//		// ContAnalog(Osc) data transfer setup
//		if (getContAnalogDataTransfer().equalsIgnoreCase(M9kStationConstants.PULL))
//		{
//			//TODO: To be implemented
//		}
//		else
//		{
//			//TODO: To be implemented
//		}
		// END: 16-DEC-2014
	}

	private void setUpFederatedLink() {
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
			stmt.executeUpdate("drop server if exists fedlink");
			stmt.executeUpdate("CREATE SERVER fedlink FOREIGN DATA WRAPPER mysql OPTIONS (USER '"+M9kStationUtil.getMasterDbUser()+"', PASSWORD '"+M9kStationUtil.getMasterDbPwd()+"', HOST '"+M9kStationUtil.getMasterHost()+"', PORT 3306, DATABASE 'm9000')");
		} catch (Exception e) {
			logger.error("Exception occured in setting up federaed server for data transfer ", e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in setUpFaultsPush cleanup", e);
			}

		}
		
	}


	private void disableFaultsTransfer() {
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.dat_changes");
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.comtrade_details_changes");
//			stmt.executeUpdate("DROP TABLE IF EXISTS m9000.dat_updates");
//			stmt.executeUpdate("DROP TABLE IF EXISTS m9000.comtrade_details_updates");
		} catch (Exception e) {
			logger.error("Exception occured in setting up dat_staging table for data transfer ", e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in setUpFaultsPush cleanup", e);
			}

		}
	}


	// Creates triggers and tracking table to push dat_staging and clears triggers in comtrade_details
	private void setUpFaultsPush() {
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
			
			//stmt.executeUpdate("DROP TABLE IF EXISTS dat_updates");
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS m9000.dat_updates (   faultId 	INT(11)	NOT NULL,   updated 	DATETIME,   PRIMARY KEY (faultId) ) ENGINE=MyISAM");
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS m9000.error_dat_updates (   faultId 	INT(11)	NOT NULL,   updated 	DATETIME,   PRIMARY KEY (faultId) ) ENGINE=MyISAM");
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.dat_changes");
			stmt.executeUpdate("CREATE DEFINER='dfr'@'localhost' TRIGGER m9000.dat_changes AFTER INSERT ON m9000.dat_staging FOR EACH ROW BEGIN INSERT INTO dat_updates (faultId, updated) VALUES (NEW.faultId, NOW()); END");
			
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.comtrade_details_changes");
//			stmt.executeUpdate("DROP TABLE IF EXISTS m9000.comtrade_details_updates");
			
		} catch (Exception e) {
			logger.error("Exception occured in setting up dat_staging table for data transfer ", e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in setUpFaultsPush cleanup", e);
			}

		}
	}
	
	// Creates triggers and tracking table to push comtrade_details and clears triggers in dat_staging
	private void setUpFaultsPull() {
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
//			stmt.executeUpdate("DROP TABLE IF EXISTS comtrade_details_updates");
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS m9000.comtrade_details_updates (   faultId 	INT(11)	NOT NULL,   updated 	DATETIME,   PRIMARY KEY (faultId) ) ENGINE=MyISAM");
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.comtrade_details_changes");
			stmt.executeUpdate("CREATE TRIGGER m9000.comtrade_details_changes AFTER INSERT ON m9000.comtrade_details FOR EACH ROW BEGIN INSERT INTO comtrade_details_updates (faultId, updated) VALUES (NEW.fault_id, NOW()); END");

			stmt.executeUpdate("DROP TRIGGER IF EXISTS dat_changes");
//			stmt.executeUpdate("DROP TABLE IF EXISTS dat_updates");
			
			
		} catch (Exception e) {
			logger.error("Exception occured in setting up comtrade_details table for data transfer ", e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in setUpFaultsPull cleanup", e);
			}

		}
	}

	// Creates triggers and tracking table to push comtrade_details and clears triggers in dat_staging
	private void setUpLtrDataPull() {
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
//			stmt.executeUpdate("DROP TABLE IF EXISTS ltr_updates");
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS m9000.ltr_updates (   id 	INT(11)	NOT NULL,   updated 	DATETIME,   PRIMARY KEY (id) ) ENGINE=MyISAM");
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.ltr_dat_changes");
			stmt.executeUpdate("CREATE DEFINER='dfr'@'localhost' TRIGGER m9000.ltr_dat_changes AFTER INSERT ON m9000.long_term_dat FOR EACH ROW BEGIN INSERT INTO ltr_updates (id, updated) VALUES (NEW.id, NOW()); END");
			
		} catch (Exception e) {
			logger.error("Exception occured in setting up comtrade_details table for data transfer ", e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in setUpFaultsPull cleanup", e);
			}

		}
	}
	
	private void disableLtrDataTransfer()
	{
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.ltr_dat_changes");
//			stmt.executeUpdate("DROP TABLE IF EXISTS ltr_updates");
			
		} catch (Exception e) {
			logger.error("Exception occured in setting up comtrade_details table for data transfer ", e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in setUpFaultsPull cleanup", e);
			}

		}

	}

	// Creates triggers and tracking table to push comtrade_details and clears triggers in dat_staging
	private void setUpSerDataPush() {
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
//			stmt.executeUpdate("DROP TABLE IF EXISTS ser_updates");
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS m9000.ser_updates (   id 	INT(11)	NOT NULL,   updated 	DATETIME,   PRIMARY KEY (id) ) ENGINE=MyISAM");
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.new_ser");
			stmt.executeUpdate("CREATE DEFINER='dfr'@'localhost' TRIGGER m9000.new_ser AFTER INSERT ON m9000.ser FOR EACH ROW BEGIN INSERT INTO ser_updates (id, updated) VALUES (NEW.id, NOW()); END");
			
		} catch (Exception e) {
			logger.error("Exception occured in setting up comtrade_details table for data transfer ", e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in setUpFaultsPull cleanup", e);
			}

		}
	}
	
	private void disableSerDataTransfer()
	{
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
//			stmt.executeUpdate("DROP TABLE IF EXISTS ser_updates");
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.new_ser");
			
		} catch (Exception e) {
			logger.error("Exception occured in setting up comtrade_details table for data transfer ", e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in setUpFaultsPull cleanup", e);
			}

		}

	}

	// Creates triggers and tracking table to push alarms data
	private void setUpAlarmDataPush() {
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
//			stmt.executeUpdate("DROP TABLE IF EXISTS ser_updates");
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS m9000.alarms_log_updates (   id 	INT(11)	NOT NULL,   updated 	DATETIME,   PRIMARY KEY (id) ) ENGINE=MyISAM");
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.new_alarms_log");
			stmt.executeUpdate("CREATE DEFINER='dfr'@'localhost' TRIGGER m9000.new_alarms_log AFTER INSERT ON m9000.alarms_log FOR EACH ROW BEGIN INSERT INTO alarms_log_updates (id, updated) VALUES (NEW.idalarms_log, NOW()); END");
			
		} catch (Exception e) {
			logger.error("Exception occured in setting up alarms_log table for data transfer ", e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in setUpFaultsPull cleanup", e);
			}

		}
	}
	
	private void disableAlarmDataTransfer()
	{
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
//			stmt.executeUpdate("DROP TABLE IF EXISTS ser_updates");
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.new_alarms_log");
			
		} catch (Exception e) {
			logger.error("Exception occured in setting up new_alarms_log trigger for data transfer ", e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in setUpFaultsPull cleanup", e);
			}

		}

	}

	// Creates triggers and tracking table to push Reports data 
	private void setUpReportsDataPush() {
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
//			stmt.executeUpdate("DROP TABLE IF EXISTS ser_updates");
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS m9000.reports_updates (   id 	INT(11)	NOT NULL,   updated 	DATETIME,   PRIMARY KEY (id) ) ENGINE=MyISAM");
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.new_reports");
			stmt.executeUpdate("CREATE DEFINER='dfr'@'localhost' TRIGGER m9000.new_reports AFTER INSERT ON m9000.reports FOR EACH ROW BEGIN INSERT INTO reports_updates (id, updated) VALUES (NEW.idreports, NOW()); END");
			
		} catch (Exception e) {
			logger.error("Exception occured in setting up reports table for data transfer ", e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in setUpFaultsPull cleanup", e);
			}

		}
	}

	private void disableReportsDataTransfer()
	{
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
//			stmt.executeUpdate("DROP TABLE IF EXISTS ser_updates");
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.new_reports");
			
		} catch (Exception e) {
			logger.error("Exception occured in setting up new_reports trigger for data transfer ", e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in setUpFaultsPull cleanup", e);
			}

		}

	}
	
	private void setUpContComtradeDataPush() {
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
//			stmt.executeUpdate("DROP TABLE IF EXISTS ser_updates");
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS continuous_comtrade_data_updates ( id int(11)	NOT NULL, updated datetime, PRIMARY KEY(id)) ENGINE=MyISAM");
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.new_continuous_comtrade_data");
			stmt.executeUpdate("CREATE DEFINER='dfr'@'localhost' TRIGGER m9000.new_continuous_comtrade_data AFTER INSERT ON m9000.continuous_comtrade_data FOR EACH ROW BEGIN  INSERT INTO continuous_comtrade_data_updates (id, updated)  VALUES (NEW.id, NOW());END");
			
		} catch (Exception e) {
			logger.error("Exception occured in setting up reports table for data transfer ", e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in setUpFaultsPull cleanup", e);
			}

		}
	}

	private void disableContComtradeDataTransfer()
	{
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
//			stmt.executeUpdate("DROP TABLE IF EXISTS ser_updates");
			stmt.executeUpdate("DROP TRIGGER IF EXISTS m9000.new_continuous_comtrade_data");
			
		} catch (Exception e) {
			logger.error("Exception occured in setting up new_reports trigger for data transfer ", e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in setUpFaultsPull cleanup", e);
			}

		}

	}

	public static void main(String args[])
	{
		M9kStationLaunch stationLaunch = new M9kStationLaunch();
		try {
			stationLaunch.launchStationApps();
		} catch (Exception e) {
			logger.error("Station master software quit running", e);
	    	M9kLED.resetLed();
	    	M9kLED.setLedWithRelay(LedName.ONLINE, LedState.RED, "Station Master Software stopped running");
	    	M9kStationDBUtil.close(); 
		}
	}

	public boolean isWatchDogEnabled() {
		return watchDogEnable;
	}

	public void setWatchDogEnable(boolean watchDogEnable) {
		this.watchDogEnable = watchDogEnable;
	}

	public boolean isLedPolling() {
		return ledPolling;
	}

	public void setLedPolling(boolean ledPolling) {
		this.ledPolling = ledPolling;
	}

	public boolean isCreateComtrade() {
		return createComtrade;
	}

	public void setCreateComtrade(boolean createComtrade) {
		this.createComtrade = createComtrade;
	}

	public String getMasterType() {
		return masterType;
	}

	public void setMasterType(String masterType) {
		this.masterType = masterType;
	}


	public String getFaultDataTransfer() {
		return faultDataTransfer;
	}


	public String getLtFaultDataTransfer() {
		return ltFaultDataTransfer;
	}


	public String getContDataTransfer() {
		return contDataTransfer;
	}


	public String getContAnalogDataTransfer() {
		return contAnalogDataTransfer;
	}


	public String getSerDataTransfer() {
		return serDataTransfer;
	}


	public String getAlarmDataTransfer() {
		return alarmDataTransfer;
	}

	public String getReportsDataTransfer() {
		return reportsDataTransfer;
	}

}
