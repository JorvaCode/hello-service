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
- Remote publication: merging PR #5 pushed `c7d3a86` to `main` on 2026-11-01 and triggered
  `cd.yml`. Run 41332436297 succeeded, published
  `quay.io/odysseia/hello-service:0.1.0@sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b`,
  deployed it by digest and passed the smoke test. The earlier statement that no `cd.yml`
  execution had occurred is superseded by `blueprint/changes/p11-remote-publication.md`.

### Current PoC boundary

- Remote publication to Quay was completed for `0.1.0` on 2026-11-01; see
  `blueprint/changes/p11-remote-publication.md`. Promotion to a second environment is not
  executed.
- `ci.yml` no longer contains a publication path: it builds, tests and records the image
  identity only. No publication by that workflow is claimed.
- `cd.yml` contains the remote publication path through `push` and `workflow_dispatch`,
  including OIDC, registry login and image push. It executed on `c7d3a86`; the workflow
  itself is not modified by this record.
- The local promotion of `0.1.0` is recorded and its rollback path was rehearsed. The
  remote publication is recorded separately and the tag `0.1.0` remains mutable.
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

## Remote publication record

- Release: `0.1.0` built from `c7d3a86` (merge commit of PR #5).
- Environment: GitHub Actions runner, deploying by digest to a Podman container on the
  runner; no staging or production target.
- Registry: `quay.io/odysseia/hello-service:0.1.0`.
- Digest:
  `sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b`.
- Auth mode: `oidc`.
- Promotion result: image published, deployed by digest and smoke-tested
  (`GET /api/hello` -> `200`, `text/plain;charset=UTF-8`, `Hello, World!`).
- Promotion mechanism: prepared for the same digest; no second environment was configured,
  so end-to-end promotion was not executed.
- Rollback: not exercised for the remote environment; no previous remote release is
  recorded. The local rollback remains exercised.
- Records: `blueprint/changes/p11-remote-publication.md`,
  `blueprint/reviews/remote-publication.md`.
- Approval: solo-maintainer self-review; no independent approval is claimed.

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
