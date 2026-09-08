-- ============================================================================
-- LendWise Mortgage System - Compliance Audit View
-- Provides compliance audit results with defect tracking
-- ============================================================================

CREATE OR REPLACE VIEW V_COMPLIANCE_AUDIT AS
SELECT
    la.LOAN_ID,
    la.LOAN_NUMBER,
    b.FIRST_NAME || ' ' || b.LAST_NAME AS BORROWER_NAME,
    la.LOAN_AMOUNT,
    la.LOAN_TYPE,
    la.STATUS AS LOAN_STATUS,
    cal.AUDIT_ID,
    cal.AUDIT_TYPE,
    cal.AUDIT_SUBTYPE,
    cal.AUDIT_RESULT,
    cal.AUDIT_SCORE,
    cal.FINDING_DESCRIPTION,
    cal.ACTUAL_VALUE,
    cal.EXPECTED_VALUE,
    cal.VARIANCE,
    cal.AUDIT_DATE,
    cal.AUDITOR_TYPE,
    cal.REVIEWED,
    cal.REVIEWED_BY,
    cr.RULE_NAME,
    cr.RULE_CATEGORY,
    cr.SEVERITY_IF_FAILED,
    cr.REGULATION_CITE,
    (SELECT COUNT(*) FROM LOAN_DEFECTS ld
     WHERE ld.LOAN_ID = la.LOAN_ID AND ld.SEVERITY = 'FATAL' AND ld.STATUS = 'OPEN') AS FATAL_DEFECTS,
    (SELECT COUNT(*) FROM LOAN_DEFECTS ld
     WHERE ld.LOAN_ID = la.LOAN_ID AND ld.SEVERITY = 'MODERATE' AND ld.STATUS = 'OPEN') AS MODERATE_DEFECTS,
    (SELECT COUNT(*) FROM LOAN_DEFECTS ld
     WHERE ld.LOAN_ID = la.LOAN_ID AND ld.SEVERITY = 'LOW' AND ld.STATUS = 'OPEN') AS LOW_DEFECTS
FROM LOAN_APPLICATIONS la
JOIN BORROWERS b ON la.BORROWER_ID = b.BORROWER_ID
LEFT JOIN COMPLIANCE_AUDIT_LOG cal ON la.LOAN_ID = cal.LOAN_ID
LEFT JOIN COMPLIANCE_RULES cr ON cal.RULE_ID = cr.RULE_ID
ORDER BY cal.AUDIT_DATE DESC;

COMMENT ON TABLE V_COMPLIANCE_AUDIT IS 'Compliance audit results with defect counts by severity';
