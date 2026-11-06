# Test evidence: staging target and digest rollback

## Scope

Verifies `CHG-DEL-005`: `cd.yml` can deploy an existing immutable digest to the selected
GitHub environment without rebuilding or republishing, and can exercise a remote rollback
by digest.

## Environment

- Commit under test: `6d5fa89a3a4de7ae911e50054ab1be2a234e1b02` on `main`.
- Workflow: `.github/workflows/cd.yml`, `workflow_dispatch`.
- Target environment: `staging`, branch policy `main`.
- Runner: GitHub-hosted `ubuntu-latest`, Podman, no external host.
- Registry: public `quay.io/odysseia/hello-service`.
- Date: 2026-11-06.

## Validation before execution

- `python -c "import yaml; yaml.safe_load(open('.github/workflows/cd.yml'))"` parsed the
  workflow locally.
- Pull request #13 CI run `42240905627` passed build/test, secret scan, Trivy scan and
  image identity before merge.
- The workflow was merged to `main` before dispatch because both `staging` and
  `production` environment branch policies allow `main` only.

## Executed runs

| Check | Run | Input | Result |
| --- | --- | --- | --- |
| Deploy current `0.1.0` digest to staging | [42241159036](https://github.com/JorvaCode/hello-service/actions/runs/42241159036) | `target_environment=staging`, `image_digest=sha256:0d8deff...c6ce17` | `success`; digest mode; build, version, registry login, tag guard and publish steps skipped; smoke passed |
| Attempt rollback to superseded `0.1.0` digest | [42241318818](https://github.com/JorvaCode/hello-service/actions/runs/42241318818) | `image_digest=sha256:4d06e51...6e31b` | `failure`; Podman reported `manifest unknown` while pulling; no container started |
| Roll back to `0.0.1-SNAPSHOT` digest | [42241543717](https://github.com/JorvaCode/hello-service/actions/runs/42241543717) | `image_digest=sha256:81ca2f2...7e9b833` | `success`; digest mode; build and publish skipped; smoke passed |

All three runs used `workflow_dispatch` on `main` at `6d5fa89`. The `staging` environment
recorded the deployments; no independent approval exists.

## Observed evidence

Run `42241159036`:

- `Resolved digest deployment:
  quay.io/odysseia/hello-service@sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17`
- `Started ... in target environment staging`
- `SMOKE_OK target=staging code=200 content-type=text/plain;charset=UTF-8 body=Hello, World!`
- Steps `Build OCI image`, `Publish to Quay.io`, `Authenticate Quay.io` and
  `Enforce immutable release tag` were skipped.

Run `42241318818`:

- `Resolved digest deployment: ...@sha256:4d06e515...`
- `Error: ... reading manifest sha256:4d06e515...: manifest unknown`
- The failure occurred before deployment and before the smoke test.

Run `42241543717`:

- `Resolved digest deployment:
  quay.io/odysseia/hello-service@sha256:81ca2f236241fe443f4d1633b254dcc2b90e32fd3bc07b35e5969be3e7e9b833`
- `Started ... in target environment staging`
- `SMOKE_OK target=staging code=200 content-type=text/plain;charset=UTF-8 body=Hello, World!`
- The digest is the newest pullable `0.0.1-SNAPSHOT` manifest and is the exercised remote
  rollback target.

## Findings

| ID | Finding | Severity | Disposition |
| --- | --- | --- | --- |
| F-01 | The Quay tag API listed the superseded `0.1.0` digest `4d06e51` but the registry returned `manifest unknown` when Podman pulled it. | Medium - rollback target selection | Resolved by selecting and verifying a pullable previous digest. The rollback run `42241543717` succeeded. Rollback procedures must verify pullability, not rely only on the tag list. |
| F-02 | The deployment runs on the GitHub Actions runner, not a separate staging server. | Info - environment boundary | Recorded. The GitHub `staging` environment is the selected target; the PoC has no external host fleet. |
| F-03 | No independent reviewer or automated rollback test suite exists. | Info - process limitation | Recorded; this is a solo-maintainer self-review. |

## Criterion coverage

| Criterion | Check | Result |
| --- | --- | --- |
| `target_environment` selects the deployment environment | Workflow input plus environment `staging` in runs 42241159036 and 42241543717 | Pass for `staging`; `production` was not dispatched |
| Existing digest skips build and publication | Skipped steps recorded in both successful runs | Pass |
| Digest format is validated | `Resolve deployment digest` and prerequisite regex | Pass |
| Deployment uses the same digest identity | Resolved and deployed image refs match in run logs | Pass |
| Post-deployment function is verified | `GET /api/hello` -> `200`, `text/plain;charset=UTF-8`, `Hello, World!` | Pass |
| Remote rollback is exercised | Run 42241543717 deployed `0.0.1-SNAPSHOT` digest and passed smoke | Pass |
| Failure is explicit when a digest is unavailable | Run 42241318818 failed at Podman pull with `manifest unknown` | Pass |

## Gate assessment

P-11 and P-12 evidence is present for a staging deployment and a rollback by digest. The
record is a solo-maintainer self-review and does not claim independent approval. The
deployment target remains the GitHub Actions runner, not an external production host.
