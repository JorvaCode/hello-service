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

## Deployment record — local 0.1.0

The local deployment of `0.1.0` is recorded in
`blueprint/deployment/local-0.1.0.md`. It records the environment, strategy,
configuration, post-deployment verification and the exercised rollback to
`0.0.1-SNAPSHOT`.

The post-deployment verification passed and the rollback path was exercised. The
deployment gate is closed with the solo-maintainer self-review recorded in
`blueprint/reviews/delivery-closure.md`; it does not claim an independent approval. This
record covers the `local` environment.

## Remote deployment records — 0.1.0

On 2026-11-01, two `cd.yml` runs published `0.1.0` and deployed it by digest to a Podman
container on the GitHub Actions runner:

| Deployment | Source commit | CI run | CD run | Digest |
| --- | --- | --- | --- | --- |
| First | `c7d3a86b04e7b39a8ccfa55c37c8ae2c3dc4c2c0` | 41332436321 (`success`) | 41332436297 (`success`) | `sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b` (superseded) |
| Second (current) | `21772ccb0012c36e2264ede3171758ddfd773525` | 41334455181 (`success`) | 41334455398 (`success`) | `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17` |

Both post-deployment smoke tests `GET /api/hello` returned `200`,
`text/plain;charset=UTF-8`, `Hello, World!`. The tag `0.1.0` is mutable and currently
points to the second digest. Automatic push publication is now disabled; publication
requires `workflow_dispatch`.

At the time of those 2026-11-01 publications, the promotion mechanism was prepared for the
same digest but no second environment was configured, so end-to-end promotion was not
executed and no remote rollback was exercised. That limitation was closed on 2026-11-06;
see the next section. The local deployment and rollback record remains
`blueprint/deployment/local-0.1.0.md`. The remote deployments are recorded in
`blueprint/changes/p11-remote-publication.md` and reviewed in
`blueprint/reviews/remote-publication.md`.

## Remote staging deployment and rollback - 2026-11-06

On 2026-11-06, `cd.yml` was dispatched from `main` at `6d5fa89` to the GitHub `staging`
environment in digest mode:

| Event | Run | Digest | Result |
| --- | --- | --- | --- |
| Staging deployment | 42241159036 | `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17` | `success`; `GET /api/hello` -> `200`, `text/plain;charset=UTF-8`, `Hello, World!` |
| Unavailable rollback target | 42241318818 | `sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b` | `failure`; `manifest unknown` before deployment |
| Remote rollback | 42241543717 | `sha256:81ca2f236241fe443f4d1633b254dcc2b90e32fd3bc07b35e5969be3e7e9b833` (`0.0.1-SNAPSHOT`) | `success`; `GET /api/hello` -> `200`, `text/plain;charset=UTF-8`, `Hello, World!` |

The staging deployment and rollback used the same digest-derived image path, with no
`podman build` or tag overwrite. The environment is left on the `0.0.1-SNAPSHOT` digest
after the rollback. The superseded `0.1.0` digest was listed by Quay but was not pullable;
the failure is recorded as F-01 in `blueprint/testing/staging-rollback.md`.

The deployment record is `blueprint/deployment/remote-staging-0.1.0.md`; the change is
`blueprint/changes/p11-staging-rollback.md`; the review is
`blueprint/reviews/staging-rollback.md`. The `staging` environment is a GitHub environment
restricted to `main`; the container still runs on the GitHub Actions runner, not an
external host. The review is a solo-maintainer self-review and does not claim independent
approval.
