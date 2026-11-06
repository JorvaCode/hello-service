# Change: `CHG-OBS-002` — Operational observability signals and alert definitions

## Requirement

Implements `REQ-OBS-001` and `REQ-OBS-002`, and closes the improvement loops `IMP-001`
(metrics), `IMP-003` (readiness), `IMP-004` (alert definitions) and `IMP-005` (access log). The
change is additive: it does not modify `REQ-HELLO-001`, `REQ-STATUS-001` or the existing
deployment topology.

## Design

- Spring Boot Actuator exposes liveness/readiness health groups and the endpoint model.
- Micrometer's Prometheus registry exposes `/actuator/prometheus`.
- `RequestAccessLogFilter` emits one correlated access line per request after the filter chain.
- `application.properties` restricts the Actuator surface to `health`, `info` and `prometheus`,
  enables probes, hides health details, sets the `application=hello-service` tag and enables
  Prometheus export.
- `gradle.lockfile` locks the new dependencies.
- `ops/prometheus/hello-service-alerts.yml` defines eight alert rules mapped to `FM-01`...`FM-07`
  plus the `HELLO-001` contract;
  `ops/prometheus/README.md` documents the application scrape and blackbox probe jobs. The
  rules are configuration evidence, not a running Prometheus/Alertmanager deployment.

### Patterns

No application pattern is applied. The access log is a standard Servlet `Filter` for a
cross-cutting concern. No service, repository, factory, strategy or DTO is introduced.

### ADR

`ADR-014` (`blueprint/architecture/adr/adr-014-observability-stack.md`) is **Accepted**. The
decision introduces runtime dependencies, changes the operational/security quality attributes
and affects consumers, so it meets `standards/terms.md` criteria (c), (d) and (e).

## Implementation

- `build.gradle.kts`: add `spring-boot-starter-actuator` and
  `micrometer-registry-prometheus`; pin `micrometer.version=1.15.12` because CI Trivy rejected
  the Boot-managed 1.15.11 for `CVE-2026-40983` and `CVE-2026-40984`.
- `gradle.lockfile`: lock the resolved Actuator, Micrometer, Prometheus client and transitive
  dependencies at Micrometer 1.15.12.
- `src/main/resources/application.properties`: new Actuator configuration.
- `src/main/java/com/example/hello/RequestAccessLogFilter.java`: new filter at
  `Ordered.HIGHEST_PRECEDENCE + 10`, after `RequestIdFilter`.
- `src/test/java/com/example/hello/ActuatorEndpointTest.java`: 5 integration tests for probes,
  metrics, exposure allowlist and hidden details.
- `src/test/java/com/example/hello/RequestAccessLogFilterTest.java`: 5 unit tests for fields,
  correlation, query omission, missing-request-id counting, server-error status and exception
  rethrow.
- `src/test/java/com/example/hello/AlertRulesTest.java`: loads the rule YAML and asserts the
  eight-rule inventory and required fields.
- `RequestAccessLogFilter` increments `hello_service_access_log_missing_request_id_total` when
  a line has no request id, giving `FM-07` a metric signal.
- `blueprint/requirements/observability-improvements.md`: new requirement for the signals.
- `blueprint/requirements/observability-alerting.md`: new requirement for `IMP-004`.
- `blueprint/analisys/observability-improvements.md`: analysis.
- `blueprint/architecture/observability-improvements.md` and ADR-014: architecture decision.
- `blueprint/design/observability-improvements.md`: design.
- `blueprint/testing/observability-improvements.md`: test evidence.
- `blueprint/reviews/observability-improvements.md`: self-review.
- `blueprint/observability/hello-service.md`: updated instrumentation, failure detection and
  gate record.
- `blueprint/improvements/continuous-improvements.md`: close `IMP-001`, `IMP-003`, `IMP-004`
  and `IMP-005`.
- `blueprint/gates.md`: update P-13 and related evidence.
- `README.md`: update observability, improvement and limitation sections.
- `blueprint/13-observability.md`: update the project record.
- `blueprint/changes/p13-observability.md`: add a post-closure addendum.
- This change record.

## Tests

- Code/test commits: `90d0227` (initial implementation), `101ea28` (exposure/exception tests),
  `3eab4a8` (alert-rule definitions and validation), `1b1db72` (missing-correlation signal) and
  `3753313` (Micrometer 1.15.12 security pin).
- Local command: `.\gradlew.bat clean test --offline --no-daemon --console=plain`.
- Local result at `3753313`: `BUILD SUCCESSFUL`; 28 tests, 0 failures, 0 errors, 0 skipped.
- New tests: `ActuatorEndpointTest` 5; `RequestAccessLogFilterTest` 5; `AlertRulesTest` 1.
  Existing 17 tests pass unchanged.
- Executed jar on 2026-11-06: bootJar built, `127.0.0.1:18081` probes returned `200
  {"status":"UP"}`, `/actuator/prometheus` returned `200` with JVM metrics, the
  `application="hello-service"` tag and the missing-correlation counter, and `/api/hello` with
  `X-Request-ID: d-jar-evidence` produced a correlated access-log line.
- PR CI run `42239560605` at `fc69901` passed the same suite plus the secret scan, Trivy
  dependency scan and image identity steps; run `42238330408` had failed on Micrometer CVEs,
  fixed by `3753313` (`micrometer.version=1.15.12`).

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| Liveness and readiness | Integration | `ActuatorEndpointTest.livenessProbeReportsUp`, `readinessProbeReportsUp` |
| Prometheus metrics | Integration | `prometheusEndpointExposesJvmMetrics` |
| Restricted Actuator exposure | Integration | `unapprovedActuatorEndpointIsNotExposed` |
| Hidden health details | Integration | `healthEndpointDoesNotDiscloseDetails` |
| Access-log fields and correlation | Unit | `logsMethodPathStatusDurationAndRequestId`, `logsServerErrorStatus` |
| Query omission | Unit | `omitsQueryStringFromPath` |
| Missing-correlation counter | Unit | `countsEntriesWithoutRequestId` |
| Exception path | Unit | `logsStatus500AndRethrowsWhenChainFails` |
| Alert inventory and fields | Unit | `AlertRulesTest.rulesCoverFailureModesAndNameResponse` |
| Existing contracts | Build | 17 pre-existing tests unchanged |
| Reproducible dependencies | Build/CI | `gradle.lockfile` and Trivy scan |

## Security

- The Actuator allowlist is `health,info,prometheus`; `/actuator/env` is `404`.
- `management.endpoint.health.show-details=never` hides component details.
- The access log excludes query strings and payloads; it logs method, path, status, duration
  and `requestId` only.
- No secret or credential is read or emitted; the alert rules contain no target credentials.
- New dependencies are pinned by the Spring Boot BOM and lockfile and scanned by Trivy.
- Residual risks: Actuator endpoints are unauthenticated inside the local boundary;
  JVM/process metrics reveal resource information; `X-Request-ID` remains a log-injection
  vector already accepted in `blueprint/reviews/api-status-endpoint.md`.

## Deployment

None beyond the existing artifact. The JAR, image, port `8080`, `Dockerfile`, CI and CD
workflows are unchanged. No container `HEALTHCHECK`, orchestrator probe or public ingress is
added. The configuration ships in the JAR's `application.properties`; no secret or environment
variable is required.

The alert rules are not deployed: no Prometheus, Alertmanager, blackbox exporter or paging
service is part of this change. An operator must load `ops/prometheus/hello-service-alerts.yml`,
configure the jobs documented in `ops/prometheus/README.md` and own routing/tuning.

## Rollback

Revert the code/test commits and the documentation commit. Restore the previous
`gradle.lockfile` and remove `application.properties`, `ops/prometheus/` and the alert-rule test
if no other configuration remains. The existing `0.1.0` artifact and remote digest are unchanged;
no data migration or external state is involved.

## Deletions

None. No test or artifact is removed.

## Post-closure notes

- The observability record now names the implemented signals, the missing-correlation counter and
  the eight alert definitions.
  The phase 13 gate remains **CLOSED with the limitation that rules are not deployed or
  evaluated**: no Prometheus, Alertmanager, blackbox exporter, dashboard or paging path is
  operated. `IMP-004` is closed at the definition level, not at the live-alerting level.
- The local test evidence is commit-bound to `3753313`; this change record and the other
  Blueprint documents are documentation-only follow-up commits.
- PR CI run `42239560605` at `fc69901` is recorded in
  `blueprint/testing/observability-improvements.md` and `blueprint/reviews/observability-improvements.md`.
