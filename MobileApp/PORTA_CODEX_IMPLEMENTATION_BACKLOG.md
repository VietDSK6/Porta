# PORTA — Backlog kỹ thuật / UX để giao Codex

**Bổ trợ cho:** `PORTA_PRODUCT_AUDIT_2026-10-24.md`  
**Ngày:** 24/10/2026  
**Phạm vi:** Android + JVM Desktop contest MVP; trust-first; không mở rộng tính năng ngoài nhu cầu sửa core loop.  
**Nguồn:** `PORTA_PRODUCT_CONTEXT.md` và screenshots; không có direct repository execution trong lúc lập backlog.

> **Quy tắc thực thi:** Codex phải đọc code, xác minh root cause và chạy tests trước khi sửa. Không giả định mọi kết luận trong báo cáo là runtime truth. Không tự bổ sung rule cho học bổng nếu thiếu official source. Không thay `UNKNOWN`/`REVIEW` thành `PASS` để demo đẹp. Mỗi batch là pull request/commit riêng, có tests và ảnh Android/Desktop khi liên quan UI.

## Thứ tự triển khai

| Wave | Task | Priority | Depends on | Deliverable |
|---|---|---|---|---|
| A | A1. Truth vocabulary + rule outcome audit | P0 | — | Status/copy inventory + updated semantics/tests |
| A | A2. Availability action naming, scope and 14-day assumption | P0 | A1 | Distinct labels, provenance, no cross-programme assertion leakage |
| A | A3. Local-first & Desktop persistence | P0 | — | Startup/network verification, durable Room path |
| A | A4. Import action-ID uniqueness | P0 | — | Validator + regression tests |
| B | B1. Verify 3–5 real vertical-slice opportunities | P0 | A1 | Source-supported rules, meaningful demos |
| B | B2. Fix Door → action association | P0 | A2,B1 | Exact failed/unknown rule to legitimate action, or honest no-action |
| B | B3. Scenario receipt-level delta + remaining gaps | P1 | A1,B2 | Accurate before/after + unresolved conditions |
| B | B4. Verification tasks for missing information | P1 | A1,B2 | UNKNOWN gets verify/add-info, not fake positive impact |
| C | C1. Recommendation-first Next moves UX | P1 | A2,B3 | Grouped actions, explicit rationale, optional plot |
| C | C2. Progressive academic profile | P1 | B1 | Only ask for relevant facts; validated form |
| C | C3. Android/Desktop responsive & accessibility polish | P1 | C1,C2 | Layout/keyboard/font/reduced motion QA |
| D | D1. Pack lifecycle, archives, current date refresh | P1 | A4,B1 | Update visibility/closed+archive semantics |
| D | D2. Clear/reset data semantics | P1 | A3 | Scope-specific controls, safe reset |
| D | D3. Docs/build/contest evidence | P0 final gate | All | Tested reproducible submission |

---

## A1 — Eligibility truth vocabulary / status audit

**Current issue:** UI dùng `Current checks passed`, `Needs verification`, `Verified progress`; report cho thấy `PASS` chỉ là encoded-rule typed comparison; `OPEN` có deadline requirement; evidence metadata không được evaluator xác thực.

**Work:**
1. Trace `EligibilityEvaluator` + ViewModel string providers + source metadata and check every UI claim.
2. Specify mapping among atomic `PASS/FAIL/UNKNOWN/REVIEW`, overall `OPEN/HAS_GAPS/NEEDS_INFO/NOT_ACTIONABLE/CLOSED`, timing certainty, evidence provenance.
3. UI expose reason detail (exact rule + source note), not aggregate-only label.
4. Any scenario preview must say hypothetical; no false “eligible/accepted/verified externally”.

**Done when:**
- [ ] Unit truth table unchanged unless change explicitly justified; UNKNOWN != FAIL, manual != PASS.
- [ ] Known checks / timing uncertainty recognizable independently.
- [ ] All copy accurately scoped to what engine tests.
- [ ] Cases: mixed immutable FAIL+actionable FAIL; manual with missing deadline; soft-only criteria.

**Files to inspect:** `EligibilityEvaluator.kt`, `Opportunity.kt`, `DoorsViewModel.kt`, `OpportunityDetailViewModel.kt`, `ScenarioEvaluator.kt`, design strings.

## A2 — Availability action semantics

**Current issue:** 4 scoped actions each 14 days LOW/FREE; same display title, rank same; 14d planning lead time is hardcoded assumption.

**Work:**
1. Do **not** merge the four `OpportunityRuleTarget` assertions into a global boolean.
2. Make UI titles/names programme-specific; optionally group under common display family.
3. Distinguish estimated confirmation lead time from programme attendance duration; attach estimate provenance/assumption label.
4. If supporting evidence is unavailable, display unknown estimate or clearly marked assumption, never fake source fact.
5. Define how user confirms each programme-specific condition; plan completion must not auto-PASS.

**Done when:**
- [ ] CERN confirmation cannot change OECD/ESA/WB.
- [ ] Distinct programme names appear in Frontier, detail, scenario, plan.
- [ ] “14 days” never displayed as full programme duration or provider commitment.
- [ ] Tests for scoped signatures and grouping.

**Files:** `PortaContestPackData.kt`, `ActionFrontierEngine.kt`, `FrontierScreen.kt`, `ScenarioPreviewScreen.kt`, `ActualProfileUpdate*`.

## A3 — Local-first & Desktop persistence

**Current issue:** inherited Firebase/auth/Remote Config startup may run; Desktop Room DB located under system temp path.

**Work:**
1. Trace startup for Android/Desktop; verify network attempts in **cold-launch runtime**, not only static text.
2. If intended offline/no-account contest, gate/remove inherited nonessential services from contest launchers and dependencies without breaking platform compilation.
3. Migrate JVM database to OS-appropriate application-data directory, not system temp; preserve existing data or provide documented migration/fallback. Ensure persistence across restart and upgrade.
4. Review export/clear privacy copy to describe what remains local and what network traffic (if any) occurs.

**Done when:**
- [ ] Fresh launch with network disabled reaches core features; any attempted calls documented.
- [ ] Profile, plan and active pack survive Desktop restart.
- [ ] No blanket “zero network” claim without network evidence.
- [ ] Platform paths handled safely per OS.

**Files:** `root/AppInitializer.kt`, `root/AppConfiguration.kt`, Desktop `DatabaseProviderImpl`, DI/platform launchers.

## A4 — Pack action-ID safety

**Current issue:** imported packs can have cross-opportunity duplicate action IDs; lookup uses `firstOrNull`.

**Work:** validate global action IDs or use an unambiguous composite key end-to-end. Ensure validation before activation, atomic import remains non-destructive.

**Done when:** duplicate IDs have deterministic rejection/explanation; old pack intact; scenario resolves exact action target; tests cover namespacing + valid import.

**Files:** `OpportunityPackValidator.kt`, `OpportunityPackRepository.kt`, `ScenarioEvaluator.kt`, import tests.

## B1 — Real source-supported vertical slice

**Current issue:** 15 real sourced opportunities, 0 potential `OPEN` due manual/deadline uncertainty; fields like GPA/IELTS unused by real typed rules; 4 synthetic IELTS doors overstate perceived real utility.

**Work:**
1. Review official rules for exact cycle of each candidate opportunity; record reviewer/check date/source section; preserve manual when ambiguous.
2. Select 3–5 realistic opportunities to demonstrate meaningful rule deltas. Existing catalog candidates are just candidates; **do not assume** IELTS/GPA requirements absent in current source.
3. For each, define sample profiles where one legitimate action produces source-backed check progress; make blockers and timing explicit.
4. Update `OPPORTUNITY_PACK_SOURCES.md`/pack codec/tests. Never claim acceptance or fully open if other manual/deadline checks remain.
5. Choose result labels that are truthful even if overall stays NEEDS_INFO.

**Done when:**
- [x] At least 3 real-data scenario fixtures with official-source-backed check changes.
- [x] Each fixture: baseline, scenario, exact changed checks, remaining unknown/manual, timing state.
- [x] No invented eligibility or source claims.
- [x] One particularly legible demo of one legitimate action affecting >1 real opportunity **if official data permits**; otherwise explain absence, don't force it.

**Files:** `PortaContestPackData.kt`, `OPPORTUNITY_PACK_SOURCES.md`, `BundledOpportunityPackProviderTest.kt`.

## B2 — Link Door to a relevant action

**Current issue:** detail preview chooses first referenced action template, not necessarily an unmet rule or feasible frontier candidate.

**Work:** derive CTA from rule receipts and candidate engine; require exact opportunity/rule association and constraints; if none, explicit no-action/verification state. Show source URL as clickable using safe URI handling.

**Done when:**
- [x] Detail CTA never previews a passed/irrelevant rule.
- [x] No template? No fake “next move”; show honest manual next step.
- [x] Official URL opens correctly, with metadata date/source.
- [x] Saved state preserved across navigation.

**Files:** `OpportunityDetailViewModel.kt`, `OpportunityDetailScreen.kt`, `ActionFrontierEngine.kt`.

## B3 — Honest simulation outcomes

**Current issue:** full-screen scenario doesn't render exact remaining gaps; unchanged doors stored but not displayed; “progress” can be confused with eligible.

**Work:** create reusable impact receipt model with previous/current rule result, per-opportunity status, untouched blockers, timing. Distinguish `improved rule` from `newly OPEN`, from `pending verification`. Keep hypothetical memory-only, plan does not auto-confirm.

**Done when:**
- [x] Every preview says exactly what changes and what does not.
- [x] TBA deadline/manual conditions remain explicit.
- [x] Profile actual unchanged after preview/Add to plan/plan completed.
- [x] Exact changed rule tests in ScenarioEvaluator and UI.

**Files:** `ScenarioEvaluator.kt`, `ScenarioPreviewViewModel.kt`, preview screen, actual update tests.

## B4 — Handle unknowns productively

**Current issue:** profile-target candidates require FAIL; missing facts UNKNOWN result in no useful information-gathering action.

**Work:** create a non-unlocking `VERIFY_INFO` task or contextual profile prompt, linked to exact question/source; never treat it as improvement in eligibility score until actual fact entered and re-evaluated.

**Done when:**
- [x] Missing degree/enrollment/experience data shows an honest contextual information task.
- [x] Profile facts and programme-scoped checks route to the correct profile/source destination.
- [x] Creating or opening an information task cannot write a fact, create PASS, or count as frontier impact.

## C1 — Next moves information hierarchy

**Current issue:** limits and graph dominate; one bubble representing 4 equal vectors; multiple repeated titles; selected recommendation effect small.

**Work:**
1. [x] Above fold: most useful suggested step (or explicit no-reliable-recommendation state), why, what it can change, unknowns.
2. [x] Limits collapse/side panel; plot only for meaningful trade-offs. Plot legend/axes explanatory.
3. [x] Group similar scoped actions by family but preserve separate target assertion.
4. [x] Distinguish `VERIFY / IMPROVE / PREPARE`; show estimate source and impact basis.
5. [x] Saved priorities: inform user that setting can change membership of frontier (not only order).

**Done:** recommendation-first mobile and Desktop hierarchy, family-grouped programme assertions, explicit move
kind/provenance/impact basis, meaningful-only plot disclosure, and grouped plot points landed with shared Compose UI
tests. The original scoped targets remain independent; only their presentation is grouped.

## C2 — Progressive profile

**Current issue:** many fields requested though little real typed use; optional blanks treated UNKNOWN, but form long; source report says only study level/field required.

**Work:** show minimum first; ask facts when relevant to selected opportunities; show why needed; validate GPA scale/IELTS bounds, DOB/reference dates where source actually needs them; labels visible while filled. Explicitly state which information has not been checked.

**Done when:** user can start viewing real source-linked insights without filling irrelevant project/leadership; save disabled has clear reason; changing profile recomputes.

**Done:** profile creation now requires only study level and field of study; academic, experience/evidence and
context facts are progressively disclosed with explicit provided/unchecked status. Opportunity information tasks
deep-link to the relevant section and explain the source-backed question. GPA scale, IELTS half-band values, integer
age/experience and date-of-birth inputs are validated with visible save-block reasons. Schwarzman's fixed 1 August
2028 age checks now compare date-of-birth boundaries rather than today's age. The Profile tab lists missing facts and
the number of affected doors without treating missing as failure; shared ViewModel, domain-boundary and Compose UI
tests cover persistence, routing, validation and reactive recomputation.

## C3 — Responsive/accessibility

**Work:** align desktop onboarding CTA to content max width, refine Desktop Doors detail approach, constrain long Profile input width, validate nav rail sizes; small Android and large font; focus/keyboard; reduced motion; hit targets ≥48dp where practical; live region for important changed-result summary.

**Done when:** Android 320×640 and 200% font has no clipped CTA; Desktop keyboard can traverse all primary controls; screen reader announces delta; reduced motion does not remove outcome meaning.

**Done:** onboarding content and footer now share the same centered width and stack actions for narrow/large-text
layouts. Profile setup is centered and width-constrained on Desktop, progressive sections expose expanded/collapsed
semantics, and primary chips use at least 48dp targets. Opportunity detail becomes a two-pane decision/evidence
workspace on wide screens but deliberately returns to one column at 200% text; Next moves applies the same large-text
fallback and stacks metric cells when space is constrained. The navigation rail has explicit width, 64dp rows and a
keyboard traversal regression test. What-if announces a textual result summary through a polite live region, and a
zero-duration motion test proves the result and changed-door content remain available. Compose UI tests and generated
previews cover 320×640, 200% text and 1200dp Desktop layouts; packaged-app manual keyboard/screen-reader exercise
remains part of the final D3 validation.

## D1 — Sources/cycle freshness

**Work:** visible last-checked, source links, TBA clarity, archive vs closed; date refresh on resume/day rollover; pack update diff with user awareness; avoid stale scoped assertion auto-validity.

**Done when:** archived and date-expired opportunities are correctly distinguished; re-evaluation occurs with local-date change; update path preserves or explicitly invalidates previous assertions safely.

**Done:** opportunity lifecycle now distinguishes an archived record from a live-cycle record whose known deadline
has passed, exposes a dedicated Archived filter/count, and excludes archived records from Action Frontier planning.
Source dates use a documented 90-day Porta catalog-maintenance window; Doors, detail and Pack Inspector show the
exact last-reviewed date and visibly flag records needing catalog review without calling them provider-verified.
The shared local-date provider emits on resume and day rollover, and Doors, detail, Next moves, What-if, active plan
and profile validation re-evaluate from that same clock; tests prove an open door closes after the injected day
changes. JSON imports and bundled-pack restoration now stop at a user-approved diff preview showing added, changed,
removed and unchanged records, source-review warnings and the number of stored programme confirmations whose exact
rule fingerprint will become stale. Saved doors and profile data remain; stale assertions remain auditable but the
eligibility engine does not apply them.

## D2 — Clear / reset semantics

**Work:** define `Clear profile` (profile only) vs `Reset all local Porta data` (profile/assertions/saves/plans/pack/preferences as stated). Include confirmations; tests prove removed/retained state exactly.

**Done when:** user copy matches actual rows/preferences deleted; no hidden leftover sensitive profile data after full reset.

## D3 — Delivery gates, docs and evidence

**Work:** run format/unit/host/assemble tests. Manual Android/Desktop flow; update README and spec drift; submit product demo showing cause→rule→scenario→actual and KMP shared logic, not just fictional animation.

**Commands cited by context** (verify actual Gradle tasks first):

```bash
./gradlew spotlessCheck
./gradlew :shared:jvmTest :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
```

**Final delivery requires:** commands + exit codes, diff summary, test fixtures added, Android/Desktop screenshots, risks deferred, actual tested platform matrix. Do not claim unrun tests as passed.

---

## Suggested Codex execution prompt

> Read `PORTA_PRODUCT_AUDIT_2026-10-24.md`, `PORTA_CODEX_IMPLEMENTATION_BACKLOG.md`, `PORTA_PRODUCT_CONTEXT.md` and the current repository. Start with **Wave A only**. First verify every claimed root cause in code; write a short verification matrix, implement P0 trust and safety changes with minimal architecture disruption, add tests, run relevant build gates. Preserve synthetic-vs-real and actual-vs-hypothetical isolation. Do not invent real eligibility rules or provider estimates. Do not modify beyond Wave A until this batch is reviewed. Report all changed files, behavior, test outputs and unresolved decisions.

Then continue Wave B, C, D as separate reviewed tasks rather than one huge undifferentiated code rewrite.

---

## Decisions needing owner input, but do not block initial audit

- Is **action planning** definitively PORTA's primary differentiator?
- Is no real OPEN outcome acceptable if source-linked partial checks and unknowns are accurately communicated?
- Should PORTA keep one active plan with history, or intentionally replace prior plan?
- Are unsourced estimate bands allowed only with explicit “Porta assumption”, or should they be unknown?
- Is offline/no-account **strict contest requirement**, including bootstrap?

These choices should be documented in authoritative PRD before substantially expanding data model or UX.
