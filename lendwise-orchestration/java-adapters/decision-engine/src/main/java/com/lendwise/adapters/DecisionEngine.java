package com.lendwise.adapters;

import java.util.ArrayList;
import java.util.List;

/**
 * Decision Engine - Java Adapter for BPEL Java Embedding
 * Automated Underwriting System (AUS) decision engine for loan evaluation.
 * Implements rules-based decisioning for mortgage loan approvals.
 */
public class DecisionEngine {

    // Decision Results
    public static final String APPROVE = "APPROVE";
    public static final String APPROVE_ELIGIBLE = "APPROVE_ELIGIBLE";
    public static final String REFER = "REFER";
    public static final String REFER_CAUTION = "REFER_CAUTION";
    public static final String DECLINE = "DECLINE";

    // Condition Types
    public static final String PTD = "PTD"; // Prior to Documents
    public static final String PTF = "PTF"; // Prior to Funding

    // Threshold Constants
    private static final int MIN_FICO_APPROVE = 700;
    private static final int MIN_FICO_ELIGIBLE = 660;
    private static final int MIN_FICO_REFER = 620;
    private static final double MAX_DTI_APPROVE = 36.0;
    private static final double MAX_DTI_ELIGIBLE = 43.0;
    private static final double MAX_DTI_REFER = 50.0;
    private static final double MAX_LTV_CONVENTIONAL = 97.0;
    private static final double MAX_LTV_HIGH_BALANCE = 95.0;

    /**
     * Evaluates a loan application and returns a decision.
     *
     * @param ficoScore Representative FICO score
     * @param dtiRatio Debt-to-Income ratio
     * @param ltvRatio Loan-to-Value ratio
     * @return Decision result (APPROVE, APPROVE_ELIGIBLE, REFER, REFER_CAUTION, DECLINE)
     */
    public String evaluateLoan(int ficoScore, double dtiRatio, double ltvRatio) {
        // Immediate decline conditions
        if (ficoScore < MIN_FICO_REFER) {
            return DECLINE;
        }
        if (dtiRatio > MAX_DTI_REFER) {
            return DECLINE;
        }
        if (ltvRatio > MAX_LTV_CONVENTIONAL) {
            return DECLINE;
        }

        // Calculate risk score
        double riskScore = calculateRiskScore(ficoScore, dtiRatio, ltvRatio);

        // Strong approval
        if (ficoScore >= MIN_FICO_APPROVE && dtiRatio <= MAX_DTI_APPROVE && ltvRatio <= 80.0) {
            return APPROVE;
        }

        // Eligible with conditions
        if (ficoScore >= MIN_FICO_ELIGIBLE && dtiRatio <= MAX_DTI_ELIGIBLE) {
            return APPROVE_ELIGIBLE;
        }

        // Refer with caution
        if (riskScore > 60) {
            return REFER_CAUTION;
        }

        // Refer for manual review
        return REFER;
    }

    /**
     * Evaluates loan with additional factors.
     *
     * @param ficoScore Representative FICO score
     * @param dtiRatio Debt-to-Income ratio
     * @param ltvRatio Loan-to-Value ratio
     * @param loanType Loan type (CONVENTIONAL, FHA, VA, USDA)
     * @param occupancy Occupancy type (PRIMARY, SECONDARY, INVESTMENT)
     * @param propertyType Property type (SINGLE_FAMILY, CONDO, MULTI_FAMILY)
     * @return Decision result
     */
    public String evaluateLoanExtended(int ficoScore, double dtiRatio, double ltvRatio,
                                       String loanType, String occupancy, String propertyType) {
        // Adjust thresholds based on loan type
        double maxLtv = getMaxLtvForLoanType(loanType, occupancy);
        double maxDti = getMaxDtiForLoanType(loanType);
        int minFico = getMinFicoForLoanType(loanType);

        // Check against adjusted thresholds
        if (ficoScore < minFico) {
            return DECLINE;
        }
        if (ltvRatio > maxLtv) {
            return DECLINE;
        }

        // Investment properties have stricter requirements
        if ("INVESTMENT".equals(occupancy)) {
            if (ficoScore < 680 || ltvRatio > 85.0) {
                return DECLINE;
            }
            if (dtiRatio > 36.0) {
                return REFER_CAUTION;
            }
        }

        // Multi-family properties
        if ("MULTI_FAMILY".equals(propertyType)) {
            if (ltvRatio > 85.0) {
                return REFER;
            }
        }

        // Standard evaluation
        return evaluateLoan(ficoScore, dtiRatio, ltvRatio);
    }

    /**
     * Generates loan conditions based on the decision and risk factors.
     *
     * @param decision The underwriting decision
     * @param ficoScore Representative FICO score
     * @param dtiRatio Debt-to-Income ratio
     * @param ltvRatio Loan-to-Value ratio
     * @return List of condition descriptions
     */
    public List<String> generateConditions(String decision, int ficoScore, double dtiRatio, double ltvRatio) {
        List<String> conditions = new ArrayList<>();

        // Standard PTD conditions for all approved/eligible loans
        if (APPROVE.equals(decision) || APPROVE_ELIGIBLE.equals(decision) || REFER.equals(decision)) {
            conditions.add("PTD|VOE|Verify current employment within 10 days of closing");
            conditions.add("PTD|VOD|Verify sufficient funds for down payment and closing costs");
        }

        // FICO-based conditions
        if (ficoScore < 680) {
            conditions.add("PTD|CREDIT|Provide letter of explanation for credit inquiries in last 90 days");
        }
        if (ficoScore < 660) {
            conditions.add("PTD|CREDIT|Verify no new credit accounts opened in last 6 months");
            conditions.add("PTD|RESERVES|Verify 6 months reserves after closing");
        }

        // DTI-based conditions
        if (dtiRatio > 41.0) {
            conditions.add("PTD|INCOME|Provide additional income documentation (2 years tax returns)");
        }
        if (dtiRatio > 43.0) {
            conditions.add("PTD|COMPENSATING|Document compensating factors (reserves, residual income)");
        }

        // LTV-based conditions
        if (ltvRatio > 80.0) {
            conditions.add("PTF|PMI|Obtain private mortgage insurance commitment");
        }
        if (ltvRatio > 90.0) {
            conditions.add("PTD|APPRAISAL|Obtain second appraisal or desk review");
        }
        if (ltvRatio > 95.0) {
            conditions.add("PTD|RESERVES|Verify 3 months reserves after closing");
        }

        // Standard PTF conditions
        conditions.add("PTF|TITLE|Clear title with no outstanding liens");
        conditions.add("PTF|INSURANCE|Obtain hazard insurance with mortgagee clause");
        conditions.add("PTF|FLOOD|Obtain flood certification and insurance if required");

        return conditions;
    }

    /**
     * Calculates a risk score for the loan application.
     *
     * @param ficoScore Representative FICO score
     * @param dtiRatio Debt-to-Income ratio
     * @param ltvRatio Loan-to-Value ratio
     * @return Risk score (0-100, higher is better)
     */
    public double calculateRiskScore(int ficoScore, double dtiRatio, double ltvRatio) {
        double ficoScore_normalized = Math.min(100, Math.max(0, (ficoScore - 500) / 3.5));
        double dtiScore = Math.min(100, Math.max(0, (60 - dtiRatio) * 2.5));
        double ltvScore = Math.min(100, Math.max(0, (100 - ltvRatio) * 1.25));

        // Weighted average: FICO 40%, DTI 35%, LTV 25%
        return (ficoScore_normalized * 0.40) + (dtiScore * 0.35) + (ltvScore * 0.25);
    }

    /**
     * Gets the risk grade based on risk score.
     *
     * @param riskScore Calculated risk score
     * @return Risk grade (A, A-, B+, B, B-, C+, C, C-, D)
     */
    public String getRiskGrade(double riskScore) {
        if (riskScore >= 90) return "A";
        if (riskScore >= 85) return "A-";
        if (riskScore >= 80) return "B+";
        if (riskScore >= 75) return "B";
        if (riskScore >= 70) return "B-";
        if (riskScore >= 65) return "C+";
        if (riskScore >= 60) return "C";
        if (riskScore >= 55) return "C-";
        return "D";
    }

    private double getMaxLtvForLoanType(String loanType, String occupancy) {
        if ("FHA".equals(loanType)) return 96.5;
        if ("VA".equals(loanType)) return 100.0;
        if ("USDA".equals(loanType)) return 100.0;
        if ("INVESTMENT".equals(occupancy)) return 85.0;
        if ("SECONDARY".equals(occupancy)) return 90.0;
        return MAX_LTV_CONVENTIONAL;
    }

    private double getMaxDtiForLoanType(String loanType) {
        if ("FHA".equals(loanType)) return 56.9;
        if ("VA".equals(loanType)) return 60.0;
        return MAX_DTI_ELIGIBLE;
    }

    private int getMinFicoForLoanType(String loanType) {
        if ("FHA".equals(loanType)) return 580;
        if ("VA".equals(loanType)) return 580;
        return MIN_FICO_REFER;
    }
}
