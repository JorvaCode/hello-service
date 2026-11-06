# ADR-014: Operational observability stack

## Status

Accepted

## Context

The phase 13 observability record closes the local PoC with manual detection: structured logs
with `requestId`, `/api/status` as a liveness signal and a manual runbook. It records four
gaps as improvement items:

- `IMP-001`: no application/JVM metrics endpoint;
- `IMP-003`: no readiness probe;
- `IMP-004`: no automated alert definitions;
- `IMP-005`: no per-request access log.

The service has no external dependencies and no production monitoring stack. Adding
observability must not change `/api/hello` or `/api/status`, and must not imply a monitoring
or tracing capability the project does not operate. The decision is significant under
`standards/terms.md`: it introduces runtime dependencies (criterion c), changes the operational
and security surface (criterion d) and affects consumers outside the team (criterion e).

## Options considered

1. **Spring Boot Actuator + Micrometer Prometheus registry + a custom access-log filter.**
   Standard health groups and a standard metrics exposition format; the access log reuses the
   existing MDC correlation. New runtime dependencies and a wider Actuator surface.
2. **Custom health, metrics and access-log endpoints.**
   No third-party observability dependency, but reimplements health-group semantics and the
   Prometheus format, creates a second operational contract and needs custom tests for every
   edge case.
3. **Actuator without the Prometheus registry.**
   Adds probes but no metrics endpoint; does not close `IMP-001`.
4. **Tomcat `AccessLogValve` or handler-level logging.**
   Produces access logs without a new application filter, but does not naturally carry the
   existing `requestId` MDC and misses requests rejected before a handler.
5. **OpenTelemetry agent/collector plus a metrics/tracing backend.**
   A fuller observability platform, but adds a collector, network dependency, operational
   ownership and a capability outside the local PoC scope.
6. **Versioned Prometheus alert rules without a deployed monitoring stack.**
   Makes detection reviewable and closes the definition gap, but cannot prove evaluation,
   routing or paging in this repository.

## Decision

Use **Spring Boot Actuator** for liveness/readiness health groups, the **Micrometer Prometheus
registry** for the `/actuator/prometheus` endpoint, and a **Servlet filter**
(`RequestAccessLogFilter`) after `RequestIdFilter` for the per-request access log.

The Actuator surface is restricted to `health`, `info` and `prometheus`; health details are
hidden with `management.endpoint.health.show-details=never`. The metrics common tag is
`application="hello-service"`. The access log records method, request URI path, status,
duration in milliseconds and `requestId`, excludes query strings and payloads, and rethrows
exceptions after logging status `500`.

`/api/status` remains the application-level liveness contract. Actuator is an additive
operational surface, not a replacement.

Alert definitions live in `ops/prometheus/hello-service-alerts.yml` and map `FM-01`...`FM-07`
plus the `HELLO-001` contract to a Prometheus expression, a `for` duration, a severity and a
runbook URL. They are versioned
configuration evidence, not a running alerting path. No Prometheus, Alertmanager, blackbox
exporter, dashboard, tracing collector or metrics backend is operated; evaluation, routing and
paging remain outside this decision.

## Consequences

### Positive

- Probes and metrics use standard Spring Boot/Micrometer behavior rather than a custom protocol.
- The `/actuator/prometheus` output is consumable by a future Prometheus server without a
  custom exporter.
- The access log is correlated by the existing `requestId` and covers `404`, `405` and filter
  failures, not only handler errors.
- The endpoint allowlist and hidden health details keep the local default surface smaller than
  the full Actuator inventory.
- The change ships in the existing JAR, image and port; no new deployable component is needed.

### Negative / trade-offs

- Actuator, Micrometer and the Prometheus Java client add runtime dependencies and CVE surface;
  they are pinned by the Spring Boot BOM and `gradle.lockfile` and scanned by Trivy.
- Actuator endpoints are unauthenticated inside the local deployment boundary. Exposing the
  service beyond that boundary requires authentication or network policy, which is out of scope.
- `/actuator/prometheus` and JVM/process metrics reveal resource information; the allowlist
  limits the surface but does not make it safe for public exposure.
- One INFO access-log line per request increases stdout volume. A production deployment needs
  retention/sampling decisions outside this change.
- Readiness is `UP` because the service has no external dependencies; it will become misleading
  unless extended when a dependency is added.
- Alert definitions are configuration only. No rule is evaluated, routed or paged by this
  repository; the observability record must not claim a live automated detection path.
- Thresholds are local-PoC defaults. Deploying the rules requires a Prometheus/Alertmanager
  owner and tuning against real traffic, which is out of scope.
- The client-controlled `X-Request-ID` remains a log-injection residual risk already accepted
  in `blueprint/reviews/api-status-endpoint.md`.

## Date

2026-11-03 (updated 2026-11-06 for `IMP-004` alert definitions and Micrometer security pin)
