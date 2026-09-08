package com.lendwise.patterns.producer.database;

import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository patterns.
 * Parser should detect: JpaRepository, @Query annotations, derived query methods
 */
@Repository
public interface SpringDataJpaRepository extends JpaRepository<SpringDataJpaRepository.UnderwritingDecision, Long> {

    // =================================================================
    // Derived Query Methods - Parser detects method name patterns
    // =================================================================

    /**
     * Find by single field.
     * Parser detects: derived query from method name
     */
    List<UnderwritingDecision> findByLoanId(String loanId);

    /**
     * Find by multiple fields.
     */
    List<UnderwritingDecision> findByLoanIdAndStatus(String loanId, String status);

    /**
     * Find with ordering.
     */
    List<UnderwritingDecision> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Find first/top N.
     */
    Optional<UnderwritingDecision> findFirstByLoanIdOrderByCreatedAtDesc(String loanId);

    /**
     * Exists check.
     */
    boolean existsByLoanId(String loanId);

    /**
     * Count by field.
     */
    long countByStatus(String status);

    /**
     * Delete by field.
     */
    void deleteByLoanId(String loanId);

    /**
     * Find with like/containing.
     */
    List<UnderwritingDecision> findByLoanIdContaining(String partialLoanId);

    /**
     * Find between range.
     */
    List<UnderwritingDecision> findByRiskScoreBetween(int minScore, int maxScore);

    /**
     * Find greater than.
     */
    List<UnderwritingDecision> findByRiskScoreGreaterThan(int score);

    /**
     * Find in list.
     */
    List<UnderwritingDecision> findByStatusIn(List<String> statuses);

    /**
     * Find not null.
     */
    List<UnderwritingDecision> findByAusFindingsCodeIsNotNull();

    // =================================================================
    // @Query Annotations - Parser detects JPQL/SQL
    // =================================================================

    /**
     * JPQL query.
     * Parser detects: @Query with JPQL
     */
    @Query("SELECT u FROM UnderwritingDecision u WHERE u.loanId = :loanId AND u.status = :status")
    List<UnderwritingDecision> findByLoanAndStatus(@Param("loanId") String loanId, @Param("status") String status);

    /**
     * Native SQL query.
     * Parser detects: @Query with nativeQuery=true
     */
    @Query(value = "SELECT * FROM underwriting_decisions WHERE risk_score > :minScore", nativeQuery = true)
    List<UnderwritingDecision> findHighRiskNative(@Param("minScore") int minScore);

    /**
     * Update query.
     * Parser detects: @Modifying @Query
     */
    @Modifying
    @Query("UPDATE UnderwritingDecision u SET u.status = :status WHERE u.loanId = :loanId")
    int updateStatusByLoanId(@Param("loanId") String loanId, @Param("status") String status);

    /**
     * Delete query.
     */
    @Modifying
    @Query("DELETE FROM UnderwritingDecision u WHERE u.status = :status")
    int deleteByStatusQuery(@Param("status") String status);

    /**
     * Projection query - return specific fields.
     */
    @Query("SELECT u.loanId, u.status, u.riskScore FROM UnderwritingDecision u WHERE u.status = :status")
    List<Object[]> findProjectionByStatus(@Param("status") String status);

    /**
     * Query with join.
     */
    @Query("SELECT u FROM UnderwritingDecision u JOIN u.conditions c WHERE c.type = :conditionType")
    List<UnderwritingDecision> findByConditionType(@Param("conditionType") String conditionType);

    // =================================================================
    // Entity
    // =================================================================

    @Entity
    @Table(name = "underwriting_decisions")
    class UnderwritingDecision {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "loan_id", nullable = false)
        private String loanId;

        @Column(name = "status")
        private String status;

        @Column(name = "risk_score")
        private int riskScore;

        @Column(name = "aus_findings_code")
        private String ausFindingsCode;

        @Column(name = "created_at")
        private java.time.LocalDateTime createdAt;

        @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL)
        private List<Condition> conditions;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getLoanId() { return loanId; }
        public void setLoanId(String loanId) { this.loanId = loanId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
        public String getAusFindingsCode() { return ausFindingsCode; }
        public void setAusFindingsCode(String ausFindingsCode) { this.ausFindingsCode = ausFindingsCode; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
        public List<Condition> getConditions() { return conditions; }
        public void setConditions(List<Condition> conditions) { this.conditions = conditions; }
    }

    @Entity
    @Table(name = "underwriting_conditions")
    class Condition {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "type")
        private String type;

        @Column(name = "description")
        private String description;

        @ManyToOne
        @JoinColumn(name = "decision_id")
        private UnderwritingDecision decision;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public UnderwritingDecision getDecision() { return decision; }
        public void setDecision(UnderwritingDecision decision) { this.decision = decision; }
    }
}
