# 07 — Code Review

Review changes against requirements, architecture, maintainability, security and tests.

## Review checklist
- Requirement satisfied?
- Design consistent with architecture?
- Does each new name in the code assert a decision or a behaviour a third party can check?
- Design quality per `standards/code-design.md`?
- Any pattern or abstraction without a stated problem, evidence and scope?
- Error handling appropriate?
- Tests adequate?
- Security concerns addressed?
- Performance concerns addressed?
- No unnecessary scope?

The reviewer does not require patterns to be added. A pattern without the justification
defined in `standards/code-design.md` is a finding; the absence of a pattern is not.

## Information items
### Inputs
- **The change** — its diff, together with the design it implements.
- **Test and CI results** — from `06-testing.md` and `09-continuous-integration.md`.
- **Reviewers** — the required reviewers, named in the project's contribution rules.

### Outputs
- **Review record** — each checklist item answered with a finding or an explicit "no finding". An item that was not considered is not the same as an item that was considered and found clean.
- **Findings** — each with a severity and a disposition: fixed in this change, deferred with an owner, or accepted as residual risk.
- **Decision record** — where the review surfaces a decision that is significant per `standards/terms.md`, an ADR is raised through `03-architecture.md` rather than settled in the review thread.

## Quality gate
Every required reviewer has recorded a verdict, every automated check has passed, and every finding is either fixed or carries a disposition. A reviewer who did not participate reaches the same verdict from the review record and the pipeline result.
