# Analysis: `REQ-OBS-001` — Operational observability signals

## Requirement analysed

- `blueprint/requirements/observability-improvements.md` — `REQ-OBS-001`.
- The requirement is additive: it adds liveness/readiness probes, a Prometheus endpoint and a
  per-request access log while preserving `REQ-HELLO-001` and `REQ-STATUS-001`.
- **Status:** analysis complete. The direction is implementable with the current tree and the
  existing test stack. Decisions D1–D6 are resolved or assigned to architecture/design.

## Existing system context (verified)

- **Runtime stack:** Java 21, Spring Boot 3.5.14, `spring-boot-starter-web` and embedded Tomcat
  (`build.gradle.kts`).
- **HTTP surface:** `HelloHandler` (`GET /api/hello`) and `StatusHandler` (`GET /api/status`)
  are the existing application endpoints. `/api/status` is liveness-only and does not use
  Actuator.
- **Correlation:** `RequestIdFilter` is a `@Component` with `Ordered.HIGHEST_PRECEDENCE`. It
  honours or generates `X-Request-ID`, echoes it in the response and binds it to the SLF4J MDC
  key `requestId`; the MDC entry is removed in a `finally` block.
- **Logging:** `logback-spring.xml` writes to stdout with the pattern
  `timestamp level [thread] [requestId=<MDC>] logger - message`.
- **Configuration before this change:** only `logback-spring.xml` existed under
  `src/main/resources`; there was no `application.properties` or `application.yml`.
- **Current implementation under test:** `application.properties` now exposes only
  `health,info,prometheus`, enables health probes, hides health details, tags metrics with
  `application=hello-service` and explicitly enables the Prometheus registry.
- **Dependencies under test:** `spring-boot-starter-actuator` 3.5.14,
  `micrometer-registry-prometheus` 1.15.11, its Prometheus Java client 1.3.10 and the
  transitive `micrometer-core`/`HdrHistogram`/`LatencyUtils` dependencies. `gradle.lockfile`
  records the resolved set.
- **Baseline and current tests:** the pre-change suite had 17 tests. The current suite has 28
  tests: the 17 existing tests, 5 Actuator tests, 5 access-log/counter tests and 1 alert-rule
  test.

## Domain concepts

- **Liveness:** the process is running and serving HTTP. It says nothing about external
  dependencies.
- **Readiness:** the process is ready to receive traffic. For this service, readiness is the
  application context being ready and the HTTP listener accepting requests; there are no
  external dependencies to check.
- **Metric:** a numeric measurement exposed in Prometheus text format. The requirement uses
  framework/JVM and HTTP-server metrics, not custom business metrics.
- **Access log:** one structured line per HTTP request, distinct from framework error logs and
  from the startup/lifecycle log.
- **Correlation identifier:** the existing `requestId` MDC value, not a second identifier.
- **Actuator surface:** the set of operational endpoints exposed by Spring Boot Actuator under
  `/actuator`.

## Dependencies and integrations

| Dependency / integration | Role for this change | Evidence | Impact |
| --- | --- | --- | --- |
| Spring Boot Actuator 3.5.14 | Health endpoints, health groups, endpoint exposure model | Dependency added; `application.properties`; `ActuatorEndpointTest` | New runtime dependency and operational endpoints |
| Micrometer Prometheus registry 1.15.11 | Binds a `PrometheusMeterRegistry` and the `/actuator/prometheus` endpoint | Dependency added; Prometheus test | New runtime dependency and metrics output |
| Prometheus Java client 1.3.10 | Text exposition format | Transitive dependency in `gradle.lockfile` | New transitive dependencies, scanned by Trivy |
| Spring MVC / embedded Tomcat | Existing request pipeline and HTTP listener | Existing application and tests | Reused; no deployment topology change |
| `RequestIdFilter` | Supplies `requestId` to the access log | Existing filter and MDC contract | Filter must run first; access filter order is `HIGHEST_PRECEDENCE + 10` |
| SLF4J/Logback | Access-log destination and pattern | `logback-spring.xml` | Access log uses the existing stdout sink |
| `gradle.lockfile` / Trivy CI | Reproducible dependency resolution and vulnerability scan | Existing lockfile and CI step | New dependencies are locked and scanned |

## Integration direction and alternatives considered

The integration point is the existing Spring Boot application. Actuator and Micrometer are
auto-configured from the classpath; the access log is a Servlet `Filter` in the existing
component scan. No new deployable component, port or network route is introduced.

| Option | Assessment | Outcome |
| --- | --- | --- |
| Spring Boot Actuator + Micrometer Prometheus + custom access-log filter | Standard probes and metrics, minimal custom code, reuses the existing correlation filter and test stack | **Chosen** |
| Custom `/api/health`, custom `/api/metrics` and a custom access-log endpoint | Reimplements health-group semantics, exposition format and endpoint security; creates a second contract to maintain | Rejected |
| Actuator without the Prometheus registry | Provides health probes but no metrics endpoint; does not close `IMP-001` | Rejected |
| Tomcat `AccessLogValve` | Writes container-format access logs but does not naturally carry the existing `requestId` MDC or the application's structured pattern | Rejected |
| Log at each handler or add a `HandlerInterceptor` | Duplicates logging and misses requests rejected before the handler (`404`, `405`, filter failures) | Rejected |
| Expose all Actuator endpoints | Wider information-disclosure surface and more configuration than needed | Rejected |
| OpenTelemetry agent/sidecar + tracing backend | Adds a collector, network dependency and a capability the local PoC does not operate | Rejected; remains out of scope |
| No readiness endpoint | Keeps the manual limitation and gives an orchestrator no readiness signal | Rejected; `IMP-003` explicitly requests readiness |

**Direction chosen:** Actuator for probes and the endpoint model, the Micrometer Prometheus
registry for metrics, and a filter after `RequestIdFilter` for the access log. The Actuator
surface is restricted to `health`, `info` and `prometheus`; health details are hidden. The
change adds no alerting backend and does not replace `/api/status`.

## Constraints

- `/api/hello` and `/api/status` shall remain unchanged (NFR-03, FR-08).
- Actuator exposure is an allowlist; `env` and other endpoints shall return `404` (FR-03).
- Health details shall not be disclosed (FR-04).
- The access log shall contain no query string or payload (FR-06, NFR-01).
- The filter shall rethrow exceptions and log status `500` (FR-07).
- The new dependencies shall be pinned and scanned (NFR-06).
- No external monitoring service is required for tests (NFR-05).
- The existing `requestId` is the only correlation identifier.

## Risks

| ID | Risk | Evidence / basis | Mitigation |
| --- | --- | --- | --- |
| R-01 | Actuator endpoints are unauthenticated inside the deployment boundary. | No Spring Security on the classpath; the service is a local PoC. | Expose only `health`, `info` and `prometheus`; hide health details; do not claim public exposure is safe; authentication remains a separate requirement. |
| R-02 | The Prometheus endpoint reveals JVM/process information. | Prometheus test observes JVM memory metrics. | Restrict the endpoint allowlist and document the local boundary; a public deployment needs network policy or authentication. |
| R-03 | The access log increases stdout volume and may contain sensitive path segments. | One INFO line per request; query strings are excluded. | Keep one line, no query/body; document that sensitive route segments require a separate logging decision. |
| R-04 | Readiness can report `UP` when a future external dependency is down. | The service has no external dependencies today. | Record the assumption; when a dependency is added, extend readiness or it becomes a defect. |
| R-05 | Filter ordering can produce `requestId=-` despite a response header. | `RequestIdFilter` runs at `HIGHEST_PRECEDENCE`; the access filter runs at `HIGHEST_PRECEDENCE + 10`. | Test the composed filter chain; keep the order explicit in code and review. |
| R-06 | The filter could swallow an exception and change error handling. | The filter has a catch for status `500`. | Rethrow the original exception; AC-07 covers the path. |
| R-07 | The new dependencies add CVE exposure. | Actuator, Micrometer and Prometheus client are new runtime libraries. | Pin through the BOM and lockfile; run the existing Trivy scan; record any accepted finding. |
| R-08 | The `X-Request-ID` log-injection residual risk is repeated in the access log. | Existing review accepted the risk; the filter logs the MDC value. | Keep the existing acceptance; do not add new client-controlled fields. |

## Assumption register

| ID | Assumption | Consequence if false | Status |
| --- | --- | --- | --- |
| A-01 | Liveness and readiness are distinct signals, and `/api/status` remains the application liveness contract. | The new Actuator paths would duplicate or contradict the old contract; consumers would need one path removed or redirected. | Confirmed by requirement scope; `/api/status` tests remain unchanged. |
| A-02 | Readiness is `UP` when the context is ready because the service has no external dependencies. | The readiness probe can be falsely green after a database/broker is introduced. | Recorded risk R-04; revisit with any dependency. |
| A-03 | The Actuator endpoints are not publicly reachable in the local PoC. | `info`/`prometheus` become an information-disclosure surface and require authentication. | Recorded in the requirement; local deployment boundary unchanged. |
| A-04 | The existing `RequestIdFilter` is the source of the access-log correlation ID. | A second ID scheme would disagree with the response header and existing logs. | Confirmed by inspection and the composed filter test. |
| A-05 | The `requestId` may contain client-controlled text and remains a log-injection vector. | A malicious header could inject log content. | Accepted residual risk from `blueprint/reviews/api-status-endpoint.md`; no new input is added. |
| A-06 | The local Gradle cache can resolve the new dependencies offline. | The local test command would fail without network and the test evidence would not be reproducible offline. | Confirmed by the green offline run at `1b1db72`. |
| A-07 | Actuator auto-configuration is active in the default profile and is not conditional on a feature flag. | The probes/metrics could be missing despite the configuration. | Confirmed by the `@SpringBootTest` Actuator tests and the default `application.properties`. |

## Discrepancies between existing artifacts and the implementation

- **`REQ-STATUS-001` excluded Actuator.** That requirement remains valid: `/api/status` is
  still a custom liveness endpoint and its NFR-01 still forbids adding Actuator *for that
  endpoint*. `REQ-OBS-001` is a new requirement and adds Actuator as a separate operational
  surface; it does not rewrite or relax `REQ-STATUS-001`.
- **Phase 13 record said no metrics, no readiness and no access log.** Those statements were
  true at phase 13 closure. `REQ-OBS-001` changes the implementation, so the observability
  record is updated in this change. The phase 13 gate remains closed with the limitation that
  alert rules are defined but no Prometheus/Alertmanager evaluation or paging path is deployed;
  the signals and rule definitions are now implemented.
- **The earlier analysis of `REQ-STATUS-001` states there is no `application.properties`.**
  That was true at the time. This change introduces one for Actuator configuration; the earlier
  analysis is historical and is not rewritten.

No other conflict was found.

## Impact on existing behaviour, tests, compatibility and security

- **`/api/hello`:** unchanged; existing 9 tests remain green.
- **`/api/status`:** unchanged; existing 8 tests remain green. It still returns
  `{"status":"UP"}` and is liveness-only; Actuator is additive.
- **Request correlation:** unchanged; `RequestIdFilter` still owns the response header and MDC.
  The access log consumes the same value.
- **Logs:** one new INFO line per request. Startup and framework error logs are unchanged.
- **Actuator:** new `/actuator/health`, `/actuator/health/liveness`,
  `/actuator/health/readiness`, `/actuator/info` and `/actuator/prometheus` paths. `/actuator/env`
  is rejected.
- **Security:** no request payload or query string is logged. The Actuator allowlist and hidden
  health details reduce the exposed surface, but the endpoints remain unauthenticated inside
  the local boundary. The existing `X-Request-ID` residual risk remains.
- **Deployment:** the same JAR, image, port and workflows. No container healthcheck or CD smoke
  test is added.
- **Tests:** 17 existing tests remain unchanged; 11 new tests cover the new behaviour. The total
  is 28.

## Files potentially affected

| File | Change | Basis |
| --- | --- | --- |
| `build.gradle.kts` | Add Actuator and Prometheus registry dependencies | FR-01–FR-03 |
| `gradle.lockfile` | Lock the resolved dependency set | NFR-06 |
| `src/main/resources/application.properties` | Expose endpoints, enable probes, hide details, set application tag, enable Prometheus export | FR-01–FR-04 |
| `src/main/java/com/example/hello/RequestAccessLogFilter.java` | New access-log filter and missing-correlation counter | FR-05–FR-07 |
| `src/test/java/com/example/hello/ActuatorEndpointTest.java` | Probe, metrics, exposure and detail tests | AC-01–AC-04 |
| `src/test/java/com/example/hello/RequestAccessLogFilterTest.java` | Access-log, counter, query and exception tests | AC-05–AC-07 |
| `ops/prometheus/hello-service-alerts.yml` | Eight Prometheus alert rules | `REQ-OBS-002` FR-01–FR-04 |
| `ops/prometheus/README.md` | Scrape and blackbox job documentation | `REQ-OBS-002` FR-05 |
| `src/test/java/com/example/hello/AlertRulesTest.java` | Rule YAML validation | `REQ-OBS-002` AC-01–AC-03 |
| `blueprint/requirements/observability-improvements.md` | New requirement | Phase 01 |
| `blueprint/requirements/observability-alerting.md` | New alerting requirement | Phase 01 |
| `blueprint/architecture/observability-improvements.md` | New architecture description | Phase 03 |
| `blueprint/architecture/adr/adr-014-observability-stack.md` | New ADR | Phase 03 |
| `blueprint/design/observability-improvements.md` | New design | Phase 04 |
| `blueprint/testing/observability-improvements.md` | New test evidence | Phase 06 |
| `blueprint/reviews/observability-improvements.md` | New review | Phase 07 |
| `blueprint/observability/hello-service.md` | Update instrumentation, failure detection and gate | Phase 13 |
| `blueprint/improvements/continuous-improvements.md` | Close `IMP-001`, `IMP-003`, `IMP-004`, `IMP-005` | Phase 14 |
| `blueprint/gates.md` | Update P-13 and related evidence | Closure |
| `README.md` | Update observability/limitations sections | Project record |
| `blueprint/changes/observability-improvements.md` | Change record | Delivery record |

## Decisions

**Resolved here**

- **D1 — Stack:** use Spring Boot Actuator for health and its endpoint model, and the Micrometer
  Prometheus registry for metrics. Standard behavior is cheaper and safer than custom
  endpoints. ADR-014 records the dependency and quality-attribute significance.
- **D2 — Access-log mechanism:** a Servlet `Filter` after `RequestIdFilter` rather than a Tomcat
  valve or handler-level logging, so every request including `404`, `405` and filter failures
  is logged with the existing MDC.
- **D3 — Exposure:** allowlist `health`, `info` and `prometheus`; hide health details. This is
  the minimum surface that closes the three improvement items.
- **D4 — Readiness semantics:** expose the standard readiness group; it is `UP` after context
  startup because no external dependency exists. Revisit when one is added.
- **D5 — Alert definitions, no evaluation:** metrics and access logs are signals; eight
  Prometheus rules map them to failure modes, but no Prometheus, Alertmanager or paging path is
  deployed. `IMP-004` is implemented at the definition level and the record must not claim live
  alerting.
- **D6 — ADR significance:** ADR-014 is required because the change introduces runtime
  dependencies (criterion c), changes the operational/security surface (d) and affects
  consumers outside the team (e).

**Deferred to design**

- Filter class name, exact order expression and logging format.
- Whether to use `@Component` or a `FilterRegistrationBean`.
- Exact test split between the Actuator integration test and the access-log unit test.
- Whether to add a custom info contributor (not needed for this requirement).

## Readiness for the next phase

The direction is implementable with the current tree. The required dependencies resolve
offline, Actuator auto-configures the probes and Prometheus endpoint, and the access log can use
the existing MDC. The implementation and tests are present at commit `1b1db72`; the remaining
work is documentation reconciliation and PR CI.

## Traceability

- **Requirements:** `REQ-OBS-001` (`blueprint/requirements/observability-improvements.md`) and
  `REQ-OBS-002` (`blueprint/requirements/observability-alerting.md`).
- **REQ-OBS-001 FR-01 ->** Actuator health groups (AC-01); **FR-02 ->** Prometheus registry
  (AC-02); **FR-03 ->** endpoint allowlist (AC-03); **FR-04 ->** hidden health details (AC-04);
  **FR-05 ->** access-log fields (AC-05); **FR-06 ->** query exclusion (AC-06);
  **FR-07 ->** exception rethrow (AC-07); **FR-08 ->** existing contracts (AC-08).
- **REQ-OBS-002 FR-01 ->** eight-rule inventory (AC-01); **FR-02 ->** failure-mode mapping
  (AC-02); **FR-03 ->** missing-correlation counter (AC-03); **FR-04 ->** runbook links (AC-04);
  **FR-05 ->** deployment limitation (AC-05).
- **NFR-01 ->** exposure/details/query review (AC-03, AC-04, AC-06); **NFR-02 ->** filter review;
  **NFR-03 ->** existing tests (AC-08); **NFR-04 ->** ADR-014 and configuration review;
  **NFR-05 ->** offline suite (AC-09); **NFR-06 ->** lockfile and Trivy (AC-09);
  **NFR-07 ->** probe tests (AC-01).
- **Design:** `blueprint/design/observability-improvements.md`.
- **ADR:** `blueprint/architecture/adr/adr-014-observability-stack.md`.

## Evidence log

| Check | Command / method | Result |
| --- | --- | --- |
| Existing baseline | Prior phase evidence | 17 tests green before this change |
| Current suite | `.\gradlew.bat clean test --offline --no-daemon --console=plain` at `1b1db72` | `BUILD SUCCESSFUL`; 28 tests, 0 failures, 0 errors, 0 skipped |
| Per class | Gradle test XML | `ActuatorEndpointTest` 5, `AlertRulesTest` 1, `HelloEndpointTest` 6, `HelloHandlerTest` 3, `RequestAccessLogFilterTest` 5, `StatusEndpointTest` 6, `StatusHandlerTest` 2 |
| Actuator probes | `ActuatorEndpointTest` | Liveness/readiness `200`, body contains `"status":"UP"` |
| Prometheus | `ActuatorEndpointTest` | `200`, contains `jvm_memory_used_bytes` and `application="hello-service"` |
| Exposure allowlist | `ActuatorEndpointTest` | `/actuator/env` is `404`; health body has no `components` |
| Access log | `RequestAccessLogFilterTest` | One line with method/path/status/duration/requestId; query omitted; status `500` and rethrow on failure |
| Dependency lock | `gradle.lockfile` diff | Actuator, Micrometer, Prometheus client and transitives locked |
