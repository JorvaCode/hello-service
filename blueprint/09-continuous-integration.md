# 09 — Continuous Integration

CI validates every relevant change automatically and continuously, before it is considered integrable.

Phase artifact of REQ-BP-001 (blueprint/requirements/software-engineering-blueprint.md). Status: v1.0.0. Traceability: FR-02 (each phase defines what must happen, expected artifacts and quality gates), FR-05 / NFR-03 (CI examples are adapters, not requirements), ADR-001 (IDE/tool agnostic), ADR-003 (docs-as-code), ADR-004 (distribution model).

## 1. Objective
Automatically validate that a change builds, passes tests, satisfies the applicable quality and security checks, and produces a versioned artifact. CI is the automated gate between "code review approved" and "change integrable".

## 2. Scope
### In scope
- Automating build, test, quality and security checks for every relevant change.
- Producing the artifact that later phases (10 Packaging, 11 Continuous Delivery) will consume.
- Failing fast and giving actionable feedback.

### Out of scope
- Deploying to environments (Phase 11/12). CI stops at a validated, versioned artifact.
- Mandating a specific CI provider, language, framework or repository hosting platform.

## 3. Prerequisites
- Repository hosted on a platform able to run CI (GitHub, GitLab, Azure DevOps, self-hosted runner, etc.).
- Phases up to 08 completed: requirement, analysis, architecture, design, implementation, testing, code review, security & quality.
- Reviewers approved the change (Phase 7 gate).
- Required checks that exist today in the project are available to run headlessly (no IDE dependency).
- Repository state follows `standards/git.md` (short-lived branches, main branch buildable).

## 4. CI triggers
CI runs automatically, without manual build steps:
- On every push to a branch (fast feedback).
- On every pull request (validation of proposed changes).
- On merges to the main branch (branch-level validation).
- Optional: on a schedule for dependency/security monitoring, and manually on demand (re-run/full pipeline).

The exact trigger set is a project decision; at minimum a change is not integrable until its PR/branch CI passes.

## 5. Build
- Checkout the exact commit under validation (no mutable state from the developer's workspace).
- Resolve dependencies from the declared sources; use lockfiles/dependency management where the ecosystem allows reproducibility.
- Compile the project and produce the artifact with the same commands a developer would run (e.g. the Gradle wrapper, Maven wrapper or equivalent), to avoid "works on my machine".
- Use the repository's pinned toolchain versions (e.g. `gradle-wrapper.properties`, toolchain files).
- Never build from a local IDE; CI is the shared, reproducible source of truth.

## 6. Tests
- Run the project's automated test suites (unit, integration, contract, etc.) in the same CI run as the build.
- Tests must be deterministic: no reliance on local state, real external services not under the project's control, or secrets.
- A test failure fails the pipeline and blocks integration.

## 7. Quality gates
The default quality gates are:
- Build succeeds.
- All required automated tests pass.
- Checks mandated by Phase 8 (Security & Quality) that are already configured in the project pass.

Gates that a project may enable, proportional to risk (see Section 8 and Phase 08 follow-ups):
- Formatting/lint enforcement.
- Dependency vulnerability scan.
- Static analysis (SAST) and container scanning where applicable.

A change is integrable only when the gates defined for the project pass.

## 8. Available security validations
Phase 8 defines the security/quality controls and their results (blueprint/08-security-quality.md). CI should run, automatically and repeatedly, the controls the project has configured, including:
- Dependency inspection / fixed version resolution.
- Secret detection (no secrets in source).
- Vulnerability scanning, lint and static analysis if the project adopted them.

Phase 08 follow-ups (lint, vulnerability scanning) can be integrated here as optional, proportional controls. They become mandatory only if the adopting project — not the Blueprint — adopts them as required gates. CI must never require secrets inside the repository; secrets are supplied by the CI platform as protected variables.

## 9. Dependency management
- Dependencies are resolved during CI from the project's declared repositories with pinned or managed versions (e.g. a BOM, lockfile, or wrapper) to make builds reproducible.
- A change that adds, removes or upgrades dependencies must still pass the build and tests.
- Changes to dependencies should be visible and reviewable through the same pull request/commit traceability as code.
- Keep secrets out of dependency configuration (e.g. no credentials committed in repositories, no credentials in `dependencies` config).

## 10. Artifacts
- Headless build (worktree) is the only source of the artifact.
- Produce a versioned artifact (e.g. application package, library, container image) only when Phase 10 defines it as applicable.
- Artifact metadata/provenance (commit, build, version) is recorded where practical so the artifact can be identified and reproduced.
- No secrets embedded in artifacts.

## 11. Failure handling
- Fail fast: as soon as a stage fails, the pipeline reports it and does not proceed.
- Feedback must be actionable (which stage, which job, which path/test) and reachable by the author (commit status, PR check, notification).
- A failed CI does not block other teams; it blocks the specific change.
- Re-running a failed pipeline uses the same checks; disabling a failing check is not a valid way to pass CI. If a check is wrong, fix the check or revisit the gate decision explicitly.

## 12. Traceability
- Every pipeline run is linked to the exact commit/PR it validates.
- Every change under CI references its requirement (standards/git.md) so the run maps to the phase artifacts produced from Phase 1 onward.
- The artifact produced by CI carries the commit/version identity for Phase 10/11 (same artifact promoted onwards).

## 13. Definition of Done / exit criteria
The change leaves Phase 09 only when:
- The build succeeds in CI on the exact commit.
- All required automated tests pass (Phase 6 verification).
- The applicable Phase 8 security/quality gates pass.
- The step "CI passes" of the Definition of Done (standards/definition-of-done.md) is satisfied.
- The artifact is versioned and identifiable per Section 10 (where applicable).

## 14. Requirements to consider CI PASS
Accepted: the pipeline completed with every required gate green for the commit under validation, and the result is attributable to that commit. Not accepted: a run that was skipped, only partially executed, or whose failing checks were silently disabled.

## 15. Relationship with Git and pull requests
- Abides by `standards/git.md`: short-lived branches, small meaningful commits, PR review for non-trivial changes, main branch kept buildable.
- CI runs on PRs to give authors early feedback and on the main branch to protect trunk.
- Merging is allowed only when the required CI checks pass for the merge target state; a PR whose CI fails is not integrable (Phase 7 gate + this phase).
- CI status is part of the merge criteria; the review recorded in Phase 7 references the automated evidence.

## 16. Relationship with previous phases
- Requires the Phase 7 gate (reviewer approval) and Phase 8 gate (security/quality) to be satisfied.
- Consumes the test suites produced in Phase 6 and the tools/configuration defined during design and implementation.
- Phase 8 controls that cannot run locally or are periodic (scans, lint in CI) move here explicitly.
- Uses the same commands, toolchain and structure defined earlier so local and CI results agree.

## 17. Relationship with the future Continuous Delivery
- Passes forward a versioned, validated artifact (Section 10).
- Phase 11 consumes that same artifact (prefer promoting the identical artifact between environments).
- CI does not deploy; environments, promotion and approval are decisions of Phases 11/12.

## Gate
A change is integrable only when the exact commit passes all the project's required CI checks and the "CI passes" item of the Definition of Done is met.

## CI provider adapters (examples, not requirements)
The Blueprint is tool-agnostic (ADR-001, NFR-03). Any CI provider may implement this phase: GitHub Actions, GitLab CI, Azure DevOps, TeamCity, Jenkins, or a self-hosted runner. Example workflow files provided under `.github/workflows/` of a project (or the Blueprint repository) are adapters of this phase, not the definition itself. If no workflow exists yet in a project, adopting a provider is a project decision made with ADR-001-style tool-choice guidance.