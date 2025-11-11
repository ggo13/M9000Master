package com.usi.m9000.station.consumers;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import com.usi.m9000.station.action.M9kStationRequestProcessor;
import com.usi.m9000.station.commands.M9kContAnalogFromDb;
import com.usi.m9000.station.commands.M9kContDataFromDb;
import com.usi.m9000.station.commands.M9kFileRequestProcessor;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;

/**
 * 
 */
public class M9kMasterFileRequestHandler extends Thread implements MessageListener, ExceptionListener {
	private static M9kMasterFileRequestHandler m9kMasterFileRequestHandler = null;
	static String stationConfigXml;
	ActiveMQConnectionFactory connectionFactory = null;
	Destination destination = null;
	private MessageProducer replyProducer;

	private Session session;
	QueueSession qSession = null;
	QueueConnection qConnection=null;
	String queueName;
	public static String masterHost;
	HierarchicalINIConfiguration iniConf;
	Connection connection  = null;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kMasterFileRequestHandler.class);
	M9kStationRequestProcessor m9kStationRequestProcessor;
//	ExecutorService executor = Executors.newFixedThreadPool(1);
//	ExecutorService executor = Executors.newSingleThreadExecutor();
	ExecutorService executor;
	ExecutorService executorFileProcessor;
	private String userName;
	public static void main(String[] args) throws Exception {
    	M9kMasterFileRequestHandler consumer = new M9kMasterFileRequestHandler();
    	consumer.launch();
//    	consumer.setUp();
//    	consumer.createConsumerAndReceiveAMessage();

    }

    
    private M9kMasterFileRequestHandler() throws M9000Exception {
		try {
			iniConf = new HierarchicalINIConfiguration("station.properties");
			masterHost = iniConf.getString("station.master_host", "192.168.2.200");
			if (M9kStationDBUtil.getStationDetails() != null)
			{
				queueName = ""+M9kStationDBUtil.getStationDetails().getSystemStationId()+"-Files";
				m9kStationRequestProcessor = M9kStationRequestProcessor.getInstance();
			}
			else
			{
				java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
				queueName = localMachine.getHostName();
			}
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

    public static M9kMasterFileRequestHandler getInstance() throws M9000Exception
	{
    	logger.debug("getting instance of M9kMasterFileRequestHandler "+m9kMasterFileRequestHandler);
		if (m9kMasterFileRequestHandler == null)
		{
			logger.debug("Creating a M9kMasterFileRequestHandler instance to listen to hostname...");
			m9kMasterFileRequestHandler = new M9kMasterFileRequestHandler();

		}
		
		return m9kMasterFileRequestHandler;
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
//        	System.out.println("Message Received "+message);
        	String requestMessageType;
        	if (message instanceof TextMessage) {
                  TextMessage textMessage = (TextMessage) message;
                  try {
                	  requestMessageType = textMessage.getStringProperty("MESSAGE_TYPE");
                  		logger.info("User "+userName+"'s request received from Master to process"+requestMessageType);
                	  userName = textMessage.getStringProperty("USER_NAME");
                	  logger.debug("QMessage type received: "+requestMessageType);
                      logger.debug("Received message: " + textMessage.getText());
//                      M9kStationMasterPublisher.sendMessage("Dfr1", textMessage.getText());
                      if (requestMessageType.equalsIgnoreCase("FILE_RETRIEVE"))
                      {
                    	  logger.debug("File name received "+textMessage.getText()+" Invoking file processing thread");
                      	logger.info("User "+userName+" requests to fetch file "+textMessage.getText());

                    	  
                    	  try
                    	  {
                    		  executorFileProcessor = Executors.newSingleThreadExecutor();
                    		  executorFileProcessor.execute(new M9kFileRequestProcessor(session,replyProducer,message));
                    		  logger.debug("Invoked successfully and now acknowledging the message");
                    		  executorFileProcessor.shutdown();
                    		  message.acknowledge();
                    		  logger.debug("acknowledged successfully ");
                    	  }
                    	  catch (Exception e) {
							logger.error("["+userName+"] File transfer process resulted in a exception ",e);
						}
                      }
                      else  if (requestMessageType.equalsIgnoreCase("CONT_DATA_RETRIEVE"))
                      {
                    	  logger.debug("File name received "+textMessage.getText()+" Invoking file processing thread");
                    	  logger.info("User "+userName+" requests to fetch file "+textMessage.getText());
                    	  String dataType = textMessage.getStringProperty("DATA_TYPE");
                    	  
                    	  try
                    	  {
                			  executor = Executors.newSingleThreadExecutor();
                    		  if (dataType != null && dataType.equalsIgnoreCase(M9kStationConstants.CONT_DATA_TYPE))
                    		  {
//                    			  if (textMessage.getStringProperty("DATA_EXPORT_TYPE") != null 
//                    					  && (textMessage.getStringProperty("DATA_EXPORT_TYPE").equalsIgnoreCase(M9kStationConstants.PHASOR)
//                    							  || textMessage.getStringProperty("DATA_EXPORT_TYPE").toLowerCase().contains(M9kStationConstants.SEQUENCE))) // Process Phasor data export
//                    			  {
//                    				  executor.execute(new M9kContPhasorDataFromDb(session,replyProducer,message));
//                    			  }
//                    			  else // Process all exports data except phasor which is hadled different
//                    			  {
                    				  executor.execute(new M9kContDataFromDb(session,replyProducer,message));
//                    			  }
                    		  }
                    		  else if (dataType != null && dataType.equalsIgnoreCase(M9kStationConstants.CONT_ANALOG_DATA_TYPE))
                    		  {
    	                    	  executor.execute(new M9kContAnalogFromDb(session,replyProducer,message));
                    		  }
	                    	  executor.shutdown();
//                    		  logger.debug("Invoked successfully and now acknowledging the message");
                    		  message.acknowledge();
                    		  logger.debug("acknowledged successfully ");
                    	  }
                    	  catch (Exception e) {
							logger.error("["+userName+"] Continuous data retrieval process resulted in a exception ",e);
						}
                      }

                  } catch (JMSException ex) {
                	  logger.error("["+userName+"] Error occured in message handling.",ex);
                  } catch (Exception e) {
					logger.error("["+userName+"] Error occured in message handling.",e);
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
			M9kMasterFileRequestHandler.stationConfigXml = stationConfigXml;
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
		          logger.info("Station Master Listening to queue in host "+masterHost);
//		          System.out.println("Listening to queue in host "+masterHost);
			}
			catch (Exception jmse) {
				logger.error("Exception occurred trying to connect to JMS server."+connection,jmse);
			}

		}
		

}

