package com.usi.m9000.station.commands;

import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.usi.m9000.common.M9kCreateComtradeFilesFromDB;
import com.usi.m9000.dao.MySqlDatDAO;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.station.util.M9kStationComtradeUtil;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationUtil;

public class M9kStationCreateComtradeFilesFromDB extends M9kCreateComtradeFilesFromDB{
	private static MySqlDatDAO mySqlDataDAO = null;

	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kStationCreateComtradeFilesFromDB.class);
	/**
	 * 
	 */
	public M9kStationCreateComtradeFilesFromDB() {
		if (mySqlDataDAO == null)
		{
			mySqlDataDAO =new MySqlDatDAO();
		}
		Runtime.getRuntime().addShutdownHook(new Thread("[ThreadPool-Shutdown]") {  
            
            @Override  
            public void run() {  
                if(scheduler != null && !scheduler.isShutdown()) {  
                	scheduler.shutdownNow();  
                }  
                  
            }  
        });  
//		Calendar.getInstance().setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	
	public static void main(String args[])
	{
		try
		{
			long startTime;
			long endTime;
			startTime = System.currentTimeMillis();
			M9kStationCreateComtradeFilesFromDB createComtrade = new M9kStationCreateComtradeFilesFromDB();
			createComtrade.createComtradeFilesForStation();
			createComtrade.closeAllConnections();
			endTime = System.currentTimeMillis();
			logger.debug("Total Time for complete process..."+(endTime-startTime));
		}
		catch (Exception e) {
			logger.error("Exception caught in main ", e);
		}
	}

	@Override
	public ComtradeDataDTO getAFaultRecordFromDB() {
		ComtradeDataDTO comtradeDataDTO = null;
		try
		{
			comtradeDataDTO = mySqlDataDAO.getLocalFaultRecordToProcess();
			if (comtradeDataDTO != null)
			{
				comtradeDataDTO.setLineGroupDetailsForFaultLoc(new M9kStationComtradeUtil().getLineGroupDetailsForFaultLoc());
			}
		}
		catch (Exception e) {
			logger.error("Unable to process a record. Database may be down.", e);
			comtradeDataDTO = null;
		}
		return comtradeDataDTO;
		

	}

	@Override
	public SimpleDateFormat getDateFormat(String dateFormat) {
		return M9kStationUtil.getDateFormat(dateFormat);
	}


	// Invoke super method
	public void scheduleCreateComtradeFiles() {
		if (M9kStationUtil.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
		{
			super.scheduleCreateComtradeFiles(ARCH_REMOTE_PULL);
		}
		else
		{
			super.scheduleCreateComtradeFiles(ARCH_LOCAL);
		}
		
		
	}
	
}
