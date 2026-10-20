---
name: kotlin-convention-audit
description: Review Kotlin and Compose Multiplatform code against the official Kotlin coding conventions and this repository's intentional ktlint exceptions. Use for Kotlin code review, weekly milestones, contest scoring evidence, or before submission; do not trigger for non-Kotlin documentation-only work.
---

# Kotlin convention audit

Evaluate both machine-formatting compliance and the official conventions that ktlint cannot prove. Preserve
working architecture and do not create churn for optional preferences that do not improve clarity.

## Read first

Read [`references/official-kotlin-conventions.md`](references/official-kotlin-conventions.md). For a final contest
audit, also open the live [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) and
record the verification date.

## Audit workflow

1. Define the Kotlin scope: changed files for a feature review, all Porta-owned Kotlin for a milestone/final audit.
2. Inspect `build.gradle.kts` and `.editorconfig` if formatting configuration changed. Repository configuration is
   the mechanical source of truth, while the official guide remains the judging reference.
3. Run `./gradlew spotlessCheck` from `MobileApp/` when validation is requested. Use `spotlessApply` only when the
   user asked to fix or complete the changes, then run `spotlessCheck` once.
4. Manually review the categories in the reference: source organization, naming, layout, formatting exceptions,
   documentation, redundancy, idiomatic Kotlin, and KMP-specific filenames.
5. Classify findings before editing:
   - **Violation:** contradicts a clear official convention without a justified project exception.
   - **Repository exception:** intentionally differs because of Compose, tests, generated code, or documented
     project configuration.
   - **Idiomatic improvement:** official preference whose benefit depends on context.
   - **Readability issue:** judge-visible complexity not fully described by a mechanical rule.
6. If fixes are in scope, make the smallest coherent edits. Do not rename public APIs or reshape architecture only
   to satisfy an optional preference without checking impact.
7. For code changes, complete validation using the repository `run-quality-gates` skill. One Gradle invocation at
   a time; never substitute aggregate `check`, `build`, or `clean build` tasks.

## Non-negotiable interpretations

- PascalCase `@Composable` functions returning `Unit` follow the official convention.
- Non-Composable functions still use lower camel case even though the repository disables ktlint's broad
  `function-naming` rule to support Compose and backtick test names.
- Backtick test names are allowed. Consider Android runtime limitations only for tests that actually run on-device.
- A green formatter does not prove good names, sensible file boundaries, immutable APIs, readable control flow,
  correct KMP suffixes, or justified KDoc.
- Optional guidance is not an automatic defect. Prefer consistent, readable code over mass mechanical rewrites.
- Generated and vendored code is excluded from manual scoring unless the submission presents it as entrant-owned
  product code.

## Output contract

Report:

1. Spotless result or why it was not run.
2. Findings ordered by severity with file and line.
3. Intentional exceptions checked.
4. Remaining manual risks.
5. A conventions score estimate only when requested, clearly labeled non-official and backed by findings.

Do not claim the contest's full 20% merely because `spotlessCheck` passes.
