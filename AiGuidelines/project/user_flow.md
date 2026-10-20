# Porta — User Flow

## Screens

1. **Launch Router**
    - Purpose: Restore local state and send the user to the correct first screen without account or network gates.
    - Key Elements: Native splash handoff, local onboarding flag, bundled-pack validation, recoverable error state.
    - Expected Actions: Continue automatically; retry local initialization; open diagnostics after a pack failure.
2. **Demo-first Onboarding**
    - Purpose: Capture the user's opportunity goal and prove Porta's core mechanic before asking for profile data.
    - Key Elements: Goal choices, sample-student label, IELTS `6.5 → 7.0` control, four changing sample doors,
      source-receipt preview, four-step progress indicator.
    - Expected Actions: Choose Scholarships, Internships, Programs, Student contests, or Show me all; run the sample
      scenario; inspect one reason; continue to build a real map or keep exploring the sample.
3. **Minimal Profile Setup**
    - Purpose: Collect only facts needed by the P0 rule pack and generate the first real eligibility map.
    - Key Elements: Study level/field, GPA with scale, nationality/residency, English test and score, experience,
      project/activity evidence flags, missing/unknown option, privacy explanation.
    - Expected Actions: Enter facts, mark unknown values, review units/scales, save locally, or return to sample mode.
4. **Doors Overview**
    - Purpose: Provide a fast, honest map of the user's current opportunity states.
    - Key Elements: Open/Has gaps/Verify/Unavailable/Closed summary, filter chips, search, saved state, deadline,
      proximity label, source freshness, recommended-next-action strip.
    - Expected Actions: Filter, search, save, open a door, edit a missing fact, or move to the Action Frontier.
5. **Opportunity Receipt**
    - Purpose: Explain one opportunity state clause by clause without mixing eligibility with competitiveness.
    - Key Elements: Passed and failed hard rules, missing facts, manual checks, soft criteria, application-readiness
      checklist, actual versus required value, official source, section, cycle, and last-checked date.
    - Expected Actions: Inspect a rule, open the official source, fill a missing fact, save the opportunity, or ask
      Porta to show actionable paths for a failed rule.
6. **Action Frontier**
    - Purpose: Rank the few realistic milestones that create the most verified new access under current constraints.
    - Key Elements: Time horizon, effort ceiling, cost band, saved-door priority, non-dominated action cards, newly
      opened door count, affected deadlines, duration/effort/cost, rank explanation, no-feasible-action state.
    - Expected Actions: Change constraints, compare actions, select a milestone, or inspect why an action was filtered.
7. **Scenario Preview**
    - Purpose: Show the exact causal effect of one hypothetical milestone without changing the actual profile.
    - Key Elements: Persistent SAMPLE/WHAT-IF banner, before/after value control, portal state transitions, opened,
      unchanged, unresolved and closed groups, source-linked explanation, deadline feasibility, text-only summary.
    - Expected Actions: Adjust the target, inspect changed doors, save as a plan, return to the Frontier, or discard.
8. **Active Plan**
    - Purpose: Turn the selected milestone into a small, honest next-step plan.
    - Key Elements: Target fact, expected duration, effort/cost band, evidence definition, affected saved doors,
      deadline risk, progress checklist, last-updated timestamp.
    - Expected Actions: Mark checklist progress, pause or remove the plan, preview impact again, and explicitly update
      the actual profile only after completing the milestone.
9. **Profile Editor**
    - Purpose: Maintain the source-of-truth profile and expose where each fact is used.
    - Key Elements: Fact groups, units/scales, self-reported/evidence-backed state, used-by count, data export/delete,
      sample-versus-real distinction.
    - Expected Actions: Add, edit, delete, export, or clear facts; inspect dependent rules; confirm a completed change.
10. **Pack Inspector and Import Diagnostics**
    - Purpose: Make the curated data and engine boundary inspectable, especially on Desktop.
    - Key Elements: Pack version, record count, publishers, verification dates, typed rule tree, source link,
      validation errors/warnings, import and rollback controls.
    - Expected Actions: Inspect provenance, import a pack, reject invalid data, export diagnostics, or restore the
      bundled pack. Form-based opportunity authoring is P1, not P0.

## User Flow

1. **New-user judge path:** `App launch → Demo-first Onboarding: choose goal → Sample scenario → Drag IELTS 6.5
   to 7.0 → Four sample doors open → Inspect one source-backed reason → Build my map → Minimal Profile Setup →
   Doors Overview → Action Frontier → Scenario Preview → Save as Active Plan`. The goal is achieved when the user
   has a source-linked, feasible next milestone and knows exactly which doors it may open.
2. **New user who is not ready to enter data:** `App launch → Onboarding → Explore sample → Doors Overview in
   clearly labeled Sample mode → Action Frontier → Scenario Preview`. The app keeps a persistent sample banner and
   offers `Build my map`; sample facts never become an actual profile.
3. **Returning-user daily path:** `App launch → Doors Overview → Recommended action strip or Frontier tab → Active
   Plan → Update progress`. Completing a checklist never changes profile facts automatically.
4. **Commit actual growth:** `Active Plan → Mark milestone complete → Review evidence/value → Confirm update →
   Profile Editor writes actual fact → Engine reevaluates → Doors Overview shows a receipt-backed state delta`.
5. **Explain a blocked door:** `Doors Overview → Opportunity Receipt → Failed hard rule → Show paths → Action
   Frontier filtered to relevant milestones → Scenario Preview`.
6. **Missing information:** `Doors Overview: Verify → Opportunity Receipt → Missing fact/manual check → Enter known
   fact or open official source`. If unresolved, the door remains Verify; it never becomes Closed or Unavailable by
   assumption.
7. **No feasible action:** `Action Frontier → Empty-state explanation → Show filtered reasons → Relax one explicit
   constraint or return to Doors`. Porta never invents an action for an immutable fact or a missed deadline.
8. **Invalid/stale data pack:** `Launch or Pack Inspector → Validation diagnostics → Keep current bundled pack →
   Import corrected pack`. A failed import is atomic and does not damage the active catalog.
9. **Desktop planning path:** `App launch → Doors workspace → Multi-select doors → Action Frontier comparison →
   Scenario Preview in detail pane → Pack Inspector`. Keyboard focus and shortcuts cover every P0 action.
10. **Privacy reset:** `Profile Editor → Data controls → Export metadata (optional) → Confirm local delete → Launch
    Router → Demo-first Onboarding`.

## Navigation Structure

- Primary Navigation: Android uses a three-item bottom bar: **Doors**, **Frontier**, and **Profile**. Desktop uses
  the same destinations in a left navigation rail with a master-detail workspace. The current Active Plan appears
  as a pinned card in Frontier instead of a fourth top-level destination.
- Secondary Navigation: Opportunity cards push **Opportunity Receipt**; Frontier cards open **Scenario Preview** as
  a full screen on compact widths and a detail pane/dialog on Desktop; Profile links to data controls; Desktop
  provides **Pack Inspector** through the rail utility section. Official sources open only through explicit links.
- Back behavior: Back closes transient previews first, then returns to the previous destination; switching top-level
  tabs preserves each tab's local back stack. No route requires authentication or payment.
- Deep-link policy: P0 supports internal opportunity ids from saved state/imported packs only. External universal
  links are deferred.
