# Release 0.1.0 — hello-service

## Local release identity

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

## Local promotion result

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

Result: promoted to the `local` environment. Two separate remote publications of `0.1.0`
were performed by `cd.yml` on 2026-11-01; see "Remote publications (post-closure)" below.
No remote promotion to a second environment is claimed.

## Remote publications (post-closure)

Two merges to `main` triggered `cd.yml` on 2026-11-01. The first published `0.1.0` from
`c7d3a86`; the second published from `21772cc` and superseded the first digest. Automatic
push publication is now disabled; `cd.yml` requires `workflow_dispatch`.

| Publication | Source commit | CI run | CD run | Remote digest |
| --- | --- | --- | --- | --- |
| First | `c7d3a86b04e7b39a8ccfa55c37c8ae2c3dc4c2c0` | 41332436321 (`success`) | 41332436297 (`success`) | `sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b` (superseded) |
| Second (current) | `21772ccb0012c36e2264ede3171758ddfd773525` | 41334455181 (`success`) | 41334455398 (`success`) | `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17` |

| Field | Value |
| --- | --- |
| Registry namespace | `odysseia` |
| Auth mode | `oidc` |
| Remote image | `quay.io/odysseia/hello-service:0.1.0` |
| Current remote digest | `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17` |
| First remote digest (superseded) | `sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b` |
| Deploy | by digest, `quay.io/odysseia/hello-service@sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17` |
| Smoke test | `GET /api/hello` -> `200`, `text/plain;charset=UTF-8`, `Hello, World!` |
| Promotion | mechanism prepared; no second environment executed |
| Remote rollback | not exercised; no previous remote release recorded |

The remote images are separate builds from the local artifact above. The local image ID and
JAR SHA-256 identify the local promotion at `870500c`; the remote digests identify the
CD builds at `c7d3a86` and `21772cc`. The tag `0.1.0` is mutable; the current digest is the
immutable identity of the published remote artifact. The remote publications are recorded
in `blueprint/changes/p11-remote-publication.md` and reviewed in
`blueprint/reviews/remote-publication.md`.

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
- Gate: P-11 is `CLOSED` for the local promotion and the remote publications with the
  self-approval above; it does not claim an independent approval.
- Remote publications to Quay were completed on 2026-11-01 by CD runs 41332436297 and
  41334455398; see "Remote publications (post-closure)" above. The current digest is
  `0d8deff`; the first digest is superseded. No remote promotion to a second environment
  or remote rollback is claimed.
- The `local` environment is not staging or production; the local promotion result is
  scoped to `local`. The remote deployment ran in the GitHub Actions runner and is not a
  staging or production environment.
- `/api/status` is a liveness signal, not a readiness or dependency health check.
