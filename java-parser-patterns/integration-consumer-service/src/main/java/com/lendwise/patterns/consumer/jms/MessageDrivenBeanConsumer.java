package com.lendwise.patterns.consumer.jms;

import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Message-Driven Bean (MDB) pattern.
 * Parser should detect: @MessageDriven, MessageListener, @ActivationConfigProperty
 */
@MessageDriven(
    name = "CreditResponseMDB",
    activationConfig = {
        @ActivationConfigProperty(
            propertyName = "destinationType",
            propertyValue = "jakarta.jms.Queue"
        ),
        @ActivationConfigProperty(
            propertyName = "destination",
            propertyValue = "queue/CreditResponseQueue"
        ),
        @ActivationConfigProperty(
            propertyName = "acknowledgeMode",
            propertyValue = "Auto-acknowledge"
        )
    }
)
@Slf4j
public class MessageDrivenBeanConsumer implements MessageListener {

    // Parser detects: @Resource for JMS connection
    @Resource
    private MessageDrivenContext mdbContext;

    @Resource(mappedName = "java:/JmsXA")
    private ConnectionFactory connectionFactory;

    /**
     * MDB onMessage implementation.
     * Parser detects: MessageListener.onMessage() override
     */
    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String content = textMessage.getText();
                String messageId = message.getJMSMessageID();

                log.info("MDB received message - ID: {}, Content: {}", messageId, content);

                processMessage(content);

            } else if (message instanceof MapMessage mapMessage) {
                String borrowerId = mapMessage.getString("borrowerId");
                int score = mapMessage.getInt("creditScore");

                log.info("MDB received MapMessage - BorrowerId: {}, Score: {}", borrowerId, score);

            } else if (message instanceof ObjectMessage objectMessage) {
                Object obj = objectMessage.getObject();
                log.info("MDB received ObjectMessage: {}", obj);
            }

        } catch (JMSException e) {
            log.error("Error processing message in MDB", e);
            // Mark for rollback if in transaction
            mdbContext.setRollbackOnly();
        }
    }

    private void processMessage(String content) {
        log.info("Processing message content: {}", content);
    }
}

/**
 * Another MDB for topic subscription.
 * Parser detects: @MessageDriven on Topic
 */
@MessageDriven(
    name = "LoanEventTopicMDB",
    activationConfig = {
        @ActivationConfigProperty(
            propertyName = "destinationType",
            propertyValue = "jakarta.jms.Topic"
        ),
        @ActivationConfigProperty(
            propertyName = "destination",
            propertyValue = "topic/LoanEvents"
        ),
        @ActivationConfigProperty(
            propertyName = "subscriptionDurability",
            propertyValue = "Durable"
        ),
        @ActivationConfigProperty(
            propertyName = "clientId",
            propertyValue = "LendWiseClient"
        ),
        @ActivationConfigProperty(
            propertyName = "subscriptionName",
            propertyValue = "LoanEventSubscription"
        )
    }
)
@Slf4j
class LoanEventTopicMDB implements MessageListener {

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                log.info("Topic MDB received loan event: {}", textMessage.getText());
            }
        } catch (JMSException e) {
            log.error("Error in Topic MDB", e);
        }
    }
}
