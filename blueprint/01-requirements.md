# 01 — Requirements Specification

## Objective
Convert a business or technical need into a specification whose acceptance criteria a third party can decide.

## Information items
### Inputs
- **Problem or opportunity** — the trigger, stated as a need rather than a solution.
- **Stakeholders** — who is affected by the change and who decides on it.
- **Constraints** — regulatory, contractual, technical and organisational, each with its source.
- **Existing system context** — what exists today, so the requirement is anchored to reality rather than to assumption.

### Outputs
- **Problem statement** — the need, the affected parties, and why it matters now. Use `templates/requirement.md`.
- **Scope** — what is in scope and, explicitly, what is not.
- **Functional requirements** — numbered `FR-nn`, each independently decidable by a single test and each stating one behaviour.
- **Non-functional requirements** — numbered `NFR-nn`, each naming the quality attribute it constrains and a threshold where one applies.
- **Acceptance criteria** — numbered `AC-nn`, at least one per functional requirement, written so a test can fail against them, each naming its verification method. The identifier survives a rewording of the criterion, so the test that verifies it and the release that delivered it name the same thing; an identifier that abbreviates its own sentence dies with the sentence.
- **Risks and assumptions** — each assumption stating what breaks if it is false.

An identifier is never reused for a different requirement, and a requirement that changes meaning keeps its identifier with a new version rather than acquiring a new one. `standards/information-items.md` states why.

## Quality gate
Every functional requirement in `templates/requirement.md` carries an `FR-nn` identifier, states one behaviour, and has at least one numbered `AC-nn` acceptance criterion that names both the observable condition that decides it and the method that verifies that condition. No identifier is reused for a different requirement. A reviewer who did not write the requirement writes the test that decides it from the artifact alone, and reaches the same verdict.

## Template
Use `templates/requirement.md`.
