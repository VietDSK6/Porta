# Porta — Kotlin Student Coding Competition scorecard

> Last updated: 20 October 2026
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
| Substantial application, not template/Hello World/library | IN PROGRESS | PRD defines a complete Android/Desktop app; implementation evidence does not exist yet. |
| Original work or sufficient rights | IN PROGRESS | Retain starter-kit and dependency notices; audit all data, icons, media, opportunity text, and generated assets before publication. |
| Cross-platform Kotlin project | IN PROGRESS | Infrastructure exists. Contest baseline is Android + JVM Desktop; both product flows must be demonstrably usable. |
| GitHub repository submitted | TODO | Create/publish the final repository and verify judge access. |
| Accepted repository license | PASS | Root repository currently contains MIT License. Preserve required notices and verify the submitted repository exposes it. |
| Comprehensive README | TODO | Add product story, architecture, data provenance, prerequisites, troubleshooting, and key-feature instructions. |
| Launch instructions for every declared target | TODO | Decide final declared target set, test each exact command on a fresh setup, and document it. Do not leave broken starter-kit targets ambiguous. |
| 300-word essay | TODO | Draft late enough to reflect the real product; verify exact word count before submission. |
| Submission form before deadline | TODO | Save the final form link and submission receipt outside the repository. |
| Truthful and rights-safe submission/media | IN PROGRESS | Source every factual claim and opportunity rule; obtain permission for any identifiable third party in media. |

## Target declaration decision

Current contest promise: **Android + JVM Desktop**.

Before the end of the first implementation week, decide how the existing iOS and Web starter-kit targets appear in
the submission. Either keep them working and document them, or clearly scope the submitted product and repository
so judges are not given unlaunchable targets. Never silently claim a target based only on generated scaffolding.

## Judging rubric

### Creativity and novelty — TBD / 40

Judge-visible thesis:

> Porta automatically finds controllable milestones, calculates their verified marginal opportunity gain, removes
> actions that cannot fit the user's constraints, and exposes the tradeoffs through an Action Frontier.

Current evidence:

- [x] Fifteen-opportunity feasibility/kill test documented in `PORTA_PRD.md`.
- [x] Competitor boundary documented; What-if is treated as the visualization, not the sole novelty claim.
- [ ] Working Action Frontier over a curated, sourced dataset.
- [ ] Constraint changes visibly reorder the frontier.
- [ ] Demo shows deterministic explanations and honest unknowns.
- [ ] Final competitor check and dated novelty statement.

Do not assign a numeric estimate until the working engine and demo evidence exist.

### Kotlin Multiplatform/shared Kotlin — TBD / 40

Required evidence:

- [ ] Shared domain models and typed rule graph.
- [ ] Shared deterministic eligibility engine and receipts.
- [ ] Shared counterfactual simulator and Action Frontier algorithm.
- [ ] Shared repository, validation, Room persistence, and import/export.
- [ ] Meaningful shared Compose UI/state, not only common constants.
- [ ] Android quick-update/tracking flow works.
- [ ] Desktop comparison/authoring flow works.
- [ ] Tests demonstrate shared behavior.
- [ ] Shared-code metric documented with generated/vendor/template code excluded or separately disclosed.

Do not assign a numeric estimate from starter-kit scaffolding.

### Kotlin coding conventions — TBD / 20

Required evidence:

- [x] Spotless + ktlint configured.
- [x] Repository exceptions identified and documented in the local convention-audit skill.
- [ ] All Porta Kotlin passes `spotlessCheck`.
- [ ] Manual audit covers organization, filenames, naming, class layout, idioms, and KDoc.
- [ ] Every manual finding is fixed or justified.
- [ ] README documents formatting/test commands.
- [ ] Final audit checks the then-current official convention page.

A green formatter alone must never be recorded as `20/20`.

## Submission assets

| Asset | Status | Definition of done |
|---|---|---|
| Product name and visual identity | IN PROGRESS | Product name is Porta; implement the Portal Atlas system consistently across icon and screens, then audit marks/assets. |
| Curated opportunity pack | IN PROGRESS | At least 15 deeply verified records, each with source and verification date. |
| README | TODO | Reproducible setup, all declared targets, architecture, feature tour, limitations, sources, license. |
| Essay | TODO | Exactly 300 words and consistent with the actual submission. |
| Screencast | TODO | 3–5 minutes; problem, magic moment, KMP proof, trust boundary, close. |
| Screenshots/GIFs | TODO | Current UI only; rights-safe and readable. |
| Submission form | TODO | Final URLs tested without private access. |

## Review cadence

- Weekly: run the `contest-readiness` milestone audit and update only evidence-backed rows.
- After material Kotlin milestones: run `kotlin-convention-audit` on the changed scope.
- Seven days before deadline: freeze features; audit README, targets, essay, licenses, media, and live rules.
- Twenty-four hours before deadline: run the final documented paths and submit early enough to preserve proof of
  receipt.

## Next three contest actions

1. Decide and record the final declared target set, especially how the existing iOS/Web modules are presented.
2. Preserve a clear Git/provenance boundary between the licensed Koko starter kit and new Porta product work.
3. Build the first vertical slice that demonstrates shared rule evaluation on Android and Desktop before adding
   secondary features.
