package com.lendwise.patterns.producer.soap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

/**
 * SOAP client using Spring WebServiceTemplate.
 * Parser should detect: WebServiceTemplate, marshalSendAndReceive, sendAndReceive
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CreditBureauWebServiceTemplate extends WebServiceGatewaySupport {

    private static final String ENDPOINT_URL = "http://creditbureau.example.com/ws/credit";
    private static final String NAMESPACE = "http://creditbureau.example.com/ws";

    /**
     * Send SOAP request using WebServiceTemplate.marshalSendAndReceive()
     * Parser detects this as a SOAP call pattern.
     */
    public CreditReportResponse pullCreditReportWithTemplate(CreditReportRequest request) {
        log.info("Sending credit report request via WebServiceTemplate for SSN: {}",
                 maskSsn(request.getSsn()));

        // Parser detects: getWebServiceTemplate().marshalSendAndReceive()
        CreditReportResponse response = (CreditReportResponse) getWebServiceTemplate()
            .marshalSendAndReceive(
                ENDPOINT_URL,
                request,
                new SoapActionCallback(NAMESPACE + "/PullCreditReport")
            );

        log.info("Received credit report response: {}", response.getReportId());
        return response;
    }

    /**
     * Alternative method using sendAndReceive with custom callback.
     */
    public Object sendCustomRequest(Object request, String soapAction) {
        WebServiceTemplate template = getWebServiceTemplate();

        // Parser detects: webServiceTemplate.sendAndReceive()
        return template.sendAndReceive(
            ENDPOINT_URL,
            webServiceMessage -> {
                // Custom message callback
                Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
                marshaller.marshal(request, webServiceMessage.getPayloadResult());
            },
            webServiceMessage -> {
                // Custom response extractor
                Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
                return unmarshaller.unmarshal(webServiceMessage.getPayloadSource());
            }
        );
    }

    /**
     * Send with explicit WebServiceTemplate instance.
     */
    public void sendNotification(NotificationRequest notification) {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();

        // Configure marshaller
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.lendwise.patterns.producer.soap");
        webServiceTemplate.setMarshaller(marshaller);
        webServiceTemplate.setUnmarshaller(marshaller);

        // Parser detects: webServiceTemplate.marshalSendAndReceive()
        webServiceTemplate.marshalSendAndReceive(
            "http://notification.example.com/ws",
            notification
        );
    }

    private String maskSsn(String ssn) {
        if (ssn == null || ssn.length() < 4) return "***";
        return "***-**-" + ssn.substring(ssn.length() - 4);
    }

    // Request/Response DTOs
    public static class CreditReportRequest {
        private String ssn;
        private String firstName;
        private String lastName;

        public String getSsn() { return ssn; }
        public void setSsn(String ssn) { this.ssn = ssn; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    public static class CreditReportResponse {
        private String reportId;
        private int ficoScore;

        public String getReportId() { return reportId; }
        public void setReportId(String reportId) { this.reportId = reportId; }
        public int getFicoScore() { return ficoScore; }
        public void setFicoScore(int ficoScore) { this.ficoScore = ficoScore; }
    }

    public static class NotificationRequest {
        private String borrowerId;
        private String message;

        public String getBorrowerId() { return borrowerId; }
        public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
