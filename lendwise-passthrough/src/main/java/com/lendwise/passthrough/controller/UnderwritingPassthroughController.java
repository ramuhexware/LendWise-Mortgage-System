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
 * Pass-through controller for Underwriting operations.
 * Called by SOA UnderwritingComposite via binding.rest
 */
@RestController
@RequestMapping("/api/underwriting")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Underwriting Pass-through", description = "Routes underwriting requests to Gold Field")
public class UnderwritingPassthroughController {

    private final GoldFieldRoutingService routingService;

    @PostMapping
    @Operation(summary = "Submit for underwriting", description = "Routes underwriting submission")
    public Mono<PassthroughResponse> createUnderwritingDecision(
            @RequestBody Object underwritingRequest,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("underwriting-service", "/api/underwriting", underwritingRequest, headerMap);
    }

    @GetMapping("/{decisionId}")
    @Operation(summary = "Get underwriting decision", description = "Gets underwriting decision details")
    public Mono<PassthroughResponse> getDecision(
            @PathVariable String decisionId,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("underwriting-service", "/api/underwriting/" + decisionId, null, headerMap);
    }

    @PostMapping("/{decisionId}/conditions")
    @Operation(summary = "Add underwriting condition", description = "Adds condition to decision")
    public Mono<PassthroughResponse> addCondition(
            @PathVariable String decisionId,
            @RequestBody Object conditionRequest,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("underwriting-service", "/api/underwriting/" + decisionId + "/conditions", conditionRequest, headerMap);
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
