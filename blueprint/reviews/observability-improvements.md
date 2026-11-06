# Code Review: `REQ-OBS-001`/`REQ-OBS-002` — Observability signals and alert definitions

## Review scope

- Requirements: `blueprint/requirements/observability-improvements.md`,
  `blueprint/requirements/observability-alerting.md`.
- Analysis: `blueprint/analisys/observability-improvements.md`.
- Architecture and ADR: `blueprint/architecture/observability-improvements.md`,
  `blueprint/architecture/adr/adr-014-observability-stack.md`.
- Design: `blueprint/design/observability-improvements.md`.
- Implementation: `build.gradle.kts`, `gradle.lockfile`,
  `src/main/resources/application.properties`,
  `src/main/java/com/example/hello/RequestAccessLogFilter.java`,
  `ops/prometheus/hello-service-alerts.yml`, `ops/prometheus/README.md`.
- Tests: `ActuatorEndpointTest`, `RequestAccessLogFilterTest`, `AlertRulesTest` and the
  pre-existing suite.
- Records: `blueprint/testing/observability-improvements.md`,
  `blueprint/observability/hello-service.md`, `blueprint/improvements/continuous-improvements.md`,
  `blueprint/gates.md`, `README.md`, `blueprint/changes/observability-improvements.md`.
- Checklist: `blueprint/07-code-review.md`.
- Evidence: local test run at `37533139e1c950d49050eb584c2d9e0e3de4e932` on 2026-11-06
  (`BUILD SUCCESSFUL`; 28 tests, 0 failures, 0 errors, 0 skipped). PR CI run `42239560605`
  at `fc69901` passed build/test, secret scan, Trivy dependency scan and image identity.

This is a self-review by the agent that produced the change. It is not an independent review.

## Board

### 1. Requirement satisfied?

- **FR-01:** liveness and readiness groups are enabled; both are tested with `200` and
  `"status":"UP"`.
- **FR-02:** `/actuator/prometheus` is tested for `200`, `jvm_memory_used_bytes` and
  `application="hello-service"`.
- **FR-03:** only `health`, `info` and `prometheus` are exposed; `/actuator/env` is tested as
  `404`.
- **FR-04:** health details are hidden; the test asserts no `components` object.
- **FR-05–FR-07:** the five access-log tests cover fields, correlation, query exclusion,
  missing-id counting, server-error status and the exception rethrow.
- **FR-08:** the 17 pre-existing tests pass unchanged; `/api/hello` and `/api/status` are not
  modified.
- **`REQ-OBS-002` FR-01/FR-02:** `AlertRulesTest` asserts eight rules and the required fields;
  the rules map to `FM-01`–`FM-07` plus the `HELLO-001` contract.
- **`REQ-OBS-002` FR-03:** the access filter counts lines without a `requestId`.
- **`REQ-OBS-002` FR-04/FR-05:** every rule carries a runbook URL; the README records the
  scrape/blackbox jobs and states that the rules are definitions only.

No requirement is changed by this review.

### 2. Design consistent with architecture?

- The implementation matches ADR-014: Actuator health groups, Micrometer Prometheus registry,
  a Servlet filter after `RequestIdFilter` and versioned alert rules without a deployed stack.
- The endpoint allowlist, hidden details and application tag match the architecture's
  interfaces and security boundary.
- No custom metrics endpoint, service layer, repository or extra deployable component was
  introduced.
- The design's single-view and quality-scenario descriptions remain accurate.

### 3. Does each new name assert a decision or a behaviour a third party can check?

- `/actuator/health/liveness`, `/actuator/health/readiness`, `/actuator/prometheus` are
  standard, externally checkable paths.
- `application="hello-service"` is a fixed metric tag asserted by the test.
- `RequestAccessLogFilter` and its `access method=... path=... status=... duration_ms=...
  requestId=...` line name observable fields, not implementation intentions.
- `management.endpoint.health.show-details=never` asserts a security behaviour verifiable by
  the health test.
- `HelloServiceDown`, `HelloServiceHigh5xxRate`, `HelloServiceLatencyHigh`,
  `HelloServiceRestarting`, `HelloServiceLivenessProbeFailing`,
  `HelloServiceReadinessProbeFailing`, `HelloServiceHelloContractFailing` and
  `HelloServiceCorrelationMissing` name observable conditions, not intentions.
- `hello_service_access_log_missing_request_id_total` names the counter condition directly.

### 4. Design quality per `standards/code-design.md`?

- The filter is small, single-purpose and uses the existing Servlet contract.
- Configuration is declarative and lives in one properties file.
- The filter rethrows exceptions and does not alter the response.
- No unnecessary abstraction, DTO, factory or wrapper is added.
- Class names and package placement follow the existing `com.example.hello` convention.

### 5. Any pattern or abstraction without a stated problem, evidence and scope?

- No application pattern is applied. The design records the filter's problem (all requests,
  including pre-handler failures, need one correlated line), the evidence (`404`/`405`/filter
  failures), the cost of handler-level or Tomcat-valve alternatives and the single-filter scope.
- The use of standard Actuator/Micrometer is a dependency decision recorded in ADR-014, not an
  unjustified in-house abstraction.

### 6. Error handling appropriate?

- The filter captures the response status after the chain.
- On `IOException`, `ServletException` or `RuntimeException`, it records status `500` and
  rethrows the original exception; it does not swallow or replace it.
- The access line is emitted in `finally`, so success and failure are both covered.
- The Actuator allowlist returns `404` for unapproved endpoints without custom error handling.
- No request payload, query string or body is read or logged.

### 7. Tests adequate?

- Local run at `3753313`: 28 tests, 0 failures, 0 errors, 0 skipped.
- New coverage: 5 Actuator tests, 5 access-log/counter tests and 1 alert-rule test.
- All `REQ-OBS-001` and `REQ-OBS-002` acceptance criteria have a named automated or inspection
  check.
- The test levels are proportionate: integration for Actuator auto-configuration and endpoint
  behavior; unit for the filter and the rule file; the pre-existing suite for compatibility.
- No load/performance or live alert-evaluation test is added because there is no throughput
  requirement and no Prometheus/Alertmanager instance; this is a stated limitation, not a
  silent gap.
- PR CI run `42239560605` at `fc69901` passed, including the secret and Trivy scans; the local
  run remains the commit-bound test evidence.

### 8. Security concerns addressed?

- Only `health`, `info` and `prometheus` are exposed; `/actuator/env` is `404`.
- Health details are hidden.
- The access log excludes query strings and payloads.
- No secret or credential is read or emitted.
- New runtime dependencies are pinned by the BOM and lockfile and covered by the existing
  Trivy scan.
- The alert file contains only metric expressions, labels and public runbook URLs; it holds no
  secret or environment-specific credential.
- Residual risks: the Actuator endpoints are unauthenticated inside the local boundary;
  JVM/process metrics reveal resource information; `X-Request-ID` remains a log-injection
  vector already accepted in `blueprint/reviews/api-status-endpoint.md`.

### 9. Performance concerns addressed?

- The access filter adds only in-process timing and one SLF4J call; it performs no network,
  database or disk I/O beyond stdout.
- The metrics endpoint reads an in-process registry.
- No load test exists; the local PoC has no throughput requirement. Log volume grows linearly
  with request rate and would need sampling/retention in a production deployment.

### 10. No unnecessary scope?

- No deployed alerting stack, dashboard, tracing, collector, metrics backend, container
  healthcheck, CD change or new release version is included. `IMP-004` is limited to versioned
  rule definitions and scrape/blackbox documentation.
- `/api/hello`, `/api/status`, `RequestIdFilter`, the Dockerfile and the workflows are
  unchanged.
- The documentation updates are required by the Blueprint lifecycle and the improvement
  closure, not unrelated refactoring.

## Findings

| # | Severity | Finding | Disposition |
| --- | --- | --- | --- |
| F-1 | Info | No independent reviewer exists; this is a self-review by the agent that produced the change. | Recorded as a process limitation; independent approval remains unavailable. |
| F-2 | Info | Eight alert rules are defined, but no Prometheus, Alertmanager, dashboard or paging path evaluates or routes them. | Accepted for the local PoC; `IMP-004` is closed at the definition level and the record does not claim live alerting. |
| F-3 | Low | Actuator endpoints are unauthenticated inside the local boundary. | Accepted for the local PoC; public exposure requires authentication or network policy, out of scope. |
| F-4 | Low | Readiness is `UP` because no external dependency exists. | Accepted with a recorded assumption; extend readiness when a dependency is added. |
| F-5 | Info | The access log increases stdout volume and logs path segments. | Accepted for the local PoC; query strings/payloads are excluded and no sensitive path exists. |
| F-6 | Info | No load/performance test was run. | Accepted; no throughput requirement exists. Reassess if traffic or processing complexity increases. |
| F-7 | High, fixed | `/actuator/prometheus` initially returned `404` because Prometheus export was not enabled. | Fixed with `management.prometheus.metrics.export.enabled=true`; covered by `prometheusEndpointExposesJvmMetrics`. |
| F-8 | Info, resolved | PR CI had not run when the local review was recorded. | Resolved: run `42239560605` at `fc69901` passed build/test, secret scan, Trivy scan and image identity. |

## Verdict

Approved for the local PoC scope on the recorded evidence. Every requirement and acceptance
criterion has a named check; the new security-relevant exposure and access-log behavior are
tested; all findings have a disposition. Independent approval is not claimed.

## Reviewer, independence and date

- Reviewer: OpenCode review assistant. It produced the change, so this is a self-review.
- Independence limitation: the repository is a single-maintainer repository and no independent
  human reviewer is recorded. This record does not satisfy the independence clause of the phase
  07 quality gate and does not claim independent approval.
- Date: 2026-11-06.
