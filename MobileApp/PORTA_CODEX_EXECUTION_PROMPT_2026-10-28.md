# CODEX — Execute PORTA Complete UX Redesign (wave-by-wave)

You are working inside the PORTA Kotlin Multiplatform / Compose Multiplatform repository.

## Read first

1. Read `PORTA_COMPLETE_UX_REDESIGN_PLAN_2026-10-28.md` in full. This is the **master plan**.
2. Read the current `PORTA_PRD.md`, repository `README.md`, `PORTA_PRODUCT_CONTEXT.md` if available, and any existing design system or prior visual refinement plan in the repository.
3. Inspect the **current working tree** and current composables/ViewModels/domain model before accepting any claim made in the older audit. The plan is based on screenshots and a 24-Oct static snapshot and may describe code already changed.
4. Do not infer screenshot behavior that has not been tested (e.g., whether Yes is preselected in Actual Update; whether 5 affected vs 4 in-plan is a bug or intentional filtering).

## Assignment

Implement the complete plan **in small, testable waves**, preserving the current product identity (Newsreader/Manrope, editorial ivory/charcoal/lavender, existing navigation) and domain correctness.

**Start with W0 only.** Do not change UI until you have completed the verification matrix and found what is already implemented. Report W0 findings, propose the smallest safe Wave W1/W2 implementation slices, and stop at that gate unless the current user has explicitly authorized you to continue further.

### W0 required outputs

Create/update:
- `PORTA_CURRENT_STATE_VERIFY.md`: screenshot-to-screen mapping, current route/navigation level map, status/estimate/affected-count semantics, potential data inconsistencies, files/classes inspected, baseline screenshot references.
- `PORTA_IMPLEMENTATION_PLAN.md`: task ordering by `T01`–`T19` in the master plan; each entry has exact file paths, dependencies, implementation change, data projection needs, test strategy and acceptance criterion.
- `PORTA_UI_CHANGELOG.md`: initially establish baseline (do not claim work that is not done).

Inspect explicitly:
- Doors, Next moves, Profile, Opportunity Details, What-if, Edit academic profile, Active Plan, Update actual profile.
- Onboarding, secondary evidence/source/pack routes and empty states.
- The full navigation graph; Level 1/2/3 are **navigation depth**, not severity.
- Rule semantics, UNKNOWN/MANUAL, Source Freshness, deadlines and cycle handling.
- `ActionTemplate` and per-programme scoped assertions; 1 vs 5 affected cases; 180-day estimate conflict.
- `Plan` transitions vs actual profile updates; evidence reference/label validation; any preselected Yes behavior.
- Profile fact-to-door counts (`13 vs 15`, zero-used facts); active plan affected count vs scenario affected count.
- Actual Android/JVM Desktop persistence and startup network behavior vs local-first promise.

## Non-negotiable invariants

1. Never invent provider requirements, deadlines, programme logos, estimates or opportunity impacts.
2. Do not convert partial rule improvement into overall eligibility or acceptance predictions.
3. `UNKNOWN` / `MANUAL_REVIEW` must never silently become `FAIL` or `PASS` because of UI presentation.
4. What-if must not change actual profile; adding/completing a plan must not automatically confirm facts.
5. Actual result must be explicitly chosen and stored even when different from planned target.
6. Scoped assertions for ESA/CERN/OECD/etc. must remain scoped to the right programme/rule.
7. Keep exact rule receipt, official sources and provenance reachable after collapsing default text.
8. Deadline/feasibility warnings that may change decisions must remain visible at summary level.
9. Visualizations and counts must derive from the same engine projection, not invented or hardcoded values.
10. No blanket font reduction; no loss of keyboard/accessibility/reduced-motion support.
11. Do not add compulsory cloud services, crawler, AI chatbot, stock illustrations, new analytics, or full rebrand.

## Implementation guardrails

Once W0 is approved, execute independent batches:

**W1 — Level 1:** Doors compact discovery, Next moves meaningful impact / active-plan priority, Profile fact/count cleanup. Preserve all states and navigation.

**W2 — Level 2:** Details grouped assessment checklist; What-if scalable 0/1/5/12+ opportunities with single summary/result dataset, compact visualization, one expanded detail at a time; Edit academic profile progressive disclosure.

**W3 — Plan/actual confirmation:** Plan state-based timeline without misleading saved=33%; actual result explicit and evidence correctly required; no raw internal IDs in user UI.

**W4 — Visual story:** Small locally-rendered Arch/Threshold/Path/Ripple assets where meaningful; preserve light/dark; animate only when the computed state changes, including reduced-motion/static variant.

**W5 — QA:** Android compact/large font; JVM Desktop resize/focus; offline restart; data import validation; cross-screen semantics; accessibility; screenshots; release build gates.

*Implementation can reorder individual slices only if dependencies/test coverage are maintained and the change is documented. Avoid one giant PR.*

## Required tests for changed features

- Sample profile vs real profile labels.
- A missing fact does not count as failed.
- Real pack vs fictional onboarding separation.
- What-if 0, 1, 5 and >10 affected; one or many deadline warnings; overall status unchanged despite rule improvement.
- One programme-scoped availability assertion does not affect another programme.
- Plan `saved` / `in progress` / `work complete` / `actual confirmed` transitions.
- Target `Yes` but actual achieved `No`; actual profile remains unchanged until explicit confirmation.
- Evidence validation does not display premature red errors before user interaction/submit.
- Profile count semantics; zero-used facts; actual vs candidate vs feasible affected opportunities.
- Full source disclosure and official URL accessible.
- Compact phone font scaling, scrollbar/IME/sticky title, Desktop resizes, dark/light, keyboard focus, reduced motion.

**Build/test reporting**: always include exact commands and outcomes. Never say tests pass if not run. Capture before/after screenshots when tooling allows; if not, state the gap.

## Completion criteria

A complete wave has:
1. exact changed-file list;
2. before→after user behavior summary;
3. domain truth impacts (or statement 'none');
4. tests actually executed and outcomes;
5. screenshots/checklist of cases verified;
6. limitations, deferred tasks, risks;
7. a short review note against the master plan's relevant acceptance criteria.

**One-sentence product requirement:**
> Less reading, more seeing — without less truth.

Do not trade the correctness of PORTA's eligibility, source or actual/hypothetical model for an attractive demo.
