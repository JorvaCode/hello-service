# Change: `CHG-DOC-001` — Add the hello-service README

## Requirement

Satisfies no functional requirement directly. It documents `REQ-HELLO-001` and
`REQ-STATUS-001`, the evidence that exists for each, and the known limitations of the PoC.

## Design

Adds one documentation file. The change alters no structure, interface, data or dependency.

- `README.md`: scope, prerequisites, local execution limitation, tests, endpoints, CI and
  local deployment status, postponed remote publication, known limitations and links.

### Patterns

No pattern is applied; the change is prose.

### ADR

Not significant. No architecture decision is introduced or changed.

## Implementation

- `README.md`: new file at the repository root.

## Tests

No executable behaviour changes. Verification is a reading of each claim against the
artifact it cites.

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| Claims cite an artifact | Document review | Each status row names its evidence |
| Limitations are stated | Document review | The known-limitations section names each open item |

## Security

None. No dependency, endpoint, secret or input path is added.

## Deployment

None.

## Rollback

Delete the file. No artifact or environment is affected.

## Deletions

None.

## Post-closure update (2026-11-01)

The README now records the remote publications of `0.1.0` by `cd.yml` runs 41332436297 and
41334455398, the current digest and the manual-only trigger; see
`blueprint/changes/p11-remote-publication.md`. The "postponed remote publication" wording
in the original README change is superseded by those results.
