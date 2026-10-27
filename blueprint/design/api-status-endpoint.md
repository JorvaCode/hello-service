# Design: REQ-STATUS-001 — `GET /api/status` liveness endpoint

## Status
Approved by the requester on 2026-10-22 for phase 04; implemented in phase 05. The NFR-06 scope
addendum was approved on 2026-10-23; it changes no code, test or HTTP behaviour. This document is
the phase-04 design artifact and fills the content `templates/change.md` requires. It translates
`blueprint/architecture/api-status-endpoint.md` into an implementation-ready design. Every element
names the requirement it satisfies.

## Requirement
- `REQ-STATUS-001` — `blueprint/requirements/api-status-endpoint.md`, analysed in
  `blueprint/analisys/api-status-endpoint.md`.
- Functional: FR-01 … FR-06. Non-functional: NFR-01 … NFR-06. Acceptance: AC-01 … AC-07.
- The change alters **structure** (one new controller class), **interface** (the HTTP contract
  fixed in phase 01), and **data** (a response object with no persistence). It alters
  **no dependency**: `build.gradle.kts` is unchanged.

## Design
### Design obligations
The implementation shall:
- add exactly one production class, `StatusHandler`, with the mapping and body stated below;
- leave `HelloHandler`, `RequestIdFilter`, `HelloApplication` and `build.gradle.kts` unchanged;
- add the two test classes in the same change as the production code;
- perform no I/O, read no configuration and introduce no collaborator on the request path.

These obligations derive from FR-01 … FR-06, NFR-01, NFR-02 and NFR-05.

### Components and files
| File | Kind | Change | Basis |
| --- | --- | --- | --- |
| `src/main/java/com/example/hello/StatusHandler.java` | Controller | New; package-private class | FR-01 … FR-06 |
| `src/test/java/com/example/hello/StatusHandlerTest.java` | Unit test | New | AC-04, NFR-03 |
| `src/test/java/com/example/hello/StatusEndpointTest.java` | Web-slice test | New | AC-01 … AC-03, AC-05, NFR-03 |
| `src/main/java/com/example/hello/HelloHandler.java` | Controller | Unchanged | NFR-05 |
| `src/main/java/com/example/hello/RequestIdFilter.java` | Filter | Unchanged; inherited by the new route | NFR-05 |
| `src/main/java/com/example/hello/HelloApplication.java` | Bootstrap | Unchanged; component scan already covers the package | Existing convention |
| `build.gradle.kts` | Build | Unchanged | NFR-01 |

No service class, repository, DTO module, configuration class or custom router is added. The
existing Spring MVC router and Jackson converter are reused.

### Handler interface
```java
@RestController
class StatusHandler {

    static final String STATUS_MEMBER = "status";
    static final String STATUS_UP = "UP";

    @GetMapping(path = "/api/status", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of(STATUS_MEMBER, STATUS_UP));
    }
}
```
- Class and method are package-private, matching `HelloHandler`.
- The mapping's `produces` and Spring MVC content negotiation set `Content-Type: application/json`;
  the handler sets no header (D1 addendum).
- No Java interface is extracted: there is one implementation, no second implementation, no test
  double and no external substitutability requirement. Extracting one is the anti-pattern named
  in `standards/code-design.md` §4.
- The handler performs no I/O, reads no configuration and has no collaborator (FR-05, NFR-02).

### HTTP contract
| Element | Value | Basis |
| --- | --- | --- |
| Method and path | `GET /api/status` | FR-01, FR-02 |
| Success status | `200 OK` | FR-02, AC-01 |
| Media type | `application/json` (compatible) | FR-03, AC-02 |
| Body | Exactly `{"status":"UP"}` in the `200 OK` response | FR-04, AC-03 |
| `HEAD` | Permitted with GET semantics, no body | FR-06 |
| `OPTIONS` | Permitted; Spring MVC advertises the methods | FR-06 |
| `POST`, `PUT`, `PATCH`, `DELETE` | `405` with `Allow` containing `GET`; handler not invoked; the framework-generated error body is not part of this contract and is outside NFR-06 | FR-06; AC-05 covers `POST`, `PUT`, `DELETE`; D6 |
| Input | No query parameters, no request body, no authentication | Phase 01 scope |
| `X-Request-ID` | Added by the inherited `RequestIdFilter`; not part of FR-04 | NFR-05 |
| Incompatible `Accept` | `406` from Spring MVC; other media types out of scope | Phase 01 scope |

### Data model
The response is a JSON object with exactly one member: `status`, a string with the value `UP`.
The in-memory representation is `Map<String, String>` built with `Map.of(STATUS_MEMBER, STATUS_UP)`.
There is no domain model, no persistence, no cache and no shared mutable state. The constants are
package-private so the unit test names the same key and value as the production code.

### Error handling and validation
- **Unsupported method:** Spring MVC returns `405` with `Allow` containing `GET` (FR-06). The
  handler is not invoked. No guard in the handler is needed and none is added. The framework
  generates the error body; it is outside NFR-06 and no error body is specified by this contract.
- **Incompatible `Accept`:** Spring MVC returns `406`; content negotiation beyond
  `application/json` is out of scope.
- **Unknown path:** Spring MVC returns `404`; other routes are out of scope.
- **Unexpected server error:** no catch block is added; the framework's default `500` handling
  applies and no error body is part of the contract.
- **Validation:** none. The endpoint reads no input, so there is no validation surface.
- `HEAD` and `OPTIONS` are handled by Spring MVC; neither is asserted as `405` (FR-06).

### Transactional boundaries
None. The request path performs no write and no persistence access; there is nothing to commit or
roll back. No `@Transactional` annotation is added.

### Patterns
**No pattern is applied.** The change has one controller, one constant body and one caller; there
is no recurring problem for a pattern to solve. The four required items are recorded as an explicit
absence:

- **Problem:** none. There is no variation, no second implementation and no external integration.
- **Evidence:** one endpoint and one response; no test double is needed, and no committed
  requirement produces a second case.
- **Cost of the simpler alternative:** the simpler alternative (a plain controller returning a
  map) already satisfies every FR and NFR; a pattern would add indirection with no removed problem.
- **Scope:** not applicable; no pattern is adopted.

Rejected patterns: a response DTO (see D1), a service layer or repository (no logic or data to
hold), a strategy or factory (no varying behaviour), a custom health indicator (Actuator is out of
scope). Per `standards/code-design.md` §4 and §6, this absence is a decision, not a gap.

### ADR
| Option | Assessment |
| --- | --- |
| New ADR (`ADR-014`) | Rejected. None of the significance criteria (a)–(e) is met; the ADR would record a non-significant local decision, which `standards/terms.md` excludes from the ADR requirement. |
| Update an existing ADR | Rejected. No existing ADR covers this endpoint. The `ADR-005`…`ADR-008` citations in the `hello-endpoint` artifacts are stale and point to unrelated decisions, so updating them would propagate the obsolete references rather than resolve them. |
| **Design documentation only (chosen)** | The significance test fails, so the architecture records the structure and this document records D1–D3. No decision record is required, and none is added. |

**No ADR is required (D4, approved 2026-10-22).** The significance table in
`blueprint/architecture/api-status-endpoint.md` shows that none of criteria (a)–(e) in
`standards/terms.md` is met: the public contract is fixed by phase 01, the structural change is one
controller in the existing application, no dependency or platform changes, no quality threshold
changes, and the architecture does not alter the consumer-visible contract. D1–D3 below are local
implementation choices, which `standards/terms.md` explicitly excludes from the ADR requirement.

No existing ADR is updated. `ADR-006` (code-design criteria), `ADR-010` (views and scenarios) and
`ADR-013` (traceability) govern this design. The `ADR-005`…`ADR-008` citations in `HelloHandler`
and the `hello-endpoint` artifacts are stale and are not reused; correcting them is separate
documentation debt. Should the requester later decide that the new endpoint is significant under (a) or
(e), the next free identifier is `ADR-014`.

## D1 — Response representation
| Option | Assessment |
| --- | --- |
| **`Map<String, String>` via `Map.of` (chosen)** | Smallest change. Jackson serializes it to the exact body; no new type or file; the key and value are named by package-private constants. The web-slice test pins the exact bytes. |
| `record StatusResponse(String status)` | Compile-time key safety and an explicit type, but adds a type and a file for a one-member constant with one caller. The simpler map already satisfies every requirement, so the record has no problem to solve under `standards/code-design.md`. |
| `ResponseEntity<String>` with a literal JSON body | Guarantees the bytes but bypasses Jackson and duplicates serialization in a string; escaping and future fields become manual. Rejected as a hidden second serialization path. |
| Spring Boot Actuator / `/actuator/health` | Out of scope in phase 01; introduces a dependency (NFR-01) and a different contract. Rejected without re-opening the requirement. |

**Decision (approved 2026-10-22):** `Map<String, String>` with `STATUS_MEMBER` and `STATUS_UP` constants.

**D1 addendum — Content-Type (approved 2026-10-22).** The explicit
`.contentType(MediaType.APPLICATION_JSON)` call is redundant: `produces` on the mapping declares the
producible media type and Spring MVC's content negotiation sets it for the selected Jackson
converter. The handler therefore sets no `Content-Type` header. `ResponseEntity` remains because it
carries the `200` status that the AC-04 unit test asserts; the media type is verified at the web
slice (AC-02), which is where negotiation happens. The planned unit-test content-type assertion is
removed; no acceptance criterion loses coverage.

## D2 — Handler identity
| Option | Assessment |
| --- | --- |
| **`StatusHandler` in `com.example.hello` (chosen)** | Mirrors `HelloHandler`; the existing test names use `Endpoint` for the slice, so `StatusHandler` keeps production/test naming consistent. |
| `StatusController` | A Spring convention, but the project's established production name is `Handler`; mixing suffixes would make the package inconsistent (NFR-05). |
| `HealthHandler` | Suggests readiness and health aggregation, which the requirement excludes; it would mislead consumers. |
| `StatusEndpoint` | Collides with the test-slice naming convention (`StatusEndpointTest`) and blurs production and test roles. |

**Decision (approved 2026-10-22):** `StatusHandler`, file `src/main/java/com/example/hello/StatusHandler.java`,
package-private class, method `status()`, mapping
`@GetMapping(path = "/api/status", produces = MediaType.APPLICATION_JSON_VALUE)`.

## D3 — Test structure
| Option | Assessment |
| --- | --- |
| **Handler unit test + `@WebMvcTest` slice (chosen)** | Mirrors `HelloHandlerTest` + `HelloEndpointTest`. The unit test proves the no-collaborator decision (AC-04); the slice exercises routing, media type, exact body and method handling (AC-01, AC-02, AC-03, AC-05) without a server. |
| Web-slice test only | Covers the HTTP contract but not AC-04, which explicitly requires a handler unit test constructed without collaborators. Insufficient. |
| Full `@SpringBootTest` + random port | Starts the whole application and adds cost without new coverage: there is no external collaborator or data store, and the slice already exercises the same MVC router. Rejected as disproportionate. |
| Manual or external end-to-end test | Not repeatable in CI and not needed for a stateless constant response. Rejected. |

**Decision (approved 2026-10-22):** two test classes:
- `StatusHandlerTest` — unit, no Spring context: `returnsStatus200`, `returnsUpStatusBody`.
- `StatusEndpointTest` — `@WebMvcTest(StatusHandler.class)` with MockMvc:
  `getReturns200WithJsonContentType`, `getReturnsExactStatusBody`, the parameterised
  `unsupportedMethodsReturn405WithAllowGet` over `POST`, `PUT` and `DELETE`, and the separate
  `patchReturns405WithAllowGet`; `PATCH` is additive FR-06 coverage and does not alter AC-05.

The slice does not import `RequestIdFilter` explicitly and does not assert `X-Request-ID`: the
status contract does not require the header. If the filter is auto-included by the slice, it is
harmless. The content-type assertion lives in the slice, not the unit test, because content
negotiation is an MVC concern (D1 addendum). `HEAD` and `OPTIONS` are not asserted because FR-06
permits rather than requires their handling. `HelloEndpointTest` is not modified and remains scoped
to `HelloHandler`.

## Implementation
Increments, not executed in this phase. Each increment names the requirement it realises.
1. Add `StatusHandler` with the mapping, constants and `ResponseEntity` body (FR-01 … FR-06).
2. Add `StatusHandlerTest` and `StatusEndpointTest` (AC-01 … AC-05, NFR-03).
3. Run the verification commands and record the result (AC-06, AC-07).

No configuration, dependency or workflow file changes. No production code is modified in this
phase.

## Tests
The levels are derived from the quality attribute scenarios in the architecture: QAS-02 (no I/O)
drives the unit level; QAS-01 and QAS-03 (availability and disclosure) drive the web-slice level;
QAS-04, QAS-05 and QAS-06 (dependency, testability, compatibility) drive the build checks.

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| AC-01 | Web slice | `getReturns200WithJsonContentType`: `GET /api/status` → `status().isOk()`; review confirms no `@Profile`/`@ConditionalOn*` on the handler. |
| AC-02 | Web slice | Same test: `content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)`. |
| AC-03 | Web slice | `getReturnsExactStatusBody`: raw body equals `{"status":"UP"}`; a JSON parse with the slice's `ObjectMapper` asserts member count `1` and `status == "UP"`. |
| AC-04 | Unit | `returnsStatus200` and `returnsUpStatusBody` on a handler built with no collaborators; review confirms no I/O on the request path. |
| AC-05 | Web slice | `unsupportedMethodsReturn405WithAllowGet`: parameterised over `POST`, `PUT`, `DELETE`; asserts `405` and `Allow` contains `GET`. |
| AC-06 | Build | `./gradlew clean test` is green; `git diff build.gradle.kts` shows no added dependency; `./gradlew dependencies --configuration runtimeClasspath` matches the baseline. |
| AC-07 | Build | The 9 pre-existing tests pass unchanged; `HelloHandler` and `HelloEndpointTest` are not edited; review against their conventions. |
| FR-05, NFR-02 | Unit | `StatusHandlerTest` constructs the handler directly and passes with no external service available. |
| NFR-06 | Web slice | AC-03's member-count assertion verifies the `200 OK` body; review confirms no diagnostic data is added to that response. Framework-generated error bodies are outside NFR-06 and are not asserted. |

The implementation's `StatusEndpointTest` also includes a separate `patchReturns405WithAllowGet`
test as additive FR-06 coverage; AC-05 still requires only `POST`, `PUT` and `DELETE`.

No mocks, fixtures, database or external server are used. Tests are written in the same change as
the code they cover, per the Definition of Done. No test is deleted.

## Risks carried
The design introduces no new product risk beyond the requirement's risk register, and one
verification risk. The items that remain visible are:
- **Unauthenticated exposure.** The endpoint has no authentication or authorization; exposure
  beyond the deployment boundary is an abuse surface. It is an assumption in `REQ-STATUS-001` and
  outside this design's scope.
- **Rigid exact body.** Adding a field to `{"status":"UP"}` is a breaking change and needs a new
  version of `REQ-STATUS-001`, not an in-place edit. FR-04 and AC-03 pin the body deliberately.
- **Permitted, unasserted `HEAD`/`OPTIONS`.** FR-06 permits their handling, so the tests do not
  assert it; a framework change could alter their behaviour without failing AC-05. This is the
  requirement's trade-off, not a gap in the design.
- **Framework-generated error bodies.** The `405` error body is outside NFR-06 under the approved
  D6 scope and is not pinned by a test; a framework change could alter it while FR-06/AC-05 remain
  satisfied. This is the accepted residual observation from Phase 06 finding F-01, not a defect
  under the clarified scope.
- **Slice versus full application.** The web slice does not start the full application; the handler
  is a `@RestController` in the scanned `com.example.hello` package, matching `HelloHandler`, and
  AC-01's review confirms there is no conditional registration.

## Security
The change adds an endpoint and a new request path, so the review is stated rather
than skipped: it adds no dependency, no secret and no request payload. Authentication and
authorization are out of scope by requirement; the endpoint is unauthenticated inside the
deployment boundary. The `200 OK` response exposes only the constant `UP` (NFR-06).
Framework-generated error bodies are outside NFR-06 and preserve the approved error contract.
`RequestIdFilter` logs no payload. Residual risk: exposure beyond the deployment boundary is an
abuse surface, recorded as a requirement assumption and out of scope here.

## Deployment
None. The route ships in the existing artifact, image and port; no configuration property,
profile, secret, volume, network rule or workflow change is required (NFR-01, NFR-02).
Configuration and operational needs are therefore none.

## Rollback
Revert the commit that adds `StatusHandler` and the two test classes. No data migration, no
configuration restore and no dependency change are involved. After implementation, the revert path
is rehearsed by reverting the commit and confirming that the pre-existing suite is green on the
reverted tree; before implementation there is nothing to rehearse.

## Deletions
None. No test or artifact is removed.

## Traceability
| Design element | Requirement or criterion | Verifying test or check |
| --- | --- | --- |
| `StatusHandler` mapping | FR-01, FR-02 | `StatusEndpointTest.getReturns200WithJsonContentType` |
| `produces` + content negotiation (no explicit header) | FR-03, AC-02 | `getReturns200WithJsonContentType` |
| `STATUS_MEMBER` / `STATUS_UP` body | FR-04, AC-03 | `getReturnsExactStatusBody`; `returnsUpStatusBody` |
| No collaborators, no I/O | FR-05, NFR-02, AC-04 | `StatusHandlerTest`; review |
| Spring MVC method handling | FR-06; AC-05 covers `POST`, `PUT`, `DELETE` | `unsupportedMethodsReturn405WithAllowGet` |
| Unchanged `build.gradle.kts` | NFR-01, AC-06 | Dependency diff |
| JUnit 5 + `@WebMvcTest` + MockMvc | NFR-03, AC-06 | Test classes and build |
| Unconditional registration | NFR-04, AC-01 | Default-configuration slice; review |
| Existing package and test conventions | NFR-05, AC-07 | Unchanged hello tests; review |
| Constant-only `200 OK` response; framework error bodies outside scope (D6) | NFR-06, AC-03 | Member-count assertion; review; HTTP contract |
| D1 representation | FR-04, NFR-01 | This design; no dependency change |
| D1 addendum — no explicit `Content-Type` | FR-03, AC-02 | `getReturns200WithJsonContentType`; D1 addendum |
| D2 handler identity | NFR-05 | This design; package layout |
| D3 test structure | NFR-03, AC-01 … AC-05 | This design; test plan |
| D4 no ADR | `standards/terms.md` significance test | Architecture significance table |

## Review and approval
**Approved by the requester on 2026-10-22** for phase 04. The approval covers:

1. **D1** — response representation: `Map<String, String>` with `STATUS_MEMBER` and `STATUS_UP`;
   the explicit `Content-Type` is removed as redundant with `produces` and content negotiation
   (D1 addendum). No DTO and no dependency are added.
2. **D2** — handler identity: `StatusHandler` in `com.example.hello`, method `status()`.
3. **D3** — test structure: `StatusHandlerTest` + `StatusEndpointTest`; existing tests are
   preserved.
4. **D4** — no new ADR; no existing ADR updated. Should the endpoint later be considered
   significant under (a) or (e), `ADR-014` is the next free identifier.
5. **Scope** — no change to `REQ-STATUS-001` at that review and no implementation in this phase;
   the later NFR-06 scope addendum is item 7.
6. **Architecture** — the single view and the recorded stakeholder-register gap are accepted.
7. **NFR-06 scope addendum (2026-10-23).** NFR-06 applies only to the `200 OK` response body;
   framework-generated error responses are outside that restriction while FR-06/AC-05 remain in
   force. This is the Phase 06 finding F-01 and changes no code, test or HTTP behaviour.

**Approval record:** requester, 2026-10-22; NFR-06 scope addendum approved by the requester on
2026-10-23. Phase 04 closed with the original approval. Development was the next phase and was
completed in phase 05.
