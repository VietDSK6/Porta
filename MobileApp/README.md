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

1. Finish onboarding with **Use ready-made demo profile** for Linh, an editable fictional CS undergraduate with
   deliberate strengths and gaps, a 180-day horizon, and Erasmus, Schwarzman, and Kotlin Contest already saved. Or
   browse the temporary sample without saving or enter your own local facts. Demo facts remain explicitly labelled.
2. Open **Doors** to see the current source-linked conclusions.
3. Open **Next moves** and change the time, budget, and effort constraints.
4. On Desktop, select two or three actions in Compare Lab to compare independent What-if results against the same
   Actual profile; scenarios are never combined or saved.
5. Save an action as the active plan, then explicitly update the actual profile when the milestone is completed. Add
   an optional image, PDF, text file, or Word document as local supporting evidence and reopen it from Porta.
6. Export profile metadata on Android, then open **Data & privacy** on Desktop to preview and import the JSON copy.
   The import replaces profile facts and scoped confirmations in one local transaction while keeping plans, saved
   doors, planning limits, and the current opportunity catalog.
7. Open **Pack inspector** on Desktop to inspect source diagnostics. Import a pack JSON file to see validation
   results and an added/changed/removed preview; the active catalog changes only after explicit approval. You can
   export the active pack or diagnostic results from the same screen.
8. From Pack inspector, open **Opportunity Studio** to draft a sourced record with form-based `ALL`/`ANY`, atomic,
   and manual-check rules. Resolve validation issues, export the draft JSON, then return to Pack inspector to preview
   and approve its import. Studio does not silently activate the draft.

The bundled catalog currently contains 15 independently encoded records. Its official URLs, interpretation boundary,
and last manual verification date are documented in [OPPORTUNITY_PACK_SOURCES.md](OPPORTUNITY_PACK_SOURCES.md).

## Declared contest targets

The Porta submission promises two runnable targets:

- **Android** — compact, touch-first tracking and profile updates.
- **JVM Desktop** — wide 2–3 scenario comparison, profile handoff, pack inspection and import, sourced rule authoring,
  and planning workspace.

The repository inherits iOS and Web wrappers from the starter kit. They remain useful compatibility scaffolding, but
they are not declared Porta contest targets and are excluded from the submission's runnable-target claim.

Git history records the first Porta product commit as
[`6be9ec1`](https://github.com/VietDSK6/Porta/commit/6be9ec19687ee7fe209c05eacd69aa3eae5fb96e)
on 20 October 2026. The inherited Koko baseline immediately before Porta work is
[`bd5fd5d`](https://github.com/VietDSK6/Porta/commit/bd5fd5dd1e268cac6ab11eb84f0d74d2d985f396).

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
                                 ├─ profile / plan / pack repositories and import validation
                                 ├─ typed published-rule + scenario engines
                                 ├─ constrained Action Frontier
                                 ├─ Desktop Opportunity Studio draft compiler
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
- Evidence attachments are copied into app-private local storage on Android/Desktop, capped at 10 MB, and labelled as
  user-provided rather than independently verified. Profile JSON handoff exports only the attachment descriptor—not
  the binary file or its private storage key.
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

## Documentation map

- [`PORTA_PRD.md`](PORTA_PRD.md) defines the current product scope and acceptance criteria.
- [`CONTEST_SCORECARD.md`](CONTEST_SCORECARD.md) records current contest readiness and unresolved gates.
- [`KMP_EVIDENCE.md`](KMP_EVIDENCE.md) documents shared-Kotlin evidence and explicit exclusions.
- [`OPPORTUNITY_PACK_SOURCES.md`](OPPORTUNITY_PACK_SOURCES.md) records source provenance and encoding boundaries.
- [`SUBMISSION/`](SUBMISSION/) contains the essay, demo, media, and fresh-setup materials.
- [`docs/history/ai-process/`](docs/history/ai-process/) preserves one-off AI-assisted prompts, audits, plans, and
  verification snapshots. Those files are historical provenance, not current submission status.

Reusable agent instructions and skills remain in their operational locations; they are development tooling, not
product or judging claims.

## Provenance and starter-kit boundary

This repository began from the official MIT-licensed
[**KMP Contest Starter Kit (Koko)**](https://kotlinfoundation.org/news/introducing-the-kmp-contest-starter-kit/).
The Kotlin Foundation created Koko for participants to use as a submission starting point and explicitly included
agent context and skills as development infrastructure.

| Inherited Koko foundation | Porta-specific contest work |
|---|---|
| Gradle targets, launchers, CI/release helpers, generic design system, reusable agent skills | Typed opportunity and rule models, deterministic eligibility and scenario engines, Action Frontier |
| Optional auth, billing, AI/media, ads, analytics, Remote Config, and notification architecture | Pack schema, validation and import; Room profile/plan persistence; saved doors and scoped assertions |
| Generic test and publishing foundations | Porta screen flows, adaptive Desktop workspace, curated sources, tests, and documentation |

Unused commercial-service infrastructure has been removed from the Porta app. Unchanged starter code,
package-only renames, generated/vendor code, and mixed-provenance integration files are excluded from Porta's
originality and shared-code evidence; see [KMP_EVIDENCE.md](KMP_EVIDENCE.md) for the reproducible allowlist and
measurement method.

The repository license is available at [../LICENSE](../LICENSE).
