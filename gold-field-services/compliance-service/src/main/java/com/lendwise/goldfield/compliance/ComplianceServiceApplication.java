package com.lendwise.goldfield.compliance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/**
 * Gold Field Compliance Service
 *
 * Microservice for compliance operations including:
 * - TRID timing validation
 * - QM/ATR eligibility checks
 * - Fee tolerance tracking
 * - State-specific compliance rules
 * - HMDA/ECOA tracking
 */
@SpringBootApplication
@EnableReactiveMongoAuditing
public class ComplianceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComplianceServiceApplication.class, args);
    }
}
