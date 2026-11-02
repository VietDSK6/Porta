# Porta UX redesign — implementation plan

> Historical AI-assisted planning artifact. Retained for provenance; see the archive README and current scorecard.

**Prepared:** 28 Oct 2026 from `PORTA_COMPLETE_UX_REDESIGN_PLAN_2026-10-28.md` and current-source W0 verification. See `PORTA_CURRENT_STATE_VERIFY.md` for evidence. This file plans work; it does not claim the UI changes below are complete. Preserve the existing editorial palette, Newsreader/Manrope typography, serialized routes and evaluator semantics. Android and JVM Desktop share state contracts but may use different layouts.

**Progress, 2 Nov:** W0, the early T09/T10 trust correction, W1A (T02/T03/T06 Doors → Details), T07/T08 What-if, T04/T14 Next moves/Active Plan count clarity, T05/T11/T16 Profile → Edit → Data & privacy, T12/T13 semantic visual primitives → Action Ripple, T15 fictional onboarding clarity, and T18 selective source-pack refresh are complete in the working tree. Live platform interaction remains a W5 check. Next are local startup/persistence (T19) and cross-platform release QA (T17). See `PORTA_UI_CHANGELOG.md` for exact evidence and gate results.

## Execution order and gates

1. **W0 complete:** T01 source/preview verification and the three scoped baseline gates. Before visual work, make a **small trust correction** for the confirmed T10/T09 defects: no preselected achieved result, no premature evidence error, no plan-as-work percentage, and human scoped labels. This is a justified reorder of W3 tasks because the current UI can lead to an incorrect actual assertion.
2. **W1A — Doors → Details:** T02, T03, T06. Keep official URL and exact receipts one disclosure away; do not alter rule evaluation. A compact Level 1 card needs a reliable short check from the same assessment model as Details.
3. **W1B/W2B — Next moves → What-if:** T07, T08, T04, T14. Build one scenario projection before resizing the graph/list or rewriting impact counts; show feasible vs merely affected opportunities clearly. Test 0/1/5/12+ cases.
4. **W1C/W2C — Profile → Edit:** T05, T11, T16. Correct count labels and zero dependencies first, then streamline editing and move privacy controls to a reachable route.
5. **W4 and data/reliability tracks:** T12, T13, T15; T18 and T19 can progress separately after their source/behavior gates. T17 is a release gate across all slices, not a substitute for per-slice checks.

Every slice updates `PORTA_UI_CHANGELOG.md`, records exact changed files, captures new Android/desktop screenshots where possible, and runs `./gradlew spotlessCheck`, `./gradlew :shared:jvmTest :shared:testAndroidHostTest`, and `./gradlew :androidApp:assembleDebug` once after code changes. Shared-code compatibility may additionally require `:shared:compileKotlinJs :shared:compileKotlinWasmJs`; do not run aggregate `check`/`build` or routine iOS tasks. Do not auto-launch Android with adb. Desktop compile/package and manual interaction are required by W5, not claimed by W0.

## T01–T19 task contracts

### T01 — Verify current navigation, counts, sources and estimates — W0, done

- **Files inspected:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/navigation/Routes.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/navigation/AppNavigation.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/usecase/EligibilityEvaluator.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/usecase/ScenarioEvaluator.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/usecase/ActionFrontierEngine.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/pack/PortaContestPackData.kt`, and the ViewModels listed in `PORTA_CURRENT_STATE_VERIFY.md`.
- **Dependency / change:** none; produce the W0 matrix and baseline references without modifying app UI.
- **Projection / test:** distinguish catalog total, dependency count, check-level impact, feasible impact and overall state. Three scoped build gates passed; live runtime checks remain open.
- **Accept:** every old screenshot claim is marked confirmed, conditional, or unverified in `PORTA_CURRENT_STATE_VERIFY.md`.

### T02 — Central display status and short reasons — before T03/T06/T08

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/components/EligibilityTrustCopy.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/components/SourceFreshnessCopy.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/doors/DoorsViewModel.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/opportunitydetail/OpportunityDetailViewModel.kt`.
- **Change / projection:** derive one short result and timing/source qualifier from `EligibilityReceipt`; preserve separate rule `UNKNOWN`/`REVIEW` and cycle status. Expose a short reason without truncating original explanation.
- **Test / accept:** add focused common tests for missing fact, manual check, known/TBA deadline and archived cycle; Doors, Details and What-if must use equivalent wording for the same receipt.

### T03 — Compact Doors — after T02

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/doors/DoorsScreen.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/doors/DoorsUiState.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/doors/DoorsViewModel.kt`.
- **Change / projection:** shorten header, retain honest sample label, compact cards, one bookmark affordance, primary filters with secondary sheet/disclosure and visible deadline concern. Use the status projection from T02, no invented category/provider identity.
- **Test / accept:** Compose UI tests for All/Action/Info/Saved, empty state, long names, 320/360dp and font scale; first opportunity appears promptly, filters never clip, official detail remains reachable.

### T04 — Next moves priority and action naming — after T07/T08 projection, with T14

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/frontier/FrontierScreen.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/frontier/FrontierUiState.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/frontier/FrontierViewModel.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/frontier/FrontierControls.kt`.
- **Change / projection:** surface completed-pending-confirmation plan first; label verify-only moves honestly; name scoped confirmations by programme; group alternatives without merging scoped targets. Show exact feasible impact and estimate provenance.
- **Test / accept:** active-plan states, verify-only, unknown estimate, duplicate scoped action titles and saved weighting; user identifies action and check-level outcome in 5 seconds. Desktop graph remains available.

### T05 — Profile count and zero-dependency copy — W1C

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/studentprofile/StudentProfileViewModel.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/studentprofile/StudentProfileUiState.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/studentprofile/StudentProfileScreen.kt`.
- **Change / projection:** explicitly distinguish fact count, total catalog, and opportunities whose typed rules reference provided facts; remove `No doors` badge at zero, keep precise explanation on expansion. Do not infer IELTS is useless outside the current pack.
- **Test / accept:** fixtures with 0/13/15 dependent opportunities and missing facts; pill wording reflects its actual set, optional unknown never looks failed.

### T06 — Details checklist and source disclosure — after T02

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/opportunitydetail/OpportunityDetailScreen.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/opportunitydetail/OpportunityDetailUiState.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/opportunitydetail/OpportunityDetailViewModel.kt`.
- **Change / projection:** one opportunity hero, grouped met/action/info/manual rows, short default copy; expand exact `RuleReceipt` and source section/URL. Keep selection factors non-scoring.
- **Test / accept:** common/jvm UI cases for PASS/FAIL/UNKNOWN/REVIEW, TBA and archived; full source URL/receipt reachable, no manual review displayed as missing editable fact.

### T07 — One What-if projection — before T08/T04/T14

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/scenariopreview/ScenarioPreviewUiState.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/scenariopreview/ScenarioPreviewViewModel.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/domain/usecase/ScenarioEvaluator.kt` only if a true domain gap is demonstrated.
- **Change / projection:** one immutable ordered list keyed by opportunity ID with actual/simulated state, changed checks, remaining checks, timing feasibility and source receipt. Hero, compact path and rows must derive from that list, not each recalculate counts.
- **Test / accept:** 0/1/5/12+ impacts, unchanged overall state, multiple deadline warnings and scoped assertion isolation; count and row IDs match evaluator receipts, actual profile remains unchanged.

### T08 — Scalable What-if screen — after T07

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/scenariopreview/ScenarioPreviewScreen.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/scenariopreview/ScenarioPreviewUiState.kt`.
- **Change / projection:** compact action→affected list, maximum one expanded detail on mobile, Desktop selected detail panel, visible timing caution, evidence/source disclosure. Same T07 rows feed diagram and list.
- **Test / accept:** default height does not grow by a full card per impact; 0/1/5/12+ states and 200% font/keyboard read correctly; no “eligible/unlocked” claim from a single improved rule.

### T09 — Plan progress truth — early trust correction, then W3 polish

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/activeplan/ActivePlanViewModel.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/activeplan/ActivePlanUiState.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/activeplan/ActivePlanScreen.kt`.
- **Change / projection:** replace `completedSteps / 3f` progress with discrete planned/in-progress/paused/work-complete/actual-confirmed presentation; do not synthesize work percentage. Show duration, effort and cost with units/basis.
- **Test / accept:** tests for all supported `PlanStatus` values and profile non-mutation; freshly saved plan shows no completed work. Actual-confirmed state needs an explicit derived signal or new persisted state, with migration/test if introduced.

### T10 — Explicit Actual Update result and evidence validation — first post-W0 slice

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/actualprofileupdate/ActualProfileUpdateViewModel.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/actualprofileupdate/ActualProfileUpdateUiState.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/actualprofileupdate/ActualProfileUpdateScreen.kt`, `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/ActualProfileUpdateViewModelTest.kt`, `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/ActualProfileUpdateUiTest.kt`.
- **Change / projection:** leave achieved value unselected until explicit user input; resolve scoped opportunity/rule names from active pack; show evidence error only after touch/submit; preserve optional reference and self-attested status. Store user-selected achieved value, even if it differs from target.
- **Test / accept:** target Yes initially has neither Yes nor No selected and cannot confirm; choose No and save No only; no write on back/cancel; required label is marked but not red at first render; stale/missing scoped target gets safe human copy.

### T11 — Edit profile progressive form — after T05

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/profilesetup/ProfileSetupScreen.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/profilesetup/ProfileSetupUiState.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/profilesetup/ProfileSetupViewModel.kt`.
- **Change / projection:** retain existing optional groups, GPA original scale and country picker; shorten repeated explanation, show group completion counts only from provided fields, improve keyboard/focus and validation timing.
- **Test / accept:** existing `ProfileSetupUiTest.kt` plus missing/optional facts, GPA 4/10 and IELTS validation; save does not force project/leadership or convert scale.

### T12 — Shared semantic visual primitives — after stable T02/T07 contracts

- **Files:** new files under `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/components/` and existing `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/theme/` only where tokens are needed.
- **Change / projection:** small Compose Canvas/vector Arch, Threshold, Path and status glyph primitives. Every graphic receives text semantics from assessment data; no fake provider logo or decorative metric.
- **Test / accept:** light/dark and accessibility previews; no meaning depends on color or animation alone.

### T13 — Action Ripple — after T07 and T12

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/frontier/FrontierScreen.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/scenariopreview/ScenarioPreviewScreen.kt`, new shared ripple component under `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/components/`.
- **Change / projection:** draw only IDs in T07/Frontier engine projection; group beyond five nodes, show timing risk per node, provide static/reduced-motion variant.
- **Test / accept:** 1/5/12+ IDs and text summary agree; TalkBack and reduced-motion preserve all information.

### T14 — Active-plan affected count and risk — after T07/T09

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/activeplan/ActivePlanViewModel.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/activeplan/ActivePlanUiState.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/activeplan/ActivePlanScreen.kt`.
- **Change / projection:** show candidate check changes and in-plan deadline-feasible IDs as separately named sets when both matter; make material deadline warning visible, with rest of risk note expandable.
- **Test / accept:** fixture where 5 checks change but 1 misses a known deadline reports 5 candidate / 4 feasible, no invented opportunity impact.

### T15 — Fictional onboarding clarity — done in working tree

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/onboarding/OnBoardingScreenVariation1.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/onboarding/OnBoardingViewModel.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/onboarding/OnBoardingUiState.kt`.
- **Change / projection:** preserve the existing fictional IELTS computation and 4 sample requirements; add a small threshold visual and short sample-rule summary, separate sample exploration from real profile creation.
- **Test / accept:** `OnboardingUiTest.kt`; slider 6.5→7.0 changes four sample checks without saying four real applications became available.

### T16 — Data & privacy route — after T05, coordinate shared navigation

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/studentprofile/StudentProfileScreen.kt`, new screen files under `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/dataprivacy/`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/navigation/Routes.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/navigation/AppNavigation.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/root/Di.kt` only if a new ViewModel is needed.
- **Change / projection:** move existing export/clear/reset controls with their exact deletion scopes to a route reachable within two taps. Preserve current confirmation behavior and stable existing route serial names.
- **Test / accept:** navigation and destructive-action tests; no reset occurs without explicit confirmation, export describes metadata and evidence-file exclusion.

### T17 — Adaptive/accessibility/release QA — cross-wave, final gate

- **Files:** changed screens above; `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/presentation/` UI tests; `shared/src/androidHostTest/kotlin/io/github/vietdsk6/porta/screenshot/PreviewScreenshotTest.kt`; `PORTA_UI_CHANGELOG.md`.
- **Change / projection:** verify each wave at 320/360dp, 1.3×/1.5×/200% text, Desktop 600/900px, keyboard/focus, light/dark, reduced motion, offline restart; snapshots must match the actual state model.
- **Test / accept:** scoped Gradle gates, Desktop compile/package check and manual interaction pass, fresh screenshots, no clipped controls/focus traps or lost caveats. Record any unavailable manual device checks rather than claiming PASS.

### T18 — Real source pack and recommendation utility — done in working tree

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/pack/PortaContestPackData.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/data/source/local/pack/OpportunityPackValidator.kt`, `OPPORTUNITY_PACK_SOURCES.md`, `shared/src/commonTest/kotlin/io/github/vietdsk6/porta/data/source/local/pack/BundledOpportunityPackProviderTest.kt`, `shared/src/commonTest/kotlin/io/github/vietdsk6/porta/domain/usecase/ActionFrontierEngineTest.kt`.
- **Change / projection:** curate 3–5 genuinely deeper current-cycle opportunities and at least one truthful cross-door action only when official sources support the exact encoded rule/threshold. Do not fabricate OPEN cases, deadlines or effort estimates.
- **Test / accept:** source register cites section/date/cycle; parser/validator and evaluator fixtures pass; all ambiguous conditions remain manual and recommendation impact follows real encoded rules.

### T19 — Local startup and persistence — separate engineering track

- **Files:** `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/root/AppInitializer.kt`, `shared/src/commonMain/kotlin/io/github/vietdsk6/porta/root/AppConfiguration.kt`, `shared/src/jvmMain/kotlin/io/github/vietdsk6/porta/data/source/local/DesktopStoragePaths.kt`, `shared/src/jvmMain/kotlin/io/github/vietdsk6/porta/data/source/local/DatabaseProviderImpl.jvm.kt`, `shared/src/androidMain/kotlin/io/github/vietdsk6/porta/util/Platform.android.kt`, `shared/src/jvmTest/kotlin/io/github/vietdsk6/porta/data/source/local/DatabaseMigrationTest.kt`.
- **Change / projection:** verify offline cold launch and Room persistence on Android/JVM; retain persistent Desktop home path and safe legacy copy; document any inherited SDK auto-init network behavior before changing it.
- **Test / accept:** start without network, restart, profile/plan/saved state intact; no compulsory online service for core. Static flags alone are not runtime proof.

## First post-W0 reviewable slices

1. **Trust fix (T10 + T09 core):** explicit achieved result and evidence timing; discrete plan status. This addresses confirmed false-confidence risks with a small test surface.
2. **Doors → Details (T02 + T03 + T06):** one assessment projection, compact discovery, expandable exact receipt/source. No domain behavior change expected.
3. **Next moves → What-if (T07 + T08 + T04 + T14):** one impact projection, honest feasible counts and compact 0/1/5/12+ visualization. Keep Pareto logic unchanged unless tests reveal a true defect.

**Stop gate for this execution:** W0 docs and baseline only. No UI source modification is included in W0.
