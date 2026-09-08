package com.lendwise.patterns.producer.jms;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * JMS Queue Producer using raw JMS API with JNDI lookup.
 * Parser should detect: InitialContext, lookup, createConnection, createSession,
 * createProducer, producer.send()
 */
@Service
@Slf4j
public class JmsQueueProducer {

    private static final String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
    private static final String JMS_FACTORY = "jms/ConnectionFactory";
    private static final String QUEUE_NAME = "jms/queue/CreditCheckQueue";
    private static final String PROVIDER_URL = "t3://localhost:7001";

    /**
     * Send message using JNDI lookup and raw JMS API.
     * This pattern is what the parser detects for JMS producer calls.
     */
    public void sendCreditCheckRequest(String borrowerId, String ssn) {
        Context context = null;
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try {
            // JNDI Lookup - Parser detects InitialContext and lookup()
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
            env.put(Context.PROVIDER_URL, PROVIDER_URL);

            context = new InitialContext(env);

            // Lookup ConnectionFactory - Parser detects context.lookup()
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(JMS_FACTORY);

            // Lookup Queue - Parser detects JNDI queue lookup
            Queue queue = (Queue) context.lookup(QUEUE_NAME);

            // Create Connection - Parser detects connectionFactory.createConnection()
            connection = connectionFactory.createConnection();

            // Create Session - Parser detects connection.createSession()
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create Producer - Parser detects session.createProducer()
            producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            // Create and send message - Parser detects producer.send()
            TextMessage message = session.createTextMessage();
            message.setText("{\"borrowerId\":\"" + borrowerId + "\",\"ssn\":\"" + ssn + "\"}");
            message.setStringProperty("MessageType", "CREDIT_CHECK_REQUEST");
            message.setStringProperty("CorrelationId", java.util.UUID.randomUUID().toString());

            producer.send(message);

            log.info("JMS message sent to queue {} for borrower {}", QUEUE_NAME, borrowerId);

        } catch (NamingException | JMSException e) {
            log.error("Failed to send JMS message", e);
            throw new RuntimeException("JMS send failed", e);
        } finally {
            closeQuietly(producer, session, connection, context);
        }
    }

    /**
     * Send message with transaction.
     */
    public void sendTransactionalMessage(String destination, String payload) throws JMSException, NamingException {
        Context context = new InitialContext();
        ConnectionFactory factory = (ConnectionFactory) context.lookup(JMS_FACTORY);
        Queue queue = (Queue) context.lookup(destination);

        try (Connection connection = factory.createConnection();
             Session session = connection.createSession(true, Session.SESSION_TRANSACTED)) {

            MessageProducer producer = session.createProducer(queue);

            TextMessage message = session.createTextMessage(payload);
            producer.send(message);

            // Commit transaction
            session.commit();
            log.info("Transactional message committed to {}", destination);
        }
    }

    /**
     * Send ObjectMessage with serializable object.
     */
    public void sendObjectMessage(Queue queue, Session session, java.io.Serializable object) throws JMSException {
        MessageProducer producer = session.createProducer(queue);

        ObjectMessage objectMessage = session.createObjectMessage(object);
        objectMessage.setJMSType("OBJECT_MESSAGE");

        producer.send(objectMessage);
    }

    /**
     * Send MapMessage for structured data.
     */
    public void sendMapMessage(String borrowerId, int creditScore, double loanAmount) throws Exception {
        Context context = new InitialContext();
        ConnectionFactory factory = (ConnectionFactory) context.lookup(JMS_FACTORY);
        Queue queue = (Queue) context.lookup(QUEUE_NAME);

        try (Connection connection = factory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {

            MessageProducer producer = session.createProducer(queue);

            MapMessage mapMessage = session.createMapMessage();
            mapMessage.setString("borrowerId", borrowerId);
            mapMessage.setInt("creditScore", creditScore);
            mapMessage.setDouble("loanAmount", loanAmount);
            mapMessage.setLong("timestamp", System.currentTimeMillis());

            producer.send(mapMessage);
        }
    }

    private void closeQuietly(MessageProducer producer, Session session, Connection connection, Context context) {
        try { if (producer != null) producer.close(); } catch (JMSException ignored) {}
        try { if (session != null) session.close(); } catch (JMSException ignored) {}
        try { if (connection != null) connection.close(); } catch (JMSException ignored) {}
        try { if (context != null) context.close(); } catch (NamingException ignored) {}
    }
}
