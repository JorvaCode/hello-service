# Change: `CHG-DEL-004` — immutable release tags and protected CD environment

## Identity

| Field | Value |
| --- | --- |
| Change ID | `CHG-DEL-004` |
| Date | 2026-11-02 |
| Owner | Jorva (maintainer) |
| Status | Implemented and verified; self-reviewed |
| Related gates | P-11 (delivery) |
| Related requirements | None; delivery control for `REQ-HELLO-001` and `REQ-STATUS-001` |
| Trigger | Risk R-02 in `blueprint/analisys/closure-docs-ci.md`: the mutable `0.1.0` tag can be republished with a new digest |

## Requirement

Satisfies no functional or non-functional requirement directly. It adds delivery controls
around the existing `0.1.0` release without changing `REQ-HELLO-001`, `REQ-STATUS-001` or
their acceptance criteria.

## Context

The post-closure analysis recorded R-02: the image tag `0.1.0` is mutable, so a later
manual dispatch can republish it with a different digest. The digest is the immutable
identity, but a consumer that follows the tag can silently receive a different artifact.
The analysis also recorded that the CD workflow had no environment restriction beyond the
manual `workflow_dispatch` trigger.

This change closes that gap with three controls:

1. A GitHub repository ruleset that blocks update, force-push and deletion of Git tags.
2. A GitHub environment named `production`, restricted to deployments from `main`.
3. A `cd.yml` guard that refuses to publish when the version tag already exists in Quay,
   unless the dispatcher explicitly sets `allow_republish=true`.

## Design

The change alters configuration and one workflow file. It does not alter the application,
its HTTP contract, its dependencies or its data.

### Git tags

The repository had no Git tags. Tag `0.1.0` was created on `main` at
`9626aa793348d974c371b72afad7000d0fca3de6` so that the protected namespace has a concrete
release reference. The ruleset `immutable-release-tags` targets `refs/tags/*` and applies
the `deletion`, `non_fast_forward` and `update` rules.

The ruleset protects Git references, not Quay image tags. That limitation is why the
workflow guard exists.

### Environment

The `production` environment has a custom deployment branch policy that allows only
`main`. The `cd` job declares `environment: production`. A dispatch on a branch other than
`main` is blocked by the environment policy.

Required reviewers were not enabled. GitHub does not allow the user who triggered a run to
approve that run's deployment, and this repository has one maintainer; enabling required
reviewers would deadlock every deployment. The explicit `workflow_dispatch` remains the
approval signal. The limitation is recorded rather than hidden.

### Image tag guard

The guard runs after the Quay login and before the image build. It queries the public Quay
tag API:

```text
https://quay.io/api/v1/repository/<namespace>/hello-service/tag/?specificTag=<version>
```

- `404` means the repository or tag does not exist; publication proceeds.
- `200` with a non-empty tag list means the tag exists; publication is refused.
- Any other status means the guard cannot verify the tag; publication is refused.

`allow_republish=true` is the explicit operator override for an intentional correction.
The default is `false`, so an ordinary dispatch cannot overwrite `0.1.0`.

### Patterns

No pattern is applied. The change is a workflow guard and external repository configuration,
not application code.

### ADR

Not significant under `standards/terms.md` (a)–(e). The change is reversible, introduces no
dependency or platform, changes no runtime contract and changes no quality threshold. It
improves the release security posture; the control itself is recorded here rather than in an
ADR.

## Implementation

- `.github/workflows/cd.yml`:
  - adds the `allow_republish` boolean dispatch input;
  - adds `environment: production` to the `cd` job;
  - adds the `Enforce immutable release tag` step;
  - records the controls in the workflow summary and header comment.
- GitHub repository ruleset `immutable-release-tags` (id `26359956`), targeting
  `refs/tags/*`.
- Git tag `0.1.0` at `9626aa793348d974c371b72afad7000d0fca3de6`.
- GitHub environments `production` (id `26009633729`) and `staging` (id
  `26009635028`), each with a custom branch policy allowing `main`. `staging` is created in
  this change for the later remote-rollback change; this change uses `production`.
- `blueprint/11-continuous-delivery.md`: release controls and limits.
- `blueprint/improvements/continuous-improvements.md`: closure-scope row for the controls.
- `blueprint/gates.md`: P-11 evidence names the controls.
- `README.md`: consumer view of the protected tag and environment.

## Evidence

| Evidence | Result | Link |
| --- | --- | --- |
| Ruleset exists | `immutable-release-tags`, target `tag`, enforcement `active`, includes `refs/tags/*` | `GET /repos/JorvaCode/hello-service/rulesets` |
| Ruleset rules | `deletion`, `non_fast_forward`, `update` | Ruleset id `26359956` |
| Git tag | `0.1.0` at `9626aa793348d974c371b72afad7000d0fca3de6` | `git ls-remote --tags origin` |
| Production environment | id `26009633729`; branch policy `main` | `GET /repos/JorvaCode/hello-service/environments` |
| Staging environment | id `26009635028`; branch policy `main` | Same |
| Existing Quay tag | `0.1.0` already exists; the guard refuses a normal republish | `quay.io/api/v1/repository/odysseia/hello-service/tag/?specificTag=0.1.0` |
| Workflow guard | `Enforce immutable release tag` refuses an existing tag unless `allow_republish=true` | `.github/workflows/cd.yml` |
| Workflow run | CI on the change commit | Pull request run; see the merge record |

## Verification

- The ruleset and both environments were read back from the GitHub API after creation.
- The branch policy for each environment names `main`, not a wildcard.
- The tag guard was exercised against the public Quay API response for `0.1.0`; the
  response contains the tag, so a normal dispatch would be refused. No CD dispatch was
  executed for this change, because refusals are the intended outcome for the existing tag.
- YAML parsing and the CI workflow validate the workflow syntax on the pull request.
- `git diff --check` is clean.

## Security

The change adds no secret, credential or runtime input path. It uses the public Quay tag
API and the existing authenticated Podman session. The `allow_republish` input is an
explicit operator decision, not a stored secret. The environment branch policy and the tag
ruleset reduce the chance of an unintended or untraceable release.

## Deployment

No application deployment. The GitHub ruleset and environments are repository
configuration. The workflow change takes effect on the next `workflow_dispatch`.

## Rollback

- Revert `.github/workflows/cd.yml` to remove the environment, input and guard.
- Delete the ruleset `immutable-release-tags` and the `production`/`staging` environments
  through the GitHub API or repository settings.
- The Git tag `0.1.0` can remain; deleting it requires removing the ruleset first.

The rollback is configuration-only. It does not touch the published image or its digest.

## Limitations

- The ruleset protects Git tags, not the Quay image tag. The workflow guard is the control
  for the image tag, and it depends on the Quay API being reachable.
- Required reviewers are not enabled because a solo maintainer cannot approve a deployment
  they triggered. The manual dispatch is the approval signal.
- `allow_republish=true` remains an intentional bypass. It is visible in the workflow run
  inputs and the warning log.
- The change does not exercise a CD run, because the existing `0.1.0` tag is correctly
  refused. A future version bump or an explicit `allow_republish=true` dispatch is needed to
  exercise the publication path again.

## Deletions

None.
