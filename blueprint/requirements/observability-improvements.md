# Requirement: `REQ-OBS-001` — Operational observability signals

## Problem

The phase 13 observability record is honest about a gap: the service has structured logs
with `requestId` correlation and `/api/status` as a liveness signal, but it has no metrics
endpoint, no readiness probe and no per-request access log. Detection of the recorded
failure modes is therefore manual. An operator cannot scrape the service for JVM or HTTP
server metrics, an orchestrator has no readiness signal, and a successful request leaves no
application log line to correlate with a failure.

The need is operational, not a change to the greeting or status contracts. The service must
emit standard, testable signals that an operator or a future orchestrator can consume
without inventing a custom protocol.

## Objective

Add the smallest standard observability surface that closes `IMP-001`, `IMP-003` and
`IMP-005` without changing the existing public API:

- expose liveness and readiness probes and a Prometheus metrics endpoint through Spring Boot
  Actuator;
- emit one structured access-log line per request, correlated by the existing `requestId`;
- keep `/api/hello` and `/api/status` unchanged.

The outcome is measurable: automated tests assert both probe bodies, the Prometheus endpoint,
the restricted Actuator exposure, the access-log fields, query-string exclusion and the
exception path, and the full suite stays green. `IMP-004` is specified separately by
`REQ-OBS-002` so alert definitions can be reviewed without claiming a running monitoring stack.

## Scope

### In scope

- Add `spring-boot-starter-actuator` and `micrometer-registry-prometheus` as runtime
  dependencies, pinned by the Spring Boot BOM and the committed `gradle.lockfile`.
- Expose only `health`, `info` and `prometheus` under `/actuator`.
- Expose `/actuator/health/liveness` and `/actuator/health/readiness` with HTTP `200` and a
  body containing `"status":"UP"` while the application context is serving HTTP.
- Expose `/actuator/prometheus` with Prometheus text exposition containing JVM metrics and
  the common tag `application="hello-service"`.
- Do not disclose health component details: `GET /actuator/health` is `200` with status only.
- Reject Actuator endpoints outside the allowlist, including `/actuator/env`, with `404`.
- Add `RequestAccessLogFilter`, ordered after `RequestIdFilter`, that logs exactly one line per
  request with method, request URI path, status, duration in milliseconds and `requestId`.
- Use `-` when no `requestId` is present; log `status=500` and rethrow when the chain throws.
- Keep the query string and request/response bodies out of the access log.
- Add automated tests for the probes, metrics, restricted exposure, access-log fields,
  correlation, query omission and exception path.
- Update the observability record, improvement register, gates, review and change record.

### Out of scope

- Deploying or evaluating rules, paging, dashboards, SLOs, error budgets or a metrics backend.
  Alert definitions are specified by `REQ-OBS-002`; this requirement does not operate them.
- Distributed tracing, OpenTelemetry, trace collectors or span propagation.
- A Prometheus server, Alertmanager, blackbox exporter, Grafana, log shipping, log retention or
  a monitoring stack.
- Container `HEALTHCHECK`, Kubernetes probes, CD smoke-test changes or ingress configuration.
- Authentication, authorization, TLS termination or network policy for Actuator endpoints.
- Custom business metrics, HTTP client metrics or per-endpoint metric tags beyond the
  framework defaults.
- Changing `/api/hello`, `/api/status`, `REQ-HELLO-001` or `REQ-STATUS-001`.
- Logging request or response payloads, the query string, headers or user identity.
- Replacing `/api/status` with Actuator. `/api/status` remains the application-level liveness
  contract and is unchanged.
- Performance or load testing. The local PoC has no throughput requirement; the change adds
  bounded, in-process work only.

## Functional requirements

Each states one observable behaviour and is decidable by a test or a named inspection.

| ID | Requirement | Verification |
| --- | --- | --- |
| FR-01 | The service shall expose a liveness probe at `/actuator/health/liveness` and a readiness probe at `/actuator/health/readiness`; each shall return HTTP `200` with a body containing `"status":"UP"` while the application context is serving HTTP. | `ActuatorEndpointTest.livenessProbeReportsUp` and `readinessProbeReportsUp`. |
| FR-02 | The service shall expose `/actuator/prometheus` with Prometheus text exposition containing at least one JVM memory metric and the common tag `application="hello-service"`. | `ActuatorEndpointTest.prometheusEndpointExposesJvmMetrics` asserts `200`, `jvm_memory_used_bytes` and `application="hello-service"`. |
| FR-03 | The service shall expose only the Actuator endpoints `health`, `info` and `prometheus`; any other Actuator endpoint, including `env`, shall return `404`. | `ActuatorEndpointTest.unapprovedActuatorEndpointIsNotExposed` requests `/actuator/env` and asserts `404`; configuration review confirms the allowlist. |
| FR-04 | `GET /actuator/health` shall not disclose health component details; the response shall contain the status and shall not contain a `components` object. | `ActuatorEndpointTest.healthEndpointDoesNotDiscloseDetails`, plus the `management.endpoint.health.show-details=never` property. |
| FR-05 | Every HTTP request shall produce exactly one application access-log line containing the method, request URI path, response status, duration in milliseconds and the `requestId` from `RequestIdFilter`; the line shall be emitted after the filter chain completes. | `RequestAccessLogFilterTest.logsMethodPathStatusDurationAndRequestId` and `logsServerErrorStatus`. |
| FR-06 | The access log shall exclude the query string and all request/response bodies; when no `requestId` is present it shall log `requestId=-`. | `RequestAccessLogFilterTest.omitsQueryStringFromPath`; review confirms the filter reads only method, `getRequestURI()`, status, duration and MDC. |
| FR-07 | When the filter chain throws, the access log shall record status `500` and the exception shall be rethrown unchanged so existing error handling is preserved. | `RequestAccessLogFilterTest.logsStatus500AndRethrowsWhenChainFails`. |
| FR-08 | The change shall preserve the existing `/api/hello` and `/api/status` contracts and the `X-Request-ID` correlation behaviour. | The pre-existing `HelloEndpointTest`, `HelloHandlerTest`, `StatusEndpointTest`, `StatusHandlerTest` and `RequestIdFilter` tests pass unchanged; `RequestAccessLogFilterTest` proves the correlation path. |

## Non-functional requirements

Each names the quality attribute it constrains and, where one applies, a threshold.

| ID | Quality attribute | Requirement | Verification |
| --- | --- | --- | --- |
| NFR-01 | Security / information disclosure | The Actuator surface shall expose only `health`, `info` and `prometheus`; health details shall be hidden; the access log shall not contain query strings or payloads; no secret or credential shall be emitted by the access log or metrics. | `ActuatorEndpointTest` exposure/details tests; `RequestAccessLogFilterTest` query test; review of `application.properties`, the filter and the Prometheus output. |
| NFR-02 | Performance / overhead | The access log shall add no network, database or disk I/O beyond the existing stdout logger; the metrics endpoint shall read in-process registries only. No request path shall call an external monitoring service. | Review of `RequestAccessLogFilter` and `application.properties`; the filter uses `System.nanoTime()` and SLF4J only. |
| NFR-03 | Compatibility | The change shall ship in the existing JAR and image on port `8080`; it shall not change `/api/hello`, `/api/status`, the `X-Request-ID` response header or the application's existing logging pattern. | Existing endpoint tests pass unchanged; `application.properties` adds only Actuator properties; the Dockerfile is unchanged. |
| NFR-04 | Maintainability / standards | The implementation shall use the standard Spring Boot Actuator health groups and Micrometer Prometheus registry rather than a custom metrics server or custom health protocol; configuration shall be declarative in `application.properties`. | Dependency and configuration review; ADR-014 records the choice. |
| NFR-05 | Testability / determinism | The new behaviour shall be verifiable without an external monitoring service, using the existing JUnit 5, `@SpringBootTest` and MockMvc/mock-servlet infrastructure; the full suite shall pass offline. | `.\gradlew.bat clean test --offline --no-daemon --console=plain` is green; the new tests use only the existing test stack. |
| NFR-06 | Dependency security | The new dependencies shall be pinned by the Spring Boot BOM and committed Gradle lockfile and shall pass the existing dependency vulnerability scan; no unaccepted HIGH/CRITICAL finding shall be introduced. | `gradle.lockfile` diff; PR CI runs the pinned Trivy scan over `gradle.lockfile`. |
| NFR-07 | Operability | A probe or operator shall be able to distinguish liveness from readiness and to scrape metrics without parsing application logs; readiness is `UP` when the context is ready and no external dependency is required. | Actuator tests; the observability record names the endpoints and their criteria. |

## Acceptance criteria

| ID | Covers | Given | When | Then (observable condition) | Verification method |
| --- | --- | --- | --- | --- | --- |
| AC-01 | FR-01, NFR-07 | The application starts with the default configuration and no external service is available. | A client requests `/actuator/health/liveness` and `/actuator/health/readiness`. | Each response is `200` and contains `"status":"UP"`. | `ActuatorEndpointTest.livenessProbeReportsUp` and `readinessProbeReportsUp`. |
| AC-02 | FR-02 | The application starts with the default configuration. | A client requests `/actuator/prometheus`. | The response is `200`, contains `jvm_memory_used_bytes`, and contains the label `application="hello-service"`. | `ActuatorEndpointTest.prometheusEndpointExposesJvmMetrics`. |
| AC-03 | FR-03, NFR-01 | The application starts with the default configuration. | A client requests `/actuator/env`. | The response is `404`; the endpoint is not exposed. | `ActuatorEndpointTest.unapprovedActuatorEndpointIsNotExposed`; configuration review. |
| AC-04 | FR-04, NFR-01 | The application starts with the default configuration. | A client requests `/actuator/health`. | The response is `200`, contains `"status":"UP"` and does not contain `"components"`. | `ActuatorEndpointTest.healthEndpointDoesNotDiscloseDetails`; `show-details=never` review. |
| AC-05 | FR-05, NFR-02 | A request passes through `RequestIdFilter` with a known `X-Request-ID`. | The request is served. | Exactly one access log line contains `method`, `path`, `status`, `duration_ms` and the same `requestId`. | `RequestAccessLogFilterTest.logsMethodPathStatusDurationAndRequestId`. |
| AC-06 | FR-06, NFR-01 | A request has a query string containing a secret. | The request is served. | The access log contains the path but not the query string; the filter reads `getRequestURI()` rather than the full URL. | `RequestAccessLogFilterTest.omitsQueryStringFromPath`. |
| AC-07 | FR-07 | The filter chain throws an `IllegalStateException`. | The request is served. | The same exception is rethrown and exactly one access log line records `status=500`. | `RequestAccessLogFilterTest.logsStatus500AndRethrowsWhenChainFails`. |
| AC-08 | FR-08, NFR-03 | The change is applied to the repository. | The full test suite is executed. | All pre-existing endpoint tests pass without modification; `/api/hello` and `/api/status` behaviour is unchanged. | `.\gradlew.bat clean test --offline --no-daemon --console=plain` is green with the existing test classes unchanged. |
| AC-09 | NFR-05, NFR-06 | The change is applied to the repository. | The suite runs offline and CI runs the dependency scan. | The local suite passes offline; the lockfile is committed; CI reports no unaccepted HIGH/CRITICAL dependency finding. | Local test run; `gradle.lockfile` diff; PR CI Trivy step. |

## Dependencies

- Java 21 toolchain and Spring Boot 3.5.14, already used by the project.
- `spring-boot-starter-actuator` 3.5.14 for health groups and the Actuator endpoint
  infrastructure.
- `micrometer-registry-prometheus` 1.15.12 and the Prometheus Java client 1.3.10 for the
  Prometheus exposition format.
- The existing `spring-boot-starter-web`, `RequestIdFilter`, `logback-spring.xml` and
  `HelloHandler`/`StatusHandler` contracts.
- The committed `gradle.lockfile` and the existing Trivy dependency scan in CI.
- No external service, metrics backend, secret or configuration server is required.

## Risks and assumptions

- **Assumption:** `/api/status` remains the application-level liveness contract and is not
  replaced by Actuator. If a consumer expects only one health path, the new `/actuator/health`
  path is additive and the old path still works.
- **Assumption:** readiness is `UP` because the service has no external dependencies. If a
  database, broker or downstream service is added, readiness must include it or the probe can
  report a false `UP`.
- **Assumption:** the Actuator endpoints remain inside the local deployment boundary. If they
  are exposed publicly, `info` and `prometheus` become an information-disclosure surface and
  authentication or network policy is required.
- **Assumption:** the access log's request ID comes from the existing `RequestIdFilter`. If the
  filter order changes, the access log may record `-` even when a response carries an ID.
- **Risk:** the Prometheus endpoint and JVM metrics reveal process and resource information.
  **Mitigation:** only `health`, `info` and `prometheus` are exposed; health details are hidden;
  the endpoint is not given a public ingress in this change.
- **Risk:** per-request access logs increase stdout volume. **Mitigation:** one line per request,
  local PoC only; the log level and destination are unchanged; a production deployment would
  add sampling or retention outside this requirement.
- **Risk:** logging the path could capture sensitive path segments even when the query string is
  excluded. **Mitigation:** no sensitive paths exist today; future sensitive routes must not put
  secrets in the path and need a separate logging requirement.
- **Risk:** the client-controlled `X-Request-ID` remains a log-injection vector. **Mitigation:**
  the existing residual risk in `blueprint/reviews/api-status-endpoint.md` remains accepted; the
  access log repeats the value but does not add a new input.
- **Risk:** the new dependencies add attack surface or CVEs. **Mitigation:** versions are pinned
  by the BOM and lockfile and scanned by the existing Trivy CI step.
- **Risk:** the per-request filter can alter error behaviour if it swallows exceptions.
  **Mitigation:** AC-07 requires rethrowing the original exception; the filter uses `finally`
  for logging and a catch only to set status `500`.

## Traceability

- **Realised by:** `RequestAccessLogFilter`, `application.properties`, the Actuator/Prometheus
  dependencies in `build.gradle.kts` and `gradle.lockfile`, `ActuatorEndpointTest` and
  `RequestAccessLogFilterTest`.
- **Design:** `blueprint/design/observability-improvements.md`.
- **Architecture:** `blueprint/architecture/observability-improvements.md` and
  `blueprint/architecture/adr/adr-014-observability-stack.md`.
- **Test evidence:** `blueprint/testing/observability-improvements.md`.
- **Operational record:** `blueprint/observability/hello-service.md`.
- **Improvement items:** `IMP-001`, `IMP-003` and `IMP-005` in
  `blueprint/improvements/continuous-improvements.md`.
- **Related requirement:** `REQ-OBS-002` (`blueprint/requirements/observability-alerting.md`)
  defines the alert rules that close `IMP-004`.
- **Change and review:** `blueprint/changes/observability-improvements.md` and
  `blueprint/reviews/observability-improvements.md`.
- **Delivered in:** no new release version; the change ships in the existing `0.1.0` artifact
  line and is recorded against the commit under closure, not a new tag.
