# Oracle Service Bus (OSB 12c) Enterprise Integration Patterns

This document details the implementation of **11 core Oracle Service Bus (OSB) Integration Patterns** within the `LendWise-Mortgage-System` architecture (`lendwise-orchestration/osb-services`).

---

## Architecture Overview

```
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                       LendWise Gateway Entry                                           │
│                 (REST HTTP / SOAP / JMS / File Adapter Ingress)                                        │
└───────────────────────────────────┬────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                              Oracle Service Bus (OSB 12c) Gateway                                      │
│                                                                                                        │
│  1. Service Proxy Pattern           : Decoupled virtual entry points (.proxy)                          │
│  2. Content-Based Routing Pattern   : Inspects payload (loanType, amount) & routes via branch nodes    │
│  3. Message Transformation Pattern  : XQuery 1.0 (.xqy) payload transformation to canonical models      │
│  4. Service Virtualization Pattern  : Dynamic routing & mock business services (.bsp)                  │
│  5. Protocol Bridging Pattern       : REST HTTP ↔ SOAP XML, JMS Queue ↔ SOAP, File ↔ SOAP               │
│  6. Service Aggregation Pattern     : Scatter-Gather credit + DTI + decision engine responses          │
│  7. Publish-Subscribe Pattern       : Event publishing to jms/LoanEventsTopic with multi-subscribers    │
│  8. Asynchronous Messaging Pattern  : Fire-and-Forget & Async Request-Reply (HTTP 202 Accepted)          │
│  9. Logging & Audit Pattern         : Correlation IDs (X-Correlation-ID) & centralized audit pipeline     │
│ 10. Security Gateway Pattern        : OAuth 2.0 JWT Bearer, API Key & role-based authorization         │
│ 11. Retry Pattern & Resilience      : Multi-URI endpoint failover, retries & fallback generator        │
└───────────────────────────────────┬────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                             WebLogic SOA Suite BPEL Composites / Legacy                                │
│          (BorrowerIntake, Underwriting, DocumentProcessing, Compliance, Pricing, Closing)             │
└────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Implemented OSB 12c Integration Patterns

### 1. Service Proxy Pattern (`ServiceProxyPattern`)
- **Purpose**: Decouples external API consumers from backend implementations using OSB Proxy Services.
- **Key Artifacts**:
  - [`BorrowerIntakeProxy.proxy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/BorrowerIntakeOSB/ProxyServices/BorrowerIntakeProxy.proxy)
  - [`UnderwritingProxy.proxy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/UnderwritingOSB/ProxyServices/UnderwritingProxy.proxy)
  - [`DocumentProcessingProxy.proxy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/DocumentProcessingOSB/ProxyServices/DocumentProcessingProxy.proxy)

### 2. Content-Based Routing Pattern (`ContentBasedRoutingPattern`)
- **Purpose**: Evaluates message body elements (`LoanType`, `LoanAmount`, `DocumentCategory`) and branches to specialized backend services (Jumbo vs Conforming vs FHA).
- **Key Artifacts**:
  - [`ContentBasedRoutingPipeline.pipeline`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/ContentBasedRouting/Pipelines/ContentBasedRoutingPipeline.pipeline)
  - [`JumboUnderwritingBusinessService.bsp`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/ContentBasedRouting/BusinessServices/JumboUnderwritingBusinessService.bsp)
  - [`FHAUnderwritingBusinessService.bsp`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/ContentBasedRouting/BusinessServices/FHAUnderwritingBusinessService.bsp)

### 3. Message Transformation Pattern (`MessageTransformationPattern`)
- **Purpose**: Translates caller data structures into canonical enterprise XML models via XQuery (`.xqy`).
- **Key Artifacts**:
  - [`BorrowerIntakeReqToSOAMap.xqy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/common/transformations/BorrowerIntakeReqToSOAMap.xqy)
  - [`UnderwritingReqToCreditBureauMap.xqy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/common/transformations/UnderwritingReqToCreditBureauMap.xqy)
  - [`PricingRequestToLLPAMap.xqy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/common/transformations/PricingRequestToLLPAMap.xqy)

### 4. Service Virtualization Pattern (`ServiceVirtualizationPattern`)
- **Purpose**: Abstract endpoint locations, support dynamic routing, and enable offline mock testing for external dependencies (Credit Bureaus, Fannie Mae / Freddie Mac AUS).
- **Key Artifacts**:
  - [`ServiceVirtualizationPipeline.pipeline`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/ServiceVirtualization/Pipelines/ServiceVirtualizationPipeline.pipeline)
  - [`MockCreditBureauBusinessService.bsp`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/ServiceVirtualization/BusinessServices/MockCreditBureauBusinessService.bsp)
  - [`MockAutomatedUnderwritingBusinessService.bsp`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/ServiceVirtualization/BusinessServices/MockAutomatedUnderwritingBusinessService.bsp)

### 5. Protocol Bridging Pattern (`ProtocolBridgingPattern`)
- **Purpose**: Bridges disparate transport protocols across consumer endpoints and backend systems (REST JSON HTTP, JMS Queue, File System to SOAP/REST).
- **Key Artifacts**:
  - [`BorrowerIntakeRESTProxy.proxy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/ProtocolBridging/ProxyServices/BorrowerIntakeRESTProxy.proxy) (REST to SOAP)
  - [`JMSDocumentIngestProxy.proxy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/ProtocolBridging/ProxyServices/JMSDocumentIngestProxy.proxy) (JMS Queue to SOAP)
  - [`FilePayloadBridgeProxy.proxy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/ProtocolBridging/ProxyServices/FilePayloadBridgeProxy.proxy) (File to SOAP)

### 6. Service Aggregation Pattern (`ServiceAggregationPattern`)
- **Purpose**: Scatter-Gather orchestration: concurrently/sequentially calls multiple backend services (Credit Bureau + DTI Engine + Decision Engine) and aggregates their responses into a single credit evaluation report.
- **Key Artifacts**:
  - [`UnderwritingAggregationPipeline.pipeline`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/ServiceAggregation/Pipelines/UnderwritingAggregationPipeline.pipeline)
  - [`AggregateUnderwritingResults.xqy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/ServiceAggregation/Transformations/AggregateUnderwritingResults.xqy)

### 7. Publish-Subscribe Pattern (`PublishSubscribePattern`)
- **Purpose**: Dispatches event notifications (`LoanStatusChangedEvent`) to JMS Topic (`jms/LoanEventsTopic`) via OSB `<con3:publish>` stage for multiple independent subscriber services.
- **Key Artifacts**:
  - [`PublishEventPipeline.pipeline`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/PublishSubscribe/Pipelines/PublishEventPipeline.pipeline)
  - [`AuditTopicSubscriberProxy.proxy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/PublishSubscribe/ProxyServices/AuditTopicSubscriberProxy.proxy)
  - [`NotificationTopicSubscriberProxy.proxy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/PublishSubscribe/ProxyServices/NotificationTopicSubscriberProxy.proxy)

### 8. Asynchronous Messaging Pattern (`AsynchronousMessagingPattern`)
- **Purpose**: Supports Fire-and-Forget processing and Asynchronous Request-Reply (returns immediate HTTP 202 Accepted tracking token, processes payload asynchronously via queue callback).
- **Key Artifacts**:
  - [`AsyncAuditLoggingProxy.proxy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/AsynchronousMessaging/ProxyServices/AsyncAuditLoggingProxy.proxy) (One-Way)
  - [`AsyncDocumentProcessingPipeline.pipeline`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/AsynchronousMessaging/Pipelines/AsyncDocumentProcessingPipeline.pipeline) (HTTP 202 Ack)
  - [`AsyncDocCallbackPipeline.pipeline`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/AsynchronousMessaging/Pipelines/AsyncDocCallbackPipeline.pipeline) (Callback)

### 9. Logging & Audit Pattern (`LoggingAuditPattern`)
- **Purpose**: Centralized correlation tracking (`X-Correlation-ID`, `X-Tracking-ID`), pipeline logging (`<con2:log>`), and enterprise error fault handling (`FaultHandler.pipeline`).
- **Key Artifacts**:
  - [`CentralizedAuditPipeline.pipeline`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/LoggingAndAudit/Pipelines/CentralizedAuditPipeline.pipeline)
  - [`FaultHandler.pipeline`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/common/faults/FaultHandler.pipeline)

### 10. Security Gateway Pattern (`SecurityGatewayPattern`)
- **Purpose**: Enforces OAuth 2.0 JWT Bearer token validation, API Key checks, and role-based authorization (`ROLE_UNDERWRITER`, `ROLE_LOAN_OFFICER`) at OSB gateway entry points.
- **Key Artifacts**:
  - [`SecurityGatewayPipeline.pipeline`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/SecurityGateway/Pipelines/SecurityGatewayPipeline.pipeline)
  - [`SecurityGatewayProxy.proxy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/SecurityGateway/ProxyServices/SecurityGatewayProxy.proxy)

### 11. Retry Pattern & Resilience (`RetryPatternAndResilience`)
- **Purpose**: Fault tolerance, multi-URI endpoint failover (Primary, Secondary, DR), OSB transport retries (`<con:retry-count>`), stage error handlers, and fallback quote generation (`GenerateFallbackResponse.xqy`).
- **Key Artifacts**:
  - [`ResilientBusinessService.bsp`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/RetryAndResilience/BusinessServices/ResilientBusinessService.bsp)
  - [`ResiliencePipeline.pipeline`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/RetryAndResilience/Pipelines/ResiliencePipeline.pipeline)
  - [`GenerateFallbackResponse.xqy`](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/osb-services/patterns/RetryAndResilience/Transformations/GenerateFallbackResponse.xqy)

---

## Build Verification

Build `lendwise-orchestration` with Maven:
```powershell
cd c:\ramu\Project_Assignment\RapidX\FreddeMac_Project_RapidX\Work\LendWise-Mortgage-System\lendwise-orchestration
mvn clean install
```
