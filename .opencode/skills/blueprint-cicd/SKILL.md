---
name: blueprint-cicd
description: Apply the Software Engineering Blueprint to continuous integration, packaging, delivery, deployment, observability and continuous improvement.
---

# Blueprint — Delivery Pipeline

Covers lifecycle phases 09 to 14. Read the phase documents; they are the source of truth.

All paths below are relative to the project root.

## Documents

- `blueprint/09-continuous-integration.md`
- `blueprint/10-packaging.md`
- `blueprint/11-continuous-delivery.md`
- `blueprint/12-deployment.md`
- `blueprint/13-observability.md`
- `blueprint/14-continuous-improvement.md`
- `.github/workflows/` — example pipeline and reusable validation workflow

## Standards

- `standards/definition-of-done.md` — the criteria a change is closed against.
- `standards/normative-language.md` — a pipeline check is a gate, and a gate needs an
  artifact, a criterion and evidence.
- `standards/information-items.md` — what the pipeline, packaging and deployment
  artifacts shall contain.

## Procedure

1. CI validates the change automatically: build, tests, quality, security, package. A
   change with failing gates is not integrable.
2. Produce a versioned, identifiable deployable artifact — package, container image, Helm
   chart or bundle. Never embed secrets in an artifact.
3. Promote the same artifact between environments. Rebuilding per environment breaks
   traceability.
4. Make deployment and rollback repeatable: configuration, secrets, database migrations,
   health checks, readiness, rollout strategy.
5. Instrument the result so failures are detectable and diagnosable: logs, metrics,
   traces, alerts, health endpoints.
6. Feed incidents, metrics, user feedback, retrospectives and technical debt back into
   future requirements and architecture.

## Output

A green pipeline, a versioned artifact, a repeatable and reversible deployment, and
observable behaviour. Anything the project has not automated is reported as a gap rather
than silently skipped.
