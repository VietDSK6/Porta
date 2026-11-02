# Contributing to Porta

Porta is an individual entry in the Kotlin Student Coding Competition. To preserve the contest's solo-entry and
original-work requirements, the repository does not accept third-party code or documentation contributions before
the submission deadline on 7 December 2026 at 23:59 CET.

Bug reports and reproducibility feedback are welcome through GitHub Issues. Please do not submit a pull request that
implements a fix or feature during the contest period. Reports should include steps to reproduce, the platform,
expected and observed behavior, and rights-safe screenshots when useful.

## Development workflow

The current implementation lives in `MobileApp/`. Run the scoped gates from that directory:

```bash
./gradlew spotlessCheck
./gradlew :shared:jvmTest :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
```

Do not use aggregate `check`, `build`, or `clean build` tasks for routine validation because they pull in unrelated
targets, including iOS. See `AGENTS.md` and `skills/` for the retained agent and starter-kit workflows.

## Starter-kit contributions

This repository began from the MIT-licensed Koko KMP Contest Starter Kit. Improvements intended for the generic
starter kit belong in its upstream project, not in Porta's contest submission. Porta-specific product truth and
provenance are documented in `README.md`, `MobileApp/PORTA_PRD.md`, and `MobileApp/CONTEST_SCORECARD.md`.
