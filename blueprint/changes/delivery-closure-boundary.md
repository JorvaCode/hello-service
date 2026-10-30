# Change: `CHG-DEL-001` — Record the PoC delivery boundary

## Requirement

Satisfies no functional requirement directly. It documents the delivery scope of the
`hello-service` PoC and the status of `REQ-HELLO-001` and `REQ-STATUS-001` for phase 11.

## Design

Documentation only. The change alters no structure, interface, data or dependency.

- `blueprint/11-continuous-delivery.md` records the external prerequisites, the conditions
  not verified, the workflow execution status and the current PoC boundary.
- The `ci.yml` publication path is removed by `CHG-CI-001`; this record states that
  publication now lives only in `cd.yml` and remains postponed.
- The gate identifiers are defined in `blueprint/gates.md`.

### Patterns

No pattern is applied; the change is prose.

### ADR

Not significant. No architecture decision is introduced or changed.

## Implementation

- `blueprint/11-continuous-delivery.md`: add the external prerequisites, unverified
  conditions, workflow execution status, PoC boundary and the gates register reference.
- Correct the earlier statement that `ci.yml` contains a `PUBLICATION_BLOCKED` step: after
  `CHG-CI-001`, `ci.yml` has no publication path.

## Tests

No executable behaviour changes. Verification is a reading of the claims against
`.github/workflows/ci.yml` and `.github/workflows/cd.yml`.

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| Claims match the workflows | Document review | Read `11-continuous-delivery.md` against both workflow files |
| Gate identifiers resolve | Document review | `blueprint/gates.md` contains P-02, P-06, P-07, P-10 and P-11 |

## Security

No secret, credential or input path is added. The change documents that no remote
publication was performed.

## Deployment

None.

## Rollback

Revert the commit; the previous document text is restored.

## Deletions

None.
