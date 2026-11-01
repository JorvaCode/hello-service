# 11 — Continuous Delivery

Continuous Delivery takes a validated artifact through controlled release and deployment stages.

## Typical flow
CI
  -> artifact
  -> registry
  -> environment validation
  -> deployment
  -> smoke tests
  -> promotion

## External prerequisites (non-normative)

This section describes external prerequisites referenced by `cd.yml`. It does not define
a new gate, approve a promotion or assert that any prerequisite exists.

### Prerequisites checked by the workflow configuration

The workflow configuration contains explicit checks for these inputs:

- GitHub repository variable `QUAY_NAMESPACE`.
- GitHub repository variable `QUAY_AUTH`, defaulting to `oidc` when empty.
- For `oidc`: GitHub repository variable `QUAY_ROBOT_USERNAME`.
- For `robot`: GitHub Actions secrets `QUAY_USERNAME` and `QUAY_TOKEN`.

The prerequisite step is written to exit with an explicit blocked message when one of
those checked inputs is missing. This describes the workflow configuration only; no
`cd.yml` run has been verified in this closure, so no runtime fail-safe behavior is
claimed.

### External conditions not verified

The following conditions are expected but are not explicitly checked by the prerequisite
step:

- A Quay destination repository that exists and grants write permission to the selected
  identity.
- OIDC federation, or a robot credential, accepted by Quay.
- A configured target environment for deployment and promotion.

### Workflow execution status

- CI: run 10 (`pull_request`) on commit `6ccd170` completed with `success`; every step
  passed, including build and test, container image build, identity record and artifact
  upload. Evidence: GitHub Actions run `41043812371`, artifact `artifact-metadata`.
- Packaging: run 13 (`pull_request`) on commit `2e54476` completed with `success`; the
  secret scan, version resolution, versioned image, identity record and artifact uploads
  passed. Artifacts: `artifact-metadata`, `hello-service-0.1.0` and `gitleaks-report`.
  Evidence: GitHub Actions run `41046888744`.
- Local promotion: version `0.1.0` was promoted to the `local` environment on
  2026-10-30. The release identity, checks, promotion result and rollback rehearsal are
  recorded in `blueprint/delivery/release-0.1.0.md`; the deployment record is
  `blueprint/deployment/local-0.1.0.md`.
- Remote publications: merging PR #5 pushed `c7d3a86` to `main` on 2026-11-01 and triggered
  `cd.yml`; run 41332436297 published
  `quay.io/odysseia/hello-service:0.1.0@sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b`
  and deployed it by digest. Merging PR #6 pushed `21772cc` and triggered run 41334455398,
  which published
  `quay.io/odysseia/hello-service:0.1.0@sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17`
  and superseded the first digest. Automatic push publication is now disabled; `cd.yml`
  requires `workflow_dispatch`. The earlier statement that no `cd.yml` execution had
  occurred is superseded by `blueprint/changes/p11-remote-publication.md`.

### Current PoC boundary

- Remote publications to Quay were completed for `0.1.0` on 2026-11-01; the current digest
  is
  `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17`;
  see `blueprint/changes/p11-remote-publication.md`. Promotion to a second environment is
  not executed.
- `ci.yml` no longer contains a publication path: it builds, tests and records the image
  identity only. No publication by that workflow is claimed.
- `cd.yml` contains the remote publication path through `workflow_dispatch`, including
  OIDC, registry login and image push. The automatic `push` trigger is removed after two
  post-closure publications; publication requires an explicit dispatch.
- The local promotion of `0.1.0` is recorded and its rollback path was rehearsed. The
  remote publications are recorded separately and the tag `0.1.0` remains mutable.
- The gate identifiers P-02, P-06, P-07, P-10 and P-11 are defined in `blueprint/gates.md`.
- P-11 is closed with the local promotion and remote publication evidence and a
  solo-maintainer self-approval; the record does not claim an independent approval. P-02
  remains open; P-07 is closed with the same solo-maintainer limitation. The Quality gate
  above remains applicable to any promotion, local or remote.

## Local promotion record

- Release: `0.1.0` at `870500c`.
- Environment: `local` (single-node Podman on Windows/WSL).
- Promotion result: local build green (17 tests), image built, container ran as `appuser`,
  and smoke tests passed on `127.0.0.1:18080`.
- Rollback: `0.0.1-SNAPSHOT` from `f2b03e5` was built, run and probed; `0.1.0` was
  restored and probed again.
- Records: `blueprint/delivery/release-0.1.0.md`,
  `blueprint/deployment/local-0.1.0.md`.
- Approval: self-review in `blueprint/reviews/delivery-closure.md`; no independent
  reviewer exists. P-11 is closed with this self-approval and does not claim an
  independent approval.

## Remote publication records

Two publications of `0.1.0` were executed on 2026-11-01:

| Field | First | Second (current) |
| --- | --- | --- |
| Source commit | `c7d3a86b04e7b39a8ccfa55c37c8ae2c3dc4c2c0` | `21772ccb0012c36e2264ede3171758ddfd773525` |
| CI run | 41332436321 (`success`) | 41334455181 (`success`) |
| CD run | 41332436297 (`success`) | 41334455398 (`success`) |
| Digest | `sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b` (superseded) | `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17` |
| Environment | GitHub Actions runner, Podman by digest; no staging or production target | Same |
| Registry | `quay.io/odysseia/hello-service:0.1.0` | Same |
| Auth mode | `oidc` | `oidc` |
| Promotion result | Published, deployed by digest and smoke-tested | Published, deployed by digest and smoke-tested |
| Promotion mechanism | Prepared for the same digest; no second environment configured | Prepared for the same digest; no second environment configured |
| Rollback | Not exercised; no previous remote release recorded | Not exercised; no previous remote release recorded |
| Records | `blueprint/changes/p11-remote-publication.md`, `blueprint/reviews/remote-publication.md` | Same |
| Approval | Solo-maintainer self-review; no independent approval | Solo-maintainer self-review; no independent approval |

The tag `0.1.0` is mutable and currently points to the second digest. Automatic push
publication is disabled; publication requires `workflow_dispatch`.

## Environments
Common examples:
- Development
- Test
- Staging
- Production

The exact environments depend on the project.

## Information items
### Inputs
- **Versioned artifact** — the output of `10-packaging.md`, with its identity.
- **Environment inventory** — the environments, their configuration source, and their owners.
- **Promotion criteria** — the checks and approvals each environment requires, declared rather than assumed.
- **Delivered scope** — the `FR-nn`, `NFR-nn` and change identifiers the artifact carries, from `01-requirements.md` and the change records. This is the endpoint of the chain that starts in requirements and a release record without it can describe what exists but not what was asked for.

### Outputs
- **Release record** — the artifact version, the identity from packaging, the environments it reached, the timestamp of each promotion, and the requirements and changes it delivers. A release that delivers no requirement, a base release, says so.
- **Promotion result** — the automated checks and the approvals that allowed each promotion, recorded per environment.
- **Rollback plan** — how to return to the previous version, and the evidence that it was rehearsed rather than only written down.

## Quality gate
Each promotion that happened is recorded with the checks and approvals that allowed it, and the rollback plan names a target version that was verified to work. The release record names the requirements and changes it delivers, or states that it delivers none, and each named criterion has a check in the criterion coverage of `06-testing.md`. A reviewer who did not take part in the release reaches the same verdict on what shipped and on whether it can be withdrawn, and can answer which requirement the release satisfies without consulting its author.
