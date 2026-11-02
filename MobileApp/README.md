# Porta

> See which realistic next milestone creates the most assessed progress—without pretending to predict who wins.

Porta is a local-first Kotlin Multiplatform planning app for students. It evaluates a bundled opportunity
pack against the student's actual profile, explains every published-check conclusion, and builds an **Action Frontier** of
controllable milestones ranked by assessed marginal opportunity gain, time, effort, and cost.

The contest build is completely free. Core profile, rule assessment, simulation, and planning work without an account,
cloud service, API key, or network connection. Opening official sources requires the network, and a future catalog refresh
may use it deliberately.

## What Porta does

Porta keeps five conclusions visible:

- **Assessed rules passed** — every published rule Porta can deterministically assess currently passes.
- **Needs action** — a published check fails and has a controllable or time-dependent path.
- **Needs information** — a fact is missing or an official-source review is still required.
- **Not available this cycle** — a current-cycle requirement cannot be addressed through a profile update.
- **Closed** — the published application window has ended.

From those explanations, Porta proposes only controllable milestones, such as improving a language score or completing
a documented project. The What-if view previews only the doors changed by one milestone. It never writes a
hypothetical value into the actual profile; the student must confirm a real update separately.

Programme-specific confirmations are scoped to one opportunity and one rule fingerprint. Confirming availability
for one internship cannot silently change another programme, and a changed source rule makes an old assertion stale.

Every opportunity shows the date Porta last reviewed its official source. Porta uses a visible 90-day catalog
maintenance window to flag records for another manual review; this is an internal stewardship signal, not a claim
that a provider independently verified the data. The app re-evaluates deadlines when the local calendar day changes.
Archived cycles remain inspectable but are separated from ordinary closed deadlines and excluded from action planning.

Porta does **not** predict admission or scholarship success, turn soft selection criteria into pass/fail rules, scrape
providers, submit applications, or use AI to decide eligibility.

## Demo path

1. Finish onboarding with the sample profile or enter six local profile facts.
2. Open **Doors** to see the current source-linked conclusions.
3. Open **Next moves** and change the time, budget, and effort constraints.
4. Select an action to compare the Actual profile with its What-if result across all affected doors.
5. Save an action as the active plan, then explicitly update the actual profile when the milestone is completed.
6. On Desktop, use the wide master-detail Frontier and Pack Inspector to inspect results and source diagnostics.

The bundled catalog currently contains 15 independently encoded records. Its official URLs, interpretation boundary,
and last manual verification date are documented in [OPPORTUNITY_PACK_SOURCES.md](OPPORTUNITY_PACK_SOURCES.md).

## Declared contest targets

The Porta submission promises two runnable targets:

- **Android** — compact, touch-first tracking and profile updates.
- **JVM Desktop** — wide comparison, inspection, and planning workspace.

The repository inherits iOS and Web wrappers from the starter kit. They remain useful compatibility scaffolding, but
they are not declared Porta contest targets and are excluded from the submission's runnable-target claim.

## Prerequisites

- JDK 17 or newer; JDK 21 is recommended for the complete local test path.
- A recent Android Studio installation with an Android SDK.
- `local.properties` in this directory with the local SDK path:

```properties
sdk.dir=/absolute/path/to/Android/sdk
```

Porta does not require Firebase configuration, authentication credentials, billing keys, or AI-provider API keys.

## Run Android

From `MobileApp/`:

```bash
./scripts/check_env.sh --phase getting-started
./gradlew :androidApp:assembleDebug
```

The APK is written to `androidApp/build/outputs/apk/debug/androidApp-debug.apk`. To interact with the app, open this
directory in Android Studio and run the `androidApp` configuration on an emulator or device.

## Run JVM Desktop

```bash
./gradlew :desktopApp:run
```

The Gradle task stays attached while the window is open; stop it with `Ctrl+C` after closing the app.

Build the native package for the current operating system with:

```bash
./gradlew :desktopApp:packageDistributionForCurrentOS
```

Compose Desktop writes the package below `desktopApp/build/compose/binaries/main/` in the platform-specific
`dmg`, `msi`, or `deb` directory.

## Architecture

```text
Android launcher ─┐
                  ├─ shared Compose UI + Navigation 3
Desktop launcher ─┘              │
                                 ├─ profile / plan / pack repositories
                                 ├─ typed published-rule + scenario engines
                                 ├─ constrained Action Frontier
                                 └─ Room + bundled local opportunity pack
```

The shared module owns the domain models, deterministic rule graph, evaluation receipts, counterfactual simulation,
Action Frontier, persistence, ViewModels, and most UI. Platform modules provide launchers and platform services.
There is no server-authoritative admission or selection decision.

The core product documents are:

- [PORTA_PRD.md](PORTA_PRD.md) — scope, trust boundary, and acceptance criteria.
- [OPPORTUNITY_PACK_SOURCES.md](OPPORTUNITY_PACK_SOURCES.md) — source register and encoding policy.
- [KMP_EVIDENCE.md](KMP_EVIDENCE.md) — reproducible shared-Kotlin measurement and exclusions.
- [CONTEST_SCORECARD.md](CONTEST_SCORECARD.md) — evidence-backed contest readiness; no self-awarded judging score.

### Shared Kotlin evidence

The conservative contest-period measurement currently finds 14,933 NCLOC across 75 newly added, explicitly
allowlisted shared Porta production files, plus 96 NCLOC across four narrowly scoped platform files. Static opportunity/sample data,
tests, integration files with mixed starter provenance, generated/build/vendor code, package-only renames, and
unchanged scaffold are excluded or reported separately. This is an internal provenance check—not an official contest
formula or a claim that the whole repository is 99.4% shared. Reproduce it with:

```bash
./scripts/measure_porta_kmp_evidence.sh
```

## Quality gates

Run the same scoped checks used by the repository workflow:

```bash
./gradlew spotlessApply
./gradlew spotlessCheck
./gradlew :shared:jvmTest :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
```

When shared code changes, these additional compile checks protect the inherited browser source sets without claiming
them as contest targets:

```bash
./gradlew :shared:compileKotlinJs :shared:compileKotlinWasmJs
```

Do not use the aggregate `check`, `build`, or `clean build` tasks for routine validation; they pull in unrelated
platform work, including iOS.

## Data and limitations

- Results apply only to the rules encoded in the installed pack and its stated verification date.
- `Assessed rules passed` means only that Porta's current deterministic assessment passes; it never means fully eligible,
  competitive,
  selected, funded, or accepted.
- Programme-scoped assertions apply only when opportunity ID, rule ID, and rule fingerprint all still match.
- Completing a plan changes plan status only. A separate evidence-aware confirmation updates the actual profile.
- Provider-specific, constituency-specific, legal, soft, or ambiguous conditions remain explicit manual checks.
- Action duration, cost, and effort are Porta planning estimates, not provider claims.
- TBA and rolling deadlines are never inferred from an older cycle.
- Opportunity-pack imports show an added/changed/removed preview before activation. Profile facts and saved doors are
  preserved; programme confirmations whose exact rule fingerprint changed remain stored but no longer satisfy checks.
- Profile data controls separate a focused clear from a full device reset. The focused action removes the current
  profile's facts, programme confirmations and plans but keeps saved doors, catalogs and planning preferences. The
  full reset removes all local Porta profiles, confirmations, plans, saved doors, imported catalogs and preferences,
  then restores the bundled catalog so the app remains usable.
- The contest build has no cloud sync, automatic source refresh, or application submission.

Always confirm the linked official source before acting on an opportunity.

## Provenance and starter-kit boundary

This repository began from the MIT-licensed **KMP Contest Starter Kit (Koko)**. Inherited scaffold includes Gradle
target configuration, platform launchers, CI/release helpers, and the generic design system. The unused starter auth,
billing, AI/media-generation, ads, analytics, Remote Config, and push-notification infrastructure has been removed from
the Porta app.

Porta-specific contest work includes the typed opportunity/rule models, deterministic eligibility and scenario
engines, constrained Pareto Action Frontier, pack schema/validation/import, profile and plan persistence, the Porta
screen flows and adaptive Desktop workspace, curated source register, tests, and product documentation. Unchanged
starter code is excluded from Porta's originality and shared-code claims.

The repository license is available at [../LICENSE](../LICENSE).
