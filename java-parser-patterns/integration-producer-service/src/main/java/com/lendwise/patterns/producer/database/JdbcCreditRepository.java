package com.lendwise.patterns.producer.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Raw JDBC database access patterns.
 * Parser should detect: DriverManager.getConnection(), PreparedStatement,
 * executeQuery(), executeUpdate(), ResultSet
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class JdbcCreditRepository {

    private final DataSource dataSource;

    // Direct JDBC connection string (for pattern detection)
    private static final String JDBC_URL = "jdbc:oracle:thin:@localhost:1521:ORCL";
    private static final String JDBC_USER = "lendwise";
    private static final String JDBC_PASSWORD = "password";

    /**
     * Get connection using DriverManager (classic pattern).
     * Parser detects: DriverManager.getConnection()
     */
    public Connection getDirectConnection() throws SQLException {
        // Parser detects: DriverManager.getConnection()
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    /**
     * Insert credit report using PreparedStatement.
     * Parser detects: prepareStatement(), setString(), setInt(), executeUpdate()
     */
    public int insertCreditReport(String borrowerId, int ficoScore, String bureau) throws SQLException {
        String sql = "INSERT INTO credit_reports (borrower_id, fico_score, bureau, created_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             // Parser detects: connection.prepareStatement()
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Parser detects: pstmt.setString(), pstmt.setInt()
            pstmt.setString(1, borrowerId);
            pstmt.setInt(2, ficoScore);
            pstmt.setString(3, bureau);
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            // Parser detects: pstmt.executeUpdate()
            int rowsAffected = pstmt.executeUpdate();
            log.info("Inserted credit report for borrower {}, rows affected: {}", borrowerId, rowsAffected);

            return rowsAffected;
        }
    }

    /**
     * Select credit reports using executeQuery.
     * Parser detects: executeQuery(), ResultSet iteration
     */
    public List<CreditReport> findByBorrowerId(String borrowerId) throws SQLException {
        String sql = "SELECT id, borrower_id, fico_score, bureau, created_at FROM credit_reports WHERE borrower_id = ?";
        List<CreditReport> reports = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, borrowerId);

            // Parser detects: pstmt.executeQuery()
            try (ResultSet rs = pstmt.executeQuery()) {
                // Parser detects: rs.next(), rs.getString(), rs.getInt()
                while (rs.next()) {
                    CreditReport report = new CreditReport();
                    report.setId(rs.getLong("id"));
                    report.setBorrowerId(rs.getString("borrower_id"));
                    report.setFicoScore(rs.getInt("fico_score"));
                    report.setBureau(rs.getString("bureau"));
                    report.setCreatedAt(rs.getTimestamp("created_at"));
                    reports.add(report);
                }
            }
        }

        return reports;
    }

    /**
     * Update using Statement.execute().
     * Parser detects: createStatement(), execute()
     */
    public boolean updateCreditScore(String borrowerId, int newScore) throws SQLException {
        String sql = "UPDATE credit_reports SET fico_score = " + newScore +
                     " WHERE borrower_id = '" + borrowerId + "'";

        try (Connection conn = dataSource.getConnection();
             // Parser detects: connection.createStatement()
             Statement stmt = conn.createStatement()) {

            // Parser detects: stmt.execute()
            return stmt.execute(sql);
        }
    }

    /**
     * Batch insert using executeBatch().
     * Parser detects: addBatch(), executeBatch()
     */
    public int[] batchInsertReports(List<CreditReport> reports) throws SQLException {
        String sql = "INSERT INTO credit_reports (borrower_id, fico_score, bureau) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (CreditReport report : reports) {
                pstmt.setString(1, report.getBorrowerId());
                pstmt.setInt(2, report.getFicoScore());
                pstmt.setString(3, report.getBureau());

                // Parser detects: pstmt.addBatch()
                pstmt.addBatch();
            }

            // Parser detects: pstmt.executeBatch()
            return pstmt.executeBatch();
        }
    }

    /**
     * Transaction with commit/rollback.
     */
    public void insertWithTransaction(CreditReport report) throws SQLException {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            String sql = "INSERT INTO credit_reports (borrower_id, fico_score, bureau) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, report.getBorrowerId());
                pstmt.setInt(2, report.getFicoScore());
                pstmt.setString(3, report.getBureau());
                pstmt.executeUpdate();
            }

            // Parser detects: conn.commit()
            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                // Parser detects: conn.rollback()
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Call stored procedure.
     * Parser detects: prepareCall(), execute()
     */
    public int callStoredProcedure(String borrowerId) throws SQLException {
        String sql = "{call calculate_credit_score(?)}";

        try (Connection conn = dataSource.getConnection();
             // Parser detects: conn.prepareCall()
             CallableStatement cstmt = conn.prepareCall(sql)) {

            cstmt.setString(1, borrowerId);
            cstmt.execute();

            return cstmt.getInt(1);
        }
    }

    // Entity class
    public static class CreditReport {
        private Long id;
        private String borrowerId;
        private int ficoScore;
        private String bureau;
        private Timestamp createdAt;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBorrowerId() { return borrowerId; }
        public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
        public int getFicoScore() { return ficoScore; }
        public void setFicoScore(int ficoScore) { this.ficoScore = ficoScore; }
        public String getBureau() { return bureau; }
        public void setBureau(String bureau) { this.bureau = bureau; }
        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    }
}
