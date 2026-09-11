package com.lendwise.goldfield.document.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@Slf4j
public class DocumentController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DocumentDto> uploadDocument(@RequestBody DocumentDto request) {
        log.info("Processing document upload for loanId={}", request.getLoanId());
        if (request.getDocumentId() == null) {
            request.setDocumentId(UUID.randomUUID().toString());
        }
        request.setStatus("PROCESSED");
        request.setUploadedAt(Instant.now());
        return Mono.just(request);
    }

    @GetMapping("/loan/{loanId}/checklist")
    public Mono<DocumentChecklistDto> getChecklist(@PathVariable String loanId) {
        log.info("Fetching document checklist for loanId={}", loanId);
        List<DocumentItem> items = List.of(
                new DocumentItem("DOC-1", "W2_2023", "INCOME", "RECEIVED"),
                new DocumentItem("DOC-2", "PAYSTUB_RECENT", "INCOME", "RECEIVED"),
                new DocumentItem("DOC-3", "BANK_STATEMENT", "ASSET", "PENDING")
        );
        return Mono.just(new DocumentChecklistDto(loanId, items, 66.7));
    }

    @PostMapping("/{documentId}/classify")
    public Mono<DocumentDto> classifyDocument(@PathVariable String documentId, @RequestBody DocumentDto request) {
        log.info("Classifying document id={}", documentId);
        request.setDocumentId(documentId);
        request.setDocumentType("W2_TAX_FORM");
        request.setStatus("CLASSIFIED");
        return Mono.just(request);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentDto {
        private String documentId;
        private String loanId;
        private String documentType;
        private String fileName;
        private String status;
        private Instant uploadedAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentChecklistDto {
        private String loanId;
        private List<DocumentItem> items;
        private double completionPercentage;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentItem {
        private String id;
        private String name;
        private String category;
        private String status;
    }
}
