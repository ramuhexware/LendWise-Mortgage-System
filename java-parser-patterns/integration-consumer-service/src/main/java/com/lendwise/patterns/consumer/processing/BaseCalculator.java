package com.lendwise.patterns.consumer.processing;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Abstract base class for calculator inheritance patterns.
 * Parser should detect: abstract class, abstract methods, protected methods
 */
@Slf4j
public abstract class BaseCalculator {

    protected static final int DEFAULT_SCALE = 4;
    protected static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    protected String calculatorName;
    protected int precision;

    protected BaseCalculator(String name) {
        this.calculatorName = name;
        this.precision = DEFAULT_SCALE;
        log.info("BaseCalculator initialized: {}", name);
    }

    /**
     * Abstract method - subclasses must implement.
     * Parser detects: abstract method declaration
     */
    public abstract BigDecimal calculate(BigDecimal... inputs);

    /**
     * Abstract method with return type.
     */
    public abstract String getCalculationType();

    /**
     * Protected helper method for subclasses.
     */
    protected BigDecimal round(BigDecimal value) {
        return value.setScale(precision, DEFAULT_ROUNDING);
    }

    /**
     * Protected helper with validation.
     */
    protected void validateInputs(BigDecimal... inputs) {
        if (inputs == null || inputs.length == 0) {
            throw new IllegalArgumentException("Inputs cannot be null or empty");
        }
        for (BigDecimal input : inputs) {
            if (input == null) {
                throw new IllegalArgumentException("Individual input cannot be null");
            }
        }
    }

    /**
     * Template method pattern.
     */
    public final BigDecimal executeCalculation(BigDecimal... inputs) {
        log.info("Executing {} calculation", calculatorName);

        beforeCalculation();
        validateInputs(inputs);
        BigDecimal result = calculate(inputs);
        BigDecimal roundedResult = round(result);
        afterCalculation(roundedResult);

        return roundedResult;
    }

    /**
     * Hook method - can be overridden.
     */
    protected void beforeCalculation() {
        log.debug("Before calculation hook");
    }

    /**
     * Hook method - can be overridden.
     */
    protected void afterCalculation(BigDecimal result) {
        log.debug("After calculation hook, result: {}", result);
    }

    // Getters and setters
    public String getCalculatorName() { return calculatorName; }
    public int getPrecision() { return precision; }
    public void setPrecision(int precision) { this.precision = precision; }
}
