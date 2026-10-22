# 12 — Deployment

Deployment shall be repeatable and observable.

## Consider
- Configuration
- Secrets
- Database migrations
- Rollback strategy
- Health checks
- Readiness/liveness
- Deployment strategy
- Environment-specific settings

## Possible platforms
- VM
- Container platform
- Kubernetes
- OpenShift
- Cloud services
- On-premises infrastructure

## Information items
### Inputs
- **Release record** — the artifact version and the environments it reached, from `11-continuous-delivery.md`.
- **Environment configuration** — the target's configuration and secrets, from wherever the project keeps them, never from the source workspace.
- **Migration plan** — the database or schema change this deployment carries, and whether it is reversible.

### Outputs
- **Deployment record** — what was deployed, to which environment, when, with which strategy.
- **Migration record** — the schema or configuration change applied, whether it is reversible, and the verification that ran after it.
- **Rollback evidence** — either a rehearsed rollback, or an explicit statement that this change cannot be rolled back, with the reason and the recovery path. An unrevertible migration discovered during an incident is a design defect, not a runtime surprise.
- **Post-deployment verification** — the health check, readiness and smoke test results after the deployment.

## Quality gate
The post-deployment verification result for this deployment is recorded and passed, and the rollback path is either exercised or explicitly declared unavailable with a recovery path. A reviewer who did not perform the deployment reaches the same verdict on whether the target is in the intended state.
