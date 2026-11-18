# Porta — contest app built on the KMP contest starter kit

> **Porta** is the submitted product: a local-first Kotlin Multiplatform planning app that evaluates a curated
> opportunity pack against a student's profile and ranks controllable next milestones (the **Action Frontier**).
> The repository began from the MIT-licensed Koko KMP contest starter kit. Product and submission sources of truth:
> root `README.md`, `MobileApp/PORTA_PRD.md`, and `MobileApp/CONTEST_SCORECARD.md`.

## Repository map

- **`MobileApp/`** — everything that builds. All Gradle commands run from here. Gradle modules
  (`settings.gradle.kts`): `:engine`, `:shared`, `:cli`, `:androidApp`, `:desktopApp`, `:webApp`, `:designsystem`.
- **`Web/`** — inherited Firebase Hosting / Cloud Functions scaffolding. Not used by Porta.
- **`Documentation/`** — inherited Docusaurus submodule for the starter kit.
- **`skills/`** — inherited starter-kit Agent Skills (`.claude/skills` symlinks here). Index: `skills/README.md`.
- **`MobileApp/.agents/skills/`** — Porta-specific skills: `contest-readiness`, `kotlin-convention-audit`.
- **`AiGuidelines/`** — role prompts and tech guides; `AiGuidelines/project/` is filled in for Porta.
- **`PROGRESS_*.md`** at the git root (not inside `MobileApp/`) — feature/phase progress. Read before resuming work.

**Declared contest targets are Android + JVM Desktop only.** iOS and Web/Wasm are inherited scaffolding: keep
shared code compiling for them, but don't run iOS builds unless asked and never claim them as targets.

### What was removed from the starter kit

Porta's contest build has **no** Firebase, KMPAuth/sign-in, subscriptions (Adapty/RevenueCat), paywall, credits,
ads, push notifications, Calf permissions, or `AiTransport`/Cloud Functions proxy. The README states the core
flows need no account, network, or API key. Many skills in `skills/` (integrations, monetization, growth,
publishing) describe those removed systems. **Don't reintroduce any of them without the developer asking**, and
don't trust a skill's file paths without checking that the files exist. `MobileApp/libs/subscription/` and some
empty folders under `presentation/screens/` are untracked leftovers that are not part of the build.

## Commands (from `MobileApp/`)

```bash
./gradlew spotlessCheck                     # lint/format (Spotless + ktlint); fix with spotlessApply
./gradlew :engine:jvmTest :shared:jvmTest :shared:testAndroidHostTest :cli:test   # CI test gate
./gradlew :androidApp:assembleDebug         # Android validation (APK in androidApp/build/outputs/apk/debug/)
./gradlew :desktopApp:run                   # Desktop app; stays attached while the window is open
./gradlew :shared:compileKotlinJs :shared:compileKotlinWasmJs   # keep shared code web-compatible
```

Single test class / method:

```bash
./gradlew :engine:jvmTest --tests "io.github.vietdsk6.porta.domain.usecase.RoutePlannerTest"
./gradlew :shared:jvmTest --tests "*DatabaseMigrationTest"
./gradlew :shared:jvmTest --tests "*.ProfileRepositoryTest.someTestName"
```

CLI (Clikt, JVM; subcommands `validate`, `evaluate`, `route`, `export-bundled`). CI runs this smoke check:

```bash
./gradlew :cli:run --args="export-bundled --out build/bundled.json"
./gradlew :cli:run --args="validate build/bundled.json"
```

Other: `./gradlew :desktopApp:packageDistributionForCurrentOS` (native installer),
`./scripts/measure_porta_kmp_evidence.sh` (shared-Kotlin evidence for `KMP_EVIDENCE.md`),
`./scripts/check_env.sh --phase getting-started` (environment check).

CI (`.github/workflows/pr_checks.yml`) = `spotlessCheck` → the test gate above → CLI smoke → `assembleDebug`.

### Validation guardrails (avoid the build loop)

- **Never run aggregate `check`, `build`, or `clean build`.** They pull in every target, iOS included, so an
  unrelated iOS failure fails the run and is slow. Run only the scoped tasks above.
- **`assembleDebug` succeeding is the Android validation.** Don't install and launch via `adb` and poll it. To
  see the UI, use the `verify-ui` skill (headless PNG) or ask the developer to hit Run in the IDE.
- **Run tasks never exit** (`:desktopApp:run`, `:webApp:wasmJsBrowserDevelopmentRun`). Start once, in the
  background if you need the shell. A task that hasn't returned is running, not hung.
- **Configuration cache is on.** Execution-time code must be config-cache safe. Never add a
  `commonWebpackConfig { }` block to `webApp/build.gradle.kts` (it breaks every `:webApp:wasmJs*` task); dev-server
  settings live in `webApp/webpack.config.d/*.js`.
- **A failed or slow gate is not a retry signal.** Read the error and fix it, or stop and report. The first build
  is slow (JBR + Compose downloads).
- Tests run on a **JDK 21** JVM while code compiles against 17 (`tasks.withType<Test>` in
  `shared/build.gradle.kts`). Robolectric loads dependency bytecode built for Java 21. The `:cli` module uses
  `jvmToolchain(21)`. The foojay resolver provisions JDKs automatically.
- Android Studio's Build Analyzer flags four `kmpPartiallyResolvedDependenciesChecker` "always-run" tasks. They
  come from the AGP KMP plugin and cost about 0.04s; ignore them.

## Architecture

```
:androidApp ─┐                      :cli (JVM, Clikt) ──┐
:desktopApp ─┼─► :shared (Compose UI, ViewModels, Room, │
:webApp ─────┘     repositories, pack I/O, Koin) ───────┴─► :engine (pure KMP domain)
                        │
                        └─► :designsystem (reusable Compose components)
```

### `:engine` — pure domain, no UI/persistence

`engine/src/commonMain/.../porta/domain/` holds the product logic that every surface shares. Dependencies are only
kotlinx-datetime and kotlinx-serialization: no Room, Compose, Koin, or Ktor. `:shared` exposes it with
`api(projects.engine)`.

- `model/` — `Opportunity`, `OpportunityPack`, `ProfileFact`, `ActionFrontier`, `RouteModels`, `PlanItem`, rule
  drafts, lifecycle and fingerprints.
- `dsl/` — Kotlin DSLs (`OpportunityPackDsl`, `EligibilityDsl`, `FactDsl`) used to author packs and rules.
- `usecase/` — `EligibilityEvaluator` (deterministic rule receipts), `ScenarioEvaluator` (What-if overlay),
  `ActionFrontierEngine` (constrained Pareto frontier), `RoutePlanner` (multi-step routes), `CandidateCatalog`
  (curated controllable milestones), `QuoteGuard`.

Put new evaluation or ranking logic in `:engine` with `commonTest` coverage, not in ViewModels.

### `:shared` — app layer (`shared/src/commonMain/kotlin/io/github/vietdsk6/porta/`)

- `root/` — `App.kt`, `AppInitializer.kt` (Koin start + startup side effects), `Di.kt` (`dataModule`,
  `domainModule`, `presentationModule`, `appModules`; `platformModule` is expect/actual), and `AppConfiguration.kt`
  (only legal URLs, contact email, and App Store id).
- `data/repository/` — concrete repositories: profile, plan, saved opportunities, rule assertions, opportunity pack,
  profile import, rule drafts, and the Opportunity Studio draft compiler.
- `data/source/local/` — Room 3 database, DAOs, and entities. `pack/` holds the bundled pack (`BundledPack.kt`,
  written in the engine DSL) plus the JSON codec, parser, normalizer, and validator for import/export.
- `data/scenario/` — the bundled pack source and sample scenarios. `data/sample/PortaSampleData.kt` is the fictional
  sample student.
- `data/source/remote/draft/` — the only network feature. `RuleDraftRepository` uses the offline `DemoDraftService`
  unless the user saved their own key (`UserPreferences.KEY_AI_DRAFT_API_KEY`), in which case it uses
  `GeminiDraftService`. Drafts are untrusted suggestions that must be grounded before use.
- `presentation/screens/<feature>/` — `doors`, `frontier`, `route`, `scenariopreview`, `opportunitydetail`,
  `activeplan`, `actualprofileupdate`, `studentprofile`, `profilesetup`, `onboarding`, `opportunitystudio`,
  `packinspector`, `dataprivacy`.
- `presentation/navigation/` — Navigation 3. `Routes.kt` (`@Serializable sealed interface ScreenRoute : NavKey`;
  the `TopLevelScreenRoute` tabs are Doors, Route, Frontier, Student profile), `NavigationState.kt`,
  `Navigator.kt` (`navigate`/`add`/`set`/`replace`/`switchToTab`/`goBack`), `AppNavigation.kt` (`NavDisplay` +
  `entry<…>` blocks), `AdaptiveAppScaffold.kt` (compact vs wide/Desktop layout), and `DesktopCommand.kt`
  (keyboard commands wired from `desktopApp`). ViewModels are scoped per `NavEntry`.
- `presentation/locale/` — in-app language switching (`AppLocale`, `AppLanguageState`) and localized
  fact/value/date text.

Source sets: `commonMain` (nearly everything), `nonWebMain` (Android/iOS/JVM; bundled SQLite driver), `webMain`
(js + wasmJs; `WebWorkerSQLiteDriver` + OPFS via `shared/sqlite-wasm-worker/worker.js`), `mobileMain`,
`nonMobileMain`, and per-platform `androidMain`/`jvmMain`/`iosMain`/`jsMain`/`wasmJsMain`.

### Product invariants (the trust model, enforced in code and tests)

These come from the README and PRD. Breaking one is a correctness bug, not a style choice.

- "Assessed rules passed" never means selected, funded, admitted, or guaranteed eligible. Never add a universal
  score or a prediction.
- Missing data stays unknown. Manual checks never become automatic passes. Soft selection criteria never change
  deterministic eligibility.
- Scenarios (What-if) never write to the actual profile. Completing a plan item never changes a fact on its own:
  updating the actual profile always needs a separate, explicit confirmation.
- Only curated, controllable milestones become advice. Immutable or sensitive attributes never do.
- Every bundled hard rule links to its official source and last manual review date
  (`MobileApp/OPPORTUNITY_PACK_SOURCES.md`). TBA and rolling deadlines are never inferred from an older cycle.
- Pack imports are validated and shown as an approval diff before activation. There is no crawling or auto-refresh.
- The Action Frontier is the product's core. What-if and the door views explain it, so don't reduce the app to a
  scholarship feed or a set of sliders.

### Conventions

- **Layering:** presentation → domain (`:engine`) → data. Repositories are concrete classes with no interface
  unless 2+ live implementations exist (`DraftService` qualifies). No pass-through use cases: a use case
  (`ManageLocalPortaDataUseCase`) exists only for real orchestration.
- **Domain models** are immutable `data class`es with no persistence, serialization, or UI concerns. Map
  DTOs/entities at the data boundary with `toModel()`/`toEntity()` extension functions.
- **`BackgroundExecutor.execute { }`** already catches, logs, and returns `Result.failure`. Don't wrap its body in a
  redundant try/catch.
- **Screens use two overloads:** a ViewModel entry point that collects `uiState` with
  `collectAsStateWithLifecycle()`, and a pure `(uiState, onUiEvent, …)` overload used by previews and
  `runComposeUiTest`. Feature folders contain only `*Screen.kt`, `*UiState.kt`, `*ViewModel.kt` (plus helper
  composables), never route files.
- **Koin:** `singleOf` for repositories, DB, and clients; `viewModelOf` for ViewModels. Inject dispatchers;
  never hardcode `Dispatchers.IO`.
- **Preferences:** `UserPreferences` (DataStore 1.3.0-alpha). The `DataStore<Preferences>` is intentionally
  not a Koin binding.
- **Strings:** `composeResources/values/strings.xml` **and** `values-vi/strings.xml` (Vietnamese). Add every new
  key to both. The files are plain XML, not Android aapt: don't backslash-escape apostrophes, and use the
  typographic `’` like the existing copy.
- **`@Preview`:** use `androidx.compose.ui.tooling.preview.Preview`, not the deprecated
  `org.jetbrains.compose…Preview` (ComposablePreviewScanner doesn't discover that one).
- **Formatting:** ktlint overrides in the root `MobileApp/build.gradle.kts` disable function naming (for
  Composables), wildcard imports, max line length, and others. `spotlessCheck` is necessary but not sufficient
  for the contest's Kotlin-conventions criterion: use the `kotlin-convention-audit` skill for reviews.

### Room 3 (`androidx.room3.*`, all in `commonMain`)

- `AppDatabase` is at **version 6**, with schemas exported to `shared/schemas/`. Every schema change needs
  a version bump, a `Migration` in `data/source/local/DatabaseMigrations.kt` registered in `DatabaseModule.kt`,
  and coverage in `jvmTest/.../DatabaseMigrationTest.kt`. Users have real local data, so never use destructive
  migration.
- DAO functions must be `suspend` or return `Flow`. Release builds are minified, and
  `androidApp/proguard-rules.pro` keeps the generated `RoomDatabase` `_Impl`.
- Keep Room and `androidx.sqlite` on the same release train (bump them together).

### Bundled pack and rule fingerprints

The bundled 15-opportunity pack is authored in the engine DSL (`BundledPack.kt`). `DslPackEquivalenceTest`
compares it with `shared/src/jvmTest/resources/porta_bundled_pack_golden.json`, a **frozen pre-DSL fixture.
Don't regenerate it.** The test requires unchanged records to stay equivalent and every atomic rule's
fingerprint to stay stable, because users' saved rule assertions are keyed by those fingerprints. Changing a
rule's identity orphans saved data. A deliberate source refresh must be listed explicitly in the test and in
`OPPORTUNITY_PACK_SOURCES.md`.

### Tests

- `engine/src/commonTest` — evaluator, frontier, route, DSL tests.
- `shared/src/commonTest` — repositories, ViewModels, and multiplatform Compose UI tests (`runComposeUiTest`).
  These run on both JVM and Android host. Use `kotlinx-coroutines-test`; there's no Turbine, so collect flows
  with `launch { flow.toList(list) }`.
- `shared/src/jvmTest` — JVM-only UI tests, migration test, DSL golden, Desktop storage paths, performance
  characterization.
- `shared/src/androidHostTest` — Robolectric. `PreviewScreenshotTest` snapshots every `@Preview` (local only, not a
  PR gate, goldens not committed): `./gradlew :shared:recordRoborazziAndroidHostTest` /
  `verifyRoborazziAndroidHostTest`.
- `cli/src/test` — `PortaCliServiceTest`.

## Scaffolding scripts (from `MobileApp/`)

Use these instead of hand-creating files. They patch shared files at `// Add new … — <script> inserts here.`
markers (leave those comments alone). They're idempotent; **never run them in parallel**.

- `./scripts/generate_screen.sh Name` — creates Screen/UiState/ViewModel and wires `Routes.kt`,
  `AppNavigation.kt`, and `Di.kt`. Then add navigation callbacks in the generated `entry<>` block.
- `./scripts/make_local.sh Model` — creates domain model + `@Entity` + `@Dao` and registers them in
  `AppDatabase`/`DatabaseModule`. Then bump the DB version and add a migration (see Room above).
- `./scripts/generate_store_screenshots.sh` — renders `@Preview @StoreScreenshot` composables to
  `distribution/store_screenshots/`.

## Agent working style

- Parallelize reads freely. Fan out implementation only across disjoint files. The orchestrating agent alone edits
  shared single-instance files: `Routes.kt`, `AppNavigation.kt`, `Di.kt`, `AppDatabase.kt`, `DatabaseModule.kt`,
  `DatabaseMigrations.kt`, both `strings.xml` files, and Gradle files.
- Run one Gradle invocation at a time, from the orchestrator, after merging subagent output. Subagents don't build.
- Before scope changes, milestones, or submission work, use `MobileApp/.agents/skills/contest-readiness`.
  `CONTEST_SCORECARD.md` is the living compliance and evidence record: never mark an item complete without
  observable evidence.
- Keep the provenance honest: preserve Git history and the starter-kit MIT license, and never present unchanged
  template code as Porta-authored (see the README's "Koko boundary" and `KMP_EVIDENCE.md`).
- Re-read the official online contest rules before irreversible submission decisions. Saved copies are dated
  snapshots.
- Archived AI-process notes live in `MobileApp/docs/history/ai-process/`. They are provenance, not current status.

## Prerequisites

JDK 17+ (21 recommended for the full test path), Android SDK path in `MobileApp/local.properties`
(`sdk.dir=…`). No Firebase files or provider credentials are needed.

Key versions (see `MobileApp/gradle/libs.versions.toml`): Kotlin 2.4.10, Compose Multiplatform 1.11.1,
AGP 9.3.1, Gradle 9.7.1, Room 3.0.1, SQLite 2.7.0, Navigation 3 1.1.1, Koin 4.2.2, Ktor 3.6.1.
Package: `io.github.vietdsk6.porta`.
