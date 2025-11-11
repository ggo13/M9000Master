package com.usi.m9000.actions;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.LongTermDataDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

public class M9kCreateLTRAction  extends ActionSupport{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LongTermDataDAO mySqlLongTermDataDAO;
//	private StationInfo stationInfo;
	private String ctFileName; 
	private String ltrFileName;
	private ResourceBundle bundle;
	String stationName="Test Station";
	String recDevId = "USI_M9000";
	String comStdRevYear = "1999";
	int noOfSamplingRates = 1;
	private String stationDir;
	private String dataDir;
	private String destinationDir;
	private File cfgFile;
	private File datFile;
	int stationId;
	int faultId;
	String ltrType;
	private long contentLength;
	private InputStream inputStream;
	private String irigTimezone = "local";
	private String userPreferredTimezone = "local";
	
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kCreateLTRAction.class);

	public String execute()throws M9000Exception
	{
		try
		{
			ltrFileName = createLTRComtradeFiles ();
		}
		catch(M9000Exception me)
		{
			logger.error("Error in creating lTR files",me);
			throw me;
		}
		catch(Exception e)
		{
			logger.error("Error in creating lTR files",e);
		}
		setContentLength(ltrFileName.length());
		try {
			inputStream =new ByteArrayInputStream(ltrFileName.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error("Error in creating input stream for filename ", e);
			throw new M9000Exception(e);
		}
		return SUCCESS;
	}
	private String createLTRComtradeFiles () throws M9000Exception
	{
		List<ComtradeDataDTO> lstComtradeDTO;
		logger.debug("Station id "+getStationId());
		logger.debug("Fault id "+getFaultId());
		logger.debug("ltr type "+getLtrType());
		M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		mySqlLongTermDataDAO = m9kDAOFactory.getLongTermDataDAO();
		bundle = ResourceBundle.getBundle("M9K_COMTRADE");
		dataDir = bundle.getString("dataDir");

			if (ltrType.equalsIgnoreCase(M9kConstants.ANALOG))
			{
				lstComtradeDTO = mySqlLongTermDataDAO.getLstOfLongTermAnalogDataStaged(stationId, faultId);
			}
			else
			{
				lstComtradeDTO = mySqlLongTermDataDAO.getLstOfLongTermMeasurementsDataStaged(stationId, faultId);
			}
			if (lstComtradeDTO == null || lstComtradeDTO.isEmpty())
			{
				return M9kConstants.NO_DATA;
			}
			try
			{
				irigTimezone = bundle.getString("irig-timezone");
			}
			catch (Exception e) {
				irigTimezone = "local";
			}
			M9kUtils.setIrigTimezone(irigTimezone);
			try
			{
				userPreferredTimezone = bundle.getString("user-preferred-timezone");
			}
			catch (Exception e) {
				userPreferredTimezone = "local";
			}
			M9kUtils.setUserPreferredTimezone(userPreferredTimezone);
			createLTRFiles(lstComtradeDTO);
//			ltrFileName = "successull call";
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
		// END: 15-Feb-2020
		destinationDir = dataDir+stationDir;
		destFile = new File(destinationDir);
		boolean booCreated  = destFile.getParentFile().setWritable(true);
		logger.debug("Set writable dest folder "+destFile.getParentFile().getPath()+" ? "+booCreated);
		logger.debug("ServletActionContext.getServletContext()).getRealPath(/)"+ServletActionContext.getServletContext().getRealPath("/"));
		logger.debug("Destination dir "+destinationDir+" file path "+destFile.getPath()+" exists? "+destFile.exists()+" can write? "+destFile.getParentFile().canWrite());
		booCreated = destFile.mkdirs();
		logger.debug("Dir created? "+booCreated);
		booCreated = destFile.mkdirs();
		logger.debug("again Dir created? "+booCreated);
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
//	    infFile = new File (destinationDir+ctFileName+M9kConstants.INF_FILE_EXTN);
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

	public String getLtrFileName() {
		return ltrFileName;
	}

	public void setLtrFileName(String ltrFileName) {
		this.ltrFileName = ltrFileName;
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
	public int getStationId() {
		return stationId;
	}
	public void setStationId(int stationId) {
		this.stationId = stationId;
	}
	public int getFaultId() {
		return faultId;
	}
	public void setFaultId(int faultId) {
		this.faultId = faultId;
	}
	public String getLtrType() {
		return ltrType;
	}
	public void setLtrType(String ltrType) {
		this.ltrType = ltrType;
	}
	public long getContentLength() {
		return contentLength;
	}
	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}
	public InputStream getInputStream() {
		return inputStream;
	}

}
