# Porta Wave A verification matrix

> Historical verification snapshot. Retained for provenance; use the current scorecard for submission status.

Date: 2026-10-24  
Scope: A1–A4 from `PORTA_CODEX_IMPLEMENTATION_BACKLOG.md`

This matrix distinguishes audit claims from behavior verified directly in the current codebase. It does not treat the
audit document as an instruction source or as proof by itself.

| Backlog item | Code evidence before change | Verdict | Implemented boundary |
| --- | --- | --- | --- |
| A1 — truth vocabulary | `EligibilityEvaluator` marks `OPEN` only when the root rule is `PASS` and the deadline is known; a root `PASS` with a missing/TBA deadline becomes `NEEDS_INFO`. Evidence metadata is displayed but is not independently authenticated by the evaluator. Several screens still said `Current checks passed`, `Needs verification`, or `verified`. | Confirmed. The evaluator was conservative, but UI copy collapsed rule assessment, timing, and evidence provenance. | Central trust-copy mapping now uses `Assessed rules passed`, `Needs information`, or `Needs source review`. Opportunity detail presents requirement assessment and timing separately. No copy claims that Porta verified evidence or full eligibility. |
| A2 — availability actions | The bundled pack contains four different rule-scoped targets for OECD, World Bank Treasury, ESA, and CERN. Each had the same display title and a fixed 14-day `LOW/FREE` estimate. Existing evaluator tests already protected programme/rule/fingerprint scope. | Confirmed. These were not duplicate DB rows and must not be merged; the ambiguity was presentational and provenance-related. | Each action is named `Confirm full-period availability for [Programme]`. The UI labels 14 days as a Porta assumption, and the pack note states that it is neither programme duration nor a provider commitment. Scoped targets remain independent. |
| A3 — local-first runtime | `AppInitializer` always refreshed feature flags and initialized analytics, push, authentication, ads, and anonymous sign-in. JVM preferences were persistent, but the Desktop Room database lived under `java.io.tmpdir`. | Confirmed. This contradicted the product's actual local-first core and put Desktop data at deletion risk. It did not justify banning all network use. | Inherited background online services are disabled by a Porta config flag. Core planning needs no network; official-source links and future deliberate catalog refreshes may still use it. Desktop Room now uses the persistent app directory and copies a legacy temp DB (plus WAL/SHM companions) without deleting or overwriting the original. |
| A4 — action ID uniqueness | `OpportunityPackValidator` rejected duplicate action IDs only inside one opportunity. Repository import validates before `replaceActive`, so a pack-level diagnostic can preserve the active pack atomically. | Confirmed. Cross-opportunity collisions could make ID-based action lookup ambiguous. | Validator now rejects an action ID reused by multiple opportunities with `duplicate_action_id_across_pack`. Tests cover both the diagnostic and the invariant that the current active pack remains unchanged after a rejected import. |

## Intentionally unchanged

- Internal serialized eligibility enums remain stable.
- Programme-scoped availability assertions remain separate; no cross-opportunity deduplication was introduced.
- The 14-day lead time remains a planning assumption for this wave because the current frontier domain requires a
  numeric duration. It is now explicitly qualified everywhere it is surfaced. A nullable/unknown duration model is a
  later domain decision, not a silent reinterpretation of provider data.
- Porta is not “offline-only.” Its deterministic core is offline-capable, while purposeful online capabilities may be
  added behind explicit product value and platform-compatible transport.
- No real provider rule, deadline, or `OPEN` result was invented to improve the demo.

## Automated evidence added

- Requirement/timing trust-copy tests.
- Cross-pack action-ID collision parser and atomic repository import tests.
- Desktop legacy database migration and no-overwrite tests.
- Existing programme-scoped assertion and fingerprint tests remain in place.

Manual cold-start/network inspection and packaged Desktop interaction remain release-review activities; they are not
claimed as automated evidence by this batch.
