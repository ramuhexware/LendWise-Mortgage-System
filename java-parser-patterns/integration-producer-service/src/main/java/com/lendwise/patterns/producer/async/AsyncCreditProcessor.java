package com.lendwise.patterns.producer.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Async processing patterns using CompletableFuture.
 * Parser should detect: CompletableFuture.supplyAsync(), thenApply(), thenCompose(),
 * thenCombine(), allOf(), anyOf(), exceptionally()
 */
@Service
@Slf4j
public class AsyncCreditProcessor {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    /**
     * Basic async operation with supplyAsync.
     * Parser detects: CompletableFuture.supplyAsync()
     */
    public CompletableFuture<Integer> fetchCreditScoreAsync(String borrowerId) {
        // Parser detects: CompletableFuture.supplyAsync()
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching credit score for borrower: {}", borrowerId);
            simulateDelay(1000);
            return 720;
        }, executor);
    }

    /**
     * Chain transformations with thenApply.
     * Parser detects: .thenApply()
     */
    public CompletableFuture<String> fetchAndClassifyCredit(String borrowerId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching credit for: {}", borrowerId);
            return 720;
        }, executor)
        // Parser detects: .thenApply()
        .thenApply(score -> {
            if (score >= 740) return "EXCELLENT";
            if (score >= 670) return "GOOD";
            if (score >= 580) return "FAIR";
            return "POOR";
        });
    }

    /**
     * Chain async operations with thenCompose.
     * Parser detects: .thenCompose()
     */
    public CompletableFuture<CreditDecision> fetchScoreAndMakeDecision(String borrowerId) {
        return fetchCreditScoreAsync(borrowerId)
            // Parser detects: .thenCompose()
            .thenCompose(score -> makeDecisionAsync(borrowerId, score));
    }

    /**
     * Combine two async results with thenCombine.
     * Parser detects: .thenCombine()
     */
    public CompletableFuture<CombinedReport> fetchCombinedReport(String borrowerId) {
        CompletableFuture<Integer> creditFuture = fetchCreditScoreAsync(borrowerId);
        CompletableFuture<Double> dtiRatioFuture = fetchDtiRatioAsync(borrowerId);

        // Parser detects: .thenCombine()
        return creditFuture.thenCombine(dtiRatioFuture, (score, dti) ->
            new CombinedReport(borrowerId, score, dti));
    }

    /**
     * Wait for all futures with allOf.
     * Parser detects: CompletableFuture.allOf()
     */
    public CompletableFuture<Void> processMultipleBorrowers(List<String> borrowerIds) {
        CompletableFuture<?>[] futures = borrowerIds.stream()
            .map(this::fetchCreditScoreAsync)
            .toArray(CompletableFuture[]::new);

        // Parser detects: CompletableFuture.allOf()
        return CompletableFuture.allOf(futures)
            .thenRun(() -> log.info("All {} borrowers processed", borrowerIds.size()));
    }

    /**
     * Get first completed with anyOf.
     * Parser detects: CompletableFuture.anyOf()
     */
    public CompletableFuture<Object> getFirstAvailableScore(String borrowerId) {
        CompletableFuture<Integer> equifax = fetchFromBureau(borrowerId, "EQUIFAX");
        CompletableFuture<Integer> experian = fetchFromBureau(borrowerId, "EXPERIAN");
        CompletableFuture<Integer> transunion = fetchFromBureau(borrowerId, "TRANSUNION");

        // Parser detects: CompletableFuture.anyOf()
        return CompletableFuture.anyOf(equifax, experian, transunion);
    }

    /**
     * Error handling with exceptionally.
     * Parser detects: .exceptionally()
     */
    public CompletableFuture<Integer> fetchWithFallback(String borrowerId) {
        return CompletableFuture.supplyAsync(() -> {
            if (borrowerId.startsWith("ERR")) {
                throw new RuntimeException("Simulated error");
            }
            return 720;
        }, executor)
        // Parser detects: .exceptionally()
        .exceptionally(ex -> {
            log.error("Error fetching credit, using default: {}", ex.getMessage());
            return 600; // Default fallback score
        });
    }

    /**
     * Handle with both success and error.
     * Parser detects: .handle()
     */
    public CompletableFuture<CreditResult> fetchWithHandle(String borrowerId) {
        return CompletableFuture.supplyAsync(() -> {
            return 720;
        }, executor)
        // Parser detects: .handle()
        .handle((score, ex) -> {
            if (ex != null) {
                return new CreditResult(borrowerId, 0, "ERROR", ex.getMessage());
            }
            return new CreditResult(borrowerId, score, "SUCCESS", null);
        });
    }

    /**
     * Run action on completion with thenRun.
     * Parser detects: .thenRun()
     */
    public CompletableFuture<Void> fetchAndLog(String borrowerId) {
        return fetchCreditScoreAsync(borrowerId)
            // Parser detects: .thenRun()
            .thenRun(() -> log.info("Credit score fetch completed for: {}", borrowerId));
    }

    /**
     * Accept result with thenAccept.
     * Parser detects: .thenAccept()
     */
    public CompletableFuture<Void> fetchAndStore(String borrowerId) {
        return fetchCreditScoreAsync(borrowerId)
            // Parser detects: .thenAccept()
            .thenAccept(score -> log.info("Storing score {} for borrower {}", score, borrowerId));
    }

    /**
     * Spring @Async method.
     * Parser detects: @Async annotation
     */
    @Async
    public CompletableFuture<String> processAsyncSpring(String borrowerId) {
        log.info("Processing async with Spring for: {}", borrowerId);
        simulateDelay(500);
        return CompletableFuture.completedFuture("Processed: " + borrowerId);
    }

    // Helper methods
    private CompletableFuture<CreditDecision> makeDecisionAsync(String borrowerId, int score) {
        return CompletableFuture.supplyAsync(() -> {
            String decision = score >= 680 ? "APPROVED" : "DENIED";
            return new CreditDecision(borrowerId, score, decision);
        }, executor);
    }

    private CompletableFuture<Double> fetchDtiRatioAsync(String borrowerId) {
        return CompletableFuture.supplyAsync(() -> {
            simulateDelay(500);
            return 35.5;
        }, executor);
    }

    private CompletableFuture<Integer> fetchFromBureau(String borrowerId, String bureau) {
        return CompletableFuture.supplyAsync(() -> {
            simulateDelay((int) (Math.random() * 2000));
            log.info("Got score from {}", bureau);
            return 700 + (int) (Math.random() * 50);
        }, executor);
    }

    private void simulateDelay(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }

    // DTOs
    public record CreditDecision(String borrowerId, int score, String decision) {}
    public record CombinedReport(String borrowerId, int creditScore, double dtiRatio) {}
    public record CreditResult(String borrowerId, int score, String status, String error) {}
}
