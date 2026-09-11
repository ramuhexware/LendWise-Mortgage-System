# LendWise Mortgage System

A comprehensive loan mortgage application system demonstrating Oracle SOA Suite + OSB 12c integration with Spring Boot microservices.

## End-to-End Architecture & Call Chain

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                  LendWise UI                                    │
│                       (Tomcat - JSP / Angular / Java 8)                         │
│   • LoanApplicationController, DocumentController, UnderwritingController       │
│   • OrchestrationClient (Spring RestTemplate with X-Correlation-ID)             │
└────────────────────────────────────────┬────────────────────────────────────────┘
                                         │ HTTP REST / SOAP (JSON/XML)
                                         ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           LendWise OSB Gateway Layer                            │
│                              (Oracle Service Bus 12c)                           │
│   • Service Proxies: BorrowerIntakeProxy, UnderwritingProxy, DocumentProxy, etc.│
│   • Protocol Bridging: REST HTTP ↔ SOAP XML, JMS Queue ↔ SOAP, File ↔ SOAP      │
│   • OSB Pipelines: Security Gateway (JWT/API Key), Logging & Audit, CBR         │
│   • Message Transformations: XQuery 1.0 (.xqy) maps to enterprise schemas        │
│   • Resilience: Multi-URI failover, transport retries & circuit breakers       │
└────────────────────────────────────────┬────────────────────────────────────────┘
                                         │ SOAP / Direct Binding
                                         ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            LendWise Orchestration                               │
│                         (WebLogic + SOA Suite BPEL)                             │
│   • SOA BPEL Composites: BorrowerIntake, Document, Underwriting, Compliance     │
│   • Java Adapters: DTICalculator, DecisionEngine, OCRProcessor, Amortization     │
│   • Oracle Database: Persistence for audit, rate locks, underwriting decisions   │
└────────────────────────────────────────┬────────────────────────────────────────┘
                                         │ HTTP REST (Spring WebClient)
                                         ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                      LendWise Pass-through Service                              │
│                      (OpenShift Container Platform - OCP)                        │
│   • Java 17, Spring Boot 3.3, @RestController Router                           │
│   • Spring WebClient with Resilience4j Circuit Breaker & Retry                  │
│   • Centralized MongoDB Audit Logging                                           │
└────────────────────────────────────────┬────────────────────────────────────────┘
                                         │ HTTP REST / JMS / Kafka
                                         ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                       Gold Field Microservices (Amazon EKS)                     │
│                            12 Domain Microservices                              │
│   • Services: borrower, kyc, document, credit, underwriting, compliance,        │
│     pricing, ratelock, amortization, closing, esign, funding, notification      │
│   • Persistence & Messaging: MongoDB Atlas, Apache Kafka, ActiveMQ              │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## Detailed Flow Call Chains (UI → OSB → SOA → Pass-through → Gold Field)

### 1. Borrower Intake & Pre-Qualification Flow
```
[User Browser]
   │ HTTP POST /loans
   ▼
[lendwise-ui]
   │ LoanApplicationController.createLoan() ──► OrchestrationClient.createLoan()
   │ Header: X-Correlation-ID: <uuid>
   ▼
[lendwise-orchestration : OSB]
   │ BorrowerIntakeRESTProxy.proxy ──► BorrowerIntakeReqToSOAMap.xqy
   │ ──► CentralizedAuditPipeline.pipeline (Logs correlation & tracking ID)
   ▼
[lendwise-orchestration : SOA BPEL]
   │ BorrowerIntakeProcess BPEL Composite
   │ ──► Embedded Java Adapter: DTICalculator.java (DTI, LTV, PITI calculations)
   │ ──► Persists application in Oracle Database
   ▼
[lendwise-passthrough-service]
   │ PassthroughBorrowerController (@RestController)
   │ ──► WebClient call with Resilience4j circuit breaker
   ▼
[gold-field-services]
   │ 1. borrower-service (POST /api/borrowers ──► MongoDB Atlas)
   │ 2. kyc-service (POST /api/kyc/verify ──► Identity Verification)
```

### 2. Document Processing Flow
```
[User Browser]
   │ HTTP POST /documents/upload
   ▼
[lendwise-ui]
   │ DocumentController.upload() ──► OrchestrationClient.submitDocument()
   ▼
[lendwise-orchestration : OSB]
   │ JMSDocumentIngestProxy / FilePayloadBridgeProxy ──► DocumentProcessingProxy
   │ ──► DocumentProcessingPipeline.pipeline
   ▼
[lendwise-orchestration : SOA BPEL]
   │ DocumentProcessingProcess BPEL Composite
   │ ──► Embedded Java Adapter: OCRProcessor.java (OCR classification & extraction)
   │ ──► File/FTP Adapter (Archives document binary)
   ▼
[lendwise-passthrough-service]
   │ PassthroughDocumentController
   ▼
[gold-field-services]
   │ 1. document-service (POST /api/documents ──► Document Store / S3 + MongoDB)
   │ 2. notification-service (Publish document uploaded event to ActiveMQ)
```

### 3. Automated Underwriting System (AUS) Flow
```
[User Browser]
   │ HTTP POST /underwriting/evaluate
   ▼
[lendwise-ui]
   │ UnderwritingController.evaluate() ──► OrchestrationClient.getUnderwritingDecision()
   ▼
[lendwise-orchestration : OSB]
   │ SecurityGatewayProxy ──► SecurityGatewayPipeline (OAuth 2.0 JWT & API Key Check)
   │ ──► ContentBasedRoutingPipeline (Branching: Jumbo vs FHA vs Conforming)
   │ ──► UnderwritingAggregationPipeline (Scatter-Gather: Bureau + Decision Engine)
   ▼
[lendwise-orchestration : SOA BPEL]
   │ UnderwritingProcess BPEL Composite
   │ ──► Embedded Java Adapter: DecisionEngine.java (FICO, DTI/LTV matrix)
   │ ──► Oracle DB Adapter (Executes sp_calculate_underwriting_decision)
   ▼
[lendwise-passthrough-service]
   │ PassthroughUnderwritingController
   ▼
[gold-field-services]
   │ 1. credit-service (GET /api/credit/report ──► Equifax/Experian/TransUnion)
   │ 2. underwriting-service (POST /api/underwriting/decision ──► Mongo + Kafka topic `underwriting-events`)
```

### 4. Compliance & TRID Audit Flow
```
[User Browser]
   │ HTTP POST /compliance/verify
   ▼
[lendwise-ui]
   │ ComplianceController.check() ──► OrchestrationClient.runComplianceCheck()
   ▼
[lendwise-orchestration : OSB]
   │ ComplianceProxy.proxy ──► CompliancePipeline.pipeline (TRID & QM/ATR rules)
   ▼
[lendwise-orchestration : SOA BPEL]
   │ ComplianceProcess BPEL Composite (Fee tolerance & QM/ATR audit validation)
   ▼
[lendwise-passthrough-service]
   │ PassthroughComplianceController
   ▼
[gold-field-services]
   │ compliance-service (POST /api/compliance/audit ──► MongoDB Compliance Store)
```

### 5. Pricing Engine & Rate Lock Flow
```
[User Browser]
   │ HTTP POST /pricing/calculate
   ▼
[lendwise-ui]
   │ PricingController.calculate() ──► OrchestrationClient.calculatePricing()
   ▼
[lendwise-orchestration : OSB]
   │ PricingEngineProxy.proxy ──► ResiliencePipeline.pipeline
   │ (Multi-URI endpoint failover, retries & GenerateFallbackResponse.xqy on failure)
   ▼
[lendwise-orchestration : SOA BPEL]
   │ PricingProcess BPEL Composite
   │ ──► Embedded Java Adapter: AmortizationCalculator.java
   │ ──► Persists Rate Lock commitment in Oracle Database
   ▼
[lendwise-passthrough-service]
   │ PassthroughPricingController
   ▼
[gold-field-services]
   │ 1. pricing-service (POST /api/pricing/quote ──► LLPA Grid Lookup)
   │ 2. ratelock-service (POST /api/ratelock/lock ──► Rate Lock Store)
   │ 3. amortization-service (POST /api/amortization/schedule ──► Schedule Store)
```

### 6. Closing Disclosure, E-Sign & Funding Flow
```
[User Browser]
   │ HTTP POST /closing/generate
   ▼
[lendwise-ui]
   │ ClosingController.generateCD() ──► OrchestrationClient.getClosingDisclosure()
   ▼
[lendwise-orchestration : OSB]
   │ ClosingDisclosureProxy.proxy ──► PublishEventPipeline.pipeline
   │ (Publishes LoanApprovedEvent to jms/LoanEventsTopic)
   ▼
[lendwise-orchestration : SOA BPEL]
   │ ClosingDisclosureProcess BPEL Composite (Cash-to-close & fee balancing)
   ▼
[lendwise-passthrough-service]
   │ PassthroughClosingController
   ▼
[gold-field-services]
   │ 1. closing-service (POST /api/closing/cd ──► CD Generation)
   │ 2. esign-service (POST /api/esign/package ──► E-Sign Package)
   │ 3. funding-service (POST /api/funding/disburse ──► Wire Transfer Audit)
   │ 4. notification-service (JMS Topic Subscribers: AuditTopicSubscriberProxy & NotificationTopicSubscriberProxy)
```

## Project Structure

| Module | Description | Build Tool |
|--------|-------------|------------|
| `lendwise-ui/` | Web UI (Tomcat, JSP/Angular) | Maven |
| `lendwise-orchestration/` | SOA Suite + OSB 12c (WebLogic) | Maven |
| `lendwise-passthrough-service/` | Pass-through service (OCP) | Gradle |
| `gold-field-services/` | 12 microservices (EKS) | Gradle |

## 11 OSB Integration Patterns Location

All OSB proxy services, pipelines, transformations, and resilience configs are located under:
`lendwise-orchestration/osb-services/`

See [OSB Integration Patterns Documentation](docs/OSB_PATTERNS.md) for full architectural details.

## Build Instructions

### LendWise UI (Maven)
```bash
cd lendwise-ui
mvn clean install
```

### LendWise Orchestration (Maven)
```bash
cd lendwise-orchestration
mvn clean install
```

### LendWise Pass-through Service (Gradle)
```bash
cd lendwise-passthrough-service
./gradlew build
```

### Gold Field Services (Gradle)
```bash
cd gold-field-services/borrower-service
./gradlew build
```

## Integration Patterns

| Pattern | Technology | Location |
|---------|------------|----------|
| OSB Proxy & Gateways | OSB 12c (.proxy / .pipeline) | `lendwise-orchestration/osb-services` |
| REST | Spring WebClient | Pass-through, Gold Field |
| SOAP | JAX-WS, WSDL | Orchestration |
| File Processing | File/FTP Adapter | Orchestration |
| Database | DB Adapter | Orchestration |
| JMS | JMS Adapter | Orchestration |
| Kafka | KafkaTemplate | Gold Field |
| AMQ | JmsTemplate | Gold Field |

## Database

- **Oracle DB** - LendWise Orchestration (schemas in `lendwise-orchestration/database/`)
- **MongoDB Atlas** - Gold Field Services (schemas in `*/src/main/resources/mongo/`)

## Documentation

- [OSB Integration Patterns](docs/OSB_PATTERNS.md)
- [Architecture Details](docs/ARCHITECTURE.md)
- [Integration Patterns](docs/INTEGRATION_PATTERNS.md)
- [Database Schema](docs/DATABASE_SCHEMA.md)
- [API Contracts](docs/API_CONTRACTS.md)
g Disclosure** - E-Sign, funding audit

## Build Instructions

### LendWise UI (Maven)
```bash
cd lendwise-ui
mvn clean install
```

### LendWise Orchestration (Maven)
```bash
cd lendwise-orchestration
mvn clean install
```

### LendWise Pass-through Service (Gradle)
```bash
cd lendwise-passthrough-service
./gradlew build
```

### Gold Field Services (Gradle)
```bash
cd gold-field-services/borrower-service
./gradlew build
```

## Integration Patterns

| Pattern | Technology | Location |
|---------|------------|----------|
| REST | Spring WebClient | Pass-through, Gold Field |
| SOAP | JAX-WS, WSDL | Orchestration |
| File Processing | File/FTP Adapter | Orchestration |
| Database | DB Adapter | Orchestration |
| JMS | JMS Adapter | Orchestration |
| Kafka | KafkaTemplate | Gold Field |
| AMQ | JmsTemplate | Gold Field |

## Database

- **Oracle DB** - LendWise Orchestration (schemas in `lendwise-orchestration/database/`)
- **MongoDB Atlas** - Gold Field Services (schemas in `*/src/main/resources/mongo/`)

## Documentation

- [Architecture Details](docs/ARCHITECTURE.md)
- [Integration Patterns](docs/INTEGRATION_PATTERNS.md)
- [Database Schema](docs/DATABASE_SCHEMA.md)
- [API Contracts](docs/API_CONTRACTS.md)
