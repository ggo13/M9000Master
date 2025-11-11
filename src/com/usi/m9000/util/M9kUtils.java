package com.usi.m9000.util;

import java.awt.Component;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jms.MapMessage;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.activemq.BlobMessage;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.common.email.SendMailUSI;
import com.usi.m9000.config.AnalogInfo;
import com.usi.m9000.config.ChannelInfo;
import com.usi.m9000.config.DigitalInfo;
import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dao.HierarchyDAO;
import com.usi.m9000.dao.M9kCleanupDAO;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.EmailReportsSettingsDTO;
import com.usi.m9000.dto.EmailSettingsDTO;
import com.usi.m9000.dto.HierarchyDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.TriggerChannelDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.xml.M9000XmlConfig;

public class M9kUtils {

	static int count;
	public static Vector<Long> vctCount = new Vector<Long>();
	long startTime;
	long endTime;
//	private static String masterType;
	private static ResourceBundle bundle;
	private static String appletCodeBase;
	private static String irigTimezone = "local";
	private static String userPreferredTimezone = "local"; 
	private static String appletWebHost="localhost";
	private static String appletDBConnectionString = null;
	private static Boolean remote = null;
	private static int stationId;
	private static StationDTO stationDto = null;
	private static int reportId = -1;
	private static int contAnalogTimeLimit = -1; // In Seconds
	private static int contDataTimeLimit = -1; // In Seconds
	private static int serResultsLimit; // Set from applet main class M9kComtradeViewerApplet. It gets the value from m9k-applet properties
	private static int faultsResultsLimit; // Set from applet main class M9kComtradeViewerApplet. It gets the value from m9k-applet properties
	private static int alarmsLogPnlResultsLimit;
	private static int timeoffset = 999;
	private static Map<String,String> mapAnalogChannelInputType = null;
	private static PropertiesConfiguration config = null;
	private static PropertiesConfiguration masterConfig = null;
	private static PropertiesConfiguration versionConfig = null;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kUtils.class);
	private static UsersDTO userDto;
	private static boolean faultFilterEnabled = false;
	private static String logicRadioButton= "OR";
	private static boolean isEmailNotificationEnabled = false;
	private static String emailHost;
	private static int emailPort;
	private static String fromEmail;
	private static String emailPwd;
	private static String[] emailIds;

	private static String stationMasterSoftwareVersion = null;
	private static String webMasterSoftwareVersion = null;
	
	private static String dataDir = null;
	private static List<DfrDTO> lstPhysicalDfrDto = null;
	// START: 19-Mar-2021 - Added more analog phase types for delta transformer
	private static Map<String,String> mapAnalogVoltagePhaseTypes = null;
	private static Map<String,String> mapAnalogCurrentPhaseTypes = null;
	private static M9000XmlConfig m9kConfig;
	// END: 19-Mar-2021 - Added more analog phase types for delta transformer

	// 25-May-2021 - Moved dfrs channels options list from ChannelsListInfo to here
	private static List<String> channelsConfList = null;
		
//	private static final String USERNAME_REGEX = "^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]$";
	private static final String USERNAME_REGEX = "^[a-zA-Z][a-zA-Z0-9]{2,14}";
	private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&]).{6,20}$";
	private static final String GET_LAST_INT_REGEX = "[^0-9]+([0-9]+)$";
	private static final Pattern userNamePattern = Pattern.compile(USERNAME_REGEX);
	private static final Pattern passwordPattern = Pattern.compile(PASSWORD_REGEX);
	private static final Pattern retrieveLastIntegerPattern = Pattern.compile(GET_LAST_INT_REGEX);
	// START: 21-May-2024 - Check for unwanted special characters. Only allowed special characters are - _
	private static final Pattern stationNamePattern = Pattern.compile("^[a-zA-Z0-9_\\-\\. ]*$");
	// END: 21-May-2024 - Check for unwanted special characters. Only allowed special characters are - _

	// 09-Nov-2021 - Hierarchy implementation
	private static HierarchyDTO hierarchyDTO = null;

	static
	{
		try {
			bundle = ResourceBundle.getBundle("M9K_COMTRADE");
			versionConfig = new PropertiesConfiguration("m9k-version.properties");
			logger.debug("Loaded m9k-version.properties..."+versionConfig.getString("station-major-version"));
			config = new PropertiesConfiguration("M9000.properties");
			logger.debug("Loaded M9000.properties..."+config.getList("analogInputTypeForReport"));
			masterConfig = new PropertiesConfiguration("m9k-master.properties");
			logger.debug("Loaded m9k-master.properties...");
		} catch (Exception e) {
			logger.error("Error in loading ",e);
			e.printStackTrace();
			config = null;
		}

	}
	public void startTimer(String moduleName)
	{
		startTime = System.currentTimeMillis();
		vctCount.add(startTime);
		logger.debug("Started Timer for module "+moduleName);
	}
	
	public void endTimer(String moduleName)
	{
        endTime = System.currentTimeMillis();
        startTime = vctCount.lastElement();
        logger.debug("Total Time for module "+moduleName+ " is "+(endTime-startTime));
        vctCount.remove(startTime);
	}
	
	public static void printMemoryFootPrint()
	{
		printMemoryFootPrint("");
	}
	public static void printMemoryFootPrint(String currentLocation)
	{
		logger.debug("Currently at "+currentLocation);
		// Get current size of heap in bytes
		long heapSize = Runtime.getRuntime().totalMemory();

		// Get maximum size of heap in bytes. The heap cannot grow beyond this size.
		// Any attempt will result in an OutOfMemoryException.
		long heapMaxSize = Runtime.getRuntime().maxMemory();

		// Get amount of free memory within the heap in bytes. This size will increase
		// after garbage collection and decrease as new objects are created.
		long heapFreeSize = Runtime.getRuntime().freeMemory();
		
		logger.debug("Total memory "+heapSize);
		logger.debug("Maximum memory "+heapMaxSize);
		logger.debug("Free memory left "+heapFreeSize);

	}
	
	@SuppressWarnings("unchecked")
	public static List<DfrDTO> parseScopeConfig(MapMessage mapMessageConfig) throws M9000Exception
	{
		BufferedReader brConfig = null;
		List<DfrDTO> lstDfrs = new ArrayList<DfrDTO>();
		Enumeration<String>  mapNames;
		String dfrName;
		String dfrConfig;
		String channelOffset;
		String strTokens[];
		int analogChannelStart = 0;
		int digitalChannelStart = 0;
		DfrDTO dfrDto;
		String channelsCntString;
		int digitalChnlsCount;
		int analogChnlsCount ;
		int physicalAnalogsCount = -1;
		String strLine;
		String strAnalogChnlName; // To add it to the list and map for select channels functionality in SCOPE tab
		int dfrId = 0;
		DfrDTO physicalDfrDto;
		try {
	 
		mapNames = mapMessageConfig.getMapNames();
		while (mapNames.hasMoreElements()) {
			physicalAnalogsCount = -1;
			dfrName = (String) mapNames.nextElement();
			logger.debug("DfrName to be parsed "+dfrName);
			dfrConfig = mapMessageConfig.getString(dfrName);
			try
			{
				dfrId = Integer.parseInt(dfrName.substring("DFR".length()));
			}
			catch (Exception e) {
				logger.debug("Trouble in getting dfr id from station name "+dfrName+ " due to exception "+e);
			}
			logger.debug("Corresponding config "+dfrConfig);
			physicalDfrDto = getPhysicalDFRBoardDetails(dfrId);
			dfrDto = new DfrDTO(dfrName);
			dfrDto.setDfrId(dfrId);
			dfrDto.setIpAddress("192.168.1."+(100+dfrId));
			logger.debug("Ip address derived "+dfrDto.getIpAddress());
//				brConfig = new BufferedReader(new InputStreamReader(isConf));
				logger.debug("DFR id set "+dfrDto.getDfrId());
				brConfig = new BufferedReader(new StringReader(dfrConfig));
				
				// Process the First line of the CFG file
				channelOffset = brConfig.readLine();
				logger.debug("First line read..."+channelOffset);
				strTokens = channelOffset.split(",");
				if (strTokens.length != 2)
				{
					throw new M9000Exception("Invalid scope Config. Missing channel offset info");
				}
				analogChannelStart = Integer.parseInt(strTokens[0]);
				digitalChannelStart = Integer.parseInt(strTokens[1]);
				dfrDto.setAnalogChannelStart(analogChannelStart);
				dfrDto.setDigitalChannelStart(digitalChannelStart);
				
				// Process the second line of the CFG file
				channelsCntString = brConfig.readLine();
				logger.debug("Second line read..."+channelsCntString);
				strTokens = channelsCntString.split(",");
				if (strTokens.length != 3)
				{
					throw new M9000Exception("Invalid scope Config. Invalid channel configuration");
				}
				if (!strTokens[1].endsWith("A"))
				{
					throw new M9000Exception("Invalid Config File Format. Analog channel count missing");
				}
				if (!strTokens[2].endsWith("D"))
				{
					throw new M9000Exception("Invalid Config File Format. Digital channel count missing");
				}
				analogChnlsCount = Integer.parseInt(strTokens[1].substring(0, strTokens[1].length()-1));
//				physicalAnalogsCount = analogChnlsCount - (analogChnlsCount%8);
				digitalChnlsCount = Integer.parseInt(strTokens[2].substring(0, strTokens[2].length()-1));
				dfrDto.setAnalogChnlCnt(analogChnlsCount);
				dfrDto.setAnalogChannelEnd(analogChannelStart+analogChnlsCount-1);
				dfrDto.setDigitalChnlCnt(digitalChnlsCount);
				dfrDto.setDigitalChannelEnd(digitalChannelStart+digitalChnlsCount-1);
				logger.debug("Total Analogs count.."+analogChnlsCount);
				logger.debug("Total Digitals count.."+digitalChnlsCount);

				// START: 9-Sept-2019 : To display in scope drop down - Physical start and stop
				
				if (physicalDfrDto != null)
				{
					if (physicalDfrDto.getAnalogChnlCnt() > 0)
					{
						physicalAnalogsCount = physicalDfrDto.getPhysicalAnalogsCount();
						dfrDto.setPhysicalAnalogsCount(physicalAnalogsCount);
						dfrDto.setPhysicalAnalogsStart(physicalDfrDto.getAnalogChannelStart());
						dfrDto.setPhysicalAnalogsEnd(physicalDfrDto.getAnalogChannelStart() + physicalAnalogsCount-1);
					}
					if (physicalDfrDto.getDigitalChnlCnt() > 0)
					{
						dfrDto.setPhysicalDigitalsCount(physicalDfrDto.getDigitalChnlCnt());
						dfrDto.setPhysicalDigitalsStart(physicalDfrDto.getDigitalChannelStart());
						dfrDto.setPhysicalDigitalsEnd(physicalDfrDto.getDigitalChannelEnd());
					}
				}
				logger.debug("Physical channels count "+physicalAnalogsCount);

				// END: 9-Sept-2019
				
				// LineFrequency and sample rate
				strTokens = brConfig.readLine().split(",");
				dfrDto.setLineFreq(Integer.parseInt(strTokens[0]));
				dfrDto.setSampleRate(Integer.parseInt(strTokens[1]));
				logger.debug("Line Freq.."+strTokens[0]+" Sample Rate "+strTokens[1]);
				
				// Parse Analog channel info
				int start = analogChannelStart;
//				AnalogChannelDTO analogDto;
				AnalogInfo analogChannelInfo;
//				dfrDto.setLstAnalogChannels(new ArrayList<AnalogChannelDTO>(analogChnlsCount));
				dfrDto.setLstAnalogsInfo(new ArrayList<AnalogInfo>(analogChnlsCount));
				dfrDto.setLstAnalogChannelNames(new ArrayList<String>(analogChnlsCount));
				dfrDto.setMapAnalogChannelNames(new LinkedHashMap<Integer, String>(analogChnlsCount));
		    	for (int j = 0; j < analogChnlsCount; j++) {
		    		strLine = brConfig.readLine();
		    		logger.debug("strLine read "+strLine);
					strTokens = strLine.split(",");
					logger.debug("String tokens "+strTokens.length+" j -> "+j+" physicalAnalogsCount "+physicalAnalogsCount);
					if (j < physicalAnalogsCount || physicalAnalogsCount == -1)
					{
						// Modified to get map to new SCOPE tab for select channels functionality
						strAnalogChnlName = "A"+(start++)+"-"+strTokens[3]+"-Phase "+strTokens[2];
						dfrDto.getMapAnalogChannelNames().put((start-1), strAnalogChnlName);
//						dfrDto.getLstAnalogChannelNames().add("A"+(start++)+"-"+strTokens[3]+"-Phase "+strTokens[2]);
						dfrDto.getLstAnalogChannelNames().add(strAnalogChnlName);
					}
					else
					{
						// Modified to get map to new SCOPE tab for select channels functionality
						strAnalogChnlName = M9kConstants.VIRTUAL_PREFIX+(start++)+"-"+strTokens[3]+"-Phase "+strTokens[2];
						dfrDto.getMapAnalogChannelNames().put((start-1), strAnalogChnlName);
//						dfrDto.getLstAnalogChannelNames().add(M9kConstants.VIRTUAL_PREFIX+(start++)+"-"+strTokens[3]+"-Phase "+strTokens[2]);
						dfrDto.getLstAnalogChannelNames().add(strAnalogChnlName);
					}
//					analogDto = new AnalogChannelDTO();
					analogChannelInfo = new AnalogInfo(Integer.parseInt(strTokens[0]));
					analogChannelInfo.setChnlId(strTokens[1]);
					analogChannelInfo.setPhaseId(strTokens[2]);
					analogChannelInfo.setCircuitName(strTokens[3]);
					logger.debug("Channel Id "+strTokens[1]+" unit "+strTokens[4]);
					analogChannelInfo.setChnlUnit(strTokens[4]);
					analogChannelInfo.setChnlMultiplier(Double.parseDouble(strTokens[5]));
					analogChannelInfo.setChnlOffset(Double.parseDouble(strTokens[6]));
					analogChannelInfo.setChnlSkew(Double.parseDouble(strTokens[7]));
					analogChannelInfo.setRangeMin(Integer.parseInt(strTokens[8]));
					analogChannelInfo.setRangeMax(Integer.parseInt(strTokens[9]));
					analogChannelInfo.setPrimary(Double.parseDouble(strTokens[10]));
					analogChannelInfo.setSecondary(Double.parseDouble(strTokens[11]));
					analogChannelInfo.setScalingFactor(strTokens[12].charAt(0));
//					analogDto.setPhase(strTokens[2]);
//					analogDto.setChannel("" + (j+analogChannelStart));
//		    		analogDto.setCircuitName(strTokens[3]);
//		    		analogDto.setScale(strTokens[5]);
//		    		dfrDto.getLstAnalogChannels().add(analogDto);
					dfrDto.getLstAnalogsInfo().add(analogChannelInfo);
				}
				
		    	start = digitalChannelStart;
				// Parse Digital channel info
		    	dfrDto.setLstEventChannelNames(new ArrayList<String>(digitalChnlsCount));
		    	dfrDto.setLstEventsInfo(new ArrayList<DigitalInfo>());
		    	DigitalInfo digitalChannelInfo;
		    	for (int j = 0; j < digitalChnlsCount; j++) {
		    		strTokens = brConfig.readLine().split(",");
		    		if (strTokens[1].toUpperCase().startsWith("E"))
		    		{
		    			dfrDto.getLstEventChannelNames().add("E"+(start++)+"-"+strTokens[3]);
		    		}
		    		else
		    		{
		    			dfrDto.getLstEventChannelNames().add(strTokens[1]+"-"+strTokens[3]);
		    		}
					digitalChannelInfo = new DigitalInfo(Integer.parseInt(strTokens[0]));
					digitalChannelInfo.setChnlId(strTokens[1]);
					digitalChannelInfo.setPhaseId(strTokens[2]);
					digitalChannelInfo.setCircuitName(strTokens[3]);
					digitalChannelInfo.setStatus(Integer.parseInt(strTokens[4]));
					dfrDto.getLstEventsInfo().add(digitalChannelInfo);
				}
		    	brConfig.close();
				
				
		    	lstDfrs.add(dfrDto);
		}
		} catch (FileNotFoundException fnfe) {
			throw new M9000Exception(fnfe);
		} catch (IOException ioe) {
			throw new M9000Exception(ioe);
		} catch (NumberFormatException nfe) {
			throw new M9000Exception("Invalid Config File Format", nfe);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new M9000Exception("Error in MessageQ provider", e);
		}
		finally
		{
			if (brConfig != null)
			{
				try {
					brConfig.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return lstDfrs;
	}
	
	/**
	 * Parse the config for specific dfr into a DFRDTO object
	 * @param srcDfrId
	 * @return
	 */
	public static DfrDTO parseDfrSpecificScopeConfig(int dfrId, String dfrSpecificConfig) throws M9000Exception
	{
		BufferedReader brConfig = null;
		String channelOffset;
		String strTokens[];
		int analogChannelStart = 0;
		int digitalChannelStart = 0;
		DfrDTO dfrDto;
		String channelsCntString;
		int digitalChnlsCount;
		int analogChnlsCount ;
		int physicalAnalogsCount = -1;
		String strLine;
		String strAnalogChnlName; // To add it to the list and map for select channels functionality in SCOPE tab
		DfrDTO physicalDfrDto;
		try {
	 
			physicalAnalogsCount = -1;
			String dfrName = "DFR"+dfrId;
			logger.debug("DfrName to be parsed "+dfrName);
			logger.debug("Corresponding config "+dfrSpecificConfig);
			physicalDfrDto = getPhysicalDFRBoardDetails(dfrId);
			dfrDto = new DfrDTO(dfrName);
			dfrDto.setDfrId(dfrId);
			dfrDto.setIpAddress("192.168.1."+(100+dfrId));
			logger.debug("Ip address derived "+dfrDto.getIpAddress());
//				brConfig = new BufferedReader(new InputStreamReader(isConf));
				logger.debug("DFR id set "+dfrDto.getDfrId());
				brConfig = new BufferedReader(new StringReader(dfrSpecificConfig));
				
				// Process the First line of the CFG file
				channelOffset = brConfig.readLine();
				logger.debug("First line read..."+channelOffset);
				strTokens = channelOffset.split(",");
				if (strTokens.length != 2)
				{
					throw new M9000Exception("Invalid scope Config. Missing channel offset info");
				}
				analogChannelStart = Integer.parseInt(strTokens[0]);
				digitalChannelStart = Integer.parseInt(strTokens[1]);
				dfrDto.setAnalogChannelStart(analogChannelStart);
				dfrDto.setDigitalChannelStart(digitalChannelStart);
				
				// Process the second line of the CFG file
				channelsCntString = brConfig.readLine();
				logger.debug("Second line read..."+channelsCntString);
				strTokens = channelsCntString.split(",");
				if (strTokens.length != 3)
				{
					throw new M9000Exception("Invalid scope Config. Invalid channel configuration");
				}
				if (!strTokens[1].endsWith("A"))
				{
					throw new M9000Exception("Invalid Config File Format. Analog channel count missing");
				}
				if (!strTokens[2].endsWith("D"))
				{
					throw new M9000Exception("Invalid Config File Format. Digital channel count missing");
				}
				analogChnlsCount = Integer.parseInt(strTokens[1].substring(0, strTokens[1].length()-1));
//				physicalAnalogsCount = analogChnlsCount - (analogChnlsCount%8);
				digitalChnlsCount = Integer.parseInt(strTokens[2].substring(0, strTokens[2].length()-1));
				dfrDto.setAnalogChnlCnt(analogChnlsCount);
				dfrDto.setAnalogChannelEnd(analogChannelStart+analogChnlsCount-1);
				dfrDto.setDigitalChnlCnt(digitalChnlsCount);
				dfrDto.setDigitalChannelEnd(digitalChannelStart+digitalChnlsCount-1);
				logger.debug("Total Analogs count.."+analogChnlsCount);
				logger.debug("Total Digitals count.."+digitalChnlsCount);

				// START: 9-Sept-2019 : To display in scope drop down - Physical start and stop
				
				if (physicalDfrDto != null)
				{
					if (physicalDfrDto.getAnalogChnlCnt() > 0)
					{
						physicalAnalogsCount = physicalDfrDto.getPhysicalAnalogsCount();
						dfrDto.setPhysicalAnalogsCount(physicalAnalogsCount);
						dfrDto.setPhysicalAnalogsStart(physicalDfrDto.getAnalogChannelStart());
						dfrDto.setPhysicalAnalogsEnd(physicalDfrDto.getAnalogChannelStart() + physicalAnalogsCount-1);
					}
					if (physicalDfrDto.getDigitalChnlCnt() > 0)
					{
						dfrDto.setPhysicalDigitalsCount(physicalDfrDto.getDigitalChnlCnt());
						dfrDto.setPhysicalDigitalsStart(physicalDfrDto.getDigitalChannelStart());
						dfrDto.setPhysicalDigitalsEnd(physicalDfrDto.getDigitalChannelEnd());
					}
				}
				logger.debug("Physical channels count "+physicalAnalogsCount);

				// END: 9-Sept-2019
				
				// LineFrequency and sample rate
				strTokens = brConfig.readLine().split(",");
				dfrDto.setLineFreq(Integer.parseInt(strTokens[0]));
				dfrDto.setSampleRate(Integer.parseInt(strTokens[1]));
				logger.debug("Line Freq.."+strTokens[0]+" Sample Rate "+strTokens[1]);
				
				// Parse Analog channel info
				int start = analogChannelStart;
//				AnalogChannelDTO analogDto;
				AnalogInfo analogChannelInfo;
//				dfrDto.setLstAnalogChannels(new ArrayList<AnalogChannelDTO>(analogChnlsCount));
				dfrDto.setLstAnalogsInfo(new ArrayList<AnalogInfo>(analogChnlsCount));
				dfrDto.setLstAnalogChannelNames(new ArrayList<String>(analogChnlsCount));
				dfrDto.setMapAnalogChannelNames(new LinkedHashMap<Integer, String>(analogChnlsCount));
		    	for (int j = 0; j < analogChnlsCount; j++) {
		    		strLine = brConfig.readLine();
		    		logger.debug("strLine read "+strLine);
					strTokens = strLine.split(",");
					logger.debug("String tokens "+strTokens.length+" j -> "+j+" physicalAnalogsCount "+physicalAnalogsCount);
					if (j < physicalAnalogsCount || physicalAnalogsCount == -1)
					{
						// Modified to get map to new SCOPE tab for select channels functionality
						strAnalogChnlName = "A"+(start++)+"-"+strTokens[3]+"-Phase "+strTokens[2];
						dfrDto.getMapAnalogChannelNames().put((start-1), strAnalogChnlName);
//						dfrDto.getLstAnalogChannelNames().add("A"+(start++)+"-"+strTokens[3]+"-Phase "+strTokens[2]);
						dfrDto.getLstAnalogChannelNames().add(strAnalogChnlName);
					}
					else
					{
						// Modified to get map to new SCOPE tab for select channels functionality
						strAnalogChnlName = M9kConstants.VIRTUAL_PREFIX+(start++)+"-"+strTokens[3]+"-Phase "+strTokens[2];
						dfrDto.getMapAnalogChannelNames().put((start-1), strAnalogChnlName);
//						dfrDto.getLstAnalogChannelNames().add(M9kConstants.VIRTUAL_PREFIX+(start++)+"-"+strTokens[3]+"-Phase "+strTokens[2]);
						dfrDto.getLstAnalogChannelNames().add(strAnalogChnlName);
					}
//					analogDto = new AnalogChannelDTO();
					analogChannelInfo = new AnalogInfo(Integer.parseInt(strTokens[0]));
					analogChannelInfo.setChnlId(strTokens[1]);
					analogChannelInfo.setPhaseId(strTokens[2]);
					analogChannelInfo.setCircuitName(strTokens[3]);
					logger.debug("Channel Id "+strTokens[1]+" unit "+strTokens[4]);
					analogChannelInfo.setChnlUnit(strTokens[4]);
					analogChannelInfo.setChnlMultiplier(Double.parseDouble(strTokens[5]));
					analogChannelInfo.setChnlOffset(Double.parseDouble(strTokens[6]));
					analogChannelInfo.setChnlSkew(Double.parseDouble(strTokens[7]));
					analogChannelInfo.setRangeMin(Integer.parseInt(strTokens[8]));
					analogChannelInfo.setRangeMax(Integer.parseInt(strTokens[9]));
					analogChannelInfo.setPrimary(Double.parseDouble(strTokens[10]));
					analogChannelInfo.setSecondary(Double.parseDouble(strTokens[11]));
					analogChannelInfo.setScalingFactor(strTokens[12].charAt(0));
//					analogDto.setPhase(strTokens[2]);
//					analogDto.setChannel("" + (j+analogChannelStart));
//		    		analogDto.setCircuitName(strTokens[3]);
//		    		analogDto.setScale(strTokens[5]);
//		    		dfrDto.getLstAnalogChannels().add(analogDto);
					dfrDto.getLstAnalogsInfo().add(analogChannelInfo);
				}
				
		    	start = digitalChannelStart;
				// Parse Digital channel info
		    	dfrDto.setLstEventChannelNames(new ArrayList<String>(digitalChnlsCount));
		    	dfrDto.setLstEventsInfo(new ArrayList<DigitalInfo>());
		    	DigitalInfo digitalChannelInfo;
		    	for (int j = 0; j < digitalChnlsCount; j++) {
		    		strTokens = brConfig.readLine().split(",");
		    		if (strTokens[1].toUpperCase().startsWith("E"))
		    		{
		    			dfrDto.getLstEventChannelNames().add("E"+(start++)+"-"+strTokens[3]);
		    		}
		    		else
		    		{
		    			dfrDto.getLstEventChannelNames().add(strTokens[1]+"-"+strTokens[3]);
		    		}
					digitalChannelInfo = new DigitalInfo(Integer.parseInt(strTokens[0]));
					digitalChannelInfo.setChnlId(strTokens[1]);
					digitalChannelInfo.setPhaseId(strTokens[2]);
					digitalChannelInfo.setCircuitName(strTokens[3]);
					digitalChannelInfo.setStatus(Integer.parseInt(strTokens[4]));
					dfrDto.getLstEventsInfo().add(digitalChannelInfo);
				}
		    	brConfig.close();
								
		} 
		catch (Exception e) {
			dfrDto = null;
			throw new M9000Exception("Error in MessageQ provider", e);
		}
		finally
		{
			if (brConfig != null)
			{
				try {
					brConfig.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return dfrDto;
	}

	
	public static DfrDTO getPhysicalDFRBoardDetails(int srcDfrId)
	{
		DfrDTO dfrDTO = null;
		for (Iterator<DfrDTO> iterator = getLstPhysicalDfrDto().iterator(); iterator.hasNext();) {
			dfrDTO = iterator.next();
			if (dfrDTO.getDfrId() == srcDfrId)
			{
				break;
			}
			else
			{
				dfrDTO = null;
			}
		}
		return dfrDTO;
	}
	/**
	 * Called from old applet implementation
	 * @param mapConfig
	 * @return
	 * @throws M9000Exception
	 */
	public static List<DfrDTO> parseScopeConfig(Map<String, String> mapConfig) throws M9000Exception
	{
		BufferedReader brConfig = null;
		List<DfrDTO> lstDfrs = new ArrayList<DfrDTO>();
		Enumeration<String>  mapNames;
		String dfrName;
		StringBuffer dfrConfig = new StringBuffer();
		String channelOffset;
		String strTokens[];
		int analogChannelStart = 0;
		int digitalChannelStart = 0;
		DfrDTO dfrDto;
		String channelsCntString;
		int digitalChnlsCount;
		int analogChnlsCount ;
		String strLine;
		int dfrId = 0;
		try {
	 
		mapNames = Collections.enumeration(mapConfig.keySet());
		while (mapNames.hasMoreElements()) {
			dfrName = (String) mapNames.nextElement();
			logger.debug("DfrName to be parsed "+dfrName);
			dfrConfig.append(1+","+1);
			dfrConfig.append(M9kStationConstants.NEWLINE);
			dfrConfig.append(mapConfig.get(dfrName));
			try
			{
				dfrId = Integer.parseInt(dfrName.substring("DFR".length()));
			}
			catch (Exception e) {
				logger.debug("Trouble in getting dfr id from station name "+dfrName+ " due to exception "+e);
			}
			logger.debug("Corresponding config "+dfrConfig);
			dfrDto = new DfrDTO(dfrName);
			dfrDto.setDfrId(dfrId);
//				brConfig = new BufferedReader(new InputStreamReader(isConf));
				logger.debug("DFR id set "+dfrDto.getDfrId());
				brConfig = new BufferedReader(new StringReader(dfrConfig.toString()));
				
				// Process the First line of the CFG file
				channelOffset = brConfig.readLine();
				logger.debug("First line read..."+channelOffset);
				strTokens = channelOffset.split(",");
				if (strTokens.length != 2)
				{
					throw new M9000Exception("Invalid scope Config. Missing channel offset info");
				}
				analogChannelStart = Integer.parseInt(strTokens[0]);
				digitalChannelStart = Integer.parseInt(strTokens[1]);
				dfrDto.setAnalogChannelStart(analogChannelStart);
				dfrDto.setDigitalChannelStart(digitalChannelStart);
				
				// Process the second line of the CFG file
				channelsCntString = brConfig.readLine();
				logger.debug("Second line read..."+channelsCntString);
				strTokens = channelsCntString.split(",");
				if (strTokens.length != 3)
				{
					throw new M9000Exception("Invalid scope Config. Invalid channel configuration");
				}
				if (!strTokens[1].endsWith("A"))
				{
					throw new M9000Exception("Invalid Config File Format. Analog channel count missing");
				}
				if (!strTokens[2].endsWith("D"))
				{
					throw new M9000Exception("Invalid Config File Format. Digital channel count missing");
				}
				analogChnlsCount = Integer.parseInt(strTokens[1].substring(0, strTokens[1].length()-1));
				digitalChnlsCount = Integer.parseInt(strTokens[2].substring(0, strTokens[2].length()-1));
				dfrDto.setAnalogChnlCnt(analogChnlsCount);
				dfrDto.setAnalogChannelEnd(analogChannelStart+analogChnlsCount-1);
				dfrDto.setDigitalChnlCnt(digitalChnlsCount);
				dfrDto.setDigitalChannelEnd(digitalChannelStart+digitalChnlsCount-1);
				logger.debug("Total Analogs count.."+analogChnlsCount);
				logger.debug("Total Digitals count.."+digitalChnlsCount);
				
				// LineFrequency and sample rate
				strTokens = brConfig.readLine().split(",");
				dfrDto.setLineFreq(Integer.parseInt(strTokens[0]));
				dfrDto.setSampleRate(Integer.parseInt(strTokens[1]));
				logger.debug("Line Freq.."+strTokens[0]+" Sample Rate "+strTokens[1]);
				
				// Parse Analog channel info
				int start = analogChannelStart;
//				AnalogChannelDTO analogDto;
				AnalogInfo analogChannelInfo;
//				dfrDto.setLstAnalogChannels(new ArrayList<AnalogChannelDTO>(analogChnlsCount));
				dfrDto.setLstAnalogsInfo(new ArrayList<AnalogInfo>(analogChnlsCount));
				dfrDto.setLstAnalogChannelNames(new ArrayList<String>(analogChnlsCount));
		    	for (int j = 0; j < analogChnlsCount; j++) {
		    		strLine = brConfig.readLine();
		    		logger.debug("strLine read "+strLine);
					strTokens = strLine.split(",");
					logger.debug("String tokens "+strTokens.length);
		    		dfrDto.getLstAnalogChannelNames().add("A"+(start++)+"-"+strTokens[3]+"-Phase "+strTokens[2]);
//					analogDto = new AnalogChannelDTO();
					analogChannelInfo = new AnalogInfo(Integer.parseInt(strTokens[0]));
					analogChannelInfo.setChnlId(strTokens[1]);
					analogChannelInfo.setPhaseId(strTokens[2]);
					analogChannelInfo.setCircuitName(strTokens[3]);
					logger.debug("Channel Id "+strTokens[1]+" unit "+strTokens[4]);
					analogChannelInfo.setChnlUnit(strTokens[4]);
					analogChannelInfo.setChnlMultiplier(Double.parseDouble(strTokens[5]));
					analogChannelInfo.setChnlOffset(Double.parseDouble(strTokens[6]));
					analogChannelInfo.setChnlSkew(Double.parseDouble(strTokens[7]));
					analogChannelInfo.setRangeMin(Integer.parseInt(strTokens[8]));
					analogChannelInfo.setRangeMax(Integer.parseInt(strTokens[9]));
					analogChannelInfo.setPrimary(Double.parseDouble(strTokens[10]));
					analogChannelInfo.setSecondary(Double.parseDouble(strTokens[11]));
					analogChannelInfo.setScalingFactor(strTokens[12].charAt(0));
//					analogDto.setPhase(strTokens[2]);
//					analogDto.setChannel("" + (j+analogChannelStart));
//		    		analogDto.setCircuitName(strTokens[3]);
//		    		analogDto.setScale(strTokens[5]);
//		    		dfrDto.getLstAnalogChannels().add(analogDto);
					// START: 28-Jan-2020 - Transducer Implementation
					// SCOPE config Units seperated with :
					if (analogChannelInfo.getChnlUnit().indexOf(":") !=1)
					{
						analogChannelInfo.setTranducer(true);
					}
					// END: 28-Jan-2020
					dfrDto.getLstAnalogsInfo().add(analogChannelInfo);
				}
				
		    	start = digitalChannelStart;
				// Parse Digital channel info
		    	dfrDto.setLstEventChannelNames(new ArrayList<String>(digitalChnlsCount));
		    	dfrDto.setLstEventsInfo(new ArrayList<DigitalInfo>());
		    	DigitalInfo digitalChannelInfo;
		    	for (int j = 0; j < digitalChnlsCount; j++) {
		    		strTokens = brConfig.readLine().split(",");
		    		if (strTokens[1].toUpperCase().startsWith("E"))
		    		{
		    			dfrDto.getLstEventChannelNames().add("E"+(start++)+"-"+strTokens[3]);
		    		}
		    		else
		    		{
		    			dfrDto.getLstEventChannelNames().add(strTokens[1]+"-"+strTokens[3]);
		    		}
					digitalChannelInfo = new DigitalInfo(Integer.parseInt(strTokens[0]));
					digitalChannelInfo.setChnlId(strTokens[1]);
					digitalChannelInfo.setPhaseId(strTokens[2]);
					digitalChannelInfo.setCircuitName(strTokens[3]);
					digitalChannelInfo.setStatus(Integer.parseInt(strTokens[4]));
					dfrDto.getLstEventsInfo().add(digitalChannelInfo);
				}
		    	brConfig.close();
				
				
		    	lstDfrs.add(dfrDto);
		}
		} catch (FileNotFoundException fnfe) {
			throw new M9000Exception(fnfe);
		} catch (IOException ioe) {
			throw new M9000Exception(ioe);
		} catch (NumberFormatException nfe) {
			throw new M9000Exception("Invalid Config File Format", nfe);
		}
		finally
		{
			if (brConfig != null)
			{
				try {
					brConfig.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return lstDfrs;
	}
	
	@SuppressWarnings("unused")
	private void parseAnalogChannelsInfo(BufferedReader brConfig, int analogsChnlCount) throws M9000Exception
	{
		int chnlCnt = 0;
		AnalogInfo analogChannelInfo;
		String strTokens[] = null;
		Collection<ChannelInfo> analogInfoList = new ArrayList<ChannelInfo>();
		// Process Analog Channels Info
		try {
			while (chnlCnt < analogsChnlCount && brConfig.ready())
			{
				strTokens = brConfig.readLine().split(",");
				analogChannelInfo = new AnalogInfo(Integer.parseInt(strTokens[0]));
				analogChannelInfo.setChnlId(strTokens[1]);
				analogChannelInfo.setPhaseId(strTokens[2]);
				analogChannelInfo.setCircuitName(strTokens[3]);
				analogChannelInfo.setChnlUnit(strTokens[4]);
				analogChannelInfo.setChnlMultiplier(Double.parseDouble(strTokens[5]));
				analogChannelInfo.setChnlOffset(Double.parseDouble(strTokens[6]));
				analogChannelInfo.setChnlSkew(Double.parseDouble(strTokens[7]));
				analogChannelInfo.setRangeMin(Integer.parseInt(strTokens[8]));
				analogChannelInfo.setRangeMax(Integer.parseInt(strTokens[9]));
				analogChannelInfo.setPrimary(Double.parseDouble(strTokens[10]));
				analogChannelInfo.setSecondary(Double.parseDouble(strTokens[11]));
				analogChannelInfo.setScalingFactor(strTokens[12].charAt(0));
				analogInfoList.add(analogChannelInfo);
				chnlCnt++;
			}
			logger.debug("Total analog channels list "+analogInfoList.size());
//			stationDetails.setAnalogInfoCollection(analogInfoList);
//			end = System.currentTimeMillis();
			logger.debug("Time taken for parseAnalogChannelsInfo in M9kParser ");

		}  catch (Exception e) {
			logger.error("Error occured during parseAnalogChannelsInfo"+e);
		}
		
	}

	@SuppressWarnings("unused")
	private static void parseDigitalChannelsInfo(BufferedReader brConfig) throws M9000Exception
	{
//		start = System.currentTimeMillis();
//		int chnlCnt = 0;
//		DigitalInfo digitalChannelInfo;
//		String strTokens[] = null;
//		Collection<ChannelInfo> digitalInfoList = new ArrayList<ChannelInfo>();
//		// Process Analog Channels Info
//		try {
//			while (chnlCnt < stationDetails.getDigitalChannelsCount() && brConfig.ready())
//			{
//				strTokens = brConfig.readLine().split(",");
//				digitalChannelInfo = new DigitalInfo(Integer.parseInt(strTokens[0]));
//				digitalChannelInfo.setChnlId(strTokens[1]);
////				logger.debug("Digital Channel ID.."+strTokens[1]);
//				digitalChannelInfo.setPhaseId(strTokens[2]);
//				digitalChannelInfo.setCircuitName(strTokens[3]);
////				logger.debug("Digital Circuit Name..."+strTokens[3]);
//				digitalChannelInfo.setStatus(Integer.parseInt(strTokens[4]));
//				digitalInfoList.add(digitalChannelInfo);
//				chnlCnt++;
//			}
//			stationDetails.setDigitalInfoCollection(digitalInfoList);
//			end = System.currentTimeMillis();
//			logger.debug("Time taken for parseDigitalChannelsInfo in M9kParser "+(end-start));
//
//		}catch (IOException e) {
//			logger.error("Error occured during parseDigitalChannelsInfo",e);
//		} catch (Exception e) {
//			logger.error("Error occured during parseDigitalChannelsInfo",e);
//		}
		

	}

	public static boolean isInteger( String input )  
	{  
	   try  
	   {  
	      Integer.parseInt( input );  
	      return true;  
	   }  
	   catch( Exception e)  
	   {  
	      return false;  
	   }  
	}  
	
	public static boolean isDouble( String input )  
	{  
	   try  
	   {  
	      Double.parseDouble( input );  
	      return true;  
	   }  
	   catch( Exception e)  
	   {  
	      return false;  
	   }  
	} 
	
	public static boolean isUnix() {
		 
		String os = System.getProperty("os.name").toLowerCase();
		// linux or unix
		return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
 
	}

//	public static String getMasterType()
//	{
//		if (masterType == null || masterType.isEmpty())
//		{
//			masterType = bundle.getString("master-type");
//		}
////		logger.debug("Master type "+masterType);
//		return masterType;
//	}
	
	public static void setWidthAsPercentages(JTable table, double... percentages) {
	    final double factor = 10000;
	    TableColumnModel model = table.getColumnModel();
	    for (int columnIndex = 0; columnIndex < percentages.length; columnIndex++) {
	        TableColumn column = model.getColumn(columnIndex);
	        column.setPreferredWidth((int) (percentages[columnIndex] * factor));
	        logger.debug(""+(int) (percentages[columnIndex] * factor));
	    }
	}

	public static String convertDateToDisplay(String dateString) throws M9000Exception
	{
		if (getIrigTimezone().trim().equalsIgnoreCase("UTC") && !getUserPreferredTimezone().trim().equalsIgnoreCase("UTC"))
	     {
			return dateString;
	     }
		Date utcDate;
		
		SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	     SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     	 sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
	     try {
			utcDate = sdfUTC.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
			logger.error("Date format error "+e);
			throw new M9000Exception(e);
		}
	     return sdfLocal.format(utcDate);
	}
	
	public static String convertDateToDisplay(Date date) throws M9000Exception
	{
	     SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	     if (!getIrigTimezone().trim().equalsIgnoreCase("UTC") || getUserPreferredTimezone().trim().equalsIgnoreCase("UTC"))
	     {
	    	 sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
	     }
	     return sdfUTC.format(date);
	}
	
	public static String convertEpochToComtradeDisplayDate(long epochTime) throws M9000Exception
	{
	     SimpleDateFormat sdfUTC = new SimpleDateFormat("dd/MM/yyyy,HH:mm:ss.SSS");
	     Date inputDate = new Date(epochTime/1000);
	     if (!getIrigTimezone().trim().equalsIgnoreCase("UTC") || getUserPreferredTimezone().trim().equalsIgnoreCase("UTC"))
	     {
	    	 sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
	     }
	     return sdfUTC.format(inputDate);
	}
	public static String convertDateToSearch(String dateString) throws M9000Exception
	{
		String convertedDate = dateString;
		System.out.println("IRIG Timezone "+getIrigTimezone()+" Date to be converted "+dateString);
//		TimeZone tz = Calendar.getInstance().getTimeZone();
//		System.out.println(tz.getDisplayName()); // (i.e. Moscow Standard Time)
//		System.out.println(tz.getID()); //
		if (getIrigTimezone().trim().equalsIgnoreCase("UTC") && !getUserPreferredTimezone().trim().equalsIgnoreCase("UTC"))
	     {
//			System.out.println("Search date returning without conversion "+dateString);
			return dateString;
	     }
		Date localDate;
		SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   	 try {
		System.out.println("Date in local timezone " +sdfLocal.parse(dateString).toString());
	} catch (ParseException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	     SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	     sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
	     try {
	    	 localDate = sdfUTC.parse(dateString);
	    	 System.out.println("local date converted to UTC "+localDate.toString() +" before conversion "+convertedDate);
	    	 convertedDate = sdfLocal.format(localDate);
//	    	 System.out.println("Converted date "+convertedDate);
		} catch (ParseException e) {
			logger.error("Date format error "+e);
			throw new M9000Exception(e);
		}
    	 System.out.println("Converted date to be returned "+convertedDate);
	     return convertedDate;
	}

	public static Date convertStringToDate(String date) throws M9000Exception
	{
		Date convertedDate;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{
			convertedDate = sdf.parse(date);
		}
		catch (ParseException e) {
			logger.error("Date format error "+e);
			throw new M9000Exception(e);
		}
		return convertedDate;
	}
	public static String convertDateToSearch(Date date) throws M9000Exception
	{
		SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	     return sdfLocal.format(date);
	}


	public static java.text.SimpleDateFormat getDateFormat(String strFormat)
	{
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat (strFormat);
		if (getIrigTimezone() == null || getUserPreferredTimezone() == null || !getIrigTimezone().trim().equalsIgnoreCase("UTC") || getUserPreferredTimezone().trim().equalsIgnoreCase("UTC"))
	    {
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	    }
		return sdf;
	}
	
	public static String convertDateToSearch(String strDate,String srcFormat, String destFormat) throws M9000Exception
	{
		String convertedFormat = convertDateFormatString(strDate, srcFormat, destFormat);
		return convertDateToDisplay(convertedFormat);
	}
	public static String convertDateFormatString(String strDate,String srcFormat, String destFormat) throws M9000Exception
	{
		String formattedDate = "";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(srcFormat);
			Date date;
			SimpleDateFormat reqSdf = new SimpleDateFormat(destFormat);
			date = sdf.parse(strDate);
			formattedDate = reqSdf.format(date);
		}
		catch (Exception e) {
			throw new M9000Exception(e);
		}
		return formattedDate;
	}
	// Retrieves the continuous data files and returns the file name created
	public static String getContinuousDataFileName(String contDataType, String startDate, String endDate) throws M9000Exception
	{
//		InputStream is = null;
		BufferedReader bfr = null;
		String contDataFileName = null;
		try
		{
//			logger.debug("URL Formed: \t"+getAppletCodeBase()+"getContinuousDataFile?stationId="+stationId+"&contDataType="+URLEncoder.encode(contDataType, "UTF-8")+"&fromDateTime="+URLEncoder.encode(startDate, "UTF-8")+"&toDateTime="+URLEncoder.encode(endDate, "UTF-8"));
			System.out.println(("URL Formed: \t"+getAppletCodeBase()+"getContinuousDataFile?stationId="+stationId+"&contDataType="+URLEncoder.encode(contDataType, "UTF-8")+"&fromDateTime="+URLEncoder.encode(startDate, "UTF-8")+"&toDateTime="+URLEncoder.encode(endDate, "UTF-8")));
			URL url = new URL(getAppletCodeBase()+"getContinuousDataFile?stationId="+stationId+"&contDataType="+URLEncoder.encode(contDataType, "UTF-8")+"&fromDateTime="+URLEncoder.encode(startDate, "UTF-8")+"&toDateTime="+URLEncoder.encode(endDate, "UTF-8"));
			long start = System.currentTimeMillis();
			HttpURLConnection urlc = (HttpURLConnection)url.openConnection();  
			urlc.setDoOutput(true);  
			urlc.setDoInput(true);  
			urlc.setAllowUserInteraction(true);
			try {
	            Thread.sleep(5000);
	        } catch (InterruptedException e) {
	        	e.printStackTrace();
	        }
			//Set the Connection Type to post  
			urlc.setRequestMethod( "GET" );
			System.out.println("Default timeouts. ConnectionTimeout"+urlc.getConnectTimeout()+" read time out "+urlc.getReadTimeout());
			urlc.setConnectTimeout(10000);
			urlc.setReadTimeout(10000);
			System.out.println("after timeout setting. ConnectionTimeout"+urlc.getConnectTimeout()+" read time out "+urlc.getReadTimeout());
//			is = urlc.getInputStream();
	        System.out.println("IS data available...."+urlc.getInputStream().available());
	        long stop = System.currentTimeMillis();
	        
			System.out.println("Total time before reading "+(stop-start));
			bfr = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			StringBuffer sb = new StringBuffer();
			while ((contDataFileName=bfr.readLine()) != null)
			{
				sb.append(contDataFileName);
				System.out.println("String buffer "+sb);
			}
			System.out.println("Cont data file name "+sb.toString());
//			Scanner scanner = new Scanner(urlc.getInputStream());
//			while (scanner.hasNextLine())
//			{
//				sb.append(scanner.nextLine());
//				System.out.println("Scanner "+sb);
//			}
			contDataFileName = sb.toString();
			System.out.println("File name received "+contDataFileName);
		}
		catch (Exception e) {
			throw new M9000Exception("Unable to fetch the required file name ",e);
			// For local development applet to work uncomment following
//			try {
//				is = new FileInputStream("//195.1.1.13/data/m9k/faults"+filePath);
//			} catch (FileNotFoundException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
		}
		finally
		{
			try
			{
			if (bfr != null)
			{
				bfr.close();
			}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		//        OutputStream os = urlc.getOutputStream();
		return contDataFileName;
	}
	
	public static String getLTRFileName(int stationId, int faultId, String ltrType) throws M9000Exception
	{
		InputStream is = null;
		BufferedReader bfr = null;
		String ltrFileName = null;
		try
		{
			URL url = new URL(getAppletCodeBase()+"getLtrFileName?stationId="+stationId+"&faultId="+faultId+"&ltrType="+ltrType);  
	        HttpURLConnection urlc = (HttpURLConnection)url.openConnection();  
			urlc.setDoOutput(true);  
			urlc.setDoInput(true);  
			   
			//Set the Connection Type to post  
			urlc.setRequestMethod( "GET" );
			
			is = urlc.getInputStream();
			bfr = new BufferedReader(new InputStreamReader(is));
			ltrFileName=bfr.readLine();
		}
		catch (Exception e) {
			throw new M9000Exception("Unable to fetch the required file name ",e);
			// For local development applet to work uncomment following
//			try {
//				is = new FileInputStream("//195.1.1.13/data/m9k/faults"+filePath);
//			} catch (FileNotFoundException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
		}
		finally
		{
			try
			{
			if (bfr != null)
			{
				bfr.close();
			}
			if (is != null)
			{
				is.close();
			}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		//        OutputStream os = urlc.getOutputStream();
		return ltrFileName;
	}

	public static void main(String args[]) throws M9000Exception
	{
		String startDateTime="2016-03-01 12:58:00";
		System.out.println("UTC Date to display "+M9kUtils.convertDateToDisplay(startDateTime));
		System.out.println("11UTC Date EPOCH to display "+M9kUtils.convertEpochToComtradeDisplayDate(1343736000000000L));
		
		System.out.println("Local Date to search "+M9kUtils.convertDateToSearch(startDateTime));
		Date date = new Date(System.currentTimeMillis());
		System.out.println("UTC Date to display "+M9kUtils.convertDateToDisplay(date));
		System.out.println("Local Date to search "+M9kUtils.convertDateToSearch(date));
		
		System.out.println(M9kUtils.convertEpochToComtradeDisplayDate(1345820581833333L));		
		String testDate = "2019-03-18 09:00:00";
		System.out.println("date converted "+M9kUtils.convertDateToDisplay(testDate));
		System.out.println("Left pad "+M9kUtils.leftpad("DFR1", 6)+"- A");
		System.out.println("right pad "+M9kUtils.rightpad("DFR1", 6)+"- A");
		System.out.println("right pad "+M9kUtils.rightpad("SUM1", 6)+"- A");
		System.out.println("right pad "+M9kUtils.rightpad("A1", 6)+"- A");
		System.out.println("is valid userName? "+M9kUtils.isValidUserName("A1234567890"));
		System.out.println("is valid password? "+M9kUtils.isValidPassword("A1b@asdf"));
		System.out.println("Is valid station name? "+M9kUtils.isValidStationName("Guinea_-."));
	}

	public static String getAppletCodeBase() {
		return appletCodeBase;
	}

	public static void setAppletCodeBase(String appletCodeBase) {
		M9kUtils.appletCodeBase = appletCodeBase;
	}

	public static String getIrigTimezone() {
		return irigTimezone;
	}

	public static void setIrigTimezone(String irigTimezone) {
		M9kUtils.irigTimezone = irigTimezone;
	}

	public static String getUserPreferredTimezone() {
		return userPreferredTimezone;
	}

	public static void setUserPreferredTimezone(String userPreferredTimezone) {
		M9kUtils.userPreferredTimezone = userPreferredTimezone;
	}

	public static String getAppletWebHost() {
		return appletWebHost;
	}

	public static void setAppletWebHost(String appletWebHost) {
		M9kUtils.appletWebHost = appletWebHost;
	}

	public static String getAppletDBConnectionString() {
		if (appletDBConnectionString == null)
		{
			appletDBConnectionString="jdbc:mysql://"+getAppletWebHost()+":3306/m9000?autoReconnect=true&amp;validationQuery=Select 1";
		}
		return appletDBConnectionString;
	}

	public static Boolean isRemote() {
		Connection mysqlConn = null;
		Statement stmt = null;
		if (remote == null)
		{
			try {
				logger.debug("Inside isRemote About to get instnace");
				try
				{
					mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
				}
				catch(Exception ce)
				{
					ce.printStackTrace();
	//				mysqlConn = M9kMySqlDatabase.getInstance().getAppletDBConnection();
				}
				stmt = mysqlConn.createStatement();
	//			ResultSet rs = stmt.executeQuery("SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'm9000' AND TABLE_NAME = 'ser' AND COLUMN_NAME = 'stationId'");
				ResultSet rs = stmt.executeQuery("SELECT * FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'm9000' AND TABLE_NAME = 'cont' OR TABLE_NAME = 'contAnalog'");
				logger.debug("Execute the query successful");
				remote = true;
				while (rs.next())
				{
					remote = false;
					break;
				}
	
			} catch (Exception e) {
				logger.error("Exception occured in determining remote or local table ", e);
				remote = null;
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
					logger.error("Error in setUpFaultsPull cleanup"+ e);
				}
	
			}
		}

		return remote;
	}

	// M9kScopePanel calls this
	public static StationDTO getStationDetails()
	{
		if (stationDto == null)
		{
			M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
			StationDAO mysqlStationDao = m9kDAOFactory.getStationDAO();
			try {
				stationDto = mysqlStationDao.getStationsDetails(getStationId());
				if (stationDto != null)
				{
					m9kConfig = new M9000XmlConfig(new StringReader(stationDto.getConfigXml()));
					stationDto.setTotalDfrsConfigured(m9kConfig.getSubstation().getDFRs().sizeOfDFRArray());
					populateAdvancedProperties();
				}
			} catch (M9000Exception e) {
				logger.error("Error while getting station details"+e);
				stationDto = null;
			}
		}
		return stationDto;
	}

	private static void populateAdvancedProperties() {
		// Trigger duration
		if (m9kConfig.getSubstation().getStationProperties() == null)
		{
			stationDto.setTriggerDuration(10); // Default 10 seconds
			// START 13-Sept-2022 - cont data days limit moved to GUI
			stationDto.setContOscDaysToRetain(5); // Default 5 days to retain OSC
			stationDto.setContMeasurementsDaysToRetain(30); // Default 30 days to retain measurements
			// END 13-Sept-2022 - cont data days limit moved to GUI
			// START: 09-Jan-2023 - Moving export time limit for cont. data to GUI
			stationDto.setContOscExportTimeLimit(120); // Default 120 seconds / 2 minutes
			stationDto.setContMeasurementsExportTimeLimit(600);// Default 600 seconds / 10 minutes
			// END: 09-Jan-2023
			// START: 11-Jun-2024 - Enable or Disable event test for incompatible old boards
			stationDto.setEnableEventTest(true);
			// END: 11-Jun-2024
			// START: 18-Jun-2024 - Set DDR deck time in seconds to be configurable
			stationDto.setDdrDeckTimeLimit(300); // Default 300 seconds
			// END: 18-Jun-2024
		}
		else
		{
			stationDto.setTriggerDuration(m9kConfig.getSubstation().getStationProperties().getTriggerDuration());
			// START 13-Sept-2022 - cont data days limit moved to GUI
			if (m9kConfig.getSubstation().getStationProperties().getContOscDaysToRetain() > 0)
			{
				stationDto.setContOscDaysToRetain(m9kConfig.getSubstation().getStationProperties().getContOscDaysToRetain()); // Default 5 days to retain OSC
			}
			else
			{
				stationDto.setContOscDaysToRetain(5);// Default 5
			}
			
			if (m9kConfig.getSubstation().getStationProperties().getContMeasurementsDaysToRetain() > 0)
			{
				stationDto.setContMeasurementsDaysToRetain(m9kConfig.getSubstation().getStationProperties().getContMeasurementsDaysToRetain()); // Default 30 days to retain measurements
			}
			else
			{
				stationDto.setContMeasurementsDaysToRetain(30);
			}
			// END 13-Sept-2022 - cont data days limit moved to GUI
			// START: 09-Jan-2023 - Moving export time limit for cont. data to GUI
			if (m9kConfig.getSubstation().getStationProperties().getContOscExportTimeLimit() > 0)
			{
				stationDto.setContOscExportTimeLimit(m9kConfig.getSubstation().getStationProperties().getContOscExportTimeLimit()); // Default 5 days to retain OSC
			}
			else
			{
				stationDto.setContOscExportTimeLimit(120);// Default 120 seconds / 2 minutes
			}
			
			if (m9kConfig.getSubstation().getStationProperties().getContMeasurementsExportTimeLimit() > 0)
			{
				stationDto.setContMeasurementsExportTimeLimit(m9kConfig.getSubstation().getStationProperties().getContMeasurementsExportTimeLimit()); // Default 30 days to retain measurements
			}
			else
			{
				stationDto.setContMeasurementsExportTimeLimit(600); // Default 600 seconds / 10 minutes
			}
			// END: 09-Jan-2023
			// START: 11-Jun-2024 - Enable or Disable event test for incompatible old boards
			logger.debug("Is event test set in xml?? "+ m9kConfig.getSubstation().getStationProperties().isSetEnableEventTest());
			if (m9kConfig.getSubstation().getStationProperties().isSetEnableEventTest() && m9kConfig.getSubstation().getStationProperties().getEnableEventTest() == M9kConstants.DISABLE_ZERO)
			{
				stationDto.setEnableEventTest(false); // SET TO TRUE if equal to one
			}
			else
			{
				stationDto.setEnableEventTest(true);
			}
			// END: 11-Jun-2024
			// START: 18-Jun-2024 - Set DDR deck time in seconds to be configurable
			if (m9kConfig.getSubstation().getStationProperties().isSetDdrDeckTimeLimit() && m9kConfig.getSubstation().getStationProperties().getDdrDeckTimeLimit() > 0)
			{
				stationDto.setDdrDeckTimeLimit(m9kConfig.getSubstation().getStationProperties().getDdrDeckTimeLimit()); // Default 300 seconds
			}
			else
			{
				stationDto.setDdrDeckTimeLimit(300); // Default 300 seconds
			}
			// END: 18-Jun-2024
		}
		
	}

	public static void setStationDetails(StationDTO stationDetails)
	{
		M9kUtils.stationDto = stationDetails;
		try {
			m9kConfig = new M9000XmlConfig(new StringReader(stationDto.getConfigXml()));
			M9kUtils.stationDto.setTotalDfrsConfigured(m9kConfig.getSubstation().getDFRs().sizeOfDFRArray());
		} catch (M9000Exception e) {
			logger.error("Error while parsing config xml.."+e);
			m9kConfig = null;
		}
	}
	
	// Used for testing purposes
	public static StationDTO getDebugStationDetails(int debugStationId)
	{
		if (stationDto == null)
		{
			M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
			StationDAO mysqlStationDao = m9kDAOFactory.getStationDAO();
			try {
				stationDto = mysqlStationDao.getStationsDetails(debugStationId);
			} catch (M9000Exception e) {
				logger.error("Error while getting station details"+e);
				stationDto = null;
			}
		}
		return stationDto;
	}

	// invoked when changing stations using dropdrown
	public static void resetStationDetails()
	{
		stationDto = null;
	}

	public static int getStationId() {
		return stationId;
	}

	public static void setStationId(int stationId) {
		M9kUtils.stationId = stationId;
		resetStationDetails();
	}

	public static int getNextReportId() {
			Connection mysqlConn = null;
			Statement stmt = null;
			try {
				mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
				stmt = mysqlConn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT max(reportsId) as reportId FROM reports_health");
				while (rs.next())
				{
					reportId=rs.getInt("reportId");
					++reportId;
					break;
				}
	
			} catch (Exception e) {
				logger.error("Exception occured in setting up comtrade_details table for data transfer "+ e);
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
					logger.error("Error in setUpFaultsPull cleanup"+ e);
				}
	
			}
		return reportId;
	}

	public static int getReportId() {
		return reportId;
	}

	public static void setReportId(int reportId) {
		M9kUtils.reportId = reportId;
	}
	
	public static String getStringFromFile(String file) throws IOException 
	{
		InputStream inputStream = 
			M9kUtils.class.getResourceAsStream(file);
		return convertStreamToString(inputStream);
	}
	private static String convertStreamToString(InputStream is) throws IOException {
	    if (is != null) {
	        Writer writer = new StringWriter();

	        char[] buffer = new char[1024];
	        try {
	            Reader reader = new BufferedReader(
	                    new InputStreamReader(is, "UTF-8"));
	            int n;
	            while ((n = reader.read(buffer)) != -1) {
	                writer.write(buffer, 0, n);
	            }
	        } finally {
	            is.close();
	        }
	        return writer.toString();
	    } else {        
	        return "";
	    }
	}
	
	public static String getDefaultDailyReportHeader()
	{
		StringBuffer strCalHeader = new StringBuffer();
		strCalHeader.append("This summary report provides a \"Health Check\" of the entire 345kV DME system. The master unit at each of the substations has provided a health report of itself based on the following criteria:");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("\t\t\t\tIndividual Station Report Criteria");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("\t\t\t1.	System ON-LINE "); 
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("\t\t\t2.	Clock SyncCommunications");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("\t\t\t3.	CommunicationClock Sync");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("\t\t\t4.	Hardware Power SupplyCheck");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("\t\t\t5.	Disk Storage");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("\t\t\t6.	Temperature");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("\t\t\t7. Analog Channel Self-test");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("\t\t\t8.	Digital Channel Self-test");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("\t\t*To obtain a \"Pass\" all it has to meet all the above test criteria successfully.");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		return strCalHeader.toString();

	}

	/**
	 * @return the contAnalogTimeLimit
	 */
	public static int getContAnalogTimeLimit() {
		// START: 09-Jan-2023 - Export time limit for continuous data is made configurable
//		contAnalogTimeLimit=masterConfig.getInt("cont-analog-time-limit",120); // Defaults to 120 seconds
		contAnalogTimeLimit = getStationDetails().getContOscExportTimeLimit();
		// END: 09-Jan-2023
		return contAnalogTimeLimit;
	}

	/**
	 * @param contAnalogTimeLimit the contAnalogTimeLimit to set
	 */
	public static void setContAnalogTimeLimit(int contAnalogTimeLimit) {
		M9kUtils.contAnalogTimeLimit = contAnalogTimeLimit;
	}

	/**
	 * @return the contDataTimeLimit
	 */
	public static int getContDataTimeLimit() {
		// START: 09-Jan-2023 - Export time limit for continuous data is made configurable
//		contDataTimeLimit=masterConfig.getInt("cont-data-time-limit",120); // Defaults to 120 seconds
		contDataTimeLimit = getStationDetails().getContMeasurementsExportTimeLimit();
		// END: 09-Jan-2023
		return contDataTimeLimit;
	}

	/**
	 * @param contDataTimeLimit the contDataTimeLimit to set
	 */
	public static void setContDataTimeLimit(int contDataTimeLimit) {
		M9kUtils.contDataTimeLimit = contDataTimeLimit;
	}
	
	// START: 09-Jan-2015 Save files locally
	public void saveComtradeFiles(Component srcComponent, String fileName) throws M9000Exception
	{
		System.out.println("In saveComtradeFiles");
		JFileChooser fc = new JFileChooser();
//		File selectedFile = null;
		fc.setFileFilter(new FileFilter() {

			// Accept all directories and all gif, jpg, tiff, or png files.
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}

				int i = file.getName().lastIndexOf(".");
				String fileExtension;
				if (i > 0 && i < (file.getName().length() - 1)) {
					fileExtension = file.getName().substring(i + 1);
					if (fileExtension.equalsIgnoreCase("dat") || fileExtension.equalsIgnoreCase("cfg") || fileExtension.equalsIgnoreCase("inf")) {
						return true;
					}
				}
				return false;
			}

			// The description of this filter
			public String getDescription() {
				return "*.cfg,*.dat,*.inf";
			}
		});
//		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setSelectedFile(new File(fileName));
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = fc.showSaveDialog(srcComponent);
		 if (returnVal == JFileChooser.APPROVE_OPTION) {
//             File file = fc.getSelectedFile();
			 createLocalComtradeFiles(fileName, fc.getSelectedFile().getAbsolutePath());
				JOptionPane.showMessageDialog(srcComponent,
						"Files Save successfull",
					    "Info",
					    JOptionPane.INFORMATION_MESSAGE);

//			 System.out.println("File name selected absolute path"+fc.getSelectedFile().getAbsolutePath()+" filename "+fc.getSelectedFile().getName());
         } else {
        	 System.out.println("Save command cancelled by user." );
         }
	}
	
	private void createLocalComtradeFiles(String srcFileName, String destFileName) throws M9000Exception {
		HttpURLConnection urlcComtrade;
		InputStream srcStream = null;
		OutputStream destStream = null;
		File datFile = null;
		File cfgFile = null;
		File infFile = null;
		boolean booFileCreateSuccess = false;
		String fileType = "";
		try {

			// Write CFG file
			fileType = M9kConstants.CFG_FILE_EXTN;
			verifyFileContent(srcFileName+fileType);

			// Write CFG file
			fileType = M9kConstants.CFG_FILE_EXTN;
			urlcComtrade = getFileContent(srcFileName+fileType);
			srcStream = urlcComtrade.getInputStream();
			cfgFile = new File(destFileName+fileType);
			destStream = new FileOutputStream(cfgFile);
			writeIntoFile(srcStream, destStream);
			srcStream.close();
			srcStream = null;
			destStream.close();
			destStream = null;

			// Write DAT file
			fileType = M9kConstants.DAT_FILE_EXTN;
			urlcComtrade = getFileContent(srcFileName+fileType);
			srcStream = urlcComtrade.getInputStream();
			datFile = new File(destFileName+fileType);
			destStream = new FileOutputStream(datFile);
			writeIntoFile(srcStream, destStream);
			srcStream.close();
			srcStream = null;
			destStream.close();
			destStream = null;
			// Write INF file
			fileType = M9kConstants.INF_FILE_EXTN;
			urlcComtrade = getFileContent(srcFileName+fileType);
			srcStream = urlcComtrade.getInputStream();
			infFile = new File(destFileName+fileType);
			destStream = new FileOutputStream(infFile);
			writeIntoFile(srcStream, destStream);
			
			// If all three files are written successfully then the flag booFileCreateSuccess is set to true else it will remain false
			booFileCreateSuccess = true;
		} catch (M9000Exception e) {
			throw e;
		} catch (IOException e) {
			throw new M9000Exception("Error in creating "+fileType+" file",e);
		} catch (Exception e)
		{
			throw new M9000Exception("Error in creating "+fileType+" file",e);
		} finally {
			try
			{
				if (!booFileCreateSuccess)
				{
					if (datFile != null && datFile.exists())
					{
						datFile.delete();
					}
					if (cfgFile != null && cfgFile.exists())
					{
						cfgFile.delete();
					}
					if (infFile != null && infFile.exists())
					{
						infFile.delete();
					}


				}
				if (srcStream != null)
				{
					srcStream.close();
					srcStream = null;
				}
				if (destStream != null)
				{
					destStream.close();
					destStream = null;
				}
			}catch (Exception e) {
				
			}
		}
		
	}

	public void verifyFileContent(String fileName) throws M9000Exception
	{
		HttpURLConnection urlcComtrade = null;

		InputStream isConfig = null;

		BufferedReader brConfig = null;
		String strLine;
		try {
			urlcComtrade = getFileContent(fileName);
			isConfig = urlcComtrade.getInputStream();
//			brConfig = new BufferedReader(new FileReader(ctConfigFile));
			brConfig = new BufferedReader(new InputStreamReader(isConfig));
			
			// Process the First line of the CFG file
			strLine = brConfig.readLine();
			String strTokens[] = strLine.split(",");
//			System.out.println("Line Read "+strLine+" Str tokens "+strTokens.length);
			if (strTokens.length != 3)
			{
				while((strLine = brConfig.readLine()) != null)
				{
					System.out.println("Error: Line Read "+strLine);
				}
				throw new M9000Exception("Invalid Config File Format");
			}
			// Process the second line of the CFG file
			strTokens = brConfig.readLine().split(",");
			if (strTokens.length != 3)
			{
				throw new M9000Exception("Invalid Config File Format");
			}
			if (!strTokens[1].endsWith("A"))
			{
				throw new M9000Exception("Invalid Config File Format. Analog channel count missing");
			}
			if (!strTokens[2].endsWith("D"))
			{
				throw new M9000Exception("Invalid Config File Format. Digital channel count missing");
			}

			
		} catch (FileNotFoundException fnfe) {
			throw new M9000Exception(fnfe);
		} catch (IOException ioe) {
			throw new M9000Exception(ioe);
		} catch (NumberFormatException nfe) {
			throw new M9000Exception("Invalid Config File Format", nfe);
		} 
		finally
		{
			if (brConfig != null)
			{
				try {
					brConfig.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (isConfig != null)
			{
				try {
					isConfig.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (urlcComtrade != null)
			{
				urlcComtrade.disconnect();
			}
		}
	}

	private void writeIntoFile(InputStream srcStream, OutputStream destStream ) throws Exception
	{
		int read = 0;
		byte[] bytes = new byte[1024];
 
		while ((read = srcStream.read(bytes)) != -1) {
			destStream.write(bytes, 0, read);
		}

	}
	private HttpURLConnection getFileContent(String filePath) throws M9000Exception
	{
//		InputStream is = null;
		HttpURLConnection urlc = null;
		try
		{
			filePath = filePath.replace("\\", "/");
			String encodedFileName = URLEncoder.encode(filePath, "UTF8");
//			System.out.println("Encoded file name: "+encodedFileName);
			System.out.println(M9kUtils.getAppletCodeBase()+"getFile?fileName="+encodedFileName+"&stationId="+M9kUtils.getStationId());
			URL url = new URL(M9kUtils.getAppletCodeBase()+"getFile?fileName="+encodedFileName+"&stationId="+M9kUtils.getStationId());  
	        urlc = (HttpURLConnection)url.openConnection();  
			urlc.setDoOutput(true);  
			urlc.setDoInput(true);  
			   
			//Set the Connection Type to post  
			urlc.setRequestMethod( "GET" );
			
//			is = urlc.getInputStream();
		}
		catch (Exception e) {
			throw new M9000Exception("Unable to fetch the required file "+filePath,e);
		}
		finally
		{
//			urlc.disconnect();
		}
		//        OutputStream os = urlc.getOutputStream();
		return urlc;
	}

	/**
	 * @return the serResultsLimit
	 */
	public static int getSerResultsLimit() {
		return serResultsLimit;
	}

	/**
	 * @param serResultsLimit the serResultsLimit to set
	 */
	public static void setSerResultsLimit(int serResultsLimit) {
		M9kUtils.serResultsLimit = serResultsLimit;
	}

	/**
	 * @return the alarmsLogPnlResultsLimit
	 */
	public static int getAlarmsLogPnlResultsLimit() {
		return alarmsLogPnlResultsLimit;
	}

	/**
	 * @param alarmsLogPnlResultsLimit the alarmsLogPnlResultsLimit to set
	 */
	public static void setAlarmsLogPnlResultsLimit(int alarmsLogPnlResultsLimit) {
		M9kUtils.alarmsLogPnlResultsLimit = alarmsLogPnlResultsLimit;
	}

	// END 09-Jan-2015
	
	public static int getTimeOffset()
	{
		try
		{
			// Start: Modified the timezone calculation logic 02-Dec-2013
//			TimeZone tz = Calendar.getInstance().getTimeZone();
//			timeCode = (tz.getRawOffset()/1000/3600)+"t";
			Calendar cal = Calendar.getInstance();
			timeoffset = Math.abs((cal.get(Calendar.ZONE_OFFSET)+cal.get(Calendar.DST_OFFSET))/1000/3600);
			// End
		}
		catch (Exception e) {
			logger.error("Cannot get TimeZone and rawoffset for timecode. Attempting to read from M9K_COMTRADE",e);
			try
			{
				timeoffset = Integer.parseInt(bundle.getString("timeCode").substring(1, 2));
			}
			catch (Exception e1) {
				logger.error("Cannot get TimeZone from M9K_COMTRADE either. Using default 5",e1);
				timeoffset = 5;
			}
		}
		return timeoffset;
	}

	/**
	 * @return the faultsResultsLimit
	 */
	public static int getFaultsResultsLimit() {
		return faultsResultsLimit;
	}

	/**
	 * @param faultsResultsLimit the faultsResultsLimit to set
	 */
	public static void setFaultsResultsLimit(int faultsResultsLimit) {
		M9kUtils.faultsResultsLimit = faultsResultsLimit;
	}

	/**
	 * @return the mapAnalogChannelInputType
	 */
	public static Map<String, String> getMapAnalogChannelInputType() {
//		System.out.println("Entered getMapAnalogChannelInputType() config: "+config+" mapAnalogChannelInputType "+mapAnalogChannelInputType);
		if (config != null && mapAnalogChannelInputType == null)
		{
			try {
				List<Object> lstAnalogInputType = config.getList("analogInputTypeForReport");
				mapAnalogChannelInputType = new LinkedHashMap<String, String>();
				String inputTypeDetails[];
				for (int i = 0; i < lstAnalogInputType.size(); i++) {
					inputTypeDetails = lstAnalogInputType.get(i).toString().split(":");
					mapAnalogChannelInputType.put(inputTypeDetails[0], inputTypeDetails[1]
							);
				}
				
			} catch (Exception e) {
				mapAnalogChannelInputType = null;
				logger.error("Unable to read from M9000.properties",e);
			}
		}
		return mapAnalogChannelInputType;
	}

	/**
	 * @param mapAnalogChannelInputType the mapAnalogChannelInputType to set
	 */
	public static void setMapAnalogChannelInputType(
			Map<String, String> mapAnalogChannelInputType) {
		M9kUtils.mapAnalogChannelInputType = mapAnalogChannelInputType;
	}

	/**
	 * @return the usersDto
	 */
	public static UsersDTO getUsersDto() {
		if (userDto == null){
			userDto = new UsersDTO();
		}
		return userDto;
	}

	/**
	 * @param usersDto the usersDto to set
	 */
	public static void setUsersDto(UsersDTO usersDto) {
		M9kUtils.userDto = usersDto;
	}

	/**
	 * @return the faultFilterEnabled
	 */
	public static boolean isFaultFilterEnabled() {
		return faultFilterEnabled;
	}

	/**
	 * @param faultFilterEnabled the faultFilterEnabled to set
	 */
	public static void setFaultFilterEnabled(boolean faultFilterEnabled) {
		M9kUtils.faultFilterEnabled = faultFilterEnabled;
	}

	/**
	 * @return the logicRadioButton
	 */
	public static String getLogicRadioButton() {
		return logicRadioButton;
	}

	/**
	 * @param logicRadioButton the logicRadioButton to set
	 */
	public static void setLogicRadioButton(String logicRadioButton) {
		M9kUtils.logicRadioButton = logicRadioButton;
	}
	
	/**
	 * 
	 * @param fileNamesToBeDeleted
	 * @return
	 * @throws M9000Exception
	 */
	@SuppressWarnings("unused")
	public  synchronized static String deleteFilesFromRemoteMasterDepreciated(String fileNamesToBeDeleted) throws M9000Exception
	{
		String strDelStatus = "ERROR";
		HttpURLConnection urlc = null;
		BufferedReader bfr = null;
			try
			{
				String filePath;
				String[] arrFileNames = fileNamesToBeDeleted.split(";;");
				String encodedFileName;
				StringBuffer URL_PARAMS = null;
				for (int i = 0; i < arrFileNames.length; i++) {
					filePath = arrFileNames[i].replace("\\", "/");					
					encodedFileName = URLEncoder.encode(filePath, "UTF8");
					if (URL_PARAMS == null)
					{
						URL_PARAMS = new StringBuffer(fileNamesToBeDeleted.length());
						URL_PARAMS.append("arrOfFileNames="+encodedFileName);
					}
					else
					{
						URL_PARAMS.append("&arrOfFileNames="+encodedFileName);
					}
					System.out.println("Encoded file name: "+encodedFileName);
					URL url = new URL(M9kUtils.getAppletCodeBase()+"deleteFiles?"+URL_PARAMS.toString());  
			        urlc = (HttpURLConnection)url.openConnection();  
					urlc.setDoOutput(true);  
					urlc.setDoInput(true);  
					   
					//Set the Connection Type to post  
					urlc.setRequestMethod( "GET" );
					urlc.setConnectTimeout(10000);
					urlc.setReadTimeout(10000);
	//				System.out.println(M9kUtils.getAppletCodeBase()+"getFile?fileName="+encodedFileName+"&stationId="+M9kUtils.getStationId());
					bfr = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
					StringBuffer sb = new StringBuffer();
					while ((strDelStatus=bfr.readLine()) != null)
					{
						sb.append(strDelStatus);
						System.out.println("String buffer "+sb);
					}
					strDelStatus = sb.toString();
					System.out.println("Remote web master files delete status. "+strDelStatus);
				}
				System.out.println("Delete status"+strDelStatus+" URL_PARAMS "+URL_PARAMS);
			}
			catch (Exception e) {
				throw new M9000Exception("Unable to delete the required file ",e);
			}
			finally
			{
//				urlc.disconnect();
			}
			//        OutputStream os = urlc.getOutputStream();
		return strDelStatus;
	}

	public static String getCurrentDateWithoutTime()
	{
		String currDate="";
		try
		{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		currDate = dateFormat.format(date); 
		}
		catch (Exception e) {
			logger.warn("Error in getting current date. Using long ",e);
			currDate = ""+System.currentTimeMillis();
		}
		return currDate;
	}

	public static String getCurrentDateTime()
	{
		String currDate="";
		try
		{
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyy HH:mm:ss");
		Date date = new Date();
		currDate = dateFormat.format(date); 
		}
		catch (Exception e) {
			logger.warn("Error in getting current date. Using long ",e);
			currDate = ""+System.currentTimeMillis();
		}
		return currDate;
	}
	/**
	 * 10-May-2021 - Email reporting Properties moved to database from m9k-master property file
	 * @return
	 */
	public static boolean isEmailNotificationEnabled() {
//		String emailNotify = masterConfig.getString("email-notification");
//		if (emailNotify != null && emailNotify.equalsIgnoreCase(M9kConstants.ENABLE))
//		{
//			isEmailNotificationEnabled = true;
//		}
//		else
//		{
//			isEmailNotificationEnabled = false;
//		}
		return isEmailNotificationEnabled;
	}

	public static void setEmailNotificationEnabled(boolean isEmailNotificationEnabled) {
		M9kUtils.isEmailNotificationEnabled = isEmailNotificationEnabled;
	}
	
	public static String getEmailHost() {
		emailHost=masterConfig.getString("mail-server-host");
		return emailHost;
	}

	public static void setEmailHost(String emailHost) {
		M9kUtils.emailHost = emailHost;
	}

	public static int getEmailPort() {
		emailPort=masterConfig.getInt("mail-server-smtp-port",587);
		return emailPort;
	}

	public static void setEmailPort(int emailPort) {
		M9kUtils.emailPort = emailPort;
	}

	public static String getFromEmail() {
		fromEmail=masterConfig.getString("from-email");
		return fromEmail;
	}

	public static void setFromEmail(String fromEmail) {
		M9kUtils.fromEmail = fromEmail;
	}

	public static String getEmailPwd() {
		emailPwd=masterConfig.getString("email-pwd");
		return emailPwd;
	}

	public static void setEmailPwd(String emailPwd) {
		M9kUtils.emailPwd = emailPwd;
	}

	public static String[] getEmailIds() {
		emailIds=masterConfig.getStringArray("email-ids");
		return emailIds;
	}

	public static void setEmailIds(String[] emailIds) {
		M9kUtils.emailIds = emailIds;
	}

	public static String getStationMasterSoftwareVersion() {
		if (stationMasterSoftwareVersion == null)
		{
			StringBuffer strSoftwareVersion = new StringBuffer();
			strSoftwareVersion.append(versionConfig.getString("station-major-version"));
			strSoftwareVersion.append(".");
			strSoftwareVersion.append(versionConfig.getString("station-minor-version"));
			strSoftwareVersion.append(".");
			strSoftwareVersion.append(versionConfig.getString("station-build-version"));
			stationMasterSoftwareVersion = strSoftwareVersion.toString();
		}
		return stationMasterSoftwareVersion;
	}

	public static void setStationMasterSoftwareVersion(String stationMasterSoftwareVersion) {
		M9kUtils.stationMasterSoftwareVersion = stationMasterSoftwareVersion;
	}

	public static String getWebMasterSoftwareVersion() {
		if (webMasterSoftwareVersion == null)
		{
			StringBuffer strSoftwareVersion = new StringBuffer();
			strSoftwareVersion.append(versionConfig.getString("web-major-version"));
			strSoftwareVersion.append(".");
			strSoftwareVersion.append(versionConfig.getString("web-minor-version"));
			strSoftwareVersion.append(".");
			strSoftwareVersion.append(versionConfig.getString("web-build-version"));
			webMasterSoftwareVersion = strSoftwareVersion.toString();
		}
		logger.info("Web Master version "+webMasterSoftwareVersion);
		return webMasterSoftwareVersion;
	}

	public static void setWebMasterSoftwareVersion(String webMasterSoftwareVersion) {
		M9kUtils.webMasterSoftwareVersion = webMasterSoftwareVersion;
	}

	public static String getDataDir()
	{
		if (dataDir == null)
		{
			try
			{
				dataDir = bundle.getString("dataDir");
				if (dataDir != null)
				{
					if (!dataDir.endsWith(File.separator))
					{
						dataDir = dataDir+File.separator;
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
		}
		return dataDir;
	}
	/**
	 * Delete the filenames from the remote master
	 * @param fileNamesToBeDeleted
	 * @return
	 * @throws M9000Exception
	 */
	public synchronized static String deleteFilesFromRemoteMaster(String fileNamesToBeDeleted) throws M9000Exception {
		String deleteFileStatus;
		File fileTobeDeleted = null;
		String[] arrOfFileNames = fileNamesToBeDeleted.split(";;");
		
	    try {
	
	        // Define base path somehow. You can define it as init-param of the servlet.
	    	for (int i = 0; i < arrOfFileNames.length; i++) {
		        logger.info("\t\tFile name received "+getDataDir()+arrOfFileNames[i]);
		        
		        // Delete conf file
		        fileTobeDeleted = new File(getDataDir()+arrOfFileNames[i]+M9kConstants.CFG_FILE_EXTN);
		        logger.info("File path "+fileTobeDeleted.getPath());
		        logger.info("File delete ? "+fileTobeDeleted.getParentFile().canWrite());
		        if (fileTobeDeleted.exists())
		        {
		        	fileTobeDeleted.delete();
		        	fileTobeDeleted = null;
		        }
		        // Delete dat file  
		        fileTobeDeleted = new File(getDataDir()+arrOfFileNames[i]+M9kConstants.DAT_FILE_EXTN);
		        if (fileTobeDeleted.exists())
		        {
		        	fileTobeDeleted.delete();
		        	fileTobeDeleted = null;
		        }
		        // Delete inf file  
		        fileTobeDeleted = new File(getDataDir()+arrOfFileNames[i]+M9kConstants.INF_FILE_EXTN);
		        if (fileTobeDeleted.exists())
		        {
		        	fileTobeDeleted.delete();
		        	fileTobeDeleted = null;
		        }
		        
				
			}
	    	deleteFileStatus = "Successfully deleted";
		} 
		catch (Exception e) {
			logger.error("Error in deleting files "+fileTobeDeleted,e);
			deleteFileStatus = "ERROR: "+e.getMessage();
//			throw new M9000Exception(e);
		}

		logger.debug("returning SUCCESS");
	    return deleteFileStatus;
	}

	public static String getLocalTimeCode()
	{
		String timeCode;
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
		return timeCode;
	}
	
	public static String getCommaSeperatedString (String[] arrStrToConvert)
	{
		StringBuffer strValue = null;
		try
		{
			for (int i = 0; i < arrStrToConvert.length; i++) {
				if (null == strValue)
				{
					strValue = new StringBuffer();
					strValue.append(arrStrToConvert[i]);
				}
				else
				{
					strValue.append(","+arrStrToConvert[i]);
				}
			}
		}
		catch (Exception e) {
			strValue = new StringBuffer();
		}
		return strValue.toString();
	}

	public static List<DfrDTO> getLstPhysicalDfrDto() {
		try {
			M9kStationXMLUtil.initXml(getStationDetails().getConfigXml());
			lstPhysicalDfrDto = M9kStationXMLUtil.getDFRsFromStationXML();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lstPhysicalDfrDto;
	}

	/**
	 * @return the run frequency for ser listener
	 */
	public static int getSERListenerRunFrequency() {
		
		return masterConfig.getInt("ser-listener-run-frequency",600); // Defaults to 600 seconds (10 Minute)
	}

	/**
	 * @return the dir for emailed ser data to be stored 
	 */
	public static String getSEREmailDir() {
		
		return masterConfig.getString("ser-email-dir","/data/m9k/ser-updates/"); 
	}
	
	/**
	 * @return the size limit of the email attachment in MB
	 */
	public static int getSEREmailAttachmentSize() {
		
		return masterConfig.getInt("ser-email-attachement-size",2); // Default 3 MB
	}
	/**
	 * @return the size limit of the email attachment in MB
	 */
	public static String getSEREmailFileType() {
		
		return masterConfig.getString("ser-email-file-type","PDF"); // Default PDF. Other format CSV
	}
	
	
	/**
	 * @return enable or disable based on the settings in m9k-master properties file
	 */
	public static String getSEREnable() {
		
		return masterConfig.getString("ser-email","disable"); // Defaults disables the SER
	}

	/**
	 * @return the run frequency for ser listener
	 */
	public static int getSerDatesViewLimit() {
		
		return masterConfig.getInt("ser-dates-limit",100); // Defaults disables the SER
	}

	public static String getM9kConfigFileExtension()
	{
		String cfgExtn = ".sql";
		try
		{
			cfgExtn = bundle.getString("m9000-config-file-extn");
			if (cfgExtn != null && !cfgExtn.isEmpty())
			{
				if (!cfgExtn.startsWith("."))
				{
					cfgExtn="."+cfgExtn;
				}
			}
		}
		catch (Exception e) {
			logger.error("Unable to get m9k config file extension. Using default sql.",e);
			cfgExtn = ".sql";
		}
		return cfgExtn;
	}

	public static void masterBackUpConfig(StationDTO stationDto) {
		BufferedWriter bwConfig = null;
		try
		{
			Set<PosixFilePermission> configDirpermissions = PosixFilePermissions.fromString("rwxrwxr--");
			Set<PosixFilePermission> configFilepermissions = PosixFilePermissions.fromString("r--r--r--");
			String dateTime = new java.text.SimpleDateFormat("MM-dd-yyyy,HH_mm_ss").format(System.currentTimeMillis());
			String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
//			String dir = "/data/m9k/config-files/"+date;
			String dataDir = M9kUtils.getDataDir();
			File configDir = new File(dataDir);
			configDir = new File(configDir.getParent()+File.separator+"config-files"+File.separator+date);
			boolean isCreatedDir = configDir.mkdirs();
			logger.debug("DId config dir"+configDir.getPath()+" get created "+isCreatedDir);
			configDir.setWritable(true, false);
			configDir.setExecutable(true,false);
			configDir.setReadable(true, false);
			
			Path path = Paths.get(configDir.getPath());
			try
			{
	            Files.setPosixFilePermissions(path, configDirpermissions);
				
				// 1. Set Ownership
	            UserPrincipal owner = FileSystems.getDefault()
	                    .getUserPrincipalLookupService()
	                    .lookupPrincipalByName("tomcat"); // Replace with target username
	            Files.setOwner(path, owner);
	            // 2. Set Group Ownership
	            GroupPrincipal group = FileSystems.getDefault()
	                    .getUserPrincipalLookupService()
	                    .lookupPrincipalByGroupName("tomcat"); // Replace with target group name
	            Files.getFileAttributeView(path, PosixFileAttributeView.class)
	                    .setGroup(group);
			}
			catch(Exception e)
			{
				logger.error("Unable to set permissions on config dir "+configDir.getPath(),e);
			}
			String fileName = configDir+"/"+dateTime+","+M9kUtils.getLocalTimeCode()+",R"+String.format("%02d", stationDto.getSystemStationId())+"-"+ stationDto.getSystemStationName()+"-config"+M9kUtils.getM9kConfigFileExtension();
			logger.debug("File name to be created "+fileName);
			File configFile = new File(fileName);
			bwConfig = new BufferedWriter(new FileWriter(configFile));
			bwConfig.write(stationDto.getStationDetailsAsSQLStmt());
			bwConfig.close();
			bwConfig = null;
			try
			{
				Files.setPosixFilePermissions(Paths.get(fileName), configFilepermissions);
			}
			catch(Exception e)
			{
				logger.error("Unable to set permissions on config file "+fileName,e);
			}
			// START: 25-May-2016 Save the config file to CFast drive for backup
			String userHome = "/home/dfr/";
			// START: 30-June-2021 - Commented out to hard code user home directory
//			try
//			{
//				 userHome = System.getProperty( "user.home" );
//				 if (!userHome.endsWith("/"))
//				 {
//					 userHome+="/";
//				 }
//			}
//			catch(Exception e)
//			{
//				userHome = "/home/dfr/";
//				logger.warn("Unable to read user.home. Using default /home/dfr ",e);
//			}
			// END: 30-June-2021
			path = Paths.get(userHome+"/m9k/config-files/");
			try
			{
	            Files.setPosixFilePermissions(path, configDirpermissions);
				
				// 1. Set Ownership
	            UserPrincipal owner = FileSystems.getDefault()
	                    .getUserPrincipalLookupService()
	                    .lookupPrincipalByName("tomcat"); // Replace with target username
	            Files.setOwner(path, owner);
	            // 2. Set Group Ownership
	            GroupPrincipal group = FileSystems.getDefault()
	                    .getUserPrincipalLookupService()
	                    .lookupPrincipalByGroupName("tomcat"); // Replace with target group name
	            Files.getFileAttributeView(path, PosixFileAttributeView.class)
	                    .setGroup(group);
			}
			catch(Exception e)
			{
				logger.error("Unable to set permissions on config dir "+userHome+"/m9k/config-files/",e);
			}
			String secondaryDir = userHome+"/m9k/config-files/"+date;
			File secondaryConfigDir = new File(secondaryDir);
			secondaryConfigDir.mkdirs();
			path = Paths.get(secondaryDir);
			try
			{
				Files.setPosixFilePermissions(path, configDirpermissions);
			}
			catch(Exception e)
			{
				logger.error("Unable to set permissions on config dir "+secondaryDir,e);
			}
			String secondaryFileName = secondaryDir+"/"+"R"+String.format("%02d", stationDto.getSystemStationId())+"-"+ stationDto.getSystemStationName()+"-config_"+dateTime+M9kUtils.getM9kConfigFileExtension();
			logger.debug("Secondary File name to be created "+secondaryFileName);
			configFile = new File(secondaryFileName);
			bwConfig = new BufferedWriter(new FileWriter(configFile));;
			bwConfig.write(stationDto.getStationDetailsAsSQLStmt());
			bwConfig.close();
			bwConfig = null;
			try
			{
				Files.setPosixFilePermissions(Paths.get(secondaryFileName), configFilepermissions);
			}
			catch(Exception e)
			{
				logger.error("Unable to set permissions on config file "+secondaryFileName,e);
			}	
			// STOP: 25-May-2016
		}
		catch (Exception e) {
			logger.error("Error in writing out config file for backup",e);
		}
		finally
		{
			
			try {
				if (bwConfig != null)
				{
					bwConfig.close();
					bwConfig = null;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
	}
	
	/**
	 * Fetch list of available backup files
	 * @param text
	 * @param length
	 * @return
	 */
	public static Map<String, String> getMapOfMasterBackUpConfigs(StationDTO stationDto) {
		File dataDir = new File(M9kUtils.getDataDir());
		String srcDirPath = dataDir.getParentFile().getPath()+File.separator+"config-files"+File.separator;
		Map<String, String> mapBackupFileNames = new TreeMap<String,String>(Collections.reverseOrder());
		logger.debug("Inside getLstOfMasterBackUpConfigs..."+srcDirPath);
		return getMapOfBackupFiles(srcDirPath,stationDto,mapBackupFileNames);
	}
	
	private static Map<String, String> getMapOfBackupFiles(String srcDirectory, StationDTO stationDto, Map<String, String> mapBackupFileNames) {
	    File directory = new File(srcDirectory);
	    try
	    {
		    // Get all files from a directory.
		    File[] fList = directory.listFiles();
		    if(fList != null)
		        for (File file : fList) {      
	            	logger.debug("File..."+file.getName());
		            if (file.isFile()) {
		            	try
		            	{
		            		if (stationDto != null)
		            		{
				            	if (file.getName().contains("R"+String.format("%02d", stationDto.getSystemStationId())+"-"+ stationDto.getSystemStationName()) && file.getName().endsWith(getM9kConfigFileExtension()))
				            	{
				            		logger.debug("Adding file "+file.getName()+" with absolute path.."+file.getAbsolutePath());
				            		mapBackupFileNames.put(file.getAbsolutePath(), file.getName().substring(0, file.getName().lastIndexOf(".")));
				            	}
		            		}
		            		else
		            		{
		            			if (file.getName().endsWith(getM9kConfigFileExtension()))
				            	{
				            		logger.debug("New station flow - Adding file "+file.getName()+" with absolute path.."+file.getAbsolutePath());
				            		mapBackupFileNames.put(file.getAbsolutePath(), file.getName().substring(0, file.getName().lastIndexOf(".")));
				            	}
		            		}
		            	}
		            	catch (Exception e) {
							logger.warn(file.getName()+" is skipped due to exception ",e);
						}
		            } else if (file.isDirectory()) {
		            	getMapOfBackupFiles(file.getAbsolutePath(), stationDto, mapBackupFileNames);
		            }
		        }
	    }
	    catch (Exception e) {
			logger.warn("Issues getting list of backup file lists. Returning empty list.",e);
		}
	    logger.debug("Returning from getMapOfBackupFiles "+mapBackupFileNames.size());
	    return mapBackupFileNames;
	}
	
	public static String leftpad(String text, int length) {
//		return String.format("%" + length + "." + length + "s", text);
	    return String.format("%" + length + "." + length + "s", text).replace(" ", "\u00a0");
	}

	public static String rightpad(String text, int length) {
	    return String.format("%-" + length + "." + length + "s", text).replace(" ", "\u00a0");
//		return String.format("%-" + length + "." + length + "s", text);
	}
	
	/**
	 * Pad chassis name based on total dfrs.
	 * @param text
	 * @return
	 */
	public static String rightpadChassisName(String text) {
		int paddingSize = 3; // 'DFR' length
		if (getStationDetails() != null)
		{
			if (getStationDetails().getTotalDfrsConfigured() < 10)
			{
				paddingSize += 1;
			}
			else if (getStationDetails().getTotalDfrsConfigured() < 100)
			{
				paddingSize += 2;
			}
			else
			{
				paddingSize += 3;
			}
		}
	    return rightpad(text, (paddingSize+1));
	}

	/**
	 * Pad chassis name based on total dfrs.
	 * @param text
	 * @return
	 */
	public static String leftpadAnalogChannel(String text) {
		int paddingSize = 1; // 'DFR' length
		if (getStationDetails() != null)
		{
			if (10 > getStationDetails().getSystemAnalogChannelsCount())
			{
				paddingSize += 1;
			}
			else if (getStationDetails().getTotalDfrsConfigured() < 100)
			{
				paddingSize += 2;
			}
			else
			{
				paddingSize += 3;
			}
		}
		else
		{
			paddingSize += 3;
		}
	    return leftpad(text, (paddingSize+1));
	}
	public static Map<String, String> convertListToMap(List<Object> lstTriggerTypes) {
		logger.debug("In convertListToMap lstTriggerTypes "+lstTriggerTypes);
		Map<String, String> mapTriggerTypes = new LinkedHashMap<String, String>(lstTriggerTypes.size());
		String keyValues[];
		if (lstTriggerTypes != null)
		{
		  for (Iterator<Object> iterator = lstTriggerTypes.iterator(); iterator.hasNext();) {
			keyValues = iterator.next().toString().split(":");
			mapTriggerTypes.put(keyValues[0], keyValues[1]);
		  }
		}

		return mapTriggerTypes;
	}

	/**
	 * @return the mapAnalogVoltagePhaseTypes
	 */
	public static Map<String, String> getMapAnalogVoltagePhaseTypes() {
		if (mapAnalogVoltagePhaseTypes == null)
		{
			try {
				List<Object> lstAnalogInputType = config.getList("analogVoltagePhaseType");
				mapAnalogVoltagePhaseTypes = new LinkedHashMap<String, String>();
				String inputTypeDetails[];
				for (int i = 0; i < lstAnalogInputType.size(); i++) {
					inputTypeDetails = lstAnalogInputType.get(i).toString().split(":");
					mapAnalogVoltagePhaseTypes.put(inputTypeDetails[0], inputTypeDetails[1]
							);
				}
				
			} catch (Exception e) {
				mapAnalogVoltagePhaseTypes = new HashMap<String, String>();
				mapAnalogVoltagePhaseTypes.put("NA", "NA");
				mapAnalogVoltagePhaseTypes.put("A", "A");
				mapAnalogVoltagePhaseTypes.put("B", "B");
				mapAnalogVoltagePhaseTypes.put("C", "C");
				mapAnalogVoltagePhaseTypes.put("N", "N");
				mapAnalogVoltagePhaseTypes.put("AB", "AB");
				mapAnalogVoltagePhaseTypes.put("BC", "BC");
				mapAnalogVoltagePhaseTypes.put("CA", "CA");
				logger.error("Unable to read from M9000.properties",e);
			}
		}
		return mapAnalogVoltagePhaseTypes;
	}

	/**
	 * @return the mapAnalogCurrentPhaseTypes
	 */
	public static Map<String, String> getMapAnalogCurrentPhaseTypes() {
		if (mapAnalogCurrentPhaseTypes == null)
		{
			try {
				List<Object> lstAnalogInputType = config.getList("analogCurrentPhaseType");
				mapAnalogCurrentPhaseTypes = new LinkedHashMap<String, String>();
				String inputTypeDetails[];
				for (int i = 0; i < lstAnalogInputType.size(); i++) {
					inputTypeDetails = lstAnalogInputType.get(i).toString().split(":");
					mapAnalogCurrentPhaseTypes.put(inputTypeDetails[0], inputTypeDetails[1]);
				}
				
			} catch (Exception e) {
				mapAnalogCurrentPhaseTypes = new HashMap<String, String>();
				mapAnalogCurrentPhaseTypes.put("NA", "NA");
				mapAnalogCurrentPhaseTypes.put("A", "A");
				mapAnalogCurrentPhaseTypes.put("B", "B");
				mapAnalogCurrentPhaseTypes.put("C", "C");
				mapAnalogCurrentPhaseTypes.put("N", "N");

				logger.error("Unable to read from M9000.properties",e);
			}
		}

		return mapAnalogCurrentPhaseTypes;
	}

	public static int getSyslogTotalLines()
	{
		return config.getInt("syslogNumLines",1000);
	}
	/**
	 * @return the m9kConfig
	 */
	public static M9000XmlConfig getM9kConfig() {
		return m9kConfig;
	}

	/**
	 * @param m9kConfig the m9kConfig to set
	 */
	public static void setM9kConfig(M9000XmlConfig m9kConfig) {
		M9kUtils.m9kConfig = m9kConfig;
	}

	/**
	 * Used for vurtual measurement algorithm to parse virtual logic
	 * @param selectedDfr
	 * @param lstTriggerChannels
	 * @return
	 */
	public static List<TriggerChannelDTO> getDfrsSpecificMeasurements(String selectedDfr, List<TriggerChannelDTO> lstTriggerChannels)
	{
		List<TriggerChannelDTO> lstDfrsMeasurements = new ArrayList<TriggerChannelDTO>();
		if (lstTriggerChannels != null)
		{
			TriggerChannelDTO triggerChannelDTO;
			for (Iterator<TriggerChannelDTO> iterator = lstTriggerChannels.iterator(); iterator.hasNext();) {
				triggerChannelDTO = iterator.next();
				if (triggerChannelDTO == null || triggerChannelDTO.getId() == null || triggerChannelDTO.getType() == null || triggerChannelDTO.getType().equalsIgnoreCase("Delete")
						|| !triggerChannelDTO.getChassis().equalsIgnoreCase(selectedDfr) )
				{
					continue;
				}
				logger.debug("Adding to the measurements list for virtual algorithm "+triggerChannelDTO);
				lstDfrsMeasurements.add(triggerChannelDTO);			
			}
		}
		logger.debug("lst of dfr measurements for virtuals "+lstDfrsMeasurements);
		return lstDfrsMeasurements;
	}

	/**
	 * Validate for below restrictions
	 * Username consists of alphanumeric characters (a-zA-Z0-9), lowercase, or uppercase.
	 * The number of characters must be between 3 to 15.
	 * @return
	 */
	public static boolean isValidUserName(String userName)
	{
		Matcher matcher = userNamePattern.matcher(userName);
        return matcher.matches();
	}
	
	/**
	 * Validate for below restrictsions
	 * Password should consist of lowercase, uppercase and number and optional special characters (!@#$%^&).
	 * The number of characters must be between 6 to 20.
	 * @return
	 */
	public static boolean isValidPassword(String password)
	{
		Matcher matcher = passwordPattern.matcher(password);
        return matcher.matches();
	}
	
	public static String getReportsDir() {
			String reportsDir= masterConfig.getString("report-files-dir","/data/m9k/status-reports/"); 
			if (!reportsDir.endsWith("/"))
			{
				reportsDir+="/";
			}
		return reportsDir;
	}
	
	public static boolean isValidEmailAddress(String email) {
		   boolean result = true;
		   try {
		      InternetAddress emailAddr = new InternetAddress(email);
		      emailAddr.validate();
		   } catch (AddressException ex) {
		      result = false;
		   }
		   return result;
		}
	
	/**
	 * gets dfrID from chassis name like 10 from DFR10
	 * @param chassisName
	 * @return
	 */
	public static int getDfrIdfromChassisName(String chassisName)
	{
		int dfrId = 0; 
		Matcher matcher = retrieveLastIntegerPattern.matcher(chassisName);
		if (matcher.find()) {
		    String matchedDfrID = matcher.group(1);
		    dfrId = Integer.parseInt(matchedDfrID);
		}
		return dfrId;
	}

	@SuppressWarnings("unchecked")
	public static List<String> getDfrsChannelsOptionsList()
	{
		if (channelsConfList == null)
		{
			channelsConfList = new ArrayList<String>();
			try {
				channelsConfList = (List<String>)(List<?>)config.getList("channelList");
			} catch (Exception e) {
				logger.error("Unable to read chhanelsList from M9000.properties file",e);
			}
		}
		return channelsConfList;
	}
	
	public static String getHostName()
	{
		String hostName = "localhost";
		try
		{
			java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
			hostName = localMachine.getHostName();
		}
		catch (Exception e) {
			logger.error("Unable to get the host name. Station Master is listening to queue name 'localhost'");
		}
			return hostName;
	}
	
	/**
	 * Database cleanup for import config and remove stations
	 */
	public static void m9kMasterStationDbCleanup() throws Exception
	{
		M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		M9kCleanupDAO mySqlM9kCleanupDAO =  m9kDAOFactory.getM9kCleanupDAO();
		mySqlM9kCleanupDAO.clearRemoteMasterTables(getStationId());
		// Faults Data dir
		logger.info("Data dir..."+getDataDir());
		deleteFilesFromDirectory(getDataDir());
	}

	private static void deleteFilesFromDirectory(String dirPath)  throws Exception
	{
		File sourceDir = new File(dirPath);
		if (sourceDir.exists())
		{
			for(File file: sourceDir.listFiles()) 
			{
				if (file.isDirectory())
				{
					deleteFilesFromDirectory(file.getAbsolutePath());
				}
				file.delete();
			}
		}
	}
	/**
     * Unzip it
     * @param zipFile input zip file
     * @param output zip file output folder
     */
    public static void unZipFaultFiles(String fileName, File zipFile) throws M9000Exception
    {
 
     byte[] buffer = new byte[1024];
     ZipInputStream zis = null;
     FileOutputStream fos = null;
 
     try{
 
    	 String destFaultFilePath = M9kStationUtil.getDataDir()+fileName.substring(0, fileName.lastIndexOf("/")+1);
//    	 String filePathPrefix =  getFileName().substring(1, getFileName().indexOf(File.separator,1)+1);// /Prefix like 1-stationName/
    	 logger.debug("destFaultFilePath "+destFaultFilePath);
    	//get the zip file content
    	zis = 
    		new ZipInputStream(new FileInputStream(zipFile));
    	//get the zipped file list entry
    	ZipEntry ze = zis.getNextEntry();
 
    	while(ze!=null){
 
    	   String zipfileName = ze.getName();
           File faultFile = new File(destFaultFilePath+ zipfileName);
 
           logger.debug("file unzip : "+ faultFile.getAbsoluteFile());
 
            //create all non exists folders
            //else you will hit FileNotFoundException for compressed folder
            new File(faultFile.getParent()).mkdirs();
 
            fos = new FileOutputStream(faultFile);             
 
            int len;
            while ((len = zis.read(buffer)) > 0) {
       		fos.write(buffer, 0, len);
            }
 
            fos.close();   
            ze = zis.getNextEntry();
    	}
 
        zis.closeEntry();
    	zis.close();
    	zipFile.delete();
    	logger.debug("Done");
 
    }catch(IOException ex){
       logger.error("Exception while unzipping files ",ex);
       throw new M9000Exception("Exception while unzipping files ",ex);
    }
    catch(Exception ex){
        logger.error("Exception while unzipping files ",ex);
        throw new M9000Exception("Exception while unzipping files ",ex);
     }
    finally
    {
    	try
    	{
	    	if(fos != null)
	    	{
	    		fos.close();
	    	}
	    	if (zis != null)
	    	{
	    		zis.close();
	    	}
    	}catch(Exception ex){
    	       logger.warn("Exception within exception while unzipping files ",ex);
        }
    }
   } 

    /**
     * 03-Nov-2021 - Hierarchy implementation - get Hierarchy info, if available
     * @throws Exception
     */
    public static HierarchyDTO getHierarchyInfo() {
		
		try
		{
			M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
			HierarchyDAO m9kHierarchyDAO =  m9kDAOFactory.getM9kHierarchyDAO();
			M9kKeyValuePair  rootZoneInfo = m9kHierarchyDAO.getRootZone();
			if (rootZoneInfo !=null)
			{
				hierarchyDTO = new HierarchyDTO();
				hierarchyDTO.setRootZoneId(Integer.parseInt(rootZoneInfo.getKey()));
				hierarchyDTO.setRootZoneName(rootZoneInfo.getValue());
				hierarchyDTO.setTotalLevels(m9kHierarchyDAO.getTotalHierarchyLevel());
				hierarchyDTO.setMapHierarchyLevels(m9kHierarchyDAO.getHierarchyMap());
				hierarchyDTO.setLstLeafZones(m9kHierarchyDAO.getLeafZones());
			}
		}
		catch (Exception e) {
			logger.error("Error occurred while getting Hierarchy info ",e);
			hierarchyDTO = null;
		}
		return hierarchyDTO;
	}

    /**
     * 03-Nov-2021 - Hierarchy implementation - get Hierarchy info, if available
     * @throws Exception
     */
    public static List<M9kKeyValuePair> getChildrenZone(int parentZoneId) {
		List<M9kKeyValuePair> lstChildZones = null;
		logger.debug("Entered getChildrenZone method..ParentZoneId "+parentZoneId);
		try
		{
			M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
			HierarchyDAO m9kHierarchyDAO =  m9kDAOFactory.getM9kHierarchyDAO();
			lstChildZones = m9kHierarchyDAO.getChildZones(parentZoneId);
		}
		catch (Exception e) {
			logger.error("Error occurred while getting children info ",e);
		}
		logger.debug("Returning list of children zone "+lstChildZones);
		return lstChildZones;
	}

    public static void getFileIfNotAvailable(String requestedFileName) throws M9000Exception 
    {
    	File zipFile;
    	OutputStream os = null;
		BufferedOutputStream bos = null;
		InputStream blobInputStream = null;
		try
		{
	    	Object receivedObj;
			MessageProperties messageProperties = new MessageProperties();
			messageProperties.addProperty("MESSAGE_TYPE", "FILE_RETRIEVE");
			messageProperties.addProperty("USER_NAME", (getUsersDto()!=null?getUsersDto().getUserName():"User "));
	        logger.info("User "+(getUsersDto() != null?getUsersDto().getUserName():"")+" has requested for the file "+requestedFileName);
	
			receivedObj = M9kMessagesUtil.getRequiredFile(getStationId()+"-Files", requestedFileName,messageProperties);
	    	if (receivedObj == null)
	    	{
	    		logger.error("Error in fetching file: NULL object returned from station master");
	    		throw new M9000Exception("NULL object returned from station master");	        		
	    	}
	    	else if (receivedObj instanceof String )
	    	{
	    		logger.error("Error in fetching file: "+receivedObj);
	    		throw new M9000Exception(receivedObj.toString());
	    	}
	    	else
	    	{
	    		byte[] buffer = new byte[16384]; 
	    		int length = 0; 
	    		int totalLength = 0;
	    		BlobMessage blobMessage = (BlobMessage) receivedObj; 
	    		String fileName = blobMessage.getStringProperty("FILE.NAME"); 
	    		long filelength = blobMessage.getLongProperty("FILE.SIZE");
	    		logger.debug("received fileName: " + fileName + ", the size of file: "+ filelength); 
	    		
	    		String srcZipFilePath = M9kStationUtil.getDataDir()+requestedFileName.substring(0, requestedFileName.lastIndexOf("/")+1)+"compressed"+"/"+"";
	     		File destZipDir = new File(srcZipFilePath);
	     		if (!destZipDir.exists())
	     		{
	     			destZipDir.mkdirs();
	     		}
	     		
	//    		srcFile = new File(filePath + fileName); 
	    		zipFile = new File(destZipDir,fileName);
	    		os = new FileOutputStream(zipFile); 
	    		bos = new BufferedOutputStream(os); 
	
	    		blobInputStream = blobMessage.getInputStream(); 
	    		while ((length = blobInputStream.read(buffer,0,buffer.length)) > 0) {
	    			totalLength+=length;
	//    			logger.debug("length read "+length+" total length "+totalLength);
	    			if (totalLength <= filelength)
	    			{
	    				bos.write(buffer, 0, length);
	    			}
	    			else
	    			{
	    				bos.write(buffer, 0, (int)(length-(totalLength - filelength)));
	    				break;
	    			}
	    		}
	    		
	    		bos.close();
	    		blobInputStream.close();
	    		os.close();
	    		if (zipFile.length() != filelength)
	    		{
	    			logger.error("Mismatch in size of file transfer: Actual size "+filelength+" Received size "+zipFile.length());
	        		throw new M9000Exception(receivedObj.toString());
	    		}
	    		else
	    		{
	    			unZipFaultFiles(requestedFileName, zipFile);
	    		}
	    	}
		}
		catch (Exception e) {
			throw new M9000Exception(e);
		}
    }
    
    public static void sendChangeEmail(String emailSubject, String emailText)
	{
		try
		{
			EmailSettingsDTO emailSettingsDTO;
				emailSettingsDTO = M9kReportUtil.getEmailSettingsDTO();
				logger.debug("Email settings "+emailSettingsDTO);
				EmailReportsSettingsDTO emailReportsSettingsDTO = M9kReportUtil.getEmailReportsSettingsDTO();
				logger.debug("Email report settings "+emailReportsSettingsDTO);
//					if (M9kUtils.isEmailNotificationEnabled() && getEmailNotificationForFaults().equalsIgnoreCase(M9kConstants.ENABLE))
				if (emailSettingsDTO.isEnableEmail() && emailReportsSettingsDTO.isEnableConfigChangeEmail())
				{
					SendMailUSI.sendEmail(emailSubject, emailText.toString());
				}
		
		} catch (Exception e)
		{
			logger.error("Email couldn't be sent for configuration change", e);
		}
	}
    
    // START: 21-May-2024 - Check for unwanted special characters. Only allowed special characters are - _
    public static boolean isValidStationName(String stationName)
	{
		if (stationName == null || stationName.trim().length() == 0)
		{
			return false;
		}
		else
		{
			Matcher matcher = stationNamePattern.matcher(stationName);
	        return matcher.matches(); // It returns true if it has any other special characters apart from _ or space
		}
	}
    // END: 21-May-2024 - Check for unwanted special characters. Only allowed special characters are - _
}
