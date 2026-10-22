---
name: blueprint-requirements
description: Apply the Software Engineering Blueprint when analysing or specifying requirements, before any implementation starts.
---

# Blueprint — Requirements and Analysis

Covers lifecycle phases 01 and 02. Read the phase documents; they are the source of truth.

All paths below are relative to the project root.

## Documents

- `blueprint/01-requirements.md`
- `blueprint/02-analysis.md`
- `templates/requirement.md`
- `templates/change.md`

## Standards

- `standards/information-items.md` — what the requirement artifact shall contain.
- `standards/normative-language.md` — how to write an acceptance criterion that can fail.
- `standards/terms.md` — load it before using a term the phase documents do not define.

## Procedure

1. Read `blueprint/02-analysis.md` first if the existing system is not yet understood:
   current components, domain concepts, dependencies, integration points, constraints,
   alternatives and trade-offs.
2. Fill in `templates/requirement.md` with problem, objective, scope, functional and
   non-functional requirements, acceptance criteria, dependencies, risks and assumptions.
3. Make every acceptance criterion verifiable. "Works correctly" is not a criterion.
4. Separate assumptions from facts. An assumption recorded as a fact becomes a silent defect.
5. Stop and ask when a requirement is ambiguous enough to change the design. Do not
   implement on assumption.

## Output

A requirement traceable to the requested change, with acceptance criteria that a test can
prove. The design work in `blueprint-architecture` consumes it.
