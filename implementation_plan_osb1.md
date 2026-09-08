# Include Oracle Service Bus (OSB) in LendWise Mortgage System Architecture

Integrate Oracle Service Bus (OSB 12c) proxy services, pipelines, and business services into `LendWise-Mortgage-System` alongside existing Oracle SOA Suite BPEL Composites to establish a combined OSB + SOA enterprise orchestration layer in `lendwise-orchestration`.

## User Review Required

> [!IMPORTANT]
> - **Architecture Shift**: OSB proxy services will act as the front door / API gateway for all 6 functional flows, virtualizing services and routing requests to downstream SOA BPEL Composites (`BorrowerIntakeComposite`, `DocumentProcessingComposite`, `UnderwritingComposite`, `ComplianceComposite`, `PricingEngineComposite`, and `ClosingDisclosureComposite`).
> - **Build Integration**: `lendwise-orchestration/pom.xml` will be updated to include `<module>osb-services</module>` so Maven builds all OSB resources alongside Java adapters and SOA composites.

## Open Questions

> [!NOTE]
> None at this time. The OSB services and WSDL/XSD definitions are fully aligned with the 6 functional flows.

## Proposed Changes

### LendWise Orchestration (`lendwise-orchestration`)

---

#### [MODIFY] [pom.xml](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/lendwise-orchestration/pom.xml)
- Add `<module>osb-services</module>` to the root parent POM of `lendwise-orchestration`.

#### [NEW] `lendwise-orchestration/osb-services/pom.xml`
- Create parent Maven POM for packaging OSB resources (proxy, pipeline, business service, WSDL, XSD).

#### [NEW] `lendwise-orchestration/osb-services/common/`
- `schemas/CommonFault.xsd`: Standardized fault schema for enterprise error responses.
- `faults/FaultHandler.pipeline`: Common OSB pipeline fault handler for error logging and fault transformation.

#### [NEW] `lendwise-orchestration/osb-services/BorrowerIntakeOSB/`
- `ProxyServices/BorrowerIntakeProxyService.proxy`: Inbound SOAP/HTTP proxy endpoint.
- `Pipelines/BorrowerIntakePipeline.pipeline`: Pipeline stage for validation, header enrichment, and dispatching to SOA service.
- `BusinessServices/BorrowerIntakeSOABusinessService.bsp`: Outbound binding targeting `BorrowerIntakeComposite`.
- `WSDLs/BorrowerIntakeOSB.wsdl` & `Schemas/BorrowerIntakeOSB.xsd`: Request/response contract definitions.

#### [NEW] `lendwise-orchestration/osb-services/DocumentProcessingOSB/`
- `ProxyServices/DocumentProcessingProxyService.proxy`: Document OCR and classification entry point.
- `Pipelines/DocumentProcessingPipeline.pipeline`: Pipeline processing for document metadata.
- `BusinessServices/DocumentProcessingSOABusinessService.bsp`: Outbound binding targeting `DocumentProcessingComposite`.
- `WSDLs/DocumentProcessingOSB.wsdl` & `Schemas/DocumentProcessingOSB.xsd`: Document processing contracts.

#### [NEW] `lendwise-orchestration/osb-services/UnderwritingOSB/`
- `ProxyServices/UnderwritingProxyService.proxy`: Automated Underwriting System (AUS) entry point.
- `Pipelines/UnderwritingPipeline.pipeline`: Credit bureau payload validation and routing pipeline.
- `BusinessServices/UnderwritingSOABusinessService.bsp`: Outbound binding targeting `UnderwritingComposite`.
- `WSDLs/UnderwritingOSB.wsdl` & `Schemas/UnderwritingOSB.xsd`: Underwriting contracts.

#### [NEW] `lendwise-orchestration/osb-services/ComplianceOSB/`
- `ProxyServices/ComplianceProxyService.proxy`: TRID & QM/ATR audit entry point.
- `Pipelines/CompliancePipeline.pipeline`: Compliance check pipeline.
- `BusinessServices/ComplianceSOABusinessService.bsp`: Outbound binding targeting `ComplianceComposite`.
- `WSDLs/ComplianceOSB.wsdl` & `Schemas/ComplianceOSB.xsd`: Compliance contracts.

#### [NEW] `lendwise-orchestration/osb-services/PricingEngineOSB/`
- `ProxyServices/PricingEngineProxyService.proxy`: Rate lock & LLPA pricing engine entry point.
- `Pipelines/PricingEnginePipeline.pipeline`: Rate calculation pipeline.
- `BusinessServices/PricingEngineSOABusinessService.bsp`: Outbound binding targeting `PricingEngineComposite`.
- `WSDLs/PricingEngineOSB.wsdl` & `Schemas/PricingEngineOSB.xsd`: Pricing engine contracts.

#### [NEW] `lendwise-orchestration/osb-services/ClosingDisclosureOSB/`
- `ProxyServices/ClosingDisclosureProxyService.proxy`: Closing disclosure & E-sign entry point.
- `Pipelines/ClosingDisclosurePipeline.pipeline`: E-sign audit pipeline.
- `BusinessServices/ClosingDisclosureSOABusinessService.bsp`: Outbound binding targeting `ClosingDisclosureComposite`.
- `WSDLs/ClosingDisclosureOSB.wsdl` & `Schemas/ClosingDisclosureOSB.xsd`: Closing disclosure contracts.

#### [NEW] `lendwise-orchestration/docker/`
- `Dockerfile`: WebLogic 12c Server image setup with OSB & SOA domain configuration.
- `docker-compose.yml`: Container orchestration setup for WebLogic OSB + SOA instance.

---

### Root Documentation

#### [MODIFY] [README.md](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/LendWise-Mortgage-System/README.md)
- Verify that OSB + SOA combination architecture diagram, directory layout, and build instructions are accurately documented.

## Verification Plan

### Automated Build Verification
- Execute Maven build in `lendwise-orchestration`:
  ```powershell
  cd c:\ramu\Project_Assignment\RapidX\FreddeMac_Project_RapidX\Work\LendWise-Mortgage-System\lendwise-orchestration
  mvn clean install
  ```
- Verify all Java adapters and OSB services build successfully without errors.

### Manual Verification
- Check directory structure of `lendwise-orchestration/osb-services` to confirm all 6 OSB functional flows and common schemas exist.
- Verify XML schema validity of WSDLs, XSDs, proxy services (`.proxy`), pipelines (`.pipeline`), and business services (`.bsp`).
