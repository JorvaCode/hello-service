# Change: `CHG-CI-001` — Simplify CI to build, test and artifact identity

## Requirement

Satisfies no functional requirement directly. It supports phase 09 verification of
`REQ-HELLO-001` and `REQ-STATUS-001` by keeping `./gradlew clean build` as the required
check. It removes a publication path that satisfied no requirement and duplicated
`.github/workflows/cd.yml`.

## Design

The change alters **structure** (workflow jobs), **interface** (manual dispatch inputs and
job outputs removed) and **dependencies** (the duplicated OCI validation and the `publish`
job removed). It alters no application code, data model or runtime dependency.

- Removed the `publish` job. It duplicated `cd.yml` and its `Block publication` step
  always failed, so the path could never publish.
- Removed the manual OCI archive validation (tar extraction, blob and digest checks).
  `podman` already produces and verifies the image it builds.
- Kept: checkout, Java 21, Gradle, `clean build`, `podman build`, image identity and an
  artifact metadata upload.

### Patterns

No pattern is applied. The workflow is a linear pipeline with one job; there is no
recurring problem for a pattern to solve.

- **Problem:** none.
- **Evidence:** one job, one runner, one required check.
- **Cost of the simpler alternative:** the simpler pipeline already satisfies the phase 09
  and phase 10 information items this change keeps.
- **Scope:** not applicable; no pattern is adopted.

### ADR

Not significant. This is local CI configuration with no architecture, interface or data
decision. No ADR is raised.

## Implementation

- `.github/workflows/ci.yml`: one `validate` job with build/test, container build, artifact
  identity and artifact upload.
- Removed `workflow_dispatch` inputs `publish_image` and `update_latest`.
- Removed job outputs `artifact_name` and `artifact_id`.

## Tests

No automated test covers workflow YAML. The verification is the first green CI run on the
commit that carries this change; that run has not been performed, so phase 09 remains
`NOT VERIFIED` for this commit.

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| Required build and test check | Pipeline | `./gradlew clean build` in `ci.yml` |
| Artifact identity | Pipeline | `podman inspect --format '{{.Id}}'` written to `artifact-metadata.json` |

## Security

The change removes the OCI publication path and its preflight from CI. No credential,
login, token or push remains in `ci.yml`. No new input path is added.

## Deployment

None. This change does not deploy anything.

## Rollback

Revert the commit; the previous workflow is restored from git history. No artifact or
environment is affected.

## Deletions

- `publish` job and its inputs/outputs: duplicated `cd.yml` and could never publish.
- Manual OCI validation: replaced by `podman` and the image identity; no consumer existed
  for the deeper structural checks.
