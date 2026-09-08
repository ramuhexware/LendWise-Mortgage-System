package com.lendwise.patterns.producer.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Java Stream and Lambda patterns.
 * Parser should detect: .stream(), .filter(), .map(), .flatMap(), .collect(),
 * .reduce(), .forEach(), lambda expressions, method references
 */
@Service
@Slf4j
public class StreamDataProcessor {

    /**
     * Basic stream with filter and map.
     * Parser detects: .stream(), .filter(), .map(), .collect()
     */
    public List<String> getApprovedBorrowerNames(List<Borrower> borrowers) {
        // Parser detects: .stream().filter().map().collect()
        return borrowers.stream()
            .filter(b -> "APPROVED".equals(b.status()))
            .map(Borrower::name)
            .collect(Collectors.toList());
    }

    /**
     * Stream with lambda expression.
     * Parser detects: lambda expression (x -> x.something)
     */
    public List<Borrower> filterByScore(List<Borrower> borrowers, int minScore) {
        // Parser detects: lambda expression b -> b.creditScore() >= minScore
        return borrowers.stream()
            .filter(b -> b.creditScore() >= minScore)
            .toList();
    }

    /**
     * Stream with method reference.
     * Parser detects: method reference Class::method
     */
    public List<String> extractNames(List<Borrower> borrowers) {
        // Parser detects: method reference Borrower::name
        return borrowers.stream()
            .map(Borrower::name)
            .toList();
    }

    /**
     * FlatMap for nested collections.
     * Parser detects: .flatMap()
     */
    public List<Document> getAllDocuments(List<Borrower> borrowers) {
        // Parser detects: .flatMap()
        return borrowers.stream()
            .flatMap(b -> b.documents().stream())
            .toList();
    }

    /**
     * Reduce operation.
     * Parser detects: .reduce()
     */
    public BigDecimal calculateTotalLoanAmount(List<Loan> loans) {
        // Parser detects: .reduce()
        return loans.stream()
            .map(Loan::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * GroupingBy collector.
     * Parser detects: Collectors.groupingBy()
     */
    public Map<String, List<Borrower>> groupByStatus(List<Borrower> borrowers) {
        // Parser detects: Collectors.groupingBy()
        return borrowers.stream()
            .collect(Collectors.groupingBy(Borrower::status));
    }

    /**
     * Partitioning collector.
     * Parser detects: Collectors.partitioningBy()
     */
    public Map<Boolean, List<Borrower>> partitionByApproval(List<Borrower> borrowers) {
        // Parser detects: Collectors.partitioningBy()
        return borrowers.stream()
            .collect(Collectors.partitioningBy(b -> b.creditScore() >= 680));
    }

    /**
     * ToMap collector.
     * Parser detects: Collectors.toMap()
     */
    public Map<String, Borrower> createBorrowerMap(List<Borrower> borrowers) {
        // Parser detects: Collectors.toMap()
        return borrowers.stream()
            .collect(Collectors.toMap(Borrower::id, Function.identity()));
    }

    /**
     * Counting and statistics.
     * Parser detects: Collectors.counting(), Collectors.summarizingInt()
     */
    public Map<String, Long> countByStatus(List<Borrower> borrowers) {
        return borrowers.stream()
            .collect(Collectors.groupingBy(Borrower::status, Collectors.counting()));
    }

    /**
     * Sorted stream.
     * Parser detects: .sorted()
     */
    public List<Borrower> sortByScore(List<Borrower> borrowers) {
        // Parser detects: .sorted() with Comparator
        return borrowers.stream()
            .sorted(Comparator.comparingInt(Borrower::creditScore).reversed())
            .toList();
    }

    /**
     * Distinct and limit.
     * Parser detects: .distinct(), .limit()
     */
    public List<String> getUniqueStatuses(List<Borrower> borrowers, int limit) {
        // Parser detects: .distinct(), .limit()
        return borrowers.stream()
            .map(Borrower::status)
            .distinct()
            .limit(limit)
            .toList();
    }

    /**
     * Skip and peek.
     * Parser detects: .skip(), .peek()
     */
    public List<Borrower> processWithSkip(List<Borrower> borrowers, int skip) {
        // Parser detects: .skip(), .peek()
        return borrowers.stream()
            .skip(skip)
            .peek(b -> log.debug("Processing: {}", b.name()))
            .toList();
    }

    /**
     * AnyMatch, allMatch, noneMatch.
     * Parser detects: .anyMatch(), .allMatch(), .noneMatch()
     */
    public boolean hasApprovedBorrower(List<Borrower> borrowers) {
        // Parser detects: .anyMatch()
        return borrowers.stream()
            .anyMatch(b -> "APPROVED".equals(b.status()));
    }

    public boolean allApproved(List<Borrower> borrowers) {
        // Parser detects: .allMatch()
        return borrowers.stream()
            .allMatch(b -> "APPROVED".equals(b.status()));
    }

    /**
     * FindFirst and findAny.
     * Parser detects: .findFirst(), .findAny()
     */
    public Optional<Borrower> findFirstApproved(List<Borrower> borrowers) {
        // Parser detects: .findFirst()
        return borrowers.stream()
            .filter(b -> "APPROVED".equals(b.status()))
            .findFirst();
    }

    /**
     * Optional operations.
     * Parser detects: Optional.ofNullable(), .orElse(), .orElseGet(), .ifPresent()
     */
    public String getBorrowerNameOrDefault(Borrower borrower) {
        // Parser detects: Optional.ofNullable(), .orElse()
        return Optional.ofNullable(borrower)
            .map(Borrower::name)
            .orElse("Unknown");
    }

    public void processBorrowerIfPresent(Borrower borrower) {
        // Parser detects: Optional.ofNullable(), .ifPresent()
        Optional.ofNullable(borrower)
            .ifPresent(b -> log.info("Processing: {}", b.name()));
    }

    /**
     * IntStream operations.
     * Parser detects: IntStream.range(), IntStream.of()
     */
    public int sumRange(int start, int end) {
        // Parser detects: IntStream.range()
        return IntStream.range(start, end).sum();
    }

    /**
     * Generate and iterate.
     * Parser detects: Stream.generate(), Stream.iterate()
     */
    public List<String> generateIds(int count) {
        // Parser detects: Stream.generate()
        return Stream.generate(() -> UUID.randomUUID().toString())
            .limit(count)
            .toList();
    }

    /**
     * Parallel stream.
     * Parser detects: .parallelStream()
     */
    public List<Borrower> processParallel(List<Borrower> borrowers) {
        // Parser detects: .parallelStream()
        return borrowers.parallelStream()
            .filter(b -> b.creditScore() >= 700)
            .toList();
    }

    /**
     * ForEach with side effects.
     * Parser detects: .forEach()
     */
    public void logAllBorrowers(List<Borrower> borrowers) {
        // Parser detects: .forEach()
        borrowers.stream()
            .forEach(b -> log.info("Borrower: {} - Score: {}", b.name(), b.creditScore()));
    }

    // Record types
    public record Borrower(String id, String name, String status, int creditScore, List<Document> documents) {}
    public record Document(String id, String type, String status) {}
    public record Loan(String id, String borrowerId, BigDecimal amount) {}
}
