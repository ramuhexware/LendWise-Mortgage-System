xquery version "1.0" encoding "utf-8";

declare namespace ns0 = "http://lendwise.com/soa/underwriting/types";
declare namespace cb = "http://lendwise.com/creditbureau/types";

declare variable $underwritingReq as element() external;

<cb:CreditBureauQueryRequest>
    <cb:SSN>{ $underwritingReq//ssn/text() | $underwritingReq//SSN/text() }</cb:SSN>
    <cb:ApplicantName>{ concat($underwritingReq//firstName/text(), " ", $underwritingReq//lastName/text()) }</cb:ApplicantName>
    <cb:Bureaus>
        <cb:BureauName>Equifax</cb:BureauName>
        <cb:BureauName>Experian</cb:BureauName>
        <cb:BureauName>TransUnion</cb:BureauName>
    </cb:Bureaus>
    <cb:RequestTimestamp>{ fn:current-dateTime() }</cb:RequestTimestamp>
</cb:CreditBureauQueryRequest>
