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
| Independent review of `REQ-STATUS-001` | Required before closure | Pending — a non-independent review record exists in `blueprint/reviews/api-status-endpoint.md` |
| Local execution | Retain the recorded execution evidence; do not invent an undocumented startup command | Evidence retained; reproducible procedure pending |
| `CHANGELOG.md` | Do not create | Approved |
| Blueprint formal validation | Do not claim validation | Remains `NOT VERIFIED` |
| Gates P-02, P-06, P-07, P-10 and P-11 | Do not close because publication is postponed | Open or not verified |
| IMP-001, IMP-002 and IMP-003 | Keep as `Deferred` | No change |

Postponing remote publication is not evidence that publication was completed and does
not close any gate. The gate identifiers P-02, P-06, P-07, P-10 and P-11 are defined in
`blueprint/gates.md`.

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