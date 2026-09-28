# ADR-008: Static route registration at server bootstrap

## Status
Accepted

## Context
REQ-HELLO-001 NFR-02 forbids new runtime dependencies; the endpoint must reuse the project's existing HTTP stack. Analysis decision D4 left open how and when `/api/hello` is registered. The Blueprint itself is tool- and technology-agnostic (ADR-001, ADR-002), so the architecture fixes the lifecycle and constraints but does not bind the framework; the exact binding is a Design decision.

## Options considered
1. Static registration once during server bootstrap (startup), as part of application wiring.
2. Dynamic, reloadable route table (routes added/removed at runtime).
3. External API gateway exposing the endpoint instead of the application routing it.

## Decision
Option 1: `/api/hello` is registered once, statically, during server startup. No dynamic reload and no external gateway. The concrete framework binding is resolved in Design and constrained to the project's existing HTTP stack (NFR-02), consistent with Blueprint ADR-001/002. The decision is deliberately minimal to match the single-endpoint scope.

## Consequences
### Positive
- Deterministic, predictable behavior; the route exists for the whole process lifetime.
- Simple wiring (one line of registration) consistent with the small scope.
- No extra infrastructure; the endpoint remains inside the existing application (system boundary).

### Negative / trade-offs
- Adding a new route requires a code change and redeploy (acceptable for this scope).
- No centralized gateway features (cross-cutting tracing, route versioning) are gained — out of scope for REQ-HELLO-001.
- If the project later introduces many routes, this decision should be revisited.

## Date
2026-09-22