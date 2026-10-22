# 08 — Security and Quality

Security and quality checks are part of the delivery pipeline.

## Typical controls
- Dependency vulnerability scanning
- Secret detection
- Static analysis
- Formatting/linting
- License checks where required
- Container image scanning where applicable
- SAST/DAST where appropriate
- Architecture or dependency rule checks where the project can enforce them

## Principle
Controls should be proportional to project risk and should provide actionable feedback.
Where the project can automate part of `standards/code-design.md` — static analysis, dependency
rules, complexity or duplication thresholds — it should; the rest stays in code review.

## Information items
### Inputs
- **The change** — its diff and the dependency manifest it modifies.
- **Architecture** — the security boundaries and data ownership from `03-architecture.md`, which determine what is actually at risk.
- **Control inventory** — the controls the project runs, and the risk rating of the change.

### Outputs
- **Control results** — for each control the project runs: pass, fail, or skipped, with the tool and its version. A control with no recorded result is not assumed to have passed.
- **Findings** — each with a severity and either a fix or a residual risk acceptance that names an owner and a review date.
- **Proportionality record** — which controls were skipped and why, where the risk rating permits it. This is the artifact that distinguishes a deliberate decision from an oversight.

## Quality gate
Every control in the project's inventory has a recorded result for the current commit, and every finding is either fixed or carries a residual risk acceptance with an owner and a review date. A reviewer who did not run the controls reaches the same verdict from the control results and the proportionality record.
