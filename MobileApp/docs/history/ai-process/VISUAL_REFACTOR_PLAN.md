# Porta visual refactor plan

> Historical AI-assisted planning artifact. Retained for provenance; see the archive README and current scorecard.

Status: selective Wave V2 implemented and validated
Baseline commit: `b530354 feat: make core journey trust-first`
Design inputs: `PORTA_VISUAL_UX_ART_DIRECTION.md` and `PORTA_CODEX_VISUAL_REFINEMENT_PROMPT.md`

## 1. Baseline and decision record

Wave V1 is complete in commit `b530354`. It established the trust-first hierarchy that the visual work must preserve:

- compact Doors cards and accessible filters;
- grouped, progressively disclosed opportunity checks;
- recommendation-first Next moves;
- What-if copy that distinguishes simulation, checked progress, remaining checks and actual profile data;
- explicit boundaries between completing work, confirming evidence and updating the saved profile;
- source and timing language that does not turn a checked rule into an admission prediction.

Wave V2 is not a rebrand or a full-screen illustration pass. It selectively adds Porta's arch, threshold and path language where it explains real state. Normal cards, receipts, forms and technical diagnostics remain restrained.

## 2. Non-negotiable truth contract

Every V2 component must follow these rules:

- Visuals consume UI state produced by the existing evaluator/scenario pipeline; they do not calculate eligibility, affected counts or status.
- `UNKNOWN`, manual review, stale source and timing uncertainty never become visual `PASS` states.
- A rule-level improvement must look different from “current assessed checks passed.” Neither state means admission, selection or award.
- One affected opportunity renders as a compact cause-to-effect path. Multiple affected opportunities may render as a restrained ripple/path, using exactly the provided IDs.
- The onboarding demo remains explicitly fictional/synthetic.
- What-if always states that the saved profile is unchanged.
- Color supports meaning but never carries meaning alone. Every diagram exposes visible labels and per-node state semantics.
- No provider logo, external image, decorative stock art or unlicensed asset is introduced.
- Light and dark modes use `AppTheme` semantic tokens; feature screens must not add fixed light-only status colors.

## 3. Selective Wave V2 scope

### V2.1 Reusable Porta primitives

Build and stabilize two small primitives rather than a large illustration framework:

- `PortaArch`: Porta's opportunity/threshold marker. It owns only geometry and styling; the caller owns labels and semantics.
- `PortaConnectorPath`: a static path segment for one source connected to one or more computed targets.

Target:

- `designsystem/src/commonMain/kotlin/io/github/vietdsk6/porta/designsystem/components/porta/PortaVisualPrimitives.kt`

Acceptance:

- scales without bitmap assets on all Compose Multiplatform targets;
- supports transparent and semantic-token containers;
- contains no eligibility logic and no built-in success claim;
- remains legible in light and dark themes.

### V2.2 Data-driven ActionImpactDiagram

Use a presentation component to express action → evaluated opportunity effects.

Targets:

- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/components/porta/ActionImpactDiagram.kt`
- consumers in `presentation/screens/scenariopreview/`;
- Next moves may reuse it only when it adds information and the same affected IDs/statuses are already available.

Required inputs:

- action label;
- actual and simulated values;
- exact opportunity IDs/titles;
- explicit outcome projection: current checks passed, checked progress, or needs verification;
- actual → simulated state label.

Acceptance:

- zero nodes renders no diagram;
- one node is labelled as one checked path, not a dramatic ripple;
- multiple nodes match the exact computed affected set;
- visible labels name the action and count, while every result exposes its own state semantics;
- the diagram never substitutes for the persistent simulation/profile-unchanged message.

### V2.3 Scenario/What-if signature moment

What-if is the primary V2 showcase. Its default order remains:

1. simulation boundary and impact summary;
2. data-driven action path;
3. per-door `What changes`;
4. per-door `What remains`;
5. evidence/timing detail on request;
6. save-plan/build-profile action.

Targets:

- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/scenariopreview/ScenarioPreviewScreen.kt`
- `ScenarioPreviewUiState.kt`
- `ScenarioPreviewViewModel.kt`

Acceptance:

- `ScenarioDoorOutcome` is derived from existing scenario results; timing uncertainty remains an orthogonal warning;
- exact before/after values remain visible without expanding technical detail;
- remaining blockers stay visible by default;
- raw result transition and timing evidence remain reachable in one action;
- no changed doors falls back to a plain before/after and neutral no-change state;
- save-plan behavior and sample-profile routing remain unchanged.

### V2.4 Onboarding threshold doors

Replace anonymous progress dots with four mini arches driven by `OnboardingDemoDoorUiState`.

Target:

- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/onboarding/OnBoardingScreenVariation1.kt`

The existing state already supplies `opportunityId`, `title`, `requiredIelts` and `isRequirementMet`; no new domain model is needed.

Acceptance:

- before the threshold, each mini arch says it needs the actual IELTS threshold;
- at the threshold, it says `IELTS check met; other criteria not assessed`;
- the demo remains labelled fictional;
- the full result arches reuse the same `PortaArch` geometry;
- 320dp width and 200% font scale keep the primary action reachable;
- no `eligible`, `accepted` or `unlocked` wording is introduced.

### V2.5 Doors empty states

Add a restrained arch/path empty state only when the filtered result list is empty.

Target:

- `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/doors/DoorsScreen.kt`

States:

- Saved: explain that saved opportunities will appear here and provide `Show all opportunities`.
- Other filters: explain that no item matches the current filter and provide the same reset action.
- Entire pack empty: explain how to activate/import a pack without implying a profile failure.

Acceptance:

- resetting emits `DoorsUiEvent.FilterChanged(DoorFilter.ALL)`;
- the empty state is neutral, not a red locked door;
- bookmark/save remains an independent ≥48dp action on populated cards;
- empty art contains no inferred recommendation or eligibility status.

### V2.6 Dark-theme semantic tokens

The design system already defines Editorial Atlas light/dark palettes in `designsystem/.../theme/Color.kt`. This wave uses those semantic tokens consistently instead of duplicating fixed teal/amber/coral constants in feature screens.

Targets include:

- Doors;
- Opportunity detail;
- Next moves/Frontier;
- What-if;
- Active Plan;
- new Porta primitives.

Acceptance:

- success/warning/error/container colors come from `AppTheme.colors.status`;
- text uses `AppTheme.colors.text` or the correct on-container token;
- no regression to cyan-on-dark, glow, gradients or light-only surfaces;
- light/dark screenshot review confirms readable outlines, connectors and arch interiors.

## 4. Deliberately deferred

The following are outside this selective V2 pass:

- a full icon/category/provider identity system;
- a reusable replacement for every existing card;
- Profile Personal Atlas illustration;
- Active Plan milestone/history illustration: the repository intentionally keeps one plan and currently deletes the previous plan on replacement;
- a fourth “actual update confirmed” plan marker until a durable plan-specific confirmation state exists;
- Pack Inspector refresh art; search/filter and diagnostics hierarchy have higher value than decoration;
- decorative art on ordinary cards, forms, source receipts or diagnostics;
- broad motion choreography, ambient loops, bounce or confetti;
- typography rebrand, navigation redesign, crawler/cloud/account/submission features.

V3 motion/adaptive work starts only after V2 truth and screenshot gates pass.

## 5. Verification and acceptance matrix

### Automated gates

Run only the repository's scoped gates after implementation is stable:

```bash
./gradlew spotlessCheck
./gradlew :shared:jvmTest :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
```

Do not use aggregate `check`, `build`, `clean build`, or routine iOS validation.

Required focused coverage:

- ActionImpactDiagram: zero, one and multiple nodes; semantic summary and exact node IDs.
- What-if: passed checks, checked progress, needs verification, no changed doors, profile-unchanged boundary.
- Onboarding: four synthetic threshold nodes before/after IELTS 7.0 and no acceptance claim.
- Doors: Saved empty and generic filter empty; reset to All; bookmark touch target.
- Existing trust tests: completing a plan does not mutate profile; scenario does not mutate profile; scoped confirmation does not leak across programmes.

### Visual/manual review

Capture and inspect, in both light and dark where applicable:

- What-if with one affected opportunity;
- What-if with four fictional affected opportunities;
- What-if where overall state does not fully pass;
- onboarding before and at the IELTS threshold;
- Saved empty and generic filter empty;
- populated Doors and detail after semantic color migration;
- Android compact 320/360dp and 200% font scale;
- Desktop resized narrow and wide.

Pass only when:

- no clipping, overlap or giant empty control appears;
- a user can identify impact, changed check and remaining uncertainty without expanding evidence;
- every visual result matches the underlying UI projection;
- static text remains sufficient with motion disabled;
- no screenshot suggests that a single met rule guarantees eligibility or selection.

## 6. Delivery sequence

1. Stabilize primitives and ActionImpactDiagram.
2. Complete Scenario projections and progressive disclosure tests.
3. Complete onboarding threshold visuals and tests.
4. Complete Doors empty states and tests.
5. Finish semantic-token sweep.
6. Run scoped gates once.
7. Record and inspect the affected Roborazzi previews, then package Desktop and compile the Wasm distribution.
8. Fix only evidence-backed regressions, then commit V2 separately from the V1 baseline.
