package com.lendwise.goldfield.amortization.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/amortization")
@Slf4j
public class AmortizationController {

    @PostMapping("/schedule")
    @ResponseStatus(HttpStatus.OK)
    public Mono<AmortizationScheduleDto> calculateSchedule(@RequestBody AmortizationRequest request) {
        log.info("Calculating amortization schedule for principal={}, rate={}, termMonths={}",
                request.getPrincipal(), request.getAnnualRate(), request.getTermMonths());

        double r = request.getAnnualRate() / 100 / 12;
        int n = request.getTermMonths();
        double monthlyPayment = request.getPrincipal() * (r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);

        List<ScheduleEntry> entries = new ArrayList<>();
        double balance = request.getPrincipal();
        for (int month = 1; month <= Math.min(n, 12); month++) {
            double interest = balance * r;
            double principalPaid = monthlyPayment - interest;
            balance -= principalPaid;
            entries.add(new ScheduleEntry(month, round(monthlyPayment), round(principalPaid), round(interest), round(balance)));
        }

        AmortizationScheduleDto schedule = AmortizationScheduleDto.builder()
                .principal(request.getPrincipal())
                .annualRate(request.getAnnualRate())
                .termMonths(request.getTermMonths())
                .monthlyPayment(round(monthlyPayment))
                .firstYearEntries(entries)
                .build();

        return Mono.just(schedule);
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AmortizationRequest {
        private double principal;
        private double annualRate;
        private int termMonths;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AmortizationScheduleDto {
        private double principal;
        private double annualRate;
        private int termMonths;
        private double monthlyPayment;
        private List<ScheduleEntry> firstYearEntries;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleEntry {
        private int month;
        private double payment;
        private double principalPaid;
        private double interestPaid;
        private double remainingBalance;
    }
}
