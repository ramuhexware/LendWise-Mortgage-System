package com.lendwise.patterns.producer.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Hibernate Session database access patterns.
 * Parser should detect: sessionFactory.getCurrentSession(), session.save(),
 * session.update(), session.delete(), session.createQuery()
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class HibernateSessionRepository {

    private final SessionFactory sessionFactory;

    /**
     * Get current Hibernate Session.
     * Parser detects: sessionFactory.getCurrentSession()
     */
    protected Session getCurrentSession() {
        // Parser detects: sessionFactory.getCurrentSession()
        return sessionFactory.getCurrentSession();
    }

    /**
     * Save entity using session.save().
     * Parser detects: session.save()
     */
    @Transactional
    public Serializable save(LoanEntity entity) {
        log.info("Saving loan for borrower: {}", entity.getBorrowerId());

        Session session = getCurrentSession();
        // Parser detects: session.save()
        return session.save(entity);
    }

    /**
     * Update entity using session.update().
     * Parser detects: session.update()
     */
    @Transactional
    public void update(LoanEntity entity) {
        Session session = getCurrentSession();
        // Parser detects: session.update()
        session.update(entity);
    }

    /**
     * Save or update.
     * Parser detects: session.saveOrUpdate()
     */
    @Transactional
    public void saveOrUpdate(LoanEntity entity) {
        Session session = getCurrentSession();
        // Parser detects: session.saveOrUpdate()
        session.saveOrUpdate(entity);
    }

    /**
     * Delete entity.
     * Parser detects: session.delete()
     */
    @Transactional
    public void delete(LoanEntity entity) {
        Session session = getCurrentSession();
        // Parser detects: session.delete()
        session.delete(entity);
    }

    /**
     * Get entity by ID.
     * Parser detects: session.get()
     */
    public LoanEntity getById(Long id) {
        Session session = getCurrentSession();
        // Parser detects: session.get()
        return session.get(LoanEntity.class, id);
    }

    /**
     * Load entity (proxy).
     * Parser detects: session.load()
     */
    public LoanEntity loadById(Long id) {
        Session session = getCurrentSession();
        // Parser detects: session.load()
        return session.load(LoanEntity.class, id);
    }

    /**
     * HQL query using createQuery.
     * Parser detects: session.createQuery()
     */
    @SuppressWarnings("unchecked")
    public List<LoanEntity> findByBorrowerId(String borrowerId) {
        Session session = getCurrentSession();

        // Parser detects: session.createQuery()
        Query<LoanEntity> query = session.createQuery(
            "FROM LoanEntity l WHERE l.borrowerId = :borrowerId"
        );
        query.setParameter("borrowerId", borrowerId);

        return query.list();
    }

    /**
     * Named query via Hibernate.
     * Parser detects: session.getNamedQuery()
     */
    @SuppressWarnings("unchecked")
    public List<LoanEntity> findByStatusNamed(String status) {
        Session session = getCurrentSession();

        // Parser detects: session.getNamedQuery() (legacy) or createNamedQuery()
        Query<LoanEntity> query = session.createNamedQuery("Loan.findByStatus", LoanEntity.class);
        query.setParameter("status", status);

        return query.list();
    }

    /**
     * Native SQL query.
     * Parser detects: session.createNativeQuery()
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> findAllNative() {
        Session session = getCurrentSession();

        // Parser detects: session.createNativeQuery()
        return session.createNativeQuery("SELECT id, borrower_id, amount FROM loans").list();
    }

    /**
     * Criteria API (JPA style via Hibernate).
     */
    public List<LoanEntity> findByAmountRange(double minAmount, double maxAmount) {
        Session session = getCurrentSession();

        var cb = session.getCriteriaBuilder();
        var cq = cb.createQuery(LoanEntity.class);
        var root = cq.from(LoanEntity.class);

        cq.select(root)
          .where(cb.between(root.get("amount"), minAmount, maxAmount));

        return session.createQuery(cq).getResultList();
    }

    /**
     * Flush session.
     */
    @Transactional
    public void flush() {
        Session session = getCurrentSession();
        // Parser detects: session.flush()
        session.flush();
    }

    /**
     * Clear session cache.
     */
    @Transactional
    public void clear() {
        Session session = getCurrentSession();
        // Parser detects: session.clear()
        session.clear();
    }

    /**
     * Evict entity from cache.
     */
    public void evict(LoanEntity entity) {
        Session session = getCurrentSession();
        // Parser detects: session.evict()
        session.evict(entity);
    }

    // Hibernate Entity
    @Entity
    @Table(name = "loans")
    @NamedQuery(name = "Loan.findByStatus",
                query = "FROM LoanEntity l WHERE l.status = :status")
    public static class LoanEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "borrower_id", nullable = false)
        private String borrowerId;

        @Column(name = "amount")
        private double amount;

        @Column(name = "status")
        private String status;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBorrowerId() { return borrowerId; }
        public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
