# MobileApp — agent context pointer

You are in `MobileApp/`, one level **below** the repository root. If your session was opened on this
folder (the "MobileApp window" of the two-window workflow), the project's agent context does not
auto-load — read it now:

- **Primary context:** [`../AGENTS.md`](../AGENTS.md) — architecture, build/test commands,
  conventions, validation guardrails. Read it before making changes.
- **Skills index:** [`../skills/README.md`](../skills/README.md) — every task and guide skill
  (`../skills/<name>/SKILL.md`). Read the matching skill before doing a task it covers.
- **Progress files:** `../PROGRESS_*.md` (at the git root, **not** in `MobileApp/`) — read before
  resuming work; continue from the first unchecked item.
- **Work in parallel by default:** independent pieces (screens, models, docs) get one subagent
  each, fanned out concurrently — see "Agent Working Style" in [`../AGENTS.md`](../AGENTS.md) for
  the file-ownership and single-Gradle rules that make it safe.

All Gradle commands run from this directory (`MobileApp/`).

## Porta contest guardrails

Porta is the contest product defined in [`PORTA_PRD.md`](PORTA_PRD.md). Keep these constraints active
throughout implementation:

- Treat [`CONTEST_SCORECARD.md`](CONTEST_SCORECARD.md) as the living record of compliance, scoring evidence,
  unresolved risks, and submission readiness. Never mark an item complete without observable evidence.
- Preserve the product's defensible novelty: the Action Frontier is the decision engine; What-if and opening
  doors are its visual explanation. Do not reduce the app to a scholarship feed or a collection of sliders.
- Maintain at least two genuinely usable declared targets. The contest baseline is Android + JVM Desktop, with
  shared rule evaluation, action generation, persistence, and reusable Compose UI in shared Kotlin.
- Keep the submission solo, substantial, app-shaped, and specific to the contest period. Preserve Git history
  and the starter-kit license/provenance; never imply authorship of unchanged template code.
- Before scope changes, weekly milestones, or submission work, use the local
  [`contest-readiness`](.agents/skills/contest-readiness/SKILL.md) skill.
- For Kotlin review, milestone sign-off, and the contest's conventions criterion, use
  [`kotlin-convention-audit`](.agents/skills/kotlin-convention-audit/SKILL.md). `spotlessCheck` is necessary but
  not sufficient because several ktlint rules are intentionally disabled for Compose and project preferences.
- Re-read the official online rules before irreversible submission decisions and at final submission. The saved
  references are dated snapshots, not substitutes for the current official rules.
