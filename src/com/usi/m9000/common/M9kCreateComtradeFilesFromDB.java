package com.usi.m9000.common;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.common.email.SendMailUSI;
import com.usi.m9000.config.M9kComtradeParser;
import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dao.ComtradeDataDAO;
import com.usi.m9000.dao.LcItemsDAO;
import com.usi.m9000.dao.MySqlDatDAO;
import com.usi.m9000.dao.MySqlLcItemsDAO;
import com.usi.m9000.dao.MySqlSystemDAO;
import com.usi.m9000.dao.SystemDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.dto.EmailReportsSettingsDTO;
import com.usi.m9000.dto.EmailSettingsDTO;
import com.usi.m9000.dto.LcItemDTO;
import com.usi.m9000.station.util.M9kSeverityLevels;
import com.usi.m9000.station.util.M9kStationComtradeUtil;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kReportUtil;
import com.usi.m9000.util.M9kUtils;

/**
 * // START: 10-May-2021 - Email Report properties moved to database from M9k_COMTRADE properties
 * @author sramasamy
 *
 */
public abstract class M9kCreateComtradeFilesFromDB {
	int analogCnt;
	int eventCnt;
	
	private int runFrequency = 5; // in seconds
	static boolean booStatus = true;
	private String dataDir;
	private String destinationDir;
	private String stationDir;
	private File cfgFile;
	private File datFile;
	private File infFile;
	private static MySqlDatDAO mySqlDataDAO = null;
	private static ComtradeDataDAO mySqlComtradeDetailsDAO = null;
//	private StationInfo stationInfo;
	private String ctFileName; 
	private ResourceBundle bundle;
	private List<ComtradeDataDTO> lstComtradeDTO;
	static M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
	String stationName="Test Station";
	String recDevId = "USI_M9000";
	String comStdRevYear = "1999";
	int noOfSamplingRates = 1;
//	M9kCrossTrigger m9kCrossTrigger;
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	ScheduledFuture<?> scheduledTask = null;
//	private static String isCrossTriggerEnabled = "N"; 
	String filePrefix = "R";
	private java.text.SimpleDateFormat sdf;
	private int sourceColumn = 0;
	private int destColumn = 0;
	private String timeCode;
	private String architectureSet = null; 
	protected static final String ARCH_REMOTE_PUSH="remotePush";
	protected static final String ARCH_REMOTE_PULL="remotePull";
	protected static final String ARCH_LOCAL="local";
//	private String emailNotificationForFaults = M9kConstants.DISABLE;
//	private String emailWithFaultAttachment = M9kConstants.DISABLE;
//	private int emailFaultAttachmentSize = 2;
//	private String emailApplyBooleanLogicFilter = M9kConstants.DISABLE;
//	private int emailFaultsDailyLimit = 10;
	private static Map<String, Integer> mapTriggerEmailCount = null;  
//	private static String fileType;
	
	// START: 10-May-2021 - Email Report properties moved to database from M9k_COMTRADE properties
	private EmailSettingsDTO emailSettingsDTO;
	private EmailReportsSettingsDTO emailReportsSettingsDTO;


	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kCreateComtradeFilesFromDB.class);
	
	/**
	 * 
	 */
	public M9kCreateComtradeFilesFromDB() {

		bundle = ResourceBundle.getBundle("M9K_COMTRADE");
		try
		{
			dataDir = bundle.getString("dataDir");
			if (dataDir != null)
			{
				if (!dataDir.endsWith("/"))
				{
					dataDir = dataDir+"/";
				}
			}
			else
			{
				logger.warn("dataDir from M9K_COMTRADE is null. Using default path /data/m9k/faults");
				dataDir = "/data/m9k/faults/";
			}
		}
		catch (Exception e)
		{
			logger.warn("There was trouble in reading dataDir from M9K_COMTRADE. Using default path /data/m9k/faults",e);
			dataDir = "/data/m9k/faults/";
		}
		try
		{
			runFrequency = Integer.parseInt(bundle.getString("run-frequency"));
			logger.debug("Run frequency "+runFrequency);
		}
		catch (Exception e) {
			runFrequency = 5; // Default seconds
			logger.error("Cannot read runFrequency. Using default"+runFrequency,e);
		}
//		try
//		{
//			isCrossTriggerEnabled = bundle.getString("cross-trigger-station");
//		}
//		catch (Exception e) {
//			isCrossTriggerEnabled = "N";
//		}
		try
		{
			sourceColumn = Integer.parseInt(bundle.getString("sourceColumn"));
			logger.debug("Src column in cfg  "+sourceColumn);
		}
		catch (Exception e) {
			sourceColumn = 4; // Default seconds
			logger.error("Cannot read sourceColumn. Using default"+sourceColumn,e);
		}
		
		try
		{
			destColumn = Integer.parseInt(bundle.getString("destColumn"));
			logger.debug("Dest column in cfg  "+destColumn);
		}
		catch (Exception e) {
			destColumn = 2; // Default seconds
			logger.error("Cannot read destColumn. Using default"+destColumn,e);
		}
		
//		try
//		{
//			emailNotificationForFaults = bundle.getString("email-notify-faults");
//			logger.debug("emailNotificationForFaults column in cfg  "+emailNotificationForFaults);
//		}
//		catch (Exception e) {
//			emailNotificationForFaults = M9kConstants.DISABLE; // Default disabled
//			logger.error("Cannot read email-notify-faults. Using default"+destColumn,e);
//		}
//		
//
//		try
//		{
//			emailWithFaultAttachment = bundle.getString("email-faults-with-attachment");
//			logger.debug("emailWithFaultAttachment column in cfg  "+emailNotificationForFaults);
//		}
//		catch (Exception e) {
//			emailWithFaultAttachment = M9kConstants.DISABLE; // Default disabled
//			logger.error("Cannot read email-faults-with-attachment. Using default"+emailWithFaultAttachment,e);
//		}
//		
//
//		try
//		{
//			emailApplyBooleanLogicFilter = bundle.getString("email-fault-boolean-logic-filter");
//			logger.debug("email-fault-boolean-logic-filter column in cfg  "+emailApplyBooleanLogicFilter);
//		}
//		catch (Exception e) {
//			emailApplyBooleanLogicFilter = M9kConstants.DISABLE; // Default disabled
//			logger.error("Cannot read email-fault-boolean-logic-filter. Using default"+emailApplyBooleanLogicFilter,e);
//		}
//		
//		try
//		{
//			emailFaultAttachmentSize = Integer.parseInt(bundle.getString("email-attachement-size"));
//			logger.debug("email-attachement-size column in cfg  "+emailFaultAttachmentSize);
//		}
//		catch (Exception e) {
//			emailFaultAttachmentSize = 2; // Default 2MB
//			logger.error("Cannot read email-attachement-size. Using default"+emailFaultAttachmentSize,e);
//		}
//		
//		try
//		{
//			emailFaultsDailyLimit = Integer.parseInt(bundle.getString("email-faults-daily-limit"));
//			logger.debug("email-faults-daily-limit column in cfg  "+emailFaultsDailyLimit);
//		}
//		catch (Exception e) {
//			emailFaultsDailyLimit = 10; // Default 2MB
//			logger.error("Cannot read email-attachement-size. Using default"+emailFaultsDailyLimit,e);
//		}
		
		if (mySqlDataDAO == null)
		{
			mySqlDataDAO =new MySqlDatDAO();
		}
		if (mySqlComtradeDetailsDAO == null)
		{
			mySqlComtradeDetailsDAO = m9kDAOFactory.getComtradeDataDAO();
			
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

	public void scheduleCreateComtradeFiles(String architectureSet)
	{
		  final Runnable taskPerformer = new Runnable() {
              public void run() {
           		  createComtradeFilesForStation();
              }
          };
          setArchitectureSet(architectureSet);
          scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, 0, getRunFrequency(), TimeUnit.SECONDS);

	}

	public abstract  ComtradeDataDTO getAFaultRecordFromDB();
	public abstract  SimpleDateFormat  getDateFormat(String dateFormat);
	public void createComtradeFilesForStation ()
	{
//		int totalRecordsInserted = mySqlDataDAO.updateDatStagingWithRemote();
//		logger.debug("Total No of records inserted.."+totalRecordsInserted);
//		if (totalRecordsInserted > 0)
//		{
		M9kComtradeParser m9kComtradeParser; 
		long start;
		long end;
		start = System.currentTimeMillis();
		ComtradeDataDTO comtradeDataDTO = null;
		try
		{

			comtradeDataDTO  =  getAFaultRecordFromDB();
			sdf  = getDateFormat("dd/MM/yyyy,HH:mm:ss.SSS");
		}
		catch (Exception e) {
			logger.error("Unable to process a record. Database may be down.", e);
			comtradeDataDTO = null;
		}
		end = System.currentTimeMillis();
		
//		logger.debug("Total time to get comtrade record from staging... "+(end - start));
		
		if (comtradeDataDTO != null)
		{

			try {
				start = System.currentTimeMillis();
				createComtradeFiles(comtradeDataDTO);
				end = System.currentTimeMillis();
				
				logger.debug("Total time to create COMTRADE files... "+(end - start));
				start = System.currentTimeMillis();
				logger.debug("File name to parse "+(destinationDir+ctFileName+M9kConstants.DAT_FILE_EXTN));
				File datFile = new File(destinationDir+ctFileName+M9kConstants.DAT_FILE_EXTN);
				m9kComtradeParser = new M9kComtradeParser(datFile, true);
				end = System.currentTimeMillis();
				
				logger.debug("Total time to parse comtrade for events... "+(end - start));
				comtradeDataDTO.setActiveEvents(m9kComtradeParser.getActiveEventsId());
				calculateTimeLength(comtradeDataDTO);
//				logger.debug("About to insert into comtradeDetail");
				mySqlComtradeDetailsDAO.insertComtradeDetails(comtradeDataDTO);
//				logger.debug("Succesfully inserted into comtradeDetail");
				createComtradeInf(comtradeDataDTO);
				logger.info("Successfully created COMTRADE files for fault "+comtradeDataDTO.getFaultId());
				// START:06-June-2023 - Lightning correlation implementation
				SystemDAO systemDao = new MySqlSystemDAO();
				if (systemDao.isTableExists("m9000", "LcItems"))
				{
					updateLcIfEnabled(comtradeDataDTO);
				}
				else
				{
					logger.info("No LcItems table. Assuming, Lightning Correlation is not enabled");
				}
				// END:06-June-2023 - Lightning correlation implementation

//				if (isCrossTriggerEnabled.equalsIgnoreCase("Y")||isCrossTriggerEnabled.toUpperCase().startsWith("Y"))
//				{
//					
//				}
				try
				{
					logger.debug("About to get email settings..."+emailSettingsDTO);
						emailSettingsDTO = M9kReportUtil.getEmailSettingsDTO();
						logger.debug("Email settings "+emailSettingsDTO);
						emailReportsSettingsDTO = M9kReportUtil.getEmailReportsSettingsDTO();
						logger.debug("Email report settings "+emailReportsSettingsDTO);
	//					if (M9kUtils.isEmailNotificationEnabled() && getEmailNotificationForFaults().equalsIgnoreCase(M9kConstants.ENABLE))
						if (emailSettingsDTO.isEnableEmail() && emailReportsSettingsDTO.isEnableFaultEmail())
						{
							logger.debug("Email enabled boolean filter? "+emailReportsSettingsDTO.isEnableFaultsBooleanLogicFilter());
	//						if (getEmailApplyBooleanLogicFilter().equalsIgnoreCase(M9kConstants.DISABLE) || ((getEmailApplyBooleanLogicFilter().equalsIgnoreCase(M9kConstants.ENABLE))&&comtradeDataDTO.isFaultLogic()))
							if (!emailReportsSettingsDTO.isEnableFaultsBooleanLogicFilter() || (emailReportsSettingsDTO.isEnableFaultsBooleanLogicFilter()&&comtradeDataDTO.isFaultLogic()))
							{
								logger.debug("About to send email...");
								if (getMapTriggerEmailCount() == null || getMapTriggerEmailCount().get(M9kUtils.getCurrentDateWithoutTime()) == null)
								{
									mapTriggerEmailCount = new HashMap<String, Integer>(1);
									mapTriggerEmailCount.put(M9kUtils.getCurrentDateWithoutTime(), 1);
									sendEmail(comtradeDataDTO);
								}
								else 
								{
									int currentEmailCount = getMapTriggerEmailCount().get(M9kUtils.getCurrentDateWithoutTime());
	//								if (currentEmailCount < getEmailFaultsDailyLimit())
									if (currentEmailCount < emailReportsSettingsDTO.getFaultsEmailDailyLimit())
									{
										mapTriggerEmailCount.put(M9kUtils.getCurrentDateWithoutTime(), (currentEmailCount+1));
										sendEmail(comtradeDataDTO);
									}
									else
									{
										logger.warn("Total number of fault emails sent has exceed the daily limit. There will be no further emails for today ("+M9kUtils.getCurrentDateWithoutTime()+"). Emails for faults will resume from next day.");
									}
								}
									
							}
						}
				
				} catch (Exception e)
				{
					logger.error("Email couldn't be sent for fault id "+comtradeDataDTO .getFaultId(), e);
				}
				
			} catch (M9000Exception e) {
				logger.error("Comtrade file Creation failed for fault id "+comtradeDataDTO .getFaultId(), e);
				deleteCreatedFiles();
				mySqlDataDAO.moveToErrorDat(comtradeDataDTO);
			} 
			catch (Exception e) {
				e.printStackTrace();
				logger.error("Comtrade file Creation failed for fault id "+comtradeDataDTO .getFaultId(), e);
				deleteCreatedFiles();
				mySqlDataDAO.moveToErrorDat(comtradeDataDTO);
			}
			
			// START: 06-FEB-2017	Removes the staging data from MySQL table 'dat_staging' after the successful creation of the COMTRADE file 
			mySqlDataDAO.removeFaultFromDatStaging(comtradeDataDTO.getFaultId());
			// END: 06-FEB-2017
			
		}
		else
		{
//			logger.debug("No record to process");
		}
//		}
	}


	public void createComtradeFiles(ComtradeDataDTO comtradeDataDTO)
	{
//		System.out.println("About to create required files");
		long start;
		long end;
		File destFile = null;
		setStationName(comtradeDataDTO.getStationName());
		if (comtradeDataDTO.getRecordingDevId() != null && !comtradeDataDTO.getRecordingDevId().isEmpty())
		{
			setRecDevId(""+comtradeDataDTO.getRecordingDevId());
		}
//		stationDir = "/"+comtradeDataDTO.getStationId()+"-"+comtradeDataDTO.getStationName()+"/";
		stationDir = "/"+getStationFolderName(comtradeDataDTO)+"/";
		destinationDir = dataDir+stationDir;
		logger.debug("Destination dir "+destinationDir);
		destFile = new File(destinationDir);
		boolean booflag = destFile.mkdirs();
		logger.debug("Created dir "+destFile.getPath()+" ? "+booflag);
		booflag = destFile.setReadable(true, false);
		booflag = destFile.setWritable(true, false);
		logger.debug("set write permission to "+destFile.getPath()+" ? "+booflag);
		createFileName(comtradeDataDTO);
		createComtradeCfg(comtradeDataDTO);
		if (comtradeDataDTO.getType().equalsIgnoreCase(M9kConstants.ASCII) )
		{
			start = System.currentTimeMillis();
			createComtradeDat(comtradeDataDTO);
			end = System.currentTimeMillis();
			logger.debug("Total time to ASCII createComtradeDat... "+(end - start));
		}
		else
		{
//			System.out.println("Its Binary. Here we go! Invoking createBinaryComtradeDat()");
			createBinaryComtradeDat(comtradeDataDTO);
		}
	}
	
	private String getStationFolderName(ComtradeDataDTO comtradeDataDTO) {
		String folderName = "R"+String.format("%02d", comtradeDataDTO.getStationId())+"-"+comtradeDataDTO.getStationName();
		return folderName;
	}
	private void deleteCreatedFiles()
	{
		try
		{
			if (cfgFile.exists())
			{
				cfgFile.delete();
			}
			if (datFile.exists())
			{
				datFile.delete();
			}
			if (infFile.exists())
			{
				infFile.delete();
			}
		}catch (Exception e) {
			logger.warn("Deleting files on error condition failed ");
		}
	}
	public void createComtradeCfg(ComtradeDataDTO comtradeDataDTO)
	{
		BufferedWriter bw = null ;
		String microseconds;
		String date;
		StringTokenizer strTok = new StringTokenizer(comtradeDataDTO.getAnalogs().toString(), M9kConstants.NEWLINE);
		analogCnt = strTok.countTokens();
		strTok = new StringTokenizer(comtradeDataDTO.getEvents().toString(), M9kConstants.NEWLINE);
		eventCnt = strTok.countTokens();
		logger.debug("No of analog cnt..."+analogCnt);
		logger.debug("No of Events cnt..."+eventCnt);
		try {
			bw = new BufferedWriter(new FileWriter(cfgFile,false));
			bw.write(stationName+","+recDevId+","+comStdRevYear);
			bw.newLine();
			bw.write((analogCnt+eventCnt)+","+analogCnt+"A,"+eventCnt+"D");
			bw.newLine();
			logger.debug("About to write analogs details into CFG"+comtradeDataDTO.getAnalogs());
//			bw.write(comtradeDataDTO.getAnalogs().toString());
//			bw.write(comtradeDataDTO.getEvents().toString());
			if (getSourceColumn() == 0 && getDestColumn() == 0)
			{
				bw.write(comtradeDataDTO.getAnalogs().toString());
				bw.write(comtradeDataDTO.getEvents().toString());
			}
			else
			{
				bw.write(copyColumn(comtradeDataDTO.getAnalogs(), getSourceColumn(), getDestColumn()).toString());
				bw.write(copyColumn(comtradeDataDTO.getEvents(), getSourceColumn(), getDestColumn()).toString());
			}
			bw.write(""+(int)comtradeDataDTO.getLineFreq());
			bw.newLine();
			bw.write(""+noOfSamplingRates);
			bw.newLine();
			bw.write(""+(int)comtradeDataDTO.getSampleRate()+","+comtradeDataDTO.getSampleCnt());
			bw.newLine();
			microseconds = (""+comtradeDataDTO.getTsPrefault());
		    microseconds = microseconds.substring(microseconds.length()-3);
		 // Start: 31-Jan-2013 All date conversion are in M9kStationUtil class
//		    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy,HH:mm:ss.SSS"); 
//		    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			// End: 31-Jan-2013
		    date = sdf.format(new java.util.Date (comtradeDataDTO.getTsPrefault()/1000));
			bw.write(date+microseconds);
			bw.newLine();
			microseconds = (""+comtradeDataDTO.getTsTrigger());
		     microseconds = microseconds.substring(microseconds.length()-3);
		    date = sdf.format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
			bw.write(date+microseconds);
			bw.newLine();
			// file Type is Read from M9K_CONFIG not from database 
			bw.write(comtradeDataDTO.getType());
			bw.newLine();
			bw.write("1");
			bw.newLine();
		} catch (IOException e) {
//			e.printStackTrace();
			logger.error("IOException occured while creating cfg file ",e);
		} 
		catch (Exception e) {
			logger.error("Exception occured while creating cfg file ",e);
		}
		finally {
			try {
				if (bw != null)
				{
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.debug("End of createComtradeCfg method");
	}

	public void createComtradeDat(ComtradeDataDTO comtradeDataDTO)
	{
		BufferedWriter bw = null ;
		BufferedReader buffReader = null ;
		String strLine;
		try {
			bw = new BufferedWriter(new FileWriter(datFile,false));
			buffReader = new BufferedReader(comtradeDataDTO.getAsciiDataReader());
			while ((strLine = buffReader.readLine()) != null)
			{
				bw.write(strLine);
				bw.newLine();
			}
			buffReader.close();
			bw.close();
			logger.debug("Dat file created successfully");
		} catch (IOException e) {
			logger.error("IOException occured while creating cfg file ",e);
		}
		catch (Exception e) {
			logger.error("Exception occured while creating cfg file ",e);
		}
		finally {
			try {
				if (bw != null)
				{
					bw.close();
					bw = null;
				}
				if (buffReader != null)
				{
					buffReader.close();
					buffReader = null;
				}
				if (comtradeDataDTO.getAsciiDataReader()!= null)
				{
					comtradeDataDTO.getAsciiDataReader().close();
					comtradeDataDTO.setAsciiDataReader(null);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	

	public void createComtradeInf(ComtradeDataDTO comtradeDataDTO)
	{
		BufferedWriter bw = null ;
		try {
			bw = new BufferedWriter(new FileWriter(infFile));
			bw.write("[USI QuickSummary]");
			bw.newLine();
			bw.write("RemoteID="+comtradeDataDTO.getStationId());
			bw.newLine();
			bw.write("FaultID="+comtradeDataDTO.getFaultId());
			bw.newLine();
			// Start: 31-Jan-2013 All date conversion are in M9kStationUtil class
//			java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("MM/dd/yyyy,HH:mm:ss.SSS"); 
//			sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
			// End: 31-Jan-2013
			String microseconds = (""+comtradeDataDTO.getTsTrigger());
			microseconds = microseconds.substring(microseconds.length()-3);
			java.text.SimpleDateFormat sdfInf  = getDateFormat("MM/dd/yyyy,HH:mm:ss.SSS");
			String date = sdfInf.format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
			bw.write("Date="+date.substring(0,date.indexOf(",")));
			bw.newLine();
			bw.write("Time="+date.substring(date.indexOf(",")+1)+microseconds);
			bw.newLine();
			// START: 01-June-2016 - Time code logic used in naming convention is used here
//			bw.write("TimeCode="+bundle.getString("timeCode"));
			bw.write("TimeCode="+timeCode);
			// END: 01-June-2016
			bw.newLine();
			bw.write("Sync=Y");
			bw.newLine();
			bw.write("logF=N");
			bw.newLine();
			bw.write("LT=N");
			bw.newLine();
			// START: 08-June-2023 - Check for Comments to differentiate Test triggers
			if (comtradeDataDTO.getUserComments() == null)
			{
				bw.write("TestRun=N");
			}
			else
			{
				bw.write("TestRun=Y");
			}
			// STOP: 08-June-2023
			bw.newLine();
			StringBuffer digitalEvents = getDigitalEvents(comtradeDataDTO.getActiveEvents());
			if (digitalEvents!=null)
			{
				bw.write("DigitalEvents="+digitalEvents.toString()+","); // USI2002 Master needs a comma at the end of last event
			}
			bw.newLine();
			bw.write("Prefault="+comtradeDataDTO.getPreFault());
			bw.newLine();
			bw.write("Postfault="+comtradeDataDTO.getPostFault());
			bw.newLine();
			bw.write("Length="+comtradeDataDTO.getLength());
			bw.newLine();
			bw.write("FileExt=inf,cfg,dat");
			// START: 12-Aug-2019 - To include linegroups information in INF file
			if (comtradeDataDTO.getLineGroupDetailsForFaultLoc() != null && !comtradeDataDTO.getLineGroupDetailsForFaultLoc().isEmpty())
			{
				bw.newLine();
				bw.write(comtradeDataDTO.getLineGroupDetailsForFaultLoc());
			}
			// END: 12-Aug-2019
			if (comtradeDataDTO.getFaultLocationDetails() != null && !comtradeDataDTO.getFaultLocationDetails().isEmpty())
			{
				bw.newLine();
				// START: 17-Nov-2020 - Implemented for For lightning correlation 
//				bw.write("[Fault Location Details]");
//				bw.newLine();
				// END: 17-Nov-2020
				bw.write(getFaultLocationDetailsToWrite(comtradeDataDTO.getFaultLocationDetails()));
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally {
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}	

	/**
	 * If Lightning Correlation is enabled then LcItem is inserted in to database table after INF creation
	 * @param comtradeDataDTO
	 */
	private void updateLcIfEnabled(ComtradeDataDTO comtradeDataDTO) {
		logger.info("Entering updateLcIfEnabled lstFaultInfo ");
		java.text.SimpleDateFormat sdfTriggerTime = sdf  = getDateFormat("yyyy-MM-dd HH:mm:ss");
		LcItemDTO lcItemDto = new LcItemDTO();
		String triggerTime = sdfTriggerTime.format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
		lcItemDto.setTriggerTime(triggerTime);
		String comtradeFilePath = infFile.getPath().substring(0, infFile.getPath().indexOf("."));
		lcItemDto.setInfPath(comtradeFilePath);
		String associatedLineGroups = comtradeDataDTO.getLineGroups();
		// Convert comma separated values to string array and then to list to pass it to fetch fault info 
		Map<String, String> mapFaultInfo = new M9kStationComtradeUtil().getFaultInfoForLC(Arrays.asList(associatedLineGroups.split(",")));
		logger.info("In updateLcIfEnabled lstFaultInfo "+mapFaultInfo);
		if (mapFaultInfo != null)
		{
			LcItemsDAO lcItemsDAO = new MySqlLcItemsDAO();
			String lgName;
			String faultInfo;
			for (Iterator<String> iterator = mapFaultInfo.keySet().iterator(); iterator.hasNext();) {
				lgName = iterator.next();
				faultInfo = mapFaultInfo.get(lgName);
				if (faultInfo != null && !faultInfo.isEmpty())
				{
					lcItemDto.setFaultInfo(faultInfo);
					lcItemsDAO.insertLcItem(lcItemDto);
				}
				else
				{
					logger.info("Line group "+lgName+" did not have faultInfo for Lightning Correlation");
				}
			}
		}
		
	}

	private String getFaultLocationDetailsToWrite(String faultLocationDetails) {
		StringBuffer infFaultLocationDetails = new StringBuffer();
		String[] arrfaultDetails = faultLocationDetails.split(M9kConstants.NEWLINE);
		// START: 17-Nov-2020 - Implemented for For lightning correlation 
		int faultLocationDetailsCount =1;
		// END: 17-Nov-2020
		for (int i = 0; i < arrfaultDetails.length; i++) {
			
			if (arrfaultDetails[i].startsWith("Line Name: "))
			{
				// START: 17-Nov-2020 - Implemented for For lightning correlation 
				infFaultLocationDetails.append("[USI FaultLoc#"+faultLocationDetailsCount++ +"]");
				infFaultLocationDetails.append(M9kConstants.NEWLINE);
				// END: 17-Nov-2020
				infFaultLocationDetails.append("LineName=");
				infFaultLocationDetails.append(arrfaultDetails[i].substring((arrfaultDetails[i].indexOf("Line Name: ")+11)));
				infFaultLocationDetails.append(M9kConstants.NEWLINE);
			}
			else if (arrfaultDetails[i].startsWith("Type: "))
			{
				infFaultLocationDetails.append("FaultType=");
				infFaultLocationDetails.append(arrfaultDetails[i].substring((arrfaultDetails[i].indexOf("Type: ")+6)));
				infFaultLocationDetails.append(M9kConstants.NEWLINE);				
			}
			else if (arrfaultDetails[i].startsWith("Duration: "))
			{
				infFaultLocationDetails.append("FaultCycles=");
				infFaultLocationDetails.append(arrfaultDetails[i].substring((arrfaultDetails[i].indexOf("Duration: ")+10)));
				infFaultLocationDetails.append(M9kConstants.NEWLINE);				
			}
			else if (arrfaultDetails[i].startsWith("Distance: "))
			{
				infFaultLocationDetails.append("FaultMiles=");
				infFaultLocationDetails.append(arrfaultDetails[i].substring((arrfaultDetails[i].indexOf("Distance: ")+10)));
				infFaultLocationDetails.append(M9kConstants.NEWLINE);				
			}
		}
		return infFaultLocationDetails.toString();
	}

	private StringBuffer getDigitalEvents(String activeEvents) {
		String[] arrEvents = null;
		StringBuffer digitalEvents = null;
		try
		{
		if (activeEvents != null && !activeEvents.isEmpty())
		{
			arrEvents = activeEvents.split(M9kConstants.NEWLINE);
		}
		if (arrEvents != null)
		{
			for (int i = 0; i < arrEvents.length; i++) {
				if (digitalEvents == null)
				{
					digitalEvents = new StringBuffer();
					digitalEvents.append(arrEvents[i].substring(0,arrEvents[i].indexOf("-")).trim());
				}
				else
				{
					digitalEvents.append(","+arrEvents[i].substring(0,arrEvents[i].indexOf("-")).trim());
				}
			}
		}
		}
		catch (Exception e) {
			logger.debug("Error in getting digital events ",e);
		}
		return digitalEvents;
	}

	private void createFileName(ComtradeDataDTO comtradeDataDTO)
	{
//		String path = M9kStationCreateComtradeFilesFromDB.class.getProtectionDomain().getCodeSource().
//		   getLocation().toString().substring(6);
//		logger.debug("Chnaged Current path... "+path);
//		logger.debug("Resource Bundle: "+bundle);
//			ctConfig = new PropertiesConfiguration("M9K_COMTRADE.properties");
//			ctConfig = new PropertiesConfiguration(cl.getResource("M9K_COMTRADE.properties"));
//		logger.debug("Base path: "+ctConfig.getBasePath());
//		logger.debug("Data dir... "+ctConfig.getString("dataDir"));

//		stationInfo = M9kXMLUtils.getStationDetails("18", 23);
		String microseconds = (""+comtradeDataDTO.getTsTrigger());
		microseconds = microseconds.substring(microseconds.length()-3);
//	    String date = new java.text.SimpleDateFormat("yyMMdd,HHmmssSS").format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
		String date =getDateFormat("yyMMdd,HHmmssSSS").format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
//	    filePrefix = "R";
	    filePrefix = "R"+String.format("%02d", comtradeDataDTO.getStationId());
//	    if (comtradeDataDTO.getStationId() < 10)
//	    {
//	    	filePrefix += "0"+comtradeDataDTO.getStationId();
//	    }
//	    else
//	    {
//	    	filePrefix += comtradeDataDTO.getStationId();
//	    }
	    filePrefix+="F"; // To match the old model 2002 statndard
		try
		{
			// Start: Modified the timezone calculation logic 02-Dec-2013
//			TimeZone tz = Calendar.getInstance().getTimeZone();
//			timeCode = (tz.getRawOffset()/1000/3600)+"t";
			Calendar cal = Calendar.getInstance();
			timeCode = (cal.get(Calendar.ZONE_OFFSET)+cal.get(Calendar.DST_OFFSET))/1000/3600 +"t";
			// End
		}
		catch (Exception e) {
			logger.error("Cannot get TimeZone and rawoffset for timecode. Attempting to read from M9K_COMTRADE",e);
			try
			{
				timeCode = bundle.getString("timeCode");
			}
			catch (Exception e1) {
				logger.error("Cannot get TimeZone from M9K_COMTRADE either. Using default -5t",e1);
				timeCode = "-5t";
			}
		}

//	    ctFileName = filePrefix+comtradeDataDTO.getFaultId()+"_"+date + ","+bundle.getString("timeCode")+","+stationName+","+recDevId+","+bundle.getString("company");
	    // START: 20-May-2016 Changed to IEEE C37.232-2011 COMNAME naming convention 
//	    ctFileName = filePrefix+comtradeDataDTO.getFaultId()+"_"+date + ","+timeCode+","+stationName+","+recDevId+","+bundle.getString("company");
	    ctFileName = date + ","+timeCode+","+stationName+","+recDevId+","+bundle.getString("company")+","+filePrefix+comtradeDataDTO.getFaultId();
	    // END: 20-May-2016
	    ctFileName = ctFileName.replace("/", "_");
	    ctFileName = ctFileName.replace("\\", "_");
	    comtradeDataDTO.setFileName(stationDir+ctFileName);
	    
	    cfgFile = new File (destinationDir+ctFileName+M9kConstants.CFG_FILE_EXTN);
	    datFile = new File (destinationDir+ctFileName+M9kConstants.DAT_FILE_EXTN);
	    infFile = new File (destinationDir+ctFileName+M9kConstants.INF_FILE_EXTN);
	}

	//TODO Yet to implement binary version
	public void createBinaryComtradeDat(ComtradeDataDTO comtradeDataDTO)
	{
		DataOutputStream dout = null ;
		if (comtradeDataDTO.getBinaryDataSteam() == null)
		{
			logger.debug("About to convert ASCII to Binary");
			short shortAnalog;
			StringBuffer eventString;
			int eventHexString;
			int i = 0;
			int j = 0;
			int k = 0;
			Integer dummy = 0x8000;
			String[] lines = null;
			try {
				dout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(datFile)));
				lines = comtradeDataDTO.getData().toString().split("\n");
				for (k = 0; k < lines.length; k++) {
					String[] dataArray = lines[k].split(",");
					dout.writeInt(Integer.reverseBytes(Integer.parseInt(dataArray[0])));
					dout.writeInt(Integer.reverseBytes(Integer.parseInt(dataArray[1])));
					for (i = 2; i < analogCnt+2; i++) {
						if (!dataArray[i].equals("99999"))
						{
							shortAnalog = Short.parseShort(dataArray[i]);
						}
						else
						{
							shortAnalog = dummy.shortValue();
						}
						dout.writeShort(Short.reverseBytes(shortAnalog));
					}
					
					for (i = (dataArray.length-1); i >= (analogCnt+2); i--) {
						eventString = new StringBuffer();
						if (k==0)
						{
							System.out.print("i-"+i);
						}
						for (j = i;j>i-16;j--)
						{
							if (j >= (analogCnt+2))
							{
								eventString.append(dataArray[j]);
							}
							else
							{
								eventString.append("0");
							}
						}
						eventHexString = convertEventToHex(eventString.toString());
						dout.writeShort(eventHexString);
						
						i=j+1;
					}
					
				}
	//			dout.write(comtradeDataDTO.getData().toString().getBytes());
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				ioe.printStackTrace();
				logger.error("IO Exception occured in createBinaryComtradeDat",ioe);
			} 
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Exception occured in createBinaryComtradeDat "+"LIne no "+k+" -> "+lines[k], e);
	
			}
			finally {
				try {
					dout.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else
		{
			try {
				logger.debug("Reading binary from BLOB"+comtradeDataDTO.getBinaryDataSteam().available());
				dout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(datFile, false)));
				byte[] buf = new byte[3000];
                int read = 0;
                while ((read = comtradeDataDTO.getBinaryDataSteam().read(buf)) > 0) {
                    dout.write(buf, 0, read);
                }
			} catch (FileNotFoundException e) {
				logger.error("Exception occured in createBinaryComtradeDat",e);
			} catch (IOException e) {
				logger.error("IOException occured in createBinaryComtradeDat ",e);
			}
			finally {
				try {
					if (dout != null)
					{
						dout.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}	

	private int convertEventToHex(String events)
	{
		int hexVal;
		long val = Long.parseLong(events,2);
		String zeros = "0000";
		String hexString = Long.toHexString(Long.reverseBytes(val));
		if (hexString.length() > 4)
		{
			hexString = hexString.substring(0,4);
		}
		else
		{
			String leadZero = zeros.substring(0,4-hexString.length());
			hexString=leadZero.concat(hexString);			
		}
		hexVal = Integer.parseInt(hexString,16);
		return hexVal;
	}
	public void createComtradeFilesForContinuousData()
	{
		
	}

	/**
	 * @return the lstComtradeDTO
	 */
	public List<ComtradeDataDTO> getLstComtradeDTO() {
		return lstComtradeDTO;
	}

	/**
	 * @param lstComtradeDTO the lstComtradeDTO to set
	 */
	public void setLstComtradeDTO(List<ComtradeDataDTO> lstComtradeDTO) {
		this.lstComtradeDTO = lstComtradeDTO;
	}
	
	private void calculateTimeLength(ComtradeDataDTO comtradeDataDTO)
	{
		long tsPrefault = comtradeDataDTO.getTsPrefault();
		long tsTrigger = comtradeDataDTO.getTsTrigger();
		int sampleCnt = comtradeDataDTO.getSampleCnt();
		double sampleRate = comtradeDataDTO.getSampleRate();
		long lastSampleTime = (long)(tsPrefault + (((sampleCnt-1)/sampleRate)*1000000));
		logger.debug("lastSampleTime..."+lastSampleTime);
		long preFault = (tsTrigger - tsPrefault)/1000;
		long postFault = (lastSampleTime - tsTrigger)/1000;
		double length = (lastSampleTime - tsPrefault)/1000;
		logger.debug("Pre fault..."+preFault);
		logger.debug("Post fault..."+postFault);
		logger.debug("Length..."+length);
		comtradeDataDTO.setPreFault(preFault);
		comtradeDataDTO.setPostFault(postFault);
		comtradeDataDTO.setLength(length);
	}

//	@Override
//	public void execute(JobExecutionContext arg0) throws JobExecutionException {
//		M9kStationCreateComtradeFilesFromDB testComtradeData = new M9kStationCreateComtradeFilesFromDB();
//		long startTime;
//		long endTime;
//		startTime = System.currentTimeMillis();
//		testComtradeData.createComtradeFilesForStation("18");
////		testComtradeData.closeAllConnections();
//		endTime = System.currentTimeMillis();
//		
//
//		logger.debug("Total Time taken for complete process..."+(endTime-startTime));
//		
//	}
	
//	public void doCrossTrigger(ComtradeDataDTO comtradeDataDTO)
//	{
//		int sampleCnt = comtradeDataDTO.getSampleCnt();
//		double sampleRate = comtradeDataDTO.getSampleRate();
//		long tsPrefault =  comtradeDataDTO.getTsPrefault();
//		
//		long lastSampleTime = (long)(tsPrefault + (((sampleCnt-1)/sampleRate)*1000000));
//		
//		logger.debug("First Sample Time"+tsPrefault+" Last Sample Time..."+lastSampleTime);
//		
//		String triggerRequest = "DFR-ID- "+comtradeDataDTO.getDfrId()+"-"+tsPrefault+"-"+lastSampleTime;
//		logger.debug("Trigger Request: "+triggerRequest);
//		try
//		{
//			m9kCrossTrigger = new M9kCrossTrigger();
//			m9kCrossTrigger.setTriggerRequest(triggerRequest);
//			m9kCrossTrigger.sendTrigger("195.1.1.73", 9977);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	public void closeAllConnections()
	{
		M9kMySqlDatabase.getInstance().closeConnection();
	}

	public int getRunFrequency() {
		return runFrequency;
	}

	public void setRunFrequency(int runFrequency) {
		this.runFrequency = runFrequency;
	}

	public String getDestinationDir() {
		return destinationDir;
	}

	public void setDestinationDir(String destinationDir) {
		this.destinationDir = destinationDir;
	}

	public String getDataDir() {
		return dataDir;
	}

	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	public String getRecDevId() {
		return recDevId;
	}

	public void setRecDevId(String recDevId) {
		this.recDevId = recDevId;
	}

	public String getStationDir() {
		return stationDir;
	}

	public void setStationDir(String stationDir) {
		this.stationDir = stationDir;
	}

	public File getCfgFile() {
		return cfgFile;
	}

	public void setCfgFile(File cfgFile) {
		this.cfgFile = cfgFile;
	}

	public File getDatFile() {
		return datFile;
	}

	public void setDatFile(File datFile) {
		this.datFile = datFile;
	}

	public File getInfFile() {
		return infFile;
	}

	public void setInfFile(File infFile) {
		this.infFile = infFile;
	}
	
	private StringBuffer copyColumn(StringBuffer analogInfo, int colSource, int colDest)
	{
		StringBuffer newStrAnalogs = new StringBuffer(100);
		String cols[];
		
		try
		{
		String[] lines = analogInfo.toString().split(M9kConstants.NEWLINE);
		for (int i = 0; i < lines.length; i++) {
			cols = lines[i].split(",");
			for (int j = 0; j < cols.length; j++) {
				if (j != (colDest - 1))
				{
					if (j <(cols.length)-1)
					{
						newStrAnalogs.append(cols[j]+",");
					}
					else
					{
						newStrAnalogs.append(cols[j]);
						newStrAnalogs.append(M9kConstants.NEWLINE);
					}
				}
				else
				{
					if (j <(cols.length)-1)
					{
						newStrAnalogs.append(cols[colSource-1]+",");
					}
					else
					{
						newStrAnalogs.append(cols[colSource-1]);
						newStrAnalogs.append(M9kConstants.NEWLINE);
					}
				}
			}
		}
		}
		catch (Exception e) {
			logger.error("Some exception during copying columns. returning default ",e);
			newStrAnalogs = analogInfo;
		}
		return newStrAnalogs;
	}

	public int getSourceColumn() {
		return sourceColumn;
	}

	public int getDestColumn() {
		return destColumn;
	}

	public String getArchitectureSet() {
		return architectureSet;
	}

	public void setArchitectureSet(String architectureSet) {
		this.architectureSet = architectureSet;
	}
	
	// Invoked from M9kComtradeDataPushTask to create a fault locally after it gets pushed to the master
	public void createComtradeFileForFaultId(int faultId )
	{
//		int totalRecordsInserted = mySqlDataDAO.updateDatStagingWithRemote();
//		logger.debug("Total No of records inserted.."+totalRecordsInserted);
//		if (totalRecordsInserted > 0)
//		{
		M9kComtradeParser m9kComtradeParser; 
		ComtradeDataDTO comtradeDataDTO = null;
		long start;
		long end;
		// START: 21-Feb-2020 - Skip if the fault files are already created
		if (isFaultAlreadyCreated(faultId))
		{
			logger.debug("Fault Id "+faultId+" is already available. Not creating again");
			return;
		}
		// END: 21-Feb-2020
		try
		{

			comtradeDataDTO  =  getFaultRecordForFaultId(faultId);
			// START: 12-Nov-2020 - Locally created fault after pushing the faults to remote master should include line group details
			if (comtradeDataDTO != null)
			{
				comtradeDataDTO.setLineGroupDetailsForFaultLoc(new M9kStationComtradeUtil().getLineGroupDetailsForFaultLoc());
			}
			// END: 12-Nov-2020
			sdf  = getDateFormat("dd/MM/yyyy,HH:mm:ss.SSS");
		}
		catch (Exception e) {
			logger.error("Unable to process a record. Database may be down.", e);
			comtradeDataDTO = null;
		}		
		if (comtradeDataDTO != null)
		{
			
			try {
				start = System.currentTimeMillis();
				createComtradeFiles(comtradeDataDTO);
				end = System.currentTimeMillis();
				
				logger.debug("Total time to create COMTRADE files... "+(end - start));
				start = System.currentTimeMillis();
				logger.debug("File name to parse "+(destinationDir+ctFileName+M9kConstants.DAT_FILE_EXTN));
				File datFile = new File(destinationDir+ctFileName+M9kConstants.DAT_FILE_EXTN);
				m9kComtradeParser = new M9kComtradeParser(datFile, true);
				end = System.currentTimeMillis();
				
				logger.debug("Total time to parse comtrade for events... "+(end - start));
				comtradeDataDTO.setActiveEvents(m9kComtradeParser.getActiveEventsId());
				calculateTimeLength(comtradeDataDTO);
//				logger.debug("About to insert into comtradeDetail");
				mySqlComtradeDetailsDAO.insertComtradeDetails(comtradeDataDTO);
//				logger.debug("Succesfully inserted into comtradeDetail");
				createComtradeInf(comtradeDataDTO);
				// START:06-June-2023 - Lightning correlation implementation
				updateLcIfEnabled(comtradeDataDTO);
				// END:06-June-2023 - Lightning correlation implementation
				logger.info("Successfully created COMTRADE files for fault "+comtradeDataDTO.getFaultId());
//				if (isCrossTriggerEnabled.equalsIgnoreCase("Y")||isCrossTriggerEnabled.toUpperCase().startsWith("Y"))
//				{
//					
//				}

			} catch (M9000Exception e) {
				logger.error("Comtrade file Creation failed for fault id "+comtradeDataDTO .getFaultId(), e);
//				deleteCreatedFiles();
				mySqlDataDAO.moveToErrorDat(comtradeDataDTO);
			} 
			catch (Exception e) {
				e.printStackTrace();
				logger.error("Comtrade file Creation failed for fault id "+comtradeDataDTO .getFaultId(), e);
//				deleteCreatedFiles();
				mySqlDataDAO.moveToErrorDat(comtradeDataDTO);
			}

			// START: 11-Feb-2020 - To avoid deleting before pushing - For local arch with remote push
				// START: 06-FEB-2017	Removes the staging data from MySQL table 'dat_staging' after the successful creation of the COMTRADE file 
//				mySqlDataDAO.removeFaultFromDatStaging(comtradeDataDTO.getFaultId());
				// END: 06-FEB-2017
			// END: 11-Feb-2020
			
		}
		else
		{
//			logger.debug("No record to process");
		}
//		}
	}

	
	private boolean isFaultAlreadyCreated(int faultId) {
		boolean booFaultAlreadyCreated = false;
		try {
			ComtradeDataDTO comtradeDataDAO = mySqlComtradeDetailsDAO.getLocalFaultById(M9kStationDBUtil.getStationDetails().getSystemStationId(), faultId);
			if (comtradeDataDAO != null)
			{
				booFaultAlreadyCreated = true;
			}
		} catch (M9000Exception e) {
			logger.error("Error in verifying whether the faults exist ",e);
		}
		return booFaultAlreadyCreated;
	}

	private ComtradeDataDTO getFaultRecordForFaultId(int faultId)
	{
		ComtradeDataDTO comtradeDataDTO = null;
		try
		{
			comtradeDataDTO = mySqlDataDAO.getLocalFaultRecordForFaultId(faultId);
		}
		catch (Exception e) {
			logger.error("Unable to process a record. Database may be down.", e);
			comtradeDataDTO = null;
		}
		return comtradeDataDTO;
	}

	private void sendEmail(ComtradeDataDTO comtradeDataDTO) 
	{
		File filetoBeEmailed = null;
		try {
			filetoBeEmailed = zipAllFiles();
			long sizeInBytes = filetoBeEmailed.length();
			long sizeInMb = sizeInBytes / (1024 * 1024);
			StringBuffer emailText = new StringBuffer("We have a new fault in the station "+comtradeDataDTO.getStationTitle());
			emailText.append(M9kConstants.NEWLINE);
			emailText.append(comtradeDataDTO.getFaultDetailsSummary());
			String emailSubject = M9kSeverityLevels.INFO.name()+": Station: "+stationName+" Date: "+comtradeDataDTO.getDisplayTime()+" FaultId:  "+comtradeDataDTO.getFaultId();
			logger.debug("About to send email with sub "+emailSubject+" body "+emailText+" sizeInMb "+sizeInMb+" size from db "+emailReportsSettingsDTO.getFaultEmailAttachementSizeLimit());
//			if (sizeInMb <= getEmailFaultAttachmentSize())
			if (sizeInMb <= Integer.parseInt(emailReportsSettingsDTO.getFaultEmailAttachementSizeLimit()))
			{
				SendMailUSI.sendEmailWithAttachment(emailSubject,emailText.toString(),filetoBeEmailed);
			}
			else
			{
				emailText.append(M9kConstants.NEWLINE);
				emailText.append("Fault files are not attached with this email as the file size exceeds the user set limit of "+emailReportsSettingsDTO.getFaultEmailAttachementSizeLimit()+" MB. Actual fault file size is "+sizeInMb+" MB."); 
				SendMailUSI.sendEmail(emailSubject, emailText.toString());
			}
		} catch (M9000Exception e) {
			logger.error("Error in sending an email ",e);
		}
		finally {
			try
			{
				if (filetoBeEmailed != null)
				{
					filetoBeEmailed.delete();
				}
			}
			catch (Exception e) {
				logger.error("Unable to delete the zip file "+filetoBeEmailed.getName()+" after email...",e);
			}
					
		}
		
	}
	private File zipAllFiles() throws M9000Exception
	{
		byte[] buffer = new byte[1024];
		File zipFile = null;
		 
    	try{
    		String destZipfilePath = destinationDir+"compressed"+File.separator+"";
    		String sourceFilePrefix = destinationDir+ctFileName;
    		logger.debug("dest zip path "+destZipfilePath+" sourceFilePrefix "+sourceFilePrefix);
    		File destDir = new File(destZipfilePath);
    		if (!destDir.exists())
    		{
    			destDir.mkdirs();
    		}
    		destDir.setWritable(true, false);
    		destDir.setExecutable(true,false);
    		destDir.setReadable(true, false);

    		zipFile = new File(destDir,(ctFileName+".zip"));
    		FileOutputStream fos = new FileOutputStream(zipFile);
    		ZipOutputStream zos = new ZipOutputStream(fos);
    		
    		ZipEntry ze= new ZipEntry(ctFileName+".dat");
    		zos.putNextEntry(ze);
    		FileInputStream in = new FileInputStream(new File(sourceFilePrefix+".dat"));
 
    		int len;
    		while ((len = in.read(buffer)) > 0) {
    			zos.write(buffer, 0, len);
    		}
 
    		in.close();
    		
    		ze= new ZipEntry(ctFileName+".cfg");
    		zos.putNextEntry(ze);
    		in = new FileInputStream(new File(sourceFilePrefix+".cfg"));
 
    		while ((len = in.read(buffer)) > 0) {
    			zos.write(buffer, 0, len);
    		}
 
    		in.close();
    		
    		File infFile = new File(sourceFilePrefix+".inf");
    		if (infFile.exists())
    		{
				ze= new ZipEntry(ctFileName+".inf");
	    		zos.putNextEntry(ze);
	    		in = new FileInputStream(infFile);
	 
	    		while ((len = in.read(buffer)) > 0) {
	    			zos.write(buffer, 0, len);
	    		}
	 
	    		in.close();
    		}
    		
    		zos.closeEntry();
 
    		//remember close it
    		zos.close();
 
    		logger.debug("Done creating zip file");
 
    	}catch(IOException ex){
    	   logger.error("Error in creating zip file ",ex);
    	   zipFile = null;
    	}
    	catch(Exception ex){
    		logger.error("Error in creating zip file ",ex);
    		zipFile = null;
     	}
    	return zipFile;
    }

//	public String getEmailNotificationForFaults() {
//		return emailNotificationForFaults;
//	}
//
//	public void setEmailNotificationForFaults(String emailNotificationForFaults) {
//		this.emailNotificationForFaults = emailNotificationForFaults;
//	}
//
//	public String getEmailWithFaultAttachment() {
//		return emailWithFaultAttachment;
//	}
//
//	public void setEmailWithFaultAttachment(String emailWithFaultAttachment) {
//		this.emailWithFaultAttachment = emailWithFaultAttachment;
//	}
//
//	public int getEmailFaultAttachmentSize() {
//		return emailFaultAttachmentSize;
//	}
//
//	public void setEmailFaultAttachmentSize(int emailFaultAttachmentSize) {
//		this.emailFaultAttachmentSize = emailFaultAttachmentSize;
//	}
//
//	public String getEmailApplyBooleanLogicFilter() {
//		return emailApplyBooleanLogicFilter;
//	}
//
//	public void setEmailApplyBooleanLogicFilter(String emailApplyBooleanLogicFilter) {
//		this.emailApplyBooleanLogicFilter = emailApplyBooleanLogicFilter;
//	}
//
//	public int getEmailFaultsDailyLimit() {
//		return emailFaultsDailyLimit;
//	}
//
//	public void setEmailFaultsDailyLimit(int emailFaultsDailyLimit) {
//		this.emailFaultsDailyLimit = emailFaultsDailyLimit;
//	}

	public static Map<String, Integer> getMapTriggerEmailCount() {
		return mapTriggerEmailCount;
	}

	public static void setMapTriggerEmailCount(Map<String, Integer> mapTriggerEmailCount) {
		M9kCreateComtradeFilesFromDB.mapTriggerEmailCount = mapTriggerEmailCount;
	}

}
