# Porta — Monetization and Paywall

> Paywall code remains in the starter kit, but Porta disables it with
> `AppConfiguration.PREMIUM_FEATURES_ENABLED = false` during the contest.

## Primary model

**No paid model in the contest release.** Porta is completely free because user trust, judge reproducibility, and
the quality of verified opportunity rules matter more than early monetization. Eligibility explanations, sources,
the Action Frontier, scenario previews, planning, and local data controls are never gated.

## Primary paywall (disabled for contest)

- **Placement**: None. Demo-first onboarding hands off to Minimal Profile Setup; no cold-launch or post-demo paywall
  is shown.
- **Surfaced goal**: The onboarding goal personalizes opportunity emphasis only; it is not reused for a purchase ask.
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

- Keep `PREMIUM_FEATURES_ENABLED = false` for the contest build.
- Do not initialize billing, display subscription rows, show credit balances, route to paywall screens, or emit
  paywall impression events.
- Preserve the starter-kit paywall code without presenting it as a Porta feature or contest contribution.
- Any future monetization proposal requires explicit product approval, real pricing, visible renewal terms, restore
  purchases, and honest cancellation language. No fake scarcity, countdown, preselected upsell, or misleading
  “free” label is permitted.

## A/B variations

No paywall variations are designed for P0 because there is no commercial offer to test. Portal Atlas visual
experiments belong in onboarding and the Action Frontier, where they improve comprehension rather than conversion.
