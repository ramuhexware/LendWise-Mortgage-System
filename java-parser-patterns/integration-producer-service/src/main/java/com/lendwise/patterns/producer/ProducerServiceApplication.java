package com.lendwise.patterns.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Integration Producer Service
 *
 * Contains Java code patterns for PRODUCER/SENDER/CLIENT operations:
 * - SOAP clients (SOAPConnection, WebServiceTemplate)
 * - JMS producers (JNDI lookup, createProducer, send)
 * - HTTP clients (Java 11 HttpClient, Apache, OkHttp, URLConnection)
 * - Database writes (JDBC, JPA, Hibernate, Spring Data JPA, MyBatis)
 * - EJB patterns (@Stateless, @Singleton, @EJB)
 * - Async patterns (CompletableFuture, Streams)
 */
@SpringBootApplication
public class ProducerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProducerServiceApplication.class, args);
    }
}
