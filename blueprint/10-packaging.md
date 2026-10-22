# 10 — Packaging and Containerization

Create a deployable artifact.

## Possible artifacts
- Application package
- Container image
- Helm chart
- Deployment bundle
- Other platform-specific artifact

## Requirements
- Versioned artifact
- Reproducible build where practical
- Metadata/provenance where practical
- No secrets embedded in artifacts

## Information items
### Inputs
- **A verified build** — the output of a CI run that passed its required checks, from `09-continuous-integration.md`.
- **Version and metadata** — the version to stamp, and the metadata the project publishes.
- **Secret material** — the project's secrets, which are inputs to the build and never to the artifact.

### Outputs
- **Versioned artifact** — carrying a version that identifies exactly this build.
- **Artifact identity** — the digest or checksum that distinguishes this artifact from every other, and that a consumer can verify after transfer.
- **Provenance and metadata** — where the project can produce them, recorded with the artifact. Absent otherwise, which is a known gap rather than an unstated one.
- **Secret scan result** — the evidence that no secret is embedded, produced before publication.

## Quality gate
The artifact carries a version and an identity that a consumer can verify independently of the source workspace, and the secret scan result for this build is recorded. A consumer who received only the artifact can determine which version it is and whether it matches what the pipeline produced.
