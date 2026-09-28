# ADR-005: Plain-text response format for GET /api/hello

## Status
Accepted

## Context
REQ-HELLO-001 only constrains the response body to contain the text "Hello" (FR-04) and to be small and readable (NFR-01). Analysis decision D1 left the response format open between plain text and JSON. The endpoint has no structured fields to convey.

## Options considered
1. Plain text (`text/plain; charset=utf-8`) — a single human-readable message, no serialization.
2. JSON (`application/json`) — structured payload, e.g. `{"message": "Hello, World!"}`.
3. HTML fragment — serving a rendered fragment as a response.

## Decision
Option 1: plain text with `Content-Type: text/plain; charset=utf-8`. The endpoint returns a single greeting message; JSON serialization and content negotiation add cost and complexity without conveying structured data. Consistent with NFR-01 and the deliberately simple scope of REQ-HELLO-001.

## Consequences
### Positive
- No serialization logic, no content-type negotiation complexity.
- Maximally readable body (NFR-01) and an exact string match for smoke tests.
- No additional dependencies (NFR-02).

### Negative / trade-offs
- Not machine-extensible: if the API later standardizes on JSON across endpoints, this endpoint must change (via a new ADR).
- No place for future structured fields (e.g. version) without switching formats.

## Date
2026-09-22