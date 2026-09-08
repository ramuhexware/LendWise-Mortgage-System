package com.lendwise.passthrough;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/**
 * LendWise Pass-through Service
 *
 * Bridge between Oracle SOA Suite (WebLogic) and Gold Field microservices (EKS/OCP).
 * Provides reactive REST endpoints that receive requests from SOA BPEL processes
 * and route them to the appropriate Gold Field service via WebClient.
 *
 * Key Responsibilities:
 * - Protocol translation (SOAP/REST from SOA to REST for microservices)
 * - Circuit breaker and retry logic for resilience
 * - Async messaging to Kafka for event-driven flows
 * - Request/Response audit logging
 * - Correlation ID propagation
 */
@SpringBootApplication
@EnableReactiveMongoAuditing
public class PassthroughApplication {

    public static void main(String[] args) {
        SpringApplication.run(PassthroughApplication.class, args);
    }
}
