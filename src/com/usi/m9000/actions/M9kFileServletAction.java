package com.usi.m9000.actions;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.jms.TextMessage;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.activemq.BlobMessage;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.reports.M9kComtradeDetailsReportPDF;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kMessagesUtil;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.util.MessageProperties;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
 
public class M9kFileServletAction extends ActionSupport implements SessionAware, ServletContextListener{
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	   private String filePath;
	   private String fileName = "";
	   private String fromDateTime = "";
	   private String toDateTime = "";
	   private String contDataType = "";
	    private ResourceBundle bundle;
	    private long contentLength;
	    private String contentType="application/octet-stream";
	    private String[] arrOfFileNames;
	    private String deleteFileStatus;
	private InputStream inputStream;

	private String stationId = null;
	private File srcFile = null;
	private File zipFile;

	private Map<String, Object> session;

	private StationDTO stationDetails;
	
	private UsersDTO userDto;
	private File importSql;
	private String importSqlFileName;
	private String importSqlContentType;
	
	private Map<String,String> mapBackupFiles;
	private String selectedBackupFile;

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kFileServletAction.class);
	
	public String getFile() throws M9000Exception {
		OutputStream os = null;
		BufferedOutputStream bos = null;
		InputStream blobInputStream = null;
	    try {
	
			bundle = ResourceBundle.getBundle("M9K_COMTRADE");
	        // Define base path somehow. You can define it as init-param of the servlet.
	    	if (getFilePath() == null)
	    	{
				try
				{
					filePath = bundle.getString("dataDir");
			        if (!filePath.endsWith("/"))
			        {
			        	filePath+="/";
			        }
				}
				catch (Exception e)
				{
					logger.error("Unable to get data directory .Assuming default /data/m9k/faults/");
					filePath="/data/m9k/faults/";
				}
	    	}
	        logger.debug("\t\tFile name received "+filePath+getFileName());
	        if (getFileName().endsWith("cfg"))
	        {
	        	setContentType("text/plain");
	        }
	        else if (getFileName().endsWith("dat"))
	        {
	        	setContentType("application/octet-stream");
	        }
	        logger.debug("Station ID received: "+getStationId());
	        srcFile = new File(filePath+getFileName());
	        if (!srcFile.exists() || srcFile.length() == 0)
	        {
	    		boolean booCreated  = srcFile.getParentFile().setWritable(true);
	    		logger.debug("Set writable dest folder "+srcFile.getParentFile().getPath()+" ? "+booCreated);
	    		logger.debug("ServletActionContext.getServletContext()).getRealPath(/)"+ServletActionContext.getServletContext().getRealPath("/"));
	    		logger.debug("Destination dir "+filePath+" file path "+srcFile.getPath()+" exists? "+srcFile.exists()+" can write? "+srcFile.getParentFile().canWrite());
	    		booCreated = srcFile.getParentFile().mkdirs();
	    		
	    		logger.debug("Dir created? "+booCreated);
//	    		booCreated = srcFile.mkdirs();
//	    		logger.debug("again Dir created? "+booCreated);
	    		
	        	Object receivedObj;
				MessageProperties messageProperties = new MessageProperties();
				messageProperties.addProperty("MESSAGE_TYPE", "FILE_RETRIEVE");
				messageProperties.addProperty("USER_NAME", (getUserDto()!=null?getUserDto().getUserName():"User "));
	            logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" has requested for the file "+getFileName());

        		receivedObj = M9kMessagesUtil.getRequiredFile(getStationId()+"-Files", getFileName(),messageProperties);
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
	        		
	        		String srcZipFilePath = M9kStationUtil.getDataDir()+getFileName().substring(0, getFileName().lastIndexOf("/")+1)+"compressed"+"/"+"";
	         		File destZipDir = new File(srcZipFilePath);
	         		if (!destZipDir.exists())
	         		{
	         			destZipDir.mkdirs();
	         		}
	         		
//	        		srcFile = new File(filePath + fileName); 
	        		zipFile = new File(destZipDir,fileName);
	        		os = new FileOutputStream(zipFile); 
	        		bos = new BufferedOutputStream(os); 
	
	        		blobInputStream = blobMessage.getInputStream(); 
	        		while ((length = blobInputStream.read(buffer,0,buffer.length)) > 0) {
	        			totalLength+=length;
//	        			logger.debug("length read "+length+" total length "+totalLength);
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
	        			inputStream = null;
		        		throw new M9000Exception(receivedObj.toString());
	        		}
	        		else
	        		{
	        			unZipFaultFiles();
	        		}
	        	}
	        }
	        logger.debug("Content type set "+getContentType());
	        setContentLength(srcFile.length());
	        logger.debug("Content length set "+getContentLength());
	        if (srcFile.length() > 0)
	        {
	        	logger.debug("File has some content");
	        	inputStream = new FileInputStream(srcFile);
	        }
	        else
	        {
	        	logger.error("Empty file found. "+srcFile);
				throw new M9000Exception("Empty file content");
	        }
		} catch (FileNotFoundException e) {
			logger.error("Error in reading file "+srcFile,e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Error in reading file "+srcFile,e);
			throw new M9000Exception(e);
		}
		finally
		{
    		try {
    			if (bos != null)
    			{
    				bos.close();
    			}
    			if (blobInputStream != null)
    			{
    				blobInputStream.close();
    			}
    			if (os != null)
    			{
    				os.close();
    			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		logger.debug("returning SUCCESS");
		logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" Successfully fetched required file "+getFileName());
	    return SUCCESS;
	}
	
	public String getZipFile() throws M9000Exception {
		OutputStream os = null;
		BufferedOutputStream bos = null;
		InputStream blobInputStream = null;
	    try {
	
			bundle = ResourceBundle.getBundle("M9K_COMTRADE");
	        // Define base path somehow. You can define it as init-param of the servlet.
	    	if (getFilePath() == null)
	    	{
				try
				{
					filePath = bundle.getString("dataDir");
			        if (!filePath.endsWith("/"))
			        {
			        	filePath+="/";
			        }
				}
				catch (Exception e)
				{
					logger.error("Unable to get data directory .Assuming default /data/m9k/faults/");
					filePath="/data/m9k/faults/";
				}
	    	}
	        logger.debug("\t\tFile name received "+filePath+getFileName());
//	        System.out.println("\t\tFile name received "+filePath+getFileName());
	        setFileName(getFileName()+".cfg");
//	        if (getFileName().endsWith("cfg"))
//	        {
//	        	setContentType("text/plain");
//	        }
//	        else if (getFileName().endsWith("dat"))
//	        {
//	        	setContentType("application/octet-stream");
//	        }
	        logger.debug("Station ID received: "+getStationId());
//	        System.out.println("Station ID received: "+getStationId());
	        srcFile = new File(filePath+getFileName());
//	        System.out.println("src File path "+srcFile.getPath());
//	        System.out.println("src File Name "+srcFile.getName());
//	        System.out.println("src File absolute path "+srcFile.getAbsolutePath());
//	        System.out.println("src File exists "+srcFile.exists());
//	        System.out.println("src File path "+srcFile.length());
//	        System.out.println("src File Parent "+srcFile.getParent());
	        if (!srcFile.exists() || srcFile.length() == 0)
	        {
	    		boolean booCreated  = srcFile.getParentFile().setWritable(true);
	    		logger.debug("Set writable dest folder "+srcFile.getParentFile().getPath()+" ? "+booCreated);
	    		logger.debug("ServletActionContext.getServletContext()).getRealPath(/)"+ServletActionContext.getServletContext().getRealPath("/"));
	    		logger.debug("Destination dir "+filePath+" file path "+srcFile.getPath()+" exists? "+srcFile.exists()+" can write? "+srcFile.getParentFile().canWrite());
	    		booCreated = srcFile.getParentFile().mkdirs();
	    		logger.debug("Dir created? "+booCreated);
//	    		booCreated = srcFile.mkdirs();
//	    		logger.debug("again Dir created? "+booCreated);
	    		
	        	Object receivedObj;
				MessageProperties messageProperties = new MessageProperties();
				messageProperties.addProperty("MESSAGE_TYPE", "FILE_RETRIEVE");
				messageProperties.addProperty("USER_NAME", (getUserDto()!=null?getUserDto().getUserName():"User "));
	            logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" has requested for the file "+getFileName());

        		receivedObj = M9kMessagesUtil.getRequiredFile(getStationId()+"-Files", getFileName(),messageProperties);
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
	        		
	        		String srcZipFilePath = M9kStationUtil.getDataDir()+getFileName().substring(0, getFileName().lastIndexOf("/")+1)+"compressed"+"/"+"";
	         		File destZipDir = new File(srcZipFilePath);
	         		if (!destZipDir.exists())
	         		{
	         			destZipDir.mkdirs();
	         		}
	         		
//	        		srcFile = new File(filePath + fileName); 
	        		zipFile = new File(destZipDir,fileName);
	        		os = new FileOutputStream(zipFile); 
	        		bos = new BufferedOutputStream(os); 
	
	        		blobInputStream = blobMessage.getInputStream(); 
	        		while ((length = blobInputStream.read(buffer,0,buffer.length)) > 0) {
	        			totalLength+=length;
//	        			logger.debug("length read "+length+" total length "+totalLength);
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
	        			inputStream = null;
		        		throw new M9000Exception(receivedObj.toString());
	        		}
	        		else
	        		{
	        			unZipFaultFiles();
	        		}
	        	}
	        }
//	        else
//	        {
	        	zipFile = zipAllFiles();
//	        }
	        setContentType("application/zip");
	        logger.debug("Content type set "+getContentType());
	        setContentLength(zipFile.length());
	        logger.debug("Content length set "+getContentLength());
	        if (zipFile.length() > 0)
	        {
	        	logger.debug("File has some content");
	        	setFileName(zipFile.getName());
	        	inputStream = new FileInputStream(zipFile);
	        }
	        else
	        {
	        	logger.error("Empty file found. "+srcFile);
				throw new M9000Exception("Empty file content");
	        }
		} catch (FileNotFoundException e) {
			logger.error("Error in reading file "+srcFile,e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Error in reading file "+srcFile,e);
			throw new M9000Exception(e);
		}
		finally
		{
    		try {
    			if (bos != null)
    			{
    				bos.close();
    			}
    			if (blobInputStream != null)
    			{
    				blobInputStream.close();
    			}
    			if (os != null)
    			{
    				os.close();
    			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		logger.debug("returning SUCCESS");
		logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" Successfully fetched required file "+getFileName());
	    return SUCCESS;
	}

	// It receives zip file with both dat and cnf file and returns the dat filestream 
	public String getContinuousDataFileName() throws M9000Exception {
		try {
			
	        logger.debug("Station ID received: "+getStationId()+" getFromDateTime() "+getFromDateTime()+" getToDateTime() "+getToDateTime()+" getContDataType() "+getContDataType());
	        	Object receivedObj;
	        	MessageProperties messageProperties = new MessageProperties();
				messageProperties.addProperty("USER_NAME", (getUserDto()!=null?getUserDto().getUserName():"User "));
				messageProperties.addProperty("FROM_DATETIME", getFromDateTime());
				messageProperties.addProperty("TO_DATETIME", getToDateTime());
				messageProperties.addProperty("DATA_TYPE", getContDataType());
				messageProperties.addProperty("MESSAGE_TYPE", "CONT_DATA_RETRIEVE");
        		receivedObj = M9kMessagesUtil.getContinuousData(getStationId()+"-Files", messageProperties);
	        	if (receivedObj == null)
	        	{
	        		logger.error("Error in fetching file name for continuous data: NULL object returned from station master");
	        		inputStream = null;
	        		throw new M9000Exception("NULL object returned from station master");	        		
	        	}
	        	else if (receivedObj instanceof TextMessage) // External calibration returns text
	            {
	        		receivedObj = ((TextMessage)receivedObj).getText();
	            }
	        	logger.debug("The received string object "+receivedObj.toString());
	        	try {
	        		inputStream = new ByteArrayInputStream(receivedObj.toString().getBytes("UTF-8"));
	        		
	    		} catch (UnsupportedEncodingException e) {
	    			logger.error("Error in creating input stream for filename ", e);
	    			throw new M9000Exception(e);
	    		}
		}
		catch (Exception e) {
			logger.error("Error in reading file "+srcFile,e);
			throw new M9000Exception(e);
		}
// Test code
//		BufferedReader bfr;
//		String contDataFileName;
//		try{
//		logger.debug("returning SUCCESS");
//		bfr = new BufferedReader(new InputStreamReader(getStream()));
//		StringBuffer sb = new StringBuffer();
//		while ((contDataFileName=bfr.readLine()) != null)
//		{
//			sb.append(contDataFileName);
//			logger.debug("String buffer "+sb);
//		}
//		logger.debug("Cont data file name "+sb.toString());
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
		logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" successfully fetched the continuous data");
	    return SUCCESS;
	}	
	/**
     * Unzip it
     * @param zipFile input zip file
     * @param output zip file output folder
     */
    public void unZipFaultFiles() throws M9000Exception
    {
 
     byte[] buffer = new byte[1024];
     ZipInputStream zis = null;
     FileOutputStream fos = null;
 
     try{
 
    	 String destFaultFilePath = M9kStationUtil.getDataDir()+getFileName().substring(0, getFileName().lastIndexOf("/")+1);
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
    
    
    public String saveConfigFile()  throws M9000Exception 
    {
    	logger.debug("Enter saveConfigFile... ");
		OutputStream os = null;
		BufferedOutputStream bos = null;
		InputStream blobInputStream = null;
		String configFileSql;
	    try {
	    	stationDetails = (StationDTO) session.get("stationDetails");
	    	configFileSql = stationDetails.getStationDetailsAsSQLStmt();
        	setContentType("text/plain");
	        setContentType("application/octet-stream");
	        setContentLength(configFileSql.length());
	        String fileTimeStamp = new SimpleDateFormat("MMddyyyyHHmmss").format(new Date());
	        // START: 17-Oct-2019 - Prefix the file name with R
//	        setFileName(stationDetails.getSystemStationName()+"-config-"+fileTimeStamp+".sql");
	        setFileName( fileTimeStamp+",R"+String.format("%02d", stationDetails.getSystemStationId())+"-"+stationDetails.getSystemStationName().replace(" ", "_")+"-config"+M9kUtils.getM9kConfigFileExtension());
	        // END: 17-Oct-2019
	        logger.debug("Content length set "+getContentLength());
	        logger.debug("Filename "+getFileName());
	        if (configFileSql.length() > 0)
	        {
	        	logger.debug("File has some content");
	        	inputStream =  new ByteArrayInputStream(configFileSql.getBytes("UTF-8"));
	        	logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" exported config file in SQL format for station "+stationDetails.getStationDisplayName());
	        }
	        else
	        {
	        	logger.error("Empty file found. "+srcFile);
	        	logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" encountered errors while exporting config file in SQL format for station "+stationDetails.getStationDisplayName());
				throw new M9000Exception("Empty file content");
	        }
		} 
		catch (Exception e) {
			logger.error("Error in saving config file",e);
			throw new M9000Exception(e);
		}
		finally
		{
    		try {
    			if (bos != null)
    			{
    				bos.close();
    			}
    			if (blobInputStream != null)
    			{
    				blobInputStream.close();
    			}
    			if (os != null)
    			{
    				os.close();
    			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		logger.debug("returning SUCCESS");
	    return SUCCESS;
	
    }
   
    public String printStationConfig1()  throws M9000Exception 
    {

    	logger.debug("Enter printStationConfigFile... ");
//		ByteArrayOutputStream baos = null;
		
		OutputStream os = null;
		BufferedOutputStream bos = null;
		InputStream blobInputStream = null;
		String configFileSql;
	    try {
	    	stationDetails = (StationDTO) session.get("stationDetails");
	    	configFileSql = stationDetails.getStationDetailsAsSQLStmt();
        	setContentType("text/plain");
	        setContentType("application/octet-stream");
	        setContentLength(configFileSql.length());
	        String fileTimeStamp = new SimpleDateFormat("MMddyyyy_HHmmss").format(new Date());
	        setFileName(stationDetails.getSystemStationName()+"-config-"+fileTimeStamp);
	        logger.debug("Content length set "+getContentLength());
	        logger.debug("Filename "+getFileName());
//	        M9kComtradeDetailsReportPDF m9kComtradeDetailsReportPDF = new M9kComtradeDetailsReportPDF(stationDetails, "Station Configuration Report");
//	        JasperReportBuilder stationConfigReport = m9kComtradeDetailsReportPDF.buildPDF();
	        if (configFileSql.length() > 0)
	        {
	        	logger.debug("File has some content");
	        	inputStream =  new ByteArrayInputStream(configFileSql.getBytes("UTF-8"));
	        }
	        else
	        {
	        	logger.error("Empty file found. "+srcFile);
				throw new M9000Exception("Empty file content");
	        }
		} 
		catch (Exception e) {
			logger.error("Error in saving config file",e);
			throw new M9000Exception(e);
		}
		finally
		{
    		try {
    			if (bos != null)
    			{
    				bos.close();
    			}
    			if (blobInputStream != null)
    			{
    				blobInputStream.close();
    			}
    			if (os != null)
    			{
    				os.close();
    			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		logger.debug("returning SUCCESS");
	    return SUCCESS;
	
    }
    public String printStationConfig()  throws M9000Exception 
    {
    	logger.info("Enter printStationConfig... ");
		ByteArrayOutputStream baos = null;
	    try {
	    	String fileTimeStamp = new SimpleDateFormat("MMddyyyy_HHmmss").format(new Date());
	    	stationDetails = (StationDTO) session.get("stationDetails");
	    	if (stationDetails == null)
	    	{
	    		logger.error("May be session timed out");
	    		throw new M9000Exception("Session timed out");
	    	}
	    	M9kComtradeDetailsReportPDF m9kComtradeDetailsReportPDF = new M9kComtradeDetailsReportPDF(stationDetails, "Station Configuration Report");
	    	JasperReportBuilder stationConfigReport = m9kComtradeDetailsReportPDF.buildPDF();
	    	if (stationConfigReport != null)
	    	{
		    	baos = new ByteArrayOutputStream();
		    	stationConfigReport.toPdf(baos);
		    	inputStream = new ByteArrayInputStream(baos.toByteArray());
		        setContentType("application/pdf");
		        setContentLength(baos.toByteArray().length);
		        setFileName(stationDetails.getSystemStationName()+"-config-report-"+fileTimeStamp+".pdf");
		        logger.debug("Content length set "+getContentLength());
		        logger.debug("Filename "+getFileName());
		        logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" printed config file in PDF format for station "+stationDetails.getStationDisplayName());
	    	}
	    	else
	        {
	        	logger.error("PDF turned out to be null. ");
	        	logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" encountered errors while printing config file in PDF format for station "+stationDetails.getStationDisplayName());
				throw new M9000Exception("No Report generated");
	        }
	    	

		} 
		catch (Exception e) {
			logger.error("Error in saving config file",e);
			throw new M9000Exception(e);
		}
		finally
		{
    		try {
    			if (baos != null)
    			{
    				baos.close();
    			}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		logger.debug("returning SUCCESS");
	    return SUCCESS;
	
    }

	public String deleteFiles() throws M9000Exception {
		File fileTobeDeleted = null;
		
		logger.info("String array of file names received "+arrOfFileNames[0]);
		
	    try {
	
			bundle = ResourceBundle.getBundle("M9K_COMTRADE");
	        // Define base path somehow. You can define it as init-param of the servlet.
	    	if (getFilePath() == null)
	    	{
				try
				{
					filePath = bundle.getString("dataDir");
			        if (!filePath.endsWith("/"))
			        {
			        	filePath+="/";
			        }
				}
				catch (Exception e)
				{
					logger.error("Unable to get data directory .Assuming default /data/m9k/faults/");
					filePath="/data/m9k/faults/";
				}
	    	}
	    	for (int i = 0; i < arrOfFileNames.length; i++) {
		        logger.info("\t\tFile name received "+filePath+arrOfFileNames[i]);
		        
		        // Delete conf file
		        fileTobeDeleted = new File(filePath+arrOfFileNames[i]+M9kConstants.CFG_FILE_EXTN);
		        logger.info("File path "+fileTobeDeleted.getPath());
		        logger.info("File delete ? "+fileTobeDeleted.getParentFile().canWrite());
		        if (fileTobeDeleted.exists())
		        {
		        	fileTobeDeleted.delete();
		        	fileTobeDeleted = null;
		        }
		        // Delete dat file  
		        fileTobeDeleted = new File(filePath+arrOfFileNames[i]+M9kConstants.DAT_FILE_EXTN);
		        if (fileTobeDeleted.exists())
		        {
		        	fileTobeDeleted.delete();
		        	fileTobeDeleted = null;
		        }
		        // Delete inf file  
		        fileTobeDeleted = new File(filePath+arrOfFileNames[i]+M9kConstants.INF_FILE_EXTN);
		        if (fileTobeDeleted.exists())
		        {
		        	fileTobeDeleted.delete();
		        	fileTobeDeleted = null;
		        }
		        
				
			}
	    	deleteFileStatus = SUCCESS;
		} 
		catch (Exception e) {
			logger.error("Error in deleting files "+fileTobeDeleted,e);
			deleteFileStatus = ERROR+e.getMessage();
//			throw new M9000Exception(e);
		}
		try {
			inputStream = new ByteArrayInputStream(deleteFileStatus.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error("Error in deleting files. Unable to return response message "+fileTobeDeleted,e);
		}

		logger.debug("returning SUCCESS");
		logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" Successfully deleted the user selected "+getArrOfFileNames().length+" files ");
	    return SUCCESS;
	}

	private File zipAllFiles() throws M9000Exception
	{
		byte[] buffer = new byte[1024];
		File zipFile = null;
		 
    	try{
    		String filePathPrefix;
    		int index;
    		if ((index = fileName.indexOf(".")) != -1)
    		{
    			fileName = fileName.substring(0, index);
    		}
    		// To overcome UNIX and Windows file seperator issue
    		fileName = fileName.replace("/", Matcher.quoteReplacement(File.separator));
    		if (fileName.startsWith(File.separator))
    		{
    			filePathPrefix =  fileName.substring(1, fileName.lastIndexOf(File.separator)+1);// /Prefix like 1-stationName/ Avoid extra file seperator at start
    		}
    		else
    		{
    			filePathPrefix =  fileName.substring(0, fileName.lastIndexOf(File.separator)+1);// /Prefix like 1-stationName/
    		}
    		String destZipfilePath = filePath+filePathPrefix+"compressed"+File.separator+"";
    		String sourceFilePrefix = filePath+fileName;
    		String fileNameWithoutPrefix = fileName.substring(fileName.lastIndexOf(File.separator)+1);
    		logger.debug("File Name "+fileName+" dest zip path "+destZipfilePath+" filePathWithPrefix "+filePath+" file name without prefix "+fileNameWithoutPrefix+" sourceFilePrefix "+sourceFilePrefix);
    		File destDir = new File(destZipfilePath);
    		logger.debug("dest dir "+destZipfilePath +" Dest dir exists ? "+destDir.exists());
    		if (!destDir.exists())
    		{
    			destDir.mkdirs();
    		}
    		logger.debug("dest dir "+destZipfilePath +" Dest dir exists after create ? "+destDir.exists());
    		destDir.setWritable(true, false);
    		destDir.setExecutable(true,false);
    		destDir.setReadable(true, false);
    		
    		zipFile = new File(destDir,(fileNameWithoutPrefix+".zip"));
    		FileOutputStream fos = new FileOutputStream(zipFile);
    		ZipOutputStream zos = new ZipOutputStream(fos);
    		
    		ZipEntry ze= new ZipEntry(fileNameWithoutPrefix+".dat");
    		zos.putNextEntry(ze);
    		FileInputStream in = new FileInputStream(new File(sourceFilePrefix+".dat"));
 
    		int len;
    		while ((len = in.read(buffer)) > 0) {
    			zos.write(buffer, 0, len);
    		}
 
    		in.close();
    		
    		ze= new ZipEntry(fileNameWithoutPrefix+".cfg");
    		zos.putNextEntry(ze);
    		in = new FileInputStream(new File(sourceFilePrefix+".cfg"));
 
    		while ((len = in.read(buffer)) > 0) {
    			zos.write(buffer, 0, len);
    		}
 
    		in.close();
    		
    		File infFile = new File(sourceFilePrefix+".inf");
    		if (infFile.exists())
    		{
				ze= new ZipEntry(fileNameWithoutPrefix+".inf");
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
//    		System.out.println("Done creating zip file"+((zipFile == null)?"Zip file is null":zipFile.getName()));
 
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
	
    @Action(value = "importStationAction", interceptorRefs = {@InterceptorRef("exceptionMappingStack")}, results = {
        		@Result(name = SUCCESS, type = "chain", params = {"actionName", "backToStationList"}),
        		@Result(name=INPUT, type = "chain", params = {"actionName", "backToStationList"}),
        		@Result(name=ERROR,type = "chain", params = {"actionName", "backToStationList"})})
	public String importStationConfig()
	{
		String returnStatus = SUCCESS;
		String queryFromConfigFile;
		int stationId;
		BufferedReader importedFile;
		StringBuilder sb = new StringBuilder();
		String line;
		try
		{
			logger.debug("Selected file to import "+getImportSql()+" actual file name "+getImportSqlFileName()+" file content type "+getImportSqlContentType());
			importedFile = new BufferedReader(new FileReader(getImportSql()));
			while ((line = importedFile.readLine()) != null)
			{
			    sb.append(line);
			    sb.append(M9kConstants.NEWLINE);
			}
			if (sb.toString().toUpperCase().indexOf("INSERT INTO STATION_DETAILS") == -1)
			{
				addActionError("ERROR: Unable to import config. Incorrect File Content.");
				logger.info("User "+userDto.getDisplayName()+" unsuccesfully attempted to import configuration with file name "+getImportSqlFileName());
				returnStatus = ERROR;
			}
			else
			{
				queryFromConfigFile = sb.toString();
				stationId = parseStationId(queryFromConfigFile);
				logger.debug("Station id from query "+stationId);
				importSqlContent(stationId, queryFromConfigFile);
			}
		}
		catch (Exception e) {
			addActionError("Unable to import config. Reason: "+e.getMessage());
			returnStatus = ERROR;
			logger.error("Unable to import config",e);
		}
		
		
		return returnStatus;
	}
    
    /**
     * Private method to take sql config content and import into database
     * @param stationId
     * @param queryFromConfigFile
     */
	private void importSqlContent(int stationId, String queryFromConfigFile) {
		M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		StationDAO stationDao = m9kDAOFactory.getStationDAO();

		try
		{
			if (M9kUtils.isRemote() == null || !M9kUtils.isRemote())
			{
					if (getStationDetails() != null)
					{
						M9kUtils.masterBackUpConfig(stationDetails);
					}
					stationDao.removeAllStationsForLocal();
					stationDao.importStationConfig(stationId, queryFromConfigFile);
					logger.debug("After importing into database");
	
					// 19-July-2021 - clear the data only if we import a new station. Keep the data if same station configuration is imported 
					logger.debug("Existing station id "+(getStationDetails() != null?getStationDetails().getSystemStationId():"No Stations")+" user trying to import station id "+stationId);
					// Before importing back up the existing configuration 
					if (getStationDetails() == null || getStationDetails().getSystemStationId() != stationId)
					{
						cleanUpStationData();
					}
					M9kUtils.setStationId(stationId);
					stationDetails = M9kUtils.getStationDetails();
					session.put("stationDetails",stationDetails);
					String reinitializeStationMasterStatus = (String) M9kMessagesUtil.sendSynchMessage(M9kUtils.getHostName(), "pkill -xf '^java.*M9kStation.jar$'","REINITIALIZE_STATION");
					if (reinitializeStationMasterStatus == null || reinitializeStationMasterStatus.toUpperCase().contains("ERROR"))
					{
						logger.warn("Reinitialization of station master unsuccucessful after removing config"+reinitializeStationMasterStatus);
					}
					addActionMessage("Successfully imported/restored configuration");
					logger.info("User "+userDto.getDisplayName()+" successfully imported configuration with file name "+getImportSqlFileName());
			}
			else
			{
				// If already a station exists then back up the station before importing a new one
				StationDTO existingStation = stationDao.getStationsDetails(stationId);
				if (existingStation != null)
				{
					M9kUtils.masterBackUpConfig(stationDetails);
				}
				stationDao.importStationConfig(stationId, queryFromConfigFile);
				M9kUtils.setStationId(stationId);
				stationDetails = M9kUtils.getStationDetails();
				session.put("stationDetails",stationDetails);
				// Nothing to clean up in case of remote architecture as there are multiple stations
			}
		}
		catch (Exception e) {
			addActionError("User "+M9kUtils.getUsersDto().getUserName()+" attempt to import/restore config failed.");
		}
		
	}

	private int parseStationId(String query) {
		int stationId;
		int secondParnathesisIndex = query.indexOf("(",query.indexOf("(")+1);
		logger.debug("second paranthesis index "+secondParnathesisIndex);
		logger.debug("station id string "+query.substring(secondParnathesisIndex+1,query.indexOf(",",secondParnathesisIndex+1)));
		stationId = Integer.parseInt(query.substring(secondParnathesisIndex+1,query.indexOf(",",secondParnathesisIndex+1)));
		return stationId;
	}

	@Action(value = "stationCleanupAction", interceptorRefs = {@InterceptorRef("exceptionMappingStack")}, results = {
    		@Result(name = SUCCESS, type = "chain", params = {"actionName", "backToStationList"}),
    		@Result(name=INPUT, type = "chain", params = {"actionName", "backToStationList"}),
    		@Result(name=ERROR,type = "chain", params = {"actionName", "backToStationList"})})
	public String cleanUpStationData()
	{
		String resultString = SUCCESS;
		try
		{
			if (getStationDetails() != null)
			{
				MessageProperties messageProperties = new MessageProperties();
				messageProperties.addProperty("MESSAGE_TYPE", "STATION_DATA_CLEANUP");
				messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
		
				String cleanupStatus = (String)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), "STATION_DATA_CLEANUP", messageProperties);
				logger.debug("After sending cleanup: "+cleanupStatus);
				if (cleanupStatus.toUpperCase().indexOf("ERROR") != -1)
				{
					logger.error("Error in clearing data");
					addActionError("User "+M9kUtils.getUsersDto().getUserName()+" attempt to clear data failed");
					resultString = ERROR;
				}
				else
				{
					logger.info("User "+M9kUtils.getUsersDto().getUserName()+" succesfully cleared the old data");
					addActionMessage("Succesfully cleared the data");
					resultString = SUCCESS;
				}
				if (M9kUtils.isRemote() != null && M9kUtils.isRemote())
				{
					// Cleanup Remote master station data
					logger.info("Cleaning up remote master database tables fo station "+M9kUtils.getStationDetails());
					M9kUtils.m9kMasterStationDbCleanup();
				}
			}

		}
		catch (Exception e) {
			logger.error("Error occurred while cleaning up station data",e);
			addActionError("User "+M9kUtils.getUsersDto().getUserName()+" attempt to clear data failed");
			resultString = ERROR;
		}
		return resultString;
	}
    @Action(value = "removeStationAction", interceptorRefs = {@InterceptorRef("exceptionMappingStack")}, results = {
    		@Result(name = SUCCESS, type = "chain", params = {"actionName", "backToStationList"}),
    		@Result(name=INPUT, type = "chain", params = {"actionName", "backToStationList"}),
    		@Result(name=ERROR,type = "chain", params = {"actionName", "backToStationList"})})
public String removeStation()
{
	String returnStatus = SUCCESS;
	M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
	StationDAO stationDao = m9kDAOFactory.getStationDAO();
	try
	{
		logger.debug("Station id from query "+getStationDetails().getSystemStationId());
//		stationDao.removeStation(Integer.parseInt(stationId));
		stationDao.removeStation(getStationDetails().getSystemStationId());
		logger.debug("After importing into database");
		addActionMessage("Succesfully deleted the station with station Id "+getStationDetails().getSystemStationId());
		if (M9kUtils.isRemote() == null || !M9kUtils.isRemote())
		{
			
			String reinitializeStationMasterStatus = (String) M9kMessagesUtil.sendSynchMessage(M9kUtils.getHostName(), "pkill -xf '^java.*M9kStation.jar$'","REINITIALIZE_STATION");
			if (reinitializeStationMasterStatus == null || reinitializeStationMasterStatus.toUpperCase().contains("ERROR"))
			{
				logger.warn("Reinitialization of station master unsuccucessful after removing config"+reinitializeStationMasterStatus);
			}
		}
		logger.info("User "+userDto.getDisplayName()+" successfully deleted the station with station id "+getStationDetails().getSystemStationId());
		session.remove("stationDetails");
	}
	catch (Exception e) {
		addActionError("Unable to delete the station"+getStationDetails().getSystemStationId()+". Reason: "+e.getMessage());
		returnStatus = ERROR;
		logger.error("Unable to delete the station"+getStationDetails().getSystemStationId(),e);
	}
	
	
	return returnStatus;
}

    @Action(value = "removeStationWithDataAction", interceptorRefs = {@InterceptorRef("exceptionMappingStack")}, results = {
    		@Result(name = SUCCESS, type = "chain", params = {"actionName", "backToStationList"}),
    		@Result(name=INPUT, type = "chain", params = {"actionName", "backToStationList"}),
    		@Result(name=ERROR,type = "chain", params = {"actionName", "backToStationList"})})
public String removeStationWithData()
{
    	String returnStatus = SUCCESS;
    	cleanUpStationData();
    	removeStation();
    	return returnStatus;
}
    @Action(value = "restoreActiveConfigAction", interceptorRefs = {@InterceptorRef("exceptionMappingStack")}, results = {
    		@Result(name = SUCCESS, type = "chain", params = {"actionName", "backToStationList"}),
    		@Result(name=INPUT, type = "chain", params = {"actionName", "backToStationList"}),
    		@Result(name=ERROR,type = "chain", params = {"actionName", "backToStationList"})})
	public String restoreActiveConfigFile() {
		String returnStatus = SUCCESS;
		String queryFromConfigFile = "";
	    try {
			MessageProperties messageProperties = new MessageProperties();
			messageProperties.addProperty("USER_NAME", (getUserDto()!=null?getUserDto().getUserName():"User "));
	    	if (M9kUtils.isRemote() == null || !M9kUtils.isRemote())
			{
				messageProperties.addProperty("ARCH_TYPE", "LOCAL");
				messageProperties.addProperty("MESSAGE_TYPE", "ACTIVE_CONFIG");
	            logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" has requested for the restoring active config file ");
	    		queryFromConfigFile = (String) M9kMessagesUtil.sendSynchMessageFromApplet(M9kUtils.getHostName(), M9kConstants.ACTIVE_CONFIG_SQL,messageProperties);
	    		if (queryFromConfigFile.toUpperCase().indexOf("INSERT INTO STATION_DETAILS") == -1)
				{
					addActionError("Either no config file available from Issue with the config file format.");					
					logger.error("User "+userDto.getDisplayName()+" unsuccesfully attempted to restore configuration from chassis ");
					returnStatus = ERROR;
				}
			}
	    	else
	    	{
				messageProperties.addProperty("MESSAGE_TYPE", "RETRIEVE_STATION_CONFIG");
				messageProperties.addProperty("ARCH_TYPE", "REMOTE");	    		
	    		queryFromConfigFile = (String) M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), "RETRIEVE_STATION_CONFIG",messageProperties);
	    		if (queryFromConfigFile.toUpperCase().indexOf("INSERT INTO STATION_DETAILS") == -1)
				{
					addActionError("Issue with the station master config file format.");					
					logger.error("User "+userDto.getDisplayName()+" unsuccesfully attempted to retrieve configuration from station");
					returnStatus = ERROR;
				}
	    	}
	    	if (returnStatus.equalsIgnoreCase(SUCCESS))
	    	{
				int stationId = parseStationId(queryFromConfigFile);
				logger.debug("Station id from query "+stationId);
				importSqlContent(stationId, queryFromConfigFile);
				if (M9kUtils.isRemote() != null && M9kUtils.isRemote())
				{
					addActionMessage("Successfully retrieved  and imported the config file from the station master");
				}
				else
				{
					addActionMessage("Successfully restored the config file from the first available chassis");
				}
				logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" Successfully imported the retrieved config");
	    	}
		} 
		catch (Exception e) {
			logger.error("Error in reading file "+srcFile,e);
			addActionError(e.getMessage());
			returnStatus = ERROR;
		}
		logger.debug("returning from restoring config file "+returnStatus);
	    return returnStatus;
	}


    @Action(value = "importFromBackupAction", interceptorRefs = {@InterceptorRef("exceptionMappingStack")}, results = {
    		@Result(name = SUCCESS, type = "chain", params = {"actionName", "backToStationList"}),
    		@Result(name=INPUT, type = "chain", params = {"actionName", "backToStationList"}),
    		@Result(name=ERROR,type = "chain", params = {"actionName", "backToStationList"})})
	public String importFromBackup() {
		String returnStatus = SUCCESS;
		try
		{
			setImportSql(new File(getSelectedBackupFile()));
			if (getImportSql() != null && getImportSql().exists())
			{
				returnStatus = importStationConfig();
			}
			else
			{
				addActionError("Unable to import config as file "+getImportSql().getName()+" doesn't seem to exist");
				returnStatus = ERROR;
			}
		}
		catch (Exception e) {
			addActionError("Unable to import config file "+getImportSql().getName()+". Reason: "+e.getMessage());
			returnStatus = ERROR;
		}
		return returnStatus;
    }
    
   @Action(value = "fetchBackupLst", results = {
   @Result(name="success",location="/jsp/backupFilesList.jsp")})
    @SkipValidation
    public String fetchBackupLst()
    {
	   logger.debug("Entered fetchBackupLst ");
        mapBackupFiles = M9kUtils.getMapOfMasterBackUpConfigs(stationDetails);
    	return SUCCESS;
    }
    
	public InputStream getFileInputStream() {
		return inputStream;
	}

	public String getFileName() {
		logger.debug("\n\t\t\t\t$$$$$$ File name to be returned "+fileName);
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}


	public String getStationId() {
		return stationId;
	}


	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	/**
	 * @return the fromDateTime
	 */
	public String getFromDateTime() {
		return fromDateTime;
	}

	/**
	 * @param fromDateTime the fromDateTime to set
	 */
	public void setFromDateTime(String fromDateTime) {
		this.fromDateTime = fromDateTime;
	}

	/**
	 * @return the toDateTime
	 */
	public String getToDateTime() {
		return toDateTime;
	}

	/**
	 * @param toDateTime the toDateTime to set
	 */
	public void setToDateTime(String toDateTime) {
		this.toDateTime = toDateTime;
	}

	/**
	 * @return the contDataType
	 */
	public String getContDataType() {
		return contDataType;
	}

	/**
	 * @param contDataType the contDataType to set
	 */
	public void setContDataType(String contDataType) {
		this.contDataType = contDataType;
	}

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public InputStream getInputStream() {
		 try {
			logger.debug("In getStream() IS data available...."+inputStream.available());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inputStream;
	}


	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.apache.struts2.interceptor.SessionAware#setSession(java.util.Map)
	 */
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
		stationDetails = (StationDTO) session.get("stationDetails");
		userDto = (UsersDTO) session.get("userDetails");
		logger.debug("In setSession "+getUserDto());

	}

	/**
	 * @return the userDto
	 */
	public UsersDTO getUserDto() {
		if (userDto == null)
    	{
    		userDto = (UsersDTO) session.get("userDetails");
    	}
		return userDto;
	}

	/**
	 * @param userDto the userDto to set
	 */
	public void setUserDto(UsersDTO userDto) {
		this.userDto = userDto;
	}

	public String[] getArrOfFileNames() {
		return arrOfFileNames;
	}

	public void setArrOfFileNames(String[] arrOfFileNames) {
		this.arrOfFileNames = arrOfFileNames;
	}

	public String getDeleteFileStatus() {
		return deleteFileStatus;
	}

	public void setDeleteFileStatus(String deleteFileStatus) {
		this.deleteFileStatus = deleteFileStatus;
	}

	/**
	 * @return the importSql
	 */
	public File getImportSql() {
		return importSql;
	}

	/**
	 * @param importSql the importSql to set
	 */
	public void setImportSql(File importSql) {
		this.importSql = importSql;
	}

	/**
	 * @return the importSqlFileName
	 */
	public String getImportSqlFileName() {
		return importSqlFileName;
	}

	/**
	 * @param importSqlFileName the importSqlFileName to set
	 */
	public void setImportSqlFileName(String importSqlFileName) {
		this.importSqlFileName = importSqlFileName;
	}

	/**
	 * @return the importSqlContentType
	 */
	public String getImportSqlContentType() {
		return importSqlContentType;
	}

	/**
	 * @param importSqlContentType the importSqlContentType to set
	 */
	public void setImportSqlContentType(String importSqlContentType) {
		this.importSqlContentType = importSqlContentType;
	}

	public Map<String,String> getMapBackupFiles() {
		return mapBackupFiles;
	}

	public void setMapBackupFiles(Map<String,String> mapBackupFiles) {
		this.mapBackupFiles = mapBackupFiles;
	}

	public String getSelectedBackupFile() {
		return selectedBackupFile;
	}

	public void setSelectedBackupFile(String selectedBackupFile) {
		this.selectedBackupFile = selectedBackupFile;
	}

	public StationDTO getStationDetails() {
		return stationDetails;
	}

	public void setStationDetails(StationDTO stationDetails) {
		this.stationDetails = stationDetails;
	}


}