# Git Standard

Git is the record of how the work happened. A record nobody can check is not a record,
so each rule below names what it applies to and what decides it.

## Branches

A change is developed on a branch whose name states its purpose, and the branch is merged
or abandoned rather than left open. A branch that has not merged in the time its scope
implies it should have is not a slow branch, it is an abandoned one.

The name carries the type and, where one applies, the scope: `fix/ci-line-endings`,
`feat/traceability`. A name like `wip` or `final` states nothing, because it cannot be
distinguished from any other branch at a glance.

## Commits

A commit is one logical change: it builds, it is reviewed as a unit, and reverting it
reverts one thing. A commit that fixes a gate and renames a variable in the same change
cannot be reverted alone, so it is two commits.

The subject line uses Conventional Commits — `type(scope): subject`. The accepted types are
`feat` and `fix` from the specification itself, `docs`, `chore`, `refactor`, `test`, `ci` and
`build` from the `commitlint` conventional configuration, and `release` as a local extension
for a commit that prepares a version. The scope names the area when one applies. The subject
states what changed, in the imperative, and the body states why when the why is not in the
subject.

A type outside that list is not a formatting preference; it is a type a reader cannot
interpret, so the list is the one the log is checked against.

`fix` is for a defect that existed; `feat` is for behaviour that did not. A change that is
both is two commits. The distinction is the one a reader of the log needs in order to decide
whether a release contains a fix or a capability, and it is not recoverable afterwards.

## Pull requests

A change reaches the main branch through a pull request, unless the repository is a single
maintainer with no reviewer, in which case it records that fact rather than pretending to
have one. A pull request names the requirements it satisfies, by identifier, or states that
it satisfies none.

The branch's checks are green before the merge, and the merge is a merge: the history keeps
the branch, which is what makes a revert traceable to a pull request.

## Main branch

The main branch is releasable: its checks pass at every commit, and no commit reaches it
with a failing check or an undeclared artifact. A red main branch is repaired before the
next change lands on it, not after.

## Traceability

The requirement identifier appears in the pull request or the commit subject, and the
release that carries it is named in the release record per `blueprint/11-continuous-delivery.md`.
This is not a practice to apply when convenient. `standards/information-items.md` states the
rule and `ADR-013` records why a requirement with no delivering release is a declared
requirement that has not shipped.

## Quality gate

The subject line of every commit in the range parses as `type(scope): subject` with a type
from the list above, and each commit in the range builds on its own. The main branch is
green at its head, and every pull request merged into that range names the requirement
identifiers it satisfies or states that it satisfies none. A reviewer who did not write the
commits reaches the same verdict on what the range contains and on whether it can be
reverted, from the log and the branch status alone.
