# Closure Gates Register — hello-service

## Purpose

This register defines the `P-nn` identifiers referenced by `.github/workflows/ci.yml`,
`blueprint/11-continuous-delivery.md`, `README.md` and
`blueprint/improvements/continuous-improvements.md`. Before this register, those identifiers
were referenced without a definition, so no third party could decide whether a gate was open,
closed or not verified.

Each row names the phase it gates, the artifact inspected, the criterion, the evidence
required to close it, the owner and the current state. The criterion is the quality gate of
the corresponding phase document; this register does not replace that document.

**A gate is `CLOSED` only when the required evidence exists for the commit under closure.
Configuration, a workflow file or an intention to run it is not evidence that the gate ran.**

## Gates

| Gate | Phase | Artifact | Criterion | Evidence required | Owner | State (2026-10-30) |
| --- | --- | --- | --- | --- | --- | --- |
| P-02 | 02 Analysis | `blueprint/analisys/hello-endpoint.md`, `blueprint/analisys/api-status-endpoint.md` | The analysis identifies decisions, alternatives, risks and traceability for the requirement under change, and records them before design. | An analysis artifact scoped to the change under closure, reviewed against `blueprint/02-analysis.md`. | Jorva (maintainer) | **NOT VERIFIED** — the existing analysis covers `REQ-HELLO-001` and `REQ-STATUS-001`; no analysis artifact exists for this documentation and CI closure change set. |
| P-06 | 06 Testing | `blueprint/testing/api-status-endpoint.md` | The test run for the commit under closure is green, and every acceptance criterion has a named check. | A recorded run naming the commit, command, environment, counts and result for the commit under closure. | Jorva (maintainer) | **CLOSED** — green run at `72c2ad51cb9d5d1805ec37ff94f297c11d7b14bd` recorded in `blueprint/testing/api-status-endpoint.md`; later closure commits are documentation-only. |
| P-07 | 07 Code Review | `blueprint/reviews/hello-endpoint.md`, `blueprint/reviews/api-status-endpoint.md`, `blueprint/reviews/delivery-closure.md` | Every required reviewer records a verdict, every automated check passes, and every finding has a disposition. | A review record for each change, naming the reviewer, the checklist answers, the findings and the verdict. | Jorva (maintainer) | **CLOSED (solo-maintainer self-review)** — `REQ-HELLO-001` has an approval; `REQ-STATUS-001` has a review record added by `CHG-REV-001`; the delivery closure change set has a self-review in `blueprint/reviews/delivery-closure.md`. The repository has no independent human reviewer, so this closure records a self-review and does not claim independent approval. |
| P-10 | 10 Packaging | `.github/workflows/ci.yml`, `Dockerfile`, `build.gradle.kts`, `blueprint/packaging/hello-service.md` | The artifact carries a version and an identity a consumer can verify independently, and the secret scan result for the build is recorded. | A versioned artifact, its identity, its provenance and a secret scan result. | Jorva (maintainer) | **CLOSED** — run 13 at `2e54476` succeeded with the secret scan, versioned image, identity record and artifacts `artifact-metadata`, `hello-service-0.1.0` and `gitleaks-report`. |
| P-11 | 11 Continuous Delivery | `blueprint/delivery/release-0.1.0.md`, `blueprint/deployment/local-0.1.0.md`, `blueprint/11-continuous-delivery.md`, `.github/workflows/ci.yml` | Each promotion that happened is recorded with the checks and approvals that allowed it, and the rollback plan names a version verified to work; the release record names the requirements it delivers. | A release record, a promotion result and rollback evidence for the artifact. | Jorva (maintainer) | **CLOSED (solo-maintainer self-approval)** — local promotion of `0.1.0` recorded with release identity, checks, promotion result and a rehearsed rollback to `0.0.1-SNAPSHOT`; deployment record in `blueprint/deployment/local-0.1.0.md`. Remote publication remains postponed and is not claimed. The closure records a self-approval and does not claim an independent approval. |

## How a gate closes

1. The evidence named in the row exists for the commit under closure.
2. A reviewer who did not produce the evidence reaches the same verdict from it. A
   single-maintainer repository with no independent reviewer records that fact and closes
   the gate with a self-review; the record must state that independent approval is
   unavailable.
3. The state changes to `CLOSED` in the same change that records the evidence.

A gate whose evidence does not exist is `NOT VERIFIED`, not `CLOSED`. Postponing the work a
gate covers does not close it.

## What this register does not do

- It does not close any gate, approve a promotion or assert that a workflow ran.
- It does not change the phase documents or their quality gates.
- It does not replace the review, test, packaging or delivery artifacts named in each row.
