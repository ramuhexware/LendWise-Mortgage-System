package com.lendwise.patterns.consumer.jms;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * Raw JMS consumer using JNDI lookup.
 * Parser should detect: createConsumer(), receive(), receiveNoWait(),
 * onMessage(), setMessageListener()
 */
@Component
@Slf4j
public class JmsQueueConsumer {

    private static final String JMS_FACTORY = "ConnectionFactory";
    private static final String QUEUE_NAME = "queue/CreditResponseQueue";

    private Connection connection;
    private Session session;
    private MessageConsumer consumer;

    /**
     * Synchronous receive with JNDI lookup.
     * Parser detects: context.lookup(), createConsumer(), receive()
     */
    public String receiveMessage() throws JMSException, NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
        env.put(Context.PROVIDER_URL, "tcp://localhost:61616");

        try {
            // Parser detects: new InitialContext()
            Context context = new InitialContext(env);

            // Parser detects: context.lookup()
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(JMS_FACTORY);
            Queue queue = (Queue) context.lookup(QUEUE_NAME);

            // Parser detects: connectionFactory.createConnection()
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Parser detects: session.createConsumer()
            consumer = session.createConsumer(queue);

            connection.start();

            // Parser detects: consumer.receive() - synchronous receive with timeout
            Message message = consumer.receive(5000);

            if (message instanceof TextMessage textMessage) {
                String text = textMessage.getText();
                log.info("Received message: {}", text);
                return text;
            }

            return null;

        } finally {
            closeResources();
        }
    }

    /**
     * Non-blocking receive.
     * Parser detects: receiveNoWait()
     */
    public String receiveNoWait() throws JMSException, NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
        env.put(Context.PROVIDER_URL, "tcp://localhost:61616");

        try {
            Context context = new InitialContext(env);
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(JMS_FACTORY);
            Queue queue = (Queue) context.lookup(QUEUE_NAME);

            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            consumer = session.createConsumer(queue);
            connection.start();

            // Parser detects: consumer.receiveNoWait()
            Message message = consumer.receiveNoWait();

            if (message instanceof TextMessage textMessage) {
                return textMessage.getText();
            }

            return null;

        } finally {
            closeResources();
        }
    }

    /**
     * Asynchronous message listener.
     * Parser detects: setMessageListener(), MessageListener.onMessage()
     */
    public void startAsyncListener() throws JMSException, NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
        env.put(Context.PROVIDER_URL, "tcp://localhost:61616");

        Context context = new InitialContext(env);
        ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(JMS_FACTORY);
        Queue queue = (Queue) context.lookup(QUEUE_NAME);

        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        consumer = session.createConsumer(queue);

        // Parser detects: consumer.setMessageListener() with MessageListener
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    if (message instanceof TextMessage textMessage) {
                        String text = textMessage.getText();
                        log.info("Async received: {}", text);
                        processMessage(text);
                    }
                } catch (JMSException e) {
                    log.error("Error processing message", e);
                }
            }
        });

        connection.start();
        log.info("Async listener started for queue: {}", QUEUE_NAME);
    }

    /**
     * Lambda-based message listener.
     * Parser detects: setMessageListener() with lambda
     */
    public void startLambdaListener() throws JMSException, NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
        env.put(Context.PROVIDER_URL, "tcp://localhost:61616");

        Context context = new InitialContext(env);
        ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(JMS_FACTORY);
        Queue queue = (Queue) context.lookup(QUEUE_NAME);

        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        consumer = session.createConsumer(queue);

        // Parser detects: setMessageListener() with lambda expression
        consumer.setMessageListener(message -> {
            try {
                if (message instanceof TextMessage textMessage) {
                    log.info("Lambda listener received: {}", textMessage.getText());
                }
            } catch (JMSException e) {
                log.error("Error in lambda listener", e);
            }
        });

        connection.start();
    }

    /**
     * Topic subscriber pattern.
     * Parser detects: createConsumer() on Topic
     */
    public void subscribeTopic(String topicName) throws JMSException, NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
        env.put(Context.PROVIDER_URL, "tcp://localhost:61616");

        Context context = new InitialContext(env);
        ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(JMS_FACTORY);
        Topic topic = (Topic) context.lookup("topic/" + topicName);

        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Parser detects: session.createConsumer() on Topic
        consumer = session.createConsumer(topic);

        consumer.setMessageListener(message -> {
            log.info("Topic message received on: {}", topicName);
        });

        connection.start();
    }

    private void processMessage(String message) {
        log.info("Processing message: {}", message);
    }

    private void closeResources() {
        try {
            if (consumer != null) consumer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (JMSException e) {
            log.error("Error closing JMS resources", e);
        }
    }
}
