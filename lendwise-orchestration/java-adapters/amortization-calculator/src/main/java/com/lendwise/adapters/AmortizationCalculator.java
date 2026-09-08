package com.lendwise.adapters;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Amortization Calculator - Java Adapter for BPEL Java Embedding
 * Generates amortization schedules and analyzes loan payment scenarios.
 */
public class AmortizationCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Generates a complete amortization schedule.
     *
     * @param principal Loan principal amount
     * @param annualRate Annual interest rate as decimal (e.g., 0.065 for 6.5%)
     * @param termMonths Loan term in months
     * @param startDate First payment date
     * @return List of payment records with details
     */
    public List<Map<String, Object>> generateSchedule(double principal, double annualRate,
                                                       int termMonths, LocalDate startDate) {
        List<Map<String, Object>> schedule = new ArrayList<>();

        double monthlyRate = annualRate / 12;
        double monthlyPayment = calculateMonthlyPayment(principal, annualRate, termMonths);
        double balance = principal;
        double cumulativeInterest = 0;
        double cumulativePrincipal = 0;

        LocalDate paymentDate = startDate;

        for (int payment = 1; payment <= termMonths && balance > 0.01; payment++) {
            Map<String, Object> record = new HashMap<>();

            double interestPortion = balance * monthlyRate;
            double principalPortion = monthlyPayment - interestPortion;

            // Handle final payment rounding
            if (payment == termMonths || principalPortion > balance) {
                principalPortion = balance;
                monthlyPayment = principalPortion + interestPortion;
            }

            double endingBalance = balance - principalPortion;
            cumulativeInterest += interestPortion;
            cumulativePrincipal += principalPortion;

            record.put("paymentNumber", payment);
            record.put("paymentDate", paymentDate.toString());
            record.put("beginningBalance", round(balance));
            record.put("paymentAmount", round(monthlyPayment));
            record.put("principalAmount", round(principalPortion));
            record.put("interestAmount", round(interestPortion));
            record.put("endingBalance", round(Math.max(0, endingBalance)));
            record.put("cumulativeInterest", round(cumulativeInterest));
            record.put("cumulativePrincipal", round(cumulativePrincipal));

            schedule.add(record);

            balance = endingBalance;
            paymentDate = paymentDate.plusMonths(1);
        }

        return schedule;
    }

    /**
     * Calculates the monthly payment using standard amortization formula.
     * M = P[c(1+c)^n]/[(1+c)^n-1]
     *
     * @param principal Loan principal
     * @param annualRate Annual interest rate as decimal
     * @param termMonths Loan term in months
     * @return Monthly payment amount
     */
    public double calculateMonthlyPayment(double principal, double annualRate, int termMonths) {
        if (principal <= 0 || termMonths <= 0) {
            return 0.0;
        }
        if (annualRate <= 0) {
            return round(principal / termMonths);
        }

        double monthlyRate = annualRate / 12;
        double factor = Math.pow(1 + monthlyRate, termMonths);
        double payment = principal * (monthlyRate * factor) / (factor - 1);

        return round(payment);
    }

    /**
     * Calculates total interest paid over the life of the loan.
     *
     * @param principal Loan principal
     * @param annualRate Annual interest rate
     * @param termMonths Loan term in months
     * @return Total interest paid
     */
    public double calculateTotalInterest(double principal, double annualRate, int termMonths) {
        double monthlyPayment = calculateMonthlyPayment(principal, annualRate, termMonths);
        double totalPayments = monthlyPayment * termMonths;
        return round(totalPayments - principal);
    }

    /**
     * Calculates total of all payments over the loan term.
     *
     * @param principal Loan principal
     * @param annualRate Annual interest rate
     * @param termMonths Loan term in months
     * @return Total payments
     */
    public double calculateTotalPayments(double principal, double annualRate, int termMonths) {
        double monthlyPayment = calculateMonthlyPayment(principal, annualRate, termMonths);
        return round(monthlyPayment * termMonths);
    }

    /**
     * Analyzes impact of extra monthly payments.
     *
     * @param principal Loan principal
     * @param annualRate Annual interest rate
     * @param termMonths Original loan term
     * @param extraMonthlyPayment Additional monthly payment amount
     * @return Analysis results including new term and savings
     */
    public Map<String, Object> analyzeExtraPayments(double principal, double annualRate,
                                                     int termMonths, double extraMonthlyPayment) {
        Map<String, Object> analysis = new HashMap<>();

        double monthlyRate = annualRate / 12;
        double standardPayment = calculateMonthlyPayment(principal, annualRate, termMonths);
        double newPayment = standardPayment + extraMonthlyPayment;

        // Calculate original totals
        double originalTotalInterest = calculateTotalInterest(principal, annualRate, termMonths);

        // Simulate accelerated payoff
        double balance = principal;
        int newTermMonths = 0;
        double newTotalInterest = 0;

        while (balance > 0.01 && newTermMonths < termMonths) {
            double interestPortion = balance * monthlyRate;
            double principalPortion = Math.min(newPayment - interestPortion, balance);

            newTotalInterest += interestPortion;
            balance -= principalPortion;
            newTermMonths++;
        }

        double interestSavings = originalTotalInterest - newTotalInterest;
        int monthsSaved = termMonths - newTermMonths;

        analysis.put("originalTermMonths", termMonths);
        analysis.put("originalMonthlyPayment", standardPayment);
        analysis.put("originalTotalInterest", round(originalTotalInterest));
        analysis.put("newMonthlyPayment", round(newPayment));
        analysis.put("newTermMonths", newTermMonths);
        analysis.put("newTotalInterest", round(newTotalInterest));
        analysis.put("interestSavings", round(interestSavings));
        analysis.put("monthsSaved", monthsSaved);
        analysis.put("yearsSaved", round(monthsSaved / 12.0));

        return analysis;
    }

    /**
     * Compares different loan scenarios side by side.
     *
     * @param principal Loan principal
     * @param scenarios List of scenarios, each containing rate and term
     * @return Comparison results
     */
    public List<Map<String, Object>> compareScenarios(double principal, List<Map<String, Object>> scenarios) {
        List<Map<String, Object>> comparisons = new ArrayList<>();

        for (Map<String, Object> scenario : scenarios) {
            double rate = (Double) scenario.get("annualRate");
            int term = (Integer) scenario.get("termMonths");
            String name = (String) scenario.getOrDefault("name", "Scenario");

            Map<String, Object> result = new HashMap<>();
            result.put("scenarioName", name);
            result.put("principal", principal);
            result.put("annualRate", rate);
            result.put("termMonths", term);
            result.put("monthlyPayment", calculateMonthlyPayment(principal, rate, term));
            result.put("totalInterest", calculateTotalInterest(principal, rate, term));
            result.put("totalPayments", calculateTotalPayments(principal, rate, term));

            comparisons.add(result);
        }

        return comparisons;
    }

    /**
     * Calculates the remaining balance at a specific payment number.
     *
     * @param principal Original loan principal
     * @param annualRate Annual interest rate
     * @param termMonths Original loan term
     * @param paymentNumber Payment number to calculate balance at
     * @return Remaining balance
     */
    public double calculateBalanceAtPayment(double principal, double annualRate,
                                            int termMonths, int paymentNumber) {
        if (paymentNumber <= 0) {
            return principal;
        }
        if (paymentNumber >= termMonths) {
            return 0.0;
        }

        double monthlyRate = annualRate / 12;
        double factor = Math.pow(1 + monthlyRate, termMonths);
        double paymentFactor = Math.pow(1 + monthlyRate, paymentNumber);

        double balance = principal * (factor - paymentFactor) / (factor - 1);
        return round(balance);
    }

    /**
     * Calculates how much of a specific payment goes to principal vs interest.
     *
     * @param principal Original loan principal
     * @param annualRate Annual interest rate
     * @param termMonths Loan term
     * @param paymentNumber Payment number to analyze
     * @return Map with principal and interest portions
     */
    public Map<String, Double> getPaymentBreakdown(double principal, double annualRate,
                                                    int termMonths, int paymentNumber) {
        Map<String, Double> breakdown = new HashMap<>();

        double monthlyPayment = calculateMonthlyPayment(principal, annualRate, termMonths);
        double balance = calculateBalanceAtPayment(principal, annualRate, termMonths, paymentNumber - 1);
        double monthlyRate = annualRate / 12;

        double interestPortion = balance * monthlyRate;
        double principalPortion = monthlyPayment - interestPortion;

        breakdown.put("payment", monthlyPayment);
        breakdown.put("principal", round(principalPortion));
        breakdown.put("interest", round(interestPortion));
        breakdown.put("principalPercent", round((principalPortion / monthlyPayment) * 100));
        breakdown.put("interestPercent", round((interestPortion / monthlyPayment) * 100));

        return breakdown;
    }

    /**
     * Calculates the Annual Percentage Rate (APR) including closing costs.
     *
     * @param principal Loan principal
     * @param annualRate Nominal annual interest rate
     * @param termMonths Loan term
     * @param closingCosts Total closing costs financed
     * @return APR as a decimal
     */
    public double calculateAPR(double principal, double annualRate, int termMonths, double closingCosts) {
        double totalFinanced = principal + closingCosts;
        double monthlyPayment = calculateMonthlyPayment(principal, annualRate, termMonths);

        // Use Newton-Raphson method to find APR
        double aprGuess = annualRate;
        double tolerance = 0.00001;
        int maxIterations = 100;

        for (int i = 0; i < maxIterations; i++) {
            double monthlyRate = aprGuess / 12;
            double factor = Math.pow(1 + monthlyRate, termMonths);
            double pv = monthlyPayment * (factor - 1) / (monthlyRate * factor);

            double error = pv - totalFinanced;

            if (Math.abs(error) < tolerance) {
                break;
            }

            // Adjust guess
            aprGuess += error / totalFinanced * 0.1;
        }

        return round(aprGuess * 100) / 100; // Return as decimal with 4 decimal places
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                         .setScale(SCALE, ROUNDING)
                         .doubleValue();
    }
}
