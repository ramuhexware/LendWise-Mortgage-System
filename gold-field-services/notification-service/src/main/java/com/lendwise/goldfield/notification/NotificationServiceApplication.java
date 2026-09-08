package com.lendwise.goldfield.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/**
 * Gold Field Notification Service
 *
 * Microservice for notification operations including:
 * - Email notifications via SendGrid/SES
 * - SMS notifications via Twilio
 * - Push notifications
 * - Notification templates
 * - Delivery tracking
 */
@SpringBootApplication
@EnableReactiveMongoAuditing
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
