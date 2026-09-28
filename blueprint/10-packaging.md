# 10 — Packaging and Containerization

Create a deployable artifact.

## Possible artifacts
- Application package
- Container image
- Helm chart
- Deployment bundle
- Other platform-specific artifact

## Requirements
- Versioned artifact
- Reproducible build where practical
- Metadata/provenance where practical
- No secrets embedded in artifacts

## Gate
Artifact can be identified, retrieved and deployed independently of the source workspace.

---

# Report: REQ-HELLO-001 — hello-service

## Deployable artifact
- Spring Boot executable JAR (fat jar): `build/libs/hello-service-0.0.1-SNAPSHOT.jar`
- Secondary thin JAR: `build/libs/hello-service-0.0.1-SNAPSHOT-plain.jar`
- Producer: `gradlew clean build` (`bootJar` task), Spring Boot plugin 3.5.3 / Java toolchain 21.

## Requirements
| Requirement | Result | Evidence |
|---|---|---|
| Versioned artifact | PASS | Filename and manifest carry `0.0.1-SNAPSHOT` (project version). |
| Reproducible build | PASS | `isPreserveFileTimestamps=false`, `isReproducibleFileOrder=true` configured on archive tasks; two consecutive `clean build` runs produced identical SHA-256 (`BC26F343...216303`). |
| Metadata/provenance | PASS | Manifest: `Implementation-Title`, `Implementation-Version`, `Build-Jdk-Spec: 21`, `Spring-Boot-Version: 3.5.3`, `Main-Class`/`Start-Class`. Artifact traceable to CI build commit (`github.sha`). |
| No secrets embedded | PASS | No sensitive files inside JAR (no `.properties`, `.env`, `.pem`, `.key`, secret/credential files); `BOOT-INF/classpath.idx` lists only standard dependencies. |

## Verification
- `.\gradlew.bat clean build` → BUILD SUCCESSFUL (7 tasks), exit 0.
- JAR inspected: contains `BOOT-INF/classes/com/example/hello/HelloApplication.class` and `HelloHandler.class`; manifest metadata confirmed.
- JAR can be identified, retrieved from `build/libs/` and deployed independently of the source workspace (Phase 10 gate satisfied).

## Scope notes
- Container image is not produced in this phase: NFR-02 / external deployment target not yet defined; container packaging is deferred to a later decision and is not part of this change (consistent with Phase 08 C-7 and Phase 12 deployment platform decision).

## Date
2026-09-24
