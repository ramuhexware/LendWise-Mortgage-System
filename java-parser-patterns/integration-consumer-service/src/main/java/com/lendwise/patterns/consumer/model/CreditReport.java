package com.lendwise.patterns.consumer.model;

import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity with inheritance (extends BaseCreditEntity).
 * Parser should detect: extends, @Override, class inheritance
 */
@Entity
@Table(name = "credit_reports")
@Slf4j
public class CreditReport extends BaseCreditEntity {

    // Static field with initializer
    private static final Map<String, Integer> SCORE_THRESHOLDS;

    // Static initializer block
    // Parser should detect: static initializer
    static {
        SCORE_THRESHOLDS = new HashMap<>();
        SCORE_THRESHOLDS.put("EXCELLENT", 740);
        SCORE_THRESHOLDS.put("GOOD", 670);
        SCORE_THRESHOLDS.put("FAIR", 580);
        SCORE_THRESHOLDS.put("POOR", 0);
        log.info("CreditReport static initializer: Score thresholds loaded");
    }

    @Column(name = "borrower_id", nullable = false)
    private String borrowerId;

    @Column(name = "fico_score")
    private int ficoScore;

    @Column(name = "bureau")
    private String bureau;

    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "creditReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CreditTradeline> tradelines;

    // Instance initializer block
    // Parser should detect: instance initializer
    {
        this.tradelines = new ArrayList<>();
        this.status = "PENDING";
        log.debug("CreditReport instance initializer: Default values set");
    }

    // Default constructor
    public CreditReport() {
        super();
        // Instance initializer runs first
    }

    // Parameterized constructor
    public CreditReport(String borrowerId, int ficoScore, String bureau) {
        super();
        this.borrowerId = borrowerId;
        this.ficoScore = ficoScore;
        this.bureau = bureau;
        this.status = classifyScore(ficoScore);
    }

    /**
     * Override of abstract method from base class.
     * Parser detects: @Override annotation, implements abstract method
     */
    @Override
    public String getEntityType() {
        return "CREDIT_REPORT";
    }

    /**
     * Override of hook method.
     * Parser detects: @Override
     */
    @Override
    protected void doSave() {
        log.info("Saving credit report for borrower: {}", borrowerId);
        // Actual save logic
    }

    /**
     * Override with super call.
     * Parser detects: super.method() call
     */
    @Override
    protected void beforeSave() {
        super.beforeSave();
        this.status = classifyScore(this.ficoScore);
        log.debug("CreditReport beforeSave: status updated to {}", status);
    }

    // Static utility method
    public static String classifyScore(int score) {
        if (score >= SCORE_THRESHOLDS.get("EXCELLENT")) return "EXCELLENT";
        if (score >= SCORE_THRESHOLDS.get("GOOD")) return "GOOD";
        if (score >= SCORE_THRESHOLDS.get("FAIR")) return "FAIR";
        return "POOR";
    }

    // Business methods
    public void addTradeline(CreditTradeline tradeline) {
        tradelines.add(tradeline);
        tradeline.setCreditReport(this);
    }

    public void removeTradeline(CreditTradeline tradeline) {
        tradelines.remove(tradeline);
        tradeline.setCreditReport(null);
    }

    // Getters and setters
    public String getBorrowerId() { return borrowerId; }
    public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
    public int getFicoScore() { return ficoScore; }
    public void setFicoScore(int ficoScore) { this.ficoScore = ficoScore; }
    public String getBureau() { return bureau; }
    public void setBureau(String bureau) { this.bureau = bureau; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<CreditTradeline> getTradelines() { return tradelines; }
    public void setTradelines(List<CreditTradeline> tradelines) { this.tradelines = tradelines; }

    @Override
    public String toString() {
        return "CreditReport{borrowerId='" + borrowerId + "', ficoScore=" + ficoScore +
               ", bureau='" + bureau + "', status='" + status + "'}";
    }
}

/**
 * Related entity for OneToMany relationship.
 */
@Entity
@Table(name = "credit_tradelines")
class CreditTradeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "balance")
    private double balance;

    @Column(name = "payment_status")
    private String paymentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_report_id")
    private CreditReport creditReport;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public CreditReport getCreditReport() { return creditReport; }
    public void setCreditReport(CreditReport creditReport) { this.creditReport = creditReport; }
}
