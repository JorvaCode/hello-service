# Change: `CHG-OBS-001` — Phase 13 observability record

## Requirement

- `REQ-HELLO-001` — `GET /api/hello`.
- `REQ-STATUS-001` — `GET /api/status`.

The change does not add or modify a requirement. It records how the deployed `0.1.0` system
is observed, which failures are detectable, and how a responder acts on them.

## Design

Add `blueprint/observability/hello-service.md` with the operating context, instrumentation,
failure modes, manual detection checks, runbook and health-check criteria. The record uses
the existing structured logging, `RequestIdFilter` correlation and `/api/status` liveness
signal. It adds no metrics, traces, dashboards, automated alerts or readiness probe.

### Patterns

None. No code structure or abstraction is introduced. The runbook is an operating
procedure.

### ADR

None. The change records the existing observability decisions and does not meet the
significance criteria in `standards/terms.md`. The comments in `logback-spring.xml` and
`RequestIdFilter.java` are corrected to stop citing `ADR-011` (versioned distribution) for
observability; they now reference the observability record.

## Implementation

- Add `blueprint/observability/hello-service.md`.
- Add `blueprint/reviews/observability-closure.md`.
- Add this change record.
- Add a project record section to `blueprint/13-observability.md`.
- Add the P-13 row to `blueprint/gates.md`.
- Add `IMP-004` (automated alerting) and `IMP-005` (access logging) to
  `blueprint/improvements/continuous-improvements.md`, and update the closure scope table.
- Add an observability section, status row and documentation link to `README.md`.
- Correct the stale `ADR-011` citations in `src/main/resources/logback-spring.xml` and
  `src/main/java/com/example/hello/RequestIdFilter.java` (comments only; no behaviour
  change).

## Tests

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| Existing contracts remain green after the comment correction | Unit/integration | `.\gradlew.bat clean test --offline --no-daemon --console=plain` — `BUILD SUCCESSFUL`, 17 tests, 0 failures, 0 errors, 0 skipped on 2026-10-30T18:45:20Z |
| Liveness is observable | Smoke | `GET /api/status` -> `200`, `application/json`, `{"status":"UP"}` |
| Hello contract is observable | Smoke | `GET /api/hello` -> `200`, `text/plain;charset=UTF-8`, `Hello, World!` |
| Unknown path is observable | Smoke | `GET /api/nope` -> `404` |
| Unsupported method is observable | Smoke | `POST /api/hello` -> `405` with a warning log |
| Correlation works on an error path | Smoke | `POST /api/hello` with `X-Request-ID: p13-correlation-test` -> `405`; response header echoed; log line carries `[requestId=p13-correlation-test]` |
| Startup is observable | Smoke | Container logs contain `Starting HelloApplication v0.1.0`, `Tomcat started on port 8080` and `Started HelloApplication` |
| Rollback path remains documented | Inspection | `RB-08` points to the rollback target and the exercised rehearsal in `blueprint/delivery/release-0.1.0.md` |

No automated alert test exists because no automated alerting exists. This is recorded as a
limitation, not hidden.

## Security

No dependency, endpoint, secret or input path is added. No request payload is logged. The
`X-Request-ID` log-injection residual risk recorded in
`blueprint/reviews/api-status-endpoint.md` is unchanged and remains accepted. The runbook
uses no credentials and is scoped to the local environment. The container continues to run
as `appuser` (UID 10001, non-root).

## Deployment

Local only: single-node Podman on Windows/WSL, port `18080` -> `8080`, image
`localhost/hello-service:0.1.0`. The runbook rehearsal used the promoted image. Remote
publication to Quay remains postponed and is not claimed.

## Rollback

The runbook's `RB-08` returns to `localhost/hello-service:0.0.1-SNAPSHOT` from commit
`f2b03e5`. The target identity and the exercised rehearsal are in
`blueprint/delivery/release-0.1.0.md`. This phase did not re-exercise the rollback; it
references the existing evidence.

## Deletions

None.
