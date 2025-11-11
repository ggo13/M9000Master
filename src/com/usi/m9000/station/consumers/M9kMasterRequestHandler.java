package com.usi.m9000.station.consumers;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.CalibrationDTO;
import com.usi.m9000.station.action.M9kStationRequestProcessor;
import com.usi.m9000.station.commands.M9kFileRequestProcessor;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.util.M9kUtils;

/**
 * 
 */
public class M9kMasterRequestHandler extends Thread implements MessageListener, ExceptionListener {
	private static M9kMasterRequestHandler m9kMasterRequestHandler = null;
	static String stationConfigXml;
	ActiveMQConnectionFactory connectionFactory = null;
	Destination destination = null;
	private MessageProducer replyProducer;

	private Session session;
	QueueSession qSession = null;
	QueueConnection qConnection=null;
	String queueName;
	public static String masterHost;
	static HierarchicalINIConfiguration iniConf;
	Connection connection  = null;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kMasterRequestHandler.class);
	M9kStationRequestProcessor m9kStationRequestProcessor;
//	ExecutorService executor = Executors.newFixedThreadPool(1);
//	ExecutorService executor = Executors.newSingleThreadExecutor();
	ExecutorService executor;
	ExecutorService executorFileProcessor = Executors.newFixedThreadPool(1);
	private String userName;
	
	public static void main(String[] args) throws Exception {
    	M9kMasterRequestHandler consumer = new M9kMasterRequestHandler();
    	consumer.launch();
//    	consumer.setUp();
//    	consumer.createConsumerAndReceiveAMessage();

    }

    
    private  M9kMasterRequestHandler() throws M9000Exception {
		try {
			iniConf = new HierarchicalINIConfiguration("station.properties");
			masterHost = iniConf.getString("station.master_host", "192.168.2.200");
			if (M9kStationDBUtil.getStationDetails() != null)
			{
				queueName = ""+M9kStationDBUtil.getStationDetails().getSystemStationId();
				// START: 20-Mar-2021 - Delta transformer implementation 
				// Station details needs to be initialized with M9kUtil otherwise it crashes	
//				logger.info("About to set station details for M9kUtils "+M9kStationDBUtil.getStationDetails());
				M9kUtils.setStationDetails(M9kStationDBUtil.getStationDetails());
				M9kUtils.setStationId(M9kStationDBUtil.getStationDetails().getSystemStationId());
			}
			else
			{
				java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
				queueName = localMachine.getHostName();
			}
			m9kStationRequestProcessor = M9kStationRequestProcessor.getInstance();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			logger.error("Error occured during initialization "+e);
			throw new M9000Exception(e);
		}
//    	mySqlStationDAO = new MySqlStationDAO();
//    	getStationConfigXML();
		catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			logger.error("Error occured fetching host name. No entry in station_details table in mysql and unable to get localhostname. "+e);
			throw new M9000Exception(e);
		}
	}

    public static M9kMasterRequestHandler getInstance() throws M9000Exception
	{
    	logger.debug("getting instance of M9kMasterRequestHandler "+m9kMasterRequestHandler);
		if (m9kMasterRequestHandler == null)
		{
			logger.debug("Creating a M9kMasterRequestHandler instance to listen to hostname...");
			m9kMasterRequestHandler = new M9kMasterRequestHandler();

		}
		
		return m9kMasterRequestHandler;
	}
//	private void getStationConfigXML()
//    {
//    	//TODO: redefine station tables in the substation master
//    	stationConfigXml = mySqlStationDAO.getLocalConfigXml();
//    }
    public void run()
    {
    	try {
			launch();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("JMS broker may be down. ", e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("JMS broker may be down. ", e);
		}
    }
    public void launch() throws JMSException, InterruptedException
    {
    	setUp();
    	createConsumerAndReceiveAMessage();
    }
 private void setUp() throws JMSException {
          connectionFactory = new ActiveMQConnectionFactory(
                  "failover:(tcp://"+masterHost+":61616)");
//          connectionFactory.setTrustAllPackages(true);
     	 logger.debug("About to listen to queuename "+queueName+" in host "+masterHost);
          
      }

private void createConsumerAndReceiveAMessage() throws JMSException, InterruptedException {
		connectToServer();
          session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
          destination = session.createQueue(queueName);
          MessageConsumer consumer = session.createConsumer(destination);
          connection.setExceptionListener(this);
          consumer.setMessageListener(this);
          this.replyProducer = this.session.createProducer(null);
          this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
          
          
//    	QueueConnectionFactory qConnectionFactory = (QueueConnectionFactory)new ActiveMQConnectionFactory("nio://0.0.0.0:61616");//envContext.lookup("jms/ConnectionFactory");
//    	System.out.println("After q conn factory");
//    	Queue queue = session.createQueue("qStationMaster");
//        // Create a Connection
//        qConnection = qConnectionFactory.createQueueConnection();
//        System.out.println("After conn start...");
//        // Create a Session
//        qSession = qConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
//        System.out.println("After session create ");
//        // Create the destination (Topic or Queue)
//        // create a queue receiver
//        QueueReceiver queueReceiver = qSession.createReceiver(queue);
//        
//        // set an asynchronous message listener
//        queueReceiver.setMessageListener(this);
        
        // set an asynchronous exception listener on the connection
//        qConnection.setExceptionListener(this);
//        
//        // start the connection
//        qConnection.start();


      }

        public void onMessage(Message message) {
        	logger.debug("Message Received "+message);
//        	System.out.println("Message Received "+message);
        	TextMessage response = null;
        	TextMessage errorResponse = null;
        	javax.jms.BytesMessage scopeData = null;
        	String requestMessageType;
        	try {
				response = session.createTextMessage();
				errorResponse = session.createTextMessage();
				scopeData = session.createBytesMessage();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("Exception occured duting initialization ",e);
			}
        	if (message instanceof TextMessage) {
                  TextMessage textMessage = (TextMessage) message;
                  try {
                      userName = textMessage.getStringProperty("USER_NAME");
                	  requestMessageType = textMessage.getStringProperty("MESSAGE_TYPE");
                	  logger.debug("QMessage type received: "+requestMessageType);
                      logger.debug("Received message: " + textMessage.getText());
//                      M9kStationMasterPublisher.sendMessage("Dfr1", textMessage.getText());
                      logger.debug("User "+userName+"'s request received from Master to process"+requestMessageType);
                      if (requestMessageType.equalsIgnoreCase("CONFIG"))
                      {
                    	  logger.debug("Correlation id set "+message.getJMSCorrelationID());
                    	  logger.info("Processing sending configuration to all chassis request from User "+userName);
                    	  StringBuffer applyConfigResult = m9kStationRequestProcessor.processAndSendConfig(textMessage.getText());
                    	  logger.debug("Result of applying conifg "+applyConfigResult);
                    	  if (applyConfigResult == null)
                    	  {
                    		  logger.info("User "+userName+" has successfully sent configuration to all chassis");
                    		  response.setText("SUCCESSFUL");
                    	  }
                    	  else
                    	  {
                    		  response.setText(applyConfigResult.toString());
                    		  logger.error("User "+userName+" has encountered a ERROR while applying the config. "+applyConfigResult.toString());
                    	  }
	                      response.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      // 28-Dec-2022 - Advanced properties change saved in local station master database
                      else if (requestMessageType.equalsIgnoreCase("CONFIG_UPDATE_STATION_DB"))
                      {
                    	  logger.debug("Correlation id set "+message.getJMSCorrelationID());
                    	  logger.info("Processing Advanced configuration from User "+userName);
                    	  StringBuffer applyConfigResult = m9kStationRequestProcessor.updateConfigInDB(textMessage.getText());
                    	  logger.debug("Result of applying conifg "+applyConfigResult);
                    	  if (applyConfigResult.toString().toUpperCase().startsWith("SUCCESS"))
                    	  {
                    		  logger.info("User "+userName+" has successfully updated Advanced Settings");
                    		  response.setText(applyConfigResult.toString());
                    	  }
                    	  else
                    	  {
                    		  response.setText("ERROR while updating Advanced Settings");
                    		  logger.error("User "+userName+" has encountered a ERROR while updating Advanced Settings. ");
                    	  }
	                      response.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      else if(!M9kStationXMLUtil.booXmlInit)
                      {
                    	  response.setText("ERROR! No Config XML in the database");
	                      response.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      else if (requestMessageType.equalsIgnoreCase("TRIGGER"))
                      {
                    	  logger.info("Processing Test Trigger request from User "+userName);
//                    	  HierarchicalINIConfiguration iniConf = new HierarchicalINIConfiguration("station.properties");
                    	  logger.debug("TRIGGER-NOW: Trigger Message received");
                    	  boolean booResult = m9kStationRequestProcessor.processTriggerNow();
                    	  if (booResult)
                    	  {
                    		  logger.info("User "+userName+"'s Test Trigger successful");
                    		  response.setText("SUCCESSFUL");
                    	  }
                    	  else
                    	  {
                    		  logger.error("There was error while processing test trigger request from user "+userName);
                    		  response.setText("ERROR");
                    	  }
	                      response.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      else if (requestMessageType.equalsIgnoreCase("CALIBRATE"))
                      {
//                    	  CalibrateService calibrateService = new CalibrateService(message);
                    	  logger.info("Processing Calibration request from User "+userName);
                    	  logger.debug("CALIBRATE: about to invoke thread ");
                    	  try
                    	  {
	                    	  executor = Executors.newSingleThreadExecutor();
	                    	  executor.execute(new CalibrateService(message));
	                    	  executor.shutdown();
                    	  }
                    	  catch (Exception e) {
							logger.error("Calibration process resulted in a exception ",e);
						}
//                    	  HierarchicalINIConfiguration iniConf = new HierarchicalINIConfiguration("station.properties");
//                    	  boolean booStatus = m9kStationRequestProcessor.processCalibration(textMessage.getIntProperty("Times"));
//                    	  if (booStatus)
//                    	  {
//                    		  response.setText("SUCCESSFUL");
//                    	  }
//                    	  else
//                    	  {
//                    		  response.setText("FAILURE");
//                    	  }
//                    	  response.setJMSCorrelationID(message.getJMSCorrelationID());
//	                      this.replyProducer.send(message.getJMSReplyTo(), response);                   	 
                      }
                      else if (requestMessageType.equalsIgnoreCase("SCOPE"))
                      {
//                    	  HierarchicalINIConfiguration iniConf = new HierarchicalINIConfiguration("station.properties");
                    	  byte[] scopeBytes = m9kStationRequestProcessor.getScopeData(textMessage.getText());
                    	  try
                    	  {
                    		  
                    		  if (scopeBytes == null)
                    		  {
                    			  errorResponse.setText("No data received from the station");
                    			  errorResponse.setStringProperty("ERROR", "Error occured");
                    			  errorResponse.setJMSCorrelationID(message.getJMSCorrelationID());
                    			  this.replyProducer.send(message.getJMSReplyTo(),errorResponse); 
                    		  }
                    		  else
                    		  {
                    			  String errorText = new String(scopeBytes);
                    			  if (errorText.toUpperCase().startsWith("ERROR"))
	                    		  {
                    				  logger.error("Error in SCOPE data retrieval request from User "+userName);
	                    			  logger.error(textMessage.getText());
	                    			  errorResponse.setText(errorText);
	                    			  errorResponse.setStringProperty("ERROR", "Error in channels request from Station Master");
	                    			  errorResponse.setJMSCorrelationID(message.getJMSCorrelationID());
	                    			  this.replyProducer.send(message.getJMSReplyTo(),errorResponse); 
	                    			  
	                    		  }
	                    		  else
	                    		  {
			                    	  scopeData.writeBytes(scopeBytes);
			                    	  scopeData.setJMSCorrelationID(message.getJMSCorrelationID());
				                      this.replyProducer.send(message.getJMSReplyTo(), scopeData);
	                    		  }
                    		  }
//		                      Queue reply = (Queue) message.getJMSReplyTo();
//		                      QueueSender sender = qSession.createSender(reply);
//		                      sender.send(scopeData);
                    	  }
                    	  catch (Exception e) {
							logger.error("Error occured in message handling.",e);
						}
                      }
                      else if (requestMessageType.equalsIgnoreCase("SCOPECONFIG"))
                      {
                    	  logger.info("Processing SCOPE request from User "+userName);
                    	  logger.debug("Processing SCOPE request from User "+userName);
                    	  MapMessage mapScopeMessage = session.createMapMessage();
                    	  String dfrNameKey;
                    	  Map<String,String> mapScopeConfig = m9kStationRequestProcessor.getScopeConfig();
                    	  logger.debug("Returns from scope request processor with map configs "+mapScopeConfig);
                    	  if (mapScopeConfig == null || mapScopeConfig.isEmpty())
                    	  {
                    		  logger.error("No SCOPE config data returned to User "+userName);
                    		  errorResponse.setText("No Config data received from the station");
                			  errorResponse.setStringProperty("ERROR", "Error occured. DFRs are not accessible");
                			  errorResponse.setJMSCorrelationID(message.getJMSCorrelationID());
                			  this.replyProducer.send(message.getJMSReplyTo(),errorResponse); 
                    	  }
                    	  else
                    	  {
                    		  logger.info("SCOPE config successfully sent back to User "+userName);
                    		  logger.debug("SCOPE config successfully sent back to User "+userName);
	                    	  for (Iterator<String> iterator = mapScopeConfig.keySet().iterator(); iterator
									.hasNext();) {
	                    		  dfrNameKey = iterator.next();
								mapScopeMessage.setString(dfrNameKey, mapScopeConfig.get(dfrNameKey));
	                    	  }
	                    	  logger.debug("About to return with map message "+mapScopeMessage);
	                    	  mapScopeMessage.setJMSCorrelationID(message.getJMSCorrelationID());
	                    	  this.replyProducer.send(message.getJMSReplyTo(), mapScopeMessage);
                    	  }
                    	  
                      }     
                      // START: 10-Jul-2020 - Hall Effect Sensor implementation - request config every time
                      else if (requestMessageType.equalsIgnoreCase("DFR_SPECIFIC_SCOPECONFIG"))
                      {
                    	  logger.info("Processing SCOPE request from User "+userName);
                    	  logger.debug("Processing SCOPE request from User "+userName);
                    	  int requestedDfrId = textMessage.getIntProperty("DFR_ID");
                    	  String scopeConfig = m9kStationRequestProcessor.getDFRSpecificScopeConfig(requestedDfrId);
                    	  logger.debug("Returns from scope request processor with the config "+scopeConfig);
                    	  if (scopeConfig == null || scopeConfig.isEmpty())
                    	  {
                    		  logger.error("No SCOPE config data requested for DFR"+requestedDfrId+" returned to User "+userName);
                    		  errorResponse.setText("ERROR: No Config data received from the station for DFR"+requestedDfrId);
                			  errorResponse.setStringProperty("ERROR", "Error occured. DFR"+requestedDfrId+" is not accessible");
                			  errorResponse.setJMSCorrelationID(message.getJMSCorrelationID());
                			  this.replyProducer.send(message.getJMSReplyTo(),errorResponse); 
                    	  }
                    	  else
                    	  {
                    		  logger.info("SCOPE config successfully sent back to User "+userName);
                    		  logger.debug("SCOPE config successfully sent back to User "+userName);
                    		  response.setText(scopeConfig);
                    		  response.setJMSCorrelationID(message.getJMSCorrelationID());
	                    	  this.replyProducer.send(message.getJMSReplyTo(), response);
                    	  }
                    	  
                      }
                      // END: 10-Jul-2020
                      else if (requestMessageType.equalsIgnoreCase("MONITOR"))
                      {
                    	  logger.debug("Correlation id set "+message.getJMSCorrelationID());
                    	  String healthStatus = m9kStationRequestProcessor.processHealthRequest();
//                    	  String healthStatus = "<SubStation id=\"1\" name=\"kettle-creek\" status=\"Active\" xmlns=\"http://www.usi.com/health\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <DFR name=\"Dfr1 --- "+ iCnt++ +"\" status=\"Active\"> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> </DFR> <DFR name=\"Dfr2\" status=\"Active\"> <Attribute> <Name>Temperature</Name> <Value>T19.509</Value> <State>Critical</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> </DFR> <DFR name=\"Dfr3\" status=\"Inactive\"> <Status-Info> DFR down. Connection refused. 		 </Status-Info> </DFR> </SubStation>";
                    	  logger.debug("Returned status "+healthStatus);
                   		  response.setText(healthStatus);
	                      response.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      // To distinguish between Moxa and Arbor
                      else if (requestMessageType.equalsIgnoreCase("STATION_MASTER_COMPUTER_TYPE"))
                      {
                    	  String computerType = m9kStationRequestProcessor.getStationMasterComputerType();
//                    	  String healthStatus = "<SubStation id=\"1\" name=\"kettle-creek\" status=\"Active\" xmlns=\"http://www.usi.com/health\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <DFR name=\"Dfr1 --- "+ iCnt++ +"\" status=\"Active\"> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> </DFR> <DFR name=\"Dfr2\" status=\"Active\"> <Attribute> <Name>Temperature</Name> <Value>T19.509</Value> <State>Critical</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> </DFR> <DFR name=\"Dfr3\" status=\"Inactive\"> <Status-Info> DFR down. Connection refused. 		 </Status-Info> </DFR> </SubStation>";
                    	  logger.debug("Returned computer Name "+computerType);
                   		  response.setText(computerType);
	                      response.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      else if (requestMessageType.equalsIgnoreCase("EXTERNAL-CALIBRATE"))
                      {
                    	  logger.info("Processing External calibration request from User "+userName);
                    	  logger.debug("EXTERNAL-CALIBRATE:Correlation id set "+message.getJMSCorrelationID());
                    	  String extCalstatus = m9kStationRequestProcessor.processExternalCalibration(textMessage.getText());
//                    	  String healthStatus = "<SubStation id=\"1\" name=\"kettle-creek\" status=\"Active\" xmlns=\"http://www.usi.com/health\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <DFR name=\"Dfr1 --- "+ iCnt++ +"\" status=\"Active\"> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> </DFR> <DFR name=\"Dfr2\" status=\"Active\"> <Attribute> <Name>Temperature</Name> <Value>T19.509</Value> <State>Critical</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> </DFR> <DFR name=\"Dfr3\" status=\"Inactive\"> <Status-Info> DFR down. Connection refused. 		 </Status-Info> </DFR> </SubStation>";
                    	  logger.debug("Returned status "+extCalstatus);
                   		  response.setText(extCalstatus);
	                      response.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      else if (requestMessageType.equalsIgnoreCase("APPLY_CAL_FACTORS"))
                      {
                    	  logger.info("Processing New Calibration Factors apply request from User "+userName);
                    	  String calApplyResult = m9kStationRequestProcessor.processAndApplyCalFactors(textMessage.getText());
                    	  logger.debug("Result of applying conifg "+calApplyResult);
                   		  response.setText(calApplyResult);
	                      response.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      else if (requestMessageType.equalsIgnoreCase("VERIFY_CAL_FACTORS"))
                      {
                    	  logger.info("Processing Verification of calibration request from User "+userName);
                    	  try
                    	  {
	                    	  executor = Executors.newSingleThreadExecutor();
	                    	  executor.execute(new CalibrateService(message, true));
	                    	  executor.shutdown();
                    	  }
                    	  catch (Exception e) {
							logger.error("Verify Calibration process resulted in a exception ",e);
						}
//                    	  CalibrationDTO calibrationDTO = m9kStationRequestProcessor.verifyCalibration(textMessage.getText());
//          				logger.debug("VERIFY: After Verify calibration "+calibrationDTO);
////          				if (iTimes == 1)
////          				{
//          				ObjectMessage objMessage = session.createObjectMessage();
//          				objMessage.setObject(calibrationDTO );
//          				objMessage.setJMSCorrelationID(message.getJMSCorrelationID());
//          				logger.debug("VERIFY: After calibration verification , about to send objMessage "+objMessage);
//                        replyProducer.send(message.getJMSReplyTo(), objMessage);
                      }
                      else if (requestMessageType.equalsIgnoreCase("FETCH_LAST_CAL_VERIFY_DATE"))
                      {
                    	  logger.info("Processing last calibration verify date request from User "+userName);
                    	  String lastCalVerifiedDate = m9kStationRequestProcessor.getLastCalVerifiedDate();
//                    	  String healthStatus = "<SubStation id=\"1\" name=\"kettle-creek\" status=\"Active\" xmlns=\"http://www.usi.com/health\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <DFR name=\"Dfr1 --- "+ iCnt++ +"\" status=\"Active\"> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> </DFR> <DFR name=\"Dfr2\" status=\"Active\"> <Attribute> <Name>Temperature</Name> <Value>T19.509</Value> <State>Critical</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> </DFR> <DFR name=\"Dfr3\" status=\"Inactive\"> <Status-Info> DFR down. Connection refused. 		 </Status-Info> </DFR> </SubStation>";
                    	  logger.debug("Returned status for last calibration verify date "+lastCalVerifiedDate);
                   		  response.setText(lastCalVerifiedDate);
	                      response.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      else if (requestMessageType.equalsIgnoreCase("FETCH_LAST_CAL_DATE"))
                      {
                    	  logger.info("Processing last calibration date request from User "+userName);
                    	  String lastCalDate = m9kStationRequestProcessor.getLastCalDate();
//                    	  String healthStatus = "<SubStation id=\"1\" name=\"kettle-creek\" status=\"Active\" xmlns=\"http://www.usi.com/health\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <DFR name=\"Dfr1 --- "+ iCnt++ +"\" status=\"Active\"> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> </DFR> <DFR name=\"Dfr2\" status=\"Active\"> <Attribute> <Name>Temperature</Name> <Value>T19.509</Value> <State>Critical</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> </DFR> <DFR name=\"Dfr3\" status=\"Inactive\"> <Status-Info> DFR down. Connection refused. 		 </Status-Info> </DFR> </SubStation>";
                    	  logger.debug("Returned status for last calibration date "+lastCalDate);
                   		  response.setText(lastCalDate);
	                      response.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      else if (requestMessageType.equalsIgnoreCase("FETCH_LAST_EVENTTEST_DATE"))
                      {
                    	  logger.info("Processing last event test date request from User "+userName);
                    	  String lastEventTestDate = m9kStationRequestProcessor.getLastEventTestDate();
//                    	  String healthStatus = "<SubStation id=\"1\" name=\"kettle-creek\" status=\"Active\" xmlns=\"http://www.usi.com/health\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <DFR name=\"Dfr1 --- "+ iCnt++ +"\" status=\"Active\"> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> </DFR> <DFR name=\"Dfr2\" status=\"Active\"> <Attribute> <Name>Temperature</Name> <Value>T19.509</Value> <State>Critical</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> </DFR> <DFR name=\"Dfr3\" status=\"Inactive\"> <Status-Info> DFR down. Connection refused. 		 </Status-Info> </DFR> </SubStation>";
                    	  logger.debug("Returned status for last event test date "+lastEventTestDate);
                   		  response.setText(lastEventTestDate);
	                      response.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      else if (requestMessageType.equalsIgnoreCase("FILE_RETRIEVE"))
                      {
                    	  logger.info("Processing file "+textMessage.getText() +" retrieval request from User "+userName);
                    	  logger.debug("File name received "+textMessage.getText()+" Invoking file processing thread");
                    	  
                    	  try
                    	  {
                    		  executorFileProcessor.execute(new M9kFileRequestProcessor(session,replyProducer,message));
                    		  logger.debug("Invoked successfully and now acknowledging the message");
                    		  message.acknowledge();
                    		  logger.debug("acknowledged successfully ");
                    	  }
                    	  catch (Exception e) {
							logger.error("File transfer process resulted in a exception ",e);
						}
                      }
                      else if (requestMessageType.equalsIgnoreCase("EVENTTEST"))
                      {
                    	  logger.info("Processing Event Test request from User "+userName);
//                    	  CalibrateService calibrateService = new CalibrateService(message);
                    	  logger.debug("EVENT-TEST: about to invoke thread ");
                    	  try
                    	  {
	                    	  executor = Executors.newSingleThreadExecutor();
	                    	  executor.execute(new EventTestService(message));
	                    	  executor.shutdown();
                    	  }
                    	  catch (Exception e) {
							logger.error("Calibration process resulted in a exception ",e);
						}
//                    	  HierarchicalINIConfiguration iniConf = new HierarchicalINIConfiguration("station.properties");
//                    	  boolean booStatus = m9kStationRequestProcessor.processCalibration(textMessage.getIntProperty("Times"));
//                    	  if (booStatus)
//                    	  {
//                    		  response.setText("SUCCESSFUL");
//                    	  }
//                    	  else
//                    	  {
//                    		  response.setText("FAILURE");
//                    	  }
//                    	  response.setJMSCorrelationID(message.getJMSCorrelationID());
//	                      this.replyProducer.send(message.getJMSReplyTo(), response);                   	 
                      }
                      else if (requestMessageType.equalsIgnoreCase("HEARTBEAT"))
                      {
                    	  logger.debug("Correlation id set "+message.getJMSCorrelationID());
                    	  logger.debug("Returned healthy ");
                   		  response.setText("ALIVE");
	                      response.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      else if (requestMessageType.equalsIgnoreCase("FETCH_MEASUREMENT_TYPES"))
                      {
                    	  logger.info("Processing fetching all available measurements types request from User "+userName);
                    	  Map<String,String>  mapMeasurementTypesWithDates = M9kStationDBUtil.getMeasurementTypes();
//                    	  String healthStatus = "<SubStation id=\"1\" name=\"kettle-creek\" status=\"Active\" xmlns=\"http://www.usi.com/health\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <DFR name=\"Dfr1 --- "+ iCnt++ +"\" status=\"Active\"> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> </DFR> <DFR name=\"Dfr2\" status=\"Active\"> <Attribute> <Name>Temperature</Name> <Value>T19.509</Value> <State>Critical</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> <Attribute> <Name>Fan Speed</Name> <Value>F2295</Value> <State>Normal</State> <Status-Info>Nothing to report</Status-Info>		 </Attribute> </DFR> <DFR name=\"Dfr3\" status=\"Inactive\"> <Status-Info> DFR down. Connection refused. 		 </Status-Info> </DFR> </SubStation>";
                    	  logger.debug("Returned Measurement Types "+mapMeasurementTypesWithDates);
                    	  ObjectMessage objMessage = session.createObjectMessage();
                    	  objMessage.setObject((Serializable) mapMeasurementTypesWithDates);
                    	  objMessage.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), objMessage);
                      }
                      else if (requestMessageType.equalsIgnoreCase("FAULT_LOC"))
                      {
                    	  logger.info("Processing fault location calculation request from User "+userName);
                    	  logger.debug("Received Fault location calculation request for "+textMessage.getText());
                    	  String faultLocDetails = null;
                    	  try
                    	  {
                    		  faultLocDetails = m9kStationRequestProcessor.calculateFaultLocation(textMessage.getText());
                    	  }
                    	  catch(Exception e)
                    	  {
                    		  logger.error("Error occured during fault location calculation ",e);
                    		  faultLocDetails = "ERROR:"+e.getMessage();
                    	  }
                    	  logger.debug("fault Loc details "+faultLocDetails);
                   		  response.setText(faultLocDetails);
	                      response.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      else if (requestMessageType.equalsIgnoreCase("DELETE_FAULTS_FILES"))
                      {
                    	  String faultFileNames = textMessage.getText();
                    	  String dataType = textMessage.getStringProperty("DATA_TYPE");
                    	  logger.info("User "+userName+" requested to delete the fault(s)"+faultFileNames+" with data type "+dataType);
                    	  try
                    	  {
                    		  m9kStationRequestProcessor.deleteFaultFiles(faultFileNames, dataType);
                       		  response.setText("SUCCESS");
                    	  }
                    	  catch(Exception e)
                    	  {
                    		  logger.error("Error occured during fault location calculation ",e);
                       		  response.setText("ERROR: Deleting selected COMTRADE files failed "+e.getMessage());
                    	  }
	                      response.setJMSCorrelationID(message.getJMSCorrelationID());
	                      this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      else if (requestMessageType.equalsIgnoreCase("REINITIALIZE_STATION"))
                      {
                    	  String reinitializeResult = null;
                    	  String commandToReinitialize = textMessage.getText();
                    	  logger.info("User "+userName+" removed station and hence reinitializing station master software");
                    	  try
                    	  {
                    		  reinitializeResult = m9kStationRequestProcessor.reinitializeStation(commandToReinitialize);
                       		  response.setText("reinitializeResult");
                    	  }
                    	  catch(Exception e)
                    	  {
                    		  logger.error("Error occured during reinitialization station ",e);
                    		  if (reinitializeResult == null)
                    		  {
                    			  reinitializeResult = "ERROR: Unable to reinitialize station"+e.getMessage();
                    		  }
                       		  response.setText(reinitializeResult);
                    	  }
                          response.setJMSCorrelationID(message.getJMSCorrelationID());
                          this.replyProducer.send(message.getJMSReplyTo(), response);
                      }
                      else if (requestMessageType.equalsIgnoreCase("STATION_DATA_CLEANUP"))
                      {
                    	  logger.info("Processing station data cleanup request from User "+userName);
                    	  try
                    	  {
	                    	  executor = Executors.newSingleThreadExecutor();
	                    	  executor.execute(new M9kCleanupService(message));
	                    	  executor.shutdown();
                    	  }
                    	  catch (Exception e) {
							logger.error("Station data cleanup resulted in a exception ",e);
						}
                      }
                      else if (requestMessageType.equalsIgnoreCase("RETRIEVE_STATION_CONFIG"))
                      {
                    	  logger.info("Processing station config request from remote master by User "+userName);
                    	  try
                    	  {
            				  String stationConfigText = M9kStationDBUtil.getStationDetails().getStationDetailsAsSQLStmt();
                      	  if (stationConfigText == null || stationConfigText.isEmpty())
                      	  {
                      		  logger.error("Error in retrieving config from station master");
                      		  errorResponse.setText("ERROR: Unable to retrieve station config");
                  			  errorResponse.setJMSCorrelationID(message.getJMSCorrelationID());
                  			  this.replyProducer.send(message.getJMSReplyTo(),errorResponse); 
                      	  }
                      	  else
                      	  {
                      		  logger.info("Station config is sent back to User "+userName);
                      		  logger.debug("Station config is sent back to User "+userName);
                      		  response.setText(stationConfigText);
                      		  response.setJMSCorrelationID(message.getJMSCorrelationID());
    	                    	  this.replyProducer.send(message.getJMSReplyTo(), response);
                      	  }
                    	  }
                    	  catch (Exception e) {
							logger.error("Station data cleanup resulted in a exception ",e);
						}
                      }

                  } catch (JMSException ex) {
                	  logger.error("Error occured in message handling.",ex);
                  } catch (M9000Exception e) {
                	  logger.error("Error occured in message handling.",e);
				} catch (Exception e) {
					logger.error("Error occured in message handling.",e);
				}
              } else  {
            	  logger.debug("Received: " + message);
              }
          }
        public synchronized void onException(JMSException je) {
        	logger.error("JMS Exception occured. ",je);
        	Exception le = je.getLinkedException();
            if (le != null)
            {
            	logger.error("linked exception "+le);
            }
//            try {
//				connection.setExceptionListener(null);
//				connection.close();
//	            connection = null;
//	            sleep(5000);
//			} catch (JMSException e) {
//				logger.error("JMS Exception occured during connection clean up",e);				
//			} catch (InterruptedException e) {
//				logger.error("Unable to sleep during connection clean up",e);
//			} catch (Exception e) {
//				logger.error("An Exception occured during connection clean up",e);
//			}
			
//			logger.debug("About to call connectToServer from onException");
//			logger.info("Exception occured in JMS connection. Attempting to connect to server again. ");
//            connectToServer();
        }

		public static String getStationConfigXml() {
			return stationConfigXml;
		}

		public static void setStationConfigXml(String stationConfigXml) {
			M9kMasterRequestHandler.stationConfigXml = stationConfigXml;
		}
		
		private void connectToServer()
		{
			try
			{
				logger.debug("Connecttion "+connection);
		          connection = connectionFactory.createConnection();
		          connection.setExceptionListener(this);
		          ((ActiveMQConnection)connection).setCopyMessageOnSend(false);
		          ((ActiveMQConnection)connection).setUseCompression(true);
		          logger.debug("About to start the connection. "+connection);
		          connection.start();
		          logger.debug("\t\t\t\t\t\t Successful connection to JMS server");
		          logger.info("Listening to queue in host "+masterHost);
//		          System.out.println("Listening to queue in host "+masterHost);
			}
			catch (Exception jmse) {
				logger.warn("Exception occurred trying to connect to JMS server."+connection,jmse);
//				while (connection == null)
//				{
//					try
//					{
//				          connection = connectionFactory.createConnection();
//				          connection.start();
//				          logger.debug("\t\t\t\t\t\t in catch clause Successful connection to JMS server");
//				          logger.info("Listening to queue in host "+masterHost);
////				          System.out.println("Listening to queue in host "+masterHost);
//					}
//					catch (Exception e) {
//						logger.info("Waiting for the web master JMS server");
//						try {
//							sleep(5000);
//						} catch (InterruptedException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//					}
//					
//				}
			}

		}
		
		class CalibrateService implements Runnable
		{
			Message message;
			TextMessage textMessage;
			ObjectMessage objMessage;
			boolean verify = false;
			public CalibrateService(Message receivedMessage)
			{
				logger.debug("CALIBRATE: Inside CalibrateService constructor "+receivedMessage);
				this.message = receivedMessage;
				this.textMessage = (TextMessage) receivedMessage; 
				logger.debug("CALIBRATE: End of constructor");
			}
			public CalibrateService(Message receivedMessage, boolean verify)
			{
				logger.debug("CALIBRATE: Inside CalibrateService constructor "+receivedMessage);
				this.message = receivedMessage;
				this.textMessage = (TextMessage) receivedMessage;
				this.verify = verify;
				logger.debug("CALIBRATE: End of constructor");
			}
			public void run()
			{
				try
				{
//				TextMessage response = session.createTextMessage();
//					logger.debug("CALIBRATE: About to call processCalibration method in M9kStationRequestProcessor with Times value "+textMessage.getIntProperty("Times"));
					// Test code to calibrate multiple times
//					int iTimes = 1;
//					do{
//				CalibrationDTO calibrationDTO = m9kStationRequestProcessor.processCalibration(textMessage.getIntProperty("Times"));
				CalibrationDTO calibrationDTO = m9kStationRequestProcessor.processCalibration();
				logger.debug("CALIBRATE: After calibration "+calibrationDTO);
				if (calibrationDTO == null)
				{
					logger.error("CALIBRATION Failed ");
				}
				else if (calibrationDTO.getCalStatus().toUpperCase().equalsIgnoreCase("PASS") )
				{
					logger.info("CALIBRATION COMPLETED SUCCESSFULLY."+M9kStationConstants.NEWLINE+M9kStationConstants.NEWLINE+calibrationDTO.getCalReport().toString());
					if (verify)
					{
		              	  calibrationDTO = m9kStationRequestProcessor.verifyCalibration(textMessage.getText());
					}

				}
				else
				{
					logger.error("CALIBRATION Failed "+calibrationDTO.getCalStatusMsg()+M9kStationConstants.NEWLINE+M9kStationConstants.NEWLINE+calibrationDTO.getCalReport().toString());
				}
//				if (iTimes == 1)
//				{
				objMessage = session.createObjectMessage();
				objMessage.setObject(calibrationDTO );
				objMessage.setJMSCorrelationID(message.getJMSCorrelationID());
				logger.debug("CALIBRATE: After calibration, about to send objMessage "+objMessage);
              replyProducer.send(message.getJMSReplyTo(), objMessage);
              	logger.debug("CALIBRATE: reply sent ");
//				}
//				else
//				{
//					logger.debug("CALIBRATE: Completed "+iTimes+" Times.");
//				}
//				iTimes++;
//					}
//					while (iTimes < 500);
			 } catch (JMSException ex) {
           	  logger.error("Error occured in Calibration.",ex);
             } catch (M9000Exception e) {
           	  logger.error("Error occured in Calibration.",e);
			} catch (Exception e) {
				logger.error("Error occured in Calibration.",e);
			}
		}
	}

		class EventTestService implements Runnable
		{
			Message message;
			TextMessage textMessage;
			ObjectMessage objMessage;
			public EventTestService(Message receivedMessage)
			{
				logger.debug("EventTest: Inside EventTest constructor "+receivedMessage);
				this.message = receivedMessage;
				this.textMessage = (TextMessage) receivedMessage; 
				logger.debug("EventTest: End of constructor");
			}
			public void run()
			{
				try
				{
//				TextMessage response = session.createTextMessage();
					logger.debug("EventTest: About to call processEventTest method in M9kStationRequestProcessor with command "+textMessage.getText());
					// Test code to calibrate multiple times
//					int iTimes = 1;
//					do{
				CalibrationDTO calibrationDTO = m9kStationRequestProcessor.processEventTest(textMessage.getText());
				logger.debug("EventTest: After EventTest "+calibrationDTO);
				if (calibrationDTO == null)
				{
					logger.error("Event Test Failed ");
				}
				else if (calibrationDTO.getCalStatus().toUpperCase().equalsIgnoreCase("PASS") )
				{
					logger.info("Event Test COMPLETED SUCCESSFULLY."+M9kStationConstants.NEWLINE+M9kStationConstants.NEWLINE+calibrationDTO.getCalReport().toString());
				}
				else
				{
					logger.error("Event Test Failed "+calibrationDTO.getCalStatusMsg()+M9kStationConstants.NEWLINE+M9kStationConstants.NEWLINE+calibrationDTO.getCalReport().toString());
				}
//				if (iTimes == 1)
//				{
				objMessage = session.createObjectMessage();
				objMessage.setObject(calibrationDTO );
				objMessage.setJMSCorrelationID(message.getJMSCorrelationID());
				logger.debug("EventTest: After EventTest, about to send objMessage "+objMessage);
              replyProducer.send(message.getJMSReplyTo(), objMessage);
              	logger.debug("EventTest: reply sent ");
//				}
//				else
//				{
//					logger.debug("CALIBRATE: Completed "+iTimes+" Times.");
//				}
//				iTimes++;
//					}
//					while (iTimes < 500);
			 } catch (JMSException ex) {
           	  logger.error("Error occured in EventTest.",ex);
             } catch (M9000Exception e) {
           	  logger.error("Error occured in EventTest.",e);
			} catch (Exception e) {
				logger.error("Error occured in EventTest.",e);
			}
		}
	}

		class M9kCleanupService implements Runnable
		{
			Message message;
			TextMessage response;
			boolean verify = false;
			public M9kCleanupService(Message receivedMessage)
			{
				logger.debug("M9kCleanup: Inside M9kCleanupService constructor "+receivedMessage);
				this.message = receivedMessage;
			}
			public void run()
			{
				try
				{
					response = session.createTextMessage();
					M9kStationUtil.m9kCleanup();
					logger.info("Data cleanup completed successfully.");
					
	              	response.setText("SUCCESS");
	                response.setJMSCorrelationID(message.getJMSCorrelationID());
	                replyProducer.send(message.getJMSReplyTo(), response);
				}
          	  catch(Exception e)
          	  {
          		  logger.error("Error occured during fault location calculation ",e);
           		  try {
					response.setText("ERROR: Deleting selected COMTRADE files failed "+e.getMessage());
	                response.setJMSCorrelationID(message.getJMSCorrelationID());
	                replyProducer.send(message.getJMSReplyTo(), response);
				} catch (JMSException e1) {
					logger.error("Unable to respond to m9kcleanup",e1);
				}
          	  }
			}
		}
	}

