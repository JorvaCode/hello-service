---
name: blueprint-development
description: Apply the Software Engineering Blueprint during implementation, testing, code review and security or quality checks.
---

# Blueprint — Implementation, Testing, Review and Quality

Covers lifecycle phases 05 to 08. Read the phase documents; they are the source of truth.

All paths below are relative to the project root.

## Documents

- `blueprint/05-implementation.md`
- `blueprint/06-testing.md`
- `blueprint/07-code-review.md`
- `blueprint/08-security-quality.md`
- `standards/code-design.md`
- `standards/definition-of-done.md`
- `standards/git.md`
- `standards/normative-language.md`
- `standards/terms.md`
- `standards/information-items.md`
- `templates/change.md`

## Procedure

1. Start from an approved requirement and design. If either is missing, return to
   `blueprint-requirements` or `blueprint-architecture`.
2. Make the smallest coherent change that satisfies the acceptance criteria.
3. Follow project standards and preserve architectural boundaries. No unrelated
   refactoring in the same change.
4. Check the change against `standards/code-design.md`. A SOLID or Clean Code question
   becomes a finding only when it names a concrete problem, and no pattern, abstraction or
   base class is introduced beyond what the design justified.
5. Write tests in the same change as the code. Pick the levels the risk justifies —
   unit, integration, contract, end to end, performance, security.
6. Every acceptance criterion from the requirement must map to a test or an explicit
   verification step. An unmapped criterion is unfinished work.
7. Review the diff against requirement, architecture, maintainability, error handling,
   tests, security and performance.
8. Run the quality and security checks the project actually has: static analysis,
   formatting, lint, dependency vulnerabilities, secret detection, licensing.

## Output

Code, tests, and a change that passes the applicable items in
`standards/definition-of-done.md`. Report which items were skipped and why, including
the design quality check: record the findings, or state that there were none.
