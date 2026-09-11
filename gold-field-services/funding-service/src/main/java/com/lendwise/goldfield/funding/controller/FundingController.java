package com.lendwise.goldfield.funding.controller;

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
@RequestMapping("/api/funding")
@Slf4j
public class FundingController {

    @PostMapping("/disburse")
    @ResponseStatus(HttpStatus.OK)
    public Mono<FundingDisbursementDto> disburseFunds(@RequestBody FundingRequest request) {
        log.info("Disbursing loan funding for loanId={}, amount={}", request.getLoanId(), request.getAmount());
        FundingDisbursementDto disbursement = FundingDisbursementDto.builder()
                .disbursementId(UUID.randomUUID().toString())
                .loanId(request.getLoanId())
                .amount(request.getAmount())
                .escrowAccount(request.getEscrowAccount())
                .status("FUNDED")
                .wireConfirmation("FEDWIRE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .disbursedAt(Instant.now())
                .build();
        return Mono.just(disbursement);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FundingRequest {
        private String loanId;
        private double amount;
        private String escrowAccount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FundingDisbursementDto {
        private String disbursementId;
        private String loanId;
        private double amount;
        private String escrowAccount;
        private String status;
        private String wireConfirmation;
        private Instant disbursedAt;
    }
}
