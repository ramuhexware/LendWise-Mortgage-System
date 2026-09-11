package com.lendwise.goldfield.kyc.controller;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/kyc")
@Slf4j
public class KycController {

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    public Mono<KycVerificationResponse> verifyIdentity(@RequestBody KycVerificationRequest request) {
        log.info("Received KYC verification request for ssn={}", request.getSsn() != null ? "***-**-****" : "null");

        KycVerificationResponse response = KycVerificationResponse.builder()
                .verificationId(UUID.randomUUID().toString())
                .status("VERIFIED")
                .ofacPassed(true)
                .identityScore(95)
                .timestamp(Instant.now())
                .build();

        return Mono.just(response);
    }

    @Data
    public static class KycVerificationRequest {
        private String borrowerId;
        private String firstName;
        private String lastName;
        private String ssn;
        private String dateOfBirth;
    }

    @Data
    @Builder
    public static class KycVerificationResponse {
        private String verificationId;
        private String status;
        private boolean ofacPassed;
        private int identityScore;
        private Instant timestamp;
    }
}
