# Porta — Onboarding

> Implementation lives in
> `MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/onboarding/`.
> Copy lives in the `onboarding_*` keys of both `composeResources/values/strings.xml` and `values-vi/strings.xml`.

## Pattern

Demo-first, two steps. Porta's value is easiest to understand by changing one clearly labelled synthetic fact and
immediately seeing checks change. A questionnaire before that proof would feel like homework and ask for trust too
early.

An earlier four-screen version opened with a goal choice (Scholarships, Internships, …). It was removed because the
choice did not influence any result. Its `onboarding_goal_*` strings and `KEY_OPPORTUNITY_GOAL` preference are
unused leftovers.

## Step 1 — The question

- **Purpose / psychology**: Name the problem before showing the product. A list of opportunities is not a plan.
- **Eyebrow / headline**: `01 · THE QUESTION` · "A list of doors is not a plan."
- **Supporting text**: See how one achievable step changes the checks behind several possibilities.
- **Trust line**: Start with a fictional example. No account or personal data needed.
- **CTA**: `See one change` (primary). `Skip` jumps straight to the unsaved sample.

## Step 2 — Try the threshold

- **Purpose / psychology**: Deliver the first taste of value through causality, not a feature tour, while keeping the
  synthetic lesson visibly separate from sourced data.
- **Eyebrow / headline**: `02 · TRY THE THRESHOLD` · "One score changes the map."
- **Interaction**: **FIRST TASTE OF VALUE**. Toggle `Try IELTS 7.0` / `Back to IELTS 6.5` on a badge-labelled
  `SYNTHETIC IELTS EXAMPLE`. The BEFORE / WHAT IF counts come from the real evaluator: the highlighted sample checks
  that can change, then the example IELTS checks that would pass.
- **Disclosures**: Fictional opportunities. Check changes say nothing about acceptance. Confirm live deadlines, fees
  and manual criteria with each provider.
- **Linh card**: `FICTIONAL PROFILE · SOURCED CATALOG`. It states honestly that Linh's sourced route improves checks
  but that no programme passes every assessed check yet.
- **CTAs**:
  - `Explore Linh's sourced route` (primary) installs the fictional demo profile into normal local storage. If a
    profile already exists, it is left untouched and an error is shown.
  - `Use my own profile` opens Profile Setup.
  - `Explore without saving a profile` opens the unsaved sample.

## Permission and paywall handoff

- **Permission priming**: None. Porta needs no camera, gallery, notification, or location permission for its core
  value.
- **Paywall**: None. The contest build has no billing or paywall code.
- **Account requirement**: None. There is no sign-in gate before or after onboarding.

## Product pains resolved

1. Matching feeds do not explain why an opportunity is open, blocked, or uncertain.
2. Students do not know which smallest controllable step has the highest cross-opportunity leverage.
3. Ambiguous or stale conditions create false confidence when tools hide sources and verification dates.

## Acceptance

- The synthetic lesson is deterministic, explicitly labelled, and completes offline in under 30 seconds on the judge
  path.
- The synthetic lesson and Linh's sourced catalog never share a count or a claim.
- English is the default on first launch. Vietnamese copy must stay in sync.
- Screen-reader and reduced-motion users receive the same counts and disclosures without relying on motion.
- No analytics SDK is present; any future event tracking needs a separate privacy decision.
