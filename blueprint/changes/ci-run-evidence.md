# Change: `CHG-CI-002` — Record the first successful CI run

## Requirement

Produces execution evidence for the CI configuration changed by `CHG-CI-001`. It does not
satisfy a functional requirement directly.

## Design

Documentation only. The change alters no structure, interface, data or dependency.

- `blueprint/11-continuous-delivery.md`: workflow execution status now distinguishes the
  verified CI run from the unverified CD path.
- `blueprint/improvements/continuous-improvements.md`: the closure table records the run.
- `README.md`: CI evidence row and known limitations updated.
- `blueprint/gates.md`: P-10 notes the run while remaining `NOT VERIFIED`.

### Patterns

No pattern is applied; the change is prose.

### ADR

Not significant. No architecture decision is introduced or changed.

## Implementation

- `blueprint/11-continuous-delivery.md`: CI run recorded under workflow execution status.
- `blueprint/improvements/continuous-improvements.md`: real CI execution row.
- `README.md`: CI row moves from configuration-only to verified execution.
- `blueprint/gates.md`: P-10 evidence updated.

## Tests

- GitHub Actions run 10, event `pull_request`, head
  `6ccd170c36dfa65bb1262ada10a74e57121ff4e3`.
- Conclusion `success`; every step passed, including `./gradlew clean build`,
  `podman build`, identity record and artifact upload.
- Artifact: `artifact-metadata` containing the commit SHA and image ID.
- Run URL: `https://github.com/JorvaCode/hello-service/actions/runs/41043812371`.
- The artifact contents and logs were not retrieved; the run status and step conclusions
  come from the GitHub Actions API.

| Criterion | Level | Check that verifies it |
| --- | --- | --- |
| Simplified CI executes end to end | CI | Run 10, all steps `success` |
| Artifact identity is produced | CI | `artifact-metadata` uploaded by the run |

## Security

None. The workflow keeps `permissions: contents: read`; no secret or publication path is
added.

## Deployment

None. No publication, promotion or remote digest is claimed.

## Rollback

Revert the commit; the previous configuration-only wording is restored.

## Deletions

None.
