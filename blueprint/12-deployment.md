# 12 — Deployment

Deployment must be repeatable and observable.

## Consider
- Configuration
- Secrets
- Database migrations
- Rollback strategy
- Health checks
- Readiness/liveness
- Deployment strategy
- Environment-specific settings

## Possible platforms
- VM
- Container platform
- Kubernetes
- OpenShift
- Cloud services
- On-premises infrastructure

---

# Report: REQ-HELLO-001 — hello-service Deployment (Phase 12)

Scope: `hello-service` (Spring Boot 3.5.3 / Java 21 toolchain, stateless single endpoint) deployed as an OCI container per ADR-009 (`blueprint/architecture/adr/adr-009-deployment-platform.md`). No registry, no image push, no Kubernetes, no secrets.

## Platform and image

- Platform: OCI container image executed with Podman 5.8.5 (runtime `podman-machine-default`); Kubernetes deferred (ADR-009).
- Image: `localhost/hello-service:0.0.1-SNAPSHOT`
- Image ID: `0fe6bfd2e77a7c3bc50b8ccb4953aea6fd3927b1b42b99d3b99a727952cf256f`
- Digest: `sha256:f7c0a1ed1cb6099424a689d795796f9eadefa35f100d0ec3f83249f3b27bab7b`
- Size: 352 MB
- Base: `docker.io/library/eclipse-temurin:21-jre@sha256:49e21e16e3c86eb7816a44a67549910ed090fbeb40c29c525d58bf5e02e91b0f` (Ubuntu 26.04, JRE 21.0.12 — pinned by digest)
- Runtime user: `appuser` (uid 10001, non-root)
- Producer: `podman build --build-arg APP_VERSION=0.0.1-SNAPSHOT -t hello-service:0.0.1-SNAPSHOT .` on the Phase 10 JAR (`build/libs/hello-service-0.0.1-SNAPSHOT.jar`)

## Mechanism decision: Dockerfile OCI (Podman) over `bootBuildImage`

A plain Dockerfile built with `podman build` was chosen over the Spring Boot `bootBuildImage`/Paketo Buildpacks path because:
- `bootBuildImage` requires Gradle to reach a container daemon (unnecessary coupling and network/socket complexity on Windows with a Podman machine, and adds Buildpacks as a build-time dependency) — conflicts with ADR-001 and PA-007 (no unnecessary dependencies/complexity).
- A `Dockerfile` + `podman build` produces a pure OCI image, reproducible by pinned base digest and `ARTIFACT → JAR` copy, without extra tooling or daemon redirection.

## Configuration

- No environment-specific setting is hardcoded in the image. The default application port is `8080` (`EXPOSE 8080`); the runtime port is configurable via the standard `SERVER_PORT` env var (relaxed binding).
- Demonstrated override: container started with `-e SERVER_PORT=9090 -p 9090:9090` served `GET /api/hello` → 200 `Hello, World!` on port 9090.
- `ENTRYPOINT ["java", "-jar", "/app/hello-service.jar"]`; base image env (`PATH`, `JAVA_HOME`, ...) only, no secrets in `Config.Env`.

## Health check

- OCI image format does not support the `HEALTHCHECK` directive (ignored with a warning by Podman), so the probe is defined at runtime for the container, keeping the image OCI-pure:
  `--health-cmd "curl -fsS http://127.0.0.1:8080/api/hello > /dev/null || exit 1" --health-interval 5s --health-timeout 3s --health-start-period 20s --health-retries 5`
- Uses the existing `GET /api/hello` endpoint; no Actuator added (NFR-02 / ADR-008: no new dependencies).
- Result: `podman inspect --format "{{.State.Health.Status}}"` → `healthy`.

## Verification

| # | Check | Result | Evidence |
|---|---|---|---|
| V-1 | Image build | PASS | `podman build` → BUILD SUCCESSFUL, tagged `hello-service:0.0.1-SNAPSHOT`. |
| V-2 | Run + health | PASS | Container `hello-service` → `Up ... (healthy)`; logs show Tomcat started on 8080, `Started HelloApplication`. |
| V-3 | Smoke test | PASS | `GET http://localhost:8080/api/hello` → HTTP 200, `Content-Type: text/plain;charset=UTF-8`, body `Hello, World!`. |
| V-4 | Generic config | PASS | Override on port 9090 (V-2 without health probe) → HTTP 200 `Hello, World!`. |
| V-5 | Redeploy from image | PASS | `podman stop` + `podman rm` + same `podman run` from the same image → same image ID, health `healthy`, smoke 200. |
| V-6 | Reproducible image | PASS | Two consecutive `podman build` runs from the Phase 10 JAR produced the identical image ID `0fe6bfd2e77a...` (`REPRODUCIBLE=True`). |
| V-7 | No secrets in image | PASS | `/app` contains only `hello-service.jar`; no `.env`/`.properties`/`.yml`/`.pem`/`.key` under the app; `Config.Env` holds base env only; regex secret scan over the deployment configuration (Dockerfile, .dockerignore) found no credentials. |
| V-8 | Base image pinned | PASS | Base referenced by digest in `Dockerfile` (`org.opencontainers.image.base.digest` label), environment-agnostic. |

## Rollback strategy

- Since only one application version (`0.0.1-SNAPSHOT`) exists, no genuine rollback to an older app build can be demonstrated; this is recorded as a limitation, not simulated.
- Procedure (holds for any previously built tag/version): keep the previous image tag local → `podman stop hello-service && podman rm hello-service` → `podman run ... <previous-tag>`. Image identity is reproducible (V-6) and tagged per version, so any deployed container can be reverted to a prior known-good tag or rebuilt from the Phase 10 JAR + pinned base.
- No database migrations apply (no persistence).

## Follow-ups (carried to later phases)
- Registry selection and image push (remote distribution) — deferred by ADR-009 (registry candidates: Quay.io / GHCR / Docker Hub).
- Pipeline-driven deployment and CD automation (Phase 11/13) — requires the registry decision.
- Kubernetes deployment — deferred by ADR-009; current deployment is the Podman container runtime.

## Gate

Phase 12 gate: PASS.
The deployed artifact is identified (version, image ID, digest), the platform is decided (ADR-009, OCI + Podman), deployment is repeatable (same image + same run config yields an identical, healthy container, V-5/V-6), observable (health check, logs, smoke), config is generic (no environment-specific hardcoding, V-4) and the image contains no secrets (V-7).

## Date
2026-09-24
