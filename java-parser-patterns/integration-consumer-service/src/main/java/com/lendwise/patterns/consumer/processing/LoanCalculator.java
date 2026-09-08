package com.lendwise.patterns.consumer.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Loan calculator with inheritance (extends BaseCalculator) and loop patterns.
 * Parser should detect: extends, @Override, for loops, while loops, do-while loops
 */
@Component
@Slf4j
public class LoanCalculator extends BaseCalculator {

    private static final MathContext PRECISION = new MathContext(10);

    public LoanCalculator() {
        super("LoanCalculator");
    }

    /**
     * Override of abstract method.
     * Parser detects: @Override annotation
     */
    @Override
    public BigDecimal calculate(BigDecimal... inputs) {
        if (inputs.length >= 3) {
            return calculateMonthlyPayment(inputs[0], inputs[1], inputs[2].intValue());
        }
        throw new IllegalArgumentException("Requires principal, rate, and term");
    }

    /**
     * Override of abstract method.
     */
    @Override
    public String getCalculationType() {
        return "LOAN_AMORTIZATION";
    }

    /**
     * Override of hook method with super call.
     * Parser detects: super.method() call
     */
    @Override
    protected void beforeCalculation() {
        super.beforeCalculation();
        log.info("LoanCalculator: Preparing amortization calculation");
    }

    /**
     * Method with traditional FOR loop.
     * Parser should detect: for loop
     */
    public BigDecimal calculateTotalInterest(BigDecimal principal, BigDecimal annualRate, int termMonths) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), PRECISION);
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, annualRate, termMonths);

        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal balance = principal;

        // Parser detects: traditional for loop
        for (int month = 1; month <= termMonths; month++) {
            BigDecimal interestPayment = balance.multiply(monthlyRate);
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment);

            totalInterest = totalInterest.add(interestPayment);
            balance = balance.subtract(principalPayment);

            if (month % 12 == 0) {
                log.debug("Year {}: Remaining balance = {}", month / 12, round(balance));
            }
        }

        return round(totalInterest);
    }

    /**
     * Method with enhanced FOR loop (for-each).
     * Parser should detect: enhanced for loop
     */
    public BigDecimal sumPayments(BigDecimal[] payments) {
        BigDecimal total = BigDecimal.ZERO;

        // Parser detects: enhanced for-each loop
        for (BigDecimal payment : payments) {
            total = total.add(payment);
        }

        return round(total);
    }

    /**
     * Method with WHILE loop.
     * Parser should detect: while loop
     */
    public int calculateMonthsToPayoff(BigDecimal principal, BigDecimal annualRate, BigDecimal monthlyPayment) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), PRECISION);
        BigDecimal balance = principal;
        int months = 0;

        // Parser detects: while loop
        while (balance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal interest = balance.multiply(monthlyRate);
            BigDecimal principalPaid = monthlyPayment.subtract(interest);

            if (principalPaid.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Payment too low to cover interest!");
                return -1;
            }

            balance = balance.subtract(principalPaid);
            months++;

            // Safety limit
            if (months > 1200) {
                log.warn("Exceeded 100 year limit");
                break;
            }
        }

        return months;
    }

    /**
     * Method with DO-WHILE loop.
     * Parser should detect: do-while loop
     */
    public BigDecimal calculatePaymentToReachBalance(BigDecimal principal, BigDecimal annualRate,
                                                      BigDecimal targetBalance, int maxMonths) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), PRECISION);
        BigDecimal minPayment = principal.multiply(monthlyRate);
        BigDecimal maxPayment = principal.divide(BigDecimal.valueOf(12), PRECISION);
        BigDecimal payment = minPayment.add(maxPayment).divide(BigDecimal.valueOf(2), PRECISION);

        int iterations = 0;

        // Parser detects: do-while loop
        do {
            BigDecimal balance = principal;

            for (int month = 0; month < maxMonths && balance.compareTo(targetBalance) > 0; month++) {
                BigDecimal interest = balance.multiply(monthlyRate);
                balance = balance.add(interest).subtract(payment);
            }

            if (balance.compareTo(targetBalance) > 0) {
                minPayment = payment;
            } else {
                maxPayment = payment;
            }

            payment = minPayment.add(maxPayment).divide(BigDecimal.valueOf(2), PRECISION);
            iterations++;

        } while (maxPayment.subtract(minPayment).compareTo(BigDecimal.valueOf(0.01)) > 0 && iterations < 100);

        return round(payment);
    }

    /**
     * Method with nested loops.
     * Parser should detect: nested for loops
     */
    public BigDecimal[][] generateAmortizationTable(BigDecimal principal, BigDecimal annualRate, int termYears) {
        int termMonths = termYears * 12;
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), PRECISION);
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, annualRate, termMonths);
        BigDecimal balance = principal;

        // [month][principal, interest, balance]
        BigDecimal[][] table = new BigDecimal[termMonths][3];

        // Parser detects: outer for loop
        for (int year = 0; year < termYears; year++) {
            log.debug("Generating year {} of amortization", year + 1);

            // Parser detects: inner for loop (nested)
            for (int month = 0; month < 12; month++) {
                int index = year * 12 + month;

                BigDecimal interest = balance.multiply(monthlyRate);
                BigDecimal principalPaid = monthlyPayment.subtract(interest);
                balance = balance.subtract(principalPaid);

                table[index][0] = round(principalPaid);
                table[index][1] = round(interest);
                table[index][2] = round(balance.max(BigDecimal.ZERO));
            }
        }

        return table;
    }

    // Private helper method
    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualRate, int termMonths) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), PRECISION);

        // M = P * [r(1+r)^n] / [(1+r)^n - 1]
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = onePlusR.pow(termMonths, PRECISION);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, PRECISION);
    }
}
