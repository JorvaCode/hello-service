# Requirement: `<ID>` — `<Title>`

## Problem
What problem are we solving? Stated as a need, not as a proposed solution.

## Objective
What outcome do we want, and how will we know we got it?

## Scope
### In scope
-

### Out of scope
-

## Functional requirements
Each states one behaviour and is decidable by a single test.

| ID | Requirement | Verification |
| --- | --- | --- |
| FR-01 | | |

## Non-functional requirements
Each names the quality attribute it constrains and a threshold where one applies. "Fast",
"secure" and "scalable" are not attributes; "p95 latency under 200 ms at 1000 rps" is.

| ID | Quality attribute | Requirement | Verification |
| --- | --- | --- | --- |
| NFR-01 | | | |

## Acceptance criteria
Numbered `AC-nn`, at least one per functional requirement. The identifier survives a rewording
of the criterion, so the test that verifies it and the release that delivered it name the same
thing. Each criterion states the observable condition and the method that verifies it; a
criterion that cannot fail is not a criterion.

| ID | Covers | Given | When | Then (observable condition) | Verification method |
| --- | --- | --- | --- | --- | --- |
| AC-01 | FR-01 | | | | |

## Dependencies
-

## Risks / assumptions
Each assumption states what breaks if it is false.

-

## Traceability
- **Realised by** — the artifact that satisfies this requirement. Until one exists, this
  requirement is declared and not delivered.
- **Delivered in** — the release that carries it, per the release record of
  `blueprint/11-continuous-delivery.md`.

A change that satisfies no requirement records that instead of inventing an identifier. A prose
erratum is not a capability; see `standards/information-items.md`.
