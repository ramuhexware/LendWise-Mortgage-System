package com.lendwise.patterns.producer.soap;

import jakarta.xml.soap.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URL;

/**
 * SOAP client using SAAJ API (SOAPConnection).
 * Parser should detect: SOAPConnectionFactory, SOAPConnection, createMessage, call
 */
@Service
@Slf4j
public class CreditBureauSoapClient {

    @Value("${soap.creditbureau.endpoint}")
    private String endpointUrl;

    private static final String NAMESPACE = "http://creditbureau.example.com/ws";

    /**
     * Pull credit report using raw SOAP/SAAJ API.
     * Demonstrates SOAPConnection pattern for parser detection.
     */
    public CreditBureauService.CreditReportResponse pullCreditReport(String ssn, String firstName, String lastName) {
        SOAPConnection soapConnection = null;
        try {
            // Create SOAP Connection - Parser detects SOAPConnectionFactory usage
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            soapConnection = soapConnectionFactory.createConnection();

            // Create SOAP Message - Parser detects MessageFactory usage
            MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            SOAPMessage soapMessage = messageFactory.createMessage();

            // Build SOAP Envelope
            SOAPPart soapPart = soapMessage.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            envelope.addNamespaceDeclaration("cred", NAMESPACE);

            // Build SOAP Body
            SOAPBody soapBody = envelope.getBody();
            SOAPElement pullCreditElement = soapBody.addChildElement("PullCreditRequest", "cred");

            SOAPElement ssnElement = pullCreditElement.addChildElement("SSN", "cred");
            ssnElement.addTextNode(ssn);

            SOAPElement firstNameElement = pullCreditElement.addChildElement("FirstName", "cred");
            firstNameElement.addTextNode(firstName);

            SOAPElement lastNameElement = pullCreditElement.addChildElement("LastName", "cred");
            lastNameElement.addTextNode(lastName);

            // Add SOAP Action header
            MimeHeaders headers = soapMessage.getMimeHeaders();
            headers.addHeader("SOAPAction", NAMESPACE + "/PullCreditReport");

            soapMessage.saveChanges();

            log.info("Sending SOAP request to: {}", endpointUrl);

            // Send SOAP Message - Parser detects soapConnection.call()
            SOAPMessage soapResponse = soapConnection.call(soapMessage, new URL(endpointUrl));

            // Parse response
            return parseResponse(soapResponse);

        } catch (Exception e) {
            log.error("SOAP call failed", e);
            throw new RuntimeException("Credit bureau SOAP call failed", e);
        } finally {
            if (soapConnection != null) {
                try {
                    soapConnection.close();
                } catch (SOAPException e) {
                    log.warn("Error closing SOAP connection", e);
                }
            }
        }
    }

    /**
     * Create SOAP fault for error scenarios.
     */
    public SOAPMessage createFaultMessage(String faultCode, String faultString) throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage faultMessage = messageFactory.createMessage();
        SOAPBody body = faultMessage.getSOAPBody();

        SOAPFault fault = body.addFault();
        fault.setFaultCode(new QName(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, faultCode));
        fault.setFaultString(faultString);

        return faultMessage;
    }

    private CreditBureauService.CreditReportResponse parseResponse(SOAPMessage response) throws SOAPException {
        SOAPBody responseBody = response.getSOAPBody();

        if (responseBody.hasFault()) {
            SOAPFault fault = responseBody.getFault();
            throw new RuntimeException("SOAP Fault: " + fault.getFaultString());
        }

        // Parse response elements (simplified)
        return new CreditBureauService.CreditReportResponse(
            "RPT-" + System.currentTimeMillis(),
            720,
            715,
            722,
            718,
            "SUCCESS"
        );
    }
}
