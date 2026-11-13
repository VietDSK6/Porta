# Porta — Kotlin Student Coding Competition scorecard

> Last updated: 12 November 2026
> Deadline: 7 December 2026, 23:59 CET
> Live rules rechecked: 12 November 2026
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
| Entrant age, full-time undergraduate/graduate enrollment or qualifying enrollment within 12 months of contest start, residency/sanctions, employment and prize eligibility | USER CONFIRM | Confirm against the live rules before submission; do not store sensitive details here. |
| Individual submission; one entry | USER CONFIRM | User confirms solo participation and no second entry. |
| Created specifically during the contest period | RISK | Git records the last pre-Porta baseline at `bd5fd5d` and the first Porta product commit at `6be9ec1` on 20 October 2026, within the contest period. The README now separates inherited Koko infrastructure from Porta work. Retain this risk until the entrant confirms there was no pre-period Porta product work. |
| Substantial application, not template/Hello World/library | PASS | Porta-owned engine, Room repositories, Compose flows, pack validation/import and 15-record catalog are implemented; keep separating these changes from Koko template code in README evidence. |
| Original work or sufficient rights | IN PROGRESS | Koko is MIT-licensed and officially intended as a contest starting point; the README preserves attribution and `KMP_EVIDENCE.md` excludes unchanged scaffold from Porta claims. `OPPORTUNITY_PACK_SOURCES.md` records independent interpretations and official URLs; still audit icons, media and generated assets before publication. |
| Cross-platform Kotlin project | PASS | Product logic, persistence and most UI are shared Kotlin; Android debug builds and the JVM Desktop package builds. An earlier packaged Desktop build launched past startup. Android + JVM Desktop are the declared contest targets; Web build work is deferred. |
| GitHub repository submitted | PASS | Public repository: `https://github.com/VietDSK6/Porta`. Commit `5a17882` was cloned and read without a credential helper, then passed the documented automated paths. |
| Accepted repository license | PASS | Root repository currently contains MIT License. Preserve required notices and verify the submitted repository exposes it. |
| Comprehensive README | PASS | The public root README covers product story, demo path, architecture, declared targets, limitations, quality gates, trust boundaries, exact baseline/first-product commits, and a table separating inherited Koko infrastructure from Porta work. |
| Launch instructions for every declared target | PASS | Android and JVM Desktop have exact commands. A credential-free public clone builds the APK and DMG; the rebuilt Desktop package was observed launching past startup. Android device interaction remains a disclosed manual QA item, not an instruction gap. |
| 300-word essay | IN PROGRESS | `SUBMISSION/ESSAY.md` has an exactly 300-word body grounded in the entrant's August 2026 CV and the current project. Entrant must verify that enrollment and employment remain accurate before submission. |
| Submission form before deadline | TODO | Save the final form link and submission receipt outside the repository. |
| Truthful and rights-safe submission/media | IN PROGRESS | Source every factual claim and opportunity rule; obtain permission for any identifiable third party in media. |

## Target declaration decision

Current contest promise: **Android + JVM Desktop**.

The README identifies iOS and Web as inherited compatibility scaffolding rather than declared Porta contest targets.
Web build work is deferred; historical shared JS/Wasm compile checks do not establish a third runnable target.

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
- [x] Shared Route planner sequences up to four controllable milestones within horizon, cost, and effort limits while
  evaluating published deadlines. It compares non-dominated paths, explains openings and improvements, shows doors lost to
  deadlines, and persists a selected path as ordered active-plan steps. Compact timeline and wide Desktop map have
  Compose UI coverage. The bundled 15-door Linh demo routes improve checks but open **zero** doors under current
  encoded rules and deadlines; neither the planner nor the demo should imply otherwise.
- [x] A JVM terminal companion validates a pack, evaluates a Porta profile export, and calculates Route paths through
  the same `:engine` used by the app. `export-bundled` and `validate` smoke checks passed on the 15-door catalog.
  This expands ways to inspect the product logic without claiming a browser target or another graphical app.
- [x] P4 optional Studio rule drafting has three offline source fixtures and a user-supplied Gemini
  key lead to untrusted suggestions; QuoteGuard requires a matching source quotation and unsupported typed values
  become manual checks. Individual curator approval, Studio validation and Pack inspector approval remain required.
  Scoped tests and Android/Desktop builds pass. A real-key exercise remains pending before treating the live
  Gemini path as verified. AI never decides eligibility, and no Web runnable-target claim follows from this feature.
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
- [x] Onboarding can install a clearly labelled fictional demo profile into the normal local Room-backed workflow, so a
  judge can edit facts, save plans and exercise actual-result/evidence updates without entering personal data; the
  original non-persisted sample path remains available. The judge profile uses a 180-day horizon and three pre-saved
  sourced goals to expose degree, English and programme-verification trade-offs without claiming a perfect applicant.
- [x] Onboarding has two steps: an editorial introduction and an interactive, explicitly synthetic IELTS threshold
  lesson. Four changed sample checks come from the evaluator, while Linh's sourced catalog remains separate. The
  previous goal choice was removed because it did not influence results. Route leads with improved checks when zero
  doors fully pass, and keeps that zero-door disclosure visible. English remains the default, with persisted
  English/Vietnamese UI selection. The revised English/Vietnamese onboarding and zero-open Route screenshots were visually reviewed.
- [x] Pack identity is immutable across every stored version: a publisher change for an existing pack ID is rejected
  before persistence, including when the original version is inactive.
- [x] Demo and real-pack receipts separate deterministic results, unknowns, manual checks and soft criteria.
- [ ] Final competitor check and dated novelty statement.

Do not self-award a numeric judging score from implementation or local tests.

### Kotlin Multiplatform/shared Kotlin — TBD / 40

Required evidence:

- [x] Shared domain models and typed rule graph.
- [x] Shared deterministic eligibility engine and receipts.
- [x] Shared counterfactual simulator and Action Frontier algorithm.
- [x] Pure `:engine` module now contains the rule/domain models, evaluator, scenario, Action Frontier, Route planner,
  and typed opportunity-pack DSL. `:shared` consumes it for Android/Desktop; `:cli` consumes the same engine and
  app pack/profile codecs for terminal commands. The bundled pack's DSL conversion was compared with the prior
  implementation for exact JSON, DTO, and rule-fingerprint equality before the old file was removed.
- [x] P4's pure QuoteGuard and closed typed suggestion conversion live in `:engine`; the shared Studio UI, draft
  service, and local key settings passed engine/shared tests and Android/Desktop builds. The live Gemini call still
  needs a user-supplied key and a documented manual exercise.
- [x] Shared repository, validation, Room persistence, and import/export.
- [x] Room schema v2 preserves existing facts, saved doors and active plans while adding scoped rule assertions;
  profile export v2 round-trips facts and assertions as separate sections.
- [x] Meaningful shared Compose UI/state, not only common constants.
- [x] Android quick-update/tracking flow completes a local plan, requires explicit actual-fact confirmation,
  persists optional evidence metadata and refreshes Doors reactively; covered by ViewModel/UI tests and the
  Android debug build.
- [x] Actual-result confirmation can attach one local image, PDF, TXT, DOC or DOCX file up to 10 MB. Android/Desktop
  copy it into app-private storage, expose open/replace/remove controls, clean up replaced/deleted files, and state
  clearly that Porta stores user-provided evidence without independently verifying it. Profile handoff exports only
  the descriptor; binaries and private storage keys stay on the originating device.
- [x] Actual-result confirmation supports and validates every encoded `FactValue`: Boolean, Number, Enum, EnumList,
  Text, Date and Duration. Invalid input does not partially update facts or programme-scoped assertions, and the
  existing rule-fingerprint guard remains active.
- [x] Desktop comparison/inspection flow works: wide windows use a navigation rail, Frontier becomes a
  selectable master-detail workspace with a transient 2–3 scenario Compare Lab against one Actual baseline, and
  Pack inspector keeps its read-only master-detail diagnostics. Compact Android behavior remains one-column with
  bottom navigation; adaptive UI tests, preview evidence, JVM tests and the JVM Desktop package cover the slice.
- [x] Android-to-Desktop profile handoff uses the existing versioned JSON export, a Desktop-only sensitive-data
  preview and an explicit replace confirmation. One Room transaction replaces facts plus scoped assertions, rebases
  the source profile ID to the active local profile, rolls back on write failure, and leaves plans, saved doors,
  constraints and the catalog untouched.
- [x] Desktop Opportunity Studio provides a form-based sourced rule draft, compiles and validates it, and exports
  JSON. Pack Inspector separately validates an imported pack, shows its change preview, and requires approval before
  activation. Repository, ViewModel, and Compose UI tests cover these paths; a final packaged-app exercise remains
  in the manual QA checklist.
- [x] Desktop adds native File/View/Help menus, Cmd/Ctrl navigation, import/export and search shortcuts, pack-JSON
  drop-to-preview, persisted window geometry, and OS-standard data locations with a one-time legacy migration.
  Desktop storage/command tests and the package task cover these changes; a fresh packaged-app interaction pass
  remains in manual QA.
- [x] Shared Route planning uses the same typed rule graph and scenario evaluation as Android and Desktop, with
  ordered Room plan persistence and migration to schema v6. Domain, repository, ViewModel, and Compose tests cover
  the planner, route UI, and the 15-door Linh fixture.
- [x] Responsive/accessibility regression evidence covers a 320×640 viewport, 200% text, keyboard traversal of the
  Desktop rail, 48dp primary chips, a wide two-pane opportunity receipt with a large-text single-column fallback,
  a polite What-if live region and zero-duration motion without losing the textual outcome. Final packaged-app
  screen-reader and full keyboard exercise remains a disclosed manual submission check.
- [x] Common/JVM/Android-host tests demonstrate shared behavior, including two contrasting profiles across all 15 records.
- [x] The 100-opportunity JVM performance characterization covers `ALL`/`ANY`, missing/manual checks, mixed deadline
  certainty, scenario overlays, the Action Frontier, and Route planning with deterministic-output assertions, warm-up,
  and median/p95 reporting. The P5 code-commit `dc4e701` Route p95 is **2.563 ms** on macOS aarch64/JDK 21; all four
  measured p95 values are below the PRD's 100-ms target on that host. See `KMP_EVIDENCE.md` for the fixture and limits.
- [ ] Repeat the characterization from the public submission commit if later Kotlin changes alter the workload or engine.
- [x] `KMP_EVIDENCE.md` documents a reproducible contest-period measurement at `dc4e701`: 21,991 NCLOC across 144
  explicitly allowlisted shared Porta production files plus 673 NCLOC across 13 platform and CLI files; tests and Kotlin data are
  reported separately, while generated/vendor/unchanged starter code, package-only renames and mixed-provenance
  integration files are excluded.
- [x] The 11 November P3 working-tree scoped gates passed `spotlessCheck`, `:engine:jvmTest`, `:shared:jvmTest`,
  `:shared:testAndroidHostTest`, `:androidApp:assembleDebug`, `:desktopApp:packageDistributionForCurrentOS`, and
  `:cli:test`. Their JUnit counts are 39 engine JVM, 269 shared JVM, 78 Android host, and 3 CLI, with zero
  failures/errors/skips. A separate Roborazzi recording passed with 72 preview cases; its XML is not added to
  the scoped-gate counts. Repeat the gates on a frozen public commit before submission.
- [x] The 12 November P4 working-tree scoped gates passed the same tasks with 46 engine JVM, 282 shared JVM,
  82 Android-host core tests, and 3 CLI tests, all clean. A separate snapshot recording passed 72 previews.
- [x] The 12 November P5 code-commit scoped gates passed the same tasks with 46 engine JVM, 279 shared JVM,
  79 Android-host tests, and 3 CLI tests, all clean. Roborazzi recording separately passed 66 previews. Android APK
  and macOS Desktop DMG assembled. Vietnamese Route/onboarding and wide English Route snapshots were visually reviewed.
  The code is committed locally at `dc4e701`, not yet verified from a fresh public clone.

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
| Essay | DRAFT READY; ENTRANT REVIEW | Body is exactly 300 words with CV-backed education, work and Myosotis details; entrant confirms current facts and final wording. |
| Screencast | SCRIPT READY; CAPTURE PENDING | `SUBMISSION/DEMO_SCRIPT.md` defines a Route-first 3:30 path with Linh's honest zero-open-door result; record it from the frozen Android/Desktop build. |
| Screenshots/GIFs | TODO | Current UI only; rights-safe and readable. |
| Submission form | TODO | Final URLs tested without private access. |

## Review cadence

- Weekly: run the `contest-readiness` milestone audit and update only evidence-backed rows.
- After material Kotlin milestones: run `kotlin-convention-audit` on the changed scope.
- Seven days before deadline: freeze features; audit README, targets, essay, licenses, media, and live rules.
- Twenty-four hours before deadline: run the final documented paths and submit early enough to preserve proof of
  receipt.

## Next three contest actions

1. Confirm the CV-backed essay's current enrollment and employment facts, preserve the exact 300-word count after any
   edits, and confirm the personal eligibility/solo-entry gates.
2. Have a second person re-read all 15 official sources, refresh every TBA deadline, and capture rights-safe Android
   and Desktop media from the frozen build.
3. Complete Android-device plus keyboard/screen-reader QA, record the video URL, approve the final essay wording, and
   submit the verified public repository before the deadline.
