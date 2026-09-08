package com.lendwise.patterns.consumer.soap;

import jakarta.annotation.Resource;
import jakarta.jws.*;
import jakarta.xml.ws.*;
import jakarta.xml.ws.handler.MessageContext;
import lombok.extern.slf4j.Slf4j;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

/**
 * JAX-WS SOAP Service implementation patterns.
 * Parser should detect: @WebService, @WebServiceProvider, @ServiceMode, @WebMethod
 */
@WebService(
    serviceName = "CreditBureauService",
    portName = "CreditBureauPort",
    targetNamespace = "http://lendwise.com/credit"
)
@Slf4j
public class CreditBureauServiceImpl {

    // Parser detects: @Resource for WebServiceContext
    @Resource
    private WebServiceContext wsContext;

    /**
     * Web method for credit check.
     * Parser detects: @WebMethod with operationName
     */
    @WebMethod(operationName = "checkCredit")
    @WebResult(name = "CreditCheckResult")
    public CreditCheckResult checkCredit(
            @WebParam(name = "borrowerId") String borrowerId,
            @WebParam(name = "ssn") String ssn) {

        log.info("@WebService checkCredit called for borrower: {}", borrowerId);

        // Access message context
        MessageContext msgContext = wsContext.getMessageContext();

        CreditCheckResult result = new CreditCheckResult();
        result.setBorrowerId(borrowerId);
        result.setFicoScore(720);
        result.setStatus("APPROVED");

        return result;
    }

    /**
     * One-way operation (no response).
     * Parser detects: @Oneway annotation
     */
    @WebMethod(operationName = "submitCreditRequest")
    @Oneway
    public void submitCreditRequest(
            @WebParam(name = "borrowerId") String borrowerId,
            @WebParam(name = "requestType") String requestType) {

        log.info("@Oneway submitCreditRequest for borrower: {}, type: {}", borrowerId, requestType);
        // Process asynchronously - no response returned
    }

    /**
     * Method with SOAPBinding annotation.
     * Parser detects: @SOAPBinding
     */
    @WebMethod
    @SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
    public String getCreditReportXml(@WebParam(name = "borrowerId") String borrowerId) {
        log.info("getCreditReportXml called for: {}", borrowerId);
        return "<creditReport><borrowerId>" + borrowerId + "</borrowerId><score>720</score></creditReport>";
    }

    public static class CreditCheckResult {
        private String borrowerId;
        private int ficoScore;
        private String status;

        public String getBorrowerId() { return borrowerId; }
        public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
        public int getFicoScore() { return ficoScore; }
        public void setFicoScore(int ficoScore) { this.ficoScore = ficoScore; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}

/**
 * WebServiceProvider pattern for raw XML handling.
 * Parser detects: @WebServiceProvider, @ServiceMode
 */
@WebServiceProvider(
    wsdlLocation = "WEB-INF/wsdl/CreditService.wsdl",
    serviceName = "CreditBureauProviderService",
    portName = "CreditBureauProviderPort",
    targetNamespace = "http://lendwise.com/credit"
)
@ServiceMode(value = Service.Mode.PAYLOAD)
@Slf4j
class CreditBureauServiceProvider implements Provider<Source> {

    /**
     * Provider invoke method for raw XML processing.
     * Parser detects: Provider.invoke()
     */
    @Override
    public Source invoke(Source request) {
        log.info("@WebServiceProvider invoke called");

        String responseXml = """
            <CreditCheckResponse xmlns="http://lendwise.com/credit">
                <borrowerId>PROVIDER-001</borrowerId>
                <ficoScore>720</ficoScore>
                <status>APPROVED</status>
            </CreditCheckResponse>
            """;

        return new StreamSource(new StringReader(responseXml));
    }
}
