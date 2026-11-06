# Architecture: `REQ-OBS-001` — Operational observability signals

## Status

Architecture description for the additive observability change. The requirement is
`blueprint/requirements/observability-improvements.md`; the analysis is
`blueprint/analisys/observability-improvements.md`. The significant decision is recorded in
ADR-014. This document does not change `REQ-HELLO-001` or `REQ-STATUS-001`.

## Stakeholders and concerns

| Stakeholder | Concern | Justifying requirement |
| --- | --- | --- |
| Maintainer / operator | See whether the process is alive, ready and emitting useful per-request logs | FR-01, FR-05, NFR-07 |
| Future orchestrator or probe | Distinguish liveness from readiness without parsing application logs | FR-01, NFR-07 |
| Future metrics consumer | Scrape standard Prometheus exposition instead of a custom format | FR-02, NFR-04 |
| Security reviewer | Limit the operational surface and avoid leaking payloads/secrets | FR-03, FR-04, FR-06, NFR-01 |
| Consumer of `/api/hello` and `/api/status` | Keep the existing contracts and correlation behavior unchanged | FR-08, NFR-03 |
| CI / packaging owner | Keep dependencies pinned, reproducible and scanned | NFR-06 |
| Test maintainer | Verify the behavior without an external monitoring service | NFR-05 |

The repository has no formal stakeholder register independent of the project artifacts. That
gap is recorded rather than guessed; the table names the stakeholders the change actually
affects.

## Architecture description

### System boundary and deployment model

The change stays inside the existing Spring Boot process. The service remains a single JAR and
a single OCI image on port `8080`; Actuator paths are served by the same embedded Tomcat on the
same port. No sidecar, collector, agent, metrics server or additional network route is added.
Logs continue to go to stdout and are collected by the existing local procedure.

The operating boundary is the local PoC. The Actuator endpoints are unauthenticated inside
that boundary. Public exposure is outside the architecture and would require authentication or
network policy.

### Components and responsibilities

| Component | Responsibility | Requirement / constraint that makes it necessary |
| --- | --- | --- |
| `HelloHandler` | Existing `GET /api/hello` contract | Unchanged by FR-08 / NFR-03 |
| `StatusHandler` | Existing `GET /api/status` liveness contract | Unchanged by FR-08 / NFR-03 |
| `RequestIdFilter` | Generates or honors `X-Request-ID`, echoes it, binds `requestId` to MDC | Existing correlation reused by FR-05 |
| `RequestAccessLogFilter` | Emits one access-log line after the chain completes; status `500` on failure | FR-05, FR-06, FR-07 |
| Spring Boot Actuator health infrastructure | Serves `/actuator/health`, liveness and readiness groups | FR-01, FR-04 |
| Micrometer `PrometheusMeterRegistry` | Collects JVM/HTTP-server meters and serves `/actuator/prometheus` | FR-02, NFR-04 |
| `application.properties` | Declares exposure allowlist, probe groups, hidden details, application tag and Prometheus enablement | FR-01–FR-04, NFR-01 |
| Logback stdout configuration | Existing destination and pattern for the access log | FR-05, NFR-03 |
| `gradle.lockfile` and Trivy CI step | Reproducible dependency identity and vulnerability scan | NFR-06 |

No custom controller, custom health protocol, custom metrics registry, database or cache is
added. The framework components are configured declaratively; the only new application class
is the access-log filter.

### Interfaces and contracts

| Interface | Contract | Basis |
| --- | --- | --- |
| `GET /actuator/health/liveness` | `200`, body contains `"status":"UP"` while serving HTTP | FR-01, AC-01 |
| `GET /actuator/health/readiness` | `200`, body contains `"status":"UP"` when the context is ready; no external dependency today | FR-01, AC-01, NFR-07 |
| `GET /actuator/prometheus` | `200`, Prometheus text exposition with JVM metrics and `application="hello-service"` | FR-02, AC-02 |
| `GET /actuator/health` | `200`, status only; no `components` object | FR-04, AC-04 |
| `GET /actuator/env` | `404`; not in the allowlist | FR-03, AC-03 |
| Access log | One line per request: `access method=... path=... status=... duration_ms=... requestId=...` | FR-05–FR-07, AC-05–AC-07 |
| Existing `/api/hello`, `/api/status` | Unchanged contracts and `X-Request-ID` behavior | FR-08, NFR-03 |

Actuator `info` is exposed as part of the allowlist but no contributor or sensitive detail is
configured. Metrics are per-process and in-memory; there is no aggregation across instances.

### Data ownership and state

- Metrics are owned by the in-process Micrometer registry and are not persisted.
- Access-log records are owned by the process and written to stdout; there is no database,
  log store or retention policy in this change.
- `requestId` is request-scoped MDC state owned by `RequestIdFilter`; the access filter only
  reads it.
- The Actuator health state is process state; no external dependency is consulted.

### Security boundaries

- The Actuator surface is restricted to `health`, `info` and `prometheus`; health details are
  hidden. This reduces, but does not remove, information disclosure.
- The access log excludes query strings and payloads. Path segments are logged; no sensitive
  application route exists today.
- No secret, credential or request body is added to logs or metrics.
- The endpoints remain unauthenticated inside the local boundary. The deployment model does not
  add a public ingress.
- The client-controlled `X-Request-ID` remains the accepted log-injection residual risk from
  `blueprint/reviews/api-status-endpoint.md`.

### Dependencies and integration patterns

- Actuator auto-configuration discovers the classpath and exposes endpoints according to
  `application.properties`.
- Micrometer binds the Prometheus registry and the Actuator endpoint.
- The access-log filter participates in the existing Servlet filter chain; no new integration
  protocol or outbound call is introduced.
- The existing `RequestIdFilter` ordering is preserved; the access filter runs later.
- The dependency set is locked in `gradle.lockfile` and covered by the existing Trivy scan.

### Views

One operational/runtime view serves all concerns in this change: the single Spring Boot
process, its HTTP surface, the Actuator/Micrometer components, the filter chain and the stdout
log sink. The concerns are operational visibility (FR-01/FR-02/FR-05), security (FR-03/FR-04/
FR-06) and compatibility (FR-08); none requires a separate structural view. The view is
therefore a single description, which `blueprint/03-architecture.md` permits.

## Quality attribute scenarios

| ID | Source | Stimulus | Environment | Artifact | Response | Response measure |
| --- | --- | --- | --- | --- | --- | --- |
| QAS-01 | Orchestrator or operator | Requests liveness and readiness after startup | Default local configuration; no external service | Actuator health groups | Both probes return `200` and `"status":"UP"` | Both responses pass AC-01; no `404` and no `DOWN` |
| QAS-02 | Future Prometheus scraper | Scrapes `/actuator/prometheus` | Default local configuration; service serving HTTP | Micrometer Prometheus endpoint | Returns Prometheus text with JVM and application metrics | `200`, contains `jvm_memory_used_bytes` and `application="hello-service"` (AC-02) |
| QAS-03 | Client request with `X-Request-ID` | Request is served, including a failing chain | Default local configuration | `RequestAccessLogFilter` and stdout log | Emits exactly one correlated access line | Line contains method, path, status, `duration_ms` and the same `requestId`; query string absent (AC-05, AC-06) |
| QAS-04 | Security reviewer | Requests an unapproved Actuator endpoint or health details | Default local configuration; endpoints unauthenticated | Actuator exposure and health endpoint | Unapproved endpoint is absent; health details are hidden | `/actuator/env` is `404`; `/actuator/health` has no `components` (AC-03, AC-04) |
| QAS-05 | Existing consumer | Calls `/api/hello` or `/api/status` | Default local configuration; new dependency present | Existing HTTP handlers and correlation filter | Existing responses and `X-Request-ID` behavior remain | All pre-existing tests pass unchanged (AC-08) |
| QAS-06 | CI / dependency owner | Resolves and scans the new runtime dependencies | Offline local build and CI | `gradle.lockfile`, Trivy scan | Dependency set is reproducible and scanned | Lockfile committed; CI has no unaccepted HIGH/CRITICAL finding (AC-09) |

**Sensitivity:** the local PoC has no throughput requirement and no load test, so the response
measures are functional rather than latency-based. Access-log volume grows linearly with
request rate and metrics scrape cost grows with the registry size; a production deployment
would need sampling/retention and load evidence outside this change.

## Architecture decision records

| ADR | Status | Scope |
| --- | --- | --- |
| [ADR-014](adr/adr-014-observability-stack.md) | Accepted | Actuator + Micrometer Prometheus + access-log filter; restricted exposure; eight versioned alert rules; no deployed Prometheus/Alertmanager/tracing |
| [ADR-006](adr/adr-006-calidad-diseno-codigo-solid-clean-code-patrones.md) | Accepted | Code-design quality criteria used by the design |
| [ADR-010](adr/adr-010-vistas-y-escenarios-de-atributo-de-calidad.md) | Accepted | View and quality-attribute-scenario format |
| [ADR-013](adr/adr-013-trazabilidad-de-extremo-a-extremo.md) | Accepted | Traceability from requirement to check |

ADR-014 is required by `standards/terms.md` significance criteria (c), (d) and (e): new
runtime dependencies, a changed operational/security quality attribute, and an effect on
consumers outside the team. No existing ADR is superseded. `ADR-009` is about information items
per phase, not an observability stack; it is not applicable.

## Gate assessment

- Every component names the requirement or constraint that makes it necessary.
- Every concern with a threshold or testable condition has a quality attribute scenario with a
  response measure: QAS-01–QAS-06.
- The significant decision is recorded as ADR-014 with Status `Accepted`.
- The single view serves all concerns; a correspondence table is unnecessary because no second
  view exists.
- Known gap: the repository has no independent architecture reviewer; the self-review is
  recorded in `blueprint/reviews/observability-improvements.md` and does not claim independent
  approval.
