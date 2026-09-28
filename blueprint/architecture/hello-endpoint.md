# Architecture: REQ-HELLO-001 — Endpoint HTTP GET /api/hello

## Purpose
Defines the structural solution for `REQ-HELLO-001`: a single HTTP endpoint `GET /api/hello`. It is deliberately minimal: one endpoint, no new dependencies, no external services, and aligned with the project's existing HTTP stack.

## System boundary
The endpoint is part of the existing application (same process and deployment unit). There is no separate service, no external gateway and no persistence. Only the HTTP contract is externalized: `GET /api/hello`.

## Components
| Component | Responsibility |
|---|---|
| HTTP Client (consumer) | Sends `GET /api/hello` (curl, browser, monitoring probe, CI smoke test). |
| HTTP Server (hosting) | Owns the network listener, accepts connections and delivers responses. |
| Router | Matches path `/api/hello` and HTTP method; enforces that only `GET` reaches the handler. |
| Hello Handler | Builds the welcome message and returns the HTTP 200 response. No external calls. |
| CI smoke test (probe) | Consumes the endpoint after deployment to validate the delivery pipeline. Not a runtime component. |

## Component interactions
```
HTTP Client → HTTP Server → Router → Hello Handler → HTTP 200 + "Hello, World!"
        └──────────── (response back through the same path)
```
- The Router is the single entry point: only a request matching path `/api/hello` and method `GET` reaches the Hello Handler (ADR-007).
- The Hello Handler owns the message content (ADR-006) and the response format (ADR-005).
- There are no outbound dependencies from the endpoint at runtime (FR-05).

## API / HTTP contract
| Element | Value |
|---|---|
| Method | `GET` |
| Path | `/api/hello` |
| Success status | `200 OK` |
| Content-Type | `text/plain; charset=utf-8` (ADR-005) |
| Success body | `Hello, World!` (ADR-006) |
| Unsupported method | `405 Method Not Allowed` with `Allow: GET` (ADR-007) |
| Auth / headers / query / payload | None required |
| Latency expectation | In-process, no I/O besides the response (FR-05) |

## Request flow: GET /api/hello
1. Client sends `GET /api/hello`.
2. The HTTP Server accepts the connection.
3. The Router matches path `/api/hello` and method `GET`.
4. The Hello Handler produces the message `Hello, World!`.
5. The HTTP Server responds `200 OK`, `Content-Type: text/plain; charset=utf-8`, body `Hello, World!`.
6. If the method is not `GET`, the Router emits `405 Method Not Allowed` with `Allow: GET`; the handler is never invoked.

## Technology decisions
| Decision | Choice | Reference |
|---|---|---|
| Response format | Plain text, `text/plain` | ADR-005 (D1) |
| Welcome message | `Hello, World!` | ADR-006 (D2) |
| 405 enforcement | Routing layer (framework-level) | ADR-007 (D3) |
| Route lifecycle | Static registration at server bootstrap | ADR-008 (D4) |
| Stack | Reuse of the project's existing HTTP stack; no new dependencies (NFR-02) | ADR-008, ADR-001/002 |
| Framework binding | Delegated to Design, constrained to the existing stack | — |

## Discarded options
| Option | Why discarded |
|---|---|
| JSON response body | No structured fields to convey (NFR-01); serialization and content negotiation add cost with no benefit for a greeting. Revisitable if the API later standardizes on JSON (ADR-005). |
| Separate microservice for the endpoint | Overkill: one stateless route adds deployment, observability and ops surface. Kept inside the existing application. |
| OpenAPI contract / SDK generation | Unjustified process weight for a single endpoint; would fight the "deliberately simple" scope. |
| External data source for the message (i18n, DB) | Message is static; persistence/i18n are explicitly out of scope (REQ-HELLO-001). |
| API gateway in front of the endpoint | No cross-cutting needs (auth, routing, rate limiting) within scope (ADR-008). |

## Security and non-functional notes
- No authentication or authorization is applied; both are out of scope.
- The endpoint is stateless, so it scales and deploys with the host application with no special handling.
- The endpoint itself serves as the connectivity probe for smoke tests; dedicated metrics/observability infrastructure is out of scope per REQ-HELLO-001.

## Traceability
| Requirement | Satisfied by |
|---|---|
| FR-01 path /api/hello | Route `/api/hello` in the Router and HTTP contract |
| FR-02 GET only | GET-only registration and routing-layer method enforcement (ADR-007) |
| FR-03 status 200 | Hello Handler success path (HTTP contract) |
| FR-04 body contains "Hello" | Message `Hello, World!` (ADR-006) |
| FR-05 no external dependencies | In-process handler, no outbound calls (component interactions) |
| NFR-01 small readable body | Plain-text single-message body (ADR-005) |
| NFR-02 no new runtime dependencies | Reuse of existing HTTP stack (ADR-008) |
| AC-1 200 + Hello | Request flow steps 1–5 |
| AC-2 body includes "Hello" | ADR-006 message choice |
| AC-3 non-GET → non-200 | Router 405 path (ADR-007) |

Analysis decisions D1–D4 (blueprint/analysis/hello-endpoint.md) are resolved by ADR-005..ADR-008 in `blueprint/architecture/adr/`.

## ADR registries
- ADR-005 — Plain-text response format (D1)
- ADR-006 — "Hello, World!" message (D2)
- ADR-007 — Method enforcement at the routing layer (D3)
- ADR-008 — Static route registration at server bootstrap (D4)