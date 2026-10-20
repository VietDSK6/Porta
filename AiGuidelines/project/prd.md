# Porta — Product Requirements

## Goal

Porta helps students identify the smallest realistic, controllable milestone that opens the most verified
opportunities under their time, effort, budget, and deadline constraints. It explains published eligibility rules
without predicting admission, scholarship selection, or personal worth.

## Target Audience

- Primary: university students, initially IT students, who are comparing scholarships, structured internships,
  student contests, fellowships, and academic programs.
- Typical context: limited time and money, evidence scattered across grades, language certificates, projects, and
  activities, and low trust in unexplained match percentages.
- Secondary: teachers, lecturers, and advisors who want a source-linked way to explain opportunity requirements;
  they do not need an account for the contest MVP.
- Technical context: students use Android for quick profile updates and milestone tracking; students and advisors
  use JVM Desktop for deeper comparison and source inspection.
- Privacy expectation: academic and identity data remains local to the device in the contest version.

## Core Features

1. **Actual Profile and Sample Profile**
    - Purpose: Represent the facts that eligibility rules can evaluate while keeping real and hypothetical values
      visibly separate.
    - Key Elements: GPA with explicit scale, field and level of study, nationality/residency, English score,
      experience duration, project/activity evidence metadata, self-reported/evidence-backed labels, bundled demo
      profile.
    - Expected User Actions: Try the sample profile, create or edit a real profile, inspect which rules use a fact,
      and explicitly commit a completed milestone to the real profile.
2. **Explainable Doors**
    - Purpose: Show whether each curated opportunity meets the encoded published hard rules and why.
    - Key Elements: Open, Has gaps, Verify, Unavailable, and Closed states; hard-rule receipt; manual and soft
      criteria separated from eligibility; source URL, source section, publisher, cycle, and last-checked date.
    - Expected User Actions: Browse and filter opportunities, save a door, open its eligibility receipt, inspect
      actual versus required values, and continue to the official source.
3. **Constraint-Aware Action Frontier**
    - Purpose: Find a small set of non-dominated, controllable milestones instead of presenting a generic advice
      list.
    - Key Elements: Automatic milestone enumeration from whitelisted actionable rules; available-time, effort,
      cost-band, deadline, and saved-door priority constraints; verified marginal opportunity gain; transparent
      rank explanation.
    - Expected User Actions: Adjust planning constraints, compare frontier cards, inspect trade-offs, and choose a
      milestone to preview.
4. **What-if Scenario Preview and Plan**
    - Purpose: Make the Action Frontier understandable by showing exactly what one milestone changes across the
      opportunity set.
    - Key Elements: Before/after values, opened/closed/unchanged door groups, source-backed reason for every state
      change, deadline feasibility, reduced-motion text summary, local active-plan checklist.
    - Expected User Actions: Drag or select a target value, preview the resulting doors, save the milestone as a
      plan, track progress, or discard the hypothetical scenario without changing the actual profile.
5. **Curated and Inspectable Opportunity Pack**
    - Purpose: Keep rule evaluation reproducible, trustworthy, and fully offline.
    - Key Elements: At least 15 deeply verified opportunities for the contest build; typed rule graph; pack version,
      source provenance, verification date, validation diagnostics, import/export; read-only desktop pack inspector.
    - Expected User Actions: Inspect pack provenance, import a valid pack, review warnings or rejected rules, and
      export local profile/plan metadata without private evidence binaries.

## Monetization

- Porta is completely free during the contest: no paywall, advertisements, subscriptions, credits, or paid feature
  gates.
- `PREMIUM_FEATURES_ENABLED` remains `false`; billing and paywall surfaces are absent from onboarding and primary
  navigation.
- All bundled opportunity rules, receipts, Action Frontier results, and local planning tools are available equally
  to every user.
- A post-contest sustainability model is intentionally deferred to `TODO(monetization)` after trust, source
  maintenance cost, and real user demand are measured.

## Competitors

- ScholarPulse — Strong scholarship matching and What-if open/close visualization / its public concept does not
  establish Porta's constraint-aware, Pareto-ranked Action Frontier with source-level receipts.
- nrol — Strong gap analysis using effort, time, and deadlines / broader guidance can obscure the exact encoded
  clause and cross-opportunity marginal gain.
- OppPaths — Strong distinction between hard, soft, fixable, and fixed gaps / primarily explanatory content rather
  than a deterministic offline scenario engine.
- AppGap — Strong profile gap analysis and recommended improvements / does not center a transparent frontier of
  non-dominated actions across a curated rule pack.
- AdMerit and UniPredict — Strong interactive admissions What-if exploration / focused on admissions outcomes and
  option exploration rather than verified cross-opportunity action planning.

## Unique Selling Points

- Automatically enumerates ethical, controllable milestones; it does not ask the student to invent arbitrary
  slider experiments.
- Measures verified marginal opportunity gain across multiple doors, then removes actions that miss relevant
  deadlines or exceed selected constraints.
- Presents a Pareto-style Action Frontier, so “best” is always explained as a trade-off among impact, time, effort,
  cost, and source confidence.
- Keeps hard eligibility, manual review, soft competitiveness, and application readiness as distinct concepts.
- Links every rule result and state change to a source and verification date; unknown information is never treated
  as failure.
- Runs offline with deterministic shared Kotlin behavior on Android and Desktop; the same profile, pack, and clock
  produce the same result.
- Never recommends changing immutable or sensitive facts and never claims a probability of winning.

## Technical Notes

- Declared contest targets: Android and JVM Desktop. Existing iOS and Web starter targets are not claimed as Porta
  targets until their full product flows are verified and documented.
- Kotlin Multiplatform shared scope: immutable domain models, typed rule graph, eligibility engine, receipt builder,
  scenario evaluator, milestone generator, feasibility filters, Action Frontier ranking, repositories, Room data,
  ViewModels, and most Compose UI.
- Persistence: local Room database plus DataStore for lightweight preferences; no cloud account, Firebase client,
  backend, or AI dependency in P0.
- Opportunity data: bundled, versioned, manually curated pack with local import/export and deterministic validation.
- Architecture: presentation → domain → data; concrete repositories; use cases only for genuine multi-repository or
  business-rule orchestration.
- Performance targets: initial sample results in under 2 seconds on reference devices; a constraint or scenario
  change updates a 50-opportunity pack in under 250 ms after input settles; first demo preview in under 30 seconds.
- Reliability: scenario values never mutate the actual profile; a fixed clock is injectable for deadline tests;
  invalid or unsupported rules fail with diagnostics rather than silent coercion.
- Accessibility: keyboard-complete Desktop P0, screen-reader labels, visible focus, 48 dp mobile targets, no
  color-only statuses, and a reduced-motion equivalent for every portal animation.
- Package and app identity: `Porta`, application id `io.github.vietdsk6.porta`.

## Design Guidelines

- Selected direction: **Portal Atlas** — a dark editorial atlas of opportunities, route lines, source receipts, and
  arched portal shapes; purposeful rather than fantasy-game themed.
- Brand personality: honest, capable, calm, optimistic, and precise. Copy uses “meets encoded rules”, “one condition
  missing”, and “verify with provider”; never “perfect match”, “weak candidate”, or “chance of success”.
- Core palette: Ink `#0D1321`, elevated Ink `#171D2B`, Warm Ivory `#F6F1E7`, Electric Violet `#7C5CFF`, Signal Teal
  `#2DD4BF`, Verify Amber `#F4B860`, and Blocked Coral `#EF6A6A`.
- Typography: the existing Poppins family; strong editorial display numbers, compact card titles, and comfortable
  body copy. Do not add a font dependency for P0.
- Motion: a 320–450 ms **Portal Aperture** for changed doors, short constellation-line reveals for dependencies,
  and calm list reordering. Motion communicates causality and is replaced by an immediate text summary when reduced
  motion is enabled.
- State communication always combines label, icon/shape, and color. Source and verification information remains
  visually prominent rather than hidden in legal copy.
- No streaks, leaderboards, acceptance scores, confetti, mascot, or easter egg in P0; the product rewards clarity,
  not compulsive engagement.
