package com.lendwise.goldfield.pricing.controller;

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
@RequestMapping("/api/pricing")
@Slf4j
public class PricingController {

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<PricingResponseDto> calculatePricing(@RequestBody PricingRequestDto request) {
        log.info("Calculating interest rates & pricing for loanId={}", request.getLoanId());
        PricingResponseDto response = PricingResponseDto.builder()
                .pricingId(UUID.randomUUID().toString())
                .loanId(request.getLoanId())
                .baseRate(6.25)
                .finalRate(6.50)
                .apr(6.62)
                .discountPoints(0.0)
                .originationFee(1200.0)
                .monthlyPayment(2528.27)
                .calculatedAt(Instant.now())
                .build();
        return Mono.just(response);
    }

    @PostMapping("/lock")
    public Mono<RateLockResponseDto> lockRate(@RequestBody RateLockRequestDto request) {
        log.info("Locking rate for loanId={}", request.getLoanId());
        RateLockResponseDto response = RateLockResponseDto.builder()
                .lockId(UUID.randomUUID().toString())
                .loanId(request.getLoanId())
                .lockedRate(6.50)
                .expirationDate(Instant.now().plusSeconds(30 * 24 * 3600))
                .status("LOCKED")
                .lockedAt(Instant.now())
                .build();
        return Mono.just(response);
    }

    @GetMapping("/loan/{loanId}")
    public Mono<PricingResponseDto> getPricingDetails(@PathVariable String loanId) {
        log.info("Fetching pricing details for loanId={}", loanId);
        PricingResponseDto response = PricingResponseDto.builder()
                .pricingId(UUID.randomUUID().toString())
                .loanId(loanId)
                .baseRate(6.25)
                .finalRate(6.50)
                .apr(6.62)
                .discountPoints(0.0)
                .originationFee(1200.0)
                .monthlyPayment(2528.27)
                .calculatedAt(Instant.now())
                .build();
        return Mono.just(response);
    }

    @GetMapping("/loan/{loanId}/scenarios")
    public Mono<List<PricingResponseDto>> getPricingScenarios(@PathVariable String loanId) {
        log.info("Fetching pricing scenarios for loanId={}", loanId);
        PricingResponseDto opt1 = PricingResponseDto.builder()
                .pricingId(UUID.randomUUID().toString())
                .loanId(loanId)
                .baseRate(6.25)
                .finalRate(6.25)
                .apr(6.38)
                .discountPoints(1.0)
                .originationFee(1200.0)
                .monthlyPayment(2463.12)
                .calculatedAt(Instant.now())
                .build();

        PricingResponseDto opt2 = PricingResponseDto.builder()
                .pricingId(UUID.randomUUID().toString())
                .loanId(loanId)
                .baseRate(6.25)
                .finalRate(6.50)
                .apr(6.62)
                .discountPoints(0.0)
                .originationFee(1200.0)
                .monthlyPayment(2528.27)
                .calculatedAt(Instant.now())
                .build();

        return Mono.just(List.of(opt1, opt2));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingRequestDto {
        private String loanId;
        private double loanAmount;
        private int creditScore;
        private double ltv;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingResponseDto {
        private String pricingId;
        private String loanId;
        private double baseRate;
        private double finalRate;
        private double apr;
        private double discountPoints;
        private double originationFee;
        private double monthlyPayment;
        private Instant calculatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLockRequestDto {
        private String loanId;
        private double rate;
        private int lockPeriodDays;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLockResponseDto {
        private String lockId;
        private String loanId;
        private double lockedRate;
        private Instant expirationDate;
        private String status;
        private Instant lockedAt;
    }
}
