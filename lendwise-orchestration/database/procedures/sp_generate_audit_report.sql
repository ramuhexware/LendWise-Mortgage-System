-- ============================================================================
-- LendWise Mortgage System - Audit Report Generation Procedure
-- Generates compliance audit report for a loan application
-- ============================================================================

CREATE OR REPLACE PROCEDURE SP_GENERATE_AUDIT_REPORT (
    p_loan_id       IN NUMBER,
    p_audit_type    IN VARCHAR2 DEFAULT 'COMPREHENSIVE',
    p_auditor_id    IN VARCHAR2 DEFAULT 'SYSTEM',
    p_overall_result OUT VARCHAR2,
    p_status        OUT VARCHAR2,
    p_message       OUT VARCHAR2
)
AS
    v_loan_exists       NUMBER;
    v_audit_id          NUMBER;
    v_pass_count        NUMBER := 0;
    v_fail_count        NUMBER := 0;
    v_warning_count     NUMBER := 0;
    v_le_issue_date     DATE;
    v_cd_issue_date     DATE;
    v_application_date  DATE;
    v_closing_date      DATE;
    v_dti_ratio         NUMBER;
    v_business_days     NUMBER;

    CURSOR c_rules IS
        SELECT RULE_ID, RULE_NAME, RULE_CATEGORY, RULE_TYPE,
               THRESHOLD_VALUE, THRESHOLD_TYPE, SEVERITY_IF_FAILED
        FROM COMPLIANCE_RULES
        WHERE IS_ACTIVE = 'Y'
        AND (p_audit_type = 'COMPREHENSIVE' OR RULE_CATEGORY = p_audit_type);

BEGIN
    p_status := 'SUCCESS';
    p_message := '';
    p_overall_result := 'PASS';

    -- Verify loan exists
    SELECT COUNT(*) INTO v_loan_exists
    FROM LOAN_APPLICATIONS WHERE LOAN_ID = p_loan_id;

    IF v_loan_exists = 0 THEN
        p_status := 'ERROR';
        p_message := 'Loan ID not found: ' || p_loan_id;
        RETURN;
    END IF;

    -- Get loan dates and DTI
    SELECT CREATED_DATE, DTI_RATIO
    INTO v_application_date, v_dti_ratio
    FROM LOAN_APPLICATIONS
    WHERE LOAN_ID = p_loan_id;

    -- Get disclosure dates
    BEGIN
        SELECT ISSUE_DATE INTO v_le_issue_date
        FROM DISCLOSURE_TRACKING
        WHERE LOAN_ID = p_loan_id
        AND DISCLOSURE_TYPE = 'LOAN_ESTIMATE'
        AND VERSION_NUMBER = 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN v_le_issue_date := NULL;
    END;

    BEGIN
        SELECT ISSUE_DATE, CLOSING_DATE INTO v_cd_issue_date, v_closing_date
        FROM CLOSING_DISCLOSURES cd
        WHERE cd.LOAN_ID = p_loan_id
        AND cd.VERSION_NUMBER = (SELECT MAX(VERSION_NUMBER) FROM CLOSING_DISCLOSURES WHERE LOAN_ID = p_loan_id);
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_cd_issue_date := NULL;
            v_closing_date := NULL;
    END;

    -- Process each compliance rule
    FOR r IN c_rules LOOP
        v_audit_id := SEQ_AUDIT_ID.NEXTVAL;

        DECLARE
            v_result        VARCHAR2(20) := 'PASS';
            v_actual        VARCHAR2(100);
            v_expected      VARCHAR2(100);
            v_variance      NUMBER := 0;
            v_finding       VARCHAR2(2000);
        BEGIN
            -- TRID LE Timing Check
            IF r.RULE_ID = 'TRID001' THEN
                IF v_le_issue_date IS NOT NULL THEN
                    v_business_days := v_le_issue_date - v_application_date;
                    v_actual := TO_CHAR(v_business_days) || ' days';
                    v_expected := '<= ' || r.THRESHOLD_VALUE || ' days';
                    IF v_business_days > r.THRESHOLD_VALUE THEN
                        v_result := 'FAIL';
                        v_finding := 'LE delivered ' || v_business_days || ' days after application, exceeds 3 business day requirement';
                    END IF;
                ELSE
                    v_result := 'NOT_APPLICABLE';
                    v_finding := 'No Loan Estimate on file';
                END IF;

            -- TRID CD Timing Check
            ELSIF r.RULE_ID = 'TRID002' THEN
                IF v_cd_issue_date IS NOT NULL AND v_closing_date IS NOT NULL THEN
                    v_business_days := v_closing_date - v_cd_issue_date;
                    v_actual := TO_CHAR(v_business_days) || ' days before closing';
                    v_expected := '>= ' || r.THRESHOLD_VALUE || ' days';
                    IF v_business_days < r.THRESHOLD_VALUE THEN
                        v_result := 'FAIL';
                        v_finding := 'CD delivered only ' || v_business_days || ' days before closing, minimum 3 required';
                    END IF;
                ELSE
                    v_result := 'NOT_APPLICABLE';
                    v_finding := 'CD or closing date not yet established';
                END IF;

            -- QM DTI Check
            ELSIF r.RULE_ID = 'QM001' THEN
                IF v_dti_ratio IS NOT NULL THEN
                    v_actual := TO_CHAR(v_dti_ratio) || '%';
                    v_expected := '<= ' || r.THRESHOLD_VALUE || '%';
                    v_variance := v_dti_ratio - r.THRESHOLD_VALUE;
                    IF v_dti_ratio > r.THRESHOLD_VALUE THEN
                        v_result := 'FAIL';
                        v_finding := 'DTI of ' || v_dti_ratio || '% exceeds QM limit of 43%';
                    END IF;
                ELSE
                    v_result := 'WARNING';
                    v_finding := 'DTI ratio not calculated';
                END IF;

            -- Default pass for other rules
            ELSE
                v_result := 'PASS';
            END IF;

            -- Insert audit record
            INSERT INTO COMPLIANCE_AUDIT_LOG (
                AUDIT_ID, LOAN_ID, RULE_ID, AUDIT_TYPE, AUDIT_SUBTYPE,
                AUDIT_RESULT, AUDIT_DETAILS, FINDING_DESCRIPTION,
                ACTUAL_VALUE, EXPECTED_VALUE, VARIANCE,
                AUDIT_DATE, AUDITOR_ID, AUDITOR_TYPE
            ) VALUES (
                v_audit_id, p_loan_id, r.RULE_ID, r.RULE_CATEGORY, r.RULE_TYPE,
                v_result, NULL, v_finding,
                v_actual, v_expected, v_variance,
                SYSTIMESTAMP, p_auditor_id, 'SYSTEM'
            );

            -- Track results
            IF v_result = 'PASS' THEN
                v_pass_count := v_pass_count + 1;
            ELSIF v_result = 'FAIL' THEN
                v_fail_count := v_fail_count + 1;

                -- Create defect for failures
                INSERT INTO LOAN_DEFECTS (
                    DEFECT_ID, LOAN_ID, AUDIT_ID, DEFECT_CODE, DEFECT_CATEGORY,
                    SEVERITY, DESCRIPTION, STATUS, IDENTIFIED_BY, IDENTIFIED_DATE
                ) VALUES (
                    SEQ_DEFECT_ID.NEXTVAL, p_loan_id, v_audit_id, r.RULE_ID,
                    r.RULE_CATEGORY, r.SEVERITY_IF_FAILED, v_finding,
                    'OPEN', p_auditor_id, SYSTIMESTAMP
                );
            ELSIF v_result = 'WARNING' THEN
                v_warning_count := v_warning_count + 1;
            END IF;
        END;
    END LOOP;

    COMMIT;

    -- Determine overall result
    IF v_fail_count > 0 THEN
        p_overall_result := 'FAIL';
    ELSIF v_warning_count > 0 THEN
        p_overall_result := 'WARNING';
    ELSE
        p_overall_result := 'PASS';
    END IF;

    p_message := 'Audit complete. Pass: ' || v_pass_count ||
                 ', Fail: ' || v_fail_count ||
                 ', Warnings: ' || v_warning_count;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_status := 'ERROR';
        p_message := 'Error generating audit report: ' || SQLERRM;
END SP_GENERATE_AUDIT_REPORT;
/

COMMENT ON PROCEDURE SP_GENERATE_AUDIT_REPORT IS 'Generates compliance audit report and creates defects for failures';
