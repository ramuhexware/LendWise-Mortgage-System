package com.lendwise.patterns.producer.ejb;

import jakarta.ejb.*;
import lombok.extern.slf4j.Slf4j;

/**
 * EJB Session Bean patterns.
 * Parser should detect: @Stateless, @Stateful, @Singleton, @EJB, @Local, @Remote
 */
@Stateless
@Local(CreditCheckLocal.class)
@Remote(CreditCheckRemote.class)
@Slf4j
public class CreditCheckEjb implements CreditCheckLocal, CreditCheckRemote {

    // Parser detects: @EJB injection
    @EJB
    private CacheManagerEjb cacheManager;

    // Parser detects: @EJB with lookup
    @EJB(lookup = "java:global/lendwise/AuditService")
    private AuditServiceLocal auditService;

    /**
     * Business method for credit check.
     */
    @Override
    public CreditCheckResult performCreditCheck(String borrowerId, String ssn) {
        log.info("Performing credit check for borrower: {}", borrowerId);

        // Check cache first
        CreditCheckResult cached = cacheManager.getCachedResult(borrowerId);
        if (cached != null) {
            log.info("Returning cached credit result for borrower: {}", borrowerId);
            return cached;
        }

        // Perform credit check (simulated)
        CreditCheckResult result = new CreditCheckResult();
        result.setBorrowerId(borrowerId);
        result.setFicoScore(720);
        result.setStatus("APPROVED");
        result.setTimestamp(System.currentTimeMillis());

        // Cache the result
        cacheManager.cacheResult(borrowerId, result);

        // Audit the check
        if (auditService != null) {
            auditService.logCreditCheck(borrowerId, result.getStatus());
        }

        return result;
    }

    /**
     * Async credit check (for larger batch operations).
     */
    @Override
    @Asynchronous
    public void performAsyncCreditCheck(String borrowerId, String ssn) {
        log.info("Starting async credit check for borrower: {}", borrowerId);
        performCreditCheck(borrowerId, ssn);
        log.info("Async credit check completed for borrower: {}", borrowerId);
    }

    /**
     * Method with transaction attribute.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void performCreditCheckWithNewTransaction(String borrowerId, String ssn) {
        performCreditCheck(borrowerId, ssn);
    }

    // Local interface
    @Local
    public interface CreditCheckLocal {
        CreditCheckResult performCreditCheck(String borrowerId, String ssn);
        void performAsyncCreditCheck(String borrowerId, String ssn);
    }

    // Remote interface
    @Remote
    public interface CreditCheckRemote {
        CreditCheckResult performCreditCheck(String borrowerId, String ssn);
        void performAsyncCreditCheck(String borrowerId, String ssn);
    }

    // Audit service interface (for @EJB lookup demo)
    @Local
    public interface AuditServiceLocal {
        void logCreditCheck(String borrowerId, String status);
    }

    // Result DTO
    public static class CreditCheckResult implements java.io.Serializable {
        private String borrowerId;
        private int ficoScore;
        private String status;
        private long timestamp;

        public String getBorrowerId() { return borrowerId; }
        public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
        public int getFicoScore() { return ficoScore; }
        public void setFicoScore(int ficoScore) { this.ficoScore = ficoScore; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
