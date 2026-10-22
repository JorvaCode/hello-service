# Code Design Standard

Criteria for the design quality of a change. This standard supplies the content for the
gates in `blueprint/04-design.md`, `blueprint/05-implementation.md` and
`blueprint/07-code-review.md`, and the automatable subset for `blueprint/08-security-quality.md`.

Read it as lenses, not as a catalogue. It defines how a design decision is justified and
reviewed, not which patterns to use. The concrete set of patterns a project accepts belongs
to the project.

## 1. Proportionality

These criteria are questions, not ceremony. Apply the depth the change deserves:

- Trivial change (typo, copy, single mechanical edit): no design analysis.
- Local change inside an existing boundary: the SOLID questions only where the change
  reveals a problem.
- New component, module, integration or public contract: full analysis, and a design
  artifact that records the pattern decisions.

A change is not over-engineered for not using a pattern. It is over-engineered when the
abstraction it adds has no stated problem behind it.

## 2. SOLID as a diagnostic

No SOLID property is a goal. Each one is a question, and it only becomes a finding when the
answer produces a concrete problem you can state in one sentence.

| Principle | Question | Finding only when |
| --- | --- | --- |
| Single responsibility | Does this unit change for one reason? | A second, unrelated reason for change exists, and the coupling causes real cost. |
| Open/closed | Does adding behaviour modify existing behaviour? | The change forces edits in code that was correct before, or `if`/type branches accumulate. |
| Liskov substitution | Can a subtype replace its base without surprises? | A subtype shall honour behaviour it cannot support, or callers need type checks to use it. |
| Interface segregation | Does every consumer need the whole interface? | Callers depend on operations they never call, and changes ripple to all of them. |
| Dependency inversion | Do high-level policies depend on volatile details? | A business rule cannot be exercised or replaced without the concrete infrastructure. |

Rules for using the table:

- Name the problem before proposing the fix. A violation that nobody has to pay for is not
  a defect.
- Never refactor for a principle alone. If the change does not need it, it is out of scope,
  under "Avoid unrelated changes" in `blueprint/05-implementation.md`.
- Existing behaviour is not evidence of a defect. A design that works and is not maximally
  SOLID is not a finding.
- The smallest change that removes the stated problem should be preferred. Splitting a module is
  justified when it is the fix, not as a preventive abstraction.

## 3. Clean Code as change cost

Clean Code is not style preference here. It is the cost of the next person reading and
changing this code. Review against these questions:

- Do the names say what the thing is, without requiring the body to be read?
- Can the intent of a block be read without reconstructing every step?
- Is duplication present that is genuinely repetitive, and would extracting it reduce
  change sites rather than hide differences?
- Are comments explaining why, with the obvious removed?
- Is the error handling at the level that can actually do something about it?
- Are the tests readable as a specification of behaviour?

Style that a formatter or linter can enforce is not a review item: that belongs to
`blueprint/08-security-quality.md`. Review only what automation cannot decide.

## 4. Design patterns: justified use only

A pattern is a solution with a high entry cost, traded for a problem it solves well. It is
adopted when the problem is real and recurring, never because it is the customary answer.

### Justification test

A pattern may enter the design only when the design artifact (`templates/change.md`, or the
ADR when the decision is architectural) states all four:

1. **The problem it solves.** Named concretely, in terms of this system. "Better
   separation of concerns" is not a problem; "three notification channels require editing
   the same class every time one is added" is.
2. **The evidence it is real.** Occurrences today, or a committed requirement that
   produces them. A hypothetical future is not evidence.
3. **The cost of the simpler alternative.** The solution without the pattern, and why it
   is not enough. If no simpler alternative is named and rejected, the justification is incomplete.
4. **The scope of application.** Where the pattern applies and where it does not. A pattern
   that is not bounded spreads by imitation.

A pattern named in a design without all four is an undecided item, not a decision.

### Absence is not a finding

- Code that does not use a pattern is not a defect, and is not a review finding.
- A pattern already present is not removed on principle. It is changed when it causes a
  stated problem, and then like any other change.
- One use does not establish a pattern. A second, different place does not require one
  either: three or more genuine instances, or a committed commitment, is the threshold.
- Do not introduce a pattern to satisfy a review preference. Record the disagreement and
  choose the simpler option.

### Anti-patterns to reject outright

- A generic wrapper, base class or manager with exactly one implementation and one caller.
- An interface extracted next to the only class that implements it, with no second
  implementation, no test double and no external substitutability requirement.
- Strategy/Factory selected for a switch statement with one meaningful branch.
- Abstraction introduced for a case that has not happened yet.

## 5. Where each decision is recorded

| Decision | Artifact | Phase |
| --- | --- | --- |
| Pattern adoption and its justification | `templates/change.md`, or ADR if architectural | 04 |
| Boundaries, responsibilities, dependencies | ADR | 03 |
| Test strategy for the change | `templates/change.md` | 04 |
| Review findings on design quality | Review record | 07 |
| Automated design checks the project can run | `blueprint/08-security-quality.md` | 08 |

If a design decision cannot be recorded in any of these, it is not decided yet.

## 6. Closure

`standards/definition-of-done.md` requires reporting which items were skipped and why. That
applies here: a change that skipped the pattern justification test says so explicitly, and
so does one where the lenses found nothing. "No findings" is a valid result; an unstated
result is not.
