package com.lendwise.patterns.consumer.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC ResultSet processing patterns.
 * Parser should detect: ResultSet iteration, getXxx() methods, ResultSetMetaData
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class JdbcResultSetProcessor {

    private final DataSource dataSource;

    /**
     * Process ResultSet with while loop.
     * Parser detects: rs.next(), rs.getString(), rs.getInt()
     */
    public List<Map<String, Object>> executeQueryAndProcess(String sql) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             // Parser detects: stmt.executeQuery()
             ResultSet rs = stmt.executeQuery(sql)) {

            // Parser detects: rs.getMetaData()
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Parser detects: while (rs.next()) pattern
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    // Parser detects: rs.getObject()
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }

                results.add(row);
            }
        }

        log.info("Query returned {} rows", results.size());
        return results;
    }

    /**
     * Process with specific column types.
     * Parser detects: various rs.getXxx() methods
     */
    public List<CreditRecord> fetchCreditRecords() throws SQLException {
        String sql = "SELECT id, borrower_id, fico_score, bureau, created_at, is_active FROM credit_reports";
        List<CreditRecord> records = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                CreditRecord record = new CreditRecord();

                // Parser detects: various getter methods
                record.id = rs.getLong("id");
                record.borrowerId = rs.getString("borrower_id");
                record.ficoScore = rs.getInt("fico_score");
                record.bureau = rs.getString("bureau");
                record.createdAt = rs.getTimestamp("created_at");
                record.isActive = rs.getBoolean("is_active");

                // Parser detects: rs.wasNull() check
                if (rs.wasNull()) {
                    log.debug("Null value detected for isActive");
                }

                records.add(record);
            }
        }

        return records;
    }

    /**
     * Process with column index.
     * Parser detects: rs.getXxx(int columnIndex)
     */
    public List<Object[]> fetchByColumnIndex(String sql) throws SQLException {
        List<Object[]> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Object[] row = new Object[columnCount];

                for (int i = 1; i <= columnCount; i++) {
                    int columnType = metaData.getColumnType(i);

                    // Parser detects: switch with rs.getXxx() calls
                    switch (columnType) {
                        case Types.VARCHAR, Types.CHAR:
                            row[i - 1] = rs.getString(i);
                            break;
                        case Types.INTEGER:
                            row[i - 1] = rs.getInt(i);
                            break;
                        case Types.BIGINT:
                            row[i - 1] = rs.getLong(i);
                            break;
                        case Types.DOUBLE:
                            row[i - 1] = rs.getDouble(i);
                            break;
                        case Types.DECIMAL, Types.NUMERIC:
                            row[i - 1] = rs.getBigDecimal(i);
                            break;
                        case Types.TIMESTAMP:
                            row[i - 1] = rs.getTimestamp(i);
                            break;
                        case Types.DATE:
                            row[i - 1] = rs.getDate(i);
                            break;
                        case Types.BOOLEAN:
                            row[i - 1] = rs.getBoolean(i);
                            break;
                        case Types.BLOB:
                            row[i - 1] = rs.getBytes(i);
                            break;
                        default:
                            row[i - 1] = rs.getObject(i);
                    }
                }

                results.add(row);
            }
        }

        return results;
    }

    /**
     * Scrollable ResultSet.
     * Parser detects: scrollable ResultSet methods
     */
    public void processScrollableResultSet(String sql) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement(
                 ResultSet.TYPE_SCROLL_INSENSITIVE,
                 ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery(sql)) {

            // Parser detects: rs.last()
            rs.last();
            int totalRows = rs.getRow();
            log.info("Total rows: {}", totalRows);

            // Parser detects: rs.first()
            rs.first();
            log.info("First row ID: {}", rs.getLong("id"));

            // Parser detects: rs.absolute()
            rs.absolute(5);
            log.info("Row 5 ID: {}", rs.getLong("id"));

            // Parser detects: rs.relative()
            rs.relative(-2);
            log.info("Row 3 ID: {}", rs.getLong("id"));

            // Parser detects: rs.previous()
            while (rs.previous()) {
                log.info("Backward: {}", rs.getLong("id"));
            }

            // Parser detects: rs.beforeFirst()
            rs.beforeFirst();

            // Parser detects: rs.afterLast()
            rs.afterLast();
        }
    }

    /**
     * Updatable ResultSet.
     * Parser detects: updatable ResultSet methods
     */
    public void processUpdatableResultSet() throws SQLException {
        String sql = "SELECT id, fico_score FROM credit_reports WHERE id < 10";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement(
                 ResultSet.TYPE_SCROLL_SENSITIVE,
                 ResultSet.CONCUR_UPDATABLE);
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int currentScore = rs.getInt("fico_score");

                // Parser detects: rs.updateInt(), rs.updateRow()
                rs.updateInt("fico_score", currentScore + 10);
                rs.updateRow();
            }

            // Parser detects: rs.moveToInsertRow(), rs.insertRow()
            rs.moveToInsertRow();
            rs.updateLong("id", 999L);
            rs.updateInt("fico_score", 720);
            rs.insertRow();
            rs.moveToCurrentRow();

            // Parser detects: rs.deleteRow()
            rs.first();
            rs.deleteRow();
        }
    }

    public static class CreditRecord {
        public Long id;
        public String borrowerId;
        public int ficoScore;
        public String bureau;
        public Timestamp createdAt;
        public boolean isActive;
    }
}
