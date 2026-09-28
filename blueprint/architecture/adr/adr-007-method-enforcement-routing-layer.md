# ADR-007: Enforce HTTP method at the routing layer

## Status
Accepted

## Context
REQ-HELLO-001 FR-02 requires the endpoint to respond only to `GET`, and AC-3 requires that any other method returns a non-200 status. Analysis decision D3 left open how the 405 is produced: by the framework/router or by a guard inside the handler. The risk identified in the requirement is that unsupported methods silently returning 200 would hide routing misconfigurations.

## Options considered
1. Framework-level routing: register `/api/hello` as a GET-only route so the router emits `405 Method Not Allowed` with `Allow: GET`.
2. Catch-all handler for the path with an explicit method guard returning 405 when not GET.
3. Single handler accepting every method and performing the check manually at the start.

## Decision
Option 1: the route is registered as GET-only at the routing layer, so method enforcement is the framework's responsibility and the handler contains no method logic. This keeps the Hello Handler minimal and guarantees that unsupported methods never reach the success path (FR-03 cannot be accidentally returned). If the chosen framework cannot express GET-only route registration, Design will fall back to option 2 (a localized guard) — a documented fallback, not a silent deviation.

## Consequences
### Positive
- Correct 405 behavior without hand-written method handling (AC-3).
- Routing misconfiguration is visible: a non-GET call can never yield 200.
- Smaller handler surface, easier to review and test.

### Negative / trade-offs
- Depends on framework capabilities; if they are absent, the guard fallback is required and must be documented in Design.
- Only routes with framework support benefit automatically; other future routes must apply the same convention.

## Date
2026-09-22