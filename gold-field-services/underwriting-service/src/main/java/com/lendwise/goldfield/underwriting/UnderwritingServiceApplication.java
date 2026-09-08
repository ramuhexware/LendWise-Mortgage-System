package com.lendwise.goldfield.underwriting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/**
 * Gold Field Underwriting Service
 *
 * Microservice for underwriting decisions including:
 * - Credit report storage and analysis
 * - AUS findings tracking
 * - Decision and condition management
 * - Risk scoring
 * - Underwriting workflow state
 */
@SpringBootApplication
@EnableReactiveMongoAuditing
public class UnderwritingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnderwritingServiceApplication.class, args);
    }
}
