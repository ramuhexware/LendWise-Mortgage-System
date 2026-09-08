package com.lendwise.patterns.consumer.soap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.namespace.QName;

/**
 * Spring WS SOAP Endpoint patterns.
 * Parser should detect: @Endpoint, @PayloadRoot, @RequestPayload, @ResponsePayload
 */
@Endpoint
@Slf4j
public class CreditBureauEndpoint {

    private static final String NAMESPACE_URI = "http://lendwise.com/credit";

    /**
     * SOAP endpoint for credit check requests.
     * Parser detects: @PayloadRoot with namespace and localPart
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreditCheckRequest")
    @ResponsePayload
    public CreditCheckResponse checkCredit(@RequestPayload CreditCheckRequest request) {
        log.info("SOAP endpoint received credit check request for borrower: {}", request.getBorrowerId());

        // Process the credit check
        CreditCheckResponse response = new CreditCheckResponse();
        response.setBorrowerId(request.getBorrowerId());
        response.setFicoScore(720);
        response.setStatus("APPROVED");
        response.setTimestamp(System.currentTimeMillis());

        log.info("Returning credit check response: {}", response);
        return response;
    }

    /**
     * Another endpoint for credit report requests.
     * Parser detects: @PayloadRoot with different localPart
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreditReportRequest")
    @ResponsePayload
    public CreditReportResponse getCreditReport(@RequestPayload CreditReportRequest request) {
        log.info("SOAP endpoint received credit report request for borrower: {}", request.getBorrowerId());

        CreditReportResponse response = new CreditReportResponse();
        response.setBorrowerId(request.getBorrowerId());
        response.setBureau("EQUIFAX");
        response.setReportDate(java.time.LocalDate.now().toString());
        response.setReportXml("<creditReport><score>720</score></creditReport>");

        return response;
    }

    // Request/Response classes
    public static class CreditCheckRequest {
        private String borrowerId;
        private String ssn;
        private String requestType;

        public String getBorrowerId() { return borrowerId; }
        public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
        public String getSsn() { return ssn; }
        public void setSsn(String ssn) { this.ssn = ssn; }
        public String getRequestType() { return requestType; }
        public void setRequestType(String requestType) { this.requestType = requestType; }
    }

    public static class CreditCheckResponse {
        private String borrowerId;
        private int ficoScore;
        private String status;
        private long timestamp;

        public String getBorrowerId() { return borrowerId; }
        public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
        public int getFicoScore() { return ficoScore; }
        public void setFicoScore(int ficoScore) { this.ficoScore = ficoScore; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        @Override
        public String toString() {
            return "CreditCheckResponse{borrowerId='" + borrowerId + "', ficoScore=" + ficoScore + ", status='" + status + "'}";
        }
    }

    public static class CreditReportRequest {
        private String borrowerId;
        private String bureau;

        public String getBorrowerId() { return borrowerId; }
        public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
        public String getBureau() { return bureau; }
        public void setBureau(String bureau) { this.bureau = bureau; }
    }

    public static class CreditReportResponse {
        private String borrowerId;
        private String bureau;
        private String reportDate;
        private String reportXml;

        public String getBorrowerId() { return borrowerId; }
        public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
        public String getBureau() { return bureau; }
        public void setBureau(String bureau) { this.bureau = bureau; }
        public String getReportDate() { return reportDate; }
        public void setReportDate(String reportDate) { this.reportDate = reportDate; }
        public String getReportXml() { return reportXml; }
        public void setReportXml(String reportXml) { this.reportXml = reportXml; }
    }
}
