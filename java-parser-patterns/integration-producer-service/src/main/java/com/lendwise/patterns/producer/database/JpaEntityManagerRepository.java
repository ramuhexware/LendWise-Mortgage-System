package com.lendwise.patterns.producer.database;

import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JPA EntityManager database access patterns.
 * Parser should detect: @PersistenceContext, em.persist(), em.find(),
 * em.merge(), em.remove(), em.createQuery()
 */
@Repository
@Slf4j
public class JpaEntityManagerRepository {

    // Parser detects: @PersistenceContext annotation
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Persist new entity.
     * Parser detects: entityManager.persist()
     */
    @Transactional
    public void save(CreditReportEntity entity) {
        log.info("Persisting credit report for borrower: {}", entity.getBorrowerId());

        // Parser detects: entityManager.persist()
        entityManager.persist(entity);
    }

    /**
     * Find entity by ID.
     * Parser detects: entityManager.find()
     */
    public CreditReportEntity findById(Long id) {
        // Parser detects: entityManager.find()
        return entityManager.find(CreditReportEntity.class, id);
    }

    /**
     * Merge (update) entity.
     * Parser detects: entityManager.merge()
     */
    @Transactional
    public CreditReportEntity update(CreditReportEntity entity) {
        // Parser detects: entityManager.merge()
        return entityManager.merge(entity);
    }

    /**
     * Remove entity.
     * Parser detects: entityManager.remove()
     */
    @Transactional
    public void delete(CreditReportEntity entity) {
        // Parser detects: entityManager.remove()
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }

    /**
     * JPQL query using createQuery.
     * Parser detects: entityManager.createQuery()
     */
    public List<CreditReportEntity> findByBorrowerId(String borrowerId) {
        // Parser detects: entityManager.createQuery()
        TypedQuery<CreditReportEntity> query = entityManager.createQuery(
            "SELECT c FROM CreditReportEntity c WHERE c.borrowerId = :borrowerId",
            CreditReportEntity.class
        );
        query.setParameter("borrowerId", borrowerId);

        return query.getResultList();
    }

    /**
     * Named query.
     * Parser detects: entityManager.createNamedQuery()
     */
    public List<CreditReportEntity> findByBureauNamed(String bureau) {
        // Parser detects: entityManager.createNamedQuery()
        TypedQuery<CreditReportEntity> query = entityManager.createNamedQuery(
            "CreditReport.findByBureau",
            CreditReportEntity.class
        );
        query.setParameter("bureau", bureau);

        return query.getResultList();
    }

    /**
     * Native SQL query.
     * Parser detects: entityManager.createNativeQuery()
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> findAllNative() {
        // Parser detects: entityManager.createNativeQuery()
        Query query = entityManager.createNativeQuery(
            "SELECT id, borrower_id, fico_score FROM credit_reports"
        );

        return query.getResultList();
    }

    /**
     * Criteria API query.
     * Parser detects: entityManager.getCriteriaBuilder()
     */
    public List<CreditReportEntity> findByScoreRange(int minScore, int maxScore) {
        // Parser detects: entityManager.getCriteriaBuilder()
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(CreditReportEntity.class);
        var root = cq.from(CreditReportEntity.class);

        cq.select(root)
          .where(cb.between(root.get("ficoScore"), minScore, maxScore));

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Flush and clear.
     */
    @Transactional
    public void flushAndClear() {
        // Parser detects: entityManager.flush()
        entityManager.flush();

        // Parser detects: entityManager.clear()
        entityManager.clear();
    }

    /**
     * Detach entity.
     */
    public void detach(CreditReportEntity entity) {
        // Parser detects: entityManager.detach()
        entityManager.detach(entity);
    }

    /**
     * Refresh entity from database.
     */
    public void refresh(CreditReportEntity entity) {
        // Parser detects: entityManager.refresh()
        entityManager.refresh(entity);
    }

    /**
     * Check if entity is managed.
     */
    public boolean isManaged(CreditReportEntity entity) {
        // Parser detects: entityManager.contains()
        return entityManager.contains(entity);
    }

    // JPA Entity
    @Entity
    @Table(name = "credit_reports")
    @NamedQuery(name = "CreditReport.findByBureau",
                query = "SELECT c FROM CreditReportEntity c WHERE c.bureau = :bureau")
    public static class CreditReportEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "borrower_id", nullable = false)
        private String borrowerId;

        @Column(name = "fico_score")
        private int ficoScore;

        @Column(name = "bureau")
        private String bureau;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBorrowerId() { return borrowerId; }
        public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
        public int getFicoScore() { return ficoScore; }
        public void setFicoScore(int ficoScore) { this.ficoScore = ficoScore; }
        public String getBureau() { return bureau; }
        public void setBureau(String bureau) { this.bureau = bureau; }
    }
}
