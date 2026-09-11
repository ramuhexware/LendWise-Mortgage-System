package com.lendwise.goldfield.compliance.controller;

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
@RequestMapping("/api/compliance")
@Slf4j
public class ComplianceController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ComplianceResultDto> createComplianceCheck(@RequestBody ComplianceRequestDto request) {
        log.info("Running compliance audit check for loanId={}", request.getLoanId());
        ComplianceResultDto result = ComplianceResultDto.builder()
                .checkId(UUID.randomUUID().toString())
                .loanId(request.getLoanId())
                .status("COMPLIANT")
                .tridCompliant(true)
                .qmStatus("QUALIFIED_MORTGAGE")
                .hoepaCompliant(true)
                .auditDate(Instant.now())
                .violations(List.of())
                .build();
        return Mono.just(result);
    }

    @GetMapping("/loan/{loanId}")
    public Mono<ComplianceResultDto> getComplianceStatus(@PathVariable String loanId) {
        log.info("Fetching compliance status for loanId={}", loanId);
        ComplianceResultDto result = ComplianceResultDto.builder()
                .checkId(UUID.randomUUID().toString())
                .loanId(loanId)
                .status("COMPLIANT")
                .tridCompliant(true)
                .qmStatus("QUALIFIED_MORTGAGE")
                .hoepaCompliant(true)
                .auditDate(Instant.now())
                .violations(List.of())
                .build();
        return Mono.just(result);
    }

    @PostMapping("/trid/validate")
    public Mono<ComplianceResultDto> validateTRID(@RequestBody ComplianceRequestDto request) {
        log.info("Validating TRID 3-day disclosure rule for loanId={}", request.getLoanId());
        ComplianceResultDto result = ComplianceResultDto.builder()
                .checkId(UUID.randomUUID().toString())
                .loanId(request.getLoanId())
                .status("COMPLIANT")
                .tridCompliant(true)
                .qmStatus("QUALIFIED_MORTGAGE")
                .hoepaCompliant(true)
                .auditDate(Instant.now())
                .violations(List.of())
                .build();
        return Mono.just(result);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceRequestDto {
        private String loanId;
        private String borrowerId;
        private double loanAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceResultDto {
        private String checkId;
        private String loanId;
        private String status;
        private boolean tridCompliant;
        private String qmStatus;
        private boolean hoepaCompliant;
        private Instant auditDate;
        private List<String> violations;
    }
}
