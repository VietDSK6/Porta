# Porta

> See which realistic next milestone opens the most verified opportunities—without pretending to predict who wins.

Porta is a local-first Kotlin Multiplatform planning app for students. It evaluates a sourced offline opportunity
pack against the student's actual profile, explains every encoded result, and builds an **Action Frontier** of
controllable milestones ranked by verified marginal opportunity gain, time, effort, and cost.

The contest build is completely free and does not require an account, cloud service, API key, or network connection.

## What Porta does

Porta keeps four boundaries visible:

- **Open** — the profile meets every encoded published hard rule.
- **Has gaps** — at least one encoded hard rule is not currently met.
- **Verify** — a published rule needs information or a manual source check.
- **Unknown** — the pack cannot safely represent the provider's condition.

From those receipts, Porta proposes only controllable milestones, such as improving a language score or completing
a documented project. The What-if view previews the exact doors changed by one milestone. It never writes a
hypothetical value into the actual profile; the student must confirm a real update separately.

Porta does **not** predict admission or scholarship success, turn soft selection criteria into pass/fail rules, scrape
providers, submit applications, or use AI to decide eligibility.

## Demo path

1. Finish onboarding with the sample profile or enter six local profile facts.
2. Open **Doors** to see the current sourced opportunity receipts.
3. Open **Frontier** and change the time, budget, and effort constraints.
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
GOOGLE_WEB_CLIENT_ID=test
```

The committed Firebase configuration files are starter placeholders. Porta's contest flow is offline and does not
depend on Firebase, authentication, billing, or AI services.

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
                                 ├─ typed eligibility + scenario engines
                                 ├─ constrained Action Frontier
                                 └─ Room + bundled local opportunity pack
```

The shared module owns the domain models, deterministic rule graph, evaluation receipts, counterfactual simulation,
Action Frontier, persistence, ViewModels, and most UI. Platform modules provide launchers and platform services.
There is no server-authoritative eligibility decision.

The core product documents are:

- [PORTA_PRD.md](PORTA_PRD.md) — scope, trust boundary, and acceptance criteria.
- [OPPORTUNITY_PACK_SOURCES.md](OPPORTUNITY_PACK_SOURCES.md) — source register and encoding policy.
- [CONTEST_SCORECARD.md](CONTEST_SCORECARD.md) — evidence-backed contest readiness; no self-awarded judging score.

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
- A hard-rule receipt means "meets the encoded eligibility rules," not "competitive" or "will be accepted."
- Provider-specific, constituency-specific, legal, soft, or ambiguous conditions remain explicit manual checks.
- Action duration, cost, and effort are Porta planning estimates, not provider claims.
- TBA and rolling deadlines are never inferred from an older cycle.
- The contest build has no cloud sync, automatic source refresh, or application submission.

Always confirm the linked official source before acting on an opportunity.

## Provenance and starter-kit boundary

This repository began from the MIT-licensed **KMP Contest Starter Kit (Koko)**. Inherited scaffold includes Gradle
target configuration, platform launchers, CI/release helpers, the generic design system, and optional auth, billing,
AI, and media-generation modules. Those optional starter services are not reachable from Porta's navigation graph and
are not required by its contest flow.

Porta-specific contest work includes the typed opportunity/rule models, deterministic eligibility and scenario
engines, constrained Pareto Action Frontier, pack schema/validation/import, profile and plan persistence, the Porta
screen flows and adaptive Desktop workspace, curated source register, tests, and product documentation. Unchanged
starter code is excluded from Porta's originality and shared-code claims.

The repository license is available at [../LICENSE](../LICENSE).
