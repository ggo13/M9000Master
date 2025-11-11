package com.usi.m9000.test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Hello world!
 */
public class Producer {
static ConnectionFactory connectionFactory = null;
static Context jndiContext = null;

    public static void main(String[] args) throws Exception {
            try {
            	jndiContext = new InitialContext();
                // Create a ConnectionFactory
            	connectionFactory = (ConnectionFactory)jndiContext.lookup("ConnectionFactory");
//                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = (Destination)jndiContext.lookup("TEST.FOO");

                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                // Read all standard input and send it as a message.

                java.io.BufferedReader stdin =
                    new java.io.BufferedReader( new java.io.InputStreamReader( System.in ) );		   
    		   System.out.println("Type message to e sent and Enter...");
                while ( true )
                {
                    String s = stdin.readLine();

                    if ( s == null )
                        break;
                    else if ( s.length() > 0 )
                    {
                        javax.jms.TextMessage message = session.createTextMessage();
                        message.setText( s );
                        // NOTE: here we set a property on messages to be published:
//                        msg.setStringProperty(PROPERTY_NAME, selection);
                        producer.send(message);
                    }
                }

                // Create a messages
//                String text = "Hello world! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
//                TextMessage message = session.createTextMessage(text);

                // Tell the producer to send the message
//                System.out.println("Sent message: "+ message.hashCode() + " : " + Thread.currentThread().getName());
//                producer.send(message);

                // Clean up
//                session.close();
//                connection.close();
            }
            catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
    }


}
