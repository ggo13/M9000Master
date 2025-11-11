package com.usi.m9000.station.consumers;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.station.commands.M9kConfigXmlProcessor;
import com.usi.m9000.station.commands.M9kRestoreActiveConfig;

/**
 * 
 */
public class M9kConfigRestoreRequestHandler extends Thread implements MessageListener, ExceptionListener {
	private static M9kConfigRestoreRequestHandler m9kConfigRestoreRequestHandler = null;
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
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kConfigRestoreRequestHandler.class);
//	ExecutorService executor = Executors.newFixedThreadPool(1);
//	ExecutorService executor = Executors.newSingleThreadExecutor();
	ExecutorService executor;
	ExecutorService executorFileProcessor;
	private String userName;
	
	public static void main(String[] args) throws Exception {
    	M9kConfigRestoreRequestHandler consumer = new M9kConfigRestoreRequestHandler();
    	consumer.launch();
//    	consumer.setUp();
//    	consumer.createConsumerAndReceiveAMessage();

    }

	public static M9kConfigRestoreRequestHandler getInstance() throws M9000Exception
	{
		logger.debug("getting instance of M9kConfigRestoreRequestHandler "+m9kConfigRestoreRequestHandler);
		if (m9kConfigRestoreRequestHandler == null)
		{
			logger.debug("Creating a m9kConfigRestoreRequestHandler instance to listen to hostname...");
			m9kConfigRestoreRequestHandler = new M9kConfigRestoreRequestHandler();

		}
		
		return m9kConfigRestoreRequestHandler;
	}
    private M9kConfigRestoreRequestHandler() throws M9000Exception {
		try {
			iniConf = new HierarchicalINIConfiguration("station.properties");
			masterHost = iniConf.getString("station.master_host", "192.168.2.200");
			java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
			queueName = localMachine.getHostName();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			logger.error("Error occured during initialization "+e);
			throw new M9000Exception(e);
		}
		catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			logger.error("Error occured fetching host name. No entry in station_details table in mysql and unable to get localhostname. "+e);
			throw new M9000Exception(e);
		}
	}

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
                "failover:(tcp://"+masterHost+":61616)?jms.blobTransferPolicy.defaultUploadUrl=http://"+masterHost+":8161/fileserver/");
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
      }

        public void onMessage(Message message) {
        	logger.debug("Message Received "+message);
        	String requestMessageType;
        	TextMessage response = null;
        	TextMessage errorResponse = null;
        	try {
				response = session.createTextMessage();
				errorResponse = session.createTextMessage();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("Exception occured duting initialization ",e);
			}
        	if (message instanceof TextMessage) {
        		TextMessage textMessage = (TextMessage) message;
        		try
        		{
        		requestMessageType = textMessage.getStringProperty("MESSAGE_TYPE");
        		// START: 15-June-2021 Retrieve the latest config from the chassis
                if (requestMessageType.equalsIgnoreCase("ACTIVE_CONFIG"))
                {
              	  logger.info("Processing RETRIEVE request from User "+userName);
              	  try
              	  {
                  	  M9kRestoreActiveConfig m9kRestoreActiveConfig = new M9kRestoreActiveConfig();
        				  String activeConfigText = m9kRestoreActiveConfig.getActiveConfigText();
                  	  if (activeConfigText == null || activeConfigText.isEmpty())
                  	  {
                  		  logger.error("Error in retrieving config text");
                  		  errorResponse.setText("ERROR: Unable to retrieve active config from chassis.");
              			  errorResponse.setStringProperty("ERROR", "Error occured. Unable to retrieve active config from any of the chassis. Either no chassis are connected or no active config is available");
              			  errorResponse.setJMSCorrelationID(message.getJMSCorrelationID());
              			  this.replyProducer.send(message.getJMSReplyTo(),errorResponse); 
                  	  }
                  	  else
                  	  {
                  		  logger.info("Active config retrieved from chassis and successfully sent back to User "+userName);
                  		  logger.debug("Active config retrieved from chassis and successfully sent back to User "+userName);
                  		  response.setText(activeConfigText);
                  		  response.setJMSCorrelationID(message.getJMSCorrelationID());
	                    	  this.replyProducer.send(message.getJMSReplyTo(), response);
                  	  }
              	  }
              	  catch (Exception e) {
              		  logger.error("Error in retrieving config text",e);
              		  errorResponse.setText("ERROR: Unable to retrieve active config from chassis. Reason: "+e.getMessage());
          			  errorResponse.setStringProperty("ERROR", "Error occured. Unable to retrieve active config from any of the chassis");
          			  errorResponse.setJMSCorrelationID(message.getJMSCorrelationID());
          			  this.replyProducer.send(message.getJMSReplyTo(),errorResponse); 
					}
                }
                else if (requestMessageType.equalsIgnoreCase("REINITIALIZE_STATION"))
                {
              	  String reinitializeResult = null;
              	  String commandToReinitialize = textMessage.getText();
              	  logger.info("User "+userName+" removed station and hence reinitializing station master software");
              	  try
              	  {
              		M9kConfigXmlProcessor m9kConfigXmlProcessor = new M9kConfigXmlProcessor();
              		  reinitializeResult = m9kConfigXmlProcessor.reinitializeStation(commandToReinitialize);
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
                
                // END: 15-June-2021

        		}
        		catch (Exception e) {
					logger.error("Error while restoring active config ",e);
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
        }

		public static String getStationConfigXml() {
			return stationConfigXml;
		}

		public static void setStationConfigXml(String stationConfigXml) {
			M9kConfigRestoreRequestHandler.stationConfigXml = stationConfigXml;
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
			}

		}
		public void closeQConnection()
		{
			try
			{
				if (session != null)
				{
					session.close();
				}
				if (connection != null)
				{
					connection.close();
				}
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
}

