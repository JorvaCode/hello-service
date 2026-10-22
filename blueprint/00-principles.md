# 00 — Principles

## 1. Requirements before implementation
Every change starts with an explicit problem, objective and acceptance criteria.

## 2. Architecture is intentional
Architectural decisions that are significant by the criteria in `standards/terms.md` are
documented and traceable. A decision that is merely felt to be important is not a decision
that gets an ADR, and a decision that is significant is recorded whether or not it felt
important at the time.

## 3. Small, verifiable changes
Changes should be small enough to be built, tested and reviewed independently.

## 4. Quality is continuous
Quality checks are performed throughout the lifecycle, not only before release.

## 5. Automation first
Repeatable activities should be automated whenever practical.

## 6. Security by design
Security requirements and checks are part of the normal delivery flow.

## 7. Traceability
A requirement is traceable through design, implementation, tests and delivery, per
`standards/information-items.md`. The chain is named on items that already exist, so it
costs a field rather than a document nobody maintains.

## 8. Feedback loop
Production and development feedback feed continuous improvement.

## 9. Tool independence
The process shall not depend on a particular IDE or coding assistant.

## 10. Design quality is justified, not assumed
Code and design decisions are checked against `standards/code-design.md`. A design
principle or a pattern is applied when a stated problem justifies it, never by
custom or by anticipation. The absence of a pattern is not a defect.
