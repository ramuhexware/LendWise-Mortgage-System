package com.lendwise.goldfield.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/**
 * Gold Field Pricing Service
 *
 * Microservice for pricing operations including:
 * - Rate lock management
 * - LLPA calculations
 * - Pricing scenarios
 * - Investor pricing integration
 * - Amortization schedules
 */
@SpringBootApplication
@EnableReactiveMongoAuditing
public class PricingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PricingServiceApplication.class, args);
    }
}
