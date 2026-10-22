# 14 — Feedback and Continuous Improvement

The lifecycle is a feedback loop.

Production and development feedback should influence future requirements, architecture and implementation.

## Sources
- Incidents
- Metrics
- User feedback
- Retrospectives
- Technical debt
- Security findings
- Performance findings

## Loop

Requirement -> Design -> Build -> Test -> Deliver -> Operate -> Learn -> Improve

## Information items
### Inputs
- **Operational feedback** — incidents, metrics, user feedback, retrospectives, technical debt, security findings and performance findings, each from a named source.
- **Previous improvement actions** — what was already changed, so the same finding is not raised twice.
- **Delivered scope** — the requirements and changes the last releases carried, from `11-continuous-delivery.md`, so an item is not raised against work that already shipped or against work that was never declared.

### Outputs
- **Improvement items** — each stating the artifact it changes, an owner, and a link back to the feedback that produced it. An item that changes a requirement names the `FR-nn` or `NFR-nn` it revises, because an undeclared requirement change is a change nobody can trace or review. A finding with no owner is a wish, not an improvement item.
- **Lifecycle changes** — the phase documents, standards, templates or ADRs this feedback changes, or an explicit record that no change is needed and why.
- **Closure record** — what was changed and what evidence the change took effect, so the loop visibly closes. An item that changed a requirement names the release that delivered the revision.

## Quality gate
Every improvement item has an owner and names the artifact it changes, and every closure record states the evidence that the change took effect. An item that revises a requirement names the identifier it revises and the release that delivered the revision. A reviewer can trace each item back to the feedback that raised it, and each change back to an item, and reaches the same verdict on whether the loop closed.
