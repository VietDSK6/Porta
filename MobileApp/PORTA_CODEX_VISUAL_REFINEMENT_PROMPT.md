# CODEX MASTER PROMPT — PORTA Visual UX Refinement

You are working in the PORTA Kotlin Multiplatform / Compose Multiplatform repository.

Read `PORTA_VISUAL_UX_ART_DIRECTION.md` in full before planning or editing. It is the visual/UX direction, **not** an authoritative source of runtime truth. Also read the current PRD and relevant existing UI, design system, domain/viewmodel code.

## The assignment

Improve PORTA's visual experience and reduce its text density **without weakening technical correctness**.

Current user feedback:
- Too much text.
- Too many similar dark cards, making screens monotonous.
- Little visual storytelling despite PORTA's "door/path/threshold" identity.
- Opportunity Details and What-if feel like audit reports.
- Need a stronger yet restrained visual personality across Android and Desktop.

**This is a scoped implementation task. Do not rebrand PORTA.**
Preserve Newsreader + Manrope, existing dark/light design tokens, navigation model, rule semantics, data provenance, actual/hypothetical separation, and scope-specific availability assertions.

### Non-negotiables
1. Do not invent eligibility requirements, opening dates, evidence, action impacts, estimates, programme logos, or current-source verification.
2. Do not convert `UNKNOWN`/`REVIEW` to `PASS`; do not imply admission likelihood.
3. Do not globally merge per-programme scoped actions because several have the same title.
4. Do not persist hypothetical changes into the user's actual profile.
5. Do not remove access to official URLs, exact rule explanations, provenance or assumptions.
6. Do not solve density by making fonts/touch targets smaller.
7. Do not add stock illustrations or random decoration.
8. Design both Android and Desktop. Use the shared Compose design system and adaptive layouts.
9. Do not add extra cloud/network dependencies to load illustrations.
10. No unnecessary changes to eligibility/recommendation engines. If a projection model is required for truthful UI, keep it thin, covered by tests, and explain it.

## Start with an audit

Before changing code:
- Find the current screen composables for `Doors`, `Opportunity Detail`, `Next Moves/Frontier`, `Scenario Preview/What-if`, `Academic Profile`, `Onboarding`, `Active Plan`, `Saved`, and `Pack Inspector`.
- Trace how eligibility statuses, exact rule receipts, source freshness, action scope, and scenario delta are produced.
- Locate shared typography/color/spacing/components, adaptive breakpoints and supported resources.
- Verify whether the screenshots/current report reflect the current working tree.
- Save an inventory of text duplication and visual layout anti-patterns.
- Inspect existing unit/UI tests and snapshot generation.
- Create `VISUAL_REFACTOR_PLAN.md` with precise file paths, proposed UI projections, and batch breakdown.

## Implementation order

Do not change everything in one blind refactor. Work in small batches. Implement Batch 1 fully, run tests, then proceed to Batch 2 and 3 only if it is safe and time/resources allow. Clearly report incomplete batches.

### Batch 1 — Less text, better hierarchy (highest priority)

**Doors**
- Compact opportunity cards: category/provider cue, name, status, one important unresolved check, known deadline or "deadline unconfirmed", save.
- Remove repetitive lengthy source-reviewed sentences from every list card; retain source info in detail, and show prominent stale warning only when justified by data.
- Primary filters compact; all secondary statuses still available.
- Sample mode copy must not claim personal real-profile assessment.

**Opportunity Detail**
- Replace default long report-style paragraphs with a concise eligibility summary and grouped checklist rows.
- Group under `Met`, `Needs action`, `Needs information / source review`.
- Each row has status + clear short label; expand to show actual/expected and exact provenance.
- Manual checks must not appear as if they correspond to a missing user-editable fact unless such a fact actually exists.
- Keep source links functional and accessible; source/evidence details available in disclosure.
- One relevant contextual CTA only if an actual source-supported action exists. Otherwise show a concise useful explanation, not a fabricated action.
- Ensure incomplete overall state remains explicit.

**Next Moves**
- Keep the recommendation-first structure.
- Reduce initial action hero to type, meaningful target, programme scope, short intended effect, affected count/meaning, primary `Preview impact`.
- Put estimate basis, technical ranking explanation and long descriptions in `Why this?`.
- Distinguish Verify / Prepare / Improve in copy and visual cues.
- Group visually equivalent independent per-programme actions without changing their separate IDs/scoped state.
- Pareto plot should not occupy primary space when there is no informative comparison.

**What-if**
- Default view must have three sections in this order:
  1. `Impact`: number of evaluated rule improvements, affected opportunities, whether overall status changed.
  2. `What changes`: exact before/after of the relevant check.
  3. `What remains`: unresolved manual/unknown/deadline checks.
- One concise persistent indication: `Simulation — profile unchanged`.
- Put full raw receipts, all unchanged doors, source notes and estimate provenance in `Detailed checks` disclosure.
- Ensure all counts are derived from the scenario engine.
- Proper action button if user is in sample vs real mode.
- Static, semantic text summary must be sufficient without animation.

**Acceptance for Batch 1**
- Every default screen has one visually dominant answer and one main action.
- No loss of technical information; all details accessible within 1–2 user actions.
- No misleading claim that individual rule progress means full eligibility.
- No regressions in sample/real mode, saved state, navigation and scroll positions.
- No dense full-screen source citations on opportunity list cards.

### Batch 2 — Build a distinctive visual language

Use `PORTA_VISUAL_UX_ART_DIRECTION.md` Section 2–5 as the authoritative *design brief*.

Build reusable, data-driven primitives:
- `PortaArch` (brand identity / small opportunity marker)
- `CategoryGlyph`
- `RequirementStatusRow`
- `OpportunityCompactCard`
- `ActionImpactConnector` or `ActionRippleDiagram`
- `ScenarioDeltaPanel`
- `EvidenceDisclosure`
- `PortaEmptyState` using restrained vector art

**Data contract rules**
- Diagram nodes are generated from actual `affectedOpportunityIds`, not hardcoded labels/counts.
- When there is only one affected opportunity, render a compact cause→effect connector, not a dramatic four-door animation.
- Use a distinct style for rule-level improvement vs overall eligibility completion.
- Arch glow/color must never imply "fully unlocked" when unresolved conditions remain.
- For onboarding synthetic demo, mark all opportunities as fictional.
- Build vector art through available Compose drawing primitives or properly licensed local vector resources; no external image loading needed.

Use illustration selectively:
- Profile empty state: Personal Atlas motif.
- Onboarding: Threshold Doors visual for IELTS demo.
- What-if: meaningful action→effect diagram.
- Saved empty state or no next move: simple arch/path asset if useful.
- NOT one illustration per every ordinary card.

**Acceptance for Batch 2**
- A visual design rhythm appears across screens, but normal content remains scannable.
- Illustration is meaningful and adds information/brand, never contradicts state.
- No platform-incompatible vector parsing or unlicensed image/logo usage.
- Static and accessible alternative for each diagram.

### Batch 3 — Motion & adaptive polish

- Add short semantic animations only for selection, expansion, specific rule changes and data-driven ripple.
- Respect reduced-motion settings where supported; otherwise implement nonessential-motion off fallback.
- Make all state changes discoverable through text and accessible semantics.
- Android: compact layouts, keyboard/insets, 48dp touch target guidance, font scale tests.
- Desktop: keyboard focus/hover, resize robustness, appropriate max widths, no giant empty full-width controls.
- Support both dark and light themes.
- Do not add repetitive ambient animation or automatic confetti.

**Acceptance for Batch 3**
- User can operate all important flows without motion.
- Scroll offsets, sticky header/footer, focus positions and navigation remain stable.
- Desktop resized to compact and Android large font scale cause no clipping/overlap.
- Visual changes do not change engine truth.

## Testing & verification

Run the applicable existing repo verification gates; use repo-defined commands rather than assuming versions.
If scripts support them, target:
- unit and UI tests for eligibility/scenario/action scope
- existing screenshot previews for Android
- JVM Desktop compile/run check
- adaptive/reduced-motion/large-font previews
- lint/format checks

For visual regression, capture before/after on at least:
1. Doors sample mode (default + filtered + saved)
2. Opportunity Detail with mixed pass/unknown/manual
3. Next Moves with scoped action + alternatives
4. What-if with 1 rule changed, overall state unchanged
5. Onboarding IELTS demo
6. Profile empty and populated state
7. Android compact, Desktop large/resize, dark/light

Manually check:
- an action impacting one opportunity
- a synthetic action impacting four fictional opportunities
- no action available
- unknown duration and unconfirmed deadline
- full screen reader summary / keyboard focus order
- sample-mode CTA route and actual profile isolation

## Output contract

Provide:
1. `VISUAL_REFACTOR_PLAN.md` — inspected files, baseline, changes, assumptions.
2. Implemented code changes in small coherent groups.
3. A short `VISUAL_REFACTOR_CHANGELOG.md` — files changed, before→after per screen, screenshots/tests actually run.
4. Explicit list of deferred items if batch incomplete.

**Do not claim a build/test/manual visual check passed unless it actually ran.**
**Do not substitute a presentation mock that does not reflect real app data.**
**Do not change underlying model truth solely to make the interface visually delightful.**

Final product experience should feel like an editorial atlas of opportunities:
**Less reading, more seeing — without less truth.**
