# Change: `CHG-IMP-001` — Phase 14 continuous improvement closure

## Requirement

- `REQ-HELLO-001` — `GET /api/hello` (delivered scope).
- `REQ-STATUS-001` — `GET /api/status` (delivered scope).

The change does not add or revise a requirement. It records five owned improvement items
against the delivered local scope and closes the phase 14 register change. None of the items
is implemented in this phase, so no `FR-nn`/`NFR-nn` revision and no new release are claimed.

## Design

Update `blueprint/improvements/continuous-improvements.md` so each item names:

- the feedback source and its evidence;
- an owner;
- the artifact it would change if implemented;
- a status that states whether the loop is open.

Add the lifecycle-changes record, the phase 14 closure record and the phase 14 gate. Add the
P-14 row to `blueprint/gates.md`, the project record to
`blueprint/14-continuous-improvement.md`, and the continuous-improvement section and status
row to `README.md`.

### Patterns

None. No code structure or abstraction is introduced.

### ADR

None. The change does not meet the significance criteria in `standards/terms.md`.

## Implementation

- Update `blueprint/improvements/continuous-improvements.md`:
  - add owners, artifacts and feedback evidence to `IMP-001` … `IMP-005`;
  - add the `Lifecycle Changes` and `Phase 14 Closure Record` sections;
  - add the `Phase 14 Gate` section;
  - update the feedback-source table and the closure-scope table.
- Add `blueprint/reviews/continuous-improvement-closure.md`.
- Add this change record.
- Add the project record to `blueprint/14-continuous-improvement.md`.
- Add the P-14 row to `blueprint/gates.md`.
- Add the continuous-improvement section, status row and documentation links to `README.md`.

No code, configuration, dependency or deployment file changes.

## Tests

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| Existing contracts remain green | Unit/integration | Local run on 2026-10-30T18:45:20Z: `.\gradlew.bat clean test --offline --no-daemon --console=plain` -> `BUILD SUCCESSFUL`; 17 tests, 0 failures, 0 errors, 0 skipped |
| The last code-bearing change is green | CI | Phase 13 CI run 41220748392 on `0fbaffc` concluded `success` |
| The phase 14 change is green in CI | CI | Phase 14 CI run 41225786925 on `195d40c` concluded `success` |
| Every improvement item is traceable | Inspection | Each `IMP-001` … `IMP-005` names a source, owner, artifact and status; the closure record maps each item to its evidence |
| The phase 14 change is documentation-only | Inspection | The diff touches only `blueprint/`, `README.md` and the gate register; no `src/`, build or deployment file changes |

No new test is required because no behaviour changes. The deferred items have no
implementation tests because they are not implemented.

## Security

No dependency, endpoint, secret or input path is added. No secret is stored in the register,
review or change record. `IMP-002` records the deferred dependency-vulnerability scan and
`IMP-005` records the deferred access-log review; neither is claimed as done. The existing
`X-Request-ID` log-injection residual risk remains accepted and unchanged.

## Deployment

This change itself makes no deployment change; the local `0.1.0` deployment and the
runbook remain unchanged. After this change merged, `cd.yml` published `0.1.0` to Quay and
deployed it by digest; see the post-merge update below and
`blueprint/changes/p11-remote-publication.md`.

## Rollback

No deployment or artifact change, so no rollback is required for this change. The existing
local rollback to `0.0.1-SNAPSHOT` remains documented in
`blueprint/delivery/release-0.1.0.md` and `blueprint/observability/hello-service.md`.

## Deletions

None.

## Post-merge update (2026-11-01)

Merging this change to `main` (`c7d3a86`) triggered `cd.yml` run 41332436297. The workflow
published
`quay.io/odysseia/hello-service:0.1.0@sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b` (superseded),
deployed it by digest and passed the smoke test. Merging the correction on `21772cc`
triggered run 41334455398, which published the current digest and superseded the first;
`cd.yml` now requires `workflow_dispatch`. The phase 14 register and the deferred
improvement items are unchanged. The remote publications are recorded in
`blueprint/changes/p11-remote-publication.md`.
