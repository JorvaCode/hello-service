# 13 — Observability

A deployed system shall provide enough information to understand its behaviour.

## Signals
- Logs
- Metrics
- Traces
- Alerts

## Operational requirements
- Health endpoints/checks
- Useful structured logs
- Error correlation
- Relevant business/technical metrics
- Actionable alerts

## Information items
### Inputs
- **Deployed system** — the services in production, from `12-deployment.md`.
- **Failure modes** — the operational requirements stated in the design, and the failures that would matter to the business if they occurred.
- **Operating context** — the environments, the traffic profile, and what normal looks like.

### Outputs
- **Instrumentation** — the logs, metrics and traces the system emits, with the structure and the correlation identifier that makes them joinable.
- **Alerts** — each mapping a detection condition to the failure it indicates and to the response it triggers. An alert with no response is noise, and a failure mode with no alert is an unmonitored risk.
- **Runbook** — what to do when each alert fires, reachable from the alert itself.
- **Health checks** — the endpoints or checks that report readiness and liveness, with the criteria each one applies.

## Quality gate
Every failure mode named in the design has an alert mapped to it, and every alert names the response it triggers and links to a runbook. A responder who was not on call reaches the same verdict on what to do from the alert and the runbook alone, without asking the author.

## Project record — local 0.1.0

The local PoC observability record is `blueprint/observability/hello-service.md`. It records
the operating context, structured logs with `requestId` correlation, the `/api/status`
liveness check, Actuator liveness/readiness probes, Prometheus metrics, the per-request access
log, eight Prometheus alert rules, seven failure modes with detection checks, and a runbook with
nine procedures.

The phase 13 gate is **CLOSED for the local scope with a limitation**: every failure mode has a
detection check, a defined alert rule, a response and a runbook step; the service now exposes
automated probes, metrics, access logs and versioned alert definitions, but no Prometheus,
Alertmanager, dashboard, metrics backend or paging path is deployed, so no rule is evaluated or
routed. The original phase 13 record was manual-only; the post-closure implementation closes
`IMP-001` (metrics), `IMP-003` (readiness), `IMP-004` (alert definitions) and `IMP-005` (access
log) under `REQ-OBS-001`/`REQ-OBS-002`.

The implementation is recorded in `blueprint/changes/observability-improvements.md`; the
original phase 13 record is `blueprint/changes/p13-observability.md`. The current review is
`blueprint/reviews/observability-improvements.md`; the original self-review is
`blueprint/reviews/observability-closure.md`. Neither claims an independent approval. The gate
is tracked in `blueprint/gates.md` (P-13).
