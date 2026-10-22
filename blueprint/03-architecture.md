# 03 — Architecture

Define the structural solution and its boundaries.

## Consider
- System boundaries
- Components and responsibilities
- Dependencies
- APIs and contracts
- Data ownership
- Integration patterns
- Security boundaries
- Scalability and availability
- Observability
- Deployment model

## Stakeholders and concerns
Name the stakeholders and the concerns they hold before describing any structure, because a
structure is designed to satisfy concerns and a concern with no stakeholder has nobody to
resolve it for. Use the vocabulary in `standards/terms.md`, which follows
ISO/IEC/IEEE 42010:2022.

A change is only required to state the stakeholders it affects and the concerns those
stakeholders hold. A project that has not established a full stakeholder register states
the ones it knows, and the known gap is recorded rather than hidden. Neither an absent
register nor a guessed one is a defect.

## Information items
### Inputs
- **Requirements and constraints** — the outputs of `01-requirements.md` and the analysis record, including the non-functional requirements that carry thresholds.
- **Existing system context** — the current architecture the change has to fit into.
- **Stakeholders and concerns** — the parties affected by the change and the quality attributes they hold, mapped to the non-functional requirements they justify.

### Outputs
- **Architecture description** — the structural solution: system boundaries, components and their responsibilities, dependencies, APIs and contracts, data ownership, security boundaries, and the deployment model. Each element states the requirement or constraint that makes it necessary, so that a component with no stated reason is visible as such.
- **Views** — where a single description does not serve every concern, the description is split into views, each taken from the standpoint of one viewpoint. Each view states which viewpoint it serves and which concerns it addresses, and the correspondence between views is stated with the rationale for it. A change whose concerns are all served by one description may state that instead, and the single view is not a defect.
- **Quality attribute scenarios** — for each concern that carries a threshold in `01-requirements.md`, at least one scenario with the six parts defined in `standards/terms.md`: source, stimulus, environment, artifact, response and response measure. A concern with no threshold needs no scenario.
- **Architecture decision records** — every decision that is significant, as defined in `standards/terms.md`, using `templates/adr.md`.

## Quality attribute scenarios
A scenario states what the system is judged on, not what it does. It is six fields, and a
field left empty is a scenario that cannot be tested:

| Field | Question it answers |
| --- | --- |
| Source | Who or what originates the stimulus? |
| Stimulus | What happens? |
| Environment | Under what conditions, at what load, against which state? |
| Artifact | Which part of the system is measured? |
| Response | What does the system do? |
| Response measure | How much, or how fast, before it counts as a failure? |

The response measure is the field that makes the scenario falsifiable. Without it the
scenario restates a concern, and `blueprint/01-requirements.md` has already produced a
threshold that this scenario would be ignoring.

Sensitivity is recorded when the response measure is not stable across the stated
environment: a design that meets its threshold at a load nobody runs has not met it.

## Architecture decision records
A decision that is significant, as defined in `standards/terms.md`, shall be captured using `templates/adr.md`. Recording a decision that is not significant is permitted, and is not a defect.

## Quality gate
The architecture description names every component and the requirement or constraint that makes it necessary, and every significant decision is recorded as an ADR whose Status is `Accepted` or `Superseded`. Every concern carrying a threshold in `01-requirements.md` has a quality attribute scenario whose response measure is that threshold, and where the description is split into views, each view names its viewpoint and the correspondence between views is stated.

A reviewer who did not participate in the change reaches the same verdict from the description, the scenarios and the ADR set alone. See [ADR-010](architecture/adr/adr-010-vistas-y-escenarios-de-atributo-de-calidad.md).
