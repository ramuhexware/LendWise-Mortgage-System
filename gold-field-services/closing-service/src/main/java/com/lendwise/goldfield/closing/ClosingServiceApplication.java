package com.lendwise.goldfield.closing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/**
 * Gold Field Closing Service
 *
 * Microservice for closing operations including:
 * - Closing Disclosure generation
 * - Cash-to-close calculations
 * - Fee tracking and comparisons
 * - eSign envelope management
 * - Closing scheduling
 */
@SpringBootApplication
@EnableReactiveMongoAuditing
public class ClosingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClosingServiceApplication.class, args);
    }
}
