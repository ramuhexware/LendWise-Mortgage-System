package com.lendwise.patterns.producer.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 * SOAP Web Service interface for Credit Bureau integration.
 * Parser should detect: @WebService annotation, @WebMethod, @WebParam
 */
@WebService(
    name = "CreditBureauService",
    targetNamespace = "http://lendwise.com/creditbureau"
)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public interface CreditBureauService {

    @WebMethod(operationName = "pullCreditReport")
    @WebResult(name = "CreditReportResponse")
    CreditReportResponse pullCreditReport(
        @WebParam(name = "ssn") String ssn,
        @WebParam(name = "firstName") String firstName,
        @WebParam(name = "lastName") String lastName
    );

    @WebMethod(operationName = "getCreditScore")
    @WebResult(name = "CreditScoreResponse")
    int getCreditScore(@WebParam(name = "ssn") String ssn);

    record CreditReportResponse(
        String reportId,
        int ficoScore,
        int equifaxScore,
        int experianScore,
        int transunionScore,
        String status
    ) {}
}
