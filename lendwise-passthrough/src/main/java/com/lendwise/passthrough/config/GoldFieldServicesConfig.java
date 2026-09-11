package com.lendwise.passthrough.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Gold Field service URLs.
 * Loaded from application.properties with prefix "goldfield.services"
 */
@Configuration
@ConfigurationProperties(prefix = "goldfield.services")
@Data
public class GoldFieldServicesConfig {

    private String borrowerService;
    private String kycService;
    private String documentService;
    private String creditService;
    private String underwritingService;
    private String complianceService;
    private String pricingService;
    private String ratelockService;
    private String amortizationService;
    private String closingService;
    private String esignService;
    private String fundingService;
    private String notificationService;
    private String valuationService;
    private String titleService;
    private String investorService;
    private String analyticsService;
    private String auditService;
}
