package com.lendwise.passthrough.service;

import com.lendwise.passthrough.config.GoldFieldServicesConfig;
import com.lendwise.passthrough.model.PassthroughRequest;
import com.lendwise.passthrough.model.PassthroughResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Service for routing requests from SOA to Gold Field microservices.
 * Applies circuit breaker, retry, and timeout policies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoldFieldRoutingService {

    private final WebClient webClient;
    private final GoldFieldServicesConfig servicesConfig;
    private final AuditService auditService;

    private static final String CIRCUIT_BREAKER_NAME = "goldfield";

    /**
     * Route a request to the appropriate Gold Field service.
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    @TimeLimiter(name = CIRCUIT_BREAKER_NAME)
    public Mono<PassthroughResponse> route(String serviceName, String path, Object payload, Map<String, String> headers) {
        String correlationId = headers.getOrDefault("X-Correlation-ID", UUID.randomUUID().toString());
        String baseUrl = resolveServiceUrl(serviceName);
        String fullUrl = baseUrl + path;

        log.info("Routing request to {} - correlationId={}", fullUrl, correlationId);

        Instant startTime = Instant.now();

        return webClient.post()
                .uri(fullUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Correlation-ID", correlationId)
                .header("X-Source", "SOA-Passthrough")
                .headers(h -> headers.forEach(h::add))
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Object.class)
                .map(response -> {
                    PassthroughResponse passthroughResponse = new PassthroughResponse();
                    passthroughResponse.setCorrelationId(correlationId);
                    passthroughResponse.setStatus("SUCCESS");
                    passthroughResponse.setData(response);
                    passthroughResponse.setDurationMs(Instant.now().toEpochMilli() - startTime.toEpochMilli());
                    return passthroughResponse;
                })
                .doOnSuccess(response -> auditService.logSuccess(serviceName, path, correlationId, response.getDurationMs()))
                .doOnError(error -> auditService.logError(serviceName, path, correlationId, error));
    }

    /**
     * Fallback when circuit breaker is open or all retries exhausted.
     */
    public Mono<PassthroughResponse> fallback(String serviceName, String path, Object payload,
                                               Map<String, String> headers, Throwable t) {
        String correlationId = headers.getOrDefault("X-Correlation-ID", "UNKNOWN");
        log.error("Circuit breaker fallback for {} - correlationId={}, error={}",
                  serviceName, correlationId, t.getMessage());

        PassthroughResponse response = new PassthroughResponse();
        response.setCorrelationId(correlationId);
        response.setStatus("FALLBACK");
        response.setErrorCode("SERVICE_UNAVAILABLE");
        response.setErrorMessage("Gold Field service temporarily unavailable: " + t.getMessage());

        return Mono.just(response);
    }

    /**
     * Resolve service name to URL.
     */
    private String resolveServiceUrl(String serviceName) {
        return switch (serviceName.toLowerCase()) {
            case "borrower", "borrower-service" -> servicesConfig.getBorrowerService();
            case "document", "document-service" -> servicesConfig.getDocumentService();
            case "underwriting", "underwriting-service" -> servicesConfig.getUnderwritingService();
            case "compliance", "compliance-service" -> servicesConfig.getComplianceService();
            case "pricing", "pricing-service" -> servicesConfig.getPricingService();
            case "closing", "closing-service" -> servicesConfig.getClosingService();
            case "notification", "notification-service" -> servicesConfig.getNotificationService();
            case "valuation", "valuation-service" -> servicesConfig.getValuationService();
            case "title", "title-service" -> servicesConfig.getTitleService();
            case "investor", "investor-service" -> servicesConfig.getInvestorService();
            case "analytics", "analytics-service" -> servicesConfig.getAnalyticsService();
            case "audit", "audit-service" -> servicesConfig.getAuditService();
            default -> throw new IllegalArgumentException("Unknown service: " + serviceName);
        };
    }
}
