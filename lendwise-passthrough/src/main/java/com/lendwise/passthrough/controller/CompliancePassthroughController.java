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
 * Pass-through controller for Compliance operations.
 * Called by SOA ComplianceComposite via binding.rest
 */
@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Compliance Pass-through", description = "Routes compliance requests to Gold Field")
public class CompliancePassthroughController {

    private final GoldFieldRoutingService routingService;

    @PostMapping
    @Operation(summary = "Run compliance check", description = "Routes compliance check request")
    public Mono<PassthroughResponse> createComplianceCheck(
            @RequestBody Object complianceRequest,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("compliance-service", "/api/compliance", complianceRequest, headerMap);
    }

    @GetMapping("/loan/{loanId}")
    @Operation(summary = "Get compliance status", description = "Gets compliance status for loan")
    public Mono<PassthroughResponse> getComplianceStatus(
            @PathVariable String loanId,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("compliance-service", "/api/compliance/loan/" + loanId, null, headerMap);
    }

    @PostMapping("/trid/validate")
    @Operation(summary = "Validate TRID timing", description = "Validates TRID compliance timing")
    public Mono<PassthroughResponse> validateTRID(
            @RequestBody Object tridRequest,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("compliance-service", "/api/compliance/trid/validate", tridRequest, headerMap);
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
