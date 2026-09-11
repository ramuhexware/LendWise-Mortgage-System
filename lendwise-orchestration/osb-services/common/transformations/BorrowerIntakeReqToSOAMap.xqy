(:: pragma bea:local-element-parameter parameter="$input" type="ns0:BorrowerApplication" location="BorrowerTypes.xsd" ::)
(:: pragma bea:local-element-return type="ns1:BorrowerIntakeProcessRequest" location="BorrowerIntakeProcess.wsdl" ::)

xquery version "1.0" encoding "utf-8";

declare namespace ns0 = "http://lendwise.com/soa/borrower/types";
declare namespace ns1 = "http://lendwise.com/soa/borrower";

declare variable $input as element() external;
declare variable $trackingId as xs:string external;

declare function local:transformBorrowerRequest($req as element(), $trackId as xs:string) as element() {
    <ns1:BorrowerIntakeProcessRequest>
        <ns0:Header>
            <ns0:TrackingId>{ $trackId }</ns0:TrackingId>
            <ns0:SourceSystem>LENDWISE_UI</ns0:SourceSystem>
            <ns0:Timestamp>{ fn:current-dateTime() }</ns0:Timestamp>
        </ns0:Header>
        <ns0:BorrowerData>
            <ns0:FirstName>{ $req//FirstName/text() }</ns0:FirstName>
            <ns0:LastName>{ $req//LastName/text() }</ns0:LastName>
            <ns0:SSN>{ $req//SSN/text() }</ns0:SSN>
            <ns0:Email>{ $req//Email/text() }</ns0:Email>
            <ns0:Phone>{ $req//Phone/text() }</ns0:Phone>
            <ns0:MonthlyIncome>{ xs:decimal($req//MonthlyIncome/text()) }</ns0:MonthlyIncome>
            <ns0:MonthlyDebt>{ xs:decimal($req//MonthlyDebt/text()) }</ns0:MonthlyDebt>
        </ns0:BorrowerData>
        <ns0:LoanData>
            <ns0:LoanAmount>{ xs:decimal($req//LoanAmount/text()) }</ns0:LoanAmount>
            <ns0:PurchasePrice>{ xs:decimal($req//PurchasePrice/text()) }</ns0:PurchasePrice>
            <ns0:DownPayment>{ xs:decimal($req//DownPayment/text()) }</ns0:DownPayment>
            <ns0:LoanType>{ if ($req//LoanType/text()) then $req//LoanType/text() else "CONFORMING" }</ns0:LoanType>
            <ns0:PropertyAddress>{ $req//PropertyAddress/text() }</ns0:PropertyAddress>
        </ns0:LoanData>
    </ns1:BorrowerIntakeProcessRequest>
};

local:transformBorrowerRequest($input, $trackingId)
