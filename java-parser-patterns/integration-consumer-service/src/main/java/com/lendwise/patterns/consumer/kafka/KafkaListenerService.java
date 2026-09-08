package com.lendwise.patterns.consumer.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Kafka Listener patterns.
 * Parser should detect: @KafkaListener, @KafkaHandler, topics, groupId
 */
@Service
@Slf4j
public class KafkaListenerService {

    /**
     * Basic @KafkaListener.
     * Parser detects: @KafkaListener with topics
     */
    @KafkaListener(topics = "loan-events", groupId = "lendwise-consumer-group")
    public void onLoanEvent(String message) {
        log.info("Kafka received loan event: {}", message);
        processLoanEvent(message);
    }

    /**
     * @KafkaListener with multiple topics.
     * Parser detects: @KafkaListener with topics array
     */
    @KafkaListener(topics = {"credit-events", "notification-events"}, groupId = "lendwise-multi-group")
    public void onMultipleTopics(String message) {
        log.info("Kafka received from multiple topics: {}", message);
    }

    /**
     * @KafkaListener with @Header annotations.
     * Parser detects: @Header for Kafka headers
     */
    @KafkaListener(topics = "loan-events-detailed", groupId = "lendwise-detailed-group")
    public void onLoanEventDetailed(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Kafka detailed - Topic: {}, Partition: {}, Offset: {}, Payload: {}",
                 topic, partition, offset, payload);
    }

    /**
     * @KafkaListener with ConsumerRecord.
     * Parser detects: ConsumerRecord parameter type
     */
    @KafkaListener(topics = "raw-events", groupId = "lendwise-raw-group")
    public void onRawEvent(ConsumerRecord<String, String> record) {
        log.info("Kafka raw record - Key: {}, Value: {}, Timestamp: {}",
                 record.key(), record.value(), record.timestamp());
    }

    /**
     * @KafkaListener with batch processing.
     * Parser detects: List parameter for batch
     */
    @KafkaListener(topics = "batch-events", groupId = "lendwise-batch-group", containerFactory = "batchFactory")
    public void onBatchEvents(List<String> messages) {
        log.info("Kafka batch received {} messages", messages.size());
        for (String message : messages) {
            log.debug("Batch message: {}", message);
        }
    }

    /**
     * @KafkaListener with manual acknowledgment.
     * Parser detects: Acknowledgment parameter
     */
    @KafkaListener(topics = "ack-events", groupId = "lendwise-ack-group")
    public void onAckEvent(String message, Acknowledgment acknowledgment) {
        try {
            log.info("Processing with ack: {}", message);
            processWithAck(message);
            // Manual acknowledgment
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing, not acknowledging", e);
        }
    }

    /**
     * @KafkaListener with specific partitions.
     * Parser detects: @TopicPartition, @PartitionOffset
     */
    @KafkaListener(
        groupId = "lendwise-partition-group",
        topicPartitions = @TopicPartition(
            topic = "partition-events",
            partitionOffsets = {
                @PartitionOffset(partition = "0", initialOffset = "0"),
                @PartitionOffset(partition = "1", initialOffset = "0")
            }
        )
    )
    public void onPartitionEvent(String message) {
        log.info("Kafka partition-specific event: {}", message);
    }

    /**
     * @KafkaListener with concurrency.
     * Parser detects: concurrency attribute
     */
    @KafkaListener(topics = "concurrent-events", groupId = "lendwise-concurrent-group", concurrency = "3")
    public void onConcurrentEvent(String message) {
        log.info("Kafka concurrent processing: {} on thread: {}",
                 message, Thread.currentThread().getName());
    }

    /**
     * @KafkaListener with error handler.
     * Parser detects: errorHandler attribute
     */
    @KafkaListener(topics = "error-prone-events", groupId = "lendwise-error-group", errorHandler = "kafkaErrorHandler")
    public void onErrorProneEvent(String message) {
        log.info("Processing error-prone event: {}", message);
        if (message.contains("ERROR")) {
            throw new RuntimeException("Simulated processing error");
        }
    }

    // Processing methods
    private void processLoanEvent(String message) {
        log.info("Processing loan event: {}", message);
    }

    private void processWithAck(String message) {
        log.info("Processing with acknowledgment: {}", message);
    }
}

/**
 * Class-level @KafkaListener with @KafkaHandler.
 * Parser detects: @KafkaListener on class, @KafkaHandler on methods
 */
@Service
@KafkaListener(topics = "multi-type-events", groupId = "lendwise-multi-type-group")
@Slf4j
class MultiTypeKafkaListener {

    /**
     * Handler for String messages.
     * Parser detects: @KafkaHandler
     */
    @KafkaHandler
    public void handleString(String message) {
        log.info("Handling String: {}", message);
    }

    /**
     * Handler for Integer messages.
     */
    @KafkaHandler
    public void handleInteger(Integer count) {
        log.info("Handling Integer: {}", count);
    }

    /**
     * Default handler for unmatched types.
     * Parser detects: @KafkaHandler(isDefault=true)
     */
    @KafkaHandler(isDefault = true)
    public void handleDefault(Object unknown) {
        log.info("Handling unknown type: {}", unknown);
    }
}
