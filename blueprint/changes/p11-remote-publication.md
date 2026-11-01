# Change: `CHG-DEL-003` — post-closure remote publication of `0.1.0`

## Identity

| Field | Value |
| --- | --- |
| Change ID | `CHG-DEL-003` |
| Date | 2026-11-01 |
| Owner | Jorva (maintainer) |
| Status | Implemented and verified; self-reviewed |
| Related gates | P-11 (delivery) |
| Related requirements | `REQ-HELLO-001`, `REQ-STATUS-001` |
| Trigger | Merge of PR #5 (`195d40c`) into `main` (`c7d3a86`) |

## Context and trigger

The phase 11, 12, 13 and 14 records closed with remote publication to Quay marked as
postponed. The `cd.yml` workflow already contained a publication path triggered by `push`
to `main` and by `workflow_dispatch`. Merging PR #5, the phase 14 documentation change,
pushed `c7d3a86` to `main` and triggered `cd.yml`. The external prerequisites were
present, so the workflow did not fail fast: it published the image and deployed it by
digest.

This record documents the resulting remote publication and supersedes the postponed
limitation for this event. It does not change a requirement, introduce a new version, or
claim a second-environment promotion.

## Evidence

| Evidence | Result | Link |
| --- | --- | --- |
| CI on `c7d3a86` | `success` | <https://github.com/JorvaCode/hello-service/actions/runs/41332436321> |
| CD on `c7d3a86` | `success` | <https://github.com/JorvaCode/hello-service/actions/runs/41332436297> |
| Version | `0.1.0` | CD step "Resolve release version" |
| Registry namespace | `odysseia` | CD step "External prerequisites" |
| Auth mode | `oidc` | CD step "External prerequisites" |
| Remote image | `quay.io/odysseia/hello-service:0.1.0` | CD step "Publish to Quay.io" |
| Remote digest | `sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b` | `image-digest` artifact and CD log |
| Deploy by digest | `quay.io/odysseia/hello-service@sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b` | CD step "Deploy by digest" |
| Smoke test | `GET /api/hello` -> `200`, `text/plain;charset=UTF-8`, `Hello, World!` | CD step "Smoke test" |
| Promotion | Mechanism prepared; no second environment executed | CD step "Promotion mechanism" |

No secret, token or credential is recorded. The OIDC token and registry credential were
masked by the workflow.

## What changed

- `blueprint/delivery/release-0.1.0.md`: adds the remote publication result and digest,
  while retaining the local promotion identity.
- `blueprint/deployment/local-0.1.0.md`: scopes the local record and links to the remote
  publication.
- `blueprint/11-continuous-delivery.md`, `blueprint/12-deployment.md` and
  `blueprint/14-continuous-improvement.md`: add the post-closure CD execution and remote
  deployment evidence.
- `blueprint/improvements/continuous-improvements.md`: changes the remote-publication
  closure item from `Postponed` to `Completed` with evidence; the improvement items remain
  deferred.
- `blueprint/gates.md`: updates the P-11 evidence to include the remote publication and
  digest.
- `README.md`: replaces the postponed-publication section with the remote-publication
  result.
- Historical closure and change records: add post-closure updates rather than rewriting
  their original scope.

## Verification

- The CD job completed every step successfully, including `Publish to Quay.io and record
  immutable digest`, `Deploy by digest` and the smoke test.
- The digest artifact `image-digest` contains the image reference and digest above.
- The remote image was built from `c7d3a86`, the merge commit that contains the phase 14
  documentation change. The local release artifact was built from `870500c`; the two
  artifacts are separate builds of version `0.1.0`; the local build is identified by an
  image ID and the remote build by a digest. The local record is not rewritten as if it
  were the remote build.

## Rollback and limitations

- The remote digest is immutable; the tag `0.1.0` is mutable and now points to the
  published digest.
- A remote rollback was not exercised. No previous remote release or target environment is
  recorded, so no remote rollback claim is made.
- The local rollback to `0.0.1-SNAPSHOT` remains recorded and exercised in
  `blueprint/delivery/release-0.1.0.md`.
- The promotion mechanism is prepared but not executed; there is no staging or production
  target.
- The change is reviewed by a solo-maintainer self-review in
  `blueprint/reviews/remote-publication.md`; it does not claim independent approval.
- Merging a later documentation change to `main` will trigger `cd.yml` again and may
  republish the mutable `0.1.0` tag with a new digest. This is a property of the current
  workflow, not a claim that the published digest is immutable across builds.

## Review

`blueprint/reviews/remote-publication.md`.
