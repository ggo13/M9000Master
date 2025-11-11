package com.usi.m9000.Master;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.ComtradeContAnalogDAO;
import com.usi.m9000.dao.ComtradeContDAO;
import com.usi.m9000.dao.LongTermDataDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

public class M9kCreateContinuousComtradeFilesFromDB{

	private ComtradeContDAO mySqlComtradeContDAO;
	private LongTermDataDAO mySqlLongTermDataDAO;
	private ComtradeContAnalogDAO mySqlComtradeContAnalogDAO;
//	private StationInfo stationInfo;
	private String ctFileName; 
	private String ltrFileName;
	private ResourceBundle bundle;
	private List<WeakReference<ComtradeDataDTO>> lstComtradeDTO;
	private StringBuffer confInputString;
	private StringBuffer datInputString;
	String stationName="Test Station";
	String recDevId = "USI_M9000";
	String comStdRevYear = "1999";
	int noOfSamplingRates = 1;
	int stationId;
	private String stationDir;
	private String dataDir;
	private String destinationDir;
	private File cfgFile;
	private File datFile;
	private String timeCode="-5t";
	private File infFile;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kCreateContinuousComtradeFilesFromDB.class);

	public M9kCreateContinuousComtradeFilesFromDB()
	{
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
		M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		mySqlComtradeContDAO = m9kDAOFactory.getContinuousComtradeDAO();
		mySqlComtradeContAnalogDAO = m9kDAOFactory.getComtradeContinuousAnalogDAO();
		mySqlLongTermDataDAO = m9kDAOFactory.getLongTermDataDAO();
	}
	/**
	 * 
	 */
	public M9kCreateContinuousComtradeFilesFromDB(int stationId) {
		this();
		this.stationId = stationId;
	}

	public void createComtradeFilesForStation (String station) throws M9000Exception
	{
			lstComtradeDTO = mySqlComtradeContDAO.getLstOfContinuousData(getStationId());
			for (Iterator<WeakReference<ComtradeDataDTO>> lstComtradeDataDTOItereator = lstComtradeDTO.iterator(); lstComtradeDataDTOItereator.hasNext();) {
				ComtradeDataDTO comtradeDataDTO =  lstComtradeDataDTOItereator.next().get();
				createComtradeFiles(comtradeDataDTO);
			}
	}

	public void createComtradeFilesForStation (String station, String startDate, String endDate) throws M9000Exception
	{
			lstComtradeDTO = mySqlComtradeContDAO.getLstOfContinuousData(getStationId());
			for (Iterator<WeakReference<ComtradeDataDTO>> lstComtradeDataDTOItereator = lstComtradeDTO.iterator(); lstComtradeDataDTOItereator.hasNext();) {
				ComtradeDataDTO comtradeDataDTO = lstComtradeDataDTOItereator.next().get();
				createComtradeFiles(comtradeDataDTO);
			}
	}

	public void constructComtradeContData (int exportId, String startDate, String endDate) throws M9000Exception
	{
//		stationInfo = M9kXMLUtils.getStationDetails("TestComtrade", 23);
			lstComtradeDTO = mySqlComtradeContDAO.getLstOfContinuousData(getStationId(), exportId, startDate, endDate);
			for (Iterator<WeakReference<ComtradeDataDTO>> lstComtradeDataDTOItereator = lstComtradeDTO.iterator(); lstComtradeDataDTOItereator.hasNext();) {
				ComtradeDataDTO comtradeDataDTO = lstComtradeDataDTOItereator.next().get();
				createComtradeInputString(comtradeDataDTO);
			}
	}	
	public boolean constructComtradeContData (String exportIds, String startDate, String endDate) throws M9000Exception
	{
		boolean booFlag = false;
//		stationInfo = M9kXMLUtils.getStationDetails("TestComtrade", 23);
		ComtradeDataDTO comtradeDataDTO = mySqlComtradeContDAO.getLstOfContinuousData(getStationId(), exportIds, startDate, endDate);
		if (comtradeDataDTO.getData() != null)
		{
			createComtradeInputString(comtradeDataDTO);
			booFlag = true;
		}
		return booFlag;
	}	

	public void createContAnalogComtradeFilesForStation (String station) throws M9000Exception
	{
			lstComtradeDTO = mySqlComtradeContAnalogDAO.getLstOfContinuousAnalogData(getStationId());
//			System.out.println("lstComtradeDTO size.."+lstComtradeDTO.size());
			for (Iterator<WeakReference<ComtradeDataDTO>> lstComtradeDataDTOItereator = lstComtradeDTO.iterator(); lstComtradeDataDTOItereator.hasNext();) {
				ComtradeDataDTO comtradeDataDTO = lstComtradeDataDTOItereator.next().get();
				createComtradeFiles(comtradeDataDTO);
			}
	}
	
	public void constructComtradeContAnalog(int exportId, String startDate, String endDate) throws M9000Exception
	{
//		stationInfo = M9kXMLUtils.getStationDetails("TestComtrade", 23);
		
			try {
				lstComtradeDTO = mySqlComtradeContAnalogDAO.getLstOfContinuousAnalogData(getStationId(), exportId, startDate, endDate);
//				System.out.println("lstComtradeDTO size.."+lstComtradeDTO.size());
				for (Iterator<WeakReference<ComtradeDataDTO>> lstComtradeDataDTOItereator = lstComtradeDTO.iterator(); lstComtradeDataDTOItereator.hasNext();) {
					ComtradeDataDTO comtradeDataDTO =  lstComtradeDataDTOItereator.next().get();
					createComtradeInputString(comtradeDataDTO);
				}
			} catch (M9000Exception e) {
				// TODO Auto-generated catch block
				throw e;
			}
	}	
	
	public String createLTRComtradeFiles (int faultId, String ltrType) throws M9000Exception
	{
		List<ComtradeDataDTO> lstComtradeDTO;
			ltrFileName = mySqlLongTermDataDAO.getLongTermDataFileName(stationId, faultId, ltrType);
			logger.debug("Long term File Name received "+ltrFileName);
			if (ltrFileName != null)
			{
				return getLtrFileName();
			}
			if (ltrType.equalsIgnoreCase(M9kConstants.ANALOG))
			{
				lstComtradeDTO = mySqlLongTermDataDAO.getLstOfLongTermAnalogDataStaged(stationId, faultId);
			}
			else
			{
				lstComtradeDTO = mySqlLongTermDataDAO.getLstOfLongTermMeasurementsDataStaged(stationId, faultId);
			}
//			for (Iterator<WeakReference<ComtradeDataDTO>> lstComtradeDataDTOItereator = lstComtradeDTO.iterator(); lstComtradeDataDTOItereator.hasNext();) {
//				ComtradeDataDTO comtradeDataDTO = lstComtradeDataDTOItereator.next().get();
//				createComtradeFiles(comtradeDataDTO);
//			}
			if (lstComtradeDTO == null || lstComtradeDTO.isEmpty())
			{
				return "";
			}
			createLTRFiles(lstComtradeDTO);
			
			return getLtrFileName();
	}

	public void createLTRFiles(List<ComtradeDataDTO> lstComtradeDataDTO) throws M9000Exception
	{
		File destFile = null;
		ComtradeDataDTO comtradeDataDTO = lstComtradeDataDTO.get(0);
		setStationName(comtradeDataDTO.getStationName());
		if (comtradeDataDTO.getRecordingDevId() != null && !comtradeDataDTO.getRecordingDevId().isEmpty())
		{
			setRecDevId(""+comtradeDataDTO.getRecordingDevId());
		}
		// START: 15-Feb-2020 - Updated LTR constants
//		stationDir = "/"+comtradeDataDTO.getStationId()+"-"+comtradeDataDTO.getStationName()+"/LTR/";
		stationDir = File.separator+comtradeDataDTO.getStationId()+"-"+comtradeDataDTO.getStationName()+File.separator+M9kConstants.LTR_DIR+File.separator;
		// END: 15-Feb-2020 - Updated LTR constants
		destinationDir = dataDir+stationDir;
		destFile = new File(destinationDir);
		destFile.mkdirs();
		destFile.setWritable(true, false);
		destFile.setExecutable(true,false);
		destFile.setReadable(true, false);

		createLtrFileName(comtradeDataDTO);
		createLtrComtradeCfg(lstComtradeDataDTO);
//		if (comtradeDataDTO.getType().equalsIgnoreCase(M9kConstants.ASCII) )
//		{
////			createLTRComtradeDat(lstComtradeDataDTO);
//		}
//		else
//		{
			createBinaryLTRComtradeDat(lstComtradeDataDTO);
//		}
//		ComtradeDataDTO ltrComtradeDat = lstComtradeDataDTO.get(0);
		calculateTimeLength(comtradeDataDTO);
		createComtradeInf(comtradeDataDTO);
		try
		{
			int insertCnt = mySqlLongTermDataDAO.insertIntoLongTermDat(comtradeDataDTO);
			logger.debug("Total records inserted "+insertCnt);
		}
		catch (Exception e) {
			logger.error("Failed to insert long term dat. ",e);
			throw new M9000Exception(e);
		}
		
	}

	private void createLtrFileName(ComtradeDataDTO comtradeDataDTO)
	{
		String microseconds = (""+comtradeDataDTO.getTsTrigger());
		microseconds = microseconds.substring(microseconds.length()-3);
	    String date = new java.text.SimpleDateFormat("yyMMdd,HHmmssSSS").format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
//	    ctFileName = "LTR_"+comtradeDataDTO.getType()+"_"+comtradeDataDTO.getFaultId()+"_"+date + ","+bundle.getString("timeCode")+","+stationName+","+recDevId+","+bundle.getString("company");
	    ctFileName = date + ","+bundle.getString("timeCode")+","+stationName+","+recDevId+","+bundle.getString("company")+","+"LTR_"+comtradeDataDTO.getType();
	    ctFileName = ctFileName.replace("/", "_");
	    ctFileName = ctFileName.replace("\\", "_");
	    comtradeDataDTO.setFileName(stationDir+ctFileName);
	    setLtrFileName(stationDir+ctFileName);
	    cfgFile = new File (destinationDir+ctFileName+M9kConstants.CFG_FILE_EXTN);
	    datFile = new File (destinationDir+ctFileName+M9kConstants.DAT_FILE_EXTN);
	    infFile = new File (destinationDir+ctFileName+M9kConstants.INF_FILE_EXTN);
	}

	private void createBinaryLTRComtradeDat(List<ComtradeDataDTO> lstComtradeDataDTO) throws M9000Exception
	{
		DataOutputStream dout = null ;
		DataInputStream dis[] = new DataInputStream[lstComtradeDataDTO.size()];
		int sampleCnt = lstComtradeDataDTO.get(0).getSampleCnt();
		logger.debug("Total sample Cnt "+sampleCnt);
		int chnlCnt = 0;
		ComtradeDataDTO comtradeDataDTO;
		try
		{
			dout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(datFile)));
			// START: 06-May-2020 - LTR and Continuous COMTRADE dat file format needs to be fixed for index that needs to start from 1 instead of 0 
//			for (int i = 0; i < sampleCnt; i++) {
			for (int i = 1; i <= sampleCnt; i++) {
			// END: 06-May-2020
				chnlCnt = 0;
//				System.out.println("Sample count  "+i);
				for (Iterator<ComtradeDataDTO> iterator = lstComtradeDataDTO.iterator(); iterator
					.hasNext();) {
					comtradeDataDTO = iterator
						.next();
//					System.out.println("Channel id "+comtradeDataDTO.getExportId());
					if (dis[chnlCnt] == null)
					{
						dis[chnlCnt] = new DataInputStream(comtradeDataDTO.getBinaryDataSteam());
					}
//					System.out.println("Skipped bytes "+(i*2));
					if (chnlCnt == 0)
					{
						dout.writeInt(Integer.reverseBytes(i));
						dout.writeInt(Integer.reverseBytes((int)((i/comtradeDataDTO.getSampleRate())*1000)));
					}
						dout.writeShort(Short.reverseBytes(dis[chnlCnt].readShort()));
					chnlCnt++;
				}
			}
		
		} catch (IOException e) {
			logger.error("Exception in creating binary ltr dat file", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Exception in creating binary ltr dat file", e);
			throw new M9000Exception(e);
		}
		finally {
			if (dout != null)
			{
				try {
					dout.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for (int i = 0; i < dis.length; i++) {
				if (dis[i] != null)
				{
					try {
						dis[i].close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}

		}

	}

	
	public void createLtrComtradeCfg(List<ComtradeDataDTO> lstComtradeDataDTO) throws M9000Exception
	{
		BufferedWriter bw = null ;
		String microseconds;
		String date;
		int analogCnt;
		int eventCnt;
//		StringTokenizer strTok = new StringTokenizer(comtradeDataDTO.getAnalogs().toString(), M9kConstants.NEWLINE);
		analogCnt = lstComtradeDataDTO.size();
		eventCnt = 0; // No events data for continuous data
		ComtradeDataDTO comtradeDataDTO = lstComtradeDataDTO.get(0);
		try {
			bw = new BufferedWriter(new FileWriter(cfgFile));
			bw.write(stationName+","+recDevId+","+comStdRevYear);
			bw.newLine();
			bw.write((analogCnt+eventCnt)+","+analogCnt+"A,"+eventCnt+"D");
			bw.newLine();
			for (Iterator<ComtradeDataDTO> iterator = lstComtradeDataDTO.iterator(); iterator
					.hasNext();) {
				ComtradeDataDTO chnlData =  iterator
						.next();
				
				bw.write(chnlData.getAnalogs().toString());
			}
			bw.write(""+(int)comtradeDataDTO.getLineFreq());
			bw.newLine();
			if (comtradeDataDTO.getSampleRate() == 0)
			{
				noOfSamplingRates = 0;
			}

			bw.write(""+noOfSamplingRates);
			bw.newLine();
			bw.write(""+(int)comtradeDataDTO.getSampleRate()+","+comtradeDataDTO.getSampleCnt());
			bw.newLine();
			microseconds = (""+comtradeDataDTO.getTsPrefault());
		     microseconds = microseconds.substring(microseconds.length()-3);
//		    date = new java.text.SimpleDateFormat("dd/MM/yyyy,HH:mm:ss.SS").format(new java.util.Date (comtradeDataDTO.getTsPrefault()/1000));
		     date = M9kUtils.convertEpochToComtradeDisplayDate(comtradeDataDTO.getTsPrefault());
			bw.write(date+microseconds);
			bw.newLine();
			microseconds = (""+comtradeDataDTO.getTsTrigger());
		     microseconds = microseconds.substring(microseconds.length()-3);
//		    date = new java.text.SimpleDateFormat("dd/MM/yyyy,HH:mm:ss.SS").format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
		     date = M9kUtils.convertEpochToComtradeDisplayDate(comtradeDataDTO.getTsTrigger());
			bw.write(date+microseconds);
			bw.newLine();
			bw.write(M9kConstants.Binary.toUpperCase());
			bw.newLine();
			bw.write("1");
			bw.newLine();
		}  catch (IOException e) {
			logger.error("Exception in creating cfg file", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Exception in creating cfg file", e);
			throw new M9000Exception(e);
		}
		finally {
			try {
				if(bw != null)
				{
					bw.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void createComtradeFiles(ComtradeDataDTO comtradeDataDTO) throws M9000Exception
	{
		createFileName(comtradeDataDTO);
		createComtradeCfg(comtradeDataDTO);
		if (bundle.getString("type").equalsIgnoreCase(M9kConstants.ASCII) )
		{
			createComtradeDat(comtradeDataDTO);
		}
		else
		{
			createBinaryComtradeDat(comtradeDataDTO);
		}
	}
	private void createComtradeInputString(ComtradeDataDTO comtradeDataDTO) throws M9000Exception
	{
		confInputString = createConfInputStream(comtradeDataDTO);
		if (bundle.getString("type").equalsIgnoreCase(M9kConstants.ASCII) )
		{
			datInputString = createDatInputString(comtradeDataDTO);
//			System.out.println("Dat String..."+datInputString);
		}
		else
		{
			createBinaryComtradeDat(comtradeDataDTO);
		}
	}

	public void createComtradeCfg(ComtradeDataDTO comtradeDataDTO) throws M9000Exception
	{
		BufferedWriter bw = null ;
		String microseconds;
		String date;
		int analogCnt;
		int eventCnt;
		StringTokenizer strTok = new StringTokenizer(comtradeDataDTO.getAnalogs().toString(), M9kConstants.NEWLINE);
		analogCnt = strTok.countTokens();
		eventCnt = 0; // No events data for continuous data
		try {
			bw = new BufferedWriter(new FileWriter(new File (bundle.getString("dataDir")+ctFileName+M9kConstants.CFG_FILE_EXTN)));
			bw.write(stationName+","+recDevId+","+comStdRevYear);
			bw.newLine();
			bw.write((analogCnt+eventCnt)+","+analogCnt+"A,"+eventCnt+"D");
			bw.newLine();
			bw.write(comtradeDataDTO.getAnalogs().toString());
//			System.out.println("Analog string.."+comtradeDataDTO.getAnalogs());
			bw.write(""+(int)comtradeDataDTO.getLineFreq());
			bw.newLine();
			if (comtradeDataDTO.getSampleRate() == 0)
			{
				noOfSamplingRates = 0;
			}

			bw.write(""+noOfSamplingRates);
			bw.newLine();
			bw.write(""+(int)comtradeDataDTO.getSampleRate()+","+comtradeDataDTO.getSampleCnt());
			bw.newLine();
			microseconds = (""+comtradeDataDTO.getTsPrefault());
		     microseconds = microseconds.substring(microseconds.length()-3);
		    date = new java.text.SimpleDateFormat("dd/MM/yyyy,HH:mm:ss.SSS").format(new java.util.Date (comtradeDataDTO.getTsPrefault()/1000));
			bw.write(date+microseconds);
			bw.newLine();
			microseconds = (""+comtradeDataDTO.getTsTrigger());
		     microseconds = microseconds.substring(microseconds.length()-3);
		    date = new java.text.SimpleDateFormat("dd/MM/yyyy,HH:mm:ss.SSS").format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
			bw.write(date+microseconds);
			bw.newLine();
			bw.write(comtradeDataDTO.getType());
			bw.newLine();
			bw.write("1");
			bw.newLine();
		} catch (IOException e) {
			logger.error("Exception in creating cfg file", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Exception in creating cfg file", e);
			throw new M9000Exception(e);
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

	private StringBuffer createConfInputStream(ComtradeDataDTO comtradeDataDTO) throws M9000Exception
	{
		StringWriter sw = null;
//		BufferedWriter bw = null ;
		String microseconds;
		String date;
		int analogCnt;
		int eventCnt;
		StringTokenizer strTok = new StringTokenizer(comtradeDataDTO.getAnalogs().toString(), M9kConstants.NEWLINE);
		analogCnt = strTok.countTokens();
		eventCnt = 0; // No events data for continuous data
		try {
//			bw = new BufferedWriter(new FileWriter(new File (bundle.getString("dataDir")+ctFileName+M9kConstants.CFG_FILE_EXTN)));
			sw = new StringWriter();
			sw.append(stationName+","+recDevId+","+comStdRevYear);
			sw.append(M9kConstants.NEWLINE);
			sw.append((analogCnt+eventCnt)+","+analogCnt+"A,"+eventCnt+"D");
			sw.append(M9kConstants.NEWLINE);
			sw.append(comtradeDataDTO.getAnalogs().toString());
//			System.out.println("Analog string.."+comtradeDataDTO.getAnalogs());
			sw.append(""+(int)comtradeDataDTO.getLineFreq());
			sw.append(M9kConstants.NEWLINE);
			if (comtradeDataDTO.getSampleRate() == 0)
			{
				noOfSamplingRates = 0;
			}
			sw.append(""+noOfSamplingRates);
			sw.append(M9kConstants.NEWLINE);
			sw.append(""+(int)comtradeDataDTO.getSampleRate()+","+comtradeDataDTO.getSampleCnt());
			sw.append(M9kConstants.NEWLINE);
			microseconds = (""+comtradeDataDTO.getTsPrefault());
//			System.out.println("microseconds.."+microseconds);
			
		     microseconds = microseconds.substring(microseconds.length()-3);
		    date = new java.text.SimpleDateFormat("dd/MM/yyyy,HH:mm:ss.SSS").format(new java.util.Date (comtradeDataDTO.getTsPrefault()/1000));
			sw.append(date+microseconds);
			sw.append(M9kConstants.NEWLINE);
			microseconds = (""+comtradeDataDTO.getTsTrigger());
		     microseconds = microseconds.substring(microseconds.length()-3);
		    date = new java.text.SimpleDateFormat("dd/MM/yyyy,HH:mm:ss.SSS").format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
			sw.append(date+microseconds);
			sw.append(M9kConstants.NEWLINE);
			sw.append(comtradeDataDTO.getType());
			sw.append(M9kConstants.NEWLINE);
			sw.append("1");
			sw.append(M9kConstants.NEWLINE);
		}
		catch (Exception e) {
			logger.error("Exception in creating cfg file from input stream ", e);
			throw new M9000Exception(e);
		}
		finally {
			try {
				sw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return sw.getBuffer();
	}

	public void createComtradeDat(ComtradeDataDTO comtradeDataDTO) throws M9000Exception
	{
		BufferedWriter bw = null ;
		try {
			bw = new BufferedWriter(new FileWriter(new File (bundle.getString("dataDir")+ctFileName+M9kConstants.DAT_FILE_EXTN)));
			bw.write(comtradeDataDTO.getData().toString());
		} catch (IOException e) {
			logger.error("Exception in creating dat file", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Exception in creating dat file", e);
			throw new M9000Exception(e);
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

	public StringBuffer createDatInputString(ComtradeDataDTO comtradeDataDTO) throws M9000Exception
	{
		StringWriter sw = null ;
		try {
			sw = new StringWriter();
			sw.append(comtradeDataDTO.getData().toString());
		}
		catch (Exception e) {
			logger.error("Exception in creating dat string ", e);
			throw new M9000Exception(e);
		}
		finally {
			try {
				sw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sw.getBuffer();
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
			java.text.SimpleDateFormat sdfInf  = M9kStationUtil.getDateFormat("MM/dd/yyyy,HH:mm:ss.SSS");
			String date = sdfInf.format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
			bw.write("Date="+date.substring(0,date.indexOf(",")));
			bw.newLine();
			bw.write("Time="+date.substring(date.indexOf(",")+1)+microseconds);
			bw.newLine();
			bw.write("TimeCode="+bundle.getString("timeCode"));
			bw.newLine();
			bw.write("Sync=Y");
			bw.newLine();
			bw.write("logF=N");
			bw.newLine();
			bw.write("LT=C");
			bw.newLine();
			bw.write("TestRun=N");
			bw.newLine();
			bw.write("Prefault=0");
			bw.newLine();
			bw.write("Postfault=0");
			bw.newLine();
			bw.write("Length="+comtradeDataDTO.getLength());
			bw.newLine();
			bw.write("FileExt=inf,cfg,dat");
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
	private void createFileName(ComtradeDataDTO comtradeDataDTO)
	{
//		stationInfo = M9kXMLUtils.getStationDetails("TestComtrade", 23);
		String microseconds = (""+comtradeDataDTO.getTsTrigger());
		microseconds = microseconds.substring(microseconds.length()-3);
	    String date = new java.text.SimpleDateFormat("yyMMdd,HHmmssSSS").format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
	    ctFileName = date + ","+bundle.getString("timeCode")+","+stationName+","+recDevId+","+bundle.getString("company")+"ExportId - "+comtradeDataDTO.getExportId()+"_"; 
	    ctFileName = ctFileName.replace("/", "_");
	    ctFileName = ctFileName.replace("\\", "_");
//	    ctFileName += "Continuous";
	    logger.debug("Filename to be created... "+ctFileName);
	    comtradeDataDTO.setFileName(ctFileName);
	}

	//TODO Yet to implement binary version
	public void createBinaryComtradeDat(ComtradeDataDTO comtradeDataDTO) throws M9000Exception
	{
		DataOutputStream dout = null ;
		try {
			dout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File (bundle.getString("dataDir")+"testComtradeBinary.dat"))));
			dout.write(comtradeDataDTO.getData().toString().getBytes());
		} catch (IOException e) {
			logger.error("Exception in creating dat file", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Exception in creating dat file", e);
			throw new M9000Exception(e);
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

	public void createComtradeFilesForContinuousData()
	{
		
	}
	public static void main(String args[])
	{
//		M9kCreateContinuousComtradeFilesFromDB testComtradeData = new M9kCreateContinuousComtradeFilesFromDB(123);
//		long startTime;
//		long endTime;
//		startTime = System.currentTimeMillis();
//		testComtradeData.createComtradeFilesForStation("18");
////		testComtradeData.createContAnalogComtradeFilesForStation("18");
//		endTime = System.currentTimeMillis();
		

//		System.out.println("Total Time for complete process..."+(endTime-startTime));
	}

	/**
	 * @return the lstComtradeDTO
	 */
	public List<WeakReference<ComtradeDataDTO>> getLstComtradeDTO() {
		return lstComtradeDTO;
	}

	/**
	 * @param lstComtradeDTO the lstComtradeDTO to set
	 */
	public void setLstComtradeDTO(List<WeakReference<ComtradeDataDTO>> lstComtradeDTO) {
		this.lstComtradeDTO = lstComtradeDTO;
	}
	
	private void calculateTimeLength(ComtradeDataDTO comtradeDataDTO)
	{
		long tsPrefault = comtradeDataDTO.getTsPrefault();
		long tsTrigger = comtradeDataDTO.getTsTrigger();
		int sampleCnt = comtradeDataDTO.getSampleCnt();
		double sampleRate = comtradeDataDTO.getSampleRate();
		long lastSampleTime = (long)(tsPrefault + (((sampleCnt-1)/sampleRate)*1000000));
		long preFault = (tsTrigger - tsPrefault)/1000;
		long postFault = (lastSampleTime - tsTrigger)/1000;
		double length = (lastSampleTime - tsPrefault)/1000.0;
		comtradeDataDTO.setPreFault(preFault);
		comtradeDataDTO.setPostFault(postFault);
		comtradeDataDTO.setLength(length);
	}

	
	/**
	 * @return the confInputString
	 */
	public StringBuffer getConfInputString() {
		return confInputString;
	}

	/**
	 * @param confInputString the confInputString to set
	 */
	public void setConfInputString(StringBuffer confInputString) {
		this.confInputString = confInputString;
	}

	/**
	 * @return the datInputString
	 */
	public StringBuffer getDatInputString() {
		return datInputString;
	}

	/**
	 * @param datInputString the datInputString to set
	 */
	public void setDatInputString(StringBuffer datInputString) {
		this.datInputString = datInputString;
	}

	public int getStationId() {
		return stationId;
	}

	public void setStationId(int stationId) {
		this.stationId = stationId;
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
	public String getDestinationDir() {
		return destinationDir;
	}
	public void setDestinationDir(String destinationDir) {
		this.destinationDir = destinationDir;
	}
	public String getLtrFileName() {
		return ltrFileName;
	}
	public void setLtrFileName(String ltrFileName) {
		this.ltrFileName = ltrFileName;
	}
}
