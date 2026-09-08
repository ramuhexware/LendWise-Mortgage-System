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
 * Pass-through controller for Closing Disclosure operations.
 * Called by SOA ClosingDisclosureComposite via binding.rest
 */
@RestController
@RequestMapping("/api/closing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Closing Pass-through", description = "Routes closing requests to Gold Field")
public class ClosingPassthroughController {

    private final GoldFieldRoutingService routingService;

    @PostMapping
    @Operation(summary = "Generate CD", description = "Routes CD generation request")
    public Mono<PassthroughResponse> createClosingDisclosure(
            @RequestBody Object closingRequest,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("closing-service", "/api/closing", closingRequest, headerMap);
    }

    @GetMapping("/{cdId}")
    @Operation(summary = "Get CD", description = "Gets closing disclosure details")
    public Mono<PassthroughResponse> getClosingDisclosure(
            @PathVariable String cdId,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("closing-service", "/api/closing/" + cdId, null, headerMap);
    }

    @PostMapping("/{cdId}/deliver")
    @Operation(summary = "Deliver CD", description = "Routes CD delivery request")
    public Mono<PassthroughResponse> deliverCD(
            @PathVariable String cdId,
            @RequestBody Object deliveryRequest,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("closing-service", "/api/closing/" + cdId + "/deliver", deliveryRequest, headerMap);
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
