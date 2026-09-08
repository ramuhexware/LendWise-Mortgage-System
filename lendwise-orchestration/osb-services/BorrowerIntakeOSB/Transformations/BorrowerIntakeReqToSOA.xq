(:: Oracle Service Bus XQuery Transformation for Borrower Intake ::)
xquery version "1.0" encoding "utf-8";

declare namespace ns1 = "http://lendwise.com/soa/borrower/types";

declare function local:enrichBorrowerRequest($app as element(*)) as element(*) {
    <ns1:BorrowerApplication>
        {$app/*}
        <ns1:HeaderInfo>
            <ns1:Channel>OSB_GATEWAY</ns1:Channel>
            <ns1:ProcessedTimestamp>{fn:current-dateTime()}</ns1:ProcessedTimestamp>
        </ns1:HeaderInfo>
    </ns1:BorrowerApplication>
};

local:enrichBorrowerRequest($body/*[1])
