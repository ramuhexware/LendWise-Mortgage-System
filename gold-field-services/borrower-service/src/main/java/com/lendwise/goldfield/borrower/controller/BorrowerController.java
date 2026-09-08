package com.lendwise.goldfield.borrower.controller;

import com.lendwise.goldfield.borrower.model.Borrower;
import com.lendwise.goldfield.borrower.service.BorrowerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * REST controller for borrower operations.
 */
@RestController
@RequestMapping("/api/borrowers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Borrower", description = "Borrower management operations")
public class BorrowerController {

    private final BorrowerService borrowerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create borrower", description = "Creates a new borrower record")
    public Mono<Borrower> createBorrower(@RequestBody Borrower borrower) {
        return borrowerService.createBorrower(borrower);
    }

    @GetMapping("/{borrowerId}")
    @Operation(summary = "Get borrower", description = "Gets borrower by ID")
    public Mono<Borrower> getBorrower(@PathVariable String borrowerId) {
        return borrowerService.getBorrower(borrowerId);
    }

    @GetMapping("/loan/{loanId}")
    @Operation(summary = "Get borrowers by loan", description = "Gets all borrowers for a loan")
    public Flux<Borrower> getBorrowersByLoan(@PathVariable String loanId) {
        return borrowerService.getBorrowersByLoan(loanId);
    }

    @PutMapping("/{borrowerId}")
    @Operation(summary = "Update borrower", description = "Updates borrower information")
    public Mono<Borrower> updateBorrower(@PathVariable String borrowerId, @RequestBody Borrower updates) {
        return borrowerService.updateBorrower(borrowerId, updates);
    }

    @PostMapping("/{borrowerId}/dti")
    @Operation(summary = "Calculate DTI", description = "Calculates DTI ratios for borrower")
    public Mono<BorrowerService.DTIResult> calculateDTI(
            @PathVariable String borrowerId,
            @RequestBody DTIRequest request) {
        return borrowerService.calculateDTI(borrowerId, request.proposedPITI());
    }

    public record DTIRequest(BigDecimal proposedPITI) {}
}
