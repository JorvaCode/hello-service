# Software Engineering Blueprint — Requirements

**Status: living.** This register states what the repository is held to, and is updated in the
same change that alters that. It is not a release snapshot: a snapshot goes stale the moment the
code moves, and a register that describes a version the repository has left is not a
requirement, it is a stale copy. The release that carries a change is named in `CHANGELOG.md`,
and the history of what this register once said is in `git log`.

Every entry declares three things, per `standards/information-items.md`: what is required, **how a
third party decides it**, and **which artifact realises it**. An entry without a verification
method is not checkable, and this repository does not accept checkable-sounding prose in place
of one.

## Problem
Software projects and teams deliver inconsistently because the engineering process is either
undefined, implicit, or tied to specific tools and IDEs. Requirements are often ambiguous,
analysis and design are skipped, quality and security gates are missed, and there is no
traceability from requirements to deployed software. Existing processes are rarely reusable
across projects, teams, or AI-assisted workflows.

## Objective
Provide a tool-agnostic, language-agnostic and reusable engineering lifecycle that a project or
team can apply from requirements through CI/CD, deployment, observability and continuous
improvement, defining for each phase what must happen, which artifacts are expected, and which
quality gates apply.

## Scope
### In scope
- Definition of the full lifecycle: Requirements, Analysis, Architecture, Technical Design,
  Implementation, Testing, Code Review, Security & Quality, Continuous Integration, Packaging,
  Continuous Delivery, Deployment, Observability, Continuous Improvement.
- Normative definition for each phase (`blueprint/00-principles.md`,
  `blueprint/01-requirements.md` … `blueprint/14-continuous-improvement.md`), each declaring its
  inputs, outputs and a falsifiable quality gate.
- Reusable templates for requirements, ADRs and changes (`templates/`).
- Engineering standards: Definition of Done, Git, code design quality, terms, normative language
  and information items (`standards/`).
- Architecture decision records for the decisions that shaped the above
  (`blueprint/architecture/adr/`).
- Optional OpenCode adapter: skills discoverable from `.opencode/skills/<name>/SKILL.md`.
- Example CI workflow for this repository and a reusable workflow for consumers
  (`.github/workflows/`).
- Distribution by allowlist installers that record what a consumer adopted.
- Adoption guidance and a checklist (`examples/adoption-checklist.md`).

### Out of scope
- Mandating a specific programming language, framework, IDE, cloud provider, artifact registry
  or deployment platform.
- Replacing or overriding existing project processes; the blueprint is an overlay, not a
  substitute.
- Providing the software application code or runtime that adopting projects will build.
- Deploying or operating the blueprint itself as a production service; it is a documentation
  methodology repository.
- Enforcing a particular tool stack beyond the examples provided.

### Not provided
Stated here rather than left for a consumer to discover:

- **Worked examples.** `examples/` holds a checklist, not a filled-in set of phase artifacts for
  a real project. A consumer that wants a worked reference builds one, or asks for it.
- **Tooling that enforces traceability.** The chain between requirement, test and release is
  declared and review-enforced. `standards/information-items.md` and `ADR-013` state why no
  automatic completeness check exists.
- **A commit SHA for `actions/checkout`.** The action is pinned to the `v7` major tag, so a new
  minor release can change what the gate does without a change in this repository. An exact SHA
  is the stronger posture, and it is not used here because this repository cannot verify one
  without network access, and a wrong SHA breaks CI for every contributor at once with no local
  signal. `.github/dependabot.yml` proposes the bump monthly so the review is scheduled.
- **Automated bumps for markdownlint-cli.** `npx markdownlint-cli@0.49.1` is exact, but Dependabot's
  npm integration reads a `package.json` and a lockfile, and this repository has neither on
  purpose: running the gate must not require a Node toolchain. That pin is reviewed by hand.

## Functional requirements

| ID | Requirement | Verification | Realised by |
| --- | --- | --- | --- |
| FR-01 | The lifecycle phases are defined in a fixed, ordered sequence. | `validate-blueprint.sh` finds one numbered file per phase `01`–`14` and no gap in the sequence. | `blueprint/00-principles.md` … `blueprint/14-continuous-improvement.md` |
| FR-02 | Each phase states what must happen, which artifacts it produces and which quality gates apply. | `validate-blueprint.sh` fails if a phase lacks `## Information items`, `### Inputs` or `### Outputs`. | `standards/information-items.md`, all fourteen phase documents |
| FR-03 | Reusable templates exist for requirements, ADRs and change descriptions. | `validate-blueprint.sh` fails if any of the three templates is missing or empty. | `templates/requirement.md`, `templates/adr.md`, `templates/change.md` |
| FR-04 | Engineering standards cover Definition of Done, Git, and code design quality, with SOLID and Clean Code as diagnostics and patterns only when justified. | Each standard file exists, and `validate-blueprint.sh` fails if one is missing. | `standards/definition-of-done.md`, `standards/git.md`, `standards/code-design.md` |
| FR-05 | An example CI workflow and a reusable GitHub workflow validate blueprint structure. | `ci.yml` and `reusable-blueprint-validation.yml` parse as valid workflow YAML and both run. | `.github/workflows/` |
| FR-06 | The optional OpenCode adapter exposes skills discoverable from `.opencode/skills/<name>/SKILL.md`. | A directory tree of `.opencode/skills` shows one `SKILL.md` per skill, and each names a valid phase. | `.opencode/skills/`, `blueprint/architecture/adr/adr-005-skill-agente-adaptor-opencode.md` |
| FR-07 | Adoption guidance and a checklist exist for new projects and teams. | `examples/adoption-checklist.md` exists and references at least one artifact per phase. | `examples/adoption-checklist.md`, `README.md` |
| FR-08 | The repository declares its own version and release status. | `VERSION` parses, `CHANGELOG.md` carries a section for it, and the two agree. | `VERSION`, `CHANGELOG.md` |
| FR-09 | Every phase declares what its artifacts contain, so a third party can decide sufficiency without the author. | `validate-blueprint.sh` fails if a phase output does not state its content. | `standards/information-items.md`, `blueprint/architecture/adr/adr-009-information-items-por-fase.md` |
| FR-10 | Where a quality attribute matters, the architecture produces a scenario with source, stimulus, environment, artifact, response and response measure. | A scenario in an architecture document carries all six fields; `validate-blueprint.sh` checks the section exists where the phase declares it. | `blueprint/03-architecture.md`, `blueprint/architecture/adr/adr-010-vistas-y-escenarios-de-atributo-de-calidad.md` |
| FR-11 | The test strategy is derived from the quality attribute scenarios, and each response measure is either verified by a check or declared unverified. | Phase `04` and phase `06` declare the derivation and the coverage; `validate-blueprint.sh` checks both sections exist. | `blueprint/04-design.md`, `blueprint/06-testing.md` |
| FR-12 | Normative text declares its modality, and a quality gate names artifact, criterion and evidence. | `validate-blueprint.sh` rejects a gate containing an unfalsifiable question and fails if `standards/normative-language.md` is missing. | `standards/normative-language.md`, `blueprint/architecture/adr/adr-008-terminologia-y-modalidad-normativa.md` |
| FR-13 | A significant architectural decision is recorded as an ADR following a template. | `validate-blueprint.sh` fails if an ADR lacks a status, options, decision or date. | `templates/adr.md`, `blueprint/architecture/adr/` |
| FR-14 | Distribution is by allowlist, and the installers record what a consumer actually adopted. | Running an installer produces `.blueprint-install.json` whose recorded entries match the files the destination received. | `scripts/blueprint-init.sh`, `scripts/blueprint-init.ps1`, `blueprint/architecture/adr/adr-011-distribucion-versionada-y-manifiesto.md` |
| FR-15 | The governance set exists and carries no placeholder owner. | `validate-blueprint.sh` fails on a `TODO(owner)` in a governance file, and checks each governance file exists. | `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, `blueprint/architecture/adr/adr-007-licencia-y-gobernanza-del-repositorio.md` |
| FR-16 | Documentation and code are licensed separately, and both licences reach the consumer. | `LICENSE` and `LICENSE-CODE` both exist with the expected SPDX identifiers, and an installer run against an empty destination produces both. | `LICENSE`, `LICENSE-CODE`, `blueprint/architecture/adr/adr-012-licencia-del-codigo-y-distribucion-de-licencias.md` |
| FR-17 | This repository's own gate is one script, runnable locally with no Node, no Python and no network. | `bash scripts/validate-blueprint.sh` succeeds on a checkout with those tools absent, and `ci.yml` invokes that same file. | `scripts/validate-blueprint.sh`, `.github/workflows/ci.yml` |
| FR-18 | The reusable workflow validates a consuming repository, and is not this repository's gate. | `reusable-blueprint-validation.yml` contains no reference to `scripts/validate-blueprint.sh`, and `validate-blueprint.sh` fails if it does. | `.github/workflows/reusable-blueprint-validation.yml` |
| FR-19 | A requirement, the change that realises it, the criterion that verifies it and the release that delivered it are named by identifier. | Each register row names its realisation, phase `05` requires the identifier on the implementation, and phase `11` requires it on the release record. | `standards/information-items.md`, `blueprint/architecture/adr/adr-013-trazabilidad-de-extremo-a-extremo.md` |
| FR-20 | Line endings are pinned, so the gate produces the same verdict on a Windows checkout as on a Linux one. | `.gitattributes` pins `*.sh`, `*.md` and `*.yml` to LF, and the gate fails on a CR in any of them, naming the file. | `.gitattributes`, `scripts/validate-blueprint.sh` |
| FR-21 | A CI run is bounded in time, and a run superseded by a newer commit is cancelled rather than queued. | Every job in both workflows declares `timeout-minutes`, and both workflows declare a `concurrency` group. | `.github/workflows/ci.yml`, `.github/workflows/reusable-blueprint-validation.yml` |
| FR-22 | The documented example for the reusable workflow pins a reference that actually carries the checks it describes. | The example in the header comment does not use `@v1.0.0`, and the comment states which input is not pinned exactly. | `.github/workflows/reusable-blueprint-validation.yml` |
| FR-23 | The language of each class of document is declared, so a contributor does not have to infer it from the existing files. | `CONTRIBUTING.md` states that normative content is English and decision records are in the maintainer's language, and `validate-blueprint.sh` fails if that declaration is removed. | `CONTRIBUTING.md`, `scripts/validate-blueprint.sh` |
| FR-24 | A reference to a document written in prose resolves to a file that exists. | `validate-blueprint.sh` fails when a backticked reference to an ADR, a phase document or a standard does not resolve to a file. | `scripts/validate-blueprint.sh` |
| FR-25 | The git standard states falsifiable rules, and the accepted commit types are declared in exactly one place. | `standards/git.md` carries a quality gate and the type list, the gate checks both, and `CONTRIBUTING.md` points at it instead of restating a list. | `standards/git.md`, `CONTRIBUTING.md`, `scripts/validate-blueprint.sh` |

## Non-functional requirements

| ID | Requirement | Verification | Realised by |
| --- | --- | --- | --- |
| NFR-01 | The lifecycle does not require a specific IDE. | No phase document names an IDE as a precondition. | `blueprint/00-principles.md` |
| NFR-02 | The process level does not depend on any programming language. | No phase document or standard requires a language. | `blueprint/00-principles.md` |
| NFR-03 | Tool references appear only as examples or adapters, never as obligations. | `validate-blueprint.sh` fails if a normative sentence binds a phase to a named tool. | `blueprint/00-principles.md` |
| NFR-04 | The blueprint is reusable by copying directories or referencing a central repository. | An installer run against an empty destination produces a working tree. | `scripts/blueprint-init.sh`, `scripts/blueprint-init.ps1` |
| NFR-05 | AI-assisted development is supported; OpenCode remains an optional adapter, not a dependency. | `validate-blueprint.sh` fails if the adapter is referenced as a dependency of the blueprint itself. | `blueprint/architecture/adr/adr-005-skill-agente-adaptor-opencode.md` |
| NFR-06 | The repository is maintainable as documentation: plain Markdown, versioned, with a clear structure. | `validate-blueprint.sh` runs without a Markdown toolchain, and `markdownlint-cli` reports no finding. | `.editorconfig`, `.markdownlint.json`, `VERSION` |
| NFR-07 | Traceable: a change names the requirement it satisfies and produces the corresponding phase artifacts. | `templates/change.md` requires the identifier, and a change without one fails review against the Definition of Done. | `templates/change.md`, `standards/definition-of-done.md` |
| NFR-08 | A third party can answer, for a named requirement, which release delivered it and which check verified it, without consulting an author. | The release record in phase `11` names the requirement and links to the criterion coverage of phase `06`; a reviewer reaches the same verdict from those two artifacts alone. | `blueprint/11-continuous-delivery.md`, `blueprint/06-testing.md` |
| NFR-09 | The mechanism is proportional: it is a field on an item that already exists, not a new artifact to maintain. | No phase gains an information item solely to carry traceability; `ADR-013` records the matrix option and why it was rejected. | `blueprint/architecture/adr/adr-013-trazabilidad-de-extremo-a-extremo.md` |
| NFR-10 | A declared limitation is stated in the artifact that carries it, and is removed when it stops being true. | `validate-blueprint.sh` fails if `standards/information-items.md` still declares traceability unresolved, and fails if it claims full enforcement. | `standards/information-items.md` |

## Acceptance criteria

| ID | Covers | Criterion | Verification |
| --- | --- | --- | --- |
| AC-01 | FR-01, FR-02, FR-04 | A consumer adopting the blueprint can follow the lifecycle end to end, and every phase document states its artifacts and its gate. | `bash scripts/validate-blueprint.sh` passes on a checkout. |
| AC-02 | FR-02, FR-03 | Every phase artifact a change produces follows the corresponding template, where a template exists. | The template is named in the phase document, and the artifact is checked against it in review. |
| AC-03 | FR-05, FR-17, FR-18 | The repository's CI runs, and the reusable workflow a consumer calls validates a consumer's repository rather than this one. | `ci.yml` and `reusable-blueprint-validation.yml` both complete successfully on a pull request. |
| AC-04 | FR-06 | The OpenCode skills are discoverable from `.opencode/skills/<name>/SKILL.md` without further configuration. | A directory tree of `.opencode/skills` shows one `SKILL.md` per skill. |
| AC-05 | FR-04, FR-12 | A change is done when every applicable Definition of Done criterion is satisfied, and each quality gate names an artifact, a criterion and evidence. | `standards/definition-of-done.md` reviewed against the change, and `validate-blueprint.sh` passing. |
| AC-06 | FR-04, FR-11 | A change that introduces a design pattern states the problem it solves, the evidence, the cost of the simpler alternative and the scope; a change that introduces none records that instead of leaving the question open. | `templates/change.md` design section, reviewed in the change record. |
| AC-07 | FR-10, FR-11 | Every quality attribute scenario carries all six fields, and every response measure is either verified by a named check or declared unverified. | Scenario coverage output of phase `06`, and review against the scenario in phase `03`. |
| AC-08 | FR-14, FR-16 | An installer run against a consumer produces the declared files, both licences, and a manifest recording exactly those entries. | `bash scripts/test-installers.sh` and `pwsh scripts/test-installers.ps1` pass. |
| AC-09 | FR-19, NFR-08 | Given a named requirement, a reviewer can name the release that delivered it and the check that verified it, using only the release record and the criterion coverage. | Reading `blueprint/11-continuous-delivery.md` and `blueprint/06-testing.md` against the change under review. |
| AC-10 | NFR-10 | A limitation this repository declares about itself is either true in the artifact that states it, or removed from it. | `validate-blueprint.sh` checks each declared limitation against the text that states it. |
| AC-11 | FR-20 | On a checkout with `core.autocrlf=true`, the gate reaches the same verdict it reaches with LF, and a CR in a tracked text file is reported as a line-ending fault naming that file. | `bash scripts/validate-blueprint.sh` on a CRLF checkout; the mutation suite converts a phase document to CRLF and asserts the gate fails on that file. |
| AC-12 | FR-21, FR-22 | No CI job can run unbounded, a superseded run is cancelled, and the reusable workflow's documented example pins a reference that carries the checks it describes. | `validate-blueprint.sh` fails if a job loses `timeout-minutes`, if either workflow loses `concurrency`, or if the example reintroduces `@v1.0.0`. |
| AC-13 | FR-23, FR-24, FR-25 | The language of each class of document is declared, every prose reference to an ADR, a phase document or a standard resolves, and the accepted commit types live in `standards/git.md` alone. | `validate-blueprint.sh` fails if the language declaration in `CONTRIBUTING.md` is removed, if a backticked document reference does not resolve, or if the git standard loses its quality gate or type list. The mutation suite introduces each of those three faults and asserts the gate fails. |

## Traceability
This register is the root of the chain, and each row names where its requirement goes. The
mechanism and its rules are in `standards/information-items.md`; the decision that produced them
is `blueprint/architecture/adr/adr-013-trazabilidad-de-extremo-a-extremo.md`.

- **Realised by** — the design element, implementation and test that satisfy the requirement.
- **Verified by** — the check that decides the acceptance criterion.
- **Delivered in** — the release whose record names it, per `## Delivery` below.
- **Verified how** — the `Verification` column of the table the entry appears in.

A requirement with no entry in `## Delivery` is declared and not shipped, and says so there.

## Delivery

This repository delivers by release, and `VERSION` names the state in progress. The chain from a
requirement to a release is recorded per entry in the tables above; a requirement with no
delivering release is declared here rather than allowed to lapse.

| Released in | Requirements delivered |
| --- | --- |
| `v1.0.0` | FR-01 … FR-08, NFR-01 … NFR-07 |
| `v1.1.0` | FR-09 … FR-25, NFR-08 … NFR-10 |

Every requirement in this register is delivered by a release. A requirement added after this
table was last updated is in development until the release that carries it names it here; it is
not left to lapse.

The working tree declares `1.2.0-dev`, which is not a release and is deliberately absent from
the table above. It is the version this tree will produce as `1.2.0` once the changelog section
for it is published, and that substitution happens as part of the release, not before it.

## Dependencies
- Git and GitHub (or an equivalent platform) for branching, pull requests and CI execution; the
  workflows are examples, not hard requirements.
- A Markdown-capable editor or CI tool for producing and validating the documentation artifacts.
- **Bash, not a POSIX shell, for this repository's own gate.** It uses `seq -w`, `compgen` and
  process substitution, none of which POSIX requires; the shebang says `bash` and the claim used
  to say otherwise. The reusable workflow carries its own smaller structural floor and declares
  `shell: bash` for the same reason.
- OpenCode, only when the optional skills adapter is used (a runtime dependency of the skills,
  not of the blueprint itself).
- No build toolchain, package manager or runtime is required to use the blueprint process.
- `actions/checkout`, pinned to the `v7` major tag, is the gate's only third-party runtime input
  and the one input that is not pinned exactly. See `## Not provided`.

## Risks / assumptions
- **Risk:** The blueprint becomes documentation-heavy without measurable adoption. **Mitigation:**
  the adoption checklist, and an information item per phase, so a consumer can see what it owes.
- **Risk:** Requirements and architecture are recorded retroactively and drift from the
  repository. **Mitigation:** the register names the artifact that realises each requirement, so a
  drift is visible as a row pointing at something that no longer says what it said.
- **Risk:** Skill scope creep blurs the boundaries between phases. **Mitigation:** each skill
  routes to the phase document rather than restating it, and a skill that disagrees with a phase
  document is a bug in the skill.
- **Risk:** A living register rots faster than a snapshot if the maintenance rule is not
  enforced. **Mitigation:** the Definition of Done requires a change that alters what the
  repository is held to to update this register, in the same change.
- **Assumption:** Consumers accept plain Markdown as the artifact format and manage it with
  version control.
- **Assumption:** Consumers have a CI platform, or accept running the gate locally.
- **Assumption:** The enforcement added by these requirements is proportionate. Where a check
  costs more to maintain than the defect it catches, the check is the thing that is wrong.

## Maintenance

This register changes in the same change that alters what the repository is held to, and never
alone in a housekeeping commit. A change that adds, removes or revises an entry states which of
FR-01 … FR-25 it is answerable to, because a requirement that exists for no reason is not a
requirement.

An identifier is never reused. A requirement that changes meaning keeps its identifier and gains
a new version, so a release that delivered the old meaning stays interpretable. The detail of
what an entry once said belongs to `git log` and `CHANGELOG.md`; this file states what is
required and what realises it, which is what a reader of the current tree needs.
