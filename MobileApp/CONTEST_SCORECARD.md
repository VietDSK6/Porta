# Porta — Kotlin Student Coding Competition scorecard

> Last updated: 2 November 2026
> Deadline: 7 December 2026, 23:59 CET
> Official sources: [contest rules](https://kotlinconf.com/contest-rules/) ·
> [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
> Internal estimates in this file are not official judging results.

## Status legend

- `PASS` — repository or user-supplied evidence exists.
- `IN PROGRESS` — work has started, but the gate is not satisfied.
- `TODO` — required evidence does not exist yet.
- `USER CONFIRM` — personal/legal fact that an agent must not infer.
- `RISK` — unresolved issue can affect eligibility or judge reproduction.

## Mandatory gates

| Gate | Status | Current evidence / next action |
|---|---|---|
| Entrant age, enrollment, residency/sanctions, employment and prize eligibility | USER CONFIRM | Confirm against the live rules before submission; do not store sensitive details here. |
| Individual submission; one entry | USER CONFIRM | User confirms solo participation and no second entry. |
| Created specifically during the contest period | RISK | Preserve Git history showing Porta-specific work began during the period; document that Koko is a licensed starter kit and distinguish new product code from template code. |
| Substantial application, not template/Hello World/library | PASS | Porta-owned engine, Room repositories, Compose flows, pack validation/import and 15-record catalog are implemented; keep separating these changes from Koko template code in README evidence. |
| Original work or sufficient rights | IN PROGRESS | `OPPORTUNITY_PACK_SOURCES.md` records independent interpretations and official URLs; retain starter-kit/dependency notices and still audit icons, media and generated assets before publication. |
| Cross-platform Kotlin project | PASS | Product logic, persistence and most UI are shared Kotlin; Android debug builds, the JVM Desktop package builds and launches past startup, and JS/Wasm shared compilation remains a compatibility check. Android + JVM Desktop are the declared contest targets. |
| GitHub repository submitted | TODO | Create/publish the final repository and verify judge access. |
| Accepted repository license | PASS | Root repository currently contains MIT License. Preserve required notices and verify the submitted repository exposes it. |
| Comprehensive README | IN PROGRESS | `README.md` now covers the product story, demo path, architecture, data provenance, limitations, quality gates and starter-kit boundary; add final media and fresh-setup troubleshooting evidence before submission. |
| Launch instructions for every declared target | IN PROGRESS | Android and JVM Desktop are explicitly declared with exact build/run commands; verify both from a fresh setup before freezing the README. |
| 300-word essay | IN PROGRESS | `SUBMISSION/ESSAY.md` has an exactly 300-word body and four explicit entrant-fact placeholders. Replace them truthfully and recount before submission. |
| Submission form before deadline | TODO | Save the final form link and submission receipt outside the repository. |
| Truthful and rights-safe submission/media | IN PROGRESS | Source every factual claim and opportunity rule; obtain permission for any identifiable third party in media. |

## Target declaration decision

Current contest promise: **Android + JVM Desktop**.

The README now identifies iOS and Web as inherited compatibility scaffolding rather than declared Porta contest
targets. Shared JS/Wasm compile checks may still protect portability, but they do not expand the runnable-target claim.

## Judging rubric

### Creativity and novelty — TBD / 40

Judge-visible thesis:

> Porta automatically finds controllable milestones, calculates their encoded-rule marginal opportunity impact, removes
> actions that cannot fit the user's constraints, and exposes the tradeoffs through an Action Frontier.

Current evidence:

- [x] Fifteen-opportunity feasibility/kill test documented in `PORTA_PRD.md`.
- [x] Competitor boundary documented; What-if is treated as the visualization, not the sole novelty claim.
- [x] Working Action Frontier over the bundled 15-record sourced dataset, with hard-gap resolution kept distinct
  from fully open eligibility and unverified deadlines.
- [x] Trust-first v2 model scopes programme confirmations by opportunity, rule and fingerprint; stale assertions are
  ignored, ambiguous v1 availability actions are rejected, and cross-opportunity leakage is covered by tests.
- [x] Constraint-aware Action Frontier now persists time/effort/cost/saved-door priorities, reacts to Room-backed
  saves, diagnoses excluded milestones, and exposes a selectable impact map; domain, ViewModel and Compose UI tests
  cover the ranking and disclosure behavior.
- [x] Wave B validates four official-source vertical-slice records and exposes check-level What-if receipts: exact
  result transitions, remaining failed/unknown/manual checks, deadline feasibility, programme-scoped impact and a
  source-opening path. Detail CTAs now bind only to an unmet, actionable rule that can fit its published deadline.
- [x] Missing real facts now create source-linked information tasks instead of simulated improvements: global facts
  route to explicit profile editing, scoped checks route to the official programme source, and tests prove that
  opening either path neither writes a fact nor creates a passing result.
- [x] Next moves now leads with one explainable recommendation, distinguishes VERIFY / IMPROVE / PREPARE, groups
  repeated programme-scoped confirmations without merging their targets, discloses estimate provenance and impact
  basis, and only renders the Action Frontier plot when distinct trade-offs exist. Overlapping vectors become one
  explicitly counted point instead of ambiguous stacked bubbles.
- [x] Progressive Profile gets students to source-linked Doors with only study level and field of study, then asks for
  optional facts in context from the exact missing rule. The form exposes why a value is needed, validates save-blocking
  input, distinguishes missing from failure, and uses date-of-birth boundaries for the sourced Schwarzman reference date.
- [x] Pack lifecycle is explicit: source records display their manual review date and Porta's 90-day maintenance
  signal; archived cycles are distinct from passed deadlines and excluded from planning; the shared local date refreshes
  on resume/day rollover; pack imports show an approval diff and disclose which scoped confirmations become stale.
- [x] Local-data controls distinguish clearing the current academic profile from resetting all Porta data. Confirmation
  copy names the exact removed and retained state; integration tests cover facts, scoped confirmations, plans, saved
  doors, imported catalogs, preferences, multiple profile IDs and restoration of the bundled catalog.
- [x] Doors now supports title/provider/category search plus category, deadline, eligibility, saved and archived filters;
  empty results can reset the full filter state without altering persisted data.
- [x] The judge-facing decision path is bounded and legible: Frontier renders at most five ranked actions and names the
  earliest affected deadline, while What-if adds an Actual-to-simulated aggregate across all conclusion states.
- [x] Pack identity is immutable across every stored version: a publisher change for an existing pack ID is rejected
  before persistence, including when the original version is inactive.
- [x] Demo and real-pack receipts separate deterministic results, unknowns, manual checks and soft criteria.
- [ ] Final competitor check and dated novelty statement.

Do not assign a numeric estimate until the working engine and demo evidence exist.

### Kotlin Multiplatform/shared Kotlin — TBD / 40

Required evidence:

- [x] Shared domain models and typed rule graph.
- [x] Shared deterministic eligibility engine and receipts.
- [x] Shared counterfactual simulator and Action Frontier algorithm.
- [x] Shared repository, validation, Room persistence, and import/export.
- [x] Room schema v2 preserves existing facts, saved doors and active plans while adding scoped rule assertions;
  profile export v2 round-trips facts and assertions as separate sections.
- [x] Meaningful shared Compose UI/state, not only common constants.
- [x] Android quick-update/tracking flow completes a local plan, requires explicit actual-fact confirmation,
  persists optional evidence metadata and refreshes Doors reactively; covered by ViewModel/UI tests and the
  Android debug build.
- [x] Desktop comparison/inspection flow works: wide windows use a navigation rail, Frontier becomes a
  selectable master-detail workspace with an Actual-versus-What-if door matrix, and Pack inspector keeps its
  read-only master-detail diagnostics. Compact Android behavior remains one-column with bottom navigation;
  adaptive UI tests, 1280×800 preview evidence, JVM tests and the JVM Desktop launch path cover the slice.
- [x] Responsive/accessibility regression evidence covers a 320×640 viewport, 200% text, keyboard traversal of the
  Desktop rail, 48dp primary chips, a wide two-pane opportunity receipt with a large-text single-column fallback,
  a polite What-if live region and zero-duration motion without losing the textual outcome. Final packaged-app
  screen-reader and full keyboard exercise remains a disclosed manual submission check.
- [x] Common/JVM/Android-host tests demonstrate shared behavior, including two contrasting profiles across all 15 records.
- [x] `KMP_EVIDENCE.md` documents a reproducible contest-period measurement at `d3d34dc`: 14,933 NCLOC across 75
  explicitly allowlisted shared Porta production files plus 96 NCLOC across four platform integration files; tests and Kotlin data are
  reported separately, while generated/vendor/unchanged starter code, package-only renames and mixed-provenance
  integration files are excluded.

Do not assign a numeric estimate from starter-kit scaffolding.

### Kotlin coding conventions — TBD / 20

Required evidence:

- [x] Spotless + ktlint configured.
- [x] Repository exceptions identified and documented in the local convention-audit skill.
- [x] All Porta Kotlin passes `spotlessCheck`.
- [x] Manual audit covers organization, filenames, naming, class layout, idioms, and KDoc.
- [x] Every manual finding is fixed or justified; the large contest-pack file is intentionally one cohesive,
  versioned data manifest rather than mixed application logic.
- [x] README documents formatting/test commands.
- [x] Final audit on 2 November 2026 checked the then-current official convention page, formatter gate, naming,
  organization, idioms and KDoc on the changed core contracts.

A green formatter alone must never be recorded as `20/20`.

## Submission assets

| Asset | Status | Definition of done |
|---|---|---|
| Product name and visual identity | READY LOCALLY | Porta uses the Editorial Atlas palette, Newsreader/Manrope typography and trust-first core journey; final light/dark Android and Desktop captures plus rights audit remain. |
| Curated opportunity pack | IN PROGRESS | First official-source pass is complete for 15 records with dated source notes, manual/soft boundaries, a source register, visible freshness policy, lifecycle handling and user-approved update diffs; complete an independent second reading before submission. |
| README | READY LOCALLY | Product, target, architecture, feature tour, limitations, sources, license and commands are documented; add final screenshots and the public fresh-clone record. |
| Essay | NEEDS ENTRANT FACTS | Body is exactly 300 words; replace the four factual placeholders without changing truth or count. |
| Screencast | SCRIPT READY | `SUBMISSION/DEMO_SCRIPT.md` defines a 4:20 path through problem, magic moment, KMP proof, trust boundary and close. |
| Screenshots/GIFs | TODO | Current UI only; rights-safe and readable. |
| Submission form | TODO | Final URLs tested without private access. |

## Review cadence

- Weekly: run the `contest-readiness` milestone audit and update only evidence-backed rows.
- After material Kotlin milestones: run `kotlin-convention-audit` on the changed scope.
- Seven days before deadline: freeze features; audit README, targets, essay, licenses, media, and live rules.
- Twenty-four hours before deadline: run the final documented paths and submit early enough to preserve proof of
  receipt.

## Next three contest actions

1. Replace the four essay placeholders with verified entrant facts, preserve the exact 300-word count, and confirm the
   personal eligibility/solo-entry gates.
2. Have a second person re-read all 15 official sources, refresh every TBA deadline, and capture rights-safe Android
   and Desktop media from the frozen build.
3. Publish the frozen snapshot to an entrant-owned public GitHub repository, run the documented judge path from that
   public clone, record the video URL, and submit before the deadline.
