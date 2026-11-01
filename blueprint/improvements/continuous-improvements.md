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
| Closure scope | PoC with CI, local deployment and remote publication | Approved and retained |
| Remote publication to Quay | Record the remote publication when `cd.yml` executes | Completed 2026-11-01 — `quay.io/odysseia/hello-service:0.1.0@sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b`; CD run 41332436297 |
| Residual risk of `cd.yml` | Document the risk without modifying the workflow | Approved; workflow unchanged |
| Independent review of `REQ-STATUS-001` | Record the absence of an independent reviewer; do not claim independent approval | Not performed — a non-independent review record exists in `blueprint/reviews/api-status-endpoint.md`; the delivery closure has a self-review in `blueprint/reviews/delivery-closure.md` |
| Local execution | Verify and document a reproducible startup procedure | Completed 2026-10-30 — procedure and evidence in `blueprint/deployment/local-0.1.0.md` and `README.md` |
| Local promotion and rollback | Record a local promotion of `0.1.0` and rehearse the rollback | Completed 2026-10-30 — `blueprint/delivery/release-0.1.0.md`; remote publication completed 2026-11-01 by CD run 41332436297 |
| `CHANGELOG.md` | Do not create | Approved |
| Blueprint formal validation | Do not claim validation | Remains `NOT VERIFIED` |
| Gates P-02, P-06, P-07, P-10, P-11, P-13 and P-14 | Do not close a gate without its evidence; record independence limitations | P-06, P-07, P-10, P-11, P-13 and P-14 closed; P-07, P-11, P-13 and P-14 closed with solo-maintainer self-review and no independent approval; P-02 not verified |
| Real CI execution | Record the successful GitHub Actions runs | Completed — run 10 on `6ccd170`, run 13 on `2e54476`, run 41220748392 on `0fbaffc`, run 41225786925 on `195d40c`, and runs 41332436321/41332436297 on `c7d3a86` concluded `success` |
| IMP-001 … IMP-005 | Keep as `Deferred` | No implementation; each item now names an owner, an artifact and its feedback source |

The remote publication of `0.1.0` was completed on 2026-11-01 by `cd.yml` run 41332436297
on `c7d3a86`; the image and digest are recorded in
`blueprint/changes/p11-remote-publication.md`. The local promotion remains recorded
separately in `blueprint/delivery/release-0.1.0.md`. The remote publication does not close
any deferred improvement item and does not change a requirement. The gate identifiers
P-02, P-06, P-07, P-10, P-11, P-13 and P-14 are defined in `blueprint/gates.md`.

## Feedback Sources

| Source | Current Evidence | Action |
|---|---|---|
| Incidents | No incidents observed | Continue monitoring; no improvement item raised |
| Metrics | Phase 13 record: no metrics endpoint | Keep `IMP-001` deferred |
| User feedback | No external user feedback available | Capture feedback when the service is consumed by users |
| Retrospectives | Blueprint phases reviewed during implementation | Keep phase-by-phase review practice |
| Technical debt | Phase 13 record: no access log and no automated alerting | Keep `IMP-004` and `IMP-005` deferred |
| Security findings | CI runs gitleaks but no dependency vulnerability scan | Keep `IMP-002` deferred |
| Performance findings | No performance issue identified | Reassess if traffic or processing complexity increases |
| Operational feedback | Phase 13 record: no readiness probe | Keep `IMP-003` deferred |

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
9. An improvement item is traceable only when it names the feedback that raised it, the
   artifact it would change and an owner; otherwise it is a wish.
10. A delivery workflow's trigger is part of its behaviour: a documentation-only merge to
    `main` can execute `cd.yml` and publish an artifact. The closure record must distinguish
    a postponed intention from the workflow's actual trigger.

## Improvement Candidates

### IMP-001 — Application Metrics

**Source:** Metrics feedback; `blueprint/observability/hello-service.md` records that no
metrics endpoint exists.

**Owner:** Jorva (maintainer).

**Artifact changed:** `blueprint/requirements/` (new metrics requirement),
`blueprint/architecture/`, `blueprint/observability/hello-service.md` and the deployment
configuration.

**Description:**
Evaluate adding application-level metrics if the service evolves beyond the current
demonstration scope.

**Potential impact:**
Requirements, observability design and deployment configuration.

**Status:** Deferred — no implementation; loop open.

---

### IMP-002 — Security Validation

**Source:** Security findings; `.github/workflows/ci.yml` runs a gitleaks secret scan but
no dependency vulnerability scan.

**Owner:** Jorva (maintainer).

**Artifact changed:** `.github/workflows/ci.yml`, `blueprint/09-continuous-integration.md`
and `blueprint/packaging/hello-service.md`.

**Description:**
Evaluate adding automated security and dependency vulnerability checks to the CI pipeline.

**Potential impact:**
CI/CD and security validation.

**Status:** Deferred — no implementation; loop open.

---

### IMP-003 — Operational Health Checks

**Source:** Operational feedback; `blueprint/deployment/local-0.1.0.md` records no readiness
probe and `blueprint/observability/hello-service.md` records readiness as not implemented.

**Owner:** Jorva (maintainer).

**Artifact changed:** `blueprint/requirements/` (new readiness requirement or a revision of
`REQ-STATUS-001`), `blueprint/architecture/api-status-endpoint.md`,
`blueprint/deployment/local-0.1.0.md` and `blueprint/observability/hello-service.md`.

**Description:**
Evaluate dedicated health/readiness endpoints if the service is deployed in an environment
requiring orchestration-level health probes.

**Potential impact:**
Requirements, architecture, deployment and observability.

**Status:** Deferred — no implementation; loop open.

---

### IMP-004 — Automated Alerting

**Source:** Observability feedback; `blueprint/observability/hello-service.md` records no
automated alerting and `blueprint/reviews/observability-closure.md` F-2 records the manual
detection limitation.

**Owner:** Jorva (maintainer).

**Artifact changed:** `blueprint/observability/hello-service.md`, the deployment/operating
model and CI/CD configuration.

**Description:**
Evaluate an automated alerting path for the failure modes recorded in
`blueprint/observability/hello-service.md` if the service is deployed beyond the local
PoC. The current detection is manual; there is no paging path or dashboard.

**Potential impact:**
Observability, deployment and operating model.

**Status:** Deferred — no implementation; loop open.

---

### IMP-005 — Request Access Logging

**Source:** Observability feedback; `blueprint/observability/hello-service.md` records no
per-request access log and `blueprint/reviews/observability-closure.md` F-3 records the
correlation limitation.

**Owner:** Jorva (maintainer).

**Artifact changed:** `src/main/java/com/example/hello/RequestIdFilter.java` or a new
access-log component, its tests, `blueprint/observability/hello-service.md` and the security
review of log injection.

**Description:**
Evaluate a request access log that records method, path, status and duration with the
`requestId`, so successful and failed requests are correlated without relying on
framework error logs. The current correlation is demonstrated on error paths only.

**Potential impact:**
Implementation, tests and log-injection review.

**Status:** Deferred — no implementation; loop open.

## Lifecycle Changes

No normative phase document, standard, template or ADR is changed by this phase. The five
improvement items are deferred and none revises a requirement. The project-specific phase
records (`blueprint/14-continuous-improvement.md`, `blueprint/gates.md` and `README.md`) are
updated to record this phase; that is not a change to the lifecycle's normative text. If an
item is implemented, it enters the lifecycle at the phase named in its "Artifact changed"
field and follows the normal requirement, design, test, review, delivery and deployment
path. The post-closure remote publication enters the lifecycle at phase 11 and is recorded
in `blueprint/changes/p11-remote-publication.md`; it is not a phase 14 improvement delivery.

## Phase 14 Closure Record

| Item | Change made | Evidence of effect | Requirement revision | Release |
| --- | --- | --- | --- | --- |
| Phase 14 register | Added an owner, named artifact, feedback evidence and closure state to `IMP-001` … `IMP-005`; added this lifecycle record and closure | `blueprint/reviews/continuous-improvement-closure.md`; local test run 2026-10-30T18:45:20Z (17 tests, 0 failures); phase 14 CI run 41225786925 (`success`); phase 13 CI run 41220748392 (`success`) | None | None; `0.1.0` local release unchanged |
| `IMP-001` … `IMP-005` | No implementation; all remain `Deferred` | No implementation evidence exists; each item's loop remains open | None | None |

The closure record closes the phase 14 register change. It does not claim that any deferred
improvement was delivered. The loops for `IMP-001` … `IMP-005` remain open by design.

Post-closure event (2026-11-01): merging PR #5 triggered `cd.yml` on `c7d3a86`; run
41332436297 published `0.1.0` to Quay and deployed it by digest. This event is recorded in
`blueprint/changes/p11-remote-publication.md`. It does not change the deferred status of
`IMP-001` … `IMP-005` or the phase 14 gate verdict.

## Phase 14 Gate

**State: CLOSED (register updated; all improvement items deferred; no requirement revision).**

- Every improvement item names an owner and the artifact it changes.
- Every item traces back to a named feedback source and its evidence.
- The closure record states what changed and the evidence that it took effect.
- No item revises a requirement, so no `FR-nn`/`NFR-nn` or release is named.
- Limitation: the loops for `IMP-001` … `IMP-005` remain open; no improvement was
  implemented in this phase.
- Post-closure remote publication: recorded separately in
  `blueprint/changes/p11-remote-publication.md`; it does not close any improvement loop.
- Review: `blueprint/reviews/continuous-improvement-closure.md`; solo-maintainer
  self-review, not independent approval.

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
