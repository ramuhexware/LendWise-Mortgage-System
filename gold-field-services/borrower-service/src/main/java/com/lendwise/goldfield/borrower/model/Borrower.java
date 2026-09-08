package com.lendwise.goldfield.borrower.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Borrower document for MongoDB storage.
 */
@Document(collection = "borrowers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Borrower {

    @Id
    private String id;

    @Indexed(unique = true)
    private String borrowerId;

    @Indexed
    private String loanId;

    // Personal Information
    private String firstName;
    private String lastName;
    private String middleName;
    private String suffix;
    private LocalDate dateOfBirth;
    private String ssn; // Encrypted
    private String email;
    private String phone;

    // Address
    private Address currentAddress;
    private List<Address> previousAddresses;
    private Integer yearsAtCurrentAddress;

    // Employment
    private List<Employment> employments;
    private BigDecimal monthlyIncome;
    private BigDecimal otherIncome;
    private String incomeType; // W2, SELF_EMPLOYED, RETIRED, etc.

    // Assets & Liabilities
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal monthlyDebt;

    // Calculated Ratios
    private BigDecimal frontEndDTI;
    private BigDecimal backEndDTI;
    private Integer creditScore;

    // Status
    private BorrowerType borrowerType; // PRIMARY, CO_BORROWER
    private BorrowerStatus status;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public enum BorrowerType {
        PRIMARY, CO_BORROWER
    }

    public enum BorrowerStatus {
        ACTIVE, INCOMPLETE, VERIFIED, ARCHIVED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String street;
        private String unit;
        private String city;
        private String state;
        private String zipCode;
        private String addressType; // CURRENT, MAILING, PREVIOUS
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Employment {
        private String employerName;
        private String employerAddress;
        private String employerPhone;
        private String position;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal monthlyIncome;
        private Boolean isCurrent;
        private String verificationType;
    }
}
