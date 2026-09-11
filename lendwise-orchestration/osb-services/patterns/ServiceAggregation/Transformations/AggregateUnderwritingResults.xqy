xquery version "1.0" encoding "utf-8";

declare namespace uw = "http://lendwise.com/soa/underwriting/types";

declare variable $creditResp as element() external;
declare variable $dtiResp as element() external;
declare variable $decisionResp as element() external;
declare variable $loanId as xs:string external;

<uw:AggregatedUnderwritingResponse>
    <uw:LoanId>{ $loanId }</uw:LoanId>
    <uw:EvaluationTimestamp>{ fn:current-dateTime() }</uw:EvaluationTimestamp>
    <uw:CreditScoreData>
        <uw:AverageFICO>{ xs:integer($creditResp//AverageScore/text() | $creditResp//FICO/text()) }</uw:AverageFICO>
        <uw:CreditStatus>{ if (xs:integer($creditResp//AverageScore/text()) >= 680) then "QUALIFIED" else "CONDITIONAL" }</uw:CreditStatus>
    </uw:CreditScoreData>
    <uw:FinancialRatios>
        <uw:DTIRatio>{ xs:decimal($dtiResp//DTIRatio/text()) }</uw:DTIRatio>
        <uw:LTVRatio>{ xs:decimal($dtiResp//LTVRatio/text()) }</uw:LTVRatio>
        <uw:PITIAmount>{ xs:decimal($dtiResp//PITI/text()) }</uw:PITIAmount>
        <uw:QMCompliant>{ xs:boolean($dtiResp//QMStatus/text()) }</uw:QMCompliant>
    </uw:FinancialRatios>
    <uw:UnderwritingDecision>
        <uw:Recommendation>{ $decisionResp//Recommendation/text() }</uw:Recommendation>
        <uw:ApprovalConditions>
            {
                for $cond in $decisionResp//Condition
                return <uw:Condition>{ $cond/text() }</uw:Condition>
            }
        </uw:ApprovalConditions>
    </uw:UnderwritingDecision>
</uw:AggregatedUnderwritingResponse>
