# Porta

> See which realistic next milestone creates the most assessed progress—without pretending to predict who wins.

Porta is a local-first Kotlin Multiplatform planning app for students. It evaluates a curated opportunity pack
against a student's actual profile, explains every assessed published rule, and builds an **Action Frontier** of
controllable milestones ranked under explicit time, effort, cost, and saved-opportunity constraints.

Porta does not predict admission, funding, or selection. It keeps missing information, manual checks, soft
selection factors, and deterministic eligibility rules visibly separate.

## Contest targets

The submission declares two runnable targets:

- **Android** — compact profile updates, opportunity review, What-if previews, and milestone tracking.
- **JVM Desktop** — wide comparison, source inspection, pack diagnostics, and keyboard-oriented planning.

iOS and Web wrappers remain inherited compatibility scaffolding from the starter kit. They are not declared as
runnable contest targets.

## What Porta does

- Stores an academic profile locally, with explicit units and evidence metadata.
- Evaluates a bundled 15-opportunity catalog using typed, deterministic rules.
- Separates passed checks, actionable gaps, missing information, manual review, soft criteria, archived cycles,
  and closed deadlines.
- Links every bundled hard rule to its official source and last manual review date.
- Generates only curated, controllable milestones; immutable or sensitive attributes never become advice.
- Computes a Pareto Action Frontier and explains impact, duration, effort, cost, deadline feasibility, and
  exclusions without showing a misleading universal score.
- Previews a milestone as a temporary scenario without modifying the saved profile.
- Tracks milestone progress and requires a separate explicit confirmation before changing an actual fact.
- Imports and exports versioned opportunity packs with validation and an approval diff before activation.
- Provides focused profile clearing, full local reset, and profile metadata export.

The contest build needs no account, cloud service, Firebase project, analytics SDK, API key, subscription, or
network connection for its core flows. Opening an official source intentionally uses the network.

## Try the core journey

1. Start with the fictional sample student or create a minimal profile.
2. Open **Doors** and inspect the current source-linked conclusions.
3. Open **Next moves** and adjust time, effort, cost, or saved-door priority.
4. Select a milestone to compare the Actual profile with its What-if result.
5. Inspect the exact rule and source behind a changed door.
6. Add the milestone to the local plan.
7. Complete the plan and explicitly confirm an actual profile update; completion alone never changes eligibility.
8. On Desktop, inspect the wide comparison workspace and opportunity-pack diagnostics.

## Repository layout

```text
MobileApp/       Porta Android/Desktop app, shared Kotlin, tests, and contest documentation
Documentation/   Inherited starter-kit documentation submodule
Web/             Inherited starter-kit web/backend scaffolding; not part of the Porta contest target claim
```

The implementation and all commands below live in `MobileApp/`.

## Prerequisites

- JDK 17 or newer; JDK 21 is recommended for the complete local test path.
- Android Studio and an Android SDK for the Android target.
- `MobileApp/local.properties` containing the local SDK path:

```properties
sdk.dir=/absolute/path/to/Android/sdk
```

No Firebase files or provider credentials are required.

## Run Android

From `MobileApp/`:

```bash
./scripts/check_env.sh --phase getting-started
./gradlew :androidApp:assembleDebug
```

The APK is written to `androidApp/build/outputs/apk/debug/androidApp-debug.apk`. Open `MobileApp/` in Android
Studio and run the `androidApp` configuration on an emulator or device to interact with the app.

## Run JVM Desktop

From `MobileApp/`:

```bash
./gradlew :desktopApp:run
```

This task stays attached while the window is open. Build a native package for the current operating system with:

```bash
./gradlew :desktopApp:packageDistributionForCurrentOS
```

Packages are written under `desktopApp/build/compose/binaries/main/`.

## Architecture and shared Kotlin

```text
Android launcher ─┐
                  ├─ shared Compose UI + Navigation 3
Desktop launcher ─┘              │
                                 ├─ profile / plan / pack repositories
                                 ├─ typed rule + scenario evaluators
                                 ├─ constrained Pareto Action Frontier
                                 └─ Room + bundled local opportunity pack
```

Shared Kotlin owns the domain models, rule graph, deterministic evaluation receipts, scenario overlay, Action
Frontier, pack validation, repositories, Room persistence, ViewModels, UI state, and most Compose UI. Platform
code is kept narrow around launchers, drivers, storage paths, and packaging.

The measurement methodology and explicit exclusions are documented in
[`MobileApp/KMP_EVIDENCE.md`](MobileApp/KMP_EVIDENCE.md). It is an internal provenance check, not an official
contest scoring formula.

## Quality gates

Run only the scoped repository gates from `MobileApp/`:

```bash
./gradlew spotlessCheck
./gradlew :shared:jvmTest :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
```

For shared-code compatibility checks:

```bash
./gradlew :shared:compileKotlinJs :shared:compileKotlinWasmJs
```

Do not use aggregate `check`, `build`, or `clean build` tasks for routine validation because they pull in
unrelated targets, including iOS.

## Trust boundaries and limitations

- Results apply only to the rules encoded in the installed pack and its stated review date.
- “Assessed rules passed” never means selected, funded, admitted, or guaranteed eligible.
- Missing data remains unknown; manual checks never become automatic passes.
- Soft selection criteria never change deterministic eligibility.
- Scenarios never write to the actual profile.
- Action duration, effort, and cost are Porta planning estimates, not provider claims.
- TBA and rolling deadlines are not inferred from an older cycle.
- There is no automatic crawling, source refresh, cloud sync, AI eligibility decision, or application submission.

Always confirm the linked official provider source before acting on an opportunity.

## Evidence and product documents

- [`MobileApp/PORTA_PRD.md`](MobileApp/PORTA_PRD.md) — product scope, trust model, and acceptance criteria.
- [`MobileApp/OPPORTUNITY_PACK_SOURCES.md`](MobileApp/OPPORTUNITY_PACK_SOURCES.md) — source register and encoding boundaries.
- [`MobileApp/KMP_EVIDENCE.md`](MobileApp/KMP_EVIDENCE.md) — reproducible shared-Kotlin evidence.
- [`MobileApp/CONTEST_SCORECARD.md`](MobileApp/CONTEST_SCORECARD.md) — evidence-backed readiness status.
- [`MobileApp/SUBMISSION/`](MobileApp/SUBMISSION/) — final-submission checklist, essay draft, and demo plan.

## Provenance

This repository started from the MIT-licensed **KMP Contest Starter Kit (Koko)**. Inherited target configuration,
platform launchers, CI/release helpers, and generic design-system infrastructure are not presented as Porta-owned
contest work.

Porta-specific contest-period work includes the opportunity and rule models, deterministic evaluators, constrained
Action Frontier, pack schema and validation, profile and plan persistence, adaptive Porta screens, curated source
register, tests, and product documentation. Git history and the evidence documents preserve this boundary.

## License

Released under the [MIT License](LICENSE). Existing copyright and attribution notices are preserved in the license
file and repository history.
