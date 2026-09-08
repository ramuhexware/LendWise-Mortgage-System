package com.lendwise.compliance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableReactiveMongoAuditing
public class ComplianceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComplianceServiceApplication.class, args);
    }
}
