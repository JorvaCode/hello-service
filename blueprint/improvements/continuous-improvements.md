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
| Closure scope | PoC with CI, local deployment and manual remote publication | Approved and retained |
| Remote publication to Quay | Record the remote publication when `cd.yml` executes | Completed 2026-11-01 — current digest `quay.io/odysseia/hello-service:0.1.0@sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17`; CD runs 41332436297 and 41334455398 |
| Residual risk of `cd.yml` | Document the risk and remove the automatic push trigger | Approved; `cd.yml` now requires `workflow_dispatch` |
| Release tag immutability and environment protection | Protect Git tags and refuse an existing Quay tag by default; restrict CD to the `production` environment on `main` | Implemented 2026-11-02 — ruleset `immutable-release-tags`, environment `production`, and the `cd.yml` tag guard; record in `blueprint/changes/p11-tag-immutable-env.md` |
| Independent review of `REQ-STATUS-001` | Record the absence of an independent reviewer; do not claim independent approval | Not performed — a non-independent review record exists in `blueprint/reviews/api-status-endpoint.md`; the delivery closure has a self-review in `blueprint/reviews/delivery-closure.md` |
| Local execution | Verify and document a reproducible startup procedure | Completed 2026-10-30 — procedure and evidence in `blueprint/deployment/local-0.1.0.md` and `README.md` |
| Local promotion and rollback | Record a local promotion of `0.1.0` and rehearse the rollback | Completed 2026-10-30 — `blueprint/delivery/release-0.1.0.md`; remote publications completed 2026-11-01 by CD runs 41332436297 and 41334455398 |
| `CHANGELOG.md` | Do not create | Approved |
| Blueprint formal validation | Do not claim validation | Remains `NOT VERIFIED` |
| Gates P-02, P-06, P-07, P-10, P-11, P-13 and P-14 | Do not close a gate without its evidence; record independence limitations | P-02, P-06, P-07, P-10, P-11, P-13 and P-14 closed; P-02, P-07, P-11, P-13 and P-14 closed with solo-maintainer self-review and no independent approval |
| Real CI execution | Record the successful GitHub Actions runs | Completed — run 10 on `6ccd170`, run 13 on `2e54476`, run 41220748392 on `0fbaffc`, run 41225786925 on `195d40c`, runs 41332436321/41332436297 on `c7d3a86`, runs 41334455181/41334455398 on `21772cc` and run 41610069726 on `8ea03d9` concluded `success` |
| IMP-002 dependency vulnerability scan | Add a pinned Trivy scan over a committed Gradle lockfile; fail on HIGH/CRITICAL and record accepted findings | Implemented 2026-11-02 — `gradle.lockfile`, Trivy `0.76.1` in `ci.yml`, `.trivyignore`; record in `blueprint/changes/p10-dependency-scan.md` |
| IMP-001, IMP-003 … IMP-005 | Reassess after closure; implement only through the normal lifecycle | Post-closure 2026-11-03/05: `IMP-001`, `IMP-003`, `IMP-004` and `IMP-005` implemented under `REQ-OBS-001`/`REQ-OBS-002` in `blueprint/changes/observability-improvements.md`; `IMP-004` is closed at the alert-definition level, not as a deployed monitoring stack |

The remote publications of `0.1.0` were completed on 2026-11-01 by `cd.yml` runs
41332436297 on `c7d3a86` and 41334455398 on `21772cc`; the current image and digest are
recorded in `blueprint/changes/p11-remote-publication.md`. The local promotion remains
recorded separately in `blueprint/delivery/release-0.1.0.md`. `cd.yml` now requires
`workflow_dispatch`; automatic push publication is disabled. The release controls added on
2026-11-02 protect Git tags, refuse an existing Quay tag by default and restrict CD to the
`production` environment on `main`; they are recorded in
`blueprint/changes/p11-tag-immutable-env.md`. The remote publications and the release
controls do not close any deferred improvement item and do not change a requirement. The gate
identifiers P-02, P-06, P-07, P-10, P-11, P-13 and P-14 are defined in
`blueprint/gates.md`.

## Feedback Sources

| Source | Current Evidence | Action |
|---|---|---|
| Incidents | No incidents observed | Continue monitoring; no improvement item raised |
| Metrics | Post-closure record: `/actuator/prometheus` exposes Micrometer/JVM metrics and the `application="hello-service"` tag; no metrics backend or scraper exists; `ops/prometheus/hello-service-alerts.yml` defines eight alert rules | `IMP-001` and `IMP-004` implemented at the definition level; no backend, evaluation or paging |
| User feedback | No external user feedback available | Capture feedback when the service is consumed by users |
| Retrospectives | Blueprint phases reviewed during implementation | Keep phase-by-phase review practice |
| Technical debt | Post-closure record: the access log and alert-rule definitions are implemented; a running alerting path, dashboards and log retention are still absent | `IMP-005` and `IMP-004` implemented at the definition level; deployment remains absent |
| Security findings | CI runs gitleaks and a Trivy dependency scan over `gradle.lockfile`; two accepted `spring-webmvc` criticals are recorded in `.trivyignore` | Keep `IMP-002` implemented; review accepted findings when Spring Framework 7 is adopted |
| Performance findings | No performance issue identified | Reassess if traffic or processing complexity increases |
| Operational feedback | Post-closure record: `/actuator/health/readiness` and liveness probes are implemented and tested; no external dependency is checked yet | `IMP-003` implemented; extend the readiness indicator when a dependency is added |

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
11. A manual workflow disable did not prevent a push-triggered run in this repository;
    removing the trigger from the workflow definition is the reliable control.
12. An alert rule is a definition, not an operating capability: closure records must
    distinguish a reviewed rule file from a deployed evaluation, routing and paging path.

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

**Status:** Implemented 2026-11-03 — `REQ-OBS-001`, ADR-014 and `CHG-OBS-002` add
Actuator/Micrometer Prometheus metrics at `/actuator/prometheus`. Evidence:
`blueprint/testing/observability-improvements.md` (28 tests at `1b1db72`) and
`blueprint/changes/observability-improvements.md`. No metrics backend is delivered; the eight
alert-rule definitions are recorded under `IMP-004`, but no evaluation or paging path exists.
Loop closed by implementation.

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

**Status:** Implemented 2026-11-02 — pinned Trivy scan over committed `gradle.lockfile`, fail on HIGH/CRITICAL, accepted findings in `.trivyignore`, reports uploaded. GitHub Actions run 41610069726 on `8ea03d9` passed with the `trivy-report` artifact. Loop closed by implementation.

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

**Status:** Implemented 2026-11-03 — `REQ-OBS-001`, ADR-014 and `CHG-OBS-002` enable the
Actuator liveness and readiness groups at `/actuator/health/liveness` and
`/actuator/health/readiness`. The readiness group is `UP` when the context is ready and does
not check an external dependency because none exists. Evidence:
`blueprint/testing/observability-improvements.md` (28 tests at `1b1db72`). Loop closed by
implementation.

---

### IMP-004 — Automated Alerting

**Source:** Observability feedback; `blueprint/observability/hello-service.md` records no
automated alerting and `blueprint/reviews/observability-closure.md` F-2 records the manual
detection limitation.

**Owner:** Jorva (maintainer).

**Artifact changed:** `ops/prometheus/hello-service-alerts.yml`,
`ops/prometheus/README.md`, `blueprint/requirements/observability-alerting.md`,
`blueprint/observability/hello-service.md` and `blueprint/testing/observability-improvements.md`.

**Description:**
Evaluate an automated alerting path for the failure modes recorded in
`blueprint/observability/hello-service.md` if the service is deployed beyond the local
PoC. The current detection is manual; there is no paging path or dashboard.

**Potential impact:**
Observability, deployment and operating model.

**Status:** Implemented 2026-11-05 at the definition level — `REQ-OBS-002` and
`CHG-OBS-002` add eight Prometheus rules mapped to `FM-01`...`FM-07` plus the `HELLO-001`
contract, each with a severity and runbook URL, and a README for the required
application/blackbox jobs. Evidence:
`blueprint/testing/observability-improvements.md` (`AlertRulesTest`, 28 tests at `1b1db72`).
No Prometheus, Alertmanager, blackbox exporter or paging path is deployed; evaluation,
routing and tuning remain outside the local PoC. Loop closed for the definition gap, with the
live-alerting limitation recorded.

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

**Status:** Implemented 2026-11-05 — `RequestAccessLogFilter` emits one correlated
`access method=… path=… status=… duration_ms=… requestId=…` line per request, with query
strings and bodies omitted, and a missing `requestId` increments
`hello_service_access_log_missing_request_id_total`. Evidence: `RequestAccessLogFilterTest`
(5 tests) at `1b1db72`,
recorded in `blueprint/testing/observability-improvements.md` and
`blueprint/changes/observability-improvements.md`. Log shipping, sampling and retention remain
unimplemented and are recorded as limitations, not as a separate open item. Loop closed by
implementation.

## Lifecycle Changes

No normative phase document, standard or template is changed by this phase. At phase 14
closure, the five improvement items were deferred and none revised a requirement; `IMP-002`
was implemented later under phase 10. Post-closure on 2026-11-03/05, `IMP-001`, `IMP-003`,
`IMP-004` and `IMP-005` were implemented under phase 13 through `REQ-OBS-001`/`REQ-OBS-002`,
ADR-014 and `CHG-OBS-002`; `IMP-004` is closed at the alert-definition level, not as a deployed
monitoring stack. The project-specific phase records
(`blueprint/14-continuous-improvement.md`, `blueprint/gates.md` and `README.md`) are updated to
record this phase; that is not a change to the lifecycle's normative text. If an item is
implemented, it enters the lifecycle at the phase named in its "Artifact changed" field and
follows the normal requirement, design, test, review, delivery and deployment path. The
post-closure remote publication enters the lifecycle at phase 11 and is recorded in
`blueprint/changes/p11-remote-publication.md`; it is not a phase 14 improvement delivery.

## Phase 14 Closure Record

| Item | Change made | Evidence of effect | Requirement revision | Release |
| --- | --- | --- | --- | --- |
| Phase 14 register | Added an owner, named artifact, feedback evidence and closure state to `IMP-001` … `IMP-005`; added this lifecycle record and closure | `blueprint/reviews/continuous-improvement-closure.md`; local test run 2026-10-30T18:45:20Z (17 tests, 0 failures); phase 14 CI run 41225786925 (`success`); phase 13 CI run 41220748392 (`success`) | None | None; `0.1.0` local release unchanged |
| `IMP-001` … `IMP-005` | At phase 14 closure: no implementation; all remained `Deferred` | No implementation evidence existed at closure; each item's loop remained open | None | None |
| Post-closure `IMP-001`, `IMP-003`, `IMP-004`, `IMP-005` | Implemented metrics, readiness/liveness probes, access logging and alert-rule definitions | `REQ-OBS-001`/`REQ-OBS-002`; `CHG-OBS-002`; 28 tests at `1b1db72`; recorded in `blueprint/testing/observability-improvements.md` | Added `REQ-OBS-001` (`FR-01`…`FR-08`, `NFR-01`…`NFR-07`) and `REQ-OBS-002` | Existing `0.1.0` artifact line; no new release or tag |
| `IMP-004` | Implemented at the definition level: eight Prometheus alert rules and their scrape/probe documentation | `ops/prometheus/hello-service-alerts.yml`, `ops/prometheus/README.md`, `AlertRulesTest`; no Prometheus/Alertmanager/paging deployment | Added `REQ-OBS-002` | None; configuration evidence only |

The closure record closes the phase 14 register change as it stood on 2026-10-30. It did not
claim that any deferred improvement had been delivered at that time. The loops for
`IMP-001` … `IMP-005` were open at closure; the post-closure rows above now close
`IMP-001`, `IMP-003`, `IMP-004` and `IMP-005`. The `IMP-004` closure is scoped to the
alert-definition gap; the absence of a deployed evaluation, routing and paging path remains a
recorded limitation.

Post-closure events (2026-11-01): merging PR #5 triggered `cd.yml` on `c7d3a86`; run
41332436297 published `0.1.0` to Quay and deployed it by digest. Merging PR #6 on
`21772cc` triggered run 41334455398, which published a second digest and superseded the
first. `cd.yml` now requires `workflow_dispatch`; automatic push publication is disabled.
These events are recorded in `blueprint/changes/p11-remote-publication.md`. They do not
change the deferred status of `IMP-001` … `IMP-005` or the phase 14 gate verdict.

Post-closure implementation (2026-11-02): `IMP-002` was implemented by the dependency
vulnerability scan in `.github/workflows/ci.yml`. The committed `gradle.lockfile`,
`.trivyignore` and `blueprint/changes/p10-dependency-scan.md` record the change, and the
scan evidence is attached to CI run 41610069726 on `8ea03d9`. This closes the
`IMP-002` loop. The phase 14 gate remains `CLOSED` because its closure record and evidence
are unchanged; the implementation is recorded against phase 10, the artifact named by
`IMP-002`.

Post-closure implementation (2026-11-03/05): `IMP-001`, `IMP-003`, `IMP-004` and `IMP-005` were
implemented under phase 13 by `REQ-OBS-001`/`REQ-OBS-002`, ADR-014 and `CHG-OBS-002`. The
change adds Actuator liveness/readiness probes, Micrometer Prometheus metrics, a correlated
access log and eight Prometheus alert-rule definitions. It does not add a metrics backend,
dashboard, deployed rule evaluation or paging path; that limitation is recorded. Evidence is
the 28-test suite at `1b1db72`, recorded in
`blueprint/testing/observability-improvements.md`, and the review in
`blueprint/reviews/observability-improvements.md`. No new release or tag was created; the
change ships in the existing `0.1.0` artifact line.

## Phase 14 Gate

**State: CLOSED (register updated; `IMP-001`/`003`/`004`/`005` implemented post-closure;
`IMP-004` at the definition level).**

- Every improvement item names an owner and the artifact it changes.
- Every item traces back to a named feedback source and its evidence.
- The closure record states what changed and the evidence that it took effect.
- `REQ-OBS-001` (`FR-01`…`FR-08`, `NFR-01`…`NFR-07`) and `REQ-OBS-002` record the
  post-closure signals and alert definitions; no new release or tag was created.
- Limitation: the eight alert rules are configuration evidence only; no Prometheus,
  Alertmanager, blackbox exporter, dashboard, metrics backend or paging path is deployed.
- Post-closure remote publication: recorded separately in
  `blueprint/changes/p11-remote-publication.md`; it does not close any improvement loop.
- Review: `blueprint/reviews/continuous-improvement-closure.md` (original register) and
  `blueprint/reviews/observability-improvements.md` (post-closure implementation);
  solo-maintainer self-reviews, not independent approval.

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
