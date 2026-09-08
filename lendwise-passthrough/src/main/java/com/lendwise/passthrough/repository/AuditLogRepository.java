package com.lendwise.passthrough.repository;

import com.lendwise.passthrough.model.AuditLog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;

/**
 * Reactive MongoDB repository for audit logs.
 */
@Repository
public interface AuditLogRepository extends ReactiveMongoRepository<AuditLog, String> {

    Flux<AuditLog> findByCorrelationId(String correlationId);

    Flux<AuditLog> findByServiceNameAndTimestampBetween(String serviceName, Instant start, Instant end);

    Flux<AuditLog> findByStatus(String status);
}
