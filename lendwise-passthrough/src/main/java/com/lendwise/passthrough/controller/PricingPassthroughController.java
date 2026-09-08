package com.lendwise.passthrough.controller;

import com.lendwise.passthrough.model.PassthroughResponse;
import com.lendwise.passthrough.service.GoldFieldRoutingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Pass-through controller for Pricing operations.
 * Called by SOA PricingEngineComposite via binding.rest
 */
@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pricing Pass-through", description = "Routes pricing requests to Gold Field")
public class PricingPassthroughController {

    private final GoldFieldRoutingService routingService;

    @PostMapping
    @Operation(summary = "Calculate pricing", description = "Routes pricing calculation request")
    public Mono<PassthroughResponse> createPricing(
            @RequestBody Object pricingRequest,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("pricing-service", "/api/pricing", pricingRequest, headerMap);
    }

    @PostMapping("/lock")
    @Operation(summary = "Lock rate", description = "Routes rate lock request")
    public Mono<PassthroughResponse> lockRate(
            @RequestBody Object lockRequest,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("pricing-service", "/api/pricing/lock", lockRequest, headerMap);
    }

    @GetMapping("/loan/{loanId}/scenarios")
    @Operation(summary = "Get pricing scenarios", description = "Gets pricing scenarios for loan")
    public Mono<PassthroughResponse> getScenarios(
            @PathVariable String loanId,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("pricing-service", "/api/pricing/loan/" + loanId + "/scenarios", null, headerMap);
    }

    private Map<String, String> extractHeaders(HttpHeaders headers) {
        Map<String, String> headerMap = new HashMap<>();
        if (headers.containsKey("X-Correlation-ID")) {
            headerMap.put("X-Correlation-ID", headers.getFirst("X-Correlation-ID"));
        }
        if (headers.containsKey("X-Loan-ID")) {
            headerMap.put("X-Loan-ID", headers.getFirst("X-Loan-ID"));
        }
        return headerMap;
    }
}
