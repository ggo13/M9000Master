/**
 * 
 */
package com.usi.m9000.backup;

import java.util.Calendar;

import org.apache.commons.configuration.HierarchicalINIConfiguration;

import com.usi.m9000.station.threads.M9kExportContAnalogComtrades;
import com.usi.m9000.station.threads.M9kExportContMeasurementsComtrades;
import com.usi.m9000.station.util.M9kBackupUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;

/**
 * @author sramasamy
 *
 */
public class M9kBackup {
	private long lastHourToProcess = Calendar.getInstance().getTimeInMillis();
	private int contAnalogComtradeSize = 1;
	private int contMeasurementsComtradeSize = 2;
	private int contComtradeNoOfDaysToRetain = 1;
	private String destExportDir = "/data/m9k/auto-exports/";
	static HierarchicalINIConfiguration iniConf;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kBackup.class);
	
	/**
	 * 
	 */
	public M9kBackup() {
		try
		{
			logger.info("Initializing M9k Back up Job");
			init();
		}
		catch (Exception e) {
			logger.error("Unable to read from property file",e);
		}
	}

	private void init() throws Exception
	{
		M9kStationXMLUtil.initXml(null);
		iniConf = new HierarchicalINIConfiguration("m9k-backup.properties");
		lastHourToProcess = M9kBackupUtil.getLastHourToAutoExport();
		contAnalogComtradeSize = iniConf.getInt("export-cont-analog-comtrade-size-in-minutes", 1);
		contMeasurementsComtradeSize = iniConf.getInt("export-cont-measurements-comtrade-size-in-minutes", 2);
		contComtradeNoOfDaysToRetain = iniConf.getInt("export-cont-comtrade-days-to-retain", 1);
		destExportDir = iniConf.getString("export-cont-comtrade-dir", "/data/m9k/auto-exports/");
	}
	public void scheduleJobs() 
	{
		try
	  	  {
	    		boolean stationAvailable = true;
	          	do
	    			{
	    				stationAvailable = 	M9kStationXMLUtil.initXml(null);
	    				logger.debug("Is station available..."+stationAvailable);
	    				if (!stationAvailable)
	    				{
	    					logger.info("No Station Configuration available. Waiting for 10 seconds before continuing to check for station configuration...");		
	    					Thread.sleep(10000);
	    				}
	    			} while(!stationAvailable);
	  	  }
	  	  catch (Exception e) {
	  		  logger.error("Exception occurred while waiting for Station Configurations",e);
	  	  }
		logger.info("Scheduling M9k Back up Job for cont Analog");
		M9kExportContAnalogComtrades m9kExportContAnalogComtrades = new M9kExportContAnalogComtrades(lastHourToProcess, contAnalogComtradeSize, destExportDir, contComtradeNoOfDaysToRetain);
		m9kExportContAnalogComtrades.scheduleCreateContComtrades();
		logger.info("Scheduling M9k Back up Job for cont Measurements");
		M9kExportContMeasurementsComtrades m9kExportContMeasurementsComtrades = new M9kExportContMeasurementsComtrades(lastHourToProcess, contMeasurementsComtradeSize, destExportDir, contComtradeNoOfDaysToRetain);
		m9kExportContMeasurementsComtrades.scheduleCreateContComtrades();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		M9kBackup m9kBackup = new M9kBackup();
		m9kBackup.scheduleJobs();

	}

}
