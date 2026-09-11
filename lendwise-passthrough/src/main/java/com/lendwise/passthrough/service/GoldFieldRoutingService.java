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
import org.springframework.http.HttpMethod;
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
     * Route a request to the appropriate Gold Field service with auto-inferred HTTP method.
     */
    public Mono<PassthroughResponse> route(String serviceName, String path, Object payload, Map<String, String> headers) {
        HttpMethod method = (payload == null) ? HttpMethod.GET : HttpMethod.POST;
        return route(method, serviceName, path, payload, headers);
    }

    /**
     * Route a request to the appropriate Gold Field service with specified HTTP method.
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    @TimeLimiter(name = CIRCUIT_BREAKER_NAME)
    public Mono<PassthroughResponse> route(HttpMethod method, String serviceName, String path, Object payload, Map<String, String> headers) {
        String correlationId = headers.getOrDefault("X-Correlation-ID", UUID.randomUUID().toString());
        String baseUrl = resolveServiceUrl(serviceName);
        String fullUrl = baseUrl + path;

        log.info("Routing {} request to {} - correlationId={}", method, fullUrl, correlationId);

        Instant startTime = Instant.now();

        WebClient.RequestBodySpec requestSpec;
        if (HttpMethod.GET.equals(method)) {
            requestSpec = (WebClient.RequestBodySpec) webClient.get().uri(fullUrl);
        } else if (HttpMethod.PUT.equals(method)) {
            requestSpec = webClient.put().uri(fullUrl);
        } else if (HttpMethod.DELETE.equals(method)) {
            requestSpec = (WebClient.RequestBodySpec) webClient.delete().uri(fullUrl);
        } else {
            requestSpec = webClient.post().uri(fullUrl);
        }

        requestSpec.header("X-Correlation-ID", correlationId)
                   .header("X-Source", "SOA-Passthrough");

        headers.forEach(requestSpec::header);

        WebClient.ResponseSpec responseSpec;
        if ((HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method)) && payload != null) {
            responseSpec = requestSpec.contentType(MediaType.APPLICATION_JSON)
                                      .bodyValue(payload)
                                      .retrieve();
        } else {
            responseSpec = requestSpec.retrieve();
        }

        return responseSpec.bodyToMono(Object.class)
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
        return fallback(HttpMethod.POST, serviceName, path, payload, headers, t);
    }

    /**
     * Overloaded fallback for explicit HttpMethod.
     */
    public Mono<PassthroughResponse> fallback(HttpMethod method, String serviceName, String path, Object payload,
                                               Map<String, String> headers, Throwable t) {
        String correlationId = headers.getOrDefault("X-Correlation-ID", "UNKNOWN");
        log.error("Circuit breaker fallback for {} {} - correlationId={}, error={}",
                  method, serviceName, correlationId, t.getMessage());

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
            case "kyc", "kyc-service" -> servicesConfig.getKycService();
            case "document", "document-service" -> servicesConfig.getDocumentService();
            case "credit", "credit-service" -> servicesConfig.getCreditService();
            case "underwriting", "underwriting-service" -> servicesConfig.getUnderwritingService();
            case "compliance", "compliance-service" -> servicesConfig.getComplianceService();
            case "pricing", "pricing-service" -> servicesConfig.getPricingService();
            case "ratelock", "ratelock-service" -> servicesConfig.getRatelockService();
            case "amortization", "amortization-service" -> servicesConfig.getAmortizationService();
            case "closing", "closing-service" -> servicesConfig.getClosingService();
            case "esign", "esign-service" -> servicesConfig.getEsignService();
            case "funding", "funding-service" -> servicesConfig.getFundingService();
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
