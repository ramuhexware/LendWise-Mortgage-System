package com.lendwise.underwriting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableReactiveMongoAuditing
public class UnderwritingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnderwritingServiceApplication.class, args);
    }
}
