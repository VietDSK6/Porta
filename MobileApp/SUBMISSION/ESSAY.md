# 300-word contest essay draft

Replace each single-token placeholder with truthful entrant information. If a replacement contains spaces, adjust
other prose so the body remains exactly 300 words. Do not submit the explanatory text or markers below.

Required facts:

- `[EDUCATION]` — degree/program and institution context.
- `[CODING_EXPERIENCE]` — truthful languages, platforms, projects, or work experience.
- `[HOBBY]` — a genuine relevant hobby or interest.
- `[MOTIVATION]` — the personal observation that led to Porta.

Count only the text between the markers:

```bash
sed -n '/^<!-- ESSAY_START -->$/,/^<!-- ESSAY_END -->$/p' SUBMISSION/ESSAY.md | sed '1d;$d' | wc -w
```

<!-- ESSAY_START -->
I am [EDUCATION], with coding experience in [CODING_EXPERIENCE]. Outside formal study, [HOBBY] has shaped how I approach difficult problems: make the evidence visible, separate assumptions from facts, and build small tools that help people decide confidently. [MOTIVATION] led me to create Porta for the Kotlin Student Coding Competition.

Students can find scholarships, internships, contests, and study programmes, yet cannot answer a question: which sequence of milestones improves their assessed options within limited time, effort, and money? Porta's Route planner orders up to four controllable steps, reevaluating checks at each step's end date. It shows improved checks, doors opened, and doors lost to deadlines. An Action Frontier supplies non-dominated moves for those routes.

Students inspect why doors need action or verification, change Route limits, and preview milestones without changing their saved profile. Every conclusion traces to encoded rules and an official source. Completing a plan never silently proves a fact; updating the actual profile requires separate confirmation, with optional evidence. Missing information stays unknown, manual checks stay manual, and soft selection criteria never become eligibility scores. Linh's fictional profile improves checks across 15 sourced doors, yet opens zero.

Porta uses Kotlin 2.4.10 and Compose Multiplatform. Shared Kotlin contains deterministic rules, scenarios, Route planning, pack validation, Room persistence, ViewModels, and interface code. Android supports profile and progress updates; JVM Desktop offers a route map, comparison workspace, and pack inspector. A JVM CLI exports, validates, evaluates, and plans with the engine. Common, JVM, and Android-host tests exercise trust boundaries and behavior.

Porta works locally without accounts, analytics, subscriptions, or an AI key. Optional drafting uses offline examples; with a personal key, Gemini may propose source-quoted rules for human review and separate pack approval. AI never decides eligibility or predicts selection. Porta gives students a source-linked map of assessed possibilities on Android and Desktop.
<!-- ESSAY_END -->
