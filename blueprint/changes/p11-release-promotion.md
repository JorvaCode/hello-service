# Change: `CHG-DEL-002` — Release 0.1.0 promotion and rollback rehearsal

## Requirement
- `REQ-HELLO-001` — `GET /api/hello`.
- `REQ-STATUS-001` — `GET /api/status`.

## Design
The change records a local promotion of the versioned `0.1.0` artifact and a rollback
rehearsal. It does not alter structure, interface, data or dependencies: no source,
configuration or dependency file changes. The delivery artifacts are
`blueprint/delivery/release-0.1.0.md` and the P-11 update in `blueprint/gates.md`.

### Patterns
None. No code is introduced, so no pattern applies. The deployment strategy is a
container replacement, not an orchestration pattern.

### ADR
None. The change is operational and does not meet the significance criteria in
`standards/terms.md`; no architecture, interface, data or dependency decision is made.

## Implementation
- Record the release identity, delivered requirements and changes in
  `blueprint/delivery/release-0.1.0.md`.
- Record the local promotion checks and result for version `0.1.0`.
- Record the rollback target `0.0.1-SNAPSHOT` and the rehearsal result.
- Update P-11 in `blueprint/gates.md` with the evidence and the independence limitation.

## Tests
| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| Local build is green at the release commit | Integration | `.\gradlew.bat clean build --offline --no-daemon --console=plain` at `870500c`; 17 tests, 0 failures, 0 errors, 0 skipped |
| The promoted artifact serves the hello contract | Smoke | `GET /api/hello` on `127.0.0.1:18080` returns `200`, `text/plain`, `Hello, World!` |
| The promoted artifact serves the status contract | Smoke | `GET /api/status` returns `200`, `application/json`, `{"status":"UP"}`, with `X-Request-ID` |
| The rollback target works | Smoke | `0.0.1-SNAPSHOT` from `f2b03e5` serves both endpoints; `0.1.0` is restored and serves both again |

## Security
No dependency, endpoint, secret or input path is added. The image runs as `appuser`
(UID 10001, non-root). The secret scan from CI run 13 is green. No new secret or
credential is introduced; the local promotion uses no registry authentication.

## Deployment
Local only: single-node Podman on Windows/WSL, port `18080` mapped to `8080`. No remote
registry publication or remote environment is used. The reproducible procedure is in
`blueprint/deployment/local-0.1.0.md`.

## Rollback
Rehearsed on 2026-10-30. Stop the `0.1.0` container, run
`localhost/hello-service:0.0.1-SNAPSHOT` from `f2b03e5`, verify both endpoints, restore
`0.1.0`, and verify both endpoints again. The service has no persistent state or schema,
so rollback is a container replacement. Evidence: `blueprint/delivery/release-0.1.0.md`.

## Deletions
None.

## Post-closure update (2026-11-01)

The local promotion described above is unchanged. After the phase 14 merge, `cd.yml` run
41332436297 published `0.1.0` to Quay and deployed it by digest. A second run, 41334455398
on `21772cc`, published the current digest and superseded the first; `cd.yml` now requires
`workflow_dispatch`. See `blueprint/changes/p11-remote-publication.md`. The remote
publications are separate events, not part of this local promotion.
