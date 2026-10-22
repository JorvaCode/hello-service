---
name: blueprint
description: Route a software change through the Software Engineering Blueprint lifecycle, from requirements to continuous improvement. Use for any feature, bug, refactor or technical request that needs more than a one-shot edit.
---

# Software Engineering Blueprint

This skill is a router, not a copy of the blueprint. The normative definition lives in
`blueprint/`. Read the phase document you need; do not work from memory of the phases.

All paths below are relative to the project root.

## Operating rules

1. Determine the current lifecycle phase before acting.
2. Inspect the existing project before making assumptions.
3. Do not implement while requirements are ambiguous — ask.
4. Never skip a phase silently; if one does not apply, say why.
5. Produce or update the artifact the phase requires.
6. Keep traceability between requirement, design, implementation and tests.
7. Verify `standards/definition-of-done.md` before declaring work complete.
8. Check design quality against `standards/code-design.md`. Patterns require a stated
   justification; their absence is never a defect.

`blueprint/00-principles.md` holds the full principle set.

## Phase map

| Phase | Document | Typical artifact |
| --- | --- | --- |
| 00 Principles | `blueprint/00-principles.md` | — |
| 01 Requirements | `blueprint/01-requirements.md` | `templates/requirement.md` |
| 02 Analysis | `blueprint/02-analysis.md` | notes in the requirement |
| 03 Architecture | `blueprint/03-architecture.md` | `templates/adr.md` |
| 04 Technical design | `blueprint/04-design.md` | `templates/change.md` |
| 05 Implementation | `blueprint/05-implementation.md` | code + tests |
| 06 Testing | `blueprint/06-testing.md` | test results |
| 07 Code review | `blueprint/07-code-review.md` | review findings |
| 08 Security & quality | `blueprint/08-security-quality.md` | scan results |
| 09 Continuous integration | `blueprint/09-continuous-integration.md` | pipeline run |
| 10 Packaging | `blueprint/10-packaging.md` | versioned artifact |
| 11 Continuous delivery | `blueprint/11-continuous-delivery.md` | promotion record |
| 12 Deployment | `blueprint/12-deployment.md` | deployment record |
| 13 Observability | `blueprint/13-observability.md` | signals and alerts |
| 14 Continuous improvement | `blueprint/14-continuous-improvement.md` | backlog items |

## Workflow

1. Read `blueprint/01-requirements.md` and fill in `templates/requirement.md`.
2. If the change is significant, read `blueprint/03-architecture.md` and record a decision
   with `templates/adr.md`.
3. Read `blueprint/04-design.md` and record the approach with `templates/change.md`.
4. Implement following `blueprint/05-implementation.md` and `blueprint/06-testing.md`.
5. Run the checks in `blueprint/08-security-quality.md` that apply to the project.
6. Close against `standards/definition-of-done.md`, `standards/git.md` and
   `standards/code-design.md`.

## Standards

The standards in `standards/` apply to every change, not only to one phase. Load the ones
the change needs; a standard that is never routed to is a standard nobody reads.

- `standards/definition-of-done.md` — the criteria a change is closed against.
- `standards/git.md` — branches, commits and pull requests.
- `standards/code-design.md` — design quality, SOLID, Clean Code and pattern selection.
- `standards/normative-language.md` — modality, and how to write a gate that can fail.
- `standards/terms.md` — the vocabulary; load it before interpreting a term the phase
  documents do not define.
- `standards/information-items.md` — what a phase artifact shall contain.

## Phase skills

Load these when a single phase needs focused attention:

- `blueprint-requirements` — phases 01 and 02.
- `blueprint-architecture` — phases 03 and 04.
- `blueprint-development` — phases 05 to 08.
- `blueprint-cicd` — phases 09 to 14.

## Subagents

For a full autonomous pass over a repository with no interactive user — a gap report, a
readiness audit, a scaffold — invoke the `blueprint-orchestrator` subagent instead of
loading this skill repeatedly.

## Scope

The blueprint is IDE and technology agnostic. OpenCode is an optional adapter
(ADR-002), not a dependency: the process applies unchanged in IntelliJ IDEA, Visual Studio,
VS Code or any other environment.
