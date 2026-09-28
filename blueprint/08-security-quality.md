# 08 — Security and Quality

Security and quality checks are part of the delivery pipeline.

## Typical controls
- Dependency vulnerability scanning
- Secret detection
- Static analysis
- Formatting/linting
- License checks where required
- Container image scanning where applicable
- SAST/DAST where appropriate

## Principle
Controls should be proportional to project risk and should provide actionable feedback.

---

# Report: REQ-HELLO-001 — GET /api/hello

Scope: `hello-service` (Spring Boot 3.5.3 / Java 21 toolchain, Gradle).
Control set is proportional to the feature risk (stateless single endpoint, no input, no persistence, no auth).

## Controls and results

| # | Control | Result | Evidence |
|---|---|---|---|
| C-1 | Dependency inspection (runtime) | PASS | `gradlew dependencies --configuration runtimeClasspath` → BUILD SUCCESSFUL. Root resolves only to `spring-boot-starter-web:3.5.3` plus its transitive tree (spring-* 6.2.8, tomcat-embed 10.1.42, jackson 2.19.1, logback 1.5.18, snakeyaml 2.4, micrometer 1.15.1). No runtime dependency added outside the starter and its transitives. Confirms design NFR-02 / ADR-008 ("no new dependencies"). |
| C-2 | Build + tests | PASS | `gradlew compileJava test --warning-mode all` → BUILD SUCCESSFUL (3 up-to-date tasks, no compiler warnings). Prior evidence: `gradlew clean build` → BUILD SUCCESSFUL (review 07). |
| C-3 | Secret detection | PASS | Regex scan across `hello-service` for credentials/tokens/private keys (password, secret, api key, token, PEM private key, AWS key pattern). No findings. No runtime config files other than `gradle-wrapper.properties` (distribution URL only). |
| C-4 | Static analysis / lint | PARTIAL | No static-analysis or formatting plugin is configured in `build.gradle.kts` (no checkstyle/spotless/spotbugs/spotless). Compiler-level check passed clean (C-2). Adding a lint/format gate is deferred to the CI phase (09). No blocking issue found. |
| C-5 | Dependency vulnerabilities | PARTIAL | Automated vulnerability scanner (OWASP dependency-check / grype / Snyk) is not configured. Versions resolved from the Spring Boot 3.5.3 managed BOM were inspected manually via C-1 (all current managed versions). Automated scanning is deferred to CI (09) and recorded as a follow-up. |
| C-6 | License check | N/A | No direct third-party dependency beyond the Spring Boot BOM-managed starter set; no LICENSE requirement has been defined for this project. Not applicable for this change. |
| C-7 | Container image scanning | N/A | No container image exists yet; packaging is a later phase (10/12). |
| C-8 | SAST/DAST | N/A | No deployable runtime surface beyond the reviewed endpoint; contract tests (06) already cover method enforcement (405 + Allow) and unknown-path 404. |

## Conclusion
Phase 8 gate: PASS. The change introduces no new runtime dependency (C-1) and no secret material (C-3); build and tests are green (C-2). Remaining automation (lint gate, dependency-vulnerability scan) is a CI-phase concern (09) and is documented there, not a blocker for this phase.

## Follow-ups (carried to Phase 9)
- Configure a lint/formatting gate (e.g. checkstyle/spotless) in CI.
- Configure dependency-vulnerability scanning (e.g. OWASP dependency-check) in CI.