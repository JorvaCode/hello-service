# Code Review: REQ-STATUS-001 — GET /api/status

## Review scope

- Implementation: `src/main/java/com/example/hello/StatusHandler.java`.
- Tests: `src/test/java/com/example/hello/StatusHandlerTest.java`,
  `src/test/java/com/example/hello/StatusEndpointTest.java`.
- Baseline artifacts: `blueprint/requirements/api-status-endpoint.md`,
  `blueprint/analisys/api-status-endpoint.md`,
  `blueprint/architecture/api-status-endpoint.md`,
  `blueprint/design/api-status-endpoint.md`.
- Checklist: `blueprint/07-code-review.md`.
- Automated evidence: `blueprint/testing/api-status-endpoint.md` — `BUILD SUCCESSFUL`, 17
  tests, 0 failures, 0 errors, 0 skipped at commit `f2b03e5`.

This review was performed from the recorded artifacts and test evidence. It did not run the
suite again and did not modify the implementation.

## Board

### 1. Requirement satisfied?

- FR-01/FR-02: `@GetMapping(path = "/api/status")` returns `200 OK` — verified by
  `StatusEndpointTest.getReturns200WithJsonContentType` and `StatusHandlerTest.returnsStatus200`.
- FR-03: `produces = MediaType.APPLICATION_JSON_VALUE` — verified by
  `contentTypeCompatibleWith(MediaType.APPLICATION_JSON)`.
- FR-04: `Map.of(STATUS_MEMBER, STATUS_UP)` yields exactly `{"status":"UP"}` — verified by
  `getReturnsExactStatusBody` (raw body equality plus member-count assertion).
- FR-05: the handler has no collaborator and no I/O — verified by
  `StatusHandlerTest.returnsUpStatusBody` on a directly constructed handler.
- FR-06: `POST`, `PUT`, `PATCH` and `DELETE` return `405` with `Allow` containing `GET` —
  verified by the parameterised test plus `patchReturns405WithAllowGet`.
- NFR-06: the `200 OK` body has exactly one member; no diagnostic data is added. Framework
  error bodies are outside the approved scope clarification D6.

No requirement is missing a check.

### 2. Design consistent with architecture?

- `StatusHandler` matches the phase 04 design: package-private class, `status()` method,
  `Map<String, String>` body, constants shared with the unit test.
- No ADR was raised, consistent with the design's significance assessment; none of criteria
  (a)–(e) in `standards/terms.md` applies.
- `HelloHandler`, `RequestIdFilter`, `HelloApplication` and `build.gradle.kts` are unchanged,
  as the design requires (NFR-05, NFR-01).

### 3. Does each new name assert a decision or a behaviour a third party can check?

- `STATUS_MEMBER` and `STATUS_UP` name the two contract values and are reused by the tests, so
  a change to either is visible in the test that asserts the exact body.
- `status()` names the single behaviour: return the liveness status. No name asserts more than
  the code does.

### 4. Design quality per `standards/code-design.md`

- One controller, one constant body, no service layer, no DTO, no repository. The abstraction
  level matches the single caller.
- No Java interface is extracted; there is one implementation and no substitutability
  requirement.

### 5. Any pattern or abstraction without a stated problem, evidence and scope?

- None. The design records the absence of a pattern with the four required items, which is a
  decision and not a finding.

### 6. Error handling appropriate?

- Unsupported methods are handled by Spring MVC (`405` + `Allow`); the handler is not invoked.
- Unknown path (`404`), incompatible `Accept` (`406`) and unexpected server error (`500`) use
  the framework defaults and are outside the contract.
- No catch block is added; there is no input to validate. Appropriate for the scope.

### 7. Tests adequate?

- Unit level: status and body without a Spring context.
- Web-slice level: status, media type, exact body, and method handling through MockMvc.
- The PATCH test is additive coverage beyond AC-05 and does not change the criterion.
- The suite also covers the pre-existing `hello` behaviour (9 tests), so the change is
  regression-checked.

### 8. Security concerns addressed?

- No request body, query parameter, authentication or secret is introduced.
- No dependency is added.
- **Observation:** `RequestIdFilter` echoes and logs the incoming `X-Request-ID` without
  validating its content or length. A client can place arbitrary text in logs and in the
  response header. The filter is pre-existing and unchanged by this change (NFR-05); servlet
  containers reject CR/LF in headers, which limits response splitting. Recorded as a residual
  risk, not a defect of this change.

### 9. Performance concerns addressed?

- The request path is in-process with no I/O, no allocation beyond the response map and no
  shared mutable state. Negligible for a liveness probe.
- No rate limiting or request timeout is added; that is a deployment concern outside this
  endpoint's scope.

### 10. No unnecessary scope?

- The change adds one production class and two test classes. It does not refactor existing
  code, add Actuator or introduce configuration.

## Findings

| # | Severity | Finding | Disposition |
| --- | --- | --- | --- |
| F-1 | Low | `RequestIdFilter` logs and echoes the client-supplied `X-Request-ID` without content or length validation. Pre-existing and unchanged by this change. | Accepted as residual risk; owner Jorva (maintainer); review date 2026-10-30. |
| F-2 | Info | The liveness endpoint has no rate limiting or timeout. Acceptable because the path has no I/O and the control belongs to the deployment layer. | Accepted, no change required. |
| F-3 | Info | `Map.of` has no specified iteration order; with one member the serialized body is stable, and `getReturnsExactStatusBody` fails if a second member is added. | No change required; the test is the guard. |

## Verdict

Approved for the reviewed scope. Every requirement and acceptance criterion has a named
check, and every finding is accepted as residual risk or requires no change.

## Reviewer, independence and date

- Reviewer: OpenCode review assistant. It did not author the implementation.
- Independence limitation: this is a single-maintainer repository and no independent human
  reviewer is recorded. This record therefore does **not** by itself satisfy an independent
  human approval; if the process requires one, the maintainer must obtain and record it.
- Date: 2026-10-30.
- Basis: recorded artifacts and the test evidence at commit `f2b03e5`; no new test run was
  performed for this review.
