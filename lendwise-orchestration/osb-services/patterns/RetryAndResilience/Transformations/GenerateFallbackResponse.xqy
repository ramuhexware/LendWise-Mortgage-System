xquery version "1.0" encoding "utf-8";

declare namespace pr = "http://lendwise.com/soa/pricing/types";

declare variable $errorMessage as xs:string external;
declare variable $loanId as xs:string external;

<pr:PricingEngineResponse>
    <pr:LoanId>{ $loanId }</pr:LoanId>
    <pr:InterestRate>0.06875</pr:InterestRate> <!-- 6.875% Benchmark Fallback Rate -->
    <pr:APR>0.07015</pr:APR>
    <pr:LLPAAdjustment>0.0050</pr:LLPAAdjustment>
    <pr:Status>FALLBACK_BENCHMARK_QUOTE</pr:Status>
    <pr:IsFallbackQuote>true</pr:IsFallbackQuote>
    <pr:FallbackReason>{ $errorMessage }</pr:FallbackReason>
    <pr:QuoteTimestamp>{ fn:current-dateTime() }</pr:QuoteTimestamp>
</pr:PricingEngineResponse>
