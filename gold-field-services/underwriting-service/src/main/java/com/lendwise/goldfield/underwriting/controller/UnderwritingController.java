package com.lendwise.goldfield.underwriting.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/underwriting")
@Slf4j
public class UnderwritingController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UnderwritingDecisionDto> createDecision(@RequestBody UnderwritingDecisionDto request) {
        log.info("Evaluating underwriting for loanId={}", request.getLoanId());
        String decisionId = UUID.randomUUID().toString();

        List<ConditionDto> defaultConditions = List.of(
                new ConditionDto(UUID.randomUUID().toString(), "Proof of Homeowners Insurance", "PRIOR_TO_DOCS", "OPEN"),
                new ConditionDto(UUID.randomUUID().toString(), "Verify Bank Statement Liquid Funds", "PRIOR_TO_CLOSING", "OPEN")
        );

        UnderwritingDecisionDto decision = UnderwritingDecisionDto.builder()
                .decisionId(decisionId)
                .loanId(request.getLoanId())
                .status("APPROVED")
                .recommendation("APPROVE_ELIGIBLE")
                .ausEngine("LendWise-AUS-v2")
                .calculatedDti(34.5)
                .calculatedLtv(75.0)
                .conditions(defaultConditions)
                .evaluatedAt(Instant.now())
                .build();

        return Mono.just(decision);
    }

    @GetMapping("/{decisionId}")
    public Mono<UnderwritingDecisionDto> getDecision(@PathVariable String decisionId) {
        log.info("Fetching decision decisionId={}", decisionId);
        UnderwritingDecisionDto decision = UnderwritingDecisionDto.builder()
                .decisionId(decisionId)
                .loanId("LOAN-1001")
                .status("APPROVED")
                .recommendation("APPROVE_ELIGIBLE")
                .ausEngine("LendWise-AUS-v2")
                .calculatedDti(34.5)
                .calculatedLtv(75.0)
                .conditions(List.of())
                .evaluatedAt(Instant.now())
                .build();
        return Mono.just(decision);
    }

    @GetMapping("/loan/{loanId}")
    public Mono<UnderwritingDecisionDto> getDecisionByLoanId(@PathVariable String loanId) {
        log.info("Fetching decision for loanId={}", loanId);
        UnderwritingDecisionDto decision = UnderwritingDecisionDto.builder()
                .decisionId(UUID.randomUUID().toString())
                .loanId(loanId)
                .status("APPROVED")
                .recommendation("APPROVE_ELIGIBLE")
                .ausEngine("LendWise-AUS-v2")
                .calculatedDti(34.5)
                .calculatedLtv(75.0)
                .conditions(List.of())
                .evaluatedAt(Instant.now())
                .build();
        return Mono.just(decision);
    }

    @PostMapping("/{decisionId}/conditions")
    public Mono<UnderwritingDecisionDto> addCondition(@PathVariable String decisionId, @RequestBody ConditionDto condition) {
        log.info("Adding condition to decisionId={}", decisionId);
        condition.setConditionId(UUID.randomUUID().toString());
        condition.setStatus("OPEN");
        
        UnderwritingDecisionDto decision = UnderwritingDecisionDto.builder()
                .decisionId(decisionId)
                .loanId("LOAN-1001")
                .status("APPROVED")
                .conditions(List.of(condition))
                .evaluatedAt(Instant.now())
                .build();
        return Mono.just(decision);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnderwritingDecisionDto {
        private String decisionId;
        private String loanId;
        private String status;
        private String recommendation;
        private String ausEngine;
        private double calculatedDti;
        private double calculatedLtv;
        private List<ConditionDto> conditions;
        private Instant evaluatedAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConditionDto {
        private String conditionId;
        private String description;
        private String category;
        private String status;
    }
}
