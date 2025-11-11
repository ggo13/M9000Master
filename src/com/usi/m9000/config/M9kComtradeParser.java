package com.usi.m9000.config;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.chart.ChannelsUtil;
import com.usi.m9000.station.dto.ProcessDatDTO;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

/**
 * 
 */

/**
 * @author sramasamy
 *
 */
public class M9kComtradeParser {

	private StationInfo stationDetails;

	private ChannelsUtil channelsUtil;
	WeakHashMap<String, String> mapTriggerInfo;
//	File ctConfigFile; 
//	File ctDataFile;
	double analogData[][];
	short digitalData[][];
	int dataTimestamps[];

	int abnormalDigiChnl[];
//	Map<Integer,Integer> mapEventsIndexInOrderTirggered = null;
	Integer activeDigitalChannels[];
	List<Integer> activeEventsInOrderOccured;
	int totalAbnormalChannels;
	long start;
	long end;
//	private ArrayList<String> analogToolTips[];
	private ArrayList<String> digitalToolTips[];
	DecimalFormat nf = new DecimalFormat("0.000");
	
	Map<String, Boolean> mapActiveEvents = null;
	InputStream isConfig = null;
	InputStream isDat = null;
	HttpURLConnection urlcConfig = null;
	HttpURLConnection urlcDat = null;
	HttpURLConnection urlcInf = null;
	
	File receivedFileName;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kComtradeParser.class);
	public static boolean booTest = false;
	/**
	 * @throws M9000Exception 
	 * 
	 */
//	public M9kComtradeParser(String ctConfigFilePath, String ctDataFilePath) throws M9000Exception {
//		channelsUtil = new ChannelsUtil();
//		ctConfigFile = new File (ctConfigFilePath);
//		ctDataFile = new File(ctDataFilePath);
//		parseConfigFile();
//		parseComtradeData();
////		parseInfFile();
//	}
	
	public M9kComtradeParser(File comtradeFile, boolean eventsOnly) throws M9000Exception {
		channelsUtil = new ChannelsUtil();
		receivedFileName = comtradeFile;
//		logger.info("Entered M9kComtradeParser...");
		logger.debug("Entered M9kComtradeParser...");
		System.out.println("Entered M9kComtradeParser...");
		if (comtradeFile.getName().endsWith(".dat"))
		{
//			System.out.println("Checking for cfg file");
//			ctDataFile = comtradeFile;
//			isDat = M9kUtils.getFileContent(comtradeFile.getPath());
			try {
				isDat = new FileInputStream(comtradeFile);
			} catch (Exception e) {
				throw new M9000Exception("Error in reading dat file ",e);
			}
			System.out.println("absolute path "+comtradeFile.getPath());
			logger.debug("Absolute path... "+comtradeFile.getPath());
			String configFile = comtradeFile.getPath().substring(0, comtradeFile.getPath().lastIndexOf(".")+1)+"cfg";
//			isConfig = M9kUtils.getFileContent(configFile);
			try {
				isConfig = new FileInputStream(configFile);
			} catch (Exception e) {
				throw new M9000Exception("Error in reading config file ", e);
			}
			
//			ctConfigFile = new File (configFile);
//			if (!ctConfigFile.exists())
//			{
//				throw new M9000Exception("Corresponding config file is required");
//			}
		}
//		m9kUtils.startTimer("M9kComtradeParser - parseConfigFile()");
		logger.debug("About to parseConfigFile()");
		long start;
		long end;
		start = System.currentTimeMillis();
		parseConfigFile();
		end = System.currentTimeMillis();
		logger.debug("Total time to parse config file... "+(end - start));

		//		m9kUtils.endTimer("M9kComtradeParser - parseConfigFile()");
//		m9kUtils.startTimer("M9kComtradeParser - parseComtradeData()");
		logger.debug("About to parseComtradeData()");
		start = System.currentTimeMillis();
		if (eventsOnly)
		{
			parseComtradeDataForEvents();
		}
		else
		{
			parseComtradeData();
		}
		end = System.currentTimeMillis();
		logger.debug("Total time to parse data file... "+(end - start));
//		throw new M9000Exception("Test whether it logs error");
//		System.out.println("Done with parseComtradeData()");
//		m9kUtils.endTimer("M9kComtradeParser - parseComtradeData()");
//		parseInfFile();
	}

	public M9kComtradeParser(File comtradeFile, Map<String, Boolean> mapActiveEvents) throws M9000Exception {
		this.mapActiveEvents = mapActiveEvents;
		channelsUtil = new ChannelsUtil();
		receivedFileName = comtradeFile;
		logger.debug("Entered M9kComtradeParser..."+mapActiveEvents);
		System.out.println("starts with R? "+comtradeFile.getPath().startsWith("R")+" starts with / "+(comtradeFile.getPath().substring(1, 2))+"Is it true?"+(comtradeFile.getPath().startsWith("R") || comtradeFile.getPath().substring(1, 2).equals("R"))+ " received file name : "+comtradeFile.getPath());
		if (comtradeFile.getPath().startsWith("R") || comtradeFile.getPath().substring(1, 2).equals("R"))
		{
			if (comtradeFile.getName().endsWith(".dat"))
			{
	//			System.out.println("Checking for cfg file");
	//			ctDataFile = comtradeFile;
				try
				{
					urlcDat = getFileContent(comtradeFile.getPath()); 
					isDat = urlcDat.getInputStream();
					String configFile = comtradeFile.getPath().substring(0, comtradeFile.getPath().lastIndexOf(".")+1)+"cfg";
					System.out.println("config file "+configFile);
		//			ctConfigFile = new File (configFile);
					urlcConfig = getFileContent(configFile); 
					isConfig = urlcConfig.getInputStream();
					System.out.println(comtradeFile.getPath());
					logger.debug(" path... "+comtradeFile.getPath());
				}
				catch (Exception e) {
					logger.error("Error in getting URL connection to read comtrade files",e);
					if (!booTest)
					{
						throw new M9000Exception("Unable to read comtrade files");
					}
					else
					{
						try
						{
						System.out.println("In Exception: path "+comtradeFile.getPath());
	//					isDat = new FileInputStream(comtradeFile);
						isDat = new FileInputStream(new File(comtradeFile.getPath()));
						logger.debug(" path... "+comtradeFile.getPath());
						String configFile = comtradeFile.getPath().substring(0, comtradeFile.getPath().lastIndexOf(".")+1)+"cfg";
						System.out.println("In Exception: config file "+configFile);
			//			ctConfigFile = new File (configFile);
						isConfig =  new FileInputStream(new File(configFile));
						}
						catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
//			if (!ctConfigFile.exists())
//			{
//				throw new M9000Exception("Corresponding config file is required");
//			}
			}
		}
		else
		{
			try
			{
			System.out.println("In else path: "+comtradeFile.getPath());
//					isDat = new FileInputStream(comtradeFile);
			isDat = new FileInputStream(new File(comtradeFile.getPath()));
			logger.debug(" path... "+comtradeFile.getPath());
			String configFile = comtradeFile.getPath().substring(0, comtradeFile.getPath().lastIndexOf(".")+1)+"cfg";
			System.out.println("config file "+configFile);
//			ctConfigFile = new File (configFile);
			isConfig =  new FileInputStream(new File(configFile));
			}
			catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		if (mapActiveEvents != null)
		{
			totalAbnormalChannels = mapActiveEvents.size();
			activeDigitalChannels = new Integer[totalAbnormalChannels];
//			activeDigitalChannels = mapActiveEvents.keySet().toArray(new Integer[0]);
		}
		logger.debug("About to parseConfigFile()");
		long start;
		long end;
		start = System.currentTimeMillis();
		parseConfigFile();	
		end = System.currentTimeMillis();
		logger.debug("Total time to parse config file... "+(end - start));

	}

	public M9kComtradeParser(StringBuffer confInputString, StringBuffer datInputString) throws M9000Exception {
		channelsUtil = new ChannelsUtil();
		parseConfigFile(confInputString);
		parseComtradeData(datInputString);
//		parseInfFile();
	}

	// Testing faultLocation calculation
	public M9kComtradeParser(File comtradeFile) throws M9000Exception {
		channelsUtil = new ChannelsUtil();
		receivedFileName = comtradeFile;
//		logger.info("Entered M9kComtradeParser...");
		logger.debug("Entered M9kComtradeParser...");
		if (comtradeFile.getName().endsWith(".dat"))
		{
//			System.out.println("Checking for cfg file");
//			ctDataFile = comtradeFile;
//			isDat = M9kUtils.getFileContent(comtradeFile.getPath());
			try {
				isDat = new FileInputStream(comtradeFile);
			} catch (Exception e) {
				throw new M9000Exception("Error in reading dat file ",e);
			}
			logger.debug("Absolute path... "+comtradeFile.getPath());
			String configFile = comtradeFile.getPath().substring(0, comtradeFile.getPath().lastIndexOf(".")+1)+"cfg";
//			isConfig = M9kUtils.getFileContent(configFile);
			try {
				isConfig = new FileInputStream(configFile);
			} catch (Exception e) {
				throw new M9000Exception("Error in reading config file ", e);
			}
			
//			ctConfigFile = new File (configFile);
//			if (!ctConfigFile.exists())
//			{
//				throw new M9000Exception("Corresponding config file is required");
//			}
		}
//		m9kUtils.startTimer("M9kComtradeParser - parseConfigFile()");
		logger.debug("About to parseConfigFile()");
		long start;
		long end;
		start = System.currentTimeMillis();
		parseConfigFile();
		end = System.currentTimeMillis();
		logger.debug("Total time to parse config file... "+(end - start));

		//		m9kUtils.endTimer("M9kComtradeParser - parseConfigFile()");
//		m9kUtils.startTimer("M9kComtradeParser - parseComtradeData()");
		logger.debug("About to parseComtradeData()");
		start = System.currentTimeMillis();
		end = System.currentTimeMillis();
		logger.debug("Total time to parse data file... "+(end - start));
//		throw new M9000Exception("Test whether it logs error");
//		System.out.println("Done with parseComtradeData()");
//		m9kUtils.endTimer("M9kComtradeParser - parseComtradeData()");
//		parseInfFile();
	}

	
	public void parseConfigFile() throws M9000Exception
	{
		start = System.currentTimeMillis();
		BufferedReader brConfig = null;
		String strLine;
		try {
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
			stationDetails = new StationInfo(strTokens[0]);
			stationDetails.setRecordingDeviceId(strTokens[1]);
			stationDetails.setComtradeStdRevYear(Integer.parseInt(strTokens[2]));
			
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
			stationDetails.setChannelsCount(Integer.parseInt(strTokens[0]));
			int analogChnlsCount = Integer.parseInt(strTokens[1].substring(0, strTokens[1].length()-1));
			int digitalChnlsCount = Integer.parseInt(strTokens[2].substring(0, strTokens[2].length()-1));
			stationDetails.setAnalogChannelsCount(analogChnlsCount);
			stationDetails.setDigitalChannelsCount(digitalChnlsCount);
			logger.debug("Total analog count "+analogChnlsCount);
//			System.out.println("Total analog count "+analogChnlsCount);
			logger.debug("Total Digital count.."+digitalChnlsCount);
			// Parse Analog channel info
			parseAnalogChannelsInfo(brConfig);
			
			// Parse Digital channel info
			parseDigitalChannelsInfo(brConfig);
			
			// Parse Line Frequency
			String lineFreq = brConfig.readLine();
			logger.debug("LIne freq "+lineFreq);
//			System.out.println("LIne freq "+lineFreq);

			if (lineFreq != null && !lineFreq.isEmpty())
			{
				stationDetails.setLineFrequency(Double.parseDouble(lineFreq));
			}
			else
			{
				stationDetails.setLineFrequency(0.0);
			}
			
			// Parse Sampling rate info
			int noOfSampleRates = Integer.parseInt(brConfig.readLine());
			stationDetails.setNoOfSamplingRates(noOfSampleRates);
			for (int i = 0; i < noOfSampleRates; i++) {
				strTokens = brConfig.readLine().split(",");
				stationDetails.addSampleRateCfg(new Double(strTokens[0]), new Integer(strTokens[1]));
			}
			
			// Parse Date/Time Stamps
			String dateString = brConfig.readLine();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy,HH:mm:ss");
			java.util.Date date = dateFormat.parse(dateString);
			Timestamp dataValueTime = new Timestamp(date.getTime());
			String microSeconds = dateString.substring(dateString.indexOf(".")+1);
			int multFactor = (int)Math.pow(10.0, (9-microSeconds.length()));
			dataValueTime.setNanos(Integer.parseInt(microSeconds)*multFactor);
			stationDetails.setTimeOfFirstDataVal(dataValueTime);
			
			dateString = brConfig.readLine();
			date = dateFormat.parse(dateString);
			Timestamp triggerPointTime = new Timestamp(date.getTime());
			microSeconds = dateString.substring(dateString.indexOf(".")+1);
			multFactor = (int)Math.pow(10.0, (9-microSeconds.length()));
			triggerPointTime.setNanos(Integer.parseInt(microSeconds)*multFactor);
			stationDetails.setTimeOfTriggerPoint(triggerPointTime);
			
			// parse File type
			stationDetails.setFileType(brConfig.readLine());
			
			// Parse time multiplication factor
			stationDetails.setTimeMult(Double.parseDouble(brConfig.readLine()));
			end = System.currentTimeMillis();
			logger.debug("Time taken for parseConfigFile() in M9kParser "+(end-start));
			
		} catch (FileNotFoundException fnfe) {
			throw new M9000Exception(fnfe);
		} catch (IOException ioe) {
			throw new M9000Exception(ioe);
		} catch (NumberFormatException nfe) {
			throw new M9000Exception("Invalid Config File Format", nfe);
		} catch (ParseException pe) {
			throw new M9000Exception(pe);
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
			if (urlcConfig != null)
			{
				urlcConfig.disconnect();
			}
		}
	}

	public void parseConfigFile(StringBuffer confInputString) throws M9000Exception
	{
		BufferedReader brConfig = null;
		try {
//			brConfig = new BufferedReader(new InputStreamReader(isConf));
//			System.out.println("String to be parsed.."+confInputString);
			brConfig = new BufferedReader(new StringReader(confInputString.toString()));
			
			// Process the First line of the CFG file
			String stationDetailsLine = brConfig.readLine();
//			System.out.println("First line read..."+stationDetailsLine);
			String strTokens[] = stationDetailsLine.split(",");
			if (strTokens.length != 3)
			{
				throw new M9000Exception("Invalid Config File Format");
			}
			stationDetails = new StationInfo(strTokens[0]);
			stationDetails.setRecordingDeviceId(strTokens[1]);
			stationDetails.setComtradeStdRevYear(Integer.parseInt(strTokens[2]));
			
			// Process the second line of the CFG file
			String channelsCntString = brConfig.readLine();
//			System.out.println("Second line read..."+channelsCntString);
			strTokens = channelsCntString.split(",");
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
			stationDetails.setChannelsCount(Integer.parseInt(strTokens[0]));
			int analogChnlsCount = Integer.parseInt(strTokens[1].substring(0, strTokens[1].length()-1));
			int digitalChnlsCount = Integer.parseInt(strTokens[2].substring(0, strTokens[2].length()-1));
			stationDetails.setAnalogChannelsCount(analogChnlsCount);
			stationDetails.setDigitalChannelsCount(digitalChnlsCount);
//			System.out.println("Total Analog count.."+digitalChnlsCount);
//			System.out.println("Total Digital count.."+digitalChnlsCount);
			// Parse Analog channel info
			parseAnalogChannelsInfo(brConfig);
			
			// Parse Digital channel info
			parseDigitalChannelsInfo(brConfig);
			
			// Parse Line Frequency
			String lineFreq = brConfig.readLine();
			
			if (lineFreq != null && !lineFreq.isEmpty())
			{
				stationDetails.setLineFrequency(Double.parseDouble(lineFreq));
			}
			else
			{
				stationDetails.setLineFrequency(0.0);
			}
			
			// Parse Sampling rate info
			int noOfSampleRates = Integer.parseInt(brConfig.readLine());
			stationDetails.setNoOfSamplingRates(noOfSampleRates);
			if (noOfSampleRates > 0)
			{
				for (int i = 0; i < noOfSampleRates; i++) {
					strTokens = brConfig.readLine().split(",");
					stationDetails.addSampleRateCfg(new Double(strTokens[0]), new Integer(strTokens[1]));
				}
			}
			else
			{
				strTokens = brConfig.readLine().split(",");
				stationDetails.addSampleRateCfg(new Double(strTokens[0]), new Integer(strTokens[1]));
			}
			
			// Parse Date/Time Stamps
			String dateString = brConfig.readLine();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy,HH:mm:ss");
			java.util.Date date = dateFormat.parse(dateString);
			Timestamp dataValueTime = new Timestamp(date.getTime());
			String microSeconds = dateString.substring(dateString.indexOf(".")+1);
			int multFactor = (int)Math.pow(10.0, (9-microSeconds.length()));
			dataValueTime.setNanos(Integer.parseInt(microSeconds)*multFactor);
			stationDetails.setTimeOfFirstDataVal(dataValueTime);
			
			dateString = brConfig.readLine();
			date = dateFormat.parse(dateString);
			Timestamp triggerPointTime = new Timestamp(date.getTime());
			microSeconds = dateString.substring(dateString.indexOf(".")+1);
			multFactor = (int)Math.pow(10.0, (9-microSeconds.length()));
			triggerPointTime.setNanos(Integer.parseInt(microSeconds)*multFactor);
			stationDetails.setTimeOfTriggerPoint(triggerPointTime);
			
			// parse File type
			stationDetails.setFileType(brConfig.readLine());
			
			// Parse time multiplication factor
			stationDetails.setTimeMult(Double.parseDouble(brConfig.readLine()));
			
			
		} catch (FileNotFoundException fnfe) {
			throw new M9000Exception(fnfe);
		} catch (IOException ioe) {
			throw new M9000Exception(ioe);
		} catch (NumberFormatException nfe) {
			throw new M9000Exception("Invalid Config File Format", nfe);
		} catch (ParseException pe) {
			throw new M9000Exception(pe);
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
	}
	
	public void parseComtradeData() throws M9000Exception
	{
		start = System.currentTimeMillis();
		short sData;
		double chnlMultiplier;
		double chnlOffset;
		BufferedInputStream dis = null;
		if (stationDetails.getFileType().equalsIgnoreCase("BINARY"))
		{
			logger.debug("Binary file...");
			try {
				int eventChannelCnt = stationDetails.getDigitalChannelsCount();
//				FileInputStream fstream = new FileInputStream(ctDataFile);
//				FileChannel channel = new RandomAccessFile(ctDataFile, "r").getChannel();
//				dis = new BufferedInputStream(new FileInputStream(ctDataFile));
				dis = new BufferedInputStream(isDat);
				int totalBytesPerSample = (int) ((stationDetails.getAnalogChannelsCount() * 2) + (2 * Math.ceil((float)eventChannelCnt/16))+4+4);
				int i = 0;
				int totalNumberOfSamples = 0;

				if (stationDetails.getNoOfSamplingRates() > 0)
				{
					totalNumberOfSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates()-1);
				}
				else
				{
					totalNumberOfSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates());
				}
//				long start1 = System.currentTimeMillis();
//				ByteBuffer roBuff = channel.map(MapMode.READ_ONLY, 0, ctDataFile.length());
//				roBuff.order(ByteOrder.LITTLE_ENDIAN);
				ByteBuffer roBuff ;
//				long end1 = System.currentTimeMillis();
//				System.out.println("Time taken to read bytes in parseBinaryData "+(end1-start1));
//				byte[] bytes = new byte[totalBytesPerSample*totalNumberOfSamples];
				byte[] bytes = new byte[totalBytesPerSample];
				analogData = new double[stationDetails.getAnalogChannelsCount()][totalNumberOfSamples];
				digitalData = new short[totalNumberOfSamples][eventChannelCnt];
				abnormalDigiChnl = new int [eventChannelCnt];
				dataTimestamps = new int[totalNumberOfSamples];
				short digitalChnl = 0;
				short numOfDigitalBytes = (short)(Math.ceil((float)eventChannelCnt/16)); 
				String digitalBits;
				int iBitCnt = 0;
				int ichnlCnt = 0;
				int totalNoOfDigitalChnl = 0;
//				ArrayList<ChannelInfo> digitalChnlConfList;
//				digitalChnlConfList = (ArrayList<ChannelInfo>)stationDetails.getDigitalInfoCollection();
				int[] eventChnlStatus = getChannelConfigurationStatus();
				String zeros = "0000000000000000";
				String leadZero;

				long start1 = System.currentTimeMillis();
//				dis.read(bytes);
				long end1 = System.currentTimeMillis();
//				System.out.println("Time taken to read bytes in parseBinaryData "+(end1-start1));
				start1 = System.currentTimeMillis();
//				roBuff = ByteBuffer.wrap(bytes);
//				roBuff.order(ByteOrder.LITTLE_ENDIAN);
				end1 = System.currentTimeMillis();
//				System.out.println("Time taken to wrap bytes as BYte Buffer in parseBinaryData "+(end1-start1));
				start1 = System.currentTimeMillis();
				int bytesRead;
				System.out.println("\t\tCORRUPT:totalBytesPerSample "+totalBytesPerSample);
				while (i<totalNumberOfSamples)
				{
					bytesRead = dis.read(bytes);
					System.out.println("\t\tCORRUPT:Bytes read "+bytesRead);
					roBuff = ByteBuffer.wrap(bytes);
					roBuff.order(ByteOrder.LITTLE_ENDIAN);
					roBuff.getInt();

					dataTimestamps[i] = roBuff.getInt();
					
					ichnlCnt = 0;
					while (ichnlCnt < stationDetails.getAnalogChannelsCount())
					{
						chnlMultiplier = ((AnalogInfo)((ArrayList<ChannelInfo>)stationDetails.getAnalogInfoCollection()).get(ichnlCnt)).getChnlMultiplier();
						chnlOffset = ((AnalogInfo)((ArrayList<ChannelInfo>)stationDetails.getAnalogInfoCollection()).get(ichnlCnt)).getChnlOffset();
						sData = roBuff.getShort();
						if (sData == M9kConstants.BINARY_MISSING_DATA.shortValue())
						{
							analogData[ichnlCnt][i] = 0;
						}
						else
						{
							analogData[ichnlCnt][i] = (chnlMultiplier*sData)+chnlOffset;
						}
						ichnlCnt++;
					}
					ichnlCnt = 0;
					totalNoOfDigitalChnl = 0;
					while (ichnlCnt++ < numOfDigitalBytes)
					{
						digitalChnl = roBuff.getShort();//Short.reverseBytes(roBuff.getShort());
						digitalBits = Integer.toBinaryString(digitalChnl);
						if (digitalBits.length() > 16)
						{
							digitalBits = digitalBits.substring(digitalBits.length() - 16);
							
						}
						else
						{
//							digitalBits = stuffLeadingzeroes(digitalBits);							
							leadZero = zeros.substring(0,16-digitalBits.length());
							digitalBits=leadZero.concat(digitalBits);
						}
						iBitCnt = 16;
						while (iBitCnt > 0 && totalNoOfDigitalChnl < eventChannelCnt)
						{
							digitalData[i][totalNoOfDigitalChnl] = Short.parseShort(digitalBits.substring(iBitCnt-1, iBitCnt));
								if (eventChnlStatus[totalNoOfDigitalChnl] == 0) // y = 0 indicates OPEN in config file
								{
									if (digitalData[i][totalNoOfDigitalChnl] == 1 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0)
									{
										abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
										totalAbnormalChannels++;
									}
								}
								else if (eventChnlStatus[totalNoOfDigitalChnl] == 1) // y = 0 indicates OPEN in config file
								{
									if (digitalData[i][totalNoOfDigitalChnl] == 0 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0)
									{
										abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
										totalAbnormalChannels++;
									}
								}
								
								totalNoOfDigitalChnl++;
							iBitCnt--;
						}
					}
					i++;
				}
				end1 = System.currentTimeMillis();
				System.out.println("Time taken for while loop in parseBinaryData "+(end1-start1));
				eventChnlStatus = null;
				bytes = null;
				roBuff = null;

			} catch (FileNotFoundException fnfe) {
				throw new M9000Exception(fnfe);
			} catch (IOException ioe) {
				throw new M9000Exception(ioe);
			}
			catch (Exception ioe) {
				throw new M9000Exception(ioe);
			}
			finally
			{
				end = System.currentTimeMillis();
				logger.debug("Time taken for ParseComtradeData binary in M9kParser "+(end-start));
				System.out.println("Time taken for ParseComtradeData binary in M9kParser "+(end-start));
				try {
					if (dis != null)
					{
						dis.close();
						dis = null;
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
				if (isDat != null)
				{
					try {
						isDat.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (urlcDat != null)
				{
					urlcDat.disconnect();
				}

			}
		}
		else if (stationDetails.getFileType().equalsIgnoreCase("ASCII"))
		{
			logger.debug("ASCII File...");
			BufferedReader bfr = null;
			try
			{
			bfr = new BufferedReader(new InputStreamReader(isDat));
			int totalColumns = (int) (stationDetails.getAnalogChannelsCount() + stationDetails.getDigitalChannelsCount()+2);
//			logger.debug("Total cols: "+totalColumns+" analog cnt: "+stationDetails.getAnalogChannelsCount()+" Digital cnt: "+stationDetails.getDigitalChannelsCount());
			String[] data = new String[totalColumns];
			String strLine = "";
			int i = 0;
			int totalNumberOfSamples = 0;
			if (stationDetails.getNoOfSamplingRates() > 0)
			{
				totalNumberOfSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates()-1);
			}
			else
			{
				totalNumberOfSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates());
			}
			logger.debug("totalNumberOfSamples..."+totalNumberOfSamples);
			analogData = new double[stationDetails.getAnalogChannelsCount()][totalNumberOfSamples];
			dataTimestamps = new int[totalNumberOfSamples];
			digitalData = new short[totalNumberOfSamples][stationDetails.getDigitalChannelsCount()];
			abnormalDigiChnl = new int [stationDetails.getDigitalChannelsCount()];
			
			int iData;
			int ichnlCnt = 0;
			int totalNoOfDigitalChnl = 0;
			ArrayList<ChannelInfo> digitalChnlConfList;
			digitalChnlConfList = (ArrayList<ChannelInfo>)stationDetails.getDigitalInfoCollection();
			int columnsCnt = 0;
			int status = 0;
			int analogCnt = stationDetails.getAnalogChannelsCount();
			int digitalCnt = stationDetails.getDigitalChannelsCount();
			logger.debug("Analog Channels Count..."+analogCnt);
			logger.debug("Digital Channels Count..."+digitalCnt);
			System.out.println("Analog Channels Count..."+analogCnt);
			System.out.println("Digital Channels Count..."+digitalCnt);
//			m9kUtils.startTimer("\n\n\t\t\tParsing Dat file");
			// START: 21-Feb-2016: Removed ready() function as it might cause failure like in Dunwoodie, where fault did not open
//			while (bfr.ready() && i<totalNumberOfSamples)
			while (i<totalNumberOfSamples)
			// END: 21-Feb-2016
			{
				strLine = bfr.readLine();
				data = strLine.split(",");
				//test
				columnsCnt=1;
//				logger.debug("Lines read..."+i + " \n Data read: "+strLine);
//				System.out.println("Lines read..."+i + " \n Data read: "+strLine);

				dataTimestamps[i] = Integer.parseInt(data[columnsCnt++]);
				
				ichnlCnt = 0;
				while (ichnlCnt < analogCnt)
				{
					chnlMultiplier = ((AnalogInfo)((ArrayList<ChannelInfo>)stationDetails.getAnalogInfoCollection()).get(ichnlCnt)).getChnlMultiplier();
					chnlOffset = ((AnalogInfo)((ArrayList<ChannelInfo>)stationDetails.getAnalogInfoCollection()).get(ichnlCnt)).getChnlOffset();
					iData = Integer.parseInt(data[columnsCnt++]);
					if (iData == M9kConstants.ASCII_MISSING_DATA)
					{
						analogData[ichnlCnt][i] = 0;
					}
					else
					{
						analogData[ichnlCnt][i] = (chnlMultiplier*iData)+chnlOffset;
					}
					
//					System.out.print(analogData[i][ichnlCnt]+" , ");
					ichnlCnt++;
				}
//				logger.debug("Columns Cnt..."+columnsCnt);
				ichnlCnt = 0;
				totalNoOfDigitalChnl = 0;
//				System.out.println("\n\n\t\t\t\t Column after analog..."+columnsCnt);
				while (totalNoOfDigitalChnl < digitalCnt)
				{
//					logger.debug("Digital Columns Cnt..."+columnsCnt+" i-> "+i + " totalNoOfDigitalChnl "+totalNoOfDigitalChnl);
//					System.out.println("Digital Columns Cnt..."+columnsCnt+" i-> "+i + " totalNoOfDigitalChnl "+totalNoOfDigitalChnl);
//					System.out.println("Digital Columns Cnt..."+columnsCnt+" i-> "+i + " totalNoOfDigitalChnl "+totalNoOfDigitalChnl);
						digitalData[i][totalNoOfDigitalChnl] = Short.parseShort(data[columnsCnt++]);
						status = ((DigitalInfo)digitalChnlConfList.get(totalNoOfDigitalChnl)).getStatus(); 
							if ( status == 0) // y = 0 indicates OPEN in config file
							{
								if (digitalData[i][totalNoOfDigitalChnl] == 1 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0)
								{
//									System.out.println("\t\t\t\t\tabnormal index i "+i+" totalNoOfDigitalChnl "+totalNoOfDigitalChnl);
									abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
									totalAbnormalChannels++;
								}
							}
							else // y = 0 indicates OPEN in config file
							{
//								System.out.println("\n\t\t\t\tStatus "+((DigitalInfo)digitalChnlConfList.get(totalNoOfDigitalChnl)).getStatus()+" chnl Id "+((DigitalInfo)digitalChnlConfList.get(totalNoOfDigitalChnl)).getChnlId());
//								System.out.println("\n\t\t\t\t Data value... "+digitalData[i][totalNoOfDigitalChnl]);
								if (digitalData[i][totalNoOfDigitalChnl] == 0 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0)
								{
//									System.out.println("\t\t\tabnormal2 index i "+i+" totalNoOfDigitalChnl "+totalNoOfDigitalChnl);
									abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
									totalAbnormalChannels++;
								}
							}
							totalNoOfDigitalChnl++;
				}
				i++;
				
			}
//			m9kUtils.endTimer("\n\n\t\t\tParsing Dat file");
			logger.debug("analogData..."+analogData.length);
			logger.debug("digitalData..."+digitalData.length);
			
		} catch (FileNotFoundException fnfe) {
			throw new M9000Exception(fnfe);
		} catch (IOException ioe) {
			throw new M9000Exception(ioe);
		}catch (Exception e) {
			throw new M9000Exception(e);
		}
		finally
		{
			end = System.currentTimeMillis();
			logger.debug("Time taken for ParseComtradeData ASCII in M9kParser "+(end-start));

			try {
				if (bfr != null)
				{
					bfr.close();
					bfr = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
			if (isDat != null)
			{
				try {
					isDat.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (urlcDat != null)
			{
				urlcDat.disconnect();
			}

		}

		}
	}
	
	public void parseComtradeData(StringBuffer datInputString) throws M9000Exception
	{
		ByteArrayInputStream dis = null;
		if (stationDetails.getFileType().equalsIgnoreCase("BINARY"))
		{
			short sData;
			String zeros = "0000000000000000";
			String leadZero;
			logger.debug("Binary file...");
			try {
				dis = new ByteArrayInputStream(datInputString.toString().getBytes());
				int totalBytesPerSample = (int) ((stationDetails.getAnalogChannelsCount() * 2) + (2 * Math.ceil((float)stationDetails.getDigitalChannelsCount()/16))+4+4);
				byte[] bytes = new byte[totalBytesPerSample];
				ByteBuffer roBuff;
				int i = 0;
				int totalNumberOfSamples = 0;
				if (stationDetails.getNoOfSamplingRates() > 0)
				{
					totalNumberOfSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates()-1);
				}
				else
				{
					totalNumberOfSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates());
				}
				analogData = new double[stationDetails.getAnalogChannelsCount()][totalNumberOfSamples];
				digitalData = new short[totalNumberOfSamples][stationDetails.getDigitalChannelsCount()];
				abnormalDigiChnl = new int [stationDetails.getDigitalChannelsCount()];
				dataTimestamps = new int[totalNumberOfSamples];
				short digitalChnl = 0;
				short numOfDigitalBytes = (short)(Math.ceil((float)stationDetails.getDigitalChannelsCount()/16)); 
				String digitalBits;
				int iBitCnt = 0;
				int ichnlCnt = 0;
				int totalNoOfDigitalChnl = 0;
				ArrayList<ChannelInfo> digitalChnlConfList;
				digitalChnlConfList = (ArrayList<ChannelInfo>)stationDetails.getDigitalInfoCollection();
				while (dis.available()>0 && i<totalNumberOfSamples)
				{
					dis.read(bytes);
					roBuff = ByteBuffer.wrap(bytes);
					roBuff.order(ByteOrder.LITTLE_ENDIAN);
					roBuff.getInt();

					dataTimestamps[i] = roBuff.getInt();
					
					ichnlCnt = 0;
					while (ichnlCnt < stationDetails.getAnalogChannelsCount())
					{
						sData = roBuff.getShort();
						if (sData == M9kConstants.BINARY_MISSING_DATA.shortValue())
						{
							analogData[ichnlCnt][i] = 0;
						}
						else
						{
							analogData[ichnlCnt][i] = sData;
						}
						ichnlCnt++;
					}
					ichnlCnt = 0;
					totalNoOfDigitalChnl = 0;
					while (ichnlCnt++ < numOfDigitalBytes)
					{
						digitalChnl = roBuff.getShort();//Short.reverseBytes(roBuff.getShort());
						digitalBits = Integer.toBinaryString(digitalChnl);
						if (digitalBits.length() > 16)
						{
							digitalBits = digitalBits.substring(digitalBits.length()-16);
							
						}
//						digitalBits = stuffLeadingzeroes(digitalBits);
						leadZero = zeros.substring(0,16-digitalBits.length());
						digitalBits=leadZero.concat(digitalBits);
						iBitCnt = 16;
						while (iBitCnt > 0 && totalNoOfDigitalChnl < stationDetails.getDigitalChannelsCount())
						{
							digitalData[i][totalNoOfDigitalChnl] = Short.parseShort(digitalBits.substring(iBitCnt-1, iBitCnt));
								if ((((DigitalInfo)digitalChnlConfList.get(totalNoOfDigitalChnl)).getStatus() == 0)) // y = 0 indicates OPEN in config file
								{
									if (digitalData[i][totalNoOfDigitalChnl] == 1 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0)
									{
										abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
										totalAbnormalChannels++;
									}
								}
								else if ((((DigitalInfo)digitalChnlConfList.get(totalNoOfDigitalChnl)).getStatus() == 1)) // y = 0 indicates OPEN in config file
								{
									if (digitalData[i][totalNoOfDigitalChnl] == 0 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0)
									{
										abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
										totalAbnormalChannels++;
									}
								}
								totalNoOfDigitalChnl++;
							iBitCnt--;
						}
					}
					i++;
				}
			} catch (FileNotFoundException fnfe) {
				throw new M9000Exception(fnfe);
			} catch (IOException ioe) {
				throw new M9000Exception(ioe);
			} catch (Exception e) {
				throw new M9000Exception(e);
			}
			finally
			{
				try {
					if (dis != null)
					{
						dis.close();
						dis = null;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			}
		}
		else if (stationDetails.getFileType().equalsIgnoreCase("ASCII"))
		{
			logger.debug("ASCII File...");
			BufferedReader bfr = null;
			Double iData;
			try
			{
			bfr = new BufferedReader(new StringReader(datInputString.toString()));
			int totalColumns = (int) (stationDetails.getAnalogChannelsCount() + stationDetails.getDigitalChannelsCount()+2);
			String[] data = new String[totalColumns];
			String strLine = "";
			int i = 0;
			int totalNumberOfSamples = 0;
			if (stationDetails.getNoOfSamplingRates() > 0)
			{
				totalNumberOfSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates()-1);
			}
			else
			{
				totalNumberOfSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates());
			}
			logger.debug("totalNumberOfSamples..."+totalNumberOfSamples);
			analogData = new double[stationDetails.getAnalogChannelsCount()][totalNumberOfSamples];
			digitalData = new short[totalNumberOfSamples][stationDetails.getDigitalChannelsCount()];
			abnormalDigiChnl = new int [stationDetails.getDigitalChannelsCount()];
			dataTimestamps = new int[totalNumberOfSamples];
			int ichnlCnt = 0;
			int totalNoOfDigitalChnl = 0;
			ArrayList<ChannelInfo> digitalChnlConfList;
			digitalChnlConfList = (ArrayList<ChannelInfo>)stationDetails.getDigitalInfoCollection();
			int columnsCnt = 0;
			logger.debug("Analog Channels Count..."+stationDetails.getAnalogChannelsCount());
			logger.debug("Digital Channels Count..."+stationDetails.getDigitalChannelsCount());
			// START: 21-Feb-2016: Removed ready() function as it might cause failure like in Dunwoodie, where fault did not open
//			while (bfr.ready() && i<totalNumberOfSamples)
			while (i<totalNumberOfSamples)
			// END: 21-Feb-2016
			{
				strLine = bfr.readLine();
				if (strLine == null || strLine.isEmpty())
				{
					break;
				}
				data = strLine.split(",");
				//test
				columnsCnt=1;
//				logger.debug("Lines read..."+i + " \n Data read: "+strLine);

				dataTimestamps[i] = Integer.parseInt(data[columnsCnt++]);
				ichnlCnt = 0;
				while (ichnlCnt < stationDetails.getAnalogChannelsCount())
				{
					iData = Double.parseDouble(data[columnsCnt++]);
					if (iData.intValue() == M9kConstants.ASCII_MISSING_DATA)
					{
						analogData[ichnlCnt][i] = 0;
					}
					else
					{
						analogData[ichnlCnt][i] = iData;
					}
					
//					System.out.print(analogData[i][ichnlCnt]+" , ");
					ichnlCnt++;
				}
//				logger.debug("Columns Cnt..."+columnsCnt);
				ichnlCnt = 0;
				totalNoOfDigitalChnl = 0;
				while (totalNoOfDigitalChnl < stationDetails.getDigitalChannelsCount())
				{
//					logger.debug("Digital Columns Cnt..."+columnsCnt);
						digitalData[i][totalNoOfDigitalChnl] = Short.parseShort(data[columnsCnt++]);
							if ((((DigitalInfo)digitalChnlConfList.get(totalNoOfDigitalChnl)).getStatus() == 0)) // y = 0 indicates OPEN in config file
							{
								if (digitalData[i][totalNoOfDigitalChnl] == 1 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0)
								{
									abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
									totalAbnormalChannels++;
								}
							}
							else if ((((DigitalInfo)digitalChnlConfList.get(totalNoOfDigitalChnl)).getStatus() == 1)) // y = 0 indicates OPEN in config file
							{
								if (digitalData[i][totalNoOfDigitalChnl] == 0 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0)
								{
									abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
									totalAbnormalChannels++;
								}
							}
							totalNoOfDigitalChnl++;
				}
			
				i++;
				
			}
			logger.debug("analogData..."+analogData.length);
			logger.debug("digitalData..."+digitalData.length);
			
		} catch (FileNotFoundException fnfe) {
			throw new M9000Exception(fnfe);
		} catch (IOException ioe) {
			throw new M9000Exception(ioe);
		}catch (Exception e) {
			throw new M9000Exception(e);
		}
		finally
		{
			try {
				if (bfr != null)
				{
					bfr.close();
					bfr = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}

		}
	}

	public void parseInfFile() throws M9000Exception
	{
		BufferedReader brConfig = null;
		
		try {
//			String filename = ctConfigFile.getParent()+"/"+ctConfigFile.getName().substring(0, ctConfigFile.getName().indexOf("."))+".inf";
			String filename = receivedFileName.getParent()+"/"+receivedFileName.getName().substring(0, receivedFileName.getName().indexOf("."))+".inf";
			File infFile = new File(filename);
			if (infFile.exists())
			{
				mapTriggerInfo = new WeakHashMap<String, String>();
				brConfig = new BufferedReader(new FileReader(infFile));
				String strLine = "";
				String tokens[] = new String[2];
				while(brConfig.ready())
				{
					if (brConfig.readLine().equals("[Triggers]"))
					{
						while(brConfig.ready())
						{
							strLine = brConfig.readLine();
							logger.debug("\t line: "+strLine);
							if (strLine != null && !strLine.isEmpty() && !strLine.startsWith("["))
							{
								tokens =strLine.split(",");
								mapTriggerInfo.put(tokens[0].trim(), tokens[1].trim());
							}
							else
							{
								break;
							}
						}
					}
				}
			}
			
		} catch (FileNotFoundException fnfe) {
			throw new M9000Exception(fnfe);
		} catch (IOException ioe) {
			throw new M9000Exception(ioe);
		}
		finally
		{
			try {
				if (brConfig != null)
				{
					brConfig.close();
					brConfig = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
	}
//	private String stuffLeadingzeroes (String bytes)
//	{
//		String result = "";
//		int len = bytes.length();
//		if ((16-len) > 0)
//		{
//			for (int i = 0; i < (16-len); i++) {
//				result+="0";
//			}
//		}
//		result+=bytes;
//		return result;
//	}
	public  int[] printBits(byte b) {
        int mask = 0x80;
        int bits[] = new int[8];
        int i = 0;
        while (mask > 0) {
            if ((mask & b) != 0) {
//                System.out.print('1');
            	bits[i++] = 1;
            } else {
//                System.out.print('0');
                bits[i++] = 0;
            }
            mask >>= 1;
        }
        return bits;
    }
	private void parseAnalogChannelsInfo(BufferedReader brConfig) throws M9000Exception
	{
		start = System.currentTimeMillis();
		int chnlCnt = 0;
		AnalogInfo analogChannelInfo;
		String strTokens[] = null;
		Collection<ChannelInfo> analogInfoList = new ArrayList<ChannelInfo>();
		// Process Analog Channels Info
		try {
			// START: 21-Feb-2016: Removed ready() function as it might cause failure like in Dunwoodie, where fault did not open
//			while (chnlCnt < stationDetails.getAnalogChannelsCount() && brConfig.ready())
			while (chnlCnt < stationDetails.getAnalogChannelsCount())
			// END: 21-Feb-2016	
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
//			System.out.println("Total analog channels list "+analogInfoList.size());
			stationDetails.setAnalogInfoCollection(analogInfoList);
			end = System.currentTimeMillis();
			logger.debug("Time taken for parseAnalogChannelsInfo in M9kParser "+(end-start));

		} catch (IOException e) {
			logger.error("Error occured during parseAnalogChannelsInfo",e);
		} catch (Exception e) {
			logger.error("Error occured during parseAnalogChannelsInfo",e);
		}
		
	}

	private void parseDigitalChannelsInfo(BufferedReader brConfig) throws M9000Exception
	{
		start = System.currentTimeMillis();
		int chnlCnt = 0;
		int activeIndex = 0;
		DigitalInfo digitalChannelInfo;
		String strTokens[] = null;
		Collection<ChannelInfo> digitalInfoList = new ArrayList<ChannelInfo>();
		// Process Analog Channels Info
		try {
			// 21-Feb-2016: START: Removed ready() function as it might cause failure like in Dunwoodie, where fault did not open
//			while (chnlCnt < stationDetails.getDigitalChannelsCount() && brConfig.ready())
			while (chnlCnt < stationDetails.getDigitalChannelsCount())
			// 21-Feb-2016: END
			{
				strTokens = brConfig.readLine().split(",");
				digitalChannelInfo = new DigitalInfo(Integer.parseInt(strTokens[0]));
				if (mapActiveEvents != null && mapActiveEvents.get(strTokens[1]) != null)
				{
					activeDigitalChannels[activeIndex++] = Integer.parseInt(strTokens[0])-1;
				}
				digitalChannelInfo.setChnlId(strTokens[1]);
//				logger.debug("Digital Channel ID.."+strTokens[1]);
				digitalChannelInfo.setPhaseId(strTokens[2]);
				digitalChannelInfo.setCircuitName(strTokens[3]);
//				logger.debug("Digital Circuit Name..."+strTokens[3]);
				digitalChannelInfo.setStatus(Integer.parseInt(strTokens[4]));
//				logger.debug("Digital chnl status: "+digitalChannelInfo.getStatus());
				digitalInfoList.add(digitalChannelInfo);
				chnlCnt++;
			}
//			System.out.println("Digitals events list "+digitalInfoList);
			stationDetails.setDigitalInfoCollection(digitalInfoList);
			end = System.currentTimeMillis();
			logger.debug("Time taken for parseDigitalChannelsInfo in M9kParser "+(end-start));

		}catch (IOException e) {
			logger.error("Error occured during parseDigitalChannelsInfo",e);
		} catch (Exception e) {
			logger.error("Error occured during parseDigitalChannelsInfo",e);
		}
		

	}


	public double[][] getAnalogData() throws M9000Exception
	{
//		start = System.currentTimeMillis();
//		double dataMatrix[][];
//		try {
//			int totalSamples = 0;
//			if (stationDetails.getNoOfSamplingRates() > 0)
//			{
//				totalSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates()-1);
//			}
//			else
//			{
//				totalSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates());
//			}
//			dataMatrix = new double[stationDetails.getAnalogChannelsCount()][totalSamples];
//			logger.debug("Creating DAT file to plot data...." + (totalSamples));
//				double chnlMultiplier = 0;
//				double chnlOffset = 0;
//				for (int i = 0; i < totalSamples; i++) {
//					for (int j = 0; j < stationDetails.getAnalogChannelsCount(); j++) {
//						chnlMultiplier = ((AnalogInfo)((ArrayList<ChannelInfo>)stationDetails.getAnalogInfoCollection()).get(j)).getChnlMultiplier();
//						chnlOffset = ((AnalogInfo)((ArrayList<ChannelInfo>)stationDetails.getAnalogInfoCollection()).get(j)).getChnlOffset();
////						logger.debug("chnlMultiplier..."+chnlMultiplier);
////						logger.debug("chnlOffset ... "+chnlOffset);
////						logger.debug("i " +i +" j "+j);
////						logger.debug("analogData ... "+analogData);
////						logger.debug("(chnlMultiplier * analogData[i][j]) + chnlOffset; ... "+(chnlMultiplier * analogData[i][j]) + chnlOffset);
//						if (analogData[i][j] != 99999)
//						{
//							dataMatrix[j][i] = (chnlMultiplier * analogData[i][j]) + chnlOffset;
//						}
//						else
//						{
//							dataMatrix[j][i] = analogData[i][j];
//						}
//					}
//				}
//				logger.debug("dataMatrix "+dataMatrix.length);
//				channelsUtil.createAnalogChannelsWithData(stationDetails.getAnalogChannelsCount(), dataMatrix);
//				end = System.currentTimeMillis();
//				logger.debug("Time taken for getAnalogData in M9kParser "+(end-start));
//				dataMatrix = null;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return channelsUtil.getAnalogChannelsData();	
		return analogData;
	}

//	public Collection<ArrayList<Double>> getSelectedAnalogChannelData(List<Integer> selectedChannels) throws M9000Exception
//	{
//		start = System.currentTimeMillis();
//		double dataMatrix[][];
//		try {
//			int totalSamples = 0;
//			if (stationDetails.getNoOfSamplingRates() > 0)
//			{
//				totalSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates()-1);
//			}
//			else
//			{
//				totalSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates());
//			}
//
//			dataMatrix = new double[stationDetails.getAnalogChannelsCount()][totalSamples];
//			logger.debug("Creating DAT file to plot data...." + (totalSamples * stationDetails.getAnalogChannelsCount() * 2));
//				double chnlMultiplier = 0;
//				double chnlOffset = 0;
//				int selectedIndex = 0;
//				for (int i = 0; i < totalSamples; i++) {
//					for (int j = 0; j < selectedChannels.size(); j++) {
//						selectedIndex = ((Integer)selectedChannels.get(selectedIndex)).intValue();
//						chnlMultiplier = ((AnalogInfo)((ArrayList<ChannelInfo>)stationDetails.getAnalogInfoCollection()).get(selectedIndex)).getChnlMultiplier();
//						chnlOffset = ((AnalogInfo)((ArrayList<ChannelInfo>)stationDetails.getAnalogInfoCollection()).get(selectedIndex)).getChnlOffset();
//						dataMatrix[j][i] = (chnlMultiplier * analogData[i][selectedIndex]) + chnlOffset;
//						
//					}
//					
//				}
//				channelsUtil.createAnalogChannelsWithData(stationDetails.getAnalogChannelsCount(), dataMatrix);
//			
//		} catch (Exception e) {
//		}
//		end = System.currentTimeMillis();
//		logger.debug("Time taken for getAnalogData in M9kParser "+(end-start));
//		dataMatrix = null;
//		return channelsUtil.getAnalogChannelsData();	
//	}

//	public ArrayList<ArrayList<Integer>> getDigitalData() throws Exception
//	{
//		System.out.println("Entered getDigitalData method ");
//		ArrayList<ArrayList<Integer>> lstEventData;
//		start = System.currentTimeMillis();
//		int totalSamples = 0;
//		if (stationDetails.getNoOfSamplingRates() > 0)
//		{
//			totalSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates()-1);
//		}
//		else
//		{
//			totalSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates());
//		}
////		int dataMatrix[][] = new int[totalAbnormalChannels][totalSamples];
//		lstEventData = new ArrayList<ArrayList<Integer>>(totalAbnormalChannels);
//			int[] sortedDigitalChnl = (int[])abnormalDigiChnl.clone();
////			for (int i = 0; i < abnormalDigiChnl.length; i++) {
////				if (abnormalDigiChnl[i] > 0)
////				{
////					System.out.println("UNSorted channel list "+i+" -> "+abnormalDigiChnl[i]);
////				}
////				
////			}
//			Arrays.sort(sortedDigitalChnl);
////			for (int i = 0; i < sortedDigitalChnl.length; i++) {
////				if (sortedDigitalChnl[i] > 0)
////				{
////					System.out.println("Sorted channel list "+i+" -> "+sortedDigitalChnl[i]);
////				}
////				
////			}
//			activeDigitalChannels = new int[totalAbnormalChannels];
//			int indexToBeWritten = 0;
//			for (int i = sortedDigitalChnl.length - 1; i >= 0; i--) {
//				for (int j = 0; j < abnormalDigiChnl.length; j++) {
//					if (sortedDigitalChnl[i] > 0 && sortedDigitalChnl[i] == abnormalDigiChnl[j])
//					{
////						System.out.println("J value after sorting... "+j);
//						activeDigitalChannels[indexToBeWritten] = j;
//						abnormalDigiChnl[j] = 0;
//						indexToBeWritten++;
//						break;
//					}						
//				}
//			}
//
//			
//			// PERFORMANCE
//			//		for (int i = 0; i < totalSamples; i++) {
////			eventData = new ArrayList<Integer>();
////				for (int j = 0; j < activeDigitalChannels.length; j++) {
////					dataMatrix[j][i] = digitalData[i][activeDigitalChannels[j]];
////				}
////		} 
////			channelsUtil.createDigitalChannelsWithData(getTotalAbnormalChannels(), dataMatrix);
//			
//			
//			ArrayList<Integer> eventData;
//			for (int i = 0; i < activeDigitalChannels.length; i++) {
//				eventData = new ArrayList<Integer>(totalSamples);
//				for (int j = 0; j < totalSamples; j++) {
//					eventData.add((int)digitalData[j][activeDigitalChannels[i]]);
//				}
//				lstEventData.add(eventData);
////				System.out.println("size of the event data for channel "+i+" is "+eventData.size());
//			}
//			channelsUtil.setDigitalChannelsData(lstEventData);
//		end = System.currentTimeMillis();
//		logger.info("Time taken for getDigitalData in M9kParser "+(end-start));
////		dataMatrix = null;
//		sortedDigitalChnl = null;
//		return channelsUtil.getDigitalChannelsData();
//	}

	public ArrayList<ArrayList<Integer>> constructDigitalData() throws Exception
	{
		System.out.println("Entered getDigitalData method "+digitalData.length);
		ArrayList<ArrayList<Integer>> lstEventData;
		start = System.currentTimeMillis();
		ArrayList<Integer> eventData;
		int totalSamples = 0;
		if (stationDetails.getNoOfSamplingRates() > 0)
		{
			totalSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates()-1);
		}
		else
		{
			totalSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates());
		}
		lstEventData = new ArrayList<ArrayList<Integer>>(totalAbnormalChannels);
//		for (int i = 0; i < mapActiveEvents.keySet().size(); i++) {
//		if (mapActiveEvents != null)
//		{
//			for (Iterator<Integer> iterator = mapActiveEvents.keySet().iterator(); iterator
//				.hasNext();) {
			for (int i = 0; i < activeDigitalChannels.length; i++) {
				eventData = new ArrayList<Integer>(totalSamples);
//				System.out.println("About to add channel " +activeDigitalChannels[i]);
				for (int j = 0; j < totalSamples; j++) {
					eventData.add((int)digitalData[j][activeDigitalChannels[i]]);
				}
				lstEventData.add(eventData);
			}
			
//			for (int i = getActiveEventsInOrderOccured().size()-1; i >= 0; i--) {
//				eventData = new ArrayList<Integer>(totalSamples);
//				for (int j = 0; j < totalSamples; j++) {
//					eventData.add((int)digitalData[j][activeDigitalChannels[getActiveEventsInOrderOccured().get(i)]]);
//				}
//				lstEventData.add(eventData);
//			}
//		}
//		else
//		{
//			for (int i = 0; i < abnormalDigiChnl.length; i++) {
//				if (abnormalDigiChnl[i] > 0)
//				{
//					eventData = new ArrayList<Integer>(totalSamples);
//					for (int j = 0; j < totalSamples; j++) {
//						eventData.add((int)digitalData[j][abnormalDigiChnl[i]-1]);
//					}
//					lstEventData.add(eventData);
//				}
//			}
//		}
		channelsUtil.setDigitalChannelsData(lstEventData);
		end = System.currentTimeMillis();
		logger.debug("Time taken for getDigitalData in M9kParser "+(end-start));
		return channelsUtil.getDigitalChannelsData();
	}

	/**
	 * @return the stationDetails
	 */
	public StationInfo getStationDetails() {
		return stationDetails;
	}


	/**
	 * @param stationDetails the stationDetails to set
	 */
	public void setStationDetails(StationInfo stationDetails) {
		this.stationDetails = stationDetails;
	}

	/**
	 * @return the channelsUtil
	 */
	public ChannelsUtil getChannelsUtil() {
		return channelsUtil;
	}


	/**
	 * @param channelsUtil the channelsUtil to set
	 */
	public void setChannelsUtil(ChannelsUtil channelsUtil) {
		this.channelsUtil = channelsUtil;
	}

	


	/**
	 * @param args
	 * @throws M9000Exception 
	 */
	public static void main(String[] args) throws M9000Exception {
//		File file = new File("//195.1.1.13/M9k/Data/2-Kettle Creek/3893_111026,090852686,-5t,Kettle Creek,USI M9000,USI.dat");
//		M9kComtradeParser ctParser =  new M9kComtradeParser(file, false);
//		try {
//			ctParser.parseConfigFile();
			// Test
//			ctParser.logger.debug(ctParser.stationDetails);
//			ctParser.parseComtradeData();
//			ctParser.getDigitalData();
//			ctParser.writeAnalogDataIntoFile();
//			ctParser.writeDigitalDataIntoFile();
//		} catch (M9000Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}


	/**
	 * @return the totalAbnormalChannels
	 */
	public int getTotalAbnormalChannels() {
		return totalAbnormalChannels;
	}


	/**
	 * @param totalAbnormalChannels the totalAbnormalChannels to set
	 */
	public void setTotalAbnormalChannels(int totalAbnormalChannels) {
		this.totalAbnormalChannels = totalAbnormalChannels;
	}


	/**
	 * @return the activeDigitalChannels
	 */
	public Integer[] getActiveDigitalChannels() {
		return activeDigitalChannels;
	}


	/**
	 * @param activeDigitalChannels the activeDigitalChannels to set
	 */
	public void setActiveDigitalChannels(Integer[] activeDigitalChannels) {
		this.activeDigitalChannels = activeDigitalChannels;
	}


	/**
	 * @return the mapTriggerInfo
	 */
	public Map<String, String> getMapTriggerInfo() {
		return mapTriggerInfo;
	}


	/**
	 * @param mapTriggerInfo the mapTriggerInfo to set
	 */
	public void setMapTriggerInfo(WeakHashMap<String, String> mapTriggerInfo) {
		this.mapTriggerInfo = mapTriggerInfo;
	}


	// Collecting events id to display in INF files and the comtradeDetails table
	public String getActiveEventsId()
	{
		StringBuffer events = new StringBuffer();
//		int[] sortedDigitalChnl = (int[])abnormalDigiChnl.clone();
//		Arrays.sort(sortedDigitalChnl);

		activeDigitalChannels = new Integer[totalAbnormalChannels];
		int indexToBeWritten = 0;
//		for (int i = sortedDigitalChnl.length - 1; i >= 0; i--) {
//			for (int j = 0; j < abnormalDigiChnl.length; j++) {
		for (int j = getActiveEventsInOrderOccured().size()-1; j >= 0; j--) {
//				if (sortedDigitalChnl[i] > 0 && sortedDigitalChnl[i] == abnormalDigiChnl[j])
//				{
//					activeDigitalChannels[indexToBeWritten] = j;
			activeDigitalChannels[indexToBeWritten] = getActiveEventsInOrderOccured().get(j);
//					abnormalDigiChnl[j] = 0;
					indexToBeWritten++;
//					break;
//				}						
			}
//		}
		DigitalInfo digitalInfo;
		String phaseId;
		for (int j = activeDigitalChannels.length-1; j>=0 ;j--) {
			digitalInfo = stationDetails.getDigitalInfo(activeDigitalChannels[j]);
			if (digitalInfo.getPhaseId() != null && !digitalInfo.getPhaseId().isEmpty())
			{
				phaseId = "- Ph "+digitalInfo.getPhaseId();
			}
			else
			{
				phaseId = "";
			}
			if (events.length() > 0)
			{
				events.append(M9kConstants.NEWLINE+digitalInfo.getChnlId()+" - "+digitalInfo.getCircuitName()+phaseId);
			}
			else
			{
				events.append(digitalInfo.getChnlId()+" - "+digitalInfo.getCircuitName()+phaseId);
			}
		}

//		logger.debug("Active events id.."+events);
		return events.toString();
	}

	private Integer[] getActiveEventsArray()
	{
//		int[] sortedDigitalChnl = (int[])abnormalDigiChnl.clone();
//		Arrays.sort(sortedDigitalChnl);
//		for (int i = 0; i < abnormalDigiChnl.length; i++) {
//			System.out.println("\t\t\tMap events index "+i+" -> "+mapEventsIndexInOrderTirggered.get(abnormalDigiChnl[i]));
//			
//		}
//		System.out.println("Sorted list of abnormal chnls "+sortedDigitalChnl);
		Integer[] activeDigitalChannels = new Integer[totalAbnormalChannels];
		int indexToBeWritten = 0;
//		for (int i = sortedDigitalChnl.length - 1; i >= 0; i--) {
//			for (int j = 0; j < getActiveEventsInOrderOccured().size(); j++) {
			for (int j = getActiveEventsInOrderOccured().size()-1; j >= 0; j--) {
//					System.out.println(getActiveEventsInOrderOccured().get(j)+" is a abnormal channel ");
					activeDigitalChannels[indexToBeWritten] = getActiveEventsInOrderOccured().get(j);
//					abnormalDigiChnl[j] = 0;
					indexToBeWritten++;
			}
//		}
		return activeDigitalChannels;
	}

	
	public void parseComtradeDataForEvents() throws M9000Exception
	{
		start = System.currentTimeMillis();
//		short sData;
		DataInputStream dis = null;
		if (stationDetails.getFileType().equalsIgnoreCase("BINARY"))
		{
			logger.debug("Binary file...");
			try {
//				dis = new DataInputStream(new FileInputStream(ctDataFile));
				dis = new DataInputStream(isDat); // Reading from the blob
				int eventChannelCnt = stationDetails.getDigitalChannelsCount(); // Total digital channels in the station
				int totalBytesPerSample = (int) ((stationDetails.getAnalogChannelsCount() * 2) + (2 * Math.ceil((float)eventChannelCnt/16))+4+4); // Total bytes per sample including analogs,digital and 8 bytes for index and timestamp
				byte skipAnalogBytes[] = new byte[(int)((stationDetails.getAnalogChannelsCount() * 2) +4+4)]; // Skip the bytes for Analogs plus 8 bytes to get to digitals 
				
				ByteBuffer roBuff;
				int i = 0;
				int totalNumberOfSamples = 0;
				if (stationDetails.getNoOfSamplingRates() > 0)
				{
					totalNumberOfSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates()-1); 
				}
				else
				{
					totalNumberOfSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates());
				}
				byte[] bytes = new byte[totalBytesPerSample*totalNumberOfSamples]; // Total bytes for the entire blob
//				analogData = new double[totalNumberOfSamples][stationDetails.getAnalogChannelsCount()];
//				digitalData = new short[totalNumberOfSamples][eventChannelCnt];
				short chnlValue;
				abnormalDigiChnl = new int [eventChannelCnt]; // To track digital channel that went abnormal 
				dataTimestamps = new int[totalNumberOfSamples];
				short digitalChnl = 0;
				short numOfDigitalBytes = (short)(Math.ceil((float)eventChannelCnt/16)); // Total number of digital bytes 
				String digitalBits;
//				char[] charDigitalBits;
				int iBitCnt = 0;
				int ichnlCnt = 0;
				int totalNoOfDigitalChnl = 0;
//				double chnlMultiplier;
//				double chnlOffset;
//				ArrayList<ChannelInfo> digitalChnlConfList;
//				digitalChnlConfList = (ArrayList<ChannelInfo>)stationDetails.getDigitalInfoCollection();
				int[] eventChnlStatus = getChannelConfigurationStatus(); // Default (open/close) status for digital channels from config
				String zeros = "0000000000000000";
				String leadZero;
				activeEventsInOrderOccured = new ArrayList<Integer>(eventChannelCnt);
				
				dis.readFully(bytes);
				roBuff = ByteBuffer.wrap(bytes);
				roBuff.order(ByteOrder.LITTLE_ENDIAN); // Read as Little Endian into the buffer
				
				while (i<totalNumberOfSamples) // Start parsing blob until all samples are read
				{
//					roBuff.getInt();

//					dataTimestamps[i] = roBuff.getInt();
					
					ichnlCnt = 0;
//					while (ichnlCnt < stationDetails.getAnalogChannelsCount())
//					{
//						chnlMultiplier = ((AnalogInfo)((ArrayList<ChannelInfo>)stationDetails.getAnalogInfoCollection()).get(ichnlCnt)).getChnlMultiplier();
//						chnlOffset = ((AnalogInfo)((ArrayList<ChannelInfo>)stationDetails.getAnalogInfoCollection()).get(ichnlCnt)).getChnlOffset();
//						sData = roBuff.getShort();
//						if (sData == M9kConstants.BINARY_MISSING_DATA.shortValue())
//						{
//							analogData[i][ichnlCnt] = 0;
//						}
//						else
//						{
//							analogData[i][ichnlCnt] = (chnlMultiplier*sData)+chnlOffset;
//						}
//						ichnlCnt++;
//					}
					roBuff.get(skipAnalogBytes); // Skip the initial bytes (analogs and timestamp) to get to digitals
					ichnlCnt = 0;
					totalNoOfDigitalChnl = 0;
					while (ichnlCnt++ < numOfDigitalBytes) 
					{
						digitalChnl = roBuff.getShort();//Short.reverseBytes(roBuff.getShort());
						digitalBits = Integer.toBinaryString(digitalChnl);
						if (digitalBits.length() > 16)
						{
							digitalBits = digitalBits.substring(digitalBits.length() - 16);
							
						}
						else
						{
//							digitalBits = stuffLeadingzeroes(digitalBits);
							leadZero = zeros.substring(0,16-digitalBits.length());
							digitalBits=leadZero.concat(digitalBits);
						}
						iBitCnt = 16; // 16 channels as 2 bytes
//						charDigitalBits = digitalBits.toCharArray();
						while (iBitCnt > 0 && totalNoOfDigitalChnl < eventChannelCnt)
						{
							chnlValue = Short.parseShort(digitalBits.substring(iBitCnt-1, iBitCnt)); // Channel status in the blob
//							chnlValue = Short.parseShort(""+charDigitalBits[iBitCnt]);
								if (eventChnlStatus[totalNoOfDigitalChnl] == 0) // 0 indicates OPEN in config file 
								{
									if (chnlValue == 1 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0) // if default config status is open and actual value is close and already not marked as abnormal
									{
										abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
										activeEventsInOrderOccured.add(totalNoOfDigitalChnl);
										totalAbnormalChannels++;
									}
								}
								else if (eventChnlStatus[totalNoOfDigitalChnl] == 1) // 1 indicates CLOSE in config file
								{
									if (chnlValue == 0 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0) // if default config status is close and actual value is Open and already not marked as abnormal
									{
										abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
										activeEventsInOrderOccured.add(totalNoOfDigitalChnl);
										totalAbnormalChannels++;
									}
								}
								
								totalNoOfDigitalChnl++;
							iBitCnt--;
						}
					}
					i++;
				}
			} catch (FileNotFoundException fnfe) {
				throw new M9000Exception(fnfe);
			} catch (IOException ioe) {
				throw new M9000Exception(ioe);
			}
			finally
			{
				end = System.currentTimeMillis();
				logger.debug("Time taken for ParseComtradeData binary in M9kParser "+(end-start));
				try {
					if (dis != null)
					{
						dis.close();
						dis = null;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
				if (isDat != null)
				{
					try {
						isDat.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (urlcDat != null)
				{
					urlcDat.disconnect();
				}

			}
		}
		else if (stationDetails.getFileType().equalsIgnoreCase("ASCII"))
		{
			logger.debug("ASCII File...");
			BufferedReader bfr = null;
			try
			{
//			bfr = new BufferedReader(new FileReader(ctDataFile));
			bfr = new BufferedReader(new InputStreamReader(isDat));
			int totalColumns = (int) (stationDetails.getAnalogChannelsCount() + stationDetails.getDigitalChannelsCount()+2);
//			logger.debug("Total cols: "+totalColumns+" analog cnt: "+stationDetails.getAnalogChannelsCount()+" Digital cnt: "+stationDetails.getDigitalChannelsCount());
			String[] data = new String[totalColumns];
			String strLine = "";
			int i = 0;
			int totalNumberOfSamples = 0;
			if (stationDetails.getNoOfSamplingRates() > 0)
			{
				totalNumberOfSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates()-1);
			}
			else
			{
				totalNumberOfSamples = stationDetails.getTotalNumberOfSamples(stationDetails.getNoOfSamplingRates());
			}
			logger.debug("totalNumberOfSamples..."+totalNumberOfSamples);
//			analogData = new double[totalNumberOfSamples][stationDetails.getAnalogChannelsCount()];
			dataTimestamps = new int[totalNumberOfSamples];
			digitalData = new short[totalNumberOfSamples][stationDetails.getDigitalChannelsCount()];
			abnormalDigiChnl = new int [stationDetails.getDigitalChannelsCount()];
			
			int totalNoOfDigitalChnl = 0;
			ArrayList<ChannelInfo> digitalChnlConfList;
			digitalChnlConfList = (ArrayList<ChannelInfo>)stationDetails.getDigitalInfoCollection();
			int columnsCnt = 0;
			int status = 0;
			int analogCnt = stationDetails.getAnalogChannelsCount();
			int digitalCnt = stationDetails.getDigitalChannelsCount();
			logger.debug("Analog Channels Count..."+analogCnt);
			logger.debug("Digital Channels Count..."+digitalCnt);
			System.out.println("Analog Channels Count..."+analogCnt);
			System.out.println("Digital Channels Count..."+digitalCnt);
//			m9kUtils.startTimer("\n\n\t\t\tParsing Dat file");
			// START: 21-Feb-2016: Removed ready() function as it might cause failure like in Dunwoodie, where fault did not open
//			while (bfr.ready() && i<totalNumberOfSamples)
			while (i<totalNumberOfSamples)
			// END: 21-Feb-2016
			{
				strLine = bfr.readLine();
				strLine = strLine.substring(nthOccurrence(strLine,',',(analogCnt+1))+1);
				data = strLine.split(",");
				//test
				columnsCnt=0;
//				logger.debug("Lines read..."+i + " \n Data read: "+strLine);
//				System.out.println("Lines read..."+i + " \n Data read: "+strLine);

//				dataTimestamps[i] = Integer.parseInt(data[columnsCnt++]);
//				columnsCnt++;
				
//				ichnlCnt = 0;
//				while (ichnlCnt < analogCnt)
//				{
//					columnsCnt++;
//					
////					System.out.print(analogData[i][ichnlCnt]+" , ");
//					ichnlCnt++;
//				}
//				logger.debug("Columns Cnt..."+columnsCnt);
//				ichnlCnt = 0;
				totalNoOfDigitalChnl = 0;
//				System.out.println("\n\n\t\t\t\t Column after analog..."+columnsCnt);
				while (totalNoOfDigitalChnl < digitalCnt)
				{
//					logger.debug("Digital Columns Cnt..."+columnsCnt+" i-> "+i + " totalNoOfDigitalChnl "+totalNoOfDigitalChnl);
//					System.out.println("Digital Columns Cnt..."+columnsCnt+" i-> "+i + " totalNoOfDigitalChnl "+totalNoOfDigitalChnl);
//					System.out.println("Digital Columns Cnt..."+columnsCnt+" i-> "+i + " totalNoOfDigitalChnl "+totalNoOfDigitalChnl);
						digitalData[i][totalNoOfDigitalChnl] = Short.parseShort(data[columnsCnt++]);
						status = ((DigitalInfo)digitalChnlConfList.get(totalNoOfDigitalChnl)).getStatus(); 
							if ( status == 0) // y = 0 indicates OPEN in config file
							{
								if (digitalData[i][totalNoOfDigitalChnl] == 1 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0)
								{
//									System.out.println("\t\t\t\t\tabnormal index i "+i+" channel no "+totalNoOfDigitalChnl);
									abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
									activeEventsInOrderOccured.add(totalNoOfDigitalChnl);
									totalAbnormalChannels++;
								}
							}
							else // y = 0 indicates OPEN in config file
							{
//								System.out.println("\n\t\t\t\tStatus "+((DigitalInfo)digitalChnlConfList.get(totalNoOfDigitalChnl)).getStatus()+" chnl Id "+((DigitalInfo)digitalChnlConfList.get(totalNoOfDigitalChnl)).getChnlId());
//								System.out.println("\n\t\t\t\t Data value... "+digitalData[i][totalNoOfDigitalChnl]);
								if (digitalData[i][totalNoOfDigitalChnl] == 0 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0)
								{
//									System.out.println("\t\t\tabnormal2 index i "+i+" totalNoOfDigitalChnl "+totalNoOfDigitalChnl);
									abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
									activeEventsInOrderOccured.add(totalNoOfDigitalChnl);
									totalAbnormalChannels++;
								}
							}
							totalNoOfDigitalChnl++;
				}
				i++;
				
			}
//			m9kUtils.endTimer("\n\n\t\t\tParsing Dat file");
			logger.debug("analogData..."+analogData.length);
			logger.debug("digitalData..."+digitalData.length);
			
		} catch (FileNotFoundException fnfe) {
			throw new M9000Exception(fnfe);
		} catch (IOException ioe) {
			throw new M9000Exception(ioe);
		}catch (Exception e) {
			throw new M9000Exception(e);
		}
		finally
		{
			end = System.currentTimeMillis();
			logger.debug("Time taken for ParseComtradeData ASCII in M9kParser "+(end-start));
			logger.debug("1Returing from the parseComtradeData function...");

			try {
				logger.debug("2Returing from the parseComtradeData function...");
				if (bfr != null)
				{
					logger.debug("3Returing from the parseComtradeData function...");
					bfr.close();
					bfr = null;
				}
				logger.debug("4Returing from the parseComtradeData function...");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.debug("5Returing from the parseComtradeData function...");
			}
			if (isDat != null)
			{
				try {
					isDat.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (urlcDat != null)
			{
				urlcDat.disconnect();
			}

			logger.debug("6Returing from the parseComtradeData function...");
		}
		logger.debug("7Returing from the parseComtradeData function...");
		}
	}
	
	public static int nthOccurrence(String str, char c, int n) {
	    int pos = str.indexOf(c, 0);
	    while (n-- > 0 && pos != -1)
	        pos = str.indexOf(c, pos+1);
	    return pos;
	}	
	/**
	 * @return the dataTimestamps
	 */
	public int[] getDataTimestamps() {
		return dataTimestamps;
	}

	/**
	 * @param dataTimestamps the dataTimestamps to set
	 */
	public void setDataTimestamps(int[] dataTimestamps) {
		this.dataTimestamps = dataTimestamps;
	}

	public void setAnalogData(double[][] analogData) {
		this.analogData = analogData;
	}

	public void setDigitalData(short[][] digitalData) {
		this.digitalData = digitalData;
	}

	/**
	 * @return the digitalData
	 */
	public short[][] getDigitalData() {
		return digitalData;
	}

	private int[] getChannelConfigurationStatus()
	{
		int[] status = new int[stationDetails.getDigitalChannelsCount()];
		ArrayList<ChannelInfo> digitalChnlConfList = (ArrayList<ChannelInfo>)stationDetails.getDigitalInfoCollection();
		for (int i = 0; i < stationDetails.getDigitalChannelsCount(); i++) {
			status[i] = ((DigitalInfo)digitalChnlConfList.get(i)).getStatus(); 
		}
		
		return status;
	}
	

	public ArrayList<String>[] getDigitalToolTips() {
		return digitalToolTips;
	}

	public void setDigitalToolTips(ArrayList<String>[] digitalToolTips) {
		this.digitalToolTips = digitalToolTips;
	}
	
	public ProcessDatDTO getProcessData() throws M9000Exception
	{
		ByteArrayOutputStream binaryData = null;
		DataOutputStream binaryOutStream  = null;
		BufferedInputStream dis  = null;
		ProcessDatDTO processDatDTO = new ProcessDatDTO();
		
		try
		{
		
		processDatDTO.setMergedAnalogsCount(getStationDetails().getAnalogChannelsCount());
		processDatDTO.setMergedEventsCount(getStationDetails().getDigitalChannelsCount());
		processDatDTO.setLstAnalogInfo((ArrayList<ChannelInfo>)getStationDetails().getAnalogInfoCollection());
		processDatDTO.setLstDigitalInfo((ArrayList<ChannelInfo>)getStationDetails().getDigitalInfoCollection());
		processDatDTO.setSampleCnt(getStationDetails().getTotalNumberOfSamples(0));
		processDatDTO.setSampleRate(getStationDetails().getSampleRate(0));
		logger.debug("sample rate "+processDatDTO.getSampleRate());
		processDatDTO.setLineFreq(getStationDetails().getLineFrequency());
		logger.debug("Line Freq "+processDatDTO.getLineFreq());
		binaryData = new ByteArrayOutputStream();
		binaryOutStream = new DataOutputStream(binaryData);
//		dis = new BufferedInputStream(new FileInputStream(ctDataFile));
		dis = new BufferedInputStream(isDat);
		byte[] buf = new byte[3000];
		int read = 0;

		while ((read = dis.read(buf)) > 0) {
//			System.out.println("Bytes read "+read);
			binaryOutStream.write(buf, 0, read);
        }
		binaryOutStream.close();
		dis.close();
		}
		catch (Exception e) {
			logger.error("Exception in getting comtrade data");
			throw new M9000Exception("Exception in getting comtrade data", e);
		}
		finally
		{
			if (binaryOutStream != null)
			{
				try {
					binaryOutStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (dis != null)
			{
				try {
					dis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		processDatDTO.setBinaryData(binaryData);
		return processDatDTO;
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
//	private boolean isAbnormalEvent(int event)
//	{
//		boolean status = false;
//		for (int i = 0; i < mapActiveEvents.length; i++) {
////			System.out.println("Active event "+mapActiveEvents[i]);
//			if (mapActiveEvents[i] == event)
//			{
//				status = true;
//				break;
//			}
//		}
//		return status;
//	}

//	public Map<Integer, Integer> getMapEventsIndexInOrderTirggered() {
//		return mapEventsIndexInOrderTirggered;
//	}

	public List<Integer> getActiveEventsInOrderOccured() {
		return activeEventsInOrderOccured;
	}

}
