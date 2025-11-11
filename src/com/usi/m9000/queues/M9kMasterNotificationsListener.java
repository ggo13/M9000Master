package com.usi.m9000.queues;

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

public class M9kMasterNotificationsListener implements MessageListener, ExceptionListener, ServletContextListener {
	String stationQ;
	ActiveMQConnectionFactory connectionFactory = null;
	Connection connection  = null;
	Destination destination = null;
	private MessageProducer replyProducer;
	private Session session;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kMasterNotificationsListener.class);

	public M9kMasterNotificationsListener()
	{
		
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
    	TextMessage response = null;
    	try {
			response = session.createTextMessage();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (message instanceof TextMessage) {
              TextMessage textMessage = (TextMessage) message;
              try {
            	  logger.debug("Message type received "+textMessage.getStringProperty("MESSAGE_TYPE"));
            	  logger.debug("Received message: " + textMessage.getText());
//                  M9kStationMasterPublisher.sendMessage("Dfr1", textMessage.getText());
                  if (textMessage.getStringProperty("MESSAGE_TYPE").equalsIgnoreCase("CRITICAL"))
                  {
                	  logger.debug("Critical message received");
                  }
                  else if (textMessage.getStringProperty("MESSAGE_TYPE").equalsIgnoreCase("WARNING"))
                  {
                	  logger.debug("Warning receieved");
                  }
              } catch (JMSException ex) {
            	  logger.debug("Error reading message: " + ex);
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
    	setUp();
    	createConsumerAndReceiveAMessage();
    }
    
	 private void setUp() throws JMSException, NamingException {
//         connectionFactory = new ActiveMQConnectionFactory(
//                 "failover:(tcp://localhost:61616)");
         
         InitialContext initCtx = new InitialContext();
         Context envContext = (Context) initCtx.lookup("java:comp/env");
         connectionFactory = (ActiveMQConnectionFactory) envContext.lookup("jms/ConnectionFactory");
     }

private void createConsumerAndReceiveAMessage() throws JMSException, InterruptedException {
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
         
         
         
         connectToServer();
         session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         destination = session.createQueue(stationQ);
         MessageConsumer consumer = session.createConsumer(destination);
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
	
}

@Override
public void contextInitialized(ServletContextEvent arg0) {
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

public String getStationQ() {
	return stationQ;
}

public void setStationQ(String stationQ) {
	this.stationQ = stationQ;
}

}
