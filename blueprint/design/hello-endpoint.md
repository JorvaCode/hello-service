# Design: REQ-HELLO-001 — Endpoint HTTP GET /api/hello

## Purpose
Translates the architecture (`blueprint/architecture/hello-endpoint.md`) into an implementation-ready design. It introduces no new architectural decision: every technical choice is either derived from ADR-005..ADR-008 or explicitly marked as a framework-dependent decision to be resolved in Implementation without architecture impact.

## 1. Components / classes
| Component | Kind | Responsibility |
|---|---|---|
| `HelloHandler` | Handler (HTTP) | Produces the `Hello, World!` message and returns HTTP 200 with `text/plain`. No method logic (ADR-007). |
| `HelloRoute` (or equivalent registration) | Registration | Mounts/registers the route `/api/hello` as GET-only during server bootstrap (ADR-008). |
| HTTP Server | Infrastructure | Owns the listener; provided by the existing HTTP stack (NFR-02). |

Notes:
- The Router is the existing stack's routing mechanism, not a new class. The design does not create a router abstraction.
- No domain model, repository, service class or DTO is needed: the endpoint is stateless and has no data model (FR-05).

## 2. Responsibility per component
- `HelloHandler`: builds and returns the welcome message. It must not know about HTTP method handling; a non-GET request must never reach it (ADR-007). It must not call any external service (FR-05).
- Route registration: binds path + method + handler once, at startup (ADR-008). Registration is the only place where the path, the GET restriction and the handler are wired.
- HTTP Server: transport concern only (accept, parse, dispatch, respond). Reuses the existing stack; no new dependency (NFR-02).

## 3. Endpoint and final HTTP contract
| Element | Value | Source |
|---|---|---|
| Method | `GET` | FR-02 |
| Path | `/api/hello` | FR-01 |
| Success status | `200 OK` | FR-03 |
| Content-Type | `text/plain; charset=utf-8` | ADR-005 |
| Success body | `Hello, World!` | ADR-006 |
| Unsupported method | `405 Method Not Allowed`, header `Allow: GET` | ADR-007, AC-3 |
| Required headers | None | analysis |
| Query params / body input | None | analysis |

## 4. HTTP method and routing
- Route is declared GET-only at the routing layer. Non-GET requests to `/api/hello` are rejected by the router with `405` (ADR-007) and never reach the handler.
- **Framework-dependent note:** whether the existing stack expresses "GET-only route" through (a) a dedicated route method, (b) a method+path matcher, or (c) an automatic `Allow` header assembly depends on the concrete HTTP stack chosen in Implementation. If the stack cannot enforce methods at routing level, Implementation must add a small guard inside the handler that returns 405 before the success path (documented fallback in ADR-007). This is the only authorized deviation point.

## 5. Request / response
Request: `GET /api/hello` — no body, no query string, no auth headers, no payload (analysis "Inputs and outputs").

Response (success):
- Status: `200`
- Headers: `Content-Type: text/plain; charset=utf-8`; optionally `Content-Length` of the body as computed by the stack.
- Body: `Hello, World!`

Response (method not allowed):
- Status: `405`
- Headers: `Allow: GET`
- Body: empty or a short plain-text message per stack convention; body content is not part of the contract (AC-3 only constrains the status).

## 6. Handling of unsupported methods
- Primary path: routing layer (ADR-007). The router returns 405 with `Allow: GET`.
- Fallback (only if the framework lacks routing-level enforcement): a guard at the start of `HelloHandler` that checks the method and returns 405; it must be documented in code pointing to ADR-007.
- The success path (200/`Hello, World!`) is reachable solely via `GET /api/hello`.

## 7. Step-by-step execution flow
1. The HTTP Server receives a request to `/api/hello`.
2. The routing layer matches the path `/api/hello` and the registered methods.
3a. If the method is `GET`: the router dispatches to `HelloHandler`; it emits status 200, `Content-Type: text/plain; charset=utf-8`, body `Hello, World!`.
3b. If the method is not `GET` and the stack enforces methods: the router returns `405` + `Allow: GET`; the handler is not invoked.
3c. If the method is not `GET` and the stack does not enforce methods (fallback): the guard returns 405 before the success path.
4. The server writes the response to the client.
There is no database access, message queue, or outbound call at any step (FR-05).

## 8. Approximate package / directory structure
```
<application-root>/
  src/
    hello/
      hello-handler.<ext>      # HelloHandler
      hello-route.<ext>        # route registration (path + GET + handler)
    main|server/bootstrap.<ext> # startup; invokes hello-route registration (ADR-008)
  test/  (or alongside sources, per stack convention)
    hello/
      hello-handler.test.<ext> # unit tests (see §10)
      hello-endpoint.test.<ext> # route/contract tests (see §10)
```
- Package/extension layout follows the host application's existing conventions (ADR-001 agnosticism). The `hello` package groups the two components to make the feature traceable.

## 9. Error handling
- **Unsupported method:** handled by the routing layer (405 + `Allow: GET`), or by the authorized guard fallback. No internal error involved.
- **Startup/registration failure:** if route registration fails at bootstrap, the application must fail fast (startup error), since a service without `/api/hello` is not satisfying REQ-HELLO-001. Mechanism is the stack's standard startup error handling (framework-dependent; no new logging/metrics infrastructure per scope).
- **Out-of-scope errors:** no authentication, no payload, no persistence, so no 4xx/5xx cases beyond 405 are expected in this feature. A 404 for other paths comes from the stack and is out of scope.
- **No transactional boundaries apply:** the endpoint performs no writes; there is nothing to commit/roll back (04-design.md "transactional boundaries" → not applicable, justified).

## 10. Tests to be implemented
Test strategy (04-design.md): small unit layer + a thin contract/route layer. No mocks of external systems needed (no external dependencies).
- T-01 (unit, handler): invoking `HelloHandler` yields an object with status 200 and body `Hello, World!` and content type `text/plain` (FR-03, FR-04, NFR-01, ADR-005/006).
- T-02 (unit, handler — fallback guard): if the guard fallback is used, a non-GET invocation returns 405 before the success path (AC-3, ADR-007 fallback). Skipped if the routing layer enforces methods.
- T-03 (route/contract): `GET /api/hello` against a running instance returns 200 with body exactly `Hello, World!` (FR-01..FR-04, AC-1, AC-2).
- T-04 (route/contract): a non-GET method on `/api/hello` returns 405 with header `Allow: GET` (FR-02, AC-3, ADR-007).
- T-05 (route/contract smoke / CI): after deployment, `GET /api/hello` returns 200 + `Hello, World!` to validate the pipeline (objective of REQ-HELLO-001; ties to the CI smoke-test actor).
- T-06 (route/contract): an unknown path (e.g. `/api/nope`) returns 404 (guards against route mis-registration; extends the 404 stack behaviour).

## 11. Traceability
| Design element | Maps to |
|---|---|
| `HelloHandler` + registration | Architecture components (Hello Handler, Router), ADR-007/008 |
| GET-only route + 405 (primary) | FR-02, AC-3, ADR-007 |
| 405 guard (fallback only) | ADR-007 (documented fallback, not a new decision) |
| Path `/api/hello` | FR-01 |
| Status 200 | FR-03, AC-1 |
| Body `Hello, World!` | FR-04, AC-2, ADR-006 |
| `text/plain; charset=utf-8` | NFR-01, ADR-005 |
| No external calls / stateless | FR-05 |
| No new dependencies | NFR-02, ADR-008 |
| Static bootstrap registration | ADR-008 |
| Tests T-01..T-06 | FR-01..FR-05, NFR-01, AC-1..AC-3 |
| Start from REQ-HELLO-001 | requirements/hello-endpoint.md; analysis/hello-endpoint.md; architecture/hello-endpoint.md |

Framework-dependent points (explicit, to be resolved in Implementation without architecture change):
- mechanism for GET-only route registration (routing API of the stack);
- `Allow` header assembly (stack or manual);
- test runner / file naming conventions (host project);
- startup error surface (stack convention).