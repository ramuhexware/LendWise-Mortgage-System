package com.lendwise.passthrough.service;

import com.lendwise.passthrough.model.AuditLog;
import com.lendwise.passthrough.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Service for logging request/response audit trails to MongoDB.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void logSuccess(String serviceName, String path, String correlationId, long durationMs) {
        AuditLog auditLog = AuditLog.builder()
                .correlationId(correlationId)
                .serviceName(serviceName)
                .path(path)
                .status("SUCCESS")
                .durationMs(durationMs)
                .timestamp(Instant.now())
                .build();

        auditLogRepository.save(auditLog)
                .doOnSuccess(saved -> log.debug("Audit log saved: {}", saved.getId()))
                .doOnError(error -> log.error("Failed to save audit log", error))
                .subscribe();
    }

    public void logError(String serviceName, String path, String correlationId, Throwable error) {
        AuditLog auditLog = AuditLog.builder()
                .correlationId(correlationId)
                .serviceName(serviceName)
                .path(path)
                .status("ERROR")
                .errorMessage(error.getMessage())
                .errorClass(error.getClass().getSimpleName())
                .timestamp(Instant.now())
                .build();

        auditLogRepository.save(auditLog)
                .doOnError(e -> log.error("Failed to save error audit log", e))
                .subscribe();
    }

    public Mono<AuditLog> logRequest(String serviceName, String path, String correlationId,
                                      String method, Object requestBody) {
        AuditLog auditLog = AuditLog.builder()
                .correlationId(correlationId)
                .serviceName(serviceName)
                .path(path)
                .method(method)
                .status("PENDING")
                .timestamp(Instant.now())
                .build();

        return auditLogRepository.save(auditLog);
    }
}
