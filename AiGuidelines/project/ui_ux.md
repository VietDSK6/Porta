# Porta — UI/UX Strategy

## User Flow

- Primary path: `Goal capture → clearly labeled sample profile → interactive IELTS milestone → doors change →
  source-backed explanation → minimal real profile → Action Frontier → scenario preview → active plan`.
- Alternative paths: users can stay in Sample mode, enter unknown profile values, inspect a Verify receipt, relax a
  constraint when no action is feasible, or discard every scenario without changing the real profile.
- Emotional journey notes: begin with curiosity, create a quick moment of agency when the sample portals open, slow
  down for trust/source inspection, restore focus while comparing the Frontier, and end with calm commitment rather
  than celebration or pressure.

## Screens

1. **Demo-first Onboarding**
    - Purpose: Prove the product before requesting personal academic data.
    - Key Elements: Goal chips, Sample profile badge, large IELTS control, four arched door cards, source receipt
      teaser, short progress marker.
    - Main Actions: Choose a goal, run the sample change, inspect one reason, build a real map.
    - Delight Factor: **Constellation Reveal** draws route lines from the changed profile fact to affected doors;
      opening doors use a restrained 380 ms **Portal Aperture** with a simultaneous text-state change.
2. **Doors Overview**
    - Purpose: Make current eligibility states scannable without reducing them to a match score.
    - Key Elements: State summary, atlas-grid cards, deadline markers, filter chips, source freshness, saved control,
      action recommendation strip.
    - Main Actions: Filter, save, inspect, fill missing information, move to Frontier.
    - Delight Factor: Cards enter with a short hierarchy-aware stagger; filtering redraws route lines and preserves
      spatial context instead of flashing the entire list.
3. **Opportunity Receipt**
    - Purpose: Turn a state into an auditable explanation.
    - Key Elements: Rule sections, actual/required values, hard/manual/soft/readiness badges, source citation block,
      last-checked stamp.
    - Main Actions: Expand a rule, open source, enter missing data, find related action.
    - Delight Factor: A thin “evidence thread” visually connects a rule result to its citation; expansion uses calm
      accordion motion, never a game-like reward.
4. **Action Frontier**
    - Purpose: Help the user choose among non-dominated next milestones.
    - Key Elements: Constraint controls, frontier skyline/route map, action cards, marginal door gain, duration,
      effort, cost, deadline feasibility, rank explanation.
    - Main Actions: Change constraints, compare, select, inspect filtered actions.
    - Delight Factor: **Magnet Shuffle** reorders only affected cards with 240–320 ms springs; a compact causal note
      appears beside the moved card so motion never replaces explanation.
5. **Scenario Preview**
    - Purpose: Expose exactly what one hypothetical change would do.
    - Key Elements: Persistent WHAT-IF label, before/after value, opened/unchanged/unresolved groups, portal grid,
      source-backed change receipt, Save plan button.
    - Main Actions: Adjust target, inspect deltas, save plan, discard.
    - Delight Factor: Changed doors open in a left-to-right wave while unchanged cards remain stable. Reduced-motion
      mode immediately swaps labels and announces “4 doors changed” through semantics.
6. **Active Plan**
    - Purpose: Keep one realistic milestone visible without manufacturing a streak.
    - Key Elements: Goal, due window, checklist, evidence definition, affected doors, risk note, progress indicator.
    - Main Actions: Update progress, pause, remove, preview, commit a completed fact.
    - Delight Factor: Checklist completion draws one new atlas segment; no confetti, streak loss, or guilt copy.
7. **Profile Editor**
    - Purpose: Maintain an accurate local source-of-truth profile.
    - Key Elements: Fact groups, scale/unit controls, evidence metadata, Used by count, privacy and delete/export tools.
    - Main Actions: Add, edit, delete, inspect dependencies, confirm real changes.
    - Delight Factor: Editing a fact softly highlights dependent doors as small constellation nodes before the user
      saves; nothing changes until confirmation.
8. **Desktop Planning Workspace**
    - Purpose: Use wide screens for comparison and data inspection rather than stretching mobile cards.
    - Key Elements: Navigation rail, opportunity list, comparison canvas, detail pane, Frontier controls, pack/source
      inspector, keyboard shortcuts and visible focus.
    - Main Actions: Multi-select doors, compare actions, preview scenarios, inspect pack provenance, import data.
    - Delight Factor: Selection creates subtle route lines across panes; focus movement has a clear 2 px violet/ivory
      ring and no spatially disorienting transition.

## Design Directions

### Option 1 — Portal Atlas (selected)

- Color Palette: Ink `#0D1321`, Elevated Ink `#171D2B`, Warm Ivory `#F6F1E7`, Electric Violet `#7C5CFF`, Signal
  Teal `#2DD4BF`, Verify Amber `#F4B860`, Blocked Coral `#EF6A6A`.
- Typography: Existing Poppins family. Display numbers use bold, compact spacing; headings use semibold; body copy
  uses regular weight and generous line height. Minimum mobile body size is 16 sp.
- Iconography: Rounded 2 px outlines with filled state cores; arched portal silhouettes for opportunity containers;
  small citation, clock, cost, and effort glyphs. Every status includes text and shape, not color alone.
- Animation Style: Restrained **Portal Warp / Ripple Gate**, **Constellation Reveal**, and **Magnet Shuffle**. Typical
  state transitions are 240–450 ms; reduced-motion mode uses crossfade or immediate replacement plus semantic text.
- Micro-interactions: Violet focus rings, 96% press scale, short haptic only for committing a real plan/profile
  change on supported devices, animated source thread, and stable list positions for unchanged cards.

### Option 2 — Daylight Cartography (not selected for P0)

- Color Palette: Paper `#F7F2E8`, Graphite `#20242C`, Cobalt `#3157D5`, Moss `#4E7A68`, Ochre `#C58A2B`.
- Typography: Poppins with calmer weights and wider spacing, resembling an academic field guide.
- Iconography: Fine map symbols, stamps, coordinates, and paper tabs.
- Animation Style: Hand-drawn route reveal and paper-layer crossfades.
- Micro-interactions: Source stamps and map-pin expansion. This direction is accessible and calm but less memorable
  for the contest demo than Portal Atlas.

### Option 3 — Data Observatory (Desktop reference only)

- Color Palette: Midnight `#0B1020`, Slate `#263044`, Cyan `#34D5E5`, Lime `#A5D86E`, White `#F8FAFC`.
- Typography: Poppins for UI with tabular numerals for comparison metrics.
- Iconography: Crisp geometric nodes, matrices, and rule-tree connectors.
- Animation Style: Minimal chart interpolation and focus-preserving panel transitions.
- Micro-interactions: Keyboard-driven selection, synchronized row highlighting, and compact tooltips. Use these ideas
  inside the Desktop workspace without replacing the shared Portal Atlas identity.

Accessibility applies to every direction: WCAG-aware contrast, dynamic text without clipped controls, minimum
48 dp touch targets, keyboard-complete Desktop flows, visible focus, screen-reader state summaries, and no essential
meaning conveyed by motion or color alone. P0 deliberately excludes gamification and easter eggs so engineering
effort stays on causal clarity and contest evidence.
