# Change: `CHG-DEL-003` - Staging target and digest rollback path

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
- The previous published digests remain available: current
  `sha256:0d8deffb7c8427c9f72be9496c6ad851e68b00fd147094d0df297db9e5c6ce17` and previous
  `sha256:4d06e515314b5efeb20d03133a88cfe9ad0ee8d7fd6a287a1dd84b886e6de31b`.

## Execution evidence

Pending: the workflow must be merged to `main` before the protected `staging` environment
can execute it. The staging deployment and rollback runs will be recorded in
`blueprint/testing/staging-rollback.md` and this change record after execution.

## Rollback of this change

Revert the `cd.yml` commit. The existing production publication path and the recorded
`0.1.0` digests remain unchanged.
