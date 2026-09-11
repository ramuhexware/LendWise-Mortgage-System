**LendWise is a layered mortgage origination demo:** a JSP UI talks to Oracle OSB/SOA orchestration, which does the business workflow, then calls a Spring Boot pass-through that forwards REST into Gold Field microservices on MongoDB.

`java-parser-patterns/` is **not** part of the loan path. It is a stand-alone sample pack for a Java parser.

---

## How a request travels

```
Borrower / loan officer (browser)
        │  HTTP  :8080/lendwise
        ▼
lendwise-ui  (Spring MVC + JSP)
        │  REST  OrchestrationClient → WebLogic :7001
        ▼
OSB proxy  (virtual front door; 6 flows)
        │  SOAP/HTTP to matching SOA composite
        ▼
SOA BPEL  (rules, adapters, Java embed)
        │  REST
        ▼
lendwise-passthrough  (WebClient + circuit breaker + audit)
        │  REST
        ▼
gold-field-services  (12 Spring Boot apps, MongoDB / Kafka / AMQ)
```

In the UI today, `OrchestrationClient` calls SOA REST endpoints such as `/BorrowerIntakeComposite/BorrowerIntakeRESTService` directly. OSB (`BorrowerIntakeOSB`, `UnderwritingOSB`, …) is the intended gateway in front of those composites.

---

## Modules

| Layer | Role |
|--------|------|
| `lendwise-ui` | Screens: new loan, details, documents, underwriting, pricing, closing. Port **8080**, context `/lendwise`. |
| `lendwise-orchestration` | OSB proxies/pipelines + 6 BPEL composites + Java adapters (`DTICalculator`, `DecisionEngine`, …) + Oracle DB adapters. |
| `lendwise-passthrough` | Thin REST router. Controllers under `/api/borrowers`, `/api/documents`, `/api/underwriting`, `/api/compliance`, `/api/pricing`, `/api/closing`. Audits to Mongo; retries via Resilience4j. |
| `gold-field-services` | Domain services: borrower, KYC, document, credit, underwriting, compliance, pricing, rate lock, amortization, closing, e-sign, funding, notification. |
| `java-parser-patterns` | Parser test fixtures only. |

---

## Loan lifecycle (the six flows)

These are the six SOA/OSB processes. A real loan would walk them in roughly this order; each can also be invoked on its own.

### 1. Borrower intake and pre-qualification
**UI:** `POST /loans` → `LoanApplicationController` → `createLoan`.

**BPEL** `BorrowerIntakeProcess`:
1. Receive application, generate a loan id.
2. Embedded Java: DTI / LTV / PITI / QM check (`DTICalculator`).
3. Call pass-through `createBorrower` → Gold Field `borrower-service` (`/api/borrowers`, Mongo).
4. Call KYC `verifyIdentity` → `kyc-service`.
5. Reply `SUBMITTED` with loan number `LW-…`.

### 2. Document processing
**UI:** `/loans/{loanId}/documents`.

**BPEL** `DocumentProcessingProcess`: OCR/classify in Java, save via pass-through to `document-service`, write files through File/FTP adapters.

### 3. Automated underwriting
**UI:** `/loans/{loanId}/underwriting`.

**BPEL** `UnderwritingProcess`:
1. Parallel credit pulls (Equifax / Experian / TransUnion).
2. `DecisionEngine`: FICO, DTI, LTV → `APPROVE` / `APPROVE_ELIGIBLE` / `REFER` / `DECLINE` (and PTD/PTF conditions).
3. Agency AUS (DU/LPA-style) submit.
4. Persist decision in **Oracle**.
5. Notify Gold Field `underwriting-service` via pass-through.

### 4. Compliance
**BPEL** `ComplianceProcess`: TRID and QM/ATR Java checks, fee-tolerance validation, persist, notify `compliance-service`.

### 5. Pricing and rate lock
**UI:** `/loans/{loanId}/pricing`.

**BPEL** `PricingProcess`: investor pricing, LLPA/rate calc in Java, persist rate lock, notify `pricing-service` (and conceptually `ratelock-service` / amortization).

### 6. Closing disclosure, e-sign, funding
**UI:** `/loans/{loanId}/closing`.

**BPEL** `ClosingDisclosureProcess`: title figures, cash-to-close, persist CD, e-sign, notification, Gold Field `closing-service` (plus `esign-service` / `funding-service` as domain stores).

---

## Data stores and protocols

- **Oracle** — orchestration: underwriting decisions, rate locks, compliance, closing records (DB adapters + stored procs such as `sp_calculate_dti.sql`).
- **MongoDB** — Gold Field domain documents and pass-through audit logs.
- **REST** — UI → SOA (as coded), SOA → pass-through → Gold Field.
- **SOAP** — OSB proxies, BPEL partner links, credit bureau / AUS style services.
- **Kafka / AMQ** — async messaging from pass-through and Gold Field.

---

## Mental model

Think of **OSB + BPEL as the mortgage “conductor”** (sequence, rules, adapters) and **Gold Field as the system of record** (borrower, docs, decisions, pricing, closing). The pass-through is a **boundary hop**: it does not implement mortgage logic; it routes, times out, retries, and logs.

If you want a next-level walkthrough, we can go step-by-step through one flow (for example underwriting) with the exact BPEL invokes and REST paths.
