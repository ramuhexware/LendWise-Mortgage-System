package com.lendwise.patterns.consumer.jms;

import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Spring JMS Listener patterns.
 * Parser should detect: @JmsListener annotation, destination, containerFactory
 */
@Service
@Slf4j
public class JmsListenerService {

    /**
     * Basic @JmsListener.
     * Parser detects: @JmsListener with destination
     */
    @JmsListener(destination = "queue/CreditResponseQueue")
    public void onCreditResponse(String message) {
        log.info("Received credit response: {}", message);
        processCreditResponse(message);
    }

    /**
     * @JmsListener with container factory.
     * Parser detects: @JmsListener with containerFactory
     */
    @JmsListener(destination = "queue/LoanNotificationQueue", containerFactory = "jmsListenerContainerFactory")
    public void onLoanNotification(String message) {
        log.info("Received loan notification: {}", message);
        processLoanNotification(message);
    }

    /**
     * @JmsListener with selector.
     * Parser detects: @JmsListener with selector
     */
    @JmsListener(destination = "queue/DocumentReadyQueue", selector = "priority = 'HIGH'")
    public void onHighPriorityDocument(String message) {
        log.info("Received HIGH priority document: {}", message);
    }

    /**
     * @JmsListener with @Payload and @Header.
     * Parser detects: @Payload, @Header annotations
     */
    @JmsListener(destination = "queue/AuditQueue")
    public void onAuditMessage(
            @Payload String payload,
            @Header(JmsHeaders.MESSAGE_ID) String messageId,
            @Header(JmsHeaders.CORRELATION_ID) String correlationId) {

        log.info("Audit message - ID: {}, CorrelationID: {}, Payload: {}",
                 messageId, correlationId, payload);
    }

    /**
     * @JmsListener receiving raw Message object.
     * Parser detects: Message parameter type
     */
    @JmsListener(destination = "queue/RawMessageQueue")
    public void onRawMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String text = textMessage.getText();
                String messageId = message.getJMSMessageID();
                log.info("Raw message received - ID: {}, Text: {}", messageId, text);
            }
        } catch (Exception e) {
            log.error("Error processing raw message", e);
        }
    }

    /**
     * @JmsListener with concurrency.
     * Parser detects: concurrency attribute
     */
    @JmsListener(destination = "queue/BulkProcessingQueue", concurrency = "3-10")
    public void onBulkMessage(String message) {
        log.info("Bulk processing message: {}", message);
    }

    /**
     * @JmsListener with multiple destinations (requires separate listener per destination in practice).
     */
    @JmsListener(destination = "queue/NotificationQueue1")
    public void onNotification1(String message) {
        log.info("Notification 1: {}", message);
    }

    @JmsListener(destination = "queue/NotificationQueue2")
    public void onNotification2(String message) {
        log.info("Notification 2: {}", message);
    }

    // Processing methods
    private void processCreditResponse(String message) {
        log.info("Processing credit response...");
    }

    private void processLoanNotification(String message) {
        log.info("Processing loan notification...");
    }
}
