# Features — Porta

> [!NOTE]
> This is the resumable build record for Porta. Continue from the first unchecked item and tick work
> as it is completed. Do not infer completion from the PRD alone.

**Source of truth:** `AiGuidelines/project/prd.md` · `user_flow.md` · `ui_ux.md` ·
`MobileApp/PORTA_PRD.md`
**Status key:** `[ ]` not started · `[~]` in progress · `[x]` done
**Plan confirmed:** 20 October 2026

---

## Vertical-slice order

1. Shared rule graph, eligibility engine, receipts, and deterministic tests.
2. Scenario evaluator, ethical milestone generation, feasibility filtering, and Action Frontier.
3. Local persistence plus a bundled five-opportunity demo pack, then expand to 15 verified records.
4. Demo-first onboarding → Doors → Receipt → Frontier → Scenario Preview.
5. Active Plan, Profile, Pack Inspector, and differentiated Desktop layouts.
6. Remove obsolete starter routes/features, verify UI, and run the scoped quality gates once.

## Persisted models

- [x] **ProfileFact** — scaffolded ☑ · real columns + mappers ☑ · DAO queries ☑
- [x] **OpportunityPack** — scaffolded ☑ · real columns + mappers ☑ · DAO queries ☑
- [x] **SavedOpportunity** — scaffolded ☑ · real columns + mappers ☑ · DAO queries ☑
- [x] **PlanItem** — scaffolded ☑ · real columns + mappers ☑ · DAO queries ☑

## Shared product engine

- [x] **Profile and rule primitives** — typed facts, actionability, opportunity, rule tree, action templates
- [x] **Eligibility evaluator** — `ALL`/`ANY`/atomic/manual semantics, unknowns, units, deadlines, receipts
- [x] **Scenario evaluator** — sparse overlay, exact state deltas, reset immutability
- [x] **Action generator** — curated templates only, immutable/context-only rejection, deduplication
- [x] **Feasibility and Action Frontier** — deadline/time/effort/cost filters, Pareto set, stable ranking/explanation
- [~] **Pack validation and loading** — schema diagnostics + atomic import/export ☑ · bundled verified pack ☐
- [x] **Demo fixtures** — clearly labelled synthetic profile and five opportunities; IELTS `6.5 → 7.0` opens four doors
- [ ] **Contest pack** — at least 15 manually checked opportunities with source and verification date

## Screens

- [~] **ProfileSetup** — scaffolded ☑ · UI per `ui_ux.md` ☐ · nav callbacks wired ☐
- [x] **Doors** *(tab)* — Portal Atlas cards, states/filters/saves, receipt navigation, preview + UI test
- [x] **OpportunityDetail** — rule receipt, actual/required/source limits, scenario navigation, preview + UI test
- [x] **Frontier** *(tab)* — horizon constraint, Pareto action card, scenario navigation, preview + UI test
- [x] **ScenarioPreview** — persistent WHAT-IF, before/after and deltas, explicit commit/discard, preview + UI test
- [~] **ActivePlan** — scaffolded ☑ · UI per `ui_ux.md` ☐ · nav callbacks wired ☐
- [~] **StudentProfile** *(tab)* — scaffolded ☑ · UI per `ui_ux.md` ☐ · nav callbacks wired ☐
- [~] **PackInspector** — scaffolded ☑ · UI per `ui_ux.md` ☐ · nav callbacks wired ☐

## Screens and infrastructure that ship with the kit

- [ ] **Onboarding** — rebuild the existing flow into four demo-first Porta steps
- [x] **Paywall disabled** — `PREMIUM_FEATURES_ENABLED=false`; onboarding cannot route to paywall
- [ ] **Navigation** — Android tabs are Doors / Frontier / Profile; Desktop uses adaptive rail/master-detail
- [ ] **Starter cleanup** — remove obsolete image-generation, gallery, credits, subscriptions, and account routes

## Stored preferences

- [ ] Onboarding completed
- [ ] Selected opportunity goal
- [ ] Planning horizon, maximum effort, maximum cost, and saved-door priority

## Acceptance tests

- [x] Atomic operators and `ALL`/`ANY` truth tables
- [x] Unknown values, incompatible units, manual checks, soft/readiness isolation, and deadline boundaries
- [x] Scenario never mutates the actual profile and reset restores the exact baseline
- [x] Immutable/context-only facts never become recommended actions
- [x] Candidate deduplication, deadline feasibility, Pareto dominance, stable ranking, and honest explanation
- [x] Synthetic test fixture opens exactly four doors for IELTS `6.5 → 7.0`
- [ ] Android navigation/back behavior and Desktop keyboard path
- [ ] Large text and reduced-motion summaries preserve all essential information

## Validation

- [ ] Every P0 screen has a pure composable test and `@Preview`
- [ ] Rendered previews inspected through `verify-ui`
- [ ] `run-quality-gates` passes: Spotless, shared JVM/Android host tests, Android debug build
- [ ] Developer ran Android or Desktop and confirmed the vertical slice behaves correctly

---

## Later additions

- P1 form-based Opportunity Studio
- Side-by-side comparison of up to three scenarios
- Deadline reminders and local evidence-file references
