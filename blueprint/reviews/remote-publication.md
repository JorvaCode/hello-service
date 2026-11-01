# Code Review: post-closure remote publications of `0.1.0` and manual-only CD

## Review scope

- `blueprint/changes/p11-remote-publication.md`
- `.github/workflows/cd.yml` trigger change
- `blueprint/delivery/release-0.1.0.md` remote-publication section
- `blueprint/deployment/local-0.1.0.md` remote-publication link
- `blueprint/11-continuous-delivery.md`, `blueprint/12-deployment.md` and
  `blueprint/14-continuous-improvement.md` post-closure updates
- `blueprint/improvements/continuous-improvements.md` closure record
- `blueprint/gates.md` P-11 row
- `README.md` remote-publication section
- Post-closure updates in the phase 11, 12, 13 and 14 change records and reviews

Checklist: `blueprint/07-code-review.md`.

Evidence: CI runs 41332436321 and 41334455181; CD runs 41332436297 and 41334455398;
the CD `image-digest` artifacts; the CD smoke tests; the local release and rollback records
at `870500c`.

This review is a self-review by the same agent that produced the remote-publication
records. It is not an independent review.

## Board

### 1. Requirement satisfied?

- `REQ-HELLO-001`: the current remote image returns `200`, `text/plain`, `Hello, World!` on
  `GET /api/hello`; the second CD smoke test records the result.
- `REQ-STATUS-001`: the remote image is built from the same versioned source and carries
  the `/api/status` liveness endpoint; the CD smoke test does not call it, so remote
  verification of `REQ-STATUS-001` is limited to the shared build and the local checks.
- No requirement is changed by this record.

### 2. Design consistent with architecture?

- No application code or dependency changes. The only configuration change is the
  `cd.yml` trigger; the remote deployments use the existing OCI image and Podman deployment
  pattern.
- The `cd.yml` change removes the automatic `push` trigger and keeps `workflow_dispatch`;
  it changes when publication happens, not how the artifact is built or deployed.
- No ADR is raised; the change records an execution of the existing delivery design and a
  correction to its trigger.

### 3. Does each new name assert a decision or a behaviour a third party can check?

- Both remote images, digests, registry namespace, auth mode, source commits and CI/CD runs
  are named and independently checkable.
- The manual-only trigger is visible in `.github/workflows/cd.yml`.
- No new code name is introduced.

### 4. Design quality per `standards/code-design.md`?

- No application code is introduced. The records follow the information items in
  `blueprint/11-continuous-delivery.md` and `blueprint/12-deployment.md`.
- The workflow change is minimal: it removes the automatic trigger and keeps the existing
  publication path available through `workflow_dispatch`.

### 5. Any pattern or abstraction without a stated problem, evidence and scope?

- None. The change records that the existing CD pattern was executed twice and that its
  trigger was narrowed after the second publication.

### 6. Error handling appropriate?

- The CD workflow fails fast when external prerequisites are missing and validates the
  digest format before publishing. Both runs exercised the success path.
- The API disable switch did not prevent the second push-triggered run. The trigger change
  is the reliable control; the failed disable attempt is recorded as a limitation.
- No new application error path is introduced.

### 7. Security and sensitive data?

- The workflow uses OIDC and masks the OIDC token and registry credential. No secret is
  recorded in the change record or review.
- The remote images are published under the configured `odysseia` namespace.

### 8. Tests and verification adequate?

- Both CD jobs ran the phase 09 build and tests, published the image, deployed by digest and
  passed the `GET /api/hello` smoke test.
- The local 17-test suite remains the application test evidence; the remote smoke test is
  functional evidence, not a replacement for the suite.
- Remote `GET /api/status` and remote rollback were not exercised; both are recorded as
  limitations.

### 9. Performance concerns addressed?

- No runtime change. The remote publication adds no request-path work.
- No performance test is required for this delivery record.

### 10. No unnecessary scope?

- No requirement or application code change is introduced.
- The improvement items remain deferred.
- Two remote publications and one workflow trigger correction are recorded; no
  second-environment promotion is claimed.
- Manual-only CD is a deliberate scope change to prevent documentation merges from
  publishing artifacts; it is recorded and reviewable.

## Findings

| # | Severity | Finding | Disposition |
| --- | --- | --- | --- |
| F-1 | Info | The first remote publication was triggered by a documentation-only merge, not by a planned release step. | Recorded as the trigger; the postponed scope is superseded for this event. |
| F-2 | Info | The second remote publication occurred after the CD workflow was disabled through the API and reported `disabled_manually`. | Recorded as a failed control; the `push` trigger is removed so the condition cannot recur. |
| F-3 | Info | The remote tag `0.1.0` is mutable; a manual dispatch can republish it with a new digest. | Recorded as a workflow limitation; manual-only publication is the current control. |
| F-4 | Info | Remote rollback was not exercised and no previous remote release is recorded. | Recorded as a limitation; the local rollback remains the exercised path. |
| F-5 | Info | The remote images were built from `c7d3a86` and `21772cc`, while the local release artifact was built from `870500c`. | All three identities are recorded separately; the local record is not presented as either remote build. |
| F-6 | Info | The remote smoke test covers `GET /api/hello` but not `GET /api/status`. | Recorded as a verification limitation; the shared build and local tests cover the status contract. |
| F-7 | Info | No independent reviewer exists; this is a self-review. | Recorded as a process limitation; independent approval remains unavailable. |

## Verdict

Approved for the corrected remote-publication records and the manual-only CD policy on the
recorded evidence. Every finding has a disposition. Both remote images, digests, source
commits and smoke results are traceable; the mutable-tag, failed-disable, remote-rollback
and remote-status limitations are explicit. Independent approval remains unavailable.

## Reviewer, independence and date

- Reviewer: OpenCode review assistant. It produced the remote-publication records in this
  session, so this is a self-review, not an independent review.
- Independence limitation: the repository is a single-maintainer repository and no
  independent human reviewer is recorded. This record closes the review with a
  solo-maintainer self-review; it does not claim an independent approval. If the process
  requires one, the maintainer must obtain and record it.
- Date: 2026-11-01.
