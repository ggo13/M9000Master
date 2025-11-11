package com.usi.m9000.station.consumers;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Hello world!
 */
public class dfrMessageConsumer implements MessageListener, ExceptionListener {

ActiveMQConnectionFactory connectionFactory = null;
Destination destination = null;
 private static Session session;
    public static void main(String[] args) throws Exception {
    	dfrMessageConsumer consumer = new dfrMessageConsumer();
    	consumer.setUp();
    	consumer.createConsumerAndReceiveAMessage();

    }

 private void setUp() throws JMSException {
          connectionFactory = new ActiveMQConnectionFactory(
                  "tcp://195.1.1.97:61616");
      }

private void createConsumerAndReceiveAMessage() throws JMSException, InterruptedException {
          Connection connection = connectionFactory.createConnection();
          connection.start();
          session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
          destination = session.createQueue("Dfr1");
          MessageConsumer consumer = session.createConsumer(destination);
          connection.setExceptionListener(this);
          consumer.setMessageListener(this);
      }

        public void onMessage(Message message) {
            try {
				System.out.println("message type????? "+message.getStringProperty("MESSAGE_TYPE"));
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	  if (message instanceof TextMessage) {
                  TextMessage textMessage = (TextMessage) message;
                  try {
                      System.out.println("Message type received "+textMessage.getStringProperty("MESSAGE_TYPE"));
                      System.out.println("Received message: " + textMessage.getText());
                      
                  } catch (JMSException ex) {
                      System.out.println("Error reading message: " + ex);
                  }
              } else  {
                  System.out.println("Received: " + message);
              }
          }
        public synchronized void onException(JMSException ex) {
            System.out.println("JMS Exception occured.  Shutting down client.");
        }
}

