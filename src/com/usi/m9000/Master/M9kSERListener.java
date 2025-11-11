package com.usi.m9000.Master;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.struts2.interceptor.SessionAware;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.common.email.SendMailUSI;
import com.usi.m9000.dao.M9kSerDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.EmailReportsSettingsDTO;
import com.usi.m9000.dto.EmailSettingsDTO;
import com.usi.m9000.dto.M9kSerDTO;
import com.usi.m9000.reports.M9kSERReportPDF;
import com.usi.m9000.reports.util.Column;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kReportUtil;
import com.usi.m9000.util.M9kUtils;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;

public class M9kSERListener implements SessionAware, ServletContextListener{
	private static final int THREAD_SIZE = 3; // Only for Remote master
	private M9kSerDAO serDao;
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(THREAD_SIZE);
	protected Runnable taskPerformer = null;
	private ScheduledFuture<?> scheduledTask = null;
	M9kSERListener serListener = null;
	M9kDAOFactory m9kDAOFactory;
	private EmailSettingsDTO emailSettingsDTO;
	private EmailReportsSettingsDTO emailReportsSettingsDTO;

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kSERListener.class);
	/**
	 * 
	 */
	public M9kSERListener() {
		m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		serDao = m9kDAOFactory.getM9kSerDAO();		
		emailSettingsDTO = M9kReportUtil.getEmailSettingsDTO();
		emailReportsSettingsDTO = M9kReportUtil.getEmailReportsSettingsDTO();

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

	public void scheduleSERListener()
	{
		  final Runnable taskPerformer = new Runnable() {
              public void run() {
            	  try
            	  {
//            		  logger.debug("ScheduleTask status in run "+scheduledTask.isCancelled());
               		  listenForNewSER();            		  
            	  }
            	  catch (Exception e) {
					logger.error("Error occured while processing ser events ",e);
				}
              }
          };
          logger.debug("Scheduling the task at every "+emailReportsSettingsDTO.getSerRunFrequency()+" seconds");
          scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, 0, emailReportsSettingsDTO.getSerRunFrequency(), TimeUnit.MINUTES);
          logger.debug("ScheduleTask status "+scheduledTask.isCancelled());
	}
	
	private void listenForNewSER() throws Exception{
		if (!M9kUtils.isRemote() && !emailSettingsDTO.isEnableEmail() || !emailReportsSettingsDTO.isEnableSerEmail())
		{
			return;
		}
		List<Integer> lstNewSerIds = serDao.getNewSERIds();
		logger.debug("Check for new SERs "+lstNewSerIds);
		List<M9kSerDTO> lstNewSerEvents = null;
		if (lstNewSerIds != null && !lstNewSerIds.isEmpty())
		{
			lstNewSerEvents = serDao.getAllNewSerEvents(lstNewSerIds);
			if (lstNewSerEvents != null && !lstNewSerEvents.isEmpty())
			{
				boolean booEmailStatus = createAndEmailNewSERFile(lstNewSerEvents);
				if (booEmailStatus && !emailReportsSettingsDTO.getSerEmailFileType().equals(M9kConstants.PDF_TYPE))
				{
					serDao.removeProcessedSerIds(lstNewSerIds);
				}
				
			}
		}
		else
		{
			logger.debug("No new SER events.");
		}

		
	}
	
	public boolean createAndEmailNewSERFile(List<M9kSerDTO> lstNewSerEvents) throws Exception{
		boolean emailStatus = false;
		File newSerFile = null;
		BufferedWriter bw = null ;
		StringBuffer strFileContent = new StringBuffer();
		M9kSerDTO m9kSerDTO = null;
		Map<Integer,List<M9kSerDTO>> mapSerPerStation = new HashMap<Integer, List<M9kSerDTO>>();
		String fileToBeWritten = new SimpleDateFormat("'ser-updates-'MMddyyyy_HHmmss'.pdf'").format(new Date());
		try
		{
			String serDestinationDir = M9kUtils.getSEREmailDir();
			File destSerDir = new File(serDestinationDir);
			boolean booflag = destSerDir.mkdirs();
			logger.debug("Created dir "+destSerDir.getPath()+" ? "+booflag);
			booflag = destSerDir.setReadable(true, false);
			booflag = destSerDir.setWritable(true, false);
			logger.debug("Destination dir "+serDestinationDir);
			logger.debug("About to create and send ser email "+strFileContent+" type of ser email file type "+M9kUtils.getSEREmailFileType());
			if (M9kUtils.getSEREmailFileType().equals(M9kConstants.PDF_TYPE))
			{
				List<Column> columnsList = createColumnData();
				logger.debug("Inside PDF processing columnsList "+columnsList);
				String reportTitle = "Sequence Of Event Recorder Report";
				List<M9kSerDTO> lstSerPerStation;
				int stationId;
				String stationName="";
				JasperReportBuilder serReport;
				M9kSERReportPDF m9kSERReportPDF;
				for (Iterator<M9kSerDTO> iterator1 = lstNewSerEvents.iterator(); iterator1.hasNext();) {
						m9kSerDTO = iterator1.next();
						if (mapSerPerStation.get(m9kSerDTO.getStationId()) == null)
						{
							mapSerPerStation.put(m9kSerDTO.getStationId(), new ArrayList<M9kSerDTO>());
						}
						mapSerPerStation.get(m9kSerDTO.getStationId()).add(m9kSerDTO);
				}
				logger.debug(" map of ser per stations "+mapSerPerStation);
				
				for (Iterator<Integer> iterator = mapSerPerStation.keySet().iterator(); iterator.hasNext();) {
					stationId = iterator.next();
					lstSerPerStation = mapSerPerStation.get(stationId);
					
					stationName = lstSerPerStation.get(0).getStationName();
					logger.debug("Invoking for station "+stationId+" name "+stationName+" list of ser per station "+lstSerPerStation);
					m9kSERReportPDF = new M9kSERReportPDF(reportTitle, columnsList, lstSerPerStation,stationId, stationName);
					logger.debug("About to buildPDF ");
					serReport = m9kSERReportPDF.buildPDF();
					logger.debug("After buildPDF serReport is null? "+(serReport == null));
					if (serReport != null)
			    	{
						ByteArrayOutputStream baos = null;
						FileOutputStream fos = null;
						try
						{
							fileToBeWritten = new SimpleDateFormat("MMddyyyy,HHmmss").format(new Date());
							fileToBeWritten+= ",R"+String.format("%02d", stationId)+","+stationName+",ser-updates.pdf";
							newSerFile = new File(destSerDir, fileToBeWritten);
							logger.debug("New file to be created "+newSerFile.getName()+" path "+newSerFile.getAbsolutePath());
							baos = new ByteArrayOutputStream();
							fos = new FileOutputStream(newSerFile);
					    	serReport.toPdf(baos);
					    	logger.debug("After toPDF method ");
					    	baos.writeTo(fos);
					    	fos.close();
					    	fos = null;
					    	baos.close();
					    	baos = null;
					    	
					    	strFileContent.append("Date, Time, Local Time Code, Substation, Device, State");
							strFileContent.append(M9kConstants.NEWLINE);

							for (Iterator<M9kSerDTO> iterator1 = lstSerPerStation.iterator(); iterator1.hasNext();) {
								m9kSerDTO = iterator1.next();
									strFileContent.append(m9kSerDTO.getDisplayTime().replace(" -", ","));
									strFileContent.append("," + M9kUtils.getLocalTimeCode());
									strFileContent.append("," + m9kSerDTO.getStationId()+"-"+m9kSerDTO.getStationName());
									strFileContent.append("," + m9kSerDTO.getName());
									strFileContent.append("," + m9kSerDTO.getCurrentStateAsString());
									strFileContent.append(M9kConstants.NEWLINE);
							}

							emailStatus = sendSerEmail(newSerFile, strFileContent.toString(),"New USI SER data. Station "+stationId+"-"+stationName);
							if (emailStatus)
							{
								serDao.removeProcessedSerIds(getListOfSerIds(lstSerPerStation));
							}
							strFileContent.setLength(0); // Clear old data
						}
						catch (Exception e) {
							logger.error("Error in creating PDF ser file ",e);
							throw new M9000Exception(e);
						}
						finally {
							if (baos!=null)
							{
								baos.close();
								baos = null;
							}
							if (fos!=null)
							{
								fos.close();
								fos = null;
							}
	
						}
			    	}
			    	else
			        {
			        	logger.error("PDF turned out to be null. ");
	//		        	logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" encountered errors while printing config file in PDF format for station "+stationDetails.getStationDisplayName());
						throw new M9000Exception("No Report generated");
			        }
				}
			}
			else
			{
				strFileContent.append("Date, Time, Local Time Code, Substation, Device, State");
				strFileContent.append(M9kConstants.NEWLINE);

				fileToBeWritten = new SimpleDateFormat("MMddyyyy,HHmmss,'ser-updates.csv'").format(new Date());
				newSerFile = new File(destSerDir, fileToBeWritten);
				for (Iterator<M9kSerDTO> iterator = lstNewSerEvents.iterator(); iterator.hasNext();) {
					m9kSerDTO = iterator.next();
						strFileContent.append(m9kSerDTO.getDisplayTime().replace(" -", ","));
						strFileContent.append("," + M9kUtils.getLocalTimeCode());
						strFileContent.append("," + m9kSerDTO.getStationId()+"-"+m9kSerDTO.getStationName());
						strFileContent.append("," + m9kSerDTO.getName());
						strFileContent.append("," + m9kSerDTO.getCurrentStateAsString());
						strFileContent.append(M9kConstants.NEWLINE);
						if (mapSerPerStation.get(m9kSerDTO.getStationId()) == null)
						{
							mapSerPerStation.put(m9kSerDTO.getStationId(), new ArrayList<M9kSerDTO>());
						}
						mapSerPerStation.get(m9kSerDTO.getStationId()).add(m9kSerDTO);
				}

				// Write Headers into files first
				bw = new BufferedWriter(new FileWriter(newSerFile,false));
				bw.write(strFileContent.toString());
				bw.flush();
				bw.close();
				emailStatus = sendSerEmail(newSerFile, strFileContent.toString(), "New USI SER data. ");

			}
		}
		catch(Exception e)
		{
			logger.error("Unable to email new SER events ",e);
			emailStatus = false;
		}
		finally{
			try {
				if (bw != null)
				{
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return emailStatus;
	}

	private List<Integer> getListOfSerIds(List<M9kSerDTO> lstSerPerStation) {
		List<Integer> lstOfProcessedIds = new ArrayList<Integer>(lstSerPerStation.size());
		M9kSerDTO m9kSerDTO;
		for (Iterator<M9kSerDTO> iterator = lstSerPerStation.iterator(); iterator.hasNext();) {
			m9kSerDTO = iterator.next();
			lstOfProcessedIds.add(m9kSerDTO.getId());
			logger.debug("SER in the list "+m9kSerDTO);
		}
		logger.debug("List of ids to be deleted "+lstOfProcessedIds);
		return lstOfProcessedIds;
	}

	private boolean sendSerEmail(File filetoBeEmailed, String fileContent, String emailSubject) 
	{
		boolean emailStatus = false;
		try {
			long sizeInBytes = filetoBeEmailed.length();
			long sizeInMb = sizeInBytes / (1024 * 1024);
			StringBuffer emailText = new StringBuffer("New USI SER data fetched in the last "+(M9kUtils.getSERListenerRunFrequency()/60)+" minutes run. Also find the "+emailReportsSettingsDTO.getSerEmailFileType()+"file attached.");
			emailText.append(M9kConstants.NEWLINE);
			emailText.append(fileContent);
			logger.debug("About to send email with sub "+emailSubject+" body "+emailText);
			if (sizeInMb <= Integer.parseInt(emailReportsSettingsDTO.getSerEmailAttachementSizeLimit()))
			{
				emailSubject+=" File attached: "+filetoBeEmailed.getName();
				SendMailUSI.sendEmailWithAttachment(emailSubject,emailText.toString(),filetoBeEmailed);
			}
			else
			{
				emailText.append(M9kConstants.NEWLINE);
				emailText.append("Fault files are not attached with this email as the file size exceeds the user set limit of "+emailReportsSettingsDTO.getSerEmailAttachementSizeLimit()+" MB. Actual fault file size is "+sizeInMb+" MB."); 
				SendMailUSI.sendEmail(emailSubject, emailText.toString());
			}
			emailStatus = true;
		} catch (Exception e) {
			logger.error("Error in sending an email ",e);
			emailStatus = false;
		}
		
		return emailStatus;
		
	}

	private List<Column> createColumnData() {
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column("Date-Time","Date-Time","string",150));//dataType = "String", "STRING", "java.lang.String", "text"
		columns.add(new Column("Event",    "Event",  "integer",50));//dataType = "Integer", "INTEGER", "java.lang.Integer"
		columns.add(new Column("Current",  "Current", "string", 50));//dataType = "bigdecimal", "BIGDECIMAL", "java.math.BigDecimal"
		columns.add(new Column("Status",  "Status", "string",60));
		columns.add(new Column("Sync",  "Sync", "string",30));//dataType = "dateyeartofraction", "DATEYEARTOFRACTION"
		columns.add(new Column("Description",  "Description", "string",150));//dataType = "DateYear", "dateyear", "DATEYEAR"
		return columns;

		
	}
	public static void main(String args[])
	{
		try
		{
			long startTime;
			long endTime;
			startTime = System.currentTimeMillis();
			M9kSERListener serListener = new M9kSERListener();
			serListener.scheduleSERListener();
			logger.debug("Thread pool size set "+THREAD_SIZE);
			endTime = System.currentTimeMillis();
			logger.debug("Total Time for complete process..."+(endTime-startTime));
		}
		catch (Exception e) {
			logger.error("Exception caught in main ", e);
		}
	}



	
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		logger.debug("In M9kSERListener context initialized..");
		scheduleSERListener();
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		try
		{
			if (serListener != null)
			{
				if (serListener.scheduledTask != null)
				{
					serListener.scheduledTask.cancel(true);
				}
				if (serListener.scheduler != null)
				{
					serListener.scheduler.shutdownNow();
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
