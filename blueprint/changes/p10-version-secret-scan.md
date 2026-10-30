# Change: `CHG-PKG-001` — Version the artifact and add the secret scan

## Requirement

Satisfies P-10 from `blueprint/gates.md`: the artifact carries a version and an identity a
consumer can verify independently, and the secret scan result is recorded. It does not
change the HTTP contract.

## Design

- Version `0.1.0` is declared once in `build.gradle.kts` and read by CI from Gradle.
- The Dockerfile receives the version as `APP_VERSION` and stamps it in the OCI version
  label; the default matches for local builds.
- The image is tagged with both the version and the commit SHA.
- `artifact-metadata.json` gains `version` and `jar_sha256` next to `commit_sha` and
  `image_id`.
- A gitleaks scan runs before the build and uploads its JSON report; a finding fails the
  job.
- `blueprint/packaging/hello-service.md` records the artifact, identity, provenance and
  scan.

### Patterns

No application pattern is applied. The workflow reuses the existing single-job shape.

### ADR

Not significant. No architecture decision is introduced or changed.

## Implementation

- `build.gradle.kts`: `version = "0.1.0"`.
- `Dockerfile`: `ARG APP_VERSION=0.1.0`.
- `.github/workflows/ci.yml`: resolve version, gitleaks scan, versioned image tags,
  extended metadata, JAR and scan report upload.
- `.gitignore`: ignore the local gitleaks report.
- `blueprint/packaging/hello-service.md`: new packaging record.
- `blueprint/gates.md`: P-10 evidence updated.

## Tests

- Local: `.\gradlew.bat clean build --offline --no-daemon --console=plain` must produce
  `build/libs/hello-service-0.1.0.jar` and keep the 17 tests green.
- CI: the run for this change must show the secret scan, version resolution, build,
  image build, identity record and artifact uploads as `success`.

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| Versioned artifact | Build | JAR named `hello-service-0.1.0.jar` |
| Verifiable identity | CI | `artifact-metadata.json` with `jar_sha256` and `image_id` |
| Secret scan recorded | CI | `gitleaks-report` artifact and a green scan step |

## Security

The gitleaks archive is pinned by version and SHA-256 before execution. The workflow keeps
`permissions: contents: read`. No secret is written to the repository or the artifact.

## Deployment

None. The image remains local to the CI runner; no registry publication or promotion is
claimed.

## Rollback

Revert the commit; the previous `0.0.1-SNAPSHOT` version and CI without the scan return.

## Deletions

None.
