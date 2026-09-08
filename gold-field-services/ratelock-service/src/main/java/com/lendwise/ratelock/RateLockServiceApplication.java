package com.lendwise.ratelock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableReactiveMongoAuditing
@EnableJms
public class RateLockServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RateLockServiceApplication.class, args);
    }
}
