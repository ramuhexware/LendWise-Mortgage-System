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
 * Pass-through controller for Document operations.
 * Called by SOA DocumentProcessingComposite via binding.rest
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document Pass-through", description = "Routes document requests to Gold Field document-service")
public class DocumentPassthroughController {

    private final GoldFieldRoutingService routingService;

    @PostMapping
    @Operation(summary = "Process document", description = "Routes document for OCR processing")
    public Mono<PassthroughResponse> processDocument(
            @RequestBody Object documentRequest,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("document-service", "/api/documents", documentRequest, headerMap);
    }

    @GetMapping("/loan/{loanId}/checklist")
    @Operation(summary = "Get document checklist", description = "Gets document checklist status")
    public Mono<PassthroughResponse> getChecklist(
            @PathVariable String loanId,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("document-service", "/api/documents/loan/" + loanId + "/checklist", null, headerMap);
    }

    @PostMapping("/{documentId}/classify")
    @Operation(summary = "Classify document", description = "Routes document for AI classification")
    public Mono<PassthroughResponse> classifyDocument(
            @PathVariable String documentId,
            @RequestBody Object classifyRequest,
            @RequestHeader HttpHeaders headers) {

        Map<String, String> headerMap = extractHeaders(headers);
        return routingService.route("document-service", "/api/documents/" + documentId + "/classify", classifyRequest, headerMap);
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
