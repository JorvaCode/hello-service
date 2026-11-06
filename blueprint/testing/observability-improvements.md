# Testing: `REQ-OBS-001` and `REQ-OBS-002` — Observability signals and alert definitions

## Scope and environment

- Requirements: `blueprint/requirements/observability-improvements.md` and
  `blueprint/requirements/observability-alerting.md`.
- Design: `blueprint/design/observability-improvements.md`.
- Implementation: `RequestAccessLogFilter`, `application.properties`, the Actuator/Micrometer
  dependencies, `ops/prometheus/hello-service-alerts.yml` and their tests.
- Environment: Windows, PowerShell 5.1, Java 21, Spring Boot 3.5.14 with embedded Tomcat,
  Gradle wrapper 8.14.2, offline dependency cache, no daemon.
- Date: 2026-11-06.
- Commit basis: `3753313` (`fix(observability): update micrometer for security advisories`),
  following `1b1db72`, `3eab4a8`, `101ea28` and `90d0227`.
- Working tree: the code/test tree of `3753313` was tested; the Blueprint documents are
  documentation-only follow-up changes.

## Test suite

| Test class | Tests | Purpose |
| --- | ---: | --- |
| `HelloEndpointTest` | 6 | Pre-existing `/api/hello` regression coverage, unchanged |
| `HelloHandlerTest` | 3 | Pre-existing handler coverage, unchanged |
| `StatusEndpointTest` | 6 | Pre-existing `/api/status` regression coverage, unchanged |
| `StatusHandlerTest` | 2 | Pre-existing handler coverage, unchanged |
| `ActuatorEndpointTest` | 5 | AC-01–AC-04: probes, metrics, exposure allowlist and hidden details |
| `RequestAccessLogFilterTest` | 5 | AC-05–AC-07: fields, correlation, query exclusion, missing-id counter and exception path |
| `AlertRulesTest` | 1 | `REQ-OBS-002` AC-01/AC-02: rule inventory and required fields |
| **Total** | **28** | |

## Test run result

- Commit basis: `3753313` (code/test tree); the run used the local checkout before the
  documentation-only changes.
- Environment: Windows, PowerShell 5.1, Java 21, Spring Boot 3.5.14 with embedded Tomcat,
  Gradle wrapper 8.14.2, offline dependency cache, no daemon.
- Date: 2026-11-06; test lifecycle completed at `15:59:36 +01:00`.
- Command: `.\gradlew.bat clean test --offline --no-daemon --console=plain`
- Result: `BUILD SUCCESSFUL` in 10 s.
- Counts: 28 tests, 0 failures, 0 errors, 0 skipped.
- Evidence: `build/test-results/test/*.xml`; the seven suite files record 28 tests with 0
  failures, 0 errors and 0 skipped.

## Executed jar verification

On 2026-11-06 the packaged jar was built with `.\gradlew.bat bootJar --offline --no-daemon
--console=plain` (`BUILD SUCCESSFUL`, `build/libs/hello-service-0.1.0.jar`, 26,201,884 bytes)
and run on `127.0.0.1:18081` with `java -jar build\libs\hello-service-0.1.0.jar
--server.port=18081`.

| Check | Result |
| --- | --- |
| `GET /actuator/health/liveness` | `200`, body `{"status":"UP"}` |
| `GET /actuator/health/readiness` | `200`, body `{"status":"UP"}` |
| `GET /api/hello` with `X-Request-ID: d-jar-evidence` | `200`, `Hello, World!`; response header and access-log line both carry `d-jar-evidence` |
| `GET /actuator/prometheus` | `200`, 16,178 bytes; contains `jvm_memory_used_bytes` and `application="hello-service"` |
| Missing-correlation counter | `hello_service_access_log_missing_request_id_total{application="hello-service"} 0.0`; the HTTP path always resolves an id, so the non-zero case is covered by `countsEntriesWithoutRequestId` |
| Access log | Four `access method=... path=... status=... duration_ms=... requestId=...` lines for the probes, hello and metrics requests; query strings absent |

## Criterion coverage

| Criterion | Automated verification | Result |
| --- | --- | --- |
| `REQ-OBS-001` AC-01 | `ActuatorEndpointTest.livenessProbeReportsUp`, `readinessProbeReportsUp` | Pass |
| `REQ-OBS-001` AC-02 | `ActuatorEndpointTest.prometheusEndpointExposesJvmMetrics` | Pass |
| `REQ-OBS-001` AC-03 | `ActuatorEndpointTest.unapprovedActuatorEndpointIsNotExposed`; allowlist review | Pass |
| `REQ-OBS-001` AC-04 | `ActuatorEndpointTest.healthEndpointDoesNotDiscloseDetails`; `show-details=never` review | Pass |
| `REQ-OBS-001` AC-05 | `RequestAccessLogFilterTest.logsMethodPathStatusDurationAndRequestId`, `logsServerErrorStatus` | Pass |
| `REQ-OBS-001` AC-06 | `RequestAccessLogFilterTest.omitsQueryStringFromPath` | Pass |
| `REQ-OBS-001` AC-07 | `RequestAccessLogFilterTest.logsStatus500AndRethrowsWhenChainFails` | Pass |
| `REQ-OBS-001` AC-08 | The 17 pre-existing tests pass unchanged; full suite green | Pass |
| `REQ-OBS-001` AC-09 | Offline suite and committed lockfile; CI Trivy scan is recorded by the PR run | Pass locally; CI result recorded in the PR record below |
| `REQ-OBS-002` AC-01 | `AlertRulesTest.rulesCoverFailureModesAndNameResponse` | Pass |
| `REQ-OBS-002` AC-02 | Eight rule names asserted by `AlertRulesTest`; mapping reviewed in the observability record | Pass |
| `REQ-OBS-002` AC-03 | `RequestAccessLogFilterTest.countsEntriesWithoutRequestId` | Pass |
| `REQ-OBS-002` AC-04 | Every `runbook_url` targets `blueprint/observability/hello-service.md#rb-`; anchors reviewed | Pass by review |
| `REQ-OBS-002` AC-05 | `ops/prometheus/README.md` and the observability record state that rules are definitions only | Pass by review |

## Scenario coverage

| Scenario | Response measure | Verifying check |
| --- | --- | --- |
| QAS-01 | Liveness and readiness `200` with `"status":"UP"` | AC-01 tests |
| QAS-02 | `200`, `jvm_memory_used_bytes`, `application="hello-service"` | AC-02 test |
| QAS-03 | One line with method/path/status/duration/requestId; query absent | AC-05/AC-06 tests |
| QAS-04 | `/actuator/env` is `404`; health has no `components` | AC-03/AC-04 tests and configuration review |
| QAS-05 | All pre-existing contracts pass unchanged | 17 pre-existing tests |
| QAS-06 | Lockfile committed and dependencies scanned | Local lockfile diff; PR Trivy run |
| QAS-07 | Eight alert rules parse and name a severity and runbook | `AlertRulesTest`; `ops/prometheus/README.md` review |

## Non-functional evidence

| Requirement | Evidence | Result |
| --- | --- | --- |
| NFR-01 | Actuator tests for allowlist and hidden details; access-log query test; no payload logging | Pass |
| NFR-02 | Filter uses `System.nanoTime()` and SLF4J only; no network or persistence | Pass by inspection |
| NFR-03 | Existing tests pass unchanged; Dockerfile and handlers unchanged | Pass |
| NFR-04 | Standard Actuator health groups and Micrometer Prometheus registry; ADR-014 | Pass |
| NFR-05 | 28-test offline run using JUnit 5, `@SpringBootTest`, mock servlet objects and a YAML parser | Pass |
| NFR-06 | Actuator/Micrometer/Prometheus versions locked in `gradle.lockfile`; CI scan | Pass locally; CI pending at local record |
| NFR-07 | Liveness/readiness tests; observability record names criteria | Pass |
| `REQ-OBS-002` NFR-03 | `AlertRulesTest` parses `ops/prometheus/hello-service-alerts.yml` with SnakeYAML | Pass |

## Defect record

| ID | Finding | Criterion affected | Severity | Owner | Disposition |
| --- | --- | --- | --- | --- | --- |
| F-01 | `/actuator/prometheus` returned `404` with only the exposure allowlist and probe settings. The condition report showed `management.defaults.metrics.export.enabled` evaluated false, so the Prometheus auto-configuration did not match and no `prometheusMeterRegistry` was created. | AC-02, FR-02 | High — the metrics signal was absent | Maintainer | **Fixed:** added `management.prometheus.metrics.export.enabled=true` to `application.properties`; the endpoint test now passes with JVM metrics and the application tag. |
| F-02 | No independent reviewer or independent test reviewer exists for this change. | Process limitation | Info | Maintainer | Recorded; the self-review is in `blueprint/reviews/observability-improvements.md` and does not claim independent approval. |
| F-03 | No load, performance, alert-evaluation or metrics-backend test exists. The rule definitions are parsed and inspected, but no Prometheus instance evaluates them. | NFR-02, `REQ-OBS-002` NFR-04 | Info — deployment out of scope | Maintainer | Accepted for the local PoC; rules are configuration evidence and no throughput or monitoring stack exists. |

No open defect remains for the tested behaviour.

## Gate assessment

- Test execution and criterion coverage pass: 28 tests, 0 failures, every AC has automated
  verification or a named review check.
- Scenario coverage is recorded for QAS-01–QAS-07.
- The local test run is commit-bound to the code/test tree of `3753313`; later commits in this
  change set are documentation-only.
- F-01 is fixed and covered by `ActuatorEndpointTest.prometheusEndpointExposesJvmMetrics`.
- Checked against `blueprint/06-testing.md`: the suite, run result (environment, commit,
  command), criterion coverage, scenario coverage and defect record are present.
- Phase 06 is **CLOSED for the local scope**. PR CI is recorded below when it completes.

## PR CI record

The branch is `feat/observability-improvements`. The PR opens against `main`; the CI run is
recorded after the push. The workflow runs the same `clean test` plus the existing secret and
Trivy dependency scans. This section is updated with the run id and conclusion after CI.

## Traceability

- Requirements: `REQ-OBS-001` and `REQ-OBS-002`.
- Design: `blueprint/design/observability-improvements.md`.
- Alert rules: `ops/prometheus/hello-service-alerts.yml` and `ops/prometheus/README.md`.
- Architecture/ADR: `blueprint/architecture/observability-improvements.md`, ADR-014.
- Review: `blueprint/reviews/observability-improvements.md`.
- Change: `blueprint/changes/observability-improvements.md`.
- Observability record: `blueprint/observability/hello-service.md`.
