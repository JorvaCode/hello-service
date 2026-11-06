# Requirement: `REQ-OBS-002` - Automated alert definitions

## Problem

Phase 13 detected failures manually: a probe, a log line or a consumer report. No alert rule
named the metric, threshold, duration, severity or response for a failure mode, so detection
depended on an operator being present and knowing where to look. `IMP-004` records that gap.

The project does not operate a Prometheus, Alertmanager or paging service. The requirement is
therefore for reviewable alert definitions, not for a running monitoring stack.

## Objective

Define Prometheus alert rules that map every failure mode in the observability record to a
query, a duration, a severity and a runbook, and document the scrape/probe jobs they require.

## Scope

### In scope

- Add `ops/prometheus/hello-service-alerts.yml` with rules for process down, 5xx rate, p95
  latency, recent restart, liveness probe failure, readiness probe failure, the
  `/api/hello` contract probe and missing request correlation.
- Give every rule an `expr`, `for`, `labels.severity`, `labels.service`, `annotations.summary`,
  `annotations.description` and `annotations.runbook_url`.
- Document the application scrape job and the three blackbox jobs in
  `ops/prometheus/README.md`.
- Add an automated test that loads the YAML and asserts the rule inventory and required fields.

### Out of scope

- Deploying Prometheus, Alertmanager, a blackbox exporter or a paging service.
- Alert routing, inhibition, silences, on-call schedules or SLO/error-budget policy.
- Dashboards, a metrics backend, tracing or log shipping.
- Tuning thresholds against production traffic; the values are local-PoC defaults.
- Changing `/api/hello`, `/api/status` or the Actuator surface implemented by `REQ-OBS-001`.

## Functional requirements

| ID | Requirement | Verification |
| --- | --- | --- |
| FR-01 | Every rule shall name a Prometheus expression, a `for` duration, a severity, a service label and a runbook URL. | `AlertRulesTest.rulesCoverFailureModesAndNameResponse`. |
| FR-02 | The rules shall cover `FM-01` ... `FM-07` from the observability record: process down, liveness, readiness, hello contract, startup/restart, 5xx/latency and correlation. | `AlertRulesTest` plus the mapping table in the observability record. |
| FR-03 | The access log shall increment `hello_service_access_log_missing_request_id_total` when it emits a line without a `requestId`, so `FM-07` has a metric signal. | `RequestAccessLogFilterTest.countsEntriesWithoutRequestId`; Prometheus output review. |
| FR-04 | The rule file and required scrape/probe jobs shall be documented separately from a running deployment. | Review of `ops/prometheus/README.md`; README names the application job and three blackbox jobs. |
| FR-05 | The rules shall not claim a deployed alerting path; the observability record shall state that evaluation and paging are not operated. | Review of the observability record's limitation section. |

## Non-functional requirements

| ID | Quality attribute | Requirement | Verification |
| --- | --- | --- | --- |
| NFR-01 | Operability | A responder shall reach the same runbook from the alert annotation without asking the author. | Every `runbook_url` targets an anchor in `blueprint/observability/hello-service.md`; review. |
| NFR-02 | Maintainability | Rules shall live in version control as declarative YAML with no custom exporter or code path. | `ops/prometheus/hello-service-alerts.yml`; no runtime dependency added. |
| NFR-03 | Reviewability | The rule file shall parse with a standard YAML parser and be checked by the test suite. | `AlertRulesTest`; `python -c "import yaml; ..."` parse. |
| NFR-04 | Scope honesty | No deployment, routing or paging capability shall be implied. | `ops/prometheus/README.md` and the observability record state the boundary. |

## Acceptance criteria

| ID | Covers | Given | When | Then (observable condition) | Verification |
| --- | --- | --- | --- | --- | --- |
| AC-01 | FR-01, NFR-03 | The repository is checked out. | The test suite runs. | The YAML parses and every rule has `expr`, `for`, `labels.severity`, `labels.service` and `annotations.runbook_url`. | `AlertRulesTest.rulesCoverFailureModesAndNameResponse`. |
| AC-02 | FR-02 | The observability record names seven failure modes. | The rule inventory is read. | Eight rule names cover the seven failure modes, with 5xx and latency both mapped to `FM-05`. | Same test; observability mapping table. |
| AC-03 | FR-03 | A request reaches the access log without a `requestId`. | The request is served. | `hello_service_access_log_missing_request_id_total` increases by one. | `RequestAccessLogFilterTest.countsEntriesWithoutRequestId`. |
| AC-04 | FR-04, NFR-01 | The rule file is loaded. | A responder follows a `runbook_url`. | The URL resolves to an existing runbook anchor in the observability record. | Review of every rule; anchor check. |
| AC-05 | FR-05, NFR-04 | The change is reviewed. | The limitations are read. | The record says rules are definitions only; no Prometheus, Alertmanager or paging is operated. | Review of `blueprint/observability/hello-service.md` and `ops/prometheus/README.md`. |

## Traceability

- Realised by `ops/prometheus/hello-service-alerts.yml`, `ops/prometheus/README.md`,
  `AlertRulesTest`, the missing-request-id counter in `RequestAccessLogFilter` and the
  failure-mode mapping in `blueprint/observability/hello-service.md`.
- Design: `blueprint/design/observability-improvements.md`.
- Architecture/ADR: `blueprint/architecture/observability-improvements.md`, ADR-014.
- Improvement item: `IMP-004`.
- Change: `blueprint/changes/observability-improvements.md`.
- Testing: `blueprint/testing/observability-improvements.md`.
- Review: `blueprint/reviews/observability-improvements.md`.
- No new release version; the change ships in the existing `0.1.0` artifact line.
