# 05 — Implementation

Implement the approved design in small, reviewable increments.

## Rules
- Follow project coding standards.
- Preserve architectural boundaries.
- Simple solutions should be preferred.
- Apply `standards/code-design.md`: SOLID and Clean Code as diagnostics, patterns only
  when justified in the design.
- Avoid unrelated changes.
- Add tests with the implementation.
- Keep documentation synchronized.

## Information items
### Inputs
- **Design** — the outputs of `04-design.md`.
- **Project coding standards** — the conventions the project has adopted, whether or not they are written down. An unwritten standard is a decision waiting to be recorded.
- **Repository state** — the branch, the existing tests, and the current build status.

### Outputs
- **Implementation** — the change, in increments that build and test independently, each naming the `FR-nn` or `NFR-nn` it realises. An increment that realises no requirement says so; a prose edit, a dependency bump and a new capability are not the same kind of change, and the field is how a reviewer tells them apart.
- **Tests** — added in the same change as the code they cover, never deferred.
- **Conformance record** — the result of the project's lint, format and static analysis. Where the project runs none, the record states that, so the absence is a decision rather than an oversight.
- **Documentation updates** — the documents this change makes wrong, updated in the same change.

## Quality gate
The change builds, the tests pass, and the conformance record for the changed paths holds no finding the change introduced. Each increment names the requirement it realises, or states that it realises none. Design quality is recorded against `standards/code-design.md`, or the change states that the check produced no finding. A reviewer who did not pair on the change reaches the same verdict from the build output and the conformance record alone.
