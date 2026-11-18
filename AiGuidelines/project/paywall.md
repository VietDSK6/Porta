# Porta — Monetization and Paywall

> The starter kit's paywall, subscription, credit and ads code has been removed from Porta's build. It survives only
> in Git history; this file records why and what a future model must respect.

## Primary model

**No paid model in the contest release.** Porta is completely free because user trust, judge reproducibility, and
the quality of verified opportunity rules matter more than early monetization. Eligibility explanations, sources,
the Action Frontier, scenario previews, planning, and local data controls are never gated.

## Primary paywall (none in the contest build)

- **Placement**: None. Onboarding hands off to Linh's sourced route, Profile Setup, or the unsaved sample; no
  cold-launch or post-demo paywall exists.
- **Offer architecture**: `TODO(monetization)` — intentionally undefined until a post-contest value and maintenance
  study establishes a product users want to fund.
- **Primary CTA**: None.

## Deferred commercial decisions

- **Prices and packages**: `TODO(monetization)`.
- **Trial length and reasoning**: `TODO(monetization)`.
- **Credit pack sizes and recommended pack**: `TODO(monetization)`.
- **Provider and store products**: `TODO(monetization)`.
- **Future free/paid boundary**: `TODO(monetization)`; it must never hide an encoded reason, official source, privacy
  control, or previously bundled contest opportunity behind payment.

## Implementation guardrails

- Do not reintroduce billing, subscription rows, credit balances, paywall screens, or paywall impression events in
  the contest build.
- Never present the starter kit's former paywall code as a Porta feature or contest contribution.
- Any future monetization proposal requires explicit product approval, real pricing, visible renewal terms, restore
  purchases, and honest cancellation language. No fake scarcity, countdown, preselected upsell, or misleading
  “free” label is permitted.

## A/B variations

No paywall variations are designed for P0 because there is no commercial offer to test. Portal Atlas visual
experiments belong in onboarding and the Action Frontier, where they improve comprehension rather than conversion.
