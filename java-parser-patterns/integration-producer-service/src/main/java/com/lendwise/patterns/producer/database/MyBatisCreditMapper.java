package com.lendwise.patterns.producer.database;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * MyBatis Mapper interface.
 * Parser should detect: @Mapper, @Select, @Insert, @Update, @Delete, @Results
 */
@Mapper
public interface MyBatisCreditMapper {

    // =================================================================
    // @Select Annotations
    // =================================================================

    /**
     * Select by ID.
     * Parser detects: @Select annotation with SQL
     */
    @Select("SELECT id, borrower_id, fico_score, bureau, created_at FROM credit_reports WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "borrowerId", column = "borrower_id"),
        @Result(property = "ficoScore", column = "fico_score"),
        @Result(property = "bureau", column = "bureau"),
        @Result(property = "createdAt", column = "created_at")
    })
    CreditReportDto selectById(@Param("id") Long id);

    /**
     * Select all.
     * Parser detects: @Select
     */
    @Select("SELECT * FROM credit_reports ORDER BY created_at DESC")
    List<CreditReportDto> selectAll();

    /**
     * Select by borrower ID.
     */
    @Select("SELECT * FROM credit_reports WHERE borrower_id = #{borrowerId}")
    List<CreditReportDto> selectByBorrowerId(@Param("borrowerId") String borrowerId);

    /**
     * Select with dynamic SQL.
     */
    @Select("<script>" +
            "SELECT * FROM credit_reports " +
            "<where>" +
            "  <if test='borrowerId != null'> AND borrower_id = #{borrowerId}</if>" +
            "  <if test='bureau != null'> AND bureau = #{bureau}</if>" +
            "  <if test='minScore != null'> AND fico_score >= #{minScore}</if>" +
            "</where>" +
            "</script>")
    List<CreditReportDto> selectWithFilters(@Param("borrowerId") String borrowerId,
                                             @Param("bureau") String bureau,
                                             @Param("minScore") Integer minScore);

    /**
     * Count by bureau.
     */
    @Select("SELECT COUNT(*) FROM credit_reports WHERE bureau = #{bureau}")
    int countByBureau(@Param("bureau") String bureau);

    // =================================================================
    // @Insert Annotations
    // =================================================================

    /**
     * Insert single record.
     * Parser detects: @Insert annotation
     */
    @Insert("INSERT INTO credit_reports (borrower_id, fico_score, bureau, created_at) " +
            "VALUES (#{borrowerId}, #{ficoScore}, #{bureau}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CreditReportDto creditReport);

    /**
     * Batch insert.
     */
    @Insert("<script>" +
            "INSERT INTO credit_reports (borrower_id, fico_score, bureau) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.borrowerId}, #{item.ficoScore}, #{item.bureau})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<CreditReportDto> creditReports);

    // =================================================================
    // @Update Annotations
    // =================================================================

    /**
     * Update record.
     * Parser detects: @Update annotation
     */
    @Update("UPDATE credit_reports SET fico_score = #{ficoScore}, bureau = #{bureau} WHERE id = #{id}")
    int update(CreditReportDto creditReport);

    /**
     * Update selective (only non-null fields).
     */
    @Update("<script>" +
            "UPDATE credit_reports " +
            "<set>" +
            "  <if test='ficoScore != null'> fico_score = #{ficoScore},</if>" +
            "  <if test='bureau != null'> bureau = #{bureau},</if>" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int updateSelective(CreditReportDto creditReport);

    // =================================================================
    // @Delete Annotations
    // =================================================================

    /**
     * Delete by ID.
     * Parser detects: @Delete annotation
     */
    @Delete("DELETE FROM credit_reports WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * Delete by borrower ID.
     */
    @Delete("DELETE FROM credit_reports WHERE borrower_id = #{borrowerId}")
    int deleteByBorrowerId(@Param("borrowerId") String borrowerId);

    /**
     * Batch delete.
     */
    @Delete("<script>" +
            "DELETE FROM credit_reports WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDelete(@Param("ids") List<Long> ids);

    // =================================================================
    // DTO
    // =================================================================

    class CreditReportDto {
        private Long id;
        private String borrowerId;
        private Integer ficoScore;
        private String bureau;
        private java.sql.Timestamp createdAt;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBorrowerId() { return borrowerId; }
        public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
        public Integer getFicoScore() { return ficoScore; }
        public void setFicoScore(Integer ficoScore) { this.ficoScore = ficoScore; }
        public String getBureau() { return bureau; }
        public void setBureau(String bureau) { this.bureau = bureau; }
        public java.sql.Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.sql.Timestamp createdAt) { this.createdAt = createdAt; }
    }
}
