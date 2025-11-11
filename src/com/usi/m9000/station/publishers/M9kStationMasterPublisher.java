package com.usi.m9000.station.publishers;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.usi.m9000.station.consumers.M9kMasterRequestHandler;

public class M9kStationMasterPublisher {
	static ConnectionFactory connectionFactory = null;
	static Context jndiContext = null;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kStationMasterPublisher.class);
	public static void sendMessage(String queueName, String messageToSend, String messageType)
	{
		QueueSession qSession = null;
		QueueConnection qConnection=null;
        try {
        	QueueConnectionFactory qConnectionFactory = (QueueConnectionFactory)new ActiveMQConnectionFactory("tcp://"+M9kMasterRequestHandler.masterHost+":61616");//envContext.lookup("jms/ConnectionFactory");
            // Create a Connection
        	qConnection = qConnectionFactory.createQueueConnection();
        	qConnection.start();
            // Create a Session
        	qSession = qConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            // Create the destination (Topic or Queue)
            Queue destination = qSession.createQueue(queueName);

         // create a queue sender
            QueueSender  sender  = qSession.createSender(destination);

            javax.jms.TextMessage message = qSession.createTextMessage();
            message.setText( messageToSend );
            // NOTE: here we set a property on messages to be published:
            message.setStringProperty("MESSAGE_TYPE", messageType);
            sender.send(message);

        }
        catch (Exception e) {
            logger.error("Error in sending the message to message queue ",e);
        }
        finally {
			try {
				if (qSession != null) {
					qSession.close();
				}
				if (qConnection != null) {
					qConnection.close();
				}
			} catch (Exception e) {
				logger.warn("Error during cleanup ",e);
			}
		}
		
	}
}
