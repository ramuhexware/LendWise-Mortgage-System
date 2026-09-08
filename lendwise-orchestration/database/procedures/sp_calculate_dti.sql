-- ============================================================================
-- LendWise Mortgage System - DTI Calculation Procedure
-- Calculates Debt-to-Income ratio for a loan application
-- ============================================================================

CREATE OR REPLACE PROCEDURE SP_CALCULATE_DTI (
    p_loan_id       IN NUMBER,
    p_dti_ratio     OUT NUMBER,
    p_front_end_dti OUT NUMBER,
    p_back_end_dti  OUT NUMBER,
    p_status        OUT VARCHAR2,
    p_message       OUT VARCHAR2
)
AS
    v_monthly_income    NUMBER := 0;
    v_monthly_debt      NUMBER := 0;
    v_housing_expense   NUMBER := 0;
    v_proposed_piti     NUMBER := 0;
    v_borrower_id       NUMBER;
    v_loan_amount       NUMBER;
    v_property_value    NUMBER;
    v_interest_rate     NUMBER := 0.065; -- Default 6.5%
    v_loan_term         NUMBER := 360;   -- Default 30 years
BEGIN
    p_status := 'SUCCESS';
    p_message := '';

    -- Get loan and borrower information
    BEGIN
        SELECT la.BORROWER_ID, la.LOAN_AMOUNT, la.PROPERTY_VALUE, la.LOAN_TERM
        INTO v_borrower_id, v_loan_amount, v_property_value, v_loan_term
        FROM LOAN_APPLICATIONS la
        WHERE la.LOAN_ID = p_loan_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_status := 'ERROR';
            p_message := 'Loan ID not found: ' || p_loan_id;
            RETURN;
    END;

    -- Calculate total monthly income from all employment records
    SELECT NVL(SUM(MONTHLY_INCOME + NVL(BONUS_INCOME, 0)/12 + NVL(COMMISSION_INCOME, 0)/12 + NVL(OVERTIME_INCOME, 0)/12), 0)
    INTO v_monthly_income
    FROM BORROWER_EMPLOYMENT
    WHERE BORROWER_ID = v_borrower_id
    AND IS_CURRENT = 'Y';

    IF v_monthly_income <= 0 THEN
        p_status := 'ERROR';
        p_message := 'No income records found for borrower';
        RETURN;
    END IF;

    -- Calculate total monthly debt from liabilities
    SELECT NVL(SUM(MONTHLY_PAYMENT), 0)
    INTO v_monthly_debt
    FROM BORROWER_LIABILITIES
    WHERE BORROWER_ID = v_borrower_id
    AND (WILL_BE_PAID_OFF = 'N' OR WILL_BE_PAID_OFF IS NULL);

    -- Calculate current housing expense
    SELECT NVL(MONTHLY_PAYMENT, 0)
    INTO v_housing_expense
    FROM BORROWER_ADDRESSES
    WHERE BORROWER_ID = v_borrower_id
    AND ADDRESS_TYPE = 'CURRENT'
    AND ROWNUM = 1;

    -- Calculate proposed PITI (Principal + Interest + Taxes + Insurance)
    -- Monthly P&I = L[c(1+c)^n]/[(1+c)^n-1]
    DECLARE
        v_monthly_rate NUMBER := v_interest_rate / 12;
        v_num_payments NUMBER := NVL(v_loan_term, 360);
        v_monthly_pi   NUMBER;
        v_monthly_tax  NUMBER;
        v_monthly_ins  NUMBER;
    BEGIN
        -- Calculate monthly P&I
        v_monthly_pi := v_loan_amount *
            (v_monthly_rate * POWER(1 + v_monthly_rate, v_num_payments)) /
            (POWER(1 + v_monthly_rate, v_num_payments) - 1);

        -- Estimate monthly taxes (1.2% of property value annually)
        v_monthly_tax := (v_property_value * 0.012) / 12;

        -- Estimate monthly insurance (0.5% of property value annually)
        v_monthly_ins := (v_property_value * 0.005) / 12;

        v_proposed_piti := v_monthly_pi + v_monthly_tax + v_monthly_ins;
    END;

    -- Calculate Front-End DTI (Housing Ratio)
    -- Front-End DTI = Proposed PITI / Monthly Income
    p_front_end_dti := ROUND((v_proposed_piti / v_monthly_income) * 100, 2);

    -- Calculate Back-End DTI (Total Debt Ratio)
    -- Back-End DTI = (Proposed PITI + Other Monthly Debt) / Monthly Income
    p_back_end_dti := ROUND(((v_proposed_piti + v_monthly_debt) / v_monthly_income) * 100, 2);

    -- Standard DTI is typically the back-end ratio
    p_dti_ratio := p_back_end_dti;

    -- Update loan application with calculated ratios
    UPDATE LOAN_APPLICATIONS
    SET DTI_RATIO = p_dti_ratio,
        LTV_RATIO = ROUND((v_loan_amount / v_property_value) * 100, 2),
        UPDATED_DATE = SYSTIMESTAMP
    WHERE LOAN_ID = p_loan_id;

    COMMIT;

    p_message := 'DTI calculated successfully. Front-End: ' || p_front_end_dti ||
                 '%, Back-End: ' || p_back_end_dti || '%';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_status := 'ERROR';
        p_message := 'Error calculating DTI: ' || SQLERRM;
END SP_CALCULATE_DTI;
/

COMMENT ON PROCEDURE SP_CALCULATE_DTI IS 'Calculates front-end and back-end DTI ratios for a loan application';
