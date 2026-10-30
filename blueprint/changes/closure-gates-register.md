# Change: `CHG-GATE-001` — Define the closure gate identifiers

## Requirement

Satisfies no functional requirement directly. It makes the `P-nn` identifiers referenced by
`ci.yml`, `blueprint/11-continuous-delivery.md`, `README.md` and
`blueprint/improvements/continuous-improvements.md` falsifiable, as required by
`standards/normative-language.md`.

## Design

Adds one register. The change alters no structure, interface, data or dependency.

- `blueprint/gates.md`: one row per gate, naming the phase, artifact, criterion, required
  evidence, owner and current state.
- Each gate maps to the quality gate of its phase document. A gate is `CLOSED` only when the
  evidence exists for the commit under closure; configuration alone does not close it.

### Patterns

No pattern is applied; the change is a register.

### ADR

Not significant. The register applies existing phase gates; it introduces no architecture
decision.

## Implementation

- `blueprint/gates.md`: new file.
- This change set updates the references in `11-continuous-delivery.md`, `README.md` and
  `continuous-improvements.md` to point to it.

## Tests

No executable behaviour changes. Verification is a reading of the register against the phase
documents it cites.

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| Every referenced gate is defined | Document review | P-02, P-06, P-07, P-10 and P-11 each have a row |
| Every row is falsifiable | Document review | Each row names artifact, criterion and evidence |
| No gate is closed by configuration | Document review | Current states distinguish evidence from configuration |

## Security

None. No dependency, endpoint, secret or input path is added.

## Deployment

None.

## Rollback

Revert the commit; the register is removed and the references must be reverted with it.

## Deletions

None.
