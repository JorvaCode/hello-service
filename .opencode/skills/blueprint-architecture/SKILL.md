---
name: blueprint-architecture
description: Apply the Software Engineering Blueprint for architecture and technical design decisions.
---

# Blueprint — Architecture and Technical Design

Covers lifecycle phases 03 and 04. Read the phase documents; they are the source of truth.

All paths below are relative to the project root.

## Documents

- `blueprint/03-architecture.md`
- `blueprint/04-design.md`
- `standards/code-design.md`
- `templates/adr.md`
- `templates/change.md`
- `blueprint/architecture/adr/` — decisions recorded so far; read before proposing a new one

## Standards

- `standards/code-design.md` — design quality, SOLID, Clean Code and pattern selection.
- `standards/terms.md` — the 42010 vocabulary: stakeholder, concern, viewpoint, view, and
  the significance criteria that decide whether a decision needs an ADR.
- `standards/normative-language.md` — modality, and the three parts a gate needs to fail.
- `standards/information-items.md` — what the design artifact shall contain.

## Procedure

1. Read the existing ADRs and the current code before proposing a design. The default
   answer is the existing architecture, not a new one.
2. Establish component boundaries, responsibilities, dependencies, APIs, data ownership,
   integration patterns and security boundaries.
3. State at least two alternatives and the trade-off that made you choose. A decision with
   no rejected alternative is not a decision.
4. Record every significant decision with `templates/adr.md` in `blueprint/architecture/adr/`,
   named `adr-NNN-slug.md` and following the format of the existing ADRs.
5. Define interfaces, contracts, data models, validation, error handling, transaction
   boundaries and the test strategy.
6. Select a pattern only when `standards/code-design.md` is satisfied, and record the
   justification in `templates/change.md` or in an ADR. The simpler option is the default;
   a pattern without a stated problem is not a design.
7. Reject designs that cannot be tested.

## Output

An ADR per significant decision plus a `templates/change.md` describing an
implementation-ready design with a stated test strategy. Any pattern named here is
accompanied by its justification, or omitted.
