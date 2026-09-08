package com.lendwise.patterns.consumer.model;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Value object with static and instance initializers.
 * Parser should detect: static initializer blocks, instance initializer blocks
 */
@Slf4j
public class CreditScore {

    // Static constants
    public static final int MIN_SCORE = 300;
    public static final int MAX_SCORE = 850;

    // Static field requiring complex initialization
    private static final Map<String, ScoreRange> SCORE_RANGES;
    private static final String[] BUREAUS;
    private static int instanceCount;

    // Static initializer block #1
    // Parser should detect: static initializer
    static {
        log.info("CreditScore static initializer #1: Initializing score ranges");

        SCORE_RANGES = new HashMap<>();
        SCORE_RANGES.put("EXCELLENT", new ScoreRange(740, 850));
        SCORE_RANGES.put("GOOD", new ScoreRange(670, 739));
        SCORE_RANGES.put("FAIR", new ScoreRange(580, 669));
        SCORE_RANGES.put("POOR", new ScoreRange(300, 579));
    }

    // Static initializer block #2 (multiple static blocks allowed)
    // Parser should detect: multiple static initializers
    static {
        log.info("CreditScore static initializer #2: Initializing bureaus");

        BUREAUS = new String[]{"EQUIFAX", "EXPERIAN", "TRANSUNION"};
        instanceCount = 0;
    }

    // Instance fields
    private int score;
    private String bureau;
    private LocalDateTime reportDate;
    private String classification;

    // Instance initializer block #1
    // Parser should detect: instance initializer
    {
        instanceCount++;
        this.reportDate = LocalDateTime.now();
        log.debug("CreditScore instance initializer #1: Set report date, count={}", instanceCount);
    }

    // Instance initializer block #2 (multiple instance blocks allowed)
    // Parser should detect: multiple instance initializers
    {
        this.classification = "UNCLASSIFIED";
        log.debug("CreditScore instance initializer #2: Set default classification");
    }

    // Constructors

    public CreditScore() {
        // Instance initializers run before constructor body
        this.score = 0;
        this.bureau = "UNKNOWN";
    }

    public CreditScore(int score) {
        // Instance initializers run first
        this.score = validateScore(score);
        this.classification = classify(score);
    }

    public CreditScore(int score, String bureau) {
        // Instance initializers run first
        this.score = validateScore(score);
        this.bureau = bureau;
        this.classification = classify(score);
    }

    // Static methods
    public static int validateScore(int score) {
        if (score < MIN_SCORE) return MIN_SCORE;
        if (score > MAX_SCORE) return MAX_SCORE;
        return score;
    }

    public static String classify(int score) {
        for (Map.Entry<String, ScoreRange> entry : SCORE_RANGES.entrySet()) {
            if (entry.getValue().contains(score)) {
                return entry.getKey();
            }
        }
        return "UNKNOWN";
    }

    public static ScoreRange getRangeFor(String classification) {
        return SCORE_RANGES.get(classification);
    }

    public static String[] getBureaus() {
        return BUREAUS.clone();
    }

    public static int getInstanceCount() {
        return instanceCount;
    }

    // Instance methods
    public boolean isGoodCredit() {
        return score >= SCORE_RANGES.get("GOOD").min();
    }

    public boolean isPrimeCredit() {
        return score >= SCORE_RANGES.get("EXCELLENT").min();
    }

    public int getPointsToNextTier() {
        if ("EXCELLENT".equals(classification)) return 0;

        String[] tiers = {"POOR", "FAIR", "GOOD", "EXCELLENT"};
        for (int i = 0; i < tiers.length - 1; i++) {
            if (tiers[i].equals(classification)) {
                return SCORE_RANGES.get(tiers[i + 1]).min() - score;
            }
        }
        return 0;
    }

    // Getters and setters
    public int getScore() { return score; }
    public void setScore(int score) {
        this.score = validateScore(score);
        this.classification = classify(this.score);
    }
    public String getBureau() { return bureau; }
    public void setBureau(String bureau) { this.bureau = bureau; }
    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }
    public String getClassification() { return classification; }

    @Override
    public String toString() {
        return "CreditScore{score=" + score + ", bureau='" + bureau +
               "', classification='" + classification + "', reportDate=" + reportDate + "}";
    }

    // Nested record for score ranges
    public record ScoreRange(int min, int max) {
        public boolean contains(int score) {
            return score >= min && score <= max;
        }
    }
}
