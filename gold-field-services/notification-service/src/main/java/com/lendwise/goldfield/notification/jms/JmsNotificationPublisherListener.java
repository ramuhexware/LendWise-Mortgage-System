package com.lendwise.goldfield.notification.jms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * JMS Notification Publisher & Listener Adapter
 *
 * Listens for incoming notification event messages on 'jms/NotificationQueue'
 * published by BPEL composites or OSB pipelines, and publishes outbound email/SMS
 * dispatch events.
 */
@Component
public class JmsNotificationPublisherListener {

    @Autowired(required = false)
    private JmsTemplate jmsTemplate;

    /**
     * Consume notification event messages from JMS Queue
     */
    @JmsListener(destination = "jms/NotificationQueue")
    public void receiveNotificationEvent(String messageText) {
        System.out.println("[JMS Queue Subscriber] Received event notification: " + messageText);

        // Process notification dispatch (Email / SMS)
        dispatchNotification(messageText);
    }

    /**
     * Publish notification event to JMS Queue
     */
    public void publishNotificationEvent(String destinationQueue, String payload) {
        if (jmsTemplate != null) {
            jmsTemplate.convertAndSend(destinationQueue, payload);
            System.out.println("[JMS Queue Publisher] Published event to " + destinationQueue + ": " + payload);
        } else {
            System.out.println("[JMS Queue Publisher Simulation] Message target: " + destinationQueue + ", Payload: " + payload);
        }
    }

    private void dispatchNotification(String message) {
        // Dispatch logic to SendGrid/Twilio API
        System.out.println("[Notification Service] Dispatched notification: " + message);
    }
}
