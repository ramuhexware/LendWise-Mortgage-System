package com.lendwise.adapters;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OCR Processor - Java Adapter for BPEL Java Embedding
 * Processes document images and extracts structured data.
 * Supports mortgage document types: Paystubs, W-2s, 1040s, Bank Statements.
 */
public class OCRProcessor {

    // Document Types
    public static final String PAYSTUB = "PAYSTUB";
    public static final String W2 = "W2";
    public static final String TAX_1040 = "TAX_1040";
    public static final String BANK_STATEMENT = "BANK_STATEMENT";
    public static final String DRIVERS_LICENSE = "DRIVERS_LICENSE";

    // Regex Patterns for common fields
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("\\$([\\d,]+\\.?\\d*)");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})");
    private static final Pattern SSN_PATTERN = Pattern.compile("(\\d{3}-\\d{2}-\\d{4})");
    private static final Pattern EIN_PATTERN = Pattern.compile("(\\d{2}-\\d{7})");
    private static final Pattern YEAR_PATTERN = Pattern.compile("(20\\d{2}|19\\d{2})");

    /**
     * Processes a document and extracts relevant fields based on document type.
     *
     * @param documentPath Path to the document file
     * @param documentType Type of document (PAYSTUB, W2, TAX_1040, BANK_STATEMENT)
     * @return Map of extracted field names to values
     */
    public Map<String, String> processDocument(String documentPath, String documentType) {
        // In production, this would call an actual OCR engine (Tesseract, AWS Textract, etc.)
        // For BPEL integration, this returns structured data that can be assigned to BPEL variables

        Map<String, String> extractedData = new HashMap<>();
        extractedData.put("documentPath", documentPath);
        extractedData.put("documentType", documentType);
        extractedData.put("processingStatus", "SUCCESS");

        switch (documentType) {
            case PAYSTUB:
                extractedData.putAll(extractPaystubFields());
                break;
            case W2:
                extractedData.putAll(extractW2Fields());
                break;
            case TAX_1040:
                extractedData.putAll(extractTax1040Fields());
                break;
            case BANK_STATEMENT:
                extractedData.putAll(extractBankStatementFields());
                break;
            case DRIVERS_LICENSE:
                extractedData.putAll(extractDriversLicenseFields());
                break;
            default:
                extractedData.put("processingStatus", "UNKNOWN_TYPE");
        }

        return extractedData;
    }

    /**
     * Extracts fields from a paystub document.
     */
    private Map<String, String> extractPaystubFields() {
        Map<String, String> fields = new HashMap<>();
        // These would be populated by actual OCR results
        fields.put("employerName", "");
        fields.put("employerAddress", "");
        fields.put("employeeSSN", "");
        fields.put("employeeName", "");
        fields.put("payPeriodStart", "");
        fields.put("payPeriodEnd", "");
        fields.put("payDate", "");
        fields.put("grossPay", "");
        fields.put("netPay", "");
        fields.put("ytdGrossPay", "");
        fields.put("ytdNetPay", "");
        fields.put("regularHours", "");
        fields.put("overtimeHours", "");
        fields.put("regularRate", "");
        fields.put("overtimeRate", "");
        fields.put("federalTax", "");
        fields.put("stateTax", "");
        fields.put("socialSecurity", "");
        fields.put("medicare", "");
        return fields;
    }

    /**
     * Extracts fields from a W-2 document.
     */
    private Map<String, String> extractW2Fields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("taxYear", "");
        fields.put("employerEIN", "");
        fields.put("employerName", "");
        fields.put("employerAddress", "");
        fields.put("employeeSSN", "");
        fields.put("employeeName", "");
        fields.put("employeeAddress", "");
        fields.put("box1_wages", "");           // Wages, tips, other compensation
        fields.put("box2_fedWithholding", "");  // Federal income tax withheld
        fields.put("box3_ssWages", "");         // Social security wages
        fields.put("box4_ssWithholding", "");   // Social security tax withheld
        fields.put("box5_medicareWages", "");   // Medicare wages and tips
        fields.put("box6_medicareWithholding", ""); // Medicare tax withheld
        fields.put("box12_codes", "");          // Box 12 codes (401k, etc.)
        fields.put("box14_other", "");          // Other
        return fields;
    }

    /**
     * Extracts fields from a 1040 tax return.
     */
    private Map<String, String> extractTax1040Fields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("taxYear", "");
        fields.put("filingStatus", "");         // Single, MFJ, MFS, HOH, QW
        fields.put("taxpayerSSN", "");
        fields.put("taxpayerName", "");
        fields.put("spouseSSN", "");
        fields.put("spouseName", "");
        fields.put("address", "");
        fields.put("line1_wages", "");          // Total wages
        fields.put("line2a_taxExemptInterest", "");
        fields.put("line2b_taxableInterest", "");
        fields.put("line3a_qualifiedDividends", "");
        fields.put("line3b_ordinaryDividends", "");
        fields.put("line7_capitalGain", "");
        fields.put("line8_otherIncome", "");
        fields.put("line9_totalIncome", "");    // AGI before adjustments
        fields.put("line11_adjustedGrossIncome", "");
        fields.put("line15_taxableIncome", "");
        fields.put("line24_totalTax", "");
        fields.put("line25_totalPayments", "");
        fields.put("scheduleC_netProfit", "");  // Self-employment income
        fields.put("scheduleE_rentalIncome", ""); // Rental income
        return fields;
    }

    /**
     * Extracts fields from a bank statement.
     */
    private Map<String, String> extractBankStatementFields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("bankName", "");
        fields.put("accountNumber", "");
        fields.put("accountType", "");          // Checking, Savings, Money Market
        fields.put("accountHolderName", "");
        fields.put("statementPeriodStart", "");
        fields.put("statementPeriodEnd", "");
        fields.put("beginningBalance", "");
        fields.put("endingBalance", "");
        fields.put("totalDeposits", "");
        fields.put("totalWithdrawals", "");
        fields.put("averageDailyBalance", "");
        fields.put("largeDepositCount", "");    // Deposits > $1000
        fields.put("largeDepositTotal", "");
        fields.put("nsfCount", "");             // NSF/overdraft count
        return fields;
    }

    /**
     * Extracts fields from a driver's license.
     */
    private Map<String, String> extractDriversLicenseFields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("licenseNumber", "");
        fields.put("firstName", "");
        fields.put("lastName", "");
        fields.put("middleName", "");
        fields.put("dateOfBirth", "");
        fields.put("address", "");
        fields.put("city", "");
        fields.put("state", "");
        fields.put("zipCode", "");
        fields.put("issueDate", "");
        fields.put("expirationDate", "");
        fields.put("sex", "");
        fields.put("height", "");
        fields.put("eyeColor", "");
        return fields;
    }

    /**
     * Validates extracted data against expected patterns.
     *
     * @param extractedData Map of extracted fields
     * @param documentType Document type for validation rules
     * @return Map with validation results
     */
    public Map<String, Object> validateExtractedData(Map<String, String> extractedData, String documentType) {
        Map<String, Object> validationResult = new HashMap<>();
        validationResult.put("isValid", true);
        validationResult.put("confidence", 0.0);

        java.util.List<String> issues = new java.util.ArrayList<>();

        // Validate SSN format
        String ssn = extractedData.get("employeeSSN");
        if (ssn != null && !ssn.isEmpty() && !SSN_PATTERN.matcher(ssn).matches()) {
            issues.add("Invalid SSN format");
        }

        // Validate currency amounts are positive
        for (String key : extractedData.keySet()) {
            if (key.contains("Pay") || key.contains("wages") || key.contains("Balance")) {
                String value = extractedData.get(key);
                if (value != null && !value.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(value.replaceAll("[,$]", ""));
                        if (amount < 0) {
                            issues.add("Negative amount for " + key);
                        }
                    } catch (NumberFormatException e) {
                        issues.add("Invalid number format for " + key);
                    }
                }
            }
        }

        if (!issues.isEmpty()) {
            validationResult.put("isValid", false);
            validationResult.put("issues", issues);
        }

        return validationResult;
    }

    /**
     * Calculates confidence score for extracted data.
     *
     * @param extractedData Map of extracted fields
     * @return Confidence score between 0.0 and 1.0
     */
    public double calculateConfidence(Map<String, String> extractedData) {
        int totalFields = extractedData.size();
        int populatedFields = 0;

        for (String value : extractedData.values()) {
            if (value != null && !value.isEmpty()) {
                populatedFields++;
            }
        }

        return totalFields > 0 ? (double) populatedFields / totalFields : 0.0;
    }

    /**
     * Parses a currency string to a numeric value.
     *
     * @param currencyString Currency string (e.g., "$1,234.56")
     * @return Numeric value or 0.0 if parsing fails
     */
    public double parseCurrency(String currencyString) {
        if (currencyString == null || currencyString.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(currencyString.replaceAll("[,$]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
