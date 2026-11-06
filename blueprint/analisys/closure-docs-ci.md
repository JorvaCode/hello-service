# Analysis: post-closure documentation and CI correction change set

## Change set analysed

| Field | Value |
| --- | --- |
| Change set | PR #5 (phase 14 closure), PR #6 (remote-publication records), PR #7 (manual-only CD), PR #8 (post-merge verification) |
| Commits | `c7d3a86`, `21772cc`, `4a79c6d`, `45d125b` |
| Related requirements | `REQ-HELLO-001`, `REQ-STATUS-001` — delivered by `0.1.0`; unchanged by this change set |
| Related gates | P-11 (closed); P-02 (this analysis) |
| Status | Analysis complete; P-02 evidence; self-reviewed |

This record analyses the documentation and CI correction change set that followed the
phase 14 closure. It does not analyse a new endpoint or a requirement change. The
requirements delivered by `0.1.0` remain `REQ-HELLO-001` and `REQ-STATUS-001`; no
functional requirement or non-functional threshold changes.

## Problem to solve

The phase 14 documentation merge (`c7d3a86`) pushed to `main` and triggered `cd.yml`,
because the workflow still had a `push` trigger. The external prerequisites were present,
so the workflow published `0.1.0` to Quay and deployed it by digest. A second
documentation merge (`21772cc`) produced a second publication after the workflow was
disabled through the GitHub API; the disable reported `disabled_manually` but did not
prevent the push-triggered run.

The closure records had to be corrected from "remote publication postponed" to "two remote
publications completed", with the current digest named and the first digest marked as
superseded. The automatic trigger had to be removed so that a documentation merge cannot
publish again. P-02 was not verified because no analysis artifact covered this
documentation and CI change set.

The problem is therefore a delivery-control and record-accuracy problem, not a
product-behaviour problem.

## Scope

### In scope

- Analysis of the post-closure documentation and CI correction change set.
- The CD trigger decision: remove the automatic `push` trigger and keep `workflow_dispatch`.
- The record correction: both publications, source commits, runs, digests, current tag and
  superseded digest.
- The verification that a merge with the corrected `cd.yml` produces no CD run.
- The P-02 analysis evidence and the gate/README references that depend on it.

### Out of scope

- Application code, configuration, dependencies or HTTP contract.
- A new functional or non-functional requirement, or a change to `REQ-HELLO-001` or
  `REQ-STATUS-001`.
- Remote rollback, a second environment, staging or production.
- Tag immutability or a full release-process redesign.
- Independent review; the repository has no independent reviewer.
- Rotation of the exposed operator credential; it is recorded as a risk and remains a
  separate security action.

## Existing system context (verified)

| Area | Verified state | Evidence |
| --- | --- | --- |
| Service | Java 21 / Spring Boot; `GET /api/hello`, `GET /api/status` | Existing source and tests |
| CI | `ci.yml` builds, tests and scans; no publication path | `.github/workflows/ci.yml` |
| CD | `cd.yml` publishes by tag, records the digest, deploys by digest, smoke-tests `/api/hello` and prepares promotion | `.github/workflows/cd.yml`; CD runs 41332436297 and 41334455398 |
| Remote image | `quay.io/odysseia/hello-service:0.1.0`; current digest `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17`; first digest `sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b` superseded | CD artifacts; `blueprint/changes/p11-remote-publication.md` |
| Local release | `0.1.0` at `870500c`; rollback to `0.0.1-SNAPSHOT` from `f2b03e5` exercised | `blueprint/delivery/release-0.1.0.md` |
| Trigger correction | `cd.yml` has `workflow_dispatch` only; merges `4a79c6d` and `45d125b` produced CI only | `.github/workflows/cd.yml`; runs 41336511334 and 41336815538 |
| Gates | P-11 closed with self-approval; P-02 was not verified before this record | `blueprint/gates.md` |

The service, CI and local deployment are otherwise unchanged. The change set is
documentation plus the CD trigger; it does not add a runtime dependency or alter the image.

## Domain concepts

- **Publication trigger** — the event that starts `cd.yml`. The change separates an
  explicit human action (`workflow_dispatch`) from an incidental repository event (`push`).
- **Manual dispatch** — an operator explicitly starts the workflow on the commit to
  publish. It is an intent signal, not a version number.
- **Immutable digest** — the registry identity that does not change when a tag is moved.
  Deployment and smoke tests use the digest.
- **Mutable tag** — `0.1.0` can be republished with a new digest by a later manual
  dispatch. The tag is a convenience reference, not the deployment identity.
- **Promotion** — moving the same digest to an environment without rebuilding. The
  mechanism is prepared but no second environment exists.
- **Rollback** — returning to a previously verified version. The local rollback was
  exercised; the remote rollback was not.
- **Closure evidence** — the artifact and recorded result that let a third party decide a
  gate. A workflow file or an intention is not evidence that the workflow ran.
- **Self-review** — a review by the author when no independent reviewer exists. The record
  must state that independence is unavailable.

## Dependencies and integrations

| Dependency / integration | Role in this change set | Impact |
| --- | --- | --- |
| GitHub Actions `push` and `workflow_dispatch` events | Select the CD trigger | `push` removed; `workflow_dispatch` retained |
| GitHub API | Create, inspect and merge PRs; retrieve workflow runs | Evidence collection; credential handling is a recorded risk |
| Quay.io with OIDC | Publish the image and return the digest | Existing path reused; no credential recorded |
| Podman | Deploy the published digest and run the smoke test | Existing path reused; no rebuild |
| Documentation and gates | Record the change, the evidence and the gate state | Updated; no runtime effect |
| Gradle/JUnit | Application test evidence | Reused; no test code change |

No new runtime or build dependency is introduced.

## Alternatives considered

| Option | Assessment | Outcome |
| --- | --- | --- |
| Keep the `push` trigger and rely on the workflow disable API | The API reported `disabled_manually`, yet CD run 41334455398 executed on the next push. The control is not reliable for this purpose. | Rejected |
| Keep the `push` trigger and rely on branch protection or manual merge discipline | A documentation merge is still a push; discipline does not make publication deliberate, and the second run proved the failure mode. | Rejected |
| Keep the `push` trigger with path filters | Path filters still publish on source changes and encode a heuristic, not an intent. A documentation-only change is not the only path that can trigger an unintended publication. | Rejected for this change |
| Trigger publication only on release tags | A tag-based release process is a larger change: it needs tag creation, protection and a release procedure. It is a possible future direction. | Deferred |
| Move publication to a separate workflow with an environment approval | Stronger control, but adds environment configuration and another workflow. The manual dispatch already provides an explicit gate. | Deferred |
| Remove `cd.yml` entirely | Stops publication, but loses the manual publication path, the digest record and the deployment/smoke evidence. | Rejected |
| Remove `push` and keep `workflow_dispatch` | Smallest change that makes publication deliberate, preserves the existing publication path and is verifiable from the workflow file and the merge run list. | **Chosen** |

**Direction chosen:** manual-only CD. The existing `workflow_dispatch` path is retained
unchanged; the `push` trigger is removed. The record names both past publications, the
current digest and the superseded digest. The merge of the correction is verified by
checking that the push event produced CI only.

## Constraints

- No application code, dependency, configuration or HTTP contract change.
- No new functional or non-functional requirement; no requirement version is created.
- The manual publication path and all existing CD steps remain available.
- Records shall name source commits, workflow runs and digests; the first digest is
  superseded, not erased.
- The tag `0.1.0` remains mutable; the digest is the deployment identity.
- No remote rollback or second environment is claimed.
- No independent reviewer exists; the analysis is self-reviewed and records that
  limitation.
- The trigger correction shall not publish on merge; this is verified by the run list for
  the merge commit.

## Risks

| ID | Risk | Evidence / basis | Mitigation / disposition |
| --- | --- | --- | --- |
| R-01 | A documentation merge publishes automatically again. | PR #5 merge published; PR #6 merge published despite the disable attempt. | `push` trigger removed; merges `4a79c6d` and `45d125b` produced CI only. |
| R-02 | A manual dispatch republishes the mutable tag with a new digest. | `0.1.0` tag is mutable; a dispatch can publish again. | Publication is explicit; the digest is recorded; future tag immutability remains an improvement. |
| R-03 | Records drift from the actual runs or digests. | The first records said "postponed" after publication; the first digest was superseded. | Change record, review and gates updated; first digest marked superseded; current digest named. |
| R-04 | The workflow disable API is treated as a reliable control. | `disabled_manually` did not prevent CD run 41334455398. | Recorded as a failed control; the trigger change is the control. |
| R-05 | No independent reviewer validates the analysis or delivery records. | Single-maintainer repository; all reviews are self-reviews. | Recorded as a limitation; P-02 closes with a self-review, not an independent approval. |
| R-06 | Remote rollback cannot be demonstrated. | No previous remote release or second environment is recorded. | Recorded as a limitation; local rollback evidence remains. |
| R-07 | The GitHub API credential used by the operator was printed in the session transcript. | Credential helper output was captured during PR automation. | Treat as compromised; revoke/rotate; use `gh auth` or a secret store; never print credential fields. A working-tree pattern scan found no token in repository files. Rotation remains open. |
| R-08 | The P-02 closure becomes a paperwork exercise. | The change set is documentation and CI only; no product behaviour changes. | The analysis states the actual decision, alternatives, risks and traceability; the gate is closed only with this artifact and the self-review. |

## Assumption register

| ID | Assumption | Consequence if false | Status |
| --- | --- | --- | --- |
| A-01 | GitHub Actions uses the workflow file from the pushed commit for a `push` event. | Removing the `push` trigger would not prevent CD on merge; an environment protection or workflow-disable control would be required. | Confirmed by merges `4a79c6d` and `45d125b`: one CI run each, no CD run. |
| A-02 | An explicit `workflow_dispatch` is an acceptable publication path for this PoC. | The project needs an automated or tag-based release trigger, which is a different design. | Confirmed by the operator decision recorded in this change set. |
| A-03 | The tag `0.1.0` may remain mutable for this PoC. | A consumer cannot treat the tag as an immutable identity; tag protection or digest-only references would be required. | Recorded as a limitation; the digest is the deployment identity. |
| A-04 | No second environment exists, so remote rollback cannot be exercised. | The remote rollback claim would be missing evidence and P-11 would need to reopen. | Confirmed by the delivery and deployment records. |
| A-05 | This change set changes no functional requirement or non-functional threshold. | The change would need to return to phase 01 and update the requirement before design. | Confirmed by the change set: documentation and `cd.yml` trigger only. |
| A-06 | The current remote digest is `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17`. | The delivery records and gate evidence would name the wrong artifact. | Confirmed by CD run 41334455398 and its `image-digest` artifact. |
| A-07 | No token is stored in repository files. | The repository would contain a secret and require immediate secret rotation and history cleanup. | Confirmed by a working-tree pattern scan of repository files; the session exposure still requires rotation. |

## Decisions

- **D1 — Trigger model:** publication is manual-only through `workflow_dispatch`; the
  `push` trigger is removed. Chosen because it is the smallest control that makes
  publication deliberate and was verified by the merge run list.
- **D2 — Keep the CD workflow:** the publication, digest, deployment, smoke-test and
  promotion-preparation steps remain. Removing the workflow would discard the manual path
  and evidence.
- **D3 — Record both publications:** both source commits, runs and digests are named; the
  first digest is superseded and the second is current. The local build at `870500c`
  remains a separate artifact.
- **D4 — Verification method:** a merge is verified by the workflow runs for its commit.
  The expected result is CI only; a CD run invalidates the trigger correction.
- **D5 — No ADR:** the change is reversible, affects delivery records and one workflow
  trigger, introduces no dependency or platform, changes no quality threshold and affects
  no external contract. Under `standards/terms.md` (a)–(e), it is not a significant
  decision; it is recorded in the change, not an ADR.
- **D6 — P-02 closure:** the analysis is self-reviewed against `blueprint/02-analysis.md`.
  The gate closes with the self-review and records that independent approval is
  unavailable.

## Traceability

| Item | Evidence |
| --- | --- |
| First publication | `c7d3a86`; CI 41332436321; CD 41332436297; digest `sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b` (superseded) |
| Second publication | `21772cc`; CI 41334455181; CD 41334455398; digest `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17` (current) |
| Trigger correction | PR #7; commit `4a79c6d`; CI 41336511334; no CD run |
| Post-merge verification | PR #8; commit `45d125b`; CI 41336815538; no CD run |
| Local release and rollback | `0.1.0` at `870500c`; rollback `0.0.1-SNAPSHOT` from `f2b03e5` |
| Change record | `blueprint/changes/p02-closure-analysis.md` |
| Delivery records | `blueprint/changes/p11-remote-publication.md`; `blueprint/reviews/remote-publication.md`; `blueprint/delivery/release-0.1.0.md` |
| Requirements | `REQ-HELLO-001`, `REQ-STATUS-001`; unchanged, delivered by `0.1.0` |
| Gate | P-02 in `blueprint/gates.md` |

## Files affected

| File | Change expected | Basis |
| --- | --- | --- |
| `blueprint/analisys/closure-docs-ci.md` | New analysis record | P-02 evidence |
| `blueprint/changes/p02-closure-analysis.md` | New change record | Traceability |
| `blueprint/gates.md` | P-02 artifact and state updated | Gate evidence |
| `README.md` | P-02 status references updated | Consumer view |
| `blueprint/11-continuous-delivery.md` | P-02 status reference updated | Delivery record |
| `blueprint/improvements/continuous-improvements.md` | P-02 status reference updated | Improvement register |

No application, workflow, test or dependency file changes in this record.

## Readiness for the next phase

The change set is implemented and verifiable. No phase 03/04 work is required because the
change introduces no structure, interface, data or dependency. The remaining actions are
operational and separate: rotate the exposed GitHub credential, and decide whether to
pursue tag immutability, environment protection or remote rollback as future improvements.

## Review against `blueprint/02-analysis.md`

| Requirement of the phase | How this record satisfies it | Verdict |
| --- | --- | --- |
| Understand current behaviour | The service, CI, CD, remote and local state are verified in the context table. | Met |
| Identify domain concepts | Publication trigger, manual dispatch, digest, tag, promotion, rollback, closure evidence and self-review are defined. | Met |
| Identify dependencies and integrations | GitHub Actions, GitHub API, Quay/OIDC, Podman, documentation and Gradle/JUnit are listed with impact. | Met |
| Identify alternatives | Seven options are assessed; the chosen direction and the rejected/deferred reasons are recorded. | Met |
| Evaluate constraints and risks | Constraints and R-01 … R-08 are recorded with mitigations. | Met |
| Record assumptions with consequences | A-01 … A-07 each state the consequence if false and a status. | Met |
| No open ambiguity changing a requirement or threshold | No functional or non-functional change; A-05 confirms the scope. | Met |
| Direction is implementable | The workflow change is in place; merges produced CI only; no new dependency. | Met |

**Verdict:** P-02 evidence is complete for the post-closure documentation and CI
correction change set. The analysis is self-reviewed against `blueprint/02-analysis.md`.
This is not an independent review; the repository has no independent reviewer. The gate
closes with that limitation recorded.

## Evidence log

| Check | Method | Result |
| --- | --- | --- |
| Workflow trigger | Read `.github/workflows/cd.yml` | `on: workflow_dispatch: {}` only; no `push` |
| Merge runs | GitHub Actions runs for `4a79c6d` and `45d125b` | One CI run each; no CD run |
| Publications | CD runs 41332436297 and 41334455398 | Both succeeded; digests recorded |
| Current digest | CD `image-digest` artifact 13095025573 | `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17` |
| Tests | `.\gradlew.bat test --offline --no-daemon --console=plain` | `BUILD SUCCESSFUL`; no application code changed by this record |
| Diff hygiene | `git diff --check` | Clean |
| Token pattern scan | `rg -l` for GitHub token patterns in the working tree | No files matched |

## Post-closure update

This analysis records the state before `CHG-DEL-005`. Its assumption A-04, risk R-06 and
the future-improvement list are superseded for the staging scope: on 2026-11-06 the GitHub
`staging` environment accepted a digest deployment (run 42241159036) and a remote rollback
by digest (run 42241543717), both successful. The deployment still runs on the Actions
runner, so the external-host limitation remains. See
`blueprint/changes/p11-staging-rollback.md` and
`blueprint/deployment/remote-staging-0.1.0.md`.
