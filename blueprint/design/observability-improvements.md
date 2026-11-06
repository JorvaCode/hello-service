# Design: `REQ-OBS-001`/`REQ-OBS-002` — Operational observability signals and alert definitions

## Status

Implementation-ready design. Implemented at commit `3753313` (`feat/observability-improvements`);
this document and the other Blueprint records are the documentation follow-up. The design
translates `blueprint/architecture/observability-improvements.md` and ADR-014 into code and
tests. Every element names the requirement it satisfies.

## Requirement

- `REQ-OBS-001` — `blueprint/requirements/observability-improvements.md`.
- `REQ-OBS-002` — `blueprint/requirements/observability-alerting.md`.
- Functional: FR-01–FR-08. Non-functional: NFR-01–NFR-07. Acceptance: AC-01–AC-09.
- The change alters **structure** (one new filter and configuration), **interface** (new
  `/actuator` paths and access-log lines) and **dependencies** (Actuator, Micrometer,
  Prometheus client). It does not alter `/api/hello`, `/api/status` or the deployment topology.

## Design obligations

The implementation shall:

- add the two runtime dependencies and update the lockfile;
- add exactly one production class, `RequestAccessLogFilter`, and one configuration file,
  `application.properties`;
- expose only `health`, `info` and `prometheus`, enable the liveness/readiness groups, hide
  health details and tag metrics with `application=hello-service`;
- emit the access line after the filter chain and rethrow any exception;
- add the Actuator integration test and the access-log unit test in the same change as the
  code;
- leave `HelloHandler`, `StatusHandler`, `RequestIdFilter`, `HelloApplication`,
  `logback-spring.xml` and the Dockerfile unchanged.

These obligations derive from FR-01–FR-08, NFR-01–NFR-07 and ADR-014.

## Components and files

| File | Kind | Change | Basis |
| --- | --- | --- | --- |
| `build.gradle.kts` | Build | Add Actuator and Micrometer Prometheus registry | FR-01–FR-03, NFR-06 |
| `gradle.lockfile` | Build | Lock the new resolved dependency set | NFR-06 |
| `src/main/resources/application.properties` | Configuration | New Actuator configuration | FR-01–FR-04, NFR-01 |
| `src/main/java/com/example/hello/RequestAccessLogFilter.java` | Filter | New access-log component | FR-05–FR-07 |
| `src/test/java/com/example/hello/ActuatorEndpointTest.java` | Integration test | New probe/metrics/exposure tests | AC-01–AC-04 |
| `src/test/java/com/example/hello/RequestAccessLogFilterTest.java` | Unit test | New access-log/query/exception tests | AC-05–AC-07 |
| `HelloHandler`, `StatusHandler`, `RequestIdFilter`, `HelloApplication` | Application | Unchanged | FR-08, NFR-03 |
| `logback-spring.xml`, `Dockerfile`, workflows | Operations | Unchanged | NFR-02, NFR-03 |

No service layer, repository, custom metrics exporter, DTO or new deployable component is
added.

## Configuration

`src/main/resources/application.properties`:

```properties
management.endpoints.web.exposure.include=health,info,prometheus
management.prometheus.metrics.export.enabled=true
management.endpoint.health.probes.enabled=true
management.endpoint.health.show-details=never
management.metrics.tags.application=hello-service
```

| Property | Purpose | Requirement |
| --- | --- | --- |
| `management.endpoints.web.exposure.include` | Allowlist only the operational endpoints | FR-03, NFR-01 |
| `management.prometheus.metrics.export.enabled` | Enable the Prometheus endpoint and registry export | FR-02 |
| `management.endpoint.health.probes.enabled` | Create the liveness and readiness groups | FR-01 |
| `management.endpoint.health.show-details` | Do not disclose component details | FR-04, NFR-01 |
| `management.metrics.tags.application` | Common metric tag for this service | FR-02 |

`management.prometheus.metrics.export.enabled=true` is explicit because with the current
Spring Boot configuration the default was evaluated as false and the endpoint returned `404`
without it. The property is not redundant; it is the fix recorded as defect F-01 in
`blueprint/testing/observability-improvements.md`.

## Interface and contracts

### Actuator endpoints

| Method and path | Success contract | Failure / absence | Basis |
| --- | --- | --- | --- |
| `GET /actuator/health/liveness` | `200`, body contains `"status":"UP"` while serving HTTP | Non-`200` if liveness is down | FR-01, AC-01 |
| `GET /actuator/health/readiness` | `200`, body contains `"status":"UP"` when context is ready | Non-`200` if readiness is down; no external dependency today | FR-01, AC-01, NFR-07 |
| `GET /actuator/health` | `200`, status only; no `components` | Not applicable | FR-04, AC-04 |
| `GET /actuator/prometheus` | `200`, Prometheus text with JVM metrics and `application="hello-service"` | `404` if the registry is disabled or not exposed | FR-02, AC-02 |
| `GET /actuator/env` | Not exposed | `404` | FR-03, AC-03 |

Actuator `info` is exposed by the allowlist but has no custom contributor; this is accepted
because the requirement only needs the allowlist and no sensitive info contributor is added.

### Access log

The filter emits one INFO line:

```text
access method=<METHOD> path=<getRequestURI()> status=<status> duration_ms=<ms> requestId=<id|->
```

- `requestId` comes from `MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY)`.
- The query string, headers, request body and response body are not read or logged.
- The status is captured after the chain; on an exception it is recorded as `500` and the
  original exception is rethrown.
- The line is emitted in `finally`, so it is produced on success and failure.
- `duration_ms` is measured with `System.nanoTime()`.

## Data model

No persistent data model is added. Metrics are in-memory Micrometer meters; access-log records
are stdout text; health state is process state. There is no schema, migration, cache or shared
mutable state.

## Error handling and validation

- **Unsupported Actuator path:** Spring Boot's endpoint exposure returns `404`; no custom
  error body is added (FR-03).
- **Actuator health down:** Spring Boot returns its standard health status; the tests cover the
  `UP` path only because the service has no dependency to make it `DOWN`.
- **Filter chain exception:** the filter sets the local status variable to `500`, logs it and
  rethrows the original `IOException`, `ServletException` or `RuntimeException`; it does not
  convert or swallow it (FR-07).
- **Response status:** the value is read from `HttpServletResponse.getStatus()` after the
  chain; no response body or header is changed by the filter.
- **Validation:** none. The filter does not parse input; the Actuator endpoints are configured
  declaratively.
- **Correlation missing:** when the MDC has no value, `requestId=-` is logged; this is the
  documented fallback, not an error.

## Transactional boundaries

None. There is no write, persistence or external call on the request path. No `@Transactional`
annotation is added.

## Patterns

**No application pattern is applied.** The only new code is a standard Servlet `Filter` used
for a cross-cutting concern, not an abstraction introduced to solve a recurring design problem.

- **Problem:** every request needs one correlated access line, including requests that never
  reach a handler.
- **Evidence:** `404`, `405` and filter failures exist and would be missed by handler-level
  logging; the existing MDC is available only in the filter chain.
- **Cost of the simpler alternative:** handler-level or interceptor logging would duplicate
  code and miss pre-handler outcomes; a Tomcat access valve would not carry the application
  MDC. The filter is the simpler correct mechanism.
- **Scope:** one filter, one concern, no reusable framework.

No service/repository/strategy/factory/DTO is added. Per `standards/code-design.md`, the
absence of an application pattern is a decision, not a gap.

## ADR

| Option | Assessment |
| --- | --- |
| New ADR-014 | **Chosen.** The decision introduces runtime dependencies, changes the operational/security quality attributes and affects consumers. |
| Update an existing ADR | Not applicable; no existing ADR covers the observability stack. ADR-009 is about information items, not this stack. |
| Design document only | Insufficient; the significance criteria (c), (d) and (e) are met. |

ADR-014 is `Accepted`. It records the Actuator/Micrometer/access-log choice, the restricted
exposure, the eight versioned alert rules and the negative consequences (dependencies,
unauthenticated surface, log volume, readiness caveat, no deployed evaluation or paging).

## Configuration and operational needs

- The same JAR, image, port and workflow are used; no new secret, volume, port or network rule.
- The `/actuator` paths share port `8080`; a future orchestrator must target the health groups.
- The Prometheus endpoint is pull-based; no Pushgateway or metrics backend is configured.
- Access logs use the existing stdout sink; no log shipper or retention policy is added.
- Alert rules live in `ops/prometheus/hello-service-alerts.yml`; the scrape and blackbox job
  definitions live in `ops/prometheus/README.md`. No Prometheus, Alertmanager or blackbox
  exporter is deployed or configured by this change.
- A future deployment must not expose the Actuator endpoints publicly without authentication
  or network policy.
- Readiness must be extended when an external dependency is added; otherwise it can report a
  false `UP`.

## Test strategy

The levels are derived from the architecture's quality scenarios:

| Scenario / requirement | Level | Why this level |
| --- | --- | --- |
| QAS-01, FR-01, FR-04, FR-03 | Integration (`@SpringBootTest` + `TestRestTemplate`, random port) | Actuator auto-configuration and endpoint exposure are context-level behavior; a web slice would not exercise the real auto-configuration. |
| QAS-02, FR-02 | Integration | The Prometheus registry and endpoint are auto-configured at context level. |
| QAS-03, FR-05–FR-07 | Unit (`RequestAccessLogFilterTest` with Logback `ListAppender` and mock servlet request/response) | The filter's fields, MDC lookup, query exclusion and exception path are local behavior and do not need a server. |
| QAS-05, FR-08 | Existing unit/slice suite | The pre-existing contracts are already covered; the new tests are additive. |
| QAS-06, NFR-06 | Build/CI | Reproducible lockfile and vulnerability scan are build-level checks. |
| QAS-07, `REQ-OBS-002` AC-01–AC-03 | Unit (YAML parse + assertions) | Alert files are static configuration; parsing them catches malformed YAML, missing rules and missing required fields without a Prometheus instance. |

No end-to-end, performance or chaos test is added: there is no external dependency, no
throughput requirement and no alerting backend to evaluate. This is a deliberate level choice,
not a missing test.

## Tests

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| AC-01 | Integration | `ActuatorEndpointTest.livenessProbeReportsUp`, `readinessProbeReportsUp` |
| AC-02 | Integration | `ActuatorEndpointTest.prometheusEndpointExposesJvmMetrics` |
| AC-03 | Integration + review | `ActuatorEndpointTest.unapprovedActuatorEndpointIsNotExposed`; allowlist review |
| AC-04 | Integration + review | `ActuatorEndpointTest.healthEndpointDoesNotDiscloseDetails`; `show-details=never` review |
| AC-05 | Unit | `RequestAccessLogFilterTest.logsMethodPathStatusDurationAndRequestId`, `logsServerErrorStatus` |
| AC-06 | Unit | `RequestAccessLogFilterTest.omitsQueryStringFromPath` |
| AC-07 | Unit | `RequestAccessLogFilterTest.logsStatus500AndRethrowsWhenChainFails` |
| AC-08 | Build | The 17 pre-existing tests pass unchanged; full suite green |
| AC-09 | Build/CI | Offline suite; `gradle.lockfile` diff; Trivy CI step |
| `REQ-OBS-002` AC-01 | Unit | `AlertRulesTest.rulesCoverFailureModesAndNameResponse` |
| `REQ-OBS-002` AC-02 | Unit + review | Eight rules map to `FM-01`–`FM-07`; `HELLO-001` contract rule is additional |
| `REQ-OBS-002` AC-03 | Unit | `RequestAccessLogFilterTest.countsEntriesWithoutRequestId` |
| `REQ-OBS-002` AC-04 | Review | Every `runbook_url` points to `blueprint/observability/hello-service.md#rb-` |
| `REQ-OBS-002` AC-05 | Review | `ops/prometheus/README.md` states the rules are definitions only |

No mocks of the Actuator infrastructure are used; the integration test starts the application
on a random port. The access-log tests use mock servlet objects and a real Logback appender.
Tests are deterministic and require no external service.

## Risks carried

- **Unauthenticated Actuator surface.** The endpoints are inside the local boundary; public
  exposure needs authentication/network policy.
- **Information disclosure through metrics.** JVM/process metrics and `info` are visible to
  anyone who can reach the port; the allowlist and hidden details reduce but do not remove the
  risk.
- **Log volume and path sensitivity.** One line per request; query strings are excluded but a
  sensitive path segment would be logged. No such route exists today.
- **Readiness semantics.** `UP` because no external dependency exists; it becomes misleading
  when one is added.
- **Filter ordering.** The access log relies on `RequestIdFilter`; a future order change could
  produce `requestId=-`.
- **Dependency CVEs.** New runtime libraries are pinned and scanned; accepted findings would
  need a `.trivyignore` entry with rationale.
- **No deployed alerting.** Eight rules are versioned, but no Prometheus, Alertmanager or paging
  path evaluates or routes them; a malformed deployed configuration or a missing scrape target
  would only be visible in the rule file and README.

## Security

The change adds no secret and logs no request/response body or query string. The Actuator
allowlist is the minimum for the three signals; health details are hidden. The endpoints are
unauthenticated inside the local deployment boundary, which is recorded as a limitation, not a
claim of production security. The existing `X-Request-ID` log-injection residual risk remains
accepted. The dependency scan is the control for the new libraries.

## Deployment

None beyond the existing artifact. The JAR, image, port `8080`, `Dockerfile` and workflows are
unchanged. No container `HEALTHCHECK`, Kubernetes probe, CD smoke test or ingress is added.
Configuration ships in the JAR's `application.properties`; no environment variable or secret is
required.

## Rollback

Revert the commits that add the dependencies, configuration, filter and tests. Restore the
previous `gradle.lockfile`; remove `application.properties` if no other configuration remains.
The previous local release and remote digest are unchanged because no new image is built by
this documentation change. A rollback rehearsal is a local `git revert` plus a green suite;
there is no data migration or external state to restore.

## Deletions

None. No test or artifact is deleted.

## Traceability

| Design element | Requirement or criterion | Verifying test or check |
| --- | --- | --- |
| Actuator dependencies and auto-configuration | FR-01, FR-02, NFR-04, NFR-06 | Actuator tests; lockfile; Trivy |
| `/actuator/health/liveness` and `readiness` | FR-01, NFR-07, AC-01 | `livenessProbeReportsUp`, `readinessProbeReportsUp` |
| `/actuator/prometheus` and application tag | FR-02, AC-02 | `prometheusEndpointExposesJvmMetrics` |
| Exposure allowlist | FR-03, NFR-01, AC-03 | `unapprovedActuatorEndpointIsNotExposed`; configuration review |
| Hidden health details | FR-04, NFR-01, AC-04 | `healthEndpointDoesNotDiscloseDetails`; property review |
| `RequestAccessLogFilter` fields and MDC | FR-05, AC-05 | `logsMethodPathStatusDurationAndRequestId` |
| Query-string exclusion | FR-06, NFR-01, AC-06 | `omitsQueryStringFromPath` |
| Exception path status `500` and rethrow | FR-07, AC-07 | `logsStatus500AndRethrowsWhenChainFails` |
| Missing-correlation counter | `REQ-OBS-002` FR-03, AC-03 | `countsEntriesWithoutRequestId` |
| Eight alert rules and required fields | `REQ-OBS-002` FR-01/FR-02, AC-01/AC-02 | `AlertRulesTest` |
| Runbook links | `REQ-OBS-002` FR-04, AC-04 | Review of `ops/prometheus/hello-service-alerts.yml` |
| No deployed evaluation/paging | `REQ-OBS-002` FR-05, AC-05 | `ops/prometheus/README.md` review |
| Existing endpoints unchanged | FR-08, NFR-03, AC-08 | Pre-existing tests; `HelloHandler`/`StatusHandler` unchanged |
| Offline suite and lockfile | NFR-05, NFR-06, AC-09 | `clean test --offline`; `gradle.lockfile` |
| Single view and ADR-014 | `standards/terms.md` | Architecture description and ADR-014 |

## Review and approval

Implemented and self-reviewed on 2026-11-06. The review is a solo-maintainer self-review in
`blueprint/reviews/observability-improvements.md`; independent approval is unavailable. The
design is accepted for the local PoC scope, not for public exposure or production alert
evaluation/paging.
