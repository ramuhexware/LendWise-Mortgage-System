package com.lendwise.patterns.producer.jms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.util.Map;

/**
 * JMS Producer using Spring JmsTemplate.
 * Parser should detect: jmsTemplate.send(), jmsTemplate.convertAndSend()
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JmsTemplateProducer {

    private final JmsTemplate jmsTemplate;

    private static final String CREDIT_CHECK_QUEUE = "credit.check.queue";
    private static final String NOTIFICATION_QUEUE = "notification.queue";
    private static final String UNDERWRITING_QUEUE = "underwriting.queue";

    /**
     * Send using JmsTemplate.send() with MessageCreator.
     * Parser detects: jmsTemplate.send()
     */
    public void sendCreditCheckRequest(String borrowerId, String ssn) {
        jmsTemplate.send(CREDIT_CHECK_QUEUE, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage();
                message.setText("{\"borrowerId\":\"" + borrowerId + "\",\"ssn\":\"" + ssn + "\"}");
                message.setStringProperty("MessageType", "CREDIT_CHECK");
                return message;
            }
        });

        log.info("Credit check request sent for borrower: {}", borrowerId);
    }

    /**
     * Send using JmsTemplate.convertAndSend() - automatic message conversion.
     * Parser detects: jmsTemplate.convertAndSend()
     */
    public void sendNotification(String borrowerId, String notificationType, String message) {
        Map<String, Object> payload = Map.of(
            "borrowerId", borrowerId,
            "type", notificationType,
            "message", message,
            "timestamp", System.currentTimeMillis()
        );

        // Parser detects this pattern
        jmsTemplate.convertAndSend(NOTIFICATION_QUEUE, payload);

        log.info("Notification sent: type={}, borrower={}", notificationType, borrowerId);
    }

    /**
     * Send with post-processor for adding headers.
     * Parser detects: jmsTemplate.convertAndSend() with MessagePostProcessor
     */
    public void sendUnderwritingRequest(String loanId, Object underwritingData) {
        jmsTemplate.convertAndSend(UNDERWRITING_QUEUE, underwritingData, message -> {
            message.setStringProperty("LoanId", loanId);
            message.setStringProperty("Priority", "HIGH");
            message.setIntProperty("RetryCount", 0);
            message.setJMSCorrelationID(java.util.UUID.randomUUID().toString());
            return message;
        });

        log.info("Underwriting request sent for loan: {}", loanId);
    }

    /**
     * Send and receive (synchronous request-reply pattern).
     * Parser detects: jmsTemplate.sendAndReceive()
     */
    public String sendAndWaitForResponse(String destination, String requestPayload) {
        Message response = jmsTemplate.sendAndReceive(destination, session -> {
            TextMessage request = session.createTextMessage(requestPayload);
            request.setJMSReplyTo(session.createTemporaryQueue());
            return request;
        });

        try {
            if (response instanceof TextMessage) {
                return ((TextMessage) response).getText();
            }
        } catch (JMSException e) {
            log.error("Failed to read response", e);
        }
        return null;
    }

    /**
     * Receive message from queue (blocking).
     * Parser detects: jmsTemplate.receive()
     */
    public Message receiveMessage(String destination) {
        return jmsTemplate.receive(destination);
    }

    /**
     * Receive and convert message.
     * Parser detects: jmsTemplate.receiveAndConvert()
     */
    public Object receiveAndConvert(String destination) {
        return jmsTemplate.receiveAndConvert(destination);
    }
}
