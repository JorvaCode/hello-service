# Change: `CHG-IMP-001` — Record the scope closure decisions

## Requirement

Satisfies no functional requirement directly. It records the approved closure decisions for
the `hello-service` PoC under phase 14 and keeps the improvement candidates deferred.

## Design

Documentation only. The change alters no structure, interface, data or dependency.

- Add a `Scope Closure Record` table to
  `blueprint/improvements/continuous-improvements.md`.
- Add learning 5: configuration is evidence that a capability is wired, not that it ran.
- Keep `IMP-001`, `IMP-002` and `IMP-003` as `Deferred`.

### Patterns

No pattern is applied; the change is prose.

### ADR

Not significant. No architecture decision is introduced or changed.

## Implementation

- `blueprint/improvements/continuous-improvements.md`: add the closure record, the learning
  and the gates register reference.

## Tests

No executable behaviour changes. Verification is a reading of the closure record against the
artifacts it cites.

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| Closure decisions are explicit | Document review | The table names each item, its treatment and its state |
| Gates are not closed by postponement | Document review | The table and `blueprint/gates.md` agree |

## Security

None. No dependency, endpoint, secret or input path is added.

## Deployment

None.

## Rollback

Revert the commit; the previous document text is restored.

## Deletions

None.
