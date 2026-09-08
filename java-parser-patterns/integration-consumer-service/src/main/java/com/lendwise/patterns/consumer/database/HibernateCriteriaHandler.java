package com.lendwise.patterns.consumer.database;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Hibernate Criteria API patterns (JPA Criteria + legacy Hibernate).
 * Parser should detect: CriteriaBuilder, CriteriaQuery, Root, Predicate
 */
@Repository
@Slf4j
public class HibernateCriteriaHandler {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * JPA Criteria API basic query.
     * Parser detects: getCriteriaBuilder(), createQuery(), from(), select()
     */
    public List<LoanEntity> findAllLoans() {
        // Parser detects: entityManager.getCriteriaBuilder()
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Parser detects: cb.createQuery()
        CriteriaQuery<LoanEntity> cq = cb.createQuery(LoanEntity.class);

        // Parser detects: cq.from()
        Root<LoanEntity> root = cq.from(LoanEntity.class);

        // Parser detects: cq.select()
        cq.select(root);

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Criteria with where clause.
     * Parser detects: cb.equal(), cb.and(), cb.or(), cq.where()
     */
    public List<LoanEntity> findByStatus(String status) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoanEntity> cq = cb.createQuery(LoanEntity.class);
        Root<LoanEntity> root = cq.from(LoanEntity.class);

        // Parser detects: cb.equal()
        Predicate statusPredicate = cb.equal(root.get("status"), status);

        // Parser detects: cq.where()
        cq.where(statusPredicate);

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Complex criteria with multiple predicates.
     * Parser detects: cb.and(), cb.or(), cb.greaterThan(), cb.lessThan()
     */
    public List<LoanEntity> findByComplexCriteria(String status, double minAmount, double maxAmount) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoanEntity> cq = cb.createQuery(LoanEntity.class);
        Root<LoanEntity> root = cq.from(LoanEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        // Parser detects: cb.equal()
        predicates.add(cb.equal(root.get("status"), status));

        // Parser detects: cb.greaterThanOrEqualTo()
        predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));

        // Parser detects: cb.lessThanOrEqualTo()
        predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));

        // Parser detects: cb.and() with array
        cq.where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Criteria with ordering.
     * Parser detects: cb.asc(), cb.desc(), cq.orderBy()
     */
    public List<LoanEntity> findOrderedByAmount(boolean ascending) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoanEntity> cq = cb.createQuery(LoanEntity.class);
        Root<LoanEntity> root = cq.from(LoanEntity.class);

        cq.select(root);

        // Parser detects: cb.asc() / cb.desc()
        if (ascending) {
            cq.orderBy(cb.asc(root.get("amount")));
        } else {
            cq.orderBy(cb.desc(root.get("amount")));
        }

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Criteria with joins.
     * Parser detects: root.join(), JoinType
     */
    public List<LoanEntity> findWithJoins() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoanEntity> cq = cb.createQuery(LoanEntity.class);
        Root<LoanEntity> root = cq.from(LoanEntity.class);

        // Parser detects: root.join()
        Join<LoanEntity, Object> borrowerJoin = root.join("borrower", JoinType.LEFT);

        cq.select(root);
        cq.where(cb.isNotNull(borrowerJoin.get("id")));

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Aggregate functions.
     * Parser detects: cb.count(), cb.sum(), cb.avg(), cb.max(), cb.min()
     */
    public Object[] getAggregates() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<LoanEntity> root = cq.from(LoanEntity.class);

        // Parser detects: aggregate functions
        cq.multiselect(
            cb.count(root),
            cb.sum(root.get("amount")),
            cb.avg(root.get("amount")),
            cb.max(root.get("amount")),
            cb.min(root.get("amount"))
        );

        return entityManager.createQuery(cq).getSingleResult();
    }

    /**
     * Group by and having.
     * Parser detects: cq.groupBy(), cq.having()
     */
    public List<Object[]> getGroupedByStatus() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<LoanEntity> root = cq.from(LoanEntity.class);

        cq.multiselect(
            root.get("status"),
            cb.count(root),
            cb.sum(root.get("amount"))
        );

        // Parser detects: cq.groupBy()
        cq.groupBy(root.get("status"));

        // Parser detects: cq.having()
        cq.having(cb.gt(cb.count(root), 5L));

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Subquery.
     * Parser detects: cq.subquery()
     */
    public List<LoanEntity> findAboveAverage() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoanEntity> cq = cb.createQuery(LoanEntity.class);
        Root<LoanEntity> root = cq.from(LoanEntity.class);

        // Parser detects: cq.subquery()
        Subquery<Double> subquery = cq.subquery(Double.class);
        Root<LoanEntity> subRoot = subquery.from(LoanEntity.class);
        subquery.select(cb.avg(subRoot.get("amount")));

        // Parser detects: cb.greaterThan() with subquery
        cq.where(cb.greaterThan(root.get("amount"), subquery));

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Like pattern matching.
     * Parser detects: cb.like()
     */
    public List<LoanEntity> findByBorrowerPattern(String pattern) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoanEntity> cq = cb.createQuery(LoanEntity.class);
        Root<LoanEntity> root = cq.from(LoanEntity.class);

        // Parser detects: cb.like()
        cq.where(cb.like(root.get("borrowerId"), "%" + pattern + "%"));

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * In clause.
     * Parser detects: root.get().in()
     */
    public List<LoanEntity> findByStatusIn(List<String> statuses) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoanEntity> cq = cb.createQuery(LoanEntity.class);
        Root<LoanEntity> root = cq.from(LoanEntity.class);

        // Parser detects: in() predicate
        cq.where(root.get("status").in(statuses));

        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Hibernate-specific: Session API access.
     * Parser detects: entityManager.unwrap(Session.class)
     */
    public void useHibernateSession() {
        // Parser detects: entityManager.unwrap()
        Session session = entityManager.unwrap(Session.class);

        // Parser detects: session.createQuery() (Hibernate HQL)
        Query<LoanEntity> query = session.createQuery(
            "FROM LoanEntity WHERE status = :status",
            LoanEntity.class
        );
        query.setParameter("status", "APPROVED");

        List<LoanEntity> results = query.list();
        log.info("Found {} loans via Hibernate Session", results.size());
    }

    // Entity class
    @Entity
    @Table(name = "loans")
    public static class LoanEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "borrower_id")
        private String borrowerId;

        @Column(name = "amount")
        private double amount;

        @Column(name = "status")
        private String status;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "borrower_fk")
        private BorrowerEntity borrower;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBorrowerId() { return borrowerId; }
        public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BorrowerEntity getBorrower() { return borrower; }
        public void setBorrower(BorrowerEntity borrower) { this.borrower = borrower; }
    }

    @Entity
    @Table(name = "borrowers")
    public static class BorrowerEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "name")
        private String name;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
