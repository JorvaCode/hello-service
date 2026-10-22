# Definition of Done

A change is done when every criterion below is satisfied. Each one names the artifact inspected, the condition it shall meet, and the evidence that a party who did not write the change uses to reach the verdict. A criterion that names an opinion instead of an artifact is not satisfied by asserting the opinion; that is the same defect `standards/normative-language.md` describes, and a change does not escape it by writing it here.

## Specification

- [ ] Every requirement the change satisfies is named by its identifier, and the
      change names the design element and the test that carry it, so that
      `blueprint/00-principles.md` principle 7 has both ends.
- [ ] Every acceptance criterion states the observable condition and the method that
      verifies it, and names the test or check that runs that method.
- [ ] New or changed requirements appear in `blueprint/requirements/` when the change
      alters what the repository itself is held to, with a traceability entry to the
      decision or commit that introduced it.
- [ ] New terms are added to `standards/terms.md`, and existing terms are used as that
      document defines them rather than as the change finds them convenient.

## Design

- [ ] The change states, in `templates/change.md`, whether it alters structure,
      interface, data or dependencies. A change that alters none of them says so
      rather than leaving the field empty.
- [ ] An alteration that is significant by the criteria in `standards/terms.md` is
      recorded as an ADR in `blueprint/architecture/adr/`, in the format of
      `templates/adr.md`. Recording one that is not significant is permitted.
- [ ] Design quality is checked against `standards/code-design.md`: each pattern the
      change introduces names the problem behind it in `templates/change.md`, and a
      change that introduces none records that instead of leaving the question open.
- [ ] Normative text the change writes or edits uses deliberate modality, per
      `standards/normative-language.md`.
- [ ] Any quality gate the change introduces or edits names its artifact, its
      criterion and its evidence, per `standards/normative-language.md`.
- [ ] Any information item the change introduces or edits states the content it
      holds, per `standards/information-items.md`.

## Implementation

- [ ] The scope the change states is what the change does. No placeholder, `TODO` or
      stub remains in the paths the change names, and the build or the gate that
      consumes the change passes on the result.
- [ ] Every new or altered behaviour has a test that fails when the behaviour is
      reverted. A test that passes both ways evidences nothing.
- [ ] Tests that the change did not need are not deleted, and a deletion is recorded
      in `templates/change.md` with the reason.

## Verification

- [ ] The checks the change requires are named, and a run of them is recorded with
      its result. "Required checks" is not a set that a reader can enumerate.
- [ ] A review that the author did not perform on their own change records an
      approval, and that review applied `blueprint/07-code-review.md`.
- [ ] The pipeline run for the change is green, and the run is linked from the change.
- [ ] Security has been reviewed against the repository's own threat surface, per
      `SECURITY.md`: a change that adds a dependency, an endpoint, a secret or an
      input path names what it reviewed and what it found. A change that adds none
      records that.

## Delivery

- [ ] The artifact a consumer receives identifies itself: its version comes from
      `VERSION` or the release that carries it, and a change to that identity is a
      changelog entry, not a silent edit.
- [ ] The deployment and the path back from it are named. A change that needs no
      rollback records why, which is a reason and not an omission.
- [ ] The signal that reports the change behaving badly in production is named, and it
      exists, or the change records why none is required.
