package com.lendwise.goldfield.ratelock.controller;

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
@RequestMapping("/api/ratelock")
@Slf4j
public class RateLockController {

    @PostMapping("/lock")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RateLockConfirmationDto> lockRate(@RequestBody RateLockRequest request) {
        log.info("Processing rate lock for loanId={}", request.getLoanId());
        RateLockConfirmationDto confirmation = RateLockConfirmationDto.builder()
                .lockId(UUID.randomUUID().toString())
                .loanId(request.getLoanId())
                .rate(request.getRate())
                .days(request.getDays())
                .expiration(Instant.now().plusSeconds(request.getDays() * 24L * 3600L))
                .status("CONFIRMED")
                .confirmedAt(Instant.now())
                .build();
        return Mono.just(confirmation);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLockRequest {
        private String loanId;
        private double rate;
        private int days;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLockConfirmationDto {
        private String lockId;
        private String loanId;
        private double rate;
        private int days;
        private Instant expiration;
        private String status;
        private Instant confirmedAt;
    }
}
