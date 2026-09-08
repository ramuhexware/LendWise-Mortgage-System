package com.lendwise.passthrough.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Audit log document for tracking all pass-through requests.
 */
@Document(collection = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    private String id;

    @Indexed
    private String correlationId;

    @Indexed
    private String serviceName;

    private String path;
    private String method;
    private String status;
    private Long durationMs;
    private String errorMessage;
    private String errorClass;

    @CreatedDate
    @Indexed
    private Instant timestamp;
}
