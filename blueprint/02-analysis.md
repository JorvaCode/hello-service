# 02 — Analysis

Analyse the requirement before choosing an implementation.

## Activities
- Understand current behaviour.
- Identify domain concepts.
- Identify dependencies and integrations.
- Identify alternatives.
- Evaluate constraints and risks.
- Record the assumptions that would change a functional requirement or a threshold if they
  proved false. An assumption that changes nothing is not worth recording, and one that is
  recorded because it felt important is indistinguishable from one that is not.

## Information items
### Inputs
- **Requirements** — the outputs of `01-requirements.md`, including their acceptance criteria and thresholds.
- **Existing system context** — the current implementation the change has to fit into.

### Outputs
- **Analysis record** — the domain concepts identified, the dependencies and integrations found, the constraints and risks evaluated, and the alternatives considered with the reason one direction was chosen. A direction chosen without recording the alternatives is not an analysis.
- **Assumption register** — each assumption with the consequence if it proves false. An assumption that would change a functional requirement or a non-functional threshold is fed back to `01-requirements.md` rather than carried into design.

## Quality gate
Every assumption in the analysis record is either confirmed, or carried into the requirements as a named risk. No ambiguity that would change a functional requirement or a non-functional threshold remains open: it is resolved in this phase or fed back, not deferred to implementation. A reviewer who did not write the analysis reaches the same verdict on whether the direction is implementable.
