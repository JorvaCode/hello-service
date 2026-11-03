# 09 — Continuous Integration

CI validates every relevant change automatically.

## Typical pipeline
1. Checkout
2. Resolve dependencies
3. Build
4. Unit tests
5. Integration tests
6. Static analysis / lint
7. Security checks
8. Package
9. Publish test artifacts when useful

## Information items
### Inputs
- **The change** — the commit under validation.
- **Required checks** — the checks the project has declared mandatory, recorded where the pipeline can read them. "Required" without a declaration is not a criterion, because a skipped check and a passing check look identical in the result.
- **Build and test configuration** — from the project.

### Outputs
- **Pipeline definition** — the workflow, in version control, naming every required check.
- **Pipeline run result** — per required check: passed, failed or skipped, with the reason for a skip.
- **Integrability verdict** — integrable or not, derived from the required checks and nothing else.

## Quality gate
Every check the pipeline declares as required has a result of passed for the current commit; a skipped required check fails the run. The verdict is integrable only when all of them passed, and a reviewer who did not watch the run reaches the same verdict from the run result.

The workflow examples in `.github/workflows/` provide a technology-neutral starting point.

## Current pipeline

`.github/workflows/ci.yml` runs on pushes to `main`, pull requests targeting `main` and
manual dispatch. The required checks are:

1. Secret scan: gitleaks `8.30.1`, pinned by version and SHA-256, fails on a finding.
2. Build and test: `./gradlew clean build --no-daemon --console=plain`.
3. Dependency vulnerability scan: Trivy `0.76.1`, pinned by version and SHA-256, scans
   the committed `gradle.lockfile` for HIGH and CRITICAL vulnerabilities and fails the job
   unless the finding is accepted in `.trivyignore`.
4. Container image build, artifact identity record and artifact upload.

### Dependency scan

- Gradle dependency locking is enabled in `build.gradle.kts`; `gradle.lockfile` is
  versioned so the scanned dependency set matches the built artifact.
- Trivy command: `trivy fs --scanners vuln --severity HIGH,CRITICAL --ignore-unfixed
  --exit-code 1 --format table --output trivy-report.txt gradle.lockfile`.
- The JSON and table reports are uploaded as the `trivy-report` artifact and summarized in
  the GitHub Actions step summary. The scan runs even when findings fail the job so the
  report is always available.
- Accepted findings live in `.trivyignore` with a rationale and a review trigger. Current
  entries are two `spring-webmvc` criticals fixed only in Spring Framework 7.0.9; the
  service does not use XsltView or Server-Sent Events.
