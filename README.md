# LendWise Mortgage System

A comprehensive loan mortgage application system demonstrating Oracle SOA Suite integration with Spring Boot microservices.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              LendWise UI                                        │
│                    (Tomcat - JSP/Angular - Java 8/17)                           │
└─────────────────────────────┬───────────────────────────────────────────────────┘
                              │ REST or SOAP
                              ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                        LendWise Orchestration                                   │
│              (WebLogic + OSB + SOA Suite BPEL - Java 8)                         │
│  • OSB Proxy Services (entry points)                                            │
│  • SOA Suite composite.xml + BPEL orchestration                                 │
│  • Oracle Database                                                              │
│  • File/FTP/Database/JMS Adapters                                               │
│  • Java Adapters (embedded in BPEL)                                             │
└──────────────┬──────────────────────────────────────────────────────────────────┘
               │ REST (WebClient)
               ▼
┌──────────────────────────────────┐
│   LendWise Pass-through Service  │
│   (OCP Bronze Field)             │
│   Java 17, Spring Boot 3.3       │
│   @RestController + WebClient    │
└──────────────┬───────────────────┘
               │ REST (WebClient)
               ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                    LendWise Gold Field Services (Amazon EKS)                    │
│                         12 Microservices                                        │
│   Java 17, Spring Boot 3.3, MongoDB Atlas, Kafka, AMQ                           │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## Project Structure

| Module | Description | Build Tool |
|--------|-------------|------------|
| `lendwise-ui/` | Web UI (Tomcat, JSP/Angular) | Maven |
| `lendwise-orchestration/` | SOA Suite + OSB (WebLogic) | Maven |
| `lendwise-passthrough-service/` | Pass-through service (OCP) | Gradle |
| `gold-field-services/` | 12 microservices (EKS) | Gradle |

## 6 Functional Flows

1. **Borrower Intake & Pre-Qualification** - KYC, DTI/LTV/PITI calculations
2. **Document Processing** - OCR, AI classification, File/FTP adapters
3. **Automated Underwriting (AUS)** - Credit bureau, decision engine
4. **Compliance & Audit** - TRID, QM/ATR rules, defect tracking
5. **Pricing Engine** - LLPA, rate lock workflow
6. **Closing Disclosure** - E-Sign, funding audit

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
