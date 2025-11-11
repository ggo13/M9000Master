/**
 * 
 */
package com.usi.m9000.station.util;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.usi.m9000.dao.ComtradeContAnalogDAO;
import com.usi.m9000.dao.ComtradeContDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.util.M9kConstants;

/**
 * @author sramasamy
 *
 */
public class M9kBackupUtil {
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kBackupUtil.class);
	
	public static String getCurrentDateFolderForAutoExport() throws Exception
	{
		DateFormat dateFileFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, -1);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

		return dateFileFormat.format(new Date(c.getTimeInMillis()));
	}

	public static String getDateFolderToDelete(int contComtradeNoOfDaysToRetain)
	{
		DateFormat dateFileFormat = new SimpleDateFormat("yyyyMMdd");
		 Calendar cal  = Calendar.getInstance();
	     cal.add(Calendar.DATE, -(contComtradeNoOfDaysToRetain+1));
	     Date toDeleteDate = new Date(cal.getTimeInMillis());
		return dateFileFormat.format(toDeleteDate);
	}

    public static long getLastHourToAutoExport() throws Exception {
	        Calendar c = Calendar.getInstance();
	        c.add(Calendar.HOUR, -1);
	        c.set(Calendar.MINUTE, 0);
	        c.set(Calendar.SECOND, 0);
	        c.set(Calendar.MILLISECOND, 0);
	        return getConvertedEPOCHTime(c.getTimeInMillis());
    }

    public static long getCurrentWholeHourTime() throws Exception {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return getConvertedEPOCHTime(c.getTimeInMillis());
    }

    public static long getMillSecondsFromStartOfHourToRun(int atTheminuteToRun) throws Exception {
        Calendar c = Calendar.getInstance();
        long currTimeInMillis = c.getTimeInMillis();
        c.set(Calendar.MINUTE, atTheminuteToRun);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return ( c.getTimeInMillis() - currTimeInMillis);
    }
    
    public static long getStartTimeOFTheDay() throws Exception {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return getConvertedEPOCHTime(c.getTimeInMillis());
    }
    
    // Get the UTC adjusted epoch time to search the database
  public static long getConvertedEPOCHTime(long timeInMillis) throws ParseException
  {
	  logger.debug("Received epoch to convert "+timeInMillis);
	     SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	     SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	     sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

	  return sdfUTC.parse(sdfLocal.format(new Date(timeInMillis))).getTime()*1000;
  }
	public static File[] getAutoExportDateFolders(String autoExportDirPath)
	{
		File[] autoExportDir = null;
		File autoExportDestFolder = new File(autoExportDirPath);
		try {
				if (autoExportDestFolder.exists())
				{
					autoExportDir = autoExportDestFolder.listFiles(new FileFilter() {
						public boolean accept(File file) {
							return file.isDirectory();
						}
					});
					if (autoExportDir.length > 0)
					{
						Arrays.sort(autoExportDir);
					}
				}
			} catch (Exception e) {
				
				logger.error("An error getting the auto exported days folder",e);
			}

		return autoExportDir;
	}

	public static String getDateStringFromFileName(String fileName)
	{
		String dateStrFromFile = null;
		DateFormat dateFileFormat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdf = new SimpleDateFormat(M9kStationConstants.COMTRADE_FILE_DATE_FORMAT);
		try
		{
			Date fileDateTime = sdf.parse(fileName.substring(0, fileName.indexOf(",",fileName.indexOf(",")+1)));
			dateStrFromFile = dateFileFormat.format(fileDateTime);
		}
		catch (Exception e) {
			try {
				dateStrFromFile = getCurrentDateFolderForAutoExport();
			} catch (Exception e1) {
				dateStrFromFile = dateFileFormat.format(new Date());
			}
			logger.error("Error occured when extracting date from file name "+fileName+" Using default last hour time "+dateStrFromFile);
		}
		logger.debug("Date String from filename for folder "+dateStrFromFile);
		
		return dateStrFromFile;
	}

	public static long getFirstAvailableHourForContData(String typeOfCont) {
		long firstAvailableTime = 0L;
		M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		if (typeOfCont.equalsIgnoreCase(M9kConstants.CONTINUOUS_ANALOG_DATA)) {
			ComtradeContAnalogDAO mySqlComtradeContAnalogDAO = m9kDAOFactory.getComtradeContinuousAnalogDAO();
			firstAvailableTime = mySqlComtradeContAnalogDAO.getFirstAvailableTime();
			
		}
		else
		{
			ComtradeContDAO mySqlComtradeContDAO = m9kDAOFactory.getContinuousComtradeDAO();
			firstAvailableTime = mySqlComtradeContDAO.getFirstAvailableTime();						
		}
		logger.info("First Available time from database "+typeOfCont+" is "+firstAvailableTime);
		firstAvailableTime = convertToStartOfTheHour(firstAvailableTime);
		logger.info("First Available Hour for "+typeOfCont+" is "+firstAvailableTime);
		return firstAvailableTime;
	}
	
	public static long convertToStartOfTheHour(long sourceEpochTime) {
		long startOfTheHour = 0L;
		Instant instant = Instant.ofEpochMilli(sourceEpochTime/1000);
		ZoneId zoneId = ZoneId.systemDefault(); // Use the system default time zone
		// If the source time is in between hours make it to start from the start of the hour
        LocalDateTime convertedTime = instant.atZone(zoneId).toLocalDateTime().withMinute(0).withSecond(0).withNano(0).truncatedTo(ChronoUnit.HOURS);
        startOfTheHour = convertedTime.atZone(zoneId).toInstant().toEpochMilli()*1000;
		return startOfTheHour;
	}
	public static long calculateSecondsUntilNextHour() {
		 LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime nextHour = currentTime.plusHours(1).withMinute(1).withSecond(0).withNano(0);

        // Calculate the difference in seconds
        long secondsUntilNextHour = ChronoUnit.SECONDS.between(currentTime, nextHour);
        logger.info("Seconds until the start of the next hour: " + secondsUntilNextHour + " seconds");
        return secondsUntilNextHour;
    }
	
	public static String getLatestFilename(String sourceDir, String typeOfCont)
	{
		String latestFileName = null;
		try
		{
		Path start = Paths.get(sourceDir);
		try (Stream<Path> stream = Files.walk(start, 2)) {
		    List<Path> collect = stream
		    	.map(Path::getFileName)
		        .sorted()
		        .filter(path -> path.getFileName().toString().indexOf(typeOfCont) != -1)
		        .collect(Collectors.toList());
		    if (collect.size() > 0)
		    {
		    	latestFileName = collect.get(collect.size()-1).toString();
		    }
		}
		}
		catch (Exception e)
		{
			logger.error("Error occured while getting latest file name. Returning null",e);
		}
		logger.info("Latest file name from "+sourceDir+" for cont type "+typeOfCont+" is "+latestFileName);
		return latestFileName;
	}
}
