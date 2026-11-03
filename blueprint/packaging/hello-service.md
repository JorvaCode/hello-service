# Packaging — hello-service

## Version

- Version: `0.1.0`.
- Source of truth: `version` in `build.gradle.kts`.
- The CI workflow reads the version from Gradle and passes it to the image as
  `APP_VERSION`; the Dockerfile default matches for local builds.

## Artifacts

- Executable JAR: `build/libs/hello-service-0.1.0.jar`.
- OCI image: `localhost/hello-service:0.1.0`, also tagged with the commit SHA.
- The image is not published to a registry in this closure.

## Identity

- `artifact-metadata.json` records `version`, `commit_sha`, `jar_sha256` and `image_id`.
- `jar_sha256` is the SHA-256 of the executable JAR.
- `image_id` is the content-addressable OCI image ID reported by `podman inspect`.
- A consumer can verify the JAR checksum and the image ID without the source workspace.

## Provenance and metadata

- OCI labels: `org.opencontainers.image.version`, `.source` and `.revision`.
- Build inputs recorded by CI: repository, commit SHA and Gradle version.
- Reproducibility: archive timestamps are disabled and file order is fixed in
  `build.gradle.kts`.
- The Dockerfile pins the base image by digest.
- Dependency locking is enabled; `gradle.lockfile` is committed and used by CI so the
  scanned dependency set matches the built artifact.

## Secret scan

- Tool: gitleaks `8.30.1`, pinned by version and SHA-256 of the release archive.
- Command: `gitleaks detect --source . --no-git --report-format json --report-path
  gitleaks-report.json --exit-code 1`.
- The report is uploaded as the `gitleaks-report` artifact and the step fails the job if a
  finding is reported.
- Result: GitHub Actions run 13 on `2e54476` completed with `success`; the scan step used
  `--exit-code 1`, so its success means no finding was reported. The `gitleaks-report`
  artifact (159 bytes) is attached to the run.
- Evidence: `https://github.com/JorvaCode/hello-service/actions/runs/41046888744`.

## Dependency scan

- Tool: Trivy `0.76.1`, pinned by version and SHA-256 of the release archive.
- Target: `gradle.lockfile`, with `--scanners vuln --severity HIGH,CRITICAL
  --ignore-unfixed --exit-code 1`.
- Reports: `trivy-report.txt` and `trivy-report.json`, uploaded as the `trivy-report`
  artifact and summarized in the workflow step summary.
- Accepted findings: `.trivyignore` records the two `spring-webmvc` criticals that are
  fixed only in Spring Framework 7.0.9, with the reason they do not apply to this service.
- The lockfile and build overrides pin Tomcat `10.1.59`, Spring Framework `6.2.19` and
  Jackson BOM `2.21.7` while the Spring Boot plugin stays on `3.5.14`.

## Gate

- P-10 is `CLOSED` for run 13. The artifact metadata, versioned JAR and secret scan report
  are attached to that run.
