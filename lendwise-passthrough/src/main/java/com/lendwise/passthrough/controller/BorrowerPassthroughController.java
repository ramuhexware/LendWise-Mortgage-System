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
 * Pass-through controller for Borrower operations.
 * Called by SOA BorrowerIntakeComposite via binding.rest
 */
@RestController
@RequestMapping("/api/borrowers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Borrower Pass-through", description = "Routes borrower requests to Gold Field borrower-service")
public class BorrowerPassthroughController {

    private final GoldFieldRoutingService routingService;

    @PostMapping
    @Operation(summary = "Create borrower", description = "Routes borrower creation to Gold Field")
    public Mono<PassthroughResponse> createBorrower(
            @RequestBody Object borrowerRequest,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("borrower-service", "/api/borrowers", borrowerRequest, headerMap);
    }

    @GetMapping("/{borrowerId}")
    @Operation(summary = "Get borrower", description = "Routes borrower retrieval to Gold Field")
    public Mono<PassthroughResponse> getBorrower(
            @PathVariable String borrowerId,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("borrower-service", "/api/borrowers/" + borrowerId, null, headerMap);
    }

    @PostMapping("/{borrowerId}/dti")
    @Operation(summary = "Calculate DTI", description = "Routes DTI calculation to Gold Field")
    public Mono<PassthroughResponse> calculateDTI(
            @PathVariable String borrowerId,
            @RequestBody Object dtiRequest,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("borrower-service", "/api/borrowers/" + borrowerId + "/dti", dtiRequest, headerMap);
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
