package com.lendwise.esign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableReactiveMongoAuditing
@EnableJms
public class ESignServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ESignServiceApplication.class, args);
    }
}
