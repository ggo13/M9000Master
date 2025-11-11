package com.usi.m9000.station.threads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.ComtradeContAnalogDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.station.Exception.M9kThreadExceptionHandler;
import com.usi.m9000.station.commands.M9kCreateContinuousComtradeFilesFromDB;
import com.usi.m9000.station.util.M9kBackupUtil;
import com.usi.m9000.station.util.M9kStationComtradeUtil;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.util.M9kConstants;


public class M9kExportContAnalogComtrades {
	private int runFrequency = 1; // in Hour
	static boolean booStatus = true;
	private Connection mysqlConn;
	private long contStartTime = 0;
	private long contStopTime = 0;
	private long lastExportedTime = 0;
	private int exportAnalogBufferTime;
	List<DfrDTO> lstDfrs = null;
	String sqlQuery;
	private StationDTO stationDetails;
	int finalMergedSampleCnt = 0;
	List<Integer> lstMissingExportsId = new ArrayList<Integer>();
	List<Integer> lstAnalogExportsId = new ArrayList<Integer>();
	
	M9kCreateContinuousComtradeFilesFromDB comtradeData;
	private long oldestDataStartHour = 0;
	private int sizeInMinutes = 0; // COMTRADE Size to be created
	private int contComtradeNoOfDaysToRetain = 1; // COMTRADE Size to be created
	private String destExportsDirectory;
	 private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	 ScheduledFuture<?> scheduledTask = null;
	private ExecutorService threadHandlerExecutor;
	private String destFileName;
	private long singleSampleTime;
	private int errorLogCount = 0;
	M9kDAOFactory m9kDAOFactory;
	ComtradeContAnalogDAO mySqlComtradeContAnalogDAO;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kExportContAnalogComtrades.class);
	public M9kExportContAnalogComtrades(long startTime, int sizeInMinutes, String destExportsDirectory, int contComtradeNoOfDaysToRetain)
	{
		this.sizeInMinutes = sizeInMinutes;
		this.destExportsDirectory = destExportsDirectory;
		this.contComtradeNoOfDaysToRetain = contComtradeNoOfDaysToRetain;
		threadHandlerExecutor = Executors.newSingleThreadExecutor();
		errorLogCount = 0;
		
		try {
			logger.debug("AutoExport: Entered M9kExportContAnalogComtrades constructor with start time "+startTime+" COMTRADE size in Minutes "+sizeInMinutes);
			m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
			mySqlComtradeContAnalogDAO = m9kDAOFactory.getComtradeContinuousAnalogDAO();

			singleSampleTime = (1/getStationDetails().getSystemLongTermSampleRate())*1000000;
			logger.debug("Auto Export Osc: Single Sample time "+singleSampleTime);
			contStartTime = startTime;
			contStopTime = contStartTime + ((sizeInMinutes*60) * 1000000)- singleSampleTime;

			// START: 08-Dec-2023 - Instead of start of the day start from first available time of the current day
//			startOfDay = M9kBackupUtil.getStartTimeOFTheDay();
			oldestDataStartHour = M9kBackupUtil.getFirstAvailableHourForContData(M9kConstants.CONTINUOUS_ANALOG_DATA);
			// END: 08-Dec-2023 - Instead of start of the day start from first available time of the current day
			lastExportedTime = calculateContStartTime();
			logger.debug("Osc: contStartTime "+contStartTime+"lastExportedTime "+lastExportedTime+" (contStartTime - singleSampleTime) "+(contStartTime - singleSampleTime));
			if (lastExportedTime != -1 && (lastExportedTime < (contStartTime - singleSampleTime) || lastExportedTime < contStartTime))
			{
				logger.debug("LastExportedTime is less so starting time is replaced with it. LastExportedTime "+lastExportedTime+" current Start Time "+contStartTime);
				if (lastExportedTime == oldestDataStartHour)
				{
					contStartTime = lastExportedTime ;
				}
				else
				{
					contStartTime = lastExportedTime + singleSampleTime;
				}
				contStopTime = contStartTime + ((sizeInMinutes*60) * 1000000)- singleSampleTime;
			}
			
			logger.debug("AutoExport: M9kContAnalogFromDb: After prefault Time start time "+contStartTime+" stop time "+contStopTime+" Last exported time "+lastExportedTime);		
			exportAnalogBufferTime = M9kStationXMLUtil.getExportAnalogBufferTime();
			lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
			Collections.sort(lstDfrs, new Comparator<DfrDTO>() {

				@Override
				public int compare(DfrDTO o1, DfrDTO o2) {
//					return ((o1.getDfrId() < o2.getDfrId())?0:1);
					return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
				}
			});
			comtradeData = new M9kCreateContinuousComtradeFilesFromDB(getStationDetails().getSystemStationId());
//			mapAnalogChnlStatus = getChannelsExportStatus();
		} catch (Exception e) {
			logger.error("Unable to process ContAnalog Data ",e);
			return;
		}
	}

	public void scheduleCreateContComtrades()
	{
	  // START:12-DEC-2023 - Instantly run the first run and then schedule the next run every start of the hour 
  	  try
  	  {
  		  logger.debug("About to delete old folder");
  		  removeOldContComtrades();
  	  }
  	  catch (Exception e) {
			logger.error("Encounted error while removing old comtrade files ",e);
  	  }

  	  try
  	  {
  		  exportContAnalogComtrades();
  	  }
  	  catch (Exception e) {
			logger.error("Encounted error while creating the comtrade files around the time "+contStartTime+". Reason may be issue with database connection",e);
			logger.info("It'll attempt to run next start of hour");
  	  }
	  final Runnable taskPerformer = new Runnable() {
              public void run() {
//            	  try
//            	  {
//            		  logger.debug("About to delete old folder");
//            		  removeOldContComtrades();
//            	  }
//            	  catch (Exception e) {
//					logger.error("Encounted error while removing old comtrade files ",e);
//            	  }

            	  try
            	  {
            		  exportContAnalogComtrades();
                	  try
                	  {
                		  logger.debug("About to delete old folder");
                		  removeOldContComtrades();
                	  }
                	  catch (Exception e) {
    					logger.error("Encounted error while removing old comtrade files ",e);
                	  }

            	  }
            	  catch (Exception e) {
					logger.error("Encounted error while creating the comtrade files around the time "+contStartTime+". Reason may be issue with database connection.",e);
					logger.info("It'll attempt to run next start of hour");
            	  }

              }
          };
          // START: 23-Jan-2023 - Schedule the task to run at start of the hour. It fixes the issue of time drifting caused due to scheduleWithFixedDelay.
//          scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer,  M9kBackupUtil.calculateSecondsUntilNextHour(), getRunFrequency()*3600, TimeUnit.SECONDS);
          scheduledTask = scheduler.scheduleAtFixedRate(taskPerformer,  M9kBackupUtil.calculateSecondsUntilNextHour(), getRunFrequency()*3600, TimeUnit.SECONDS);
          // END: 23-Jan-2023
          threadHandlerExecutor.execute(new M9kThreadExceptionHandler(scheduledTask));
	}
	
	private void exportContAnalogComtrades() throws Exception
	{
		long start = System.currentTimeMillis();
		long end;
	
		try {
//			long actualContStopTime = contStartTime + 3599000000L; // end time of hr:59:59 so that next hour starts from 00
			long actualContStopTime = M9kBackupUtil.getCurrentWholeHourTime(); // end time of hr:59:59 so that next hour starts from 00
			logger.debug("AutoExport: Current whole hour time/actualContStopTime " + actualContStopTime);
			logger.debug("AutoExport: Start Time "+contStartTime+" stop time "+contStopTime+" Last exported time "+lastExportedTime);
			// START: 12-Dec-2023 - Check if stop Time is already exported until last hour
//			if (contStartTime < lastExportedTime)
			if (actualContStopTime <= lastExportedTime)
			// END: 12-Dec-2023
			{
				logger.debug("Already exported until last hour. Skipping this run.");
				contStartTime = lastExportedTime + singleSampleTime;
				contStopTime = contStartTime + ((sizeInMinutes*60) * 1000000)- singleSampleTime;
			}
			else
			{
				do {
					destFileName = createFileName();
					try
					{
						processMergeAndCreateComtrade();
					}
					catch (Exception e)
					{
						logger.error("Unable to get database connection.",e);
						throw e;
					}
					contStartTime = contStopTime + singleSampleTime;
					contStopTime = contStartTime + ((sizeInMinutes*60) * 1000000)- singleSampleTime;
					logger.debug("AutoExport: contStopTime "+contStopTime+" actualContStopTime "+actualContStopTime);
				} while ( contStopTime <= actualContStopTime);
				if (contStartTime < actualContStopTime) //// If size in minutes is not evenly distributed within hour like 7 minutes file then we need to get the last few odd minutes of the hour 
				{
					logger.debug("Overflow due to unevenly distributed minutes contStopTime "+contStopTime+" actualContStopTime "+actualContStopTime);
					contStopTime = actualContStopTime;
					destFileName = createFileName();
					processMergeAndCreateComtrade(); 
					contStartTime = contStopTime + singleSampleTime;
					contStopTime = contStartTime + ((sizeInMinutes*60) * 1000000)- singleSampleTime;
				}
			}
		} catch (Exception e) {
			logger.error("Exception occured in process Cont Analog data with start time "+contStartTime+" and stop time "+contStopTime, e);
			contStartTime = contStopTime + singleSampleTime;
			contStopTime = contStartTime + ((sizeInMinutes*60) * 1000000)- singleSampleTime;
		}
		end = System.currentTimeMillis();
		logger.debug("Total time for creating ContAnalog Data " + (end-start));

	}
	
	private void processMergeAndCreateComtrade() throws M9000Exception
	{
		PreparedStatement ps = null;
		ResultSet rs= null;
		int i = 0;
		ComtradeDataDTO comtradeDataDTO = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		Map<String,ComtradeDataDTO> mapComtradeContDat = new HashMap<String, ComtradeDataDTO>();
		double oscSampleCnt = getExportAnalogBufferTime() * getStationDetails().getSystemLongTermSampleRate();
		logger.debug("Oscillography sample count "+oscSampleCnt);
		Double sampleTime = (((oscSampleCnt - 1)/getStationDetails().getSystemLongTermSampleRate()) * 1000000);
		logger.debug("Sample time in double "+sampleTime);
		long sampleTtimeVal = sampleTime.longValue(); 
		logger.debug("SampleTimeVal long "+sampleTtimeVal);
		finalMergedSampleCnt = 0;

		String sqlQuery = "SELECT recId, expId, name, phase, units, tsLast, sampleRate, sampleCnt, scale, offset, "
			+ "data FROM `m9000`.`contAnalog` where (tsLast >= ? "
			+ "and tsLast <= ?)order by recId,expId, tsLast";

		try {
			logger.debug("DB-POOL New connection from processMergeAndInsertLongTermData ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("AutoExport: ContAnalog-Data-Debug: Start time "+contStartTime+" Stop time "+contStopTime );
			logger.debug("ContAnalog-Data-Debug: sampleTime.longValue() "+sampleTtimeVal);
			logger.debug("ContAnalog-Data-Debug: SQL query to be executed "+ sqlQuery);
			ps = mysqlConn.prepareStatement(sqlQuery);
			ps.setLong(++i, (contStartTime));
			logger.debug("ContAnalog-Data-Debug: param "+i+" - "+(contStartTime));
			ps.setLong(++i, (contStopTime+(getExportAnalogBufferTime() * 1000000)));
			logger.debug("ContAnalog-Data-Debug: param "+i+" - "+(contStopTime+(getExportAnalogBufferTime() * 1000000)));
			rs = ps.executeQuery();

			InputStream is;
			StringBuffer channelInfoBuffer;
			InputStream isData;
			double sampleRate;
			int sampleCnt = 0;
			int startSampleNo = -1;
			int endSampleNo = -1;
			int expId;
			int recId;
			double startSampleTime = 0;
			long lastSampleTime = 0;
			Double missingSampleCnt;
			double tus;
			double tusDivBy2;
			// START: 15-Jul-2020 - LTR Analog COMTRADE config file need to include primary and secondary values
			AnalogChannelDTO analogChannelDto;
			// END: 16-Jul-2020
			while (rs.next())
			{
				sampleCnt = rs.getInt("sampleCnt");
				logger.debug("ContAnalog-Data-Debug: Sample Cnt "+sampleCnt);
				sampleRate = rs.getDouble("sampleRate");
				logger.debug("ContAnalog-Data-Debug: sampleRate "+sampleRate);
				expId = rs.getInt("expId");
				logger.debug("ContAnalog-Data-Debug: expId "+expId);
				recId = rs.getInt("recId");
				is = rs.getBinaryStream("data");
				lastSampleTime = rs.getLong("tsLast");
				logger.debug("ContAnalog-Data-Debug: lastSampleTime "+lastSampleTime);
				tus = 1e6/sampleRate;
				tusDivBy2 = tus / 2;
//				if (lastSampleTime > contStartTime)
//				{
//					startSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - contStartTime)*sampleRate)/1000000) + 1;
//					if (startSampleNo < 0)
//					{
//						startSampleNo = 1;
//					}
//				}
//				else
//				{
//					startSampleNo = 1;
//				}
//				logger.debug("ContAnalog-Data-Debug: Start sample No "+startSampleNo);
//				
//				if (lastSampleTime > contStopTime)
//				{
//					endSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - contStopTime)*sampleRate)/1000000)+1;
//					if (endSampleNo < 0)
//					{
//						endSampleNo = sampleCnt;
//					}
//				}
//				else
//				{
//					endSampleNo = sampleCnt;
//				}
//				logger.debug("ContAnalog-Data-Debug: End sample No "+endSampleNo);
//				
//				startSampleTime = (long) (lastSampleTime -(((sampleCnt-1)/sampleRate) * 1000000));
//				logger.debug("ContAnalog-Data-Debug: calculated startSampleTime"+startSampleTime);
				startSampleTime = (lastSampleTime - (sampleCnt-1) * tus);
				if(contStopTime < (startSampleTime - tusDivBy2) || contStartTime > (lastSampleTime + tusDivBy2))
				{
					logger.debug("TODD: contAnalog - Skipping the blob...");
					continue;
				}
				
				startSampleNo = M9kStationUtil.toIndex(contStartTime - startSampleTime, tus, sampleCnt);
				logger.debug("TODD: Start sample no "+startSampleNo);
				endSampleNo = M9kStationUtil.toIndex(contStopTime - startSampleTime, tus, sampleCnt);
				logger.debug("TODD: End sample no "+endSampleNo);
				// START: 15-Jul-2020 - LTR Analog COMTRADE config file need to include primary and secondary values
				analogChannelDto = getAnalogChannelForDfr(recId,expId);
				// END: 16-Jul-2020
				if (mapComtradeContDat.get(recId+"-"+expId) == null)
				{
						sampleCnt=endSampleNo-startSampleNo+1;
					comtradeDataDTO = new ComtradeDataDTO();
					comtradeDataDTO.setTsLast( lastSampleTime);
					if (contStartTime < startSampleTime) // Missing data at the start
					{
						// Construct dummy data for initial missing data
						missingSampleCnt = Math.floor(((startSampleTime - contStartTime )/1000000.0)*sampleRate);
						logger.info("Constructing dummy data for missing samples count of "+missingSampleCnt+" data Start time "+contStartTime+" actual start sample time "+startSampleTime+" for expId "+expId);
						isData = constructDummyContinuousData(missingSampleCnt.intValue());
						comtradeDataDTO.setBinaryDataSteam(isData);
						sampleCnt+=missingSampleCnt;
						isData.close();
					}
					isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo);
					comtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
					comtradeDataDTO.setDfrId(recId); // Just a value to use in file name creation
					comtradeDataDTO.setStationId(getStationDetails().getSystemStationId());
					comtradeDataDTO.setStationName(getStationDetails().getSystemStationName());
					comtradeDataDTO.setRecordingDevId(getStationDetails().getSystemRecordingDeviceId());
					channelInfoBuffer = new StringBuffer();
					// START: 18-Mar-2018 Prefix A or VIRTUAL_ANALOG prefix in cfg files
					if (expId <= M9kStationXMLUtil.getSystem().getAnalogsCount())
					{
						channelInfoBuffer.append("A"+expId+M9kStationConstants.COMMA_SEPERATOR);
					}
					else
					{
						channelInfoBuffer.append(M9kStationConstants.VIRTUAL_PREFIX+expId+M9kStationConstants.COMMA_SEPERATOR);
					}

//					channelInfoBuffer.append(expId+M9kStationConstants.COMMA_SEPERATOR);
					// END: 18-Mar-2018 Prefix A or VIRTUAL_ANALOG prefix in cfg files
					channelInfoBuffer.append(rs.getString("phase")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("name")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("units")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getFloat("scale")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getFloat("offset")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_CHNL_SKEW+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MIN+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MAX+M9kStationConstants.COMMA_SEPERATOR);
					// START: 15-Jul-2020 - LTR Analog COMTRADE config file need to include primary and secondary values
					if (analogChannelDto == null)
					{
						channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(M9kConstants.COMTRADE_SECONDARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
					}
					else
					{
						channelInfoBuffer.append(analogChannelDto.getPrimaryRatio()+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(analogChannelDto.getSecondaryRatio()+M9kStationConstants.COMMA_SEPERATOR);
					}
					// END: 15-Jul-2020
					channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY);
					channelInfoBuffer.append(M9kStationConstants.NEWLINE);
					comtradeDataDTO.setAnalogs(channelInfoBuffer);
					comtradeDataDTO.setSampleRate(sampleRate);
					comtradeDataDTO.setSampleCnt(sampleCnt);
					comtradeDataDTO.setType(M9kStationConstants.ANALOG);
					comtradeDataDTO.setBinaryDataSteam(isData);
					isData.close();
					mapComtradeContDat.put(recId+"-"+expId, comtradeDataDTO);
					comtradeDataDTO.setTsPrefault(contStartTime);
					comtradeDataDTO.setTsTrigger(contStartTime);
					logger.debug("ContAnalog-Data-Debug: sample count for expId "+expId+" is "+sampleCnt );
				}
				else
				{
					comtradeDataDTO = mapComtradeContDat.get(recId+"-"+expId);
					
					if (startSampleTime > (comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000)) ) // First sample should be Last sample + 1 of the previous record
					{
						missingSampleCnt = Math.floor(((startSampleTime - comtradeDataDTO.getTsLast() )/1000000.0)*sampleRate);
						isData = constructDummyContinuousData(missingSampleCnt.intValue());
						comtradeDataDTO.setBinaryDataSteam(isData);
						sampleCnt+=missingSampleCnt;
						isData.close();
					}
					comtradeDataDTO.setTsLast(lastSampleTime);
					logger.debug("ContAnalog-Data-Debug:  StartSampleNo..."+startSampleNo+" End Sample No "+endSampleNo);
						isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo);
						 
						sampleCnt=(endSampleNo-startSampleNo+1)+comtradeDataDTO.getSampleCnt();
						logger.debug("ContAnalog-Data-Debug: sample count for expId "+expId+" is "+sampleCnt );
					
					comtradeDataDTO.setSampleCnt(sampleCnt);
					comtradeDataDTO.setBinaryDataSteam(isData);
					isData.close();
				}
				if (sampleCnt > finalMergedSampleCnt)
				{
					finalMergedSampleCnt = sampleCnt;
					logger.debug("ContAnalog-Data-Debug: finalMergedSampleCnt "+finalMergedSampleCnt);
				}
				is.close();
			}
			if(!mapComtradeContDat.isEmpty())
			{
				if (errorLogCount > 0)
				{
					errorLogCount = 0;
					logger.info("Data match found after errors at previous attempts. Error logging for no match found will be renewed.");
				}
				lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>( mapComtradeContDat.values());
				Collections.sort(lstComtradeDataDtos, new Comparator<ComtradeDataDTO>() {

					@Override
					public int compare(ComtradeDataDTO o1, ComtradeDataDTO o2) {
						int returnVal;
						if ((o1.getDfrId() < o2.getDfrId()))
						{
							returnVal= -1;
						}
						else if (o1.getDfrId() == o2.getDfrId())
						{
							if (o1.getExportId() <= o2.getExportId())
							{
								returnVal =  -1;
							}
							else
							{
								returnVal = 1;
							}
						}
						else
						{
							returnVal = 1 ;
						}
//						return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
						return returnVal;
					}
				});
				int finalSampleCountWithDummy = 0;
				for (int j = 0; j < lstComtradeDataDtos.size(); j++) {
					if (lstComtradeDataDtos.get(j).getTsLast() < contStopTime)
					{
						// Construct dummy data for missing data at the end
						missingSampleCnt = Math.floor(((contStopTime - lstComtradeDataDtos.get(j).getTsLast() )/1000000.0)*lstComtradeDataDtos.get(j).getSampleRate());
						logger.debug("Index count j--> "+j+" -- Constructing dummy Osc data for missing samples at the end  count of "+missingSampleCnt+" From data stop time "+lstComtradeDataDtos.get(j).getTsLast()+" to actual stop sample time "+contStopTime+" for expId "+lstComtradeDataDtos.get(j).getExportId());
						isData = constructDummyContinuousData(missingSampleCnt.intValue());
						lstComtradeDataDtos.get(j).setBinaryDataSteam(isData);
						isData.close();
						lstComtradeDataDtos.get(j).setTsLast( contStopTime);
						finalSampleCountWithDummy = (int)(lstComtradeDataDtos.get(j).getSampleCnt()+missingSampleCnt+1);
						lstComtradeDataDtos.get(j).setSampleCnt(finalSampleCountWithDummy);
						logger.debug("dummy added to end Sample count updated? "+lstComtradeDataDtos.get(j).getSampleCnt());
					}
					
					logger.debug(" Final Sample count updated? "+lstComtradeDataDtos.get(j).getSampleCnt());

				}
				
				comtradeData.createContinuousDataFiles(destExportsDirectory, destFileName, lstComtradeDataDtos);
			}
			else
			{
				if (errorLogCount < 5)
				{
					logger.error("ERROR: No Matching Data available from start time "+contStartTime+" to end time "+contStopTime);
					if (errorLogCount == 4)
					{
						logger.error("Error logging for no matching data will be suspended temporarily to avoid too many errors and it will resume when it recovers.");
					}
					errorLogCount++;
				}
				
			}
			logger.debug("Size of analog cont..."+lstComtradeDataDtos.size());
			getLstMissingExportsId().clear();
			getLstAnalogExportsId().clear();
			lstComtradeDataDtos.clear();
			mapComtradeContDat.clear();
			if (rs != null)
			{
				rs.close();
				rs = null;
			}				
			
			if (ps != null)
			{
				ps.close();
				ps = null;
			}
			if (mysqlConn != null)
			{
//				logger.info("Closing the connection... "+mysqlConn);
				logger.debug("DB-POOL Closing connection from processMergeAndInsertLongTermData ");
				mysqlConn.close();
				mysqlConn = null;
			}
			
		} catch (SQLException e) {
			logger.error("Error in fetching continuous Analog data ", e);
			String sqlState = e.getSQLState();
		    if (sqlState != null && sqlState.startsWith("08")) {
		        // It's likely a communications link failure
		    	throw new M9000Exception(e);
		    }
			
		}
		catch (Exception e) {
			logger.error("Error in fetching continuous Analog data ", e);
//			throw new M9000Exception(e);
		}
		finally
		{
			try {
				System.gc();
				if (rs != null)
				{
					rs.close();
					rs = null;
				}				
				
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (mysqlConn != null)
				{
//					logger.info("Closing the connection... "+mysqlConn);
					logger.debug("DB-POOL Closing connection from processMergeAndInsertLongTermData ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}
		
		
	}
	
	
	public int getExportAnalogBufferTime() {
		return exportAnalogBufferTime;
	}

	public void setExportAnalogBufferTime(int exportAnalogBufferTime) {
		this.exportAnalogBufferTime = exportAnalogBufferTime;
	}


	public long getLtrStartTime() {
		return contStartTime;
	}


	public void setLtrStartTime(long ltrStartTime) {
		this.contStartTime = ltrStartTime;
	}


	public long getLtrStopTime() {
		return contStopTime;
	}


	public void setLtrStopTime(long ltrStopTime) {
		this.contStopTime = ltrStopTime;
	}

	private InputStream constructContinuousData(InputStream is, double sampleRate, int fromSampleNo, int endSampleNo)
	{
		DataInputStream dis = new DataInputStream(is);
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bais);
		
		// To verify whether this is causing the extra sample
		int totalSamplesToRead = endSampleNo - fromSampleNo + 1;
		logger.debug("In constructContinuousData totalSamplesToRead "+totalSamplesToRead);
		short sampleData;
		
		try {
		if (fromSampleNo > 1)
		{
			dis.skipBytes((fromSampleNo - 1)*M9kStationConstants.SIZE_OF_SHORT);
		}
		while (dis.available() > 0)
		{
			sampleData = dis.readShort();
			dos.writeShort(sampleData);
			totalSamplesToRead--;
			if (totalSamplesToRead == 0)
			{
				break;
			}

		}
		dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (dis != null)
			{
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		logger.debug("Before returning constructContinuousData totalSamplesToRead "+totalSamplesToRead);
		return new ByteArrayInputStream(bais.toByteArray());

	}

	private InputStream constructDummyContinuousData(int totalSamplesMissing)
	{
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bais);
		Integer iDummyHexData = 0x8000;
		
		try {
			
//		logger.debug("ContAnalog-Data-Debug-DUMMY: Sample cnt of those missing "+totalSamplesMissing);
		for (int i = 0; i < totalSamplesMissing; i++)
				
		{
			dos.writeShort(iDummyHexData);
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (dos != null)
			{
				try {
					dos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
//		logger.debug("ContAnalog-Data-Debug-DUMMY: byte array size "+bais.size());
		return new ByteArrayInputStream(bais.toByteArray());

	}
	public List<Integer> getLstMissingExportsId() {
		return lstMissingExportsId;
	}

	public void setLstMissingExportsId(List<Integer> lstMissingExportsId) {
		this.lstMissingExportsId = lstMissingExportsId;
	}

	public List<Integer> getLstAnalogExportsId() {
		return lstAnalogExportsId;
	}

	public void setLstAnalogExportsId(List<Integer> lstAnalogExportsId) {
		this.lstAnalogExportsId = lstAnalogExportsId;
	}

	public static void main(String args[])
	{
		try
		{
		M9kExportContAnalogComtrades M9kContAnalogFromDb = new M9kExportContAnalogComtrades(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-06-04 12:00:00").getTime(), 1, "c:/data/m9k/auto-exports",1);
		M9kContAnalogFromDb.exportContAnalogComtrades();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	private String getRequiredDataFormat(String requiredFormat, Date dateToFormat)
	{
		String convertedDateFormat = null;
//		System.out.println("date format for date "+dateToFormat);
		SimpleDateFormat requiredDateFormat = new SimpleDateFormat(requiredFormat);
		requiredDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		convertedDateFormat = requiredDateFormat.format(dateToFormat.getTime());
//		System.out.println("Converted date format "+convertedDateFormat);
		return convertedDateFormat;
	}
	
	private String createFileName()
	{
		String ctFileName;
		String timeCode;
		Date startDate;

		startDate = new Date(contStartTime/1000);
		String date = getRequiredDataFormat(M9kStationConstants.COMTRADE_FILE_DATE_FORMAT,startDate);
//	    String filePrefix = "R";
		String filePrefix = "R"+String.format("%02d", getStationDetails().getSystemStationId());
//	    if (getStationDetails().getSystemStationId() < 10)
//	    {
//	    	filePrefix += "0"+getStationDetails().getSystemStationId();
//	    }
//	    else
//	    {
//	    	filePrefix += getStationDetails().getSystemStationId();
//	    }
	    filePrefix+="CONT"; // To match the old model 2002 statndard
		try
		{
			Calendar cal = Calendar.getInstance();
			timeCode = (cal.get(Calendar.ZONE_OFFSET)+cal.get(Calendar.DST_OFFSET))/1000/3600 +"t";
		}
		catch (Exception e) {
			logger.error("Cannot get TimeZone and rawoffset for timecode. Attempting to read from M9K_COMTRADE",e);
			timeCode = M9kStationComtradeUtil.getComtradeProperty("timeCode");

		}

	    ctFileName = date + ","+timeCode+","+getStationDetails().getSystemStationName()+","+getStationDetails().getSystemRecordingDeviceId()+","+M9kStationComtradeUtil.getComtradeProperty("company")+","+filePrefix+","+M9kStationConstants.OSC;
	    ctFileName = ctFileName.replace("/", "_");
	    ctFileName = ctFileName.replace("\\", "_");
	    return ctFileName;
	}
	
	@SuppressWarnings("unused")
	private long calculateContStartTimeOld()
	{
		long calculatedStartTime = -1; // return the actual start time i.e. an hour before by default
		try {
				File[] exportDateDirs = M9kBackupUtil.getAutoExportDateFolders(destExportsDirectory);
				if (exportDateDirs != null && exportDateDirs.length > 0)
				{
					Arrays.sort(exportDateDirs);
					String latestFolderName = exportDateDirs[exportDateDirs.length-1].getName();
					logger.debug("\tLatest folder name "+latestFolderName);
					File[] exportedFiles = exportDateDirs[exportDateDirs.length-1].listFiles(new FileFilter() {
						public boolean accept(File file) {
							return (file.getName().indexOf(M9kStationConstants.OSC) != -1);
						}
					});
					if (exportedFiles != null && exportedFiles.length > 0)
					{
						Arrays.sort(exportedFiles);
						String latestFileName = exportedFiles[exportedFiles.length-1].getName();
						logger.debug("\tLatest File name "+latestFileName + " size of folder "+exportedFiles.length);
		//				String dateFromFile = latestFileName.substring(0, latestFileName.indexOf(",",latestFileName.indexOf(",")+1));
						String lastExportedTime = latestFileName.substring(0, latestFileName.indexOf(",",latestFileName.indexOf(",")+1));
						logger.debug("Time portion of file name "+lastExportedTime);
						
						// START: 12-Dec-2023 - Updating date parsing logic to Java 8
						DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern(M9kStationConstants.COMTRADE_FILE_DATE_FORMAT);
						LocalDateTime lastExportedLocalDateTime = LocalDateTime.parse(lastExportedTime, dateformatter);
						long latestTimeFromFile = (lastExportedLocalDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()*1000) + (((sizeInMinutes*60) * 1000000) - singleSampleTime);
						
//						SimpleDateFormat sdf = new SimpleDateFormat(M9kStationConstants.COMTRADE_FILE_DATE_FORMAT);
//						Date latestTime = sdf.parse(lastExportedTime);
//						logger.debug("Formatted Date "+latestTime);
//						Date latestContStartTime = new Date(latestTime.getTime());
//						logger.debug("Available end time "+latestContStartTime);
//						logger.debug("time in milli last time "+latestContStartTime.getTime());
//						logger.debug(" Measurement singleSampleTime "+singleSampleTime+" sizeInMinutes "+sizeInMinutes+" (((sizeInMinutes*60) * 1000000) - singleSampleTime "+(((sizeInMinutes*60) * 1000000) - singleSampleTime));
//						long latestTimeFromFile = M9kBackupUtil.getConvertedEPOCHTime(latestContStartTime.getTime()) + (((sizeInMinutes*60) * 1000000) - singleSampleTime);
//						logger.debug("Osc: Latest exported time after adjusting to UTC "+latestTimeFromFile+ " Start Time of the day "+startOfDay );
						// END: 12-Dec-2023
						if (latestTimeFromFile < oldestDataStartHour) 
						{
							calculatedStartTime = oldestDataStartHour;
						}
						else 
						{
							calculatedStartTime = latestTimeFromFile;
						}
						logger.debug("Calculated start time "+ calculatedStartTime);
					}
					else
					{
						calculatedStartTime = oldestDataStartHour;
					}
				}
				else
				{
					calculatedStartTime = oldestDataStartHour;
				}
			} catch (Exception e) {
				
				logger.error("An error occurred calculating the last exported time. Exports will start from last hour from default. ",e);
			}

		return calculatedStartTime;
	}

	// START: 12-Dec-2023 - Updated logic to Java 8
		private long calculateContStartTime()
		{
			long calculatedStartTime = -1; // return the actual start time i.e. an hour before by default
			try {
				
				String latestFileName = M9kBackupUtil.getLatestFilename(destExportsDirectory, M9kStationConstants.OSC);
				if (latestFileName != null && latestFileName.length() > 0)
				{
					String lastExportedTime = latestFileName.substring(0, latestFileName.indexOf(",",latestFileName.indexOf(",")+1));
					logger.debug("Time portion of file name "+lastExportedTime);
					DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern(M9kStationConstants.COMTRADE_FILE_DATE_FORMAT);
					LocalDateTime lastExportedLocalDateTime = LocalDateTime.parse(lastExportedTime, dateformatter);
					long latestTimeFromFile = (lastExportedLocalDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()*1000) + (((sizeInMinutes*60) * 1000000) - singleSampleTime);
					
					if (latestTimeFromFile < oldestDataStartHour) 
					{
						calculatedStartTime = oldestDataStartHour;
					}
					else 
					{
						calculatedStartTime = latestTimeFromFile;
					}
				}
				else
				{
					calculatedStartTime = oldestDataStartHour;
				}
					
				} catch (Exception e) {
					
					logger.error("An error occurred calculating the last exported time. Exports will start from last hour from default. ",e);
				}

			logger.debug("Calculated start time "+ calculatedStartTime);
			return calculatedStartTime;
		}
	public void removeOldContComtrades()
	{
		String dateTodelete = M9kBackupUtil.getDateFolderToDelete(contComtradeNoOfDaysToRetain);
		File folderToDelete;
		logger.debug("Cont Osc export folder to be deleted "+dateTodelete);
		
		try
		{
			File[] exportedDateFolders = M9kBackupUtil.getAutoExportDateFolders(destExportsDirectory);
			if (exportedDateFolders != null)
			{
				for (int j = 0; j < exportedDateFolders.length; j++) {
					folderToDelete = exportedDateFolders[j];
					if (folderToDelete.getName().compareTo(dateTodelete) <= 0)
					{
						logger.info("Cont Osc export folder to be deleted "+dateTodelete);
						File[] exportedFiles = folderToDelete.listFiles();
						for (int i = 0; i < exportedFiles.length; i++) {
							if (exportedFiles[i].delete())
							{
								logger.debug("Succesffully deleted "+exportedFiles[i]);
							}
							else
							{
								logger.debug("Failed to delete "+exportedFiles[i]);
							}
							
						}
						folderToDelete.delete();
					}
				}
			}
		}
		catch (Exception e) {
			logger.error("Unable to delete the folder "+dateTodelete,e);
		}
	}
	public int getRunFrequency() {
		return runFrequency;
	}

	public void setRunFrequency(int runFrequency) {
		this.runFrequency = runFrequency;
	}
	
	/**
	 * START: 15-Jul-2020 - LTR Analog COMTRADE config file need to include primary and secondary values
	 * @param dfrId
	 * @param expId
	 * @return
	 */
	private AnalogChannelDTO getAnalogChannelForDfr(int dfrId, int expId)
	{
		DfrDTO dfrDto;
		AnalogChannelDTO analogChannelDTO = null;
		for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
    		dfrDto = iterator.next();
    		if (dfrDto.getDfrId() == dfrId)
    		{
    			for (Iterator<AnalogChannelDTO> iterator1 = dfrDto.getLstAnalogChannels().iterator(); iterator1.hasNext();) {
    				analogChannelDTO = iterator1.next();
    				if (analogChannelDTO.getChannel().equals(""+expId))
    				{
    					break;
    				}
    			}
    		}
		}
		logger.debug("Analog channel to return for dfrId "+dfrId+" with expId "+expId+" is "+analogChannelDTO);
		return analogChannelDTO;
	}

	public StationDTO getStationDetails() {
		stationDetails = M9kStationDBUtil.getStationDetails();
		return stationDetails;
	}
}
