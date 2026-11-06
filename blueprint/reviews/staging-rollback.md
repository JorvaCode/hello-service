# Review: staging target and digest rollback

## Scope

Reviews `CHG-DEL-005` in `.github/workflows/cd.yml`:

- `target_environment` (`staging` default, `production`) selects the GitHub environment.
- Empty `image_digest` keeps the publish-and-deploy path.
- Non-empty `image_digest` validates `sha256:<64 hex>`, skips build/publication and deploys
  that existing digest.
- The same digest-derived image is deployed and smoke-tested.

## Evidence

- Workflow commit: `7a116d9a9191a0e6235b974a6caa717131c35b74`; merge to `main`:
  `6d5fa89a3a4de7ae911e50054ab1be2a234e1b02`.
- PR #13 CI run `42240905627`: `success`.
- Workflow YAML parsed locally before merge.
- Staging deployment run
  [42241159036](https://github.com/JorvaCode/hello-service/actions/runs/42241159036):
  `success`, digest `0d8deff`, smoke passed, build/publish steps skipped.
- Unavailable-digest attempt run
  [42241318818](https://github.com/JorvaCode/hello-service/actions/runs/42241318818):
  `failure` with `manifest unknown` for the superseded `4d06e51` digest.
- Remote rollback run
  [42241543717](https://github.com/JorvaCode/hello-service/actions/runs/42241543717):
  `success`, digest `81ca2f2` (`0.0.1-SNAPSHOT`), smoke passed, build/publish steps skipped.
- Records: `blueprint/changes/p11-staging-rollback.md`,
  `blueprint/testing/staging-rollback.md`,
  `blueprint/deployment/remote-staging-0.1.0.md`.

This is a self-review by the agent that produced the change. It is not an independent
review.

## Board

### 1. Does the change match the requirement?

Yes. The gap was that promotion to a second environment and remote rollback were not
executable. The workflow now selects `staging` or `production` and supports deploying an
existing digest without rebuilding.

### 2. Is the rollback path safe?

The requested digest is validated against `^sha256:[0-9a-f]{64}$` before use. Digest mode
skips publishing and tag mutation, so it cannot overwrite the immutable release tag. The
failed attempt demonstrated that a digest listed by the Quay tag API is not necessarily
pullable; the procedure now records that pullability must be verified before choosing a
rollback target.

### 3. Is the deployed artifact verified?

Yes. Each successful run records the digest-derived image reference and executes the same
`GET /api/hello` smoke test. Both successful runs returned `200`,
`text/plain;charset=UTF-8`, `Hello, World!`.

### 4. Are environment controls preserved?

Yes. The job uses the selected GitHub environment; both `staging` and `production` have a
branch policy for `main`. Publish mode retains the OIDC/robot authentication and immutable
tag guard.

### 5. Are limitations stated?

Yes. The deployment still runs on the GitHub Actions runner, not an external host. There is
no independent reviewer. The failed rollback target is retained as a finding rather than
hidden.

## Findings

| ID | Severity | Finding | Disposition |
| --- | --- | --- | --- |
| R-01 | Medium, resolved | The superseded `0.1.0` digest listed by the Quay tag API was not pullable. | The exercised rollback used the pullable `0.0.1-SNAPSHOT` digest `81ca2f2`; the failure is recorded in F-01. |
| R-02 | Info | The staging deployment runs on the GitHub Actions runner. | Accepted and recorded as the PoC boundary; no external host is claimed. |
| R-03 | Info | No independent reviewer exists. | Recorded; this is a solo-maintainer self-review. |

## Verdict

Approved for the local/remote PoC scope on the recorded evidence. The staging deployment
and remote rollback by digest were exercised successfully. This review does not claim an
independent approval or an external production target.

## Reviewer, independence and date

- Reviewer: OpenCode review assistant. It produced this change and its evidence, so this is
  a self-review, not an independent review.
- Independence limitation: the repository has one maintainer and no independent human
  reviewer is recorded.
- Date: 2026-11-06.
