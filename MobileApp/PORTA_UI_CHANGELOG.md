# Porta UI changelog

## 2026-10-28 — W0 baseline and decisions

**Status:** verification and implementation planning only. No Compose screen, ViewModel, domain model, database, source pack or navigation code changed in this wave.

### Baseline

- Current top-level tabs are Doors, Next moves and Profile; Level 2/3 depth varies with the route path.
- Roborazzi preview PNGs recorded on 27 Oct are the available visual baseline. They are fixture renders, not fresh live Android/Desktop screenshots. Exact paths and screen mapping are in `PORTA_CURRENT_STATE_VERIFY.md`.
- `./gradlew spotlessCheck` — PASS, 28 Oct.
- `./gradlew :shared:jvmTest :shared:testAndroidHostTest` — PASS, 28 Oct; both test tasks were up to date.
- `./gradlew :androidApp:assembleDebug` — PASS, 28 Oct.

### Confirmed findings

- Actual Update preselects a plan target as the achieved value; scoped evidence label shows an error before interaction.
- A newly saved plan receives one-third visual progress although no work has started.
- Profile's “doors” pill counts opportunities whose encoded rules use provided facts, not the full catalog. Zero dependencies are displayed as “No doors.”
- Scoped Actual Update exposes raw opportunity and rule IDs.
- Scenario affected IDs and Active Plan feasible IDs have different filters; a 5 vs 4 screenshot discrepancy needs a same-input reproduction before being called a bug.

### Decision record

- Follow the complete redesign plan in small slices, preserving source receipts, manual/unknown semantics, hypothetical vs actual separation and programme-scoped assertions.
- Correct the confirmed actual-result and progress-truth defects before visual simplification. Then do Doors→Details and Next moves→What-if as reviewable slices.
- Do not treat the 24 Oct context or older screenshots as proof of current runtime behavior.

### Open verification

- No Android emulator/physical device interaction, JVM Desktop window, offline cold start, restart persistence, large-font/keyboard pass or fresh post-change screenshot was run in W0.
- W1/W2 work has not started; this changelog will gain before/after behavior, changed files, tests and screenshot references per completed slice.

## 2026-10-28 — First post-W0 trust correction (T10 + T09 core)

### Changed behavior

- **Actual profile update:** opening the confirmation screen now leaves the achieved result blank/unselected. The planned target stays visible only as a reference. A save requires an explicitly entered result; a programme-scoped result also requires an evidence label. Optional evidence no longer shows an error before interaction. The screen states that evidence is self-reported and not independently verified by Porta.
- **Programme scope:** the confirmation screen names the programme and requirement instead of showing raw IDs. If the current pack no longer contains the matching rule fingerprint, confirmation is blocked, including a second check immediately before saving.
- **Active plan:** replaced the synthetic one-third/two-thirds progress bar with discrete plan status and a separate actual-result step. Marking work complete still does not edit the profile. Time is shown in days with its assumption basis; effort and cost are explicitly labeled. Removing a plan requires confirmation.

### Files and evidence

- Implementation: `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/activeplan/` and `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/actualprofileupdate/`.
- Tests: `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/ActivePlan*Test.kt` and `ActualProfileUpdate*Test.kt` (16 focused JVM cases, all passed).
- Before baseline: `/private/tmp/porta-w0-before/ActivePlan.png` and `/private/tmp/porta-w0-before/ActualUpdate.png`.
- Fresh post-change Roborazzi previews: `shared/src/androidHostTest/snapshots/io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanScreenKt_ActivePlanPlannedPreview.png`, `ActivePlanScreenPreview.png`, and `shared/src/androidHostTest/snapshots/io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateScreenKt_ActualProfileUpdateScreenPreview.png`. These are fixture renders, not emulator interaction.

### Validation and remaining work

- `./gradlew :shared:jvmTest :shared:testAndroidHostTest` — PASS after correcting a new UI-test generic assertion type.
- `./gradlew :androidApp:assembleDebug` — PASS.
- `./gradlew :shared:recordRoborazziAndroidHostTest` — PASS; planned and in-progress Active Plan plus Actual Update previews visually inspected in light/dark fixtures.
- Actual-result confirmation is not yet represented as a persisted completed step on the Active Plan timeline. That needs the explicit state/migration work scoped for T09 W3; the current UI does not claim it is complete. Live device interaction, desktop manual interaction and large-font checks remain open.

## 2026-10-28 — W1A Doors → Details (T02, T03, T06)

### Changed behavior

- Doors and Details now derive their headline and concise next reason from the same `EligibilityReceipt.displayCopy()` projection. It keeps application timing separate from assessed requirements and never treats a failed alternative in an `ANY` rule as the next step when the root check passes.
- Doors removes the large count panel and repeated explanation, keeps one compact catalog/status line, uses All/Action/Verify/Saved primary filters, and keeps other states under More filters. Cards retain title, provider/category, assessment, a short reason, deadline, source-review warning when needed and one bookmark control. The duplicate Saved label is gone.
- Details uses a small app-bar label with the opportunity name as hero. It groups requirements by action, missing information, manual source review and passed checks. When the root `ANY` check passes, failed sibling routes appear as alternatives instead of `Needs action`. Rows expose short meaning first; expanding a row retains the original explanation, actual/required values where applicable and source note. Manual review never presents `Not provided` as an editable user fact. Official source access, section, last-check date, disclaimer and non-scoring factors remain available through the Evidence & sources area. The action CTA says `Preview impact`.

### Files and evidence

- Shared copy: `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/components/EligibilityTrustCopy.kt`.
- Doors: `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/doors/DoorsScreen.kt`, `DoorsViewModel.kt`.
- Details: `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/opportunitydetail/OpportunityDetailScreen.kt`, `OpportunityDetailUiState.kt`, `OpportunityDetailViewModel.kt`.
- Tests: `shared/src/commonTest/kotlin/io/github/vietdsk6/porta/presentation/components/EligibilityTrustCopyTest.kt`, `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/OpportunityDetailProgressiveDisclosureUiTest.kt`, `PortaVerticalSliceUiTest.kt`.
- Before: `/private/tmp/porta-w1a-before/`. After: Roborazzi files under `shared/src/androidHostTest/snapshots/` with `DoorsScreenKt_` and `OpportunityDetailScreenKt_` prefixes. Inspected mobile dark, 320dp/200% font, and desktop preview fixtures. Previews are not live device recordings.

### Validation and open items

- `spotlessCheck`, `:shared:jvmTest :shared:testAndroidHostTest`, `:androidApp:assembleDebug` — PASS after a fixture assertion and a lazy-list UI test were corrected. Roborazzi record — PASS. A combined gate invocation once hit a transient generated KSP directory read error; the three scoped gates passed when run sequentially after inspecting the path. The final `ANY` branch test passed.
- W1A uses the existing source URL and receipt, without changing evaluator rules or pack contents. Full screenshot/live interaction for Android, desktop keyboard navigation, and source-opening behavior on device remain W5 verification. Next slice is the shared What-if projection before Next moves layout changes.

## 2026-10-28 — T07 What-if projection foundation

- `ScenarioPreviewUiState` now owns one ordered `affectedDoors` list keyed by opportunity ID. The `openedDoors` and `progressedDoors` views are computed from its outcomes; the path diagram and displayed rows consume the same list. Scenario evaluation still leaves saved profile facts unchanged.
- Every affected row now carries changed-rule source notes, official source URL/section and source check date for the forthcoming compact disclosure. The existing screen remains visually unchanged in this step; T08 will replace its repeated full cards and move the mobile graph behind a disclosure.
- Changed `ScenarioPreviewUiState.kt`, `ScenarioPreviewViewModel.kt`, `ScenarioPreviewScreen.kt` and their JVM fixtures/tests. Scoped and cross-opportunity tests assert unique affected IDs, deadline warnings, source provenance and opportunity isolation.
- `spotlessCheck`, JVM/Android host tests and `assembleDebug` — PASS. No new screenshot was claimed for this data-contract-only step.

## 2026-10-28 — T08 Scalable What-if screen

### Changed behavior

- Mobile now leads with the simulation notice, one outcome summary, the hypothetical fact change, and compact affected-opportunity rows. The ripple diagram is optional. Opening one result closes the previous detail; exact changed checks, remaining blockers, timing, source notes, source section and official URL remain reachable through disclosure.
- Desktop retains a prominent diagram beside an independent results list and selected-detail pane. Both views consume T07's ordered `affectedDoors` projection. The diagram caps at five nodes and points to the remaining rows; the list remains complete.
- Status copy distinguishes a check that would pass from one changed requirement that still leaves other checks or source review. Timing concerns are named before adding a milestone. The scenario banner continues to state that the saved profile is unchanged.
- The preview fixture now includes three actual check transitions, so its summary count matches its displayed results. Added separate light and dark previews.

### Files and evidence

- Implementation: `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/scenariopreview/ScenarioPreviewScreen.kt`.
- Tests: `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/ScenarioPreviewScalabilityUiTest.kt`, `ScenarioPreviewProgressiveDisclosureUiTest.kt`, and `PortaVerticalSliceUiTest.kt`.
- Before: `/private/tmp/porta-t08-before/ScenarioPreview.png`. After: `shared/src/androidHostTest/snapshots/io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewScreenKt_ScenarioPreviewScreenLightPreview.png` and `ScenarioPreviewScreenKt_ScenarioPreviewScreenPreview.png`. Both after images were visually inspected; these are fixture snapshots, not live-device captures.

### Validation and remaining work

- `./gradlew spotlessCheck`, `./gradlew :shared:jvmTest :shared:testAndroidHostTest`, and `./gradlew :androidApp:assembleDebug` — PASS on the final T08 code. Roborazzi record — PASS.
- JVM UI coverage includes zero, one, five and twelve affected results; 320dp at 200% font; multiple deadline concerns; mobile selected-detail behavior; reduced motion; and the desktop official-source callback. Manual Android/Desktop interaction and keyboard traversal remain W5 checks.
- Next slice: T04/T14 Next moves recommendation and count clarity, using the T07 projection without changing evaluator semantics.

## 2026-10-29 — T04/T14 Next moves and Active Plan count clarity

### Changed behavior

- Next moves places an in-progress, paused or work-complete plan ahead of suggestions. A planned milestone stays after the recommendation. The plan button names the next step; work-complete opens the plan to confirm the actual result.
- Programme-scoped verification actions use a user-facing title such as `Confirm your availability for CERN Technical Studentship`, while retaining their distinct action IDs and scenario targets. Alternative rows show the programme name under the shared action family.
- Recommendation impact now counts assessed rule transitions from the scenario receipts for the opportunities retained by planning limits. The displayed opportunity count is separate from the number of checks that could change. Missing/TBA deadlines get an explicit caution; a check change is never labeled admission or acceptance.
- Time, effort and cost have visible labels. If the duration basis is not stated, the UI calls the number a planner assumption; the basis remains available under `Why this move?`. The mobile metric layout uses a full-width time row when three columns would wrap.
- Active Plan keeps candidate check changes, known-deadline fits, timing-unverified opportunities and known misses as separate sets. Its hero highlights a material missed-deadline concern, and the impact section distinguishes candidates from opportunities not ruled out by known deadlines. Detailed timing and planning caveats stay expandable.

### Files and evidence

- Next moves: `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/frontier/FrontierScreen.kt`, `FrontierUiState.kt`, `FrontierViewModel.kt`; tests in `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/FrontierPriorityUiTest.kt` and `PersistedProfileViewModelTest.kt`.
- Active Plan: `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/activeplan/ActivePlanScreen.kt`, `ActivePlanUiState.kt`, `ActivePlanViewModel.kt`; tests in `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/ActivePlanUiTest.kt` and `ActivePlanViewModelTest.kt`.
- Before previews: `/private/tmp/porta-t04-t14-before/`. After previews: Roborazzi PNGs under `shared/src/androidHostTest/snapshots/` with `FrontierScreenKt_` and `ActivePlanScreenKt_` prefixes. Mobile Next moves and planned/in-progress Active Plan fixture images were inspected; these are not live-device recordings.

### Validation and remaining work

- `./gradlew spotlessCheck`, `./gradlew :shared:jvmTest :shared:testAndroidHostTest`, and `./gradlew :androidApp:assembleDebug` — PASS on the final code. Roborazzi record — PASS.
- Focused tests cover work-complete/plan-priority ordering, plan navigation, explicit check counts and estimate basis, 5 candidate vs 4 not ruled out by known deadlines, unknown duration, and missed-deadline disclosure. Existing Frontier tests continue to cover verify-only suggestions, grouped scoped IDs, saved weighting and graph behavior.
- Desktop and Android manual interaction, keyboard traversal and end-to-end restart behavior remain W5 verification. The Action Ripple visual remains T12/T13; this slice preserves the existing desktop graph and mobile trade-off disclosure.

## 2026-10-29 — T05/T11/T16 Profile, Edit profile and Data & privacy

### Changed behavior

- Profile now names provided details separately from the number of distinct catalog opportunities whose typed rules reference those details. It suppresses a zero-use badge and explains the current-pack scope only when a fact is expanded. Missing details remain unknown, never failed.
- Edit profile starts with essentials and collapses optional academic, experience and context groups with provided counts. Completed groups say `Review`, empty groups say `Add`, and groups with invalid entered values say `Fix`. GPA keeps its original 4.0/10.0 scale; optional project and leadership evidence do not auto-pass hard rules.
- Saving an edit opened from Profile returns there with a dismissible local-save confirmation. It does not claim external verification.
- Date of birth uses a calendar picker with a clear action and disallows future selection. Nationality and residency use a searchable ISO country dialog with all matching results and a `Not provided` choice. Numeric inputs request matching keyboards; study-level and boolean choices wrap at narrow widths. The form links to Data & privacy.
- Profile has one concise Data & privacy entry. The nested route holds opportunity-pack inspection, profile metadata export, and two clearly scoped destructive actions. Both clear and reset still require an explicit confirmation; the export states that evidence files are excluded.

### Files and evidence

- Profile: `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/studentprofile/StudentProfileScreen.kt`, `StudentProfileUiState.kt`, `StudentProfileViewModel.kt`; `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/StudentProfileUiTest.kt` and `StudentProfileProjectionTest.kt`.
- Form: `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/profilesetup/ProfileSetupScreen.kt`, `ProfileSetupUiState.kt`, `ProfileSetupViewModel.kt`; `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/ProfileSetupUiTest.kt`.
- Privacy: new `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/dataprivacy/DataPrivacyScreen.kt`, `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/DataPrivacyUiTest.kt`, plus `presentation/navigation/Routes.kt`, `AppNavigation.kt`, and `shared/src/commonTest/kotlin/io/github/vietdsk6/porta/presentation/navigation/NavigatorTest.kt`.
- Roborazzi preview images for `StudentProfileScreenKt_StudentProfileScreenPreview.png`, `ProfileSetupScreenKt_ProfileSetupScreenPreview.png`, and `DataPrivacyScreenKt_DataPrivacyScreenPreview.png` under `shared/src/androidHostTest/snapshots/` were visually inspected. The form preview exposed a completed-section `Add` label, which was corrected and rendered again. These images use fixtures, not a live device.

### Validation and remaining work

- `./gradlew spotlessCheck`, `./gradlew :shared:jvmTest :shared:testAndroidHostTest`, and `./gradlew :androidApp:assembleDebug` — PASS on the final code. Roborazzi record — PASS; `git diff --check` — PASS.
- Focused tests cover 0/13/15 referenced-opportunity counts, country search and ISO code selection, date picker confirmation/clear/future guard, 320dp with 200% text, optional GPA scale behavior, the post-save notice, privacy navigation, export availability and destructive confirmation copy.
- Live Android/Desktop interaction, on-screen keyboard behavior on a device, and restart/persistence remain W5 checks. T12/T13 visual primitives and Action Ripple are the next UI slice.
