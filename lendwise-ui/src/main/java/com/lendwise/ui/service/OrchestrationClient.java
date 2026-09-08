package com.lendwise.ui.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * Client for calling SOA orchestration layer.
 * Routes requests to the appropriate SOA composite services.
 */
@Service
@Slf4j
public class OrchestrationClient {

    @Value("${orchestration.base-url:http://localhost:7001/soa-infra/services/lendwise}")
    private String orchestrationBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Object createLoan(Object applicationData) {
        String url = orchestrationBaseUrl + "/BorrowerIntakeComposite/BorrowerIntakeRESTService";
        return callOrchestration(url, applicationData);
    }

    public Object getLoanDetails(String loanId) {
        String url = orchestrationBaseUrl + "/BorrowerIntakeComposite/BorrowerIntakeRESTService/" + loanId;
        return restTemplate.getForObject(url, Map.class);
    }

    public Object getDocumentChecklist(String loanId) {
        String url = orchestrationBaseUrl + "/DocumentProcessingComposite/DocumentProcessingRESTService/checklist/" + loanId;
        return restTemplate.getForObject(url, Map.class);
    }

    public Object getUnderwritingDecision(String loanId) {
        String url = orchestrationBaseUrl + "/UnderwritingComposite/UnderwritingRESTService/loan/" + loanId;
        return restTemplate.getForObject(url, Map.class);
    }

    public Object getPricingDetails(String loanId) {
        String url = orchestrationBaseUrl + "/PricingEngineComposite/PricingRESTService/loan/" + loanId;
        return restTemplate.getForObject(url, Map.class);
    }

    public Object getClosingDisclosure(String loanId) {
        String url = orchestrationBaseUrl + "/ClosingDisclosureComposite/ClosingDisclosureRESTService/loan/" + loanId;
        return restTemplate.getForObject(url, Map.class);
    }

    public Object submitDocument(String loanId, Object documentData) {
        String url = orchestrationBaseUrl + "/DocumentProcessingComposite/DocumentProcessingRESTService";
        return callOrchestration(url, documentData);
    }

    public Object calculatePricing(Object pricingRequest) {
        String url = orchestrationBaseUrl + "/PricingEngineComposite/PricingRESTService";
        return callOrchestration(url, pricingRequest);
    }

    public Object runComplianceCheck(Object complianceRequest) {
        String url = orchestrationBaseUrl + "/ComplianceComposite/ComplianceRESTService";
        return callOrchestration(url, complianceRequest);
    }

    private Object callOrchestration(String url, Object payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Correlation-ID", UUID.randomUUID().toString());

        HttpEntity<Object> request = new HttpEntity<>(payload, headers);
        log.debug("Calling orchestration: {}", url);
        return restTemplate.postForObject(url, request, Map.class);
    }
}
