# Change: `CHG-PKG-002` - Dependency vulnerability scan

## Requirement

Satisfies IMP-002 (security findings). No change to the HTTP contract or deployment topology. Records automated dependency vulnerability checks in CI and documents accepted findings via `.trivyignore`.

## Design

- Trivy `v0.76.1` is downloaded and verified by SHA-256 in CI, matching the pinning style used for gitleaks.
- The lockfile `gradle.lockfile` is generated and committed so Gradle dependencies are scanned in a reproducible form.
- The scan targets `gradle.lockfile` with `--scanners vuln`, `--severity HIGH,CRITICAL`, `--ignore-unfixed` and fails the job on findings. Accepted findings are tracked in `.trivyignore` with rationale.
- Spring Boot and related dependencies are pinned via Gradle overrides in `build.gradle.kts` to remediate fixed vulnerabilities where possible. The remaining CRITICAL findings in `spring-webmvc` (CVE-2026-47884, CVE-2026-47890) are accepted temporarily with rationale because they are fixed in Spring Framework 7.0.9 and the service does not use XsltView or Server-Sent Events.
- Trivy JSON and table reports are uploaded as artifacts and summarized in the GitHub Actions step summary.

### Patterns

No application pattern is applied. CI reuses the existing single-job validate shape.

### ADR

Not significant. No architecture decision is introduced or changed.

## Implementation

- `build.gradle.kts`: enable dependency locking, override `tomcat.version` to `10.1.59`, `spring-framework.version` to `6.2.19`, `jackson-bom.version` to `2.21.7`, bump `org.springframework.boot` to `3.5.14`.
- `gradle.lockfile`: committed dependency lock state.
- `.github/workflows/ci.yml`: add `Dependency vulnerability scan` step and upload `trivy-report` artifact.
- `.trivyignore`: document accepted CRITICAL findings with rationale.
- `blueprint/09-continuous-integration.md`: document Trivy scan, reports, enforcement and accepted findings.
- `blueprint/packaging/hello-service.md`: document dependency scan in packaging/provenance record.
- `blueprint/improvements/continuous-improvements.md`: mark IMP-002 as implemented/in progress; track closure.
- `blueprint/gates.md`: update evidence as applicable.

## Tests

- Local: `.\gradlew.bat clean build --write-locks --no-daemon --console=plain` succeeds.
- Local: `trivy fs --scanners vuln --severity HIGH,CRITICAL --ignore-unfixed --exit-code 1 gradle.lockfile` exits 0 with current `.trivyignore`.
- CI: run for this change shows `Dependency vulnerability scan` green and `trivy-report` artifacts uploaded.

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| Reproducible dependencies | Build | `gradle.lockfile` committed and used by build |
| Scan enforced | CI | Trivy step fails on unaccepted HIGH/CRITICAL |
| Reports recorded | CI | `trivy-report` artifact uploaded; summary present |
| Accepted findings tracked | Repo | `.trivyignore` with rationale |

## Security

Trivy archive pinned by version and SHA-256. Scan runs after build with `permissions: contents: read`. Accepted findings are explicit and documented with rationale; no secrets introduced.

## Deployment

None.

## Rollback

Revert the commit; remove Trivy step, `.trivyignore`, `gradle.lockfile` (or restore previous), revert overrides. Previous CI behavior without dependency scan returns.

## Deletions

None.
