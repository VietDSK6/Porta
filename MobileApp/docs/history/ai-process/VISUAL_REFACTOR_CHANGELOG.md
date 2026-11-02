# Porta visual refactor changelog

> Historical implementation log. Retained for provenance; the shipped UI and current scorecard are authoritative.

Status: implemented and validated
Baseline: `b530354 feat: make core journey trust-first`

This log records the selective Wave V2 implementation and the validation completed in this checkpoint. It does not claim physical-device, screen-reader or exhaustive responsive QA that was not performed.

## Baseline already complete

Commit `b530354` completed Wave V1 hierarchy and trust work across the core journey. The current working tree builds selectively on that baseline rather than reopening its domain decisions.

## Wave V2 work currently implemented

### Reusable visual primitives

- Added `PortaArch`, a geometry-only arch container whose callers supply status text and semantics.
- Added `PortaConnectorPath` and connector positions for single or multi-target paths.
- Kept the primitives Compose-native and free of eligibility calculations or external assets.

Files:

- `designsystem/src/commonMain/kotlin/io/github/vietdsk6/porta/designsystem/components/porta/PortaVisualPrimitives.kt`

### Action impact diagram

- Added `ActionImpactDiagram` with explicit node IDs, titles, transition labels and typed outcomes.
- Added separate visual outcomes for current checks passed, checked progress and needs verification.
- Kept visible action/count labels and added per-node state descriptions without duplicating the entire diagram in one accessibility description.
- Added a restrained single-path label when exactly one opportunity is affected.

Files:

- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/components/porta/ActionImpactDiagram.kt`

### What-if / Scenario Preview

- Added `ScenarioDoorOutcome` to the UI projection.
- Derived passed/progress/verification outcomes from the simulated eligibility result; timing uncertainty remains a separate warning.
- Inserted the data-driven action impact path when changed opportunities exist.
- Moved `What changes` and `What remains` into the default per-door view.
- Kept technical result transition and planning timing behind `Review evidence details`.
- Retained the persistent `Simulation — your saved profile is unchanged` boundary and the plain before/after fallback when no door changes.

Files:

- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/scenariopreview/ScenarioPreviewScreen.kt`
- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/scenariopreview/ScenarioPreviewUiState.kt`
- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/scenariopreview/ScenarioPreviewViewModel.kt`
- `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/ScenarioPreviewProgressiveDisclosureUiTest.kt`
- `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/PortaVerticalSliceUiTest.kt`

### Onboarding threshold doors

- Replaced four anonymous IELTS impact dots with four mini `PortaArch` threshold nodes.
- Bound each node to the existing synthetic `OnboardingDemoDoorUiState`.
- Added semantic state descriptions that distinguish `needs IELTS …` from `IELTS check met; other criteria not assessed`.
- Reused `PortaArch` for the larger Step 3 fictional opportunity cards.
- Added UI assertions for the four threshold node IDs.

Files:

- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/onboarding/OnBoardingScreenVariation1.kt`
- `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/OnboardingUiTest.kt`

### Doors empty states and save affordance

- Added distinct empty copy for Saved, other filters and an empty pack.
- Added a neutral arch marker and a `Show all opportunities` reset action where applicable.
- Wired reset to `FilterChanged(ALL)`.
- Increased the independent save control to a 48dp touch target.
- Added a UI test for the Saved empty state and filter reset.
- Refined summary copy from a generic “next action” to the trust-safe “need action.”

Files:

- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/doors/DoorsScreen.kt`
- `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/PortaVerticalSliceUiTest.kt`

### Semantic color cleanup

- Replaced feature-local fixed success/warning/error colors with `AppTheme.colors.status` in the touched Doors, detail, Next moves, What-if and Active Plan surfaces.
- Reused the existing dark/light Editorial Atlas tokens instead of adding a parallel palette.

Files currently touched:

- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/doors/DoorsScreen.kt`
- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/opportunitydetail/OpportunityDetailScreen.kt`
- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/frontier/FrontierScreen.kt`
- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/scenariopreview/ScenarioPreviewScreen.kt`
- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/activeplan/ActivePlanScreen.kt`

## Explicitly not implemented in this working tree

- Profile Personal Atlas empty-state art.
- Active Plan milestone/history illustration or durable plan history.
- Pack Inspector refresh illustration or record search.
- Full category glyph/provider identity system.
- Broad motion/reduced-motion refactor.
- Global card replacement or navigation redesign.

## Verification completed

- [x] `./gradlew spotlessCheck`
- [x] `./gradlew :shared:jvmTest :shared:testAndroidHostTest` — 196 tests passed
- [x] `./gradlew :androidApp:assembleDebug`
- [x] `./gradlew :shared:recordRoborazziAndroidHostTest`
- [x] Visual inspection of affected dark What-if/onboarding previews and the light Doors empty state
- [x] `./gradlew :desktopApp:packageDistributionForCurrentOS`
- [x] `./gradlew :webApp:wasmJsBrowserDistribution` — existing webpack size warnings only

Generated Desktop package:

- `desktopApp/build/compose/binaries/main/dmg/io.github.vietdsk6.porta-1.0.0.dmg`

## Verification still pending

- [ ] Physical Android interaction at compact widths and 200% font scale
- [ ] Desktop manual interaction at narrow/wide window sizes
- [ ] Screen-reader and keyboard traversal on real platform surfaces
- [ ] Manual interaction for every one-node and no-change scenario permutation

These items are deliberately left open; automated UI coverage and recorded previews do not substitute for platform assistive-technology or physical-device testing.
