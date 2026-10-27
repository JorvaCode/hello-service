# Testing: REQ-STATUS-001 — `GET /api/status` liveness endpoint

## Scope and environment
- Requirement: `blueprint/requirements/api-status-endpoint.md`.
- Design: `blueprint/design/api-status-endpoint.md`.
- Implementation: `StatusHandler` and its two test classes.
- Environment: Windows, PowerShell 5.1, Java 21, Spring Boot 3.5.3 with embedded Tomcat, Gradle wrapper 8.14.2, offline dependency cache, no daemon.
- Date: 2026-10-27.
- Commit basis: `HEAD f2b03e5f8d8306192922051dbf6359e23a9a0ee2`; the working tree was clean before and after the test run, so the result is commit-bound.

## Test suite
| Test class | Tests | Purpose |
| --- | ---: | --- |
| `HelloEndpointTest` | 6 | Pre-existing `/api/hello` regression coverage, unchanged |
| `HelloHandlerTest` | 3 | Pre-existing handler coverage, unchanged |
| `StatusEndpointTest` | 6 | AC-01 … AC-03, AC-05, plus additive PATCH coverage |
| `StatusHandlerTest` | 2 | AC-04 unit coverage without collaborators |
| **Total** | **17** | |

## Test run result
- Commit: `f2b03e5f8d8306192922051dbf6359e23a9a0ee2`.
- Working tree: clean before and after the run.
- Environment: Windows, PowerShell 5.1, Java 21, Spring Boot 3.5.3 with embedded Tomcat, Gradle wrapper 8.14.2, offline dependency cache, no daemon.
- Date: 2026-10-27.
- Command: `.\gradlew.bat clean test --offline --no-daemon --console=plain`
- Result: `BUILD SUCCESSFUL`.
- Counts: 17 tests, 0 failures, 0 errors, 0 skipped.
- Evidence: `build/test-results/test/*.xml`; the suite files record 17 tests with 0 failures, 0 errors and 0 skipped, timestamped after the commit was created.
- New file SHA-256 values:
  - `StatusHandler.java`: `2C9F6D9888B0A5B7D83E9927C0793D9161D3B5808F30850E05518294A2DE2413`
  - `StatusHandlerTest.java`: `C84E2BF6ADDDA9C3BBFA2E43AD34BD8D76B85AD249807A3D3564FAE727560436`
  - `StatusEndpointTest.java`: `45211F48E7F97EDDCB6DACCC1894312C5A13EE0F72999FB15B536A927A7FF65E`

### Live HTTP probe
The application was run on `127.0.0.1:18080` and probed directly.

| Request | Observed result |
| --- | --- |
| `GET /api/status` | `200`, `application/json`, body exactly `{"status":"UP"}`, `X-Request-ID` present |
| `HEAD /api/status` | `200` |
| `OPTIONS /api/status` | `200`, `Allow: GET,HEAD,OPTIONS` |
| `POST /api/status` | `405`, `Allow: GET` |
| `PUT /api/status` | `405`, `Allow: GET` |
| `PATCH /api/status` | `405`, `Allow: GET` |
| `DELETE /api/status` | `405`, `Allow: GET` |
| `GET /api/hello` | `200`, `text/plain;charset=UTF-8`, body `Hello, World!` |

The default `405` body is framework-generated and contains diagnostic-looking fields such as `timestamp` and `path`. Under the approved NFR-06 scope clarification, that error body is outside NFR-06; FR-06/AC-05 require only `405` and `Allow` containing `GET`.

## Criterion coverage
| Criterion | Automated verification | Result |
| --- | --- | --- |
| AC-01 | `StatusEndpointTest.getReturns200WithJsonContentType`; review of `StatusHandler` for absent `@Profile`/`@ConditionalOn*` | Pass |
| AC-02 | `StatusEndpointTest.getReturns200WithJsonContentType` with `contentTypeCompatibleWith(APPLICATION_JSON)` | Pass |
| AC-03 | `StatusEndpointTest.getReturnsExactStatusBody`: raw body equality plus JSON member-count assertion | Pass |
| AC-04 | `StatusHandlerTest.returnsStatus200` and `returnsUpStatusBody`; handler has no collaborators and no I/O | Pass |
| AC-05 | `StatusEndpointTest.unsupportedMethodsReturn405WithAllowGet` over `POST`, `PUT`, `DELETE`, asserting `405` and `Allow` containing `GET`; `patchReturns405WithAllowGet` adds PATCH coverage | Pass |
| AC-06 | Clean test run green; `build.gradle.kts` unchanged; dependency tree matches the baseline | Pass |
| AC-07 | 9 pre-existing tests pass unchanged; `HelloHandler` and `HelloEndpointTest` not edited | Pass |

The implementation adds a separate PATCH test beyond AC-05's required methods. It is additive FR-06 coverage and does not change the criterion.

## Scenario coverage
| Scenario | Response measure | Verifying check |
| --- | --- | --- |
| QAS-01 | `200` and not `404`; no conditional registration | AC-01 test and handler review |
| QAS-02 | Zero network, disk or database calls | AC-04 unit test and request-path review |
| QAS-03 | JSON object with exactly one member, `status` = `UP` | AC-03 body and member-count assertions |
| QAS-04 | Zero added dependencies; Actuator absent | `build.gradle.kts` diff and dependency report |
| QAS-05 | Green build using JUnit 5, `@WebMvcTest` and MockMvc | Clean test run |
| QAS-06 | All 9 pre-existing tests pass; hello files unchanged | `HelloEndpointTest`, `HelloHandlerTest` and hash comparison |

## Non-functional evidence
| Requirement | Evidence | Result |
| --- | --- | --- |
| NFR-01 | `build.gradle.kts` SHA-256 `BEE6F176A6A8DB8BE9E89CF8D48EC23E5BA510F2E59DBF877A34043A1C67F1ED`; no dependency added | Pass |
| NFR-02 | Handler has no collaborators and no I/O; AC-04 unit test passes | Pass |
| NFR-03 | New tests use JUnit 5, `@WebMvcTest` and MockMvc; 17-test run green | Pass |
| NFR-04 | No profile, feature flag, configuration property or conditional bean on the handler | Pass |
| NFR-05 | `HelloHandler` SHA-256 `BA094269870800BBB28DC841F67DF2C4644F4E563AD798FF4896E09337E84E96`; `RequestIdFilter` SHA-256 `A8961ACD8E71673AF958AEE7F98D420B5EA0377C9D80A9E4B959AE44DF920EF9`; existing tests pass | Pass |
| NFR-06 | AC-03 verifies the `200 OK` body has one member; review finds no diagnostic data added to that response. Framework-generated error bodies are outside NFR-06 under D6 | Pass under clarified scope |

## Defect record
| ID | Finding | Criterion affected | Severity | Owner | Disposition |
| --- | --- | --- | --- | --- | --- |
| F-01 | NFR-06's literal wording could be read as covering error response bodies, while its verification and AC-03 observe only the `200 OK` body and AC-05 defines no error body. Spring MVC returns a framework-generated `405` body containing `timestamp`, `status`, `error` and `path`. | NFR-06 (scope), AC-05 | Low — verification ambiguity, no observed failure of an approved acceptance criterion | Requester / maintainer | **Closed — not applicable under the approved scope clarification (2026-10-23).** NFR-06 applies only to the `200 OK` body; framework error bodies are outside it while FR-06/AC-05 remain in force. No new automated test is required; the error body is a recorded residual observation. |

No open defect remains for the tested behaviour. The error-body observation is retained as a residual risk in `blueprint/design/api-status-endpoint.md`, not as an open defect.

## Gate assessment
- Test execution and criterion coverage pass: 17 tests, 0 failures, every AC has automated verification or a named review check.
- Scenario coverage is recorded for QAS-01 … QAS-06.
- F-01 is closed under the approved NFR-06 scope; the error-body observation remains a residual risk, not an open defect.
- L-01 is resolved: the recorded run is commit-bound to `f2b03e5f8d8306192922051dbf6359e23a9a0ee2` with a clean working tree, and the environment, commit and command are recorded.
- Checked against `blueprint/06-testing.md`: the test suite and its mapped coverage, the test run result with environment, commit and command, the criterion coverage, the scenario coverage and the defect record are all present, and every required check passed. A reviewer who did not run the tests reaches the same verdict from this report.
- Phase 06 is **CLOSED**. No release or push was performed.
