# Change: `CHG-DEL-003` — post-closure remote publications of `0.1.0` and manual-only CD

## Identity

| Field | Value |
| --- | --- |
| Change ID | `CHG-DEL-003` |
| Date | 2026-11-01 |
| Owner | Jorva (maintainer) |
| Status | Implemented and self-reviewed; operational trigger check pending merge |
| Related gates | P-11 (delivery) |
| Related requirements | `REQ-HELLO-001`, `REQ-STATUS-001` |
| Trigger | Merge of PR #5 (`195d40c`) into `main` (`c7d3a86`); merge of PR #6 (`b52b71e`) into `main` (`21772cc`) after a disable attempt |

## Context and trigger

The phase 11, 12, 13 and 14 records closed with remote publication to Quay marked as
postponed. The `cd.yml` workflow already contained a publication path triggered by `push`
to `main` and by `workflow_dispatch`. Merging PR #5, the phase 14 documentation change,
pushed `c7d3a86` to `main` and triggered `cd.yml`. The external prerequisites were
present, so the workflow did not fail fast: it published the image and deployed it by
digest.

That first publication was recorded by PR #6. Before merging PR #6, the CD workflow was
disabled through the GitHub API and reported `disabled_manually`. The merge still produced
CD run 41334455398 on `21772cc`, which published a second digest. The disable switch did
not prevent the push-triggered run.

This change records both publications, updates the current digest to the second
publication and removes the automatic `push` trigger from `cd.yml`. Publication now
requires an explicit `workflow_dispatch`. Because a `push` event uses the workflow file in
the pushed commit, merging this change will not trigger CD again.

The change does not change a requirement, introduce a new version, or claim a
second-environment promotion.

## Evidence

| Evidence | Result | Link |
| --- | --- | --- |
| First CI on `c7d3a86` | `success` | <https://github.com/JorvaCode/hello-service/actions/runs/41332436321> |
| First CD on `c7d3a86` | `success` | <https://github.com/JorvaCode/hello-service/actions/runs/41332436297> |
| First remote digest | `sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b` (superseded) | CD run 41332436297 `image-digest` artifact |
| Second CI on `21772cc` | `success` | <https://github.com/JorvaCode/hello-service/actions/runs/41334455181> |
| Second CD on `21772cc` | `success` | <https://github.com/JorvaCode/hello-service/actions/runs/41334455398> |
| Current remote image | `quay.io/odysseia/hello-service:0.1.0` | CD step "Publish to Quay.io" |
| Current remote digest | `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17` | CD run 41334455398 `image-digest` artifact 13095025573 |
| Deploy by digest | `quay.io/odysseia/hello-service@sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17` | CD step "Deploy by digest" |
| Smoke test | `GET /api/hello` -> `200`, `text/plain;charset=UTF-8`, `Hello, World!` | CD step "Smoke test" |
| Promotion | Mechanism prepared; no second environment executed | CD step "Promotion mechanism" |
| Disable attempt | API state `disabled_manually`; CD run 41334455398 still executed | GitHub API and CD run 41334455398 |
| Manual-only trigger | `cd.yml` has `workflow_dispatch` only | `.github/workflows/cd.yml` |

No secret, token or credential is recorded. The OIDC token and registry credential were
masked by the workflow.

## What changed

- `.github/workflows/cd.yml`: removes the `push` trigger and keeps `workflow_dispatch` only;
  publication requires an explicit dispatch on the commit to publish.
- `blueprint/delivery/release-0.1.0.md`: records both remote publications, marks the first
  digest as superseded and names the current digest.
- `blueprint/deployment/local-0.1.0.md`: scopes the local record and links to the remote
  publications.
- `blueprint/11-continuous-delivery.md`, `blueprint/12-deployment.md` and
  `blueprint/14-continuous-improvement.md`: add the post-closure CD executions and
  manual-only trigger; the delivery and deployment records name the current digest.
- `blueprint/improvements/continuous-improvements.md`: changes the remote-publication
  closure item from `Postponed` to `Completed` with the current digest and records the
  trigger lesson.
- `blueprint/gates.md`: updates the P-11 evidence to include both publications, the current
  digest and the manual-only trigger.
- `README.md`: replaces the postponed-publication section with the remote-publication
  results and the manual-only policy.
- Historical closure and change records: add post-closure updates rather than rewriting
  their original scope.

## Verification

- Both CD jobs completed every step successfully, including `Publish to Quay.io and record
  immutable digest`, `Deploy by digest` and the smoke test.
- The `image-digest` artifact for run 41334455398 contains
  `quay.io/odysseia/hello-service:0.1.0@sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17`.
- `cd.yml` now has no `push` trigger; the only trigger is `workflow_dispatch`.
- The merge of this change is the operational check: the push event must not create a CD
  run because the merge commit contains the manual-only trigger. If a CD run appears, this
  record is invalid and must be corrected.
- The first remote image was built from `c7d3a86`; the second from `21772cc`. The local
  release artifact was built from `870500c`. The three builds are separate artifacts of
  version `0.1.0`; the local build is identified by an image ID and the remote builds by
  their digests. The local record is not rewritten as if it were either remote build.
- The current tag `0.1.0` points to the second digest; the first digest is retained only as
  historical evidence.

## Rollback and limitations

- The remote digests are immutable; the tag `0.1.0` is mutable and now points to the second
  published digest. A later manual dispatch can republish the tag with a new digest.
- A remote rollback was not exercised. No previous remote release or target environment is
  recorded, so no remote rollback claim is made.
- The local rollback to `0.0.1-SNAPSHOT` remains recorded and exercised in
  `blueprint/delivery/release-0.1.0.md`.
- The promotion mechanism is prepared but not executed; there is no staging or production
  target.
- The disable switch did not prevent the second push-triggered run. The trigger change is
  the reliable control; the disable attempt is recorded as a failed control.
- The change is reviewed by a solo-maintainer self-review in
  `blueprint/reviews/remote-publication.md`; it does not claim independent approval.
- Manual-only publication means a push to `main`, including a documentation-only merge,
  does not publish. Publication requires an explicit `workflow_dispatch` on the commit to
  publish.

## Review

`blueprint/reviews/remote-publication.md`.
