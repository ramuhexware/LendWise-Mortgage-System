package com.lendwise.goldfield.esign.controller;

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
@RequestMapping
@Slf4j
public class ESignController {

    @PostMapping("/api/esign/package")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ESignEnvelopeDto> createPackage(@RequestBody ESignPackageRequest request) {
        log.info("Creating e-signature package for loanId={}", request.getLoanId());
        ESignEnvelopeDto envelope = ESignEnvelopeDto.builder()
                .envelopeId(UUID.randomUUID().toString())
                .loanId(request.getLoanId())
                .status("SENT")
                .signerEmail(request.getSignerEmail())
                .signingUrl("http://esign.goldfield.svc.cluster.local:8080/sign/" + UUID.randomUUID())
                .createdAt(Instant.now())
                .build();
        return Mono.just(envelope);
    }

    @PostMapping("/api/envelopes")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ESignEnvelopeDto> createEnvelope(@RequestBody ESignPackageRequest request) {
        log.info("Creating e-signature envelope for loanId={}", request.getLoanId());
        ESignEnvelopeDto envelope = ESignEnvelopeDto.builder()
                .envelopeId(UUID.randomUUID().toString())
                .loanId(request.getLoanId())
                .status("SENT")
                .signerEmail(request.getSignerEmail())
                .signingUrl("http://esign.goldfield.svc.cluster.local:8080/sign/" + UUID.randomUUID())
                .createdAt(Instant.now())
                .build();
        return Mono.just(envelope);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ESignPackageRequest {
        private String loanId;
        private String signerName;
        private String signerEmail;
        private String documentType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ESignEnvelopeDto {
        private String envelopeId;
        private String loanId;
        private String status;
        private String signerEmail;
        private String signingUrl;
        private Instant createdAt;
    }
}
