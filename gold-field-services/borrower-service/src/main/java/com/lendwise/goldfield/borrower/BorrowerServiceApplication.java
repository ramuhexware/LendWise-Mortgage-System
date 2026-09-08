package com.lendwise.goldfield.borrower;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/**
 * Gold Field Borrower Service
 *
 * Microservice for managing borrower data including:
 * - Borrower profile creation and updates
 * - Income/employment verification tracking
 * - DTI/LTV/PITI calculations
 * - Borrower status management
 */
@SpringBootApplication
@EnableReactiveMongoAuditing
public class BorrowerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BorrowerServiceApplication.class, args);
    }
}
