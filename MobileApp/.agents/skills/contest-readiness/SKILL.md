---
name: contest-readiness
description: Audit Porta against the Kotlin Student Coding Competition eligibility gates, 40/40/20 judging rubric, milestone evidence, and submission deliverables. Use for scope tradeoffs, weekly contest reviews, README/demo planning, and final submission readiness; do not use as a substitute for ordinary feature testing.
---

# Contest readiness

Keep Porta eligible, judgeable, and focused on evidence that can earn points. Report uncertainty explicitly;
do not convert legal eligibility or subjective judging into a guaranteed score.

## Read first

- Read [`../../../CONTEST_SCORECARD.md`](../../../CONTEST_SCORECARD.md) for current state.
- Read the relevant scope and acceptance criteria in [`../../../PORTA_PRD.md`](../../../PORTA_PRD.md).
- For an eligibility, deadline, licensing, or submission decision, read
  [`references/official-rules-2026.md`](references/official-rules-2026.md) and then verify the live
  [official rules](https://kotlinconf.com/contest-rules/). The rules may change.
- For the conventions criterion, use
  [`../kotlin-convention-audit/SKILL.md`](../kotlin-convention-audit/SKILL.md).

## Choose the audit mode

### Scope review

Use before accepting a feature or architecture change. Determine whether it improves at least one of:

- the Action Frontier's defensible novelty;
- meaningful shared Kotlin behavior;
- the Android/Desktop product story;
- judge reproducibility or demo clarity;
- a mandatory submission gate.

Reject or defer work that adds polish or breadth while putting a P0 acceptance criterion at risk. A feature that
touches both platforms does not automatically demonstrate KMP value; identify the shared state, algorithm,
persistence, or UI behavior it exercises.

### Milestone audit

Use at the end of each weekly milestone:

1. Recheck mandatory gates whose evidence may have changed.
2. Assess each rubric category using repository evidence, not intention.
3. Run the Kotlin convention audit on the changed Kotlin scope.
4. Use the repository `run-quality-gates` skill once after code is merged. Do not invent wider Gradle tasks.
5. Update the scorecard with evidence links, blockers, owners, and the next review date.

### Submission audit

Use before publishing the repository and again immediately before sending the form:

1. Refresh the live official rules and record the verification date.
2. Verify entrant-provided facts separately; never infer age, enrollment, residency, sanctions, employment, or
   prize eligibility from the repository.
3. Verify the repository, accepted license, solo entry, contest-period provenance, target launch instructions,
   README, exact essay word count, and submission link.
4. Run every declared target using documented instructions, or report the exact target that remains unverified.
5. Run the convention audit and scoped repository quality gates.
6. Check the 3–5 minute screencast against the demo story and ensure every factual claim is supportable.
7. Produce a final `PASS`, `BLOCKED`, or `PASS WITH DISCLOSED RISKS`; never silently waive a requirement.

## Evaluate the rubric

### Creativity and novelty — 40%

Look for a coherent, demonstrable distinction rather than a feature count. Porta's current boundary is:

> Automatically enumerate controllable milestones, calculate verified marginal opportunity gain, filter actions
> by time/deadline/effort/cost, and explain the tradeoff across opportunities.

Treat What-if sliders and opening doors as the demonstration of that engine, not as the novelty claim by
themselves. Keep competitor comparisons factual and dated.

### Kotlin Multiplatform use/shared Kotlin — 40%

Inventory what is genuinely shared and what each platform contributes. Favor evidence such as:

- rule graph and deterministic eligibility engine;
- counterfactual scenario evaluation and Action Frontier ranking;
- domain models, repositories, Room database, and import/export validation;
- reusable Compose screens/components and shared UI state;
- tests proving the same behavior on relevant targets;
- Android's quick profile/action loop and Desktop's comparison/authoring workflow.

If calculating an internal shared-code ratio, disclose the method and exclusions. Do not present generated,
vendored, build, or unchanged starter-kit code as product-specific shared Kotlin evidence. The official rules do
not prescribe a ratio formula.

### Kotlin coding conventions — 20%

Use the convention-audit skill. A green Spotless task proves only the configured mechanical subset. The manual
review must cover source organization, names, class/file layout, idiomatic Kotlin, documentation, and intentional
exceptions.

## Reporting contract

Report in this order:

1. mandatory gates and blockers;
2. evidence-based rubric assessment (`Novelty /40`, `KMP /40`, `Conventions /20`), using `TBD` where evidence is
   insufficient;
3. the three highest-leverage actions before the next review;
4. scorecard changes made;
5. commands run and any unverified platform.

Do not award points merely because a requirement appears in the PRD. Plans are not judge-visible evidence.
