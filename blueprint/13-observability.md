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
