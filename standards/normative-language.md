# Normative Language

This standard defines how normative text is written in the blueprint, so that a requirement can be checked by a third party instead of interpreted.

## Scope

Applies to every normative document in the blueprint: `blueprint/00-principles.md` … `blueprint/14-continuous-improvement.md`, `blueprint/requirements/`, `standards/`, and `templates/`. It does not apply to `examples/`, which is illustrative, or to `.opencode/`, which is an adapter and not a source of truth.

## Source

The vocabulary comes from **RFC 2119** (Bradner, 1997) and **RFC 8174** (Leiba, 2017), which update BCP 14. These are the keywords every Internet Protocol Working Group document is expected to use, and they are the most widely implemented way to write a checkable requirement.

Two facts from that source drive the rules below:

- The keywords carry normative force **only in a language's convention for emphasis**. RFC 2119 assumes all capitals: `MUST`, `SHOULD`, `MAY`.
- `MUST` and `SHALL` are **exact synonyms**. Choosing one over the other carries no meaning, so this blueprint uses `shall` throughout and never `must`.

RFC 2119 §4 also advises against using these keywords to impose a method rather than to state an interoperability requirement. That advice addresses **protocol** specifications, where the point is that independent implementations can interoperate. It does not transfer to a process blueprint, whose entire purpose is to mandate a process. This blueprint therefore uses `shall` freely to mandate process, and reserves its attention for a different question: whether the mandated thing is checkable.

## Deviation: lowercase

RFC 2119 requires all capitals for the keywords to be binding. This blueprint writes them in lowercase, following the ISO convention, and **this section is the declaration that gives them force inside this repository**. The consequence is concrete: `should` and `Should` are the same word, and only this document says they are normative rather than stylistic. A reader who does not know the convention cannot tell an obligation from a preference, which is exactly the defect this standard removes.

## Modality

| Keyword | Meaning | A deviation requires |
| --- | --- | --- |
| **shall** | An obligation. Non-compliance is a defect. | A recorded reason, in the change or the decision that carries it |
| **should** | A recommendation. The default answer. | A recorded reason for not following it |
| **may** | A genuine option. Choosing not to is not a deviation. | Nothing |
| **shall not** | A prohibition. | Nothing |

Two words are not modality and must not be used as if they were:

- **prefer** — an instruction with no defined strength. Where it appears today it is replaced by `should` or `may`.
- **need to**, **have to**, **try to**, **consider** — the same problem. `consider` names no outcome, so it cannot be falsified.

## Normative and non-normative text

| Location | Status |
| --- | --- |
| `blueprint/` | Normative. The lifecycle definition. |
| `blueprint/requirements/` | Normative. The requirements this repository is held to. |
| `standards/` | Normative. Engineering standards for every change. |
| `templates/` | Normative in structure, non-normative in content. A template's headings are required; its example text is not. |
| `examples/` | Non-normative. Illustrative only. An example never establishes a requirement. |
| `.opencode/` | Non-normative. An adapter. Where it conflicts with `blueprint/`, `blueprint/` wins and the skill is a bug. |
| `README.md` | Non-normative, except where it restates a normative rule. |
| `CHANGELOG.md` | Non-normative. A record, not a requirement. |

## Writing a quality gate that can be falsified

A quality gate is a check, not a sentiment. Every gate in `blueprint/` shall satisfy all three of:

1. **Artifact** — the thing inspected. A document, a pipeline result, a measurement.
2. **Criterion** — the observable condition it must meet, with a threshold where one applies.
3. **Evidence** — how a party who did not write the change determines the result.

Where any of the three is missing, the gate is an opinion. Rewriting it is a correction, not a new requirement.

**Before**, from `blueprint/03-architecture.md` as of 1.0.0:

> The architecture satisfies requirements and known constraints, and important decisions are documented.

Nothing is named. "Satisfies" has no criterion, "known" has no owner, and "important" has no definition — so the gate can be passed or failed by argument.

**After**:

> The architecture description in `blueprint/03-architecture.md` (or the adopting project's equivalent) lists the stakeholders and the concerns in scope, and every decision that satisfies a significance criterion in `standards/terms.md` is recorded as an ADR whose Status is `Accepted` or `Superseded`. A reviewer who is not a participant in the change reaches the same verdict from the ADRs alone.

Same intent, now checkable: the artifact is the description plus the ADR set, the criterion is the significance test, and the evidence is that a non-participant reproduces the verdict.

## What this standard does not do

It does not add requirements. Deciding *which* obligations a project carries is the job of `blueprint/requirements/` and the phase documents. This standard only fixes the strength and the checkability of the ones that already exist.
