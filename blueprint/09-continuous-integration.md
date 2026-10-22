# 09 — Continuous Integration

CI validates every relevant change automatically.

## Typical pipeline
1. Checkout
2. Resolve dependencies
3. Build
4. Unit tests
5. Integration tests
6. Static analysis / lint
7. Security checks
8. Package
9. Publish test artifacts when useful

## Information items
### Inputs
- **The change** — the commit under validation.
- **Required checks** — the checks the project has declared mandatory, recorded where the pipeline can read them. "Required" without a declaration is not a criterion, because a skipped check and a passing check look identical in the result.
- **Build and test configuration** — from the project.

### Outputs
- **Pipeline definition** — the workflow, in version control, naming every required check.
- **Pipeline run result** — per required check: passed, failed or skipped, with the reason for a skip.
- **Integrability verdict** — integrable or not, derived from the required checks and nothing else.

## Quality gate
Every check the pipeline declares as required has a result of passed for the current commit; a skipped required check fails the run. The verdict is integrable only when all of them passed, and a reviewer who did not watch the run reaches the same verdict from the run result.

The workflow examples in `.github/workflows/` provide a technology-neutral starting point.
