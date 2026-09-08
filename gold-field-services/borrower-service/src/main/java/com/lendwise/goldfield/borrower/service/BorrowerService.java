package com.lendwise.goldfield.borrower.service;

import com.lendwise.goldfield.borrower.model.Borrower;
import com.lendwise.goldfield.borrower.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Service for borrower operations including DTI calculations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String BORROWER_EVENTS_TOPIC = "goldfield.borrower.events";

    public Mono<Borrower> createBorrower(Borrower borrower) {
        borrower.setBorrowerId(UUID.randomUUID().toString());
        borrower.setStatus(Borrower.BorrowerStatus.INCOMPLETE);

        return borrowerRepository.save(borrower)
                .doOnSuccess(saved -> {
                    log.info("Borrower created: {}", saved.getBorrowerId());
                    kafkaTemplate.send(BORROWER_EVENTS_TOPIC, saved.getBorrowerId(),
                            new BorrowerCreatedEvent(saved.getBorrowerId(), saved.getLoanId()));
                });
    }

    public Mono<Borrower> getBorrower(String borrowerId) {
        return borrowerRepository.findByBorrowerId(borrowerId);
    }

    public Flux<Borrower> getBorrowersByLoan(String loanId) {
        return borrowerRepository.findByLoanId(loanId);
    }

    public Mono<Borrower> updateBorrower(String borrowerId, Borrower updates) {
        return borrowerRepository.findByBorrowerId(borrowerId)
                .flatMap(existing -> {
                    // Merge updates
                    if (updates.getMonthlyIncome() != null) {
                        existing.setMonthlyIncome(updates.getMonthlyIncome());
                    }
                    if (updates.getMonthlyDebt() != null) {
                        existing.setMonthlyDebt(updates.getMonthlyDebt());
                    }
                    if (updates.getCreditScore() != null) {
                        existing.setCreditScore(updates.getCreditScore());
                    }
                    return borrowerRepository.save(existing);
                });
    }

    /**
     * Calculate DTI ratios for a borrower.
     */
    public Mono<DTIResult> calculateDTI(String borrowerId, BigDecimal proposedPITI) {
        return borrowerRepository.findByBorrowerId(borrowerId)
                .map(borrower -> {
                    BigDecimal monthlyIncome = borrower.getMonthlyIncome();
                    BigDecimal monthlyDebt = borrower.getMonthlyDebt();

                    if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalStateException("Monthly income not set or invalid");
                    }

                    // Front-end DTI = PITI / Monthly Income * 100
                    BigDecimal frontEndDTI = proposedPITI
                            .divide(monthlyIncome, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);

                    // Back-end DTI = (PITI + Monthly Debt) / Monthly Income * 100
                    BigDecimal totalMonthlyObligations = proposedPITI.add(monthlyDebt != null ? monthlyDebt : BigDecimal.ZERO);
                    BigDecimal backEndDTI = totalMonthlyObligations
                            .divide(monthlyIncome, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);

                    // QM compliance check (43% DTI cap)
                    boolean isQMCompliant = backEndDTI.compareTo(BigDecimal.valueOf(43)) <= 0;

                    // Update borrower with calculated values
                    borrower.setFrontEndDTI(frontEndDTI);
                    borrower.setBackEndDTI(backEndDTI);
                    borrowerRepository.save(borrower).subscribe();

                    return DTIResult.builder()
                            .borrowerId(borrowerId)
                            .frontEndDTI(frontEndDTI)
                            .backEndDTI(backEndDTI)
                            .monthlyIncome(monthlyIncome)
                            .monthlyDebt(monthlyDebt)
                            .proposedPITI(proposedPITI)
                            .isQMCompliant(isQMCompliant)
                            .build();
                });
    }

    @lombok.Data
    @lombok.Builder
    public static class DTIResult {
        private String borrowerId;
        private BigDecimal frontEndDTI;
        private BigDecimal backEndDTI;
        private BigDecimal monthlyIncome;
        private BigDecimal monthlyDebt;
        private BigDecimal proposedPITI;
        private boolean isQMCompliant;
    }

    public record BorrowerCreatedEvent(String borrowerId, String loanId) {}
}
