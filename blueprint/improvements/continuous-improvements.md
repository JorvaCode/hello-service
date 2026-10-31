# Continuous Improvement — hello-service

## Purpose

Capture learnings from development and operation and convert them into
future improvements to requirements, architecture, implementation and
operational practices.

## Scope Closure Record

The following closure decisions are approved for the `hello-service` PoC. This record
does not change requirements, acceptance criteria, ADRs, gates or the status of the
improvement candidates below.

| Item | Approved treatment | State after closure |
|---|---|---|
| Closure scope | PoC with CI and local deployment | Approved and retained |
| Remote publication to Quay | Postponed | Approved; not completed |
| Residual risk of `cd.yml` | Document the risk without modifying the workflow | Approved; workflow unchanged |
| Independent review of `REQ-STATUS-001` | Record the absence of an independent reviewer; do not claim independent approval | Not performed — a non-independent review record exists in `blueprint/reviews/api-status-endpoint.md`; the delivery closure has a self-review in `blueprint/reviews/delivery-closure.md` |
| Local execution | Verify and document a reproducible startup procedure | Completed 2026-10-30 — procedure and evidence in `blueprint/deployment/local-0.1.0.md` and `README.md` |
| Local promotion and rollback | Record a local promotion of `0.1.0` and rehearse the rollback | Completed 2026-10-30 — `blueprint/delivery/release-0.1.0.md`; remote publication remains postponed |
| `CHANGELOG.md` | Do not create | Approved |
| Blueprint formal validation | Do not claim validation | Remains `NOT VERIFIED` |
| Gates P-02, P-06, P-07, P-10, P-11 and P-13 | Do not close a gate without its evidence; record independence limitations | P-06, P-07, P-10, P-11 and P-13 closed; P-07, P-11 and P-13 closed with solo-maintainer self-review and no independent approval; P-02 not verified |
| Real CI execution | Record the successful GitHub Actions runs | Completed — run 10 on `6ccd170` and run 13 on `2e54476` concluded `success` |
| IMP-001 … IMP-005 | Keep as `Deferred` | No change |

Postponing remote publication is not evidence that publication was completed and does
not close any gate. The local promotion of `0.1.0` is recorded separately in
`blueprint/delivery/release-0.1.0.md` and does not claim a remote promotion. The gate
identifiers P-02, P-06, P-07, P-10 and P-11 are defined in `blueprint/gates.md`.

## Feedback Sources

| Source | Current Evidence | Action |
|---|---|---|
| Incidents | No incidents observed | Continue monitoring |
| Metrics | Basic HTTP behavior validated | Consider application metrics if operational complexity grows |
| User feedback | No external user feedback available | Capture feedback when the service is consumed by users |
| Retrospectives | Blueprint phases reviewed during implementation | Keep phase-by-phase review practice |
| Technical debt | Minimal for current scope | Reassess if functionality expands |
| Security findings | No known findings for current scope | Include security validation in future changes |
| Performance findings | No performance issue identified | Reassess if traffic or processing complexity increases |

## Learnings

1. Request correlation through `X-Request-ID` provides a lightweight basis
   for tracing individual HTTP requests.
2. Structured logging can be introduced without changing the application
   contract.
3. The service remains intentionally small, so operational complexity should
   not be introduced without a concrete requirement.
4. The Blueprint lifecycle provides explicit feedback points for future
   changes.
5. Configuration is evidence that a capability is wired, not that it ran. Closure
   records must distinguish configuration, local execution and remote execution.
6. A local promotion is a real promotion only when the artifact identity, checks,
   approval and rollback are recorded; remote publication is a separate event.
7. A single-maintainer repository can record the absence of an independent reviewer, but
   it cannot convert a self-review into an independent approval.
8. Manual detection can satisfy a local PoC runbook, but it is not automated alerting; the
   distinction must be recorded rather than implied.

## Improvement Candidates

### IMP-001 — Application Metrics

**Source:** Metrics

**Description:**  
Evaluate adding application-level metrics if the service evolves beyond
the current demonstration scope.

**Potential impact:**  
Requirements, observability design and deployment configuration.

**Status:** Deferred

---

### IMP-002 — Security Validation

**Source:** Security findings

**Description:**  
Evaluate adding automated security and dependency vulnerability checks to
the CI pipeline.

**Potential impact:**  
CI/CD and security validation.

**Status:** Deferred

---

### IMP-003 — Operational Health Checks

**Source:** Operational feedback

**Description:**  
Evaluate dedicated health/readiness endpoints if the service is deployed
in an environment requiring orchestration-level health probes.

**Potential impact:**  
Requirements, architecture, deployment and observability.

**Status:** Deferred

---

### IMP-004 — Automated Alerting

**Source:** Observability / phase 13

**Description:**
Evaluate an automated alerting path for the failure modes recorded in
`blueprint/observability/hello-service.md` if the service is deployed beyond the local
PoC. The current detection is manual; there is no paging path or dashboard.

**Potential impact:**
Observability, deployment and operating model.

**Status:** Deferred

---

### IMP-005 — Request Access Logging

**Source:** Observability / phase 13

**Description:**
Evaluate a request access log that records method, path, status and duration with the
`requestId`, so successful and failed requests are correlated without relying on
framework error logs. The current correlation is demonstrated on error paths only.

**Potential impact:**
Implementation, tests and log-injection review.

**Status:** Deferred

## Feedback Loop

Requirement
-> Design
-> Build
-> Test
-> Deliver
-> Operate
-> Learn
-> Improve

Improvements identified here become inputs for future requirements or
changes and must go through the Blueprint lifecycle.