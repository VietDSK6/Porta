# Porta current-state verification — W0

> Historical AI-assisted verification snapshot. Retained for provenance; see the current contest scorecard.

**Checked:** 28 Oct 2026. **Scope:** current `MobileApp/` working tree and generated Compose previews. This is a static source and preview audit plus scoped build gates; it is not a live Android or Desktop interaction test. The 24 Oct product context and screenshots in the redesign plan are leads, not proof of current behavior.

## Evidence and screenshot mapping

The baseline PNGs below are local Roborazzi `@Preview` renders last recorded on 27 Oct. They are fixtures, sometimes synthetic and sometimes dark themed. They do not prove the current installed app's state, scroll behavior, or Android/Desktop runtime behavior. The plan's older user screenshots cannot be mapped to a specific current commit from the repository alone.

| User journey / navigation level | Current screen | Baseline preview |
|---|---|---|
| Level 1 discovery | `doors/DoorsScreen.kt` | `shared/src/androidHostTest/snapshots/io.github.vietdsk6.porta.presentation.screens.doors.DoorsScreenKt_DoorsScreenPreview.png` |
| Level 1 planning | `frontier/FrontierScreen.kt` (label **Next moves**) | `shared/src/androidHostTest/snapshots/io.github.vietdsk6.porta.presentation.screens.frontier.FrontierScreenKt_FrontierScreenPreview.png` |
| Level 1 profile | `studentprofile/StudentProfileScreen.kt` | `shared/src/androidHostTest/snapshots/io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileScreenKt_StudentProfileScreenPreview.png` |
| Level 2 from Doors | `opportunitydetail/OpportunityDetailScreen.kt` | `shared/src/androidHostTest/snapshots/io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailScreenKt_OpportunityDetailScreenPreview.png` |
| Level 2 from Next moves or Details | `scenariopreview/ScenarioPreviewScreen.kt` | `shared/src/androidHostTest/snapshots/io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewScreenKt_ScenarioPreviewScreenPreview.png` |
| Level 2 from Profile or contextual prompt | `profilesetup/ProfileSetupScreen.kt` | `shared/src/androidHostTest/snapshots/io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupScreenKt_ProfileSetupScreenPreview.png` |
| Level 2 from Next moves; sometimes deeper after What-if | `activeplan/ActivePlanScreen.kt` | `shared/src/androidHostTest/snapshots/io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanScreenKt_ActivePlanScreenPreview.png` |
| Level 3 after Active plan | `actualprofileupdate/ActualProfileUpdateScreen.kt` | `shared/src/androidHostTest/snapshots/io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateScreenKt_ActualProfileUpdateScreenPreview.png` |
| Separate first-run flow | `onboarding/OnBoardingScreenVariation1.kt` | `shared/src/androidHostTest/snapshots/io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingScreenVariation1Kt_ScenarioStepPreview.png` and `shared/src/androidHostTest/snapshots/io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingScreenVariation1Kt_ResultStepPreview.png` |

Additional baseline states exist for Doors saved-empty, Profile collapsed/expanded/data controls, Details Desktop/accessibility, Profile Setup requested fact/accessibility, Frontier Desktop and onboarding steps. There is no dedicated preview for `packinspector/PackInspectorScreen.kt` in the recorded PNG list. Capture fresh light/dark Android and Desktop screenshots as part of each changed wave; do not call these old PNGs “after” screenshots.

## Navigation map

`presentation/navigation/Routes.kt` uses stable serialized route names. `AppNavigation.kt` starts at Onboarding and swaps among three top-level roots: **Doors**, **Next moves** (`FrontierScreenRoute`, serialized as `Frontier`) and **Profile**. Top-level navigation is a bottom bar on compact windows and rail on wide windows; pushed screens hide it. The actual depth depends on entry path:

```text
Onboarding -> Doors or ProfileSetup -> Doors
Doors -> OpportunityDetail -> ScenarioPreview -> ActivePlan -> ActualProfileUpdate
                         \-> ProfileSetup
Next moves -> ScenarioPreview -> ActivePlan -> ActualProfileUpdate
           \-> ActivePlan
Profile -> ProfileSetup
        \-> PackInspector
```

`ScenarioPreview` replaces itself with `ActivePlan` after saving; `ActivePlan` can replace itself with `ScenarioPreview`. There is currently no separate Data & privacy or source/evidence detail route. Source disclosure lives inside Details; data controls live inside Profile. Saved, no-filter-results and no-action states are branches of the existing screens, not routes. The Level numbers in the master plan describe common navigation depth, not a fixed property of a route or a priority.

## Verified semantics and discrepancies

| Area | Current implementation | W0 conclusion / change gate |
|---|---|---|
| Rule assessment | `EligibilityEvaluator` preserves `PASS`, `FAIL`, `UNKNOWN`, `REVIEW`. Manual checks return `REVIEW`; missing facts and incompatible comparison return `UNKNOWN`. Archived/closed cycle has precedence. Root pass with TBA/missing deadline yields `NEEDS_INFO`, not `OPEN`. | Keep rule result and timing as distinct UI facts. `OPEN` is internally named; display says `Assessed rules passed`, which is narrower than admission/eligibility. |
| Status vocabulary | Current `EligibilityTrustCopy` labels `NEEDS_INFO` as “Needs information”; PRD trust-first text says “Needs verification”, while the redesign plan uses both in examples. | T02 must choose a consistent, contextual display label across Doors, Details, What-if and Plan. Preserve the underlying enum and distinguish missing user fact from manual source review. |
| Source freshness | `OpportunityLifecycle.sourceFreshness()` uses a 90-day Porta review window, cycle-start check and future-date check. `SourceFreshnessCopy` says source *reviewed*, not provider-verified. | Preserve reviewed date and official URL while shortening repeated list copy. A review date does not establish live deadline validity. |
| Scenario impact | `ScenarioEvaluator.impacts` contains opportunities with at least one **rule-result** change. `deltas` contains overall status changes only. `ScenarioPreviewViewModel` lists `impacts`, including cases where the overall state remains `NEEDS_INFO`, and splits them into current-checks-passed vs progress. | “Affected” is check-level, not necessarily a newly open opportunity. A single projection must drive hero, graph and rows in W2. |
| Frontier impact | `ActionFrontierEngine` starts with `resolvedHardGapOpportunityIds(target)`, then removes opportunities whose known deadline cannot fit the action's minimum duration. It also filters by horizon, effort, cost and Pareto dominance. Scope-specific targets remain keyed by opportunity/rule/fingerprint. | A hypothetical 5 affected and an in-plan 4 can legitimately differ by deadline feasibility; the same action/profile/date must be reproduced before calling the observed screenshots a bug. UI must label candidate vs feasible counts. |
| Estimate | Curated pack templates include a `180` day degree milestone (`PortaContestPackData.kt`), while availability confirmations are 14 days. The duration is a Porta assumption in source notes, not a provider requirement. | Do not alter dates or invent a range. Show assumption and timing caution when the selected action's estimate exceeds a published deadline. Unknown duration needs an explicit unknown state in future model work. |
| Profile count | `StudentProfileViewModel` counts provided facts; `ProfileIdentity()` computes its “doors” pill as the union of opportunity **titles whose atomic rules read one of those provided facts**. It is not the total catalog or number of evaluated opportunities. A 13/15 display can therefore be internally consistent but the label is misleading. | Rename to “opportunities using your provided facts” or omit until a typed count projection is shared. Profile's `No doors` badge means zero encoded dependencies in the installed pack, not that the fact has no real-world value. |
| Sample vs real | Empty saved profile falls back to `PortaSampleData.profile`; active pack normally comes from the bundled 15-record pack. Onboarding IELTS thresholds are explicitly fictional in current copy. | Keep profile mode and catalog mode separate in labels/tests. Do not infer real-pack IELTS reach from onboarding. |
| Plan vs actual | `ActivePlanViewModel` changes only `PlanStatus`; `ActualProfileUpdateViewModel` writes the achieved value to `ProfileRepository` or `RuleAssertionRepository` only after explicit Confirm. Scoped assertions are checked against opportunity, rule and fingerprint by `EligibilityEvaluator`. | Core isolation exists. Confirmed actual is not a distinct `PlanStatus`; after saving, navigation returns to Doors. The plan status/actual-result presentation needs a truthful model. |
| **Actual Update prefill — confirmed issue** | `FormState.from(plan)` preloads the target into `achievedBoolean`, numeric `achievedValue`, and text `achievedValue`. `canConfirm` can then become true without user choosing the actual result (provided required evidence is supplied). The UI copy explicitly says the target is prefilled. | This is a real trust defect, not merely an old screenshot interpretation. Before UI polish, require an explicit achieved-value choice/edit and test target Yes → actual No. |
| Evidence validation — confirmed issue | For scoped actions `includeEvidence` defaults true, and `EvidenceCard` sets `isError = evidenceLabel.isBlank()` immediately. Evidence label is required; reference is optional. | Required marker is present, but red error appears before touch/submit. Change to touched/submitted validation. An evidence label is self-attested metadata, not provider verification. |
| **Plan progress — confirmed issue** | `completedSteps = 1 + hasStarted + isComplete`; `progress = completedSteps / 3f`, so a freshly saved `PLANNED` item renders 1/3 fill. | Replace percentage bar with discrete states; saving a plan is not 33% of the work. |
| Raw IDs — confirmed issue | Scoped Actual Update's `scopeDescription` concatenates `opportunityId` and `ruleId` directly into user UI. | Resolve title and short requirement label from current pack; keep raw IDs for diagnostics. Handle missing/stale pack safely. |
| Persistence/network | Android uses local Room; JVM Desktop database path is under `user.home/.io.github.vietdsk6.porta/`, with one-time copy from a legacy temp path. `OPTIONAL_ONLINE_SERVICES_ENABLED = false` and `PREMIUM_FEATURES_ENABLED = false` gate inherited startup services. Platform Firebase bindings still exist and Android contains Firebase configuration. | Static trace supports offline-capable core and durable Desktop path. It does **not** prove a cold launch makes zero network requests or that migration/restart succeeds on user machines. Test offline cold start and restart separately in T19. |

The bundled pack has 15 source-linked records in `PortaContestPackData.kt`. The older 24 Oct context claims no real `OPEN` result, but the pack has since gained deeper typed rules; do not repeat that claim without a current evaluator fixture. The current preview PNGs mix synthetic and curated data and cannot establish corpus-wide status totals.

## Source inventory

The route graph was inspected in `presentation/navigation/Routes.kt` and `AppNavigation.kt`. Display state and flow were traced through `DoorsViewModel`, `FrontierViewModel`, `StudentProfileViewModel`, `OpportunityDetailViewModel`, `ScenarioPreviewViewModel`, `ActivePlanViewModel`, `ActualProfileUpdateViewModel` and their matching `*UiState.kt`/`*Screen.kt` files under `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/`. The remaining routes were checked in `profilesetup/`, `packinspector/` and `onboarding/`. Domain/data evidence came from `EligibilityEvaluator`, `ScenarioEvaluator`, `ActionFrontierEngine`, `ActionFrontier.kt`, `PlanItem.kt`, `Opportunity.kt`, `OpportunityLifecycle.kt`, `PortaContestPackData.kt`, `ProfileRepository`, `PlanRepository`, `RuleAssertionRepository`, `AppInitializer`, `AppConfiguration`, the Android platform module and JVM `DesktopStoragePaths`/`DatabaseProviderImpl`. Existing tests and Roborazzi preview files were inventoried but not expanded into a new full test matrix during W0.

## Duplication and current screen surfaces

- Doors: a header, explanatory paragraph, status summary and wrapping filters precede the first card. Cards still show a long reason and both bookmark state and `SAVED` text in the preview. `DoorsViewModel` already reacts to profile/pack/date flows and highlights changed conclusions once.
- Next moves: recommendation first and grouped action families already exist; graph is secondary on mobile and prominent on Desktop. Current action copy still exposes estimate chips without units in the card preview, and a pending completed plan has no special top priority in `FrontierViewModel`/screen.
- Profile: personalized identity and progressive sections already exist. `No doors` badges and per-fact provenance repeat. Data controls are an inline section with confirmation dialogs.
- Details: result, rule notes and source link are present; the large app-bar headline and long default explanations still create two headline levels and repeated reading.
- What-if: simulation banner and rule-level progress exist, but default impact path plus full result cards repeat each affected opportunity. No shared compact result projection yet.
- Edit profile: optional groups and GPA scale/country selectors already exist; revise specific copy/focus/validation only after checking current behavior.
- Active Plan: one active item and explicit status actions exist, with the misleading percent bar described above.
- Pack Inspector: source/provenance and import preview exist; keep its diagnostics while simplifying entry paths. Onboarding already says fictional/sample in several steps.

## Baseline gates and unverified interaction checks

| Command | Outcome on 28 Oct 2026 |
|---|---|
| `./gradlew spotlessCheck` | PASS (`BUILD SUCCESSFUL in 14s`) |
| `./gradlew :shared:jvmTest :shared:testAndroidHostTest` | PASS (`BUILD SUCCESSFUL in 1s`; both test tasks up to date) |
| `./gradlew :androidApp:assembleDebug` | PASS (`BUILD SUCCESSFUL in 4s`) |

No Desktop window, Android emulator interaction, offline cold start, keyboard/focus pass, font-scale pass, or live import round-trip was run in W0. Current Roborazzi previews are baseline references only. Those checks belong to the relevant implementation waves and T17/T19.

**W0 gate:** the critical semantic ambiguities are recorded. The confirmed trust defects are target prefill, early evidence error, plan percentage and raw scoped IDs. They are the first corrective slice before broader visual changes.
