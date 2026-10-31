# Change: `CHG-DEP-001` — Local 0.1.0 deployment record

## Requirement
- `REQ-HELLO-001` — `GET /api/hello`.
- `REQ-STATUS-001` — `GET /api/status`.

## Design
The change records the local deployment of the `0.1.0` release and documents the
reproducible procedure in `README.md`. It does not alter structure, interface, data or
dependencies: no application code, configuration or dependency changes.

### Patterns
None. No code is introduced.

### ADR
None. The deployment is a local container replacement and does not meet the significance
criteria in `standards/terms.md`.

## Implementation
- Add `blueprint/deployment/local-0.1.0.md` with the deployment record, migration record,
  post-deployment verification, reproducible procedure and rollback evidence.
- Update `README.md` to replace the "no reproducible startup command" limitation with the
  verified local procedure and the deployment record link.
- Link the deployment record from `blueprint/12-deployment.md`.

## Tests
| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| The deployment starts the intended version | Smoke | Container log line `Starting HelloApplication v0.1.0`; OCI label `version=0.1.0` |
| The deployment serves the hello contract | Smoke | `GET /api/hello` returns `200`, `text/plain`, `Hello, World!` |
| The deployment serves the status contract | Smoke | `GET /api/status` returns `200`, `application/json`, `{"status":"UP"}` |
| The deployment runs as non-root | Inspection | `podman inspect --format '{{.Config.User}}'` returns `appuser` |
| The rollback path works | Smoke | `0.0.1-SNAPSHOT` serves both endpoints; `0.1.0` restored and serves both |

## Security
No dependency, endpoint, secret or input path is added. The container runs as `appuser`
(UID 10001, non-root). No secret is stored in the deployment record or README. The local
environment uses no registry credentials.

## Deployment
Local only: single-node Podman, port `18080` -> `8080`, no database, no external
configuration. Remote publication remains postponed.

## Rollback
Exercised on 2026-10-30. Details and identity of the target:
`blueprint/delivery/release-0.1.0.md`. The deployment record repeats the procedure.

## Deletions
None.
