# Porta — Onboarding

> Implementation lives in
> `MobileApp/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/presentation/screens/onboarding/`.

## Recommended pattern

Demo-first short emotional, 4 screens — because Porta's value is easiest to understand by changing one clearly
labeled sample fact and immediately seeing source-backed doors change; a long questionnaire before this proof would
feel like homework and demand trust too early.

## Screen 1 — Goal capture

- **Purpose / psychology**: Capture intent with one low-friction choice and tailor which opportunity type is
  emphasized after onboarding. This choice never affects eligibility.
- **Headline**: What are you trying to open next?
- **Supporting Text**: Pick a direction. Porta will show how one realistic milestone can change your options.
- **Interaction**: **GOAL CAPTURE** — single choice: `Scholarships`, `Internships`, `Academic programs`,
  `Student contests`, or `Show me all`.
- **Illustration Description**: Dark Portal Atlas map, calm and precise; five small route branches lead toward
  distinct arched door outlines in violet, teal, amber, and ivory.
- **CTA**: Show me how

## Screen 2 — Trust before data

- **Purpose / psychology**: Establish the product's honesty boundary before demonstrating it.
- **Headline**: Eligibility, without the mystery.
- **Supporting Text**: Porta checks encoded published rules, shows what is missing, and links the source. It never
  predicts whether you will be selected.
- **Interaction**: Tap the sample receipt to reveal `Your value`, `Required value`, `Source`, and `Checked date`.
- **Illustration Description**: A warm-ivory rule receipt floats over an ink atlas; one evidence thread connects a
  clause to a small official-source stamp. Mood is trustworthy, not legalistic.
- **CTA**: Try a real example

## Screen 3 — Sample magic moment

- **Purpose / psychology**: Deliver value before requesting any profile information and demonstrate causality rather
  than a feature tour.
- **Headline**: See what one step opens.
- **Supporting Text**: This is a sample student, not your profile. Move IELTS from 6.5 to 7.0 and watch only the doors
  whose encoded hard rules actually change.
- **Interaction**: **FIRST TASTE OF VALUE** — drag or tap the `IELTS 6.5 → 7.0` control. Four sample doors use the
  Portal Aperture transition; each exposes a short source-backed reason. A text summary reads `4 doors changed · 11
  unchanged · 2 still need verification`.
- **Illustration Description**: Interactive dark atlas with one glowing route from the IELTS fact node to four
  portals. Changed portals open with signal teal and violet; unchanged and Verify doors remain spatially stable.
- **CTA**: Show why they changed

## Screen 4 — Real-map handoff

- **Purpose / psychology**: Convert the demo's agency into a small commitment while preserving a no-data sample path.
- **Headline**: Now map your real next move.
- **Supporting Text**: Add only the facts used by Porta's rule pack. Your academic profile stays on this device, and
  unknown answers can stay unknown.
- **Interaction**: Review the selected goal and sample result. Choose the primary CTA for Minimal Profile Setup or
  the secondary sample path.
- **Illustration Description**: The sample constellation folds into a blank personal atlas with six unfilled fact
  nodes. The mood is calm and invitational; no lock, countdown, or scarcity cue appears.
- **CTA**: `Build my map` (primary) · `Keep exploring the sample` (secondary)

## Permission and paywall handoff

- **Permission priming**: None in onboarding. Porta P0 does not need camera, gallery, notifications, or location to
  deliver its core value. If a future feature adds a permission, show a benefit explanation immediately before the
  system request.
- **Handoff to paywall**: Not applicable. The contest version is free and routes `Build my map` to Minimal Profile
  Setup, never to a paywall or account screen.
- **Account requirement**: None. There is no sign-in gate before or after the demo.

## Product pains resolved

1. Matching feeds do not explain why an opportunity is open, blocked, or uncertain.
2. Students do not know which smallest controllable step has the highest cross-opportunity leverage.
3. Ambiguous or stale conditions create false confidence when tools hide sources and verification dates.

## Measurement and acceptance

- Track locally testable events: onboarding started, goal selected, sample control used, source reason opened,
  onboarding completed, sample mode chosen, and profile setup started. Analytics integration is deferred.
- The sample result is deterministic and explicitly labeled; it completes without network access in under 30
  seconds for the judge path.
- Screen-reader and reduced-motion users receive the same numeric delta and explanation without relying on portal
  motion.
