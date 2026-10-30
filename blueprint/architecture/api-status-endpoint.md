# Architecture: REQ-STATUS-001 — `GET /api/status` liveness endpoint

## Purpose
Defines the structural solution for `REQ-STATUS-001` (`blueprint/requirements/api-status-endpoint.md`),
analysed in `blueprint/analisys/api-status-endpoint.md`. The endpoint is a second stateless route
in the existing Spring Boot application. This description introduces no new component type, no new
dependency, no data store and no deployment unit.

**Status:** approved by the requester on 2026-10-22 for phase 03 (see **Review and approval**);
the endpoint was implemented in phase 05. The NFR-06 scope addendum was approved on 2026-10-23;
it does not change the HTTP contract or the implementation.

## Stakeholders and concerns
| Stakeholder | Concern | Requirement |
| --- | --- | --- |
| Operator or monitoring consumer | A stable liveness signal that exists whenever the application is serving HTTP | FR-01, FR-02, NFR-04 |
| API consumer | An exact, predictable JSON contract: path, media type, body and method handling | FR-03, FR-04, FR-06 |
| Maintainer | The change fits the existing package, handler and test conventions and does not disturb `/api/hello` | NFR-03, NFR-05 |
| Release or operations engineer | No new runtime dependency, configuration or deployment change | NFR-01, NFR-02 |
| Security owner | No information disclosure in the `200 OK` body beyond the constant status | NFR-06 |

Known gap: this repository has no stakeholder register. The roles above are the ones the
requirement and its analysis name; the absence of a formal register is recorded rather than
guessed, as `blueprint/03-architecture.md` permits.

## System boundary
The endpoint is part of the existing application: same process, same embedded Tomcat listener on
port 8080 and same deployment artifact as `GET /api/hello`. There is no separate service, API
gateway, database, broker, cache or outbound call. The only external surface added is the HTTP
contract `GET /api/status`, and it reports process liveness only (FR-05), not readiness.

## Structural alternatives
| Option | Trade-off | Verdict |
| --- | --- | --- |
| **A. New controller in the existing application (chosen)** | One class, no new dependency, reuses the existing router, Jackson converter and request-id filter. The endpoint shares the application's lifecycle, which is correct because it reports that application's liveness. | Chosen. Smallest change that meets FR-01 … FR-06 and NFR-01 … NFR-06. |
| B. Separate microservice for the status route | Independent deployment and scaling, but adds a deployment unit, a second probe target, observability and operational surface for a constant response, and the signal would describe the new service rather than this application. | Rejected: the operational cost has no requirement behind it and contradicts NFR-01/NFR-02. |
| C. Spring Boot Actuator `/actuator/health` | Mature health model and built-in probes, but introduces a dependency, a different contract and a readiness vocabulary that phase 01 places out of scope. | Rejected without re-opening `REQ-STATUS-001`; the body and path would violate FR-01/FR-04. |
| D. Custom servlet or filter outside Spring MVC | Control over the raw response, but duplicates path/method handling, `405` + `Allow`, content negotiation and JSON serialization that the existing router already provides. | Rejected: more code for the same observable contract. |
| E. Static resource or build-time file | Zero runtime code, but cannot report process liveness and cannot return the required dynamic status contract. | Rejected: does not satisfy FR-02/FR-05. |

The rationale for A is that the liveness signal belongs to the process whose liveness is being
reported, and the existing stack already supplies every mechanism the contract needs. No
alternative that satisfies FR-01 … FR-06 reduces the dependency footprint or the code below A.

## Solution constraints
The solution shall:
- add exactly one production class, in `com.example.hello`;
- reuse the existing Spring MVC router, Jackson converter and `RequestIdFilter`;
- add no runtime dependency, configuration property, profile, secret or deployment unit;
- keep `GET /api/hello`, `HelloHandler` and the existing tests unchanged;
- perform no network, disk or database I/O on the request path.

These constraints are derived from FR-05, FR-06, NFR-01, NFR-02, NFR-04 and NFR-05. A design that
breaks one of them is not this design.

## Components
| Component | Responsibility | Basis |
| --- | --- | --- |
| HTTP client or probe (consumer) | Sends `GET /api/status` and reads the response | FR-01, FR-02 |
| Embedded Tomcat (existing platform) | Accepts the connection, dispatches the request and writes the response | NFR-01, NFR-02 |
| `RequestIdFilter` (existing) | Binds `requestId` into the MDC and echoes `X-Request-ID`; inherited unchanged | NFR-05, existing observability convention |
| Spring MVC `DispatcherServlet` and handler mapping (existing router) | Matches path and method, dispatches `GET`, rejects unsupported methods | FR-01, FR-06 |
| `StatusHandler` (new, one class) | Returns `200 OK` with the constant status body; performs no I/O | FR-02, FR-03, FR-04, FR-05 |
| Jackson `MappingJackson2HttpMessageConverter` (existing) | Serializes the response object to `application/json` | FR-03, FR-04, NFR-01 |

No persistence component, service layer, repository, DTO module, health aggregator or custom
router is added: no functional or non-functional requirement needs one, and each would add code
or a dependency that NFR-01 and NFR-02 exclude.

## Component interactions
```
Client or probe
  → Tomcat (accepts)
  → RequestIdFilter (request id + X-Request-ID)
  → DispatcherServlet / handler mapping (path + method)
  → StatusHandler (constant body, no outbound call)
  → Jackson converter (JSON)
  → Tomcat (writes 200)
```
- The router resolves path and method before the handler runs. `POST`, `PUT`, `PATCH` and `DELETE`
  are rejected with `405` and an `Allow` header containing `GET`; the handler is never invoked
  (FR-06).
- `HEAD` is served with the semantics of `GET` (status and headers, no body) and `OPTIONS` is
  answered by Spring MVC to advertise the available methods; both are permitted and neither is
  required to return `405` (FR-06).
- `StatusHandler` makes no outbound call and reads no external state (FR-05, NFR-02).
- `RequestIdFilter` runs for this route because it is a servlet filter registered for the whole
  application; the header is additive and does not alter the body (NFR-05, NFR-06).

## Views
This change uses a **single view**: a component-and-connector view together with the HTTP contract
below. One runtime path, one deployment unit and no owned data mean the concerns in the table are
all served by that view, so no second view and no correspondence between views are stated. The
rationale is recorded here per `ADR-010`; a single view is not a defect.

A separate data view is not produced because the endpoint owns no persisted or shared data. A
separate deployment view is not produced because the deployment model is unchanged. The security
boundary is stated in this description because it constrains the same component and connector.

## API / HTTP contract
| Element | Value | Basis |
| --- | --- | --- |
| Method | `GET` | FR-01, FR-02 |
| Path | `/api/status` | FR-01 |
| Success status | `200 OK` | FR-02, AC-01 |
| Media type | Compatible with `application/json`, set by `produces` and content negotiation; the handler sets no explicit `Content-Type` | FR-03, AC-02 |
| Success body | Exactly `{"status":"UP"}`: one member, `status`, value `UP` | FR-04, AC-03 |
| `HEAD` | Permitted with GET semantics: status and headers, no body | FR-06 |
| `OPTIONS` | Permitted: Spring MVC advertises the available methods | FR-06 |
| Unsupported methods | `POST`, `PUT`, `PATCH`, `DELETE` → `405 Method Not Allowed` with `Allow` containing `GET`; the framework-generated error body is not part of this contract and is outside NFR-06 | FR-06; AC-05 covers `POST`, `PUT`, `DELETE`; D6 |
| Authentication / authorization | None; the endpoint is unauthenticated inside the deployment boundary | Out of scope in phase 01 |
| Query parameters / request body | None read or required | FR-04, analysis |
| Response headers | Framework headers plus `X-Request-ID` from the existing filter | NFR-05, NFR-06 |
| Incompatible `Accept` | `406` from Spring MVC; other media types are out of scope | Phase 01 scope |
| Caching policy | None declared; `Cache-Control`/`ETag` are out of scope | Phase 01 scope |
| Latency target | No numeric target; no network, disk or database I/O on the request path | NFR-02 |

## Data ownership
No data is persisted, cached or shared. `StatusHandler` owns the constant `UP` and the response key
`status`; the response object is created per request. There is no transaction, no repository and no
data migration. The design document states the concrete representation.

## Integration patterns
The integration is a synchronous HTTP request/response with in-process method dispatch. No
messaging, asynchronous, batch or external-system integration exists, because there is no external
collaborator (FR-05, NFR-02). No integration pattern is selected; selecting one would add a
dependency that NFR-01 forbids.

## Security boundaries
- The endpoint is unauthenticated and performs no authorization; authentication, authorization and rate
  limiting are out of scope in `REQ-STATUS-001`.
- It is reachable only inside the deployment boundary. If it is exposed beyond that boundary, the
  requirement's assumption breaks and a separate requirement covers the exposure policy.
- No request payload or query parameter is parsed, so no validation or deserialization surface is
  added. Only the method, path and headers reach the routing layer.
- The `200 OK` response discloses only the constant `status` value (NFR-06): no version,
  hostname, uptime, timestamp, dependency name, stack trace or internal identifier.
  Framework-generated error responses, including their bodies, are outside NFR-06; they preserve
  the approved HTTP error contract (FR-06/AC-05) and add no diagnostic data to the `200 OK` body.
- `RequestIdFilter` binds an identifier into the MDC and echoes it; it logs no request payload and
  introduces no new log statement.
- No secret, credential or configuration property is added.

Residual risk: an unauthenticated liveness endpoint is an abuse and scanning surface if exposed
publicly. It is recorded as an assumption in the requirement; exposure policy is out of scope, so
this design does not mitigate it.

## Scalability and availability
The handler is stateless and holds no shared mutable state, so it scales with the host application
and needs no coordination between instances. Registration is unconditional (NFR-04): no profile,
feature flag, configuration property or conditional bean can remove the endpoint. A failed instance
makes the endpoint unavailable in the same way it makes `/api/hello` unavailable; no additional
availability mechanism is introduced. Readiness semantics are out of scope.

## Observability
The route inherits `RequestIdFilter` and the existing Logback configuration. No new metric, log
statement, dashboard, alert or probe is added: pipeline and probe wiring are a separate requirement.
The endpoint itself is the liveness signal that a consumer reads.

## Deployment model
The change ships in the existing artifact, container image and port, with no new configuration,
profile, secret, volume or network rule. `Dockerfile`, workflows and deployment manifests are
unchanged (NFR-01, NFR-02). Rollback is the revert of the commit that adds the handler and its
tests; there is no data migration and no configuration to restore.

## Quality attribute scenarios
| ID | Source | Stimulus | Environment | Artifact | Response | Response measure |
| --- | --- | --- | --- | --- | --- | --- |
| QAS-01 | Operator or probe | `GET /api/status` | Default configuration, no external service available | `StatusHandler` registration and route | `200 OK` with the status body | Status is `200` and not `404`; no `@Profile`, `@ConditionalOn*` or configuration guard on the handler (NFR-04, AC-01) |
| QAS-02 | Probe | `GET /api/status` | No network, disk or database access available | Request path from router to handler | `200 OK` with `status` = `UP` | Zero network, disk or database calls; the handler unit test passes with no collaborators (NFR-02, AC-04) |
| QAS-03 | API consumer | `GET /api/status` | Normal operation | Response body and headers | JSON object with one member | Member count is exactly `1`, the member is `status` with value `UP`, and no other field is present (NFR-06, AC-03) |
| QAS-04 | Maintainer | Build and dependency report | Offline Gradle cache | `build.gradle.kts` and `runtimeClasspath` | Unchanged dependency set | Zero dependencies added; Actuator is absent (NFR-01, AC-06) |
| QAS-05 | Developer | `./gradlew clean test` | No external server started | New and existing test suite | Tests execute and pass | Build is green; the new tests use JUnit 5, `@WebMvcTest` and MockMvc (NFR-03, AC-06) |
| QAS-06 | Maintainer | Full test suite | Pre-existing tests unmodified | `/api/hello` tests and handler conventions | All tests pass | The 9 pre-existing tests pass and `HelloHandler`/`HelloEndpointTest` are unchanged (NFR-05, AC-07) |

Sensitivity: NFR-02 is a binary no-I/O constraint, not a latency threshold, and phase 01 states no
numeric latency target. The endpoint's latency therefore follows the host platform and no
load-sensitivity claim is made. Introducing a numeric latency target is a new requirement, not an
architecture decision.

## Architecture decisions and significance (D4)
The significance test is the one in `standards/terms.md`:

| Criterion | Verdict for this change |
| --- | --- |
| (a) Expensive to reverse | No. The public contract is fixed by `REQ-STATUS-001`; the structural choice is one controller in the existing application, reversible by deleting one class. |
| (b) Constrains two or more lifecycle phases | No. Phases 04–06 are constrained by the acceptance criteria in `REQ-STATUS-001`; this description adds no constraint a later phase would otherwise avoid. |
| (c) Introduces or removes a dependency, platform or standard | No. The existing Spring Boot stack is reused and `build.gradle.kts` is unchanged. |
| (d) Changes a quality attribute the system is judged on | No. No threshold changes; the NFRs are met by reusing the platform. |
| (e) Affects consumers outside the team | No new effect. The consumer-visible contract is the requirement; this description does not alter it. |

**Conclusion (D4, approved 2026-10-22):** no decision in this change is significant, so no new ADR
is required and no existing ADR needs updating. D1–D3 are local implementation choices recorded in
`blueprint/design/api-status-endpoint.md`. Recording a non-significant decision as an ADR is
permitted, but it would add a record that no phase depends on; should the requester later decide
otherwise, the next free identifier is `ADR-014`. `ADR-006` (code-design criteria), `ADR-010`
(views and scenarios) and `ADR-013` (traceability) apply to this work; no other ADR is cited.

**Stale references.** `HelloHandler` and the `hello-endpoint` architecture and design artifacts
cite `ADR-005`…`ADR-008` for routing, response format and route registration. Those identifiers now
belong to other decisions (`ADR-005` skill adapter, `ADR-006` code design, `ADR-007` licence,
`ADR-008` terminology). This document neither copies nor corrects those historical citations;
correcting them is separate documentation debt outside this requirement.

## Traceability
| Requirement or criterion | Architecture element |
| --- | --- |
| FR-01 path `/api/status` | HTTP contract; handler mapping; `StatusHandler` mapping |
| FR-02 `200` while serving HTTP | HTTP contract; `StatusHandler` response; QAS-01 |
| FR-03 JSON media type | HTTP contract; Jackson converter |
| FR-04 exact body | HTTP contract; `StatusHandler` constant; QAS-03 |
| FR-05 liveness only, no external call | Component interactions; data ownership; QAS-02 |
| FR-06 unsupported methods and permitted `HEAD`/`OPTIONS` | Router component; HTTP contract |
| NFR-01 no new runtime dependency | Components (existing platform); QAS-04 |
| NFR-02 no I/O on the request path | Component interactions; QAS-02 |
| NFR-03 test infrastructure | QAS-05; design test strategy |
| NFR-04 unconditional exposure | Scalability and availability; QAS-01 |
| NFR-05 conventions and `/api/hello` unchanged | Components; QAS-06 |
| NFR-06 no information disclosure in the `200 OK` body; framework error bodies outside scope (D6) | Security boundaries; QAS-03; HTTP contract |
| AC-01, AC-02, AC-03, AC-05 | HTTP contract; design tests |
| AC-04 | `StatusHandler`; QAS-02 |
| AC-06, AC-07 | QAS-04, QAS-05, QAS-06; design tests |

## Deliberately left out
Spring Boot Actuator and `/actuator/health`; readiness or dependency checks; a `DOWN` state or
`503`; `200 OK` response fields other than `status`; framework-generated error bodies, which are
outside NFR-06 while FR-06/AC-05 remain in force; authentication, authorization, rate limiting,
CORS, TLS and exposure policy; smoke-test, probe and `HEALTHCHECK` wiring; caching and content
negotiation beyond `application/json`; metrics, dashboards, alerts and new logging; i18n and
user-specific responses. The requirement places each of these out of scope.

## Review and approval
**Approved by the requester on 2026-10-22** for phase 03. The approval covers:

1. **D4 — no ADR.** The significance table above is accepted: no new ADR and no update to an
   existing ADR. If the endpoint is later considered significant under (a) or (e), `ADR-014` is
   the next free identifier.
2. **D1–D3.** The representation, handler identity and test structure approved in
   `blueprint/design/api-status-endpoint.md`, including the D1 content-type clarification.
3. **No phase-01 change at that review.** `REQ-STATUS-001` was unchanged in phase 03; the NFR-06
   scope addendum in item 6 was approved later.
4. **Scope and single view.** One component-and-connector view is accepted. The absence of a formal
   stakeholder register is recorded above; no stakeholder is invented and the gap does not block
   the change.
5. **No implementation.** No code, configuration or dependency change is made in this phase.
6. **NFR-06 scope addendum (2026-10-23).** NFR-06 applies only to the `200 OK` response body;
   framework-generated error responses are outside that restriction, provided they preserve the
   approved HTTP error contract. FR-06/AC-05 still require `405` with `Allow` containing `GET`.
   This is the Phase 06 finding F-01 and changes no code, test or HTTP behaviour.

**Approval record:** requester, 2026-10-22; NFR-06 scope addendum approved by the requester on
2026-10-23. Phase 03 closed with the original approval. The design carries its own phase-04 approval
record; Development was the next phase and was completed in phase 05.
