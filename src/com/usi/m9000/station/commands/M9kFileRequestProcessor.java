package com.usi.m9000.station.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.BlobMessage;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.station.util.M9kStationUtil;

public class M9kFileRequestProcessor  implements Runnable{
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kFileRequestProcessor.class);
	private ActiveMQSession session;
	private Message receivedMessage;
	private TextMessage textMessage;
	private MessageProducer replyProducer;
	public M9kFileRequestProcessor(Session session, MessageProducer replyProducer, Message receivedMessage) {
		this.session = (ActiveMQSession) session;
		this.replyProducer = replyProducer;
		this.receivedMessage = receivedMessage;
		textMessage = (TextMessage) receivedMessage;
	}
	
	public void run()
	{
		BlobMessage blobMessage = null;
		File fileToRetrieve = null;
		File activeConfigFile = null;
		String requestMessageType = null;
		try {
			requestMessageType = textMessage.getStringProperty("MESSAGE_TYPE");
			if (requestMessageType != null && requestMessageType.equalsIgnoreCase("ACTIVE_CONFIG"))
			{
				M9kRestoreActiveConfig m9kRestoreActiveConfig = new M9kRestoreActiveConfig();
				activeConfigFile = m9kRestoreActiveConfig.getActiveConfig();
				logger.debug("Requested active config sql file "+activeConfigFile);
				if (activeConfigFile != null && activeConfigFile.exists() && activeConfigFile.canRead())
		      	  {
					blobMessage = session.createBlobMessage(new FileInputStream(activeConfigFile));
					blobMessage.setStringProperty("FILE.NAME", activeConfigFile.getName());
					blobMessage.setLongProperty("FILE.SIZE", activeConfigFile.length()); 
					logger.debug("About to Send blob message "+activeConfigFile.getName()+" Size of file "+activeConfigFile.length());
	          		blobMessage.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
	          		Destination destinationQ = receivedMessage.getJMSReplyTo();
	
	           		  logger.debug("About to Send blob message "+destinationQ.toString()+" correlation Id "+receivedMessage.getJMSCorrelationID());
	           		  replyProducer.send(destinationQ, blobMessage);
	           		  logger.debug("Sent blob message ");
	           	  }
	           	  else
	           	  {
	           		TextMessage errorResponse = session.createTextMessage();
	           		  errorResponse.setText("Unable to restore active config. Please ensure atleast one chassis is connected"+M9kStationUtil.getDataDir()+textMessage.getText());
	       			  errorResponse.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
	       			  this.replyProducer.send(receivedMessage.getJMSReplyTo(),errorResponse); 
	           	  }
			}
			else
			{
	//			zipAllFiles();
				fileToRetrieve = zipAllFiles();
				if (fileToRetrieve != null && fileToRetrieve.exists() && fileToRetrieve.canRead())
		      	  {
					blobMessage = session.createBlobMessage(new FileInputStream(fileToRetrieve));
					blobMessage.setStringProperty("FILE.NAME", fileToRetrieve.getName());
					blobMessage.setLongProperty("FILE.SIZE", fileToRetrieve.length()); 
					logger.debug("About to Send blob message "+fileToRetrieve.getName()+" Size of file "+fileToRetrieve.length());
	          		blobMessage.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
	          		Destination destinationQ = receivedMessage.getJMSReplyTo();
	
	//                replyProducer = session.createProducer(destinationQ);
	//                replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	
	           		  logger.debug("About to Send blob message "+destinationQ.toString()+" correlation Id "+receivedMessage.getJMSCorrelationID());
	           		  replyProducer.send(destinationQ, blobMessage);
	           		  logger.debug("Sent blob message ");
	//           		  fileToRetrieve.delete();
	           	  }
	           	  else
	           	  {
	           		TextMessage errorResponse = session.createTextMessage();
	           		  errorResponse.setText("Unable to fetch requested file. Check the file path/Name."+M9kStationUtil.getDataDir()+textMessage.getText());
	       			  errorResponse.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
	       			  this.replyProducer.send(receivedMessage.getJMSReplyTo(),errorResponse); 
	           	  }
			}
		} catch (JMSException e) {
			try
			{
				TextMessage errorResponse = session.createTextMessage();
         		  errorResponse.setText("Unable to fetch requested file. Check the file path/Name."+M9kStationUtil.getDataDir()+textMessage.getText());
     			  errorResponse.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
     			  this.replyProducer.send(receivedMessage.getJMSReplyTo(),errorResponse); 
			}
			catch (Exception ex) {
				logger.warn("Exception caught within exception during file processing ",ex);
			}
			
			logger.error("Error occured while getting requested file",e);
		} catch (Exception e) {
			try
			{
				TextMessage errorResponse = session.createTextMessage();
         		  errorResponse.setText("Unable to fetch requested file. Check the file path/Name."+M9kStationUtil.getDataDir()+textMessage.getText());
     			  errorResponse.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
     			  this.replyProducer.send(receivedMessage.getJMSReplyTo(),errorResponse); 
			}
			catch (Exception ex) {
				logger.warn("Exception caught within exception during file processing ",ex);
			}
		} 
		finally
		{
			try
			{
				if (fileToRetrieve != null && fileToRetrieve.exists())
				{
					fileToRetrieve.delete();
				}
			}
			catch (Exception fe)
			{
				logger.warn("Exception caught within finally during file processing ",fe);
			}
		}
		
	}
	
	private File zipAllFiles() throws M9000Exception
	{
		byte[] buffer = new byte[1024];
		File zipFile = null;
		 
    	try{
    		String fileName = textMessage.getText().substring(textMessage.getText().lastIndexOf(File.separator)+1);
    		String filePathPrefix;
    		fileName = fileName.replace("/", Matcher.quoteReplacement(File.separator));
    		if (textMessage.getText().startsWith(File.separator))
    		{
    			filePathPrefix =  textMessage.getText().substring(1, textMessage.getText().lastIndexOf(File.separator)+1);// /Prefix like 1-stationName/ Avoid extra file seperator at start
    		}
    		else
    		{
    			filePathPrefix =  textMessage.getText().substring(0, textMessage.getText().lastIndexOf(File.separator)+1);// /Prefix like 1-stationName/
    		}
    		int index;
    		if ((index = fileName.indexOf(".")) != -1)
    		{
    			fileName = fileName.substring(0, index);
    		}
    		String destZipfilePath = M9kStationUtil.getDataDir()+filePathPrefix+"compressed"+File.separator+"";
    		String sourceFilePrefix = M9kStationUtil.getDataDir()+filePathPrefix+fileName;
    		logger.debug("dest zip path "+destZipfilePath+" filePathPrefix "+filePathPrefix +" file name "+fileName+" sourceFilePrefix "+sourceFilePrefix);
    		File destDir = new File(destZipfilePath);
    		if (!destDir.exists())
    		{
    			destDir.mkdirs();
    		}
    		destDir.setWritable(true, false);
    		destDir.setExecutable(true,false);
    		destDir.setReadable(true, false);

    		zipFile = new File(destDir,(fileName+".zip"));
    		FileOutputStream fos = new FileOutputStream(zipFile);
    		ZipOutputStream zos = new ZipOutputStream(fos);
    		
    		ZipEntry ze= new ZipEntry(fileName+".dat");
    		zos.putNextEntry(ze);
    		FileInputStream in = new FileInputStream(new File(sourceFilePrefix+".dat"));
 
    		int len;
    		while ((len = in.read(buffer)) > 0) {
    			zos.write(buffer, 0, len);
    		}
 
    		in.close();
    		
    		ze= new ZipEntry(fileName+".cfg");
    		zos.putNextEntry(ze);
    		in = new FileInputStream(new File(sourceFilePrefix+".cfg"));
 
    		while ((len = in.read(buffer)) > 0) {
    			zos.write(buffer, 0, len);
    		}
 
    		in.close();
    		
    		File infFile = new File(sourceFilePrefix+".inf");
    		if (infFile.exists())
    		{
				ze= new ZipEntry(fileName+".inf");
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
//	public BlobMessage getRequestedFile(String fileName, ActiveMQSession session)
//	{
//		BlobMessage blobMessage = null;
//		try {
//			File fileToRetrieve = new File(M9kStationUtil.getDataDir()+fileName);
//			if (fileToRetrieve.exists() && fileToRetrieve.canRead())
//	      	  {
//				blobMessage = session.createBlobMessage(fileToRetrieve);
//				blobMessage.setStringProperty("FILE.NAME", fileToRetrieve.getName());
//				blobMessage.setLongProperty("FILE.SIZE", fileToRetrieve.length()); 
//				logger.debug("About to Send blob message "+fileToRetrieve.getName()+" Size of file "+fileToRetrieve.length());
//	      	  }
//		} catch (JMSException e) {
//			blobMessage = null;
//			logger.error("Error occured while getting requested file",e);
//		} catch (Exception e) {
//			blobMessage = null;
//			logger.error("Error occured while getting requested file",e);
//		} 
//		return blobMessage;
//	}


}
