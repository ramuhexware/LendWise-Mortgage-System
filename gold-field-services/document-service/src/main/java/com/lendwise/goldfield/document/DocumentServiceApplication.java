package com.lendwise.goldfield.document;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

/**
 * Gold Field Document Service
 *
 * Microservice for document management including:
 * - Document upload and storage
 * - OCR processing results
 * - AI classification results
 * - Document checklist management
 * - Cross-document validation
 */
@SpringBootApplication
@EnableReactiveMongoAuditing
public class DocumentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentServiceApplication.class, args);
    }
}
