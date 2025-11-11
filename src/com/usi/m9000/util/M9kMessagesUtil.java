package com.usi.m9000.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.BlobMessage;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.CalibrationDTO;

public class M9kMessagesUtil {
	static ActiveMQConnectionFactory connectionFactory = null;
	static InitialContext jndiContext = null;
	static byte[] scopeData;
	static ResourceBundle resourceBundle;
//	static String mq_host;
	static long timeToLive;
	static long filesTransferWaitTime;
	static long mqResponseWaitTime;
	private static long heartBeatCheckFrequency = 300;
	
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kMessagesUtil.class);
	static
	{
		 resourceBundle = ResourceBundle.getBundle("db");
		 try
		 {
			 timeToLive = Long.parseLong(resourceBundle.getString("time-to-live").trim());
			 logger.debug("timeToLive - time to live in the queue "+timeToLive);
//			 System.out.println("timeToLive - time to live in the queue "+timeToLive);
		 }
		 catch (Exception e) {
			 logger.warn("Not able to read time to live from properties file. Using default 300000",e);
			 e.printStackTrace();
			 System.out.println("Not able to read time to live from properties file. Using default 300000");
			 timeToLive = 300000;
		}
		 
		 try
		 {
			 filesTransferWaitTime = Long.parseLong(resourceBundle.getString("files-transfer-wait-time").trim());
			 logger.info("filesTransferWaitTime - time to wait for file transfer "+filesTransferWaitTime);
		 }
		 catch (Exception e) {
			 logger.warn("Not able to read time to filesTransferWaitTime from properties file. Using default 300000",e);
			 filesTransferWaitTime = 300000;
		}
		 try
		 {
			 mqResponseWaitTime = Long.parseLong(resourceBundle.getString("mq-response-wait-time").trim());
			 logger.info("mqResponseWaitTime - time to wait for the response after request "+mqResponseWaitTime);
		 }
		 catch (Exception e) {
			 logger.warn("Not able to read time to mqResponseWaitTime from properties file. Using default 300000",e);
			 mqResponseWaitTime = 300000;
		}
	}


	public static Object sendSynchMessageFromApplet(String queueName, String messageToSend,MessageProperties messageProperties) throws Exception
	{
		Object returnObject = null;
		String strResult = "";
		QueueSession qSession = null;
		QueueConnection qConnection=null;
		QueueSender  sender = null;
		MessageConsumer responseConsumer = null;
		String actionToPerform = "";
		String messageTypeProperty;
		String userName;
		
        try {
//        	jndiContext = new InitialContext();
//        	Context envContext = (Context) jndiContext.lookup("java:comp/env");
            // Create a ConnectionFactory
//        	QueueConnectionFactory qConnectionFactory = (QueueConnectionFactory)new ActiveMQConnectionFactory("tcp://"+M9kUtils.getAppletWebHost()+":61616");//envContext.lookup("jms/ConnectionFactory");
////        	logger.debug("After q conn factory");
//            // Create a Connection
//        	qConnection = qConnectionFactory.createQueueConnection();
//        	qConnection.start();
//            logger.debug("After conn start...");
        	System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");
        	QueueConnectionFactory qConnectionFactory =getQConnectionFactory(); 
//        	qConnection = getQConnection();
        	qConnection = qConnectionFactory.createQueueConnection();
        	qConnection.start();
        	
            // Create a Session
        	qSession = qConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
//            logger.debug("After session create ");
            // Create the destination (Topic or Queue)
            Queue destination = qSession.createQueue(queueName);

         // create a queue sender
//            QueueRequestor requestor = new QueueRequestor(session, destination);
            sender  = qSession.createSender(destination);
            sender.setTimeToLive(timeToLive);
            Queue replyQ = qSession.createTemporaryQueue();
            responseConsumer = qSession.createConsumer(replyQ);
            
            javax.jms.TextMessage message = qSession.createTextMessage();
            message.setText( messageToSend );
//            logger.debug("Message to be sent "+messageToSend);
            message.setJMSReplyTo(replyQ);
            String correlationId = createRandomString();
            message.setJMSCorrelationID(correlationId);
//            // NOTE: here we set a property on messages to be published:
            messageTypeProperty = (String)messageProperties.getProperty("MESSAGE_TYPE");
            message.setStringProperty("MESSAGE_TYPE", messageTypeProperty);
            userName = (String)messageProperties.getProperty("USER_NAME");
            message.setStringProperty("USER_NAME", userName);
//            if (property.equalsIgnoreCase("CALIBRATE"))
//            {
//            	message.setIntProperty("Times", 1);
//            }
            logger.debug("Before sending message to queue "+queueName+ " in machine "+M9kUtils.getAppletWebHost()+" with message "+messageToSend);
            actionToPerform="";
            if (messageTypeProperty.equalsIgnoreCase("CALIBRATE"))
            {
            	actionToPerform = "Calibration";
            	logger.info("User is performing calibration");
            }
            else if (messageTypeProperty.equalsIgnoreCase("APPLY_CAL_FACTORS"))
            {
            	actionToPerform = "Applying new Calibration factors";
            	logger.info("User is applying new calibrated factors");
            }
            else if (messageTypeProperty.equalsIgnoreCase("VERIFY_CAL_FACTORS"))
            {
            	actionToPerform = "Verifying Calibration factors";
            	logger.info("User is performing verification of calibration");
            }
            else if (messageTypeProperty.equalsIgnoreCase("EVENTTEST"))
            {
            	actionToPerform = "Event Test";
            	logger.info("User is performing event test");
            }
            else if (messageTypeProperty.equalsIgnoreCase("FETCH_MEASUREMENT_TYPES"))
            {
            	actionToPerform = "Fetching all available Measurement Types";
            	logger.info("User is fetching all available Measurement Types");
            }
            else if (messageTypeProperty.equalsIgnoreCase("EXTERNAL-CALIBRATE"))
            {
            	actionToPerform = "External Calibration";
            	logger.info("User is performing external calibration using the command "+messageToSend);
            }
            else if (messageTypeProperty.equalsIgnoreCase("SCOPECONFIG"))
            {
            	actionToPerform = "SCOPE Configuration";
            	logger.info("User is requesting to view SCOPE "+messageToSend);
            }
            // START: 13-July-2020 - Hall Effect Implementation
            else if (messageTypeProperty.equalsIgnoreCase("DFR_SPECIFIC_SCOPECONFIG"))
            {
            	int destDfrId = (Integer) messageProperties.getProperty("DFR_ID");
            	logger.debug("Destination dfr id to be sent to station master "+destDfrId);
            	message.setIntProperty("DFR_ID", destDfrId);
            	actionToPerform = "SCOPE Configuration request from dfr id "+destDfrId;
            	logger.info("User is requesting updated config "+messageToSend);
            }
            // END: 13-July-2020
            else if (messageTypeProperty.equalsIgnoreCase("DELETE_FAULTS_FILES"))
            {
            	String dataType = (String)messageProperties.getProperty("DATA_TYPE");
            	message.setStringProperty("DATA_TYPE", dataType);
            	actionToPerform = "Delete selected COMTRADE files";
            	logger.info("User is requesting to delete selected COMTRADE files "+messageToSend+" Data Type "+dataType);
            }
            sender.send(message);
            logger.debug("After sending message ");
            logger.debug("Awaiting response from the server");
            Message response ;
            if (messageTypeProperty.equalsIgnoreCase("CALIBRATE") || messageTypeProperty.equalsIgnoreCase("VERIFY_CAL_FACTORS"))
            {
            	logger.debug("Receiver will wait for 600000 for "+ messageTypeProperty);
            	response = responseConsumer.receive(600000);
            }
            else if (messageTypeProperty.equalsIgnoreCase("FETCH_MEASUREMENT_TYPES"))
            {
            	logger.debug("Receiver will wait for 600000 for FETCH_MEASUREMENT_TYPES");
            	response = responseConsumer.receive(600000);
            }
            else
            {
//            	response = responseConsumer.receive(60000);
            	response = responseConsumer.receive(mqResponseWaitTime);
            }
            logger.debug("Received response"+response + " for message property "+ messageTypeProperty);
//            Message response = requestor.request(message);
            if (response == null)
            {
            	strResult = "Station Master did not respond. Verify IPAddress or Check the status of the station master";
            	logger.error(actionToPerform+" Failed as station master did not respond.");
            	throw new M9000Exception("Error occured: "+strResult);
            }
            else if (response instanceof ObjectMessage)
            {

        		ObjectMessage responseData = (ObjectMessage)response;
        		returnObject = responseData.getObject();
        		logger.debug("Check for calibration instance "+(returnObject instanceof CalibrationDTO));
            	if (returnObject instanceof CalibrationDTO)
            	{
            	logger.debug("Object message"+response);
            		logger.debug("returnObject to be sent "+((CalibrationDTO)returnObject).toString());
            	}
            	else
            	{
                	logger.debug("Object message"+response);
            		logger.debug("returnObject to be sent "+((Map<String,String>)returnObject).toString());
            		
            	}
            }

//            else if (response instanceof TextMessage)
//            {
//            	logger.debug("Text message"+response);
//            		TextMessage responseData = (TextMessage)response;
//            		strResult = responseData.getText();
//            		returnObject = strResult;
//            		logger.debug("returnObject to be sent "+returnObject.toString());
//            }
            else if (response instanceof MapMessage)
            {
            	MapMessage mapMessageConfig = (MapMessage) response;
            	returnObject = M9kUtils.parseScopeConfig(mapMessageConfig);
            }
            else if (response instanceof TextMessage) // External calibration returns text or DFR specific SCOPE config 
            {
            	// START: 13-Jun-2020 - Hall Effect implementation
            	String resultText = ((TextMessage)response).getText();
            	if (messageTypeProperty.equalsIgnoreCase("DFR_SPECIFIC_SCOPECONFIG") && resultText != null && !resultText.isEmpty() && resultText.toUpperCase().indexOf("ERROR") == -1)
            	{
            		int destDfrId = (Integer) messageProperties.getProperty("DFR_ID");
            		returnObject = M9kUtils.parseDfrSpecificScopeConfig(destDfrId,resultText);
            	}
            	else
            	{
                	returnObject = resultText;            		
            	}
            	// END: 13-Jun-2020 
            }

            

        }
        catch (Exception e) {
        	logger.error("Error occurred in sending message for "+actionToPerform, e);
        	throw e;
        }
        finally
        {
    		try {
	        	if (sender != null)
	        	{
	        		sender.close();
	        		sender = null;
	        	}
	        	if (responseConsumer != null)
	        	{
	        		responseConsumer.close();
	        		responseConsumer = null;
	        	}
	        	if (qSession != null)
	        	{
	        		qSession.close();
	        		qSession = null;
	        	}
	        	if (qConnection != null)
	        	{
	        		qConnection.close();
	        		qConnection = null;
	        	}
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        if (messageTypeProperty.equalsIgnoreCase("CALIBRATE") && returnObject instanceof CalibrationDTO)
        {
        	if (((CalibrationDTO)returnObject).getCalStatus().toUpperCase().equalsIgnoreCase("PASS") )
        	{
        		logger.info("Calibration successful"+((CalibrationDTO)returnObject).getCalReport().toString());
        	}
        	else
        	{
        		logger.error("Calibration Failed"+((CalibrationDTO)returnObject).getCalStatusMsg()+M9kConstants.NEWLINE+((CalibrationDTO)returnObject).getCalReport().toString());
        	}
        }
        else if (messageTypeProperty.equalsIgnoreCase("APPLY_CAL_FACTORS") && returnObject instanceof String)
        {
        	if (((String)returnObject).toUpperCase().indexOf("ERROR") == -1)
        	{
        		logger.info("Successfully Applied the calibrated factors");
        	}
        	else
        	{
        		logger.error("Failed to appliy the calibrated factors");
        	}
        }
        else if (messageTypeProperty.equalsIgnoreCase("VERIFY_CAL_FACTORS") && returnObject instanceof CalibrationDTO)
        {
        	if (((CalibrationDTO)returnObject).getCalStatus().toUpperCase().equalsIgnoreCase("PASS") )
        	{
        		logger.info("Calibration verification successful"+((CalibrationDTO)returnObject).getCalReport().toString());
        	}
        	else
        	{
        		logger.error("Calibration verification Failed"+((CalibrationDTO)returnObject).getCalReport().toString());
        	}
        }
        else if (messageTypeProperty.equalsIgnoreCase("EVENTTEST") && returnObject instanceof CalibrationDTO)
        {
        	if (((CalibrationDTO)returnObject).getCalStatus().toUpperCase().equalsIgnoreCase("PASS") )
        	{
        		logger.info("Event Test Successful"+((CalibrationDTO)returnObject).getCalReport().toString());
        	}
        	else
        	{
        		logger.error("Event Test Failed"+((CalibrationDTO)returnObject).getCalStatusMsg()+M9kConstants.NEWLINE+((CalibrationDTO)returnObject).getCalReport().toString());
        	}

        }
        else if (messageTypeProperty.equalsIgnoreCase("EXTERNAL-CALIBRATE") && returnObject instanceof String)
        {
        	if (((String)returnObject).toUpperCase().contains("FAIL"))
        	{
        		logger.error("External Calibration failed. "+((String)returnObject));
        	}
        	else
        	{
        		logger.info("External calibration successful");
        	}
        }
        else if (messageTypeProperty.equalsIgnoreCase("SCOPECONFIG"))
        {
        	if (returnObject != null && returnObject instanceof List<?>)
        	{
        		logger.info("User is successfully viewing SCOPE");
        	}
        	else
        	{
        		logger.error("Error in viewing SCOPE. Unable to get the SCOPE configuration. Please Check whether atleast one of the DFRs is connected to the station master");
        	}
        }
        
        else if (messageTypeProperty.equalsIgnoreCase("DELETE_FAULTS_FILES") && returnObject instanceof String)
        {
        	if (((String)returnObject).toUpperCase().startsWith("ERROR"))
        	{
        		logger.error("Deleting COMTRADE files failed. "+((String)returnObject));
        	}
        	else
        	{
        		logger.info("Deleting COMTRADE files successful");
        	}
        }

        return returnObject;
	}

	
	public static byte[] sendAndReceiveScopeMessage(String queueName, String messageToSend) throws M9000Exception
	{
		String errorResponse = "";
		QueueSession session = null;
		QueueConnection qConnection=null;
		MessageConsumer responseConsumer = null;
        try {
//        	jndiContext = new InitialContext();
//        	Context envContext = (Context) jndiContext.lookup("java:comp/env");
            // Create a ConnectionFactory
//        	QueueConnectionFactory qConnectionFactory = (QueueConnectionFactory)new ActiveMQConnectionFactory("tcp://"+M9kUtils.getAppletWebHost()+":61616");//envContext.lookup("jms/ConnectionFactory");
////        	logger.debug("After q conn factory");
//            // Create a Connection
//        	qConnection = qConnectionFactory.createQueueConnection();
//        	qConnection.start();
//            logger.debug("After conn start...");
        	
        	QueueConnectionFactory qConnectionFactory =getQConnectionFactory(); 
//        	qConnection = getQConnection();
        	qConnection = qConnectionFactory.createQueueConnection();
        	qConnection.start();
        	
            // Create a Session
            session = qConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
//            logger.debug("After session create ");
            // Create the destination (Topic or Queue)
            Queue destination = session.createQueue(queueName);

         // create a queue sender
//            QueueRequestor requestor = new QueueRequestor(session, destination);
            QueueSender  sender  = session.createSender(destination);
            sender.setTimeToLive(timeToLive);
            sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            Queue replyQ = session.createTemporaryQueue();
            responseConsumer = session.createConsumer(replyQ);
            
//            // Create a MessageProducer from the Session to the Topic or Queue
//            MessageProducer producer = session.createProducer(destination);
//            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

//            Destination tempDest = session.createTemporaryQueue();
//            MessageConsumer responseConsumer = session.createConsumer(tempDest);
//            responseConsumer.setMessageListener(new MessageListener() {
//				
//				@Override
//				public void onMessage(Message message) {
//					try {
//						if (message instanceof BytesMessage) {
//			                BytesMessage responseData = (BytesMessage) message;
//			                scopeData = new byte[(int)responseData.getBodyLength()];
//			                responseData.readBytes(scopeData);
////			                setResponse(response);
////			                logger.debug("messageText = " + response);
//			            }					
//						} catch (JMSException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//				}
//			});
            javax.jms.TextMessage message = session.createTextMessage();
            message.setText( messageToSend );
//            logger.debug("Message to be sent "+messageToSend);
            message.setJMSReplyTo(replyQ);
            String correlationId = createRandomString();
            message.setJMSCorrelationID(correlationId);
//            // NOTE: here we set a property on messages to be published:
            message.setStringProperty("MESSAGE_TYPE", "SCOPE");
            logger.debug("Before sending message ");
            sender.send(message);
            logger.debug("After sending message ");
            logger.debug("Awaiting response from the server");
//            Message response = responseConsumer.receive(10000);
            Message response = responseConsumer.receive(mqResponseWaitTime);
            logger.debug("Received response"+response);
//            Message response = requestor.request(message);
            if (response == null || response instanceof TextMessage)
            {
            	logger.debug("message "+response);
            	if (response != null)
            	{
	            	TextMessage responseData = (TextMessage)response;
	            	errorResponse = responseData.getText();
            	}
            	else
            	{
            		errorResponse = "Station Master may be down. It did not respond.";
            	}
            	throw new M9000Exception("Error in data retrieval: "+errorResponse);
            }
            else if (response instanceof BytesMessage)
            {
            	logger.debug("Bytes message");
                BytesMessage responseData = (BytesMessage)response;
                scopeData = new byte[(int)responseData.getBodyLength()];
                responseData.readBytes(scopeData);            	
            }
//            logger.debug("After read bytes");
        }
        catch (Exception e) {
        	logger.error("Error occurred in sending message ", e);
            throw new M9000Exception(e);
        }
        finally
        {
        	try
        	{
	        	if (session != null)
	        	{
	        		session.close();
	        	}
	        	if (qConnection != null)
	        	{
	        		qConnection.close();
	        	}
	        	if (responseConsumer != null)
	        	{
	        		responseConsumer.close();
	        		responseConsumer = null;
	        	}
        	}catch (Exception e) {
				// TODO: handle exception
        		e.printStackTrace();
			}
        }
//		logger.debug("Length of byte array returned "+scopeData.length);
        return scopeData;
	}

	public static String sendSynchMessage(String queueName, String messageToSend, String messageType)
	{
		QueueSession qSession = null;
		QueueConnection connection=null;
		QueueSender qSender = null;
		QueueReceiver qReceiver = null;
//		QueueRequestor qRequestor = null;
		String response = "Error";
        try {
//        	jndiContext = new InitialContext();
//        	Context envContext = (Context) jndiContext.lookup("java:comp/env");
            // Create a ConnectionFactory
//        	connectionFactory = (ConnectionFactory)envContext.lookup("jms/ConnectionFactory");
        	
//        	QueueConnectionFactory qConnectionFactory = (QueueConnectionFactory)new ActiveMQConnectionFactory("tcp://localhost:61616");
        	
        	QueueConnectionFactory qConnectionFactory =getQConnectionFactory(); 
            // Create a Connection
        	connection = qConnectionFactory.createQueueConnection();
            connection.start();

            // Create a Session
            qSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
            Queue destination = qSession.createQueue(queueName);
            Queue responseQ = qSession.createTemporaryQueue();

//            // create a queue sender
//            QueueSender  sender  = session.createSender(destination);

            // Create a MessageProducer from the Session to the Topic or Queue
//            MessageProducer producer = qSession.createProducer(destination);
//            qRequestor = new QueueRequestor(qSession, destination);
            qSender = qSession.createSender(destination);
//            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

//            Destination tempDest = qSession.createTemporaryQueue();
//            MessageConsumer responseConsumer = qSession.createConsumer(tempDest);
//            responseConsumer.setMessageListener(new MessageListener() {
//				
//				@Override
//				public void onMessage(Message message) {
//					String response = null;
//					logger.debug("MEssage received "+message);
//					try {
//						if (message instanceof TextMessage) {
//			                TextMessage textMessage = (TextMessage) message;
//			                response = textMessage.getText();
//			                setResponse(response);
//			                logger.debug("messageText = " + response);
//			            }					
//						} catch (JMSException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//				}
//			});
            javax.jms.TextMessage message = qSession.createTextMessage();
            message.setText( messageToSend );
            message.setJMSReplyTo(responseQ);
            String correlationId = createRandomString();
            message.setJMSCorrelationID(correlationId);
            logger.debug("Message ID "+correlationId);
            // NOTE: here we set a property on messages to be published:
            message.setStringProperty("MESSAGE_TYPE", messageType);
            message.setStringProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
            qSender.setTimeToLive(timeToLive);
            qSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            qSender.send(message);
//            Message result = qRequestor.request(message);
            
            //TODO: The following line gives javax.jms.InvalidSelectorException because of correlationId. 
//            qReceiver = qSession.createReceiver(responseQ, correlationId);
            qReceiver = qSession.createReceiver(responseQ);
//            Message result = qReceiver.receive(30000);
            Message result = null;
//            if (messageType.equalsIgnoreCase("HEARTBEAT"))
//            {
//            	result = qReceiver.receive(15*1000); // converting to milliseconds
//            }
//            else
//            {
            	result = qReceiver.receive(mqResponseWaitTime);
//            }
//            logger.info("mqResponseWaitTime "+mqResponseWaitTime);
            if (result != null && result instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) result;
                response = textMessage.getText();
                logger.debug("messageText = " + response);
            }
            else
            {
            	response = "Error. No Response received.";
            }
            qReceiver.close();

        }
        catch (Exception e) {
        	logger.error("Error occurred in sending message ", e);
        	response = "ERROR";
        }
        finally
        {
    		try {
	        	if (qReceiver != null)
	        	{
	        		qReceiver.close();
	        	}
	        	if (qSender != null)
	        	{
	        		qSender.close();
	        		qSender = null;
	        	}
	        	if (qSession != null)
	        	{
	        		qSession.close();
	        		qSession = null;
	        	}
	        	if (connection != null)
	        	{
	        		connection.close();
	        		connection = null;
	        	}
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		logger.debug("Message to be returned "+response);
        return response;
	}

	// Message Property is whatever properties needs to be set in messages sent using MessageProperties class
	public static Object getRequiredFile(String queueName, String fileName, MessageProperties messageProperties)
	{
		QueueSession qSession = null;
		QueueConnection connection=null;
		QueueSender qSender = null;
		QueueReceiver qReceiver = null;
		MessageConsumer responseConsumer = null;
		Object resultObject;
        try {
        	connection = getQConnection();
            // Create a Session
            qSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
            logger.debug("About to send and receive in queue name "+queueName);
            Queue destination = qSession.createQueue(queueName);
            Queue responseQ = qSession.createTemporaryQueue();

            qSender = qSession.createSender(destination);
            javax.jms.TextMessage message = qSession.createTextMessage();
            message.setText( fileName );
            message.setJMSReplyTo(responseQ);
            String correlationId = createRandomString();
            message.setJMSCorrelationID(correlationId);
            logger.debug("Message ID "+correlationId+" queue name "+queueName);
            // NOTE: here we set a property on messages to be published:
            String key;
            for (Iterator<String> iterator = messageProperties.getMapMessageProperties().keySet().iterator(); iterator.hasNext();) {
				key = iterator.next();
				message.setStringProperty(key, (String)messageProperties.getProperty(key));	
			}
            
            
            qSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            qSender.setTimeToLive(timeToLive);
            qSender.send(message);
            responseConsumer = qSession.createConsumer(responseQ);
//            Message result = qRequestor.request(message);
            
            //TODO: The following line gives javax.jms.InvalidSelectorException because of correlationId. 
//            qReceiver = qSession.createReceiver(responseQ, correlationId);
//            qReceiver = qSession.createReceiver(responseQ);
            logger.debug("About to wait "+filesTransferWaitTime+" ms for response from station master");
            Message result = responseConsumer.receive(filesTransferWaitTime);
            if(result == null )
            {
            	resultObject="ERROR: NULL object returned from Station Master. May be the file is too huge ";
            }
            else if ( result instanceof BlobMessage) {
            	resultObject = (BlobMessage)result;
            }
            else
            {
            	TextMessage textMessage = (TextMessage) result;
            	resultObject = textMessage.getText();
                logger.debug("messageText = " + resultObject);
            }
//            responseConsumer.close();

        }
        catch (Exception e) {
        	logger.error("Error occurred in sending message ", e);
        	resultObject = "ERROR";
        }
        finally
        {
    		try {
	        	if (qReceiver != null)
	        	{
	        		qReceiver.close();
	        	}
	        	if (responseConsumer != null)
	        	{
	        		responseConsumer.close();
	        		responseConsumer = null;
	        	}
	        	if (qSender != null)
	        	{
	        		qSender.close();
	        		qSender = null;
	        	}
	        	if (qSession != null)
	        	{
	        		qSession.close();
	        		qSession = null;
	        	}
	        	if (connection != null)
	        	{
	        		connection.close();
	        		connection = null;
	        	}
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		logger.debug("Message to be returned "+resultObject);
        return resultObject;
	}

	// To get both contunuous Analog and export data
//	public static Object getContinuousData(String queueName, String messageType, String fromDateTime, String toDateTime, String... dataType)
	public static Object getContinuousData(String queueName, MessageProperties messageProperties)
	{
		QueueSession qSession = null;
		QueueConnection connection=null;
		QueueSender qSender = null;
		QueueReceiver qReceiver = null;
		MessageConsumer responseConsumer = null;
		Object resultObject;
		String messageType;
		String dataType;
        try {
        	connection = getQConnection();
            // Create a Session
            qSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
            logger.debug("About to send and receive in queue name "+queueName);
            Queue destination = qSession.createQueue(queueName);
            Queue responseQ = qSession.createTemporaryQueue();

            qSender = qSession.createSender(destination);
            javax.jms.TextMessage message = qSession.createTextMessage();
            messageType = (String)messageProperties.getProperty("MESSAGE_TYPE");
            message.setText( messageType );
            message.setJMSReplyTo(responseQ);
            String correlationId = createRandomString();
            message.setJMSCorrelationID(correlationId);
            logger.debug("Message ID "+correlationId+" queue name "+queueName);
            // NOTE: here we set a property on messages to be published:
            String key;
            for (Iterator<String> iterator = messageProperties.getMapMessageProperties().keySet().iterator(); iterator.hasNext();) {
				key = iterator.next();
				message.setStringProperty(key, (String)messageProperties.getProperty(key));	
			}

//            message.setStringProperty("MESSAGE_TYPE", messageType);
//            message.setStringProperty("FROM_DATETIME", (String)messageProperties.getProperty("FROM_DATETIME"));
//            message.setStringProperty("TO_DATETIME", (String)messageProperties.getProperty("TO_DATETIME"));
            dataType = (String)messageProperties.getProperty("DATA_TYPE");
            message.setStringProperty("DATA_TYPE", dataType);
            if (dataType.equalsIgnoreCase(M9kConstants.CONTINUOUS_DATA))
            {
            	logger.debug("DATA_EXPORT_TYPE property set to "+(String)messageProperties.getProperty("DATA_TYPE"));
            	logger.info("User "+M9kUtils.getUsersDto().getUserName()+" has requested for continuous data for export type "+(String)messageProperties.getProperty("DATA_EXPORT_TYPE"));
            }
            else
            {
            	logger.info("User "+M9kUtils.getUsersDto().getUserName()+" is requested for continuous Analog data");
            }
            qSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            qSender.setTimeToLive(timeToLive);
            qSender.send(message);
            
            responseConsumer = qSession.createConsumer(responseQ);
//            Message result = qRequestor.request(message);
            
            //TODO: The following line gives javax.jms.InvalidSelectorException because of correlationId. 
//            qReceiver = qSession.createReceiver(responseQ, correlationId);
//            qReceiver = qSession.createReceiver(responseQ);
            logger.debug("About to wait "+filesTransferWaitTime+" ms for response from station master");
            Message result = responseConsumer.receive(filesTransferWaitTime);
            if(result == null )
            {
            	resultObject="ERROR: No response from Station Master. May be the Station Master is down ";
            	logger.error("Error in fetching continuous data. May be Station Master is down ");
            }
            else
            {
            	TextMessage textMessage = (TextMessage) result;
            	resultObject = textMessage.getText();
            	logger.debug("messageText = " + resultObject);
            	logger.info("User "+M9kUtils.getUsersDto().getUserName()+" is successfully fetched continuous data ");
            }
//            responseConsumer.close();

        }
        catch (Exception e) {
        	logger.error("Error occurred in sending message ", e);
        	resultObject = "ERROR";
        }
        finally
        {
    		try {
	        	if (qReceiver != null)
	        	{
	        		qReceiver.close();
	        	}
	        	if (responseConsumer != null)
	        	{
	        		responseConsumer.close();
	        		responseConsumer = null;
	        	}
	        	if (qSender != null)
	        	{
	        		qSender.close();
	        		qSender = null;
	        	}
	        	if (qSession != null)
	        	{
	        		qSession.close();
	        		qSession = null;
	        	}
	        	if (connection != null)
	        	{
	        		connection.close();
	        		connection = null;
	        	}
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        logger.debug("Message to be returned "+resultObject);
        return resultObject;
	}

    private static String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }
    
    private static QueueConnection getQConnection() throws JMSException
    {
//    	System.out.println("applet base "+M9kUtils.getAppletWebHost());
    	QueueConnection connection=null;
    	QueueConnectionFactory qConnectionFactory = (QueueConnectionFactory)new ActiveMQConnectionFactory("tcp://"+M9kUtils.getAppletWebHost()+":61616");

        // Create a Connection
    	connection = qConnectionFactory.createQueueConnection();
    	((ActiveMQConnection)connection).setCopyMessageOnSend(false);
        ((ActiveMQConnection)connection).setUseCompression(true);
        connection.start();
        return connection;

    }
   
    private static QueueConnectionFactory getQConnectionFactory()
    {
    	logger.debug("Entered getQConnectionFactory() "); 
    	if (connectionFactory != null)
    	{
    		return connectionFactory;
    	}
        InitialContext initCtx;
		try {
			initCtx = new InitialContext();
			logger.debug("After Initial context");
	        Context envContext = (Context) initCtx.lookup("java:comp/env");
	        logger.debug("After look up java:comp/env");
	        connectionFactory = (ActiveMQConnectionFactory) envContext.lookup("jms/ConnectionFactory");
	        // To allow packages by activemq by serializing. Without this event test or calibration from SCOPE will fail with classnotfoundexception
	        connectionFactory.setTrustAllPackages(true);
        	System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");
	        logger.debug("After look up of connection factory");
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.warn("Connection factory look up failed. Using default localhost connection",e);
			connectionFactory = (ActiveMQConnectionFactory)new ActiveMQConnectionFactory("tcp://localhost:61616");
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("No Active mq connection ",e);
		}

		logger.debug("Returning from getQConnectionFactory() "+connectionFactory);
        return connectionFactory;
    }


    // Test code  to implement in future - 30-Jan-2023
    public static void isStationListening(String queueName)
    {
    	JMXConnector connector = null;
    	try
    	{
		    String brokerUrl = "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";
		    JMXServiceURL serviceUrl = new JMXServiceURL(brokerUrl);
		    connector = JMXConnectorFactory.connect(serviceUrl);
		    MBeanServerConnection connection = connector.getMBeanServerConnection();
		
		    ObjectName name = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=" + queueName);
		    Integer consumerCount = (Integer) connection.getAttribute(name, "ConsumerCount");
		
		    if (consumerCount > 0) {
		      System.out.println("There is/are " + consumerCount + " consumer(s) listening on the queue " + queueName);
		    } else {
		      System.out.println("There are no consumers listening on the queue " + queueName);
		    }
		    connector.close();
    	}
    	catch (Exception e)
    	{
    		
    	}
    	finally
    	{
    		if (connector != null)
    		{
    			try {
					connector.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    }

	public static long getMqResponseWaitTime() {
		return mqResponseWaitTime;
	}


	public static void setMqResponseWaitTime(long mqResponseWaitTime) {
		M9kMessagesUtil.mqResponseWaitTime = mqResponseWaitTime;
	}


	/**
	 * @return the heartBeatCheckFrequency
	 */
	public static long getHeartBeatCheckFrequency() {
		return heartBeatCheckFrequency;
	}


	/**
	 * @param heartBeatCheckFrequency the heartBeatCheckFrequency to set
	 */
	public static void setHeartBeatCheckFrequency(long heartBeatCheckFrequency) {
		M9kMessagesUtil.heartBeatCheckFrequency = heartBeatCheckFrequency;
	}
	

}
