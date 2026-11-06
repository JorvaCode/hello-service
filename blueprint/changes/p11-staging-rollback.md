# Change: `CHG-DEL-005` - Staging target and digest rollback path

## Requirement

Close the remaining P-11/P-12 gap: promotion to a second environment and remote rollback
were not executable. This change adds a selected GitHub environment target
(`staging` or `production`) and an existing-digest deployment path to `cd.yml`.

## Design

`cd.yml` keeps one job and two explicit modes:

| Input | Behaviour |
| --- | --- |
| `target_environment` | Selects the GitHub environment, `staging` (default) or `production`. Both environments are restricted to `main`. |
| `image_digest` empty | Publish-and-deploy: validate the commit, build, publish the version tag, enforce tag immutability, then deploy the returned digest. |
| `image_digest` set | Digest deployment: validate `sha256:<64 hex>`, skip build/publication and deploy that existing digest. This is the promotion/rollback path. |

The deployment always runs the same digest-derived image reference,
`quay.io/<namespace>/hello-service@<digest>`, removes the previous CD container, starts the
replacement on port `8080`, and runs the existing function smoke test. No `podman build` or
tag overwrite occurs in digest mode. The requested digest is written to the `image-digest`
artifact for traceability.

## Safety and limitations

- The registry repository is public, so digest deployment needs no registry write credential.
  Publish mode still requires the existing Quay OIDC/robot prerequisites.
- The selected GitHub environment is `staging` or `production`; the deployment itself still
  runs on the GitHub Actions runner, not an external host. This is the existing remote PoC
  boundary and is not represented as a separate server fleet.
- Rollback is a container replacement by digest. There is no persistent state or schema
  migration in `hello-service`.
- The current published digest is
  `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17`. The earlier
  `0.1.0` digest `sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b`
  is still listed by the tag API but was not pullable on 2026-11-06; the exercised remote
  rollback target is the pullable `0.0.1-SNAPSHOT` digest
  `sha256:81ca2f236241fe443f4d1633b254dcc2b90e32fd3bc07b35e5969be3e7e9b833`.

## Execution evidence

The workflow was merged to `main` as `6d5fa89` and dispatched from `main` against the
protected `staging` environment.

| Step | Run | Digest | Result |
| --- | --- | --- | --- |
| Staging deployment | [42241159036](https://github.com/JorvaCode/hello-service/actions/runs/42241159036) | `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17` | `success`; build/publication skipped; `SMOKE_OK target=staging code=200 content-type=text/plain;charset=UTF-8 body=Hello, World!` |
| Rollback attempt | [42241318818](https://github.com/JorvaCode/hello-service/actions/runs/42241318818) | `sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b` | `failure`; the digest returned by the Quay tag API was no longer pullable (`manifest unknown`). |
| Remote rollback | [42241543717](https://github.com/JorvaCode/hello-service/actions/runs/42241543717) | `sha256:81ca2f236241fe443f4d1633b254dcc2b90e32fd3bc07b35e5969be3e7e9b833` (`0.0.1-SNAPSHOT`) | `success`; build/publication skipped; `SMOKE_OK target=staging code=200 content-type=text/plain;charset=UTF-8 body=Hello, World!` |

All three runs used commit `6d5fa89a3a4de7ae911e50054ab1be2a234e1b02` on `main`. The
failed attempt is retained as a finding: a digest listed by the tag API must be verified
pullable before being used as a rollback target. Details are in
`blueprint/testing/staging-rollback.md`; the deployment record is
`blueprint/deployment/remote-staging-0.1.0.md`.

## Rollback of this change

Revert the `cd.yml` commit. The existing production publication path and the recorded
`0.1.0` digests remain unchanged.
