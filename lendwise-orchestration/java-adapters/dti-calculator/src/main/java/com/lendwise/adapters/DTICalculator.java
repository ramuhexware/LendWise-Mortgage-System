package com.lendwise.adapters;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTI Calculator - Java Adapter for BPEL Java Embedding
 * Calculates Debt-to-Income (DTI), Loan-to-Value (LTV), and PITI ratios
 * for mortgage loan pre-qualification.
 */
public class DTICalculator {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Calculates the Debt-to-Income ratio.
     * DTI = (Total Monthly Debt / Gross Monthly Income) * 100
     *
     * @param monthlyIncome Gross monthly income
     * @param monthlyDebt Total monthly debt obligations
     * @return DTI ratio as a percentage
     */
    public double calculateDTI(double monthlyIncome, double monthlyDebt) {
        if (monthlyIncome <= 0) {
            return 0.0;
        }
        BigDecimal income = BigDecimal.valueOf(monthlyIncome);
        BigDecimal debt = BigDecimal.valueOf(monthlyDebt);
        return debt.divide(income, SCALE, ROUNDING)
                   .multiply(BigDecimal.valueOf(100))
                   .setScale(2, ROUNDING)
                   .doubleValue();
    }

    /**
     * Calculates the Front-End DTI (Housing Ratio).
     * Front-End DTI = (PITI / Gross Monthly Income) * 100
     *
     * @param monthlyIncome Gross monthly income
     * @param proposedPITI Proposed monthly housing payment (Principal + Interest + Taxes + Insurance)
     * @return Front-end DTI ratio as a percentage
     */
    public double calculateFrontEndDTI(double monthlyIncome, double proposedPITI) {
        if (monthlyIncome <= 0) {
            return 0.0;
        }
        BigDecimal income = BigDecimal.valueOf(monthlyIncome);
        BigDecimal piti = BigDecimal.valueOf(proposedPITI);
        return piti.divide(income, SCALE, ROUNDING)
                   .multiply(BigDecimal.valueOf(100))
                   .setScale(2, ROUNDING)
                   .doubleValue();
    }

    /**
     * Calculates the Back-End DTI (Total Debt Ratio).
     * Back-End DTI = (PITI + Other Monthly Debts) / Gross Monthly Income * 100
     *
     * @param monthlyIncome Gross monthly income
     * @param proposedPITI Proposed monthly housing payment
     * @param otherMonthlyDebt Other monthly debt obligations (car payments, credit cards, etc.)
     * @return Back-end DTI ratio as a percentage
     */
    public double calculateBackEndDTI(double monthlyIncome, double proposedPITI, double otherMonthlyDebt) {
        if (monthlyIncome <= 0) {
            return 0.0;
        }
        BigDecimal income = BigDecimal.valueOf(monthlyIncome);
        BigDecimal totalDebt = BigDecimal.valueOf(proposedPITI + otherMonthlyDebt);
        return totalDebt.divide(income, SCALE, ROUNDING)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, ROUNDING)
                        .doubleValue();
    }

    /**
     * Calculates the Loan-to-Value ratio.
     * LTV = (Loan Amount / Property Value) * 100
     *
     * @param loanAmount The loan amount
     * @param propertyValue The appraised property value
     * @return LTV ratio as a percentage
     */
    public double calculateLTV(double loanAmount, double propertyValue) {
        if (propertyValue <= 0) {
            return 0.0;
        }
        BigDecimal loan = BigDecimal.valueOf(loanAmount);
        BigDecimal value = BigDecimal.valueOf(propertyValue);
        return loan.divide(value, SCALE, ROUNDING)
                   .multiply(BigDecimal.valueOf(100))
                   .setScale(2, ROUNDING)
                   .doubleValue();
    }

    /**
     * Calculates the Combined Loan-to-Value ratio.
     * CLTV = (All Loan Amounts / Property Value) * 100
     *
     * @param firstMortgage Primary loan amount
     * @param subordinateLoans Total of any subordinate financing
     * @param propertyValue The appraised property value
     * @return CLTV ratio as a percentage
     */
    public double calculateCLTV(double firstMortgage, double subordinateLoans, double propertyValue) {
        if (propertyValue <= 0) {
            return 0.0;
        }
        BigDecimal totalLoans = BigDecimal.valueOf(firstMortgage + subordinateLoans);
        BigDecimal value = BigDecimal.valueOf(propertyValue);
        return totalLoans.divide(value, SCALE, ROUNDING)
                         .multiply(BigDecimal.valueOf(100))
                         .setScale(2, ROUNDING)
                         .doubleValue();
    }

    /**
     * Calculates the PITI payment ratio.
     * PITI Ratio = (Monthly PITI / Gross Monthly Income) * 100
     *
     * @param loanAmount The loan amount
     * @param monthlyIncome Gross monthly income
     * @return PITI ratio as a percentage (simplified calculation)
     */
    public double calculatePITI(double loanAmount, double monthlyIncome) {
        if (monthlyIncome <= 0) {
            return 0.0;
        }
        // Simplified PITI estimate: ~0.5% of loan amount per month
        double estimatedMonthlyPayment = loanAmount * 0.005;
        BigDecimal payment = BigDecimal.valueOf(estimatedMonthlyPayment);
        BigDecimal income = BigDecimal.valueOf(monthlyIncome);
        return payment.divide(income, SCALE, ROUNDING)
                      .multiply(BigDecimal.valueOf(100))
                      .setScale(2, ROUNDING)
                      .doubleValue();
    }

    /**
     * Calculates the monthly Principal and Interest payment.
     * Uses the standard amortization formula: M = P[c(1+c)^n]/[(1+c)^n-1]
     *
     * @param principal The loan principal amount
     * @param annualRate Annual interest rate as a decimal (e.g., 0.065 for 6.5%)
     * @param termMonths Loan term in months
     * @return Monthly P&I payment
     */
    public double calculateMonthlyPI(double principal, double annualRate, int termMonths) {
        if (principal <= 0 || termMonths <= 0) {
            return 0.0;
        }
        if (annualRate <= 0) {
            return principal / termMonths;
        }

        double monthlyRate = annualRate / 12;
        double factor = Math.pow(1 + monthlyRate, termMonths);
        double payment = principal * (monthlyRate * factor) / (factor - 1);

        return BigDecimal.valueOf(payment)
                         .setScale(2, ROUNDING)
                         .doubleValue();
    }

    /**
     * Calculates full PITI payment including taxes and insurance estimates.
     *
     * @param principal The loan principal amount
     * @param annualRate Annual interest rate as a decimal
     * @param termMonths Loan term in months
     * @param propertyValue Property value for tax/insurance estimates
     * @param annualTaxRate Annual property tax rate (default 1.2%)
     * @param annualInsuranceRate Annual insurance rate (default 0.5%)
     * @return Monthly PITI payment
     */
    public double calculateFullPITI(double principal, double annualRate, int termMonths,
                                     double propertyValue, double annualTaxRate, double annualInsuranceRate) {
        double monthlyPI = calculateMonthlyPI(principal, annualRate, termMonths);
        double monthlyTaxes = (propertyValue * annualTaxRate) / 12;
        double monthlyInsurance = (propertyValue * annualInsuranceRate) / 12;

        return BigDecimal.valueOf(monthlyPI + monthlyTaxes + monthlyInsurance)
                         .setScale(2, ROUNDING)
                         .doubleValue();
    }

    /**
     * Evaluates if the loan meets QM (Qualified Mortgage) DTI requirements.
     *
     * @param dtiRatio The calculated DTI ratio
     * @return true if DTI is within QM limits (43%), false otherwise
     */
    public boolean isQMCompliant(double dtiRatio) {
        return dtiRatio <= 43.0;
    }

    /**
     * Gets the maximum loan amount based on income and target DTI.
     *
     * @param monthlyIncome Gross monthly income
     * @param targetDTI Target DTI ratio (e.g., 43.0 for QM)
     * @param existingDebt Existing monthly debt obligations
     * @param annualRate Annual interest rate
     * @param termMonths Loan term in months
     * @return Maximum affordable loan amount
     */
    public double getMaxLoanAmount(double monthlyIncome, double targetDTI,
                                   double existingDebt, double annualRate, int termMonths) {
        double maxTotalDebt = monthlyIncome * (targetDTI / 100);
        double availableForHousing = maxTotalDebt - existingDebt;

        if (availableForHousing <= 0 || annualRate <= 0) {
            return 0.0;
        }

        // Reverse the P&I calculation to find max principal
        double monthlyRate = annualRate / 12;
        double factor = Math.pow(1 + monthlyRate, termMonths);
        double maxPrincipal = availableForHousing * (factor - 1) / (monthlyRate * factor);

        return BigDecimal.valueOf(maxPrincipal)
                         .setScale(2, ROUNDING)
                         .doubleValue();
    }
}
