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

## Gate
Promotion requires the defined automated checks and, where required, an explicit approval.

---

# Report: REQ-HELLO-001 — Continuous Delivery (Phase 11)

Project adapter for `hello-service` (Spring Boot 3.5.3 / Java 21 / Gradle). Implements ADR-009 (platform: OCI + Podman) and ADR-010 (registry: Quay.io, digest as immutable identity, no rebuild on promotion). The Blueprint remains registry-agnostic (ADR-001, REQ-BP-001): Quay.io is a decision of this reference project only.

## Objective

The problem Continuous Delivery solves: the validated artifact produced by CI (Phase 09) and packaged in Phase 10 is only useful once it can be released repeatably and observably — published to a registry, deployed by an immutable identity, verified with a real smoke test and promoted between environments without being rebuilt. Phase 11 automates that flow for `hello-service` and records what still requires external configuration.

## Preconditions

| Precondition | State |
|---|---|
| Phase 09 — CI | PASS (`.github/workflows/ci.yml`) |
| Phase 10 — Packaging | PASS (reproducible JAR, `blueprint/10-packaging.md`) |
| Phase 12 — Deployment | PASS (OCI image + Podman, `blueprint/12-deployment.md`) |
| ADR-009 (OCI + Podman) | Accepted |
| ADR-010 (registry Quay.io) | Accepted |

## Pipeline

```text
Commit
  -> CI (compile + tests + build)          [Phase 09 gates, reused in cd.yml]
  -> resolve version (Gradle project.version)
  -> podman build (Phase 10 artifact, OCI, provenance labels)
  -> publish quay.io/<namespace>/hello-service:<version>     (version tag only; NO :latest)
  -> digest (immutable identity, from podman push --digestfile, validated ^sha256:[0-9a-f]{64}$)
  -> deploy by digest (Podman, SAME artifact)
  -> smoke GET /api/hello  (HTTP 200, text/plain, "Hello, World!")
  -> promotion mechanism prepared: quay.io/<namespace>/hello-service@sha256:<digest>
     (SAME digest, NO podman build; execution requires the target environment)
```

Implemented as `.github/workflows/cd.yml` (15 steps, triggered on push to `main` and manual `workflow_dispatch`).

## Registry

Quay.io (ADR-010). Image name format below. The pipeline reads the namespace from the GitHub repository variable `QUAY_NAMESPACE`; no registry default is hardcoded in the workflow.

## Authentication

Per ADR-010: **OIDC/keyless is the preferred mechanism and is fully prepared in `cd.yml`** — the job requests a GitHub OIDC token (`id-token: write`) and exchanges it, at runtime, for a short-lived Quay robot token via `quay.io/oauth2/federation/robot/token` using the federated robot in `vars.QUAY_ROBOT_USERNAME`. No credential is stored.

This exchange flow was verified against official documentation (Red Hat Quay / Project Quay "keyless authentication with robot accounts"; Red Hat Developer guide for GitHub Actions, 2026-07-22): robot name `quay-org+robot`, exchange by `GET {registry}/oauth2/federation/robot/token` with basic auth `robot:OIDC_token`, response `.token` valid for 1 hour, then `podman login quay.io -u <robot> --password-stdin`. Federation is configured in Quay at robot-account level by `POST /api/v1/organization/{org}/robots/{robot}/federation` (issuer `https://token.actions.githubusercontent.com` + subject `repo:<prefix>:ref:refs/heads/main`) — an external step listed under BLOCKED below.

The **documented alternative (robot account/token via GitHub Actions Secret)** is also implemented in the workflow: set `vars.QUAY_AUTH=robot` and provide `secrets.QUAY_USERNAME` / `secrets.QUAY_TOKEN`; the workflow logs in with those. Tokens are never written to the repository or printed (they are `::add-mask`-ed and passed via env).

## Image

`quay.io/<namespace>/hello-service:<version>` — `<version>` is the Gradle project version (`0.0.1-SNAPSHOT`), resolved reproducibly from `./gradlew properties`. The version tag identifies the release; the OCI digest is the immutable identity (ADR-010). **`latest` is intentionally NOT published**: the CD flow does not depend on it, and it is never used to identify the deployed or promoted artifact (deploy and promotion both use `@sha256:<digest>`). This removes the ambiguous moving tag from the release path.

## Immutable identity

The OCI **digest** is the immutable identity of the artifact (ADR-010). `cd.yml` captures it via `podman push --digestfile build/hello-service.digest` — Podman itself writes the digest of the pushed image to the file, so no assumption is made about the order of `RepoDigests`. The value is validated strictly (`^sha256:[0-9a-f]{64}$`), published as a job output and uploaded as `build/image-digest.txt` for traceability. `podman push --digestfile` was validated locally against a real (local) registry: it writes exactly `sha256:<64 hex>`.

## Promotion

**Promotion mechanism: IMPLEMENTED/PREPARED** — `cd.yml` contains an explicit promotion step whose only action is to emit, from the digest produced by the publish step, the reference to promote:

```text
quay.io/<namespace>/hello-service@sha256:<digest>
```

It uses exactly the same digest; **no image is rebuilt and no `podman build` is executed** by the promotion step (ADR-010: rebuilding during promotion is prohibited).

**Promotion execution: BLOCKED — external environment not configured.** No second, real environment (staging/production/named namespace) exists, so no end-to-end promotion is executed or simulated. The promotion step records the immutable reference and stops; running the promoted artifact is deferred until the target environment is provided externally.

## Deployment

Podman per ADR-009 / Phase 12: the workflow installs Podman and runs the image **by digest** from Quay. Deliberately, the workflow does **not** rely on the Podman runtime healthcheck (`--health-*`) on the GitHub Actions runner: its behavior there was never executed and is **not presented as validated**. Readiness and function are asserted by the HTTP smoke test. The Phase 12 healthcheck remains a locally validated capability of the Phase 12 deployment (`blueprint/12-deployment.md`), unchanged; ADR-009 is not modified.

## Smoke test

`GET /api/hello` on the deployed-by-digest container is the functional evidence of Phase 11: it must return **HTTP 200**, **`Content-Type: text/plain`** and **exactly `Hello, World!`**. `cd.yml` polls readiness with a bounded curl loop, then asserts all three (code, content-type prefix `text/plain`, exact body); any mismatch fails the pipeline.

## Rollback

Rollback restores a previous release by its previous version tag/digest:

```text
current:  quay.io/<namespace>/hello-service@sha256:AAA
rollback: quay.io/<namespace>/hello-service@sha256:BBB
```

The actual `AAA`/`BBB` are the digests recorded at each publish (job output + `image-digest.txt` artifact). Local reference for the current validated image in this workspace: `localhost/hello-service@sha256:215e7e8ceb1ce02e3e2dc5b0becc565268178815476467376185ffb09224a301`. No hashes are invented; digests come from the real registry/push output. No DB migrations apply.

## Security

- Container runs as non-root (`USER appuser`, uid 10001) — verified in Phase 12 report and re-validated here.
- No secrets in the repository: `cd.yml` references credentials only via `${{ secrets.* }}` / runtime OIDC tokens; nothing is hardcoded (grep verification in Phase 11).
- Tokens are masked (`::add-mask`) and passed via environment variables; never printed, committed, or placed in `.env`, YAML or the Dockerfile.
- `permissions: contents: read, id-token: write` (least privilege; no `packages` scope).
- Quay robot OIDC federation is scoped by subject (repo/branch/environment) in the Quay configuration (external step).

## Reproducibility

- Phase 10: JAR reproducible (identical SHA-256 across builds).
- Phase 12: image reproducible from the pinned base digest + JAR.
- Phase 11 re-validated: two consecutive `podman build` runs (no provenance args) produced the **identical** image ID `e6fb093b9db0…`/digest `sha256:215e7e8c…`.
- Provenance labels (`org.opencontainers.image.source`, `.revision`) are injected at build time from the real CI context (`github.repository` / `github.sha`); with no remote (local build) they are empty — no fabricated values.

## External prerequisites

### IMPLEMENTED (works now)
- `cd.yml` workflow (validate, build, publish-by-config, digest via `--digestfile`, deploy-by-digest, smoke, promotion mechanism prepared — same digest, no `podman build`).
- Dockerfile provenance labels; OCI image reproducible, non-root, Java 21.
- Local deployment by digest + smoke test (validated: HTTP 200, `text/plain`, `Hello, World!`).
- `latest` removed from the release path (version tag + digest only).

### VALIDATED (local)
- `cd.yml` YAML valid (15 steps); no secrets/credentials hardcoded; `id-token: write` present; OIDC preferred + robot-secret fallback (both implemented); deploy/promotion use `@sha256:<digest>` only; no `podman build` in the promotion mechanism; no incorrect ADR references.
- Quay OIDC exchange flow verified against official documentation (endpoint, robot naming, basic-auth exchange, 1h token validity, federation API/UI configuration).
- `podman push --digestfile` validated against a local real registry → writes `sha256:<64 hex>` (strict format OK).
- CI gates (`gradlew clean build` → SUCCESS); image non-root / Java 21 (jdk-21.0.12+8); provenance labels correct via build args; reproducibility (identical image ID across builds) — evidence in `10-packaging.md`/`12-deployment.md` and this report’s verification table.

### BLOCKED — external prerequisite (cannot be verified in this workspace)
1. **GitHub remote** — `hello-service` is not a Git repository and has no remote; `cd.yml` cannot yet run on GitHub Actions. Required: create/push the repository to GitHub (and install/authorize `gh` or use PAT), then set repository Variables/Secrets.
2. **Quay.io namespace + repository** — local auth exists (real robot `jorvacode+github_actions`, discovered from `$HOME/.config/containers/auth.json`, namespace `jorvacode`) but the repository `quay.io/jorvacode/hello-service` does NOT exist (pull → unauthorized). It must be created **as PUBLIC** in the Quay UI (a CLI push would auto-create a private repo, contradicting ADR-010) and the robot `github_actions` granted write access. No push was performed.
3. **Pipeline authentication** — either Quay robot-account federation (OIDC, preferred; the exchange code is ready and verified, but the federation entry `issuer` + `subject` must be configured in Quay for this repository — see Authentication), or the documented alternative GitHub Secrets `QUAY_USERNAME` / `QUAY_TOKEN`. Neither exists yet.
4. **GitHub repository variables** — `QUAY_NAMESPACE` (e.g. `jorvacode`), `QUAY_AUTH` (`oidc`|`robot`, default `oidc`), `QUAY_ROBOT_USERNAME` (e.g. `jorvacode+github_actions`) for the OIDC path.
5. **Second real environment (target) for promotion** — no staging/production/named-namespace environment exists. The promotion mechanism is implemented and prepared; **its end-to-end execution is `BLOCKED — external environment not configured`** and is not simulated.

## Verification (this phase)

| # | Check | Result | Evidence |
|---|---|---|---|
| V-1 | cd.yml YAML valid | VALIDATED | PyYAML parse OK, 15 steps (GitHub Actions `on` as string per YAML 1.2). |
| V-2 | No secrets/credentials in cd.yml / informe | VALIDATED | Grep: only `${{ secrets.* }}` / `${{ vars.* }}`; no tokens, passwords, private keys, JWTs. |
| V-3 | `id-token: write`, OIDC preferred, robot-secret fallback | VALIDATED | `permissions` present; auth step implements both paths per ADR-010. |
| V-4 | Quay OIDC exchange flow | VALIDATED | Verified against official docs: `GET {registry}/oauth2/federation/robot/token` with basic auth `robot:OIDC_token`, `.token` valid 1h; federation = `issuer` + `subject` (external). |
| V-5 | Digest obtained reliably after push | VALIDATED | `podman push --digestfile` → `sha256:<64 hex>` (real test on local registry); strict regex enforced in workflow; no `RepoDigests[0]` assumption. |
| V-6 | No `latest` in the pipeline | VALIDATED | `latest` tag/push removed; deploy & promotion use `@sha256:<digest>` only. |
| V-7 | Promotion mechanism prepared (no rebuild) | VALIDATED | Step emits `quay.io/<ns>/hello-service@sha256:<digest>` (same digest); no `podman build` in promotion. |
| V-8 | Promotion execution | BLOCKED — external environment not configured | No second real environment; not simulated. |
| V-9 | No Podman healthcheck dependency in GitHub Actions | VALIDATED | `--health-*` removed from workflow; HTTP smoke is the evidence; Phase 12 local validation untouched (ADR-009 unchanged). |
| V-10 | CI gates (local) | VALIDATED | `gradlew clean build` → BUILD SUCCESSFUL, exit 0. |
| V-11 | Image: non-root, Java 21, provenance, reproducible | VALIDATED | `appuser` (uid 10001), `jdk-21.0.12+8`, labels injected via args / empty without, identical build ID `e6fb093b9db0…` / digest `sha256:215e7e8c…`. |
| V-12 | Deploy by digest + smoke (local) | VALIDATED | `localhost/hello-service@sha256:215e7e8c…` → HTTP 200, `text/plain`, `Hello, World!`. |
| V-13 | ADR references correct | VALIDATED | No `ADR-012` / `ADR-009/012` anywhere; Deploy = ADR-009, Promotion = ADR-010. |
| V-14 | Publish to Quay.io | BLOCKED — external prerequisite | Repo `quay.io/jorvacode/hello-service` missing (pull → unauthorized); no push (would create private repo). |
| V-15 | Run cd.yml in GitHub Actions | BLOCKED — external prerequisite | No GitHub remote; OIDC federation / secrets / repository variables not configured. |

## Gate

**PHASE 11 — GATE: BLOCKED**

Reason: the end-to-end pipeline cannot execute without external configuration that is genuinely absent in this workspace — no GitHub remote, no public `hello-service` repository on Quay.io (local auth exists), no pipeline authentication (OIDC federation or GitHub Secrets) configured, and no second real environment to promote to. The publish, remote-digest, remote-deploy, remote smoke and promotion-execution steps are therefore `BLOCKED — external prerequisite` and are not simulated.

Everything implementable locally is implemented and validated (V-1…V-7, V-9…V-13): the CD workflow, the provenance-ready Dockerfile, image reproducibility/identity, deploy-by-digest and the HTTP smoke test, and a promotion mechanism prepared for the exact same digest — but not executed. Phase 11 unblocks as soon as the external prerequisites in the section above are provided.

## Date
2026-09-24