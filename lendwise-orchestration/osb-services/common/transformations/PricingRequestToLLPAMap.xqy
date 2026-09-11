xquery version "1.0" encoding "utf-8";

declare namespace pr = "http://lendwise.com/soa/pricing/types";

declare variable $pricingReq as element() external;

<pr:LLPALookupRequest>
    <pr:CreditScore>{ xs:integer($pricingReq//creditScore/text() | $pricingReq//CreditScore/text()) }</pr:CreditScore>
    <pr:LTV>{ xs:decimal($pricingReq//ltvRatio/text() | $pricingReq//LTV/text()) }</pr:LTV>
    <pr:LoanAmount>{ xs:decimal($pricingReq//loanAmount/text() | $pricingReq//LoanAmount/text()) }</pr:LoanAmount>
    <pr:PropertyType>{ $pricingReq//propertyType/text() | $pricingReq//PropertyType/text() }</pr:PropertyType>
    <pr:Occupancy>{ $pricingReq//occupancy/text() | $pricingReq//Occupancy/text() }</pr:Occupancy>
</pr:LLPALookupRequest>
