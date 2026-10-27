# Requirement: `REQ-STATUS-001` — `GET /api/status` liveness endpoint

## Problem
The service exposes only a functional endpoint (`GET /api/hello`). Any monitoring system or
operator that wants to know whether the process is alive reuses that endpoint, which couples
liveness checking to the greeting contract: a legitimate change to the message would change
what the probe observes, and the greeting says nothing about the state of the service.
There is no dedicated, stable signal that answers "is this process serving HTTP right now?".

## Objective
Provide a dedicated liveness signal that a client, operator or future probe can consume without
depending on any other endpoint's contract: while the process is serving HTTP,
`GET /api/status` returns HTTP `200 OK` with the JSON body `{"status":"UP"}`. The outcome is
measurable: an automated test asserts status, media type, exact body and method handling, and the
full test suite stays green.

The liveness-only semantics, the exact response body, the `405` behaviour for unsupported
methods and the exclusion of pipeline wiring were confirmed with the requester before this
document was written. The handling of `HEAD` and `OPTIONS` was raised in the analysis of this
requirement and the resulting revision of FR-06 was approved before phase 03.

## Scope
### In scope
- Expose `GET /api/status` on the existing service.
- Return HTTP `200 OK` while the process is able to serve HTTP requests.
- Return a response whose `Content-Type` is compatible with `application/json`, with the body
  exactly `{"status":"UP"}`.
- Report liveness only: the decision uses in-process state and consults no external dependency.
- Reject unsupported methods on that path (`POST`, `PUT`, `PATCH`, `DELETE`) with
  `405 Method Not Allowed` and an `Allow` header that contains `GET`.
- Allow `HEAD` to be served with the semantics of `GET` (status and headers, no body) and
  `OPTIONS` to be answered by Spring MVC to advertise the available methods.
- Automated tests at handler level and at web-slice level (`@WebMvcTest` + MockMvc).

### Out of scope
- Spring Boot Actuator, `/actuator/health` or any health-check framework.
- Readiness checks, dependency checks (databases, brokers, downstream services) and any `DOWN`
  state or `503` response. The service has no external dependencies today, and this requirement
  deliberately does not introduce the ability to report them.
- Response fields other than `status` in the `200 OK` body: no `timestamp`, `version`, `host`,
  `uptime`, component list or free-text message.
- Framework-generated error responses, including their bodies. NFR-06 constrains only the
  `200 OK` body; FR-06 and AC-05 continue to require `405 Method Not Allowed` with an `Allow`
  header containing `GET` for unsupported methods.
- Authentication, authorization, rate limiting, CORS, TLS termination or network exposure
  policy for the endpoint.
- Wiring the endpoint into the CD smoke test, the deployment health probe (`--health-cmd`) or a
  container `HEALTHCHECK`; that is a separate requirement.
- Caching policy (`Cache-Control`, `ETag`, conditional requests), content negotiation for media
  types other than `application/json`, and any HTML or plain-text representation.
- Metrics, dashboards, alerts or logging changes beyond the request-id correlation already
  applied to every request.
- Internationalization and user-specific responses.

## Functional requirements
Each states one behaviour and is decidable by a single test.

| ID | Requirement | Verification |
| --- | --- | --- |
| FR-01 | The service shall expose an HTTP endpoint at path `/api/status`. | Web-slice test: `GET /api/status` resolves to a handler and does not return `404`. |
| FR-02 | A `GET /api/status` request shall return HTTP status `200 OK` while the process is serving HTTP. | Web-slice test asserts `status().isOk()` on `GET /api/status`. |
| FR-03 | The response to `GET /api/status` shall declare a media type compatible with `application/json`. | Web-slice test asserts `contentTypeCompatibleWith(MediaType.APPLICATION_JSON)`. |
| FR-04 | The response body to `GET /api/status` shall be exactly `{"status":"UP"}`: a JSON object with exactly one member, `status`, whose value is the string `UP`, and no other members. | Web-slice test compares the raw body with `{"status":"UP"}` and parses it, asserting exactly one member with value `UP`. |
| FR-05 | The status reported by `/api/status` shall reflect process liveness only: it shall be `UP` whenever the process is serving HTTP and shall not depend on any external dependency or outbound call. | Handler unit test with no collaborators returns `UP`; review confirms no network, disk or database access on the request path. |
| FR-06 | A request to `/api/status` with a method the route does not support (for example `POST`, `PUT`, `PATCH` or `DELETE`) shall return `405 Method Not Allowed` with an `Allow` header containing `GET`. `HEAD` may be served with the semantics of `GET` (same status and headers, no body), and `OPTIONS` may be answered by Spring MVC to advertise the available methods; neither is required to return `405`. | Parameterised web-slice test sends `POST`, `PUT` and `DELETE` and asserts `405` plus an `Allow` header containing `GET`; `HEAD` and `OPTIONS` are observed but not asserted because their handling is permitted, not required. |

## Non-functional requirements
Each names the quality attribute it constrains and a threshold where one applies. "Fast", "secure" and "scalable" are not attributes; "p95 latency under 200 ms at 1000 rps" is.

| ID | Quality attribute | Requirement | Verification |
| --- | --- | --- | --- |
| NFR-01 | Compatibility / dependency footprint | The endpoint shall be built on the HTTP and JSON support already provided by `spring-boot-starter-web`; no runtime dependency shall be added and Spring Boot Actuator shall not be introduced. | Diff of `build.gradle.kts` shows no added dependency; `./gradlew dependencies --configuration runtimeClasspath` matches the baseline. |
| NFR-02 | Performance | The request path shall perform no network, disk or database I/O, so the response cost is bounded by in-process work only. | Review finds no I/O call in the handler or its collaborators; the handler unit test completes with no external service available. |
| NFR-03 | Testability | The contract shall be verifiable without starting an external server, using the test infrastructure already in the project (JUnit 5, `@WebMvcTest`, MockMvc). | The new tests live under `src/test/java/com/example/hello/`, use that infrastructure, and `./gradlew clean test` passes. |
| NFR-04 | Availability / operability | The endpoint shall be exposed unconditionally: it shall not be disabled by a profile, feature flag, configuration property or conditional bean, so a probe can rely on it being present whenever the application context is up. | Review finds no `@Profile`, `@ConditionalOn*` or configuration guard on the handler or its registration; the web-slice test passes with the default test configuration. |
| NFR-05 | Maintainability / consistency | The change shall follow the conventions already used by `HelloHandler` and `HelloEndpointTest` (same package, `@RestController`, routing-level method enforcement) and shall not alter the behaviour or the tests of `/api/hello`. | Review against `src/main/java/com/example/hello/HelloHandler.java` and `src/test/java/com/example/hello/HelloEndpointTest.java`; existing `/api/hello` tests pass unmodified. |
| NFR-06 | Security / information disclosure | The `200 OK` response body to `GET /api/status` shall disclose no runtime information other than the `status` value: no version, hostname, uptime, timestamps, dependency names, stack traces or internal identifiers. Framework-generated error responses are outside this response-body restriction, provided they preserve the approved HTTP error contract; FR-06 and AC-05 continue to require `405 Method Not Allowed` with an `Allow` header containing `GET`. | The body equality in FR-04/AC-03 is the observable check for the `200 OK` response; review confirms no diagnostic data is added to that response. Framework-generated error responses are outside the restriction and are not asserted beyond FR-06/AC-05. |

## Acceptance criteria
Numbered `AC-nn`, at least one per functional requirement. The identifier survives a rewording
of the criterion, so the test that verifies it and the release that delivered it name the same
thing. Each criterion states the observable condition and the method that verifies it; a
criterion that cannot fail is not a criterion.

| ID | Covers | Given | When | Then (observable condition) | Verification method |
| --- | --- | --- | --- | --- | --- |
| AC-01 | FR-01, FR-02, NFR-04 | The application context is started with its default configuration and no external service is available. | A client sends `GET /api/status`. | The response status is `200 OK`, the request is not answered with `404`, and the handler is registered without a profile or conditional guard. | `@WebMvcTest` + MockMvc test asserting `status().isOk()` under the default test configuration; review confirms no `@Profile` or `@ConditionalOn*` on the handler. |
| AC-02 | FR-03 | The application context is started. | A client sends `GET /api/status`. | The response `Content-Type` is compatible with `application/json`. | MockMvc `content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)`. |
| AC-03 | FR-04, NFR-06 | The application context is started. | A client sends `GET /api/status`. | The response body is exactly `{"status":"UP"}`: parsing it yields a JSON object with one member, `status`, of value `UP`, and no other member. | MockMvc `content().string("{\"status\":\"UP\"}")` plus a JSON parse asserting member count `1` and `status == "UP"`. |
| AC-04 | FR-05, NFR-02 | The handler is exercised with no external dependency, network access or configuration available. | `GET /api/status` is served. | The response is `200` with `status` = `UP`, proving the decision path uses only in-process state. | Handler unit test constructed without collaborators; code review confirms no I/O on the request path. |
| AC-05 | FR-06 | The application context is started. | A client sends `POST`, `PUT` or `DELETE` to `/api/status`. | The response status is `405 Method Not Allowed` and the `Allow` header contains `GET`. | Parameterised MockMvc test over the three methods asserting `405` and an `Allow` header containing `GET`. |
| AC-06 | NFR-01, NFR-03 | The change is applied to the repository. | `./gradlew clean test` is executed. | The build passes with the new tests, and `build.gradle.kts` declares no dependency that the baseline did not declare. | `./gradlew clean test` green; diff of `build.gradle.kts` against the baseline shows no added dependency. |
| AC-07 | NFR-05 | The change is applied to the repository. | The full test suite is executed. | All pre-existing `/api/hello` tests pass without modification, and the new code follows the existing package and routing conventions. | `./gradlew clean test` green with `HelloEndpointTest` and `HelloHandlerTest` unchanged; review against those files. |

## Dependencies
- Java 21 toolchain and Spring Boot 3.5.3 with `spring-boot-starter-web`, already declared in
  `build.gradle.kts`.
- Spring MVC routing and JSON serialization provided by that starter; no additional library is
  required to satisfy FR-01 … FR-06.
- JUnit 5, MockMvc and `@WebMvcTest` from `spring-boot-starter-test`, already declared, for
  NFR-03 and the acceptance criteria.
- The conventions of `src/main/java/com/example/hello/HelloHandler.java` and
  `src/test/java/com/example/hello/HelloEndpointTest.java` as the reference for NFR-05.
- Phase 02 (Analysis) and phase 03 (Architecture) shall decide the route registration and JSON
  serialization mechanics and record any significant decision; this requirement prescribes the
  contract, not the implementation.
- No dependency on external services, configuration properties, secrets or data stores.

## Risks / assumptions
Each assumption states what breaks if it is false.

- **Assumption:** A liveness signal is sufficient for the consumers of this endpoint. If a
  consumer requires readiness or dependency state, a later requirement adds a `DOWN` state and a
  non-200 response; the current tests would pass while that requirement is absent.
- **Assumption:** Consumers accept `application/json` with the exact body
  `{"status":"UP"}`. If a consumer expects a vendor media type, an envelope or additional
  fields, FR-03/FR-04 change and the existing acceptance tests become wrong rather than merely
  incomplete.
- **Assumption:** The endpoint is reachable without authentication inside its current
  deployment boundary. If it is later exposed beyond that boundary, the unauthenticated
  endpoint becomes an information-disclosure and abuse surface, and this requirement is
  insufficient for that context.
- **Assumption:** `/api/status` is absolute within the application. If a servlet context path or
  an ingress prefix is introduced, the externally reachable path changes and FR-01 no longer
  describes what clients call.
- **Risk:** `/api/status` is mistaken for a readiness or health check and wired into deployment
  probes that expect dependency state. **Mitigation:** the liveness-only semantics are stated in
  the Objective and Scope, pipeline wiring is out of scope, and any probe that requires more is
  the subject of a separate requirement.
- **Risk:** The exact-body rule makes adding a field a breaking change, and a later edit quietly
  relaxes it. **Mitigation:** FR-04 and AC-03 pin the body; adding a field requires a new version
  of `REQ-STATUS-001` and an explicit contract decision, not an in-place edit.
- **Risk:** The endpoint drifts from this requirement once implemented. **Mitigation:** the
  acceptance criteria are traceable by identifier through design, tests and release, per
  `standards/information-items.md`.

## Traceability
- **Realised by** — `StatusHandler`, `StatusHandlerTest` and `StatusEndpointTest`; the analysis,
  architecture and design artifacts under `blueprint/`; and the Phase 06 evidence in
  `blueprint/testing/api-status-endpoint.md`. These artifacts name `REQ-STATUS-001` and its
  acceptance criteria.
- **Delivered in** — none yet. The release record, per `blueprint/11-continuous-delivery.md`,
  shall name `REQ-STATUS-001` and its acceptance criteria when a release carries it, per
  `standards/information-items.md`.
