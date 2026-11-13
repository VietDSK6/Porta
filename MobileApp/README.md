# Porta

> Plan an honest route through student opportunities, without pretending to predict who wins.

Porta is a local-first Kotlin Multiplatform planning app for students. **Route** sequences up to four milestones
against an opportunity pack's published checks and the student's time, cost, effort, and deadlines. The app explains
each assessed conclusion and offers an **Action Frontier** for comparing individual next moves. It does not predict
admission, selection, or funding.

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

Opportunity Studio also offers optional AI **rule drafting**. With no API key, three bundled source excerpts provide an
offline demonstration. If the student adds a personal Gemini key in the Profile tab's AI drafting settings, Studio
sends only the text they paste for that draft request to Gemini. The key stays in local preferences and is absent from
profile exports. Every suggested rule must quote the pasted source: Porta checks the quotation, marks a missing quote
as rejected, and turns unsupported facts, operators or units into manual checks. The curator must accept, edit or
reject each suggestion. Accepted rules still pass Studio's compiler and validator, then Pack inspector's explicit
diff and import approval. AI never supplies an eligibility conclusion.

## Demo path

1. In the two-step onboarding, try the clearly fictional IELTS 6.5 → 7.0 threshold lesson, then tap **Explore Linh's
   sourced route**. The lesson demonstrates four changed checks in a synthetic five-opportunity sample; it is separate
   from Linh's editable fictional CS profile and the sourced 15-door catalog. Linh's current 180-day route improves
   checks but opens **zero** doors under the encoded rules and deadlines. You can also browse without saving a profile
   or enter your own local facts.
2. Open **Doors** to see the current source-linked conclusions.
3. Open **Route** to compare ordered milestone paths. Compact layouts show a timeline; wide Desktop layouts show a
   metro-style map. Inspect which checks improve, which doors open, and which deadlines can close along the way.
   Change the time, estimated cost per step, and effort limits, then save a chosen path as ordered steps, replacing the current active plan.
4. Open **Next moves** for individual actions. On Desktop, select two or three actions in Compare Lab to compare
   independent What-if results against the same
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
   and manual-check rules. Paste a source excerpt to try optional AI suggestions, inspect each highlighted quotation,
   then accept, edit or reject individual rules. Resolve validation issues, export the draft JSON, then return to
   Pack inspector to preview and approve its import. Studio does not silently activate the draft.

On Desktop, the **File**, **View**, and **Help** menus expose import/export, navigation, search, and source actions.
The View shortcuts use Cmd+1–5 on macOS or Ctrl+1–5 elsewhere; Cmd/Ctrl+O, E, and F open pack import, profile export,
and search. Dropping a pack JSON file on the window opens its validation preview in Pack inspector. Window size,
position, and maximized state are saved locally.

The bundled catalog currently contains 15 independently encoded records. Its official URLs, interpretation boundary,
and last manual verification date are documented in [OPPORTUNITY_PACK_SOURCES.md](OPPORTUNITY_PACK_SOURCES.md).

The interface starts in English and offers English and Vietnamese controls in Profile settings.
Official provider quotations remain in their published language. Date and fact-value display follows the selected
interface language; pack JSON and profile exports retain stable, language-independent values.

## Terminal companion

The JVM CLI uses the same `:engine` rule evaluators and Route planner as the Android and Desktop app, along with
the app's pack parser and profile-export decoder. From `MobileApp/` with JDK 21 available:

```bash
./gradlew :cli:run --args='export-bundled --out porta-pack.json'
./gradlew :cli:run --args='validate porta-pack.json'
./gradlew :cli:run --args='evaluate --profile my-profile.json --pack porta-pack.json --date 2026-11-11'
./gradlew :cli:run --args='route --profile my-profile.json --pack porta-pack.json --horizon 180 --budget low --effort high --json'
```

Use a JSON profile exported from Porta for `--profile`. `evaluate` and `route` use the bundled catalog when `--pack`
is omitted; `--date` defaults to the local day. `route` prints human-readable paths unless `--json` is supplied.
These commands assess encoded checks and planning estimates, not provider decisions. The CLI is a terminal companion,
not a third graphical contest target.

## Declared contest targets

The Porta submission promises two runnable targets:

- **Android** — compact, touch-first tracking and profile updates.
- **JVM Desktop** — wide route map and 2–3 scenario comparison, profile handoff, pack inspection and import, sourced
  rule authoring, native menus and shortcuts, and planning workspace.

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
The AI drafting demo runs offline; a personal Gemini key is optional for live drafting and is stored only on that device.

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
                  ├─ :shared Compose UI, Navigation 3, Room, pack/profile codecs
Desktop launcher ─┘       │
                          └─ :engine rules, scenario, Frontier, Route, pack DSL
CLI ────────────────── :shared codecs + :engine decisions
```

The `:engine` module owns pure domain models, the deterministic rule graph, eligibility receipts, counterfactual
simulation, Action Frontier, Route planner, and the typed pack DSL. `:shared` uses that engine for Android/Desktop
while owning Room persistence, pack/profile codecs, ViewModels, and Compose UI. `:cli` calls the same engine through
the shared codecs. The DSL-generated 15-door bundle was compared with the previous pack for exact JSON, DTO, and
rule-fingerprint equality before the previous source file was removed. Platform modules provide launchers and
platform services.
There is no server-authoritative admission or selection decision.

The core product documents are:

- [PORTA_PRD.md](PORTA_PRD.md) — scope, trust boundary, and acceptance criteria.
- [OPPORTUNITY_PACK_SOURCES.md](OPPORTUNITY_PACK_SOURCES.md) — source register and encoding policy.
- [KMP_EVIDENCE.md](KMP_EVIDENCE.md) — reproducible shared-Kotlin measurement and exclusions.
- [CONTEST_SCORECARD.md](CONTEST_SCORECARD.md) — evidence-backed contest readiness; no self-awarded judging score.

### Shared Kotlin evidence

The conservative contest-period measurement at the local P5 code commit `dc4e701` finds 21,991 NCLOC across 144 newly added, explicitly
allowlisted shared Porta production files, plus 673 NCLOC across 13 platform and CLI files. Static opportunity/sample data,
tests, integration files with mixed starter provenance, generated/build/vendor code, package-only renames, and
unchanged scaffold are excluded or reported separately. This is an internal provenance check—not an official contest
formula or a claim that the whole repository is 97.0% shared. Reproduce it with:

```bash
./scripts/measure_porta_kmp_evidence.sh
```

## Quality gates

Run these scoped local checks:

```bash
./gradlew spotlessApply
./gradlew spotlessCheck
./gradlew :engine:jvmTest
./gradlew :shared:jvmTest :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
./gradlew :desktopApp:packageDistributionForCurrentOS
./gradlew :cli:test
```

Web build work is deferred. Historical shared JS/Wasm compile checks are not evidence of a runnable browser target.

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
- Route counts only doors opened under the encoded rules before their deadlines. The bundled 15-door Linh demo
  currently yields paths that improve checks but open zero doors; the UI keeps that outcome visible.
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
| Gradle targets, launchers, CI/release helpers, generic design system, reusable agent skills | Typed opportunity and rule models, deterministic eligibility and scenario engines, Action Frontier, Route planner, pack DSL and CLI |
| Optional auth, billing, AI/media, ads, analytics, Remote Config, and notification architecture | Pack schema, validation and import; Room profile/plan persistence; saved doors and scoped assertions |
| Generic test and publishing foundations | Porta screen flows, adaptive Desktop workspace, curated sources, tests, and documentation |

Unused commercial-service infrastructure has been removed from the Porta app. Unchanged starter code,
package-only renames, generated/vendor code, and mixed-provenance integration files are excluded from Porta's
originality and shared-code evidence; see [KMP_EVIDENCE.md](KMP_EVIDENCE.md) for the reproducible allowlist and
measurement method.

The repository license is available at [../LICENSE](../LICENSE).
