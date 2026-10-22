# 04 — Technical Design

Translate architecture into an implementation-ready design.

## Activities
- Define interfaces and contracts.
- Define data models.
- Define error handling.
- Define validation.
- Define transactional boundaries.
- Select appropriate patterns.
- Define test strategy.
- Identify configuration and operational needs.

## Information items
### Inputs
- **Architecture description and ADRs** — the outputs of `03-architecture.md`.
- **Requirements** — the functional and non-functional requirements, with their acceptance criteria.
- **Code design standard** — `standards/code-design.md`, which the design is checked against.

### Outputs
- **Design** — interfaces and contracts, data models, error handling, validation, transactional boundaries, and configuration and operational needs. Each element states the requirement it satisfies.
- **Pattern justification** — for each pattern applied, the four items required by `standards/code-design.md`: the problem it solves, the evidence, the cost of the simpler alternative, and the scope. Absent when no pattern is applied, which is not a defect.
- **Test strategy** — which testing levels apply to this change, and why the risk justifies those levels. A strategy that applies every level to every change is not a strategy. Where `03-architecture.md` produced quality attribute scenarios, the levels are derived from their response measures rather than chosen independently.

## Pattern selection
A pattern is selected only when `standards/code-design.md` is satisfied: the design states
the problem it solves, the evidence that the problem is real, the cost of the simpler
alternative, and the scope of application. Code that does not use a pattern is not a defect.

## Quality gate
A developer who did not attend the design can implement the change without inventing an architectural decision: every interface, data model and transactional boundary is stated, and each element of the design names the requirement it satisfies. The reviewer confirms this from the design document alone, without consulting the author.
