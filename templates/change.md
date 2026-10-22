# Change: `<ID>` — `<Title>`

## Requirement
The `FR-nn` or `NFR-nn` this change satisfies, by identifier. A change that satisfies none says
so here; it does not leave the field empty, and it does not invent an identifier. A dependency
bump, a prose edit and a new capability are not the same kind of change, and this field is how a
reviewer tells them apart.

-

## Design
Summary of the technical approach. State whether the change alters **structure, interface, data
or dependencies**; a change that alters none of them says so rather than leaving the choice open.

### Patterns
For each pattern applied, the four items `standards/code-design.md` requires: the problem it
solves, the evidence that the problem is real, the cost of the simpler alternative, and the
scope. **A change that applies no pattern records that**, which is not a finding. Code that does
not use a pattern is not a defect.

-

### ADR
Is this alteration significant by the criteria in `standards/terms.md`? If so, it is recorded in
`blueprint/architecture/adr/`. Recording one that is not significant is permitted.

-

## Implementation
What changes, and in which increments. Each increment names the requirement it realises.

-

## Tests
The test strategy, and why the risk justifies those levels. Where the architecture produced
quality attribute scenarios, the levels are derived from their response measures rather than
chosen independently. Tests are written in the same change as the code they cover.

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| AC-01 | | |

## Security
Relevant considerations, or an explicit statement that the change adds no dependency, endpoint,
secret or input path and therefore reviewed none.

-

## Deployment
Special deployment/configuration requirements, or that there are none.

-

## Rollback
How is the change reverted, and the evidence that the path was rehearsed. A change that cannot
be rolled back records why, which is a reason and not an omission.

-

## Deletions
Tests or artifacts this change removes, each with its reason. An unrelated deletion is not
housekeeping; it is an undeclared change.
