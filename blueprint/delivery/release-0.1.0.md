# Release 0.1.0 — hello-service

## Release identity

| Field | Value |
| --- | --- |
| Version | `0.1.0` |
| Release commit | `870500c503d852912b90de13ee2248c6a2b194de` |
| Release date | 2026-10-30 |
| JAR | `build/libs/hello-service-0.1.0.jar` |
| JAR SHA-256 | `ce33444125587544e54f6ad6a38fa3420cdee0e9edcd73a54ee8a7fcc7ceb5b7` |
| OCI image | `localhost/hello-service:0.1.0` |
| Image ID | `0373d3f5d6d318e801e39162d2bc61b799fa361575e9f637d608d0be498256ca` |
| OCI labels | `version=0.1.0`, `revision=870500c503d852912b90de13ee2248c6a2b194de`, `source=JorvaCode/hello-service` |
| Base image | `eclipse-temurin:21-jre@sha256:49e21e16e3c86eb7816a44a67549910ed090fbeb40c29c525d58bf5e02e91b0f` |

The promoted artifact is a local rebuild from `870500c`. The source tree at `870500c`
has no changes under `src/`, `build.gradle.kts`, `Dockerfile` or
`.github/workflows/ci.yml` relative to `2e54476`, the commit of CI run 13. The rebuild
therefore carries the same application code; its identity is the one above, not the
identity of the image built on the CI runner.

## Delivered scope

Requirements:

- `REQ-HELLO-001` — `GET /api/hello`.
- `REQ-STATUS-001` — `GET /api/status`.

Changes:

- `CHG-PKG-001`, `CHG-PKG-002` — version `0.1.0`, secret scan and CI packaging evidence.
- `CHG-DEL-002` — this release record, the promotion result and the rollback rehearsal.
- `CHG-DEP-001` — local deployment record.
- `CHG-CI-001`, `CHG-CI-002`, `CHG-DOC-001`, `CHG-GATE-001`, `CHG-IMP-001`,
  `CHG-REV-001`, `CHG-TEST-001` — closure documentation, CI simplification, review and
  test evidence. These do not change the runtime contract.

This is the first versioned release. The previous artifact was `0.0.1-SNAPSHOT`; no
earlier release record exists.

## Promotion result

| Field | Value |
| --- | --- |
| Environment | `local` — single-node Podman on Windows/WSL |
| Promotion timestamp | 2026-10-30T18:25:52Z (verification run) |
| Strategy | Recreate a container from the versioned image; no orchestration |
| Approval | Self-review recorded in `blueprint/reviews/delivery-closure.md`; no independent reviewer exists |

Checks:

- Local build at `870500c`: `.\gradlew.bat clean build --offline --no-daemon
  --console=plain` completed with `BUILD SUCCESSFUL`; 17 tests, 0 failures, 0 errors,
  0 skipped.
- CI packaging evidence: run 13 at `2e54476` completed with `success`; artifacts
  `artifact-metadata`, `hello-service-0.1.0` and `gitleaks-report`.
  <https://github.com/JorvaCode/hello-service/actions/runs/41046888744>
- Image build: `podman build` succeeded; image ID and OCI labels recorded above.
- Container runtime: user `appuser` (UID 10001, non-root), port `8080`; log line
  `Starting HelloApplication v0.1.0`.
- Smoke tests on `127.0.0.1:18080`:
  - `GET /api/hello` -> `200`, `text/plain;charset=UTF-8`, `Hello, World!`.
  - `GET /api/status` -> `200`, `application/json`, `{"status":"UP"}`, with
    `X-Request-ID`.

Result: promoted to the `local` environment. No remote registry publication or remote
environment promotion is claimed.

## Rollback plan and rehearsal

Target: `0.0.1-SNAPSHOT` from
`f2b03e5f8d8306192922051dbf6359e23a9a0ee2`, the last green version before packaging.

Identity of the rehearsed target:

- JAR SHA-256:
  `1ac46faeafd6ebd278be5a2847d777125bd9c303f2a6b40b234b8379a31aac93`
- Image ID:
  `e7eb44f50a87310723f1b90d24f1382462ce02adae64fab59068c0dbd8d9f658`

Procedure:

1. Stop and remove the running `0.1.0` container.
2. Run `localhost/hello-service:0.0.1-SNAPSHOT` on the same port.
3. Probe `/api/hello` and `/api/status`.
4. Restore `0.1.0` and probe both endpoints again.

Rehearsal result (2026-10-30):

- `0.0.1-SNAPSHOT`: `/api/hello` -> `200` `Hello, World!`; `/api/status` -> `200`
  `{"status":"UP"}` with `X-Request-ID`.
- Restored `0.1.0`: both probes passed again.
- No persistent state or schema migration is involved; rollback is a container
  replacement.

## Approval and limitations

- Approval: self-review recorded in `blueprint/reviews/delivery-closure.md`. The
  repository is a single-maintainer repository and no independent human reviewer is
  recorded. This record does not claim an independent approval; `standards/git.md`
  requires the single-maintainer fact to be recorded rather than simulated.
- Gate: P-11 is `CLOSED` for this local promotion with the self-approval above; it does
  not claim an independent approval.
- Remote publication to Quay remains postponed. `cd.yml` was not executed. No remote
  digest, registry tag or remote promotion is claimed.
- The `local` environment is not staging or production; the promotion result is scoped
  to `local`.
- `/api/status` is a liveness signal, not a readiness or dependency health check.
