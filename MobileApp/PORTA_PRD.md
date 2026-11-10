# Porta — Product Requirements Document

> **Product name:** Porta
> **Document version:** 0.2
> **Status:** Trust-first core journey implemented; stabilization in progress
> **Date:** 24/10/2026
> **Contest deadline:** 07/12/2026, 23:59 CET
> **Target platforms:** Android + JVM Desktop
> **Delivery model:** Local-first Kotlin Multiplatform app
> **Contest monetization:** Completely free; no paywall, ads, subscription or paid feature gate

---

## 1. Executive summary

Porta helps students answer three questions:

1. Which opportunities pass all deterministic checks Porta can currently evaluate for my actual profile?
2. Why does each door need action, verification, or no further deterministic action right now?
3. Under my available time, budget and effort, which realistic next milestone creates the largest verified increase in accessible opportunities?

Porta is **not** another scholarship search feed, AI admissions predictor or profile score.

Its contest-scale core loop is:

```text
Actual profile
    ↓
Explainable published-check conclusions
    ↓
Action Frontier ranked by constraints
    ↓
Preview one milestone
    ↓
See exactly which doors change
    ↓
Track the milestone and later update the actual profile
```

### Product promise

> See which realistic next milestone creates the most verified progress—without pretending to predict who wins.

### Magic moment

The app proposes several controllable milestones. The student taps one such as `IELTS 6.5 → 7.0`. Only opportunity cards whose published-check conclusion changes are shown, while every change remains traceable to an official clause and verification date.

The user can then change available time, budget or effort. The Action Frontier reorders transparently.

---

## 2. Validation outcome and concept correction

The original concept was:

> Change one profile fact and watch opportunities open or close.

Competitor research found this interaction is not sufficiently novel on its own:

- [ScholarPulse](https://devpost.com/software/scholarpulse-p8dg3f) publicly describes a What-If Simulator where profile changes open or close scholarships.
- [AdMerit](https://admerit.ai/) and [UniPredict](https://unipredicts.com/) provide live counterfactual interactions for university options.
- [nrol](https://nrol.me/how-it-works) combines profile gaps, effort, time and deadlines into a next action.
- [OppPaths](https://opppaths.com/docs/scholarship-guides/what-is-a-scholarship-gap-analysis) separates hard/soft and fixable/fixed gaps.

Therefore, draggable what-if controls remain a supporting visualization, but the defensible product core becomes:

> A constrained Action Frontier that automatically enumerates controllable milestones, measures their verified marginal opportunity gain, removes options that cannot finish before deadlines, and explains the trade-off between impact, time, effort and cost.

### Novelty boundary

Porta must lead with:

- automatic discovery of controllable actions;
- cross-opportunity marginal impact;
- time/budget/effort constraints;
- deterministic hard-rule evaluation;
- source-level explanations;
- ethical treatment of immutable and sensitive facts.

Porta must not lead with:

- scholarship search;
- profile completeness;
- an AI score;
- arbitrary sliders;
- animated doors alone.

### Kill test result

The concept passed a 15-opportunity rule-encoding test with conditions:

- Published hard gates can be represented deterministically for a useful subset of programs.
- Real profile changes can move a student from `not currently eligible` to `meets encoded eligibility rules`.
- Many programs also contain soft, ambiguous or constituency-specific criteria that must never be converted into automatic pass/fail rules.
- Age, citizenship, protected categories, family income and participation history may be relevant context but must never become recommended changes.
- The product can only claim eligibility against the rules it has encoded and verified on a stated date.

Full validation data appears in Appendix A.

### Trust-first v0.2 correction

The implemented product deliberately uses more conservative language than the original concept:

- `OPEN` remains an internal serialized enum but is shown as **Current checks passed**.
- `HAS_GAPS` is shown as **Needs action**; Porta does not claim that one action is always enough.
- `NEEDS_INFO` is shown as **Needs verification**; it is never presented as a successful result.
- Completing a plan updates plan status only. An explicit confirmation, with evidence required for programme-scoped
  checks, is the only path that changes the actual profile.
- Global facts and programme-scoped assertions are different data. A scoped assertion is keyed by profile,
  opportunity and rule, and is accepted only while its rule fingerprint still matches the installed pack.
- What-if always states that the saved profile is unchanged and renders only doors whose evaluated conclusion changes.

This correction removes cross-opportunity availability leakage and avoids turning a deterministic rule check into a
claim about admission, selection or funding.

---

## 3. Product vision

### Long-term vision

Help students move from discovering an opportunity to deliberately building the capabilities and evidence needed to access it.

### Contest product

A small, polished planning engine that works completely offline with a curated opportunity pack and demonstrates:

- explainable eligibility;
- ethical action generation;
- transparent trade-offs;
- a memorable cross-opportunity simulation;
- a strong Android/Desktop KMP story.

### Product thesis

Students do not only suffer from a shortage of opportunity listings. They also lack an honest answer to:

> Given my actual profile and limited resources, what controllable milestone would create the most new possibilities?

Static matching shows the present. Porta is designed to explore the **reachable future** without confusing eligibility with probability of success.

---

## 4. Goals and non-goals

## 4.1 P0 goals

By submission, Porta must:

1. Store an actual student profile locally.
2. Evaluate a curated catalog using deterministic, source-linked hard rules.
3. Distinguish pass, fail, missing information and manual review.
4. Never merge hypothetical values into the real profile automatically.
5. Generate only controllable, ethically valid candidate milestones.
6. Rank milestones by impact subject to user-selected time, effort and budget.
7. Preview the exact opportunities changed by one milestone.
8. Explain every opportunity state and action ranking in plain language.
9. Run offline on Android and Desktop.
10. Put the rule engine, scenario engine, Action Frontier, persistence and most UI in shared Kotlin.

## 4.2 Explicit non-goals

The contest version will not include:

- admission or scholarship-award probability;
- automatic web crawling;
- automatic application submission;
- essay, SOP, CV or recommendation generation;
- chatbot or AI mentor;
- community feed;
- mentor/alumni marketplace;
- institutional analytics;
- cloud accounts or sync;
- Firebase;
- paywalls, subscriptions, credit packs or ads;
- social verification of evidence;
- global coverage;
- automatic grade-scale conversion;
- automatic interpretation of arbitrary legal/eligibility text;
- claims that a student will win, be admitted or receive funding.

---

## 5. Target users

## 5.1 Primary persona — Lan

Lan is a second- or third-year IT student.

- She has a GPA, English result, projects and activities scattered across multiple files.
- She regularly encounters scholarships, fellowships, contests, summer programs and internships.
- She does not know which opportunities are actually accessible.
- She has limited time and money, so she cannot improve everything at once.
- She distrusts unexplained match percentages.

### Primary job to be done

> When I am planning the next several months, show which controllable milestone creates the most useful new opportunities under my actual constraints, so I can invest my effort deliberately.

## 5.2 Secondary persona — Minh

Minh is a lecturer, counselor or student advisor.

- He frequently forwards opportunities to students.
- He repeats the same eligibility explanation.
- He wants students to examine official sources and make realistic plans.

### Secondary job to be done

> When I recommend an opportunity, let me encode its published requirements once and give students a transparent self-checking tool.

Minh is not required for P0 adoption. The bundled opportunity pack makes the product valuable without a teacher account.

## 5.3 Desktop planner

The same student may use Desktop for deeper comparison.

> When I compare semester-level plans, let me inspect several milestones and their trade-offs on a larger screen without modifying my actual profile.

---

## 6. Product principles

1. **Deterministic before intelligent.** Same input, catalog version and date must produce the same result on every platform.
2. **Eligibility is not probability.** The app evaluates published gates; it does not predict selection.
3. **Hard, soft and unknown are separate.** A soft criterion never becomes a fake numeric score.
4. **Unknown is not false.** Missing information must not silently disqualify the user.
5. **Actual and hypothetical never mix.** Scenarios are visibly separate overlays.
6. **Sources before recommendations.** Every rule has a source and last-checked date.
7. **Only controllable facts become actions.** Immutable or sensitive attributes may filter, never become advice.
8. **Constraints are explicit.** “Best” only means best under selected time, effort, budget and priorities.
9. **Local-first and private.** Academic and identity information stays on device.
10. **Actions, not judgment.** Use “one condition missing”, never “weak candidate”.
11. **One memorable interaction.** Tapping a milestone must visibly and accessibly reveal its marginal opportunity gain.

---

## 7. Product vocabulary and state model

### Door

An opportunity with a known source, cycle, deadline and eligibility rules.

### Actual profile

Facts the user currently asserts as true. A fact may optionally have evidence metadata and one app-private local
attachment. The attachment is user-provided supporting material, not proof independently verified by Porta.

### Scenario

A temporary overlay of hypothetical values. It never changes the actual profile until the user explicitly updates a fact separately.

### Milestone

A controllable future change represented by a target fact, expected duration, effort, cost band and completion evidence.

### Action Frontier

A small set of non-dominated milestones. An action is dominated when another action is at least as good on all selected dimensions and better on at least one.

Example dimensions:

- number of newly accessible saved opportunities;
- total newly accessible opportunities;
- completion before deadline;
- duration;
- effort;
- cost;
- rule confidence.

### Eligibility state

| Internal state | UI label | Meaning |
|---|---|---|
| `OPEN` | Current checks passed | All deterministic checks Porta can currently evaluate pass |
| `HAS_GAPS` | Needs action | At least one published check fails and an actionable path may exist |
| `NEEDS_INFO` | Needs verification | Missing, ambiguous or manual information prevents a conclusion |
| `NOT_ACTIONABLE` | Not available this cycle | A current-cycle requirement cannot be addressed through a profile update |
| `CLOSED` | Closed | Deadline has passed |

### Proximity

Proximity remains an internal planning concept and is not shown as a certainty claim:

- `ONE_STEP_AWAY`
- `MULTI_STEP`
- `MANUAL_REVIEW`
- `OUT_OF_REACH`

The UI must not say **one step away** unless the engine can prove that one checked requirement is the only remaining
deterministic blocker; the contest build uses the broader **Needs action** label instead.

### Requirement type

| Type | Engine behavior |
|---|---|
| `HARD` | Can contribute to deterministic eligibility |
| `MANUAL` | Requires the user to confirm or inspect an official source |
| `SOFT` | Display only; never changes eligibility automatically |
| `READINESS` | Application preparation checklist; never changes eligibility |

This distinction prevents “CV uploaded” or “essay completed” from falsely making a student eligible.

---

## 8. Scope and prioritization

## 8.1 P0 — required for contest submission

### Profile

- Create, edit and delete a local profile.
- Supported P0 fact types:
  - number with explicit unit/scale;
  - Boolean;
  - enum;
  - enum list;
  - date;
  - duration in months/hours;
  - text for display only.
- Mark a fact as self-reported or evidence-backed.
- Attach evidence metadata without bundling private binaries by default.
- Display which opportunity rules use each fact.

### Opportunity catalog

- Ship 15 manually checked opportunities or an equivalently rich verified pack.
- Display source URL, publisher, cycle, deadline and last-checked date.
- Search and filter by type, deadline and eligibility state.
- Save/unsave an opportunity.
- Archive expired cycles without deleting history.

### Decision explanation

Each opportunity must show:

- passed hard rules;
- failed hard rules;
- missing facts;
- manual checks;
- soft criteria separated from eligibility;
- actual value versus required value;
- source section/link and last-checked date;
- disclaimer to confirm with the provider.

### Action Frontier

- Generate milestones only from whitelisted actionable hard rules.
- Ask for planning constraints:
  - available horizon/date;
  - effort ceiling;
  - cost band;
  - saved-opportunity priority.
- Remove milestones impossible before relevant deadlines.
- Compute a Pareto frontier.
- Rank the frontier deterministically.
- Explain why each milestone appears.
- Never display the raw ranking score as objective truth.

### Preview

- Tap a milestone to preview it as a scenario.
- Show baseline and scenario counts.
- Animate only doors whose state changes.
- Show a text summary for reduced-motion and screen-reader users.
- Show exact rules responsible for every changed door.
- Reset to actual profile in one action.

### Planning

- Add a milestone to a local plan.
- Mark progress without changing eligibility.
- Completing a plan item must not update the actual profile automatically.
- Updating the actual profile requires explicit confirmation and optional evidence.

### Data packs

- Load a bundled pack.
- Import/export a versioned JSON opportunity pack.
- Validate the whole pack before writing.
- Reject unsupported rules and units with actionable diagnostics.
- Export the profile separately with a sensitive-data warning.

### Offline and privacy

- All P0 flows work offline after installation.
- No account, analytics SDK or cloud transmission.
- Provide data export and deletion.

## 8.2 P1 — only after every P0 acceptance criterion passes

- Desktop Opportunity Studio for form-based rule authoring.
- Side-by-side comparison of up to three scenarios.
- Deadline reminders.
- Local evidence file references. **Implemented for actual-result confirmation:** one image/PDF/TXT/DOC/DOCX file,
  up to 10 MB, copied into app-private storage; profile export carries only its descriptor.
- Opportunity pack update preview.
- CSV profile import/export.
- Small QR or file handoff for pack metadata; do not encode a large catalog directly in QR.
- Separate application-readiness checklist.

## 8.3 P2 — after contest

- Signed packs from schools or organizations.
- Public pack registry.
- Advisor/student shared workspace.
- Cloud synchronization.
- OCR or AI-assisted rule drafting with mandatory human confirmation.
- Automatic source-change detection.
- Mentor/alumni journeys.
- Application tracking.
- Community and cohort analytics.

---

## 9. Information architecture

## 9.1 Android navigation

Three bottom destinations:

1. **Doors**
2. **Next Steps**
3. **Profile**

What-if Preview is opened from a milestone or opportunity detail. It is not a permanent fourth tab.

## 9.2 Desktop navigation

Left navigation rail:

1. Overview
2. Compare
3. Next Steps
4. Profile
5. Data Packs
6. Opportunity Studio — P1 only

Desktop is not a stretched phone screen. It prioritizes:

- dense published-check matrices;
- actual versus scenario comparison;
- rule/source inspector;
- pack validation;
- keyboard-first navigation.

## 9.3 Screen inventory

### P0 screens

1. Welcome / privacy promise.
2. Sample profile or create profile.
3. Minimum profile setup.
4. Doors overview.
5. Opportunity detail and decision explanation.
6. Next Steps / Action Frontier.
7. Planning constraints sheet.
8. Milestone detail.
9. Scenario preview.
10. Actual profile.
11. Edit fact.
12. Local plan.
13. Data pack list/import diagnostics.
14. Settings/data controls.

### P1 screens

15. Opportunity Studio.
16. Pack update diff.
17. Reminder settings.

---

## 10. Core user flows

## 10.1 Judge/sample flow

```text
Welcome
→ Use ready-made fictional demo profile
→ Doors overview
→ Recommended next move
→ Compare degree, IELTS 6.5 → 7.0, and programme-verification moves
→ Preview only the doors whose checked conclusions change
→ Inspect exact official clauses
→ Adjust time/budget
→ Frontier reorders
```

Acceptance target: first meaningful preview in under 30 seconds.

## 10.2 Create-profile flow

```text
Welcome
→ Create my profile
→ Enter minimum facts
→ Unknown rules remain “Needs verification”
→ Doors overview
→ Add a missing fact from the decision explanation
→ Re-evaluate deterministically
```

Minimum profile entry must be progressive. Do not force the user to complete every field before seeing value.

## 10.3 Door-explanation flow

```text
Doors
→ Select opportunity
→ Decision explanation
→ See Current conclusion / Needs action / Needs verification / Passed checks / Soft factors
→ Open official source
→ Preview a mapped milestone
```

## 10.4 Action-Frontier flow

```text
Next Steps
→ Set horizon, effort and cost constraints
→ View 2–5 non-dominated milestones
→ Tap one milestone
→ Inspect doors that would pass current checks / address one check / remain unchanged
→ Inspect deadline feasibility
→ Add milestone to plan
```

## 10.5 Actual-growth flow

```text
Planned milestone
→ Mark work complete
→ App asks whether the actual fact has changed
→ User confirms new value
→ Optional evidence metadata
→ Actual profile updates
→ Doors re-evaluate
```

The app must never infer that completing a checklist item proves a fact.

## 10.6 Import flow

```text
Data Packs
→ Choose JSON file
→ Parse to temporary DTO
→ Validate schema, IDs, units, sources and action references
→ Show summary or errors
→ Confirm
→ Transactional import
```

---

## 11. Detailed functional requirements

## FR-1 Profile facts

- Every numeric fact must include a unit or scale.
- GPA must store the original scale.
- The engine must not silently convert between GPA scales.
- Language tests must include test type, score and optional date.
- Experience must specify its unit and, where relevant, whether it occurs after graduation.
- Citizenship, age and protected-category facts must be flagged `CONTEXT_ONLY` or `IMMUTABLE` for action generation.
- Users may delete any optional fact.
- Deleted/missing facts evaluate as `UNKNOWN`, not `FAIL`.

## FR-2 Opportunity provenance

Every opportunity record must include:

- stable ID;
- title and provider;
- category;
- source URL;
- cycle label;
- last-checked date;
- deadline or explicit `TBA`;
- publisher/pack ID;
- rule version;
- human-readable disclaimer.

No record may claim `verified` without a documented manual checking process and timestamp.

## FR-3 Rule evaluation

- P0 supports `ALL`, `ANY`, `ATOMIC` and `MANUAL_CHECK`.
- P0 intentionally excludes arbitrary scripts, regex and free-form formulas.
- P0 operators:
  - `EXISTS`
  - `EQUALS`
  - `NOT_EQUALS`
  - `GREATER_THAN_OR_EQUAL`
  - `LESS_THAN_OR_EQUAL`
  - `IN_SET`
  - `CONTAINS_ANY`
  - `CONTAINS_ALL`
  - `DATE_BEFORE`
  - `DATE_AFTER`
- Unsupported unit comparison returns `UNKNOWN`.
- Manual rules never return automatic `PASS`.
- Soft rules never affect the root eligibility state.

## FR-4 Rule receipts

Every evaluated rule must expose:

- result;
- actual value if present;
- expected value if representable;
- explanation;
- source section or note;
- rule type;
- actionability class;
- scenario override responsible for a change, when applicable.

## FR-5 Action generation

- Candidate actions come only from explicit, curated `ActionTemplate` records.
- A failed rule without an action template remains a gap but creates no advice.
- Actions targeting immutable/context-only facts are rejected at pack validation.
- Manual and soft requirements may generate “Review source” or “Build evidence” tasks, but those tasks must not claim to unlock eligibility.
- Similar actions across opportunities are deduplicated.
- Multi-step actions are P1 unless representable as a single milestone with one target fact.

## FR-6 Constraint-aware feasibility

Each action may include:

- minimum duration;
- typical duration range;
- effort level 1–5;
- cost band: free, low, medium, high, unknown;
- earliest completion date;
- accepted evidence types;
- confidence/source note.

The user supplies:

- planning horizon/date;
- maximum effort;
- maximum cost band;
- opportunity priorities.

An action that cannot finish before a door's deadline contributes zero impact value for that door.

## FR-7 Action Frontier

- Compute the non-dominated action set first.
- Rank only within that frontier.
- Show no more than five actions by default.
- Each action explanation must name:
  - saved doors that would pass current checks;
  - total doors that would pass current checks;
  - doors moved closer;
  - earliest affected deadline;
  - expected duration, effort and cost;
  - excluded doors and reason.
- Raw scores remain internal.

## FR-8 Scenario preview

- A scenario is a sparse overlay on the actual profile.
- Scenario UI has a persistent `Simulation` label.
- Scenario changes are never written to the actual profile.
- Reset produces exactly the baseline result.
- Only changed door cards animate.
- Reduced motion replaces movement with state transition/highlight and a live-region summary.

## FR-9 Saved opportunities

- Users can save a door.
- Saved doors receive priority only when the user enables that preference.
- Saving does not affect eligibility.
- The ranking explanation must disclose when saved-door weighting changes the order.

## FR-10 Local plan

- Add/remove milestone.
- Planned, in progress and completed states.
- Optional target date and note.
- Completion alone never changes a fact.
- The app offers an explicit `Update actual profile` action after completion.

## FR-11 Import/export

- Validate fully before persistence.
- Import within one database transaction.
- Same pack and version is a no-op.
- Newer version shows a summary before replacement.
- Conflicting publisher/ID requires cancellation or new ID.
- Unsupported future schema fails safely.
- Export/import round-trip preserves evaluation behavior.

## FR-12 Data controls

- Export profile metadata.
- `Clear academic profile and plans` deletes the current profile's facts, programme-scoped confirmations and plans,
  while retaining saved doors, the opportunity catalog and planning preferences.
- `Reset Porta on this device` deletes facts, confirmations, plans and saved doors for every local profile, removes
  imported catalogs and clears preferences, then restores the bundled catalog.
- Both destructive actions list their exact removal and retention scope before confirmation.
- Show which sensitive fields are included before export.
- Evidence binary files are excluded from P0 export; metadata and source references only.

---

## 12. Rule semantics

## 12.1 Atomic results

| Status | Meaning |
|---|---|
| `PASS` | Known fact satisfies the hard rule |
| `FAIL` | Known fact contradicts the hard rule |
| `UNKNOWN` | Fact is absent or units cannot be compared safely |
| `REVIEW` | Rule cannot be decided structurally |

## 12.2 Compound truth tables

### `ALL`

- Any `FAIL` → `FAIL`.
- Otherwise any `REVIEW` → `REVIEW`.
- Otherwise any `UNKNOWN` → `UNKNOWN`.
- All `PASS` → `PASS`.

### `ANY`

- Any `PASS` → `PASS`.
- If none pass and any `REVIEW` → `REVIEW`.
- If none pass/review and any `UNKNOWN` → `UNKNOWN`.
- All `FAIL` → `FAIL`.

## 12.3 Deadline precedence

1. If a known deadline has passed, UI state is `CLOSED`.
2. If the cycle is `TBA`, the app may evaluate profile compatibility but must not claim the user can currently apply.
3. If deadline is missing, show `Verify deadline`.

## 12.4 Hard versus soft example

For Chevening:

- `2,800 hours of post-graduation work experience` may be encoded as a hard rule.
- `Leadership potential` is soft and must not become a score.
- `Return to country for at least two years` is a hard commitment/manual confirmation.
- Actual selection probability is outside the product.

---

## 13. Domain model

The following structures express product behavior, not final file layout.

```kotlin
data class Profile(
    val id: String,
    val facts: Map<FactKey, ProfileFact>,
    val evidence: List<Evidence>,
    val updatedAt: Instant,
)

data class ProfileFact(
    val key: FactKey,
    val value: FactValue,
    val provenance: FactProvenance,
    val actionability: Actionability,
    val evidenceIds: Set<String>,
    val updatedAt: Instant,
)

sealed interface FactValue {
    data class NumberValue(val amount: Double, val unit: String) : FactValue
    data class BooleanValue(val value: Boolean) : FactValue
    data class EnumValue(val value: String) : FactValue
    data class EnumListValue(val values: Set<String>) : FactValue
    data class DateValue(val value: LocalDate) : FactValue
    data class DurationValue(val amount: Int, val unit: DurationUnit) : FactValue
    data class TextValue(val value: String) : FactValue
}

enum class Actionability {
    CONTROLLABLE,
    TIME_DEPENDENT,
    CONTEXT_ONLY,
    IMMUTABLE,
    UNKNOWN,
}
```

```kotlin
data class Opportunity(
    val id: String,
    val title: String,
    val provider: String,
    val category: OpportunityCategory,
    val cycle: String,
    val sourceUrl: String,
    val sourceSection: String?,
    val lastCheckedOn: LocalDate,
    val deadline: Deadline,
    val eligibilityRule: RuleNode,
    val softCriteria: List<InformationalCriterion>,
    val readinessItems: List<ReadinessItem>,
    val actionTemplates: List<ActionTemplate>,
    val schemaVersion: Int,
)
```

```kotlin
sealed interface RuleNode {
    data class All(val children: List<RuleNode>) : RuleNode
    data class Any(val children: List<RuleNode>) : RuleNode
    data class Atomic(
        val id: String,
        val factKey: FactKey,
        val operator: Operator,
        val expected: FactValue,
        val explanation: String,
        val sourceNote: String?,
        val actionTemplateId: String?,
    ) : RuleNode
    data class ManualCheck(
        val id: String,
        val explanation: String,
        val sourceNote: String?,
    ) : RuleNode
}
```

```kotlin
data class ActionTemplate(
    val id: String,
    val title: String,
    val targetFactKey: FactKey,
    val targetValue: FactValue,
    val minimumDurationDays: Int?,
    val typicalDurationDays: IntRange?,
    val effort: EffortLevel,
    val costBand: CostBand,
    val completionEvidenceTypes: Set<EvidenceType>,
    val sourceNote: String?,
)
```

```kotlin
data class Scenario(
    val id: String,
    val name: String,
    val overrides: Map<FactKey, FactValue>,
    val planningConstraints: PlanningConstraints,
    val createdAt: Instant,
)
```

---

## 14. Action Frontier algorithm

## 14.1 Candidate generation

For every active opportunity:

1. Evaluate actual profile.
2. Collect failed actionable atomic hard rules.
3. Resolve each rule's explicit action template.
4. Reject any template targeting `IMMUTABLE` or `CONTEXT_ONLY` facts.
5. Deduplicate actions by target fact and target value.

No free-form AI action generation is allowed in P0.

## 14.2 Scenario evaluation

For each candidate action:

1. Apply target value as a temporary overlay.
2. Re-evaluate every active opportunity.
3. Record exact state deltas.
4. Remove deltas whose relevant deadline precedes the earliest plausible completion date.
5. Record source confidence and manual-review dependencies.

## 14.3 Pareto frontier

Action A dominates action B only when A is:

- no worse in saved doors that would pass current checks;
- no worse in total doors that would pass current checks;
- no worse in duration;
- no worse in effort;
- no worse in cost;

and strictly better in at least one dimension.

Only non-dominated actions enter the default frontier.

## 14.4 Deterministic ranking

The UI should use an explainable lexicographic order rather than present a mysterious number:

1. Feasible within the selected horizon.
2. More saved opportunities would pass current checks.
3. More total opportunities would pass current checks.
4. More published checks would be addressed.
5. Earlier affected deadline.
6. Shorter duration.
7. Lower effort.
8. Lower cost.
9. Stable action ID as final tie-breaker.

The app may compute an internal score for tests, but it must explain rankings in factors, for example:

> Ranked first because 3 saved opportunities and 2 others would pass their current checks before their deadlines. Estimated duration: 8–10 weeks. Effort: high. Cost: medium.

## 14.5 Ethical rules

The following can never be generated as recommended changes:

- nationality/citizenship;
- ethnicity, caste or protected category;
- family income;
- age/date of birth;
- disability status;
- family relationships;
- prior award/participation history;
- sanctions/residency history;
- any fact whose change would require deception.

These facts may still determine that an opportunity is unavailable.

---

## 15. UX requirements

## 15.1 Visual metaphor

Doors are a state metaphor, not literal 3D doors everywhere.

Recommended presentation:

- opportunity cards with a clear checked-conclusion state;
- subtle opening transition when status changes;
- delta badges such as `3 would pass current checks`;
- a comparison ribbon showing `Actual → Preview`;
- accessible labels on every state.

Avoid a fantasy-game map that makes serious eligibility data feel arbitrary.

## 15.2 Doors overview

Must show:

- Current checks passed;
- Needs action;
- Needs verification;
- Not available this cycle;
- Closed.

Every group has an explanation and count. Color is supplementary, never the only signal.

## 15.3 Opportunity decision explanation

Recommended sections:

1. Current conclusion.
2. Needs action.
3. Needs verification.
4. Passed checks.
5. Soft selection factors.
6. Source, section and last checked date.

## 15.4 Action Frontier card

Each card displays:

- milestone title;
- doors that would pass current checks;
- saved doors affected;
- duration range;
- effort;
- cost band;
- earliest relevant deadline;
- confidence/manual-review warning;
- `Preview impact` primary action.

## 15.5 Scenario preview

- Persistent `Simulation — your actual profile is unchanged` banner.
- Actual and hypothetical value shown together.
- Changed cards animate once.
- Summary uses precise wording:
  - `3 opportunities would pass the current checked requirements.`
  - Not `You unlocked 3 scholarships you can win.`
- Reset and Add to Plan are distinct actions.

## 15.6 Language and tone

Use:

- “Current checks passed.”
- “At least one published check needs action.”
- “This programme needs verification against the official source.”
- “Source checked on 20 Oct 2026.”
- “Always confirm on the official page.”

Avoid:

- weak/strong candidate;
- chance of winning;
- guaranteed match;
- perfect fit;
- best action without named constraints;
- verified/current/real-time without a timestamp.

---

## 16. Platform requirements and KMP story

## 16.1 Android

Optimized for short, frequent actions:

- edit one fact;
- review current doors;
- inspect next milestone;
- run a quick preview;
- update plan progress;
- attach local evidence metadata;
- import a pack from a file/share flow;
- receive deadline reminders — P1.

## 16.2 Desktop

Optimized for planning and inspection:

- side-by-side actual/scenario comparison;
- dense requirement matrix;
- full receipt/source inspector;
- pack import/export and diagnostics;
- keyboard-first planning;
- Opportunity Studio — P1.

## 16.3 Shared Kotlin

Must live in common code:

- profile/opportunity/rule models;
- rule evaluator;
- truth-table semantics;
- scenario overlay;
- action candidate generation;
- deadline feasibility;
- Pareto frontier;
- deterministic ranking;
- pack serialization/validation;
- repositories and Room entities where supported;
- ViewModels/UI state;
- most Compose UI;
- common tests.

Platform-specific code remains narrow:

- Android notification and share/file hooks;
- Desktop drag-and-drop/menu/window behavior;
- local file-path handling.

## 16.4 Architecture constraint

P0 is local-first and requires no Firebase. This avoids web-target incompatibility and keeps the contest build reliable.

---

## 17. Persistence and data portability

Suggested local entities:

- `ProfileEntity`
- `ProfileFactEntity`
- `EvidenceEntity`
- `OpportunityPackEntity`
- `OpportunityEntity`
- `SavedOpportunityEntity`
- `ScenarioEntity`
- `ScenarioOverrideEntity`
- `ActionTemplateEntity`
- `PlannedMilestoneEntity`

### File formats

- `.unlockpack.json` — opportunity and action rules only.
- `.unlockprofile.json` — private profile/evidence metadata.
- `.unlockscenario.json` — optional scenario.

If the product is renamed, extensions can change before public release.

### Pack validation

A pack is invalid if it contains:

- duplicate IDs;
- unknown fact types/operators/units;
- an action targeting an immutable/context-only fact;
- missing source URL or last-checked date;
- referenced action IDs that do not exist;
- hard rules containing free-form text evaluation;
- unsupported future schema;
- a deadline earlier than its cycle without an explicit archive flag.

Invalid packs must never partially write to Room.

---

## 18. Opportunity content strategy

## 18.1 Contest dataset

Target:

- minimum 15 thoroughly checked records;
- stretch 30–50 only if core product is already stable;
- multiple opportunity types, but every record must demonstrate a supported rule pattern;
- at least five actions that produce meaningful cross-opportunity deltas;
- at least three `NEEDS_INFO` examples;
- at least three legitimate immutable blockers;
- at least three soft-criteria examples.

Quality is more important than count.

## 18.2 Manual curation workflow

For every opportunity:

1. Read the official page/rules.
2. Record cycle and checked date.
3. Separate hard, manual, soft and readiness criteria.
4. Encode only rules that can be represented safely.
5. Add a source note to every hard rule.
6. Validate against at least two profiles.
7. Have the rule receipt reviewed against the source.
8. Mark uncertain rules `MANUAL`, never guess.

## 18.3 Update policy

- Expired records remain viewable but do not contribute to current recommendations.
- A new cycle is a new version of the opportunity.
- A TBA deadline never inherits last year's deadline automatically.
- Changed source requirements require a new checked date and pack version.

---

## 19. Privacy, trust and safety

- All personal data stays local in P0.
- No third-party analytics SDK.
- No cloud backup by default.
- Export warns about GPA, nationality, age and other sensitive fields.
- Delete clears profile, scenarios and plans.
- Official providers are not presented as partners.
- Brand names/logos are used only as factual references where permitted; the demo may prefer text to avoid implied endorsement.
- The app displays a persistent disclaimer:

> Porta evaluates only the published rules encoded in this catalog. It does not guarantee eligibility, admission, selection or funding. Always confirm on the official source.

---

## 20. Accessibility and internationalization

- Minimum 48dp touch targets.
- State is represented by text + icon + color.
- TalkBack/VoiceOver-equivalent semantics in shared UI where available.
- Desktop keyboard navigation and visible focus.
- Live-region announcement after scenario changes.
- Reduced-motion mode.
- Layouts remain usable at 200% text size.
- Explicit units for GPA, scores, experience and currency.
- Dates display in a locale-aware format while retaining an unambiguous ISO representation in detail views.
- Do not infer GPA equivalence across countries.
- English is the required contest language.
- Vietnamese localization is P1 only after English P0 is stable.

---

## 21. Success criteria

## 21.1 Product success for contest

- A first-time judge reaches the magic preview in under 30 seconds using sample mode.
- A judge can install one clearly labelled fictional profile locally, then edit facts, save a plan and exercise the
  actual-result/evidence flow without entering personal data.
- The core concept is explainable in one sentence.
- The 3–5 minute demo shows input, deterministic transformation, result and KMP value.
- At least five curated milestones create legitimate state deltas in the bundled catalog.
- No demo claim confuses eligibility with award probability.

## 21.2 Technical success

- Same profile + pack + clock produces identical results on Android and Desktop.
- Pure engine re-evaluates 100 opportunities in under 100 ms on ordinary development hardware.
- P0 works fully offline.
- Invalid imports are atomic and non-destructive.
- Shared tests cover all truth tables, frontier rules and ranking tie-breaks.
- Scoped quality gates pass.

## 21.3 Trust success

- 100% of hard rules in the bundled pack have a source note and last-checked date.
- Missing data always appears as unknown/verify.
- Soft criteria never change deterministic eligibility.
- Immutable/context-only facts never produce recommended actions.
- Every recommended milestone explains its impact and constraints.

---

## 22. Acceptance criteria

## 22.1 Rule engine

- `ALL` and `ANY` conform to documented truth tables.
- Missing facts return `UNKNOWN`.
- Incompatible units return `UNKNOWN`.
- Manual rules never return automatic `PASS`.
- Soft/readiness rules never affect eligibility state.
- Closed deadline overrides current-application presentation.
- TBA deadline does not present `Apply now`.

## 22.2 Action generation

- Every candidate action references a curated template.
- Immutable/context-only target facts fail pack validation.
- Actions infeasible before every relevant deadline are hidden from the default frontier.
- The same inputs produce a stable frontier and order.
- The explanation factors match the actual state deltas.

## 22.3 Scenario

- Preview never writes to actual profile storage.
- Reset restores exact baseline results.
- Changed-card count matches engine deltas.
- Screen readers receive a useful summary.
- Reduced motion has no loss of information.

## 22.4 Profile and plan

- Completing a plan item does not change actual facts.
- Updating an actual fact requires explicit confirmation.
- Deleting a fact produces unknown results where appropriate.
- Export clearly lists included sensitive field categories.

## 22.5 Import/export

- Invalid pack does not partially modify storage.
- Round-trip preserves rules and evaluation outcomes.
- Unsupported schema produces a clear error.
- Sources, cycles and checked dates survive round-trip.

## 22.6 UX

- Sample magic moment is reachable in under 30 seconds.
- Core flows work offline.
- Android back navigation is correct.
- Desktop is fully usable by keyboard for P0 screens.
- Primary actions remain visible at 200% font size.

---

## 23. Test strategy

## 23.1 Common unit tests

- Parameterized atomic operators.
- `ALL`/`ANY` truth tables.
- Unknown and incompatible-unit cases.
- Deadline boundaries with an injected fixed clock.
- Scenario immutability.
- Action-template validation.
- Immutable-action rejection.
- Candidate deduplication.
- Deadline feasibility.
- Pareto dominance.
- Deterministic ranking and tie-breaks.
- Soft/readiness isolation.
- JSON schema validation and round-trip.
- Repository/database flows.

## 23.2 Compose UI tests

- Sample onboarding.
- Create/edit/delete fact.
- Opportunity decision-explanation sections.
- Constraint editing.
- Frontier ordering.
- Preview changed doors.
- Reset scenario.
- Add milestone to plan.
- Explicit actual-profile update.
- Invalid import state.
- Large-text and reduced-motion variants where practical.

## 23.3 Manual validation

- Android phone portrait.
- Android landscape/tablet preview.
- Desktop narrow and wide window.
- Keyboard-only Desktop.
- Screen-reader semantics.
- Offline cold launch.
- Import/export using a clean install.
- Every bundled rule receipt compared with its official source.

## 23.4 Repository quality gates

Run only the repository-approved scoped gates:

```bash
./gradlew spotlessCheck
./gradlew :shared:jvmTest :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
```

Run the web compile task only if Porta changes web-relevant code that requires validation. Do not run aggregate `check`, `build` or iOS tests for routine validation.

---

## 24. Risks and mitigations

| Risk | Severity | Mitigation |
|---|---:|---|
| Looks like ScholarPulse/nrol | Critical | Lead with constrained Action Frontier and verified marginal opportunity gain, not what-if alone |
| Opportunity rules become stale | High | Source URL, cycle, pack version, checked date, archive policy |
| Hard and soft criteria get mixed | Critical | Separate data types, engine isolation and acceptance tests |
| “Best step” sounds objective | High | Require explicit constraints; show factors, not raw score |
| Sensitive facts become recommendations | Critical | Actionability enum plus pack-validation rejection |
| Catalog work consumes development time | High | 15 high-quality records; 30–50 only as stretch |
| Few real mutable hard gates | High | Curate suitable opportunity categories; show honest no-action state |
| User fakes a scenario | Medium | Persistent Simulation label and explicit actual-profile update |
| Desktop looks like stretched mobile | Medium | Matrix, comparison and pack inspector |
| KMP appears superficial | High | Share evaluator, frontier, ranking, persistence, UI state and most UI |
| GPA conversion produces false conclusions | Critical | Exact scales only; incompatible scale → unknown |
| UI becomes a spreadsheet | Medium | Progressive disclosure, strong door/delta visual system |
| Name “Porta” is unownable | Medium | Treat as working title; run naming/trademark check before store release |
| Vibecoding introduces silent rule bugs | Critical | Engine-first, fixed fixtures, exhaustive common tests before UI polish |

---

## 25. Delivery plan — 49 days

Days 47–49 are protected submission buffer, not feature time.

| Days | Workstream | Exit criterion |
|---:|---|---|
| 1–3 | Freeze PRD, storyboard and visual direction | P0/kill list approved; sample demo written |
| 4–10 | Domain model and deterministic engine | Truth tables, rule receipts and deadline logic fully tested |
| 11–15 | Action generation and frontier | Immutable guardrails, feasibility, Pareto and ranking tests pass |
| 16–20 | Room and data packs | Bundled pack loads; validation/import/export atomic |
| 21–26 | Profile and Doors UX | Actual profile → explainable receipt works on Android/Desktop |
| 27–31 | Frontier and preview UX | Constraint change → rank → preview doors is polished |
| 32–35 | Local plan and actual update loop | Milestone → plan → explicit fact update closes the loop |
| 36–39 | Desktop differentiation | Matrix/compare/pack inspector usable by keyboard |
| 40–43 | Accessibility, states and performance | Offline, 200% text, reduced motion, empty/error/unknown complete |
| 44–46 | Quality gates and content audit | Tests/build pass; all bundled rules source-reviewed |
| 47–49 | README, essay, screenshots, video, clean clone | Submission ready and sent early |

### Scope kill list

The following are automatically cut if P0 slips:

1. Opportunity Studio.
2. QR handoff.
3. Reminders.
4. Scenario comparison beyond one preview.
5. Vietnamese localization.
6. Cloud-synced or binary-embedded evidence export; local app-private attachments may ship only after P0 stays green.
7. Any AI feature.

---

## 26. Contest demo script

## 0:00–0:25 — problem

Show a student with multiple opportunity tabs and generic advice.

> Lan does not need another list. She needs to know what she can access now, why the other doors are closed, and which realistic next milestone creates the most new possibilities.

## 0:25–0:50 — actual profile

Open Android sample profile:

- Vietnamese undergraduate IT student;
- explicit GPA scale;
- current semester count;
- English score;
- experience duration;
- selected time/budget constraints.

Show counts:

- current checks passed;
- needs action;
- needs verification;
- not available this cycle.

Mention local-only/offline.

## 0:50–1:20 — decision explanation

Open one opportunity:

- two hard requirements pass;
- one measurable requirement fails;
- one soft criterion is displayed separately;
- source URL and checked date are visible.

> Porta does not say “84% match.” It shows exactly which published checks were evaluated, which need action, and which still need verification.

## 1:20–2:05 — Action Frontier

Open Next Steps. Show three non-dominated milestones with different trade-offs:

- higher impact but longer/more costly;
- smaller impact but feasible before an early deadline;
- low-cost milestone affecting saved doors.

Change the planning horizon or cost ceiling. The frontier reorders.

## 2:05–2:45 — magic preview

Tap one milestone.

- Opportunity cards that truly change state animate.
- A text summary reports exact deltas.
- Immutable facts remain locked context, not suggestions.
- Tap one changed door and show the official clause that caused it.

Reset and show the actual profile unchanged.

## 2:45–3:15 — growth loop

Add the milestone to the local plan. Demonstrate that checking it complete does not falsify the profile. Then explicitly update the real fact with evidence metadata. Actual door states re-evaluate.

## 3:15–3:55 — Desktop/KMP

Switch to Desktop:

- actual versus scenario matrix;
- rule/source inspector;
- pack validation/import diagnostics.

Explain that the evaluator, action generation, frontier, ranking, persistence, ViewModels and most UI are shared Kotlin.

## 3:55–4:25 — trust and close

Show:

- `Verify` state;
- soft criteria separated from hard eligibility;
- last-checked date;
- keyboard navigation/reduced motion;
- local data controls.

Close with:

> Porta does not predict a student's future. It gives them an honest, source-linked map of the next possibilities they can realistically create.

---

## 27. Naming

`Porta` is the selected product and repository-facing name. It evokes a doorway without reducing the product to
the door animation: the differentiator remains the constrained Action Frontier behind it.

Before a public store release, perform domain, app-store and trademark checks. That check is a publishing task and
does not reopen the contest working name unless a material conflict is found.

---

## 28. Open decisions

These decisions should be resolved by Day 3:

1. Exact opportunity categories in the bundled pack.
2. Five to eight controllable fact types supported by the Action Frontier.
3. Which 3–5 real state transitions will be the official demo fixtures.

Recommended defaults:

- Bundle academic programs, student contests and structured internships with strong hard rules.
- Use cost bands, never pretend globally accurate currency estimates.
- Store evidence metadata in P0, no binary bundle.
- Desktop P0 has read-only inspector/import diagnostics; authoring is P1.

---

# Appendix A — 15-opportunity kill test

Checked on 20/10/2026. This table validates the model; it is not automatically the final bundled catalog. Always re-check before shipping.

| # | Opportunity | Encodable hard gates | Actionable versus non-actionable | Rule caveat |
|---:|---|---|---|---|
| 1 | [Kotlin Student Coding Competition](https://kotlinconf.com/contest/) | Age, student/recent graduate status, solo entry, new project, KMP ≥2 platforms, public GitHub and accepted license | Project/platform/repo/license are actionable; age and sanctions/residency are not | Essay and product quality are not eligibility predictions; see [rules](https://kotlinconf.com/contest-rules/) |
| 2 | [Erasmus Mundus Joint Masters](https://erasmus-plus.ec.europa.eu/opportunities/individuals/students/erasmus-mundus-joint-masters) | Bachelor completed or final year with completion before master begins | Degree completion and course/language requirements may be time-dependent | Each consortium has separate rules/deadline; best-ranked selection is soft |
| 3 | [Chevening](https://www.chevening.org/resource-hub/guidance/eligibility/) | Eligible citizenship, return commitment, degree timing, post-graduation work hours, course/offer requirements | Experience, time since degree and offers are time-dependent; citizenship/conflicts are not | Leadership/networking are soft; use official [timeline](https://www.chevening.org/scholarships/application-timeline/) |
| 4 | [Rhodes Scholarship](https://www.rhodeshouse.ox.ac.uk/scholarships/applications/) | Constituency-specific age, citizenship/residency/education and Oxford course eligibility | Degree/course may be actionable; age/citizenship/history are not | Rules vary by constituency; character/service/leadership are soft |
| 5 | [Schwarzman Scholars](https://www.schwarzmanscholars.org/admissions/application-instructions/) | Age range, undergraduate completion and accepted English test or exemption | Graduation/English may be actionable; DOB/passport category are not | Leadership/intellectual ability/character are soft |
| 6 | [France Excellence Eiffel](https://www.campusfrance.org/fr/le-programme-de-bourses-france-excellence-eiffel) | Foreign nationality, age cap, eligible field and French-institution nomination | Course selection/nomination process is actionable; age/nationality are not | Institutional internal deadline may be earlier; excellence is soft |
| 7 | [DAAD EPOS](https://static.daad.de/media/daad_de/pdfs_nicht_barrierefrei/in-deutschland-studieren-forschen-lehren/daad_programmbroschuere_epos_2026-27.pdf) | Developing-country scope, relevant bachelor, professional experience and course-specific rules | Experience/language/course fit may be actionable | Above-average performance and motivation are soft; rules vary by course |
| 8 | [JJ/WBGSP](https://www.worldbank.org/en/programs/scholarships/jj-wbgsp) | Eligible citizenship, bachelor, paid development-related work and participating-program admission | Experience/admission are time-dependent; citizenship/history are not | Development commitment is soft; use current participating-program list |
| 9 | [OECD Internship](https://www.oecd.org/en/about/careers/internships.html) | Full-time enrollment throughout, relevant field, availability and language | Enrollment timing, availability and language may be actionable | Nationality is allocation preference, not always a universal hard rejection; “excellent/good” is soft |
| 10 | [UNICEF Internship](https://www.unicef.org/careers/internships) | Age, enrollment/recent graduation, language and family-conflict rules | Language/enrollment timing may be actionable; DOB/family relation are context | Vacancy-specific experience and motivation remain soft/manual |
| 11 | [World Bank Treasury student careers](https://treasury.worldbank.org/en/about/unit/treasury/about/student-and-graduate-careers) | Degree stage/graduation window, good standing, full-period availability and no conflicting job | Availability/conflicting work may be actionable; education timing is time-dependent | Finance/Python/leadership strengths are soft; closing date may require manual check |
| 12 | [ESA Student Internships](https://www.esa.int/About_Us/Careers_at_ESA/Student_Internships2) | Enrollment, citizenship set, duration, application limit and prior internship | Enrollment/timing/posting fit may be actionable; citizenship/history are not | Final/penultimate Master preference should not be encoded as a universal hard gate |
| 13 | [CERN openlab Summer Student](https://openlab.web.cern.ch/cern-openlab-summer-student-programme/) | Degree type, semesters completed, enrollment/recent graduation, computing profile, prior CERN limits and English | Semester/enrollment/English may be time-dependent | Strong computing profile is soft; future cycle/deadline may be TBA; see [FAQ](https://openlab.web.cern.ch/faqs-summer-student-programme/) |
| 14 | [Google Summer of Code](https://developers.google.com/open-source/gsoc/faq) | Age, work eligibility, student or open-source beginner, prior acceptance and embargo rules | Proposal/org fit/skills are actionable preparation; age/history/location are not | Beginner status is contextual; GSoC is not employment; use current [timeline](https://developers.google.com/open-source/gsoc/timeline) |
| 15 | [Apple Swift Student Challenge](https://developer.apple.com/swift-student-challenge/policy/) | Country age, student/recent-graduate category, professional-developer exclusion, registration, solo artifact format and prior-award limits | Registration/artifact format are actionable; age/history are not | “Professional developer” boundary and judging quality require manual/soft treatment |

## A.1 Valid state transitions found

These examples mean only “meets encoded published eligibility rules”, never “likely to win”.

1. Chevening: accumulate and document 2,800 post-graduation work hours and satisfy degree-timing requirements for a future cycle.
2. JJ/WBGSP: reach three years of eligible paid development work and obtain admission to a participating program.
3. CERN openlab: complete the required semester count and schedule participation while enrollment timing remains valid.
4. Erasmus Mundus: finish the bachelor before the joint master's start date for a program accepting final-year applicants.
5. Eiffel: select an eligible French program and meet the institution's internal nomination process/deadline.
6. OECD Internship: choose a period in which full-time enrollment remains valid and availability requirements are met.
7. World Bank Treasury: resolve a conflicting job/internship and confirm full-period availability.
8. Apple Swift Student Challenge: register appropriately and produce a compliant solo offline `.swiftpm` artifact within the size limit.
9. Kotlin Contest: add a functioning second KMP target, public repository, accepted license and required submission material.
10. Schwarzman: complete an accepted English test where no exemption applies.

---

# Appendix B — competitor boundary

| Product | Overlap | Porta must differentiate through |
|---|---|---|
| [ScholarPulse](https://devpost.com/software/scholarpulse-p8dg3f) | Scholarship what-if and open/close visualization | Automatic ethical action generation + constraints + Action Frontier |
| [nrol](https://nrol.me/how-it-works) | Gap engine and next-best action | Deterministic published-rule deltas and transparent cross-catalog frontier |
| [AdMerit](https://admerit.ai/) | Live profile changes and admissions outcomes | No probability; hard-rule eligibility only |
| [UniPredict](https://unipredicts.com/) | IELTS/GRE changes alter university options | Cross-opportunity hard-rule receipts and action constraints |
| [OppPaths](https://opppaths.com/docs/scholarship-guides/what-is-a-scholarship-gap-analysis) | Fixable/fixed gap prioritization | Multi-opportunity marginal gain and visible source-level deltas |
| [EnsureCollege](https://ensurecollege.com/) | Explainable matching and application plans | Counterfactual Action Frontier rather than a discovery workspace |

---

# Appendix C — submission claims

## Claims allowed

- “Evaluates a curated catalog against encoded published eligibility rules.”
- “Shows the source and last-checked date for every bundled hard rule.”
- “Ranks controllable milestones under user-selected time, effort and cost constraints.”
- “Runs locally on Android and Desktop.”
- “Does not predict admissions or scholarship awards.”

## Claims prohibited

- World's first scholarship what-if simulator.
- Shows scholarships you can win.
- Predicts admission/funding probability.
- Guarantees eligibility.
- Real-time or globally verified catalog.
- Best action without stating the selected constraints.
- AI perfectly understands official requirements.
- Partnership or endorsement by listed organizations.
