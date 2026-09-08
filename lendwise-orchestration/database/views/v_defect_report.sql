-- ============================================================================
-- LendWise Mortgage System - Defect Report View
-- Provides detailed defect tracking information
-- ============================================================================

CREATE OR REPLACE VIEW V_DEFECT_REPORT AS
SELECT
    ld.DEFECT_ID,
    la.LOAN_ID,
    la.LOAN_NUMBER,
    b.FIRST_NAME || ' ' || b.LAST_NAME AS BORROWER_NAME,
    la.LOAN_AMOUNT,
    la.LOAN_TYPE,
    la.STATUS AS LOAN_STATUS,
    la.STAGE AS LOAN_STAGE,
    ld.DEFECT_CODE,
    ld.DEFECT_CATEGORY,
    ld.DEFECT_SUBCATEGORY,
    ld.SEVERITY,
    ld.DESCRIPTION AS DEFECT_DESCRIPTION,
    ld.IMPACT_ASSESSMENT,
    ld.ROOT_CAUSE,
    ld.STATUS AS DEFECT_STATUS,
    ld.IDENTIFIED_BY,
    ld.IDENTIFIED_DATE,
    ld.ASSIGNED_TO,
    ld.DUE_DATE,
    ld.REMEDIATION_PLAN,
    ld.REMEDIATION_ACTION,
    ld.REMEDIATED_DATE,
    ld.REMEDIATED_BY,
    ld.WAIVED,
    ld.WAIVED_BY,
    ld.WAIVED_DATE,
    ld.WAIVER_REASON,
    CASE
        WHEN ld.STATUS = 'OPEN' AND ld.DUE_DATE < SYSDATE THEN 'OVERDUE'
        WHEN ld.STATUS = 'OPEN' AND ld.DUE_DATE BETWEEN SYSDATE AND SYSDATE + 3 THEN 'DUE_SOON'
        WHEN ld.STATUS = 'OPEN' THEN 'ON_TRACK'
        ELSE ld.STATUS
    END AS DEFECT_PRIORITY_STATUS,
    TRUNC(SYSDATE - ld.IDENTIFIED_DATE) AS DAYS_OPEN
FROM LOAN_DEFECTS ld
JOIN LOAN_APPLICATIONS la ON ld.LOAN_ID = la.LOAN_ID
JOIN BORROWERS b ON la.BORROWER_ID = b.BORROWER_ID
ORDER BY
    CASE ld.SEVERITY WHEN 'FATAL' THEN 1 WHEN 'MODERATE' THEN 2 ELSE 3 END,
    ld.IDENTIFIED_DATE DESC;

COMMENT ON TABLE V_DEFECT_REPORT IS 'Detailed defect report with priority status and aging';
