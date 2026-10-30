# Deployment record — local 0.1.0

## Deployment

| Field | Value |
| --- | --- |
| Release | `0.1.0` — `blueprint/delivery/release-0.1.0.md` |
| Environment | `local` — single-node Podman on Windows/WSL |
| Date | 2026-10-30 |
| Strategy | Recreate: stop/remove the previous container, run the versioned image |
| Artifact | `localhost/hello-service:0.1.0`, image ID `0373d3f5d6d318e801e39162d2bc61b799fa361575e9f637d608d0be498256ca` |
| Configuration | Port `18080` -> `8080`; no secrets, no external configuration, no database |
| Runtime user | `appuser` (UID 10001, non-root) |
| Entrypoint | `java -jar /app/hello-service.jar` |

## Migration record

None. The service has no database, schema or persistent state. No configuration
migration was applied.

## Post-deployment verification

- Container started; log lines `Starting HelloApplication v0.1.0` and
  `Tomcat started on port 8080 (http)`.
- `GET /api/hello` -> `200`, `text/plain;charset=UTF-8`, `Hello, World!`.
- `GET /api/status` -> `200`, `application/json`, `{"status":"UP"}`, with
  `X-Request-ID`.
- OCI labels: `version=0.1.0`, `revision=870500c503d852912b90de13ee2248c6a2b194de`,
  `source=JorvaCode/hello-service`.
- No readiness probe is configured; `/api/status` is a liveness signal only.

## Reproducible procedure

From the repository root in Windows PowerShell 5.1:

```powershell
.\gradlew.bat clean build --offline --no-daemon --console=plain
$sha = (git rev-parse HEAD).Trim()
podman build --build-arg APP_VERSION=0.1.0 --build-arg CI_REPOSITORY=JorvaCode/hello-service --build-arg CI_SHA=$sha -t localhost/hello-service:0.1.0 .
podman run -d --name hello-service -p 18080:8080 localhost/hello-service:0.1.0
Invoke-WebRequest -Uri 'http://127.0.0.1:18080/api/hello' -UseBasicParsing
Invoke-WebRequest -Uri 'http://127.0.0.1:18080/api/status' -UseBasicParsing
podman stop hello-service
podman rm hello-service
```

Prerequisites: Java 21, a running Podman machine, and Gradle dependencies cached for
`--offline`.

## Rollback evidence

Exercised on 2026-10-30. The target `0.0.1-SNAPSHOT` from `f2b03e5` was built and run;
both endpoints passed. `0.1.0` was then restored and both endpoints passed again.
Identity of the target and the full rehearsal record:
`blueprint/delivery/release-0.1.0.md`.

## Gate

The post-deployment verification passed and the rollback path was exercised. The
deployment gate is closed with the solo-maintainer self-review recorded in
`blueprint/reviews/delivery-closure.md`; it does not claim an independent approval. The
deployment is scoped to the `local` environment; no remote publication is claimed.
