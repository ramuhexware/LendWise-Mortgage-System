package com.lendwise.goldfield.closing.controller;

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
@RequestMapping("/api/closing")
@Slf4j
public class ClosingController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ClosingDisclosureDto> createClosingDisclosure(@RequestBody ClosingDisclosureDto request) {
        log.info("Generating Closing Disclosure for loanId={}", request.getLoanId());
        String cdId = UUID.randomUUID().toString();
        request.setCdId(cdId);
        request.setStatus("GENERATED");
        request.setGeneratedAt(Instant.now());
        request.setCashToClose(45200.00);
        return Mono.just(request);
    }

    @GetMapping("/{cdId}")
    public Mono<ClosingDisclosureDto> getClosingDisclosure(@PathVariable String cdId) {
        log.info("Fetching CD cdId={}", cdId);
        ClosingDisclosureDto cd = ClosingDisclosureDto.builder()
                .cdId(cdId)
                .loanId("LOAN-1001")
                .borrowerName("John Doe")
                .status("GENERATED")
                .loanAmount(400000.0)
                .interestRate(6.50)
                .cashToClose(45200.00)
                .generatedAt(Instant.now())
                .build();
        return Mono.just(cd);
    }

    @GetMapping("/loan/{loanId}")
    public Mono<ClosingDisclosureDto> getClosingDisclosureByLoanId(@PathVariable String loanId) {
        log.info("Fetching CD for loanId={}", loanId);
        ClosingDisclosureDto cd = ClosingDisclosureDto.builder()
                .cdId(UUID.randomUUID().toString())
                .loanId(loanId)
                .borrowerName("John Doe")
                .status("GENERATED")
                .loanAmount(400000.0)
                .interestRate(6.50)
                .cashToClose(45200.00)
                .generatedAt(Instant.now())
                .build();
        return Mono.just(cd);
    }

    @PostMapping("/{cdId}/deliver")
    public Mono<ClosingDisclosureDto> deliverCD(@PathVariable String cdId, @RequestBody DeliveryRequestDto deliveryRequest) {
        log.info("Delivering CD cdId={} via method={}", cdId, deliveryRequest.getDeliveryMethod());
        ClosingDisclosureDto cd = ClosingDisclosureDto.builder()
                .cdId(cdId)
                .loanId("LOAN-1001")
                .status("DELIVERED")
                .deliveryMethod(deliveryRequest.getDeliveryMethod())
                .deliveredAt(Instant.now())
                .build();
        return Mono.just(cd);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClosingDisclosureDto {
        private String cdId;
        private String loanId;
        private String borrowerName;
        private String status;
        private double loanAmount;
        private double interestRate;
        private double cashToClose;
        private String deliveryMethod;
        private Instant generatedAt;
        private Instant deliveredAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryRequestDto {
        private String deliveryMethod;
        private String recipientEmail;
    }
}
