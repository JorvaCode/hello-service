# 11 — Continuous Delivery

Continuous Delivery takes a validated artifact through controlled release and deployment stages.

## Typical flow
CI
  -> artifact
  -> registry
  -> environment validation
  -> deployment
  -> smoke tests
  -> promotion

## Environments
Common examples:
- Development
- Test
- Staging
- Production

The exact environments depend on the project.

## Information items
### Inputs
- **Versioned artifact** — the output of `10-packaging.md`, with its identity.
- **Environment inventory** — the environments, their configuration source, and their owners.
- **Promotion criteria** — the checks and approvals each environment requires, declared rather than assumed.
- **Delivered scope** — the `FR-nn`, `NFR-nn` and change identifiers the artifact carries, from `01-requirements.md` and the change records. This is the endpoint of the chain that starts in requirements and a release record without it can describe what exists but not what was asked for.

### Outputs
- **Release record** — the artifact version, the identity from packaging, the environments it reached, the timestamp of each promotion, and the requirements and changes it delivers. A release that delivers no requirement, a base release, says so.
- **Promotion result** — the automated checks and the approvals that allowed each promotion, recorded per environment.
- **Rollback plan** — how to return to the previous version, and the evidence that it was rehearsed rather than only written down.

## Quality gate
Each promotion that happened is recorded with the checks and approvals that allowed it, and the rollback plan names a target version that was verified to work. The release record names the requirements and changes it delivers, or states that it delivers none, and each named criterion has a check in the criterion coverage of `06-testing.md`. A reviewer who did not take part in the release reaches the same verdict on what shipped and on whether it can be withdrawn, and can answer which requirement the release satisfies without consulting its author.
