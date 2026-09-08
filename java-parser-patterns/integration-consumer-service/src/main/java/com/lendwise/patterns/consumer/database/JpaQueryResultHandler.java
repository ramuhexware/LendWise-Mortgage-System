package com.lendwise.patterns.consumer.database;

import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

/**
 * JPA Query result handling patterns.
 * Parser should detect: TypedQuery, Query result methods, pagination
 */
@Repository
@Slf4j
public class JpaQueryResultHandler {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * TypedQuery with getResultList.
     * Parser detects: createQuery(), getResultList()
     */
    public List<CreditReportJpa> findAllReports() {
        // Parser detects: entityManager.createQuery()
        TypedQuery<CreditReportJpa> query = entityManager.createQuery(
            "SELECT c FROM CreditReportJpa c ORDER BY c.createdAt DESC",
            CreditReportJpa.class
        );

        // Parser detects: query.getResultList()
        return query.getResultList();
    }

    /**
     * Query with getSingleResult.
     * Parser detects: getSingleResult()
     */
    public CreditReportJpa findById(Long id) {
        TypedQuery<CreditReportJpa> query = entityManager.createQuery(
            "SELECT c FROM CreditReportJpa c WHERE c.id = :id",
            CreditReportJpa.class
        );
        query.setParameter("id", id);

        try {
            // Parser detects: query.getSingleResult()
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Query with pagination.
     * Parser detects: setFirstResult(), setMaxResults()
     */
    public List<CreditReportJpa> findWithPagination(int page, int pageSize) {
        TypedQuery<CreditReportJpa> query = entityManager.createQuery(
            "SELECT c FROM CreditReportJpa c ORDER BY c.id",
            CreditReportJpa.class
        );

        // Parser detects: pagination methods
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    /**
     * Query with streaming results.
     * Parser detects: getResultStream()
     */
    public void processWithStream() {
        TypedQuery<CreditReportJpa> query = entityManager.createQuery(
            "SELECT c FROM CreditReportJpa c",
            CreditReportJpa.class
        );

        // Parser detects: query.getResultStream()
        try (Stream<CreditReportJpa> stream = query.getResultStream()) {
            stream.filter(c -> c.getFicoScore() >= 700)
                  .forEach(c -> log.info("High score: {}", c.getBorrowerId()));
        }
    }

    /**
     * Query with hints.
     * Parser detects: setHint()
     */
    public List<CreditReportJpa> findWithHints() {
        TypedQuery<CreditReportJpa> query = entityManager.createQuery(
            "SELECT c FROM CreditReportJpa c",
            CreditReportJpa.class
        );

        // Parser detects: query.setHint()
        query.setHint("org.hibernate.fetchSize", 50);
        query.setHint("org.hibernate.readOnly", true);
        query.setHint("org.hibernate.cacheable", true);

        return query.getResultList();
    }

    /**
     * Query with lock mode.
     * Parser detects: setLockMode()
     */
    public CreditReportJpa findForUpdate(Long id) {
        TypedQuery<CreditReportJpa> query = entityManager.createQuery(
            "SELECT c FROM CreditReportJpa c WHERE c.id = :id",
            CreditReportJpa.class
        );
        query.setParameter("id", id);

        // Parser detects: query.setLockMode()
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

        return query.getSingleResult();
    }

    /**
     * Aggregate query results.
     * Parser detects: Query with scalar results
     */
    public Object[] getAggregateStats() {
        Query query = entityManager.createQuery(
            "SELECT COUNT(c), AVG(c.ficoScore), MAX(c.ficoScore), MIN(c.ficoScore) " +
            "FROM CreditReportJpa c"
        );

        return (Object[]) query.getSingleResult();
    }

    /**
     * Native query execution.
     * Parser detects: createNativeQuery()
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> executeNativeQuery() {
        // Parser detects: entityManager.createNativeQuery()
        Query query = entityManager.createNativeQuery(
            "SELECT borrower_id, fico_score FROM credit_reports WHERE fico_score > 700"
        );

        return query.getResultList();
    }

    /**
     * Stored procedure call.
     * Parser detects: createStoredProcedureQuery()
     */
    public void callStoredProcedure(String borrowerId) {
        // Parser detects: entityManager.createStoredProcedureQuery()
        StoredProcedureQuery spQuery = entityManager.createStoredProcedureQuery("calculate_credit_score");

        spQuery.registerStoredProcedureParameter("borrower_id", String.class, ParameterMode.IN);
        spQuery.registerStoredProcedureParameter("result", Integer.class, ParameterMode.OUT);

        spQuery.setParameter("borrower_id", borrowerId);
        spQuery.execute();

        Integer result = (Integer) spQuery.getOutputParameterValue("result");
        log.info("Stored procedure result: {}", result);
    }

    // JPA Entity
    @Entity
    @Table(name = "credit_reports")
    public static class CreditReportJpa {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "borrower_id")
        private String borrowerId;

        @Column(name = "fico_score")
        private int ficoScore;

        @Column(name = "created_at")
        private java.time.LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBorrowerId() { return borrowerId; }
        public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
        public int getFicoScore() { return ficoScore; }
        public void setFicoScore(int ficoScore) { this.ficoScore = ficoScore; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
