# Terms and Definitions

The blueprint uses a small vocabulary of its own and inherits the rest from published standards. A term used as if it were defined is not defined, and the quality gates in `blueprint/` depend on these definitions being unambiguous.

**Source** records where the definition comes from. `ISO/IEC/IEEE 15289:2019` is cited for document types, `ISO/IEC/IEEE 29148:2018` for requirement elements, and `RFC 2119` / `RFC 8174` for modality; see [standards/normative-language.md](normative-language.md). `local` marks a term this repository defines for itself, because no external standard supplies one that fits.

## Vocabulary

| Term | Definition | Source |
| --- | --- | --- |
| **Normative** | Text that states a requirement. A reader must comply, or record a reason for not complying. | RFC 2119 |
| **Non-normative** | Text that informs without obliging. Examples and adapter content are non-normative. | RFC 2119 |
| **Artifact** | A document, diagram, configuration or measurement that a phase produces and that a later phase or a reviewer consumes. An artifact is identified by its content, not by its file name. | local |
| **Information item** | An artifact that is required by, or expected of, a process, together with the content it must contain. "Output" and "deliverable" are used as synonyms in this repository; the standard term is information item. | ISO/IEC/IEEE 15289:2019 |
| **Phase** | One of the 14 numbered stages in `blueprint/`. A phase defines activities, the information items it produces, and a quality gate. | local |
| **Activity** | Work performed within a phase. An activity has no gate of its own; only the phase does. | local |
| **Quality gate** | The check that ends a phase. It names the artifact inspected, the criterion, and the evidence a third party uses to reach the same verdict. | local |
| **Decision gate** | A quality gate placed where a decision is required to continue, as opposed to at the end of a step. A decision gate may halt work; a quality gate closes a phase. | ISO/IEC/IEEE 24748-2:2024 |
| **Falsifiable** | A property of a requirement or a gate: two independent reviewers looking at the stated evidence reach the same verdict. A requirement that cannot fail is not a requirement. | local |
| **Acceptance criterion** | The observable condition that decides whether a requirement is satisfied, written so that it can be tested. A criterion without a method of verification is a wish. | ISO/IEC/IEEE 29148:2018 |
| **Non-functional requirement** | A requirement about a quality attribute of the product rather than its behaviour, stated in a way that can be measured. | local |
| **Traceability** | The recorded relationship between a requirement, the decision that satisfies it, the artifact that implements it, and the check that verifies it. All four ends, or the relationship is not traceable. | local |
| **Architecture decision record** | A dated document that records a significant decision, the options considered, the decision, and its consequences including the negative ones. | local, after Michael Nygard, *Documenting Architecture Decisions* (2011) |
| **Blueprint** | The normative content of this repository: `blueprint/`, `standards/` and `templates/`. | local |
| **Adapter** | An optional integration that presents the blueprint to a tool. `.opencode/` is an adapter. It is not a dependency and never becomes a source of truth. | local, see ADR-002 and ADR-005 |
| **Router** | An adapter component that points at a normative document instead of restating it. Every skill in `.opencode/skills/` is a router. | local, see ADR-005 |
| **Adopting project** | A repository that installed the blueprint with one of the installers. Also called a consumer. | local |
| **Source of truth** | The single document that a given rule is read from. For lifecycle rules it is always a document in `blueprint/`, never a skill, an agent or a README. | local |

## Architecture description vocabulary

`blueprint/03-architecture.md` describes structure, and it borrows this vocabulary from **ISO/IEC/IEEE 42010:2022** (*Software, systems and enterprise — Architecture description*). These terms are used in a narrow sense on purpose. 42010 describes them in much greater generality, and a blueprint that adopts the full generality would produce an architecture framework instead of a gate.

| Term | Definition | Source |
| --- | --- | --- |
| **Stakeholder** | An individual, team or organization with an interest in, or affected by, the system. A stakeholder is identified by name or role, never by "the business". | ISO/IEC/IEEE 42010:2022 |
| **Concern** | A quality attribute that a stakeholder wants the system to exhibit, such as availability, latency, security or maintainability. A concern belongs to a stakeholder; an unowned concern has no one to resolve it for. | ISO/IEC/IEEE 42010:2022 |
| **Viewpoint** | The concerns of one stakeholder, or of a set of stakeholders treated as having compatible concerns. | ISO/IEC/IEEE 42010:2022 |
| **View** | A representation of a system, or of part of it, from the standpoint of one viewpoint. A view is partial by construction: it exists to serve the concerns of its viewpoint, so "the complete view" is not a goal. | ISO/IEC/IEEE 42010:2022 |
| **View type** | A view that is concerned with a system aspect other than its behavior, used when behavior is decomposed over several views. | ISO/IEC/IEEE 42010:2022 |
| **Model** | The formalized description of the subject system as it appears in a view, comprising the views' content together with their correspondence and rationale. | ISO/IEC/IEEE 42010:2022 |
| **Correspondence** | The relation between two elements of a model: how the content of one view maps onto another. A set of views with no stated correspondence cannot be checked for consistency. | ISO/IEC/IEEE 42010:2022 |
| **Rationale** | The reason a correspondence holds, or the reason a modeling decision was made. Rationale is retained alongside the model, because a correspondence without a reason is an assertion. | ISO/IEC/IEEE 42010:2022 |
| **Quality attribute scenario** | A six-part description of a quality concern: source, stimulus, environment, artifact, response and response measure. Named "scenario" rather than "use case", because a use case describes what the system does while this describes how it is judged. | ISO/IEC/IEEE 42010:2022 |
| **Sensitivity** | How much a quality attribute response changes when a condition of the environment varies. Recorded when it is not obvious, because a design that is fast at the load nobody runs is not a fast design. | ISO/IEC/IEEE 42010:2022 |

## Significant decision

A decision is recorded as an ADR rather than in the change when **at least one** of the following holds:

- **(a) It is expensive to reverse.** A public interface, a persisted data model, an external protocol, a published contract.
- **(b) It constrains two or more lifecycle phases.** A change that leaves a later phase no route it would otherwise have taken.
- **(c) It introduces or removes a dependency, a platform or a standard.**
- **(d) It changes a quality attribute the system is judged on.** Performance, security, availability, cost.
- **(e) It affects consumers outside the team.**

Anything else is not significant: a local implementation choice, a naming preference, a library no other phase depends on. Such a decision is recorded in the change, not in an ADR. Recording it as an ADR is not a defect, but it is not required — and requiring one for every choice is what turns the practice into paperwork.

## Why these definitions matter

The gate in `blueprint/03-architecture.md` required that "important decisions are documented". Before **significant decision** was defined, "important" had no threshold, so two reviewers could disagree about whether an ADR was required and both could be right. Defining the threshold is what makes the gate falsifiable, and the gate is only falsifiable if the terms are.

## Extending this file

Add a term here when a phase document or a standard relies on a word that is not defined anywhere. Do not redefine a term that already has a source; if this repository's usage genuinely diverges from the standard, record the divergence explicitly rather than silently.
