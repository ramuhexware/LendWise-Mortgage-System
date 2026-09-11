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
 * Pass-through controller for KYC operations.
 * Called by SOA BorrowerIntakeComposite via binding.rest
 */
@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "KYC Pass-through", description = "Routes KYC requests to Gold Field kyc-service")
public class KycPassthroughController {

    private final GoldFieldRoutingService routingService;

    @PostMapping("/verify")
    @Operation(summary = "Verify KYC identity", description = "Routes KYC identity verification to Gold Field")
    public Mono<PassthroughResponse> verifyIdentity(
            @RequestBody Object kycRequest,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("kyc-service", "/api/kyc/verify", kycRequest, headerMap);
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
