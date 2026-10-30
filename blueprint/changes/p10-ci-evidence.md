# Change: `CHG-PKG-002` — Record the P-10 CI evidence

## Requirement

Records the evidence required by P-10 for `CHG-PKG-001`. It does not change behaviour.

## Design

Documentation only. No structure, interface, data or dependency changes.

- `blueprint/packaging/hello-service.md`: run 13 result, artifacts and gate.
- `blueprint/gates.md`: P-10 moves to `CLOSED`.
- `README.md`: CI and packaging rows, known limitations.
- `blueprint/11-continuous-delivery.md`: workflow execution status and boundary.
- `blueprint/improvements/continuous-improvements.md`: closure table.

### Patterns

No pattern is applied; the change is prose.

### ADR

Not significant. No architecture decision is introduced or changed.

## Implementation

- `blueprint/packaging/hello-service.md`: result and evidence.
- `blueprint/gates.md`: P-10 state.
- `README.md`: execution evidence.
- `blueprint/11-continuous-delivery.md`: packaging run.
- `blueprint/improvements/continuous-improvements.md`: gate state.

## Tests

- GitHub Actions run 13, event `pull_request`, head
  `2e54476379ce283de7786a61fb2e023b1ad19932`.
- Conclusion `success`; every step passed, including Secret scan, Resolve version, Build
  and test, Build container image, Record artifact identity and all artifact uploads.
- Artifacts: `artifact-metadata` (335 bytes), `hello-service-0.1.0` (18,903,433 bytes) and
  `gitleaks-report` (159 bytes).
- The secret scan used `--exit-code 1`; its success means no finding was reported.
- Run URL: `https://github.com/JorvaCode/hello-service/actions/runs/41046888744`.
- The artifact contents and logs were not retrieved; names and sizes come from the GitHub
  Actions API.

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| Versioned artifact | CI | `hello-service-0.1.0` artifact |
| Verifiable identity | CI | `artifact-metadata` artifact |
| Secret scan recorded | CI | `gitleaks-report` artifact and a green Secret scan step |

## Security

The scan reported no finding. No secret is recorded in this change.

## Deployment

None. The image remains local to the runner.

## Rollback

Revert the commit; the packaging record returns to its pending state.

## Deletions

None.
