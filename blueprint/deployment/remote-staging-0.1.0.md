# Remote staging deployment and rollback - `0.1.0`

## Deployment identity

| Field | Value |
| --- | --- |
| Environment | `staging` (GitHub environment restricted to `main`) |
| Source commit | `6d5fa89a3a4de7ae911e50054ab1be2a234e1b02` |
| Strategy | Replace the `hello-service-cd` Podman container with an image pulled by digest; no rebuild |
| Initial deployed digest | `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17` |
| Initial image | `quay.io/odysseia/hello-service@sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17` |
| Initial deployment run | [42241159036](https://github.com/JorvaCode/hello-service/actions/runs/42241159036), `success` |
| Deployment timestamp | 2026-11-06T15:14:19Z to 2026-11-06T15:14:50Z |
| Approval | Solo-maintainer `workflow_dispatch`; no independent approval |

## Post-deployment verification

- Podman started the digest-derived image in the `staging` environment.
- `GET /api/hello` returned `200`, `text/plain;charset=UTF-8`, `Hello, World!`.
- The build, version resolution, registry login, tag guard and publish steps were skipped;
  the deployed digest is the immutable identity.
- Evidence log: `SMOKE_OK target=staging code=200 content-type=text/plain;charset=UTF-8
  body=Hello, World!`

## Remote rollback

The first rollback attempt targeted the superseded `0.1.0` digest
`sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b`, which the Quay
tag API still listed. Run
[42241318818](https://github.com/JorvaCode/hello-service/actions/runs/42241318818) failed
before deployment because the registry returned `manifest unknown`. This is recorded as
F-01 in `blueprint/testing/staging-rollback.md`.

The exercised rollback target is the pullable newest `0.0.1-SNAPSHOT` digest:

| Field | Value |
| --- | --- |
| Rollback target | `sha256:81ca2f236241fe443f4d1633b254dcc2b90e32fd3bc07b35e5969be3e7e9b833` |
| Tag association | `0.0.1-SNAPSHOT` |
| Rollback image | `quay.io/odysseia/hello-service@sha256:81ca2f236241fe443f4d1633b254dcc2b90e32fd3bc07b35e5969be3e7e9b833` |
| Rollback run | [42241543717](https://github.com/JorvaCode/hello-service/actions/runs/42241543717), `success` |
| Rollback timestamp | 2026-11-06T15:16:45Z to 2026-11-06T15:17:23Z |
| Rollback verification | `GET /api/hello` -> `200`, `text/plain;charset=UTF-8`, `Hello, World!` |
| Resulting environment state | `staging` is left on the `0.0.1-SNAPSHOT` digest after the exercised rollback |

Rollback is a container replacement; `hello-service` has no persistent state or schema
migration to reverse.

## Boundary

The GitHub `staging` environment is the selected deployment target, but the container runs
on the GitHub Actions runner. No external staging host, orchestrator or load balancer is
part of this PoC. The record does not claim a production deployment or independent
approval.

## Records

- Change: `blueprint/changes/p11-staging-rollback.md`.
- Test evidence: `blueprint/testing/staging-rollback.md`.
- Continuous delivery: `blueprint/11-continuous-delivery.md`.
- Review: `blueprint/reviews/staging-rollback.md`.
