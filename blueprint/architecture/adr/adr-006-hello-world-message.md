# ADR-006: Response message "Hello, World!" for GET /api/hello

## Status
Accepted

## Context
REQ-HELLO-001 requires the body to contain the text "Hello" (FR-04) but does not prescribe the exact wording. Analysis decision D2 left the message content open. The messages differ in traceability strength for smoke tests and in coupling to branding.

## Options considered
1. `Hello, World!` — idiomatic, canonical greeting.
2. `Hello` — the bare minimum satisfying FR-04.
3. `Welcome to &lt;project-name&gt;` — brand-aware, contextual greeting.
4. Localized message set (i18n) selected by the client.

## Decision
Option 1: the response body is exactly `Hello, World!`. It is immediately recognizable, unambiguous and enables a deterministic exact-match assertion in the CI smoke test. Tailoring to the project name is deliberately avoided because it couples the HTTP contract to branding that may change (ADR-001 agnosticism).

## Consequences
### Positive
- Deterministic body: the smoke test can assert an exact string, satisfying FR-04 and AC-2 directly.
- No branding or i18n dependencies (i18n is out of scope for REQ-HELLO-001).
- Trivial to understand for anyone probing the service.

### Negative / trade-offs
- Not localizable out of the box; internationalization would require revisiting this ADR.
- The message is a static literal; if it ever becomes data-driven, the contract reference must be updated.

## Date
2026-09-22