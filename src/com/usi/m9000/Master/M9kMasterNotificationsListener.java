package com.usi.m9000.Master;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.usi.m9000.common.email.SendMailUSI;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

public class M9kMasterNotificationsListener implements MessageListener, ExceptionListener, ServletContextListener {
	String stationQ;
	ActiveMQConnectionFactory connectionFactory = null;
	Connection connection  = null;
	Destination destination = null;
	private MessageProducer replyProducer;
	private Session session;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kMasterNotificationsListener.class);
	private String masterNotifyListener=M9kConstants.ENABLE;
	PropertiesConfiguration config = null;
	
	public M9kMasterNotificationsListener()
	{
		try {
			config = new PropertiesConfiguration("/m9k-master.properties");
		} catch (ConfigurationException e) {
			logger.error("Error in reading m9k-master.properties files. Enabling health poll by default.",e);
			config = null;
			masterNotifyListener=M9kConstants.ENABLE;
		}
	}
	public M9kMasterNotificationsListener(String stationQ) {
		this.stationQ = stationQ;
		// TODO Auto-generated constructor stub
	}


	@Override
	public void onException(JMSException arg0) {
		logger.error("JMS Exception occured.  Shutting down client.");
		
	}

	@Override
	public void onMessage(Message message) {
    	if (message instanceof TextMessage) {
              TextMessage textMessage = (TextMessage) message;
              try {
            	  String stationName = textMessage.getStringProperty("STATION");
            	  logger.debug("Message received from station "+stationName);
            	  String dateOccured = textMessage.getStringProperty("DATE_OF_OCCURENCE");
            	  logger.debug("Message received time "+dateOccured);
            	  String severity = textMessage.getStringProperty("SEVERITY");
                  logger.debug("Message type received "+severity);
                  String bodyText = textMessage.getText();
                  logger.debug("Received message: " + bodyText);
                  StringBuffer emailText = new StringBuffer();
                  emailText.append("Station:		"+stationName);
                  emailText.append(M9kConstants.NEWLINE);
                  emailText.append("Date occured:	"+dateOccured);
                  emailText.append(M9kConstants.NEWLINE);
                  emailText.append("Severity: 		"+ severity);
                  emailText.append(M9kConstants.NEWLINE);
                  emailText.append("Description:	"+bodyText);
                  
//                  try
//                  {
//                  SendMailTLS.sendEmail("vrsarav@gmail.com", "sramasamy@faultrecorder.com", severity+": Station: "+stationName+" Date: "+dateOccured, emailText.toString());
//                  }
//                  catch (Exception e) {
//					logger.error("Error in sending gmail ",e);
//				}
                  try{
                	  if (M9kUtils.isEmailNotificationEnabled())
                	  {
                		  SendMailUSI.sendEmail(severity+": Station: "+stationName+" Date: "+dateOccured, emailText.toString());
                	  }
                	  else
                	  {
                		  logger.info(severity+": Station: "+stationName+" Date: "+dateOccured+" Email content"+ emailText.toString());
                	  }
			          }
			          catch (Exception e) {
						logger.error("Error in sending USI ",e);
					}
//                  M9kStationMasterPublisher.sendMessage("Dfr1", textMessage.getText());
//                  if (textMessage.getStringProperty("SEVERITY").equalsIgnoreCase("CRITICAL"))
//                  {
//                	  System.out.println("Critical message received");
//                  }
//                  else if (textMessage.getStringProperty("SEVERITY").equalsIgnoreCase("WARNING"))
//                  {
//                	  System.out.println("Warning receieved");
//                  }
              } catch (JMSException ex) {
                  logger.error("Error reading message: " + ex);
              }
          } else  {
        	  logger.debug("Received: " + message);
          }
      }

	public void run()
    {
    	try {
			launch();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public void launch() throws JMSException, InterruptedException, NamingException
    {
//    	setUp();
    	createConsumerAndReceiveAMessage();
    }
    
	 private void setUp() throws JMSException, NamingException {
//         connectionFactory = new ActiveMQConnectionFactory(
//                 "failover:(tcp://localhost:61616)");
         
         InitialContext initCtx = new InitialContext();
         Context envContext = (Context) initCtx.lookup("java:comp/env");
         connectionFactory = (ActiveMQConnectionFactory) envContext.lookup("jms/ConnectionFactory");
     }

private void createConsumerAndReceiveAMessage() throws JMSException, InterruptedException, NamingException {
//         Connection connection = connectionFactory.createConnection();
//         connection.start();
//         session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//         destination = session.createQueue(stationQ);
//         System.out.println("Destination created");
//         MessageConsumer consumer = session.createConsumer(destination);
//         connection.setExceptionListener(this);
//         consumer.setMessageListener(this);
//         this.replyProducer = this.session.createProducer(null);
//         this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
//         System.out.println("Returning from createConsumerAndReceiveAMessage ");
         
         
    InitialContext initCtx = new InitialContext();
    Context envContext = (Context) initCtx.lookup("java:comp/env");
    connectionFactory = (ActiveMQConnectionFactory) envContext.lookup("jms/ConnectionFactory");
        
//         connectToServer();
	    connection = connectionFactory.createConnection();
	    connection.setExceptionListener(this);
	    ((ActiveMQConnection)connection).setCopyMessageOnSend(false);
	    ((ActiveMQConnection)connection).setUseCompression(true);
	    logger.debug("About to start the connection. "+connection);
	    connection.start();
	    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//         destination = session.createQueue(stationQ);
         MessageConsumer consumer = session.createConsumer((Destination) envContext.lookup("jms/queue/NotificationQ"));
         connection.setExceptionListener(this);
         consumer.setMessageListener(this);
         this.replyProducer = this.session.createProducer(null);
         this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
     }

private void connectToServer()
{
	try
	{
		logger.debug("Connection "+connection);
          connection = connectionFactory.createConnection();
          connection.setExceptionListener(this);
          ((ActiveMQConnection)connection).setCopyMessageOnSend(false);
          ((ActiveMQConnection)connection).setUseCompression(true);
          logger.debug("About to start the connection. "+connection);
          connection.start();
          logger.debug("\t\t\t\t\t\t Successful connection to JMS server");
          logger.info("Listening to queue in host localhost");
//          System.out.println("Listening to queue in host "+masterHost);
	}
	catch (Exception jmse) {
		logger.warn("Exception occurred trying to connect to JMS server."+connection,jmse);
	}

}

@Override
public void contextDestroyed(ServletContextEvent arg0) {
//	ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//    LogFactory.release(contextClassLoader);
	try {
		if (connection != null)
		{
			connection.close();
		}
	} catch (Exception e) {
		logger.warn("Error occured in contextDestroyed method ",e);
	}
	
}

@Override
public void contextInitialized(ServletContextEvent arg0) {
	if (getMasterNotifyListener().equalsIgnoreCase(M9kConstants.ENABLE))
	{
        	  try {
				launch();
			}
//        	  catch (JMSException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (NamingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
			catch (Exception e) {
				logger.error("Unable to establish Master Notification Listener. ",e);
			}
	}
	else
	{
		logger.info("Master Notification listener is disabled as per configuration in m9k-master.properties");
	}
}

public String getStationQ() {
	return stationQ;
}

public void setStationQ(String stationQ) {
	this.stationQ = stationQ;
}
public String getMasterNotifyListener() {
	if (config != null)
	{
		masterNotifyListener=config.getString("master-notification-listener", M9kConstants.DISABLE);
	}

	return masterNotifyListener;
}
public void setMasterNotifyListener(String masterNotifyListener) {
	this.masterNotifyListener = masterNotifyListener;
}

}
