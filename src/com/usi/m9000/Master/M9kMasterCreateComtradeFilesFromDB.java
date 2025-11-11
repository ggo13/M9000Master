package com.usi.m9000.Master;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.struts2.interceptor.SessionAware;

import com.usi.m9000.common.M9kCreateComtradeFilesFromDB;
import com.usi.m9000.dao.MySqlDatDAO;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.station.util.M9kStationComtradeUtil;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

public class M9kMasterCreateComtradeFilesFromDB extends M9kCreateComtradeFilesFromDB implements SessionAware, ServletContextListener{
	private static final int THREAD_SIZE = 3; // Only for Remote master
	private static MySqlDatDAO mySqlDataDAO = null;
	private static StationDAO stationDAO = null;
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	protected Runnable taskPerformer = null;
	private ScheduledFuture<?> scheduledTask = null;
	M9kMasterCreateComtradeFilesFromDB createComtrade = null;
	M9kDAOFactory m9kDAOFactory;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kMasterCreateComtradeFilesFromDB.class);
	/**
	 * 
	 */
	public M9kMasterCreateComtradeFilesFromDB() {
		m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		if (mySqlDataDAO == null)
		{
			mySqlDataDAO =new MySqlDatDAO();
		}
		if (stationDAO == null)
		{
			stationDAO =m9kDAOFactory.getStationDAO();
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
			M9kMasterCreateComtradeFilesFromDB createComtrade = new M9kMasterCreateComtradeFilesFromDB();
			logger.debug("Thread pool size set "+THREAD_SIZE);
			createComtrade.scheduler = Executors.newScheduledThreadPool(THREAD_SIZE);
			createComtrade.scheduleCreateComtradeFiles(ARCH_LOCAL);
//		createComtrade.createComtradeFilesForStation();
			endTime = System.currentTimeMillis();
	//		
	//
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
				comtradeDataDTO  = mySqlDataDAO.getFaultRecordToProcess();
				if (comtradeDataDTO != null)
				{
					String configXml = stationDAO.getConfigXml(comtradeDataDTO.getStationId());
					comtradeDataDTO.setLineGroupDetailsForFaultLoc(new M9kStationComtradeUtil().getLineGroupDetailsForFaultLoc(configXml));
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
		return M9kUtils.getDateFormat(dateFormat);
	}
	
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		if (M9kUtils.isRemote())
		{
			createComtrade = new M9kMasterCreateComtradeFilesFromDB();
			logger.debug("Thread pool size set "+THREAD_SIZE);
			createComtrade.scheduler = Executors.newScheduledThreadPool(THREAD_SIZE);
			createComtrade.scheduleCreateComtradeFiles(ARCH_REMOTE_PUSH);
		}
		else
		{
			logger.info("Master comtrade creation is disabled as it is set to local architecture");
		}
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		try
		{
			if (createComtrade != null)
			{
				if (createComtrade.scheduledTask != null)
				{
					createComtrade.scheduledTask.cancel(true);
				}
				if (createComtrade.scheduler != null)
				{
					createComtrade.scheduler.shutdownNow();
				}
			}
		}
		catch (Exception e) {
			logger.warn("Error occured in contextDestroyed method.",e);
		}
	}

	@Override
	public void setSession(Map<String, Object> arg0) {
		// TODO Auto-generated method stub
		
	}
}
