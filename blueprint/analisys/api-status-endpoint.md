# Analysis: `REQ-STATUS-001` — Endpoint HTTP GET /api/status

## Requirement analysed
- `blueprint/requirements/api-status-endpoint.md` — `GET /api/status` liveness endpoint,
  returning `200 OK` with the exact body `{"status":"UP"}`.
- The requirement is approved and is the only input that defines the contract. This record does
  not change it; discrepancies are fed back to phase `01` instead. The one discrepancy raised
  here, D5, was approved and applied before phase `03`.
- **Status:** analysis complete. D5, D1–D4 and D6 are resolved. D1–D4 were approved in phases `03`
  and `04` on 2026-10-22; D5 was approved before phase 03; D6 was approved on 2026-10-23.

## Existing system context (verified)
- **Runtime stack:** Java 21 and Spring Boot 3.5.3 with `spring-boot-starter-web`
  (`build.gradle.kts`), embedded Tomcat on the default port `8080`.
- **HTTP surface:** one controller, `src/main/java/com/example/hello/HelloHandler.java`, a
  package-private `@RestController` with `@GetMapping(path = "/api/hello", produces =
  MediaType.TEXT_PLAIN_VALUE)` returning a `ResponseEntity<String>`.
- **Cross-cutting filter:** `src/main/java/com/example/hello/RequestIdFilter.java`
  (`@Component`, `HIGHEST_PRECEDENCE`) applies to every request: it echoes or generates
  `X-Request-ID` and binds `requestId` into the SLF4J MDC. A new route inherits this without
  changes.
- **Configuration:** the only file under `src/main/resources` is `logback-spring.xml`. There is
  no `application.properties` or `application.yml`, so there is no context path, no custom
  Jackson configuration, no security configuration and no Actuator exposure.
- **Routing behaviour:** method enforcement happens at the Spring MVC mapping, not in the
  handler. The existing `HelloEndpointTest` asserts `POST /api/hello` → `405` with
  `Allow: GET`, and an unknown path → `404`.
- **Tests:** `HelloHandlerTest` (3 unit tests, direct handler instantiation) and
  `HelloEndpointTest` (6 `@WebMvcTest(HelloHandler.class)` + MockMvc tests, with
  `@Import(RequestIdFilter.class)`). Baseline `./gradlew clean test --offline` is
  `BUILD SUCCESSFUL`, 9 tests, 0 failures.
- **Build dependencies:** `spring-boot-starter-web` brings `spring-boot-starter-json` 3.5.3,
  which brings `jackson-databind` 2.19.1 (`jackson-core`, `jackson-annotations`, JSR-310 and
  JDK-8 datatypes). `spring-boot-starter-test` brings `spring-test` 6.2.8 (MockMvc) and
  `json-path` 2.9.0 (JsonPath assertions). The runtime classpath contains no Actuator, no
  Spring Security, no JDBC, no JPA and no H2.

## Domain concepts
- **Liveness** — the process is running and serving HTTP. It is not the same as **readiness**
  (the process can do useful work) or **dependency health** (its collaborators respond). This
  requirement asks for liveness only.
- **Status token** — the constant string `UP`, carried in a fixed JSON member `status`. It is a
  contract value, not a measurement, a timestamp or a state machine.
- **Process-scoped resource** — the resource describes the process, not a user, a request or a
  stored entity. There is no identity, no persistence and no input beyond the HTTP method.
- **JSON representation** — the response is a JSON object with exactly one member; the media
  type and the member set are part of the contract, not an implementation detail.

## Dependencies and integrations
| Dependency / integration | Role for this change | Evidence | Impact |
| --- | --- | --- | --- |
| Spring MVC (`spring-webmvc` via `spring-boot-starter-web` 3.5.3) | Route registration, method enforcement, content negotiation | Existing `/api/hello` mapping and its `405`/`404` tests | Reused unchanged; no new dependency |
| Jackson (`jackson-databind` 2.19.1 via `spring-boot-starter-json`) | Serializes the JSON body | `./gradlew dependencies --configuration runtimeClasspath --offline` | Present and sufficient; no new dependency |
| `RequestIdFilter` | `X-Request-ID` correlation on every request | Filter is a `@Component` with highest precedence | Applies to the new route automatically; no change |
| Spring Boot test stack (JUnit 5, `spring-test` 6.2.8, `json-path` 2.9.0) | Unit and web-slice verification | `./gradlew dependencies --configuration testRuntimeClasspath --offline` | Reused; covers AC-01 … AC-07 |
| Embedded Tomcat / port 8080 | Runtime entry point | `Dockerfile` (`EXPOSE 8080`) and an observed run on `127.0.0.1:8080` | No change |
| CI/CD workflows (`ci.yml`, `cd.yml`) | Build, publish and smoke test `/api/hello` | Workflow inspection | Not touched; pipeline wiring is out of scope for `REQ-STATUS-001` |
| Actuator, Spring Security, JDBC/JPA | Not present | Dependency tree shows none | Cannot be used; shall not be added (NFR-01) |

## Integration direction and alternatives considered
The integration point is the existing Spring MVC dispatcher: a second controller next to
`HelloHandler`, discovered by the `@SpringBootApplication` component scan, mapped with
`@GetMapping` and serialized by the Jackson converter already on the classpath. The response is
built in the handler from in-process state only.

| Option | Assessment | Outcome |
| --- | --- | --- |
| Spring Boot Actuator `/actuator/health` | Requires a new dependency, exposes a broader contract (components, details, status codes) and is explicitly out of scope | Rejected |
| Reuse `GET /api/hello` as the liveness probe | Couples monitoring to the greeting contract, which the requirement identifies as the problem | Rejected |
| New MVC controller on the existing stack | Reuses routing, content negotiation, automatic `405`, request-id filter and the existing test approach; no new dependency | **Chosen** |
| Servlet `Filter` or dedicated `Servlet` for `/api/status` | Bypasses MVC conventions, loses the automatic `405`/`Allow` handling, is inconsistent with `HelloHandler` and is harder to test with MockMvc | Rejected |
| Pre-serialized JSON `String` in the handler | Bypasses Jackson and content negotiation, hand-rolls JSON and duplicates what the framework already guarantees | Rejected |
| Static resource or build-time JSON file | Is not a live signal and cannot satisfy the liveness semantics | Rejected |

**Direction chosen:** a new Spring MVC handler on the existing stack, mirroring `HelloHandler`
and `HelloEndpointTest`. The route, method restriction and media type are declared on the
mapping, and Jackson performs the serialization. This direction adds no dependency, no
configuration and no change to `HelloHandler`.

**Not decided in this phase** (deferred to Architecture/Design): the response representation
(`Map`, a record/POJO or a `ResponseEntity` wrapper), the handler name and file name, the exact
`produces` declaration, and whether the project records a significant decision for the public
contract under `standards/terms.md`. None of these changes the direction; all are local choices.

## Constraints
- The `200 OK` body is pinned to exactly `{"status":"UP"}` (FR-04), which rules out envelopes,
  extra members and any diagnostic data (NFR-06). Framework-generated error bodies are outside
  NFR-06; FR-06 and AC-05 still require `405` with an `Allow` header containing `GET`.
- No new runtime dependency and no Actuator (NFR-01); the implementation shall use Jackson from the existing
  starter.
- The endpoint is exposed unconditionally (NFR-04); no profile, flag or conditional bean.
- The request path performs no I/O (NFR-02) and the decision reflects liveness only (FR-05).
- The contract shall be verifiable without an external server, using JUnit 5 + `@WebMvcTest` +
  MockMvc (NFR-03).
- `/api/hello`, its tests and the `X-Request-ID` behaviour shall remain unchanged (NFR-05).

## Risks
| ID | Risk | Evidence / basis | Mitigation |
| --- | --- | --- | --- |
| R-01 | FR-06, as originally written, said every method other than `GET` returns `405`, while Spring MVC answers `HEAD` with `200` (no body) and `OPTIONS` with `200` plus `Allow: GET,HEAD,OPTIONS`. HTTP semantics expect this behaviour. | Runtime probe of the existing `@GetMapping` `/api/hello`: `HEAD` → `200`; `OPTIONS` → `200`, `Allow: GET,HEAD,OPTIONS`; `PUT` → `405`, `Allow: GET` | Resolved: FR-06 was amended and approved (see **Discrepancies**), so `HEAD`/`OPTIONS` are permitted and unsupported methods still return `405`. AC-05 is unchanged. |
| R-02 | The byte-exact body depends on Jackson defaults. A future custom `ObjectMapper`, indentation setting or naming strategy would break AC-03. | No Jackson configuration exists today (only `logback-spring.xml` under `src/main/resources`) | AC-03 tests the raw body; a later Jackson change is a contract change, not an incidental edit |
| R-03 | The path `/api/status` is currently at the application root. A servlet context path or ingress prefix would change the externally reachable path. | No `server.servlet.context-path` or gateway-prefix configuration exists | Recorded as an assumption; a path change requires a requirement update |
| R-04 | The endpoint is unauthenticated because no security stack is present. | No Spring Security on the runtime classpath; `RequestIdFilter` is the only filter | Acceptable inside the current deployment boundary; exposure outside it is a separate requirement |
| R-05 | The new route could be mistaken for a readiness/dependency probe. | Requirement objective is liveness-only | Liveness semantics are stated in the requirement and in the handler documentation; pipeline wiring stays out of scope |
| R-06 | The existing code and analysis artifacts cite `ADR-007`/`ADR-008` with application-level meanings, but the current ADR set uses those numbers for other decisions. | `HelloHandler` javadoc cites both; `blueprint/architecture/adr/adr-007-…` is the licence ADR and `adr-008-…` is the terminology ADR | Do not propagate stale ADR references into the new handler or tests; phase 03 decides how to record the contract decision |

## Assumption register
| ID | Assumption | Consequence if false | Status |
| --- | --- | --- | --- |
| A-01 | Liveness is sufficient for the consumers of this endpoint; no `DOWN` state or readiness check is required now. | The contract needs a second state and a non-200 response, and `REQ-STATUS-001` is insufficient. | Confirmed with the requester in phase 01; recorded in the requirement |
| A-02 | Consumers accept `application/json` with the exact single-member body. | FR-03/FR-04 and AC-02/AC-03 become wrong rather than incomplete; a vendor media type or envelope needs a new requirement version. | Recorded in the requirement |
| A-03 | No servlet context path or ingress prefix exists, so `/api/status` is the external path. | FR-01 no longer describes what clients call; deployment and probes would change. | Confirmed by inspection of `src/main/resources` and the running service |
| A-04 | Adding a second `@RestController` in `com.example.hello` is picked up by the existing component scan and does not change `HelloEndpointTest`, which is scoped with `@WebMvcTest(HelloHandler.class)`. | If the existing slice were unscoped, the new controller would join its context and the existing tests could change behaviour. | Confirmed by inspection of the annotations and the green baseline |
| A-05 | Access to the endpoint inside the current deployment boundary does not require authentication. | The unauthenticated resource becomes a security defect if exposure widens. | Recorded in the requirement as an assumption; no security stack exists today |
| A-06 | Jackson default serialization of a single-member object yields compact `{"status":"UP"}` with no whitespace. | AC-03 fails immediately; the representation or Jackson configuration is pinned instead. | Supported by `jackson-databind` 2.19.1 with no custom configuration; to be proven by the test that AC-03 mandates |
| A-07 | The build and tests can run offline with the cached Gradle distribution and dependencies. | CI would require network access for dependency resolution; the local evidence below would not be reproducible. | Confirmed by running dependency reports and `clean test` with `--offline` |

## Discrepancies between the requirement and the implementation
- **FR-06 versus the framework — resolved and approved before phase 03.** FR-06 originally
  stated that *a method other than `GET`* returns `405` with `Allow: GET`. Spring MVC maps
  `HEAD` implicitly to a `GET` handler and answers `OPTIONS` with the allowed methods. Verified
  against the existing `@GetMapping` `/api/hello`: `HEAD` → `200`, `OPTIONS` → `200` with
  `Allow: GET,HEAD,OPTIONS`, `PUT` → `405` with `Allow: GET`. AC-05 only requires `POST`, `PUT`
  and `DELETE`, so the acceptance criteria were already consistent; the FR text was broader
  than the criterion. The requester approved the amendment, and FR-06 now reads: *a request
  with a method the route does not support (for example `POST`, `PUT`, `PATCH` or `DELETE`)
  shall return `405 Method Not Allowed` with an `Allow` header containing `GET`; `HEAD` may be
  served with the semantics of `GET`, and `OPTIONS` may be answered by Spring MVC to
  advertise the available methods; neither is required to return `405`.* The identifier FR-06
  is preserved, AC-05 is unchanged, and no implementation behaviour depends on the change.
- **NFR-06 scope vs. framework error bodies — resolved and approved on 2026-10-23.** The literal
  NFR-06 text could be read as applying to every response, but its verification and AC-03 observe
  only the `200 OK` body, and AC-05 does not define an error body. Spring MVC's default `405`
  response includes a framework-generated body (`timestamp`, `status`, `error`, `path`). The
  requester approved the scope clarification: NFR-06 applies only to the `200 OK` response body;
  framework-generated error responses are outside that restriction provided they preserve the
  approved HTTP error contract; FR-06/AC-05 still require `405` plus `Allow` containing `GET`.
  This is the Phase 06 finding F-01. No requirement behaviour, acceptance criterion, code or test
  changed.
- **Stale ADR references (documentation debt, not a blocker).** `HelloHandler` and the existing
  analysis of `REQ-HELLO-001` cite `ADR-007`/`ADR-008` for routing and registration decisions,
  but the current ADR set assigns those numbers to the licence and terminology decisions
  (`blueprint/architecture/adr/`). The new handler and tests shall not copy those references;
  phase 03 decides whether the application-level decisions need new ADRs.
- No other discrepancy was found: the requirement's path, media type, body, method handling,
  dependency constraint and test constraint are all satisfiable with the current tree.

## Impact on existing behaviour, tests, compatibility and security
- **Existing endpoints:** `/api/hello` keeps its mapping, body, content type and `405`/`404`
  behaviour; no shared code or configuration is modified.
- **Existing tests:** all 9 tests remain valid. `HelloEndpointTest` is scoped to
  `HelloHandler.class`, so the new controller does not enter that slice. The new tests are
  additive.
- **Request-id correlation:** the new route inherits `RequestIdFilter`; this is a compatible,
  additive header and logs no additional data.
- **Content negotiation:** a request with an `Accept` header incompatible with
  `application/json` will receive `406` from the framework. This is HTTP-correct and the
  requirement places other media types out of scope, so no special handling is required.
- **Security:** no authentication or authorization is introduced; the `200 OK` response exposes
  only a constant, so the information-disclosure surface is minimal. Framework-generated error
  bodies are outside NFR-06 and preserve the approved error contract. No new input is parsed,
  which keeps the attack surface at method and header handling already exercised by `/api/hello`.
- **Deployment:** no configuration, port, image or workflow change is needed or in scope.

## Files potentially affected
| File | Change expected | Basis |
| --- | --- | --- |
| `src/main/java/com/example/hello/<StatusHandler>.java` | New controller; name chosen in Design | Chosen direction |
| `src/test/java/com/example/hello/<StatusHandler>Test.java` | New handler unit test | NFR-03, AC-04 |
| `src/test/java/com/example/hello/<Status>EndpointTest.java` | New `@WebMvcTest` + MockMvc test | NFR-03, AC-01 … AC-03, AC-05 |
| `src/main/java/com/example/hello/HelloHandler.java` | No change | NFR-05 |
| `src/main/java/com/example/hello/HelloApplication.java` | No change (component scan already covers the package) | Existing convention |
| `src/main/java/com/example/hello/RequestIdFilter.java` | No change | Additive inheritance |
| `build.gradle.kts` | No change (Jackson already present) | NFR-01, dependency tree |
| `src/main/resources/*`, `Dockerfile`, `.github/workflows/*` | No change | Out of scope |
| `blueprint/requirements/api-status-endpoint.md` | FR-06 amendment and NFR-06 scope clarification applied and approved (see **Discrepancies**) | D5, D6 resolution |

## Decisions
**Resolved before phase 03**
- **D5 — FR-06 amendment:** resolved and approved (see **Discrepancies**). The requirement text
  changed; no implementation behaviour and no acceptance criterion changed.

**Deferred to Architecture / Design**
- **D1 — Representation and serialization:** `Map`, record/POJO or `ResponseEntity`; all are
  serialized by the existing Jackson converter and can produce the exact body. Local decision,
  no dependency impact.
- **D2 — Handler identity:** class name, file name, package and mapping declaration, following
  the `HelloHandler` convention.
- **D3 — Test structure:** one handler unit test plus one `@WebMvcTest` slice test, and whether
  the slice imports `RequestIdFilter` when it does not assert the header.
- **D4 — ADR significance:** phase 03 applies `standards/terms.md`. The preliminary assessment
  is that no ADR is required for the representation or naming; the public contract itself was
  decided in phase 01. The stale `ADR-007`/`ADR-008` references are documentation debt to
  resolve separately.

**Resolved in phase 06**
- **D6 — NFR-06 scope:** resolved and approved on 2026-10-23 (see **Discrepancies**). NFR-06 now
  applies only to the `200 OK` response body; framework-generated error bodies are outside that
  restriction while FR-06/AC-05 remain in force. No code, test or HTTP behaviour changed.

**Resolution in phase 03/04 (approved 2026-10-22).** The architecture and design artifacts
resolve these decisions:
- D1–D3: `blueprint/design/api-status-endpoint.md` — `Map<String, String>` representation (no
explicit `Content-Type`; see the D1 addendum), `StatusHandler` identity, and the unit plus
web-slice test structure.
- D4: `blueprint/architecture/api-status-endpoint.md` — no ADR; none of criteria (a)–(e) in
  `standards/terms.md` is met, and no existing ADR is updated.
The scope, the single view and the recorded stakeholder-register gap were approved in the same
review. No requirement text changed in that review.

## Readiness for the next phase
The direction is implementable with the current tree: Jackson 2.19.1 is already on the runtime
classpath, the MVC routing already provides method enforcement and `405`/`Allow`, the existing
tests are isolated from a new controller, and the baseline is green. No new dependency,
configuration or deployment change is needed, and the requirement's acceptance criteria are
verifiable with the existing test stack.

The FR-06 discrepancy was resolved: the amendment was approved and applied, so no requirement
text conflicts with the framework behaviour anymore. Phase 03 applied the significant-decision
test (D4), and D1–D3 were resolved in phase 04. Phase 06 raised F-01 for the NFR-06 scope
ambiguity; the approved clarification closes it as not applicable under the clarified scope. No
requirement behaviour changed. Phase 06 test evidence and the remaining commit-binding limitation
L-01 are recorded in `blueprint/testing/api-status-endpoint.md`. The quality gate of
`blueprint/02-analysis.md` is met: every assumption is confirmed or recorded as a named risk,
no ambiguity that would alter a functional requirement or a non-functional threshold remains
open, and the direction is implementable with the current tree.

## Traceability
- **Requirement:** `REQ-STATUS-001` (`blueprint/requirements/api-status-endpoint.md`).
- **FR-01/FR-02 →** new MVC mapping on `/api/status` (AC-01); **FR-03 →** Jackson converter and
  `produces` media type (AC-02); **FR-04 →** single-member JSON representation (AC-03);
  **FR-05 →** in-process constant, no collaborators (AC-04); **FR-06 →** routing-level method
  enforcement (AC-05), with `HEAD`/`OPTIONS` permitted as approved before phase 03.
- **NFR-01/NFR-03 →** existing dependencies and test stack (AC-06); **NFR-02 →** no I/O on the
  request path (AC-04); **NFR-04 →** no conditional registration (AC-01); **NFR-05 →**
  unchanged `HelloHandler` and tests (AC-07); **NFR-06 →** exact `200 OK` body (AC-03), with
  framework-generated error bodies outside the restriction and FR-06/AC-05 unchanged.
- **Resolved:** D5 (FR-06 amendment) was approved and applied in `01-requirements.md`. D1–D4
  were resolved and approved on 2026-10-22 in `03-architecture.md` and `04-design.md`. D6
  (NFR-06 scope) was approved on 2026-10-23 and applied in `01-requirements.md` and this record.

## Evidence log
| Check | Command / method | Result |
| --- | --- | --- |
| Runtime dependency tree | `./gradlew dependencies --configuration runtimeClasspath --offline` | `jackson-databind` 2.19.1 via `spring-boot-starter-json` 3.5.3; no Actuator, Security, JDBC, JPA or H2 |
| Test dependency tree | `./gradlew dependencies --configuration testRuntimeClasspath --offline` | `spring-test` 6.2.8, `json-path` 2.9.0 via `spring-boot-starter-test` 3.5.3 |
| Baseline suite | `./gradlew clean test --offline` | `BUILD SUCCESSFUL`; `HelloEndpointTest` 6/6, `HelloHandlerTest` 3/3, 0 failures |
| Framework method behaviour | Run the application and probe the existing `@GetMapping` `/api/hello` on `127.0.0.1:8080` | `GET` → `200`; `HEAD` → `200`; `OPTIONS` → `200`, `Allow: GET,HEAD,OPTIONS`; `PUT` → `405`, `Allow: GET` |
| Configuration surface | Inspect `src/main/resources` and root config files | Only `logback-spring.xml`; no `application.properties`/`application.yml`, no context path, no Jackson or security configuration |
| Existing route collision | Search `src/**/*.java` for `api/status` | No existing mapping; no collision |
| FR-06 resolution | Requester approval after the phase-01 feedback | FR-06 amended in `blueprint/requirements/api-status-endpoint.md`, identifier preserved; AC-05 unchanged |
| Phase 03/04 approval | Requester review of the architecture and design artifacts | D1–D4, scope, single view and stakeholder-register gap approved 2026-10-22; Development not started |
| Phase 06 test run | `.\gradlew.bat clean test --offline --no-daemon --console=plain` | `BUILD SUCCESSFUL`; 17 tests, 0 failures/errors/skips; see `blueprint/testing/api-status-endpoint.md` |
| Phase 06 F-01 resolution | Requester approval of the NFR-06 scope clarification | Approved 2026-10-23: NFR-06 applies to the `200 OK` body only; framework error bodies are outside scope; no code, test or HTTP change |
