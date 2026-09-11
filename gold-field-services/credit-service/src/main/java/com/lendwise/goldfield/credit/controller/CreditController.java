package com.lendwise.goldfield.credit.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/credit")
@Slf4j
public class CreditController {

    @PostMapping("/inquiry")
    @ResponseStatus(HttpStatus.OK)
    public Mono<CreditReportDto> requestCreditInquiry(@RequestBody CreditInquiryRequest request) {
        log.info("Processing credit inquiry for borrowerId={}", request.getBorrowerId());
        CreditReportDto report = CreditReportDto.builder()
                .reportId(UUID.randomUUID().toString())
                .borrowerId(request.getBorrowerId())
                .creditScore(740)
                .bureau("TRI_MERGE")
                .totalDebt(25000.0)
                .monthlyDebtPayments(650.0)
                .retrievedAt(Instant.now())
                .build();
        return Mono.just(report);
    }

    @GetMapping("/report/{ssn}")
    public Mono<CreditReportDto> getCreditReportBySsn(@PathVariable String ssn) {
        log.info("Retrieving credit report by SSN");
        CreditReportDto report = CreditReportDto.builder()
                .reportId(UUID.randomUUID().toString())
                .creditScore(740)
                .bureau("TRI_MERGE")
                .totalDebt(25000.0)
                .monthlyDebtPayments(650.0)
                .retrievedAt(Instant.now())
                .build();
        return Mono.just(report);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreditInquiryRequest {
        private String borrowerId;
        private String ssn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreditReportDto {
        private String reportId;
        private String borrowerId;
        private int creditScore;
        private String bureau;
        private double totalDebt;
        private double monthlyDebtPayments;
        private Instant retrievedAt;
    }
}
